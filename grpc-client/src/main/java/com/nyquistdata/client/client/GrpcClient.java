package com.nyquistdata.client.client;

import com.google.common.util.concurrent.ListenableFuture;
import com.nyquistdata.client.client.config.DefaultGrpcClientConfig;
import com.nyquistdata.client.client.config.GrpcClientConfig;
import com.nyquistdata.client.client.connection.Connection;
import com.nyquistdata.client.client.connection.GrpcConnection;
import com.nyquistdata.client.client.domains.enums.RpcClientStatus;
import com.nyquistdata.grpc.api.BiRequestStreamGrpc;
import com.nyquistdata.grpc.api.Payload;
import com.nyquistdata.grpc.api.RequestGrpc;
import com.nyquistdata.grpc.api.domains.enums.ConnectionType;
import com.nyquistdata.grpc.api.exceptions.GrpcException;
import com.nyquistdata.grpc.api.remote.impl.request.*;
import com.nyquistdata.grpc.api.remote.impl.response.ErrorResponse;
import com.nyquistdata.grpc.api.remote.impl.response.Response;
import com.nyquistdata.grpc.api.remote.impl.response.ServerCheckResponse;
import com.nyquistdata.grpc.api.remote.impl.response.SetupAckResponse;
import com.nyquistdata.grpc.api.utils.GrpcUtils;
import com.nyquistdata.grpc.api.utils.ThreadFactoryBuilder;
import io.grpc.CompressorRegistry;
import io.grpc.DecompressorRegistry;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.netty.shaded.io.grpc.netty.NegotiationType;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version: 1.0.0
 * @date: 2026/7/29
 * @description: gRPC client
 */
@Slf4j
public abstract class GrpcClient extends RpcClient {
    private final GrpcClientConfig clientConfig;

    private ThreadPoolExecutor grpcExecutor;

    /**
     * Block to wait setup success response.
     */
    private final RecAbilityContext recAbilityContext = new RecAbilityContext(null);

    /**
     * for receiving server abilities.
     */
    private SetupRequestHandler setupRequestHandler;

    @Override
    public ConnectionType getConnectionType() {
        return ConnectionType.GRPC;
    }

    /**
     * constructor.
     *
     * @param name .
     */
    public GrpcClient(String name) {
        this(DefaultGrpcClientConfig.newBuilder().setName(name).build());
    }

    /**
     * constructor.
     *
     * @param clientConfig .
     */
    public GrpcClient(GrpcClientConfig clientConfig) {
        super(clientConfig);
        this.clientConfig = clientConfig;
        initSetupHandler();
    }

    /**
     * constructor.
     *
     * @param clientConfig      .
     * @param serverListFactory .
     */
    public GrpcClient(GrpcClientConfig clientConfig, ServerListFactory serverListFactory) {
        super(clientConfig, serverListFactory);
        this.clientConfig = clientConfig;
        initSetupHandler();
    }

    /**
     * setup handler.
     */
    private void initSetupHandler() {
        // register to handler setup request
        setupRequestHandler = new SetupRequestHandler(this.recAbilityContext);
    }

    public GrpcClient(String name, Integer threadPoolCoreSize, Integer threadPoolMaxSize) {
        this(DefaultGrpcClientConfig.newBuilder().setName(name).setThreadPoolCoreSize(threadPoolCoreSize)
                .setThreadPoolMaxSize(threadPoolMaxSize)
                .build());
    }

    protected ThreadPoolExecutor createGrpcExecutor(String serverIp) {
        // Thread name will use String.format, ipv6 maybe contain special word %, so handle it first.
        serverIp = serverIp.replaceAll("%", "-");
        ThreadPoolExecutor grpcExecutor = new ThreadPoolExecutor(clientConfig.threadPoolCoreSize(),
                clientConfig.threadPoolMaxSize(), clientConfig.threadPoolKeepAlive(), TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(clientConfig.threadPoolQueueSize()),
                new ThreadFactoryBuilder().daemon(true).nameFormat("grpc-client-executor-" + serverIp + "-%d")
                        .build());
        grpcExecutor.allowCoreThreadTimeOut(true);
        return grpcExecutor;
    }

