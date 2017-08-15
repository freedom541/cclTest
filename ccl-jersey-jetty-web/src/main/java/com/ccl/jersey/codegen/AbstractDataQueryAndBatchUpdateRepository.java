package com.ccl.jersey.codegen;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.sql.RelationalPath;
import org.apache.commons.lang3.reflect.FieldUtils;

import javax.sql.DataSource;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by ccl on 2015/9/17.
 */
public abstract class AbstractDataQueryAndBatchUpdateRepository<Entity extends IdEntity<ID>, ID extends Serializable> extends AbstractDataUpdateRepository<Entity, ID>
        implements DataQueryAndBatchUpdateRepository<Entity, ID> {

    public AbstractDataQueryAndBatchUpdateRepository(DataSource dataSource) {
        super(dataSource);
    }

    public AbstractDataQueryAndBatchUpdateRepository(QueryDslConfig queryDslConfig) {
        super(queryDslConfig);
    }

    @Override
    public void updateAll(Entity entity, Filter filter) {
        checkHasUniqueProperty(entity);
        Predicate predicate = convertFilter(filter);
        updateAll(entity, predicate);
    }

    private void checkHasUniqueProperty(Entity entity) {
        List<Unique> uniqueList = getUniques();
        if (!uniqueList.isEmpty()) {
            try {
                for (Unique uni : uniqueList) {
                    boolean hasUnique = true;
                    String[] propertys = uni.value();
                    List<String> uniquePropertys = new ArrayList<>();
                    for (String property : propertys) {
                        if (null == FieldUtils.readDeclaredField(entity, property,
                                true)) {
                            hasUnique = false;
                        } else {
                            uniquePropertys.add(property);
                        }
                    }
                    if (hasUnique) {
                        throw new RuntimeException(Messages.getMessage("Jupiter.Persistence.Unique.NotSupported"));
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                throw new RuntimeException(Messages.getMessage("Jupiter.Persistence.ReadProperty.Error"));
            }
        }
    }

    @Override
    public void updateAll(Entity entity, Entity example) {
        checkHasUniqueProperty(entity);
        Predicate predicate = convertExample(example);
        updateAll(entity, predicate);
    }

    @Override
    public void deleteAll(Filter filter) {
        Predicate predicate = null;
        if (null != filter) {
            predicate = convertFilter(filter);
        }
        deleteAll(predicate);
    }

    @Override
    public void deleteAll(Entity example) {
        Predicate predicate = null;
        if (null != example) {
            predicate = convertExample(example);
        }
        deleteAll(predicate);
    }

    @Override
    public Entity findById(ID id) {
        return findOne(builder.get(ID).eq(id));
    }

    @Override
    public List<Entity> findByIds(Collection<ID> ids) {
        if (null != ids) {
            BooleanBuilder booleanBuilder = new BooleanBuilder();
            for (ID id : ids) {
                booleanBuilder.or(builder.get(ID).eq(id));
            }
            return findAll(booleanBuilder.getValue(), null);
        }

        return Collections.emptyList();
    }

    @Override
    public Entity findOne(Filter filter) {
        return findOne(filter, null);
    }

    @Override
    public Entity findOne(Entity example) {
        return findOne(example, null);
    }

    @Override
    public Entity findOne(@NotNull Filter filter, Sort sort) {
        Predicate predicate = null;
        if (null != filter) {
            predicate = convertFilter(filter);
        }
        OrderSpecifier<?>[] orders = null;
        // 处理排序
        if (null != sort) {
            orders = new OrderSpecifier<?>[sort.size()];
            for (int i = 0; i < sort.size(); i++) {
                orders[i] = toOrder(root, builder, sort.get(i));
            }
        }
        return findOne(predicate, orders);
    }

    @Override
    public Entity findOne(@NotNull Entity example, Sort sort) {
        Predicate predicate = null;
        if (null != example) {
            predicate = convertExample(example);
        }
        OrderSpecifier<?>[] orders = null;
        // 处理排序
        if (null != sort) {
            orders = new OrderSpecifier<?>[sort.size()];
            for (int i = 0; i < sort.size(); i++) {
                orders[i] = toOrder(root, builder, sort.get(i));
            }
        }
        return findOne(predicate, orders);
    }

    @Override
    public List<Entity> findAll(Filter filter, Sort sort) {
        Predicate predicate = null;
        if (null != filter) {
            predicate = convertFilter(filter);
        }
        OrderSpecifier<?>[] orders = null;
        // 处理排序
        if (null != sort) {
            orders = new OrderSpecifier<?>[sort.size()];
            for (int i = 0; i < sort.size(); i++) {
                orders[i] = toOrder(root, builder, sort.get(i));
            }
        }

        return findAll(predicate, orders);
    }

    @Override
    public List<Entity> findAll(Entity example, Sort sort) {
        Predicate predicate = null;
        if (null != example) {
            predicate = convertExample(example);
        }
        OrderSpecifier<?>[] orders = null;
        // 处理排序
        if (null != sort) {
            orders = new OrderSpecifier<?>[sort.size()];
            for (int i = 0; i < sort.size(); i++) {
                orders[i] = toOrder(root, builder, sort.get(i));
            }
        }

        return findAll(predicate, orders);
    }

    @Override
    public Page<Entity> findAll(int page, int size, Filter filter, Sort sort) {

        Predicate predicate = null;
        if (null != filter) {
            predicate = convertFilter(filter);
        }
        long count = count(predicate);
        OrderSpecifier<?>[] orders = null;
        // 处理排序
        if (null != sort) {
            orders = new OrderSpecifier<?>[sort.size()];
            for (int i = 0; i < sort.size(); i++) {
                orders[i] = toOrder(root, builder, sort.get(i));
            }
        }

        List<Entity> list = findAll(predicate, page,
                size, orders);
        return new Page<>(list, page, size, sort, count);
    }


    @Override
    public Page<Entity> findAll(int page, int size, Entity example, Sort sort) {

        Predicate predicate = null;
        if (null != example) {
            predicate = convertExample(example);
        }
        long count = count(predicate);

        OrderSpecifier<?>[] orders = null;
        // 处理排序
        if (null != sort) {
            orders = new OrderSpecifier<?>[sort.size()];
            for (int i = 0; i < sort.size(); i++) {
                orders[i] = toOrder(root, builder, sort.get(i));
            }
        }

        List<Entity> list = findAll(predicate, page,
                size, orders);
        return new Page<>(list, page, size, sort, count);
    }

    @Override
    public long count(Filter filter) {
        // 处理查询条件
        Predicate predicate = null;
        if (null != filter) {
            predicate = convertFilter(filter);
        }
        return count(predicate);
    }

    @Override
    public long count(Entity example) {
        // 处理查询条件
        Predicate predicate = null;
        if (null != example) {
            predicate = convertExample(example);
        }
        return count(predicate);
    }

    Predicate convertExample(Entity entity) {
        RelationalPath<Entity> relationalPath = entityPathResolver
                .createPath(entityClass);
        PathBuilder<Entity> pathBuilder = entityPathResolver
                .getPathBuilder(relationalPath);
        Specification<Entity> specification = DynamicSpecifications.byExample(
                entity);
        Predicate predicate = specification.toPredicate(relationalPath,
                pathBuilder, entityPathResolver);
        return predicate;
    }

    Predicate convertFilter(Filter filter) {
        RelationalPath<Entity> relationalPath = entityPathResolver
                .createPath(entityClass);
        PathBuilder<Entity> pathBuilder = entityPathResolver
                .getPathBuilder(relationalPath);
        Specification<Entity> specification = DynamicSpecifications.bySearchFilter(
                filter);
        Predicate predicate = specification.toPredicate(relationalPath,
                pathBuilder, entityPathResolver);
        return predicate;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    <T> OrderSpecifier toOrder(RelationalPath<T> root, PathBuilder<T> builder,
                               Sort.Order order) {

        Path<T> property = entityPathResolver.getProperty(root,
                order.getProperty());
        if (null == property) {
            property = (Path<T>) builder.get(order.getProperty());
        }

        return new OrderSpecifier(
                Sort.Direction.ASC.equals(order.getDirection()) ? com.querydsl.core.types.Order.ASC
                        : com.querydsl.core.types.Order.DESC, property);
    }

}
