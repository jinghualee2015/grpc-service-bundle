package com.nyquistdata.client.client;

import com.nyquistdata.client.client.config.DefaultGrpcClientConfig;
import com.nyquistdata.client.client.config.GrpcClientConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Hello world!
 */
@Slf4j
public class ClientApp {
    public static void main(String[] args) throws Exception {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        GrpcClientConfig clientConfig = DefaultGrpcClientConfig.newBuilder().build();
        ServerListFactory factory = new ServerListFactory() {
            @Override
            public String genNextServer() {
                return "127.0.0.1:9848";
            }

            @Override
            public String getCurrentServer() {
                return "127.0.0.1:9848";
            }

            @Override
            public List<String> getServerList() {
                return Arrays.asList("127.0.0.1");
            }
        };
        GrpcSdkClient client = new GrpcSdkClient(clientConfig, factory);
        client.start();
        log.error("grpc client started");
        countDownLatch.await();
    }
}
