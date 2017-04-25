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

import java.rmi.RemoteException;
import java.util.*;

/**
 * <pre>
 * VMCreate
 *
 * This sample creates a VM
 *
 * <b>Parameters:</b>
 * url              [required] : url of the web service
 * username         [required] : username for the authentication
 * password         [required] : password for the authentication
 * vmname           [required] : Name of the virtual machine
 * datacentername   [required] : Name of the datacenter
 * hostname         [required] : Name of the host
 * guestosid        [optional] : Type of Guest OS
 * cpucount         [optional] : Total cpu count
 * disksize         [optional] : Size of the Disk
 * memorysize       [optional] : Size of the Memory in 1024MB blocks
 * datastorename    [optional] : Name of dataStore
 *
 * <b>Command Line:</b>
 * Create a VM given datacenter and host names
 * run.bat com.vmware.vm.VMCreate --url [webserviceurl]
 * --username [username] --password [password] --vmname [vmname]
 * --datacentername [DataCenterName] --hostname [hostname]
 *
 * Create a VM given its name, Datacenter name and GuestOsId
 * run.bat com.vmware.vm.VMCreate --url [webserviceurl]
 * --username [username] --password [password] --vmname [vmname]
 * --datacentername [DataCenterName] --guestosid [GuestOsId]
 *
 * Create a VM given its name, Datacenter name and its cpucount
 * run.bat com.vmware.vm.VMCreate --url [webserviceurl]
 * --username [username] --password [password] --vmname [vmname]
 * --datacentername [DataCenterName] --cpucount [cpucount]
 * </pre>
 */
@Sample(name = "vm-create", description = "This sample creates a VM")
public class VMCreatetest extends ConnectedVimServiceBase {

    String dataStore;
    String virtualMachineName;
    long vmMemory = 1024;
    int numCpus = 1;
    String dataCenterName;
    int diskSize = 5 * 1024 * 1024;
    String hostname;
    String guestOsId = "windows7Guest";

    @Option(name = "vmname", description = "Name of the virtual machine")
    public void setVirtualMachineName(String vmname) {
        this.virtualMachineName = vmname;
    }

    @Option(name = "datacentername", description = "Name of the datacenter")
    public void setDataCenterName(String dcname) {
        this.dataCenterName = dcname;
    }

    @Option(name = "hostname", description = "Name of the host")
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    @Option(name = "guestosid", required = false, description = "Type of Guest OS [Windows|Posix]")
    public void setGuestOsId(String guestOsId) {
        this.guestOsId = guestOsId;
    }

    @Option(name = "cpucount", required = false, description = "Total cpu count")
    public void setCpuCount(String cpuCount) {
        this.numCpus = Integer.parseInt(cpuCount);
    }

    @Option(name = "disksize", required = false, description = "Size of the Disk")
    public void setDiskSize(String size) {
        this.diskSize = Integer.parseInt(size);
    }

    @Option(name = "memorysize", required = false, description = "Size of Memory in 1024MB blocks")
    public void setMemorySize(String memorySize) {
        this.vmMemory = Long.parseLong(memorySize);
    }

    @Option(name = "datastorename", required = false, description = "Name of dataStore")
    public void setDataStore(String dsname) {
        this.dataStore = dsname;
    }


