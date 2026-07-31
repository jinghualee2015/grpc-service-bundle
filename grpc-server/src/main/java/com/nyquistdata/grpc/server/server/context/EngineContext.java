package com.nyquistdata.grpc.server.server.context;

import java.util.HashMap;
import java.util.Map;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version:
 * @date: 2026/7/26
 * @description:
 */
public class EngineContext {
    private String version;

    private final Map<String, String> contexts;

    public EngineContext() {
        version = "1.0.0";
        contexts = new HashMap<>(1);
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getContext(String key) {
        return contexts.get(key);
    }

    public String getContext(String key, String defaultValue) {
        return contexts.getOrDefault(key, defaultValue);
    }

    public void setContext(String key, String value) {
        contexts.put(key, value);
    }
}
