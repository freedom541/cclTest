package com.ccl.jersey.codegen;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.dml.SQLDeleteClause;
import com.querydsl.sql.dml.SQLUpdateClause;
import org.joda.time.DateTime;

import javax.sql.DataSource;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractMultiTableQueryDslRepository<Entity extends IdEntity<ID>, ID extends Serializable>
        extends AbstractQueryDslRepository<Entity, ID> implements
        MutiTableQueryDslRepository<Entity, ID> {

    public AbstractMultiTableQueryDslRepository(
            DataSource dataSource) {
        super(dataSource);
    }

    public AbstractMultiTableQueryDslRepository(QueryDslConfig queryDslConfig) {
        super(queryDslConfig);
    }

    @Override
    public Tuple findOne(List<AssociatedTable> tables, List<Predicate> predicates, OrderSpecifier<?>... orders) {
        return query(new Query() {
            @Override
            public Tuple executeQuery(SQLQueryFactory sqlQuery) {
                List<RelationalPath> resultPath = new ArrayList<>();
                resultPath.add(root);
                if (null != tables && !tables.isEmpty()) {
                    for (AssociatedTable associatedTable : tables) {
                        resultPath.add(associatedTable.getEntityPath());
                    }
                }
                SQLQuery<Tuple> query = sqlQueryFactory.select(resultPath
                        .toArray(new RelationalPath[resultPath.size()])).from(root);
                if (null != tables && !tables.isEmpty()) {
                    for (AssociatedTable associatedTable : tables) {
                        query.leftJoin(associatedTable.getEntityPath()).on(
                                associatedTable.getOn());
                    }
                }
                if (null != predicates) {
                    query.where(predicates.toArray(new Predicate[predicates
                            .size()]));
                }
                if (null != orders && orders.length > 0) {
                    query.orderBy(orders);
                }
                Tuple tuple = query.fetchFirst();
                return tuple;
            }
        });
    }

    @Override
    public long count(List<AssociatedTable> tables, Predicate... predicates) {
        return query(new Query() {
            @Override
            public Long executeQuery(SQLQueryFactory sqlQuery) {
                SQLQuery query = sqlQueryFactory.select(builder.get(ID).count()).from(root);
                if (null != tables && !tables.isEmpty()) {
                    for (AssociatedTable associatedTable : tables) {
                        query.leftJoin(associatedTable.getEntityPath()).on(
                                associatedTable.getOn());
                    }
                }
                if (null != predicates && 0 < predicates.length) {
                    query.where(predicates);
                }
                return query.fetchCount();
            }
        });
    }

    @Override
    public List<Tuple> findAll(List<AssociatedTable> tables,
                               List<Predicate> predicates, OrderSpecifier<?>... orders) {
        return query(new Query() {
            @Override
            public List<Tuple> executeQuery(SQLQueryFactory sqlQuery) {
                List<RelationalPath<?>> resultPath = new ArrayList<>();
                resultPath.add(root);
                if (null != tables && !tables.isEmpty()) {
                    for (AssociatedTable associatedTable : tables) {
                        resultPath.add(associatedTable.getEntityPath());
                    }
                }
                SQLQuery<Tuple> query = sqlQueryFactory.select(resultPath
                        .toArray(new RelationalPath<?>[resultPath.size()])).from(root);
                if (null != tables && !tables.isEmpty()) {
                    for (AssociatedTable associatedTable : tables) {
                        query.leftJoin(associatedTable.getEntityPath()).on(
                                associatedTable.getOn());
                    }
                }
                if (null != predicates) {
                    query.where(predicates.toArray(new Predicate[predicates
                            .size()]));
                }
                if (null != orders && orders.length > 0) {
                    query.orderBy(orders);
                }
                List<Tuple> list = query.fetch();
                return list;
            }
        });
    }

    @Override
    public List<Tuple> findAll(List<AssociatedTable> tables,
                               List<Predicate> predicates, int page, int size,
                               OrderSpecifier<?>... orders) {
        final Page p = new Page(page, size);
        return query(new Query() {
            @Override
            public List<Tuple> executeQuery(SQLQueryFactory sqlQuery) {
                List<RelationalPath<?>> resultPath = new ArrayList<>();
                resultPath.add(root);
                if (null != tables && !tables.isEmpty()) {
                    for (AssociatedTable associatedTable : tables) {
                        resultPath.add(associatedTable.getEntityPath());
                    }
                }
                SQLQuery<Tuple> query = sqlQueryFactory.select(resultPath.toArray(new RelationalPath<?>[resultPath
                        .size()])).from(root);
                if (null != tables && !tables.isEmpty()) {
                    for (AssociatedTable associatedTable : tables) {
                        query.leftJoin(associatedTable.getEntityPath()).on(
                                associatedTable.getOn());
                    }
                }
                if (null != predicates) {
                    query.where(predicates.toArray(new Predicate[predicates
                            .size()]));
                }
                if (null != orders && orders.length > 0) {
                    query.orderBy(orders);
                }
                List<Tuple> list = query
                        .offset((p.getPage() - 1) * p.getSize())
                        .limit(p.getSize())
                        .fetch();
                return list;
            }
        });
    }

    @Override
    public long updateAll(Entity entity, List<AssociatedTable> tables,
                          Predicate... predicates) {
        return query(new Query() {
            @Override
            public Long executeQuery(SQLQueryFactory sqlQuery) {
                if (entity instanceof StatisticsEntity) {
                    StatisticsEntity statisticsEntity = (StatisticsEntity) entity;
                    statisticsEntity.setUpdateTime(new DateTime());
                }
                SQLUpdateClause update = sqlQueryFactory.update(root).populate(
                        entity);
                SQLQuery query = sqlQueryFactory.select(builder.get(ID)).from(root);
                if (null != tables && !tables.isEmpty()) {
                    for (AssociatedTable associatedTable : tables) {
                        query.leftJoin(associatedTable.getEntityPath()).on(
                                associatedTable.getOn());
                    }
                }
                if (null != predicates && 0 < predicates.length) {
                    query.where(predicates);
                }
                update.where(builder.get(ID).in(query.fetch()));
                return update.execute();
            }
        });
    }

    @Override
    public long deleteAll(List<AssociatedTable> tables, Predicate... predicates) {
        return query(new Query() {
            @Override
            public Long executeQuery(SQLQueryFactory sqlQuery) {
                SQLDeleteClause delete = sqlQueryFactory.delete(root);
                SQLQuery query = sqlQueryFactory.select(builder.get(ID)).from(root);
                if (null != tables && !tables.isEmpty()) {
                    for (AssociatedTable associatedTable : tables) {
                        query.leftJoin(associatedTable.getEntityPath()).on(
                                associatedTable.getOn());
                    }
                }
                if (null != predicates && 0 < predicates.length) {
                    query.where(predicates);
                }
                return delete
                        .where(builder.get(ID).in(query.fetch()))
                        .execute();
            }
        });
    }
}
