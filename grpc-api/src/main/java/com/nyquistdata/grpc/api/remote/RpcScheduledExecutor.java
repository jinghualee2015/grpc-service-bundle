package com.nyquistdata.grpc.api.remote;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version: 1.0.0
 * @date: 2026/7/26
 * @description: rpc scheduler executor
 */
public class RpcScheduledExecutor extends ScheduledThreadPoolExecutor {
    public static final RpcScheduledExecutor TIMEOUT_SCHEDULER = new RpcScheduledExecutor(1, "com.nyquistdata.grpc.TimeoutScheduler");

    public static final RpcScheduledExecutor CONTROL_SCHEDULER = new RpcScheduledExecutor(1, "com.nyquistdata.grpc.ControlScheduler");

    public static final RpcScheduledExecutor COMMON_SERVER_EXECUTOR = new RpcScheduledExecutor(1, "com.nyquistdata.grpc.CommonServerExecutor");

    public RpcScheduledExecutor(int corePoolSize, final String threadName) {
        super(corePoolSize, new ThreadFactory() {
            private final AtomicLong index = new AtomicLong();

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, threadName + "-" + index.incrementAndGet());
                thread.setDaemon(true);
                return thread;
            }
        });
    }
}
