package com.vmware.test;

import com.vmware.connection.ConnectedVimServiceBase;
import com.vmware.vim25.ConfigTarget;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.RuntimeFaultFaultMsg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by ccl on 17/3/24.
 */
public class SearchEntityTest extends ConnectedVimServiceBase {

    public List<Object> getSnapshot() throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        ManagedObjectReference vmr = getMOREFs.vmByVMname("P0fZ6-N3wgMG",serviceContent.getPropertyCollector());
        String[] strings = {"childSnapshot","config"};
        Map<ManagedObjectReference, Map<String, Object>> entities = getMOREFs.inContainerByType(vmr,"VirtualMachineSnapshot",strings);
        List<Object> list = new ArrayList<Object>();
        if (entities != null && entities.size() > 0){
            list.addAll(entities.keySet().stream().collect(Collectors.toList()));
        }
        System.out.println(list);
        return list;
    }

    public List<Object> getVMS() throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        String[] strings = {"summary","config","name","config.template"};
        Map<ManagedObjectReference, Map<String, Object>> entities = getMOREFs.inContainerByType(serviceContent.getRootFolder(),"VirtualMachine",strings);
        List<Object> list = new ArrayList<Object>();
        if (entities != null && entities.size() > 0){
            list.addAll(entities.keySet().stream().collect(Collectors.toList()));
        }
        System.out.println(list);
        return list;
    }
    public List<Object> getDataStore() throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        String[] strings = {"browser","host","info","summary","capability","iormConfiguration"};
        Map<ManagedObjectReference, Map<String, Object>> entities = getMOREFs.inContainerByType(serviceContent.getRootFolder(),"Datastore",strings);
        List<Object> list = new ArrayList<Object>();
        if (entities != null && entities.size() > 0){
            list.addAll(entities.keySet().stream().collect(Collectors.toList()));
        }
        System.out.println(list);
        return list;
    }
    public List<Object> getHostSystem() throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        String name = "HostSystem";
        String[] strings = {"config","capability","configManager","summary","datastore","datastoreBrowser","hardware","network","systemResources","parent"};
        Map<ManagedObjectReference, Map<String, Object>> entities = getMOREFs.inContainerByType(serviceContent.getRootFolder(),name,strings);
        List<Object> list = new ArrayList<Object>();
        if (entities != null && entities.size() > 0){
            list.addAll(entities.keySet().stream().collect(Collectors.toList()));
        }
        return list;
    }
    public List<Object> getClusterComputeResource() throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        String name = "ClusterComputeResource";
        String[] strings = {"name","datastore","host","network","resourcePool","summary","configuration"};
        Map<ManagedObjectReference, Map<String, Object>> entities = getMOREFs.inContainerByType(serviceContent.getRootFolder(),name,strings);
        List<Object> list = new ArrayList<Object>();
        if (entities != null && entities.size() > 0){
            list.addAll(entities.keySet().stream().collect(Collectors.toList()));
        }
        return list;
    }
    public List<Object> getDataCenter() throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        String[] strings = {"configuration","datastore","datastoreFolder","hostFolder","datastore","network","networkFolder","vmFolder"};
        Map<ManagedObjectReference, Map<String, Object>> entities = getMOREFs.inContainerByType(serviceContent.getRootFolder(),"Datacenter",strings);
        List<Object> list = new ArrayList<Object>();
        if (entities != null && entities.size() > 0){
            list.addAll(entities.keySet().stream().collect(Collectors.toList()));
        }
        return list;
    }
    public List<Map<String,ManagedObjectReference>> getNetwork() throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        String[] strings = {"host","name","summary","vm","summary.name","summary.network"};
        Map<ManagedObjectReference, Map<String, Object>> entities = getMOREFs.inContainerByType(serviceContent.getRootFolder(),"Network",strings);
        List<Map<String,ManagedObjectReference>> list = new ArrayList<Map<String,ManagedObjectReference>>();
        if (entities != null && entities.size() > 0){
            for (ManagedObjectReference net : entities.keySet()){
                Map<String,ManagedObjectReference> anet = new HashMap<String,ManagedObjectReference>();
                Map<String, Object> netMap = entities.get(net);
                anet.put(netMap.get("summary.name").toString(), net);
                list.add(anet);
            }
        }
        return list;
    }
    public List<Object> getNetwork2() throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        String hostname = "";
        //ManagedObjectReference hostmor = getMOREFs.virtual(hostname,"HostSystem", serviceContent.getRootFolder());// getMOREFsInContainerByType(dcmor, "HostSystem").get(hostname);
        Map<ManagedObjectReference, Map<String, Object>> entities = getMOREFs.inContainerByType(serviceContent.getRootFolder(),"HostSystem",new String[]{"summary"});
        ManagedObjectReference hostmor = (ManagedObjectReference) entities.keySet().toArray()[0];
        if (hostmor == null) {
            throw new RuntimeException("Host " + hostname + " not found");
        }

        ManagedObjectReference crmor = (ManagedObjectReference) getMOREFs.entityProps(hostmor, new String[]{"parent"}).get("parent");
        if (crmor == null) {
            throw new RuntimeException("No Compute Resource Found On Specified Host");
        }
        ManagedObjectReference envBrowseMor = (ManagedObjectReference) getMOREFs.entityProps(crmor, new String[]{"environmentBrowser"}).get("environmentBrowser");
        ConfigTarget configTarget = vimPort.queryConfigTarget(envBrowseMor, hostmor);
        if (configTarget == null) {
            throw new RuntimeException("No ConfigTarget found in ComputeResource");
        }
        List<Object> list = new ArrayList<Object>();
//        if (entities != null && entities.size() > 0){
//            list.addAll(entities.keySet().stream().collect(Collectors.toList()));
//        }
        return list;
    }

    public void getDPortGroup() throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
        String name = "DistributedVirtualPortgroup";
        String[] strings = {"config","name","host","summary","vm"};
        Map<ManagedObjectReference, Map<String, Object>> entities = getMOREFs.inContainerByType(serviceContent.getRootFolder(),name,strings);
        List<Object> list = new ArrayList<Object>();
        if (entities != null && entities.size() > 0){
            list.addAll(entities.keySet().stream().collect(Collectors.toList()));
        }
    }

    public void gethostsysteminfo() throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
        ManagedObjectReference host = getMOREFs.virtual("10.200.6.81","HostSystem",serviceContent.getRootFolder());
        System.out.println(host);
    }
}
