package com.ccl.jersey.codegen;

import com.querydsl.core.Tuple;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 實體對象和模型對象之间的转换
 *
 * @author ccl
 */
public class BeanConvertUtils {
    private static final String ID = "id";
    private static final String I_18_N_PROPERTY_PREFIX = "i18n";
    /**
     * 缓存bean定义
     */
    private static Map<Class<?>, BeanDesc> cachedBeanParser = new ConcurrentHashMap<>();


    /**
     * 注册模型类型
     *
     * @param modelClass
     */
    public static void registerModelType(Class<?> modelClass) {
        if (null == cachedBeanParser.get(modelClass)) {
            cachedBeanParser.put(modelClass, new BeanDesc(modelClass));
        }
    }

    /**
     * 获取模型定义
     *
     * @param modelClass
     * @return
     */
    public static BeanDesc getModelBeanDesc(Class<?> modelClass) {
        BeanDesc beanDesc = cachedBeanParser.get(modelClass);
        if (null == beanDesc) {
            beanDesc = new BeanDesc(modelClass);
            cachedBeanParser.put(modelClass, beanDesc);
        }
        return beanDesc;
    }

    /**
     * 转换模型到實體對象
     *
     * @param models
     * @param entityClass
     * @return
     */
    public static <T> List<T> convertModelToEntity(Collection<?> models,
                                                   Class<T> entityClass) {
        List<T> answer = new ArrayList<>();
        for (Object entity : models) {
            T model = convertModelToEntity(entity, entityClass);
            if (null != model)
                answer.add(model);
        }
        return answer;
    }

    /**
     * 转换模型到實體對象
     *
     * @param models
     * @param entityClass
     * @return
     */
    public static <T> Page<T> convertModelToEntity(Page<?> models,
                                                   Class<T> entityClass) {
        return new Page<>(convertModelToEntity(models.getContent(), entityClass), models.getPage(), models.getSize(), models.getSort(), models.getTotalElements());
    }

    /**
     * 转换模型到實體對象
     *
     * @param model
     * @param entityClass
     * @return
     */
    public static <T> T convertModelToEntity(Object model, Class<T> entityClass) {
        try {
            if (null == model) {
                return null;
            }

            T domainInstance = entityClass.newInstance();
            Class<?> modelClass = model.getClass();
            BeanDesc beanParser = cachedBeanParser.get(modelClass);
            if (null == beanParser) {
                beanParser = new BeanDesc(modelClass);
                cachedBeanParser.put(modelClass, beanParser);
            }
            for (PropertyDesc propertyDescriptor : beanParser.getProperties()) {
                String voPropertyName = propertyDescriptor.getName();
                // 只转换基本数据类型或者枚举的属性
                Class<?> propertyType = propertyDescriptor.getType();
                if (BeanHelper.isPrimitive(propertyType) || propertyType.isEnum()) {

                    // 是否有需要忽略的字段
                    IgnoreProperty ignoreField = propertyDescriptor
                            .getIgnoreProperty();
                    if (null == ignoreField) {
                        String domainPropertyName = voPropertyName;
                        // 是否对字段重命名
                        Property proField = propertyDescriptor.getProperty();
                        if (null != proField) {
                            String name = proField.name();
                            if (null != name && name.length() > 0) {
                                domainPropertyName = name;
                            }

                        }

                        // 忽略领域类中没有的属性
                        Field declaredField = FieldUtils.getField(entityClass,
                                domainPropertyName, true);
                        if (null != declaredField) {
                            Class<?> domainFieldType = declaredField.getType();
                            Object propertyValue = propertyDescriptor
                                    .getValue(model);
                            if (null != propertyValue) {
                                propertyValue = DataTypeConvertUtils.convert(
                                        propertyValue, domainFieldType);
                                FieldUtils
                                        .writeField(domainInstance,
                                                domainPropertyName,
                                                propertyValue, true);
                            }
                        }
                    }
                }
            }
            // 处理属于关联关系
            if (beanParser.hasBelongsTo()) {
                List<AssociatedDesc> belongsTos = beanParser.getBelongsTos();
                for (AssociatedDesc belongsTo : belongsTos) {
                    if (null != belongsTo.getRootProperty()
                            && belongsTo.getRootProperty().length() > 0) {
                        String property = belongsTo.getProperty();
                        Object propValue = FieldUtils.readField(model, property,
                                true);
                        if (null != propValue) {
                            Object propIdValue = FieldUtils.readField(
                                    propValue, ID, true);
                            FieldUtils.writeField(domainInstance,
                                    belongsTo.getRootProperty(), propIdValue,
                                    true);
                        }
                    }
                }
            }
            return domainInstance;
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            throw new BeanConvertException("Convert model to entity error.", e);
        }

    }


