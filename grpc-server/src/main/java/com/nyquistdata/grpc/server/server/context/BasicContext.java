package com.nyquistdata.grpc.server.server.context;

import static com.nyquistdata.grpc.api.domains.constants.GrpcConstants.ENCODE;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version: 1.0.0
 * @date: 2026/7/26
 * @description: request basic information context.
 */
public class BasicContext {
    private static final String DEFAULT_APP = "unknown";

    public static final String HTTP_PROTOCOL = "HTTP";

    public static final String GRPC_PROTOCOL = "GRPC";

    private final AddressContext addressContext;

    /**
     * Request user agent, such as Nacos-Java-client:v2.4.0
     */
    private String userAgent;

    /**
     * Request protocol type, HTTP or GRPC and so on.
     */
    private String requestProtocol;

    /**
     * Request target.
     * <ul>
     *     <li>For HTTP protocol it should be `${Method} ${URI}`, such as `POST /v2/ns/instance`</li>
     *     <li>For GRPC protocol, it should be `${requestClass}`, such as `InstanceRequest`</li>
     * </ul>
     */
    private String requestTarget;

    /**
     * Optional, mark the app name of the request from when client set app name, default `unknown`.
     */
    private String app;

    /**
     * Optional, mark the encoding of the request from when client set encoding, default `UTF-8`.
     */
    private String encoding;

    public BasicContext() {
        this.addressContext = new AddressContext();
        this.app = DEFAULT_APP;
        this.encoding = ENCODE;
    }

    public AddressContext getAddressContext() {
        return addressContext;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getRequestProtocol() {
        return requestProtocol;
    }

    public void setRequestProtocol(String requestProtocol) {
        this.requestProtocol = requestProtocol;
    }

    public String getRequestTarget() {
        return requestTarget;
    }

    public void setRequestTarget(String requestTarget) {
        this.requestTarget = requestTarget;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
}
