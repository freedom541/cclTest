package com.ccl.jersey.codegen;

import java.lang.annotation.Annotation;

/**
 * @author ccl
 * 
 */
@SuppressWarnings("all")
public class DomainImpl implements Domain {

	private String value;

	public DomainImpl(String value) {
		super();
		this.value = value;
	}

	@Override
	public Class<? extends Annotation> annotationType() {
		return Domain.class;
	}

	@Override
	public String domainClassName() {
		return value;
	}

}