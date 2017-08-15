package com.ccl.jersey.codegen;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.sql.SQLQueryFactory;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * 领域查询语言执行仓库
 *
 * @param <Entity>
 * @param <ID>
 * @author ccl
 */
public interface QueryDslRepository<Entity extends IdEntity<ID>, ID extends Serializable>
        extends Repository<Entity, ID> {
    /**
     * 插入实体
     *
     * @param entity 实体
     */
    void create(Entity entity);

    /**
     * 批量插入实体
     *
     * @param entities 实体集合
     */
    void create(Collection<Entity> entities);

    /**
     * 更新实体
     *
     * @param entity 实体
     * @param withNullBindings 是否更新空值字段
     */
    void update(Entity entity, boolean withNullBindings);

    /**
     * 批量更新实体
     *
     * @param entities 实体集合
     * @param withNullBindings 是否更新空值字段
     */
    void update(Collection<Entity> entities, boolean withNullBindings);

    /**
     * 单值查询 {@link Predicate}.
     *
     * @param predicate
     * @param orders
     * @return
     */
    Entity findOne(Predicate predicate, OrderSpecifier<?>... orders);

    /**
     * 查询所有实体 {@link Predicate} 根据指定的排序 {@link OrderSpecifier}s.
     *
     * @param predicate
     * @param orders
     * @return
     */
    List<Entity> findAll(Predicate predicate, OrderSpecifier<?>... orders);

    /**
     * 查询所有实体 {@link Predicate} 根据指定的排序 {@link OrderSpecifier}s.
     *
     * @param predicate
     * @param page
     * @param size
     * @param orders
     * @return
     */
    List<Entity> findAll(Predicate predicate, int page, int size,
                         OrderSpecifier<?>... orders);

    /**
     * 根据条件 {@link Predicate} 统计记录数
     *
     * @param predicate
     * @return the number of instances
     */
    long count(Predicate predicate);

    /**
     * 根据条件 {@link Predicate} 批量更新
     *
     * @param entity
     * @param predicate
     * @return
     */
    long updateAll(Entity entity, Predicate predicate);

    /**
     * 根据条件 {@link Predicate} 批量删除
     *
     * @param predicate
     * @return
     */
    long deleteAll(Predicate predicate);

    /**
     * 执行自定义查询
     *
     * @param query
     * @return
     */
    <T> T query(Query query);

    /**
     * 自定义查询回调
     *
     * @author ccl
     */
    interface Query {
        /**
         * 执行查询
         *
         * @param sqlQuery
         * @return
         */
        <T> T executeQuery(SQLQueryFactory sqlQuery);
    }

}
