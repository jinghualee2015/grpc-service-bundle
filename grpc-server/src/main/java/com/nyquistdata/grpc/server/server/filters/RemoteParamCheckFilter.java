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
 * @description: the type remote param check filter
 */
@Slf4j
public class RemoteParamCheckFilter extends AbstractRequestFilter {
    public RemoteParamCheckFilter(RequestFilters requestFilters) {
        super(requestFilters);
    }

    @Override
    public Response filter(Request request, RequestMeta meta, Class handlerClazz) throws GrpcException {
        log.info("[RemoteParamCheckFilter] request:{}, meta:{}, handlerClazz:{}", request, meta, handlerClazz);
        return null;

    }

    private Response generateFailResponse(Request request, String message, Class handlerClazz) {
        Response response;
        try {
            response = super.getDefaultResponseInstance(handlerClazz);
            response.setErrorInfo(GrpcException.INVALID_PARAM,
                    "Param check invalid:" + message);
            log.info("Param check invalid,{},request:{}:", message, request.getClass().getSimpleName());
            return response;
        } catch (Exception e) {
            log.error("Param check fail ,request:{}", request.getClass().getSimpleName(), e);
            return null;
        }
    }
}
