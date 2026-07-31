package io.grpc.netty.shaded.io.grpc.netty;

import com.alipay.sofa.jraft.util.internal.ReferenceFieldUpdater;
import com.alipay.sofa.jraft.util.internal.Updaters;
import io.grpc.internal.ServerStream;
import io.grpc.netty.shaded.io.netty.channel.Channel;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version: 1.0.0
 * @date: 2026/7/27
 * @description: Get netty channel
 */
public class NettyChannelHelper {
    private static final ReferenceFieldUpdater<NettyServerStream, WriteQueue> WRITE_QUEUE_GETTER = Updaters.newReferenceFieldUpdater(
            NettyServerStream.class, "writeQueue");

    private static final ReferenceFieldUpdater<WriteQueue, Channel> CHANNEL_GETTER = Updaters.newReferenceFieldUpdater(
            WriteQueue.class, "channel");

    public static Channel getChannel(final ServerStream stream) {
        if (stream instanceof NettyServerStream) {
            return CHANNEL_GETTER.get(WRITE_QUEUE_GETTER.get((NettyServerStream) stream));
        }
        return null;
    }
}
