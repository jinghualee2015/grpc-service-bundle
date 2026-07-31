package com.nyquistdata.grpc.server.server.connection;

import com.nyquistdata.grpc.api.remote.Requester;

import java.util.Map;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version: 1.0.0
 * @date: 2026/7/26
 * @description: connection
 */
public abstract class Connection implements Requester {
    private boolean traced = false;

    private Map<String, Boolean> abilityTable;

    private final ConnectionMeta metaInfo;

    public Connection(ConnectionMeta metaInfo) {
        this.metaInfo = metaInfo;
    }

    public Map<String, String> getLabels() {
        return metaInfo.getLabels();
    }

    public boolean isTraced() {
        return traced;
    }

    public void setTraced(boolean traced) {
        this.traced = traced;
    }

    public void setAbilityTable(Map<String, Boolean> abilityTable) {
        this.abilityTable = abilityTable;
    }

    public Map<String, Boolean> getAbilityTable() {
        return this.abilityTable;
    }

    /**
     * check is connected.
     *
     * @return if connection or not,check the inner connection is active.
     */
    public abstract boolean isConnected();

    /**
     * Update last Active Time to now.
     */
    public void freshActiveTime() {
        metaInfo.setLastActiveTime(System.currentTimeMillis());
    }

    /**
     * Getter method for property <tt>metaInfo</tt>.
     *
     * @return property value of metaInfo
     */
    public ConnectionMeta getMetaInfo() {
        return metaInfo;
    }

    @Override
    public String toString() {
        return "Connection{" + "traced=" + traced + ", abilities=" + abilityTable + ", metaInfo=" + metaInfo + '}';
    }
}