    /**
     * Creates the virtual machine.
     *
     * @throws RemoteException the remote exception
     * @throws Exception       the exception
     */
    public void createVirtualMachine() throws RemoteException, RuntimeFaultFaultMsg, InvalidPropertyFaultMsg, InvalidCollectorVersionFaultMsg, OutOfBoundsFaultMsg, DuplicateNameFaultMsg, VmConfigFaultFaultMsg, InsufficientResourcesFaultFaultMsg, AlreadyExistsFaultMsg, InvalidDatastoreFaultMsg, FileFaultFaultMsg, InvalidStateFaultMsg, InvalidNameFaultMsg, TaskInProgressFaultMsg {

        ManagedObjectReference dcmor =
                getMOREFsInContainerByType(serviceContent.getRootFolder(), "Datacenter").get(dataCenterName);

        if (dcmor == null) {
            System.out.println("Datacenter " + dataCenterName + " not found.");
            return;
        }
        ManagedObjectReference hostmor = getMOREFsInContainerByType(dcmor, "HostSystem").get(hostname);
        if (hostmor == null) {
            System.out.println("Host " + hostname + " not found");
            return;
        }

        ManagedObjectReference crmor = (ManagedObjectReference) getMOREFs.entityProps(hostmor, new String[]{"parent"}).get("parent");
        if (crmor == null) {
            System.out.println("No Compute Resource Found On Specified Host");
            return;
        }

        ManagedObjectReference resourcepoolmor = (ManagedObjectReference) getMOREFs.entityProps(crmor, new String[]{"resourcePool"}).get("resourcePool");
        ManagedObjectReference vmFolderMor = (ManagedObjectReference) getMOREFs.entityProps(dcmor, new String[]{"vmFolder"}).get("vmFolder");

        VirtualMachineConfigSpec vmConfigSpec = createVmConfigSpec(virtualMachineName, dataStore, diskSize, crmor, hostmor);

        vmConfigSpec.setName(virtualMachineName);
        vmConfigSpec.setAnnotation("VirtualMachine Annotation");
        vmConfigSpec.setMemoryMB(new Long(vmMemory));
        vmConfigSpec.setNumCPUs(numCpus);
        vmConfigSpec.setGuestId(guestOsId);

        ManagedObjectReference taskmor =
                vimPort.createVMTask(vmFolderMor, vmConfigSpec, resourcepoolmor, null);
        if (getTaskResultAfterDone(taskmor)) {
            System.out.printf("Success: Creating VM  - [ %s ] %n",
                    virtualMachineName);
        } else {
            String msg = "Failure: Creating [ " + virtualMachineName + "] VM";
            throw new RuntimeException(msg);
        }
        ManagedObjectReference vmMor =
                (ManagedObjectReference) getMOREFs.entityProps(taskmor,
                        new String[]{"info.result"}).get("info.result");
        System.out.println("Powering on the newly created VM "
                + virtualMachineName);
        // Start the Newly Created VM.
        powerOnVM(vmMor);
    }

