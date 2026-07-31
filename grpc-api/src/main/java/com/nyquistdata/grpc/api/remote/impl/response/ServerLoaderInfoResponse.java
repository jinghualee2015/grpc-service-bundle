package com.nyquistdata.grpc.api.remote.impl.response;

import java.util.HashMap;
import java.util.Map;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version:
 * @date: 2026/7/26
 * @description: server loader info response
 */
public class ServerLoaderInfoResponse extends Response{
    String address;

    Map<String, String> loaderMetrics = new HashMap<>();

    public String getMetricsValue(String key) {
        return loaderMetrics.get(key);
    }

    public void putMetricsValue(String key, String value) {
        this.loaderMetrics.put(key, value);
    }

    /**
     * Getter method for property <tt>loaderMetrics</tt>.
     *
     * @return property value of loaderMetrics
     */
    public Map<String, String> getLoaderMetrics() {
        return loaderMetrics;
    }

    /**
     * Setter method for property <tt>loaderMetrics</tt>.
     *
     * @param loaderMetrics value to be assigned to property loaderMetrics
     */
    public void setLoaderMetrics(Map<String, String> loaderMetrics) {
        this.loaderMetrics = loaderMetrics;
    }
}
