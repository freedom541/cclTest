package com.ccl.jersey.codegen;

import java.lang.annotation.Annotation;

/**
 * @author ccl
 * 
 */
@SuppressWarnings("all")
public class ParentModuleImpl implements ParentModule {

	private Class value;

	public ParentModuleImpl(Class value) {
		super();
		this.value = value;
	}

	@Override
	public Class<? extends Annotation> annotationType() {
		return ParentModule.class;
	}

	@Override
	public Class value() {
		return value;
	}

}