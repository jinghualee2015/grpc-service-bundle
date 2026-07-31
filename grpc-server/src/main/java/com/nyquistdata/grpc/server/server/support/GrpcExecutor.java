package com.nyquistdata.grpc.server.server.support;


import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version: 1.0.0
 * @date: 2026/7/28
 * @description: thread executor pool
 */
public abstract class GrpcExecutor {
    public static final ThreadPoolExecutor gRpcExecutor = new ThreadPoolExecutor(16, 16, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(1000), new ThreadFactory() {
        private final AtomicInteger threadNumber = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable task) {
            Thread thread = new Thread(task, "grpc-executor-" + threadNumber.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        }
    });

    public static void runWithoutThread(Runnable runnable) {
        runnable.run();
    }
}
