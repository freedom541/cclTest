package com.ccl.jersey.codegen;

import java.lang.annotation.Annotation;

/**
 * @author ccl
 * 
 */
@SuppressWarnings("all")
public class BelongsToImpl implements BelongsTo {

	private final String property;

	private final String rootField;

	private String associatedField;

	public BelongsToImpl(String property, String rootField) {
		super();
		this.property = property;
		this.rootField = rootField;
	}

	public BelongsToImpl(String property, String rootField,
			String associatedField) {
		super();
		this.property = property;
		this.rootField = rootField;
		this.associatedField = associatedField;
	}

	@Override
	public Class<? extends Annotation> annotationType() {
		return BelongsTo.class;
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