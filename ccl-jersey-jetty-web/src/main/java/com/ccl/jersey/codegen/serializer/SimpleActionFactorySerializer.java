package com.ccl.jersey.codegen.serializer;

import com.ccl.jersey.codegen.AutowiredImpl;
import com.ccl.jersey.codegen.ControllerImpl;
import com.ccl.jersey.codegen.DataAdminModule;
import com.ccl.jersey.codegen.PEntityType;
import com.google.common.base.Function;
import com.mysema.codegen.CodeWriter;
import com.mysema.codegen.model.ClassType;
import com.mysema.codegen.model.Parameter;
import com.mysema.codegen.model.Type;
import com.querydsl.codegen.EntityType;
import com.querydsl.codegen.Serializer;
import com.querydsl.codegen.SerializerConfig;
import com.querydsl.codegen.Supertype;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.*;

public class SimpleActionFactorySerializer implements Serializer {
    private static final Function<Type, Parameter> propertyToParameter = new Function<Type, Parameter>() {
        @Override
        public Parameter apply(Type input) {
            return new Parameter(Character.toLowerCase(input.getSimpleName().charAt(0))+input.getSimpleName().substring(1), input);
        }
    };

    private final String javadocSuffix;

    private boolean printSupertype = true;

    /**
     * Create a new BeanSerializer
     */
    public SimpleActionFactorySerializer() {
        this(" is a Codegen action factory type");
    }

    /**
     * Create a new BeanSerializer with the given javadoc suffix
     *
     * @param javadocSuffix
     */
    public SimpleActionFactorySerializer(String javadocSuffix) {
        this.javadocSuffix = javadocSuffix;
    }

    public void serialize(EntityType model, SerializerConfig serializerConfig,
                          CodeWriter writer) throws IOException {
        String simpleName = model.getSimpleName();

        // package
        if (!model.getPackageName().isEmpty()) {
            writer.packageDecl(model.getPackageName());
        }

        PEntityType entityType = (PEntityType) model;
        Supertype parentType = entityType.getParentType();
        ClassType superType = (ClassType) parentType.getType();
        List<Type> parameters = superType.getParameters();
        Type serviceType = parameters.get(0);
        Type modelType = parameters.get(1);
        Type voType = parameters.get(2);
        Type pkType = parameters.get(3);

        // header
        ControllerImpl repAnnotation = new ControllerImpl(
                         simpleName.substring(0, simpleName.indexOf("ActionFactory"))+"DataAdmin");
        model.addAnnotation(repAnnotation);

        // imports
        Set<String> importedClasses = getAnnotationTypes(model);
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
        importedClasses.add(Autowired.class.getName());
        importedClasses.add(superType.getFullName());
        importedClasses.add(serviceType.getFullName());
        importedClasses.add(DataAdminModule.class.getName());
        importedClasses.add(modelType.getFullName());
        importedClasses.add(voType.getFullName());
        writer.importClasses(importedClasses.toArray(new String[importedClasses
                .size()]));

        // javadoc
        writer.javadoc(simpleName + javadocSuffix);

        for (Annotation annotation : model.getAnnotations()) {
            writer.annotation(annotation);
        }

        Set<Supertype> superTypes = entityType.getSuperTypes();
        if (printSupertype && superTypes != null) {
            List<Type> ifcs = new ArrayList<>();
            for (Supertype sup : superTypes) {
                ifcs.add(sup.getType());
            }
            writer.beginClass(model, parentType.getType(),
                    ifcs.toArray(new Type[ifcs.size()]));
        } else {
            writer.beginClass(model);
        }

        Set<Type> types = new HashSet<>();
        types.add(serviceType);
        addFullConstructor(model, writer, types, voType);

        bodyStart(model, voType, pkType, writer);

        bodyEnd(model, writer);

        writer.end();
    }

    protected void bodyStart(EntityType model, Type voType, Type pkType, CodeWriter writer)
            throws IOException {
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
        return imports;
    }

    protected void addFullConstructor(EntityType model, CodeWriter writer,
                                      Set<Type> types, Type voType) throws IOException {

        // full constructor
        Annotation annotation = new AutowiredImpl();
        writer.annotation(annotation);
        writer.beginConstructor(types, propertyToParameter);
        for (Type type : types) {
            writer.line("super(", (Character.toLowerCase(type.getSimpleName().charAt(0))+type.getSimpleName().substring(1)), ");");
        }
        writer.end();
    }

    public void setPrintSupertype(boolean printSupertype) {
        this.printSupertype = printSupertype;
    }

}
