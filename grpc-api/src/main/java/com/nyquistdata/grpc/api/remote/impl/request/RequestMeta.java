package com.nyquistdata.grpc.api.remote.impl.request;

import java.util.HashMap;
import java.util.Map;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version: 1.0.0
 * @date: 2026/7/26
 * @description: request meta info
 */
public class RequestMeta {
    private String connectionId = "";

    private String clientIp = "";

    private String clientVersion = "";

    private Map<String, String> labels = new HashMap<>();

    private Map<String, Boolean> abilityTable;

    /**
     * Setter method for property <tt>abilityTable</tt>.
     *
     * @param  abilityTable property value of clientVersion
     */
    public void setAbilityTable(Map<String, Boolean> abilityTable) {
        this.abilityTable = abilityTable;
    }

    /**
     * Getter method for property <tt>clientVersion</tt>.
     *
     * @return property value of clientVersion
     */
    public String getClientVersion() {
        return clientVersion;
    }

    /**
     * Setter method for property <tt>clientVersion</tt>.
     *
     * @param clientVersion value to be assigned to property clientVersion
     */
    public void setClientVersion(String clientVersion) {
        this.clientVersion = clientVersion;
    }

    /**
     * Getter method for property <tt>labels</tt>.
     *
     * @return property value of labels
     */
    public Map<String, String> getLabels() {
        return labels;
    }

    /**
     * Setter method for property <tt>labels</tt>.
     *
     * @param labels value to be assigned to property labels
     */
    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }

    /**
     * Getter method for property <tt>connectionId</tt>.
     *
     * @return property value of connectionId
     */
    public String getConnectionId() {
        return connectionId;
    }

    /**
     * Setter method for property <tt>connectionId</tt>.
     *
     * @param connectionId value to be assigned to property connectionId
     */
    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    /**
     * Getter method for property <tt>clientIp</tt>.
     *
     * @return property value of clientIp
     */
    public String getClientIp() {
        return clientIp;
    }

    /**
     * Setter method for property <tt>clientIp</tt>.
     *
     * @param clientIp value to be assigned to property clientIp
     */
    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    @Override
    public String toString() {
        return "RequestMeta{" + "connectionId='" + connectionId + '\'' + ", clientIp='" + clientIp + '\''
                + ", clientVersion='" + clientVersion + '\'' + ", labels=" + labels + '}';
    }
}
