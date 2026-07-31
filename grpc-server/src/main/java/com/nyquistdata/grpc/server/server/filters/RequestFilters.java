package com.nyquistdata.grpc.server.server.filters;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version: 1.0.0
 * @date: 2026/7/26
 * @description: filter of request
 */
public class RequestFilters {
    List<AbstractRequestFilter> filters = new ArrayList<>();

    public void registerFilter(AbstractRequestFilter filter) {
        filters.add(filter);
    }

    public List<AbstractRequestFilter> getFilters() {
        return filters;
    }
}
