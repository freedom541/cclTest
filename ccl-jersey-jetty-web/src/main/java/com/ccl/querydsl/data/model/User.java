package com.ccl.querydsl.data.model;

import com.ccl.jersey.codegen.Label;
import javax.validation.constraints.Size;
import com.ccl.jersey.codegen.Domain;
import java.lang.String;
import javax.validation.constraints.NotNull;
import java.lang.Integer;

import com.ccl.jersey.codegen.AbstractDataModel;

import com.ccl.querydsl.data.entity.EUser;

import com.ccl.jersey.codegen.CreateCheck;

import com.ccl.jersey.codegen.UpdateCheck;

/**
 * User is a Codegen model type
 */
@Label("User")
@Domain(domainClassName="com.ccl.querydsl.data.entity.EUser")
public class User extends AbstractDataModel<EUser, Integer> {

    @Label("id")
    private Integer id;

    @Size(max=255)
    @Label("name")
    private String name;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
         return String.format("User { id : %s,name : %s }",id,name);
    }

}

