package com.ccl.jersey.codegen;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Label("查询条件项")
public class Term extends Criterion implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -464760821506810447L;
    @NotNull
    @Label("属性")
    private String property;
    @Label("值")
    private Object value;
    @NotNull
    @Label("操作符")
    private Operator operator;

    public Term(String property, Object value) {
        this.property = property;
        this.value = value;
        this.operator = Operator.EQ;
    }

    public Term(String property, Operator operator, Object value) {
        this.property = property;
        this.value = value;
        this.operator = operator;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Term term = (Term) o;

        if (!property.equals(term.property)) return false;
        if (value != null ? !value.equals(term.value) : term.value != null) return false;
        return operator == term.operator;

    }

    @Override
    public int hashCode() {
        int result = property.hashCode();
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + operator.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return (super.toString()) + String.format("%s %s %s", property, operator, value);
    }

}