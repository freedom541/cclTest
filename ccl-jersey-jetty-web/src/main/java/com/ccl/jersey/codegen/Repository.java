package com.ccl.jersey.codegen;

import java.io.Serializable;

/**
 * 仓储公共标识接口
 * 
 * @author ccl
 * 
 * @param <Entity>
 *            实体类
 * @param <ID>
 *            主键类
 */
public interface Repository<Entity extends IdEntity<ID>, ID extends Serializable>{

}
