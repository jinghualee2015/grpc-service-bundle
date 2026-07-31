package com.nyquistdata.grpc.api.remote.support;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version: 1.0.0
 * @date: 2026/7/26
 * @description: id generator to server push ack
 */
public class PushAckIdGenerator {
    private static AtomicLong id = new AtomicLong(0L);

    private static final int ID_PREV_REGEN_OFFSET = 1000;

    /**
     * get server push id.
     */
    public static long getNextId() {
        if (id.longValue() > Long.MAX_VALUE - ID_PREV_REGEN_OFFSET) {
            id.getAndSet(0L);
        }
        return id.incrementAndGet();
    }
}
