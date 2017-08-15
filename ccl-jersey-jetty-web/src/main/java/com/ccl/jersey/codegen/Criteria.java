package com.ccl.jersey.codegen;

import java.io.Serializable;

/**
 * 聚合搜索条件
 *
 * @author ccl
 */
@Label("聚合条件")
public final class Criteria extends Criterion implements Serializable {

    private final Criterion left;

    private final AggOpr opr;

    private final Criterion right;

    public Criteria(Criterion left, AggOpr opr, Criterion right) {
        this.left = left;
        this.opr = opr;
        this.right = right;
    }

    public Criterion getLeft() {
        return left;
    }

    public AggOpr getOpr() {
        return opr;
    }

    public Criterion getRight() {
        return right;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Criteria criteria = (Criteria) o;

        if (!left.equals(criteria.left)) return false;
        if (opr != criteria.opr) return false;
        return right.equals(criteria.right);

    }

    @Override
    public int hashCode() {
        int result = left.hashCode();
        result = 31 * result + opr.hashCode();
        result = 31 * result + right.hashCode();
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sql = new StringBuilder();
        sql.append(super.toString());
        sql.append(" ( ");
        sql.append(left).append(" ").append(opr).append(" ").append(right);
        sql.append(" ) ");
        return sql.toString();
    }

}
