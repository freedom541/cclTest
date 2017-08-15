package com.ccl.jersey.codegen;

import javax.validation.Payload;
import javax.validation.constraints.NotNull;
import java.lang.annotation.Annotation;

/**
 * @author ccl
 * 
 */
@SuppressWarnings("all")
public class NotNullImpl implements NotNull {

	private Class<?>[] groups;

	@Override
	public Class<?>[] groups() {
		return groups == null ? new Class<?>[0] : groups;
	}

	@Override
	public String message() {
		return "{javax.validation.constraints.NotNull.message}";
	}

	@Override
	public Class<? extends Annotation> annotationType() {
		return NotNull.class;
	}

	@Override
	public Class<? extends Payload>[] payload() {
		return new Class[0];
	}

	public void setGroups(Class<?>[] groups) {
		this.groups = groups;
	}

}