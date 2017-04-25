package com.vmware.resourcepool;

import com.vmware.connection.BasicConnection;
import com.vmware.connection.Connection;

/**
 * Created by ccl on 17/3/13.
 */
public class VmwareResource implements ResourceFactory<Connection> {
    private String username;
    private String password;
    private String url;

    public VmwareResource(String username, String password, String url) {
        this.username = username;
        this.password = password;
        this.url = url;
    }

    public VmwareResource() {

    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * 创建资源
     */
    @Override
    public Connection createResource() {
        BasicConnection basic = new BasicConnection();
        basic.setUsername(username);
        basic.setPassword(password);
        basic.setUrl(url);
        return basic.connect();
    }

    /**
     * 验证资源是否有效
     *
     * @param o
     */
    @Override
    public boolean validateResource(Connection o) {
        return o.isConnected();
    }
}
