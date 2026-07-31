package com.nyquistdata.grpc.api.remote.impl.response;

import com.nyquistdata.grpc.api.domains.enums.ResponseCode;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version:
 * @date: 2026/7/26
 * @description: unknown response
 */
public class ErrorResponse extends Response{
    /**
     * build an error response.
     *
     * @param errorCode errorCode
     * @param msg msg
     * @return response
     */
    public static Response build(int errorCode, String msg) {
        ErrorResponse response = new ErrorResponse();
        response.setErrorInfo(errorCode, msg);
        return response;
    }

    /**
     * build an error response.
     *
     * @param exception exception
     * @return response
     */
    public static Response build(Throwable exception) {
        int errorCode = ResponseCode.FAIL.getCode();
        ErrorResponse response = new ErrorResponse();
        response.setErrorInfo(errorCode, exception.getMessage());
        response.setResultCode(errorCode);
        return response;
    }
}
