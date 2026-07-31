package com.nyquistdata.grpc.server.server.acceptors;

import com.nyquistdata.grpc.api.BiRequestStreamGrpc;
import com.nyquistdata.grpc.api.Payload;
import com.nyquistdata.grpc.api.remote.impl.request.ConnectionSetupRequest;
import com.nyquistdata.grpc.api.remote.impl.response.Response;
import com.nyquistdata.grpc.api.utils.GrpcUtils;
import com.nyquistdata.grpc.server.domains.constants.GrpcServerConstants;
import com.nyquistdata.grpc.api.domains.enums.ConnectionType;
import com.nyquistdata.grpc.server.server.connection.ConnectionManager;
import com.nyquistdata.grpc.server.server.connection.ConnectionMeta;
import com.nyquistdata.grpc.server.server.connection.GrpcConnection;
import com.nyquistdata.grpc.server.server.connection.listeners.support.RpcAckCallbackSynchronizer;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version: 1.0.0
 * @date: 2026/7/27
 * @description: grpc bi stream request
 */
@Slf4j
public class GrpcBiStreamRequestAcceptor extends BiRequestStreamGrpc.BiRequestStreamImplBase {
    private final ConnectionManager connectionManager;

    public GrpcBiStreamRequestAcceptor(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    private void traceDetail(Payload payload) {
        String connId = GrpcServerConstants.CONTEXT_KEY_CONN_ID.get();
        try {
            log.info("[{}]Bi stream request receive, meta={},body={}", connId,
                    payload.getMetadata().toByteString().toStringUtf8(),
                    payload.getBody().toByteString().toStringUtf8());
        } catch (Throwable throwable) {
            log.error("[{}]Bi stream request error,payload={},error={}", connId,
                    payload.toByteString().toStringUtf8(), throwable);
        }
    }

    @Override
    public StreamObserver<Payload> requestBiStream(StreamObserver<Payload> responseObserver) {
        StreamObserver<Payload> streamObserver = new StreamObserver<Payload>() {

            final String connectionId = GrpcServerConstants.CONTEXT_KEY_CONN_ID.get();

            final Integer localPort = GrpcServerConstants.CONTEXT_KEY_CONN_LOCAL_PORT.get();

            final int remotePort = GrpcServerConstants.CONTEXT_KEY_CONN_REMOTE_PORT.get();

            String remoteIp = GrpcServerConstants.CONTEXT_KEY_CONN_REMOTE_IP.get();

            String clientIp = "";

            @Override
            public void onNext(Payload payload) {

                clientIp = payload.getMetadata().getClientIp();
                traceDetail(payload);

                Object parseObj;
                try {
                    parseObj = GrpcUtils.parse(payload);
                } catch (Throwable throwable) {
                    log
                            .warn("[{}]Grpc request bi stream,payload parse error={}", connectionId, throwable);
                    return;
                }

                if (parseObj == null) {
                    log
                            .warn("[{}]Grpc request bi stream,payload parse null ,body={},meta={}", connectionId,
                                    payload.getBody().getValue().toStringUtf8(), payload.getMetadata());
                    return;
                }
                if (parseObj instanceof ConnectionSetupRequest) {
                    ConnectionSetupRequest setUpRequest = (ConnectionSetupRequest) parseObj;
                    Map<String, String> labels = setUpRequest.getLabels();
                    String appName = "-";
                    ConnectionMeta metaInfo = new ConnectionMeta(connectionId, payload.getMetadata().getClientIp(),
                            remoteIp, remotePort, localPort, ConnectionType.GRPC.getType(),
                            setUpRequest.getClientVersion(), appName, setUpRequest.getLabels());
                    metaInfo.setTenant(setUpRequest.getTenant());
                    GrpcConnection connection = new GrpcConnection(metaInfo, responseObserver,
                            GrpcServerConstants.CONTEXT_KEY_CHANNEL.get());
                    // null if supported
                    if (setUpRequest.getAbilityTable() != null) {
                        // map to table
                        connection.setAbilityTable(setUpRequest.getAbilityTable());
                    }
                    boolean rejectSdkOnStarting = false;

                    if (rejectSdkOnStarting || !connectionManager.register(connectionId, connection)) {
                        try {
                            log.warn("[{}]Connection register fail,reason:{}", connectionId,
                                    rejectSdkOnStarting ? " server is not started" : " server is over limited.");
                            connection.close();
                        } catch (Exception e) {
                            log
                                    .warn("[{}]Send connect reset request error,error={}", connectionId, e);
                        }
                    }

                } else if (parseObj instanceof Response) {
                    Response response = (Response) parseObj;
                    log
                            .warn("[{}]Receive response of server request  ,response={}", connectionId, response);
                    RpcAckCallbackSynchronizer.ackNotify(connectionId, response);
                    connectionManager.refreshActiveTime(connectionId);
                } else {
                    log
                            .warn("[{}]Grpc request bi stream,unknown payload receive ,parseObj={}", connectionId,
                                    parseObj);
                }

            }

            @Override
            public void onError(Throwable t) {
                log.warn("[{}]Bi stream on error,error={}", connectionId, t);

                if (responseObserver instanceof ServerCallStreamObserver) {
                    ServerCallStreamObserver serverCallStreamObserver = ((ServerCallStreamObserver) responseObserver);
                    if (serverCallStreamObserver.isCancelled()) {
                    } else {
                        try {
                            serverCallStreamObserver.onCompleted();
                        } catch (Throwable throwable) {
                        }
                    }
                }

            }

            @Override
            public void onCompleted() {
                log.warn("[{}]Bi stream on completed", connectionId);
                if (responseObserver instanceof ServerCallStreamObserver) {
                    ServerCallStreamObserver serverCallStreamObserver = ((ServerCallStreamObserver) responseObserver);
                    if (serverCallStreamObserver.isCancelled()) {
                    } else {
                        try {
                            serverCallStreamObserver.onCompleted();
                        } catch (Throwable throwable) {
                        }

                    }
                }
            }
        };

        return streamObserver;
    }
}
