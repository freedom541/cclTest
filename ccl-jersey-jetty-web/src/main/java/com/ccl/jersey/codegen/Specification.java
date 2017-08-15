package com.ccl.jersey.codegen;


import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.sql.RelationalPath;

/**
 * 查询条件构建器
 * 
 * @author ccl
 * 
 * @param <T>
 */
public interface Specification<T> {
	/**
	 * 构建查询条件
	 * 
	 * @param root
	 * @param builder
	 * @param entityPathResolver
	 * @return
	 */
	public Predicate toPredicate(RelationalPath<T> root,
								 PathBuilder<T> builder, EntityPathResolver entityPathResolver);
}
