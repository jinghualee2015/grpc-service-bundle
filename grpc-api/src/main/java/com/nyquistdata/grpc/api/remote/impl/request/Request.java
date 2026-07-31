package com.nyquistdata.grpc.api.remote.impl.request;

import com.nyquistdata.grpc.api.remote.Payload;

import java.util.Map;
import java.util.TreeMap;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version: 1.0.0
 * @date: 2026/7/26
 * @description: request
 */
public  abstract class Request  implements Payload {
    private final Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    private String requestId;

    /**
     * put header.
     *
     * @param key   key of value.
     * @param value value.
     */
    public void putHeader(String key, String value) {
        headers.put(key, value);
    }

    /**
     * put headers .
     *
     * @param headers headers to put.
     */
    public void putAllHeader(Map<String, String> headers) {
        if (headers == null || headers.isEmpty()) {
            return;
        }
        this.headers.putAll(headers);
    }

    /**
     * get a header value .
     *
     * @param key key of value.
     * @return return value of key. return null if not exist.
     */
    public String getHeader(String key) {
        return headers.get(key);
    }

    /**
     * get a header value of default value.
     *
     * @param key          key of value.
     * @param defaultValue default value if key is not exist.
     * @return return final value.
     */
    public String getHeader(String key, String defaultValue) {
        String value = headers.get(key);
        return (value == null) ? defaultValue : value;
    }

    /**
     * Getter method for property <tt>requestId</tt>.
     *
     * @return property value of requestId
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * Setter method for property <tt>requestId</tt>.
     *
     * @param requestId value to be assigned to property requestId
     */
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    /**
     * Getter method for property <tt>type</tt>.
     *
     * @return property value of type
     */
    public abstract String getModule();

    /**
     * Getter method for property <tt>headers</tt>.
     *
     * @return property value of headers
     */
    public Map<String, String> getHeaders() {
        return headers;
    }

    public void clearHeaders() {
        this.headers.clear();
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" + "headers=" + headers + ", requestId='" + requestId + '\'' + '}';
    }
}
