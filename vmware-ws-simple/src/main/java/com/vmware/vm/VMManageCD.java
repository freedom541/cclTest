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
 * VMManageCD
 *
 * This sample adds / removes CDROM to / from an existing VM
 * This sample lists information about a VMs CDROMs
 * This sample updates an existing CDROM a VM
 *
 * <b>Parameters:</b>
 * url               [required] : url of the web service
 * username          [required] : username for the authentication
 * password          [required] : password for the authentication
 * vmname            [required] : name of the virtual machine
 * operation         [required] : operation type - [get|add|remove|set]
 * isopath           [optional] : full datastore path to the iso file
 * remote            [optional] : Specify the device is a remote or client device or iso
 * startconnected    [optional] : virtual CD starts connected when VM powers on
 * connect           [optional] : virtual CD is connected after creation or update
 *                                Set only if VM is powered on
 * label             [optional] : used to find the device.key value
 * devicename        [optional] : Specify the path to the CD on the VM's host
 *
 * <b>Command Line:</b>
 * Get CD-Rom Info");
 * run.bat com.vmware.vm.VMManageCD
 * --url [webserviceurl] --username [username] --password [password]
 * --operation get --vmname [Virtual Machine Name]
 *
 * Add CD-Rom
 * run.bat com.vmware.vm.VMManageCD
 * --url <webserviceurl> --username [username] --password  [password]
 * --operation add --vmname [Virtual Machine Name]
 * --isoPath "[datastore1] test.iso" --remote false --connect true
 *
 * Remove CD-Rom
 * run.bat com.vmware.vm.VMManageCD
 * --url [webserviceurl] --username [username] --password  [password]
 * --operation remove --vmname [Virtual Machine Name]
 * --label CD\\DVD Drive 1
 *
 * Reconfigure CD-Rom
 * run.bat com.vmware.vm.VMManageCD
 * --url [webserviceurl] --username [username] --password  [password]
 * --operation set --vmname [Virtual Machine Name]
 * --label CD\\DVD Drive 1 --connect false
 */

@Sample(
        name = "vm-manage-cd",
        description =
                "This sample adds / removes CDROM to / from an existing VM\n" +
                        "This sample lists information about a VMs CDROMs\n" +
                        "This sample updates an existing CDROM a VM\n"
)
public class VMManageCD extends ConnectedVimServiceBase {
    static final String[] operations = {"get", "add", "remove", "set"};
    private ManagedObjectReference vmRef;

    String virtualMachineName;
    String operation;
    String labelName;
    String connect;
    String isoPath;
    String deviceName;
    String remote;
    String startConnected;

    @Option(name = "vmname", description = "name of the virtual machine")
    public void setVirtualMachineName(String vmname) {
        this.virtualMachineName = vmname;
    }

    @Option(name = "operation", description = "operation type - [get|add|remove|set]")
    public void setOperation(final String operation) {
        if (check(operation, operations)) {
            this.operation = operation;
        }
    }

    @Option(name = "isopath", required = false, description = "full datastore path to the iso file")
    public void setIsoPath(String path) {
        this.isoPath = path;
    }

    @Option(name = "remote", required = false, description = "Specify the device is a remote or client device or iso")
    public void setRemote(String remote) {
        this.remote = remote;
    }

    @Option(name = "startconnected", required = false, description = "virtual CD starts connected when VM powers on")
    public void setStartConnected(String setting) {
        this.startConnected = setting;
    }

    @Option(
            name = "connect",
            required = false,
            description = "virtual CD is connected after creation or update\n" +
                    "Set only if VM is powered on"
    )
    public void setConnect(String setting) {
        this.connect = setting;
    }

    @Option(name = "label", required = false, description = "used to find the device.key value")
    public void setLabelName(String name) {
        this.labelName = name;
    }

    @Option(name = "devicename", required = false, description = "Specify the path to the CD on the VM's host")
    public void setDeviceName(String name) {
        this.deviceName = name;
    }


