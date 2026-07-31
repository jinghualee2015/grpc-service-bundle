package com.nyquistdata.grpc.server.server.connection;

import com.nyquistdata.grpc.api.exceptions.ConnectionAlreadyClosedException;
import com.nyquistdata.grpc.api.remote.impl.request.ConnectResetRequest;
import com.nyquistdata.grpc.api.utils.StringUtils;
import com.nyquistdata.grpc.server.server.connection.listeners.support.ClientConnectionEventListenerRegistry;
import com.nyquistdata.grpc.server.server.enjector.GrpcRuntimeConnectionEjector;
import com.nyquistdata.grpc.server.server.enjector.RuntimeConnectionEjector;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static com.nyquistdata.grpc.api.domains.constants.GrpcConstants.*;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version: 1.0.0
 * @date: 2026/7/26
 * @description: connection manager
 */
@Slf4j
public class ConnectionManager {
    private Map<String, AtomicInteger> connectionForClientIp = new ConcurrentHashMap<>(16);

    public Map<String, Connection> connections = new ConcurrentHashMap<>();

    private RuntimeConnectionEjector runtimeConnectionEjector;

    private ClientConnectionEventListenerRegistry clientConnectionEventListenerRegistry;

    public ConnectionManager(ClientConnectionEventListenerRegistry clientConnectionEventListenerRegistry) {
        this.clientConnectionEventListenerRegistry = clientConnectionEventListenerRegistry;
    }

    /**
     * check connection id is valid.
     *
     * @param connectionId connectionId to be check.
     * @return is valid or not.
     */
    public boolean checkValid(String connectionId) {
        return connections.containsKey(connectionId);
    }

    /**
     * register a new connect.
     *
     * @param connectionId connectionId
     * @param connection   connection
     */
    public synchronized boolean register(String connectionId, Connection connection) {

        if (connection.isConnected()) {
            String clientIp = connection.getMetaInfo().clientIp;
            if (connections.containsKey(connectionId)) {
                return true;
            }
            connections.put(connectionId, connection);
            connectionForClientIp.computeIfAbsent(clientIp, k -> new AtomicInteger(0)).getAndIncrement();

            clientConnectionEventListenerRegistry.notifyClientConnected(connection);

            log.info("new connection registered successfully, connectionId = {},connection={} ", connectionId,
                    connection);
            return true;

        }
        return false;

    }


    /**
     * unregister a connection .
     *
     * @param connectionId connectionId.
     */
    public synchronized void unregister(String connectionId) {
        Connection remove = this.connections.remove(connectionId);
        if (remove != null) {
            String clientIp = remove.getMetaInfo().clientIp;
            AtomicInteger atomicInteger = connectionForClientIp.get(clientIp);
            if (atomicInteger != null) {
                int count = atomicInteger.decrementAndGet();
                if (count <= 0) {
                    connectionForClientIp.remove(clientIp);
                }
            }
            remove.close();
            log.info("[{}]Connection unregistered successfully. ", connectionId);
            clientConnectionEventListenerRegistry.notifyClientDisConnected(remove);
        }
    }

    /**
     * get by connection id.
     *
     * @param connectionId connection id.
     * @return connection of the id.
     */
    public Connection getConnection(String connectionId) {
        return connections.get(connectionId);
    }

    /**
     * get by client ip.
     *
     * @param clientIp client ip.
     * @return connections of the client ip.
     */
    public List<Connection> getConnectionByIp(String clientIp) {
        Set<Map.Entry<String, Connection>> entries = connections.entrySet();
        List<Connection> connections = new ArrayList<>();
        for (Map.Entry<String, Connection> entry : entries) {
            Connection value = entry.getValue();
            if (clientIp.equals(value.getMetaInfo().clientIp)) {
                connections.add(value);
            }
        }
        return connections;
    }

    /**
     * init connection ejector.
     */
    public void initConnectionEjector() {
        String connectionRuntimeEjector = null;
        if (runtimeConnectionEjector == null) {
            log .info("Fail to find connection runtime ejector for name {},use default", connectionRuntimeEjector);
            GrpcRuntimeConnectionEjector ejector = new GrpcRuntimeConnectionEjector();
            ejector.setConnectionManager(this);
            runtimeConnectionEjector = ejector;
        }
    }

    /**
     * get current connections count.
     *
     * @return get all connection count
     */
    public int getCurrentConnectionCount() {
        return this.connections.size();
    }

    /**
     * refresh connection active time.
     *
     * @param connectionId connectionId.
     */
    public void refreshActiveTime(String connectionId) {
        Connection connection = connections.get(connectionId);
        if (connection != null) {
            connection.freshActiveTime();
        }
    }

    /**
     * Start Task：Expel the connection which active Time expire.
     */
    @PostConstruct
    public void start() {
        initConnectionEjector();
    }

    public void loadCount(int loadClient, String redirectAddress) {
        runtimeConnectionEjector.setLoadClient(loadClient);
        runtimeConnectionEjector.setRedirectAddress(redirectAddress);
    }

    /**
     * send load request to specific connectionId.
     *
     * @param connectionId    connection id of client.
     * @param redirectAddress server address to redirect.
     * @return whether remove connection.
     */
    public boolean loadSingle(String connectionId, String redirectAddress) {
        Connection connection = getConnection(connectionId);

        if (connection != null) {
            if (connection.getMetaInfo().isSdkSource()) {
                ConnectResetRequest connectResetRequest = new ConnectResetRequest();
                if (!StringUtils.isBlank(redirectAddress) && redirectAddress.contains(COLON)) {
                    String[] split = redirectAddress.split(COLON);
                    connectResetRequest.setServerIp(split[0]);
                    connectResetRequest.setServerPort(split[1]);
                    connectResetRequest.setConnectionId(connectionId);
                }
                try {
                    connection.request(connectResetRequest, 3000L);
                } catch (                                                                                                                                                                                                            ConnectionAlreadyClosedException e) {
                    unregister(connectionId);
                } catch (Exception e) {
                    log.error("error occurs when expel connection, connectionId: {} ", connectionId, e);
                    return false;
                }
            }
        }
        return true;

    }

    /**
     * get all client count.
     *
     * @return client count.
     */
    public int currentClientsCount() {
        return connections.size();
    }

    /**
     * get client count with labels filter.
     *
     * @param filterLabels label to filter client count.
     * @return count with the specific filter labels.
     */
    public int currentClientsCount(Map<String, String> filterLabels) {
        int count = 0;
        for (Connection connection : connections.values()) {
            Map<String, String> labels = connection.getMetaInfo().labels;
            boolean disMatchFound = false;
            for (Map.Entry<String, String> entry : filterLabels.entrySet()) {
                if (!entry.getValue().equals(labels.get(entry.getKey()))) {
                    disMatchFound = true;
                    break;
                }
            }
            if (!disMatchFound) {
                count++;
            }
        }
        return count;
    }

    /**
     * get client count from sdk.
     *
     * @return sdk client count.
     */
    public int currentSdkClientCount() {
        Map<String, String> filter = new HashMap<>(2);
        filter.put(LABEL_SOURCE, LABEL_SOURCE_SDK);
        return currentClientsCount(filter);
    }


    public Map<String, Connection> currentClients() {
        return connections;
    }

    public Map<String, AtomicInteger> getConnectionForClientIp() {
        return connectionForClientIp;
    }
}
