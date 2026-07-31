package com.nyquistdata.client.client;

import com.nyquistdata.client.client.config.RpcClientConfig;
import com.nyquistdata.client.client.connection.Connection;
import com.nyquistdata.client.client.connection.listener.ConnectionEventListener;
import com.nyquistdata.client.client.domains.enums.RpcClientStatus;
import com.nyquistdata.client.client.support.NamedThreadFactory;
import com.nyquistdata.grpc.api.domains.enums.ConnectionType;
import com.nyquistdata.grpc.api.exceptions.GrpcException;
import com.nyquistdata.grpc.api.lifcycle.Closeable;
import com.nyquistdata.grpc.api.remote.RequestCallBack;
import com.nyquistdata.grpc.api.remote.RequestFuture;
import com.nyquistdata.grpc.api.remote.impl.request.ClientDetectionRequest;
import com.nyquistdata.grpc.api.remote.impl.request.ConnectResetRequest;
import com.nyquistdata.grpc.api.remote.impl.request.HealthCheckRequest;
import com.nyquistdata.grpc.api.remote.impl.request.Request;
import com.nyquistdata.grpc.api.remote.impl.response.ClientDetectionResponse;
import com.nyquistdata.grpc.api.remote.impl.response.ConnectResetResponse;
import com.nyquistdata.grpc.api.remote.impl.response.ErrorResponse;
import com.nyquistdata.grpc.api.remote.impl.response.Response;
import com.nyquistdata.grpc.api.remote.support.PayloadRegistry;
import com.nyquistdata.grpc.api.resource.DefaultResourceLoader;
import com.nyquistdata.grpc.api.resource.ResourceLoader;
import com.nyquistdata.grpc.api.utils.CollectionUtils;
import com.nyquistdata.grpc.api.utils.InternetAddressUtil;
import com.nyquistdata.grpc.api.utils.NumberUtils;
import com.nyquistdata.grpc.api.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.nyquistdata.grpc.api.domains.constants.GrpcConstants.COLON;
import static com.nyquistdata.grpc.api.exceptions.GrpcException.SERVER_ERROR;
import static com.sun.deploy.si.SingleInstanceManager.connectToServer;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version: 1.0.0
 * @date: 2026/7/28
 * @description: abstract remote client to connect server
 */
@Slf4j
public abstract class RpcClient implements Closeable {
    private ServerListFactory serverListFactory;

    protected BlockingQueue<ConnectionEvent> eventLinkedBlockingQueue = new LinkedBlockingQueue<>();

    protected volatile AtomicReference<RpcClientStatus> rpcClientStatus = new AtomicReference<>(
            RpcClientStatus.WAIT_INIT);

    protected ScheduledExecutorService clientEventExecutor;

    private final BlockingQueue<ReconnectContext> reconnectionSignal = new ArrayBlockingQueue<>(1);

    protected volatile Connection currentConnection;

    private String tenant;

    private long lastActiveTimeStamp = System.currentTimeMillis();

    /**
     * listener called where connection's status changed.
     */
    protected List<ConnectionEventListener> connectionEventListeners = new ArrayList<>();

    /**
     * handlers to process server push request.
     */
    protected List<ServerRequestHandler> serverRequestHandlers = new ArrayList<>();

    private static final Pattern EXCLUDE_PROTOCOL_PATTERN = Pattern.compile("(?<=\\w{1,5}://)(.*)");

    protected RpcClientConfig rpcClientConfig;

    protected final ResourceLoader resourceLoader = new DefaultResourceLoader();

    static {
        PayloadRegistry.init();
    }

    public RpcClient(RpcClientConfig rpcClientConfig) {
        this(rpcClientConfig, null);
    }

    public RpcClient(RpcClientConfig rpcClientConfig, ServerListFactory serverListFactory) {
        this.rpcClientConfig = rpcClientConfig;
        this.serverListFactory = serverListFactory;
        init();
    }

