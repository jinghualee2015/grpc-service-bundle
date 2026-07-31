package com.nyquistdata.grpc.api.utils;

import java.net.*;
import java.util.Enumeration;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version: 1.0.0
 * @date: 2026/7/26
 * @description: Net utils
 */
public class NetUtils {
    private static final String LEGAL_LOCAL_IP_PROPERTY = "java.net.preferIPv6Addresses";

    private static final String DEFAULT_SOLVE_FAILED_RETURN = "resolve_failed";

    private static String localIp;

    /**
     * Get local ip.
     *
     * @return local ip
     */
    public static String localIP() {
        if (!StringUtils.isEmpty(localIp)) {
            return localIp;
        }
        localIp = getAddress();
        return localIp;
    }

    private static String getAddress() {
        InetAddress inetAddress = findFirstNonLoopbackAddress();
        if (inetAddress == null) {
            return DEFAULT_SOLVE_FAILED_RETURN;
        }

       return inetAddress.getHostAddress();
    }

    private static InetAddress findFirstNonLoopbackAddress() {
        InetAddress result = null;

        try {
            int lowest = Integer.MAX_VALUE;
            for (Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();
                 nics.hasMoreElements(); ) {
                NetworkInterface ifc = nics.nextElement();
                if (ifc.isUp()) {
                    if (ifc.getIndex() < lowest || result == null) {
                        lowest = ifc.getIndex();
                    } else {
                        continue;
                    }

                    for (Enumeration<InetAddress> addrs = ifc.getInetAddresses(); addrs.hasMoreElements(); ) {
                        InetAddress address = addrs.nextElement();
                        boolean isLegalIpVersion = Boolean.parseBoolean(System.getProperty(LEGAL_LOCAL_IP_PROPERTY))
                                ? address instanceof Inet6Address : address instanceof Inet4Address;
                        if (isLegalIpVersion && !address.isLoopbackAddress()) {
                            result = address;
                        }
                    }

                }
            }
        } catch (Exception ignore) {
        }

        if (result != null) {
            return result;
        }

        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException ignore) {
        }

        return null;

    }
}
