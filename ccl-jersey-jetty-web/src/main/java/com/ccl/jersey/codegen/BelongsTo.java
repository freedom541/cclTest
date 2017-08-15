/**
 * 
 */
package com.ccl.jersey.codegen;

import java.lang.annotation.*;

/**
 * 映射多对一的情况
 * 
 * @author ccl
 * 
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BelongsTo {
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
	String rootField();

	/**
	 * 外键关联字段
	 * 
	 * @return
	 */
	String associatedField() default "id";

}
