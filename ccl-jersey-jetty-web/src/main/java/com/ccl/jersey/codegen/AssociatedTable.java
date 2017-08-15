package com.ccl.jersey.codegen;


import com.querydsl.core.types.Predicate;
import com.querydsl.sql.RelationalPath;

public class AssociatedTable {

	private final RelationalPath<?> entityPath;

	private final Predicate on;

	private AssociatedDesc associatedDesc;

	public AssociatedTable(RelationalPath<?> entityPath, Predicate on) {
		super();
		this.entityPath = entityPath;
		this.on = on;
	}

	public AssociatedTable(RelationalPath<?> entityPath, Predicate on,
                           AssociatedDesc associatedDesc) {
		super();
		this.entityPath = entityPath;
		this.on = on;
		this.associatedDesc = associatedDesc;
	}

	public RelationalPath<?> getEntityPath() {
		return entityPath;
	}

	public Predicate getOn() {
		return on;
	}

	public AssociatedDesc getAssociatedDesc() {
		return associatedDesc;
	}

}
