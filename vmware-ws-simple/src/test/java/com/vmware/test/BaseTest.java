package com.vmware.test;

import com.vmware.connection.BasicConnection;
import org.junit.BeforeClass;

/**
 * Created by ccl on 17/2/15.
 */
public class BaseTest {
    public static BasicConnection connect = null;
    @BeforeClass
    public static void start(){
        connect = new BasicConnection();
        connect.setPassword("Wb1234==");
        connect.setUrl("https://10.200.6.92:443/sdk/vimService");
        connect.setUsername("administrator@vsphere.local");
       /* try {
            connect.connect();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }*/
    }

    /*@AfterClass
    public void end(){
        try {
            if(connect != null)
                connect.disconnect();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }*/


    public void testdd(){
        String nnn = "https://10.200.6.92:7343/console/?vmId=vm-330&vmName=ilYEnU%40iIF50&host=10.200.6.92:443&sessionTicket=cst-VCT-52389f83-de49-6544-f7c2-75317b27e106--tp-58-75-53-03-DD-BB-97-BB-B9-79-35-DB-D5-B4-2F-DD-68-45-69-59&thumbprint=58:75:53:03:DD:BB:97:BB:B9:79:35:DB:D5:B4:2F:DD:68:45:69:59";
        String sss = "https://10.200.6.92:7343/console/?vmId=vm-303&vmName=A6n*6GA6j9Xw&host=10.200.6.92:443&sessionTicket=cst-VCT-52328044-a02e-2520-fd32-044068ed76ac--tp-58-75-53-03-DD-BB-97-BB-B9-79-35-DB-D5-B4-2F-DD-68-45-69-59&thumbprint=58:75:53:03:DD:BB:97:BB:B9:79:35:DB:D5:B4:2F:DD:68:45:69:59";
        String str = "https://10.200.6.92:7343/console/?" +
                "vmId=vm-303" +
                "&" +
                "vmName=A6n*6GA6j9Xw" +
                "&" +
                "host=10.200.6.92:443" +
                "&" +
                "sessionTicket=cst-VCT-52328044-a02e-2520-fd32-044068ed76ac--tp-58-75-53-03-DD-BB-97-BB-B9-79-35-DB-D5-B4-2F-DD-68-45-69-59" +
                "&" +
                "thumbprint=58:75:53:03:DD:BB:97:BB:B9:79:35:DB:D5:B4:2F:DD:68:45:69:59";
    }
}
