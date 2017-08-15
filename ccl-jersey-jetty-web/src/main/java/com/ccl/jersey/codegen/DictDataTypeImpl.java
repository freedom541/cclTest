package com.ccl.jersey.codegen;

import java.lang.annotation.Annotation;

/**
 * @author ccl
 * @date 2016/6/17.
 */
public class DictDataTypeImpl implements DictDataType {

    Class<? extends Enum> value;

    public DictDataTypeImpl(Class<? extends Enum> value) {
        this.value = value;
    }

    @Override
    public Class<? extends Enum> value() {
        return value;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return DictDataType.class;
    }
}
