package com.ccl.jersey.codegen;

import org.springframework.stereotype.Repository;

import java.lang.annotation.Annotation;

@SuppressWarnings("all")
public class RepositoryImpl implements Repository {
	String name;

	public RepositoryImpl(String name) {
		super();
		this.name = name;
	}

	@Override
	public Class<? extends Annotation> annotationType() {
		return Repository.class;
	}

	@Override
	public String value() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
