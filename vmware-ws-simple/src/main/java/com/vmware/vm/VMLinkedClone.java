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

import java.util.*;

/**
 * <pre>
 * VMLinkedClone
 *
 * This sample creates a linked clone from an existing snapshot
 *
 * Each independent disk needs a DiskLocator with
 * diskmovetype as moveAllDiskBackingsAndDisallowSharing
 *
 * <b>Parameters:</b>
 * url             [required] : url of the web service
 * username        [required] : username for the authentication
 * password        [required] : password for the authentication
 * vmname          [required] : Name of the virtual machine
 * snapshotname    [required] : Name of the snaphot
 * clonename       [required] : Name of the cloneName
 *
 * <b>Command Line:</b>
 * Create a linked clone
 * run.bat com.vmware.vm.VMLinkedClone --url [webserviceurl]
 * --username [username] --password [password]  --vmname [myVM]
 * --snapshotname [snapshot name]  --clonename [clone name]
 * </pre>
 */
@Sample(
        name = "vm-linked-clone",
        description =
                "This sample creates a linked clone from an existing snapshot\n" +
                        "Each independent disk needs a DiskLocator with\n" +
                        "diskmovetype as moveAllDiskBackingsAndDisallowSharing\n"
)
public class VMLinkedClone extends ConnectedVimServiceBase {

    String cloneName;
    String virtualMachineName;
    String snapshotName;

    @Option(name = "vmname", description = "Name of the virtual machine")
    public void setVirtualMachineName(String virtualMachineName) {
        this.virtualMachineName = virtualMachineName;
    }

    @Option(name = "snapshotname", description = "Name of the snaphot")
    public void setSnapshotName(String snapshotName) {
        this.snapshotName = snapshotName;
    }

    @Option(name = "clonename", description = "Name of the cloneName")
    public void setCloneName(String cloneName) {
        this.cloneName = cloneName;
    }

    /**
     * Creates the linked clone.
     *
     * @throws Exception the exception
     */
    void createLinkedClone() throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg, CustomizationFaultFaultMsg, TaskInProgressFaultMsg, VmConfigFaultFaultMsg, InsufficientResourcesFaultFaultMsg, InvalidDatastoreFaultMsg, FileFaultFaultMsg, MigrationFaultFaultMsg, InvalidStateFaultMsg, InvalidCollectorVersionFaultMsg {
        ManagedObjectReference vmMOR =
                getMOREFsInContainerByType(serviceContent.getRootFolder(),
                        "VirtualMachine").get(virtualMachineName);

        if (vmMOR != null) {
            ManagedObjectReference snapMOR =
                    getSnapshotReference(vmMOR, snapshotName);
            if (snapMOR != null) {
                ArrayList<Integer> independentVirtualDiskKeys =
                        getIndependenetVirtualDiskKeys(vmMOR);

                VirtualMachineRelocateSpec rSpec = new VirtualMachineRelocateSpec();
                if (independentVirtualDiskKeys.size() > 0) {
                    List<ManagedObjectReference> ds =
                            ((ArrayOfManagedObjectReference) getMOREFs.entityProps(vmMOR,
                                    new String[]{"datastore"}).get("datastore"))
                                    .getManagedObjectReference();
                    List<VirtualMachineRelocateSpecDiskLocator> diskLocator =
                            new ArrayList<VirtualMachineRelocateSpecDiskLocator>();

                    for (Integer iDiskKey : independentVirtualDiskKeys) {
                        VirtualMachineRelocateSpecDiskLocator diskloc =
                                new VirtualMachineRelocateSpecDiskLocator();
                        diskloc.setDatastore(ds.get(0));
                        diskloc
                                .setDiskMoveType(VirtualMachineRelocateDiskMoveOptions.MOVE_ALL_DISK_BACKINGS_AND_DISALLOW_SHARING
                                        .value());
                        diskloc.setDiskId(iDiskKey);
                        diskLocator.add(diskloc);
                    }
                    rSpec.setDiskMoveType(VirtualMachineRelocateDiskMoveOptions.CREATE_NEW_CHILD_DISK_BACKING
                            .value());
                    rSpec.getDisk().addAll(diskLocator);
                } else {
                    rSpec.setDiskMoveType(VirtualMachineRelocateDiskMoveOptions.CREATE_NEW_CHILD_DISK_BACKING
                            .value());
                }
                VirtualMachineCloneSpec cloneSpec = new VirtualMachineCloneSpec();
                cloneSpec.setPowerOn(false);
                cloneSpec.setTemplate(false);
                cloneSpec.setLocation(rSpec);
                cloneSpec.setSnapshot(snapMOR);

                ManagedObjectReference parentMOR =
                        (ManagedObjectReference) getMOREFs.entityProps(vmMOR,
                                new String[]{"parent"}).get("parent");
                if (parentMOR == null) {
                    throw new RuntimeException(
                            "The selected VM is a part of vAPP. This sample only "
                                    + "works with virtual machines that are not a part "
                                    + "of any vAPP");
                }
                ManagedObjectReference cloneTask =
                        vimPort
                                .cloneVMTask(vmMOR, parentMOR, cloneName, cloneSpec);
                if (getTaskResultAfterDone(cloneTask)) {
                    System.out.println(" Cloning Successful");
                } else {
                    System.out.println(" Cloning Failure");
                }
            } else {
                System.out.println("Snapshot " + snapshotName + " doesn't exist");
            }
        } else {
            System.out.println("Virtual Machine " + virtualMachineName
                    + " doesn't exist");
        }
    }

