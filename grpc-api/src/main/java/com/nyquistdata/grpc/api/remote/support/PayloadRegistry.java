package com.nyquistdata.grpc.api.remote.support;

import com.nyquistdata.grpc.api.remote.Payload;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version: 1.0.0
 * @date: 2026/7/25
 * @description: payload registry, define basic scan behavior request and response
 */
public class PayloadRegistry {
    private static final Map<String, Class<?>> REGISTRY_REQUEST = new HashMap<>();

    static boolean initialized = false;

    public static void init() {
        scan();
    }

    private static synchronized void scan() {
        if (initialized) {
            return;
        }
        ServiceLoader<Payload> payloads = ServiceLoader.load(Payload.class);
        for (Payload payload : payloads) {
            register(payload.getClass().getSimpleName(), payload.getClass());
        }
        initialized = true;
    }

    static void register(String type, Class<?> clazz) {
        if (Modifier.isAbstract(clazz.getModifiers())) {
            return;
        }
        if (REGISTRY_REQUEST.containsKey(type)) {
            throw new RuntimeException(String.format("Failed to register payload of type %s, clazz:%s", type, clazz.getName()));
        }
        REGISTRY_REQUEST.put(type, clazz);
    }

    public static Class<?> getClassByType(String type) {
        return REGISTRY_REQUEST.get(type);
    }
}
