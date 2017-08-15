package com.ccl.jersey.codegen;

import java.lang.annotation.Annotation;

/**
 * @author ccl
 * 
 */
@SuppressWarnings("all")
public class HasManyImpl implements HasMany {

	private final String property;

	private String rootField;

	private final String associatedField;

	public HasManyImpl(String property, String associatedField) {
		super();
		this.property = property;
		this.associatedField = associatedField;
	}

	public HasManyImpl(String property, String rootField, String associatedField) {
		super();
		this.property = property;
		this.rootField = rootField;
		this.associatedField = associatedField;
	}

	@Override
	public Class<? extends Annotation> annotationType() {
		return HasMany.class;
	}

	@Override
	public String property() {
		return property;
	}

	@Override
	public String rootField() {
		return rootField;
	}

	@Override
	public String associatedField() {
		return associatedField;
	}

}