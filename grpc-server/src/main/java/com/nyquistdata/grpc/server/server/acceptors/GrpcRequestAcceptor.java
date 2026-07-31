package com.nyquistdata.grpc.server.server.acceptors;

import com.nyquistdata.grpc.api.Payload;
import com.nyquistdata.grpc.api.RequestGrpc;
import com.nyquistdata.grpc.api.exceptions.GrpcException;
import com.nyquistdata.grpc.api.remote.RpcScheduledExecutor;
import com.nyquistdata.grpc.api.remote.impl.request.Request;
import com.nyquistdata.grpc.api.remote.impl.request.RequestMeta;
import com.nyquistdata.grpc.api.remote.impl.request.ServerCheckRequest;
import com.nyquistdata.grpc.api.remote.impl.response.ErrorResponse;
import com.nyquistdata.grpc.api.remote.impl.response.Response;
import com.nyquistdata.grpc.api.remote.impl.response.ServerCheckResponse;
import com.nyquistdata.grpc.api.utils.GrpcUtils;
import com.nyquistdata.grpc.api.utils.StringUtils;
import com.nyquistdata.grpc.server.domains.constants.GrpcServerConstants;
import com.nyquistdata.grpc.server.server.connection.Connection;
import com.nyquistdata.grpc.server.server.connection.ConnectionManager;
import com.nyquistdata.grpc.server.server.context.BasicContext;
import com.nyquistdata.grpc.server.server.context.RequestContext;
import com.nyquistdata.grpc.server.server.context.RequestContextHolder;
import com.nyquistdata.grpc.server.server.handlers.RequestHandler;
import com.nyquistdata.grpc.server.server.handlers.RequestHandlerRegistry;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version: 1.0.0
 * @date: 2026/7/26
 * @description: rpc request acceptor for grpc
 */
@Slf4j
public class GrpcRequestAcceptor extends RequestGrpc.RequestImplBase {
    private final RequestHandlerRegistry requestHandlerRegistry;
    private final ConnectionManager connectionManager;

    public GrpcRequestAcceptor(RequestHandlerRegistry requestHandlerRegistry, ConnectionManager connectionManager) {
        this.requestHandlerRegistry = requestHandlerRegistry;
        this.connectionManager = connectionManager;
    }


