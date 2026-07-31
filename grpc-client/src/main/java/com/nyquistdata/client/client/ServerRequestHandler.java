package com.nyquistdata.client.client;

import com.nyquistdata.client.client.connection.Connection;
import com.nyquistdata.grpc.api.remote.impl.request.Request;
import com.nyquistdata.grpc.api.remote.impl.response.Response;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version:
 * @date: 2026/7/28
 * @description: to process the request from server side
 */
public interface ServerRequestHandler {
    /**
     * handle request from server
     * @param request request
     * @param connection current connection, it can be used to know server ability
     * @return
     */
    Response requestReply(Request request, Connection connection);
}
