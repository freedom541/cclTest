package com.ccl.jersey.codegen;

import com.mysema.codegen.model.Type;
import com.querydsl.codegen.EntityType;
import com.querydsl.codegen.Supertype;

import java.util.Set;

public class PEntityType extends EntityType {

    private Supertype parentType;

    public PEntityType(Type type, Supertype parentType,
                       Set<Supertype> superTypes) {
        super(type, superTypes);
        this.parentType = parentType;
    }

    public PEntityType(Type type, Supertype parentType) {
        super(type);
        this.parentType = parentType;
    }

    public PEntityType(Type type) {
        super(type);
    }

    public Supertype getParentType() {
        return parentType;
    }

    public void setParentType(Supertype parentType) {
        this.parentType = parentType;
    }
}
