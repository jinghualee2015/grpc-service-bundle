package com.nyquistdata.grpc.server.server.context;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version: 1.0.0
 * @date: 2026/7/26
 * @description: grpc request context
 */
public class RequestContext {
    /**
     * Optional, the request id.
     * <ul>
     *     <li>For HTTP request, the id not usage, will generate automatically.</li>
     *     <li>For GRPC, the id is same with real request id.</li>
     * </ul>
     */
    private String requestId;

    /**
     * Request start timestamp.
     */
    private final long requestTimestamp;

    private final BasicContext basicContext;

    private final EngineContext engineContext;

    private final Map<String, Object> extensionContexts;

    RequestContext(long requestTimestamp) {
        this.requestId = UUID.randomUUID().toString();
        this.requestTimestamp = requestTimestamp;
        this.basicContext = new BasicContext();
        this.engineContext = new EngineContext();
        this.extensionContexts = new HashMap<>(1);
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getRequestId() {
        return requestId;
    }

    public long getRequestTimestamp() {
        return requestTimestamp;
    }

    public BasicContext getBasicContext() {
        return basicContext;
    }

    public EngineContext getEngineContext() {
        return engineContext;
    }


    public Object getExtensionContext(String key) {
        return extensionContexts.get(key);
    }

    public void addExtensionContext(String key, Object value) {
        extensionContexts.put(key, value);
    }
}