    /**
     * 转换實體對象到模型
     *
     * @param entity
     * @param modelClass
     * @return
     */
    public static <T> T convertEntityToModel(Object entity, Class<T> modelClass) {
        try {
            if (null == entity) {
                return null;
            }

            if (entity instanceof Tuple) {
                return convertTupleToModel((Tuple) entity, modelClass);
            }

            if (entity instanceof IdEntity) {
                if (null == ((IdEntity<?>) entity).getId()) {
                    return null;
                }
            }

            T modelInstance = modelClass.newInstance();
            BeanDesc beanParser = cachedBeanParser.get(modelClass);
            if (null == beanParser) {
                beanParser = new BeanDesc(modelClass);
                cachedBeanParser.put(modelClass, beanParser);
            }
            for (PropertyDesc propertyDescriptor : beanParser.getProperties()) {
                String voPropertyName = propertyDescriptor.getName();
                // 只转换基本数据类型的属性
                Class<?> propertyType = propertyDescriptor.getType();
                if (BeanHelper.isPrimitive(propertyType) || propertyType.isEnum()) {
                    // 是否有需要忽略的字段
                    IgnoreProperty ignoreField = propertyDescriptor
                            .getIgnoreProperty();
                    if (null == ignoreField) {
                        String domainPropertyName = voPropertyName;
                        // 是否对字段重命名
                        Property proField = propertyDescriptor.getProperty();
                        if (null != proField) {
                            String name = proField.name();
                            if (null != name && name.length() > 0) {
                                domainPropertyName = name;
                            }

                        }
                        // 忽略领域类中没有的属性
                        Field declaredField = FieldUtils.getField(
                                entity.getClass(), domainPropertyName, true);
                        if (null != declaredField) {
                            Object fieldValue = FieldUtils.readField(entity,
                                    domainPropertyName, true);
                            if (null != fieldValue) {

                                fieldValue = DataTypeConvertUtils.convert(
                                        fieldValue, propertyType);
                                if (domainPropertyName.startsWith(I_18_N_PROPERTY_PREFIX)) {
                                    fieldValue = Messages.getMessage(String.valueOf(fieldValue));
                                }
                                propertyDescriptor.setValue(modelInstance,
                                        fieldValue);
                            }
                        }
                    }
                }
            }

            // 处理属于关联关系
            if (beanParser.hasBelongsTo()) {
                List<AssociatedDesc> belongsTos = beanParser.getBelongsTos();
                for (AssociatedDesc belongsTo : belongsTos) {
                    if (null != belongsTo.getRootProperty()
                            && belongsTo.getRootProperty().length() > 0) {
                        Object assfieldValue = FieldUtils.readField(entity,
                                belongsTo.getRootProperty(), true);
                        if (null != assfieldValue && !"".equals(assfieldValue)) {
                            String property = belongsTo.getProperty();
                            PropertyDesc propertyDesc = beanParser
                                    .getProperty(property);
                            Class<?> propType = propertyDesc.getType();
                            Object propInstance = propType.newInstance();
                            FieldUtils.writeField(propInstance, ID,
                                    assfieldValue, true);
                            propertyDesc.setValue(modelInstance, propInstance);
                        }
                    }
                }
            }
            return modelInstance;
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            throw new BeanConvertException("Convert entity to model error.", e);
        }

    }

