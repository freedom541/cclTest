package com.ccl.test.math;

/**
 * Created by ccl on 17/1/5.
 */
public class MapJ {
    private String key;//替换的编号
    private String value;//值
    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }
    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }
    public MapJ(String key, String value) {
        super();
        this.key = key;
        this.value = value;
    }
    public MapJ() {
        super();
    }

}
