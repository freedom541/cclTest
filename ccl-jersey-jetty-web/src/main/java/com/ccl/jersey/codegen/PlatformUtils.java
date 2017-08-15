package com.ccl.jersey.codegen;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class PlatformUtils {
    private HashMap jdbcSubProtocolToPlatform = new HashMap();
    private HashMap jdbcDriverToPlatform = new HashMap();

    public PlatformUtils() {
        this.jdbcSubProtocolToPlatform.put("axiondb", "Axion");
        this.jdbcSubProtocolToPlatform.put("db2j:net", "Cloudscape");
        this.jdbcSubProtocolToPlatform.put("cloudscape:net", "Cloudscape");
        this.jdbcSubProtocolToPlatform.put("db2", "DB2");
        this.jdbcSubProtocolToPlatform.put("db2os390", "DB2");
        this.jdbcSubProtocolToPlatform.put("db2os390sqlj", "DB2");
        this.jdbcSubProtocolToPlatform.put("as400", "DB2");
        this.jdbcSubProtocolToPlatform.put("datadirect:db2", "DB2");
        this.jdbcSubProtocolToPlatform.put("inetdb2", "DB2");
        this.jdbcSubProtocolToPlatform.put("derby", "Derby");
        this.jdbcSubProtocolToPlatform.put("firebirdsql", "Firebird");
        this.jdbcSubProtocolToPlatform.put("hsqldb", "HsqlDb");
        this.jdbcSubProtocolToPlatform.put("interbase", "Interbase");
        this.jdbcSubProtocolToPlatform.put("sapdb", "SapDB");
        this.jdbcSubProtocolToPlatform.put("mckoi", "McKoi");
        this.jdbcSubProtocolToPlatform.put("microsoft:sqlserver", "MsSql");
        this.jdbcSubProtocolToPlatform.put("sqlserver", "MsSql");
        this.jdbcSubProtocolToPlatform.put("sqljdbc", "MsSql");
        this.jdbcSubProtocolToPlatform.put("datadirect:sqlserver", "MsSql");
        this.jdbcSubProtocolToPlatform.put("inetdae", "MsSql");
        this.jdbcSubProtocolToPlatform.put("inetdae6", "MsSql");
        this.jdbcSubProtocolToPlatform.put("inetdae7", "MsSql");
        this.jdbcSubProtocolToPlatform.put("inetdae7a", "MsSql");
        this.jdbcSubProtocolToPlatform.put("inetpool:inetdae", "MsSql");
        this.jdbcSubProtocolToPlatform.put("inetpool:inetdae6", "MsSql");
        this.jdbcSubProtocolToPlatform.put("inetpool:inetdae7", "MsSql");
        this.jdbcSubProtocolToPlatform.put("inetpool:inetdae7a", "MsSql");
        this.jdbcSubProtocolToPlatform.put("inetpool:jdbc:inetdae", "MsSql");
        this.jdbcSubProtocolToPlatform.put("inetpool:jdbc:inetdae6", "MsSql");
        this.jdbcSubProtocolToPlatform.put("inetpool:jdbc:inetdae7", "MsSql");
        this.jdbcSubProtocolToPlatform.put("inetpool:jdbc:inetdae7a", "MsSql");
        this.jdbcSubProtocolToPlatform.put("JSQLConnect", "MsSql");
        this.jdbcSubProtocolToPlatform.put("jtds:sqlserver", "MsSql");
        this.jdbcSubProtocolToPlatform.put("mysql", "MySQL");
        this.jdbcSubProtocolToPlatform.put("oracle:thin", "Oracle");
        this.jdbcSubProtocolToPlatform.put("oracle:oci8", "Oracle");
        this.jdbcSubProtocolToPlatform.put("oracle:dnldthin", "Oracle");
        this.jdbcSubProtocolToPlatform.put("datadirect:oracle", "Oracle");
        this.jdbcSubProtocolToPlatform.put("inetora", "Oracle");
        this.jdbcSubProtocolToPlatform.put("postgresql", "PostgreSql");
        this.jdbcSubProtocolToPlatform.put("sybase:Tds", "Sybase");
        this.jdbcSubProtocolToPlatform.put("datadirect:sybase", "Sybase");
        this.jdbcSubProtocolToPlatform.put("inetsyb", "Sybase");
        this.jdbcSubProtocolToPlatform.put("inetpool:inetsyb", "Sybase");
        this.jdbcSubProtocolToPlatform.put("inetpool:jdbc:inetsyb", "Sybase");
        this.jdbcSubProtocolToPlatform.put("jtds:sybase", "Sybase");
        this.jdbcDriverToPlatform.put("org.axiondb.jdbc.AxionDriver", "Axion");
        this.jdbcDriverToPlatform.put("com.ibm.db2.jcc.DB2Driver", "DB2");
        this.jdbcDriverToPlatform.put("COM.ibm.db2.jdbc.app.DB2Driver", "DB2");
        this.jdbcDriverToPlatform.put("COM.ibm.db2os390.sqlj.jdbc.DB2SQLJDriver", "DB2");
        this.jdbcDriverToPlatform.put("com.ibm.as400.access.AS400JDBCDriver", "DB2");
        this.jdbcDriverToPlatform.put("com.ddtek.jdbc.db2.DB2Driver", "DB2");
        this.jdbcDriverToPlatform.put("com.inet.drda.DRDADriver", "DB2");
        this.jdbcDriverToPlatform.put("org.apache.derby.jdbc.EmbeddedDriver", "Derby");
        this.jdbcDriverToPlatform.put("org.apache.derby.jdbc.ClientDriver", "Derby");
        this.jdbcDriverToPlatform.put("org.firebirdsql.jdbc.FBDriver", "Firebird");
        this.jdbcDriverToPlatform.put("org.hsqldb.jdbcDriver", "HsqlDb");
        this.jdbcDriverToPlatform.put("interbase.interclient.Driver", "Interbase");
        this.jdbcDriverToPlatform.put("com.sap.dbtech.jdbc.DriverSapDB", "SapDB");
        this.jdbcDriverToPlatform.put("com.mckoi.JDBCDriver", "McKoi");
        this.jdbcDriverToPlatform.put("com.microsoft.jdbc.sqlserver.SQLServerDriver", "MsSql");
        this.jdbcDriverToPlatform.put("com.microsoft.sqlserver.jdbc.SQLServerDriver", "MsSql");
        this.jdbcDriverToPlatform.put("com.ddtek.jdbc.sqlserver.SQLServerDriver", "MsSql");
        this.jdbcDriverToPlatform.put("com.inet.tds.TdsDriver", "MsSql");
        this.jdbcDriverToPlatform.put("com.jnetdirect.jsql.JSQLDriver", "MsSql");
        this.jdbcDriverToPlatform.put("com.mysql.jdbc.Driver", "MySQL");
        this.jdbcDriverToPlatform.put("org.gjt.mm.mysql.Driver", "MySQL");
        this.jdbcDriverToPlatform.put("oracle.jdbc.driver.OracleDriver", "Oracle");
        this.jdbcDriverToPlatform.put("oracle.jdbc.dnlddriver.OracleDriver", "Oracle");
        this.jdbcDriverToPlatform.put("com.ddtek.jdbc.oracle.OracleDriver", "Oracle");
        this.jdbcDriverToPlatform.put("com.inet.ora.OraDriver", "Oracle");
        this.jdbcDriverToPlatform.put("org.postgresql.Driver", "PostgreSql");
        this.jdbcDriverToPlatform.put("com.sybase.jdbc2.jdbc.SybDriver", "Sybase");
        this.jdbcDriverToPlatform.put("com.sybase.jdbc.SybDriver", "Sybase");
        this.jdbcDriverToPlatform.put("com.ddtek.jdbc.sybase.SybaseDriver", "Sybase");
        this.jdbcDriverToPlatform.put("com.inet.syb.SybDriver", "Sybase");
    }

    public String determineDatabaseType(DataSource dataSource) throws RuntimeException {
        return this.determineDatabaseType(dataSource, (String) null, (String) null);
    }

    public String determineDatabaseType(DataSource dataSource, String username, String password) throws RuntimeException {
        Connection connection = null;

        String var6;
        try {
            if (username != null) {
                connection = dataSource.getConnection(username, password);
            } else {
                connection = dataSource.getConnection();
            }

            DatabaseMetaData ex = connection.getMetaData();
            var6 = this.determineDatabaseType(ex.getDriverName(), ex.getURL());
        } catch (SQLException var15) {
            throw new RuntimeException("Error while reading the database metadata: " + var15.getMessage(), var15);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException var14) {
                    ;
                }
            }

        }

        return var6;
    }

    public String determineDatabaseType(String driverName, String jdbcConnectionUrl) {
        if (this.jdbcDriverToPlatform.containsKey(driverName)) {
            return (String) this.jdbcDriverToPlatform.get(driverName);
        } else if (jdbcConnectionUrl == null) {
            return null;
        } else {
            Iterator it = this.jdbcSubProtocolToPlatform.entrySet().iterator();

            Entry entry;
            String curSubProtocol;
            do {
                if (!it.hasNext()) {
                    return null;
                }

                entry = (Entry) it.next();
                curSubProtocol = "jdbc:" + (String) entry.getKey() + ":";
            } while (!jdbcConnectionUrl.startsWith(curSubProtocol));

            return (String) entry.getValue();
        }
    }
}

