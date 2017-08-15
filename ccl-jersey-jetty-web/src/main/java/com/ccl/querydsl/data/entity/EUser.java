package com.ccl.querydsl.data.entity;

import com.ccl.jersey.codegen.AbstractIdEntity;

import com.ccl.jersey.codegen.CreateCheck;

import com.ccl.jersey.codegen.UpdateCheck;

import com.ccl.jersey.codegen.Label;
import javax.validation.constraints.Size;
import java.lang.String;
import javax.validation.constraints.NotNull;
import java.lang.Integer;

/**
 * EUser is a Querydsl bean type
 */
@Label("User")
public class EUser extends AbstractIdEntity<Integer> {

    public EUser() {
    }

    @NotNull(groups={UpdateCheck.class})
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
    public void setDefaultValue() {
    }

    @Override
    public String toString() {
         return String.format("EUser { id : %s,name : %s }",id,name);
    }

}