    /**
     * 转换實體對象到模型
     *
     * @param entities
     * @param modelClass
     * @return
     */
    public static <T> List<T> convertEntityToModel(Collection entities,
                                                   Class<T> modelClass) {
        List<T> answer = new ArrayList<>();
        if (null == entities || entities.isEmpty()) {
            return answer;
        }
        Object next = entities.iterator().next();
        if (next instanceof Tuple) {
            return convertTupleToModel(entities, modelClass);
        }
        for (Object domain : entities) {
            if (null != domain) {
                T vo = convertEntityToModel(domain, modelClass);
                if (null != vo)
                    answer.add(vo);
            }
        }
        return answer;
    }

    /**
     * 转换實體對象到模型
     *
     * @param entities
     * @param modelClass
     * @return
     */
    public static <T> Page<T> convertEntityToModel(Page entities,
                                                   Class<T> modelClass) {

        return new Page(convertEntityToModel(entities.getContent(), modelClass), entities.getPage(), entities.getSize(),
                entities.getSort(), entities.getTotalElements());
    }

    /**
     * 转换结果集到模型
     *
     * @param result
     * @param modelClass
     * @return
     */
    public static <T> T convertTupleToModel(Tuple result, Class<T> modelClass) {
        try {
            T modelObj = convertSingeleObj(result, modelClass);
            if (null == modelObj) {
                return null;
            }
            convertNestedTupleToModel(result, modelClass, modelObj, null, null);
            return modelObj;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new BeanConvertException("Convert tuple to model error.", e);
        }
    }

