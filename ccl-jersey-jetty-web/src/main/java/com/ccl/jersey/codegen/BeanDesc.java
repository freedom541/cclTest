package com.ccl.jersey.codegen;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class BeanDesc {

	private final Class<?> beanClass;

	private final Map<String, PropertyDesc> properties;

	private final List<AssociatedDesc> belongsTos;

	private final List<AssociatedDesc> hasManys;

	private Class<?> entityClass;

	public BeanDesc(Class<?> beanClass) {
		super();
		this.beanClass = beanClass;

		Domain domain = beanClass.getAnnotation(Domain.class);
		if (null != domain) {
			String domainClassName = domain.domainClassName();
			try {
				entityClass = Class.forName(domainClassName);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}

		Field[] fields = getAllVisibleFields(beanClass);
		properties = new LinkedHashMap<>();
		for (Field field : fields) {
			PropertyDesc propertyParser = new PropertyDesc(field);
			properties.put(field.getName(), propertyParser);
		}

		belongsTos = new ArrayList<>();
		BelongsTo belongsTo = beanClass.getAnnotation(BelongsTo.class);
		if (null != belongsTo) {
			AssociatedDesc belongsToIns = findBelongsToPropertyAssociatedClass(belongsTo);
			belongsTos.add(belongsToIns);
		}
		BelongsTos belongsTos2 = beanClass.getAnnotation(BelongsTos.class);
		if (null != belongsTos2) {
			for (int i = 0; i < belongsTos2.values().length; i++) {
				BelongsTo belongsTo2 = belongsTos2.values()[i];
				AssociatedDesc belongsToIns = findBelongsToPropertyAssociatedClass(belongsTo2);
				belongsTos.add(belongsToIns);
			}
		}

		hasManys = new ArrayList<>();
		HasMany hasMany = beanClass.getAnnotation(HasMany.class);
		if (null != hasMany) {
			AssociatedDesc hasMany_ = findHasManyPropertyAssociatedClass(hasMany);
			hasManys.add(hasMany_);
		}
		HasManys hasManys2 = beanClass.getAnnotation(HasManys.class);
		if (null != hasManys2) {
			for (int i = 0; i < hasManys2.values().length; i++) {
				HasMany hasMany2 = hasManys2.values()[i];
				AssociatedDesc hasMany_ = findHasManyPropertyAssociatedClass(hasMany2);
				hasManys.add(hasMany_);
			}
		}

	}

	private Field[] getAllVisibleFields(Class<?> beanClass) {
		List<Field> fieldList = new ArrayList<>();
		Class<?> superClass = beanClass;
		while (null != superClass.getSuperclass()) {
			superClass = superClass.getSuperclass();
			Field[] fields = superClass.getDeclaredFields();
			for (int i = 0; i < fields.length; i++) {
				if (Modifier.isProtected(fields[i].getModifiers())
						&& !Modifier.isStatic(fields[i].getModifiers())) {
					fields[i].setAccessible(true);
					fieldList.add(fields[i]);
				}
			}
		}

		Field[] fields = beanClass.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			if (!Modifier.isStatic(fields[i].getModifiers())) {
				fields[i].setAccessible(true);
				fieldList.add(fields[i]);
			}
		}
		return fieldList.toArray(new Field[fieldList.size()]);
	}

	private AssociatedDesc findBelongsToPropertyAssociatedClass(
			BelongsTo belongsTo) {
		Class<?> associatedClass = null;
		try {
			Field field = beanClass.getDeclaredField(belongsTo.property());
			field.setAccessible(true);
			associatedClass = field.getType();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		AssociatedDesc associatedDesc = new AssociatedDesc(
				belongsTo.property(), beanClass, 
				belongsTo.rootField(), associatedClass,
				belongsTo.associatedField());
		return associatedDesc;
	}

	private AssociatedDesc findHasManyPropertyAssociatedClass(HasMany hasMany) {
		Class<?> associatedClass = null;
		String property = hasMany.property();
		try {
			Field field = beanClass.getDeclaredField(property);
			field.setAccessible(true);
			// 从集合类泛型中取出关联类
			if (!Null.class.equals(associatedClass)) {
				Type genericType = field.getGenericType();
				if (genericType instanceof ParameterizedType) {
					Type[] types = ((ParameterizedType) genericType)
							.getActualTypeArguments();
					associatedClass = (Class<?>) types[0];
				}
			}
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		AssociatedDesc associatedDesc = new AssociatedDesc(hasMany.property(),
				beanClass,  hasMany.rootField(), associatedClass,
				hasMany.associatedField());
		return associatedDesc;
	}

	public Class<?> getBeanClass() {
		return beanClass;
	}

	public Collection<PropertyDesc> getProperties() {
		return properties.values();
	}

	public PropertyDesc getProperty(String property) {
		return properties.get(property);
	}

	public Class<?> getEntityClass() {
		return entityClass;
	}

	public List<AssociatedDesc> getBelongsTos() {
		return belongsTos;
	}

	public List<AssociatedDesc> getHasManys() {
		return hasManys;
	}

	public boolean hasBelongsTo() {
		return null != belongsTos && belongsTos.size() > 0;
	}

	public boolean hasHasMany() {
		return null != hasManys && hasManys.size() > 0;
	}
}
