package com.ccl.jersey.codegen;

import org.springframework.stereotype.Controller;

import java.lang.annotation.Annotation;

@SuppressWarnings("all")
public class ControllerImpl implements Controller {
	String name;

	public ControllerImpl(String name) {
		super();
		this.name = name;
	}

	@Override
	public Class<? extends Annotation> annotationType() {
		return Controller.class;
	}

	@Override
	public String value() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
