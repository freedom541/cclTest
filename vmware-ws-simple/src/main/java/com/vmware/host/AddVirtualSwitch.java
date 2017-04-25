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
 * AddVirtualSwitch
 *
 * This sample is used to add a virtual switch
 *
 * <b>Parameters:</b>
 * url              [required] : url of the web service
 * username         [required] : username for the authentication
 * password         [required] : password for the authentication
 * vswitchid        [required]: Name of the switch to be added
 * hostname         [required]: Name of the host
 *
 * <b>Command Line:</b>
 * Add a Virtual Switch
 * run.bat com.vmware.host.AddVirtualSwitch --url [webserviceurl]
 * --username [username] --password  [password] --hostname [hostname]
 * --vswitchid [mySwitch]
 * </pre>
 */
@Sample(
        name = "add-virtual-switch",
        description = "This sample is used to add a virtual switch"
)
public class AddVirtualSwitch extends ConnectedVimServiceBase {

    private String host;
    private String virtualswitchid;

    @Option(name = "hostname", description = "Name of the host")
    public void setHost(String host) {
        this.host = host;
    }

    @Option(name = "vswitchid", description = "Name of the switch to be added")
    public void setVirtualswitchid(String virtualswitchid) {
        this.virtualswitchid = virtualswitchid;
    }

    void addVirtualSwitch() throws InvalidPropertyFaultMsg,
            RuntimeFaultFaultMsg {
        Map<String, ManagedObjectReference> hostList =
                getMOREFs.inFolderByType(serviceContent.getRootFolder(),
                        "HostSystem");
        ManagedObjectReference hostmor = hostList.get(host);
        if (hostmor != null) {
            try {
                HostConfigManager configMgr =
                        (HostConfigManager) getMOREFs.entityProps(hostmor,
                                new String[]{"configManager"}).get("configManager");
                ManagedObjectReference nwSystem = configMgr.getNetworkSystem();
                HostVirtualSwitchSpec spec = new HostVirtualSwitchSpec();
                spec.setNumPorts(8);
                vimPort.addVirtualSwitch(nwSystem, virtualswitchid, spec);
                System.out.println("Successfully created vswitch : "
                        + virtualswitchid);
            } catch (AlreadyExistsFaultMsg ex) {
                System.out.println("Failed : Switch already exists " + " Reason : "
                        + ex.getMessage());
            } catch (HostConfigFaultFaultMsg ex) {
                System.out.println("Failed : Configuration failures. "
                        + " Reason : " + ex.getMessage());
            } catch (ResourceInUseFaultMsg ex) {
                System.out.println("Failed adding switch: " + virtualswitchid
                        + " Reason : " + ex.getMessage());
            } catch (RuntimeFaultFaultMsg ex) {
                System.out.println("Failed adding switch: " + virtualswitchid
                        + " Reason : " + ex.getMessage());
            } catch (Exception ex) {
                System.out.println("Failed adding switch: " + virtualswitchid
                        + " Reason : " + ex.getMessage());
            }
        } else {
            System.out.println("Host not found");
        }
    }

    @Action
    public void run() throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        addVirtualSwitch();
    }
}
