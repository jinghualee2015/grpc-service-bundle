package com.nyquistdata.grpc.server.server.enjector;

import com.nyquistdata.grpc.server.server.connection.ConnectionManager;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version: 1.0.0
 * @date: 2026/7/26
 * @description: runtime connection ejector
 */
public abstract class RuntimeConnectionEjector {
    /**
     * 4 times of client keep alive.
     */
    public static final long KEEP_ALIVE_TIME = 20000L;

    /**
     * current loader adjust count,only effective once,use to re balance.
     */
    private int loadClient = -1;

    String redirectAddress = null;

    protected ConnectionManager connectionManager;

    public RuntimeConnectionEjector() {
    }

    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }

    public void setConnectionManager(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    /**
     * eject runtime connection.
     */
    public abstract void doEject();

    public int getLoadClient() {
        return loadClient;
    }

    public void setLoadClient(int loadClient) {
        this.loadClient = loadClient;
    }

    public String getRedirectAddress() {
        return redirectAddress;
    }

    public void setRedirectAddress(String redirectAddress) {
        this.redirectAddress = redirectAddress;
    }

    /**
     * get name.
     *
     * @return
     */
    public abstract String getName();
}
