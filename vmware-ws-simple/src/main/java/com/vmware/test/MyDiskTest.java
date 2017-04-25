package com.vmware.test;

import com.vmware.connection.ConnectedVimServiceBase;
import com.vmware.vim25.*;

/**
 * Created by ccl on 17/3/30.
 */
public class MyDiskTest extends ConnectedVimServiceBase {
    private String dataCenterName = "zhuyun";
    private String vmName = "all";

    public void setDataCenterName(String dataCenterName) {
        this.dataCenterName = dataCenterName;
    }

    public void setVmName(String vmName) {
        this.vmName = vmName;
    }

    public void queryVirtualDiskUuid() throws RuntimeFaultFaultMsg, InvalidDatastoreFaultMsg, FileFaultFaultMsg, InvalidPropertyFaultMsg {
        ManagedObjectReference datacenterRef = vimPort.findByInventoryPath(serviceContent.getSearchIndex(), dataCenterName);
        ManagedObjectReference vmRef = getMOREFs.vmByVMname(vmName, serviceContent.getPropertyCollector());
        VirtualMachineSummary summary = (VirtualMachineSummary) getMOREFs.entityProps(vmRef, new String[]{"summary"}).get("summary");
        String diskName = "[datastore1] allll/allll.vmdk";
        String uuid = vimPort.queryVirtualDiskUuid(serviceContent.getVirtualDiskManager(),diskName,datacenterRef);
        System.out.println(uuid);
    }
    public void copyDatastoreFile() throws RuntimeFaultFaultMsg, InvalidDatastoreFaultMsg, FileFaultFaultMsg, InvalidPropertyFaultMsg, InvalidCollectorVersionFaultMsg {
        ManagedObjectReference datacenterRef = vimPort.findByInventoryPath(serviceContent.getSearchIndex(), dataCenterName);
//        ManagedObjectReference vmRef = getMOREFs.vmByVMname(vmName, serviceContent.getPropertyCollector());
//        VirtualMachineSummary summary = (VirtualMachineSummary) getMOREFs.entityProps(vmRef, new String[]{"summary"}).get("summary");
        String diskName = "[datastore1] allll/allll.vmdk";//原来
        String diskPath = "[datastore1] gxl/adb.vmdk";//目标
        ManagedObjectReference  task = vimPort.copyDatastoreFileTask(serviceContent.getFileManager(),diskName,datacenterRef,diskPath,datacenterRef,true);
        if (getTaskResultAfterDone(task)) {
            System.out.println("It is OK.");
        } else {
            System.out.println("It is Error.");
        }
    }



    public void copyvirtualdisk() throws RuntimeFaultFaultMsg, InvalidDiskFormatFaultMsg, FileFaultFaultMsg, InvalidDatastoreFaultMsg, InvalidPropertyFaultMsg, InvalidCollectorVersionFaultMsg {
        ManagedObjectReference datacenterRef = vimPort.findByInventoryPath(serviceContent.getSearchIndex(), dataCenterName);
        String diskName = "[datastore1] allll/allll.vmdk";//原来
        String diskPath = "[datastore1] jbk/jbk2.vmdk";//目标
        VirtualDiskSpec diskSpec = new VirtualDiskSpec();
        ManagedObjectReference  task = vimPort.copyVirtualDiskTask(serviceContent.getVirtualDiskManager(),diskName,datacenterRef,diskPath,datacenterRef,null,true);
        if (getTaskResultAfterDone(task)) {
            System.out.println("It is OK.");
        } else {
            System.out.println("It is Error.");
        }
        System.out.println("");
    }


}
