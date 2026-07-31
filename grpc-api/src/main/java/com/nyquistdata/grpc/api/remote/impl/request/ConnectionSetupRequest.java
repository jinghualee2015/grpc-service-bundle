package com.nyquistdata.grpc.api.remote.impl.request;

import java.util.HashMap;
import java.util.Map;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version: 1.0.0
 * @date: 2026/7/26
 * @description: request to setup a connection
 */
public class ConnectionSetupRequest extends InternalRequest {
    private String clientVersion;
    private String tenant;
    private Map<String, String> labels = new HashMap<>();
    private Map<String, Boolean> abilityTable;

    public ConnectionSetupRequest() {

    }

    public String getClientVersion() {
        return clientVersion;
    }

    public void setClientVersion(String clientVersion) {
        this.clientVersion = clientVersion;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public Map<String, Boolean> getAbilityTable() {
        return abilityTable;
    }

    public void setAbilityTable(Map<String, Boolean> abilityTable) {
        this.abilityTable = abilityTable;
    }
}
