package com.ccl.jersey.codegen;

import java.lang.annotation.*;

/**
 * 執行順序
 *
 * @author ccl
 *
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Order {

	/**
	 * 值越小，優先級越高
	 *
	 * @return
	 */
	int value();

}
