package com.ccl.jersey.codegen;

import java.io.Serializable;

/**
 * @author ccl
 */
public abstract class AbstractDataModel<Entity extends IdEntity<ID>, ID extends Serializable> implements
        DataModel<Entity, ID>, Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 4011808646935395228L;

    @Override
    public int hashCode() {
        if (getId() == null) {
            return Integer.MIN_VALUE;
        } else {
            return getId().hashCode();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass().equals(obj.getClass())) {
            final AbstractDataModel<Entity,ID> entity = (AbstractDataModel<Entity,ID>) obj;
            if ((entity.getId() == null) || (this.getId() == null)) {
                return false;
            } else {
                return entity.getId().equals(this.getId());
            }
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "DataModel {id=" + getId() + "}";
    }
}
