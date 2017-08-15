package com.ccl.jersey.codegen;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;

import java.io.Serializable;
import java.util.List;

/**
 * 支持多表领域查询语言执行仓库
 * 
 * @author ccl
 * 
 * @param <Entity>
 * @param <ID>
 */
public interface MutiTableQueryDslRepository<Entity extends IdEntity<ID>, ID extends Serializable>
		extends QueryDslRepository<Entity, ID> {
	/**
	 * 多表单值查询
	 * 
	 * @param tables
	 * @param predicates
     * @param orders
	 * @return
	 */
	Tuple findOne(List<AssociatedTable> tables, List<Predicate> predicates, OrderSpecifier<?>... orders);

	/**
	 * 根据多表条件统计记录数
	 * 
	 * @param tables
	 * @param predicates
	 * @return
	 */
	long count(List<AssociatedTable> tables, Predicate... predicates);

	/**
	 * 多表多数据查询
	 * 
	 * @param tables
	 * @param predicates
	 * @param orders
	 * @return
	 */
	List<Tuple> findAll(List<AssociatedTable> tables,
						List<Predicate> predicates, OrderSpecifier<?>... orders);

	/**
	 * 多表多数据查询
	 * 
	 * @param tables
	 * @param predicates
	 * @param page
	 * @param size
	 * @param orders
	 * @return
	 */
	List<Tuple> findAll(List<AssociatedTable> tables,
						List<Predicate> predicates, int page, int size,
						OrderSpecifier<?>... orders);

	/**
	 * 根据条件 {@link Predicate} 批量更新
	 * 
	 * @param entity
	 * @param tables
	 * @param predicates
	 * @return
	 */
	long updateAll(Entity entity, List<AssociatedTable> tables,
				   Predicate... predicates);

	/**
	 * 根据条件 {@link Predicate} 批量删除
	 * 
	 * @param tables
	 * @param predicates
	 * @return
	 */
	long deleteAll(List<AssociatedTable> tables, Predicate... predicates);

}
