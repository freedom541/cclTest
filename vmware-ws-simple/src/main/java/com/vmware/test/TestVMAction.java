package com.vmware.test;

import com.vmware.connection.ConnectedVimServiceBase;
import com.vmware.vim25.*;

/**
 * Created by ccl on 17/4/20.
 */
public class TestVMAction extends ConnectedVimServiceBase {
    private String dataCenterName = "zhuyun";
    public void migrateVM(){
        String vm = "test";
        String tHost = "10.200.6.82";
        try {
            ManagedObjectReference vmMOR = getMOREFs.vmByVMname(vm, serviceContent.getPropertyCollector());
            ManagedObjectReference dcmor = vimPort.findByInventoryPath(serviceContent.getSearchIndex(), dataCenterName);
            ManagedObjectReference hMOR = getMOREFsInContainerByType(dcmor, "HostSystem").get(tHost);
            ManagedObjectReference crmor = (ManagedObjectReference) getMOREFs.entityProps(hMOR, new String[]{"parent"}).get("parent");
            ManagedObjectReference poolMOR = (ManagedObjectReference) getMOREFs.entityProps(crmor, new String[]{"resourcePool"}).get("resourcePool");
            String name = (String) getMOREFs.entityProps(poolMOR,new String[]{"name"}).get("name");
            System.out.println(name);


            ManagedObjectReference taskMOR = vimPort.migrateVMTask(vmMOR, poolMOR, hMOR, VirtualMachineMovePriority.LOW_PRIORITY, VirtualMachinePowerState.POWERED_OFF);
            if (getTaskResultAfterDone(taskMOR)) {
                System.out.println("Migration of Virtual Machine " + vm + " done successfully to " + tHost);
            } else {
                System.out.println("Error::  Migration failed");
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void relocateVM(){
        String vm = "nihao";
        String tHost = "10.200.6.82";
        String datastore = "datastore2";
        try {
            ManagedObjectReference vmMOR = getMOREFs.vmByVMname(vm, serviceContent.getPropertyCollector());
            ManagedObjectReference dcmor = vimPort.findByInventoryPath(serviceContent.getSearchIndex(), dataCenterName);
            ManagedObjectReference hMOR = getMOREFsInContainerByType(dcmor, "HostSystem").get(tHost);
            ManagedObjectReference crmor = (ManagedObjectReference) getMOREFs.entityProps(hMOR, new String[]{"parent"}).get("parent");
            ManagedObjectReference poolMOR = (ManagedObjectReference) getMOREFs.entityProps(crmor, new String[]{"resourcePool"}).get("resourcePool");
            ManagedObjectReference dsmor = getMOREFs.inContainerByType(dcmor, "Datastore").get(datastore);

            VirtualMachineRelocateSpec relSpec = new VirtualMachineRelocateSpec();
            relSpec.setDatastore(dsmor);
            relSpec.setHost(hMOR);
            relSpec.setPool(poolMOR);

            ManagedObjectReference taskMOR = vimPort.relocateVMTask(vmMOR, relSpec, VirtualMachineMovePriority.LOW_PRIORITY);
            if (getTaskResultAfterDone(taskMOR)) {
                System.out.println("Migration of Virtual Machine " + vm + " done successfully to " + tHost);
            } else {
                System.out.println("Error::  Migration failed");
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void cloneVM(){
        String vm = "cm";
        String tHost = "10.200.6.81";
        String datastore = "datastore1";
        try {
            ManagedObjectReference vmRef = getMOREFs.vmByVMname(vm, serviceContent.getPropertyCollector());
            ManagedObjectReference dcmor = vimPort.findByInventoryPath(serviceContent.getSearchIndex(), dataCenterName);
            ManagedObjectReference vmFolderRef = (ManagedObjectReference) getMOREFs.entityProps(dcmor, new String[]{"vmFolder"}).get("vmFolder");
            ManagedObjectReference hMOR = getMOREFsInContainerByType(dcmor, "HostSystem").get(tHost);
            ManagedObjectReference crmor = (ManagedObjectReference) getMOREFs.entityProps(hMOR, new String[]{"parent"}).get("parent");
            ManagedObjectReference poolMOR = (ManagedObjectReference) getMOREFs.entityProps(crmor, new String[]{"resourcePool"}).get("resourcePool");
//            ManagedObjectReference dsmor = getMOREFsInContainerByType(dcmor, "Datastore").get("datastore1");
            ManagedObjectReference dsmor = getMOREFs.inContainerByType(dcmor, "Datastore").get(datastore);

            VirtualMachineCloneSpec cloneSpec = new VirtualMachineCloneSpec();
            VirtualMachineRelocateSpec relocSpec = new VirtualMachineRelocateSpec();
            relocSpec.setHost(hMOR);
            relocSpec.setPool(poolMOR);
            relocSpec.setDatastore(dsmor);

            cloneSpec.setLocation(relocSpec);
            cloneSpec.setPowerOn(false);
            cloneSpec.setTemplate(false);

            ManagedObjectReference cloneTask = vimPort.cloneVMTask(vmRef, vmFolderRef, "nihao2", cloneSpec);

            if (getTaskResultAfterDone(cloneTask)) {
                System.out.println("Successfully cloned Virtual Machine.");
            } else {
                System.out.println("Failure Cloning Virtual Machine.");
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
