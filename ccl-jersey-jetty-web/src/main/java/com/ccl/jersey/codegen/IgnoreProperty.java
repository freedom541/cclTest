package com.ccl.jersey.codegen;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 标注需要忽略的字段
 * 
 * @author ccl
 */
@Target({ FIELD, METHOD, })
@Retention(RUNTIME)
@Documented
public @interface IgnoreProperty {

	Class<?>[] groups() default {};

}