    @Override
    public void request(Payload grpcRequest, StreamObserver<Payload> responseObserver) {

        String type = grpcRequest.getMetadata().getType();
        long startTime = System.nanoTime();

        // server check.
        if (ServerCheckRequest.class.getSimpleName().equals(type)) {
            Payload serverCheckResponseP = GrpcUtils.convert(new ServerCheckResponse(GrpcServerConstants.CONTEXT_KEY_CONN_ID.get(), true));
            responseObserver.onNext(serverCheckResponseP);
            responseObserver.onCompleted();
            return;
        }

        RequestHandler requestHandler = requestHandlerRegistry.getByRequestType(type);
        //no handler found.
        if (requestHandler == null) {
            log.warn(String.format("[%s] No handler for request type : %s :", "grpc", type));
            Payload payloadResponse = GrpcUtils
                    .convert(ErrorResponse.build(GrpcException.NO_HANDLER, "RequestHandler Not Found"));
            responseObserver.onNext(payloadResponse);
            responseObserver.onCompleted();
            return;
        }

        //check connection status.
        String connectionId = GrpcServerConstants.CONTEXT_KEY_CONN_ID.get();
        boolean requestValid = connectionManager.checkValid(connectionId);
        if (!requestValid) {
            log
                    .warn("[{}] Invalid connection Id ,connection [{}] is un registered ,", "grpc", connectionId);
            Payload payloadResponse = GrpcUtils
                    .convert(ErrorResponse.build(GrpcException.UN_REGISTER, "Connection is unregistered."));
            responseObserver.onNext(payloadResponse);
            responseObserver.onCompleted();
            return;
        }

        Object parseObj = null;
        try {
            parseObj = GrpcUtils.parse(grpcRequest);
        } catch (Exception e) {
            log
                    .warn("[{}] Invalid request receive from connection [{}] ,error={}", "grpc", connectionId, e);
            Payload payloadResponse = GrpcUtils.convert(ErrorResponse.build(GrpcException.BAD_GATEWAY, e.getMessage()));
            responseObserver.onNext(payloadResponse);
            responseObserver.onCompleted();
            return;
        }

        if (parseObj == null) {
            log.warn("[{}] Invalid request receive  ,parse request is null", connectionId);
            Payload payloadResponse = GrpcUtils
                    .convert(ErrorResponse.build(GrpcException.BAD_GATEWAY, "Invalid request"));
            responseObserver.onNext(payloadResponse);
            responseObserver.onCompleted();
            return;
        }

        if (!(parseObj instanceof Request)) {
            log
                    .warn("[{}] Invalid request receive  ,parsed payload is not a request,parseObj={}", connectionId,
                            parseObj);
            Payload payloadResponse = GrpcUtils
                    .convert(ErrorResponse.build(GrpcException.BAD_GATEWAY, "Invalid request"));
            responseObserver.onNext(payloadResponse);
            responseObserver.onCompleted();
            return;
        }

        Request request = (Request) parseObj;
        try {
            Connection connection = connectionManager.getConnection(GrpcServerConstants.CONTEXT_KEY_CONN_ID.get());
            RequestMeta requestMeta = new RequestMeta();
            requestMeta.setClientIp(connection.getMetaInfo().getClientIp());
            requestMeta.setConnectionId(GrpcServerConstants.CONTEXT_KEY_CONN_ID.get());
            requestMeta.setClientVersion(connection.getMetaInfo().getVersion());
            requestMeta.setLabels(connection.getMetaInfo().getLabels());
            requestMeta.setAbilityTable(connection.getAbilityTable());
            connectionManager.refreshActiveTime(requestMeta.getConnectionId());
            prepareRequestContext(request, requestMeta, connection);
            Response response = requestHandler.handleRequest(request, requestMeta);
            Payload payloadResponse = GrpcUtils.convert(response);
            if (response.getErrorCode() == GrpcException.OVER_THRESHOLD) {
                RpcScheduledExecutor.CONTROL_SCHEDULER.schedule(() -> {
                    responseObserver.onNext(payloadResponse);
                    responseObserver.onCompleted();
                }, 1000L, TimeUnit.MILLISECONDS);
            } else {
                responseObserver.onNext(payloadResponse);
                responseObserver.onCompleted();
            }
        } catch (Throwable e) {
            log
                    .error("[{}] Fail to handle request from connection [{}] ,error message :{}", "grpc", connectionId,
                            e);
            Payload payloadResponse = GrpcUtils.convert(ErrorResponse.build(e));
            responseObserver.onNext(payloadResponse);
            responseObserver.onCompleted();
        } finally {
            RequestContextHolder.removeContext();
        }

    }

    private void prepareRequestContext(Request request, RequestMeta requestMeta, Connection connection) {
        RequestContext requestContext = RequestContextHolder.getContext();
        requestContext.setRequestId(request.getRequestId());
        requestContext.getBasicContext().setUserAgent(requestMeta.getClientVersion());
        requestContext.getBasicContext().setRequestProtocol(BasicContext.GRPC_PROTOCOL);
        requestContext.getBasicContext().setRequestTarget(request.getClass().getSimpleName());
        String app = connection.getMetaInfo().getAppName();
        requestContext.getBasicContext().setApp(app);
        requestContext.getBasicContext().getAddressContext().setRemoteIp(connection.getMetaInfo().getRemoteIp());
        requestContext.getBasicContext().getAddressContext().setRemotePort(connection.getMetaInfo().getRemotePort());
        requestContext.getBasicContext().getAddressContext().setSourceIp(connection.getMetaInfo().getClientIp());
    }
}
