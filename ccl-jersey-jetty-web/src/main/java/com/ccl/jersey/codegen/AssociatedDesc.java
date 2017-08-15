package com.ccl.jersey.codegen;

public class AssociatedDesc {

	private final String property;

	private final Class<?> rootClass;

	private final String rootProperty;

	private final Class<?> associatedClass;

	private final String associatedProperty;

	public AssociatedDesc(String property, Class<?> rootClass,
			String rootProperty, Class<?> associatedClass,
			String associatedProperty) {
		super();
		this.property = property;
		this.rootClass = rootClass;
		this.rootProperty = rootProperty;
		this.associatedClass = associatedClass;
		this.associatedProperty = associatedProperty;
	}

	public Class<?> getRootClass() {
		return rootClass;
	}

	public String getRootProperty() {
		return rootProperty;
	}

	public String getProperty() {
		return property;
	}

	public Class<?> getAssociatedClass() {
		return associatedClass;
	}

	public String getAssociatedProperty() {
		return associatedProperty;
	}

}
