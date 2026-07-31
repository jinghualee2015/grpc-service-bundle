package com.nyquistdata.grpc.api.exceptions;

import com.nyquistdata.grpc.api.utils.NestedExceptionUtils;

import java.io.IOException;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version:
 * @date: 2026/7/29
 * @description:
 */
public class NestedIoException extends IOException {

    static {
        NestedExceptionUtils.class.getName();
    }
    /**
     * Construct a {@code NestedIOException} with the specified detail message.
     *
     * @param msg the detail message
     */
    public NestedIoException(String msg) {
        super(msg);
    }

    /**
     * Construct a {@code NestedIOException} with the specified detail message
     * and nested exception.
     *
     * @param msg   the detail message
     * @param cause the nested exception
     */
    public NestedIoException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * Return the detail message, including the message from the nested exception
     * if there is one.
     */
    @Override
    public String getMessage() {
        return NestedExceptionUtils.buildMessage(super.getMessage(), getCause());
    }

}
