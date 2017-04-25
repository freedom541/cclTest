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

package com.vmware.vm;

import com.vmware.common.annotations.Action;
import com.vmware.common.annotations.Option;
import com.vmware.common.annotations.Sample;
import com.vmware.connection.ConnectedVimServiceBase;
import com.vmware.vim25.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <pre>
 * VMClone
 *
 * This sample makes a template of an existing VM and
 * deploy multiple instances of this template onto a datacenter
 *
 * <b>Parameters:</b>
 * url             [required] : url of the web service
 * username        [required] : username for the authentication
 * password        [required] : password for the authentication
 * datacentername  [required] : name of Datacenter
 * vmpath          [required] : inventory path of the VM
 * clonename       [required] : name of the clone
 *
 * <b>Command Line:</b>
 * java com.vmware.vm.VMClone --url [webserviceurl]
 * --username [username] --password [password]
 * --datacentername [DatacenterName]"
 * --vmpath [vmPath] --clonename [CloneName]
 * </pre>
 */
@Sample(
        name = "vm-clone",
        description = "This sample makes a template of an " +
                "existing VM and deploy multiple instances of this " +
                "template onto a datacenter"
)
public class VMClone extends ConnectedVimServiceBase {
    private ManagedObjectReference propCollectorRef;

    private String dataCenterName;
    private String vmPathName;
    private String cloneName;

    @Option(name = "datacentername", description = "name of Datacenter")
    public void setDataCenterName(String dataCenterName) {
        this.dataCenterName = dataCenterName;
    }

    @Option(name = "vmpath", description = "inventory path of the VM")
    public void setVmPathName(String vmPathName) {
        this.vmPathName = vmPathName;
    }

    @Option(name = "clonename", description = "name of the clone")
    public void setCloneName(String cloneName) {
        this.cloneName = cloneName;
    }

    void cloneVM() throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg, InvalidCollectorVersionFaultMsg, CustomizationFaultFaultMsg, TaskInProgressFaultMsg, VmConfigFaultFaultMsg, InsufficientResourcesFaultFaultMsg, InvalidDatastoreFaultMsg, FileFaultFaultMsg, MigrationFaultFaultMsg, InvalidStateFaultMsg, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        // Find the Datacenter reference by using findByInventoryPath().
        ManagedObjectReference datacenterRef =
                vimPort.findByInventoryPath(serviceContent.getSearchIndex(),
                        dataCenterName);
        if (datacenterRef == null) {
            System.out.printf("The specified datacenter [ %s ]is not found %n",
                    dataCenterName);
            return;
        }
        // Find the virtual machine folder for this datacenter.
        ManagedObjectReference vmFolderRef =
                (ManagedObjectReference) getDynamicProperty(datacenterRef,
                        "vmFolder");
        if (vmFolderRef == null) {
            System.out.println("The virtual machine is not found");
            return;
        }
        ManagedObjectReference vmRef =
                vimPort.findByInventoryPath(serviceContent.getSearchIndex(),
                        vmPathName);
        if (vmRef == null) {
            System.out.printf("The VMPath specified [ %s ] is not found %n",
                    vmPathName);
            return;
        }
        VirtualMachineCloneSpec cloneSpec = new VirtualMachineCloneSpec();
        VirtualMachineRelocateSpec relocSpec = new VirtualMachineRelocateSpec();
        cloneSpec.setLocation(relocSpec);
        cloneSpec.setPowerOn(false);
        cloneSpec.setTemplate(false);

        System.out.printf("Cloning Virtual Machine [%s] to clone name [%s] %n",
                vmPathName.substring(vmPathName.lastIndexOf("/") + 1), cloneName);
        ManagedObjectReference cloneTask =
                vimPort.cloneVMTask(vmRef, vmFolderRef, cloneName, cloneSpec);

        if (getTaskResultAfterDone(cloneTask)) {
            System.out
                    .printf(
                            "Successfully cloned Virtual Machine [%s] to clone name [%s] %n",
                            vmPathName.substring(vmPathName.lastIndexOf("/") + 1),
                            cloneName);
        } else {
            System.out.printf(
                    "Failure Cloning Virtual Machine [%s] to clone name [%s] %n",
                    vmPathName.substring(vmPathName.lastIndexOf("/") + 1),
                    cloneName);
        }
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

    Object getDynamicProperty(ManagedObjectReference mor,
                              String propertyName) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        ObjectContent[] objContent =
                getObjectProperties(mor, new String[]{propertyName});

