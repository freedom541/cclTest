package com.vmware.test;

import com.vmware.events.VMEventHistoryCollectorMonitor;
import com.vmware.general.*;
import com.vmware.host.AddVirtualNic;
import com.vmware.host.DVSCreate;
import com.vmware.performance.Basics;
import com.vmware.performance.History;
import com.vmware.vim25.*;
import com.vmware.vm.*;
import org.junit.Test;

import javax.xml.datatype.DatatypeConfigurationException;
import java.awt.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.Map;

/**
 * Created by ccl on 17/2/15.
 */
public class VMOptions extends BaseTest{
    @Test
    public void vmcreate() throws TaskInProgressFaultMsg, RemoteException, RuntimeFaultFaultMsg, DuplicateNameFaultMsg, OutOfBoundsFaultMsg, InvalidPropertyFaultMsg, FileFaultFaultMsg, InvalidCollectorVersionFaultMsg, InvalidDatastoreFaultMsg, AlreadyExistsFaultMsg, VmConfigFaultFaultMsg, InsufficientResourcesFaultFaultMsg, InvalidNameFaultMsg, InvalidStateFaultMsg {
        VMCreatetest vmcreate=new VMCreatetest();
        vmcreate.setDataCenterName("zhuyun");
        vmcreate.setHostname("10.200.6.81");
        vmcreate.setVirtualMachineName("avad");
        //vmcreate.setDataStore("datastore1");
        vmcreate.setHostConnection(true);
        vmcreate.setConnection(connect);
        vmcreate.connect();
        vmcreate.createVirtualMachine();
        vmcreate.disconnect();
    }

    @Test
    public void power() throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg, IOException {
        VMPowerOps op = new VMPowerOps();
        op.setOperation("poweron");  //[poweron | poweroff | reset | suspend | reboot | shutdown | standby]
        op.setDatacenter("zhuyun");
        op.setHost("10.200.6.81");
        op.setVmName("fds");
        op.setAll(false);
        op.setConnection(connect);
        op.connect();
        op.run();
        op.disconnect();

    }

    @Test
    public void vms() throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg, IOException {
        VMPowerOps op = new VMPowerOps();
        op.setDatacenter("zhuyun");
        op.setHost("10.200.6.81");
        op.setConnection(connect);
        op.connect();
        Map<String, ManagedObjectReference> vmMap = op.getVms();
        op.disconnect();
        for (Map.Entry<String, ManagedObjectReference> entry : vmMap.entrySet()){
            System.out.println(entry.getKey());
        }
    }

    @Test
    public void vminfo() throws DatatypeConfigurationException, InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, IOException, InvalidCollectorVersionFaultMsg {
        GetUpdates get = new GetUpdates();
        get.setVmname("cc");
        //get.setVmname("500513ce-129e-6c6e-86aa-b258dd06d3e5");
        get.setConnection(connect);
        get.connect();
        get.action();
        get.disconnect();
    }

    @Test
    public void getVmConsoleUrl() throws DatatypeConfigurationException, InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, IOException, InvalidCollectorVersionFaultMsg {
        GetVmConsoleUrl get = new GetVmConsoleUrl();
        get.setVmName("jQgnWCclYr8k");
        get.setConnection(connect);
        get.connect();
        String url = get.getVmUrl();
        //get.disconnect();
        //启用系统默认浏览器来打开网址。
        try {
            URI uri = new URI(url);
            Desktop.getDesktop().browse(uri);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("url: " + url);
    }

    @Test
    public void vmsnapshot() throws InvalidCollectorVersionFaultMsg, TaskInProgressFaultMsg, InvalidStateFaultMsg, InsufficientResourcesFaultFaultMsg, InvalidNameFaultMsg, FileFaultFaultMsg, RuntimeFaultFaultMsg, InvalidPropertyFaultMsg, SnapshotFaultFaultMsg, VmConfigFaultFaultMsg {
        VMSnapshot snapshot = new VMSnapshot();
        snapshot.setVirtualMachineName("sdf");
        snapshot.setOperation("revert"); //[list|create|remove|revert]
        snapshot.setSnapshotname("kuaizhao");
        snapshot.setDescription("this is test");
        snapshot.setConnection(connect);
        snapshot.connect();
        snapshot.run();
        snapshot.disconnect();
    }


    @Test
    public void vcenterInfo(){
        Info info = new Info();
        info.setConnection(connect);
        info.connect();
        info.main();
        info.disconnect();
    }


    @Test
    public void vmdel() throws RemoteException, InvalidCollectorVersionFaultMsg, InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, VimFaultFaultMsg {
        Delete del = new Delete();
        del.setManagedEntityName("bgff");
        del.setConnection(connect);
        del.connect();
        del.deleteManagedEntity();
        del.disconnect();
    }

    @Test
    public void time() throws RuntimeFaultFaultMsg {
        GetCurrentTime time = new GetCurrentTime();
        time.setConnection(connect);
        time.connect();
        time.getCurrentTime();
        time.disconnect();
    }


    @Test
    public void hostinfo() throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
        GetHostName info = new GetHostName();
        info.setConnection(connect);
        info.connect();
        info.printHostProductDetails();
        info.disconnect();
    }

