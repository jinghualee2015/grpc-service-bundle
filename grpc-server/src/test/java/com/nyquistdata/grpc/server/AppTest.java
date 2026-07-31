package com.nyquistdata.grpc.server;


import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

@Slf4j
public class AppTest {
    Logger logger = LogManager.getLogger(AppTest.class);

    @Test
    public void test_app() {
        logger.info("test_app");
        log.info("slf4j test_app");
    }
}
