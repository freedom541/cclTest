package com.ccl.jersey.codegen;

import java.lang.annotation.Annotation;

/**
 * @author ccl
 * @date 2016/6/29.
 */
public class OverrideImpl implements Override {
    @Override
    public Class<? extends Annotation> annotationType() {
        return Override.class;
    }
}
