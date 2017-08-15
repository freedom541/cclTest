package com.ccl.jersey.codegen;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.List;

public abstract class AbstractCrudActionFactory<Repository extends DataQueryAndBatchUpdateRepository<Entity, ID>, Entity extends IdEntity<ID>, ID extends Serializable>
        extends AbstractActionFactory implements CrudActionFactory<Entity, ID> {

    protected final Repository repository;

    public AbstractCrudActionFactory(Repository repository) {
        super();
        this.repository = repository;
    }

    @Action
    @Label("创建")
    @Validated(CreateCheck.class)
    @Order(101)
    @Override
    public Entity create(Entity entity) {
        repository.create(entity);
        return entity;
    }

    @Action
    @Validated(UpdateCheck.class)
    @Order(102)
    @Label("更新")
    @Override
    public Entity update(Entity entity) {
        repository.update(entity);
        return entity;
    }

//    @Action
//    @Label("批量更新")
//    @Order(103)
//    @Override
//    public void updateAll(Entity entity, Filter filter) {
//        repository.updateAll(entity, filter);
//    }

    @Action
    @Label("根据实例批量更新")
    @Order(104)
    @Override
    public void updateAllByExample(Entity entity, @Label("实例") Entity example) {
        repository.updateAll(entity, example);
    }

    @Action
    @Label("查看")
    @Order(105)
    @Override
    public Entity view(@Label("编号") ID id) {
        return repository.findById(id);
    }

//    @Action
//    @Label("单值查询")
//    @Order(106)
//    @Override
//    public Entity findOne(Filter filter) {
//        return repository.findOne(filter);
//    }

    @Action
    @Label("根据实例单值查询")
    @Order(107)
    @Override
    public Entity findOneByExample(@Label("实例") Entity example) {
        return repository.findOne(example);
    }

//    @Action
//    @Label("统计")
//    @Order(108)
//    @Override
//    public long count(Filter filter) {
//        return repository.count(filter);
//    }

    @Action
    @Label("根据实例统计")
    @Order(109)
    @Override
    public long countByExample(@Label("实例") Entity example) {
        return repository.count(example);
    }

//    @Action
//    @Label("列表查询")
//    @Order(110)
//    @Override
//    public List<Entity> list(Filter filter, Sort sort) {
//        return repository.findAll(filter, sort);
//    }

    @Action
    @Label("根据实例列表查询")
    @Order(111)
    @Override
    public List<Entity> listByExample(@Label("实例") Entity example, Sort sort) {
        return repository.findAll(example, sort);
    }

    @Action
    @Label("分页查询")
    @Order(112)
    @Override
    public Page<Entity> findAll(PageRequest pageable, @Label("实例") Entity example, Sort sort) {
        return repository.findAll(pageable.getPage(), pageable.getSize(), example, sort);
    }

    @Action
    @Label("删除")
    @Order(113)
    @Override
    public Entity delete(@Label("编号") ID id) {
        Entity view = view(id);
        repository.deleteById(id);
        return view;
    }

//    @Action
//    @Label("批量删除")
//    @Order(114)
//    @Override
//    public void deleteAll(Filter filter) {
//        repository.deleteAll(filter);
//    }

    @Action
    @Label("根据实例批量删除")
    @Order(115)
    @Override
    public void deleteAllByExample(@Label("实例") Entity example) {
        repository.deleteAll(example);
    }

}
