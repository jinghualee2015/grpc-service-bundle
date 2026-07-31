package com.nyquistdata.client.client.connection;

import com.nyquistdata.client.client.RpcClient;
import com.nyquistdata.grpc.api.remote.Requester;

import java.util.Map;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version: 1.0.0
 * @date: 2026/7/28
 * @description: connection on the client side
 */
public abstract class Connection implements Requester {
    private String connectionId;

    private boolean abandon = false;

    protected RpcClient.ServerInfo serverInfo;


    public Connection(RpcClient.ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    public RpcClient.ServerInfo getServerInfo() {
        return serverInfo;
    }

    /**
     * Getter method for property <tt>abandon</tt>.
     *
     * @return property value of abandon
     */
    public boolean isAbandon() {
        return abandon;
    }

    /**
     * Setter method for property <tt>abandon</tt>. connection event will be ignored if connection is abandoned.
     *
     * @param abandon value to be assigned to property abandon
     */
    public void setAbandon(boolean abandon) {
        this.abandon = abandon;
    }
}
