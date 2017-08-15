package com.ccl.jersey.codegen.serializer;

import com.ccl.jersey.codegen.*;
import com.mysema.codegen.CodeWriter;
import com.mysema.codegen.model.*;
import com.querydsl.codegen.*;
import com.querydsl.codegen.Property;
import com.querydsl.core.util.BeanUtils;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.util.*;

public class SimpleBeanSerializer implements Serializer {

    private final boolean propertyAnnotations;

    private final String javadocSuffix;

    private boolean addToString, addFullConstructor;

    private boolean printSupertype = false;

    /**
     * Create a new BeanSerializer
     */
    public SimpleBeanSerializer() {
        this(true, " is a Querydsl bean type");
    }

    /**
     * Create a new BeanSerializer with the given javadoc suffix
     *
     * @param javadocSuffix
     */
    public SimpleBeanSerializer(String javadocSuffix) {
        this(true, javadocSuffix);
    }

    /**
     * Create a new BeanSerializer
     *
     * @param propertyAnnotations
     */
    public SimpleBeanSerializer(boolean propertyAnnotations) {
        this(propertyAnnotations, " is a Querydsl bean type");
    }

    /**
     * Create a new BeanSerializer
     *
     * @param propertyAnnotations
     * @param javadocSuffix
     */
    public SimpleBeanSerializer(boolean propertyAnnotations,
                                String javadocSuffix) {
        this.propertyAnnotations = propertyAnnotations;
        this.javadocSuffix = javadocSuffix;
    }

    public void serialize(EntityType model, SerializerConfig serializerConfig,
                          CodeWriter writer) throws IOException {
        String simpleName = model.getSimpleName();

        // package
        if (!model.getPackageName().isEmpty()) {
            writer.packageDecl(model.getPackageName());
        }


        // imports
        Set<String> importedClasses = getAnnotationTypes(model);
        for (Property property : model.getProperties()) {
            Type propertyType = property.getType();
            if (Collection.class.isAssignableFrom(propertyType.getJavaClass())) {
                propertyType = propertyType.getParameters().get(0);
            } else if (propertyType.getJavaClass().isArray()) {
                propertyType = propertyType.getComponentType();
            }
            if (!propertyType.isPrimitive()) {
                importedClasses.add(propertyType.getFullName());
            }
        }
        if (model.hasLists()) {
            importedClasses.add(List.class.getName());
        }
        if (model.hasCollections()) {
            importedClasses.add(Collection.class.getName());
        }
        if (model.hasSets()) {
            importedClasses.add(Set.class.getName());
        }
        if (model.hasMaps()) {
            importedClasses.add(Map.class.getName());
        }
        if (addToString && model.hasArrays()) {
            importedClasses.add(Arrays.class.getName());
        }
        writer.importClasses(((PEntityType) model).getParentType().getType().getFullName());
        if (null != model.getSuperType()) {
            writer.importClasses(model.getSuperType().getType().getFullName());
        }
        writer.importClasses(CreateCheck.class.getName());
        writer.importClasses(UpdateCheck.class.getName());
        writer.importClasses(importedClasses.toArray(new String[importedClasses
                .size()]));

        // javadoc
        writer.javadoc(simpleName + javadocSuffix);

        // header
        for (Annotation annotation : model.getAnnotations()) {
            if (annotation instanceof Uniques) {
                Uniques belongsTos = (Uniques) annotation;
                writer.beginLine("@"
                        + annotation.annotationType().getSimpleName()
                        + "(values = {");
                writer.beginLine("\n");
                for (int i = 0; i < belongsTos.values().length; i++) {
                    Unique belongsTo = belongsTos.values()[i];
                    writer.append("\t");
                    writer.annotation(belongsTo);
                    if (i != belongsTos.values().length - 1) {
                        writer.append(",");
                    }
                }
                writer.beginLine("})");
                writer.beginLine("\n");
            } else {
                writer.annotation(annotation);
            }
        }

        List<Type> ifcs = new ArrayList<>();
        if (model.getSuperType() != null) {
            Set<Supertype> superTypes = model.getSuperTypes();
            for (Supertype sup : superTypes) {
                ifcs.add(sup.getType());
            }
        }
        writer.beginClass(model, ((PEntityType) model).getParentType().getType(),
                ifcs.toArray(new Type[ifcs.size()]));

        bodyStart(model, writer);

        if (addFullConstructor) {
            addFullConstructor(model, writer);
        }

        // fields
        for (Property property : model.getProperties()) {

            if (propertyAnnotations) {
                for (Annotation annotation : property.getAnnotations()) {
                    if (NotNull.class
                            .equals(annotation.annotationType())) {
                        NotNullImpl notNull = (NotNullImpl) annotation;
                        if ("id".equalsIgnoreCase(property.getName())) {
                            notNull.setGroups(new Class<?>[]{UpdateCheck.class});
                        } else {
                            notNull.setGroups(new Class<?>[]{CreateCheck.class, UpdateCheck.class});
                        }
                        if (null == property.getDefaultValue() &&
                                !"createTime".equals(property.getName()) && !"updateTime".equals(property.getName())) {
                            writer.annotation(annotation);
                        }
                    } else {
                        writer.annotation(annotation);
                    }
                }
            }
            writer.privateField(property.getType(), property.getEscapedName());
        }

        // accessors
        for (Property property : model.getProperties()) {
            String propertyName = property.getEscapedName();
            // getter
            writer.beginPublicMethod(property.getType(),
                    "get" + BeanUtils.capitalize(propertyName));
            writer.line("return ", propertyName, ";");
            writer.end();
            // setter
            Parameter parameter = new Parameter(propertyName,
                    property.getType());
            writer.beginPublicMethod(Types.VOID,
                    "set" + BeanUtils.capitalize(propertyName), parameter);
            writer.line("this.", propertyName, " = ", propertyName, ";");
            writer.end();
        }

        addSetDefault(model, writer);

        if (addToString) {
            addToString(model, writer);
        }

        bodyEnd(model, writer);

        writer.end();
    }