    /**
     * Creates the vm config spec object.
     *
     * @param vmName        the vm name
     * @param datastoreName the datastore name
     * @param diskSizeMB    the disk size in mb
     * @param computeResMor the compute res moref
     * @param hostMor       the host mor
     * @return the virtual machine config spec object
     * @throws Exception the exception
     */
    VirtualMachineConfigSpec createVmConfigSpec(String vmName,
                                                String datastoreName, int diskSizeMB,
                                                ManagedObjectReference computeResMor, ManagedObjectReference hostMor) throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {

        ConfigTarget configTarget =
                getConfigTargetForHost(computeResMor, hostMor);
        List<VirtualDevice> defaultDevices =
                getDefaultDevices(computeResMor, hostMor);
        VirtualMachineConfigSpec configSpec = new VirtualMachineConfigSpec();
        String networkName = null;
        if (configTarget.getNetwork() != null) {
            for (int i = 0; i < configTarget.getNetwork().size(); i++) {
                VirtualMachineNetworkInfo netInfo =
                        configTarget.getNetwork().get(i);
                NetworkSummary netSummary = netInfo.getNetwork();
                if (netSummary.isAccessible()) {
                    networkName = netSummary.getName();
                    break;
                }
            }
        }
        ManagedObjectReference datastoreRef = null;
        if (datastoreName != null) {
            boolean flag = false;
            for (int i = 0; i < configTarget.getDatastore().size(); i++) {
                VirtualMachineDatastoreInfo vdsInfo =
                        configTarget.getDatastore().get(i);
                DatastoreSummary dsSummary = vdsInfo.getDatastore();
                if (dsSummary.getName().equals(datastoreName)) {
                    flag = true;
                    if (dsSummary.isAccessible()) {
                        datastoreRef = dsSummary.getDatastore();
                    } else {
                        throw new RuntimeException(
                                "Specified Datastore is not accessible");
                    }
                    break;
                }
            }
            if (!flag) {
                throw new RuntimeException("Specified Datastore is not Found");
            }
        } else {
            boolean flag = false;
            for (int i = 0; i < configTarget.getDatastore().size(); i++) {
                VirtualMachineDatastoreInfo vdsInfo =
                        configTarget.getDatastore().get(i);
                DatastoreSummary dsSummary = vdsInfo.getDatastore();
                if (dsSummary.isAccessible()) {
                    datastoreName = dsSummary.getName();
                    datastoreRef = dsSummary.getDatastore();
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                throw new RuntimeException("No Datastore found on host");
            }
        }
        String datastoreVolume = getVolumeName(datastoreName);
        VirtualMachineFileInfo vmfi = new VirtualMachineFileInfo();
        vmfi.setVmPathName(datastoreVolume);
        configSpec.setFiles(vmfi);
        // Add a scsi controller
        int diskCtlrKey = 1;
        VirtualDeviceConfigSpec scsiCtrlSpec = new VirtualDeviceConfigSpec();
        scsiCtrlSpec.setOperation(VirtualDeviceConfigSpecOperation.ADD);
        VirtualLsiLogicController scsiCtrl = new VirtualLsiLogicController();
        scsiCtrl.setBusNumber(0);
        scsiCtrlSpec.setDevice(scsiCtrl);
        scsiCtrl.setKey(diskCtlrKey);
        scsiCtrl.setSharedBus(VirtualSCSISharing.NO_SHARING);
        String ctlrType = scsiCtrl.getClass().getName();
        ctlrType = ctlrType.substring(ctlrType.lastIndexOf(".") + 1);

        // Find the IDE controller
        VirtualDevice ideCtlr = null;
        for (int di = 0; di < defaultDevices.size(); di++) {
            if (defaultDevices.get(di) instanceof VirtualIDEController) {
                ideCtlr = defaultDevices.get(di);
                break;
            }
        }

        // Add a floppy
        VirtualDeviceConfigSpec floppySpec = new VirtualDeviceConfigSpec();
        floppySpec.setOperation(VirtualDeviceConfigSpecOperation.ADD);
        VirtualFloppy floppy = new VirtualFloppy();
        VirtualFloppyDeviceBackingInfo flpBacking =
                new VirtualFloppyDeviceBackingInfo();
        flpBacking.setDeviceName("/dev/fd0");
        floppy.setBacking(flpBacking);
        floppy.setKey(3);
        floppySpec.setDevice(floppy);

        // Add a cdrom based on a physical device
//        VirtualDeviceConfigSpec cdSpec = null;
//
//        if (ideCtlr != null) {
//            cdSpec = new VirtualDeviceConfigSpec();
//            cdSpec.setOperation(VirtualDeviceConfigSpecOperation.ADD);
//            VirtualCdrom cdrom = new VirtualCdrom();
//            VirtualCdromIsoBackingInfo cdDeviceBacking =
//                    new VirtualCdromIsoBackingInfo();
//            cdDeviceBacking.setDatastore(datastoreRef);
//            cdDeviceBacking.setFileName(datastoreVolume + "testcd.iso");
//            cdrom.setBacking(cdDeviceBacking);
//            cdrom.setKey(20);
//            cdrom.setControllerKey(new Integer(ideCtlr.getKey()));
//            cdrom.setUnitNumber(new Integer(0));
//            cdSpec.setDevice(cdrom);
//        }

        // Create a new disk - file based - for the vm
//        VirtualDeviceConfigSpec diskSpec = null;
//        diskSpec =
//                createVirtualDisk(datastoreName, diskCtlrKey, datastoreRef,
//                        diskSizeMB);

        // Add a NIC. the network Name must be set as the device name to create the NIC.
        VirtualDeviceConfigSpec nicSpec = new VirtualDeviceConfigSpec();
        if (networkName != null) {
            nicSpec.setOperation(VirtualDeviceConfigSpecOperation.ADD);
            VirtualEthernetCard nic = new VirtualPCNet32();
            VirtualEthernetCardNetworkBackingInfo nicBacking =
                    new VirtualEthernetCardNetworkBackingInfo();
            nicBacking.setDeviceName(networkName);
            nic.setAddressType("generated");
            nic.setBacking(nicBacking);
            nic.setKey(4);
            nicSpec.setDevice(nic);
        }

        List<VirtualDeviceConfigSpec> deviceConfigSpec =
                new ArrayList<VirtualDeviceConfigSpec>();
        deviceConfigSpec.add(scsiCtrlSpec);
        deviceConfigSpec.add(floppySpec);
        //deviceConfigSpec.add(diskSpec);
        if (ideCtlr != null) {
            //deviceConfigSpec.add(cdSpec);
            deviceConfigSpec.add(nicSpec);
        } else {
            deviceConfigSpec = new ArrayList<VirtualDeviceConfigSpec>();
            deviceConfigSpec.add(nicSpec);
        }
        configSpec.getDeviceChange().addAll(deviceConfigSpec);
        return configSpec;
    }

    /**
     * This method returns the ConfigTarget for a HostSystem.
     *
     * @param computeResMor A MoRef to the ComputeResource used by the HostSystem
     * @param hostMor       A MoRef to the HostSystem
     * @return Instance of ConfigTarget for the supplied
     * HostSystem/ComputeResource
     * @throws Exception When no ConfigTarget can be found
     */
    ConfigTarget getConfigTargetForHost(
            ManagedObjectReference computeResMor, ManagedObjectReference hostMor) throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        ManagedObjectReference envBrowseMor =
                (ManagedObjectReference) getMOREFs.entityProps(computeResMor,
                        new String[]{"environmentBrowser"}).get(
                        "environmentBrowser");
        ConfigTarget configTarget =
                vimPort.queryConfigTarget(envBrowseMor, hostMor);
        if (configTarget == null) {
            throw new RuntimeException("No ConfigTarget found in ComputeResource");
        }
        return configTarget;
    }

