package com.ccl.test.stopWatch;

import org.junit.Test;
import org.springframework.util.StopWatch;

/**
 * Created by ccl on 16/12/12.
 */
public class TestStopWatch {
    @Test
    public void test1() throws Exception{
        StopWatch first = new StopWatch("First");
        first.start("A");
        Thread.sleep(200);
        first.stop();
        first.start("B");
        Thread.sleep(200);
        first.stop();
        first.start("C");
        Thread.sleep(120);
        first.stop();
        System.out.println(first.prettyPrint());
    }
}
