package com.nyquistdata.grpc.api.remote.impl.response;

import com.nyquistdata.grpc.api.domains.enums.ResponseCode;
import com.nyquistdata.grpc.api.remote.Payload;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version: 1.0.0
 * @date: 2026/7/26
 * @description: abstract response class
 */
public abstract class Response implements Payload {
    int resultCode = ResponseCode.SUCCESS.getCode();

    int errorCode;

    String message;

    String requestId;

    /**
     * Getter method for property <tt>requestId</tt>.
     *
     * @return property value of requestId
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * Setter method for property <tt>requestId</tt>.
     *
     * @param requestId value to be assigned to property requestId
     */
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    /**
     * Check Response  is Successed.
     *
     * @return success or not.
     */
    public boolean isSuccess() {
        return this.resultCode == ResponseCode.SUCCESS.getCode();
    }

    /**
     * Getter method for property <tt>resultCode</tt>.
     *
     * @return property value of resultCode
     */
    public int getResultCode() {
        return resultCode;
    }

    /**
     * Setter method for property <tt>resultCode</tt>.
     *
     * @param resultCode value to be assigned to property resultCode
     */
    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    /**
     * Getter method for property <tt>message</tt>.
     *
     * @return property value of message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Setter method for property <tt>message</tt>.
     *
     * @param message value to be assigned to property message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Getter method for property <tt>errorCode</tt>.
     *
     * @return property value of errorCode
     */
    public int getErrorCode() {
        return errorCode;
    }

    /**
     * Setter method for property <tt>errorCode</tt>.
     *
     * @param errorCode value to be assigned to property errorCode
     */
    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public void setErrorInfo(int errorCode, String errorMsg) {
        this.resultCode = ResponseCode.FAIL.getCode();
        this.errorCode = errorCode;
        this.message = errorMsg;
    }

    @Override
    public String toString() {
        return "Response{" + "resultCode=" + resultCode + ", errorCode=" + errorCode + ", message='" + message + '\''
                + ", requestId='" + requestId + '\'' + '}';
    }
}
