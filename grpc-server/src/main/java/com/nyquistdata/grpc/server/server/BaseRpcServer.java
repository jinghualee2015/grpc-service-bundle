package com.nyquistdata.grpc.server.server;

import com.nyquistdata.grpc.api.domains.enums.ConnectionType;
import com.nyquistdata.grpc.api.remote.support.PayloadRegistry;
import lombok.extern.slf4j.Slf4j;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version: 1.0.0
 * @date: 2026/7/25
 * @description: abstract rpc server
 */
@Slf4j
public abstract class BaseRpcServer {
    protected final int port;

    static {
        PayloadRegistry.init();
    }

    protected BaseRpcServer() {
        this(9848);
    }

    protected BaseRpcServer(int port) {
        this.port = port;
    }

    public void start() throws Exception {
        String serverName = getClass().getSimpleName();
        log.info("{} Rpc server starting at port {}", serverName, port);
        startServer();
        log.info("{} Rpc server started at port {}", serverName, port);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("{} rpc server stopping", serverName);
            try {
                BaseRpcServer.this.stopServer();
                log.warn("{} rpc server stopped successfully....", serverName);
            } catch (Exception e) {
                log.error("{} rpc server stopping failed", serverName, e);
            }
        }));
    }

    public final void stopServer() throws Exception {
        shutdownServer();
    }

    public abstract ConnectionType getConnectionType();

    public abstract void reloadProtocolContext();

    public abstract void startServer() throws Exception;

    public abstract void shutdownServer();
}
