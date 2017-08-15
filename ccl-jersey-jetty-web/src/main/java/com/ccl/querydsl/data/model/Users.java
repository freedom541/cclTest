package com.ccl.querydsl.data.model;

import com.ccl.jersey.codegen.Label;
import javax.validation.constraints.Size;
import com.ccl.jersey.codegen.Domain;
import java.lang.Boolean;
import java.lang.String;
import javax.validation.constraints.NotNull;
import java.lang.Integer;

import com.ccl.jersey.codegen.AbstractDataModel;

import com.ccl.querydsl.data.entity.EUsers;

import com.ccl.jersey.codegen.CreateCheck;

import com.ccl.jersey.codegen.UpdateCheck;

/**
 * Users is a Codegen model type
 */
@Label("Users")
@Domain(domainClassName="com.ccl.querydsl.data.entity.EUsers")
public class Users extends AbstractDataModel<EUsers, Integer> {

    @Label("available")
    private Boolean available;

    @Size(max=45)
    @Label("email")
    private String email;

    @Size(max=2)
    @Label("gender")
    private String gender;

    @Label("id")
    private Integer id;

    @Size(max=20)
    @Label("password")
    private String password;

    @Size(max=15)
    @Label("qq")
    private String qq;

    @Size(max=15)
    @Label("username")
    private String username;

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getQq() {
        return qq;
    }

    public void setQq(String qq) {
        this.qq = qq;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
         return String.format("Users { available : %s,email : %s,gender : %s,id : %s,password : %s,qq : %s,username : %s }",available,email,gender,id,password,qq,username);
    }

}

