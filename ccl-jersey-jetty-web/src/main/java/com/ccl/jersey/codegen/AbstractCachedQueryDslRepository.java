package com.ccl.jersey.codegen;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.sql.RelationalPath;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author ccl
 * @date 2015/9/30.
 */
public abstract class AbstractCachedQueryDslRepository<Entity extends IdEntity<ID>, ID extends Serializable>
        extends AbstractMultiTableQueryDslRepository<Entity, ID> {

    @Autowired(required = false)
    protected SetCacheService cacheService;

    protected boolean hasCache;

    private static final int EXPIRE_SECONDS = 3600;

    private static final String CACHE_KEY_SEPARATOR = "_";

    public AbstractCachedQueryDslRepository(DataSource dataSource) {
        super(dataSource);
    }

    public AbstractCachedQueryDslRepository(QueryDslConfig queryDslConfig) {
        super(queryDslConfig);
    }

    @Override
    public Tuple findOne(List<AssociatedTable> tables, List<Predicate> predicates, OrderSpecifier<?>... orders) {
        String md5Hex = null;
        if (hasCache && null != cacheService) {
            StringBuilder queryString = new StringBuilder();
            queryString.append("findOne").append(CACHE_KEY_SEPARATOR);
            if (null != tables) {
                for (AssociatedTable associatedTable : tables) {
                    queryString.append(getModule(associatedTable.getEntityPath())).append(CACHE_KEY_SEPARATOR);
                }
            }
            if (null != predicates) {
                for (Predicate predicate : predicates) {
                    queryString.append(predicate).append(CACHE_KEY_SEPARATOR);
                }
            }

            md5Hex = DigestUtils.md5Hex(queryString.toString());
            Object value = cacheService.get(md5Hex);
            if (null != value) {
                return (Tuple) value;
            }
        }

        Tuple one = super.findOne(tables, predicates, orders);

        if (hasCache && null != cacheService && null != one) {
            cacheService.set(md5Hex, one, EXPIRE_SECONDS);
            for (AssociatedTable associatedTable : tables) {
                putToClearDatas(getModule(associatedTable.getEntityPath()), md5Hex);
            }
        }
        return one;
    }

    @Override
    public long count(List<AssociatedTable> tables, Predicate... predicates) {
        return super.count(tables, predicates);
    }

    @Override
    public List<Tuple> findAll(List<AssociatedTable> tables, List<Predicate> predicates, OrderSpecifier<?>... orders) {
        String md5Hex = null;
        if (hasCache && null != cacheService) {
            StringBuilder queryString = new StringBuilder();
            queryString.append("findAll").append(CACHE_KEY_SEPARATOR);
            if (null != tables) {
                for (AssociatedTable associatedTable : tables) {
                    queryString.append(getModule(associatedTable.getEntityPath())).append(CACHE_KEY_SEPARATOR);
                }
            }
            if (null != predicates) {
                for (Predicate predicate : predicates) {
                    queryString.append(predicate).append(CACHE_KEY_SEPARATOR);
                }
            }
            if (null != orders) {
                for (OrderSpecifier orderSpecifier : orders) {
                    queryString.append(orderSpecifier).append(CACHE_KEY_SEPARATOR);
                }
            }
            md5Hex = DigestUtils.md5Hex(queryString.toString());
            Object value = cacheService.get(md5Hex);
            if (null != value) {
                return (List<Tuple>) value;
            }
        }

        List<Tuple> all = super.findAll(tables, predicates, orders);

        if (hasCache && null != cacheService) {
            cacheService.set(md5Hex, all, EXPIRE_SECONDS);
            for (AssociatedTable associatedTable : tables) {
                putToClearDatas(getModule(associatedTable.getEntityPath()), md5Hex);
            }
        }
        return all;
    }

    @Override
    public List<Tuple> findAll(List<AssociatedTable> tables, List<Predicate> predicates, int page, int size, OrderSpecifier<?>... orders) {
        String md5Hex = null;
        if (hasCache && null != cacheService) {
            StringBuilder queryString = new StringBuilder();
            queryString.append("findAll").append(CACHE_KEY_SEPARATOR);
            if (null != tables) {
                for (AssociatedTable associatedTable : tables) {
                    queryString.append(getModule(associatedTable.getEntityPath())).append(CACHE_KEY_SEPARATOR);
                }
            }
            if (null != predicates) {
                for (Predicate predicate : predicates) {
                    queryString.append(predicate).append(CACHE_KEY_SEPARATOR);
                }
            }
            if (null != orders) {
                for (OrderSpecifier orderSpecifier : orders) {
                    queryString.append(orderSpecifier).append(CACHE_KEY_SEPARATOR);
                }
            }
            queryString.append(page).append(CACHE_KEY_SEPARATOR).append(size).append(CACHE_KEY_SEPARATOR);
            md5Hex = DigestUtils.md5Hex(queryString.toString());
            Object value = cacheService.get(md5Hex);
            if (null != value) {
                return (List<Tuple>) value;
            }
        }

        List<Tuple> all = super.findAll(tables, predicates, page, size, orders);

        if (hasCache && null != cacheService) {
            cacheService.set(md5Hex, all, EXPIRE_SECONDS);
            for (AssociatedTable associatedTable : tables) {
                putToClearDatas(getModule(associatedTable.getEntityPath()), md5Hex);
            }
        }
        return all;
    }

    @Override
    public long updateAll(Entity entity, List<AssociatedTable> tables, Predicate... predicates) {
        long l = super.updateAll(entity, tables, predicates);
        if (hasCache && null != cacheService) {
            for (AssociatedTable associatedTable : tables) {
                clearCachedDatas(getModule(associatedTable.getEntityPath()));
            }
        }
        return l;
    }


    @Override
    public long deleteAll(List<AssociatedTable> tables, Predicate... predicates) {
        long l = super.deleteAll(tables, predicates);
        if (hasCache && null != cacheService) {
            for (AssociatedTable associatedTable : tables) {
                clearCachedDatas(getModule(associatedTable.getEntityPath()));
            }
        }
        return l;
    }

    @Override
    public void create(Entity entity) {
        super.create(entity);
        if (hasCache && null != cacheService) {
            clearCachedDatas(getModule(root));
        }
    }

    @Override
    public void create(Collection<Entity> entities) {
        super.create(entities);
        if (hasCache && null != cacheService) {
            clearCachedDatas(getModule(root));
        }
    }

    @Override
    public void update(Entity entity, boolean withNullBindings) {
        super.update(entity, withNullBindings);
        if (hasCache && null != cacheService) {
            clearCachedDatas(getModule(root));
        }
    }

    @Override
    public void update(Collection<Entity> entities, boolean withNullBindings) {
        super.update(entities, withNullBindings);
        if (hasCache && null != cacheService) {
            clearCachedDatas(getModule(root));
        }
    }

    @Override
    public Entity findOne(Predicate predicate, OrderSpecifier<?>... orders) {
        String md5Hex = null;
        if (hasCache && null != cacheService) {
            StringBuilder queryString = new StringBuilder();
            queryString.append("findOne").append(CACHE_KEY_SEPARATOR);
            queryString.append(getModule(root)).append(CACHE_KEY_SEPARATOR);
            queryString.append(predicate).append(CACHE_KEY_SEPARATOR);
            md5Hex = DigestUtils.md5Hex(queryString.toString());
            Object value = cacheService.get(md5Hex);
            if (null != value) {
                return (Entity) value;
            }
        }

        Entity one = super.findOne(predicate, orders);

        if (hasCache && null != cacheService && null != one) {
            cacheService.set(md5Hex, one, EXPIRE_SECONDS);
            putToClearDatas(getModule(root), md5Hex);
        }
        return one;
    }

    @Override
    public List<Entity> findAll(Predicate predicate, OrderSpecifier<?>... orders) {
        String md5Hex = null;
        if (hasCache && null != cacheService) {
            StringBuilder queryString = new StringBuilder();
            queryString.append("findAll").append(CACHE_KEY_SEPARATOR);
            queryString.append(getModule(root)).append(CACHE_KEY_SEPARATOR);
            queryString.append(predicate).append(CACHE_KEY_SEPARATOR);
            if (null != orders) {
                for (OrderSpecifier orderSpecifier : orders) {
                    queryString.append(orderSpecifier).append(CACHE_KEY_SEPARATOR);
                }
            }
            md5Hex = DigestUtils.md5Hex(queryString.toString());
            Object value = cacheService.get(md5Hex);
            if (null != value) {
                return (List<Entity>) value;
            }
        }

        List<Entity> all = super.findAll(predicate, orders);

        if (hasCache && null != cacheService) {
            cacheService.set(md5Hex, all, EXPIRE_SECONDS);
            putToClearDatas(getModule(root), md5Hex);
        }
        return all;
    }

    @Override
    public List<Entity> findAll(Predicate predicate, int page, int size, OrderSpecifier<?>... orders) {
        String md5Hex = null;
        if (hasCache && null != cacheService) {
            StringBuilder queryString = new StringBuilder();
            queryString.append("findAll").append(CACHE_KEY_SEPARATOR);
            queryString.append(getModule(root)).append(CACHE_KEY_SEPARATOR);
            queryString.append(predicate).append(CACHE_KEY_SEPARATOR);
            if (null != orders) {
                for (OrderSpecifier orderSpecifier : orders) {
                    queryString.append(orderSpecifier).append(CACHE_KEY_SEPARATOR);
                }
            }
            queryString.append(page).append(CACHE_KEY_SEPARATOR).append(size).append(CACHE_KEY_SEPARATOR);
            md5Hex = DigestUtils.md5Hex(queryString.toString());
            Object value = cacheService.get(md5Hex);
            if (null != value) {
                return (List<Entity>) value;
            }
        }

        List<Entity> all = super.findAll(predicate, page, size, orders);

        if (hasCache && null != cacheService) {
            cacheService.set(md5Hex, all, EXPIRE_SECONDS);
            putToClearDatas(getModule(root), md5Hex);
        }
        return all;
    }

    @Override
    public long count(Predicate predicate) {
        return super.count(predicate);
    }

    @Override
    public long updateAll(Entity entity, Predicate predicate) {
        long l = super.updateAll(entity, predicate);
        if (hasCache && null != cacheService) {
            clearCachedDatas(getModule(root));
        }
        return l;
    }

    @Override
    public long deleteAll(Predicate predicate) {
        long l = super.deleteAll(predicate);
        if (hasCache && null != cacheService) {
            clearCachedDatas(getModule(root));
        }
        return l;
    }

    @Override
    public <T> T query(Query query) {
        return super.query(query);
    }


    String getModule(RelationalPath relationalPath) {
        return relationalPath.getSchemaName() + CACHE_KEY_SEPARATOR + relationalPath.getTableName();
    }

    void clearCachedDatas(String module) {
        Set<Object> members = cacheService.members(module);
        for (Object value : members) {
            cacheService.delete((String) value);
            cacheService.remove(module, value);
        }
    }

    void putToClearDatas(String module, String key) {
        cacheService.add(module, key);
    }
}
