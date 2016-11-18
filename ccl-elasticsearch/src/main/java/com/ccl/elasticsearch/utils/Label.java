package com.ccl.elasticsearch.utils;

import java.lang.annotation.*;

/**
 * 给bean或者属性添加注释
 * 
 * @author dean.lu
 * 
 */
@Target({ ElementType.TYPE, ElementType.FIELD, ElementType.METHOD,ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Label {
	String value();
}
