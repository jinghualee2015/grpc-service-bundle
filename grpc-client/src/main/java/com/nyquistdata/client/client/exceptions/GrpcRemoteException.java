package com.nyquistdata.client.client.exceptions;

import com.nyquistdata.grpc.api.exceptions.GrpcRuntimeException;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version: 1.0.0
 * @date: 2026/7/29
 * @description: super exception in remote side
 */
public class GrpcRemoteException extends GrpcRuntimeException {
    public GrpcRemoteException(int errorCode) {
        super(errorCode);
    }

    public GrpcRemoteException(int errorCode, String msg) {
        super(errorCode, msg);
    }

    public GrpcRemoteException(int errorCode, Throwable throwable) {
        super(errorCode, throwable);
    }
}
