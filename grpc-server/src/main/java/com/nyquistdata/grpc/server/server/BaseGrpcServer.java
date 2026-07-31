package com.nyquistdata.grpc.server.server;

import com.nyquistdata.grpc.api.Payload;
import com.nyquistdata.grpc.server.domains.constants.GrpcServerConstants;
import com.nyquistdata.grpc.api.domains.enums.ConnectionType;
import com.nyquistdata.grpc.server.server.acceptors.GrpcBiStreamRequestAcceptor;
import com.nyquistdata.grpc.server.server.acceptors.GrpcRequestAcceptor;
import com.nyquistdata.grpc.server.server.connection.ConnectionManager;
import com.nyquistdata.grpc.server.server.filters.AddressTransportFilter;
import com.nyquistdata.grpc.server.server.interceptor.GrpcConnectionInterceptor;
import com.nyquistdata.grpc.server.server.negotiator.GrpcProtocolNegotiator;
import io.grpc.*;
import io.grpc.netty.shaded.io.grpc.netty.InternalProtocolNegotiator;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.stub.ServerCalls;
import io.grpc.util.MutableHandlerRegistry;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version: 1.0.0
 * @date: 2026/7/25
 * @description: Grpc implementation as a rpc server
 */
@Slf4j
public abstract class BaseGrpcServer extends BaseRpcServer {
    protected GrpcProtocolNegotiator protocolNegotiator;

    protected Server server;

    protected final GrpcRequestAcceptor grpcCommonRequestAcceptor;

    protected final GrpcBiStreamRequestAcceptor grpcBiStreamRequestAcceptor;

    protected final ConnectionManager connectionManager;

    protected BaseGrpcServer(GrpcRequestAcceptor grpcCommonRequestAcceptor, GrpcBiStreamRequestAcceptor grpcCommonBiStreamRequestAcceptor, ConnectionManager connectionManager) {
        this.grpcCommonRequestAcceptor = grpcCommonRequestAcceptor;
        this.grpcBiStreamRequestAcceptor = grpcCommonBiStreamRequestAcceptor;
        this.connectionManager = connectionManager;
    }

    @Override
    public ConnectionType getConnectionType() {
        return ConnectionType.GRPC;
    }

    @Override
    public void startServer() throws Exception {
        final MutableHandlerRegistry handlerRegistry = new MutableHandlerRegistry();
        addServices(handlerRegistry, getSeverInterceptors().toArray(new ServerInterceptor[0]));
        NettyServerBuilder builder = NettyServerBuilder.forPort(port).executor(getRpcExecutor());

        Optional<InternalProtocolNegotiator.ProtocolNegotiator> negotiator = newProtocolNegotiator();
        if (negotiator.isPresent()) {
            InternalProtocolNegotiator.ProtocolNegotiator actual = negotiator.get();
            log.info("Add protocol negotiator {}", actual.getClass().getCanonicalName());
            builder.protocolNegotiator(actual);
        }

        for (ServerTransportFilter each : getServerTransportFilters()) {
            builder.addTransportFilter(each);
        }
        server = builder.maxInboundMessageSize(getMaxInboundMessageSize()).fallbackHandlerRegistry(handlerRegistry)
                .compressorRegistry(CompressorRegistry.getDefaultInstance())
                .decompressorRegistry(DecompressorRegistry.getDefaultInstance())
                .keepAliveTime(getKeepAliveTime(), TimeUnit.MILLISECONDS)
                .keepAliveTimeout(getKeepAliveTimeout(), TimeUnit.MILLISECONDS)
                .permitKeepAliveTime(getPermitKeepAliveTime(), TimeUnit.MILLISECONDS).build();

        server.start();
    }

    @Override
    public void reloadProtocolContext() {
        reloadProtocolNegotiator();
    }

    /**
     * Build new one protocol negotiator.
     *
     * <p>Such as support tls, proxy protocol and so on</p>
     *
     * @return ProtocolNegotiator
     */
    protected Optional<InternalProtocolNegotiator.ProtocolNegotiator> newProtocolNegotiator() {
        return Optional.empty();
    }

