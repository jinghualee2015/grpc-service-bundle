package com.nyquistdata.grpc.api.remote;

import com.nyquistdata.grpc.api.exceptions.GrpcException;
import com.nyquistdata.grpc.api.remote.impl.request.Request;
import com.nyquistdata.grpc.api.remote.impl.response.Response;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version: 1.0.0
 * @date: 2026/7/26
 * @description: connection interface, define basic operation
 */
public interface Requester {
    /**
     * send request.
     *
     * @param request      request.
     * @param timeoutMills mills of timeouts.
     * @return response  response returned.
     */
    Response request(Request request, long timeoutMills) throws GrpcException;

    /**
     * send request.
     *
     * @param request request.
     * @return request future.
     * @throws Exception exception throw.
     */
    RequestFuture requestFuture(Request request) throws GrpcException;

    /**
     * send async request.
     *
     * @param request         request.
     * @param requestCallBack callback of request.
     * @throws Exception exception throw.
     */
    void asyncRequest(Request request, RequestCallBack requestCallBack) throws GrpcException;

    /**
     * close connection.
     */
    void close();
}
