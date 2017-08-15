package com.ccl.jersey.codegen;

import java.util.List;

/**
 * Created by ccl on 2015/7/20.
 * <p>
 * 日志元数据
 */
public class LogMetadata {
    /**
     * 事件
     */
    private String event;

    /**
     * 是否成功
     */
    private boolean success;
    /**
     * 對象類型
     */
    private String objectType;
    /**
     * 對象編號
     */
    private String objectId;
    /**
     * 標籤
     */
    private List<String> tag;
    /**
     * 操作用戶
     */
    private String user;
    /**
     * 操作地址
     */
    private String remoteHost;


    public LogMetadata(String event, boolean success, String objectType, String objectId, List<String> tag) {
        this.event = event;
        this.success = success;
        this.objectType = objectType;
        this.objectId = objectId;
        this.tag = tag;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public List<String> getTag() {
        return tag;
    }

    public void setTag(List<String> tag) {
        this.tag = tag;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }

    @Override
    public String toString() {
        return "LogMetadata{" +
                "event='" + event + '\'' +
                ", success=" + success +
                ", objectType='" + objectType + '\'' +
                ", objectId='" + objectId + '\'' +
                ", tag=" + tag +
                ", user='" + user + '\'' +
                ", remoteHost='" + remoteHost + '\'' +
                '}';
    }
}
