package com.nyquistdata.grpc.api.resource;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version: 1.0.0
 * @date: 2026/7/29
 * @description:
 */
@FunctionalInterface
public interface ProtocolResolver {
    /**
     * Resolve the given location against the given resource loader
     * if this implementation's protocol matches.
     *
     * @param location       the user-specified resource location
     * @param resourceLoader the associated resource loader
     * @return a corresponding {@code Resource} handle if the given location
     * matches this resolver's protocol, or {@code null} otherwise
     */

    Resource resolve(String location, ResourceLoader resourceLoader);
}