    @Override
    public void shutdown() throws GrpcException {
        super.shutdown();
        if (grpcExecutor != null) {
            log.info("Shutdown grpc executor {}", grpcExecutor);
            grpcExecutor.shutdown();
        }
    }

    /**
     * Create a stub using a channel.
     *
     * @param managedChannelTemp channel.
     * @return if server check success,return a non-null stub.
     */
    protected RequestGrpc.RequestFutureStub createNewChannelStub(ManagedChannel managedChannelTemp) {
        return RequestGrpc.newFutureStub(managedChannelTemp);
    }

    /**
     * create a new channel with specific server address.
     *
     * @param serverIp   serverIp.
     * @param serverPort serverPort.
     * @return if server check success,return a non-null channel.
     */
    private ManagedChannel createNewManagedChannel(String serverIp, int serverPort) {
        log.info("grpc client connection server:{} ip,serverPort:{}", serverIp, serverPort);
        ManagedChannelBuilder<?> managedChannelBuilder = buildChannel(serverIp, serverPort,buildSslContext()).executor(
                        grpcExecutor).compressorRegistry(CompressorRegistry.getDefaultInstance())
                .decompressorRegistry(DecompressorRegistry.getDefaultInstance())
                .maxInboundMessageSize(clientConfig.maxInboundMessageSize())
                .keepAliveTime(clientConfig.channelKeepAlive(), TimeUnit.MILLISECONDS)
                .keepAliveTimeout(clientConfig.channelKeepAliveTimeout(), TimeUnit.MILLISECONDS);
        return managedChannelBuilder.build();
    }

    /**
     * shutdown a  channel.
     *
     * @param managedChannel channel to be shutdown.
     */
    private void shuntDownChannel(ManagedChannel managedChannel) {
        if (managedChannel != null && !managedChannel.isShutdown()) {
            managedChannel.shutdownNow();
        }
    }

    /**
     * check server if success.
     *
     * @param requestBlockingStub requestBlockingStub used to check server.
     * @return success or not
     */
    private Response serverCheck(String ip, int port, RequestGrpc.RequestFutureStub requestBlockingStub) {
        try {
            ServerCheckRequest serverCheckRequest = new ServerCheckRequest();
            Payload grpcRequest = GrpcUtils.convert(serverCheckRequest);
            ListenableFuture<Payload> responseFuture = requestBlockingStub.request(grpcRequest);
            Payload response = responseFuture.get(clientConfig.serverCheckTimeOut(), TimeUnit.MILLISECONDS);
            //receive connection unregister response here,not check response is success.
            return (Response) GrpcUtils.parse(response);
        } catch (Exception e) {
            log.error("Server check fail, please check server {} ,port {} is available , error ={}", ip, port, e);
            return null;
        }
    }

