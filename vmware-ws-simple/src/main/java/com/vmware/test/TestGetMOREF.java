package com.vmware.test;

import com.vmware.connection.ConnectedVimServiceBase;
import com.vmware.vim25.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ccl on 17/4/17.
 */
public class TestGetMOREF extends ConnectedVimServiceBase {

    //inContainerByType():用来查询某个对象下得某个属性的所有值
    public void testinContainerByType() throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
        ManagedObjectReference attachMoref = getMOREFs.inFolderByType(serviceContent.getRootFolder(),
                        "HostSystem", new RetrieveOptions()).get("10.200.6.81");
        Map<String, ManagedObjectReference> map = getMOREFs.inContainerByType(attachMoref,"VirtualMachine");
        System.out.println(map);
    }

    //inFolderByType():用来查询某个目录下得某个属性的所有值
    public void testinFolderByType() throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
        ManagedObjectReference attachMoref = getMOREFs.inFolderByType(serviceContent.getRootFolder(),
                        "HostSystem", new RetrieveOptions()).get("10.200.6.81");
        System.out.println(attachMoref);
    }

    //inContainerByType():用来查询某个对象下得某个属性的所有值
    public void testinContainerByType2() throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
        ManagedObjectReference attachMoref = getMOREFs.inFolderByType(serviceContent.getRootFolder(),
                "Datacenter", new RetrieveOptions()).get("zhuyun");
        String[] strings = {"name","summary.hardware.numCpuCores","summary.hardware.memorySize","summary.quickStats.overallCpuUsage","summary.quickStats.overallMemoryUsage","datastore"};
        Map<ManagedObjectReference,Map<String,Object>> map = getMOREFs.inContainerByType(attachMoref,"HostSystem",strings);

//        Map<ManagedObjectReference,Map<String,Object>> pp = (Map<ManagedObjectReference, Map<String, Object>>) map.entrySet()
//                .stream()
//                .sorted(Map.Entry.comparingByValue((Map<String,Object> m1, Map<String,Object> m2)->{
//                    int a = Integer.parseInt(m1.get("summary.hardware.numCpuCores").toString());
//                    int b = Integer.parseInt(m2.get("summary.hardware.numCpuCores").toString());
//                    int aa = Integer.parseInt(m1.get("summary.quickStats.overallCpuUsage").toString());
//                    int bb = Integer.parseInt(m2.get("summary.quickStats.overallCpuUsage").toString());
//                    int ca = a*10-aa;
//                    int cb = b*2-bb;
//                    return ca-cb;
//        })).findFirst().get();

        System.out.println(map);
    }

    public void testinContainerByType3() throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
        ManagedObjectReference attachMoref = getMOREFs.inFolderByType(serviceContent.getRootFolder(),
                "HostSystem", new RetrieveOptions()).get("10.200.6.81");
        ArrayOfManagedObjectReference amr = (ArrayOfManagedObjectReference) getMOREFs.entityProps(attachMoref,new String[]{"datastore"}).get("datastore");
        Map<ManagedObjectReference,DatastoreSummary> storeMap = new HashMap<>();
        if (amr != null){
            List<ManagedObjectReference> list = amr.getManagedObjectReference();
            if (list != null && list.size() > 0){
                for (ManagedObjectReference mor : list){
                    DatastoreSummary info = (DatastoreSummary) getMOREFs.entityProps(mor,new String[]{"summary"}).get("summary");
                    storeMap.put(mor,info);
                }
            }
        }
        List<ManagedObjectReference> bestStore = new ArrayList<>();
        if (storeMap != null && storeMap.size() > 0){
            storeMap.entrySet().stream().sorted((k,v)->{
                return ((k.getValue().getFreeSpace()-v.getValue().getFreeSpace())>0)?-1:1;
            }).forEach(p->{
                long free = p.getValue().getFreeSpace();
                long total = p.getValue().getCapacity();
                double rate = free/total;
                if (rate <= 0.1){
                    return;
                }else {
                    bestStore.add(p.getKey());
                }
            });
        }
        System.out.println(storeMap);
    }
}
