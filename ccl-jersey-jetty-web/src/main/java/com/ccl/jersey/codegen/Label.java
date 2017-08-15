package com.ccl.jersey.codegen;

import java.lang.annotation.*;

/**
 * 给bean或者属性添加注释
 * 
 * @author ccl
 * 
 */
@Target({ ElementType.TYPE, ElementType.FIELD, ElementType.METHOD,ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Label {
	String value();
}
