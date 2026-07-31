package com.nyquistdata.grpc.api.remote;

import com.nyquistdata.grpc.api.remote.impl.response.Response;

import java.util.concurrent.Executor;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version: 1.0.0
 * @date: 2026/7/26
 * @description: call back for request
 */
public interface RequestCallBack<T extends Response> {
    Executor getExecutor();

    long getTimeout();

    void onResponse(T response);

    void onException(Throwable throwable);
}
