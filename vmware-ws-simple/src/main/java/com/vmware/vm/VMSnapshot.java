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
 * VMSnapshot
 *
 * This sample demonstrates VM snapshot operations
 *
 * <b>Parameters:</b>
 * url            [required] : url of the web service.
 * username       [required] : username for the authentication
 * password       [required] : password for the authentication
 * vmname         [required] : Name of the virtual machine
 * operation      [required] : operation type - [list|create|remove|revert]
 * snapshotname   [optional] : Name of the snapshot
 * description    [optional] : description of the sanpshot
 * removechild    [optional] : remove snapshot children - [1 | 0]
 *
 * <b>Command Line:</b>
 * List VM snapshot names
 * run.bat com.vmware.vm.VMSnapshot
 * --url [webserviceurl] --username [username] --password  [password]
 * --vmname [vmname] --operation list
 *
 * Create VM snapshot
 * run.bat com.vmware.vm.VMSnapshot
 * --url [webserviceurl] --username [username] --password  [password]
 * --vmname [vmname] --operation create
 * --description [Description of the snapshot]
 *
 * Revert VM snapshot
 * run.bat com.vmware.vm.VMSnapshot
 * --url [webserviceurl] --username [username] --password  [password]
 * --vmname [vmname] --operation revert --description [Snapshot Description]
 *
 * Remove VM snapshot
 * run.bat com.vmware.vm.VMSnapshot
 * --url [webserviceurl] --username [username] --password  [password]
 * --vmname [vmname] --operation remove --removechild 0
 * </pre>
 */

@Sample(
        name = "vm-snapshot",
        description = "This sample demonstrates VM snapshot operations"
)
public class VMSnapshot extends ConnectedVimServiceBase {

    String virtualMachineName;
    String operation;
    String snapshotname;
    String description;
    String removechild;

    @Option(name = "vmname", description = "Name of the virtual machine")
    public void setVirtualMachineName(String virtualMachineName) {
        this.virtualMachineName = virtualMachineName;
    }

    @Option(name = "operation", description = "operation type - [list|create|remove|revert]")
    public void setOperation(String operation) {
        this.operation = operation;
    }

    @Option(name = "snapshotname", required = false, description = "Name of the snapshot")
    public void setSnapshotname(String snapshotname) {
        this.snapshotname = snapshotname;
    }

    @Option(name = "description", required = false, description = "description of the sanpshot")
    public void setDescription(String description) {
        this.description = description;
    }

    @Option(name = "removechild", required = false, description = "remove snapshot children - [1 | 0]")
    public void setRemovechild(String removechild) {
        this.removechild = removechild;
    }


    boolean createSnapshot(ManagedObjectReference vmMor)
            throws FileFaultFaultMsg, InvalidNameFaultMsg, InvalidStateFaultMsg,
            RuntimeFaultFaultMsg, SnapshotFaultFaultMsg, TaskInProgressFaultMsg,
            VmConfigFaultFaultMsg, InvalidPropertyFaultMsg,
            InvalidCollectorVersionFaultMsg {
        ManagedObjectReference taskMor =
                vimPort.createSnapshotTask(vmMor, snapshotname, description, false,
                        false);
        if (getTaskResultAfterDone(taskMor)) {
            System.out.printf(" Creating Snapshot - [ %s ] Successful %n",
                    snapshotname);
            return true;
        } else {
            System.out.printf(" Creating Snapshot - [ %s ] Failure %n",
                    snapshotname);
            return false;
        }
    }

