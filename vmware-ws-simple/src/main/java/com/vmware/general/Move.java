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

import java.util.Arrays;
import java.util.Map;


/**
 * <pre>
 * Move
 *
 * This sample moves a managed entity from its current
 * location in the inventory to a new location, in a specified folder.
 *
 * This sample finds both the managed entity and the target
 * folder in the inventory tree before attempting the move.
 * If either of these is not found, an error message displays.
 *
 * <b>Parameters:</b>
 * url          [required] : url of the web service
 * username     [required] : username for the authentication
 * password     [required] : password for the authentication
 * entityname   [required] : name of the inventory object - a managed entity
 * foldername   [required] : name of folder to move inventory object into
 *
 * <b>Command Line:</b>
 * Move an inventory object into the target folder:
 * run.bat com.vmware.general.Move --url [webserviceurl]
 * --username [username] --password [password] --entityname [inventory object name]
 * --foldername [target folder name]
 * </pre>
 */
@Sample(name = "move", description = "moves a managed entity from its current " +
        "location in the inventory to a new location, in a specified folder")
public class Move extends ConnectedVimServiceBase {
    private String entityname;
    private String foldername;

    @Option(name = "entityname", description = "name of the inventory object - a managed entity")
    public void setEntityname(String entityname) {
        this.entityname = entityname;
    }

    @Option(name = "foldername", description = "name of folder to move inventory object into")
    public void setFoldername(String foldername) {
        this.foldername = foldername;
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
    public void move() throws InvalidPropertyFaultMsg,
            RuntimeFaultFaultMsg, DuplicateNameFaultMsg, InvalidFolderFaultMsg, InvalidStateFaultMsg, InvalidCollectorVersionFaultMsg {
        Map<String, ManagedObjectReference> entities =
                getMOREFs.inFolderByType(serviceContent.getRootFolder(),
                        "ManagedEntity");
        ManagedObjectReference memor = entities.get(entityname);
        if (memor == null) {
            System.out.println("Unable to find a managed entity named '"
                    + entityname + "' in the Inventory");
            return;
        }
        entities =
                getMOREFs.inFolderByType(serviceContent.getRootFolder(), "Folder");
        ManagedObjectReference foldermor = entities.get(foldername);
        if (foldermor == null) {
            System.out.println("Unable to find folder '" + foldername
                    + "' in the Inventory");
            return;
        } else {
            ManagedObjectReference taskmor =
                    vimPort.moveIntoFolderTask(foldermor, Arrays.asList(memor));
            if (getTaskResultAfterDone(taskmor)) {
                System.out.println("ManagedEntity '" + entityname
                        + "' moved to folder '" + foldername + "' successfully.");
            } else {
                System.out.println("Failure -: Managed Entity cannot be moved");
            }
        }
    }

}