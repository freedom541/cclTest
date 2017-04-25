/*
 * ******************************************************
 * Copyright VMware, Inc. 2010-2012.  All Rights Reserved.
 * ******************************************************
 *
 * DISCLAIMER. THIS PROGRAM IS PROVIDED TO YOU "AS IS" WITHOUT
 * WARRANTIES OR CONDITIONS # OF ANY KIND, WHETHER ORAL OR WRITTEN,
 * EXPRESS OR IMPLIED. THE AUTHOR SPECIFICALLY # DISCLAIMS ANY IMPLIED
 * WARRANTIES OR CONDITIONS OF MERCHANTABILITY, SATISFACTORY # QUALITY,
 * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.vmware.host;

import com.vmware.common.annotations.Action;
import com.vmware.common.annotations.Option;
import com.vmware.common.annotations.Sample;
import com.vmware.connection.ConnectedVimServiceBase;
import com.vmware.vim25.*;

import java.util.Map;

/**
 * <pre>
 * RemoveVirtualSwitchPortGroup
 *
 * This sample removes a Virtual Switch PortGroup
 *
 * <b>Parameters:</b>
 * url             [required] : url of the web service
 * username        [required] : username for the authentication
 * password        [required] : password for the authentication
 * portgroupname   [required] : Name of the port group to be removed
 * hostname        [required] : Name of the host
 *
 * <b>Command Line:</b>
 * Remove Virtual Switch Port Group
 * run.bat com.vmware.host.RemoveVirtualSwitchPortGroup
 * --url [webserviceurl] --username [username] --password [password]
 * --portgroupname[myportgroup] --hostname [hostname]
 *
 * </pre>
 */
@Sample(
        name = "remove-virtual-switch-port-group",
        description = "This sample removes a Virtual Switch PortGroup"
)
public class RemoveVirtualSwitchPortGroup extends ConnectedVimServiceBase {

    private String host;
    private String portgroupname;

    @Option(name = "hostname", description = "Name of the host")
    public void setHost(String host) {
        this.host = host;
    }

    @Option(name = "portgroupname", description = "Name of the port group to be removed")
    public void setPortgroupname(String portgroupname) {
        this.portgroupname = portgroupname;
    }

    void removeVirtualSwitchPortGroup()
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
        Map<String, ManagedObjectReference> hostList =
                getMOREFs.inFolderByType(serviceContent.getRootFolder(),
                        "HostSystem");
        ManagedObjectReference hostmor = hostList.get(host);

        if (hostmor != null) {
            try {
                HostConfigManager configMgr =
                        (HostConfigManager) getMOREFs.entityProps(hostmor,
                                new String[]{"configManager"}).get("configManager");
                vimPort
                        .removePortGroup(configMgr.getNetworkSystem(), portgroupname);
                System.out.println("Successfully removed portgroup "
                        + portgroupname);
            } catch (HostConfigFaultFaultMsg ex) {
                System.out.println("Failed removing " + portgroupname);
                System.out.println("Datacenter or Host may be invalid \n");
            } catch (NotFoundFaultMsg ex) {
                System.out.println("Failed removing " + portgroupname);
                System.out.println(" portgroup not found.\n");
            } catch (ResourceInUseFaultMsg ex) {
                System.out.println("Failed removing portgroup " + portgroupname);
                System.out.println("port group can not be removed because "
                        + "there are virtual network adapters associated with it.");
            } catch (RuntimeFaultFaultMsg ex) {
                System.out.println("Failed removing " + portgroupname
                        + ex.getMessage());
            }
        } else {
            System.out.println("Host not found");
        }

    }

    @Action
    public void run() throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        removeVirtualSwitchPortGroup();
    }

}