        Object propertyValue = null;
        if (objContent != null) {
            List<DynamicProperty> listdp = objContent[0].getPropSet();
            if (listdp != null) {
                /*
                * Check the dynamic property for ArrayOfXXX object
                */
                Object dynamicPropertyVal = listdp.get(0).getVal();
                String dynamicPropertyName =
                        dynamicPropertyVal.getClass().getName();
                if (dynamicPropertyName.indexOf("ArrayOf") != -1) {
                    String methodName =
                            dynamicPropertyName.substring(
                                    dynamicPropertyName.indexOf("ArrayOf")
                                            + "ArrayOf".length(),
                                    dynamicPropertyName.length());
                    /*
                    * If object is ArrayOfXXX object, then get the XXX[] by
                    * invoking getXXX() on the object.
                    * For Ex:
                    * ArrayOfManagedObjectReference.getManagedObjectReference()
                    * returns ManagedObjectReference[] array.
                    */
                    if (methodExists(dynamicPropertyVal, "get" + methodName, null)) {
                        methodName = "get" + methodName;
                    } else {
                        /*
                        * Construct methodName for ArrayOf primitive types
                        * Ex: For ArrayOfInt, methodName is get_int
                        */
                        methodName = "get_" + methodName.toLowerCase();
                    }
                    Method getMorMethod =
                            dynamicPropertyVal.getClass().getDeclaredMethod(
                                    methodName, (Class[]) null);
                    propertyValue =
                            getMorMethod.invoke(dynamicPropertyVal, (Object[]) null);
                } else if (dynamicPropertyVal.getClass().isArray()) {
                    /*
                    * Handle the case of an unwrapped array being deserialized.
                    */
                    propertyValue = dynamicPropertyVal;
                } else {
                    propertyValue = dynamicPropertyVal;
                }
            }
        }
        return propertyValue;
    }

    /**
     * Retrieve contents for a single object based on the property collector
     * registered with the service.
     *
     * @param mobj       Managed Object Reference to get contents for
     * @param properties names of properties of object to retrieve
     * @return retrieved object contents
     */
    ObjectContent[] getObjectProperties(
            ManagedObjectReference mobj, String[] properties) throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        if (mobj == null) {
            return null;
        }

        PropertyFilterSpec spec = new PropertyFilterSpec();
        spec.getPropSet().add(new PropertySpec());
        if ((properties == null || properties.length == 0)) {
            spec.getPropSet().get(0).setAll(Boolean.TRUE);
        } else {
            spec.getPropSet().get(0).setAll(Boolean.FALSE);
        }
        spec.getPropSet().get(0).setType(mobj.getType());
        spec.getPropSet().get(0).getPathSet().addAll(Arrays.asList(properties));
        spec.getObjectSet().add(new ObjectSpec());
        spec.getObjectSet().get(0).setObj(mobj);
        spec.getObjectSet().get(0).setSkip(Boolean.FALSE);
        List<PropertyFilterSpec> listpfs = new ArrayList<PropertyFilterSpec>(1);
        listpfs.add(spec);
        List<ObjectContent> listobjcont = retrievePropertiesAllObjects(listpfs);
        return listobjcont.toArray(new ObjectContent[listobjcont.size()]);
    }

    /**
     * Determines of a method 'methodName' exists for the Object 'obj'.
     *
     * @param obj            The Object to check
     * @param methodName     The method name
     * @param parameterTypes Array of Class objects for the parameter types
     * @return true if the method exists, false otherwise
     */
    @SuppressWarnings("rawtypes")
    boolean methodExists(Object obj, String methodName,
                         Class[] parameterTypes) throws NoSuchMethodException {
        boolean exists = false;
        Method method = obj.getClass().getMethod(methodName, parameterTypes);
        if (method != null) {
            exists = true;
        }
        return exists;
    }

    @Action
    public void run() throws RuntimeFaultFaultMsg, TaskInProgressFaultMsg, VmConfigFaultFaultMsg, InvalidDatastoreFaultMsg, FileFaultFaultMsg, NoSuchMethodException, MigrationFaultFaultMsg, InvalidStateFaultMsg, InvalidCollectorVersionFaultMsg, IllegalAccessException, CustomizationFaultFaultMsg, InsufficientResourcesFaultFaultMsg, InvocationTargetException, InvalidPropertyFaultMsg {
        propCollectorRef = serviceContent.getPropertyCollector();
        cloneVM();
    }
}
