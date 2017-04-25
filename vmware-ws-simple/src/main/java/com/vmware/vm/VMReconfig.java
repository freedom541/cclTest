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
 * VMReconfig
 *
 * Reconfigures a virtual machine, which include reconfiguring the disk size, disk mode, etc.
 *
 * <b>Parameters:</b>
 * url            [required] : url of the web service
 * username       [required] : username for the authentication
 * password       [required] : password for the authentication
 * vmname         [required] : name of the virtual machine
 * device         [required] : cpu|memory|disk|cd|nic
 * operation      [required] : add|remove|update
 * update operation is only possible for cpu and memory, add|remove are not allowed for cpu and memory
 * value          [required] : high|low|normal|numeric value, label of device when removing
 * disksize       [optional] : Size of virtual disk
 * diskmode       [optional] : persistent|independent_persistent,independent_nonpersistent
 *
 * <b>Command Line:</b>
 * run.bat com.vmware.vm.VMReconfig --url [URLString] --username [User] --password [Password]
 * --vmname [VMName] --operation [Operation] --device [Devicetype] --value [Value]
 * --disksize [Virtualdisksize] --diskmode [VDiskmode]
 * </pre>
 */
@Sample(
        name = "vm-reconfig",
        description = "Reconfigures a virtual machine, which include reconfiguring the disk size, disk mode, etc."
)
public class VMReconfig extends ConnectedVimServiceBase {

    ManagedObjectReference virtualMachine = null;

    String vmName = null;
    String operation = null;
    String device = null;
    String value = null;
    String disksize = null;
    String diskmode = null;

    @Option(name = "vmname", required = true, description = "name of the virtual machine")
    public void setVmName(String vmName) {
        this.vmName = vmName;
    }

    @Option(name = "device", description = "[cpu|memory|disk|cd|nic]")
    public void setDevice(String device) {
        this.device = device;
    }

    @Option(
            name = "operation",
            required = true,
            description = "[add|remove|update]" +
                    "update operation is only possible " +
                    "for cpu and memory, " +
                    "add|remove are not allowed for cpu and memory"
    )
    public void setOperation(String operation) {
        this.operation = operation;
    }

    @Option(name = "value", description = "high|low|normal|numeric value, label of device when removing")
    public void setValue(String value) {
        this.value = value;
    }

    @Option(name = "disksize", required = false, description = "Size of virtual disk")
    public void setDisksize(String disksize) {
        this.disksize = disksize;
    }

    @Option(name = "diskmode", required = false, description = "persistent|independent_persistent,independent_nonpersistent")
    public void setDiskmode(String diskmode) {
        this.diskmode = diskmode;
    }


    /**
     * Gets the controller key and the next available free unit number on the
     * SCSI controller
     *
     * @param vmMor
     * @return
     * @throws InvalidPropertyFaultMsg
     * @throws RuntimeFaultFaultMsg
     */
    List<Integer> getControllerKey(ManagedObjectReference vmMor)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
        List<Integer> retVal = new ArrayList<Integer>();

        List<VirtualDevice> listvd =
                ((ArrayOfVirtualDevice) getMOREFs.entityProps(vmMor,
                        new String[]{"config.hardware.device"}).get(
                        "config.hardware.device")).getVirtualDevice();

