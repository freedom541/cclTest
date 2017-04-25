/*
 * ******************************************************
 * Copyright VMware, Inc. 2010-2012.  All Rights Reserved.
 * ******************************************************
 *
 * DISCLAIMER. THIS PROGRAM IS PROVIDED TO YOU "AS IS" WITHOUT
 * WARRANTIES OR CONDITIONS # OF ANY KIND, WHETHER ORAL OR WRITTEN,
 * EXPRESS OR IMPLIED. THE AUTHOR SPECIFICALLY # DISCLAIMS ANY IMPLIED
 * WARRANTIES OR CONDITIONS OF MERCHANTABILITY, SATISFACTORY # QUALITY,
 * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.vmware.general;

import com.vmware.common.annotations.Action;
import com.vmware.common.annotations.Sample;
import com.vmware.connection.VCenterSampleBase;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.RuntimeFaultFaultMsg;

import java.util.Map;

/**
 * <pre>
 * Browser
 *
 * This sample prints managed entity, its type, reference value,
 * property name, Property Value, Inner Object Type, its Inner Reference Value
 * and inner property value
 *
 * <b>Parameters:</b>
 * url         [required] : url of the web service
 * username    [required] : username for the authentication
 * password    [required] : password for the authentication
 *
 * <b>Command Line:</b>
 * run.bat com.vmware.general.Browser --url [webserviceurl]
 * --username [username] --password [password]
 * </pre>
 *
 * @see com.vmware.connection.ConnectedVimServiceBase
 */
@Sample(name = "browser",
        description = "This sample prints managed entity, its type, reference value, " +
                "property name, Property Value, Inner Object Type, its Inner Reference Value " +
                "and inner property value")
public class Browser extends VCenterSampleBase {
    /**
     * The main action for this sample.
     *
     * @throws InvalidPropertyFaultMsg
     * @throws RuntimeFaultFaultMsg
     */
    @Action
    public void printInventory() throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
        Map<String, ManagedObjectReference> inventory = inventory();

        for (String entityName : inventory.keySet()) {
            System.out.printf("> " + inventory.get(entityName).getType() + ":\t"
                    + inventory.get(entityName).getValue() + "\t{" + entityName + "}%n");
        }
    }

    public Map<String, ManagedObjectReference> inventory() throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
        return getMOREFs.inFolderByType(rootRef, "ManagedEntity");
    }
}
