package com.nyquistdata.grpc.server.server;

import com.nyquistdata.grpc.server.server.acceptors.GrpcBiStreamRequestAcceptor;
import com.nyquistdata.grpc.server.server.acceptors.GrpcRequestAcceptor;
import com.nyquistdata.grpc.server.server.connection.ConnectionManager;
import com.nyquistdata.grpc.server.server.support.GrpcExecutor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version: 1.0.0
 * @date: 2026/7/28
 * @description: grpc server implementation
 */
@Slf4j
public class GrpcServer extends BaseGrpcServer {
    public GrpcServer(GrpcRequestAcceptor grpcCommonRequestAcceptor,
                      GrpcBiStreamRequestAcceptor grpcCommonBiStreamRequestAcceptor,
                      ConnectionManager connectionManager) {
        super(grpcCommonRequestAcceptor, grpcCommonBiStreamRequestAcceptor, connectionManager);
    }

    @Override
    public ThreadPoolExecutor getRpcExecutor() {
        return GrpcExecutor.gRpcExecutor;
    }
}
