package com.nyquistdata.grpc.server.server.handlers;

import com.nyquistdata.grpc.api.exceptions.GrpcException;
import com.nyquistdata.grpc.api.remote.impl.request.Request;
import com.nyquistdata.grpc.api.remote.impl.request.RequestMeta;
import com.nyquistdata.grpc.api.remote.impl.response.Response;
import com.nyquistdata.grpc.server.server.filters.AbstractRequestFilter;
import com.nyquistdata.grpc.server.server.filters.RequestFilters;
import lombok.extern.slf4j.Slf4j;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version: 1.0.0
 * @date: 2026/7/26
 * @description: base request handler
 */
@Slf4j
public abstract class RequestHandler<T extends Request, S extends Response> {

    private final RequestFilters filters;
    private final RequestHandlerRegistry requestHandlerRegistry;

    public RequestHandler(RequestFilters filters, RequestHandlerRegistry requestHandlerRegistry) {
        this.filters = filters;
        this.requestHandlerRegistry = requestHandlerRegistry;
        this.requestHandlerRegistry.register(this);
    }

    /**
     * Handler request.
     *
     * @param request request
     * @param meta    request meta data
     * @return response
     * @throws GrpcException nacos exception when handle request has problem.
     */
    public Response handleRequest(T request, RequestMeta meta) throws GrpcException {
        for (AbstractRequestFilter filter : filters.getFilters()) {
            try {
                Response filterResult = filter.filter(request, meta, this.getClass());
                if (filterResult != null && !filterResult.isSuccess()) {
                    return filterResult;
                }
            } catch (Throwable throwable) {
                log.error("filter error", throwable);
            }

        }
        return handle(request, meta);
    }

    /**
     * Handler request.
     *
     * @param request request
     * @param meta    request meta data
     * @return response
     * @throws GrpcException nacos exception when handle request has problem.
     */
    public abstract S handle(T request, RequestMeta meta) throws GrpcException;

}
