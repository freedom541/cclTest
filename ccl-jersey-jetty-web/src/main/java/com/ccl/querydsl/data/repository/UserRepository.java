package com.ccl.querydsl.data.repository;

import com.ccl.jersey.codegen.ModelQueryAndBatchUpdateRepository;

import com.ccl.querydsl.data.model.User;

import com.ccl.querydsl.data.entity.EUser;

import com.ccl.jersey.codegen.Label;

/**
 * UserRepository is a Querydsl repository interface type
 */
@Label("User存储")
public interface UserRepository extends ModelQueryAndBatchUpdateRepository<User, EUser, Integer> {

}