    protected void init() {
        if (this.serverListFactory != null) {
            rpcClientStatus.compareAndSet(RpcClientStatus.WAIT_INIT, RpcClientStatus.INITIALIZED);
            log.info("RpcClient init in constructor, ServerListFactory = {}",
                    serverListFactory.getClass().getName());
        }
    }

    /**
     * init server list factory. only can init once.
     *
     * @param serverListFactory serverListFactory
     */
    public RpcClient serverListFactory(ServerListFactory serverListFactory) {
        if (!isWaitInitiated()) {
            return this;
        }
        this.serverListFactory = serverListFactory;
        rpcClientStatus.compareAndSet(RpcClientStatus.WAIT_INIT, RpcClientStatus.INITIALIZED);

        log.info("[{}] RpcClient init, ServerListFactory = {}", rpcClientConfig.name(),
                serverListFactory.getClass().getName());
        return this;
    }

    /**
     * Notify when client disconnected.
     *
     * @param connection connection has disconnected
     */
    protected void notifyDisConnected(Connection connection) {
        if (connectionEventListeners.isEmpty()) {
            return;
        }
        log.info("[{}] Notify disconnected event to listeners", rpcClientConfig.name());
        for (ConnectionEventListener connectionEventListener : connectionEventListeners) {
            try {
                connectionEventListener.onDisConnect(connection);
            } catch (Throwable throwable) {
                log.warn("[{}] Notify disconnect listener error, listener = {}",
                        rpcClientConfig.name(), connectionEventListener.getClass().getName());
            }
        }
    }

    /**
     * Notify when client new connected.
     *
     * @param connection connection has connected
     */
    protected void notifyConnected(Connection connection) {
        if (connectionEventListeners.isEmpty()) {
            return;
        }
        log.info("[{}] Notify connected event to listeners.", rpcClientConfig.name());
        for (ConnectionEventListener connectionEventListener : connectionEventListeners) {
            try {
                connectionEventListener.onConnected(connection);
            } catch (Throwable throwable) {
                log.error("[{}] Notify connect listener error, listener = {}",
                        rpcClientConfig.name(), connectionEventListener.getClass().getName());
            }
        }
    }

    /**
     * check is this client is initiated.
     *
     * @return is wait initiated or not.
     */
    public boolean isWaitInitiated() {
        return this.rpcClientStatus.get() == RpcClientStatus.WAIT_INIT;
    }

    /**
     * check is this client is running.
     *
     * @return is running or not.
     */
    public boolean isRunning() {
        return this.rpcClientStatus.get() == RpcClientStatus.RUNNING;
    }

    /**
     * check is this client is shutdown.
     *
     * @return is shutdown or not.
     */
    public boolean isShutdown() {
        return this.rpcClientStatus.get() == RpcClientStatus.SHUTDOWN;
    }

