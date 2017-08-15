package com.ccl.jersey.codegen;

import java.lang.reflect.Field;

public class PropertyDesc {

	private final Field propertyField;

	private final String name;

	private final Class<?> type;

	private final Property property;

	private final IgnoreProperty ignoreProperty;

	public PropertyDesc(Field propertyField) {
		super();
		this.propertyField = propertyField;
		this.name = propertyField.getName();
		this.type = propertyField.getType();
		property = propertyField.getAnnotation(Property.class);
		ignoreProperty = propertyField.getAnnotation(IgnoreProperty.class);
	}

	public Field getPropertyField() {
		return propertyField;
	}

	public String getName() {
		return name;
	}

	public Class<?> getType() {
		return type;
	}

	public Property getProperty() {
		return property;
	}

	public IgnoreProperty getIgnoreProperty() {
		return ignoreProperty;
	}

	public void setValue(Object obj, Object value)
			throws IllegalArgumentException, IllegalAccessException {
		propertyField.set(obj, value);
	}

	public Object getValue(Object obj) throws IllegalArgumentException,
			IllegalAccessException {
		return propertyField.get(obj);
	}
}
