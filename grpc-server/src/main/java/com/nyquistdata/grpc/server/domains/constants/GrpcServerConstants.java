package com.nyquistdata.grpc.server.domains.constants;

import io.grpc.Attributes;
import io.grpc.Context;
import io.grpc.internal.GrpcUtil;
import io.grpc.netty.shaded.io.netty.channel.Channel;

import java.util.concurrent.TimeUnit;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version:
 * @date: 2026/7/26
 * @description:
 */
public final class GrpcServerConstants {
    public static final Attributes.Key<String> ATTR_TRANS_KEY_CONN_ID = Attributes.Key.create("conn_id");

    public static final Attributes.Key<String> ATTR_TRANS_KEY_REMOTE_IP = Attributes.Key.create("remote_ip");

    public static final Attributes.Key<Integer> ATTR_TRANS_KEY_REMOTE_PORT = Attributes.Key.create("remote_port");

    public static final Attributes.Key<Integer> ATTR_TRANS_KEY_LOCAL_PORT = Attributes.Key.create("local_port");

    public static final Context.Key<String> CONTEXT_KEY_CONN_ID = Context.key("conn_id");

    public static final Context.Key<String> CONTEXT_KEY_CONN_REMOTE_IP = Context.key("remote_ip");

    public static final Context.Key<Integer> CONTEXT_KEY_CONN_REMOTE_PORT = Context.key("remote_port");

    public static final Context.Key<Integer> CONTEXT_KEY_CONN_LOCAL_PORT = Context.key("local_port");

    public  static final Context.Key<Channel> CONTEXT_KEY_CHANNEL = Context.key("ctx_channel");

    public  static final String REQUEST_BI_STREAM_SERVICE_NAME = "BiRequestStream";

    public static final String REQUEST_BI_STREAM_METHOD_NAME = "requestBiStream";

    public static final String REQUEST_SERVICE_NAME = "Request";

    public static final String REQUEST_METHOD_NAME = "request";

    public static class GrpcConfig {

        public static final int DEFAULT_GRPC_MAX_INBOUND_MSG_SIZE = 10 * 1024 * 1024;

        public  static final long DEFAULT_GRPC_KEEP_ALIVE_TIME = TimeUnit.NANOSECONDS.toMillis(GrpcUtil.DEFAULT_SERVER_KEEPALIVE_TIME_NANOS);

        public static final long DEFAULT_GRPC_KEEP_ALIVE_TIMEOUT = TimeUnit.NANOSECONDS.toMillis(GrpcUtil.DEFAULT_SERVER_KEEPALIVE_TIMEOUT_NANOS);

        public  static final long DEFAULT_GRPC_PERMIT_KEEP_ALIVE_TIME = TimeUnit.MINUTES.toMillis(5L);
    }
}