    boolean listSnapshot(ManagedObjectReference vmMor)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
        VirtualMachineSnapshotInfo snapInfo =
                (VirtualMachineSnapshotInfo) getMOREFs.entityProps(vmMor,
                        new String[]{"snapshot"}).get("snapshot");
        if (snapInfo == null) {
            System.out.println("No Snapshots found");
        } else {
            List<VirtualMachineSnapshotTree> listvmsht =
                    snapInfo.getRootSnapshotList();
            traverseSnapshotInTree(listvmsht, null, true);
        }
        return true;
    }

    boolean revertSnapshot(ManagedObjectReference vmMor) throws RuntimeFaultFaultMsg, TaskInProgressFaultMsg, VmConfigFaultFaultMsg, InsufficientResourcesFaultFaultMsg, FileFaultFaultMsg, InvalidStateFaultMsg, InvalidPropertyFaultMsg, InvalidCollectorVersionFaultMsg {
        ManagedObjectReference snapmor =
                getSnapshotReference(vmMor, virtualMachineName, snapshotname);
        if (snapmor != null) {
            ManagedObjectReference taskMor =
                    vimPort.revertToSnapshotTask(snapmor, null, true);
            if (getTaskResultAfterDone(taskMor)) {
                System.out.printf(" Reverting Snapshot - [ %s ] Successful %n",
                        snapshotname);
                return true;
            } else {
                System.out.printf(" Reverting Snapshot - [ %s ] Failure %n",
                        snapshotname);
                return false;
            }
        } else {
            System.out.println("Snapshot not found");
        }
        return false;
    }

    boolean removeAllSnapshot(ManagedObjectReference vmMor) throws RuntimeFaultFaultMsg, TaskInProgressFaultMsg, SnapshotFaultFaultMsg, InvalidStateFaultMsg, InvalidPropertyFaultMsg, InvalidCollectorVersionFaultMsg {
        ManagedObjectReference taskMor =
                vimPort.removeAllSnapshotsTask(vmMor, true);
        if (taskMor != null) {
            String[] opts =
                    new String[]{"info.state", "info.error", "info.progress"};
            String[] opt = new String[]{"state"};
            Object[] results =
                    waitForValues.wait(taskMor, opts, opt, new Object[][]{new Object[]{
                            TaskInfoState.SUCCESS, TaskInfoState.ERROR}});

            // Wait till the task completes.
            if (results[0].equals(TaskInfoState.SUCCESS)) {
                System.out.printf(
                        " Removing All Snapshots on - [ %s ] Successful %n",
                        virtualMachineName);
                return true;
            } else {
                System.out.printf(" Removing All Snapshots on - [ %s ] Failure %n",
                        virtualMachineName);
                return false;
            }
        } else {
            return false;
        }
    }

    boolean removeSnapshot(ManagedObjectReference vmMor) throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg, InvalidCollectorVersionFaultMsg, TaskInProgressFaultMsg {
        int rem = Integer.parseInt(removechild);
        boolean flag = true;
        if (rem == 0) {
            flag = false;
        }
        ManagedObjectReference snapmor =
                getSnapshotReference(vmMor, virtualMachineName, snapshotname);
        if (snapmor != null) {
            ManagedObjectReference taskMor =
                    vimPort.removeSnapshotTask(snapmor, flag, true);
            if (taskMor != null) {
                String[] opts =
                        new String[]{"info.state", "info.error", "info.progress"};
                String[] opt = new String[]{"state"};
                Object[] results =
                        waitForValues.wait(taskMor, opts, opt,
                                new Object[][]{new Object[]{TaskInfoState.SUCCESS,
                                        TaskInfoState.ERROR}});

                // Wait till the task completes.
                if (results[0].equals(TaskInfoState.SUCCESS)) {
                    System.out.printf(" Removing Snapshot - [ %s ] Successful %n",
                            snapshotname);
                    return true;
                } else {
                    System.out.printf(" Removing Snapshot - [ %s ] Failure %n",
                            snapshotname);
                    return false;
                }
            }
        } else {
            System.out.println("Snapshot not found");
        }
        return false;
    }

    ManagedObjectReference traverseSnapshotInTree(
            List<VirtualMachineSnapshotTree> snapTree, String findName,
            boolean print) {
        ManagedObjectReference snapmor = null;
        if (snapTree == null) {
            return snapmor;
        }
        for (VirtualMachineSnapshotTree node : snapTree) {
            if (print) {
                System.out.println("Snapshot Name : " + node.getName());
            }
            if (findName != null && node.getName().equalsIgnoreCase(findName)) {
                return node.getSnapshot();
            } else {
                List<VirtualMachineSnapshotTree> listvmst =
                        node.getChildSnapshotList();
                List<VirtualMachineSnapshotTree> childTree = listvmst;
                snapmor = traverseSnapshotInTree(childTree, findName, print);
            }
        }
        return snapmor;
    }

    ManagedObjectReference getSnapshotReference(
            ManagedObjectReference vmmor, String vmName, String snapName) throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        VirtualMachineSnapshotInfo snapInfo =
                (VirtualMachineSnapshotInfo) getMOREFs.entityProps(vmmor,
                        new String[]{"snapshot"}).get("snapshot");
        ManagedObjectReference snapmor = null;
        if (snapInfo != null) {
            List<VirtualMachineSnapshotTree> listvmst =
                    snapInfo.getRootSnapshotList();
            snapmor = traverseSnapshotInTree(listvmst, snapName, false);
            if (snapmor == null) {
                System.out.println("No Snapshot named : " + snapName
                        + " found for VirtualMachine : " + vmName);
            }
        } else {
            System.out
                    .println("No Snapshots found for VirtualMachine : " + vmName);
        }
        return snapmor;
    }

    boolean isOptionSet(String test) {
        return (test == null) ? false : true;
    }

    boolean verifyInputArguments() {
        boolean flag = true;
        String op = operation;
        if (op.equalsIgnoreCase("create")) {
            if ((!isOptionSet(snapshotname)) || (!isOptionSet(description))) {
                System.out.println("For Create operation SnapshotName"
                        + " and Description are the Mandatory options");
                flag = false;
            }
        }
        if (op.equalsIgnoreCase("remove")) {
            if ((!isOptionSet(snapshotname)) || (!isOptionSet(removechild))) {
                System.out.println("For Remove operation Snapshotname"
                        + " and removechild are the Mandatory option");
                flag = false;
            } else {
                int child = Integer.parseInt(removechild);
                if (child != 0 && child != 1) {
                    System.out.println("Value of removechild parameter"
                            + " must be either 0 or 1");
                    flag = false;
                }
            }
        }
        if (op.equalsIgnoreCase("revert")) {
            if ((!isOptionSet(snapshotname))) {
                System.out.println("For Revert operation SnapshotName"
                        + " is the Mandatory option");
                flag = false;
            }
        }
        return flag;
    }

    @Action
    public void run() throws InvalidPropertyFaultMsg, TaskInProgressFaultMsg, SnapshotFaultFaultMsg, VmConfigFaultFaultMsg, FileFaultFaultMsg, InvalidStateFaultMsg, InvalidCollectorVersionFaultMsg, InvalidNameFaultMsg, RuntimeFaultFaultMsg, InsufficientResourcesFaultFaultMsg {
        boolean valid = false;
        valid = verifyInputArguments();
        if (!valid) {
            return;
        }
        ManagedObjectReference vmRef =
                getMOREFsInContainerByType(serviceContent.getRootFolder(),
                        "VirtualMachine").get(virtualMachineName);

        if (vmRef != null) {
            boolean res = false;
            if (operation.equalsIgnoreCase("create")) {
                res = createSnapshot(vmRef);
            } else if (operation.equalsIgnoreCase("list")) {
                res = listSnapshot(vmRef);
            } else if (operation.equalsIgnoreCase("revert")) {
                res = revertSnapshot(vmRef);
            } else if (operation.equalsIgnoreCase("removeall")) {
                res = removeAllSnapshot(vmRef);
            } else if (operation.equalsIgnoreCase("remove")) {
                res = removeSnapshot(vmRef);
            } else {
                System.out.println("Invalid operation [create|list|"
                        + "revert|removeall|remove]");
            }
            if (res) {
                System.out.println("Operation " + operation
                        + "snapshot completed sucessfully");
            }
        } else {
            System.out.println("Virtual Machine " + virtualMachineName
                    + " not found.");
            return;
        }
    }
}
