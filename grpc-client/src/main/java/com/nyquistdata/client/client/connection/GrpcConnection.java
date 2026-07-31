package com.nyquistdata.client.client.connection;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.nyquistdata.client.client.RpcClient;
import com.nyquistdata.grpc.api.Payload;
import com.nyquistdata.grpc.api.RequestGrpc;
import com.nyquistdata.grpc.api.domains.enums.ResponseCode;
import com.nyquistdata.grpc.api.exceptions.GrpcException;
import com.nyquistdata.grpc.api.remote.RequestCallBack;
import com.nyquistdata.grpc.api.remote.RequestFuture;
import com.nyquistdata.grpc.api.remote.RpcScheduledExecutor;
import com.nyquistdata.grpc.api.remote.impl.request.Request;
import com.nyquistdata.grpc.api.remote.impl.response.ErrorResponse;
import com.nyquistdata.grpc.api.remote.impl.response.Response;
import com.nyquistdata.grpc.api.utils.GrpcUtils;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CancellationException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version: 1.0.0
 * @date: 2026/7/29
 * @description: gRPC connection
 */
public class GrpcConnection extends Connection {
    private static final Logger log = LoggerFactory.getLogger(GrpcConnection.class);
    protected ManagedChannel channel;

    Executor executor;

    protected RequestGrpc.RequestFutureStub grpcFutureServiceStub;

    protected StreamObserver<Payload> payloadStreamObserver;

    public GrpcConnection(RpcClient.ServerInfo serverInfo, Executor executor) {
        super(serverInfo);
        this.executor = executor;
    }

    @Override
    public Response request(Request request, long timeoutMills) throws GrpcException {
        Payload grpcRequest = GrpcUtils.convert(request);
        ListenableFuture<Payload> requestFuture = grpcFutureServiceStub.request(grpcRequest);
        Payload grpcResponse;
        try {
            if (timeoutMills <= 0) {
                grpcResponse = requestFuture.get();
            } else {
                grpcResponse = requestFuture.get(timeoutMills, TimeUnit.MILLISECONDS);
            }
        } catch (Exception e) {
            throw new GrpcException(GrpcException.SERVER_ERROR, e);
        }

        return (Response) GrpcUtils.parse(grpcResponse);
    }

    @Override
    public RequestFuture requestFuture(Request request) throws GrpcException {
        Payload grpcRequest = GrpcUtils.convert(request);

        final ListenableFuture<Payload> requestFuture = grpcFutureServiceStub.request(grpcRequest);
        return new RequestFuture() {

            @Override
            public boolean isDone() {
                return requestFuture.isDone();
            }

            @Override
            public Response get() throws Exception {
                Payload grpcResponse = requestFuture.get();
                Response response = (Response) GrpcUtils.parse(grpcResponse);
                if (response instanceof ErrorResponse) {
                    throw new GrpcException(response.getErrorCode(), response.getMessage());
                }
                return response;
            }

            @Override
            public Response get(long timeout) throws Exception {
                Payload grpcResponse = requestFuture.get(timeout, TimeUnit.MILLISECONDS);
                Response response = (Response) GrpcUtils.parse(grpcResponse);
                if (response instanceof ErrorResponse) {
                    throw new GrpcException(response.getErrorCode(), response.getMessage());
                }
                return response;
            }
        };
    }

    @Override
    public void asyncRequest(Request request, final RequestCallBack requestCallBack) throws GrpcException {
        Payload grpcRequest = GrpcUtils.convert(request);
        ListenableFuture<Payload> requestFuture = grpcFutureServiceStub.request(grpcRequest);
        Futures.addCallback(requestFuture, new FutureCallback<Payload>() {
            @Override
            public void onSuccess(Payload result) {
                Response response = (Response) GrpcUtils.parse(result);
                if (response != null) {
                    if (response instanceof ErrorResponse) {
                        requestCallBack.onException(new GrpcException(response.getErrorCode(), response.getMessage()));
                    } else {
                        requestCallBack.onResponse(response);
                    }
                } else {
                    requestCallBack.onException(new GrpcException(ResponseCode.FAIL.getCode(), "response is null"));
                }
            }

            @Override
            public void onFailure(Throwable t) {
                if (t instanceof CancellationException) {
                    requestCallBack.onException(
                            new TimeoutException("Timeout after " + requestCallBack.getTimeout() + " milliseconds."));
                } else {
                    requestCallBack.onException(t);
                }
            }
        }, requestCallBack.getExecutor() != null ? requestCallBack.getExecutor() : this.executor);

        ListenableFuture<Payload> payloadListenableFuture = Futures.withTimeout(requestFuture, requestCallBack.getTimeout(), TimeUnit.MILLISECONDS, RpcScheduledExecutor.TIMEOUT_SCHEDULER);
        payloadListenableFuture.addListener(() -> {
            log.warn("task {} executed time out.", request.getRequestId());
        }, RpcScheduledExecutor.TIMEOUT_SCHEDULER);
    }

    @Override
    public void close() {
        if (null != this.payloadStreamObserver) {
            try {
                payloadStreamObserver.onCompleted();
            } catch (Throwable ignore) {
                //
            }
        }
        if (null != this.channel && !this.channel.isShutdown()) {
            try {
                this.channel.shutdownNow();
            } catch (Throwable ignore) {
                //
            }
        }
    }

    public void sendResponse(Response response) {
        Payload convert = GrpcUtils.convert(response);
        payloadStreamObserver.onNext(convert);
    }

    public void sendRequest(Request request) {
        Payload convert = GrpcUtils.convert(request);
        payloadStreamObserver.onNext(convert);
    }


    /**
     * Getter method for property <tt>channel</tt>.
     *
     * @return property value of channel
     */
    public ManagedChannel getChannel() {
        return channel;
    }

    /**
     * Setter method for property <tt>channel</tt>.
     *
     * @param channel value to be assigned to property channel
     */
    public void setChannel(ManagedChannel channel) {
        this.channel = channel;
    }

    /**
     * Getter method for property <tt>grpcFutureServiceStub</tt>.
     *
     * @return property value of grpcFutureServiceStub
     */
    public RequestGrpc.RequestFutureStub getGrpcFutureServiceStub() {
        return grpcFutureServiceStub;
    }

    /**
     * Setter method for property <tt>grpcFutureServiceStub</tt>.
     *
     * @param grpcFutureServiceStub value to be assigned to property grpcFutureServiceStub
     */
    public void setGrpcFutureServiceStub(RequestGrpc.RequestFutureStub grpcFutureServiceStub) {
        this.grpcFutureServiceStub = grpcFutureServiceStub;
    }

    /**
     * Getter method for property <tt>payloadStreamObserver</tt>.
     *
     * @return property value of payloadStreamObserver
     */
    public StreamObserver<Payload> getPayloadStreamObserver() {
        return payloadStreamObserver;
    }

    /**
     * Setter method for property <tt>payloadStreamObserver</tt>.
     *
     * @param payloadStreamObserver value to be assigned to property payloadStreamObserver
     */
    public void setPayloadStreamObserver(StreamObserver<Payload> payloadStreamObserver) {
        this.payloadStreamObserver = payloadStreamObserver;
    }
}
