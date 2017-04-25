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

import com.vmware.common.annotations.Option;
import com.vmware.common.annotations.Sample;
import com.vmware.connection.ConnectedVimServiceBase;
import com.vmware.vim25.*;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * <pre>
 * OneTimeScheduledTask
 *
 * This sample demonstrates creation of ScheduledTask using the ScheduledTaskManager
 *
 * <b>Parameters:</b>
 * url           [required] : url of the web service.
 * username      [required] : username for the authentication
 * password      [required] : password for the authentication
 * vmname        [required] : name of VM to poweroff
 * taskname      [required] : name of the task
 *
 * <b>Command Line:</b>
 * run.bat com.vmware.scheduling.OneTimeScheduledTask
 * --url [webserviceurl] --username [username] --password [password]
 * --vmname [VM name] --taskname [Task name]
 * </pre>
 */
@Sample(
        name = "one-time-task",
        description =
                "This sample demonstrates creation of ScheduledTask using the ScheduledTaskManager"
)
public class OneTimeScheduledTask extends ConnectedVimServiceBase {
    ManagedObjectReference propCollectorRef;
    ManagedObjectReference scheduleManager;
    ManagedObjectReference virtualMachine;

    String vmName = null;
    String taskName = null;

    @Option(name = "vmname", description = "name of the VM to power off")
    public void setVmName(String vmName) {
        this.vmName = vmName;
    }

    @Option(name = "taskname", description = "name of the task")
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
     * @return TraversalSpec specification to get to the VirtualMachine managed
     *         object.
     */
    TraversalSpec getVMTraversalSpec() {
        // Create a traversal spec that starts from the 'root' objects
        // and traverses the inventory tree to get to the VirtualMachines.
        // Build the traversal specs bottoms up

        //Traversal to get to the VM in a VApp
        TraversalSpec vAppToVM = new TraversalSpec();
        vAppToVM.setName("vAppToVM");
        vAppToVM.setType("VirtualApp");
        vAppToVM.setPath("vm");

        //Traversal spec for VApp to VApp
        TraversalSpec vAppToVApp = new TraversalSpec();
        vAppToVApp.setName("vAppToVApp");
        vAppToVApp.setType("VirtualApp");
        vAppToVApp.setPath("resourcePool");
        //SelectionSpec for VApp to VApp recursion
        SelectionSpec vAppRecursion = new SelectionSpec();
        vAppRecursion.setName("vAppToVApp");
        //SelectionSpec to get to a VM in the VApp
        SelectionSpec vmInVApp = new SelectionSpec();
        vmInVApp.setName("vAppToVM");
        //SelectionSpec for both VApp to VApp and VApp to VM
        List<SelectionSpec> vAppToVMSS = new ArrayList<SelectionSpec>();
        vAppToVMSS.add(vAppRecursion);
        vAppToVMSS.add(vmInVApp);
        vAppToVApp.getSelectSet().addAll(vAppToVMSS);

        //This SelectionSpec is used for recursion for Folder recursion
        SelectionSpec sSpec = new SelectionSpec();
        sSpec.setName("VisitFolders");

        // Traversal to get to the vmFolder from DataCenter
        TraversalSpec dataCenterToVMFolder = new TraversalSpec();
        dataCenterToVMFolder.setName("DataCenterToVMFolder");
        dataCenterToVMFolder.setType("Datacenter");
        dataCenterToVMFolder.setPath("vmFolder");
        dataCenterToVMFolder.setSkip(false);
        dataCenterToVMFolder.getSelectSet().add(sSpec);

        // TraversalSpec to get to the DataCenter from rootFolder
        TraversalSpec traversalSpec = new TraversalSpec();
        traversalSpec.setName("VisitFolders");
        traversalSpec.setType("Folder");
        traversalSpec.setPath("childEntity");
        traversalSpec.setSkip(false);
        List<SelectionSpec> sSpecArr = new ArrayList<SelectionSpec>();
        sSpecArr.add(sSpec);
        sSpecArr.add(dataCenterToVMFolder);
        sSpecArr.add(vAppToVM);
        sSpecArr.add(vAppToVApp);
        traversalSpec.getSelectSet().addAll(sSpecArr);
        return traversalSpec;
    }

    /**
     * Get the MOR of the Virtual Machine by its name.
     */
    void getVmByVMname() throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        ManagedObjectReference retVal = null;
        ManagedObjectReference rootFolder = serviceContent.getRootFolder();
        TraversalSpec tSpec = getVMTraversalSpec();
        // Create Property Spec
        PropertySpec propertySpec = new PropertySpec();
        propertySpec.setAll(Boolean.FALSE);
        propertySpec.getPathSet().add("name");
        propertySpec.setType("VirtualMachine");

