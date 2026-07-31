package com.nyquistdata.grpc.api.exceptions;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version: 1.0.0
 * @date: 2026/7/26
 * @description: connection already closed exception
 */
public class ConnectionAlreadyClosedException extends GrpcRuntimeException{
    private static final int CONNECTION_ALREADY_CLOSED = 600;

    public ConnectionAlreadyClosedException(String msg) {
        super(CONNECTION_ALREADY_CLOSED, msg);
    }

    public ConnectionAlreadyClosedException() {
        super(CONNECTION_ALREADY_CLOSED);
    }

    public ConnectionAlreadyClosedException(Throwable throwable) {
        super(CONNECTION_ALREADY_CLOSED, throwable);
    }
}
