package com.nyquistdata.grpc.api.remote.impl.response;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version:
 * @date: 2026/7/26
 * @description: response of server check
 */
public class ServerCheckResponse extends Response{
    private String connectionId;

    private boolean supportAbilityNegotiation;

    public ServerCheckResponse() {

    }

    public ServerCheckResponse(String connectionId, boolean supportAbilityNegotiation) {
        this.connectionId = connectionId;
        this.supportAbilityNegotiation = supportAbilityNegotiation;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    public boolean isSupportAbilityNegotiation() {
        return supportAbilityNegotiation;
    }

    public void setSupportAbilityNegotiation(boolean supportAbilityNegotiation) {
        this.supportAbilityNegotiation = supportAbilityNegotiation;
    }
}
