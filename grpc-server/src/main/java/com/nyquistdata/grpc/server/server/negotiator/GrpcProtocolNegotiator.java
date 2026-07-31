package com.nyquistdata.grpc.server.server.negotiator;

import io.grpc.netty.shaded.io.grpc.netty.InternalProtocolNegotiator;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version: 1.0.0
 * @date: 2026/7/25
 * @description: grpc protocol negotiator
 */
public interface GrpcProtocolNegotiator extends InternalProtocolNegotiator.ProtocolNegotiator {

    /**
     * Reload this negotiator, such as config, tls context and so on if necessary.
     */
    void reloadNegotiator();
}
