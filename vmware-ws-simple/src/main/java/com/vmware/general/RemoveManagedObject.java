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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <pre>
 * This sample demonstrates Destroy or Unregister
 * Managed Inventory Object like a Host, VM, Folder, etc
 *
 * <b>Parameters:</b>
 * url          [required] : url of the web service
 * username     [required] : username for the authentication
 * password     [required] : password for the authentication
 * objtype      [required] : type of managedobject to remove or unregister");
 *                           e.g. HostSystem, Datacenter, ResourcePool, Folder
 * objname      [required] : Name of the object
 * operation    [optional] : Name of the operation - [remove | unregister]
 *
 * <b>Command Line:</b>
 * Remove a folder named Fold
 * run.bat com.vmware.general.RemoveManagedObject --url [webserviceurl]
 * --username [username] --password  [password]
 * --objtype Folder --objname  Fold
 *
 * Unregister a virtual machine named VM1
 * run.bat com.vmware.general.RemoveManagedObject
 * --url [webserviceurl] --username [username] --password  [password]
 * --objtype VirtualMachine --objname VM1 --operation unregister
 * </pre>
 */

@Sample(name = "remove-managed-object", description = "demonstrates Destroy or Unregister Managed Inventory Object like a Host, VM, Folder, etc")
public class RemoveManagedObject extends ConnectedVimServiceBase {

    public final String SVC_INST_NAME = "ServiceInstance";
    public final static String[] OBJECT_TYPES = {
            "HostSystem","VirtualMachine","Folder","ResourcePool","Datacenter"
    };
    private ManagedObjectReference propCollectorRef;

    String objectname;
    String objecttype;
    String operation;

    @Option(
            name = "objtype",
            description = "type of managedobject to remove or unregister  " +
                    "e.g. HostSystem, VirtualMachine, Folder, ResourcePool, Datacenter"
    )
    public void setObjecttype(String objecttype) {
        this.objecttype = objecttype;
    }

    @Option(name = "objname", description = "Name of the object")
    public void setObjectname(String objectname) {
        this.objectname = objectname;
    }

    @Option(name = "operation", required = false, description = "Name of the operation - [remove | unregister]")
    public void setOperation(String operation) {
        this.operation = operation;
    }

    public boolean validateObjectType(final String type) {
        boolean found = false;

        for(String name : OBJECT_TYPES) {
            found |= name.equalsIgnoreCase(type);
        }

        return found;
    }

    public boolean validateTheInput() {
        if (operation != null) {
            if (!(operation.equalsIgnoreCase("remove"))
                    && (!(operation.equalsIgnoreCase("unregister")))) {
                throw new IllegalArgumentException("Invalid Operation type");
            }
        }


        if (!validateObjectType(objecttype)) {
            final StringBuilder list = new StringBuilder();
            for(final String name : OBJECT_TYPES) {
                list.append("'");
                list.append(name);
                list.append("' ");
            }
            throw new IllegalArgumentException(
                    String.format(
                            "Invalid --objtype %s! Object Type should be one of: %s",
                            objecttype,list.toString()
                    )
            );
        }
        return true;
    }

