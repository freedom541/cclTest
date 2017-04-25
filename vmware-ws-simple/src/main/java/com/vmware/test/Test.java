package com.vmware.test;

import com.vmware.connection.BasicConnection;
import com.vmware.connection.Connection;
import com.vmware.vim25.*;

import java.util.List;

/**
 * Created by ccl on 17/2/15.
 */
public class Test {
//    public static void main(String[] args)  throws TaskInProgressFaultMsg, RemoteException, RuntimeFaultFaultMsg, DuplicateNameFaultMsg, OutOfBoundsFaultMsg, InvalidPropertyFaultMsg, FileFaultFaultMsg, InvalidCollectorVersionFaultMsg, InvalidDatastoreFaultMsg, AlreadyExistsFaultMsg, VmConfigFaultFaultMsg, InsufficientResourcesFaultFaultMsg, InvalidNameFaultMsg, InvalidStateFaultMsg {
//        VMCreate vmcreate=new VMCreate();
//        vmcreate.setDataCenterName("zhuyun");
//        vmcreate.setHostname("10.200.6.81");
//        vmcreate.setVirtualMachineName("ideaTest");
//        vmcreate.setDataStore("datastore1");
//        vmcreate.setHostConnection(true);
//        BasicConnection connect=new BasicConnection();
//        connect.setPassword("Wb1234==");
//        connect.setUrl("https://10.200.6.92:443/sdk/vimService");
//        connect.setUsername("administrator@vsphere.local");
//        vmcreate.setConnection(connect);
//        vmcreate.connect();
//        vmcreate.createVirtualMachine();
//        vmcreate.disconnect();
//    }


    @org.junit.Test
    public void testVMs() throws NotFoundFaultMsg, HostConfigFaultFaultMsg, RuntimeFaultFaultMsg {
        BasicConnection connect=new BasicConnection();
        connect.setPassword("Wb1234==");
        connect.setUrl("https://10.200.6.92:443/sdk/vimService");
        connect.setUsername("administrator@vsphere.local");
        Connection conn = connect.connect();
        ManagedObjectReference reference = conn.getServiceContent().getRootFolder();
        VimPortType type = conn.getVimPort();
        List<VmfsDatastoreOption> list = type.queryVmfsDatastoreCreateOptions(reference, "zhuyun", 0);
        System.out.println(reference);

    }
}
