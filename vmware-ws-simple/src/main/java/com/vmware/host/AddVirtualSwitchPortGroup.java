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
 * AddVirtualSwitchPortGroup
 *
 * This sample is used to add a Virtual Machine Port Group to a vSwitch
 *
 * <b>Parameters:</b>
 * url              [required] : url of the web service
 * username         [required] : username for the authentication
 * password         [required] : password for the authentication
 * vswitchid        [required] : Name of the vSwitch to add portgroup to
 * portgroupname    [required] : Name of the port group
 * hostname         [optional] : Name of the host
 *
 * <b>Command Line:</b>
 * Add Virtual switch Port Group:
 * run.bat com.vmware.host.AddVirtualSwitchPortGroup
 * --url [webserviceurl] --username [username] --password  [password]
 * --vswitchid [mySwitch] --portgroupname [myportgroup] --hostname [hostname]
 * </pre>
 */

@Sample(name = "add-virtual-switch-port-group", description = "add a Virtual Machine Port Group to a vSwitch")
public class AddVirtualSwitchPortGroup extends ConnectedVimServiceBase {

    private String host;
    private String portgroupname;
    private String virtualswitchid;

    @Option(name = "hostname", required = false, description = "Name of the host")
    public void setHost(String host) {
        this.host = host;
    }

    @Option(name = "portgroupname", description = "Name of the port group")
    public void setPortgroupname(String portgroupname) {
        this.portgroupname = portgroupname;
    }

    @Option(name = "vswitchid", description = "Name of the vSwitch to add portgroup to")
    public void setVirtualswitchid(String virtualswitchid) {
        this.virtualswitchid = virtualswitchid;
    }

    void addVirtualSwitchPortGroup()
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
        Map<String, ManagedObjectReference> hostList =
                getMOREFs.inFolderByType(serviceContent.getRootFolder(),
                        "HostSystem");
        ManagedObjectReference hostmor = hostList.get(host);
        try {
            if (hostmor != null) {
                HostConfigManager configMgr =
                        (HostConfigManager) getMOREFs.entityProps(hostmor,
                                new String[]{"configManager"}).get("configManager");
                ManagedObjectReference nwSystem = configMgr.getNetworkSystem();

                HostPortGroupSpec portgrp = new HostPortGroupSpec();
                portgrp.setName(portgroupname);
                portgrp.setVswitchName(virtualswitchid);
                portgrp.setPolicy(new HostNetworkPolicy());

                vimPort.addPortGroup(nwSystem, portgrp);

                System.out.println("Successfully created : " + virtualswitchid
                        + "/" + portgroupname);
            } else {
                System.out.println("Host not found");
            }
        } catch (AlreadyExistsFaultMsg ex) {
            System.out.println("Failed creating : " + virtualswitchid + "/"
                    + portgroupname);
            System.out.println("Portgroup name already exists");
        } catch (HostConfigFaultFaultMsg ex) {
            System.out.println("Failed : Configuration failures. " + " Reason : "
                    + ex.getMessage());
        } catch (RuntimeFaultFaultMsg ex) {
            System.out.println("Failed creating : " + virtualswitchid + "/"
                    + portgroupname);
        } catch (Exception ex) {
            System.out.println("Failed creating : " + virtualswitchid + "/"
                    + portgroupname);
        }
    }


    @Action
    public void run() throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        addVirtualSwitchPortGroup();
    }
}