    void doOperation() throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg, DuplicateNameFaultMsg, TaskInProgressFaultMsg, InsufficientResourcesFaultFaultMsg, VmConfigFaultFaultMsg, InvalidDatastoreFaultMsg, FileFaultFaultMsg, ConcurrentAccessFaultMsg, InvalidStateFaultMsg, InvalidCollectorVersionFaultMsg, InvalidNameFaultMsg {
        vmRef =
                getMOREFsInContainerByType(serviceContent.getRootFolder(),
                        "VirtualMachine").get(virtualMachineName);

        if (vmRef == null) {
            System.out.println("Virtual Machine " + virtualMachineName
                    + " not found.");
            return;
        }
        if (operation.equalsIgnoreCase("get")) {
            getInfo();
        } else if (operation.equalsIgnoreCase("add")) {
            addCdRom();
        }
        if (operation.equalsIgnoreCase("remove")) {
            removeCdRom();
        }
        if (operation.equalsIgnoreCase("set")) {
            setCdRom();
        }
    }

    // Prints the information for all the CD Roms attached
    void getInfo() throws InvalidPropertyFaultMsg,
            RuntimeFaultFaultMsg {
        List<VirtualDevice> deviceArr =
                ((ArrayOfVirtualDevice) getMOREFs.entityProps(vmRef,
                        new String[]{"config.hardware.device"}).get(
                        "config.hardware.device")).getVirtualDevice();
        int count = 0;
        for (VirtualDevice device : deviceArr) {
            if (device instanceof VirtualCdrom) {
                String name = device.getDeviceInfo().getLabel();
                int key = device.getKey();
                boolean isCdConnected = device.getConnectable().isConnected();
                boolean isConnectedAtPowerOn =
                        device.getConnectable().isStartConnected();
                boolean isRemote = false;
                String devName = "";
                String isopath = "";
                if (device.getBacking() instanceof VirtualCdromRemoteAtapiBackingInfo) {
                    isRemote = true;
                    devName =
                            ((VirtualCdromRemoteAtapiBackingInfo) device.getBacking())
                                    .getDeviceName();
                } else if (device.getBacking() instanceof VirtualCdromRemotePassthroughBackingInfo) {
                    isRemote = true;
                    devName =
                            ((VirtualCdromRemotePassthroughBackingInfo) device
                                    .getBacking()).getDeviceName();
                } else if (device.getBacking() instanceof VirtualCdromAtapiBackingInfo) {
                    devName =
                            ((VirtualCdromAtapiBackingInfo) device.getBacking())
                                    .getDeviceName();
                } else if (device.getBacking() instanceof VirtualCdromPassthroughBackingInfo) {
                    devName =
                            ((VirtualCdromPassthroughBackingInfo) device.getBacking())
                                    .getDeviceName();
                } else if (device.getBacking() instanceof VirtualCdromIsoBackingInfo) {
                    isopath =
                            ((VirtualCdromIsoBackingInfo) device.getBacking())
                                    .getFileName();
                }
                System.out.println("ISO Path                : " + isopath);
                System.out.println("Device                  : " + devName);
                System.out.println("Remote                  : " + isRemote);
                System.out.println("Connected               : " + isCdConnected);
                System.out.println("ConnectedAtPowerOn      : "
                        + isConnectedAtPowerOn);
                System.out.println("Id                      : " + "VirtualMachine-"
                        + vmRef.getValue() + "/" + key);
                System.out.println("Name                    : " + "CD/" + name);
                count++;
            }
        }
        if (count == 0) {
            System.out.println("No CdRom device attached to this VM.");
        }
    }

