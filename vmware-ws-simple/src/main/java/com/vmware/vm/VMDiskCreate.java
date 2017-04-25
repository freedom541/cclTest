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
import com.vmware.common.annotations.Before;
import com.vmware.common.annotations.Option;
import com.vmware.common.annotations.Sample;
import com.vmware.connection.ConnectedVimServiceBase;
import com.vmware.vim25.*;

import java.util.*;

/**
 * <pre>
 * VMDiskCreate
 *
 * This sample demonstrates how to create a virtual disk
 *
 * <b>Parameters:</b>
 * url             [required] : url of the web service
 * username        [required] : username for the authentication
 * password        [required] : password for the authentication
 * vmname          [required] : Name of the virtual machine
 * datastorename   [optional] : name of the DataStore
 * disksize        [required] : Size of the virtual disk in MB
 * disktype        [optional] : Virtual Disk Type
 *                 [thin | preallocated | eagerzeroed | rdm | rdmp]
 * persistence     [optional] : Persistence mode of the virtual disk
 *                 [persistent | independent_persistent | independent_nonpersistent]
 * devicename      [optional] : Canonical name of the LUN to use for disk types
 *
 * <b>Command Line:</b>
 * VMDiskCreate --url [webserviceurl]
 * --username [username] --password [password]
 * --vmname [vmname] --disksize [8]
 * --disktype [thin | preallocated | eagerzeroed | rdm | rdmp]
 * --persistence [persistent | independent_persistent | independent_nonpersistent]
 * --devicename vmhba0:0:0:0
 * </pre>
 */

@Sample(
        name = "vm-disk-create",
        description = "This sample demonstrates how to create a virtual disk"
)
public class VMDiskCreate extends ConnectedVimServiceBase {
    private ManagedObjectReference dataStore;

    /**
     * Disk Types allowed. *
     */
    private static enum DISKTYPE {
        THIN, THICK, PRE_ALLOCATED, RDM, RDMP, EAGERZEROED;
    }

    private static enum CONTROLLERTYPE {

        /**
         * Default device count max for SCSI controller is 16, with unit# 7 being
         * the reserver slot. Similarly for IDE controller the count is 2 a.k.a
         * primary and secondary. These are mentioned here just to keep the logic
         * of this sample simple and should NOT be used in production. Use
         * {@link VirtualMachineConfigOption} to retrieve these at the runtime
         * instead in production
         */
        SCSI(16, 7), IDE(2);

        private final int maxdevice;
        private int reserveSlot = -1;

        private CONTROLLERTYPE(int maxdevice) {
            this.maxdevice = maxdevice;
        }

        private CONTROLLERTYPE(int maxdevice, int reserveSlot) {
            this.maxdevice = maxdevice;
            this.reserveSlot = reserveSlot;
        }

        public int getMaxDevice() {
            return this.maxdevice;
        }

        public int getReserveSlot() {
            return this.reserveSlot;
        }
    }

    /**
     * Hard Disk Bean. Inner class. *
     */
    private static class HardDiskBean {

        private DISKTYPE diskType;
        private String deviceName;
        private int disksize;

        public HardDiskBean() {
        }

        public void setDiskSize(int key) {
            this.disksize = key;
        }

        public int getDiskSize() {
            return this.disksize;
        }

        public void setDiskType(DISKTYPE dsktype) {
            this.diskType = dsktype;
        }

        public DISKTYPE getDiskType() {
            return this.diskType;
        }

        public void setDeviceName(String dvcname) {
            this.deviceName = dvcname;
        }

        public String getDeviceName() {
            return this.deviceName;
        }
    }

    final HardDiskBean hDiskBean = new HardDiskBean();

    String virtualMachineName;
    int diskSize;
    String dataStoreName;
    String disktype;
    String persistence;
    String devicename;

    @Option(name = "vmname", required = false, description = "Name of the virtual machine")
    public void setVirtualMachineName(String name) {
        this.virtualMachineName = name;
    }

    @Option(name = "datastorename", required = false, description = "name of the DataStore")
    public void setDataStoreName(String name) {
        this.dataStoreName = name;
    }

    @Option(name = "disksize", description = "Size of the virtual disk in MB")
    public void setDiskSize(String size) {
        this.diskSize = Integer.parseInt(size);
    }

    @Option(
            name = "disktype",
            required = false,
            description = "Virtual Disk Type\n[thin | preallocated | eagerzeroed | rdm | rdmp]"
    )
    public void setDisktype(String type) {
        this.disktype = type;
    }

    @Option(
            name = "persistence",
            required = false,
            description = "Persistence mode of the virtual disk\n" +
                    "[persistent | independent_persistent | independent_nonpersistent]"
    )
    public void setPersistence(String persistence) {
        this.persistence = persistence;
    }

    @Option(
            name = "devicename",
            required = false,
            description = "Canonical name of the LUN to use for disk types"
    )
    public void setDevicename(String name) {
        this.devicename = name;
    }


    final Map<String, DISKTYPE> disktypehm =
            new HashMap<String, DISKTYPE>();

