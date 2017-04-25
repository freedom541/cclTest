package com.vmware.connection.helpers.builders;

import com.vmware.vim25.ObjectSpec;
import com.vmware.vim25.PropertyFilterSpec;
import com.vmware.vim25.PropertySpec;

import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 */
public class PropertyFilterSpecBuilder extends PropertyFilterSpec {
    private void init() {
        if (propSet == null) {
            propSet = new ArrayList<PropertySpec>();
        }
        if(objectSet == null) {
            objectSet = new ArrayList<ObjectSpec>();
        }
    }

    public PropertyFilterSpecBuilder reportMissingObjectsInResults(final Boolean value) {
        this.setReportMissingObjectsInResults(value);
        return this;
    }

    public PropertyFilterSpecBuilder propSet(final PropertySpec... propertySpecs) {
        init();
        this.propSet.addAll(Arrays.asList(propertySpecs));
        return this;
    }

    public PropertyFilterSpecBuilder objectSet(final ObjectSpec... objectSpecs) {
        init();
        this.objectSet.addAll(Arrays.asList(objectSpecs));
        return this;
    }
}
