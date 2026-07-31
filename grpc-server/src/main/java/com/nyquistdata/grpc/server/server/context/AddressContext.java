package com.nyquistdata.grpc.server.server.context;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version: 1.0.0
 * @date: 2026/7/26
 * @description:
 */
public class AddressContext {
    /**
     * Request source ip, it's the ip of the client, most situations are same with remoteIp.
     */
    private String sourceIp;

    /**
     * Request source port, it's the port of the client,  most situations are same with remoteIp.
     */
    private int sourcePort;

    /**
     * Request connection ip, it should be got from the socket, which means the ip seen by nacos server.
     */
    private String remoteIp;

    /**
     * Request connection port, it should be got from the socket, which means the port seen by nacos server.
     */
    private int remotePort;

    /**
     * Request host, it's the host of the client, nullable when can't get it from request or connection.
     */
    private String host;

    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    public int getSourcePort() {
        return sourcePort;
    }

    public void setSourcePort(int sourcePort) {
        this.sourcePort = sourcePort;
    }

    public String getRemoteIp() {
        return remoteIp;
    }

    public void setRemoteIp(String remoteIp) {
        this.remoteIp = remoteIp;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
}
