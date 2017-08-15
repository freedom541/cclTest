package com.ccl.jersey.codegen;

import java.lang.annotation.Annotation;

/**
 * @author ccl
 * 
 */
@SuppressWarnings("all")
public class LabelImpl implements Label {

	private String value;

	public LabelImpl(String value) {
		super();
		this.value = value;
	}

	@Override
	public Class<? extends Annotation> annotationType() {
		return Label.class;
	}

	@Override
	public String value() {
		return value;
	}

}