package com.nyquistdata.grpc.api.resource;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version:
 * @date: 2026/7/29
 * @description:
 */
public interface ContextResource extends Resource {
    /**
     * Return the path within the enclosing 'context'.
     *
     * <p>This is typically path relative to a context-specific root directory,
     * e.g. a ServletContext root or a PortletContext root.
     */
    String getPathWithinContext();
}
