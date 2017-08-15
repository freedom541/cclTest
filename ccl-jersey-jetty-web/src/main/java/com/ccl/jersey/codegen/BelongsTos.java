package com.ccl.jersey.codegen;

import java.lang.annotation.*;

/**
 * 标注多个属于的关联关系
 * 
 * @author ccl
 * 
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BelongsTos {
	BelongsTo[] values();
}