    // Add new CD Rom
    void addCdRom() throws DuplicateNameFaultMsg, RuntimeFaultFaultMsg, TaskInProgressFaultMsg, VmConfigFaultFaultMsg, InsufficientResourcesFaultFaultMsg, InvalidDatastoreFaultMsg, FileFaultFaultMsg, ConcurrentAccessFaultMsg, InvalidStateFaultMsg, InvalidNameFaultMsg, InvalidPropertyFaultMsg, InvalidCollectorVersionFaultMsg {
        if (remote == null) {
            remote = "false";
        }
        if (startConnected == null) {
            startConnected = "false";
        }
        if (connect == null) {
            connect = "false";
        }
        int controllerKey = -1;
        int unitNumber = 0;

        VirtualMachineConfigSpec configSpec = new VirtualMachineConfigSpec();
        List<VirtualDeviceConfigSpec> deviceConfigSpecArr =
                new ArrayList<VirtualDeviceConfigSpec>();
        VirtualDeviceConfigSpec deviceConfigSpec = new VirtualDeviceConfigSpec();

        List<VirtualDevice> listvd =
                ((ArrayOfVirtualDevice) getMOREFs.entityProps(vmRef,
                        new String[]{"config.hardware.device"}).get(
                        "config.hardware.device")).getVirtualDevice();

        Map<Integer, VirtualDevice> deviceMap =
                new HashMap<Integer, VirtualDevice>();
        for (VirtualDevice virtualDevice : listvd) {
            deviceMap.put(virtualDevice.getKey(), virtualDevice);
        }
        boolean found = false;
        for (VirtualDevice virtualDevice : listvd) {
            if (virtualDevice instanceof VirtualIDEController) {
                VirtualIDEController vscsic = (VirtualIDEController) virtualDevice;
                int[] slots = new int[2];
                List<Integer> devicelist = vscsic.getDevice();
                for (Integer deviceKey : devicelist) {
                    if (deviceMap.get(deviceKey).getUnitNumber() != null) {
                        slots[deviceMap.get(deviceKey).getUnitNumber()] = 1;
                    }
                }
                for (int i = 0; i < slots.length; i++) {
                    if (slots[i] != 1) {
                        controllerKey = vscsic.getKey();
                        unitNumber = i;
                        found = true;
                        break;
                    }
                }
                if (found) {
                    break;
                }
            }
        }

        if (!found) {
            throw new RuntimeException(
                    "The IDE controller on the vm has maxed out its "
                            + "capacity. Please add an additional IDE controller");
        }
        VirtualCdrom cdRom = new VirtualCdrom();
        cdRom.setControllerKey(controllerKey);
        cdRom.setUnitNumber(unitNumber);
        cdRom.setKey(-1);

        VirtualDeviceConnectInfo cInfo = new VirtualDeviceConnectInfo();
        if (connect != null) {
            cInfo.setConnected(Boolean.valueOf(connect));
        }
        if (startConnected != null) {
            cInfo.setStartConnected(Boolean.valueOf(startConnected));
        }
        cdRom.setConnectable(cInfo);
        if (deviceName == null && isoPath == null) {
            if (remote.equalsIgnoreCase("true")) {
                VirtualCdromRemotePassthroughBackingInfo backingInfo =
                        new VirtualCdromRemotePassthroughBackingInfo();
                backingInfo.setExclusive(false);
                backingInfo.setDeviceName("");
                backingInfo.setUseAutoDetect(true);
                cdRom.setBacking(backingInfo);
            } else {
                System.out.println("For Local option, either specify ISOPath or "
                        + "Device Name");
                return;
            }
        } else {
            if (deviceName != null) {
                if (remote.equalsIgnoreCase("true")) {
                    System.out.println("For Device name option is only valid for "
                            + "Local CD Rom");
                    return;
                } else {
                    VirtualCdromAtapiBackingInfo backingInfo =
                            new VirtualCdromAtapiBackingInfo();
                    backingInfo.setDeviceName(deviceName);
                    cdRom.setBacking(backingInfo);
                }
            } else if (isoPath != null) {
                VirtualCdromIsoBackingInfo backingInfo =
                        new VirtualCdromIsoBackingInfo();
                if (remote.equalsIgnoreCase("true")) {
                    System.out.println("Iso path option is only valid for Local "
                            + "CD Rom");
                    return;
                } else {
                    backingInfo.setFileName(isoPath);
                }
                cdRom.setBacking(backingInfo);
            }
        }
        deviceConfigSpec.setDevice(cdRom);
        deviceConfigSpec.setOperation(VirtualDeviceConfigSpecOperation.ADD);

        deviceConfigSpecArr.add(deviceConfigSpec);
        configSpec.getDeviceChange().addAll(deviceConfigSpecArr);

        ManagedObjectReference task = vimPort.reconfigVMTask(vmRef, configSpec);
        if (getTaskResultAfterDone(task)) {
            System.out
                    .printf(
                            " Reconfiguring the Virtual Machine  - [ %s ] Successful on %s%n",
                            virtualMachineName, operation);
        } else {
            System.out.printf(
                    " Reconfiguring the Virtual Machine  - [ %s ] Failure on %s%n",
                    virtualMachineName, operation);
        }
    }