    @Before
    public void init() {
        disktypehm.put("thin", DISKTYPE.THIN);
        disktypehm.put("thick", DISKTYPE.THICK);
        disktypehm.put("pre-allocated", DISKTYPE.PRE_ALLOCATED);
        disktypehm.put("rdm", DISKTYPE.RDM);
        disktypehm.put("rdmp", DISKTYPE.RDMP);
        disktypehm.put("eagerzeroed", DISKTYPE.EAGERZEROED);
    }

    void setDiskInformation() throws IllegalArgumentException {
        DISKTYPE vmdisktype = null;
        /** Set the Disk Type. **/
        if (disktype == null || disktype.trim().length() == 0) {
            System.out
                    .println(" Disktype is not specified Assuming disktype [thin] ");
            vmdisktype = DISKTYPE.THIN;
            hDiskBean.setDiskType(vmdisktype);
            hDiskBean.setDiskSize(diskSize);
        } else {
            vmdisktype = disktypehm.get(disktype.trim().toLowerCase());
            if (vmdisktype == null) {
                System.out
                        .println("Invalid value for option disktype. Possible values are : "
                                + disktypehm.keySet());
                throw new IllegalArgumentException("The DISK Type " + disktype
                        + " is Invalid");
            }
            hDiskBean.setDiskType(vmdisktype);
            hDiskBean.setDiskSize(diskSize);
        }

        /** Set the Size of the newvirtualdisk. **/
        hDiskBean.setDiskSize((diskSize <= 0) ? 1 : diskSize);

        /** Set the device name for this newvirtualdisk. **/
        if (devicename == null || devicename.trim().length() == 0) {
            if ((vmdisktype == DISKTYPE.RDM) || (vmdisktype == DISKTYPE.RDMP)) {
                throw new IllegalArgumentException(
                        "The devicename is mandatory for specified disktype [ "
                                + vmdisktype + " ]");
            }
        } else {
            hDiskBean.setDeviceName(devicename);
        }
    }


    /**
     * @param vmMor     ManagedObjectReference of the VM on which the operation is
     *                  carried out
     * @param hdiskbean {@link HardDiskBean}
     * @return {@link VirtualDeviceConfigSpec} spec for the device change
     */
    VirtualDeviceConfigSpec virtualDiskOp(
            ManagedObjectReference vmMor, HardDiskBean hdiskbean) throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        String deviceName = hdiskbean.getDeviceName();
        DISKTYPE diskType = hdiskbean.getDiskType();
        int diskSizeMB = hdiskbean.getDiskSize();
        VirtualDeviceConfigSpec vmcs = null;
        List<Integer> getControllerKeyReturnArr = getControllerKey(vmMor);
        String msg = "Failure Disk Create : SCSI Controller not found";
        if (!getControllerKeyReturnArr.isEmpty()) {
            Integer controllerKey = getControllerKeyReturnArr.get(0);
            Integer unitNumber = getControllerKeyReturnArr.get(1);
            vmcs =
                    createVirtualDiskConfigSpec(deviceName, controllerKey,
                            unitNumber, diskType, diskSizeMB);
        } else {
            throw new RuntimeException(msg);
        }
        return vmcs;
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
                int[] slots = new int[CONTROLLERTYPE.SCSI.getMaxDevice()];
                slots[CONTROLLERTYPE.SCSI.getReserveSlot()] = 1;
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

