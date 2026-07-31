package com.nyquistdata.grpc.server.server.interceptor;

import com.nyquistdata.grpc.server.domains.constants.GrpcServerConstants;
import io.grpc.*;
import io.grpc.internal.ServerStream;
import io.grpc.internal.ServerStreamHelper;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelHelper;
import io.grpc.netty.shaded.io.netty.channel.Channel;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version: 1.0.0
 * @date: 2026/7/27
 * @description: grpc connection interceptor
 */
public class GrpcConnectionInterceptor implements ServerInterceptor {
    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        Context ctx = Context.current().withValue(GrpcServerConstants.CONTEXT_KEY_CONN_ID,
                        call.getAttributes().get(GrpcServerConstants.ATTR_TRANS_KEY_CONN_ID))
                .withValue(GrpcServerConstants.CONTEXT_KEY_CONN_REMOTE_IP,
                        call.getAttributes().get(GrpcServerConstants.ATTR_TRANS_KEY_REMOTE_IP))
                .withValue(GrpcServerConstants.CONTEXT_KEY_CONN_REMOTE_PORT,
                        call.getAttributes().get(GrpcServerConstants.ATTR_TRANS_KEY_REMOTE_PORT))
                .withValue(GrpcServerConstants.CONTEXT_KEY_CONN_LOCAL_PORT,
                        call.getAttributes().get(GrpcServerConstants.ATTR_TRANS_KEY_LOCAL_PORT));
        if (GrpcServerConstants.REQUEST_BI_STREAM_SERVICE_NAME.equals(call.getMethodDescriptor().getServiceName())) {
            Channel internalChannel = getInternalChannel(call);
            ctx = ctx.withValue(GrpcServerConstants.CONTEXT_KEY_CHANNEL, internalChannel);
        }

        return Contexts.interceptCall(ctx, call, headers, next);
    }

    private Channel getInternalChannel(ServerCall call) {
        ServerStream serverStream = ServerStreamHelper.getServerStream(call);
        return NettyChannelHelper.getChannel(serverStream);
    }
}
