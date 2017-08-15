package com.ccl.jersey.codegen;

import java.lang.annotation.*;

/**
 * 标注Action所属模块
 * 
 * @author ccl
 * 
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Action {
	/**
	 * 动作名称
	 * 
	 * @return
	 */
	String value() default "";

}
