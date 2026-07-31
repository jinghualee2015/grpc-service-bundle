package com.nyquistdata.grpc.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version: 1.0.0
 * @date: 2026/7/25
 * @description:
 */
public class LogTest {
    Logger logger= LogManager.getLogger(LogTest.class);

    @Test
    public void test_01(){
        logger.info("test_01:{}","test_aaa");
    }
}
