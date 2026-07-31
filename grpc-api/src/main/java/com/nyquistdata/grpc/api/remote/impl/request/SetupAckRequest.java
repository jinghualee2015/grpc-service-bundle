package com.nyquistdata.grpc.api.remote.impl.request;

import java.util.Map;

import static com.nyquistdata.grpc.api.domains.constants.GrpcConstants.INTERNAL_MODULE;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version: 1.0.0
 * @date: 2026/7/26
 * @description: server tells client that the connection is established.
 */
public class SetupAckRequest extends ServerRequest {
    private Map<String, Boolean> abilityTable;

    public SetupAckRequest() {
    }

    public SetupAckRequest(Map<String, Boolean> abilityTable) {
        this.abilityTable = abilityTable;
    }

    public Map<String, Boolean> getAbilityTable() {
        return abilityTable;
    }

    public void setAbilityTable(Map<String, Boolean> abilityTable) {
        this.abilityTable = abilityTable;
    }

    @Override
    public String getModule() {
        return INTERNAL_MODULE;
    }
}
