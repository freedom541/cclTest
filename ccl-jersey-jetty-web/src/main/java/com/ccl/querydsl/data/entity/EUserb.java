package com.ccl.querydsl.data.entity;

import com.ccl.jersey.codegen.AbstractIdEntity;

import com.ccl.jersey.codegen.CreateCheck;

import com.ccl.jersey.codegen.UpdateCheck;

import com.ccl.jersey.codegen.Label;
import javax.validation.constraints.Size;
import org.joda.time.DateTime;
import java.lang.String;
import javax.validation.constraints.NotNull;
import java.lang.Integer;

/**
 * EUserb is a Querydsl bean type
 */
@Label("Userb")
public class EUserb extends AbstractIdEntity<Integer> {

    public EUserb() {
    }

    @Size(max=128)
    @Label("addr")
    private String addr;

    @Label("createTime")
    private DateTime createTime;

    @NotNull(groups={UpdateCheck.class})
    @Label("id")
    private Integer id;

    @Size(max=64)
    @Label("userId")
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

    @Override
    public void setDefaultValue() {
    }

    @Override
    public String toString() {
         return String.format("EUserb { addr : %s,createTime : %s,id : %s,userId : %s }",addr,createTime,id,userId);
    }

}

