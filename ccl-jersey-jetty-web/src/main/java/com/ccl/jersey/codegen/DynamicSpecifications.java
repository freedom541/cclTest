package com.ccl.jersey.codegen;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.*;
import com.querydsl.sql.RelationalPath;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DynamicSpecifications {

    public static <T> Specification<T> bySearchFilter(final Filter filter) {
        return (root, builder, entityPathResolver) -> {
            if (null != filter) {
                return parseFilter(filter, root, builder,
                        entityPathResolver);
            }
            return null;
        };
    }

    public static <T> Specification<T> bySearchTerm(final Term term) {
        return (root, builder, entityPathResolver) -> {
            if (null != term) {
                return parseTerm(term, root, builder,
                        entityPathResolver);
            }
            return null;
        };
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <T> Specification<T> byExample(final T entity) {
        return (root, builder, entityPathResolver) -> {
            BooleanBuilder booleanBuilder = new BooleanBuilder();
            Class entityClass1 = entity.getClass();
            Field[] fields = entityClass1.getDeclaredFields();
            for (Field field : fields) {
                if (!Modifier.isFinal(field.getModifiers())
                        && !Modifier.isStatic(field.getModifiers())) {
                    Object value = null;
                    try {
                        field.setAccessible(true);
                        value = field.get(entity);
                    } catch (IllegalArgumentException
                            | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    if (null != value) {
                        value = DataTypeConvertUtils
                                .convert(value, field.getType());
                        Path property = entityPathResolver
                                .getProperty(root, field.getName());
                        if (null != property
                                && property instanceof SimpleExpression) {
                            SimpleExpression simpleExpression = (SimpleExpression) property;
                            if (value instanceof Collection || value.getClass().isArray()) {
                                booleanBuilder.and(simpleExpression.in(value));
                            } else {
                                booleanBuilder.and(simpleExpression.eq(value));
                            }
                        }

                    }
                }

            }
            return booleanBuilder.getValue();
        };
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static <T> Predicate parseFilter(final Filter filter,
                                             RelationalPath<T> root, PathBuilder<T> builder,
                                             EntityPathResolver entityPathResolver) {
        if (null != filter.getCriterion()) {
            return parseCriterion(filter.getCriterion(), root, builder, entityPathResolver);
        }
        return null;
    }

    private static <T> Predicate parseCriterion(Criterion criterion, RelationalPath<T> root, PathBuilder<T> builder, EntityPathResolver entityPathResolver) {
        if (criterion instanceof Criteria) {
            Criteria criteria = (Criteria) criterion;
            Criterion left = criteria.getLeft();
            Criterion right = criteria.getRight();
            BooleanBuilder booleanBuilder = new BooleanBuilder(parseCriterion(left, root, builder, entityPathResolver));
            AggOpr opr = criteria.getOpr();
            if (opr.equals(AggOpr.AND)) {
                booleanBuilder.and(parseCriterion(right, root, builder, entityPathResolver));
            } else if (opr.equals(AggOpr.OR)) {
                booleanBuilder.or(parseCriterion(right, root, builder, entityPathResolver));
            }
            if (criterion.isNot()) {
                booleanBuilder.not();
            }
            return booleanBuilder.getValue();
        }
        if (criterion instanceof Term) {
            BooleanExpression expression = parseTerm((Term) criterion, root, builder, entityPathResolver);
            return expression;
        }
        return null;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static <T> BooleanExpression parseTerm(final Term term,
                                                   RelationalPath<T> root, PathBuilder<T> builder,
                                                   EntityPathResolver entityPathResolver) {
        BooleanExpression booleanExpression = null;
        Object value = term.getValue();
        Path<T> expression = entityPathResolver.getProperty(root,
                term.getProperty());
        if (null == expression) {
            expression = (Path<T>) builder.get(term.getProperty());
        }
        if (null == expression) {
            throw new IllegalArgumentException("term property ["
                    + term.getProperty() + "] not found.");
        }
        if (null == value) {// 空值处理
            if (expression instanceof SimpleExpression) {
                SimpleExpression simpleExpression = (SimpleExpression) expression;
                if (Operator.EQ.equals(term.getOperator())) {
                    booleanExpression = simpleExpression.isNull();
                } else {
                    throw new IllegalArgumentException("term property ["
                            + term.getProperty()
                            + "] condition value is empty.");

                }
            }
        } else {

            if (expression instanceof SimpleExpression) {
                SimpleExpression simpleExpression = (SimpleExpression) expression;
                if (value instanceof Collection) {
                    Collection collection = (Collection) value;
                    List<Object> list = new ArrayList<>();
                    for (Object val : collection) {
                        list.add(DataTypeConvertUtils.convert(val, expression.getType()));
                    }
                    collection = list;
                    switch (term.getOperator()) {
                        case IN:
                            booleanExpression = simpleExpression.in(collection);
                            break;
                        default:
                            throw new IllegalArgumentException(
                                    "term property [" + term.getProperty()
                                            + "] operator ["
                                            + term.getOperator()
                                            + "] collection not support.");
                    }
                } else if (value.getClass().isArray()) {
                    Object[] array = (Object[]) value;
                    List<Object> list = new ArrayList<>();
                    for (int i = 0; i < array.length; i++) {
                        list.add(DataTypeConvertUtils.convert(array[i], expression.getType()));
                    }
                    switch (term.getOperator()) {
                        case IN:
                            booleanExpression = simpleExpression.in(list);
                            break;
                        default:
                            throw new IllegalArgumentException(
                                    "term property [" + term.getProperty()
                                            + "] operator ["
                                            + term.getOperator()
                                            + "] array not support.");
                    }
                } else {
                    value = DataTypeConvertUtils.convert(value, expression.getType());

                    switch (term.getOperator()) {
                        case EQ:
                            booleanExpression = simpleExpression.eq(value);
                            break;
                        case IN:
                            booleanExpression = simpleExpression.in(value);
                            break;
                        default:
                            if (value instanceof Comparable && expression instanceof ComparableExpression) {
                                Comparable comparable = (Comparable) value;
                                ComparableExpression comparableExpression = (ComparableExpression) expression;
                                switch (term.getOperator()) {
                                    case GT:
                                        booleanExpression = comparableExpression
                                                .gt(comparable);
                                        break;
                                    case LT:
                                        booleanExpression = comparableExpression
                                                .lt(comparable);
                                        break;
                                    case GE:
                                        booleanExpression = comparableExpression
                                                .goe(comparable);
                                        break;
                                    case LE:
                                        booleanExpression = comparableExpression
                                                .loe(comparable);
                                        break;
                                    default:
                                        if (expression instanceof StringExpression) {
                                            StringExpression stringPath = (StringExpression) expression;
                                            switch (term.getOperator()) {
                                                case LK:
                                                    booleanExpression = stringPath
                                                            .like(value + "");
                                                    break;
                                                default:
                                                    throw new IllegalArgumentException(
                                                            "term property ["
                                                                    + term.getProperty()
                                                                    + "] operator ["
                                                                    + term.getOperator()
                                                                    + "] not support.");
                                            }

                                        }
                                        break;
                                }
                            }
                            if (value instanceof Number && expression instanceof NumberExpression) {
                                Number number = (Number) value;
                                NumberExpression numberExpression = (NumberExpression) expression;
                                switch (term.getOperator()) {
                                    case GT:
                                        booleanExpression = numberExpression
                                                .gt(number);
                                        break;
                                    case LT:
                                        booleanExpression = numberExpression
                                                .lt(number);
                                        break;
                                    case GE:
                                        booleanExpression = numberExpression
                                                .goe(number);
                                        break;
                                    case LE:
                                        booleanExpression = numberExpression
                                                .loe(number);
                                        break;
                                }
                            }
                            break;
                    }
                }
            } else {
                throw new IllegalArgumentException("term property ["
                        + term.getProperty() + "] not simple expression.");
            }
        }

        if (null != booleanExpression && term.isNot()) {
            booleanExpression = booleanExpression.not();
        }
        return booleanExpression;
    }
}