        Map<Integer, VirtualDevice> deviceMap =
                new HashMap<Integer, VirtualDevice>();
        for (VirtualDevice virtualDevice : listvd) {
            deviceMap.put(virtualDevice.getKey(), virtualDevice);
        }
        boolean found = false;
        for (VirtualDevice virtualDevice : listvd) {
            if (virtualDevice instanceof VirtualSCSIController) {
                VirtualSCSIController vscsic =
                        (VirtualSCSIController) virtualDevice;
                int[] slots = new int[16];
                slots[7] = 1;
                List<Integer> devicelist = vscsic.getDevice();
                for (Integer deviceKey : devicelist) {
                    if (deviceMap.get(deviceKey).getUnitNumber() != null) {
                        slots[deviceMap.get(deviceKey).getUnitNumber()] = 1;
                    }
                }
                for (int i = 0; i < slots.length; i++) {
                    if (slots[i] != 1) {
                        retVal.add(vscsic.getKey());
                        retVal.add(i);
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
                    "The SCSI controller on the vm has maxed out its "
                            + "capacity. Please add an additional SCSI controller");
        }
        return retVal;
    }

    boolean customValidation() {
        boolean flag = true;
        if (device.equalsIgnoreCase("disk")) {
            if (operation.equalsIgnoreCase("add")) {
                if ((disksize == null) || (diskmode == null)) {
                    System.out.println("For add disk operation, disksize "
                            + "and diskmode are the Mandatory options");
                    flag = false;
                }
                if (disksize != null && Integer.parseInt(disksize) <= 0) {
                    System.out.println("Disksize must be a greater than zero");
                    flag = false;
                }
            }
            if (operation.equalsIgnoreCase("remove")) {
                if (value == null) {
                    System.out
                            .println("Please specify a label in value field to remove the disk");
                }
            }
        }
        if (device.equalsIgnoreCase("nic")) {
            if (operation == null) {
                System.out.println("For add nic operation is the Mandatory option");
                flag = false;
            }
        }
        if (device.equalsIgnoreCase("cd")) {
            if (operation == null) {
                System.out.println("For add cd operation is the Mandatory options");
                flag = false;
            }
        }
        if (operation != null) {
            if (operation.equalsIgnoreCase("add")
                    || operation.equalsIgnoreCase("remove")
                    || operation.equalsIgnoreCase("update")) {
                if (device.equals("cpu") || device.equals("memory")) {
                    if (operation != null && operation.equals("update")) {
                    } else {
                        System.out
                                .println("Invalid operation specified for device cpu or memory");
                        flag = false;
                    }
                }
            } else {
                System.out
                        .println("Operation must be either add, remove or update");
                flag = false;
            }
        }
        return flag;
    }

    ResourceAllocationInfo getShares() {
        ResourceAllocationInfo raInfo = new ResourceAllocationInfo();
        SharesInfo sharesInfo = new SharesInfo();
        if (value.equalsIgnoreCase(SharesLevel.HIGH.toString())) {
            sharesInfo.setLevel(SharesLevel.HIGH);
        } else if (value.equalsIgnoreCase(SharesLevel.NORMAL.toString())) {
            sharesInfo.setLevel(SharesLevel.NORMAL);
        } else if (value.equalsIgnoreCase(SharesLevel.LOW.toString())) {
            sharesInfo.setLevel(SharesLevel.LOW);
        } else {
            sharesInfo.setLevel(SharesLevel.CUSTOM);
            sharesInfo.setShares(Integer.parseInt(value));
        }
        raInfo.setShares(sharesInfo);
        return raInfo;
    }

    String getDatastoreNameWithFreeSpace(int minFreeSpace)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
        String dsName = null;
        List<ManagedObjectReference> datastores =
                ((ArrayOfManagedObjectReference) getMOREFs.entityProps(virtualMachine,
                        new String[]{"datastore"}).get("datastore"))
                        .getManagedObjectReference();
        for (ManagedObjectReference datastore : datastores) {
            DatastoreSummary ds =
                    (DatastoreSummary) getMOREFs.entityProps(datastore,
                            new String[]{"summary"}).get("summary");
            if (ds.getFreeSpace() > minFreeSpace) {
                dsName = ds.getName();
                break;
            }
        }
        return dsName;
    }

    VirtualDeviceConfigSpec getDiskDeviceConfigSpec() throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        String ops = operation;
        VirtualDeviceConfigSpec diskSpec = new VirtualDeviceConfigSpec();

