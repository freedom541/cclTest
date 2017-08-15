package com.ccl.jersey.codegen;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Collection;

/**
 * 提供模型更新操作的仓储服务的接口
 *
 * @param <Model> 模型类
 * @param <Entity> 实体类
 * @param <ID> 主键类
 * @author ccl
 */
public interface ModelUpdateRepository<Model extends DataModel<Entity, ID>, Entity extends IdEntity<ID>, ID extends Serializable> {
    /**
     * 创建数据模型，关联对象会级联更新
     *
     * @param model
     */
    Model createModel(@NotNull @Valid Model model);

    /**
     * 批量創建模型
     *
     * @param models
     */
    void createModels(@NotNull @Valid Collection<Model> models);

    /**
     * 更新模型对象，关联对象会级联更新，会更新空值字段
     *
     * @param model
     */
    Model updateModel(@NotNull @Valid Model model);

    /**
     * 批量更新模型
     *
     * @param models
     */
    void updateModels(@NotNull @Valid Collection<Model> models);

    /**
     * 更新模型对象，关联对象会级联更新，非空字段不更新
     *
     * @param model
     */
    Model updateModelWithNotNull(@NotNull @Valid Model model);

    /**
     * 删除模型,关联对象会级联删除
     *
     * @param id
     */
    void deleteModel(@NotNull ID id);

    /**
     * 批量刪除模型
     *
     * @param ids
     */
    void deleteModels(Collection<ID> ids);
}
