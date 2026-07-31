package com.nyquistdata.grpc.server.server.filters;

import com.nyquistdata.grpc.api.exceptions.GrpcException;
import com.nyquistdata.grpc.api.remote.impl.request.Request;
import com.nyquistdata.grpc.api.remote.impl.request.RequestMeta;
import com.nyquistdata.grpc.api.remote.impl.response.Response;
import lombok.extern.slf4j.Slf4j;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version: 1.0.0
 * @date: 2026/7/26
 * @description: auth filter
 */
@Slf4j
public class RemoteRequestAuthFilter extends AbstractRequestFilter {
    public RemoteRequestAuthFilter(RequestFilters requestFilters) {
        super(requestFilters);
    }

    @Override
    public Response filter(Request request, RequestMeta meta, Class handlerClazz) throws GrpcException {
        log.info("[RemoteRequestAuthFilter] request:{}, meta:{}, handlerClazz:{}", request, meta, handlerClazz);
        return null;
    }
}
