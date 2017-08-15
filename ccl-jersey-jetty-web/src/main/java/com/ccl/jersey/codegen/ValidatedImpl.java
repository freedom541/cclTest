package com.ccl.jersey.codegen;

import org.springframework.validation.annotation.Validated;

import java.lang.annotation.Annotation;

/**
 * @author ccl
 * @date 2016/1/17.
 */
public class ValidatedImpl implements Validated {

    private Class<?>[] group;

    public ValidatedImpl(Class<?>[] group) {
        this.group = group;
    }

    @Override
    public Class<?>[] value() {
        return group;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return Validated.class;
    }
}