    @Test
    public void info(){
        try {
            Info info = new Info();
            info.setConnection(connect);
            info.main();
            info.disconnect();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void rename() throws InvalidPropertyFaultMsg, DuplicateNameFaultMsg, InvalidNameFaultMsg, RuntimeFaultFaultMsg, InvalidCollectorVersionFaultMsg {
        Rename name = new Rename();
        name.setEntityname("sdf");
        name.setNewentityname("sdf");
        name.setConnection(connect);
        name.connect();
        name.rename();
        name.disconnect();
    }


    @Test
    public void testSimpleClient() throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        SimpleClient client = new SimpleClient();
        client.setConnection(connect);
        client.connect();
        client.main();
        client.disconnect();
    }

    //Distributed Switch
    @Test
    public void dvs_create() throws InvalidNameFaultMsg, NotFoundFaultMsg, DuplicateNameFaultMsg, InvalidCollectorVersionFaultMsg, RemoteException, InvalidPropertyFaultMsg, DvsNotAuthorizedFaultMsg, DvsFaultFaultMsg, RuntimeFaultFaultMsg {
        DVSCreate create = new DVSCreate();
        create.setOption("createdvs");
        create.setDcName("zhuyun");
        create.setDvsName("tt");
        create.setDvsDesc("test");
        create.setDvsVersion("5.0.0");
        create.setConnection(connect);
        create.connect();
        create.run();
        create.disconnect();
    }


    @Test
    public void brower() throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
        Browser browser = new Browser();
        browser.setConnection(connect);
        browser.connect();
        browser.printInventory();
        browser.disconnect();
    }

    @Test
    public void propertyCollector() throws InvalidStateFaultMsg, InsufficientResourcesFaultFaultMsg, TaskInProgressFaultMsg, DuplicateNameFaultMsg, ConcurrentAccessFaultMsg, InvalidNameFaultMsg, FileFaultFaultMsg, InvalidCollectorVersionFaultMsg, InvalidPropertyFaultMsg, InvalidDatastoreFaultMsg, VmConfigFaultFaultMsg, RuntimeFaultFaultMsg {
        PropertyCollector pc = new PropertyCollector();
        pc.setExtension("updates");
        pc.setVmName("sdf");
        pc.setUpdateType("checkforupdates");
        pc.setConnection(connect);
        pc.connect();
        pc.main();
        pc.disconnect();
    }