    /**
     * @return An array of SelectionSpec covering all the entities that provide
     *         performance statistics. The entities that provide performance
     *         statistics are VM, Host, Resource pool, Cluster Compute Resource
     *         and Datastore.
     */
    public SelectionSpec[] buildFullTraversal() {
        // Terminal traversal specs

        // RP -> VM
        TraversalSpec rpToVm = new TraversalSpec();
        rpToVm.setName("rpToVm");
        rpToVm.setType("ResourcePool");
        rpToVm.setPath("vm");
        rpToVm.setSkip(Boolean.FALSE);

        // vApp -> VM
        TraversalSpec vAppToVM = new TraversalSpec();
        vAppToVM.setName("vAppToVM");
        vAppToVM.setType("VirtualApp");
        vAppToVM.setPath("vm");

        // HostSystem -> VM
        TraversalSpec hToVm = new TraversalSpec();
        hToVm.setType("HostSystem");
        hToVm.setPath("vm");
        hToVm.setName("HToVm");
        hToVm.setSkip(Boolean.FALSE);

        // DC -> DS
        TraversalSpec dcToDs = new TraversalSpec();
        dcToDs.setType("Datacenter");
        dcToDs.setPath("datastore");
        dcToDs.setName("dcToDs");
        dcToDs.setSkip(Boolean.FALSE);

        // For RP -> RP recursion
        SelectionSpec rpToRpSpec = new SelectionSpec();
        rpToRpSpec.setName("rpToRp");

        // Recurse through all ResourcePools
        TraversalSpec rpToRp = new TraversalSpec();
        rpToRp.setType("ResourcePool");
        rpToRp.setPath("resourcePool");
        rpToRp.setSkip(Boolean.FALSE);
        rpToRp.setName("rpToRp");
        SelectionSpec[] sspecs = new SelectionSpec[]{rpToRpSpec};
        rpToRp.getSelectSet().addAll(Arrays.asList(sspecs));

        TraversalSpec crToRp = new TraversalSpec();
        crToRp.setType("ComputeResource");
        crToRp.setPath("resourcePool");
        crToRp.setSkip(Boolean.FALSE);
        crToRp.setName("crToRp");
        SelectionSpec[] sspecarrayrptorprtptovm = new SelectionSpec[]{rpToRp};
        crToRp.getSelectSet().addAll(Arrays.asList(sspecarrayrptorprtptovm));

        TraversalSpec crToH = new TraversalSpec();
        crToH.setSkip(Boolean.FALSE);
        crToH.setType("ComputeResource");
        crToH.setPath("host");
        crToH.setName("crToH");
        crToH.getSelectSet().add(hToVm);

        // For Folder -> Folder recursion
        SelectionSpec sspecvfolders = new SelectionSpec();
        sspecvfolders.setName("VisitFolders");

        TraversalSpec dcToHf = new TraversalSpec();
        dcToHf.setSkip(Boolean.FALSE);
        dcToHf.setType("Datacenter");
        dcToHf.setPath("hostFolder");
        dcToHf.setName("dcToHf");
        dcToHf.getSelectSet().add(sspecvfolders);

        TraversalSpec vAppToRp = new TraversalSpec();
        vAppToRp.setName("vAppToRp");
        vAppToRp.setType("VirtualApp");
        vAppToRp.setPath("resourcePool");
        SelectionSpec[] vAppToVMSS = new SelectionSpec[]{rpToRpSpec};
        vAppToRp.getSelectSet().addAll(Arrays.asList(vAppToVMSS));

        TraversalSpec dcToVmf = new TraversalSpec();
        dcToVmf.setType("Datacenter");
        dcToVmf.setSkip(Boolean.FALSE);
        dcToVmf.setPath("vmFolder");
        dcToVmf.setName("dcToVmf");
        dcToVmf.getSelectSet().add(sspecvfolders);

        TraversalSpec visitFolders = new TraversalSpec();
        visitFolders.setType("Folder");
        visitFolders.setPath("childEntity");
        visitFolders.setSkip(Boolean.FALSE);
        visitFolders.setName("VisitFolders");
        List<SelectionSpec> sspecarrvf = new ArrayList<SelectionSpec>();
        sspecarrvf.add(crToRp);
        sspecarrvf.add(crToH);
        sspecarrvf.add(dcToVmf);
        sspecarrvf.add(dcToHf);
        sspecarrvf.add(vAppToRp);
        sspecarrvf.add(vAppToVM);
        sspecarrvf.add(dcToDs);
        sspecarrvf.add(rpToVm);
        sspecarrvf.add(sspecvfolders);
        visitFolders.getSelectSet().addAll(sspecarrvf);
        return new SelectionSpec[]{visitFolders};
    }

