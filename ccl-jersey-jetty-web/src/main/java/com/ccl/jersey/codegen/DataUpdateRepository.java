package com.ccl.jersey.codegen;

import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Collection;

/**
 * 提供数据更新操作的仓储服务的接口
 *
 * @param <Entity> 实体类
 * @param <ID> 主键类
 * @author ccl
 */
public interface DataUpdateRepository<Entity extends IdEntity<ID>, ID extends Serializable> extends Repository<Entity, ID> {
    /**
     * 插入实体
     *
     * @param entity 实体
     */
    @Validated(CreateCheck.class)
    void create(@NotNull @Valid Entity entity);

    /**
     * 批量插入实体
     *
     * @param entities 实体集合
     */
    @Validated(CreateCheck.class)
    void create(@Valid Collection<Entity> entities);

    /**
     * 更新实体，默认会将空值也一起更新
     *
     * @param entity 实体
     */
    @Validated(UpdateCheck.class)
    void update(@NotNull @Valid Entity entity);

    /**
     * 更新实体，不更新空值字段
     *
     * @param entity 实体
     */
    void updateWithNotNull(@NotNull @Valid Entity entity);

    /**
     * 批量更新实体，默认会将空值也一起更新
     *
     * @param entities 实体集合
     */
    @Validated(UpdateCheck.class)
    void update(@Valid Collection<Entity> entities);

    /**
     * 批量更新实体，不更新空值字段
     *
     * @param entities 实体集合
     */
    void updateWithNotNull(@Valid Collection<Entity> entities);


    /**
     * 根据主键删除
     *
     * @param id 主键
     */
    void deleteById(@NotNull ID id);

    /**
     * 根据主键列表删除
     *
     * @param ids
     */
    void deleteByIds(Collection<ID> ids);


}
