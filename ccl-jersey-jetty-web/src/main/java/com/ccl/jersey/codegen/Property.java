/**
 * 
 */
package com.ccl.jersey.codegen;

import java.lang.annotation.*;

/**
 * 对应实体的字段
 * 
 * @author ccl
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Property {
	/**
	 * 对应实体的字段名
	 * 
	 * @return
	 */
	String name() default "";
}
