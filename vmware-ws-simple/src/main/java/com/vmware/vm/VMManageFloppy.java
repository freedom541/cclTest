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
 * VMManageFloppy
 *
 * This sample adds / removes floppy to / from an existing VM
 * This sample lists information about a VMs Floppies
 * This sample updates an existing floppy drive on a VM
 *
 * <b>Parameters:</b>
 * url               [required] : url of the web service
 * username          [required] : username for the authentication
 * password          [required] : password for the authentication
 * vmname            [required] : name of the virtual machine
 * operation         [required] : operation type - [get|add|remove|set]
 * imgpath           [optional] : path of image file
 * remote            [optional] : device is a remote or client device or iso
 * startconnected    [optional] : virtual floppy starts connected on VM poweron
 * connect           [optional] : virtual floppy is connected
 *                                Set only if the VM is powered on
 * label             [optional] : used to find the device.key value
 * device            [optional] : path to the floppy on the VM's host
 *
 * <b>Command Line:</b>
 * Get Floppy Info");
 * run.bat com.vmware.vm.VMManageFloppy
 * --url [webserviceurl] --username [username] --password  [password]
 * --operation get --vmname [Virtual Machine Name]
 *
 * Add Floppy
 * run.bat com.vmware.vm.VMManageFloppy
 * --url [webserviceurl] --username [username] --password [password]
 * --operation add --vmname [Virtual Machine Name]
 * --imgpath test.flp --remote false --connect true
 *
 * Remove Floppy
 * run.bat com.vmware.vm.VMManageFloppy
 * --url [webserviceurl] --username [username] --password  [password]
 * --operation remove --vmname [Virtual Machine Name]
 * --label Floppy Drive 1
 *
 * Reconfigure Floppy
 * run.bat com.vmware.vm.VMManageFloppy
 * --url [webserviceurl] --username [username] --password  [password]
 * --operation set --vmname [Virtual Machine Name]
 * --label Floppy Drive 1 --connect false
 * </pre>
 */
@Sample(
        name = "vm-manage-floppy",
        description =
                "This sample adds / removes floppy to / from an existing VM\n" +
                        "This sample lists information about a VMs Floppies\n" +
                        "This sample updates an existing floppy drive on a VM\n"
)
public class VMManageFloppy extends ConnectedVimServiceBase {

    ManagedObjectReference vmRef;

    String virtualmachinename;
    String operation;
    String imagePath;
    String remote;
    String startConnected;
    String device;
    String label;
    String setConnect;

    @Option(name = "vmname", description = "name of the virtual machine")
    public void setVirtualmachinename(String virtualmachinename) {
        this.virtualmachinename = virtualmachinename;
    }

    @Option(name = "operation", description = "operation type - [get|add|remove|set]")
    public void setOperation(String operation) {
        this.operation = operation;
    }

    @Option(name = "imgpath", required = false, description = "path of image file")
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    @Option(name = "remote", required = false, description = "device is a remote or client device or iso")
    public void setRemote(String remote) {
        this.remote = remote;
    }

    @Option(name = "startconnected", required = false, description = "virtual floppy starts connected on VM poweron")
    public void setStartConnected(String startConnected) {
        this.startConnected = startConnected;
    }

    @Option(
            name = "connect",
            required = false,
            description = "virtual floppy is connected. Set only if the VM is powered on"
    )
    public void setSetConnect(String setConnect) {
        this.setConnect = setConnect;
    }

    @Option(name = "lable", required = false, description = "used to find the device.key value")
    public void setLabel(String label) {
        this.label = label;
    }

    @Option(name = "device", required = false, description = "path to the floppy on the VM's host")
    public void setDevice(String device) {
        this.device = device;
    }

    boolean validateTheInput() {

        boolean valid = true;

        if (operation != null) {
            if (!operation.equalsIgnoreCase("add")
                    && !operation.equalsIgnoreCase("get")
                    && !operation.equalsIgnoreCase("remove")
                    && !operation.equalsIgnoreCase("set")) {
                System.out.println("Invalid option for operation");
                System.out.println("Valid Options : get | remove | add | set");
                valid = false;
            }
        }
        if (setConnect != null) {
            if (!setConnect.equalsIgnoreCase("true")
                    && !setConnect.equalsIgnoreCase("false")) {
                System.out.println("Invalid option for connect");
                System.out.println("Valid Options : true | false");
                valid = false;
            }
        }
        if (startConnected != null) {
            if (!startConnected.equalsIgnoreCase("true")
                    && !startConnected.equalsIgnoreCase("false")) {
                System.out.println("Invalid option for startConnected");
                System.out.println("Valid Options : true | false");
                valid = false;
            }
        }
        if (remote != null) {
            if (!remote.equalsIgnoreCase("true")
                    && !remote.equalsIgnoreCase("false")) {
                System.out.println("Invalid option for remote");
                System.out.println("Valid Options : true | false");
                valid = false;
            }
        }
        return valid;
    }

