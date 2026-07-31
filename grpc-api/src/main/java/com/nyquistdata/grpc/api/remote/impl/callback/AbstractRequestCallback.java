package com.nyquistdata.grpc.api.remote.impl.callback;

import com.nyquistdata.grpc.api.remote.RequestCallBack;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version: 1.0.0
 * @date: 2026/7/26
 * @description: abstract request call back
 */
public abstract class AbstractRequestCallback implements RequestCallBack {
    long timeoutMills;

    public AbstractRequestCallback(long timeoutMills) {
        this.timeoutMills = timeoutMills;
    }

    public AbstractRequestCallback() {
        this(3000L);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getTimeout() {
        return timeoutMills;
    }
}
