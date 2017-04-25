package com.vmware.resourcepool;

import com.vmware.connection.BasicConnection;
import com.vmware.connection.Connection;

import java.sql.SQLException;

/**
 * Created by ccl on 17/3/13.
 */
public class VmwareConnection extends MyConnectionPool<Connection> {

    @Override
    protected Connection create() {
        BasicConnection connect = new BasicConnection();
        connect.setPassword("Wb1234==");
        connect.setUrl("https://10.200.6.92:443/sdk/vimService");
        connect.setUsername("administrator@vsphere.local");
        return connect.connect();
    }

    @Override
    public boolean validate(Connection o) {
        return o.isConnected();
    }

    @Override
    public void expire(Connection o) {
        o.disconnect();
    }

    public static void main(String args[]) throws SQLException {
        VmwareConnection pool = new VmwareConnection();

        for (int i = 0; i < 1000; i++){
            Connection con = pool.checkOut();
            System.out.println(con.getHost());
        }
        //pool.checkIn(con);

    }
}
