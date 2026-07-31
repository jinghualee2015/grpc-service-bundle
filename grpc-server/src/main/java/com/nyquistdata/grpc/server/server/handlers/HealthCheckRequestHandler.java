package com.nyquistdata.grpc.server.server.handlers;

import com.alibaba.fastjson.JSON;
import com.nyquistdata.grpc.api.exceptions.GrpcException;
import com.nyquistdata.grpc.api.remote.impl.request.HealthCheckRequest;
import com.nyquistdata.grpc.api.remote.impl.request.RequestMeta;
import com.nyquistdata.grpc.api.remote.impl.response.HealthCheckResponse;
import com.nyquistdata.grpc.server.server.filters.RequestFilters;
import lombok.extern.slf4j.Slf4j;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version: 1.0.0
 * @date: 2026/7/27
 * @description: push response to clients
 */
@Slf4j
public class HealthCheckRequestHandler extends RequestHandler<HealthCheckRequest, HealthCheckResponse> {
    public HealthCheckRequestHandler(RequestFilters filters, RequestHandlerRegistry requestHandlerRegistry) {
        super(filters, requestHandlerRegistry);
    }

    @Override
    public HealthCheckResponse handle(HealthCheckRequest request, RequestMeta meta) throws GrpcException {
        log.info("[HealthCheck] received health check request:{},meta:{}", JSON.toJSONString(request), JSON.toJSONString(meta));
        return new HealthCheckResponse();
    }
}
