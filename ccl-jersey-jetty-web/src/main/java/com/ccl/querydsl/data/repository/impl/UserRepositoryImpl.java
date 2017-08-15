package com.ccl.querydsl.data.repository.impl;

import com.ccl.jersey.codegen.AbstractModelQueryAndBatchUpdateRepository;

import com.ccl.querydsl.data.entity.EUser;

import com.ccl.querydsl.data.model.User;

import com.ccl.querydsl.data.repository.UserRepository;

import com.ccl.jersey.codegen.Label;
import org.springframework.validation.annotation.Validated;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * UserRepositoryImpl is a Querydsl repository implement type
 */
@Label("User存储实现")
@Validated({})
@Repository("userRepository")
public class UserRepositoryImpl extends AbstractModelQueryAndBatchUpdateRepository<EUser, Integer, User> implements UserRepository {

    @Autowired
    public UserRepositoryImpl(DataSource dataSource) {
    	super(dataSource);
    }
    
}

