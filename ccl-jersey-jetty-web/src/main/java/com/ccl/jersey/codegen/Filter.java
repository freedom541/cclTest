package com.ccl.jersey.codegen;

import com.google.common.collect.Lists;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.nonNull;

/**
 * Created by ccl on 2015/8/10.
 */
@Label("搜索条件")
public final class Filter implements Serializable {

    private Criterion criterion;

    private boolean not;

    private Filter() {
    }

    public static Filter condition() {
        return new Filter();
    }

    public Filter equal(String property, Object value) {
        buildTerm(property, Operator.EQ, value);
        return this;
    }

    public Filter isNull(String property) {
        buildTerm(property, Operator.EQ, null);
        return this;
    }

    public Filter like(String property, Object value) {
        buildTerm(property, Operator.LK, value);
        return this;
    }

    public Filter greaterThan(String property, Object value) {
        buildTerm(property, Operator.GT, value);
        return this;
    }

    public Filter greaterThanOrEqual(String property, Object value) {
        buildTerm(property, Operator.GE, value);
        return this;
    }

    public Filter lessThan(String property, Object value) {
        buildTerm(property, Operator.LT, value);
        return this;
    }

    public Filter lessThanOrEqual(String property, Object value) {
        buildTerm(property, Operator.LE, value);
        return this;
    }

    public Filter in(String property, Object value) {
        buildTerm(property, Operator.IN, value);
        return this;
    }


    public Filter equalAtNotNull(String property, Object value) {
        if (nonNull(value)) {
            buildTerm(property, Operator.EQ, value);
        }
        return this;
    }


    public Filter likeAtNotNull(String property, Object value) {
        if (nonNull(value)) {
            buildTerm(property, Operator.LK, value);
        }
        return this;
    }


    public Filter greaterThanAtNotNull(String property, Object value) {
        if (nonNull(value)) {
            buildTerm(property, Operator.GT, value);
        }
        return this;
    }

    public Filter greaterThanOrEqualAtNotNull(String property, Object value) {
        if (nonNull(value)) {
            buildTerm(property, Operator.GE, value);
        }
        return this;
    }

    public Filter lessThanAtNotNull(String property, Object value) {
        if (nonNull(value)) {
            buildTerm(property, Operator.LT, value);
        }
        return this;
    }

    public Filter lessThanOrEqualAtNotNull(String property, Object value) {
        if (nonNull(value)) {
            buildTerm(property, Operator.LE, value);
        }
        return this;
    }


    public Filter inAtNotNull(String property, Object value) {
        if (nonNull(value)) {
            if (value instanceof Collection) {
                Collection collection = (Collection) value;
                if (collection.isEmpty()) {
                    return this;
                }
                Iterator iterator = collection.iterator();
                while (iterator.hasNext()) {
                    Object next = iterator.next();
                    if (Objects.isNull(next)) {
                        iterator.remove();
                    }
                }
            }

            if (value.getClass().isArray()) {
                Object[] array = (Object[]) value;
                if (0 == array.length) {
                    return this;
                }
                List list = Lists.newArrayList();
                for (Object obj : array) {
                    if (nonNull(obj)) {
                        list.add(obj);
                    }
                }
                value = list;
            }
            buildTerm(property, Operator.IN, value);
        }
        return this;
    }


    public Filter buildTerm(String property, Operator operator, Object value) {
        if (Objects.isNull(this.criterion)) {
            this.criterion = new Term(property, operator, value);
        } else {
            this.criterion = new Criteria(criterion, AggOpr.AND, new Term(property, operator, value));
        }
        if (not) {
            this.criterion.not();
        }
        return this;
    }

    public Filter not() {
        if (Objects.isNull(criterion)) {
            not = true;
        } else {
            criterion.not();
        }
        return this;
    }

    public Filter and(Filter filter) {
        Assert.notNull(filter);
        if (nonNull(filter.getCriterion())) {
            if (Objects.isNull(criterion)) {
                this.criterion = filter.criterion;
            } else {
                Criteria criteria = new Criteria(criterion, AggOpr.AND, filter.criterion);
                this.criterion = criteria;
            }
        }
        return this;
    }

    public Filter or(Filter filter) {
        Assert.notNull(filter);
        if (nonNull(filter.getCriterion())) {
            if (Objects.isNull(criterion)) {
                this.criterion = filter.criterion;
            } else {
                Criteria criteria = new Criteria(criterion, AggOpr.OR, filter.criterion);
                this.criterion = criteria;
            }
        }
        return this;
    }

    public Criterion getCriterion() {
        return criterion;
    }

    @Override
    public String toString() {
        if (Objects.isNull(criterion)) return "";
        return criterion.toString();
    }

    public static void main(String[] args) {
        Filter filter = Filter.condition().equal("a", "b").and(Filter.condition().equal("c", "d"));
        System.out.println(filter);
        Filter filter1 = Filter.condition().equal("a", "b").equal("c", "d");
        System.out.println(filter1);
        Filter filter2 = Filter.condition().equal("a", "b").and(Filter.condition().equal("c", "d").or(Filter.condition().greaterThan("e", "f")));
        System.out.println(filter2);
        Filter filter3 = Filter.condition().equal("a", "b").equal("c", "d").isNull("e").not().and(Filter.condition().isNull("f"));
        System.out.println(filter3);
        Filter filter4 = Filter.condition().equal("a", "b").and(Filter.condition().not().equal("c", "d"));
        System.out.println(filter4);
    }
}
