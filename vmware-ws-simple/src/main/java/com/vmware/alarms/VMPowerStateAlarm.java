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

package com.vmware.alarms;

import com.vmware.common.annotations.Action;
import com.vmware.common.annotations.Option;
import com.vmware.common.annotations.Sample;
import com.vmware.connection.ConnectedVimServiceBase;
import com.vmware.vim25.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <pre>
 * VMPowerStateAlarm
 *
 * This sample which creates an Alarm to monitor the virtual machine's power state
 *
 * <b>Parameters:</b>
 * url         [required] : url of the web service
 * username    [required] : username for the authentication
 * password    [required] : password for the authentication
 * vmname      [required] : Name of the virtual machine
 * alarm       [required] : Name of the alarms
 *
 * <b>Command Line:</b>
 * Create an alarm AlarmABC on a virtual machine
 * run.bat com.vmware.vm.VMPowerStateAlarm --url [webserviceurl]
 * --username [username] --password  [password] --vmname [vmname] --alarm [alarm]
 * </pre>
 */

@Sample(name = "vm-power-state-alarm", description = "This sample which creates an Alarm to monitor the virtual machine's power state")
public class VMPowerStateAlarm extends ConnectedVimServiceBase {
    private ManagedObjectReference propCollectorRef;
    private ManagedObjectReference alarmManager;
    private ManagedObjectReference vmMor;

    private String alarm = null;
    private String vmname = null;

    @Option(name = "vmname", description = "name of the virtual machine to monitor")
    public void setVmname(String vmname) {
        this.vmname = vmname;
    }

    @Option(name = "alarm", description = "Name of the alarms")
    public void setAlarm(String alarm) {
        this.alarm = alarm;
    }


    /**
     * Gets the VM traversal spec.
     *
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
     * Get the MOR of the Virtual Machine by its name.
     *
     * @param vmName The name of the Virtual Machine
     * @return The Managed Object reference for this VM
     */
    ManagedObjectReference getVmByVMname(String vmName) throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
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
        return retVal;
    }

    /**
     * Creates the state alarm expression.
     *
     * @return the state alarm expression
     * @throws Exception the exception
     */
    StateAlarmExpression createStateAlarmExpression() {
        StateAlarmExpression expression = new StateAlarmExpression();
        expression.setType("VirtualMachine");
        expression.setStatePath("runtime.powerState");
        expression.setOperator(StateAlarmOperator.IS_EQUAL);
        expression.setRed("poweredOff");
        return expression;
    }

    /**
     * Creates the power on action.
     *
     * @return the method action
     */
    MethodAction createPowerOnAction() {
        MethodAction action = new MethodAction();
        action.setName("PowerOnVM_Task");
        MethodActionArgument argument = new MethodActionArgument();
        argument.setValue(null);
        action.getArgument().addAll(
                Arrays.asList(new MethodActionArgument[]{argument}));
        return action;
    }

    /**
     * Creates the alarm trigger action.
     *
     * @param methodAction the method action
     * @return the alarm triggering action
     * @throws Exception the exception
     */
    AlarmTriggeringAction createAlarmTriggerAction(
            MethodAction methodAction) {
        AlarmTriggeringAction alarmAction = new AlarmTriggeringAction();
        alarmAction.setYellow2Red(true);
        alarmAction.setAction(methodAction);
        return alarmAction;
    }

    /**
     * Creates the alarm spec.
     *
     * @param action     the action
     * @param expression the expression
     * @return the alarm spec object
     * @throws Exception the exception
     */
    AlarmSpec createAlarmSpec(AlarmAction action,
                              AlarmExpression expression) {
        AlarmSpec spec = new AlarmSpec();
        spec.setAction(action);
        spec.setExpression(expression);
        spec.setName(alarm);
        spec.setDescription("Monitor VM state and send email if VM power's off");
        spec.setEnabled(true);
        return spec;
    }

    /**
     * Creates the alarm.
     *
     * @param alarmSpec the alarm spec object
     * @throws Exception the exception
     */
    void createAlarm(AlarmSpec alarmSpec) throws DuplicateNameFaultMsg, RuntimeFaultFaultMsg, InvalidNameFaultMsg {
        ManagedObjectReference alarmmor =
                vimPort.createAlarm(alarmManager, vmMor, alarmSpec);
        System.out.println("Successfully created Alarm: " + alarmmor.getValue());
    }

    @Action
    public void run() throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg, DuplicateNameFaultMsg, InvalidNameFaultMsg {
        propCollectorRef = serviceContent.getPropertyCollector();
        alarmManager = serviceContent.getAlarmManager();

        vmMor = getVmByVMname(vmname);
        if (vmMor != null) {
            StateAlarmExpression expression = createStateAlarmExpression();
            MethodAction methodAction = createPowerOnAction();
            AlarmAction alarmAction = createAlarmTriggerAction(methodAction);
            AlarmSpec alarmSpec = createAlarmSpec(alarmAction, expression);
            createAlarm(alarmSpec);
        } else {
            System.out.println("Virtual Machine " + vmname + " Not Found");
        }
    }

}
