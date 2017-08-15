package com.ccl.querydsl.data.model;

import com.ccl.jersey.codegen.Label;
import javax.validation.constraints.Size;
import com.ccl.jersey.codegen.Domain;
import java.lang.String;
import javax.validation.constraints.NotNull;
import java.lang.Integer;

import com.ccl.jersey.codegen.AbstractDataModel;

import com.ccl.querydsl.data.entity.EBlog;

import com.ccl.jersey.codegen.CreateCheck;

import com.ccl.jersey.codegen.UpdateCheck;

/**
 * Blog is a Codegen model type
 */
@Label("Blog")
@Domain(domainClassName="com.ccl.querydsl.data.entity.EBlog")
public class Blog extends AbstractDataModel<EBlog, Integer> {

    @Size(max=16777215)
    @Label("content")
    private String content;

    @Label("id")
    private Integer id;

    @Size(max=200)
    @Label("title")
    private String title;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
         return String.format("Blog { content : %s,id : %s,title : %s }",content,id,title);
    }

}

