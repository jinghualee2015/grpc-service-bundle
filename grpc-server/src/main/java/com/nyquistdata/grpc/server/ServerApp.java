package com.nyquistdata.grpc.server;

import com.nyquistdata.grpc.server.server.GrpcServer;
import com.nyquistdata.grpc.server.server.acceptors.GrpcBiStreamRequestAcceptor;
import com.nyquistdata.grpc.server.server.acceptors.GrpcRequestAcceptor;
import com.nyquistdata.grpc.server.server.connection.ConnectionManager;
import com.nyquistdata.grpc.server.server.connection.listeners.support.ClientConnectionEventListenerRegistry;
import com.nyquistdata.grpc.server.server.filters.RemoteParamCheckFilter;
import com.nyquistdata.grpc.server.server.filters.RemoteRequestAuthFilter;
import com.nyquistdata.grpc.server.server.filters.RequestFilters;
import com.nyquistdata.grpc.server.server.handlers.HealthCheckRequestHandler;
import com.nyquistdata.grpc.server.server.handlers.RequestHandlerRegistry;
import com.nyquistdata.grpc.server.server.handlers.ServerLoaderInfoRequestHandler;
import com.nyquistdata.grpc.server.server.handlers.ServerReloaderRequestHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CountDownLatch;

/**
 * grpc server
 */
@Slf4j
public class ServerApp {
    public static void main(String[] args) throws Exception {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        ClientConnectionEventListenerRegistry connectionEventListenerRegistry = new ClientConnectionEventListenerRegistry();
        ConnectionManager connectionManager = buildConnectionManager(connectionEventListenerRegistry);
        RequestFilters requestFilters = buildRequestFilters();
        RequestHandlerRegistry requestHandlerRegistry = buildRequestHandlerRegistry(requestFilters, connectionManager);
        GrpcRequestAcceptor grpcRequestAcceptor = buildGrpcRequestAcceptor(requestHandlerRegistry, connectionManager);
        GrpcBiStreamRequestAcceptor biStreamRequestAcceptor = buildGrpcBiStreamRequestAcceptor(connectionManager);
        GrpcServer grpcServer = new GrpcServer(grpcRequestAcceptor, biStreamRequestAcceptor, connectionManager);
        grpcServer.start();
        log.info("grpc server started!");
        countDownLatch.await();
    }

    static ConnectionManager buildConnectionManager(ClientConnectionEventListenerRegistry connectionEventListenerRegistry) {
        return new ConnectionManager(connectionEventListenerRegistry);
    }

    static RequestFilters buildRequestFilters() {
        RequestFilters filters = new RequestFilters();
        new RemoteParamCheckFilter(filters);
        new RemoteRequestAuthFilter(filters);
        return filters;
    }

    static RequestHandlerRegistry buildRequestHandlerRegistry(RequestFilters filters, ConnectionManager connectionManager) {
        RequestHandlerRegistry registry = new RequestHandlerRegistry();
        new HealthCheckRequestHandler(filters, registry);
        new ServerLoaderInfoRequestHandler(filters, registry, connectionManager);
        new ServerReloaderRequestHandler(filters, registry, connectionManager);
        return registry;

    }

    static GrpcRequestAcceptor buildGrpcRequestAcceptor(RequestHandlerRegistry registry, ConnectionManager connectionManager) {
        GrpcRequestAcceptor acceptor = new GrpcRequestAcceptor(registry, connectionManager);
        return acceptor;
    }

    static GrpcBiStreamRequestAcceptor buildGrpcBiStreamRequestAcceptor(ConnectionManager connectionManager) {
        GrpcBiStreamRequestAcceptor biStreamRequestAcceptor = new GrpcBiStreamRequestAcceptor(connectionManager);
        return biStreamRequestAcceptor;
    }
}
