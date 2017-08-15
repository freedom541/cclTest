package com.ccl.querydsl.data.repository;

import com.ccl.jersey.codegen.ModelQueryAndBatchUpdateRepository;

import com.ccl.querydsl.data.model.Blog;

import com.ccl.querydsl.data.entity.EBlog;

import com.ccl.jersey.codegen.Label;

/**
 * BlogRepository is a Querydsl repository interface type
 */
@Label("Blog存储")
public interface BlogRepository extends ModelQueryAndBatchUpdateRepository<Blog, EBlog, Integer> {

}

