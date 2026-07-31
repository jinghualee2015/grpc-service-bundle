package com.nyquistdata.grpc.server.server.handlers;

import com.nyquistdata.grpc.api.exceptions.GrpcException;
import com.nyquistdata.grpc.api.remote.impl.request.RequestMeta;
import com.nyquistdata.grpc.api.remote.impl.request.ServerReloadRequest;
import com.nyquistdata.grpc.api.remote.impl.response.ServerReloadResponse;
import com.nyquistdata.grpc.server.server.connection.ConnectionManager;
import com.nyquistdata.grpc.server.server.filters.RequestFilters;
import lombok.extern.slf4j.Slf4j;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version: 1.0.0
 * @date: 2026/7/27
 * @description: server reload request handler
 */
@Slf4j
public class ServerReloaderRequestHandler extends RequestHandler<ServerReloadRequest, ServerReloadResponse> {
    private final ConnectionManager connectionManager;

    public ServerReloaderRequestHandler(RequestFilters filters, RequestHandlerRegistry requestHandlerRegistry, ConnectionManager connectionManager) {
        super(filters, requestHandlerRegistry);
        this.connectionManager = connectionManager;
    }

    @Override
    public ServerReloadResponse handle(ServerReloadRequest request, RequestMeta meta) throws GrpcException {
        ServerReloadResponse response = new ServerReloadResponse();
        log.info("server reload request receive,reload count={},redirectServer={},requestIp={}",
                request.getReloadCount(), request.getReloadServer(), meta.getClientIp());
        int reloadCount = request.getReloadCount();
        int sdkCount = connectionManager.currentClientsCount();
        if (sdkCount <= reloadCount) {
            response.setMessage("ignore");
        } else {
            reloadCount = (int) Math.max(reloadCount, sdkCount);
            connectionManager.loadCount(reloadCount, request.getReloadServer());
            response.setMessage("ok");
        }
        return response;
    }
}
