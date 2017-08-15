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
public interface CrudModelActionFactory<Model extends DataModel<Entity, ID>, Entity extends IdEntity<ID>, ID extends Serializable> extends ActionFactory {
    /**
     * 创建模型對象
     *
     * @param model
     * @return
     */
    Model create(@NotNull @Valid Model model);

    /**
     * 更新模型對象
     *
     * @param model
     * @return
     */
    Model update(@NotNull @Valid Model model);

//    /**
//     * 批量更新对象
//     *
//     * @param model
//     * @param filter
//     */
//    void updateAll(@NotNull @Valid Model model, @NotNull Filter filter);

    /**
     * 根据实例查询批量更新对象
     *
     * @param model
     * @param example
     */
    void updateAllByExample(@NotNull @Valid Model model, @NotNull @Valid Model example);

    /**
     * 查看模型對象
     *
     * @param id
     * @return
     */
    Model view(@NotNull ID id);

//    /**
//     * 统计模型對象个数
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
    long countByExample(@Valid Model example);

//    /**
//     * 查询模型對象列表
//     *
//     * @param filter
//     * @param sort
//     * @return
//     */
//    List<Model> list(Filter filter, Sort sort);

    /**
     * 根据实例条件查询
     *
     * @param example
     * @return
     */
    List<Model> listByExample(@Valid Model example, @Valid Sort sort);

//    /**
//     * 单值查询
//     *
//     * @param filter
//     * @return
//     */
//    Model findOne(@NotNull Filter filter);

    /**
     * 根据实例做单值查询
     *
     * @param example
     * @return
     */
    Model findOneByExample(@NotNull @Valid Model example);

    /**
     * 分页查询模型對象
     *
     * @param pageable
     * @return
     */
    Page<Model> findAll(@NotNull PageRequest pageable, @Valid Model example, @Valid Sort sort);

    /**
     * 删除模型對象
     *
     * @param id
     * @return
     */
    Model delete(@NotNull ID id);

//    /**
//     * 删除所有的模型對象
//     *
//     * @param filter
//     */
//    void deleteAll(Filter filter);

    /**
     * 根据实例查询批量删除对象
     *
     * @param example
     */
    void deleteAllByExample(@Valid Model example);
}
