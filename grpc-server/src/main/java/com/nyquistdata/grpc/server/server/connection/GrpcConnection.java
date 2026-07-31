package com.nyquistdata.grpc.server.server.connection;

import com.nyquistdata.grpc.api.Payload;
import com.nyquistdata.grpc.api.exceptions.ConnectionAlreadyClosedException;
import com.nyquistdata.grpc.api.exceptions.GrpcException;
import com.nyquistdata.grpc.api.exceptions.GrpcRuntimeException;
import com.nyquistdata.grpc.api.remote.DefaultRequestFuture;
import com.nyquistdata.grpc.api.remote.RequestCallBack;
import com.nyquistdata.grpc.api.remote.RequestFuture;
import com.nyquistdata.grpc.api.remote.impl.request.Request;
import com.nyquistdata.grpc.api.remote.impl.response.Response;
import com.nyquistdata.grpc.api.remote.support.PushAckIdGenerator;
import com.nyquistdata.grpc.api.utils.GrpcUtils;
import com.nyquistdata.grpc.server.server.connection.listeners.support.RpcAckCallbackSynchronizer;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.shaded.io.netty.channel.Channel;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version: 1.0.0
 * @date: 2026/7/26
 * @description: grpc connection
 */
@Slf4j
public class GrpcConnection  extends Connection{
    private StreamObserver streamObserver;

    private Channel channel;;

    public GrpcConnection(ConnectionMeta metaInfo, StreamObserver streamObserver, Channel channel) {
        super(metaInfo);
        this.streamObserver = streamObserver;
        this.channel = channel;
    }

    /**
     * send request without ack.
     *
     * @param request request data.
     * @throws com.nyquistdata.grpc.api.exceptions.GrpcRuntimeException NacosException
     */
    public void sendRequestNoAck(Request request) throws GrpcRuntimeException {
        Future<Boolean> executeFuture = this.channel.eventLoop().submit(() -> {
            //StreamObserver#onNext() is not thread-safe,synchronized is required to avoid direct memory leak.
            synchronized (streamObserver) {
                try {
                    Payload payload = GrpcUtils.convert(request);
                    traceIfNecessary(payload);
                    streamObserver.onNext(payload);
                    return true;
                } catch (Throwable e) {
                    if (e instanceof StatusRuntimeException) {
                        throw new ConnectionAlreadyClosedException(e);
                    } else if (e instanceof IllegalStateException) {
                        throw new ConnectionAlreadyClosedException(e);
                    }
                    throw new GrpcRuntimeException(GrpcException.SERVER_ERROR, e);
                }
            }
        });
        try {
            executeFuture.get();
        } catch (Throwable throwable) {
            if (throwable instanceof ExecutionException && throwable.getCause() != null
                    && throwable.getCause() instanceof GrpcRuntimeException) {
                throw (GrpcRuntimeException) throwable.getCause();
            }
            throw new GrpcRuntimeException(GrpcException.SERVER_ERROR, throwable);
        }
    }

    private void traceIfNecessary(Payload payload) {
        String connectionId = null;
        if (this.isTraced()) {
            try {
                connectionId = getMetaInfo().getConnectionId();
                log.info("[{}]Send request to client ,payload={}", connectionId,
                        payload.toByteString().toStringUtf8());
            } catch (Throwable throwable) {
                log.warn("[{}]Send request to client trace error, ,error={}", connectionId,
                        throwable);
            }
        }
    }

    private DefaultRequestFuture sendRequestInner(Request request, RequestCallBack callBack) throws GrpcException {
        final String requestId = String.valueOf(PushAckIdGenerator.getNextId());
        request.setRequestId(requestId);

        DefaultRequestFuture defaultPushFuture = new DefaultRequestFuture(getMetaInfo().getConnectionId(), requestId,
                callBack, () -> RpcAckCallbackSynchronizer.clearFuture(getMetaInfo().getConnectionId(), requestId));

        RpcAckCallbackSynchronizer.syncCallback(getMetaInfo().getConnectionId(), requestId, defaultPushFuture);
        sendRequestNoAck(request);
        return defaultPushFuture;
    }

    @Override
    public Response request(Request request, long timeoutMills) throws GrpcException {
        DefaultRequestFuture pushFuture = sendRequestInner(request, null);
        try {
            return pushFuture.get(timeoutMills);
        } catch (Exception e) {
            throw new GrpcException(GrpcException.SERVER_ERROR, e);
        } finally {
            RpcAckCallbackSynchronizer.clearFuture(getMetaInfo().getConnectionId(), pushFuture.getRequestId());
        }
    }

    @Override
    public RequestFuture requestFuture(Request request) throws GrpcException {
        return sendRequestInner(request, null);
    }

    @Override
    public void asyncRequest(Request request, RequestCallBack requestCallBack) throws GrpcException {
        sendRequestInner(request, requestCallBack);
    }

    @Override
    public void close() {
        String connectionId = null;

        try {
            connectionId = getMetaInfo().getConnectionId();

            if (isTraced()) {
                log.warn("[{}] try to close connection ", connectionId);
            }

            try {
                closeBiStream();
            } catch (Throwable e) {
                log.warn("[{}] connection  close bi stream exception  : {}", connectionId, e);
            }
            channel.close();

        } catch (Exception e) {
            log.warn("[{}] connection  close exception  : {}", connectionId, e);
        }
    }

    private void closeBiStream() {
        if (streamObserver instanceof ServerCallStreamObserver) {
            ServerCallStreamObserver serverCallStreamObserver = ((ServerCallStreamObserver) streamObserver);
            if (!serverCallStreamObserver.isCancelled()) {
                serverCallStreamObserver.onCompleted();
            }
        }
    }

    @Override
    public boolean isConnected() {
        return channel != null && channel.isOpen() && channel.isActive();
    }
}
