package com.ccl.jersey.codegen;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.List;

public abstract class AbstractCrudModelActionFactory<Repository extends ModelQueryAndBatchUpdateRepository<Model, Entity, ID>, Model extends DataModel<Entity, ID>, Entity extends IdEntity<ID>, ID extends Serializable>
        extends AbstractActionFactory implements CrudModelActionFactory<Model, Entity, ID> {

    protected final Repository repository;

    public AbstractCrudModelActionFactory(Repository repository) {
        super();
        this.repository = repository;
    }

    @Action
    @Label("创建")
    @Validated(CreateCheck.class)
    @Order(101)
    @Override
    public Model create(Model model) {
        return repository.createModel(model);
    }

    @Action
    @Validated(UpdateCheck.class)
    @Order(102)
    @Label("更新")
    @Override
    public Model update(Model model) {
        repository.updateModel(model);
        return model;
    }

//    @Action
//    @Label("批量更新")
//    @Order(103)
//    @Override
//    public void updateAll(Model model, Filter filter) {
//        repository.updateAll(model, filter);
//    }

    @Action
    @Label("根据实例批量更新")
    @Order(104)
    @Override
    public void updateAllByExample(Model model, @Label("实例") Model example) {
        repository.updateAllByModel(model, example);
    }

    @Action
    @Label("查看")
    @Order(105)
    @Override
    public Model view(@Label("编号") ID id) {
        return repository.findModelById(id);
    }

//    @Action
//    @Label("单值查询")
//    @Order(106)
//    @Override
//    public Model findOne(Filter filter) {
//        return repository.findOne(filter);
//    }

    @Action
    @Label("根据实例单值查询")
    @Order(107)
    @Override
    public Model findOneByExample(@Label("实例") Model example) {
        return repository.findOneByModel(example);
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
    public long countByExample(@Label("实例") Model example) {
        return repository.countByModel(example);
    }

//    @Action
//    @Label("列表查询")
//    @Order(110)
//    @Override
//    public List<Model> list(Filter filter, Sort sort) {
//        return repository.findAll(filter, sort);
//    }

    @Action
    @Label("根据实例列表查询")
    @Order(111)
    @Override
    public List<Model> listByExample(@Label("实例") Model example, @Label("排序") Sort sort) {
        return repository.findAllByModel(example, sort);
    }

    @Action
    @Label("分页查询")
    @Order(112)
    @Override
    public Page<Model> findAll(PageRequest pageable, @Label("实例") Model example, @Label("排序") Sort sort) {
        return repository.findAllByModel(pageable.getPage(), pageable.getSize(), example, sort);
    }

    @Action
    @Label("删除")
    @Order(113)
    @Override
    public Model delete(@Label("编号") ID id) {
        Model view = view(id);
        repository.deleteModel(id);
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
    public void deleteAllByExample(@Label("实例") Model example) {
        repository.deleteAllByModel(example);
    }

}
