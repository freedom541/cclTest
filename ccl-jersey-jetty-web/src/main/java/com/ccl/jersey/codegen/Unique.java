package com.ccl.jersey.codegen;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 标注唯一键
 * 
 * @author ccl
 *
 */
@Target({ TYPE })
@Retention(RUNTIME)
@Documented
public @interface Unique {
	/**
	 * 唯一键字段列表
	 * 
	 * @return
	 */
	String[] value();
}
