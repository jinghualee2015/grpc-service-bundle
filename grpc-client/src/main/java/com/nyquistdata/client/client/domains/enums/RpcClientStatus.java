package com.nyquistdata.client.client.domains.enums;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version: 1.0.0
 * @date: 2026/7/29
 * @description: status of rpc client
 */
public enum RpcClientStatus {
    /**
     * wait to init.
     */
    WAIT_INIT(0, "Wait to init server list factory..."),

    /**
     * already init.
     */
    INITIALIZED(1, "Server list factory is ready, wait to starting..."),

    /**
     * in starting.
     */
    STARTING(2, "Client already staring, wait to connect with server..."),

    /**
     * unhealthy.
     */
    UNHEALTHY(3, "Client unhealthy, may closed by server, in reconnecting"),

    /**
     * in running.
     */
    RUNNING(4, "Client is running"),

    /**
     * shutdown.
     */
    SHUTDOWN(5, "Client is shutdown");

    int status;

    String desc;

    RpcClientStatus(int status, String desc) {
        this.status = status;
        this.desc = desc;
    }
}