    /**
     * The method returns the default devices from the HostSystem.
     *
     * @param computeResMor A MoRef to the ComputeResource used by the HostSystem
     * @param hostMor       A MoRef to the HostSystem
     * @return Array of VirtualDevice containing the default devices for the
     * HostSystem
     * @throws Exception
     */
    List<VirtualDevice> getDefaultDevices(
            ManagedObjectReference computeResMor, ManagedObjectReference hostMor) throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        ManagedObjectReference envBrowseMor =
                (ManagedObjectReference) getMOREFs.entityProps(computeResMor,
                        new String[]{"environmentBrowser"}).get(
                        "environmentBrowser");
        VirtualMachineConfigOption cfgOpt =
                vimPort.queryConfigOption(envBrowseMor, null, hostMor);
        List<VirtualDevice> defaultDevs = null;
        if (cfgOpt == null) {
            throw new RuntimeException(
                    "No VirtualHardwareInfo found in ComputeResource");
        } else {
            List<VirtualDevice> lvds = cfgOpt.getDefaultDevice();
            if (lvds == null) {
                throw new RuntimeException("No Datastore found in ComputeResource");
            } else {
                defaultDevs = lvds;
            }
        }
        return defaultDevs;
    }

    String getVolumeName(String volName) {
        String volumeName = null;
        if (volName != null && volName.length() > 0) {
            volumeName = "[" + volName + "]";
        } else {
            volumeName = "[Local]";
        }

        return volumeName;
    }

    /**
     * Creates the virtual disk.
     *
     * @param volName      the vol name
     * @param diskCtlrKey  the disk ctlr key
     * @param datastoreRef the datastore ref
     * @param diskSizeMB   the disk size in mb
     * @return the virtual device config spec object
     */
    VirtualDeviceConfigSpec createVirtualDisk(String volName,
                                              int diskCtlrKey, ManagedObjectReference datastoreRef, int diskSizeMB) {
        String volumeName = getVolumeName(volName);
        VirtualDeviceConfigSpec diskSpec = new VirtualDeviceConfigSpec();

        diskSpec.setFileOperation(VirtualDeviceConfigSpecFileOperation.CREATE);
        diskSpec.setOperation(VirtualDeviceConfigSpecOperation.ADD);

        VirtualDisk disk = new VirtualDisk();
        VirtualDiskFlatVer2BackingInfo diskfileBacking =
                new VirtualDiskFlatVer2BackingInfo();

        diskfileBacking.setFileName(volumeName);
        diskfileBacking.setDiskMode("persistent");

        disk.setKey(new Integer(0));
        disk.setControllerKey(new Integer(diskCtlrKey));
        disk.setUnitNumber(new Integer(0));
        disk.setBacking(diskfileBacking);
        disk.setCapacityInKB(diskSizeMB);

        diskSpec.setDevice(disk);

        return diskSpec;
    }

    /**
     * Power on vm.
     *
     * @param vmMor the vm moref
     * @throws RemoteException the remote exception
     * @throws Exception       the exception
     */
    void powerOnVM(ManagedObjectReference vmMor)
            throws RemoteException, RuntimeFaultFaultMsg, InvalidPropertyFaultMsg, InvalidCollectorVersionFaultMsg, TaskInProgressFaultMsg, VmConfigFaultFaultMsg, InsufficientResourcesFaultFaultMsg, FileFaultFaultMsg, InvalidStateFaultMsg {
        ManagedObjectReference cssTask = vimPort.powerOnVMTask(vmMor, null);
        if (getTaskResultAfterDone(cssTask)) {
            System.out.println("Success: VM started Successfully");
        } else {
            String msg = "Failure: starting [ " + vmMor.getValue() + "] VM";
            throw new RuntimeException(msg);
        }
    }

    @Action
    public void run() throws RuntimeFaultFaultMsg, VmConfigFaultFaultMsg, AlreadyExistsFaultMsg, InvalidDatastoreFaultMsg, FileFaultFaultMsg, InvalidStateFaultMsg, InvalidCollectorVersionFaultMsg, InvalidNameFaultMsg, OutOfBoundsFaultMsg, DuplicateNameFaultMsg, InsufficientResourcesFaultFaultMsg, InvalidPropertyFaultMsg, RemoteException, TaskInProgressFaultMsg {
        createVirtualMachine();
    }
}