    private StreamObserver<Payload> bindRequestStream(final BiRequestStreamGrpc.BiRequestStreamStub streamStub,
                                                      final GrpcConnection grpcConn) {

        return streamStub.requestBiStream(new StreamObserver<Payload>() {

            @Override
            public void onNext(Payload payload) {

                log.info("[{}]Stream server request receive, original info: {}",
                        grpcConn.getConnectionId(), payload.toString());
                try {
                    Object parseBody = GrpcUtils.parse(payload);
                    final Request request = (Request) parseBody;
                    if (request != null) {

                        try {
                            if (request instanceof SetupAckRequest) {
                                // there is no connection ready this time
                                setupRequestHandler.requestReply(request, null);
                                return;
                            }
                            Response response = handleServerRequest(request);
                            if (response != null) {
                                response.setRequestId(request.getRequestId());
                                sendResponse(response);
                            } else {
                                log.warn("[{}]Fail to process server request, ackId->{}", grpcConn.getConnectionId(),
                                        request.getRequestId());
                            }

                        } catch (Exception e) {
                            log.error("[{}]Handle server request exception: {}",
                                    grpcConn.getConnectionId(), payload.toString(), e.getMessage());
                            Response errResponse = ErrorResponse.build(GrpcException.CLIENT_ERROR,
                                    "Handle server request error");
                            errResponse.setRequestId(request.getRequestId());
                            sendResponse(errResponse);
                        }

                    }

                } catch (Exception e) {

                    log.error("[{}]Error to process server push response: {}",
                            grpcConn.getConnectionId(), payload.getBody().getValue().toStringUtf8());
                    // remove and notify
                    recAbilityContext.release(null);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                boolean isRunning = isRunning();
                boolean isAbandon = grpcConn.isAbandon();
                if (isRunning && !isAbandon) {
                    log.error("[{}]Request stream error, switch server,error={}",
                            grpcConn.getConnectionId(), throwable);
                    if (rpcClientStatus.compareAndSet(RpcClientStatus.RUNNING, RpcClientStatus.UNHEALTHY)) {
                        switchServerAsync();
                    }

                } else {
                    log.warn("[{}]Ignore error event,isRunning:{},isAbandon={}",
                            grpcConn.getConnectionId(), isRunning, isAbandon);
                }

            }

            @Override
            public void onCompleted() {
                boolean isRunning = isRunning();
                boolean isAbandon = grpcConn.isAbandon();
                if (isRunning && !isAbandon) {
                    log.error("[{}]Request stream onCompleted, switch server",
                            grpcConn.getConnectionId());
                    if (rpcClientStatus.compareAndSet(RpcClientStatus.RUNNING, RpcClientStatus.UNHEALTHY)) {
                        switchServerAsync();
                    }

                } else {
                    log.info("[{}]Ignore complete event,isRunning:{},isAbandon={}",
                            grpcConn.getConnectionId(), isRunning, isAbandon);
                }

            }
        });
    }

    private void sendResponse(Response response) {
        try {
            ((GrpcConnection) this.currentConnection).sendResponse(response);
        } catch (Exception e) {
            log.error("[{}]Error to send ack response, ackId->{}", this.currentConnection.getConnectionId(),
                    response.getRequestId());
        }
    }

    @Override
    public Connection connectToServer(ServerInfo serverInfo) {
        // the newest connection id
        String connectionId = "";
        try {
            if (grpcExecutor == null) {
                this.grpcExecutor = createGrpcExecutor(serverInfo.getServerIp());
            }
            int port = serverInfo.getServerPort() + rpcPortOffset();
            ManagedChannel managedChannel = createNewManagedChannel(serverInfo.getServerIp(), port);
            RequestGrpc.RequestFutureStub newChannelStubTemp = createNewChannelStub(managedChannel);

            Response response = serverCheck(serverInfo.getServerIp(), port, newChannelStubTemp);
            if (!(response instanceof ServerCheckResponse)) {
                shuntDownChannel(managedChannel);
                return null;
            }
            // submit ability table as soon as possible
            // ability table will be null if server doesn't support ability table
            ServerCheckResponse serverCheckResponse = (ServerCheckResponse) response;
            connectionId = serverCheckResponse.getConnectionId();

            BiRequestStreamGrpc.BiRequestStreamStub biRequestStreamStub = BiRequestStreamGrpc.newStub(
                    newChannelStubTemp.getChannel());
            GrpcConnection grpcConn = new GrpcConnection(serverInfo, grpcExecutor);
            grpcConn.setConnectionId(connectionId);
            // if not supported, it will be false
            if (serverCheckResponse.isSupportAbilityNegotiation()) {
                // mark
                this.recAbilityContext.reset(grpcConn);
            }

            //create stream request and bind connection event to this connection.
            StreamObserver<Payload> payloadStreamObserver = bindRequestStream(biRequestStreamStub, grpcConn);

            // stream observer to send response to server
            grpcConn.setPayloadStreamObserver(payloadStreamObserver);
            grpcConn.setGrpcFutureServiceStub(newChannelStubTemp);
            grpcConn.setChannel(managedChannel);
            //send a  setup request.
            ConnectionSetupRequest conSetupRequest = new ConnectionSetupRequest();
            conSetupRequest.setClientVersion("1.0.0");
            conSetupRequest.setTenant(super.getTenant());
            grpcConn.sendRequest(conSetupRequest);
            // wait for response
            if (recAbilityContext.isNeedToSync()) {
                // try to wait for notify response
                recAbilityContext.await(this.clientConfig.capabilityNegotiationTimeout(), TimeUnit.MILLISECONDS);
                // if no server abilities receiving, then reconnect
                if (!recAbilityContext.check(grpcConn)) {
                    return null;
                }
            } else {
                // leave for adapting old version server
                // registration is considered successful by default after 100ms
                // wait to register connection setup
                Thread.sleep(100L);
            }
            return grpcConn;
        } catch (Exception e) {
            log.error("[{}]Fail to connect to server!,error={}", GrpcClient.this.getName(), e);
            // remove and notify
            recAbilityContext.release(null);
        }
        return null;
    }

    @Override
    protected void afterReset(ConnectResetRequest request) {
        recAbilityContext.release(null);
    }

    /**
     * This is for receiving server abilities.
     */
    static class RecAbilityContext {

        /**
         * connection waiting for server abilities.
         */
        private volatile Connection connection;

        /**
         * way to block client.
         */
        private volatile CountDownLatch blocker;

        private volatile boolean needToSync = false;

        public RecAbilityContext(Connection connection) {
            this.connection = connection;
            this.blocker = new CountDownLatch(1);
        }

        /**
         * whether to sync for ability table.
         *
         * @return whether to sync for ability table.
         */
        public boolean isNeedToSync() {
            return this.needToSync;
        }

        /**
         * reset with new connection which is waiting for ability table.
         *
         * @param connection new connection which is waiting for ability table.
         */
        public void reset(Connection connection) {
            this.connection = connection;
            this.blocker = new CountDownLatch(1);
            this.needToSync = true;
        }

        /**
         * notify sync by abilities.
         *
         * @param abilities abilities.
         */
        public void release(Map<String, Boolean> abilities) {
            if (this.connection != null) {
                this.connection = null;
            }
            if (this.blocker != null) {
                blocker.countDown();
            }
            this.needToSync = false;
        }

        /**
         * await for abilities.
         *
         * @param timeout timeout.
         * @param unit    unit.
         * @throws InterruptedException by blocker.
         */
        public void await(long timeout, TimeUnit unit) throws InterruptedException {
            if (this.blocker != null) {
                this.blocker.await(timeout, unit);
            }
            this.needToSync = false;
        }

        /**
         * check whether receive abilities.
         *
         * @param connection conn.
         * @return whether receive abilities.
         */
        public boolean check(Connection connection) {
            return true;
        }
    }

    private Optional<SslContext> buildSslContext() {
        return Optional.empty();
    }

    private ManagedChannelBuilder buildChannel(String serverIp, int port, Optional<SslContext> sslContext) {
        if (sslContext.isPresent()) {
            return NettyChannelBuilder.forAddress(serverIp, port).negotiationType(NegotiationType.TLS)
                    .sslContext(sslContext.get());

        } else {
            return ManagedChannelBuilder.forAddress(serverIp, port).usePlaintext();
        }
    }

    /**
     * Setup response handler.
     */
    class SetupRequestHandler implements ServerRequestHandler {

        private final RecAbilityContext abilityContext;

        public SetupRequestHandler(RecAbilityContext abilityContext) {
            this.abilityContext = abilityContext;
        }

        @Override
        public Response requestReply(Request request, Connection connection) {
            // if finish setup
            if (request instanceof SetupAckRequest) {
                SetupAckRequest setupAckRequest = (SetupAckRequest) request;
                // remove and count down
                recAbilityContext.release(
                        Optional.ofNullable(setupAckRequest.getAbilityTable()).orElse(new HashMap<>(0)));
                return new SetupAckResponse();
            }
            return null;
        }
    }
}
