package com.nyquistdata.grpc.server.server.filters;

import com.nyquistdata.grpc.api.exceptions.GrpcException;
import com.nyquistdata.grpc.api.remote.impl.request.Request;
import com.nyquistdata.grpc.api.remote.impl.request.RequestMeta;
import com.nyquistdata.grpc.api.remote.impl.response.Response;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version: 1.0.0
 * @date: 2026/7/26
 * @description: interceptor for request
 */
public abstract class AbstractRequestFilter {
    static final String HANDLE = "handle";

    private final RequestFilters requestFilters;

    public AbstractRequestFilter(RequestFilters requestFilters) {
        this.requestFilters = requestFilters;
        this.init();
    }

    void init() {
        requestFilters.registerFilter(this);
    }

    protected Method getHandleMethod(Class handlerClazz) throws GrpcException {
        try {
            Method method = handlerClazz.getMethod(HANDLE, Request.class, RequestMeta.class);
            return method;
        } catch (NoSuchMethodException e) {
            throw new GrpcException(GrpcException.SERVER_ERROR, e);
        }
    }

    protected <T> Response getDefaultResponseInstance(Class handlerClazz) throws GrpcException {
        ParameterizedType parameterizedType = (ParameterizedType) handlerClazz.getGenericSuperclass();
        try {
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            return (Response) Class.forName(actualTypeArguments[1].getTypeName()).newInstance();

        } catch (Exception e) {
            throw new GrpcException(GrpcException.SERVER_ERROR, e);
        }
    }

    /**
     * filter request.
     *
     * @param request      request.
     * @param meta         request meta.
     * @param handlerClazz request handler clazz.
     * @return response
     * @throws GrpcException NacosException.
     */
    public abstract Response filter(Request request, RequestMeta meta, Class handlerClazz) throws GrpcException;

}
