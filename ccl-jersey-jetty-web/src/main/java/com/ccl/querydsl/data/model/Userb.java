package com.ccl.querydsl.data.model;

import com.ccl.jersey.codegen.Label;
import javax.validation.constraints.Size;
import com.ccl.jersey.codegen.Domain;
import org.joda.time.DateTime;
import java.lang.String;
import javax.validation.constraints.NotNull;
import java.lang.Integer;

import com.ccl.jersey.codegen.AbstractDataModel;

import com.ccl.querydsl.data.entity.EUserb;

import com.ccl.jersey.codegen.CreateCheck;

import com.ccl.jersey.codegen.UpdateCheck;

/**
 * Userb is a Codegen model type
 */
@Label("Userb")
@Domain(domainClassName="com.ccl.querydsl.data.entity.EUserb")
public class Userb extends AbstractDataModel<EUserb, Integer> {

    @Size(max=128)
    @Label("addr")
    private String addr;

    @Label("createTime")
    private DateTime createTime;

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
    public String toString() {
         return String.format("Userb { addr : %s,createTime : %s,id : %s,userId : %s }",addr,createTime,id,userId);
    }

}

