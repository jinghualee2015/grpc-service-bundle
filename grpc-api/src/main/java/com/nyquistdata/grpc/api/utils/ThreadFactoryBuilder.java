package com.nyquistdata.grpc.api.utils;

import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version:
 * @date: 2026/7/30
 * @description:
 */
public class ThreadFactoryBuilder {
    /**
     *  Whether it is a daemon thread.
     */
    private Boolean daemon = false;

    /**
     *   Thread priority.
     */
    private Integer priority = null;

    /**
     *   Thread name template.
     */
    private String nameFormat = null;

    /**
     *   Uncaught exception handler.
     */
    private Thread.UncaughtExceptionHandler uncaughtExceptionHandler = null;

    /**
     *   Customize thread factory.
     */
    private ThreadFactory customizeFactory = null;

    /**
     * set nameFormat property.
     */
    public ThreadFactoryBuilder nameFormat(String nameFormat) {
        checkNullParameter(nameFormat, "nameFormat cannot be null.");
        this.nameFormat = nameFormat;
        return this;
    }

    /**
     * set priority property.
     */
    public ThreadFactoryBuilder priority(int priority) {
        if (priority > Thread.MAX_PRIORITY || priority < Thread.MIN_PRIORITY) {
            throw new IllegalArgumentException(
                    String.format("The value of priority should be between %s and %s", Thread.MIN_PRIORITY + 1, Thread.MAX_PRIORITY + 1)
            );
        }
        this.priority = priority;
        return this;
    }

    /**
     * set uncaughtExceptionHandler property.
     */
    public ThreadFactoryBuilder uncaughtExceptionHandler(
            Thread.UncaughtExceptionHandler uncaughtExceptionHandler) {
        checkNullParameter(uncaughtExceptionHandler, "uncaughtExceptionHandler cannot be null.");
        this.uncaughtExceptionHandler = uncaughtExceptionHandler;
        return this;
    }

    /**
     * set daemon property.
     */
    public ThreadFactoryBuilder daemon(boolean daemon) {
        this.daemon = daemon;
        return this;
    }

    /**
     * set customizeFactory property.
     */
    public ThreadFactoryBuilder customizeFactory(ThreadFactory factory) {
        checkNullParameter(factory, "factory cannot be null.");
        this.customizeFactory = factory;
        return this;
    }

    /**
     *  build thread factory.
     */
    public ThreadFactory build() {
        ThreadFactory factory = customizeFactory == null ? Executors.defaultThreadFactory() : customizeFactory;
        final AtomicLong count = (nameFormat != null) ? new AtomicLong(0) : null;
        return r -> {
            Thread thread = factory.newThread(r);
            if (nameFormat != null) {
                thread.setName(format(nameFormat, count.getAndIncrement()));
            }
            if (priority != null) {
                thread.setPriority(priority);
            }
            if (uncaughtExceptionHandler != null) {
                thread.setUncaughtExceptionHandler(uncaughtExceptionHandler);
            }
            thread.setDaemon(daemon);
            return thread;
        };
    }

    private String format(String format, Object... args) {
        return String.format(Locale.ROOT, format, args);
    }

    private void checkNullParameter(Object obj, String msg) {
        if (obj == null) {
            throw new IllegalArgumentException(msg);
        }
    }
}