    protected void addFullConstructor(EntityType model, CodeWriter writer)
            throws IOException {
        // public empty constructor
        writer.beginConstructor();
        for (Property property : model.getProperties()) {
            if ("available".equals(property.getEscapedName())) {
                writer.line("this.", property.getEscapedName(), " = true;");
                break;
            }
        }
        writer.end();
    }


    protected void addSetDefault(EntityType model, CodeWriter writer)
            throws IOException {
        writer.annotation(new OverrideImpl());
        writer.beginPublicMethod(Types.VOID, "setDefaultValue");
        for (Property property : model.getProperties()) {
            Type type = property.getType();
            Object defaultValue = property.getDefaultValue();
            if (null != defaultValue) {
                String def = String.valueOf(defaultValue);
                if (type instanceof ClassType) {
                    Class cls = ((ClassType) type).getJavaClass();
                    if (String.class.equals(cls)) {
                        def = "\"" + defaultValue + "\"";
                    } else if (Long.class.equals(cls)) {
                        def = defaultValue + "l";
                    } else if (Float.class.equals(cls)) {
                        def = defaultValue + "f";
                    } else if (BigDecimal.class.equals(cls)) {
                        def = "new BigDecimal(" + defaultValue + ")";
                    }
                }
                writer.line("if(null==", property.getEscapedName(), "){");
                writer.line("   this.", property.getEscapedName(), " = ", def, ";");
                writer.line("}");
            }
        }
        writer.end();
    }

    protected void addToString(EntityType model, CodeWriter writer)
            throws IOException {
        writer.annotation(new OverrideImpl());
        writer.beginPublicMethod(Types.STRING, "toString");
        StringBuilder builder = new StringBuilder();
        StringBuilder params = new StringBuilder();
        builder.append("String.format(\"" + model.getSimpleName());
        builder.append(" { ");
        int index = 0;
        for (Property property : model.getProperties()) {
            String propertyName = property.getEscapedName();
            builder.append(propertyName + " : %s");
            if (property.getType().getCategory() == TypeCategory.ARRAY) {
                params.append("Arrays.toString(" + propertyName + ")");
            } else {
                params.append(propertyName);
            }
            if (index != (model.getProperties().size() - 1)) {
                builder.append(",");
                params.append(",");
            }
            index++;
        }
        builder.append(" }\"");
        builder.append(",");
        builder.append(params);
        builder.append(")");
        writer.line(" return ", builder.toString(), ";");
        writer.end();
    }

    protected void bodyStart(EntityType model, CodeWriter writer)
            throws IOException {
//        long currentTimeMillis = System.nanoTime();
//        writer.beginLine("private static final long serialVersionUID =",
//                currentTimeMillis + "L;");
//        writer.beginLine("\n");

    }

    protected void bodyEnd(EntityType model, CodeWriter writer)
            throws IOException {
        // template method
    }

    private Set<String> getAnnotationTypes(EntityType model) {
        Set<String> imports = new HashSet<String>();
        for (Annotation annotation : model.getAnnotations()) {
            imports.add(annotation.annotationType().getName());
        }
        if (propertyAnnotations) {
            for (Property property : model.getProperties()) {
                for (Annotation annotation : property.getAnnotations()) {
                    imports.add(annotation.annotationType().getName());
                }
            }
        }
        return imports;
    }

    public void setAddToString(boolean addToString) {
        this.addToString = addToString;
    }

    public void setAddFullConstructor(boolean addFullConstructor) {
        this.addFullConstructor = addFullConstructor;
    }

    public void setPrintSupertype(boolean printSupertype) {
        this.printSupertype = printSupertype;
    }

}
