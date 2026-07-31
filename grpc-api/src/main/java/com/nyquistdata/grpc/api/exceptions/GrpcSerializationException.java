package com.nyquistdata.grpc.api.exceptions;

import static com.nyquistdata.grpc.api.domains.constants.GrpcConstants.SERIALIZE_ERROR_CODE;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version: 1.0.0
 * @date: 2026/7/26
 * @description: serialization exception
 */
public class GrpcSerializationException extends GrpcRuntimeException{
    private static final long serialVersionUID = -4308536346316915612L;

    private static final String DEFAULT_MSG = "Nacos serialize failed. ";

    private static final String MSG_FOR_SPECIFIED_CLASS = "Nacos serialize for class [%s] failed. ";

    private Class<?> serializedClass;

    public GrpcSerializationException() {
        super(SERIALIZE_ERROR_CODE);
    }

    public GrpcSerializationException(Class<?> serializedClass) {
        super(SERIALIZE_ERROR_CODE, String.format(MSG_FOR_SPECIFIED_CLASS, serializedClass.getName()));
        this.serializedClass = serializedClass;
    }

    public GrpcSerializationException(Throwable throwable) {
        super(SERIALIZE_ERROR_CODE, DEFAULT_MSG, throwable);
    }

    public GrpcSerializationException(Class<?> serializedClass, Throwable throwable) {
        super(SERIALIZE_ERROR_CODE, String.format(MSG_FOR_SPECIFIED_CLASS, serializedClass.getName()), throwable);
        this.serializedClass = serializedClass;
    }

    public Class<?> getSerializedClass() {
        return serializedClass;
    }
}
