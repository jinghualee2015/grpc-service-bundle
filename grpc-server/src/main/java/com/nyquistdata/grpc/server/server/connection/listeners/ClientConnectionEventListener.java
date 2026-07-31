package com.nyquistdata.grpc.server.server.connection.listeners;

import com.nyquistdata.grpc.server.server.connection.Connection;
import com.nyquistdata.grpc.server.server.connection.listeners.support.ClientConnectionEventListenerRegistry;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version: 1.0.0
 * @date: 2026/7/26
 * @description: client connection event listener
 */
public abstract class ClientConnectionEventListener {
    /**
     * listener name.
     */
    private String name;

    protected final ClientConnectionEventListenerRegistry clientConnectionEventListenerRegistry;

    protected ClientConnectionEventListener(ClientConnectionEventListenerRegistry clientConnectionEventListenerRegistry) {
        this.clientConnectionEventListenerRegistry = clientConnectionEventListenerRegistry;
        this.init();
    }

    public void init() {
        clientConnectionEventListenerRegistry.registerClientConnectionEventListener(this);
    }

    /**
     * Getter method for property <tt>name</tt>.
     *
     * @return property value of name
     */
    public String getName() {
        return name;
    }

    /**
     * Setter method for property <tt>name</tt>.
     *
     * @param name value to be assigned to property name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * notified when a client connected.
     *
     * @param connect connect.
     */
    public abstract void clientConnected(Connection connect);

    /**
     * notified when a client disconnected.
     *
     * @param connect connect.
     */
    public abstract void clientDisConnected(Connection connect);
}
