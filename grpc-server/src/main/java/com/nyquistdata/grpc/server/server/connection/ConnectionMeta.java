package com.nyquistdata.grpc.server.server.connection;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Copyright (C), 2022 ~ 2026, NyquistAi.inc
 *
 * @author: Nyquist Data Tech Team
 * @version: 1.0.0
 * @date: 2026/7/26
 * @description: Connection Meta info
 */
public class ConnectionMeta {
    /**
     * ConnectionType.
     */
    String connectType;

    /**
     * Client IP Address.
     */
    String clientIp;

    /**
     * Remote IP Address.
     */
    String remoteIp;

    /**
     * Remote IP Port.
     */
    int remotePort;

    /**
     * Local Ip Port.
     */
    int localPort;

    /**
     * Client version.
     */
    String version;

    /**
     * Identify Unique connectionId.
     */
    String connectionId;

    /**
     * create time.
     */
    Date createTime;

    /**
     * lastActiveTime.
     */
    long lastActiveTime;

    /**
     * String appName.
     */
    String appName;

    /**
     * tenant.
     */
    String tenant;

    long firstPushQueueBlockTime = 0;

    long lastPushQueueBlockTime = 0;

    protected Map<String, String> labels = new HashMap<>();

    public String getLabel(String labelKey) {
        return labels.get(labelKey);
    }

    public String getTag() {
        return labels.get("Vipserver-Tag");
    }

    public ConnectionMeta(String connectionId, String clientIp, String remoteIp, int remotePort, int localPort,
                          String connectType, String version, String appName, Map<String, String> labels) {
        this.connectionId = connectionId;
        this.clientIp = clientIp;
        this.connectType = connectType;
        this.version = version;
        this.appName = appName;
        this.remoteIp = remoteIp;
        this.remotePort = remotePort;
        this.localPort = localPort;
        this.createTime = new Date();
        this.lastActiveTime = System.currentTimeMillis();
        this.labels.putAll(labels);
    }

    /**
     * check if this connection is sdk source.
     *
     * @return if this connection is sdk source.
     */
    public boolean isSdkSource() {
        return true;
    }


    /**
     * Getter method for property <tt>labels</tt>.
     *
     * @return property value of labels
     */
    public Map<String, String> getLabels() {
        return labels;
    }


    /**
     * Setter method for property <tt>labels</tt>.
     *
     * @param labels value to be assigned to property labels
     */
    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }

    /**
     * Getter method for property <tt>clientIp</tt>.
     *
     * @return property value of clientIp
     */
    public String getClientIp() {
        return clientIp;
    }

    /**
     * Setter method for property <tt>clientIp</tt>.
     *
     * @param clientIp value to be assigned to property clientIp
     */
    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    /**
     * Getter method for property <tt>remoteIp</tt>.
     *
     * @return property value of remoteIp
     */
    public String getRemoteIp() {
        return remoteIp;
    }

    /**
     * Getter method for property <tt>remotePort</tt>.
     *
     * @return property value of remotePort
     */
    public int getRemotePort() {
        return remotePort;
    }

    /**
     * Getter method for property <tt>connectionId</tt>.
     *
     * @return property value of connectionId
     */
    public String getConnectionId() {
        return connectionId;
    }

    /**
     * Setter method for property <tt>connectionId</tt>.
     *
     * @param connectionId value to be assigned to property connectionId
     */
    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    /**
     * Getter method for property <tt>createTime</tt>.
     *
     * @return property value of createTime
     */
    public Date getCreateTime() {
        return createTime;
    }

    /**
     * Setter method for property <tt>createTime</tt>.
     *
     * @param createTime value to be assigned to property createTime
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * Getter method for property <tt>lastActiveTime</tt>.
     *
     * @return property value of lastActiveTime
     */
    public long getLastActiveTime() {
        return lastActiveTime;
    }

    /**
     * Setter method for property <tt>lastActiveTime</tt>.
     *
     * @param lastActiveTime value to be assigned to property lastActiveTime
     */
    public void setLastActiveTime(long lastActiveTime) {
        this.lastActiveTime = lastActiveTime;
    }

    /**
     * Getter method for property <tt>connectType</tt>.
     *
     * @return property value of connectType
     */
    public String getConnectType() {
        return connectType;
    }

    /**
     * Setter method for property <tt>connectType</tt>.
     *
     * @param connectType value to be assigned to property connectType
     */
    public void setConnectType(String connectType) {
        this.connectType = connectType;
    }

    /**
     * Getter method for property <tt>version</tt>.
     *
     * @return property value of version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Setter method for property <tt>version</tt>.
     *
     * @param version value to be assigned to property version
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Getter method for property <tt>localPort</tt>.
     *
     * @return property value of localPort
     */
    public int getLocalPort() {
        return localPort;
    }

    /**
     * Setter method for property <tt>localPort</tt>.
     *
     * @param localPort value to be assigned to property localPort
     */
    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    /**
     * recordPushQueueBlockTimes.
     */
    public void recordPushQueueBlockTimes() {
        if (this.firstPushQueueBlockTime == 0) {
            firstPushQueueBlockTime = System.currentTimeMillis();
        } else {
            lastPushQueueBlockTime = System.currentTimeMillis();
        }
    }

    /**
     * clear push queue block times.
     */
    public void clearPushQueueBlockTimes() {
        this.firstPushQueueBlockTime = 0;
        this.lastPushQueueBlockTime = 0;
    }

    /**
     * check block greater than the specific time.
     *
     * @param timeMillsSeconds check times.
     * @return
     */
    public boolean pushQueueBlockTimesLastOver(long timeMillsSeconds) {
        return this.lastPushQueueBlockTime - this.firstPushQueueBlockTime > timeMillsSeconds;
    }

    @Override
    public String toString() {
        return "ConnectionMeta{" + "connectType='" + connectType + '\'' + ", clientIp='" + clientIp + '\''
                + ", remoteIp='" + remoteIp + '\'' + ", remotePort=" + remotePort + ", localPort=" + localPort
                + ", version='" + version + '\'' + ", connectionId='" + connectionId + '\'' + ", createTime="
                + createTime + ", lastActiveTime=" + lastActiveTime + ", appName='" + appName + '\'' + ", tenant='"
                + tenant + '\'' + ", labels=" + labels + '}';
    }
}
