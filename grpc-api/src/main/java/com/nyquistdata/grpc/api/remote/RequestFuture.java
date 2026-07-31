package com.nyquistdata.grpc.api.remote;

import com.nyquistdata.grpc.api.remote.impl.response.Response;


/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version: 1.0.0
 * @date: 2026/7/26
 * @description: future for request
 */
public interface RequestFuture {
    /**
     * check that is done or not
     *
     * @return true is done
     */
    boolean isDone();

    /**
     * get response without timeout
     *
     * @return return response is done
     * @throws Exception
     */
    Response get() throws Exception;

    /**
     * get response with a given timeouts
     *
     * @param timeout - timeout milliseconds
     * @return return response if done
     * @throws Exception exception throws.
     */
    Response get(long timeout) throws Exception;
}