    // Remove new CD Rom
    void removeCdRom() throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg, DuplicateNameFaultMsg, TaskInProgressFaultMsg, VmConfigFaultFaultMsg, InsufficientResourcesFaultFaultMsg, InvalidDatastoreFaultMsg, FileFaultFaultMsg, ConcurrentAccessFaultMsg, InvalidStateFaultMsg, InvalidNameFaultMsg, InvalidCollectorVersionFaultMsg {
        if (labelName == null) {
            System.out.println("Option label is required for remove option");
            return;
        }
        List<VirtualDevice> deviceArr =
                ((ArrayOfVirtualDevice) getMOREFs.entityProps(vmRef,
                        new String[]{"config.hardware.device"}).get(
                        "config.hardware.device")).getVirtualDevice();
        VirtualDevice cdRom = null;

        for (VirtualDevice device : deviceArr) {
            if (device instanceof VirtualCdrom) {
                Description info = device.getDeviceInfo();
                if (info != null) {
                    if (info.getLabel().equalsIgnoreCase(labelName)) {
                        cdRom = device;
                        break;
                    }
                }
            }
        }
        if (cdRom == null) {
            System.out.println("Specified Device Not Found");
            return;
        }

        VirtualMachineConfigSpec configSpec = new VirtualMachineConfigSpec();
        List<VirtualDeviceConfigSpec> deviceConfigSpecArr =
                new ArrayList<VirtualDeviceConfigSpec>();

        VirtualDeviceConfigSpec deviceConfigSpec = new VirtualDeviceConfigSpec();
        deviceConfigSpec.setDevice(cdRom);
        deviceConfigSpec.setOperation(VirtualDeviceConfigSpecOperation.REMOVE);

        deviceConfigSpecArr.add(deviceConfigSpec);
        configSpec.getDeviceChange().addAll(deviceConfigSpecArr);

        ManagedObjectReference task = vimPort.reconfigVMTask(vmRef, configSpec);
        if (getTaskResultAfterDone(task)) {
            System.out.printf(" Reconfiguring the Virtual "
                    + "Machine  - [ %s ] Successful on %s%n", virtualMachineName,
                    operation);
        } else {
            System.out.printf(" Reconfiguring the Virtual Machine  "
                    + "- [ %s ] Failure on %s%n", virtualMachineName, operation);
        }
    }