    /**
     * Uses the new RetrievePropertiesEx method to emulate the now deprecated
     * RetrieveProperties method
     *
     * @param listpfs
     * @return list of object content
     * @throws Exception
     */
    public List<ObjectContent> retrievePropertiesAllObjects(
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

    public String getEntityName(ManagedObjectReference obj,
                                String entityType) throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        String retVal = null;
        // Create Property Spec
        PropertySpec propertySpec = new PropertySpec();
        propertySpec.setAll(Boolean.FALSE);
        propertySpec.getPathSet().add("name");
        propertySpec.setType(entityType);

        // Now create Object Spec
        ObjectSpec objectSpec = new ObjectSpec();
        objectSpec.setObj(obj);

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
                List<DynamicProperty> dps = oc.getPropSet();
                if (dps != null) {
                    for (DynamicProperty dp : dps) {
                        retVal = (String) dp.getVal();
                        return retVal;
                    }
                }
            }
        }
        return retVal;
    }

    /**
     * Getting the MOREF of the entity.
     */
    public ManagedObjectReference getEntityByName(String entityName,
                                                  String entityType) throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        ManagedObjectReference retVal = null;
        ManagedObjectReference rootFolder = serviceContent.getRootFolder();

        // Create Property Spec
        PropertySpec propertySpec = new PropertySpec();
        propertySpec.setAll(Boolean.FALSE);
        propertySpec.setType(entityType);
        propertySpec.getPathSet().add("name");

        // Now create Object Spec
        ObjectSpec objectSpec = new ObjectSpec();
        objectSpec.setObj(rootFolder);
        objectSpec.setSkip(Boolean.TRUE);
        objectSpec.getSelectSet().addAll(Arrays.asList(buildFullTraversal()));

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
                if (getEntityName(oc.getObj(), entityType).equals(entityName)) {
                    retVal = oc.getObj();
                    break;
                }
            }
        }
        return retVal;
    }

    public void deleteManagedObjectReference() throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg, VimFaultFaultMsg, InvalidCollectorVersionFaultMsg, TaskInProgressFaultMsg, InvalidPowerStateFaultMsg {
        ManagedObjectReference objmor =
                getEntityByName(objectname, objecttype);

        if (objmor != null) {
            if ("remove".equals(operation)) {
                ManagedObjectReference taskmor = vimPort.destroyTask(objmor);
                String[] opts = new String[]{"info.state", "info.error"};
                String[] opt = new String[]{"state"};

                Object[] result =
                        waitForValues.wait(taskmor, opts, opt,
                                new Object[][]{new Object[]{
                                        TaskInfoState.SUCCESS, TaskInfoState.ERROR}});
                if (result[0].equals(TaskInfoState.SUCCESS)) {
                    System.out.printf("Success Managed Entity - [ %s ]"
                            + " deleted %n", objectname);
                } else {
                    System.out.printf("Failure Deletion of Managed Entity - "
                            + "[ %s ] %n", objectname);
                }
            } else if ("VirtualMachine".equalsIgnoreCase(objecttype)) {
                vimPort.unregisterVM(objmor);
            } else {
                throw new IllegalArgumentException("Invalid Operation specified.");
            }
            System.out.println("Successfully completed " + operation + " for "
                    + objecttype + " : " + objectname);
        } else {
            System.out.println("Unable to find object of type  " + objecttype
                    + " with name  " + objectname);
            System.out.println(" : Failed " + operation + " of " + objecttype
                    + " : " + objectname);
        }
    }

    @Action
    public void run() throws RuntimeFaultFaultMsg, TaskInProgressFaultMsg, InvalidPropertyFaultMsg, VimFaultFaultMsg, InvalidCollectorVersionFaultMsg, InvalidPowerStateFaultMsg {
        validateTheInput();

        propCollectorRef = serviceContent.getPropertyCollector();

        if ((operation == null || operation.length() == 0)
                && (objecttype.equalsIgnoreCase("VirtualMachine"))) {
            operation = "unregisterVM";
        } else if ((operation == null || operation.length() == 0)
                && !(objecttype.equalsIgnoreCase("VirtualMachine"))) {
            operation = "remove";
        } else {
            if (!("remove".equals(operation))
                    && !("unregisterVM".equals(operation))) {
                operation = "unregisterVM";
            }
        }
        deleteManagedObjectReference();
    }
}
