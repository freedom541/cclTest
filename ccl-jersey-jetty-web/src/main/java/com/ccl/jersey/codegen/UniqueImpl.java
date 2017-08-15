package com.ccl.jersey.codegen;

import java.lang.annotation.Annotation;

/**
 * @author ccl
 * @date 2015/9/28.
 */
public class UniqueImpl implements Unique {
    private final String[] value;

    public UniqueImpl(String[] value) {
        this.value = value;
    }

    @Override
    public String[] value() {
        return value;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return Unique.class;
    }
}
