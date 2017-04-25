package com.vmware.test;

import com.vmware.connection.ConnectedVimServiceBase;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.VirtualMachineConfigOptionDescriptor;

import java.util.List;

/**
 * Created by ccl on 17/4/24.
 */
public class MySearch extends ConnectedVimServiceBase {

    public void findByInventoryPath(){
        try {
            ManagedObjectReference dcmor = vimPort.findByInventoryPath(serviceContent.getSearchIndex(), "zhuyun");
            ManagedObjectReference folder = vimPort.findByInventoryPath(serviceContent.getSearchIndex(),"zhuyun/vm");
            ManagedObjectReference vmor = vimPort.findByInventoryPath(serviceContent.getSearchIndex(), "zhuyun/vm/cc");
            ManagedObjectReference vmor1 = vimPort.findByUuid(serviceContent.getSearchIndex(),null, "50055127-86c6-ba94-8c5d-ff35b45769d8",true,true);
            ManagedObjectReference hmor = vimPort.findByDnsName(serviceContent.getSearchIndex(),null, "10.200.6.81", false);
            System.out.println();
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }


    public void queryDatacenterConfigOptionDescriptor(){
        try {
            ManagedObjectReference dcmor = vimPort.findByInventoryPath(serviceContent.getSearchIndex(), "zhuyun");
            List<VirtualMachineConfigOptionDescriptor> results = vimPort.queryDatacenterConfigOptionDescriptor(dcmor);
            System.out.println(results);
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }
}
