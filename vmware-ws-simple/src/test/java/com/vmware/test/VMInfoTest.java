package com.vmware.test;

import com.vmware.connection.BasicConnection;
import com.vmware.general.GetUpdates;
import com.vmware.vim25.InvalidCollectorVersionFaultMsg;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.RuntimeFaultFaultMsg;

import java.io.IOException;

/**
 * Created by ccl on 17/2/17.
 */
public class VMInfoTest {
    public static void main(String[] args) throws InvalidPropertyFaultMsg, InvalidCollectorVersionFaultMsg, RuntimeFaultFaultMsg, IOException {
        BasicConnection connect = new BasicConnection();
        connect.setPassword("Wb1234==");
        connect.setUrl("https://10.200.6.92:443/sdk/vimService");
        connect.setUsername("administrator@vsphere.local");


        GetUpdates get = new GetUpdates();
        get.setVmname("sdf");
        get.setConnection(connect);
        get.connect();
        get.action();
        get.disconnect();
    }
}
