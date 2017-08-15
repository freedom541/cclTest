/**
 * 
 */
package com.ccl.jersey.codegen;

import java.lang.annotation.*;

/**
 * 映射一对多的情况
 * 
 * @author ccl
 * 
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HasMany {
	/**
	 * 属性名称
	 * 
	 * @return
	 */
	String property();
	
	/**
	 * 关联字段
	 * 
	 * @return
	 */
	String rootField() default "id";

	/**
	 * 外键关联字段
	 * 
	 * @return
	 */
	String associatedField();
}
