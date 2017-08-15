package com.ccl.jersey.codegen;

import java.lang.annotation.Annotation;

/**
 * @author ccl
 * 
 */
@SuppressWarnings("all")
public class BelongsTosImpl implements BelongsTos {

	private final BelongsTo[] belongsTos;

	public BelongsTosImpl(BelongsTo[] belongsTos) {
		super();
		this.belongsTos = belongsTos;
	}

	@Override
	public Class<? extends Annotation> annotationType() {
		return BelongsTos.class;
	}

	@Override
	public BelongsTo[] values() {
		return belongsTos;
	}

}