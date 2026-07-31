package com.nyquistdata.client.client.exceptions;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version: 1.0.0
 * @date: 2026/7/29
 * @description:
 */
public class ConnectionAlreadyClosedException extends GrpcRemoteException {
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
