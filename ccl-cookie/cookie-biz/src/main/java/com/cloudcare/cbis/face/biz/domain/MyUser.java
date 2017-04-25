package com.cloudcare.cbis.face.biz.domain;

import org.joda.time.DateTime;

/**
 * Created by ccl on 17/4/9.
 */
public class MyUser {
    private String addr;
    private DateTime createTime;
    private Integer id;
    private String userId;

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public DateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(DateTime createTime) {
        this.createTime = createTime;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
