package com.nyquistdata.grpc.api.exceptions;

import java.lang.reflect.Type;

import static com.nyquistdata.grpc.api.domains.constants.GrpcConstants.DESERIALIZE_ERROR_CODE;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version: 1.0.0
 * @date: 2026/7/26
 * @description:
 */
public class GrpcDeserializationException extends GrpcRuntimeException{
    private static final long serialVersionUID = -2742350751684273728L;

    private static final String DEFAULT_MSG = "Nacos deserialize failed. ";

    private static final String MSG_FOR_SPECIFIED_CLASS = "Nacos deserialize for class [%s] failed. ";

    private static final String ERROR_MSG_FOR_SPECIFIED_CLASS = "Nacos deserialize for class [%s] failed, cause error[%s]. ";

    private Class<?> targetClass;

    public GrpcDeserializationException() {
        super(DESERIALIZE_ERROR_CODE);
    }

    public GrpcDeserializationException(Class<?> targetClass) {
        super(DESERIALIZE_ERROR_CODE, String.format(MSG_FOR_SPECIFIED_CLASS, targetClass.getName()));
        this.targetClass = targetClass;
    }

    public GrpcDeserializationException(Type targetType) {
        super(DESERIALIZE_ERROR_CODE, String.format(MSG_FOR_SPECIFIED_CLASS, targetType.toString()));
    }

    public GrpcDeserializationException(Throwable throwable) {
        super(DESERIALIZE_ERROR_CODE, DEFAULT_MSG, throwable);
    }

    public GrpcDeserializationException(Class<?> targetClass, Throwable throwable) {
        super(DESERIALIZE_ERROR_CODE, String.format(ERROR_MSG_FOR_SPECIFIED_CLASS, targetClass.getName(), throwable.getMessage()), throwable);
        this.targetClass = targetClass;
    }

    public GrpcDeserializationException(Type targetType, Throwable throwable) {
        super(DESERIALIZE_ERROR_CODE, String.format(ERROR_MSG_FOR_SPECIFIED_CLASS, targetType.toString(), throwable.getMessage()), throwable);
    }

    public Class<?> getTargetClass() {
        return targetClass;
    }
}
