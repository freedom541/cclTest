import com.vmware.utils.VMwareConnection;
import org.junit.BeforeClass;

/**
 * Created by ccl on 17/2/17.
 */
public class BaseConnection {
    public static com.vmware.utils.VMwareConnection conn;
    private static String serverName = "10.200.6.92:443";
    private static String userName = "administrator@vsphere.local";
    private static String password = "Wb1234==";
    @BeforeClass
    public static void init() throws Exception {
        conn = new VMwareConnection(serverName, userName,password);
    }
}
