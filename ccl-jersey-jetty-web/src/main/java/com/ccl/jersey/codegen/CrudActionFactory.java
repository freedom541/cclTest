package com.ccl.jersey.codegen;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 提供基本增删改查服务的工厂
 *
 * @param <Entity>
 * @param <ID>
 * @author ccl
 */
public interface CrudActionFactory<Entity extends IdEntity<ID>, ID extends Serializable> extends ActionFactory {
    /**
     * 创建值对象
     *
     * @param entity
     * @return
     */
    Entity create(@NotNull @Valid Entity entity);

    /**
     * 更新值对象
     *
     * @param entity
     * @return
     */
    Entity update(@NotNull @Valid Entity entity);

//    /**
//     * 批量更新对象
//     *
//     * @param entity
//     * @param filter
//     */
//    void updateAll(@NotNull @Valid Entity entity, @NotNull Filter filter);

    /**
     * 根据实例查询批量更新对象
     *
     * @param entity
     * @param example
     */
    void updateAllByExample(@NotNull @Valid Entity entity, @NotNull Entity example);

    /**
     * 查看值对象
     *
     * @param id
     * @return
     */
    Entity view(@NotNull ID id);

//    /**
//     * 统计值对象个数
//     *
//     * @param filter
//     * @return
//     */
//    long count(Filter filter);

    /**
     * 统计实例条件对象个数
     *
     * @param example
     * @return
     */
    long countByExample(Entity example);

//    /**
//     * 查询值对象列表
//     *
//     * @param filter
//     * @param sort
//     * @return
//     */
//    List<Entity> list(Filter filter, Sort sort);

    /**
     * 根据实例条件查询
     *
     * @param example
     * @return
     */
    List<Entity> listByExample(Entity example, Sort sort);

//    /**
//     * 单值查询
//     *
//     * @param filter
//     * @return
//     */
//    Entity findOne(@NotNull Filter filter);

    /**
     * 根据实例做单值查询
     *
     * @param example
     * @return
     */
    Entity findOneByExample(@NotNull Entity example);

    /**
     * 分页查询值对象
     *
     * @param pageable
     * @return
     */
    Page<Entity> findAll(@NotNull PageRequest pageable, Entity example, Sort sort);

    /**
     * 删除值对象
     *
     * @param id
     * @return
     */
    Entity delete(@NotNull ID id);

//    /**
//     * 删除所有的值对象
//     *
//     * @param filter
//     */
//    void deleteAll(Filter filter);

    /**
     * 根据实例查询批量删除对象
     *
     * @param example
     */
    void deleteAllByExample(Entity example);
}
