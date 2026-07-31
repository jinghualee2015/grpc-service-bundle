package com.nyquistdata.client.client.support;

import com.nyquistdata.grpc.api.utils.StringUtils;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version: 1.0.0
 * @date: 2026/7/29
 * @description: name thread factory
 */
public class NamedThreadFactory implements ThreadFactory {
    private final AtomicInteger id = new AtomicInteger(0);

    private String name;

    public NamedThreadFactory(String name) {
        if (!name.endsWith(StringUtils.DOT)) {
            name += StringUtils.DOT;
        }
        this.name = name;
    }

    @Override
    public Thread newThread(Runnable r) {
        String threadName = name + id.getAndIncrement();
        Thread thread = new Thread(r, threadName);
        thread.setDaemon(true);
        return thread;
    }
}
