package com.ccl.test.junit;

import org.junit.*;

/**
 * Created by ccl on 17/2/14.
 */
public class TestJunit {
    private TestJunit tju = null;

    @BeforeClass
    public static void enter() {
        System.out.println("进来了！\r");
    }

    @Before
    public void init() {
        System.out.println("正在初始化...");
        tju = new TestJunit();
        System.out.println("初始化完毕！");
    }

    @Test
    public void testit() {
        tju.run();
    }

    @After
    public void destroy() {
        System.out.println("销毁对象。。。");
        tju = null;
        System.out.println("销毁完毕！");
    }

    @AfterClass
    public static void leave() {
        System.out.println("离开了！");
    }

    public void run(){
        System.out.println("执行run.");
    }
}
