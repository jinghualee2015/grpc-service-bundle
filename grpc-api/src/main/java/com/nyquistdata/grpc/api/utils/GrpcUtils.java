package com.nyquistdata.grpc.api.utils;

import com.fasterxml.jackson.databind.util.ByteBufferBackedInputStream;
import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.UnsafeByteOperations;
import com.nyquistdata.grpc.api.Metadata;
import com.nyquistdata.grpc.api.Payload;
import com.nyquistdata.grpc.api.exceptions.GrpcException;
import com.nyquistdata.grpc.api.exceptions.GrpcRuntimeException;
import com.nyquistdata.grpc.api.remote.impl.request.Request;
import com.nyquistdata.grpc.api.remote.impl.request.RequestMeta;
import com.nyquistdata.grpc.api.remote.impl.response.Response;
import com.nyquistdata.grpc.api.remote.support.PayloadRegistry;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version: 1.0.0
 * @date: 2026/7/26
 * @description: gRPC utils, use to parse request and response
 */
public class GrpcUtils {

    /**
     * convert request to payload
     *
     * @param request  request
     * @param metadata request medata
     * @return payload instance
     */
    public static Payload convert(Request request, RequestMeta metadata) {
        Payload.Builder payloadBuilder = Payload.newBuilder();
        Metadata.Builder metadataBuilder = Metadata.newBuilder();
        if (metadata != null) {
            metadataBuilder.putAllHeaders(request.getHeaders())
                    .setType(request.getClass().getSimpleName());
        }
        metadataBuilder.setClientIp(NetUtils.localIP());
        payloadBuilder.setMetadata(metadataBuilder.build());

        byte[] jsonBytes = convertRequestToByte(request);
        Payload payload = payloadBuilder.setBody(Any
                .newBuilder()
                .setValue(UnsafeByteOperations.unsafeWrap(jsonBytes))).build();
        return payload;
    }

    /**
     * convert request to payload.
     *
     * @param request request.
     * @return payload.
     */
    public static Payload convert(Request request) {

        Metadata newMeta = Metadata.newBuilder().setType(request.getClass().getSimpleName())
                .setClientIp(NetUtils.localIP()).putAllHeaders(request.getHeaders()).build();

        byte[] jsonBytes = convertRequestToByte(request);

        Payload.Builder builder = Payload.newBuilder();

        return builder.setBody(Any.newBuilder().setValue(UnsafeByteOperations.unsafeWrap(jsonBytes)))
                .setMetadata(newMeta).build();

    }

    /**
     * convert response to payload.
     *
     * @param response response.
     * @return payload.
     */
    public static Payload convert(Response response) {
        byte[] jsonBytes = JacksonUtils.toJsonBytes(response);

        Metadata.Builder metaBuilder = Metadata.newBuilder().setType(response.getClass().getSimpleName());
        return Payload.newBuilder().setBody(Any.newBuilder().setValue(UnsafeByteOperations.unsafeWrap(jsonBytes)))
                .setMetadata(metaBuilder.build()).build();
    }


    /**
     * parse payload to request/response model.
     *
     * @param payload payload to be parsed.
     * @return payload
     */
    public static Object parse(Payload payload) {
        Class classType = PayloadRegistry.getClassByType(payload.getMetadata().getType());
        if (classType != null) {
            ByteString byteString = payload.getBody().getValue();
            ByteBuffer byteBuffer = byteString.asReadOnlyByteBuffer();
            Object obj = JacksonUtils.toObj(new ByteBufferBackedInputStream(byteBuffer), classType);
            if (obj instanceof Request) {
                ((Request) obj).putAllHeader(payload.getMetadata().getHeadersMap());
            }
            return obj;
        } else {
            throw new GrpcRuntimeException(GrpcException.SERVER_ERROR,
                    "Unknown payload type:" + payload.getMetadata().getType());
        }
    }

    private static byte[] convertRequestToByte(Request request) {
        Map<String, String> headers = new HashMap<>(request.getHeaders());
        request.clearHeaders();
        byte[] jsonBytes = JacksonUtils.toJsonBytes(request);
        request.putAllHeader(headers);
        return jsonBytes;
    }
}
