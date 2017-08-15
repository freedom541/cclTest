package com.ccl.jersey.codegen;


import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.sql.RelationalPath;

/**
 * 路径解析器
 * 
 * @author ccl
 * 
 */
public interface EntityPathResolver {
	/**
	 * 根据实体类构建对应的查询对象。<br/>
	 * 如根据PAgentJob类可以找到QPAgentJob.pAgentJob
	 * 
	 * @param domainClass
	 * @return
	 */
	public <T> RelationalPath<T> createPath(Class<T> domainClass);
	/**
	 * 根据实体类构建对应的查询对象。<br/>
	 * 如根据PAgentJob类可以找到QPAgentJob.pAgentJob
	 * 
	 * @param domainClass
	 * @param alias
	 * @return
	 */
	public <T> RelationalPath<T> createPath(Class<T> domainClass, String alias);

	public <T> PathBuilder<T> getPathBuilder(RelationalPath<T> root);

	/**
	 * 根据实体属性找到对应的查询路径。<br/>
	 * 如根据startTime可以找到QPAgentJob.pAgentJob.startTime
	 * 
	 * @param root
	 * @param property
	 * @return
	 */
	public <T> Path<T> getProperty(RelationalPath<?> root, String property);
}
