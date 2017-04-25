package com.vmware.test;

import com.vmware.connection.ConnectedVimServiceBase;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.RuntimeFaultFaultMsg;

import java.util.List;

/**
 * Created by ccl on 17/4/14.
 */
public class SearchIndexDemo extends ConnectedVimServiceBase {
    //String uuid = "500540e2-953f-76e0-9e02-506979a9696f";
    String uuid = "5005f016-47fb-f2ae-0f61-4c38ef7d8cc5";
    String datacenter = "zhuyun";
    String hostname = "10.200.6.81";
    public void findbyuuid() throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
        ManagedObjectReference simr = serviceContent.getSearchIndex();
        ManagedObjectReference dcmor = getMOREFs.inContainerByType(serviceContent.getRootFolder(),"Datacenter").get(datacenter);
        ManagedObjectReference vmmr = vimPort.findByUuid(simr,dcmor,uuid,true,true);
        if (vmmr != null){
            String center = (String) getMOREFs.entityProps(vmmr, new String[]{"name"}).get("name");
            System.out.println(center);
        }
    }
    public void findallbyuuid() throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
        ManagedObjectReference simr = serviceContent.getSearchIndex();
        ManagedObjectReference dcmor = getMOREFs.inContainerByType(serviceContent.getRootFolder(),"Datacenter").get(datacenter);
        List<ManagedObjectReference> vmmrs = vimPort.findAllByUuid(simr,dcmor,uuid,true,true);
        if (vmmrs != null){
           for (ManagedObjectReference vmmr : vmmrs){
               String center = (String) getMOREFs.entityProps(vmmr, new String[]{"name"}).get("name");
               System.out.println(center);
           }
        }
    }
    public void findchild() throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
        ManagedObjectReference simr = serviceContent.getSearchIndex();
        ManagedObjectReference dcmor = getMOREFs.inContainerByType(serviceContent.getRootFolder(),"Datacenter").get(datacenter);
        ManagedObjectReference vmmrs = vimPort.findChild(simr,dcmor,"ccl");
        if (vmmrs != null){
            String center = (String) getMOREFs.entityProps(vmmrs, new String[]{"name"}).get("name");
            System.out.println(center);
        }
    }
}