    // Reconfigure the CdRom
    void setCdRom() throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg, InvalidCollectorVersionFaultMsg, DuplicateNameFaultMsg, TaskInProgressFaultMsg, VmConfigFaultFaultMsg, InsufficientResourcesFaultFaultMsg, InvalidDatastoreFaultMsg, FileFaultFaultMsg, ConcurrentAccessFaultMsg, InvalidStateFaultMsg, InvalidNameFaultMsg {
        List<VirtualDevice> deviceArr =
                ((ArrayOfVirtualDevice) getMOREFs.entityProps(vmRef,
                        new String[]{"config.hardware.device"}).get(
                        "config.hardware.device")).getVirtualDevice();

        VirtualDevice cdRom = null;
        for (VirtualDevice device : deviceArr) {
            if (device instanceof VirtualCdrom) {
                Description info = device.getDeviceInfo();
                if (info != null) {
                    if (info.getLabel().equalsIgnoreCase(labelName)) {
                        cdRom = device;
                        break;
                    }
                }
            }
        }
        if (cdRom == null) {
            System.out.println("Specified Device Not Found");
            return;
        }

        VirtualMachineConfigSpec configSpec = new VirtualMachineConfigSpec();
        List<VirtualDeviceConfigSpec> deviceConfigSpecArr =
                new ArrayList<VirtualDeviceConfigSpec>();

        VirtualDeviceConfigSpec deviceConfigSpec = new VirtualDeviceConfigSpec();

        VirtualDeviceConnectInfo cInfo = new VirtualDeviceConnectInfo();
        if (connect != null) {
            cInfo.setConnected(Boolean.valueOf(connect));
        }

        if (startConnected != null) {
            cInfo.setStartConnected(Boolean.valueOf(startConnected));
        }

        cdRom.setConnectable(cInfo);

        if (deviceName == null && isoPath == null) {
            if (remote.equalsIgnoreCase("true")) {
                VirtualCdromRemotePassthroughBackingInfo backingInfo =
                        new VirtualCdromRemotePassthroughBackingInfo();
                backingInfo.setExclusive(false);
                backingInfo.setDeviceName("");
                backingInfo.setUseAutoDetect(true);
                cdRom.setBacking(backingInfo);
            }
        } else {
            if (deviceName != null) {
                if (remote.equalsIgnoreCase("true")) {
                    System.out.println("For Device name option is only valid for "
                            + "Local CD Rom");
                    return;
                } else {
                    VirtualCdromAtapiBackingInfo backingInfo =
                            new VirtualCdromAtapiBackingInfo();
                    backingInfo.setDeviceName(deviceName);
                    cdRom.setBacking(backingInfo);
                }
            } else if (isoPath != null) {
                VirtualCdromIsoBackingInfo backingInfo =
                        new VirtualCdromIsoBackingInfo();
                if (remote.equalsIgnoreCase("true")) {
                    System.out.println("Iso path option is only valid for Local "
                            + "CD Rom");
                    return;
                } else {
                    backingInfo.setFileName(isoPath);
                }
                cdRom.setBacking(backingInfo);
            }
        }

        deviceConfigSpec.setDevice(cdRom);
        deviceConfigSpec.setOperation(VirtualDeviceConfigSpecOperation.EDIT);

        deviceConfigSpecArr.add(deviceConfigSpec);
        configSpec.getDeviceChange().addAll(deviceConfigSpecArr);

        ManagedObjectReference task = vimPort.reconfigVMTask(vmRef, configSpec);
        if (getTaskResultAfterDone(task)) {
            System.out
                    .printf(
                            " Reconfiguring the Virtual Machine  - [ %s ] Successful on %s%n",
                            virtualMachineName, operation);
        } else {
            System.out.printf(
                    "Reconfiguring the Virtual Machine  - [ %s ] Failure on %s%n",
                    virtualMachineName, operation);
        }
    }

    boolean check(final String operation, final String[] operations) {
        boolean found = false;
        for (String op : operations) {
            if (op.equals(operation)) {
                found = true;
            }
        }
        return found;
    }

    @Action
    public void run() throws DuplicateNameFaultMsg, RuntimeFaultFaultMsg, TaskInProgressFaultMsg, VmConfigFaultFaultMsg, InsufficientResourcesFaultFaultMsg, InvalidDatastoreFaultMsg, InvalidPropertyFaultMsg, FileFaultFaultMsg, ConcurrentAccessFaultMsg, InvalidStateFaultMsg, InvalidCollectorVersionFaultMsg, InvalidNameFaultMsg {
        doOperation();
    }
}