    @Test
    public void reconfig() throws InvalidStateFaultMsg, InsufficientResourcesFaultFaultMsg, TaskInProgressFaultMsg, DuplicateNameFaultMsg, ConcurrentAccessFaultMsg, InvalidNameFaultMsg, FileFaultFaultMsg, RuntimeFaultFaultMsg, InvalidPropertyFaultMsg, InvalidDatastoreFaultMsg, VmConfigFaultFaultMsg, InvalidCollectorVersionFaultMsg {
        VMReconfig vmReconfig = new VMReconfig();
        vmReconfig.setVmName("test2");
        vmReconfig.setOperation("add");
        vmReconfig.setDevice("disk");
        vmReconfig.setValue("xxsxx");
        vmReconfig.setDisksize(String.valueOf(20*1024));
        vmReconfig.setDiskmode("independent_persistent");
        vmReconfig.setConnection(connect);
        vmReconfig.connect();
        vmReconfig.run();
        vmReconfig.disconnect();
    }
    @Test
    public void gehostname() throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
        GetHostName gh = new GetHostName();
        gh.setConnection(connect);
        gh.connect();
        gh.printHostProductDetails();
        gh.disconnect();
    }

    @Test
    public void searchindex() throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        SearchIndex index = new SearchIndex();
        index.setDcName("zhuyun");
        index.setHostDnsName("10.200.6.81");
        index.setConnection(connect);
        index.connect();
        index.action();
        index.disconnect();
    }

    @Test
    public void addnic() throws AlreadyExistsFaultMsg, RuntimeFaultFaultMsg, HostConfigFaultFaultMsg, InvalidPropertyFaultMsg, IllegalAccessException, NoSuchMethodException, InvocationTargetException, InvalidStateFaultMsg {
        AddVirtualNic nic = new AddVirtualNic();
        nic.setDatacentername("zhuyun");
        nic.setHostname("10.200.6.81");
        nic.setPortgroupname("zhangsan");
        nic.setIpaddress("192.168.9.1");
        nic.setConnection(connect);
        nic.connect();
        nic.run();
        nic.disconnect();
    }

    @Test
    public void history() throws Exception {
        History history = new History();
        history.setHostname("10.200.6.81");
        history.setInterval("300");
        history.setStarttime(0);
        history.setDuration(0);
        //mem.usage.AVERAGE
        //disk.provisioned.LATEST
        //cpu.usage.AVERAGE
//        history.setGroupname("cpu");
//        history.setCountername("usage");
//        history.setGroupname("disk");
//        history.setCountername("usage");
        history.setGroupname("mem");
        history.setCountername("usage");
        history.setConnection(connect);
        history.connect();
        history.run();
        history.disconnect();
    }

    @Test
    public void basic() throws DatatypeConfigurationException, InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
        Basics basics = new Basics();
        basics.setInfo("host");
        basics.setHostname("10.200.6.81");
        basics.setConnection(connect);
        basics.connect();
        basics.run();
        basics.disconnect();
    }

    @Test
    public void performance() throws Exception {
        GetPerformance performance = new GetPerformance();
        performance.setConnection(connect);
        performance.connect();
        performance.perfCountersInfo();
        performance.disconnect();
    }
    @Test
    public void searchEntityTest() throws Exception {
        SearchEntityTest test = new SearchEntityTest();
        test.setConnection(connect);
        test.connect();
        //test.getSnapshot();
        //test.getVMS();
        //test.getDataStore();
        test.getHostSystem();
        //test.getClusterComputeResource();
        //test.getDataCenter();
        //test.getNetwork();
        //test.getNetwork2();
        //test.disconnect();
        //test.getDPortGroup();
        //test.gethostsysteminfo();
    }

    @Test
    public void historyMonitor() throws InvalidPropertyFaultMsg, InvalidStateFaultMsg, RuntimeFaultFaultMsg {
//        EventHistoryCollectorMonitor monitor = new EventHistoryCollectorMonitor();
//        monitor.setConnection(connect);
//        monitor.connect();
//        monitor.run();
//        monitor.disconnect();

        VMEventHistoryCollectorMonitor monitor = new VMEventHistoryCollectorMonitor();
        monitor.setVmName("all");
        monitor.setConnection(connect);
        monitor.connect();
        monitor.run();
        monitor.disconnect();
    }

    @Test
    public void mydisktest() throws InvalidDatastoreFaultMsg, FileFaultFaultMsg, InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InvalidCollectorVersionFaultMsg, InvalidDiskFormatFaultMsg {
        MyDiskTest test = new MyDiskTest();
        test.setVmName("all");
        test.setDataCenterName("zhuyun");
        test.setConnection(connect);
        test.connect();
        //test.queryVirtualDiskUuid();
        //test.copyDatastoreFile();
        test.copyvirtualdisk();
        test.disconnect();

    }

    @Test
    public void linkeClone() throws InsufficientResourcesFaultFaultMsg, MigrationFaultFaultMsg, TaskInProgressFaultMsg, InvalidStateFaultMsg, CustomizationFaultFaultMsg, FileFaultFaultMsg, InvalidCollectorVersionFaultMsg, InvalidPropertyFaultMsg, InvalidDatastoreFaultMsg, VmConfigFaultFaultMsg, RuntimeFaultFaultMsg {
        VMLinkedClone clone = new VMLinkedClone();
        clone.setConnection(connect);
        clone.setVirtualMachineName("all");
        clone.setSnapshotName("acc");
        clone.setCloneName("jbk");
        clone.connect();
        clone.run();
        clone.disconnect();
    }

    @Test
    public void datastorefile() throws Exception {
        MyDataStoreFile test = new MyDataStoreFile();
        test.setConnection(connect);
        test.connect();
        test.getfiles();
        test.disconnect();

    }

    @Test
    public void searchindexdemo() throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
        SearchIndexDemo demo = new SearchIndexDemo();
        demo.setConnection(connect);
        demo.connect();
        demo.findbyuuid();
        demo.findallbyuuid();
        demo.findchild();
        demo.disconnect();
    }

    @Test
    public void testGetMOREF() throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
        TestGetMOREF moref = new TestGetMOREF();
        moref.setConnection(connect);
        moref.connect();
        moref.testinContainerByType2();
        moref.testinContainerByType3();
    }


    @Test
    public void copyData() throws InvalidDatastoreFaultMsg, FileFaultFaultMsg, InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InvalidCollectorVersionFaultMsg, InvalidDiskFormatFaultMsg {
        VirtualDiskManagerTest test = new VirtualDiskManagerTest();
        test.setConnection(connect);
        test.connect();
        //test.copyvirtualdisk();
        //test.cloneVmbymodel();
        test.mark();
        test.disconnect();
    }

    @Test
    public void TestVMAction(){
        TestVMAction action = new TestVMAction();
        action.setConnection(connect);
        action.connect();
        action.cloneVM();
        action.disconnect();
    }

    @Test
    public void testMysearch(){
        MySearch my = new MySearch();
        my.setConnection(connect);
        my.connect();
        my.findByInventoryPath();
        my.disconnect();
    }
}
