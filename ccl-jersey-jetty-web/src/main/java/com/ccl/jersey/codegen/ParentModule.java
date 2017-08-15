package com.ccl.jersey.codegen;

import java.lang.annotation.*;

/**
 * 标记上级模块
 *
 * @author ccl
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ParentModule {

    /**
     * 上级模块类
     *
     * @return
     */
    Class value();

}
