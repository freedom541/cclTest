package com.ccl.jersey.codegen;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.dml.DefaultMapper;
import com.querydsl.sql.dml.SQLDeleteClause;
import com.querydsl.sql.dml.SQLInsertClause;
import com.querydsl.sql.dml.SQLUpdateClause;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.joda.time.DateTime;
import org.slf4j.MarkerFactory;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Transactional(rollbackFor = RuntimeException.class)
public abstract class AbstractQueryDslRepository<Entity extends IdEntity<ID>, ID extends Serializable>
        extends AbstractInitialRepository<Entity, ID> implements
        QueryDslRepository<Entity, ID> {

    protected final EntityPathResolver entityPathResolver = SimpleEntityPathResolver.INSTANCE;
    protected final RelationalPath<Entity> root;
    protected final PathBuilder<Entity> builder;
    protected final SQLQueryFactory sqlQueryFactory;

    public AbstractQueryDslRepository(
            DataSource dataSource) {
        this(new QueryDslConfig(dataSource));
    }

    public AbstractQueryDslRepository(QueryDslConfig queryDslConfig) {
        this.root = entityPathResolver.createPath(entityClass);
        this.builder = entityPathResolver.getPathBuilder(root);
        sqlQueryFactory = queryDslConfig.getSqlQueryFactory();
    }

    @Override
    public void create(Entity entity) {
        uniqueValidator(entity);

        generatorStringPrimaryKey(entity);

        markCreateTimestamp(entity);

        entity.setDefaultValue();

        query(new QueryDslRepository.Query() {
            @Override
            public Object executeQuery(SQLQueryFactory sqlQuery) {
                ID executeWithKey = sqlQueryFactory.insert(root).populate(entity)
                        .executeWithKey(primaryKeyClass);
                if (null != executeWithKey) {
                    entity.setId(executeWithKey);
                }
                return null;
            }
        });
    }

    private void markCreateTimestamp(Entity entity) {
        if (entity instanceof StatisticsEntity) {
            StatisticsEntity statisticsEntity = (StatisticsEntity) entity;
            DateTime createTime = new DateTime();
            statisticsEntity.setCreateTime(createTime);
            statisticsEntity.setUpdateTime(createTime);
        }
    }

    private void generatorStringPrimaryKey(Entity entity) {
        if (String.class.equals(primaryKeyClass)) {
            String id = (String) entity.getId();
            if (StringUtils.isBlank(id)) {
                entity.setId((ID) ObjectId.get());
            }
        }
    }

    @Override
    public void create(Collection<Entity> entities) {
        if (null != entities && !entities.isEmpty()) {
            query(new QueryDslRepository.Query() {
                @Override
                public Object executeQuery(SQLQueryFactory sqlQuery) {
                    SQLInsertClause insertClause = sqlQueryFactory.insert(root);
                    for (Entity entity : entities) {
                        uniqueValidator(entity);
                        generatorStringPrimaryKey(entity);
                        markCreateTimestamp(entity);
                        entity.setDefaultValue();
                        insertClause.populate(entity).addBatch();
                    }
                    insertClause.execute();
                    return null;
                }
            });

        }
    }

    @Override
    public void update(Entity entity, boolean withNullBindings) {
        uniqueValidator(entity);

        optimisticLockValidator(entity);

        markUpdateTimestamp(entity);

        if (withNullBindings) {
            entity.setDefaultValue();
        }

        query(new Query() {
            @Override
            public Object executeQuery(SQLQueryFactory sqlQuery) {
                sqlQueryFactory.update(root)
                        .populate(
                                entity,
                                withNullBindings ? DefaultMapper.WITH_NULL_BINDINGS
                                        : DefaultMapper.DEFAULT)
                        .where(builder.get(ID).eq(entity.getId())).execute();
                return null;
            }
        });

    }

    private void optimisticLockValidator(Entity entity) {
        if (entity instanceof VersionOfEntity) {
            Entity one = findOne(builder.get(ID).eq(entity.getId()));
            VersionOfEntity olde = (VersionOfEntity) one;
            VersionOfEntity newe = (VersionOfEntity) entity;
            if (null != olde.getVersion() && null != newe.getVersion() && olde.getVersion().longValue() != newe.getVersion().longValue()) {
                throw new RuntimeException("Jupiter.Persistence.OptimisticLock.Error");
            }

            newe.setVersion((null == newe.getVersion() ? 0 : newe.getVersion()) + 1);
        }
    }

    private void markUpdateTimestamp(Entity entity) {
        if (entity instanceof StatisticsEntity) {
            StatisticsEntity statisticsEntity = (StatisticsEntity) entity;
            statisticsEntity.setUpdateTime(new DateTime());
        }
    }

    @Override
    public void update(Collection<Entity> entities, boolean withNullBindings) {
        if (null != entities && !entities.isEmpty()) {
            query(new Query() {
                @Override
                public Object executeQuery(SQLQueryFactory sqlQuery) {
                    SQLUpdateClause updateClause = sqlQueryFactory.update(root);
                    for (Entity entity : entities) {
                        uniqueValidator(entity);
                        markUpdateTimestamp(entity);
                        if (withNullBindings) {
                            entity.setDefaultValue();
                        }
                        updateClause
                                .populate(
                                        entity,
                                        withNullBindings ? DefaultMapper.WITH_NULL_BINDINGS
                                                : DefaultMapper.DEFAULT)
                                .where(builder.get(ID).eq(entity.getId())).addBatch();
                    }
                    updateClause.execute();
                    return null;
                }
            });

        }

    }

    @Override
    public Entity findOne(Predicate predicate, OrderSpecifier<?>... orders) {
        return query(new Query() {
            @Override
            public Entity executeQuery(SQLQueryFactory sqlQuery) {
                SQLQuery<Entity> query = sqlQueryFactory.select(root).from(root);
                if (null != predicate) {
                    query.where(predicate);
                }
                if (null != orders && orders.length > 0) {
                    query.orderBy(orders);
                }
                return query.fetchFirst();
            }
        });
    }

    @Override
    public List<Entity> findAll(Predicate predicate, OrderSpecifier<?>... orders) {
        return query(new Query() {
            @Override
            public List<Entity> executeQuery(SQLQueryFactory sqlQuery) {
                SQLQuery<Entity> query = sqlQueryFactory.select(root).from(root);
                if (null != predicate) {
                    query.where(predicate);
                }
                if (null != orders && orders.length > 0) {
                    query.orderBy(orders);
                }
                return query.fetch();
            }
        });
    }

    @Override
    public List<Entity> findAll(Predicate predicate, int page, int size,
                                OrderSpecifier<?>... orders) {
        final Page p = new Page(page, size);
        return query(new Query() {
            @Override
            public List<Entity> executeQuery(SQLQueryFactory sqlQuery) {
                SQLQuery<Entity> query = sqlQueryFactory.select(root).from(root);
                if (null != predicate) {
                    query.where(predicate);
                }
                if (null != orders && orders.length > 0) {
                    query.orderBy(orders);
                }
                return query.offset((p.getPage() - 1) * p.getSize())
                        .limit(p.getSize()).fetch();
            }
        });
    }

    @Override
    public long count(Predicate predicate) {
        return query(new Query() {
            @Override
            public Long executeQuery(SQLQueryFactory sqlQuery) {
                SQLQuery query = sqlQueryFactory.select(builder.get(ID).count()).from(root);
                if (null != predicate) {
                    query.where(predicate);
                }
                return query.fetchCount();
            }
        });
    }

    @Override
    public long updateAll(Entity entity, Predicate predicate) {
        markUpdateTimestamp(entity);
        return query(new Query() {
            @Override
            public Long executeQuery(SQLQueryFactory sqlQuery) {
                SQLUpdateClause update = sqlQueryFactory.update(root).populate(
                        entity);
                update.where(predicate);
                return update.execute();
            }
        });
    }

    @Override
    public long deleteAll(Predicate predicate) {
        return query(new Query() {
            @Override
            public Long executeQuery(SQLQueryFactory sqlQuery) {
                SQLDeleteClause delete = sqlQueryFactory.delete(root);
                if (null != predicate) {
                    delete.where(predicate);
                }
                return delete.execute();
            }
        });
    }

    @Override
    public <T> T query(Query query) {
        try {
            T t = query.executeQuery(sqlQueryFactory);
            return t;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(MarkerFactory.getMarker(LogMarker.DATABASE), Messages.getMessage("Jupiter.Persistence.Query.Error", e.getMessage()), e);
            throw new RuntimeException(Messages.getMessage("Jupiter.Persistence.Query.Error", e.getMessage()));
        }
    }

    void uniqueValidator(Entity entity) {
        List<Unique> uniqueList = getUniques();
        if (!uniqueList.isEmpty()) {
            for (Unique uni : uniqueList) {
                try {
                    String[] propertys = uni.value();
                    if (null != propertys) {
                        boolean isEmpty = true;
                        for (String property : propertys) {
                            if (null != FieldUtils.readDeclaredField(entity,
                                    property, true)) {
                                isEmpty = false;
                            }
                        }
                        if (isEmpty) {
                            continue;
                        }
                    }
                    BooleanBuilder booleanBuilder = new BooleanBuilder();
                    if (null != entity.getId()) {
                        booleanBuilder.and(builder.get(ID).ne(entity.getId()));
                    }
                    if (null != propertys) {
                        for (String property : propertys) {
                            booleanBuilder.and(((SimpleExpression) entityPathResolver.getProperty(root, property)).eq(FieldUtils
                                    .readDeclaredField(entity, property, true)));
                        }
                    }
                    long count = count(booleanBuilder.getValue());
                    if (0 < count) {
                        logger.error(MarkerFactory.getMarker(LogMarker.DATABASE), Messages.getMessage("Jupiter.Persistence.Unique.Error",
                                entityClass.getSimpleName(), StringUtils.join(propertys, ",")));
                        throw new RuntimeException(Messages.getMessage("Jupiter.Persistence.Unique.Error"));
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    logger.error(MarkerFactory.getMarker(LogMarker.DATABASE), Messages.getMessage("Jupiter.Persistence.ReadProperty.Error"), e);
                    throw new RuntimeException(Messages.getMessage("Jupiter.Persistence.ReadProperty.Error"));
                }
            }
        }
    }

    List<Unique> getUniques() {
        List<Unique> uniqueList = new ArrayList<Unique>();
        Uniques uniques = entityClass.getAnnotation(Uniques.class);
        if (null != uniques) {
            uniqueList.addAll(Arrays.asList(uniques.values()));
        }
        Unique unique = entityClass.getAnnotation(Unique.class);
        if (null != unique) {
            uniqueList.add(unique);
        }
        return uniqueList;
    }

}