    /**
     * reload protocol negotiator If necessary.
     */
    public void reloadProtocolNegotiator() {
        if (protocolNegotiator != null) {
            try {
                protocolNegotiator.reloadNegotiator();
            } catch (Throwable throwable) {
                log.info("Nacos {} Rpc server reload negotiator fail at port {}.",
                        this.getClass().getSimpleName(), port);
                throw throwable;
            }
        }
    }

    protected long getPermitKeepAliveTime() {
        return GrpcServerConstants.GrpcConfig.DEFAULT_GRPC_PERMIT_KEEP_ALIVE_TIME;
    }

    protected long getKeepAliveTime() {
        return GrpcServerConstants.GrpcConfig.DEFAULT_GRPC_KEEP_ALIVE_TIME;
    }

    protected long getKeepAliveTimeout() {
        return GrpcServerConstants.GrpcConfig.DEFAULT_GRPC_KEEP_ALIVE_TIMEOUT;
    }

    protected int getMaxInboundMessageSize() {
        return GrpcServerConstants.GrpcConfig.DEFAULT_GRPC_MAX_INBOUND_MSG_SIZE;
    }

    protected List<ServerInterceptor> getSeverInterceptors() {
        List<ServerInterceptor> result = new LinkedList<>();
        result.add(new GrpcConnectionInterceptor());
        return result;
    }

    protected List<ServerTransportFilter> getServerTransportFilters() {
        return Collections.singletonList(new AddressTransportFilter(connectionManager));
    }

    private void addServices(MutableHandlerRegistry handlerRegistry, ServerInterceptor... serverInterceptor) {

        // unary common call register.
        final MethodDescriptor<Payload, Payload> unaryPayloadMethod = MethodDescriptor.<Payload, Payload>newBuilder()
                .setType(MethodDescriptor.MethodType.UNARY).setFullMethodName(
                        MethodDescriptor.generateFullMethodName(GrpcServerConstants.REQUEST_SERVICE_NAME,
                                GrpcServerConstants.REQUEST_METHOD_NAME))
                .setRequestMarshaller(ProtoUtils.marshaller(Payload.getDefaultInstance()))
                .setResponseMarshaller(ProtoUtils.marshaller(Payload.getDefaultInstance())).build();

        final ServerCallHandler<Payload, Payload> payloadHandler = ServerCalls.asyncUnaryCall(
                (request, responseObserver) -> grpcCommonRequestAcceptor.request(request, responseObserver));

        final ServerServiceDefinition serviceDefOfUnaryPayload = ServerServiceDefinition.builder(
                GrpcServerConstants.REQUEST_SERVICE_NAME).addMethod(unaryPayloadMethod, payloadHandler).build();
        handlerRegistry.addService(ServerInterceptors.intercept(serviceDefOfUnaryPayload, serverInterceptor));

        // bi stream register.
        final ServerCallHandler<Payload, Payload> biStreamHandler = ServerCalls.asyncBidiStreamingCall(
                (responseObserver) -> grpcBiStreamRequestAcceptor.requestBiStream(responseObserver));

        final MethodDescriptor<Payload, Payload> biStreamMethod = MethodDescriptor.<Payload, Payload>newBuilder()
                .setType(MethodDescriptor.MethodType.BIDI_STREAMING).setFullMethodName(
                        MethodDescriptor.generateFullMethodName(GrpcServerConstants.REQUEST_BI_STREAM_SERVICE_NAME,
                                GrpcServerConstants.REQUEST_BI_STREAM_METHOD_NAME))
                .setRequestMarshaller(ProtoUtils.marshaller(Payload.newBuilder().build()))
                .setResponseMarshaller(ProtoUtils.marshaller(Payload.getDefaultInstance())).build();

        final ServerServiceDefinition serviceDefOfBiStream = ServerServiceDefinition.builder(
                GrpcServerConstants.REQUEST_BI_STREAM_SERVICE_NAME).addMethod(biStreamMethod, biStreamHandler).build();
        handlerRegistry.addService(ServerInterceptors.intercept(serviceDefOfBiStream, serverInterceptor));

    }

    @Override
    public void shutdownServer() {
        if (server != null) {
            server.shutdownNow();
        }
    }

    /**
     * get rpc executor.
     *
     * @return executor.
     */
    public abstract ThreadPoolExecutor getRpcExecutor();
}
