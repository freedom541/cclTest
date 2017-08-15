package com.ccl.jersey.codegen.serializer;

import com.ccl.jersey.codegen.DictItemType;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mysema.codegen.CodeWriter;
import com.mysema.codegen.model.ClassType;
import com.mysema.codegen.model.Parameter;
import com.mysema.codegen.model.Type;
import com.querydsl.codegen.*;
import com.querydsl.core.util.BeanUtils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.*;

public class SimpleDictSerializer implements Serializer {

    private final String javadocSuffix;

    private boolean printSupertype = true;

    private List<DictItemType> itemTypes;

    /**
     * Create a new BeanSerializer
     */
    public SimpleDictSerializer() {
        this(" is a Codegen dict type");
    }

    /**
     * Create a new BeanSerializer with the given javadoc suffix
     *
     * @param javadocSuffix
     */
    public SimpleDictSerializer(String javadocSuffix) {
        this.javadocSuffix = javadocSuffix;
    }

    public void serialize(EntityType model, SerializerConfig serializerConfig,
                          CodeWriter writer) throws IOException {
        String simpleName = model.getSimpleName();

        // package
        if (!model.getPackageName().isEmpty()) {
            writer.packageDecl(model.getPackageName());
        }

        Set<Supertype> superTypes = model.getSuperTypes();

        // header

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
        importedClasses.add(JsonDeserialize.class.getName());
        importedClasses.add(DictDataDeserializer.class.getName());
        for (Supertype supertype : superTypes) {
            importedClasses.add(supertype.getType().getFullName());
        }
        writer.importClasses(importedClasses.toArray(new String[importedClasses
                .size()]));

        // javadoc
        writer.javadoc(simpleName + javadocSuffix);

        for (Annotation annotation : model.getAnnotations()) {
            writer.annotation(annotation);
        }
        writer.line("@JsonDeserialize(using = " + model.getSimpleName() + "." + model.getSimpleName() + "Deserializer.class)");

        if (printSupertype && superTypes != null) {
            List<Type> ifcs = new ArrayList<>();
            for (Supertype sup : superTypes) {
                ifcs.add(sup.getType());
            }
            writer.beginClass(model.getInnerType(), null,
                    ifcs.toArray(new Type[ifcs.size()]));
        } else {
            writer.beginClass(model);
        }

        bodyStart(model, writer);

        addFullConstructor(model, writer);

        // fields
        for (Property property : model.getProperties()) {
            writer.privateField(property.getType(), property.getEscapedName());
        }

        // accessors
        // getter
        for (Property property : model.getProperties()) {
            String propertyName = property.getEscapedName();
            // getter
            writer.beginPublicMethod(property.getType(),
                    "get" + BeanUtils.capitalize(propertyName));
            writer.line("return ", propertyName, ";");
            writer.end();
        }

        writer.beginStaticMethod(model.getInnerType(), "fromValue", new Parameter("value", new ClassType(Integer.class)));
        writer.line("for (" + model.getSimpleName() + " dict : " + model.getSimpleName() + ".values()) {");
        writer.line("  if (dict.value.equals(value)) {");
        writer.line("     return dict;");
        writer.line("  }");
        writer.line("}");
        writer.line("return null;");
        writer.end();

        writer.line(" public static class " + model.getSimpleName() + "Deserializer extends DictDataDeserializer<" + model.getSimpleName() + "> {");
        writer.line("");
        writer.line("}");

        bodyEnd(model, writer);

        writer.end();
    }

    protected void bodyStart(EntityType model, CodeWriter writer)
            throws IOException {
        StringBuilder seg = new StringBuilder();
        int i = 0;
        for (DictItemType dictItemType : itemTypes) {
            seg.append(dictItemType.getName()).append("(\"").append(dictItemType.getLabel())
                    .append("\", ").append(dictItemType.getValue()).append(")");
            if (i < itemTypes.size() - 1) {
                seg.append(",");
            } else {
                seg.append(";");
            }
            i++;
        }
        writer.line(seg.toString());
        writer.line("");

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

    protected void addFullConstructor(EntityType model, CodeWriter writer)
            throws IOException {

        // full constructor
        String seg = "";
        int i = 0;
        for (Property property : model.getProperties()) {
            seg += property.getType().getSimpleName() + " "
                    + property.getName()
                    + ((i < model.getProperties().size() - 1) ? "," : "");
            i++;
        }
        writer.line(model.getSimpleName(), "(", seg, ") {");
        for (Property property : model.getProperties()) {
            writer.line("	this.", property.getName(), " = ",
                    property.getName(), ";");
        }
        writer.line("}");
        writer.line("");
    }

    public void setPrintSupertype(boolean printSupertype) {
        this.printSupertype = printSupertype;
    }

    public void setItemTypes(List<DictItemType> itemTypes) {
        this.itemTypes = itemTypes;
    }
}
