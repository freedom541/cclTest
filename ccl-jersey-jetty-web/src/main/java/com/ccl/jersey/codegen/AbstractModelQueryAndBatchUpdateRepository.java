package com.ccl.jersey.codegen;

import com.google.common.collect.Lists;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.sql.RelationalPath;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Created by ccl on 2015/9/18.
 */
public abstract class AbstractModelQueryAndBatchUpdateRepository<Entity extends IdEntity<ID>, ID extends Serializable, Model extends DataModel<Entity, ID>>
        extends AbstractModelUpdateRepository<Entity, ID, Model> implements ModelQueryAndBatchUpdateRepository<Model, Entity, ID> {

    public AbstractModelQueryAndBatchUpdateRepository(DataSource dataSource) {
        super(dataSource);
    }

    public AbstractModelQueryAndBatchUpdateRepository(QueryDslConfig queryDslConfig) {
        super(queryDslConfig);
    }

    @Override
    public Model updateAllByModel(Model model, Filter filter) {
        Assert.notNull(model, "update model is required.");
        Entity entity = BeanConvertUtils.convertModelToEntity(model,
                entityClass);
        if (modelBeanDesc.hasBelongsTo()) {
            List<AssociatedTable> tables = getAssociatedTableFromDesc(modelBeanDesc
                    .getBelongsTos());
            List<Predicate> predicates = null;
            if (null != filter) {
                predicates = new ArrayList<>();
                predicates.add(convertModelFilter(tables, filter));
            }
            updateAll(entity, tables, null == predicates ? null
                    : predicates.toArray(new Predicate[predicates.size()]));
        } else {
            super.updateAll(entity, filter);
        }
        return BeanConvertUtils.convertEntityToModel(entity, modelClass);
    }

    @Override
    public Model updateAllByModel(Model model, Model example) {
        Assert.notNull(model, "update model is required.");
        Entity entity = BeanConvertUtils.convertModelToEntity(model,
                entityClass);
        if (modelBeanDesc.hasBelongsTo()) {
            List<AssociatedTable> tables = getAssociatedTableFromDesc(modelBeanDesc
                    .getBelongsTos());
            List<Predicate> predicates = null;
            if (null != example) {
                predicates = new ArrayList<>();
                predicates.add(convertModelFilter(tables, convertExampleToFilter(example, null)));
            }
            updateAll(entity, tables, null == predicates ? null
                    : predicates.toArray(new Predicate[predicates.size()]));
        } else {
            super.updateAll(entity, BeanConvertUtils.convertModelToEntity(example, entityClass));
        }
        return BeanConvertUtils.convertEntityToModel(entity, modelClass);
    }

    @Override
    public void deleteAllByModel(Filter filter) {
        if (modelBeanDesc.hasBelongsTo()) {
            List<AssociatedTable> tables = getAssociatedTableFromDesc(modelBeanDesc
                    .getBelongsTos());
            List<Predicate> predicates = null;
            if (null != filter) {
                predicates = new ArrayList<>();
                predicates.add(convertModelFilter(tables, filter));
            }
            deleteAll(tables, null == predicates ? null
                    : predicates.toArray(new Predicate[predicates.size()]));
        } else {
            super.deleteAll(filter);
        }
    }

    @Override
    public void deleteAllByModel(Model example) {
        if (modelBeanDesc.hasBelongsTo()) {
            List<AssociatedTable> tables = getAssociatedTableFromDesc(modelBeanDesc
                    .getBelongsTos());
            List<Predicate> predicates = null;
            if (null != example) {
                predicates = new ArrayList<>();
                predicates.add(convertModelFilter(tables, convertExampleToFilter(example, null)));
            }
            deleteAll(tables, null == predicates ? null
                    : predicates.toArray(new Predicate[predicates.size()]));
        } else {
            super.deleteAll(BeanConvertUtils.convertModelToEntity(example, entityClass));
        }
    }

    @Override
    public Model findModelById(ID id) {
        Assert.notNull(id, "view id is required.");
        Model model;
        if (modelBeanDesc.hasBelongsTo()) {
            List<AssociatedTable> tables = getAssociatedTableFromDesc(modelBeanDesc
                    .getBelongsTos());
            Tuple findOne = findOne(tables, Lists.newArrayList(builder.get(ID).eq(id)), null);
            model = convertWithBelongsToTuple(tables, findOne);
        } else {
            Entity entity = super.findById(id);
            model = BeanConvertUtils.convertEntityToModel(entity, modelClass);
        }
//        if (null != model && modelBeanDesc.hasHasMany()) {
//            for (AssociatedDesc associatedDesc : modelBeanDesc.getHasManys()) {
//                handleHasManyQuery(model, associatedDesc);
//            }
//        }
        return model;
    }

    @Override
    public Model findOneByModel(Filter filter) {
        return findOneByModel(filter, null);
    }

    @Override
    public Model findOneByModel(Model example) {
        return findOneByModel(example, null);
    }

    @Override
    public Model findOneByModel(@NotNull Filter filter, Sort sort) {
        Model model;
        if (modelBeanDesc.hasBelongsTo()) {
            List<AssociatedTable> tables = getAssociatedTableFromDesc(modelBeanDesc
                    .getBelongsTos());
            List<Predicate> predicates = null;
            if (null != filter) {
                predicates = new ArrayList<>();
                predicates.add(convertModelFilter(tables, filter));
            }
            OrderSpecifier[] orders = null;
            // 处理排序
            if (null != sort) {
                List<OrderSpecifier> orderList = convertModelSort(tables, sort);
                orders = orderList.toArray(new OrderSpecifier[orderList.size()]);
            }
            Tuple findOne = findOne(
                    tables, predicates, orders);
            model = convertWithBelongsToTuple(tables, findOne);
        } else {
            Entity entity = super.findOne(filter, sort);
            model = BeanConvertUtils.convertEntityToModel(entity, modelClass);
        }
//        if (null != model && modelBeanDesc.hasHasMany()) {
//            for (AssociatedDesc associatedDesc : modelBeanDesc.getHasManys()) {
//                handleHasManyQuery(model, associatedDesc);
//            }
//        }
        return model;
    }

    @Override
    public Model findOneByModel(@NotNull Model example, Sort sort) {
        Model model;
        if (modelBeanDesc.hasBelongsTo()) {
            List<AssociatedTable> tables = getAssociatedTableFromDesc(modelBeanDesc
                    .getBelongsTos());
            List<Predicate> predicates = null;
            if (null != example) {
                predicates = new ArrayList<>();
                predicates.add(convertModelFilter(tables, convertExampleToFilter(example, null)));
            }
            OrderSpecifier[] orders = null;
            // 处理排序
            if (null != sort) {
                List<OrderSpecifier> orderList = convertModelSort(tables, sort);
                orders = orderList.toArray(new OrderSpecifier[orderList.size()]);
            }
            Tuple findOne = findOne(
                    tables, predicates, orders);
            model = convertWithBelongsToTuple(tables, findOne);
        } else {
            Entity entity = super.findOne(BeanConvertUtils.convertModelToEntity(example, entityClass), sort);
            model = BeanConvertUtils.convertEntityToModel(entity, modelClass);
        }
//        if (null != model && modelBeanDesc.hasHasMany()) {
//            for (AssociatedDesc associatedDesc : modelBeanDesc.getHasManys()) {
//                handleHasManyQuery(model, associatedDesc);
//            }
//        }

        return model;
    }

    @Override
    public List<Model> findAllByModel(Filter filter, Sort sort) {
        List<Model> answer = new ArrayList<>();
        if (modelBeanDesc.hasBelongsTo()) {
            List<AssociatedTable> tables = getAssociatedTableFromDesc(modelBeanDesc
                    .getBelongsTos());
            List<Predicate> predicates = null;
            if (null != filter) {
                predicates = new ArrayList<>();
                predicates.add(convertModelFilter(tables, filter));
            }
            OrderSpecifier[] orders = null;
            // 处理排序
            if (null != sort) {
                List<OrderSpecifier> orderList = convertModelSort(tables, sort);
                orders = orderList.toArray(new OrderSpecifier[orderList.size()]);
            }
            List<Tuple> findAll = findAll(tables, predicates, orders);
            for (Tuple tuple : findAll) {
                Model vo = convertWithBelongsToTuple(tables, tuple);
                answer.add(vo);
            }
        } else {
            List<Entity> entityList = super.findAll(filter, sort);
            answer = BeanConvertUtils.convertEntityToModel(entityList, modelClass);
        }
//        for (Model model : answer) {
//            if (null != model && modelBeanDesc.hasHasMany()) {
//                for (AssociatedDesc associatedDesc : modelBeanDesc.getHasManys()) {
//                    handleHasManyQuery(model, associatedDesc);
//                }
//            }
//        }
        return answer;
    }

    @Override
    public List<Model> findAllByModel(Model example, Sort sort) {
        List<Model> answer = new ArrayList<>();
        if (modelBeanDesc.hasBelongsTo()) {
            List<AssociatedTable> tables = getAssociatedTableFromDesc(modelBeanDesc
                    .getBelongsTos());
            List<Predicate> predicates = null;
            if (null != example) {
                predicates = new ArrayList<>();
                predicates.add(convertModelFilter(tables, convertExampleToFilter(example, null)));
            }
            OrderSpecifier[] orders = null;
            // 处理排序
            if (null != sort) {
                List<OrderSpecifier> orderList = convertModelSort(tables, sort);
                orders = orderList.toArray(new OrderSpecifier[orderList.size()]);
            }
            List<Tuple> findAll = findAll(tables, predicates, orders);
            for (Tuple tuple : findAll) {
                Model vo = convertWithBelongsToTuple(tables, tuple);
                answer.add(vo);
            }
        } else {
            List<Entity> entityList = super.findAll(BeanConvertUtils.convertModelToEntity(example, entityClass), sort);
            answer = BeanConvertUtils.convertEntityToModel(entityList, modelClass);
        }
//        for (Model model : answer) {
//            if (null != model && modelBeanDesc.hasHasMany()) {
//                for (AssociatedDesc associatedDesc : modelBeanDesc.getHasManys()) {
//                    handleHasManyQuery(model, associatedDesc);
//                }
//            }
//        }
        return answer;
    }

    @Override
    public Page<Model> findAllByModel(int page, int size, Filter filter, Sort sort) {
        Page<Model> answer;
        if (modelBeanDesc.hasBelongsTo()) {
            List<AssociatedTable> tables = getAssociatedTableFromDesc(modelBeanDesc
                    .getBelongsTos());
            List<Predicate> predicates = null;
            if (null != filter) {
                predicates = new ArrayList();
                predicates.add(convertModelFilter(tables, filter));
            }
            OrderSpecifier[] orders = null;
            // 处理排序
            if (null != sort) {
                List<OrderSpecifier> orderList = convertModelSort(tables, sort);
                orders = orderList.toArray(new OrderSpecifier[orderList.size()]);
            }

            long count = count(tables, null == predicates ? null
                    : predicates.toArray(new Predicate[predicates.size()]));

            List<Tuple> findAll = findAll(tables, predicates,
                    page, size, orders);
            List<Model> list = new ArrayList<>();
            for (Tuple tuple : findAll) {
                Model vo = convertWithBelongsToTuple(tables, tuple);
                if (null != vo && modelBeanDesc.hasHasMany()) {
                    for (AssociatedDesc associatedDesc : modelBeanDesc
                            .getHasManys()) {
                        handleHasManyQuery(vo, associatedDesc);
                    }
                }
                list.add(vo);
            }
            answer = new Page(list, page, size, sort, count);
        } else {
            Page<Entity> entityPage = super.findAll(page, size, filter, sort);
            answer = new Page(BeanConvertUtils.convertEntityToModel(entityPage.getContent(), modelClass), page, size, sort, entityPage.getTotalElements());
        }
//        for (Model vo : answer) {
//            if (null != vo && modelBeanDesc.hasHasMany()) {
//                for (AssociatedDesc associatedDesc : modelBeanDesc.getHasManys()) {
//                    handleHasManyQuery(vo, associatedDesc);
//                }
//            }
//        }
        return answer;
    }

    @Override
    public Page<Model> findAllByModel(int page, int size, Model example, Sort sort) {
        Page<Model> answer;
        if (modelBeanDesc.hasBelongsTo()) {
            List<AssociatedTable> tables = getAssociatedTableFromDesc(modelBeanDesc
                    .getBelongsTos());
            List<Predicate> predicates = null;
            if (null != example) {
                predicates = new ArrayList<>();
                predicates.add(convertModelFilter(tables, convertExampleToFilter(example, null)));
            }
            OrderSpecifier[] orders = null;
            // 处理排序
            if (null != sort) {
                List<OrderSpecifier> orderList = convertModelSort(tables, sort);
                orders = orderList.toArray(new OrderSpecifier[orderList.size()]);
            }

            long count = count(tables, null == predicates ? null
                    : predicates.toArray(new Predicate[predicates.size()]));

            List<Tuple> findAll = findAll(tables, predicates,
                    page, size, orders);
            List<Model> list = new ArrayList<>();
            for (Tuple tuple : findAll) {
                Model vo = convertWithBelongsToTuple(tables, tuple);
                if (null != vo && modelBeanDesc.hasHasMany()) {
                    for (AssociatedDesc associatedDesc : modelBeanDesc
                            .getHasManys()) {
                        handleHasManyQuery(vo, associatedDesc);
                    }
                }
                list.add(vo);
            }
            answer = new Page(list, page, size, sort, count);
        } else {
            Page<Entity> entityPage = super.findAll(page, size, BeanConvertUtils.convertModelToEntity(example, entityClass), sort);
            answer = new Page(BeanConvertUtils.convertEntityToModel(entityPage.getContent(), modelClass), page, size, sort, entityPage.getTotalElements());
        }
//        for (Model vo : answer) {
//            if (null != vo && modelBeanDesc.hasHasMany()) {
//                for (AssociatedDesc associatedDesc : modelBeanDesc.getHasManys()) {
//                    handleHasManyQuery(vo, associatedDesc);
//                }
//            }
//        }
        return answer;
    }

    @Override
    public long countByModel(Filter filter) {
        if (modelBeanDesc.hasBelongsTo()) {
            List<AssociatedTable> tables = getAssociatedTableFromDesc(modelBeanDesc
                    .getBelongsTos());
            List<Predicate> predicates = null;
            if (null != filter) {
                predicates = new ArrayList<>();
                predicates.add(convertModelFilter(tables, filter));
            }
            long count = count(tables, null == predicates ? null
                    : predicates.toArray(new Predicate[predicates.size()]));
            return count;
        }
        return super.count(filter);
    }

    @Override
    public long countByModel(Model example) {
        if (modelBeanDesc.hasBelongsTo()) {
            List<AssociatedTable> tables = getAssociatedTableFromDesc(modelBeanDesc
                    .getBelongsTos());
            List<Predicate> predicates = null;
            if (null != example) {
                predicates = new ArrayList<>();
                predicates.add(convertModelFilter(tables, convertExampleToFilter(example, null)));
            }
            long count = count(tables, null == predicates ? null
                    : predicates.toArray(new Predicate[predicates.size()]));
            return count;
        }
        return super.count(BeanConvertUtils.convertModelToEntity(example, entityClass));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void handleHasManyQuery(Model model, AssociatedDesc associatedDesc) {
        try {
            RelationalPath<?> relationalPath = entityPathResolver
                    .createPath(BeanConvertUtils.getModelBeanDesc(
                            associatedDesc.getAssociatedClass())
                            .getEntityClass());
            SimpleExpression property = (SimpleExpression) entityPathResolver
                    .getProperty(relationalPath,
                            associatedDesc.getAssociatedProperty());
            List findAll = findAll(property.eq(model.getId()));
            List voList = BeanConvertUtils.convertEntityToModel(findAll,
                    associatedDesc.getAssociatedClass());
            FieldUtils.writeDeclaredField(model, associatedDesc.getProperty(),
                    voList, true);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(Messages.getMessage("Jupiter.Persistence.QueryHasMany.Error"));
        }
    }

    private Model convertWithBelongsToTuple(List<AssociatedTable> tables,
                                            Tuple tuple) {
        if (null == tuple) {
            return null;
        }
        Entity entity = tuple.get(root);
        Model model = BeanConvertUtils.convertEntityToModel(entity, modelClass);
        for (AssociatedTable associatedTable : tables) {
            Object associateEntity = tuple.get(associatedTable.getEntityPath());
            if (null != associateEntity) {
                Object associateVo = BeanConvertUtils.convertEntityToModel(
                        associateEntity, associatedTable.getAssociatedDesc()
                                .getAssociatedClass());
                try {
                    FieldUtils.writeDeclaredField(model, associatedTable
                                    .getAssociatedDesc().getProperty(), associateVo,
                            true);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(Messages.getMessage("Jupiter.Persistence.QueryBelongsTo.Error"));
                }
            }
        }
        return model;
    }

    @SuppressWarnings("unchecked")
    List<AssociatedTable> getAssociatedTableFromDesc(
            List<AssociatedDesc> belongsTos) {
        List<AssociatedTable> tables = new ArrayList<AssociatedTable>();
        Set<String> tableNames = new HashSet<>();
        for (AssociatedDesc associatedDesc : belongsTos) {
            RelationalPath<?> relationalPath = entityPathResolver
                    .createPath(BeanConvertUtils.getModelBeanDesc(
                            associatedDesc.getAssociatedClass())
                            .getEntityClass());
            int index = 0;
            String alias = relationalPath.getTableName();
            while (tableNames.contains(alias)) {
                index++;
                alias = relationalPath.getTableName() + "_" + index;
            }
            tableNames.add(alias);
            if (0 < index) {
                relationalPath = entityPathResolver.createPath(BeanConvertUtils
                        .getModelBeanDesc(associatedDesc.getAssociatedClass())
                        .getEntityClass(), alias);
            }
            SimpleExpression<Object> associateProperty = (SimpleExpression<Object>) entityPathResolver
                    .getProperty(relationalPath,
                            associatedDesc.getAssociatedProperty());
            SimpleExpression<Object> rootProperty = (SimpleExpression<Object>) entityPathResolver
                    .getProperty(root, associatedDesc.getRootProperty());
            tables.add(new AssociatedTable(relationalPath, associateProperty
                    .eq(rootProperty), associatedDesc));
        }
        return tables;
    }

    Filter convertExampleToFilter(Object example, String parentProperty) {
        Filter filter = Filter.condition();
        Field[] fields = example.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (!Modifier.isFinal(field.getModifiers())
                    && !Modifier.isStatic(field.getModifiers())) {
                Object value = null;
                try {
                    field.setAccessible(true);
                    value = field.get(example);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    e.printStackTrace();
                }
                if (null != value) {
                    boolean isBelongsToField = false;
                    List<AssociatedDesc> belongsTos = modelBeanDesc
                            .getBelongsTos();
                    for (AssociatedDesc associatedDesc : belongsTos) {
                        if (field.getName()
                                .equals(associatedDesc.getProperty())) {
                            isBelongsToField = true;
                            break;
                        }
                    }

                    if (isBelongsToField) {
                        Filter filter1 = convertExampleToFilter(value, field.getName());
                        if (null != filter1.getCriterion()) {
                            filter.and(filter1);
                        }
                    } else {
                        if (value instanceof Collection || value.getClass().isArray()) {
                            filter.in((null == parentProperty ? ""
                                            : parentProperty + ".") + field.getName(),
                                    value);
                        } else {
                            filter.equal((null == parentProperty ? ""
                                            : parentProperty + ".") + field.getName(),
                                    value);
                        }
                    }

                }
            }

        }
        return filter;
    }


    Predicate convertModelFilter(List<AssociatedTable> associatedTables, Filter filter) {
        Criterion criterion = filter.getCriterion();
        return convertModelCriterion(associatedTables, criterion);
    }

    private Predicate convertModelCriterion(List<AssociatedTable> associatedTables, Criterion criterion) {
        if (criterion instanceof Criteria) {
            Criteria criteria = (Criteria) criterion;
            BooleanBuilder booleanBuilder = new BooleanBuilder(convertModelCriterion(associatedTables, criteria.getLeft()));
            if (criteria.getOpr().equals(AggOpr.AND)) {
                booleanBuilder.and(convertModelCriterion(associatedTables, criteria.getRight()));
            } else if (criteria.getOpr().equals(AggOpr.OR)) {
                booleanBuilder.or(convertModelCriterion(associatedTables, criteria.getRight()));
            }
            if (criterion.isNot()) {
                booleanBuilder.not();
            }
            return booleanBuilder.getValue();
        }

        if (criterion instanceof Term) {
            Term term = (Term) criterion;
            String[] split = term.getProperty().split("\\.");
            if (split.length > 1) {
                return convertModelTerm(associatedTables, split[0], new Term(split[1], term.getOperator(), term.getValue()));
            } else {
                return convertModelTerm(associatedTables, null, term);
            }
        }
        return null;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    Predicate convertModelTerm(List<AssociatedTable> associatedTables, String property, Term term) {
        if (StringUtils.isNotBlank(property)) {
            for (AssociatedTable associatedTable : associatedTables) {
                if (property.equals(
                        associatedTable.getAssociatedDesc().getProperty())) {
                    RelationalPath relationalPath = associatedTable
                            .getEntityPath();
                    PathBuilder pathBuilder = entityPathResolver
                            .getPathBuilder(relationalPath);
                    Specification specification = DynamicSpecifications
                            .bySearchTerm(term);
                    Predicate predicate = specification
                            .toPredicate(relationalPath, pathBuilder,
                                    entityPathResolver);
                    return predicate;
                }
            }
        }
        RelationalPath<Entity> relationalPath = entityPathResolver
                .createPath(entityClass);
        PathBuilder<Entity> pathBuilder = entityPathResolver
                .getPathBuilder(relationalPath);
        Specification<Entity> specification = DynamicSpecifications.bySearchTerm(
                term);
        Predicate predicate = specification.toPredicate(relationalPath,
                pathBuilder, entityPathResolver);
        return predicate;
    }

    List<OrderSpecifier> convertModelSort(List<AssociatedTable> associatedTables, Sort sort) {
        Map<String, Sort> map = new HashMap<>();
        for (Sort.Order order : sort) {
            String[] split = order.getProperty().split("\\.");
            if (split.length > 1) {
                Sort sort2 = map.get(split[0]);
                if (null == sort2) {
                    sort2 = Sort.condition();
                }
                sort2.add(new Sort.Order(order.getDirection(), split[1]));
                map.put(split[0], sort2);
            } else {
                Sort sort2 = map.get(null);
                if (null == sort2) {
                    sort2 = Sort.condition();
                }
                sort2.add(order);
                map.put(null, sort2);
            }
        }

        List<OrderSpecifier> answer = new ArrayList<>();
        for (Map.Entry<String, Sort> entry : map.entrySet()) {
            if (null == entry.getKey()) {
                Sort sort1 = entry.getValue();
                for (Sort.Order order : sort1) {
                    answer.add(toOrder(root, builder, order));
                }
            } else {
                for (AssociatedTable associatedTable : associatedTables) {
                    if (entry.getKey().equals(
                            associatedTable.getAssociatedDesc().getProperty())) {
                        RelationalPath relationalPath = associatedTable
                                .getEntityPath();
                        PathBuilder pathBuilder = entityPathResolver
                                .getPathBuilder(relationalPath);
                        Sort sort1 = entry.getValue();
                        for (Sort.Order order : sort1) {
                            answer.add(toOrder(relationalPath, pathBuilder, order));
                        }
                        break;
                    }
                }

            }
        }
        return answer;
    }


}
