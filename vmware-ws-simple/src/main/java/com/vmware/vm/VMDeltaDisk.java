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

import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 * VMDeltaDisk
 *
 * This sample creates a delta disk on top of an existing virtual disk in a VM,
 * and simultaneously removes the original disk using the reconfigure API.
 *
 * <b>Parameters:</b>
 * url            [required] : url of the web service
 * username       [required] : username for the authentication
 * password       [required] : password for the authentication
 * vmname         [required] : Name of the virtual machine
 * devicename     [required] : Name of the new delta disk
 * diskname       [required] : Name of the disk
 *
 * <b>Command Line:</b>
 * run.bat com.vmware.vm.VMDeltaDisk --url [webserviceurl]
 * --username [username] --password [password]
 * --vmname [myVM] --devicename [myDeltaDisk]  --diskname [dname1]
 * </pre>
 */

@Sample(
        name = "vm-delta-disk",
        description =
                "This sample creates a delta disk on top of an existing virtual disk in a VM,\n" +
                        "and simultaneously removes the original disk using the reconfigure API.\n"
)
public class VMDeltaDisk extends ConnectedVimServiceBase {
    private ManagedObjectReference rootFolderRef;
    private ManagedObjectReference propCollectorRef;

    String vmName = null;
    String device = null;
    String diskName = null;

    @Option(name = "vmname", description = "Name of the virtual machine")
    public void setVmName(String vmName) {
        this.vmName = vmName;
    }

    @Option(name = "devicename", description = "Name of the new delta disk")
    public void setDevice(String device) {
        this.device = device;
    }

    @Option(name = "diskname", description = "Name of the disk")
    public void setDiskName(String diskName) {
        this.diskName = diskName;
    }

    /**
     * Uses the new RetrievePropertiesEx method to emulate the now deprecated
     * RetrieveProperties method.
     *
     * @param listpfs
     * @return list of object content
     * @throws Exception
     */
    List<ObjectContent> retrievePropertiesAllObjects(
            List<PropertyFilterSpec> listpfs) {

        RetrieveOptions propObjectRetrieveOpts = new RetrieveOptions();

        List<ObjectContent> listobjcontent = new ArrayList<ObjectContent>();

        try {
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
        } catch (Exception e) {
            System.out.println(" : Failed Getting Contents");
            e.printStackTrace();
        }

        return listobjcontent;
    }

