package com.ccl.test;

import com.ccl.com.ccl.service.UserService;
import org.junit.Test;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

/**
 * Created by ccl on 17/4/25.
 */
public class TestDemo {


    @Configuration
    @ComponentScan("com.ccl")
    @ImportResource({"classpath*:META-INF/spring/*.xml"})
    public static class SpringRootConfiguration {

    }

    public static AnnotationConfigWebApplicationContext getContext(){
        AnnotationConfigWebApplicationContext ctx = new AnnotationConfigWebApplicationContext();
        ctx.register(SpringRootConfiguration.class);
        ctx.refresh();
        return ctx;
    }

    @Test
    public void test(){
        AnnotationConfigWebApplicationContext ctx = TestDemo.getContext();
        UserService user = ctx.getBean(UserService.class);
        user.say();
    }
}
