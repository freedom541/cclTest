package com.ccl.test.duotai;

/**
 * Created by ccl on 16/11/24.
 */
public class Sun extends Father {
    {
        bean = this.getClass().getName();
    }
    private String action;
    private String name;

    @Override
    public String getClassName(){
        return "com.ccl.test.duotai." + action;
    }
    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Sun{" +
                "action='" + action + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
