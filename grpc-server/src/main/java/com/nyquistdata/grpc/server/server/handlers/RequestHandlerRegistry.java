package com.nyquistdata.grpc.server.server.handlers;

import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Map;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version: 1.0.0
 * @date: 2026/7/26
 * @description: requestHandlerRegistry
 */
public class RequestHandlerRegistry {
    private final Map<String, RequestHandler> requestHandlers = new HashMap<>();

    public void register(RequestHandler requestHandler) {
        Class tClass = (Class) ((ParameterizedType) requestHandler.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        requestHandlers.put(tClass.getSimpleName(), requestHandler);
    }

    public RequestHandler getByRequestType(String requestType) {
        return requestHandlers.get(requestType);
    }
}
