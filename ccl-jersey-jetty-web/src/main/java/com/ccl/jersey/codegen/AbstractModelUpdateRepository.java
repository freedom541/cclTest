package com.ccl.jersey.codegen;

import org.springframework.context.ApplicationContext;

import javax.inject.Inject;
import javax.sql.DataSource;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by ccl on 2015/9/21.
 */
public abstract class AbstractModelUpdateRepository<Entity extends IdEntity<ID>, ID extends Serializable, Model extends DataModel<Entity, ID>>
        extends AbstractDataQueryAndBatchUpdateRepository<Entity, ID> implements ModelUpdateRepository<Model, Entity, ID> {

    protected Class<Model> modelClass;

    protected BeanDesc modelBeanDesc;

    @Inject
    protected ApplicationContext ctx;

    public AbstractModelUpdateRepository(DataSource dataSource) {
        super(dataSource);
        initType(this.getClass());
    }

    public AbstractModelUpdateRepository(QueryDslConfig queryDslConfig) {
        super(queryDslConfig);
        initType(this.getClass());
    }

    private void initType(Class<?> cls) {
        Type superType = cls.getGenericSuperclass();
        if (superType instanceof ParameterizedType) {
            ParameterizedType ptype = (ParameterizedType) superType;
            Type cmpType = ptype.getActualTypeArguments()[2];
            this.modelClass = (Class<Model>) cmpType;
        }

        BeanConvertUtils.registerModelType(modelClass);
        modelBeanDesc = BeanConvertUtils.getModelBeanDesc(modelClass);
    }

    @Override
    public Model createModel(Model model) {
        Entity entity = BeanConvertUtils.convertModelToEntity(model, entityClass);
        create(entity);
        return BeanConvertUtils.convertEntityToModel(entity, modelClass);
    }

    @Override
    public void createModels(@NotNull @Valid Collection<Model> models) {
        List<Entity> entities = new ArrayList<>();
        for (Model model : models) {
            entities.add(BeanConvertUtils.convertModelToEntity(model, entityClass));
        }
        create(entities);
    }

    @Override
    public Model updateModel(Model model) {
        Entity entity = BeanConvertUtils.convertModelToEntity(model, entityClass);
        update(entity);
        return BeanConvertUtils.convertEntityToModel(entity, modelClass);
    }

    @Override
    public void updateModels(@NotNull @Valid Collection<Model> models) {
        List<Entity> entities = new ArrayList<>();
        for (Model model : models) {
            entities.add(BeanConvertUtils.convertModelToEntity(model, entityClass));
        }
        update(entities);
    }

    @Override
    public Model updateModelWithNotNull(Model model) {
        Entity entity = BeanConvertUtils.convertModelToEntity(model, entityClass);
        updateWithNotNull(entity);
        return BeanConvertUtils.convertEntityToModel(entity, modelClass);
    }

    @Override
    public void deleteModel(ID id) {
        deleteById(id);
    }

    @Override
    public void deleteModels(Collection<ID> ids) {
        deleteByIds(ids);
    }

}
