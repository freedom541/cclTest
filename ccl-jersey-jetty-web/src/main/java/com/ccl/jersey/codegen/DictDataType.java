package com.ccl.jersey.codegen;

import java.lang.annotation.*;

/**
 * @author ccl
 * @date 2016/6/17.
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DictDataType {
    Class<? extends Enum> value();
}
