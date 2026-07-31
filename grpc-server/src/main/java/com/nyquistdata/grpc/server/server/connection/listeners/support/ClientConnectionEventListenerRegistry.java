package com.nyquistdata.grpc.server.server.connection.listeners.support;

import com.nyquistdata.grpc.server.server.connection.Connection;
import com.nyquistdata.grpc.server.server.connection.listeners.ClientConnectionEventListener;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version: 1.0.0
 * @date: 2026/7/26
 * @description: registry for client connection event listeners
 */
@Slf4j
public class ClientConnectionEventListenerRegistry {
    final List<ClientConnectionEventListener> clientConnectionEventListeners = new ArrayList<>();

    /**
     * notify where a new client connected.
     *
     * @param connection connection that new created.
     */
    public void notifyClientConnected(final Connection connection) {

        for (ClientConnectionEventListener clientConnectionEventListener : clientConnectionEventListeners) {
            try {
                clientConnectionEventListener.clientConnected(connection);
            } catch (Throwable throwable) {
                log.info("[NotifyClientConnected] failed for listener {}", clientConnectionEventListener.getName(),
                                throwable);

            }
        }

    }

    /**
     * notify where a new client disconnected.
     *
     * @param connection connection that disconnected.
     */
    public void notifyClientDisConnected(final Connection connection) {

        for (ClientConnectionEventListener clientConnectionEventListener : clientConnectionEventListeners) {
            try {
                clientConnectionEventListener.clientDisConnected(connection);
            } catch (Throwable throwable) {
                log.info("[NotifyClientDisConnected] failed for listener {}",
                        clientConnectionEventListener.getName(), throwable);
            }
        }

    }

    /**
     * register ClientConnectionEventListener.
     *
     * @param listener listener.
     */
    public void registerClientConnectionEventListener(ClientConnectionEventListener listener) {
        log.info("[ClientConnectionEventListenerRegistry] registry listener - " + listener.getClass()
                .getSimpleName());
        this.clientConnectionEventListeners.add(listener);
    }
}
