package com.nyquistdata.client.client.connection.listener;

import com.nyquistdata.client.client.connection.Connection;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version: 1.0.0
 * @date: 2026/7/29
 * @description:connect event listener of client side
 */
public interface ConnectionEventListener {
    /**
     * notify when connected to server
     *
     * @param connection connection has connected
     */
    void onConnected(Connection connection);

    /**
     * notify when disconnected to server
     *
     * @param connection the connection has disconnected.
     */
    void onDisConnect(Connection connection);
}
