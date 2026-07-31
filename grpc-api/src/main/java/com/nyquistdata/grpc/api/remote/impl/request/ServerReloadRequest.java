package com.nyquistdata.grpc.api.remote.impl.request;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version: 1.0.0
 * @date: 2026/7/26
 * @description: reload long connection request
 */
public class ServerReloadRequest extends InternalRequest {
    int reloadCount = 0;

    String reloadServer;

    /**
     * Getter method for property <tt>reloadCount</tt>.
     *
     * @return property value of reloadCount
     */
    public int getReloadCount() {
        return reloadCount;
    }

    /**
     * Setter method for property <tt>reloadCount</tt>.
     *
     * @param reloadCount value to be assigned to property reloadCount
     */
    public void setReloadCount(int reloadCount) {
        this.reloadCount = reloadCount;
    }

    public String getReloadServer() {
        return reloadServer;
    }

    public void setReloadServer(String reloadServer) {
        this.reloadServer = reloadServer;
    }
}
