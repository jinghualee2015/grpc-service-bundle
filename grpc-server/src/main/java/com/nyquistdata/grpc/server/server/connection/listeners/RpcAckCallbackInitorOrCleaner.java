package com.nyquistdata.grpc.server.server.connection.listeners;

import com.nyquistdata.grpc.server.server.connection.Connection;
import com.nyquistdata.grpc.server.server.connection.listeners.support.ClientConnectionEventListenerRegistry;
import com.nyquistdata.grpc.server.server.connection.listeners.support.RpcAckCallbackSynchronizer;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version: 1.0.0
 * @date: 2026/7/26
 * @description: remote connection event listener
 */
public class RpcAckCallbackInitorOrCleaner extends ClientConnectionEventListener {

    public RpcAckCallbackInitorOrCleaner(ClientConnectionEventListenerRegistry clientConnectionEventListenerRegistry) {
        super(clientConnectionEventListenerRegistry);
    }

    @Override
    public void clientConnected(Connection connect) {
        RpcAckCallbackSynchronizer.initContextIfNecessary(connect.getMetaInfo().getConnectionId());
    }

    @Override
    public void clientDisConnected(Connection connect) {
        RpcAckCallbackSynchronizer.clearContext(connect.getMetaInfo().getConnectionId());
    }
}
