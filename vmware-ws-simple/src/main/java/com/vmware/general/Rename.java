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
import com.vmware.common.annotations.Option;
import com.vmware.common.annotations.Sample;
import com.vmware.connection.ConnectedVimServiceBase;
import com.vmware.vim25.*;

import java.util.Map;

/**
 * <pre>
 * Rename
 *
 * This sample demonstrates renaming a manged entity
 *
 * <b>Parameters:</b>
 * url          [required] : url of the web service
 * username     [required] : username for the authentication
 * password     [required] : password for the authentication
 * entityname   [required] : name of the inventory object - a managed entity
 * newname      [required] : new name of the inventory object - a managed entity
 *
 * <b>Command Line:</b>
 * Rename a virtual machine VM to new name VM2
 * run.bat com.vmware.general --url [webserviceurl]
 * --username [username] --password [password] --entityname VM
 * --newname VM2
 *
 * Rename a resource pool named Rp to new name pool
 * java com.vmware.general --url [webserviceurl]
 * --username [username] --password [password] --entityname Rp
 * --newname pool
 * </pre>
 */

@Sample(name = "rename", description = "This sample demonstrates renaming a manged entity")
public class Rename extends ConnectedVimServiceBase {

    private String entityname;
    private String newentityname;

    @Option(name = "entityname", description = "name of the inventory object - a managed entity")
    public void setEntityname(String entityname) {
        this.entityname = entityname;
    }

    @Option(name = "newname", description = "new name of the inventory object - a managed entity")
    public void setNewentityname(String newentityname) {
        this.newentityname = newentityname;
    }

    /**
     * This method returns a boolean value specifying whether the Task is
     * succeeded or failed.
     *
     * @param task ManagedObjectReference representing the Task.
     * @return boolean value representing the Task result.
     * @throws InvalidCollectorVersionFaultMsg
     *
     * @throws RuntimeFaultFaultMsg
     * @throws InvalidPropertyFaultMsg
     */
    public boolean getTaskResultAfterDone(ManagedObjectReference task)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg,
            InvalidCollectorVersionFaultMsg {

        boolean retVal = false;

        // info has a property - state for state of the task
        Object[] result =
                waitForValues.wait(task, new String[]{"info.state", "info.error"},
                        new String[]{"state"}, new Object[][]{new Object[]{
                        TaskInfoState.SUCCESS, TaskInfoState.ERROR}});

        if (result[0].equals(TaskInfoState.SUCCESS)) {
            retVal = true;
        }
        if (result[1] instanceof LocalizedMethodFault) {
            throw new RuntimeException(
                    ((LocalizedMethodFault) result[1]).getLocalizedMessage());
        }
        return retVal;
    }

    @Action
    public void rename() throws DuplicateNameFaultMsg, RuntimeFaultFaultMsg, InvalidNameFaultMsg, InvalidPropertyFaultMsg, InvalidCollectorVersionFaultMsg {
        Map<String, ManagedObjectReference> entities =
                getMOREFs.inFolderByType(serviceContent.getRootFolder(),
                        "ManagedEntity");
        ManagedObjectReference memor = entities.get(entityname);
        if (memor == null) {
            System.out.println("Unable to find a Managed Entity '" + entityname
                    + "' in the Inventory");
            return;
        } else {
            ManagedObjectReference taskmor =
                    vimPort.renameTask(memor, newentityname);
            if (getTaskResultAfterDone(taskmor)) {
                System.out.println("ManagedEntity '" + entityname
                        + "' renamed successfully.");
            } else {
                System.out.println("Failure -: Managed Entity Cannot Be Renamed");
            }
        }
    }
}
