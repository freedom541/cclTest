package com.ccl.jersey.codegen;

import java.lang.annotation.Annotation;

/**
 * @author ccl
 * @date 2015/9/28.
 */
public class UniquesImpl implements Uniques {
    private final Unique[] value;

    public UniquesImpl(Unique[] value) {
        this.value = value;
    }

    @Override
    public Unique[] values() {
        return value;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return Uniques.class;
    }
}