    /*
    * This method constructs a VirtualDeviceConfigSpec for a Virtual Disk.
    *
    * @param deviceName Name of the device, must be the absolute path
    * like /vmfs/devices/disks/vmhba1:0:0:0
    * @param controllerKey index on the controller
    * @param unitNumber of the device on the controller
    * @param diskType one of thin, thick, rdm, rdmp
    * @param diskSizeGB size of the newvirtualdisk in GB.
    * @return VirtualDeviceConfigSpec used for adding / removing an
    * RDM based virtual newvirtualdisk.
    */
    VirtualDeviceConfigSpec createVirtualDiskConfigSpec(
            String deviceName, int controllerkey, int unitNumber,
            DISKTYPE diskType, int diskSizeMB) {

        VirtualDeviceConnectInfo vdci = new VirtualDeviceConnectInfo();
        vdci.setStartConnected(true);
        vdci.setConnected(true);
        vdci.setAllowGuestControl(false);

        VirtualDisk newvirtualdisk = new VirtualDisk();
        newvirtualdisk.setControllerKey(new Integer(controllerkey));
        newvirtualdisk.setUnitNumber(new Integer(unitNumber));
        newvirtualdisk.setCapacityInKB(1024 * diskSizeMB);
        newvirtualdisk.setKey(-1);
        newvirtualdisk.setConnectable(vdci);

        VirtualDiskFlatVer2BackingInfo backinginfo =
                new VirtualDiskFlatVer2BackingInfo();
        VirtualDiskRawDiskMappingVer1BackingInfo rdmorrdmpbackinginfo =
                new VirtualDiskRawDiskMappingVer1BackingInfo();

        switch (diskType) {
            case RDM:
                rdmorrdmpbackinginfo
                        .setCompatibilityMode(VirtualDiskCompatibilityMode.VIRTUAL_MODE
                                .value());
                rdmorrdmpbackinginfo.setDeviceName(deviceName);
                rdmorrdmpbackinginfo.setDiskMode(persistence);
                rdmorrdmpbackinginfo.setDatastore(dataStore);
                rdmorrdmpbackinginfo.setFileName("");
                newvirtualdisk.setBacking(rdmorrdmpbackinginfo);
                break;
            case RDMP:
                rdmorrdmpbackinginfo
                        .setCompatibilityMode(VirtualDiskCompatibilityMode.PHYSICAL_MODE
                                .value());
                rdmorrdmpbackinginfo.setDeviceName(deviceName);
                rdmorrdmpbackinginfo.setDatastore(dataStore);
                rdmorrdmpbackinginfo.setFileName("");
                newvirtualdisk.setBacking(rdmorrdmpbackinginfo);
                break;
            case THICK:
                backinginfo
                        .setDiskMode(VirtualDiskMode.INDEPENDENT_PERSISTENT.value());
                backinginfo.setThinProvisioned(Boolean.FALSE);
                backinginfo.setEagerlyScrub(Boolean.FALSE);
                backinginfo.setDatastore(dataStore);
                backinginfo.setFileName("");
                newvirtualdisk.setBacking(backinginfo);
                break;
            case THIN:
                if (persistence == null) {
                    persistence = "persistent";
                }
                backinginfo.setDiskMode(persistence);
                backinginfo.setThinProvisioned(Boolean.TRUE);
                backinginfo.setEagerlyScrub(Boolean.FALSE);
                backinginfo.setDatastore(dataStore);
                backinginfo.setFileName("");
                newvirtualdisk.setBacking(backinginfo);
                break;
            case PRE_ALLOCATED:
                backinginfo.setDiskMode(persistence);
                backinginfo.setThinProvisioned(Boolean.FALSE);
                backinginfo.setEagerlyScrub(Boolean.FALSE);
                backinginfo.setDatastore(dataStore);
                backinginfo.setFileName("");
                newvirtualdisk.setBacking(backinginfo);
                break;
            case EAGERZEROED:
                backinginfo.setDiskMode(persistence);
                backinginfo.setThinProvisioned(Boolean.FALSE);
                backinginfo.setEagerlyScrub(Boolean.TRUE);
                backinginfo.setDatastore(dataStore);
                backinginfo.setFileName("");
                newvirtualdisk.setBacking(backinginfo);
                break;
            default:
                break;
        }

        VirtualDeviceConfigSpec virtualdiskconfigspec =
                new VirtualDeviceConfigSpec();
        virtualdiskconfigspec
                .setFileOperation(VirtualDeviceConfigSpecFileOperation.CREATE);
        virtualdiskconfigspec.setOperation(VirtualDeviceConfigSpecOperation.ADD);
        virtualdiskconfigspec.setDevice(newvirtualdisk);
        return virtualdiskconfigspec;
    }

    @Action
    public void run() throws InvalidPropertyFaultMsg, DuplicateNameFaultMsg, RuntimeFaultFaultMsg, TaskInProgressFaultMsg, VmConfigFaultFaultMsg, InsufficientResourcesFaultFaultMsg, InvalidDatastoreFaultMsg, FileFaultFaultMsg, ConcurrentAccessFaultMsg, InvalidStateFaultMsg, InvalidNameFaultMsg, InvalidCollectorVersionFaultMsg {

        ManagedObjectReference vmmor =
                getMOREFsInContainerByType(serviceContent.getRootFolder(),
                        "VirtualMachine").get(virtualMachineName);

        if (vmmor == null) {
            System.out.printf(" Virtual Machine [ %s ] not found",
                    virtualMachineName);
            return;
        }

        // Start Setting the Required Objects,
        //to configure a hard newvirtualdisk to this
        // Virtual machine.
        VirtualMachineConfigSpec vmcsreconfig = new VirtualMachineConfigSpec();

        // Initialize the Hard Disk bean.
        setDiskInformation();

        //Initialize the data store
        dataStoreName = "[" + dataStoreName + "]";

        VirtualDeviceConfigSpec diskSpecification =
                virtualDiskOp(vmmor, hDiskBean);
        if (diskSpecification == null) {
            System.exit(0);
        }
        List<VirtualDeviceConfigSpec> alvdcs =
                new ArrayList<VirtualDeviceConfigSpec>();
        alvdcs.add(diskSpecification);

        vmcsreconfig.getDeviceChange().addAll(alvdcs);
        System.out.printf(" Reconfiguring the Virtual Machine  - [ %s ]",
                virtualMachineName);
        ManagedObjectReference task =
                vimPort.reconfigVMTask(vmmor, vmcsreconfig);
        if (getTaskResultAfterDone(task)) {
            System.out.printf("\n Reconfiguring the Virtual Machine "
                    + " - [ %s ] Successful", virtualMachineName);
        } else {
            System.out.printf(" Reconfiguring the Virtual Machine "
                    + " - [ %s ] Failed", virtualMachineName);
        }
    }
}
