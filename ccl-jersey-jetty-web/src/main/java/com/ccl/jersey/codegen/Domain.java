package com.ccl.jersey.codegen;

import java.lang.annotation.*;

/**
 * 在VO对象上标注对应的实体类，以便完成相互的转换。
 * 
 * @author ccl
 * 
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Domain {
	String domainClassName();
}