        // Now create Object Spec
        ObjectSpec objectSpec = new ObjectSpec();
        objectSpec.setObj(rootFolder);
        objectSpec.setSkip(Boolean.TRUE);
        objectSpec.getSelectSet().add(tSpec);

        // Create PropertyFilterSpec using the PropertySpec and ObjectPec
        // created above.
        PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
        propertyFilterSpec.getPropSet().add(propertySpec);
        propertyFilterSpec.getObjectSet().add(objectSpec);

        List<PropertyFilterSpec> listpfs =
                new ArrayList<PropertyFilterSpec>(1);
        listpfs.add(propertyFilterSpec);
        List<ObjectContent> listobjcont =
                retrievePropertiesAllObjects(listpfs);

        if (listobjcont != null) {
            for (ObjectContent oc : listobjcont) {
                ManagedObjectReference mr = oc.getObj();
                String vmnm = null;
                List<DynamicProperty> dps = oc.getPropSet();
                if (dps != null) {
                    for (DynamicProperty dp : dps) {
                        vmnm = (String) dp.getVal();
                    }
                }
                if (vmnm != null && vmnm.equals(vmName)) {
                    retVal = mr;
                    break;
                }
            }
        }
        if (retVal == null) {
            System.out.println("No VM with name " + vmName + " exists!");
        } else {
            virtualMachine = retVal;
        }
    }

    /**
     * Create method action to power off a vm.
     *
     * @return the action to run when the schedule runs
     */
    Action createTaskAction() {
        MethodAction action = new MethodAction();

        // Method Name is the WSDL name of the
        // ManagedObject's method that is to be run,
        // in this Case, the powerOff method of the VM
        action.setName("PowerOffVM_Task");
        return action;
    }

    /**
     * Create a Once task scheduler to run 30 minutes from now.
     *
     * @return one time task scheduler
     * @throws DatatypeConfigurationException
     */
    TaskScheduler createTaskScheduler()
            throws DatatypeConfigurationException {
        // Create a Calendar Object and add 30 minutes to allow
        // the Action to be run 30 minutes from now
        GregorianCalendar gcal = new GregorianCalendar();
        gcal.add(Calendar.MINUTE, 30);
        XMLGregorianCalendar runTime =
                DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
        // Create a OnceTaskScheduler and set the time to
        // run tha Task Action at in the Scheduler.
        OnceTaskScheduler scheduler = new OnceTaskScheduler();
        scheduler.setRunAt(runTime);
        return scheduler;
    }

    /**
     * Create a Scheduled Task using the poweroff method action and the onetime
     * scheduler, for the VM found.
     *
     * @param taskAction action to be performed when schedule executes
     * @param scheduler  the scheduler used to execute the action
     * @throws Exception
     */
    void createScheduledTask(Action taskAction, TaskScheduler scheduler) throws DuplicateNameFaultMsg, RuntimeFaultFaultMsg, InvalidNameFaultMsg {
        // Create the Scheduled Task Spec and set a unique task name
        // and description, and enable the task as soon as it is created
        ScheduledTaskSpec scheduleSpec = new ScheduledTaskSpec();
        scheduleSpec.setName(taskName);
        scheduleSpec.setDescription("PowerOff VM in 30 minutes");
        scheduleSpec.setEnabled(true);

        // Set the PowerOff Method Task Action and the
        // Once scheduler in the spec
        scheduleSpec.setAction(taskAction);
        scheduleSpec.setScheduler(scheduler);

        // Create ScheduledTask for the VirtualMachine we found earlier
        if (virtualMachine != null) {
            ManagedObjectReference task =
                    vimPort.createScheduledTask(scheduleManager, virtualMachine,
                            scheduleSpec);
            // printout the MoRef id of the Scheduled Task
            System.out.println("Successfully created Once Task: "
                    + task.getValue());
        } else {
            System.out.println("Virtual Machine " + vmName + " not found");
            return;
        }
    }

    @com.vmware.common.annotations.Action
    public void run() throws DatatypeConfigurationException, DuplicateNameFaultMsg, RuntimeFaultFaultMsg, InvalidNameFaultMsg, InvalidPropertyFaultMsg {
        propCollectorRef = serviceContent.getPropertyCollector();
        scheduleManager = serviceContent.getScheduledTaskManager();

        // find vm moref
        getVmByVMname();
        // create the power Off action to be scheduled
        Action taskAction = createTaskAction();
        // create a One time scheduler to run
        TaskScheduler taskScheduler = createTaskScheduler();
        // Create Scheduled Task
        createScheduledTask(taskAction, taskScheduler);
    }
}