    /**
     * 转换结果集到模型
     *
     * @param results
     * @param modelClass
     * @return
     */
    public static <T> List<T> convertTupleToModel(Collection<Tuple> results,
                                                  Class<T> modelClass) {
        List<T> answer = new ArrayList<>();
        if (null != results) {
            for (Tuple tuple : results) {
                T modelObj;
                try {
                    modelObj = convertSingeleObj(tuple, modelClass);
                    if (null == modelObj) {
                        continue;
                    }
                    // 去除重复的对象
                    if (answer.contains(modelObj)) {
                        Iterator<T> iterator = answer.iterator();
                        while (iterator.hasNext()) {
                            T t = iterator.next();
                            if (modelObj.equals(t)) {
                                modelObj = t;
                                break;
                            }
                        }
                    }
                    convertNestedTupleToModel(tuple, modelClass, modelObj, null, null);
                    if (!answer.contains(modelObj)) {
                        answer.add(modelObj);
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    throw new BeanConvertException(
                            "Convert tuple to model error.", e);
                }
            }
        }
        return answer;
    }

    private static <T> void convertNestedTupleToModel(Tuple result,
                                                      Class<T> modelClass, Object modelObj, Class<?> parentClass, Object parentObj) {
        try {
            if (null == modelObj) {
                return;
            }
            BeanDesc beanParser = cachedBeanParser.get(modelClass);
            if (null == beanParser) {
                beanParser = new BeanDesc(modelClass);
                cachedBeanParser.put(modelClass, beanParser);
            }
            // 处理属于关联关系
            if (beanParser.hasBelongsTo()) {
                List<AssociatedDesc> belongsTos = beanParser.getBelongsTos();
                for (AssociatedDesc belongsTo : belongsTos) {
                    parserBelongsTo(belongsTo,
                            beanParser.getProperty(belongsTo.getProperty()),
                            result, modelClass, modelObj, parentClass, parentObj);
                }
            }

            // 处理一对多关联关系
            if (beanParser.hasHasMany()) {
                List<AssociatedDesc> hasManys = beanParser.getHasManys();
                for (AssociatedDesc hasMany : hasManys) {
                    parserHasMany(hasMany,
                            beanParser.getProperty(hasMany.getProperty()),
                            result, modelClass, modelObj, parentClass);
                }
            }

        } catch (ClassNotFoundException | IllegalArgumentException
                | IllegalAccessException e) {
            e.printStackTrace();
            throw new BeanConvertException("Convert tuple to model error.", e);
        }
    }

    private static <T> void parserHasMany(AssociatedDesc hasMany,
                                          PropertyDesc propertyParser, Tuple result, Class<T> modelClass,
                                          Object modelObj, Class<?> parentClass) throws ClassNotFoundException,
            IllegalAccessException {
        Class<?> associatedType = hasMany.getAssociatedClass();

        // 去除循环引用，支持双向关联
        if (associatedType.equals(parentClass)) {
            return;
        }
        Object propertyValue = propertyParser.getValue(modelObj);
        // 集合属性初始化，只支持Set和List
        if (null == propertyValue) {
            Class<?> propertyType = propertyParser.getType();
            if (propertyType.equals(Set.class)) {
                propertyValue = new LinkedHashSet<>();
            } else if (propertyType.equals(List.class)) {
                propertyValue = new ArrayList<>();
            } else {
                throw new IllegalArgumentException(
                        "Has many property only supported set or list collection type.");
            }
        }
        @SuppressWarnings("unchecked")
        Collection<Object> collection = (Collection<Object>) propertyValue;
        Object associatedObj = convertSingeleObj(result, associatedType);
        // 去除重复的对象
        if (null != associatedObj) {
            if (collection.contains(associatedObj)) {
                Iterator<?> iterator = collection.iterator();
                while (iterator.hasNext()) {
                    Object t = iterator.next();
                    if (associatedObj.equals(t)) {
                        associatedObj = t;
                        break;
                    }
                }
            } else {
                collection.add(associatedObj);
            }
        }
        propertyParser.setValue(modelObj, collection);
        convertNestedTupleToModel(result, associatedType, associatedObj, modelClass, modelObj);
    }

    private static <T> void parserBelongsTo(AssociatedDesc belongsTo,
                                            PropertyDesc propertyParser, Tuple result, Class<T> modelClass,
                                            Object modelObj, Class<?> parentClass, Object parentObj) throws ClassNotFoundException, IllegalAccessException {
        Class<?> associatedType = belongsTo.getAssociatedClass();
        // 去除循环引用，支持双向关联
        if (associatedType.equals(parentClass)) {
            propertyParser.setValue(modelObj, parentObj);
        } else {
            Object associatedObj = propertyParser.getValue(modelObj);
            if (null != associatedObj) {
                Object associatedObj_ = convertSingeleObj(result, associatedType);
                if (null != associatedObj_) {
                    BeanDesc beanParser = cachedBeanParser.get(associatedType);
                    if (null == beanParser) {
                        beanParser = new BeanDesc(associatedType);
                        cachedBeanParser.put(associatedType, beanParser);
                    }
                    for (PropertyDesc propertyDesc : beanParser.getProperties()) {
                        Object propertyValue = propertyDesc.getValue(associatedObj);
                        if (null != propertyValue) {
                            propertyDesc.setValue(associatedObj_, propertyValue);
                        }
                    }
                    associatedObj = associatedObj_;
                }

            }
            propertyParser.setValue(modelObj, associatedObj);
            convertNestedTupleToModel(result, associatedType, associatedObj, modelClass, modelObj);
        }
    }

    private static <T> T convertSingeleObj(Tuple result, Class<T> modelClass)
            throws ClassNotFoundException {
        BeanDesc beanDesc = cachedBeanParser.get(modelClass);
        Object domainObj = result.get(SimpleEntityPathResolver.INSTANCE
                .createPath(beanDesc.getEntityClass()));
        if (null != domainObj) {
            T vo = convertEntityToModel(domainObj, modelClass);
            return vo;
        }
        return null;
    }

}
