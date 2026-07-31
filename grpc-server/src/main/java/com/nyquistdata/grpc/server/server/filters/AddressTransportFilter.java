package com.nyquistdata.grpc.server.server.filters;

import com.alipay.remoting.util.StringUtils;
import com.nyquistdata.grpc.server.server.connection.ConnectionManager;
import io.grpc.Attributes;
import io.grpc.Grpc;
import io.grpc.ServerTransportFilter;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

import static com.nyquistdata.grpc.server.domains.constants.GrpcServerConstants.*;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version: 1.0.0
 * @date: 2026/7/28
 * @description: address transport filter process remote address, local address and connection id attributes.
 */
@Slf4j
public class AddressTransportFilter extends ServerTransportFilter {
    private final ConnectionManager connectionManager;

    public AddressTransportFilter(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public Attributes transportReady(Attributes transportAttrs) {
        InetSocketAddress remoteAddress = (InetSocketAddress) transportAttrs
                .get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR);
        InetSocketAddress localAddress = (InetSocketAddress) transportAttrs
                .get(Grpc.TRANSPORT_ATTR_LOCAL_ADDR);
        int remotePort = remoteAddress.getPort();
        int localPort = localAddress.getPort();
        String remoteIp = remoteAddress.getAddress().getHostAddress();
        Attributes attrWrapper = transportAttrs.toBuilder()
                .set(ATTR_TRANS_KEY_CONN_ID, System.currentTimeMillis() + "_" + remoteIp + "_" + remotePort)
                .set(ATTR_TRANS_KEY_REMOTE_IP, remoteIp).set(ATTR_TRANS_KEY_REMOTE_PORT, remotePort)
                .set(ATTR_TRANS_KEY_LOCAL_PORT, localPort).build();
        String connectionId = attrWrapper.get(ATTR_TRANS_KEY_CONN_ID);
        log.info("Connection transportReady,connectionId = {} ", connectionId);
        return attrWrapper;

    }

    @Override
    public void transportTerminated(Attributes transportAttrs) {
        String connectionId = null;
        try {
            connectionId = transportAttrs.get(ATTR_TRANS_KEY_CONN_ID);
        } catch (Exception e) {
            // Ignore
        }
        if (StringUtils.isNotBlank(connectionId)) {
            log
                    .info("Connection transportTerminated,connectionId = {} ", connectionId);
            connectionManager.unregister(connectionId);
        }
    }
}
