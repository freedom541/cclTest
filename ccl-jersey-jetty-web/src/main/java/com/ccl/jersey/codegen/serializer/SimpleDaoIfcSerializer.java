package com.ccl.jersey.codegen.serializer;

import com.mysema.codegen.CodeWriter;
import com.mysema.codegen.model.Type;
import com.querydsl.codegen.EntityType;
import com.querydsl.codegen.Serializer;
import com.querydsl.codegen.SerializerConfig;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.*;


public class SimpleDaoIfcSerializer implements Serializer {

    private final String javadocSuffix;

    private boolean printSupertype = true;

    /**
     * Create a new BeanSerializer
     */
    public SimpleDaoIfcSerializer() {
        this(" is a Querydsl repository interface type");
    }

    /**
     * Create a new BeanSerializer with the given javadoc suffix
     *
     * @param javadocSuffix
     */
    public SimpleDaoIfcSerializer(String javadocSuffix) {
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
        writer.importClasses(model.getSuperType().getType().getFullName());

        List<Type> parameters = model.getSuperType().getType().getParameters();
        if (parameters.size() > 0) {
            writer.importClasses(parameters.get(0).getFullName());
        }
        if (parameters.size() > 1) {
            writer.importClasses(parameters.get(1).getFullName());
        }
        writer.importClasses(importedClasses.toArray(new String[importedClasses
                .size()]));

        // javadoc
        writer.javadoc(simpleName + javadocSuffix);

        // header
        for (Annotation annotation : model.getAnnotations()) {
            writer.annotation(annotation);
        }

        if (printSupertype && model.getSuperType() != null) {
            writer.beginInterface(model, model.getSuperType().getType());
        } else {
            writer.beginInterface(model);
        }

        bodyStart(model, writer);

        bodyEnd(model, writer);

        writer.end();
    }

    protected void bodyStart(EntityType model, CodeWriter writer)
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

    public void setPrintSupertype(boolean printSupertype) {
        this.printSupertype = printSupertype;
    }

}
