package com.ccl.jersey.codegen;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.sql.RelationalPath;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple implementation of {@link EntityPathResolver} to lookup a query class
 * by reflection and using the static field of the same type.
 * 
 * @author ccl
 */
public enum SimpleEntityPathResolver implements EntityPathResolver {

	INSTANCE;

	private static final String NO_CLASS_FOUND_TEMPLATE = "Not find a query class %s for domain class %s!";
	private static final String NO_FIELD_FOUND_TEMPLATE = "Not find a static field of the same type in %s!";

	@Override
	public <T> RelationalPath<T> createPath(Class<T> domainClass) {
		return createPath(domainClass, null);
	}

	/**
	 * Creates an {@link EntityPath} instance for the given domain class. Tries
	 * to lookup a class matching the naming convention (prepend Q to the simple
	 * name of the class, same package) and find a static field of the same type
	 * in it.
	 * 
	 * @param domainClass
	 * @return
	 */

	@SuppressWarnings("unchecked")
	public <T> RelationalPath<T> createPath(Class<T> domainClass, String alias) {

		String pathClassName = getQueryClassName(domainClass);

		try {
			Class<?> pathClass = ClassUtils.forName(pathClassName,
					domainClass.getClassLoader());
			if (StringUtils.isBlank(alias)) {
				Field field = getStaticFieldOfType(pathClass);

				if (field == null) {
					throw new IllegalStateException(String.format(
							NO_FIELD_FOUND_TEMPLATE, pathClass));
				} else {
					return (RelationalPath<T>) ReflectionUtils.getField(field,
							null);
				}
			} else {
				Constructor<?> constructor = pathClass
						.getConstructor(String.class);
				return (RelationalPath<T>) constructor.newInstance(alias);
			}

		} catch (Exception e) {
			throw new IllegalArgumentException(String.format(
					NO_CLASS_FOUND_TEMPLATE, pathClassName,
					domainClass.getName()), e);
		}
	}

	Map<RelationalPath<?>, PathBuilder<?>> pathBuilderRegister = new HashMap<>();

	@SuppressWarnings("unchecked")
	@Override
	public <T> PathBuilder<T> getPathBuilder(RelationalPath<T> root) {
		PathBuilder<T> pathBuilder = (PathBuilder<T>) pathBuilderRegister
				.get(root);
		if (null != pathBuilder) {
			return pathBuilder;
		}
		pathBuilder = new PathBuilder<T>(root.getType(), root.getMetadata());
		pathBuilderRegister.put(root, pathBuilder);
		return pathBuilder;
	}

	@SuppressWarnings("unchecked")
	public <T> Path<T> getProperty(RelationalPath<?> root, String property) {
		Class<? extends Object> pathClass = root.getClass();
		try {
			Field field = pathClass.getDeclaredField(property);
			return (Path<T>) field.get(root);
		} catch (NoSuchFieldException | SecurityException
				| IllegalArgumentException | IllegalAccessException e) {
			return null;
		}

	}

	/**
	 * Returns the first static field of the given type inside the given type.
	 * 
	 * @param type
	 * @return
	 */
	private Field getStaticFieldOfType(Class<?> type) {

		for (Field field : type.getDeclaredFields()) {

			boolean isStatic = Modifier.isStatic(field.getModifiers());
			boolean hasSameType = type.equals(field.getType());

			if (isStatic && hasSameType) {
				return field;
			}
		}

		Class<?> superclass = type.getSuperclass();
		return Object.class.equals(superclass) ? null
				: getStaticFieldOfType(superclass);
	}

	/**
	 * Returns the name of the query class for the given domain class.
	 * 
	 * @param domainClass
	 * @return
	 */
	private String getQueryClassName(Class<?> domainClass) {

		String packageName = domainClass.getPackage().getName();
		packageName = packageName.substring(0, packageName.length() - 6)
				+ "query";
		String simpleClassName = ClassUtils.getShortName(domainClass);
		return String.format("%s.Q%s%s", packageName,
				getClassBase(simpleClassName), domainClass.getSimpleName()
						.substring(1));
	}

	/**
	 * Analyzes the short class name and potentially returns the outer class.
	 * 
	 * @param shortName
	 * @return
	 */
	private String getClassBase(String shortName) {

		String[] parts = shortName.split("\\.");

		if (parts.length < 2) {
			return "";
		}

		return parts[0] + "_";
	}
}
