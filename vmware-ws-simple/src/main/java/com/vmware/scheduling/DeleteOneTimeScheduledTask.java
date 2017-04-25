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

package com.vmware.scheduling;

import com.vmware.common.annotations.Action;
import com.vmware.common.annotations.Option;
import com.vmware.common.annotations.Sample;
import com.vmware.connection.ConnectedVimServiceBase;
import com.vmware.vim25.*;

import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 * DeleteOneTimeScheduledTask
 *
 * This sample demonstrates deleting a scheduled task
 *
 * <b>Parameters:</b>
 * url            [required] : url of the web service
 * username       [required] : username for the authentication
 * password       [required] : password for the authentication
 * taskname       [required] : name of the task to be deleted
 *
 * <b>Command Line:</b>
 * run.bat com.vmware.scheduling.DeleteOneTimeScheduledTask
 * --url [webserviceurl] --username [username] --password [password]
 * --taskname [TaskToBeDeleted]
 * </pre>
 */
@Sample(name = "delete-one-time-task", description = "This sample demonstrates deleting a scheduled task")
public class DeleteOneTimeScheduledTask extends ConnectedVimServiceBase {
    private ManagedObjectReference propCollectorRef;
    private ManagedObjectReference scheduleManager;

    String taskName = null;

    @Option(name = "taskname", description = "name of the task to be deleted")
    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    /**
     * Uses the new RetrievePropertiesEx method to emulate the now deprecated
     * RetrieveProperties method
     *
     * @param listpfs
     * @return list of object content
     * @throws Exception
     */
    List<ObjectContent> retrievePropertiesAllObjects(
            List<PropertyFilterSpec> listpfs) throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {

        RetrieveOptions propObjectRetrieveOpts = new RetrieveOptions();

        List<ObjectContent> listobjcontent = new ArrayList<ObjectContent>();

        RetrieveResult rslts =
                vimPort.retrievePropertiesEx(propCollectorRef, listpfs,
                        propObjectRetrieveOpts);
        if (rslts != null && rslts.getObjects() != null
                && !rslts.getObjects().isEmpty()) {
            listobjcontent.addAll(rslts.getObjects());
        }
        String token = null;
        if (rslts != null && rslts.getToken() != null) {
            token = rslts.getToken();
        }
        while (token != null && !token.isEmpty()) {
            rslts =
                    vimPort.continueRetrievePropertiesEx(propCollectorRef, token);
            token = null;
            if (rslts != null) {
                token = rslts.getToken();
                if (rslts.getObjects() != null && !rslts.getObjects().isEmpty()) {
                    listobjcontent.addAll(rslts.getObjects());
                }
            }
        }

        return listobjcontent;
    }

