package com.nyquistdata.grpc.server.server.negotiator;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version: 1.0.0
 * @date: 2026/7/25
 * @description: Protocol negotiator builder
 */
public interface ProtocolNegotiatorBuilder {
    /**
     * @return
     */
    GrpcProtocolNegotiator build();

    /**
     * Builder type of ProtocolNegotiator.
     *
     * @return type
     */
    String type();
}
