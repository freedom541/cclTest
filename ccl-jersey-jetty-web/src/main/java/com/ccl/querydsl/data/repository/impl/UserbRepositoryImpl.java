package com.ccl.querydsl.data.repository.impl;

import com.ccl.jersey.codegen.AbstractModelQueryAndBatchUpdateRepository;

import com.ccl.querydsl.data.entity.EUserb;

import com.ccl.querydsl.data.model.Userb;

import com.ccl.querydsl.data.repository.UserbRepository;

import com.ccl.jersey.codegen.Label;
import org.springframework.validation.annotation.Validated;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * UserbRepositoryImpl is a Querydsl repository implement type
 */
@Label("Userb存储实现")
@Validated({})
@Repository("userbRepository")
public class UserbRepositoryImpl extends AbstractModelQueryAndBatchUpdateRepository<EUserb, Integer, Userb> implements UserbRepository {

    @Autowired
    public UserbRepositoryImpl(DataSource dataSource) {
    	super(dataSource);
    }
    
}

