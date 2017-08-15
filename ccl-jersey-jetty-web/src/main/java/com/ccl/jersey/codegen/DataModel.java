package com.ccl.jersey.codegen;

import java.io.Serializable;

/**
 * 数据模型
 *
 * @author ccl
 */
public interface DataModel<Entity extends IdEntity<ID>, ID extends Serializable> {
    /**
     * 获取实体标识
     *
     * @return 实体标识
     */
    ID getId();

    /**
     * 设置实体标识
     *
     * @param id
     */
    void setId(ID id);
}
