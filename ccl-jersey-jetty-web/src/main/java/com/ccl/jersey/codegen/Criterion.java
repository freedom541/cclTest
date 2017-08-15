package com.ccl.jersey.codegen;

/**
 * @author ccl
 * @date 2016/5/19.
 */
public abstract class Criterion {

    private boolean not;

    public boolean isNot() {
        return not;
    }

    public void not() {
        this.not = true;
    }

    @Override
    public String toString() {
        return isNot() ? " NOT " : "";
    }
}
