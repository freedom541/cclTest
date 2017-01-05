package com.ccl.test.duotai;

/**
 * Created by ccl on 16/11/24.
 */
public class Sun extends Father {
    {
        bean = this.getClass().getName();
    }
    private String name;

    @Override
    public String getClassName(){
        return "com.ccl.test.duotai." + super.getAction();
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
                "action='" + super.getAction() + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
