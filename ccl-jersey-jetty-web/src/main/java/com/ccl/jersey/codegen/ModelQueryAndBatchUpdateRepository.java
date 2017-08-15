package com.ccl.jersey.codegen;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 提供自定义的过滤功能的数据模型仓储服务的接口
 *
 * @param <Model> 数据模型类
 * @param <Entity> 实体类
 * @param <ID> 主键类
 * @author ccl
 */
public interface ModelQueryAndBatchUpdateRepository<Model extends DataModel<Entity, ID>, Entity extends IdEntity<ID>, ID extends Serializable> extends ModelUpdateRepository<Model, Entity, ID> {

    /**
     * 批量更新对象
     *
     * @param model
     * @param filter
     */
    Model updateAllByModel(@NotNull @Valid Model model, @NotNull Filter filter);

    /**
     * 根据实例批量更新对象
     *
     * @param model
     * @param example
     */
    Model updateAllByModel(@NotNull @Valid Model model, @NotNull Model example);

    /**
     * 批量删除对象
     *
     * @param filter
     */
    void deleteAllByModel(Filter filter);

    /**
     * 根据实例批量删除对象
     *
     * @param example
     */
    void deleteAllByModel(Model example);

    /**
     * 主键查询
     *
     * @param id 主键
     * @return 实体
     */
    Model findModelById(@NotNull ID id);

    /**
     * 单值查询
     *
     * @param filter
     * @return
     */
    Model findOneByModel(@NotNull Filter filter);

    /**
     * 根据实例条件做单值查询
     */
    Model findOneByModel(@NotNull Model example);

    /**
     * 单值查询
     *
     * @param filter
     * @param sort
     * @return
     */
    Model findOneByModel(@NotNull Filter filter, Sort sort);

    /**
     * 根据实例条件做单值查询
     *
     * @param sort
     */
    Model findOneByModel(@NotNull Model example, Sort sort);

    /**
     * 查询值对象列表
     *
     * @param filter
     * @param sort
     * @return
     */
    List<Model> findAllByModel(Filter filter, Sort sort);

    /**
     * 根据实例条件查询
     *
     * @param example
     * @param sort
     * @return
     */
    List<Model> findAllByModel(Model example, Sort sort);

    /**
     * 分页查询值对象
     *
     * @param page
     * @param size
     * @param filter
     * @param sort
     * @return
     */
    Page<Model> findAllByModel(int page, int size, Filter filter, Sort sort);

    /**
     * 分页查询值对象
     *
     * @param page
     * @param size
     * @param example
     * @param sort
     * @return
     */
    Page<Model> findAllByModel(int page, int size, Model example, Sort sort);

    /**
     * 统计值对象个数
     *
     * @param filter
     * @return
     */
    long countByModel(Filter filter);

    /**
     * 统计实例条件对象个数
     *
     * @param example
     * @return
     */
    long countByModel(Model example);

}