    /**
     * check if current connected server is in server list, if not switch server.
     */
    public void onServerListChange() {
        if (currentConnection != null && currentConnection.getServerInfo() != null) {
            ServerInfo serverInfo = currentConnection.getServerInfo();
            boolean found = false;
            for (String serverAddress : serverListFactory.getServerList()) {
                if (resolveServerInfo(serverAddress).getAddress().equalsIgnoreCase(serverInfo.getAddress())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                log.info(
                        "Current connected server {} is not in latest server list, switch switchServerAsync",
                        serverInfo.getAddress());
                switchServerAsync();
            }

        }
    }

    /**
     * Start this client.
     */
    public final void start() throws GrpcException {

        boolean success = rpcClientStatus.compareAndSet(RpcClientStatus.INITIALIZED, RpcClientStatus.STARTING);
        if (!success) {
            return;
        }

        clientEventExecutor = new ScheduledThreadPoolExecutor(2,
                new NamedThreadFactory("client.remote.worker"));

        // connection event consumer.
        clientEventExecutor.submit(() -> {
            while (!clientEventExecutor.isTerminated() && !clientEventExecutor.isShutdown()) {
                ConnectionEvent take;
                try {
                    take = eventLinkedBlockingQueue.take();
                    if (take.isConnected()) {
                        notifyConnected(take.connection);
                    } else if (take.isDisConnected()) {
                        notifyDisConnected(take.connection);
                    }
                } catch (Throwable e) {
                    // Do nothing
                }
            }
        });

        clientEventExecutor.submit(() -> {
            while (true) {
                try {
                    if (isShutdown()) {
                        break;
                    }
                    ReconnectContext reconnectContext = reconnectionSignal
                            .poll(rpcClientConfig.connectionKeepAlive(), TimeUnit.MILLISECONDS);
                    if (reconnectContext == null) {
                        // check alive time.
                        if (System.currentTimeMillis() - lastActiveTimeStamp >= rpcClientConfig.connectionKeepAlive()) {
                            boolean isHealthy = healthCheck();
                            if (!isHealthy) {
                                if (currentConnection == null) {
                                    continue;
                                }
                                log.info(
                                        "[{}] Server healthy check fail, currentConnection = {}",
                                        rpcClientConfig.name(), currentConnection.getConnectionId());

                                RpcClientStatus rpcClientStatus = RpcClient.this.rpcClientStatus.get();
                                if (RpcClientStatus.SHUTDOWN.equals(rpcClientStatus)) {
                                    break;
                                }

                                boolean statusFLowSuccess = RpcClient.this.rpcClientStatus
                                        .compareAndSet(rpcClientStatus, RpcClientStatus.UNHEALTHY);
                                if (statusFLowSuccess) {
                                    reconnectContext = new ReconnectContext(null, false);
                                } else {
                                    continue;
                                }

                            } else {
                                lastActiveTimeStamp = System.currentTimeMillis();
                                continue;
                            }
                        } else {
                            continue;
                        }

                    }

                    if (reconnectContext.serverInfo != null) {
                        // clear recommend server if server is not in server list.
                        boolean serverExist = false;
                        for (String server : getServerListFactory().getServerList()) {
                            ServerInfo serverInfo = resolveServerInfo(server);
                            if (serverInfo.getServerIp().equals(reconnectContext.serverInfo.getServerIp())) {
                                serverExist = true;
                                reconnectContext.serverInfo.serverPort = serverInfo.serverPort;
                                break;
                            }
                        }
                        if (!serverExist) {
                            log.info(
                                    "[{}] Recommend server is not in server list, ignore recommend server {}",
                                    rpcClientConfig.name(), reconnectContext.serverInfo.getAddress());

                            reconnectContext.serverInfo = null;

                        }
                    }
                    reconnect(reconnectContext.serverInfo, reconnectContext.onRequestFail);
                } catch (Throwable throwable) {
                    // Do nothing
                }
            }
        });

        // connect to server, try to connect to server sync retryTimes times, async starting if failed.
        Connection connectToServer = null;
        rpcClientStatus.set(RpcClientStatus.STARTING);

        int startUpRetryTimes = rpcClientConfig.retryTimes();
        while (startUpRetryTimes >= 0 && connectToServer == null) {
            try {
                startUpRetryTimes--;
                ServerInfo serverInfo = nextRpcServer();

                log.info("[{}] Try to connect to server on start up, server: {}",
                        rpcClientConfig.name(), serverInfo);

                connectToServer = connectToServer(serverInfo);
            } catch (Throwable e) {
                log.warn("[{}] Fail to connect to server on start up, error message = {}, start up retry times left: {}",
                        rpcClientConfig.name(), e.getMessage(), startUpRetryTimes, e);
            }

        }

        if (connectToServer != null) {
            log.info("[{}] Success to connect to server [{}] on start up, connectionId = {}",
                    rpcClientConfig.name(), connectToServer.getServerInfo().getAddress(),
                    connectToServer.getConnectionId());
            this.currentConnection = connectToServer;
            rpcClientStatus.set(RpcClientStatus.RUNNING);
            eventLinkedBlockingQueue.offer(new ConnectionEvent(ConnectionEvent.CONNECTED, currentConnection));
        } else {
            switchServerAsync();
        }

        registerServerRequestHandler(new ConnectResetRequestHandler());

        // register client detection request.
        registerServerRequestHandler((request, connection) -> {
            if (request instanceof ClientDetectionRequest) {
                return new ClientDetectionResponse();
            }

            return null;
        });

    }

    class ConnectResetRequestHandler implements ServerRequestHandler {

        @Override
        public Response requestReply(Request request, Connection connection) {

            if (request instanceof ConnectResetRequest) {

                try {
                    synchronized (RpcClient.this) {
                        if (isRunning()) {
                            ConnectResetRequest connectResetRequest = (ConnectResetRequest) request;
                            if (StringUtils.isNotBlank(connectResetRequest.getServerIp())) {
                                ServerInfo serverInfo = resolveServerInfo(
                                        connectResetRequest.getServerIp() + COLON + connectResetRequest
                                                .getServerPort());
                                switchServerAsync(serverInfo, false);
                            } else {
                                switchServerAsync();
                            }
                            afterReset(connectResetRequest);
                        }
                    }
                } catch (Exception e) {
                    log.error("[{}] Switch server error, {}", rpcClientConfig.name(), e);
                }
                return new ConnectResetResponse();
            }
            return null;
        }
    }

    /**
     * . invoke after receiving reset request
     *
     * @param request request for resetting
     */
    protected void afterReset(ConnectResetRequest request) {
        // hook for GrpcClient
    }

    @Override
    public void shutdown() throws GrpcException {
        log.info("Shutdown rpc client, set status to shutdown");
        rpcClientStatus.set(RpcClientStatus.SHUTDOWN);
        log.info("Shutdown client event executor {}", clientEventExecutor);
        if (clientEventExecutor != null) {
            clientEventExecutor.shutdownNow();
        }
        closeConnection(currentConnection);
    }

    private boolean healthCheck() {
        HealthCheckRequest healthCheckRequest = new HealthCheckRequest();
        if (this.currentConnection == null) {
            return false;
        }
        int reTryTimes = rpcClientConfig.healthCheckRetryTimes();
        Random random = new Random();
        while (reTryTimes >= 0) {
            reTryTimes--;
            try {
                if (reTryTimes > 1) {
                    Thread.sleep(random.nextInt(500));
                }
                Response response = this.currentConnection
                        .request(healthCheckRequest, rpcClientConfig.healthCheckTimeOut());
                // not only check server is ok, also check connection is register.
                return response != null && response.isSuccess();
            } catch (Exception e) {
                // ignore
            }
        }
        return false;
    }

    public void switchServerAsyncOnRequestFail() {
        switchServerAsync(null, true);
    }

    public void switchServerAsync() {
        switchServerAsync(null, false);
    }

    protected void switchServerAsync(final ServerInfo recommendServerInfo, boolean onRequestFail) {
        reconnectionSignal.offer(new ReconnectContext(recommendServerInfo, onRequestFail));
    }

    /**
     * switch server .
     */
    protected void reconnect(final ServerInfo recommendServerInfo, boolean onRequestFail) {

        try {

            AtomicReference<ServerInfo> recommendServer = new AtomicReference<>(recommendServerInfo);
            if (onRequestFail && healthCheck()) {
                log.info("[{}] Server check success, currentServer is {} ",
                        rpcClientConfig.name(), currentConnection.getServerInfo().getAddress());
                rpcClientStatus.set(RpcClientStatus.RUNNING);
                return;
            }

            log.info("[{}] Try to reconnect to a new server, server is {}",
                    rpcClientConfig.name(), recommendServerInfo == null ? " not appointed, will choose a random server."
                            : (recommendServerInfo.getAddress() + ", will try it once."));

            // loop until start client success.
            boolean switchSuccess = false;

            int reConnectTimes = 0;
            int retryTurns = 0;
            Exception lastException;
            while (!switchSuccess && !isShutdown()) {

                // 1.get a new server
                ServerInfo serverInfo = null;
                try {
                    serverInfo = recommendServer.get() == null ? nextRpcServer() : recommendServer.get();
                    // 2.create a new channel to new server
                    Connection connectionNew = connectToServer(serverInfo);
                    if (connectionNew != null) {
                        log.info("[{}] Success to connect a server [{}], connectionId = {}",
                                rpcClientConfig.name(), serverInfo.getAddress(),
                                connectionNew.getConnectionId());
                        // successfully create a new connect.
                        if (currentConnection != null) {
                            log.info(
                                    "[{}] Abandon prev connection, server is {}, connectionId is {}",
                                    rpcClientConfig.name(), currentConnection.getServerInfo().getAddress(),
                                    currentConnection.getConnectionId());
                            // set current connection to enable connection event.
                            currentConnection.setAbandon(true);
                            closeConnection(currentConnection);
                        }
                        currentConnection = connectionNew;
                        rpcClientStatus.set(RpcClientStatus.RUNNING);
                        switchSuccess = true;
                        eventLinkedBlockingQueue.add(new ConnectionEvent(ConnectionEvent.CONNECTED, currentConnection));
                        return;
                    }

                    // close connection if client is already shutdown.
                    if (isShutdown()) {
                        closeConnection(currentConnection);
                    }

                    lastException = null;

                } catch (Throwable throwable) {
                    log.error("Fail to connect server, error = {}", throwable.getMessage());
                    lastException = new Exception(throwable);
                } finally {
                    recommendServer.set(null);
                }

                if (CollectionUtils.isEmpty(RpcClient.this.serverListFactory.getServerList())) {
                    throw new Exception("server list is empty");
                }

                if (reConnectTimes > 0
                        && reConnectTimes % RpcClient.this.serverListFactory.getServerList().size() == 0) {
                    log.info(
                            "[{}] Fail to connect server, after trying {} times, last try server is {}, error = {}",
                            rpcClientConfig.name(), reConnectTimes, serverInfo,
                            lastException == null ? "unknown" : lastException);
                    if (Integer.MAX_VALUE == retryTurns) {
                        retryTurns = 50;
                    } else {
                        retryTurns++;
                    }
                }

                reConnectTimes++;

                try {
                    // sleep x milliseconds to switch next server.
                    if (!isRunning()) {
                        // first round, try servers at a delay 100ms;second round, 200ms; max delays 5s. to be reconsidered.
                        Thread.sleep(Math.min(retryTurns + 1, 50) * 100L);
                    }
                } catch (InterruptedException e) {
                    // Do nothing.
                    // set the interrupted flag
                    Thread.currentThread().interrupt();
                }
            }

            if (isShutdown()) {
                log.info("[{}] Client is shutdown, stop reconnect to server",
                        rpcClientConfig.name());
            }

        } catch (Exception e) {
            log.warn("[{}] Fail to reconnect to server, error is {}", rpcClientConfig.name(),
                    e);
        }
    }

    private void closeConnection(Connection connection) {
        if (connection != null) {
            log.info("Close current connection {}", connection.getConnectionId());
            connection.close();
            eventLinkedBlockingQueue.add(new ConnectionEvent(ConnectionEvent.DISCONNECTED, connection));
        }
    }

    /**
     * get connection type of this client.
     *
     * @return ConnectionType.
     */
    public abstract ConnectionType getConnectionType();

    /**
     * increase offset of the nacos server port for the rpc server port.
     *
     * @return rpc port offset
     */
    public abstract int rpcPortOffset();

    /**
     * get current server.
     *
     * @return server info.
     */
    public ServerInfo getCurrentServer() {
        if (this.currentConnection != null) {
            return currentConnection.getServerInfo();
        }
        return null;
    }

    /**
     * send request.
     *
     * @param request request.
     * @return response from server.
     */
    public Response request(Request request) throws GrpcException {
        return request(request, rpcClientConfig.timeOutMills());
    }

    /**
     * send request.
     *
     * @param request request.
     * @return response from server.
     */
    public Response request(Request request, long timeoutMills) throws GrpcException {
        int retryTimes = 0;
        Response response;
        Throwable exceptionThrow = null;
        long start = System.currentTimeMillis();
        while (retryTimes <= rpcClientConfig.retryTimes() && (timeoutMills <= 0
                || System.currentTimeMillis() < timeoutMills + start)) {
            boolean waitReconnect = false;
            try {
                if (this.currentConnection == null || !isRunning()) {
                    waitReconnect = true;
                    throw new GrpcException(GrpcException.CLIENT_DISCONNECT,
                            "Client not connected, current status:" + rpcClientStatus.get());
                }
                response = this.currentConnection.request(request, timeoutMills);
                if (response == null) {
                    throw new GrpcException(SERVER_ERROR, "Unknown Exception.");
                }
                if (response instanceof ErrorResponse) {
                    if (response.getErrorCode() == GrpcException.UN_REGISTER) {
                        synchronized (this) {
                            waitReconnect = true;
                            if (rpcClientStatus.compareAndSet(RpcClientStatus.RUNNING, RpcClientStatus.UNHEALTHY)) {
                                log.error("Connection is unregistered, switch server, connectionId = {}, request = {}",
                                        currentConnection.getConnectionId(), request.getClass().getSimpleName());
                                switchServerAsync();
                            }
                        }

                    }
                    throw new GrpcException(response.getErrorCode(), response.getMessage());
                }
                // return response.
                lastActiveTimeStamp = System.currentTimeMillis();
                return response;

            } catch (Throwable e) {
                if (waitReconnect) {
                    try {
                        // wait client to reconnect.
                        Thread.sleep(Math.min(100, timeoutMills / 3));
                    } catch (Exception exception) {
                        // Do nothing.
                    }
                }

                log.error("Send request fail, request = {}, retryTimes = {}, errorMessage = {}", request, retryTimes,
                        e.getMessage());

                exceptionThrow = e;

            }
            retryTimes++;

        }

        if (rpcClientStatus.compareAndSet(RpcClientStatus.RUNNING, RpcClientStatus.UNHEALTHY)) {
            switchServerAsyncOnRequestFail();
        }

        if (exceptionThrow != null) {
            throw (exceptionThrow instanceof GrpcException) ? (GrpcException) exceptionThrow
                    : new GrpcException(SERVER_ERROR, exceptionThrow);
        } else {
            throw new GrpcException(SERVER_ERROR, "Request fail, unknown Error");
        }
    }

    /**
     * send async request.
     *
     * @param request request.
     */
    public void asyncRequest(Request request, RequestCallBack callback) throws GrpcException {
        int retryTimes = 0;
        Throwable exceptionToThrow = null;
        long start = System.currentTimeMillis();
        while (retryTimes <= rpcClientConfig.retryTimes() && System.currentTimeMillis() < start + callback
                .getTimeout()) {
            boolean waitReconnect = false;
            try {
                if (this.currentConnection == null || !isRunning()) {
                    waitReconnect = true;
                    throw new GrpcException(GrpcException.CLIENT_DISCONNECT, "Client not connected.");
                }
                this.currentConnection.asyncRequest(request, callback);
                return;
            } catch (Throwable e) {
                if (waitReconnect) {
                    try {
                        // wait client to reconnect.
                        Thread.sleep(Math.min(100, callback.getTimeout() / 3));
                    } catch (Exception exception) {
                        // Do nothing.
                    }
                }
                log.error("[{}] Send request fail, request = {}, retryTimes = {}, errorMessage = {}",
                        rpcClientConfig.name(), request, retryTimes, e.getMessage());
                exceptionToThrow = e;

            }
            retryTimes++;

        }

        if (rpcClientStatus.compareAndSet(RpcClientStatus.RUNNING, RpcClientStatus.UNHEALTHY)) {
            switchServerAsyncOnRequestFail();
        }
        if (exceptionToThrow != null) {
            throw (exceptionToThrow instanceof GrpcException) ? (GrpcException) exceptionToThrow
                    : new GrpcException(SERVER_ERROR, exceptionToThrow);
        } else {
            throw new GrpcException(SERVER_ERROR, "AsyncRequest fail, unknown error");
        }
    }

    /**
     * send async request.
     *
     * @param request request.
     * @return request future.
     */
    public RequestFuture requestFuture(Request request) throws GrpcException {
        int retryTimes = 0;
        long start = System.currentTimeMillis();
        Exception exceptionToThrow = null;
        while (retryTimes <= rpcClientConfig.retryTimes() && System.currentTimeMillis() < start + rpcClientConfig
                .timeOutMills()) {
            boolean waitReconnect = false;
            try {
                if (this.currentConnection == null || !isRunning()) {
                    waitReconnect = true;
                    throw new GrpcException(GrpcException.CLIENT_DISCONNECT, "Client not connected.");
                }
                return this.currentConnection.requestFuture(request);
            } catch (Exception e) {
                if (waitReconnect) {
                    try {
                        // wait client to reconnect.
                        Thread.sleep(100L);
                    } catch (Exception exception) {
                        // Do nothing.
                    }
                }
                log.error("[{}] Send request fail, request = {}, retryTimes = {}, errorMessage = {}",
                        rpcClientConfig.name(), request, retryTimes, e.getMessage());
                exceptionToThrow = e;

            }
            retryTimes++;
        }

        if (rpcClientStatus.compareAndSet(RpcClientStatus.RUNNING, RpcClientStatus.UNHEALTHY)) {
            switchServerAsyncOnRequestFail();
        }

        if (exceptionToThrow != null) {
            throw (exceptionToThrow instanceof GrpcException) ? (GrpcException) exceptionToThrow
                    : new GrpcException(SERVER_ERROR, exceptionToThrow);
        } else {
            throw new GrpcException(SERVER_ERROR, "Request future fail, unknown error");
        }

    }

    /**
     * connect to server.
     *
     * @param serverInfo server address to connect.
     * @return return connection when successfully connect to server, or null if failed.
     * @throws Exception exception when fail to connect to server.
     */
    public abstract Connection connectToServer(ServerInfo serverInfo) throws Exception;

    /**
     * handle server request.
     *
     * @param request request.
     * @return response.
     */
    protected Response handleServerRequest(final Request request) {

        log.info("[{}] Receive server push request, request = {}, requestId = {}",
                rpcClientConfig.name(), request.getClass().getSimpleName(), request.getRequestId());
        lastActiveTimeStamp = System.currentTimeMillis();
        for (ServerRequestHandler serverRequestHandler : serverRequestHandlers) {
            try {
                Response response = serverRequestHandler.requestReply(request, currentConnection);

                if (response != null) {
                    log.info("[{}] Ack server push request, request = {}, requestId = {}",
                            rpcClientConfig.name(), request.getClass().getSimpleName(), request.getRequestId());
                    return response;
                }
            } catch (Exception e) {
                log.info("[{}] HandleServerRequest:{}, errorMessage = {}",
                        rpcClientConfig.name(), serverRequestHandler.getClass().getName(), e.getMessage());
                throw e;
            }

        }
        return null;
    }

    /**
     * Register connection handler. Will be notified when inner connection's state changed.
     *
     * @param connectionEventListener connectionEventListener
     */
    public synchronized void registerConnectionListener(ConnectionEventListener connectionEventListener) {

        log.info("[{}] Registry connection listener to current client:{}",
                rpcClientConfig.name(), connectionEventListener.getClass().getName());
        this.connectionEventListeners.add(connectionEventListener);
    }

    /**
     * Register serverRequestHandler, the handler will handle the request from server side.
     *
     * @param serverRequestHandler serverRequestHandler
     */
    public synchronized void registerServerRequestHandler(ServerRequestHandler serverRequestHandler) {
        log.info("[{}] Register server push request handler:{}", rpcClientConfig.name(),
                serverRequestHandler.getClass().getName());

        this.serverRequestHandlers.add(serverRequestHandler);
    }

    /**
     * Getter method for property <tt>name</tt>.
     *
     * @return property value of name
     */
    public String getName() {
        return rpcClientConfig.name();
    }

    /**
     * Getter method for property <tt>serverListFactory</tt>.
     *
     * @return property value of serverListFactory
     */
    public ServerListFactory getServerListFactory() {
        return serverListFactory;
    }

    protected ServerInfo nextRpcServer() {
        String serverAddress = getServerListFactory().genNextServer();
        return resolveServerInfo(serverAddress);
    }

    protected ServerInfo currentRpcServer() {
        String serverAddress = getServerListFactory().getCurrentServer();
        return resolveServerInfo(serverAddress);
    }

    /**
     * resolve server info.
     *
     * @param serverAddress address.
     * @return
     */
    @SuppressWarnings("PMD.UndefineMagicConstantRule")
    private ServerInfo resolveServerInfo(String serverAddress) {
        Matcher matcher = EXCLUDE_PROTOCOL_PATTERN.matcher(serverAddress);
        if (matcher.find()) {
            serverAddress = matcher.group(1);
        }
        String[] ipPortTuple = InternetAddressUtil.splitIPPortStr(serverAddress);
        int defaultPort = Integer.parseInt(System.getProperty("nacos.server.port", "8848"));
        String serverPort = CollectionUtils.getOrDefault(ipPortTuple, 1, Integer.toString(defaultPort));

        return new ServerInfo(ipPortTuple[0], NumberUtils.toInt(serverPort, defaultPort));
    }

    public static class ServerInfo {

        protected String serverIp;

        protected int serverPort;

        public ServerInfo() {

        }

        public ServerInfo(String serverIp, int serverPort) {
            this.serverPort = serverPort;
            this.serverIp = serverIp;
        }

        /**
         * get address, ip:port.
         *
         * @return address.
         */
        public String getAddress() {
            return serverIp + COLON + serverPort;
        }

        /**
         * Setter method for property <tt>serverIp</tt>.
         *
         * @param serverIp value to be assigned to property serverIp
         */
        public void setServerIp(String serverIp) {
            this.serverIp = serverIp;
        }

        /**
         * Setter method for property <tt>serverPort</tt>.
         *
         * @param serverPort value to be assigned to property serverPort
         */
        public void setServerPort(int serverPort) {
            this.serverPort = serverPort;
        }

        /**
         * Getter method for property <tt>serverIp</tt>.
         *
         * @return property value of serverIp
         */
        public String getServerIp() {
            return serverIp;
        }

        /**
         * Getter method for property <tt>serverPort</tt>.
         *
         * @return property value of serverPort
         */
        public int getServerPort() {
            return serverPort;
        }

        @Override
        public String toString() {
            return "{serverIp = '" + serverIp + '\'' + ", server main port = " + serverPort + '}';
        }
    }

    public static class ConnectionEvent {

        public static final int CONNECTED = 1;

        public static final int DISCONNECTED = 0;

        int eventType;

        Connection connection;

        public ConnectionEvent(int eventType, Connection connection) {
            this.eventType = eventType;
            this.connection = connection;
        }

        public boolean isConnected() {
            return eventType == CONNECTED;
        }

        public boolean isDisConnected() {
            return eventType == DISCONNECTED;
        }
    }

    static class ReconnectContext {

        public ReconnectContext(ServerInfo serverInfo, boolean onRequestFail) {
            this.onRequestFail = onRequestFail;
            this.serverInfo = serverInfo;
        }

        boolean onRequestFail;

        ServerInfo serverInfo;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

}
