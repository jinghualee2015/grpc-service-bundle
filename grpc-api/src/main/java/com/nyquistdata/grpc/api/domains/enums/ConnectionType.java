package com.nyquistdata.grpc.api.domains.enums;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version: 1.0.0
 * @date: 2026/7/25
 * @description:
 */
public enum ConnectionType {
    GRPC("GRPC", "Grpc Connection");
    final String type;
    final String name;

    ConnectionType(String type, String name) {
        this.type = type;
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }
}
