package com.nyquistdata.client.client;

import java.util.List;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version:
 * @date: 2026/7/28
 * @description: server list factory, use to an inner client to connect and switch servers
 */
public interface ServerListFactory {
    /**
     * switch to a new server and get it.
     *
     * @return server " ip:port".
     */
    String genNextServer();

    /**
     * get current server.
     * @return server " ip:port".
     */
    String getCurrentServer();

    /**
     * get current server.
     *
     * @return servers.
     */
    List<String> getServerList();
}