    void doOperation() throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg, DuplicateNameFaultMsg, TaskInProgressFaultMsg, VmConfigFaultFaultMsg, InsufficientResourcesFaultFaultMsg, InvalidDatastoreFaultMsg, FileFaultFaultMsg, ConcurrentAccessFaultMsg, InvalidStateFaultMsg, InvalidCollectorVersionFaultMsg, InvalidNameFaultMsg {
        vmRef =
                getMOREFsInContainerByType(serviceContent.getRootFolder(),
                        "VirtualMachine").get(virtualmachinename);
        if (vmRef == null) {
            System.out.println("Virtual Machine " + virtualmachinename
                    + " not found.");
            return;
        }
        if (operation.equalsIgnoreCase("get")) {
            getInfo();
        } else if (operation.equalsIgnoreCase("add")) {
            addFloppy();
        }
        if (operation.equalsIgnoreCase("remove")) {
            removeFloppy();
        }
        if (operation.equalsIgnoreCase("set")) {
            setFloppy();
        }
    }

    void getInfo() throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        List<VirtualDevice> deviceArr =
                ((ArrayOfVirtualDevice) getMOREFs.entityProps(vmRef,
                        new String[]{"config.hardware.device"}).get(
                        "config.hardware.device")).getVirtualDevice();
        int count = 0;
        for (VirtualDevice devicearr : deviceArr) {
            if (devicearr instanceof VirtualFloppy) {
                String name = devicearr.getDeviceInfo().getLabel();
                int key = devicearr.getKey();
                boolean isconnected = devicearr.getConnectable().isConnected();
                boolean isConnectedAtPowerOn =
                        devicearr.getConnectable().isStartConnected();

                boolean isRemote = false;
                String deviceName = "";
                String imgPath = "";

                if (devicearr.getBacking() instanceof VirtualFloppyRemoteDeviceBackingInfo) {
                    isRemote = true;
                    deviceName =
                            ((VirtualFloppyRemoteDeviceBackingInfo) devicearr
                                    .getBacking()).getDeviceName();
                }

                if (devicearr.getBacking() instanceof VirtualFloppyDeviceBackingInfo) {
                    deviceName =
                            ((VirtualFloppyDeviceBackingInfo) devicearr.getBacking())
                                    .getDeviceName();
                }

                if (devicearr.getBacking() instanceof VirtualFloppyImageBackingInfo) {
                    imgPath =
                            ((VirtualFloppyImageBackingInfo) devicearr.getBacking())
                                    .getFileName();
                }
                System.out.println("Image Path              : " + imgPath);
                System.out.println("Device                  : " + deviceName);
                System.out.println("Remote                  : " + isRemote);
                System.out.println("Connected               : " + isconnected);
                System.out.println("ConnectedAtPowerOn      : "
                        + isConnectedAtPowerOn);
                System.out.println("Id                      : " + "VirtualMachine-"
                        + vmRef.getValue() + "/" + key);
                System.out.println("Name                    : " + "Floppy/" + name);
                System.out.println("---------------------------------------------");
                count++;
            }
        }
        if (count == 0) {
            System.out.println("No Floppy device attached to this VM.");
        }
    }

    void addFloppy() throws ConcurrentAccessFaultMsg,
            DuplicateNameFaultMsg, FileFaultFaultMsg,
            InsufficientResourcesFaultFaultMsg, InvalidDatastoreFaultMsg,
            InvalidNameFaultMsg, InvalidStateFaultMsg, RuntimeFaultFaultMsg,
            TaskInProgressFaultMsg, VmConfigFaultFaultMsg,
            InvalidPropertyFaultMsg, InvalidCollectorVersionFaultMsg {
        if (remote == null) {
            remote = "false";
        }
        if (startConnected == null) {
            startConnected = "false";
        }
        if (setConnect == null) {
            setConnect = "false";
        }

        VirtualMachineConfigSpec configSpec = new VirtualMachineConfigSpec();
        List<VirtualDeviceConfigSpec> deviceConfigSpecArr =
                new ArrayList<VirtualDeviceConfigSpec>();

        VirtualDeviceConfigSpec deviceConfigSpec = new VirtualDeviceConfigSpec();
        VirtualFloppy floppyDev = new VirtualFloppy();
        floppyDev.setKey(-1);


        VirtualDeviceConnectInfo cInfo = new VirtualDeviceConnectInfo();
        if (setConnect != null) {
            cInfo.setConnected(Boolean.valueOf(setConnect));
        }

        if (startConnected != null) {
            cInfo.setStartConnected(Boolean.valueOf(startConnected));
        }

        floppyDev.setConnectable(cInfo);

        if (remote.equalsIgnoreCase("true")) {
            VirtualFloppyRemoteDeviceBackingInfo backingInfo =
                    new VirtualFloppyRemoteDeviceBackingInfo();
            backingInfo.setDeviceName("/dev/fd0");
            floppyDev.setBacking(backingInfo);
        } else if (imagePath != null) {
            VirtualFloppyImageBackingInfo backingInfo =
                    new VirtualFloppyImageBackingInfo();
            backingInfo.setFileName(imagePath);
            floppyDev.setBacking(backingInfo);
        } else if (device != null) {
            VirtualFloppyDeviceBackingInfo backingInfo =
                    new VirtualFloppyDeviceBackingInfo();
            backingInfo.setDeviceName(device);
            floppyDev.setBacking(backingInfo);
        } else {
            throw new IllegalArgumentException(
                    "Plese specify the --imgpath or --device option if --remote "
                            + "is either omitted or set to false while adding a floppy\n");
        }

        deviceConfigSpec.setDevice(floppyDev);
        deviceConfigSpec.setOperation(VirtualDeviceConfigSpecOperation.ADD);

        deviceConfigSpecArr.add(deviceConfigSpec);
        configSpec.getDeviceChange().addAll(deviceConfigSpecArr);

        ManagedObjectReference task = vimPort.reconfigVMTask(vmRef, configSpec);
        if (getTaskResultAfterDone(task)) {
            System.out.printf(" Reconfiguring the Virtual "
                    + "Machine  - [ %s ] Successful on %s%n", virtualmachinename,
                    operation);
        } else {
            System.out.printf(" Reconfiguring the Virtual Machine  "
                    + "- [ %s ] Failure on %s%n", virtualmachinename, operation);
        }
    }

    void removeFloppy() throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg, InvalidCollectorVersionFaultMsg, DuplicateNameFaultMsg, TaskInProgressFaultMsg, VmConfigFaultFaultMsg, InsufficientResourcesFaultFaultMsg, InvalidDatastoreFaultMsg, FileFaultFaultMsg, ConcurrentAccessFaultMsg, InvalidStateFaultMsg, InvalidNameFaultMsg {
        if (label == null) {
            System.out.println("Option label is required for remove option");
            return;
        }
        List<VirtualDevice> deviceArr =
                ((ArrayOfVirtualDevice) getMOREFs.entityProps(vmRef,
                        new String[]{"config.hardware.device"}).get(
                        "config.hardware.device")).getVirtualDevice();
        VirtualDevice floppy = null;

        for (VirtualDevice device : deviceArr) {
            if (device instanceof VirtualFloppy) {
                Description info = device.getDeviceInfo();
                if (info != null) {
                    if (info.getLabel().equalsIgnoreCase(label)) {
                        floppy = device;
                        break;
                    }
                }
            }
        }
        if (floppy == null) {
            System.out.println("Specified Device Not Found");
            return;
        }

        VirtualMachineConfigSpec configSpec = new VirtualMachineConfigSpec();
        List<VirtualDeviceConfigSpec> deviceConfigSpecArr =
                new ArrayList<VirtualDeviceConfigSpec>();

        VirtualDeviceConfigSpec deviceConfigSpec = new VirtualDeviceConfigSpec();
        deviceConfigSpec.setDevice(floppy);
        deviceConfigSpec.setOperation(VirtualDeviceConfigSpecOperation.REMOVE);

        deviceConfigSpecArr.add(deviceConfigSpec);
        configSpec.getDeviceChange().addAll(deviceConfigSpecArr);

        ManagedObjectReference task = vimPort.reconfigVMTask(vmRef, configSpec);
        if (getTaskResultAfterDone(task)) {
            System.out.printf(" Reconfiguring the Virtual "
                    + "Machine  - [ %s ] Successful on %s%n", virtualmachinename,
                    operation);
        } else {
            System.out.printf(" Reconfiguring the Virtual Machine  "
                    + "- [ %s ] Failure on %s%n", virtualmachinename, operation);
        }
    }

    void setFloppy() throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg, DuplicateNameFaultMsg, TaskInProgressFaultMsg, VmConfigFaultFaultMsg, InsufficientResourcesFaultFaultMsg, InvalidDatastoreFaultMsg, FileFaultFaultMsg, ConcurrentAccessFaultMsg, InvalidStateFaultMsg, InvalidNameFaultMsg, InvalidCollectorVersionFaultMsg {
        if (label == null) {
            System.out.println("Option label is required for set option");
            return;
        }

        if (remote == null) {
            remote = "false";
        }

        if (startConnected == null) {
            startConnected = "false";
        }

        if (setConnect == null) {
            setConnect = "false";
        }

        VirtualMachineConfigSpec configSpec = new VirtualMachineConfigSpec();
        List<VirtualDeviceConfigSpec> deviceConfigSpecArr =
                new ArrayList<VirtualDeviceConfigSpec>();

        VirtualDeviceConfigSpec deviceConfigSpec = new VirtualDeviceConfigSpec();

        List<VirtualDevice> deviceArr =
                ((ArrayOfVirtualDevice) getMOREFs.entityProps(vmRef,
                        new String[]{"config.hardware.device"}).get(
                        "config.hardware.device")).getVirtualDevice();
        VirtualDevice floppy = null;

        for (VirtualDevice device : deviceArr) {
            if (device instanceof VirtualFloppy) {
                Description info = device.getDeviceInfo();
                if (info != null) {
                    if (info.getLabel().equalsIgnoreCase(label)) {
                        floppy = device;
                        break;
                    }
                }
            }
        }
        if (floppy == null) {
            System.out.println("Specified Device Not Found");
            return;
        }

        VirtualDeviceConnectInfo cInfo = new VirtualDeviceConnectInfo();
        if (setConnect != null) {
            cInfo.setConnected(Boolean.valueOf(setConnect));
        }

        if (startConnected != null) {
            cInfo.setStartConnected(Boolean.valueOf(startConnected));
        }

        floppy.setConnectable(cInfo);

        if (remote.equalsIgnoreCase("true")) {
            VirtualFloppyRemoteDeviceBackingInfo backingInfo =
                    new VirtualFloppyRemoteDeviceBackingInfo();
            backingInfo.setDeviceName("/dev/fd0");
            floppy.setBacking(backingInfo);
        } else if (imagePath != null) {
            VirtualFloppyImageBackingInfo backingInfo =
                    new VirtualFloppyImageBackingInfo();
            backingInfo.setFileName(imagePath);
            floppy.setBacking(backingInfo);
        } else if (device != null) {
            VirtualFloppyDeviceBackingInfo backingInfo =
                    new VirtualFloppyDeviceBackingInfo();
            backingInfo.setDeviceName(device);
            floppy.setBacking(backingInfo);
        }
        deviceConfigSpec.setDevice(floppy);
        deviceConfigSpec.setOperation(VirtualDeviceConfigSpecOperation.EDIT);

        deviceConfigSpecArr.add(deviceConfigSpec);
        configSpec.getDeviceChange().addAll(deviceConfigSpecArr);

        ManagedObjectReference task = vimPort.reconfigVMTask(vmRef, configSpec);
        if (getTaskResultAfterDone(task)) {
            System.out.printf(" Reconfiguring the Virtual "
                    + "Machine  - [ %s ] Successful on %s%n", virtualmachinename,
                    operation);
        } else {
            System.out.printf(" Reconfiguring the Virtual Machine  "
                    + "- [ %s ] Failure on %s%n", virtualmachinename, operation);
        }
    }

    @Action
    public void run() throws DuplicateNameFaultMsg, RuntimeFaultFaultMsg, TaskInProgressFaultMsg, InsufficientResourcesFaultFaultMsg, VmConfigFaultFaultMsg, InvalidDatastoreFaultMsg, InvalidPropertyFaultMsg, FileFaultFaultMsg, ConcurrentAccessFaultMsg, InvalidStateFaultMsg, InvalidCollectorVersionFaultMsg, InvalidNameFaultMsg {
        if (validateTheInput()) {
            doOperation();
        }
    }
}
