package com.vmware.test;

import com.vmware.connection.ConnectedVimServiceBase;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.RuntimeFaultFaultMsg;

/**
 * Created by ccl on 17/4/5.
 */
public class GetVmConsoleUrl extends ConnectedVimServiceBase {
    //https://10.200.6.92:7343/console/?
    // vmId=vm-327
    // &vmName=jQgnWCclYr8k
    // &host=10.200.6.92:443
    // &sessionTicket=cst-VCT-5262ba80-a4a0-36a0-7e7c-baa31dd6d16b--tp-58-75-53-03-DD-BB-97-BB-B9-79-35-DB-D5-B4-2F-DD-68-45-69-59
    // &thumbprint=58:75:53:03:DD:BB:97:BB:B9:79:35:DB:D5:B4:2F:DD:68:45:69:59

    private String baseUrl = "https://10.200.6.92:7343/console/?vmId=%s&vmName=%s&host=%s&sessionTicket=%s&thumbprint=%s";
    private String vmId;
    private String vmName;
    private String host = "10.200.6.92:443";
    private String sessionTicket;
    private String thumbprint;

    public void setVmName(String vmName) {
        this.vmName = vmName;
    }

    public String getVmUrl() throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
        ManagedObjectReference vmRef = getMOREFs.vmByVMname(vmName, serviceContent.getPropertyCollector());
        if (vmRef != null){
            vmId = vmRef.getValue();
        }
        String acquireCloneTicket = vimPort.acquireCloneTicket(serviceContent.getSessionManager());
        sessionTicket = acquireCloneTicket;
        String[] str =acquireCloneTicket.split("--tp-");
        thumbprint = str[1].replaceAll("-",":");
        return String.format(baseUrl,vmId,vmName,host,sessionTicket,thumbprint);
    }
}
