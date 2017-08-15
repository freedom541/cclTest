package com.ccl.jersey.codegen;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 完成仓储初始化操作的抽象类
 *
 * @param <Entity> 实体类
 * @param <ID> 主键类
 * @author ccl
 */
public abstract class AbstractInitialRepository<Entity extends IdEntity<ID>, ID extends Serializable>
        implements Repository<Entity, ID> {
    protected  final Logger logger = LoggerFactory
            .getLogger(Repository.class);

    protected final static String ID = "id";

    protected Class<Entity> entityClass;
    protected Class<ID> primaryKeyClass;

    public AbstractInitialRepository() {
        initType(this.getClass());

    }

    @SuppressWarnings("unchecked")
    private void initType(Class<?> cls) {
        Type superType = cls.getGenericSuperclass();
        if (superType instanceof ParameterizedType) {
            ParameterizedType ptype = (ParameterizedType) superType;
            Type cmpType = ptype.getActualTypeArguments()[0];
            this.entityClass = (Class<Entity>) cmpType;
            Type cmpType1 = ptype.getActualTypeArguments()[1];
            this.primaryKeyClass = (Class<ID>) cmpType1;
        }
    }

}
