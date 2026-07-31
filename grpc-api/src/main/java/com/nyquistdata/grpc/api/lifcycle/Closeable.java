package com.nyquistdata.grpc.api.lifcycle;

import com.nyquistdata.grpc.api.exceptions.GrpcException;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version: 1.0.0
 * @date: 2026/7/29
 * @description:
 */
public interface Closeable {
    /**
     * Shutdown the Resources, such as Thread Pool.
     *
     * @throws GrpcException exception.
     */
    void shutdown() throws GrpcException;
}
