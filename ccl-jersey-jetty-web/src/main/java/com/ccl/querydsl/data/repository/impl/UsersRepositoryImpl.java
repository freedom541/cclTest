package com.ccl.querydsl.data.repository.impl;

import com.ccl.jersey.codegen.AbstractModelQueryAndBatchUpdateRepository;

import com.ccl.querydsl.data.entity.EUsers;

import com.ccl.querydsl.data.model.Users;

import com.ccl.querydsl.data.repository.UsersRepository;

import com.ccl.jersey.codegen.Label;
import org.springframework.validation.annotation.Validated;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * UsersRepositoryImpl is a Querydsl repository implement type
 */
@Label("Users存储实现")
@Validated({})
@Repository("usersRepository")
public class UsersRepositoryImpl extends AbstractModelQueryAndBatchUpdateRepository<EUsers, Integer, Users> implements UsersRepository {

    @Autowired
    public UsersRepositoryImpl(DataSource dataSource) {
    	super(dataSource);
    }
    
}

