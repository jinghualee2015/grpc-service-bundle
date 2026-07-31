package com.nyquistdata.client.client.exceptions;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version:
 * @date: 2026/7/29
 * @description: connection is busy exception
 */
public class ConnectionBusyException extends GrpcRemoteException{
    private static final int CONNECTION_BUSY = 601;

    public ConnectionBusyException(String msg) {
        super(CONNECTION_BUSY, msg);
    }

    public ConnectionBusyException(Throwable throwable) {
        super(CONNECTION_BUSY, throwable);
    }
}