        if (ops.equalsIgnoreCase("Add")) {
            VirtualDisk disk = new VirtualDisk();
            VirtualDiskFlatVer2BackingInfo diskfileBacking =
                    new VirtualDiskFlatVer2BackingInfo();
            String dsName =
                    getDatastoreNameWithFreeSpace(Integer.parseInt(disksize));

            int ckey = 0;
            int unitNumber = 0;
            List<Integer> getControllerKeyReturnArr =
                    getControllerKey(virtualMachine);
            if (!getControllerKeyReturnArr.isEmpty()) {
                ckey = getControllerKeyReturnArr.get(0);
                unitNumber = getControllerKeyReturnArr.get(1);
            }
            String fileName = "[" + dsName + "] " + vmName + "/" + value + ".vmdk";
            diskfileBacking.setFileName(fileName);
            diskfileBacking.setDiskMode(diskmode);

            disk.setControllerKey(ckey);
            disk.setUnitNumber(unitNumber);
            disk.setBacking(diskfileBacking);
            int size = 1024 * (Integer.parseInt(disksize));
            disk.setCapacityInKB(size);
            disk.setKey(-1);
//            Description description = new Description();
//            description.setLabel(value);
//            disk.setDeviceInfo(description);

            diskSpec.setOperation(VirtualDeviceConfigSpecOperation.ADD);
            diskSpec.setFileOperation(VirtualDeviceConfigSpecFileOperation.CREATE);
            diskSpec.setDevice(disk);
        } else if (ops.equalsIgnoreCase("Remove")) {
            VirtualDisk disk = null;
            List<VirtualDevice> deviceList =
                    ((ArrayOfVirtualDevice) getMOREFs.entityProps(virtualMachine,
                            new String[]{"config.hardware.device"}).get(
                            "config.hardware.device")).getVirtualDevice();
            for (VirtualDevice device : deviceList) {
                if (device instanceof VirtualDisk) {
                    if (value.equalsIgnoreCase(device.getDeviceInfo().getLabel())) {
                        disk = (VirtualDisk) device;
                        break;
                    }else if(value.equalsIgnoreCase(((VirtualDisk) device).getDiskObjectId())){
                        disk = (VirtualDisk) device;
                        break;
                    }
                }
            }
            if (disk != null) {
                diskSpec.setOperation(VirtualDeviceConfigSpecOperation.REMOVE);
                diskSpec
                        .setFileOperation(VirtualDeviceConfigSpecFileOperation.DESTROY);
                diskSpec.setDevice(disk);
            } else {
                System.out.println("No device found " + value);
                return null;
            }
        }
        return diskSpec;
    }

    VirtualDeviceConfigSpec getCDDeviceConfigSpec() throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        String ops = operation;
        VirtualDeviceConfigSpec cdSpec = new VirtualDeviceConfigSpec();
        List<VirtualDevice> listvd =
                ((ArrayOfVirtualDevice) getMOREFs.entityProps(virtualMachine,
                        new String[]{"config.hardware.device"}).get(
                        "config.hardware.device")).getVirtualDevice();

        if (ops.equalsIgnoreCase("Add")) {
            cdSpec.setOperation(VirtualDeviceConfigSpecOperation.ADD);

            VirtualCdrom cdrom = new VirtualCdrom();

            VirtualCdromRemoteAtapiBackingInfo vcrabi =
                    new VirtualCdromRemoteAtapiBackingInfo();
            vcrabi.setDeviceName("");
            vcrabi.setUseAutoDetect(true);

            Map<Integer, VirtualDevice> deviceMap =
                    new HashMap<Integer, VirtualDevice>();
            for (VirtualDevice virtualDevice : listvd) {
                deviceMap.put(virtualDevice.getKey(), virtualDevice);
            }
            int controllerKey = 0;
            int unitNumber = 0;
            boolean found = false;
            for (VirtualDevice virtualDevice : listvd) {
                if (virtualDevice instanceof VirtualIDEController) {
                    VirtualIDEController vscsic =
                            (VirtualIDEController) virtualDevice;
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

            cdrom.setBacking(vcrabi);
            cdrom.setControllerKey(controllerKey);
            cdrom.setUnitNumber(unitNumber);
            cdrom.setKey(-1);

            cdSpec.setDevice(cdrom);
            return cdSpec;
        } else {
            VirtualCdrom cdRemove = null;
            cdSpec.setOperation(VirtualDeviceConfigSpecOperation.REMOVE);
            for (VirtualDevice device : listvd) {
                if (device instanceof VirtualCdrom) {
                    if (value.equalsIgnoreCase(device.getDeviceInfo().getLabel())) {
                        cdRemove = (VirtualCdrom) device;
                        break;
                    }
                }
            }
            if (cdRemove != null) {
                cdSpec.setDevice(cdRemove);
            } else {
                System.out.println("No device available " + value);
                return null;
            }
        }
        return cdSpec;
    }

    VirtualDeviceConfigSpec getNICDeviceConfigSpec() throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        String ops = operation;
        VirtualDeviceConfigSpec nicSpec = new VirtualDeviceConfigSpec();
        if (ops.equalsIgnoreCase("Add")) {
            nicSpec.setOperation(VirtualDeviceConfigSpecOperation.ADD);
            VirtualEthernetCard nic = new VirtualPCNet32();
            VirtualEthernetCardNetworkBackingInfo nicBacking =
                    new VirtualEthernetCardNetworkBackingInfo();
            nicBacking.setDeviceName(value);
            nic.setAddressType("generated");
            nic.setBacking(nicBacking);
            nic.setKey(-1);
            nicSpec.setDevice(nic);
        } else if (ops.equalsIgnoreCase("Remove")) {
            VirtualEthernetCard nic = null;
            nicSpec.setOperation(VirtualDeviceConfigSpecOperation.REMOVE);
            List<VirtualDevice> listvd =
                    ((ArrayOfVirtualDevice) getMOREFs.entityProps(virtualMachine,
                            new String[]{"config.hardware.device"}).get(
                            "config.hardware.device")).getVirtualDevice();
            for (VirtualDevice device : listvd) {
                if (device instanceof VirtualEthernetCard) {
                    if (value.equalsIgnoreCase(device.getDeviceInfo().getLabel())) {
                        nic = (VirtualEthernetCard) device;
                        break;
                    }
                }
            }
            if (nic != null) {
                nicSpec.setDevice(nic);
            } else {
                System.out.println("No device available " + value);
                return null;
            }
        }
        return nicSpec;
    }

    void reConfig() throws InvalidPropertyFaultMsg, DuplicateNameFaultMsg, TaskInProgressFaultMsg, VmConfigFaultFaultMsg, InsufficientResourcesFaultFaultMsg, InvalidDatastoreFaultMsg, FileFaultFaultMsg, ConcurrentAccessFaultMsg, InvalidStateFaultMsg, InvalidNameFaultMsg, InvalidCollectorVersionFaultMsg, RuntimeFaultFaultMsg {
        String deviceType = device;
        VirtualMachineConfigSpec vmConfigSpec = new VirtualMachineConfigSpec();

        if (deviceType.equalsIgnoreCase("memory") && operation.equals("update")) {
            System.out
                    .println("Reconfiguring The Virtual Machine For Memory Update "
                            + vmName);
            try {
                vmConfigSpec.setMemoryAllocation(getShares());
            } catch (NumberFormatException nfe) {
                System.out.println("Value of Memory update must "
                        + "be one of high|low|normal|[numeric value]");
                return;
            }
        } else if (deviceType.equalsIgnoreCase("cpu")
                && operation.equals("update")) {
            System.out.println("Reconfiguring The Virtual Machine For CPU Update "
                    + vmName);
            try {
                vmConfigSpec.setCpuAllocation(getShares());
            } catch (NumberFormatException nfe) {
                System.out.println("Value of CPU update must "
                        + "be one of high|low|normal|[numeric value]");
                return;
            }
        } else if (deviceType.equalsIgnoreCase("disk")
                && !operation.equals("update")) {
            System.out.println("Reconfiguring The Virtual Machine For Disk Update "
                            + vmName);
            VirtualDeviceConfigSpec vdiskSpec = getDiskDeviceConfigSpec();
            if (vdiskSpec != null) {
                List<VirtualDeviceConfigSpec> vdiskSpecArray =
                        new ArrayList<VirtualDeviceConfigSpec>();
                vdiskSpecArray.add(vdiskSpec);
                vmConfigSpec.getDeviceChange().addAll(vdiskSpecArray);
            } else {
                return;
            }
        } else if (deviceType.equalsIgnoreCase("nic")
                && !operation.equals("update")) {
            System.out.println("Reconfiguring The Virtual Machine For NIC Update "
                    + vmName);
            VirtualDeviceConfigSpec nicSpec = getNICDeviceConfigSpec();
            if (nicSpec != null) {
                List<VirtualDeviceConfigSpec> nicSpecArray =
                        new ArrayList<VirtualDeviceConfigSpec>();
                nicSpecArray.add(nicSpec);
                vmConfigSpec.getDeviceChange().addAll(nicSpecArray);
            } else {
                return;
            }
        } else if (deviceType.equalsIgnoreCase("cd")
                && !operation.equals("update")) {
            System.out.println("Reconfiguring The Virtual Machine For CD Update "
                    + vmName);
            VirtualDeviceConfigSpec cdSpec = getCDDeviceConfigSpec();
            if (cdSpec != null) {
                List<VirtualDeviceConfigSpec> cdSpecArray =
                        new ArrayList<VirtualDeviceConfigSpec>();
                cdSpecArray.add(cdSpec);
                vmConfigSpec.getDeviceChange().addAll(cdSpecArray);
            } else {
                return;
            }
        } else {
            System.out.println("Invalid device type [memory|cpu|disk|nic|cd]");
            return;
        }

        ManagedObjectReference tmor =
                vimPort.reconfigVMTask(virtualMachine, vmConfigSpec);
        if (getTaskResultAfterDone(tmor)) {
            System.out.println("Virtual Machine reconfigured successfully");
        } else {
            System.out.println("Virtual Machine reconfigur failed");
        }
    }

    @Action
    public void run() throws InvalidPropertyFaultMsg, DuplicateNameFaultMsg, TaskInProgressFaultMsg, InsufficientResourcesFaultFaultMsg, VmConfigFaultFaultMsg, InvalidDatastoreFaultMsg, FileFaultFaultMsg, ConcurrentAccessFaultMsg, InvalidStateFaultMsg, InvalidCollectorVersionFaultMsg, InvalidNameFaultMsg, RuntimeFaultFaultMsg {
        if (customValidation()) {
            connect();
            virtualMachine =
                    getMOREFsInContainerByType(serviceContent.getRootFolder(),
                            "VirtualMachine").get(vmName);
            if (virtualMachine != null) {
                reConfig();
            } else {
                System.out.println("Virtual Machine " + vmName + " Not Found");
            }
        }
    }

}
