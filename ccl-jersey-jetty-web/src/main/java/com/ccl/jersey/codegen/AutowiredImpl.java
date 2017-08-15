package com.ccl.jersey.codegen;

import org.springframework.beans.factory.annotation.Autowired;

import java.lang.annotation.Annotation;

@SuppressWarnings("all")
public class AutowiredImpl implements Autowired{

	@Override
	public Class<? extends Annotation> annotationType() {
		return Autowired.class;
	}

	@Override
	public boolean required() {
		return true;
	}

}
