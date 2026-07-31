package com.nyquistdata.grpc.server.server.handlers;

import com.nyquistdata.grpc.api.exceptions.GrpcException;
import com.nyquistdata.grpc.api.remote.impl.request.RequestMeta;
import com.nyquistdata.grpc.api.remote.impl.request.ServerLoaderInfoRequest;
import com.nyquistdata.grpc.api.remote.impl.response.ServerLoaderInfoResponse;
import com.nyquistdata.grpc.server.server.connection.ConnectionManager;
import com.nyquistdata.grpc.server.server.filters.RequestFilters;
import lombok.extern.slf4j.Slf4j;


/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version: 1.0.0
 * @date: 2026/7/27
 * @description: request handler to handle server load info
 */
@Slf4j
public class ServerLoaderInfoRequestHandler extends RequestHandler<ServerLoaderInfoRequest, ServerLoaderInfoResponse> {
    private final ConnectionManager connectionManager;

    public ServerLoaderInfoRequestHandler(RequestFilters filters, RequestHandlerRegistry requestHandlerRegistry, ConnectionManager connectionManager) {
        super(filters, requestHandlerRegistry);
        this.connectionManager = connectionManager;
    }

    @Override
    public ServerLoaderInfoResponse handle(ServerLoaderInfoRequest request, RequestMeta meta) throws GrpcException {
        ServerLoaderInfoResponse serverLoaderInfoResponse = new ServerLoaderInfoResponse();
        serverLoaderInfoResponse.putMetricsValue("conCount", String.valueOf(connectionManager.currentClientsCount()));
        serverLoaderInfoResponse.putMetricsValue("load", "2.0");
        serverLoaderInfoResponse.putMetricsValue("cpu", "10%");

        return serverLoaderInfoResponse;
    }
}
