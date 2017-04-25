package com.ccl.test.resourcePool;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by ccl on 17/3/13.
 */
public class JDBCConnectionPool extends ObjectPool<Connection> {

    private String dsn, usr, pwd;

    public JDBCConnectionPool(String driver, String dsn, String usr, String pwd) {
        super();
        try {
            Class.forName(driver).newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.dsn = dsn;
        this.usr = usr;
        this.pwd = pwd;
    }

    @Override
    protected Connection create() {
        try {
            return (DriverManager.getConnection(dsn, usr, pwd));
        } catch (SQLException e) {
            e.printStackTrace();
            return (null);
        }
    }

    @Override
    public void expire(Connection o) {
        try {
            ((Connection) o).close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean validate(Connection o) {
        try {
            return (!((Connection) o).isClosed());
        } catch (SQLException e) {
            e.printStackTrace();
            return (false);
        }
    }

    public static void main(String args[]) throws SQLException {
        JDBCConnectionPool pool = new JDBCConnectionPool("com.mysql.jdbc.Driver",
                "jdbc:mysql://db.cloudcare.com:3306/cbis_billing?zeroDateTimeBehavior=convertToNull", "cbis", "zy#CBIS@2");

        for (int i = 0; i < 1000; i++){
            Connection con = pool.checkOut();
            System.out.println(con.getCatalog());
        }
        //pool.checkIn(con);

    }
}