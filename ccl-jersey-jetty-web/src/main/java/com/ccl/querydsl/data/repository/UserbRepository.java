package com.ccl.querydsl.data.repository;

import com.ccl.jersey.codegen.ModelQueryAndBatchUpdateRepository;

import com.ccl.querydsl.data.model.Userb;

import com.ccl.querydsl.data.entity.EUserb;

import com.ccl.jersey.codegen.Label;

/**
 * UserbRepository is a Querydsl repository interface type
 */
@Label("Userb存储")
public interface UserbRepository extends ModelQueryAndBatchUpdateRepository<Userb, EUserb, Integer> {

}

