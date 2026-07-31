package com.nyquistdata.client.client.config;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version: 1.0.0
 * @date: 2026/7/29
 * @description: grpc Client config.
 */
public interface GrpcClientConfig extends RpcClientConfig {
    /**
     * get threadPoolCoreSize.
     *
     * @return threadPoolCoreSize.
     */
    int threadPoolCoreSize();

    /**
     * get threadPoolMaxSize.
     *
     * @return threadPoolMaxSize.
     */
    int threadPoolMaxSize();

    /**
     * get thread pool keep alive time.
     *
     * @return threadPoolKeepAlive.
     */
    long threadPoolKeepAlive();

    /**
     * get server check time out.
     *
     * @return serverCheckTimeOut.
     */
    long serverCheckTimeOut();

    /**
     * get thread pool queue size.
     *
     * @return threadPoolQueueSize.
     */
    int threadPoolQueueSize();

    /**
     * get maxInboundMessage size.
     *
     * @return maxInboundMessageSize.
     */
    int maxInboundMessageSize();

    /**
     * get channelKeepAlive time.
     *
     * @return channelKeepAlive.
     */
    int channelKeepAlive();

    /**
     * get channelKeepAliveTimeout.
     *
     * @return channelKeepAliveTimeout.
     */
    long channelKeepAliveTimeout();

    /**
     * get timeout of connection setup(TimeUnit.MILLISECONDS).
     *
     * @return timeout of connection setup
     */
    long capabilityNegotiationTimeout();
}
