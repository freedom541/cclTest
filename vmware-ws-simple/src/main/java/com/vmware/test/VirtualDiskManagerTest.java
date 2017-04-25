package com.vmware.test;

import com.vmware.connection.ConnectedVimServiceBase;
import com.vmware.vim25.*;

/**
 * Created by ccl on 17/4/19.
 */
public class VirtualDiskManagerTest extends ConnectedVimServiceBase {
    private String dataCenterName = "zhuyun";
    public void copyDatastoreFile() throws RuntimeFaultFaultMsg, InvalidDatastoreFaultMsg, FileFaultFaultMsg, InvalidPropertyFaultMsg, InvalidCollectorVersionFaultMsg {
        ManagedObjectReference datacenterRef = vimPort.findByInventoryPath(serviceContent.getSearchIndex(), dataCenterName);
        String diskName = "[datastore1] ccl/ccl.vmdk";//原来
        String diskPath = "[datastore1] cc/cc.vmdk";//目标
        ManagedObjectReference  task = vimPort.copyDatastoreFileTask(serviceContent.getFileManager(),diskName,datacenterRef,diskPath,datacenterRef,true);
        if (getTaskResultAfterDone(task)) {
            System.out.println("It is OK.");
        } else {
            System.out.println("It is Error.");
        }
    }


    public void copyvirtualdisk() throws RuntimeFaultFaultMsg, InvalidDiskFormatFaultMsg, FileFaultFaultMsg, InvalidDatastoreFaultMsg, InvalidPropertyFaultMsg, InvalidCollectorVersionFaultMsg {
        ManagedObjectReference datacenterRef = vimPort.findByInventoryPath(serviceContent.getSearchIndex(), dataCenterName);
        String diskName = "[datastore1] Centos/Centos.vmdk";//原来
        String diskPath = "[datastore1] sysdisk/linux/centostest.vmdk";//目标

        ManagedObjectReference  task = vimPort.copyVirtualDiskTask(serviceContent.getVirtualDiskManager(),diskName,datacenterRef,diskPath,datacenterRef,null,true);
        if (getTaskResultAfterDone(task)) {
            System.out.println("It is OK.");
        } else {
            System.out.println("It is Error.");
        }
        System.out.println("");
    }

    public void cloneVmbymodel(){
        String vm = "ccm";
        try {
            ManagedObjectReference dcmor = vimPort.findByInventoryPath(serviceContent.getSearchIndex(), dataCenterName);
            ManagedObjectReference vmFolderMor = (ManagedObjectReference) getMOREFs.entityProps(dcmor, new String[]{"vmFolder"}).get("vmFolder");
            ManagedObjectReference vmRef = getMOREFs.vmByVMname(vm, serviceContent.getPropertyCollector());
            VirtualMachineCloneSpec cloneSpec = new VirtualMachineCloneSpec();
            VirtualMachineRelocateSpec relocSpec = new VirtualMachineRelocateSpec();
            cloneSpec.setLocation(relocSpec);
            cloneSpec.setPowerOn(false);
            cloneSpec.setTemplate(true);
            ManagedObjectReference  task = vimPort.cloneVMTask(vmRef,vmFolderMor,"newcc2",cloneSpec);
            if (getTaskResultAfterDone(task)) {
                System.out.println("It is OK.");

//                ManagedObjectReference vmRef2 = getMOREFs.vmByVMname("newcc", serviceContent.getPropertyCollector());
//                ManagedObjectReference hostmor = getMOREFsInContainerByType(dcmor, "HostSystem").get("10.200.6.82");
//                ManagedObjectReference crmor = (ManagedObjectReference) getMOREFs.entityProps(hostmor, new String[]{"parent"}).get("parent");
//                ManagedObjectReference resourcepoolmor = (ManagedObjectReference) getMOREFs.entityProps(crmor, new String[]{"resourcePool"}).get("resourcePool");
//                vimPort.markAsVirtualMachine(vmRef2,resourcepoolmor,hostmor);
//                System.out.println("over.");

            } else {
                System.out.println("It is Error.");
            }
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    public void mark(){
        try {
            ManagedObjectReference dcmor = vimPort.findByInventoryPath(serviceContent.getSearchIndex(), dataCenterName);
            ManagedObjectReference vmRef2 = getMOREFs.vmByVMname("newcc", serviceContent.getPropertyCollector());
            ManagedObjectReference hostmor = getMOREFsInContainerByType(dcmor, "HostSystem").get("10.200.6.82");
            ManagedObjectReference crmor = (ManagedObjectReference) getMOREFs.entityProps(hostmor, new String[]{"parent"}).get("parent");
            ManagedObjectReference resourcepoolmor = (ManagedObjectReference) getMOREFs.entityProps(crmor, new String[]{"resourcePool"}).get("resourcePool");
            vimPort.markAsVirtualMachine(vmRef2,resourcepoolmor,hostmor);
            System.out.println("over.");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
