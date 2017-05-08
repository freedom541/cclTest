import org.junit.Test;
import org.zstack.sdk.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.Arrays.asList;

/**
 * Created by ccl on 17/5/2.
 */
public class TestZstackApi extends BaseApi {
    @Test
    public void create(){
        //创建计算规格
        CreateInstanceOfferingAction createInstanceOfferingAction = new CreateInstanceOfferingAction();
        createInstanceOfferingAction.name = "thisisoo";
        createInstanceOfferingAction.cpuNum = 1;
        createInstanceOfferingAction.memorySize = 10240000000l;
        createInstanceOfferingAction.sessionId = sessionId;
        InstanceOfferingInventory instanceOffering = createInstanceOfferingAction.call().value.inventory;
        System.out.println(String.format("createInstanceOffering:%s successfully", instanceOffering.name));
    }

    //查询云主机规格
    @Test
    public void query1(){
        QueryInstanceOfferingAction action = new QueryInstanceOfferingAction();
        //action.conditions = Arrays.asList("uuid=777e99df30424187a944103c4b5b1916");
        action.sessionId = sessionId;
        QueryInstanceOfferingAction.Result res = action.call();
        System.out.println(res);
    }
    //查询镜像列表
    @Test
    public void query2(){
        QueryImageAction action = new QueryImageAction();
        //action.conditions = asList("uuid=e9ba2d3fe55d4d4fb21854dc6b12a831");
        action.sessionId = sessionId;
        QueryImageAction.Result res = action.call();
        System.out.println(res);
    }
    //查询区域列表
    @Test
    public void query3(){
        QueryZoneAction action = new QueryZoneAction();
        //action.conditions = asList("name=TestZone","state=Enabled");
        action.sessionId = sessionId;
        QueryZoneAction.Result res = action.call();
        System.out.println(res);
    }

    @Test
    public void delete3(){
        QueryZoneAction action = new QueryZoneAction();
        //action.conditions = asList("name=TestZone","state=Enabled");
        action.sessionId = sessionId;
        QueryZoneAction.Result res = action.call();
        if(Objects.nonNull(res)){
            List<ZoneInventory> zonelist = res.value.getInventories();
            int i = zonelist.size();
            for (ZoneInventory zone : zonelist){
                DeleteZoneAction action1 = new DeleteZoneAction();
                action1.uuid = zone.getUuid();
                action1.deleteMode = "Permissive";
                action1.sessionId = sessionId;
                DeleteZoneAction.Result del = action1.call();
                i--;
            }
        }
    }

    //查询三层网络
    @Test
    public void query4(){
        QueryL3NetworkAction action = new QueryL3NetworkAction();
        action.conditions = asList();
        action.sessionId = sessionId;
        QueryL3NetworkAction.Result res = action.call();
        System.out.println(res);
    }

    //创建云盘规格
    @Test
    public void create2(){
        CreateDiskOfferingAction createDiskOfferingAction = new CreateDiskOfferingAction();
        createDiskOfferingAction.name = "dasfsaewafdsaf";
        createDiskOfferingAction.diskSize = 2148000000l;
        createDiskOfferingAction.sessionId = sessionId;
        DiskOfferingInventory diskOffering = createDiskOfferingAction.call().value.inventory;
        System.out.println(String.format("createDiskOffering:%s successfully", diskOffering.name));
    }

    @Test
    public void createvm(){
//        CreateVmInstanceAction createVmInstanceAction = new CreateVmInstanceAction();
//        createVmInstanceAction.name = "vm1";
//        createVmInstanceAction.instanceOfferingUuid = "777e99df30424187a944103c4b5b1916";
//        createVmInstanceAction.imageUuid = image.uuid;
//        createVmInstanceAction.l3NetworkUuids = Collections.singletonList(l3PublicNetwork.uuid);
//        createVmInstanceAction.dataDiskOfferingUuids = Collections.singletonList(diskOffering.uuid);
//        createVmInstanceAction.clusterUuid = cluster.uuid;
//        createVmInstanceAction.description = "this is a vm";
//        createVmInstanceAction.sessionId = sessionId;
//        VmInstanceInventory vm = createVmInstanceAction.call().value.inventory;
//        System.out.println(String.format("createVm:%s successfully", vm.name));
    }


    //query host的监控数据用

    /**
     * Host Cpu

     //expression
     collectd:collectd_cpu_percent



     Host Disk

     //expression
     collectd:collectd_disk_disk_octets_read
     collectd:collectd_disk_disk_octets_write

     collectd:collectd_disk_disk_ops_read
     collectd:collectd_disk_disk_ops_write

     collectd:collectd_disk_disk_time_read
     collectd:collectd_disk_disk_time_write




     Host Memory

     //expression
     collectd:collectd_memory




     Host Network

     //expression
     collectd:collectd_interface_if_errors_rx
     collectd:collectd_interface_if_errors_tx

     collectd:collectd_interface_if_octets_rx
     collectd:collectd_interface_if_octets_tx

     collectd:collectd_interface_if_packets_rx
     collectd:collectd_interface_if_packets_tx

     */
    @Test
    public void monitor1(){
        PrometheusQueryPassThroughAction action = new PrometheusQueryPassThroughAction();
        action.instant = false;
        action.sessionId = sessionId;
        action.expression = "collectd:collectd_cpu_percent";
        PrometheusQueryPassThroughAction.Result res = action.call();
        System.out.println(res.value.inventories);
    }

    //云主机监控信息

