package com.ccl.jersey.codegen;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * 提供自定义的过滤功能的仓储服务的接口
 *
 * @param <Entity> 实体类
 * @param <ID> 主键类
 * @author ccl
 */
public interface DataQueryAndBatchUpdateRepository<Entity extends IdEntity<ID>, ID extends Serializable> extends DataUpdateRepository<Entity, ID> {

    /**
     * 批量更新对象
     *
     * @param entity
     * @param filter
     */
    void updateAll(@NotNull @Valid Entity entity, @NotNull Filter filter);

    /**
     * 根据实例批量更新对象
     *
     * @param entity
     * @param example
     */
    void updateAll(@NotNull @Valid Entity entity, @NotNull Entity example);


    /**
     * 批量删除对象
     *
     * @param filter
     */
    void deleteAll(Filter filter);

    /**
     * 根据实例批量删除对象
     *
     * @param example
     */
    void deleteAll(Entity example);

    /**
     * 主键查询
     *
     * @param id 主键
     * @return 实体
     */
    Entity findById(@NotNull ID id);

    /**
     * 指定主键列表的查询
     *
     * @param ids 主键列表
     * @return 实体集合
     */
    List<Entity> findByIds(Collection<ID> ids);

    /**
     * 单值查询
     *
     * @param filter
     * @return
     */
    Entity findOne(@NotNull Filter filter);

    /**
     * 根据实例条件做单值查询
     *
     * @param example
     * @return
     */
    Entity findOne(@NotNull Entity example);

    /**
     * 单值查询
     *
     * @param filter
     * @param sort
     * @return
     */
    Entity findOne(@NotNull Filter filter, Sort sort);

    /**
     * 根据实例条件做单值查询
     *
     * @param example
     * @param sort
     * @return
     */
    Entity findOne(@NotNull Entity example, Sort sort);

    /**
     * 查询值对象列表
     *
     * @param filter
     * @param sort
     * @return
     */
    List<Entity> findAll(Filter filter, Sort sort);

    /**
     * 根据实例条件查询
     *
     * @param example
     * @param sort
     * @return
     */
    List<Entity> findAll(Entity example, Sort sort);

    /**
     * 分页查询值对象
     *
     * @param page
     * @param size
     * @param filter
     * @param sort
     * @return
     */
    Page<Entity> findAll(int page, int size, Filter filter, Sort sort);

    /**
     * 分页查询值对象
     *
     * @param page
     * @param size
     * @param example
     * @param sort
     * @return
     */
    Page<Entity> findAll(int page, int size, Entity example, Sort sort);

    /**
     * 统计值对象个数
     *
     * @param filter
     * @return
     */
    long count(Filter filter);

    /**
     * 统计实例条件对象个数
     *
     * @param example
     * @return
     */
    long count(Entity example);

}
