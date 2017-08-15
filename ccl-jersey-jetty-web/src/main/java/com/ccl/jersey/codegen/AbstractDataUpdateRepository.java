package com.ccl.jersey.codegen;

import com.querydsl.core.BooleanBuilder;

import javax.sql.DataSource;
import java.io.Serializable;
import java.util.Collection;

public abstract class AbstractDataUpdateRepository<Entity extends IdEntity<ID>, ID extends Serializable>
        extends AbstractCachedQueryDslRepository<Entity, ID> implements
        DataUpdateRepository<Entity, ID> {

    public AbstractDataUpdateRepository(DataSource dataSource) {
        super(dataSource);
    }

    public AbstractDataUpdateRepository(QueryDslConfig queryDslConfig) {
        super(queryDslConfig);
    }

    @Override
    public void update(Entity entity) {
        update(entity, true);
    }

    @Override
    public void updateWithNotNull(Entity entity) {
        update(entity, false);
    }

    @Override
    public void update(Collection<Entity> entities) {
        update(entities, true);
    }

    @Override
    public void updateWithNotNull(Collection<Entity> entities) {
        update(entities, false);
    }

    @Override
    public void deleteById(ID id) {
        deleteAll(builder.get(ID).eq(id));
    }

    @Override
    public void deleteByIds(Collection<ID> ids) {
        if (null != ids && !ids.isEmpty()) {
            BooleanBuilder booleanBuilder = new BooleanBuilder();
            for (ID id : ids) {
                booleanBuilder.or(builder.get(ID).eq(id));
            }
            deleteAll(booleanBuilder.getValue());
        }

    }

}