    /**
     * Vm Cpu

     //expression
     collectd:collectd_virt_virt_vcpu
     collectd:collectd_virt_virt_cpu_total

     Vm Disk

     //expression
     collectd:collectd_virt_disk_octets_read
     collectd:collectd_virt_disk_octets_write

     collectd:collectd_virt_disk_ops_read
     collectd:collectd_virt_disk_ops_write


     Vm Memory

     //expression
     collectd:collectd_virt_memory



     Vm Network

     //expression
     collectd:collectd_virt_if_octets_rx
     collectd:collectd_virt_if_octets_tx

     collectd:collectd_virt_if_packets_rx
     collectd:collectd_virt_if_packets_tx

     collectd:collectd_virt_if_errors_rx
     collectd:collectd_virt_if_errors_tx

     collectd:collectd_virt_if_dropped_read
     collectd:collectd_virt_if_dropped_write





     */
    @Test
    public void monitor2(){
        List<String> uuids = new ArrayList<>();
        uuids.add("f5ba0b0fd3b147728b5327d3340f50b6");
        PrometheusQueryVmMonitoringDataAction action = new PrometheusQueryVmMonitoringDataAction();
        action.vmUuids = uuids;
        action.instant = false;
        action.sessionId = sessionId;
        action.expression = "collectd:collectd_virt_virt_vcpu";
        action.relativeTime = "10m";
        PrometheusQueryVmMonitoringDataAction.Result res = action.call();
        System.out.println(res);
    }



    //从云盘创建快照
    @Test
    public void createsnapshot(){
        CreateVolumeSnapshotAction action = new CreateVolumeSnapshotAction();
        action.volumeUuid = "b23188f185fc4038a13cee75f8fb8d0c";
        action.name = "dama";
        action.description = "a snapshot for volume";
        action.sessionId = sessionId;
        CreateVolumeSnapshotAction.Result res = action.call();
        System.out.println(res);
    }

    //查询云盘快照
    @Test
    public void snapshot(){
        QueryVolumeSnapshotAction action = new QueryVolumeSnapshotAction();
        //action.conditions = asList("uuid=e9aa20d8d89e427d9c056a1e3dc9f1dd");
        action.conditions = asList("volumeUuid=06c51a7411be419bb3b408a224750494");
        action.sessionId = sessionId;
        QueryVolumeSnapshotAction.Result res = action.call();
        System.out.println(res);
    }

    //更新云盘快照信息
    @Test
    public void snapshot2(){
        UpdateVolumeSnapshotAction action = new UpdateVolumeSnapshotAction();
        action.uuid = "e9aa20d8d89e427d9c056a1e3dc9f1dd";
        action.name = "My Snapshotddddddddd";
        action.sessionId = sessionId;
        UpdateVolumeSnapshotAction.Result res = action.call();

        System.out.println(res);
    }
    //将云盘回滚至某个指定的快照
    @Test
    public void snapshot3(){
        RevertVolumeFromSnapshotAction action = new RevertVolumeFromSnapshotAction();
        action.uuid = "e9aa20d8d89e427d9c056a1e3dc9f1dd";
        action.sessionId = sessionId;
        RevertVolumeFromSnapshotAction.Result res = action.call();

        System.out.println(res);
    }
    //将云盘回滚至某个指定的快照
    @Test
    public void snapshot4(){
        DeleteVolumeSnapshotAction action = new DeleteVolumeSnapshotAction();
        action.uuid = "e9aa20d8d89e427d9c056a1e3dc9f1dd";
        action.deleteMode = "Permissive";
        action.sessionId = sessionId;
        DeleteVolumeSnapshotAction.Result res = action.call();

        System.out.println(res);
    }

    //http://10.200.6.227:5000/vendor/console/vnc_auto.html?host=10.200.6.227&port=4900&token=6f7cdd13c7404019a0ac47e65d1dbc5f_f5ba0b0fd3b147728b5327d3340f50b6&title=vm1
    @Test
    public void url(){
        RequestConsoleAccessAction action = new RequestConsoleAccessAction();
        action.vmInstanceUuid = "f5ba0b0fd3b147728b5327d3340f50b6";
        action.sessionId = sessionId;
        RequestConsoleAccessAction.Result res = action.call();
        ConsoleInventory console = res.value.getInventory();
        String URL = String.format("%s://10.200.6.227:5000/vendor/console/vnc_auto.html?host=%s&port=%s&token=%s",console.getScheme(),console.getHostname(),console.getPort(),console.getToken()) + "&title=vmdddd1";
        System.out.println(URL);
    }

    @Test
    public void opvm(){
        StopVmInstanceAction action = new StopVmInstanceAction();
        action.uuid = "f5ba0b0fd3b147728b5327d3340f50b6";
        action.type = "grace";
        action.sessionId = sessionId;
        StopVmInstanceAction.Result res = action.call();
        if (res.error == null){
            System.out.println(res.value);
        }else {
            System.out.println(res.error.getDetails());
        }
    }
    @Test
    public void opvm2(){
        StartVmInstanceAction action = new StartVmInstanceAction();
        action.uuid = "f5ba0b0fd3b147728b5327d3340f50b6";
        action.sessionId = sessionId;
        StartVmInstanceAction.Result res = action.call();
        if (res.error == null){
            System.out.println(res.value);
        }else {
            System.out.println(res.error.getDetails());
        }
    }
    @Test
    public void opvm3(){
        PauseVmInstanceAction action = new PauseVmInstanceAction();
        action.uuid = "f5ba0b0fd3b147728b5327d3340f50b6";
        action.sessionId = sessionId;
        PauseVmInstanceAction.Result res = action.call();
        if (res.error == null){
            System.out.println(res.value);
        }else {
            System.out.println(res.error.getDetails());
        }
    }
}