    /**
     * Gets the independenet virtual disk keys.
     *
     * @param vmMOR the vm mor
     * @return the independent virtual disk keys
     * @throws RuntimeFaultFaultMsg
     * @throws InvalidPropertyFaultMsg
     */
    ArrayList<Integer> getIndependenetVirtualDiskKeys(
            ManagedObjectReference vmMOR) throws InvalidPropertyFaultMsg,
            RuntimeFaultFaultMsg {
        ArrayList<Integer> independenetVirtualDiskKeys = new ArrayList<Integer>();
        VirtualHardware hw =
                (VirtualHardware) getMOREFs.entityProps(vmMOR,
                        new String[]{"config.hardware"}).get("config.hardware");
        List<VirtualDevice> listvd = hw.getDevice();
        for (VirtualDevice vDisk : listvd) {
            if (vDisk instanceof VirtualDisk) {
                String diskMode = "";
                if (vDisk.getBacking() instanceof VirtualDiskFlatVer1BackingInfo) {
                    diskMode =
                            ((VirtualDiskFlatVer1BackingInfo) vDisk.getBacking())
                                    .getDiskMode();
                } else if (vDisk.getBacking() instanceof VirtualDiskFlatVer2BackingInfo) {
                    diskMode =
                            ((VirtualDiskFlatVer2BackingInfo) vDisk.getBacking())
                                    .getDiskMode();
                } else if (vDisk.getBacking() instanceof VirtualDiskRawDiskMappingVer1BackingInfo) {
                    diskMode =
                            ((VirtualDiskRawDiskMappingVer1BackingInfo) vDisk
                                    .getBacking()).getDiskMode();
                } else if (vDisk.getBacking() instanceof VirtualDiskSparseVer1BackingInfo) {
                    diskMode =
                            ((VirtualDiskSparseVer1BackingInfo) vDisk.getBacking())
                                    .getDiskMode();
                } else if (vDisk.getBacking() instanceof VirtualDiskSparseVer2BackingInfo) {
                    diskMode =
                            ((VirtualDiskSparseVer2BackingInfo) vDisk.getBacking())
                                    .getDiskMode();
                }
                if (diskMode.indexOf("independent") != -1) {
                    independenetVirtualDiskKeys.add(vDisk.getKey());
                }
            }
        }
        return independenetVirtualDiskKeys;
    }

    /**
     * Gets the snapshot reference.
     *
     * @param vmmor    the vmmor
     * @param snapName the snap name
     * @return the snapshot reference
     * @throws Exception the exception
     */
    ManagedObjectReference getSnapshotReference(
            ManagedObjectReference vmmor, String snapName) throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        VirtualMachineSnapshotInfo snapInfo =
                (VirtualMachineSnapshotInfo) getMOREFs.entityProps(vmmor,
                        new String[]{"snapshot"}).get("snapshot");
        ManagedObjectReference snapmor = null;
        if (snapInfo != null) {
            List<VirtualMachineSnapshotTree> listvmsst =
                    snapInfo.getRootSnapshotList();
            List<VirtualMachineSnapshotTree> snapTree = listvmsst;
            snapmor = traverseSnapshotInTree(snapTree, snapName);
        }
        return snapmor;
    }

    /**
     * Traverse snapshot in tree.
     *
     * @param snapTree the snap tree
     * @param findName the find name
     * @return the managed object reference
     */
    ManagedObjectReference traverseSnapshotInTree(
            List<VirtualMachineSnapshotTree> snapTree, String findName) {
        ManagedObjectReference snapmor = null;
        if (snapTree == null) {
            return snapmor;
        }
        for (int i = 0; i < snapTree.size() && snapmor == null; i++) {
            VirtualMachineSnapshotTree node = snapTree.get(i);
            if (findName != null && node.getName().equals(findName)) {
                snapmor = node.getSnapshot();
            } else {
                List<VirtualMachineSnapshotTree> childTree =
                        node.getChildSnapshotList();
                snapmor = traverseSnapshotInTree(childTree, findName);
            }
        }
        return snapmor;
    }

    @Action
    public void run() throws CustomizationFaultFaultMsg, RuntimeFaultFaultMsg, TaskInProgressFaultMsg, InsufficientResourcesFaultFaultMsg, VmConfigFaultFaultMsg, InvalidDatastoreFaultMsg, InvalidPropertyFaultMsg, FileFaultFaultMsg, InvalidStateFaultMsg, MigrationFaultFaultMsg, InvalidCollectorVersionFaultMsg {
        createLinkedClone();
    }
}
