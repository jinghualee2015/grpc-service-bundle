package com.nyquistdata.client.client.config;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version: 1.0.0
 * @date: 2026/7/29
 * @description: rpc client config
 */
public interface RpcClientConfig {
    /**
     * get name.
     *
     * @return name.
     */
    String name();

    /**
     * get request retry times.
     *
     * @return retryTimes.
     */
    int retryTimes();

    /**
     * get time out mills.
     *
     * @return timeOutMills.
     */
    long timeOutMills();

    /**
     * get connection keep alive time.
     *
     * @return connectionKeepAlive.
     */
    long connectionKeepAlive();

    /**
     * get health check retry times.
     *
     * @return healthCheckRetryTimes.
     */
    int healthCheckRetryTimes();

    /**
     * get health check time out.
     *
     * @return healthCheckTimeOut.
     */
    long healthCheckTimeOut();
}
