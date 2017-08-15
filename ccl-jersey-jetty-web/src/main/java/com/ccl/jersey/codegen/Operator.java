package com.ccl.jersey.codegen;

@Label("操作符")
public enum Operator {
    @Label("等于")
    EQ, // 等于
    @Label("匹配查询")
    LK, // 匹配查询
    @Label("大于")
    GT, // 大于
    @Label("小于")
    LT, // 小于
    @Label("大于等于")
    GE, // 大于等于
    @Label("小于等于")
    LE, // 小于等于
    @Label("枚举值")
    IN, // 枚举值
}