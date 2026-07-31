package com.nyquistdata.grpc.server.server.connection.listeners.support;

import com.alipay.hessian.clhm.ConcurrentLinkedHashMap;
import com.nyquistdata.grpc.api.exceptions.GrpcException;
import com.nyquistdata.grpc.api.remote.DefaultRequestFuture;
import com.nyquistdata.grpc.api.remote.impl.response.Response;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version: 1.0.0
 * @date: 2026/7/26
 * @description: server push ack synchronizer
 */
@Slf4j
public class RpcAckCallbackSynchronizer {
    public static final Map<String, Map<String, DefaultRequestFuture>> CALLBACK_CONTEXT = new ConcurrentLinkedHashMap.Builder<String, Map<String, DefaultRequestFuture>>()
            .maximumWeightedCapacity(1000000)
            .listener((s, pushCallBack) -> pushCallBack.entrySet().forEach(
                    stringDefaultPushFutureEntry -> stringDefaultPushFutureEntry.getValue().setFailResult(new TimeoutException()))).build();

    /**
     * notify  ack.
     *
     * @param connectionId connectionId
     * @param response     response
     */
    public static void ackNotify(String connectionId, Response response) {

        Map<String, DefaultRequestFuture> stringDefaultPushFutureMap = CALLBACK_CONTEXT.get(connectionId);
        if (stringDefaultPushFutureMap == null) {

            log
                    .warn("Ack receive on a outdated connection ,connection id={},requestId={} ", connectionId,
                            response.getRequestId());
            return;
        }

        DefaultRequestFuture currentCallback = stringDefaultPushFutureMap.remove(response.getRequestId());
        if (currentCallback == null) {

            log
                    .warn("Ack receive on a outdated request ,connection id={},requestId={} ", connectionId,
                            response.getRequestId());
            return;
        }

        if (response.isSuccess()) {
            currentCallback.setResponse(response);
        } else {
            currentCallback.setFailResult(new GrpcException(response.getErrorCode(), response.getMessage()));
        }
    }

    /**
     * sync callback.
     *
     * @param connectionId      connectionId
     * @param requestId         requestId
     * @param defaultPushFuture defaultPushFuture
     * @throws GrpcException NacosException
     */
    public static void syncCallback(String connectionId, String requestId, DefaultRequestFuture defaultPushFuture)
            throws GrpcException {

        Map<String, DefaultRequestFuture> stringDefaultPushFutureMap = initContextIfNecessary(connectionId);

        if (!stringDefaultPushFutureMap.containsKey(requestId)) {
            DefaultRequestFuture pushCallBackPrev = stringDefaultPushFutureMap
                    .putIfAbsent(requestId, defaultPushFuture);
            if (pushCallBackPrev == null) {
                return;
            }
        }
        throw new GrpcException(GrpcException.INVALID_PARAM, "request id conflict");

    }

    /**
     * clear context of connectionId.
     *
     * @param connectionId connectionId
     */
    public static void clearContext(String connectionId) {
        CALLBACK_CONTEXT.remove(connectionId);
    }

    /**
     * init context of connectionId if necessary.
     *
     * @param connectionId connectionId
     */
    public static Map<String, DefaultRequestFuture> initContextIfNecessary(String connectionId) {
        if (!CALLBACK_CONTEXT.containsKey(connectionId)) {
            Map<String, DefaultRequestFuture> context = new HashMap<>(128);
            Map<String, DefaultRequestFuture> stringDefaultRequestFutureMap = CALLBACK_CONTEXT
                    .putIfAbsent(connectionId, context);
            return stringDefaultRequestFutureMap == null ? context : stringDefaultRequestFutureMap;
        } else {
            return CALLBACK_CONTEXT.get(connectionId);
        }
    }

    /**
     * clear context of requestId.
     *
     * @param connectionId connectionId
     * @param requestId    requestId
     */
    public static void clearFuture(String connectionId, String requestId) {
        Map<String, DefaultRequestFuture> stringDefaultPushFutureMap = CALLBACK_CONTEXT.get(connectionId);

        if (stringDefaultPushFutureMap == null || !stringDefaultPushFutureMap.containsKey(requestId)) {
            return;
        }
        stringDefaultPushFutureMap.remove(requestId);
    }
}
