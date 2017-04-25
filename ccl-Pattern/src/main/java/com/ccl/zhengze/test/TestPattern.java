package com.ccl.zhengze.test;

import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ccl on 17/4/13.
 */
public class TestPattern {
    @Test
    public void test(){
        // 定义一个样式模板，此中使用正则表达式，括号中是要抓的内容
        // 相当于埋好了陷阱匹配的地方就会掉下去
        Pattern pattern = Pattern.compile("href=\"(.+?)\"");
        Pattern p2 = Pattern.compile("src=//(.+?)>");
        // 定义一个matcher用来做匹配
        Matcher matcher = pattern.matcher("＜a href=\"index.html\"＞我的主页＜/a＞");
        Matcher m2 = p2.matcher("<img src=//www.baidu.com/img/gs.gif>");
        // 如果找到了
        if (matcher.find()) {
            // 打印出结果
            System.out.println(matcher.group(1));
        }
        if (m2.find()){
            System.out.println(m2.group());
            System.out.println(m2.group(1));
        }
    }
}
