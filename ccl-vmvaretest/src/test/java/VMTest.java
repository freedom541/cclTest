import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.ObjectContent;
import org.junit.Test;

/**
 * Created by ccl on 17/2/17.
 */
public class VMTest extends BaseConnection {

    @Test
    public void clonevm(){

        try {
            ObjectContent vm = conn.findObject("VirtualMachine", "sdf");
            ManagedObjectReference vmRef = vm.getObj();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