    /**
     * Create Property Filter Spec to get names of all ScheduledTasks the
     * ScheduledTaskManager has.
     *
     * @return PropertyFilterSpec to get properties
     */
    private PropertyFilterSpec createTaskPropertyFilterSpec() {
        // The traversal spec traverses the "scheduledTask" property of
        // ScheduledTaskManager to get names of ScheduledTask ManagedEntities
        // A Traversal Spec allows traversal into a ManagedObjects
        // using a single attribute of the managedObject
        TraversalSpec scheduledTaskTraversal = new TraversalSpec();

        scheduledTaskTraversal.setType(scheduleManager.getType());
        scheduledTaskTraversal.setPath("scheduledTask");

        // We want to get values of the scheduleTask property
        // of the scheduledTaskManager, which are the ScheduledTasks
        // so we set skip = false.
        scheduledTaskTraversal.setSkip(Boolean.FALSE);
        scheduledTaskTraversal.setName("scheduleManagerToScheduledTasks");

        // Setup a PropertySpec to return names of Scheduled Tasks so
        // we can find the named ScheduleTask ManagedEntity to delete
        // Name is an attribute of ScheduledTaskInfo so
        // the path set will contain "info.name"
        PropertySpec propSpec = new PropertySpec();
        propSpec.setAll(new Boolean(false));
        propSpec.getPathSet().add("info.name");
        propSpec.setType("ScheduledTask");

        // PropertySpecs are wrapped in a PropertySpec array
        // since we only have a propertySpec for the ScheduledTask,
        // the only values we will get back are names of scheduledTasks
        List<PropertySpec> propSpecArray = new ArrayList<PropertySpec>();
        propSpecArray.add(propSpec);

        // Create an Object Spec to specify the starting or root object
        // and the SelectionSpec to traverse to each ScheduledTask in the
        // array of scheduledTasks in the ScheduleManager
        ObjectSpec objSpec = new ObjectSpec();
        objSpec.setObj(scheduleManager);
        objSpec.getSelectSet().add(scheduledTaskTraversal);

        // Set skip = true so properties of ScheduledTaskManager
        // are not returned, and only values of info.name property of
        // each ScheduledTask is returned
        objSpec.setSkip(Boolean.TRUE);

        // ObjectSpecs used in PropertyFilterSpec are wrapped in an array
        List<ObjectSpec> objSpecArray = new ArrayList<ObjectSpec>();
        objSpecArray.add(objSpec);

        // Create the PropertyFilter spec with
        // ScheduledTaskManager as "root" object
        PropertyFilterSpec spec = new PropertyFilterSpec();
        spec.getPropSet().addAll(propSpecArray);
        spec.getObjectSet().addAll(objSpecArray);
        return spec;
    }

    private ManagedObjectReference findOneTimeScheduledTask(
            PropertyFilterSpec scheduledTaskSpec) throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        boolean found = false;
        ManagedObjectReference oneTimeTask = null;
        // Use PropertyCollector to get all scheduled tasks the
        // ScheduleManager has
        List<PropertyFilterSpec> propertyFilterSpecList =
                new ArrayList<PropertyFilterSpec>();
        propertyFilterSpecList.add(scheduledTaskSpec);
        List<ObjectContent> scheduledTasks =
                retrievePropertiesAllObjects(propertyFilterSpecList);

        // Find the task name we're looking for and return the
        // ManagedObjectReference for the ScheduledTask with the
        // name that matched the name of the OneTimeTask created earlier
        if (scheduledTasks != null) {
            for (int i = 0; i < scheduledTasks.size() && !found; i++) {
                ObjectContent taskContent = scheduledTasks.get(i);
                List<DynamicProperty> props = taskContent.getPropSet();
                for (int p = 0; p < props.size() && !found; p++) {
                    DynamicProperty prop = props.get(p);
                    String taskNameVal = (String) prop.getVal();
                    if (taskName.equals(taskNameVal)) {
                        oneTimeTask = taskContent.getObj();
                        found = true;
                    }
                }
            }
        }
        if (!found) {
            System.out.println("Scheduled task '" + taskName + "' not found");
        }
        return oneTimeTask;
    }

    private void deleteScheduledTask(ManagedObjectReference oneTimeTask) throws RuntimeFaultFaultMsg, InvalidStateFaultMsg {
        // Remove the One Time Scheduled Task
        vimPort.removeScheduledTask(oneTimeTask);
        System.out.println("Successfully Deleted ScheduledTask: "
                + oneTimeTask.getValue());
    }

    @Action
    public void run() throws RuntimeFaultFaultMsg, InvalidStateFaultMsg, InvalidPropertyFaultMsg {
        propCollectorRef = serviceContent.getPropertyCollector();
        scheduleManager = serviceContent.getScheduledTaskManager();
        // create a Property Filter Spec to get names
        // of all scheduled tasks
        PropertyFilterSpec taskFilterSpec = createTaskPropertyFilterSpec();

        // Retrieve names of all ScheduledTasks and find
        // the named one time Scheduled Task
        ManagedObjectReference oneTimeTask = findOneTimeScheduledTask(taskFilterSpec);

        // Delete the one time scheduled task
        if (oneTimeTask != null) {
            deleteScheduledTask(oneTimeTask);
        }
    }
}
