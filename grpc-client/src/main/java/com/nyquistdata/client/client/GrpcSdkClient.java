package com.nyquistdata.client.client;

import com.nyquistdata.client.client.config.GrpcClientConfig;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version: 1.0.0
 * @date: 2026/7/30
 * @description: gRPC client for sdk.
 */
public class GrpcSdkClient extends GrpcClient {
    /**
     * Constructor.
     *
     * @param name name of a client.
     */
    public GrpcSdkClient(String name) {
        super(name);
    }


    public GrpcSdkClient(String name, Integer threadPoolCoreSize, Integer threadPoolMaxSize) {
        super(name, threadPoolCoreSize, threadPoolMaxSize);
    }


    /**
     * constructor.
     *
     * @param config of GrpcClientConfig.
     */
    public GrpcSdkClient(GrpcClientConfig config) {
        super(config);
    }

    public GrpcSdkClient(GrpcClientConfig config, ServerListFactory factory) {
        super(config, factory);
    }

    @Override
    public int rpcPortOffset() {
        return 0;
    }
}
