package com.ccl.test.StringTest;

import org.junit.Test;

/**
 * Created by ccl on 17/3/23.
 */
public class TestStr {
    @Test
    public void string(){
        String str = "jzang/zhang/nihao.vk";
        if (str.contains("nihao")){
            System.out.println("nihao");
        }
    }
}
