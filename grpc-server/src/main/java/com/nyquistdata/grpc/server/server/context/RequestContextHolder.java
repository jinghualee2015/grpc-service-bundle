package com.nyquistdata.grpc.server.server.context;

import java.util.function.Supplier;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version: 1.0.0
 * @date: 2026/7/26
 * @description: holder for request context for each worker thread
 */
public class RequestContextHolder {
    private static final Supplier<RequestContext> REQUEST_CONTEXT_FACTORY = () -> {
        long requestTimeStamp = System.currentTimeMillis();
        return new RequestContext(requestTimeStamp);
    };

    private static final ThreadLocal<RequestContext> CONTEXT_HOLDER = ThreadLocal.withInitial(REQUEST_CONTEXT_FACTORY);

    public static RequestContext getContext() {
        return CONTEXT_HOLDER.get();
    }

    public static void removeContext() {
        CONTEXT_HOLDER.remove();
    }
}
