package com.ccl.jersey.codegen;

import java.lang.annotation.Annotation;

/**
 * @author ccl
 * 
 */
@SuppressWarnings("all")
public class HasManysImpl implements HasManys {

	private final HasMany[] hasManys;

	public HasManysImpl(HasMany[] hasManys) {
		super();
		this.hasManys = hasManys;
	}

	@Override
	public Class<? extends Annotation> annotationType() {
		return HasManys.class;
	}

	@Override
	public HasMany[] values() {
		return hasManys;
	}

}