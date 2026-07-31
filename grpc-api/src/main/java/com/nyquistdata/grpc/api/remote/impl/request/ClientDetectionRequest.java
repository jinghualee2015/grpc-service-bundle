package com.nyquistdata.grpc.api.remote.impl.request;

import static com.nyquistdata.grpc.api.domains.constants.GrpcConstants.INTERNAL_MODULE;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version: 1.0.0
 * @date: 2026/7/26
 * @description:
 */
public class ClientDetectionRequest extends ServerRequest {
    @Override
    public String getModule() {
        return INTERNAL_MODULE;
    }
}
