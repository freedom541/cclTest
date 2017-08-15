package com.ccl.jersey.codegen;

import java.lang.annotation.*;

/**
 * 标注多个一对多的关联关系
 * 
 * @author ccl
 * 
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HasManys {
	HasMany[] values();
}
