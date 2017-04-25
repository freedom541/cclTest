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
 * VMPromoteDisks
 *
 * Used to consolidate a linked clone by using promote API.
 *
 * <b>Parameters:</b>
 * url              [required] : url of the web service
 * username         [required] : username for the authentication
 * password         [required] : password for the authentication
 * vmname           [required] : name of the virtual machine
 * unlink           [required] : True|False to unlink
 * devicenames      [optional] : disk name to unlink
 *
 * <b>Command Line:</b>
 * run.bat com.vmware.vm.VMPromoteDisks --url [URLString] --username [User] --password [Password]
 * --vmname [VMName] --unlink [True|False] --devicenames [dname1:dname2...]
 * </pre>
 */
@Sample(
        name = "vm-promote-disks",
        description = "Used to consolidate a linked clone by using promote API."
)
public class VMPromoteDisks extends ConnectedVimServiceBase {

    String vmName = null;
    Boolean unLink = null;
    String diskNames = null;

    @Option(name = "vmname", description = "name of the virtual machine")
    public void setVmName(String vmName) {
        this.vmName = vmName;
    }

    @Option(name = "unlink", description = "True|False to unlink")
    public void setUnLink(String unLink) {
        this.unLink = Boolean.valueOf(unLink);
    }

    @Option(name = "devicenames", required = false, description = "disk name to unlink")
    public void setDiskNames(String diskNames) {
        this.diskNames = diskNames;
    }


    void promoteDeltaDisk() throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg, TaskInProgressFaultMsg, InvalidStateFaultMsg, InvalidPowerStateFaultMsg, InvalidCollectorVersionFaultMsg {
        ManagedObjectReference vmRef =
                getMOREFsInContainerByType(serviceContent.getRootFolder(),
                        "VirtualMachine").get(vmName);

        boolean unlink = Boolean.valueOf(unLink);
        List<VirtualDisk> vDiskList = new ArrayList<VirtualDisk>();
        if (vmRef != null) {
            if (diskNames != null) {
                String disknames = diskNames;
                String[] diskArr = disknames.split(":");
                Map<String, String> disks = new HashMap<String, String>();
                for (String disk : diskArr) {
                    disks.put(disk, null);
                }
                List<VirtualDevice> devices =
                        ((ArrayOfVirtualDevice) getMOREFs.entityProps(vmRef,
                                new String[]{"config.hardware.device"}).get(
                                "config.hardware.device")).getVirtualDevice();
                for (VirtualDevice device : devices) {
                    if (device instanceof VirtualDisk) {
                        if (disks.containsKey(device.getDeviceInfo().getLabel())) {
                            vDiskList.add((VirtualDisk) device);
                        }
                    }
                }
            }
            ManagedObjectReference taskMOR =
                    vimPort.promoteDisksTask(vmRef, unlink, vDiskList);
            if (getTaskResultAfterDone(taskMOR)) {
                System.out.println("Virtual Disks Promoted successfully.");
            } else {
                System.out.println("Failure -: Virtual Disks "
                        + "cannot be promoted");
            }
        } else {
            System.out.println("Virtual Machine " + vmName + " doesn't exist");
        }
    }

    @Action
    public void run() throws RuntimeFaultFaultMsg, TaskInProgressFaultMsg, InvalidPropertyFaultMsg, InvalidStateFaultMsg, InvalidCollectorVersionFaultMsg, InvalidPowerStateFaultMsg {
        promoteDeltaDisk();
    }
}
