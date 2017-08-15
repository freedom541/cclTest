package com.ccl.jersey.codegen;

import java.io.Serializable;

/**
 * 实体标识
 *
 * @author ccl
 */
public interface IdEntity<ID extends Serializable> extends Serializable{
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

    /**
     * 設置默認值
     */
    void setDefaultValue();
}
