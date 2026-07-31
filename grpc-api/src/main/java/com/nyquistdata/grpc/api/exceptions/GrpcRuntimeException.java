package com.nyquistdata.grpc.api.exceptions;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version: 1.0.0
 * @date: 2026/7/26
 * @description: grpc base runtime exception
 */
public class GrpcRuntimeException extends RuntimeException {
    private static final long serialVersionUID = 3513491993982293262L;

    public static final String ERROR_MESSAGE_FORMAT = "errCode: %d, errMsg: %s ";

    private final int errCode;

    public GrpcRuntimeException(int errCode) {
        super();
        this.errCode = errCode;
    }

    public GrpcRuntimeException(int errCode, String errMsg) {
        super(String.format(ERROR_MESSAGE_FORMAT, errCode, errMsg));
        this.errCode = errCode;
    }

    public GrpcRuntimeException(int errCode, Throwable throwable) {
        super(throwable);
        this.errCode = errCode;
    }

    public GrpcRuntimeException(int errCode, String errMsg, Throwable throwable) {
        super(String.format(ERROR_MESSAGE_FORMAT, errCode, errMsg), throwable);
        this.errCode = errCode;
    }

    public int getErrCode() {
        return errCode;
    }
}
