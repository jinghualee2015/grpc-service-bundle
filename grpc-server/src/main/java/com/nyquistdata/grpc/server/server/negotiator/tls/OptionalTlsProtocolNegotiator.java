package com.nyquistdata.grpc.server.server.negotiator.tls;

import com.nyquistdata.grpc.server.server.negotiator.GrpcProtocolNegotiator;
import io.grpc.netty.shaded.io.grpc.netty.GrpcHttp2ConnectionHandler;
import io.grpc.netty.shaded.io.netty.channel.ChannelHandler;
import io.grpc.netty.shaded.io.netty.util.AsciiString;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version: 1.0.0
 * @date: 2026/7/25
 * @description:
 */
public class OptionalTlsProtocolNegotiator implements GrpcProtocolNegotiator {
    @Override
    public void reloadNegotiator() {

    }

    @Override
    public AsciiString scheme() {
        return null;
    }

    @Override
    public ChannelHandler newHandler(GrpcHttp2ConnectionHandler grpcHttp2ConnectionHandler) {
        return null;
    }

    @Override
    public void close() {

    }
}