    void createDeltaDisk() throws IllegalArgumentException {
        ManagedObjectReference vmMOR = getVMByVMname(vmName);
        String dsName = null;
        VirtualHardware hw = new VirtualHardware();
        ArrayList<PropertyFilterSpec> listpfs =
                new ArrayList<PropertyFilterSpec>();
        listpfs.add(createPropertyFilterSpec(vmMOR, "config.hardware"));
        List<ObjectContent> listobjcont = retrievePropertiesAllObjects(listpfs);
        if (listobjcont != null) {
            for (ObjectContent oc : listobjcont) {
                List<DynamicProperty> dps = oc.getPropSet();
                if (dps != null) {
                    for (DynamicProperty dp : dps) {
                        hw = (VirtualHardware) dp.getVal();
                    }
                }
            }
        }
        if (vmMOR != null) {
            VirtualDisk vDisk = findVirtualDisk(vmMOR, diskName, hw);
            if (vDisk != null) {
                VirtualMachineConfigSpec configSpec =
                        new VirtualMachineConfigSpec();
                VirtualDeviceConfigSpec deviceSpec = new VirtualDeviceConfigSpec();

                deviceSpec.setOperation(VirtualDeviceConfigSpecOperation.ADD);
                deviceSpec
                        .setFileOperation(VirtualDeviceConfigSpecFileOperation.CREATE);

                VirtualDisk newDisk = new VirtualDisk();

                newDisk.setCapacityInKB(vDisk.getCapacityInKB());
                if (vDisk.getShares() != null) {
                    newDisk.setShares(vDisk.getShares());
                }
                if (vDisk.getConnectable() != null) {
                    newDisk.setConnectable(vDisk.getConnectable());
                }
                if (vDisk.getControllerKey() != null) {
                    newDisk.setControllerKey(vDisk.getControllerKey());
                }
                VirtualDeviceFileBackingInfo fBacking =
                        (VirtualDeviceFileBackingInfo) vDisk.getBacking();
                ArrayList<PropertyFilterSpec> deviceList =
                        new ArrayList<PropertyFilterSpec>();
                deviceList.add(createPropertyFilterSpec(fBacking.getDatastore(),
                        "summary.name"));
                List<ObjectContent> listdevobjcont =
                        retrievePropertiesAllObjects(deviceList);
                if (listdevobjcont != null) {
                    for (ObjectContent oc : listdevobjcont) {
                        List<DynamicProperty> dps = oc.getPropSet();
                        if (dps != null) {
                            for (DynamicProperty dp : dps) {
                                dsName = (String) dp.getVal();
                            }
                        }
                    }
                }
                newDisk.setUnitNumber(vDisk.getUnitNumber());
                newDisk.setKey(vDisk.getKey());
                if (vDisk.getBacking() instanceof VirtualDiskFlatVer1BackingInfo) {
                    VirtualDiskFlatVer1BackingInfo temp =
                            new VirtualDiskFlatVer1BackingInfo();
                    temp.setDiskMode(((VirtualDiskFlatVer1BackingInfo) vDisk
                            .getBacking()).getDiskMode());
                    temp.setFileName("[" + dsName + "] " + vmName + "/" + device
                            + ".vmdk");
                    temp.setParent((VirtualDiskFlatVer1BackingInfo) vDisk
                            .getBacking());
                    newDisk.setBacking(temp);
                } else if (vDisk.getBacking() instanceof VirtualDiskFlatVer2BackingInfo) {
                    VirtualDiskFlatVer2BackingInfo temp =
                            new VirtualDiskFlatVer2BackingInfo();
                    temp.setDiskMode(((VirtualDiskFlatVer2BackingInfo) vDisk
                            .getBacking()).getDiskMode());
                    temp.setFileName("[" + dsName + "] " + vmName + "/" + device
                            + ".vmdk");
                    temp.setParent((VirtualDiskFlatVer2BackingInfo) vDisk
                            .getBacking());
                    newDisk.setBacking(temp);
                } else if (vDisk.getBacking() instanceof VirtualDiskRawDiskMappingVer1BackingInfo) {
                    VirtualDiskRawDiskMappingVer1BackingInfo temp =
                            new VirtualDiskRawDiskMappingVer1BackingInfo();
                    temp.setDiskMode(((VirtualDiskRawDiskMappingVer1BackingInfo) vDisk
                            .getBacking()).getDiskMode());
                    temp.setFileName("[" + dsName + "] " + vmName + "/" + device
                            + ".vmdk");
                    temp.setParent((VirtualDiskRawDiskMappingVer1BackingInfo) vDisk
                            .getBacking());
                    newDisk.setBacking(temp);
                } else if (vDisk.getBacking() instanceof VirtualDiskSparseVer1BackingInfo) {
                    VirtualDiskSparseVer1BackingInfo temp =
                            new VirtualDiskSparseVer1BackingInfo();
                    temp.setDiskMode(((VirtualDiskSparseVer1BackingInfo) vDisk
                            .getBacking()).getDiskMode());
                    temp.setFileName("[" + dsName + "] " + vmName + "/" + device
                            + ".vmdk");
                    temp.setParent((VirtualDiskSparseVer1BackingInfo) vDisk
                            .getBacking());
                    newDisk.setBacking(temp);
                } else if (vDisk.getBacking() instanceof VirtualDiskSparseVer2BackingInfo) {
                    VirtualDiskSparseVer2BackingInfo temp =
                            new VirtualDiskSparseVer2BackingInfo();
                    temp.setDiskMode(((VirtualDiskSparseVer2BackingInfo) vDisk
                            .getBacking()).getDiskMode());
                    temp.setFileName("[" + dsName + "] " + vmName + "/" + device
                            + ".vmdk");
                    temp.setParent((VirtualDiskSparseVer2BackingInfo) vDisk
                            .getBacking());
                    newDisk.setBacking(temp);
                }
                deviceSpec.setDevice(newDisk);
                VirtualDeviceConfigSpec removeDeviceSpec =
                        new VirtualDeviceConfigSpec();
                removeDeviceSpec
                        .setOperation(VirtualDeviceConfigSpecOperation.REMOVE);
                removeDeviceSpec.setDevice(vDisk);
                List<VirtualDeviceConfigSpec> vdList =
                        new ArrayList<VirtualDeviceConfigSpec>();
                vdList.add(removeDeviceSpec);
                vdList.add(deviceSpec);
                configSpec.getDeviceChange().addAll(vdList);
                try {
                    ManagedObjectReference taskMOR =
                            vimPort.reconfigVMTask(vmMOR, configSpec);
                    if (getTaskResultAfterDone(taskMOR)) {
                        System.out.println("Delta Disk Created successfully.");
                    } else {
                        System.out.println("Failure -: Delta Disk "
                                + "cannot be created");
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                System.out.println("Virtual Disk " + diskName + " not found");
            }
        } else {
            System.out.println("Virtual Machine " + vmName + " doesn't exist");
        }
    }

    VirtualDisk findVirtualDisk(ManagedObjectReference vmMOR,
                                String diskname, VirtualHardware hw) {
        VirtualDisk ret = null;
        List<VirtualDevice> deviceArray = hw.getDevice();
        for (VirtualDevice virtualDevice : deviceArray) {
            if (virtualDevice instanceof VirtualDisk) {
                if (diskname.equalsIgnoreCase(virtualDevice.getDeviceInfo()
                        .getLabel())) {
                    ret = (VirtualDisk) virtualDevice;
                    break;
                }
            }
        }
        return ret;
    }

    PropertyFilterSpec createPropertyFilterSpec(
            ManagedObjectReference ref, String property) {
        PropertySpec propSpec = new PropertySpec();
        propSpec.setAll(new Boolean(false));
        propSpec.getPathSet().add(property);
        propSpec.setType(ref.getType());

        ObjectSpec objSpec = new ObjectSpec();
        objSpec.setObj(ref);
        objSpec.setSkip(new Boolean(false));

        PropertyFilterSpec spec = new PropertyFilterSpec();
        spec.getPropSet().add(propSpec);
        spec.getObjectSet().add(objSpec);
        return spec;
    }

    /**
     * Gets the VM TraversalSpec.
     *
     * @return the VM TraversalSpec
     */
    TraversalSpec getVMTraversalSpec() {
        TraversalSpec vAppToVM = new TraversalSpec();
        vAppToVM.setName("vAppToVM");
        vAppToVM.setType("VirtualApp");
        vAppToVM.setPath("vm");

        TraversalSpec vAppToVApp = new TraversalSpec();
        vAppToVApp.setName("vAppToVApp");
        vAppToVApp.setType("VirtualApp");
        vAppToVApp.setPath("resourcePool");

        SelectionSpec vAppRecursion = new SelectionSpec();
        vAppRecursion.setName("vAppToVApp");

        SelectionSpec vmInVApp = new SelectionSpec();
        vmInVApp.setName("vAppToVM");

        List<SelectionSpec> vAppToVMSS = new ArrayList<SelectionSpec>();
        vAppToVMSS.add(vAppRecursion);
        vAppToVMSS.add(vmInVApp);
        vAppToVApp.getSelectSet().addAll(vAppToVMSS);

        SelectionSpec sSpec = new SelectionSpec();
        sSpec.setName("VisitFolders");

        TraversalSpec dataCenterToVMFolder = new TraversalSpec();
        dataCenterToVMFolder.setName("DataCenterToVMFolder");
        dataCenterToVMFolder.setType("Datacenter");
        dataCenterToVMFolder.setPath("vmFolder");
        dataCenterToVMFolder.setSkip(false);
        dataCenterToVMFolder.getSelectSet().add(sSpec);

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
     * Gets VM by Name.
     *
     * @param vmname the VMName
     * @return ManagedObjectReference of the VM
     */
    ManagedObjectReference getVMByVMname(String vmname)
            throws IllegalArgumentException {
        ManagedObjectReference retVmRef = null;
        TraversalSpec tSpec = getVMTraversalSpec();

        PropertySpec propertySpec = new PropertySpec();
        propertySpec.setAll(Boolean.FALSE);
        propertySpec.getPathSet().add("name");
        propertySpec.setType("VirtualMachine");

        ObjectSpec objectSpec = new ObjectSpec();
        objectSpec.setObj(rootFolderRef);
        objectSpec.setSkip(Boolean.TRUE);
        objectSpec.getSelectSet().add(tSpec);

        PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
        propertyFilterSpec.getPropSet().add(propertySpec);
        propertyFilterSpec.getObjectSet().add(objectSpec);

        List<PropertyFilterSpec> listpfs = new ArrayList<PropertyFilterSpec>(1);
        listpfs.add(propertyFilterSpec);
        List<ObjectContent> listobjcont = retrievePropertiesAllObjects(listpfs);

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
                if (vmnm != null && vmnm.equals(vmname)) {
                    retVmRef = mr;
                    break;
                }
            }
        }
        if (retVmRef == null) {
            throw new IllegalArgumentException("VM not found.");
        }
        return retVmRef;
    }

    @Action
    public void run() {
        propCollectorRef = serviceContent.getPropertyCollector();
        rootFolderRef = serviceContent.getRootFolder();
        createDeltaDisk();
    }

}
