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
 * EBlog is a Querydsl bean type
 */
@Label("Blog")
public class EBlog extends AbstractIdEntity<Integer> {

    public EBlog() {
    }

    @Size(max=16777215)
    @NotNull(groups={CreateCheck.class, UpdateCheck.class})
    @Label("content")
    private String content;

    @NotNull(groups={UpdateCheck.class})
    @Label("id")
    private Integer id;

    @Size(max=200)
    @NotNull(groups={CreateCheck.class, UpdateCheck.class})
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
    public void setDefaultValue() {
    }

    @Override
    public String toString() {
         return String.format("EBlog { content : %s,id : %s,title : %s }",content,id,title);
    }

}

