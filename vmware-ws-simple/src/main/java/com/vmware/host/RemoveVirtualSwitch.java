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

import javax.xml.ws.soap.SOAPFaultException;
import java.util.Map;

/**
 * <pre>
 * RemoveVirtualSwitch
 *
 * This sample removes a virtual switch
 *
 * <b>Parameters:</b>
 * url             [required] : url of the web service
 * username        [required] : username for the authentication
 * password        [required] : password for the authentication
 * vswitchid       [required] : Name of the switch to be added
 * hostname        [required] : Name of the host
 *
 * <b>Command Line:</b>
 * Remove a Virtual Switch
 * run.bat com.vmware.host.RemoveVirtualSwitch --url [webserviceurl]
 * --username [username] --password  [password]
 * --vswitchid [mySwitch] --hostname [hostname]
 * </pre>
 */

@Sample(name = "remove-virtual-switch", description = "removes a virtual switch")
public class RemoveVirtualSwitch extends ConnectedVimServiceBase {
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

    void removeVirtualSwitch() throws InvalidPropertyFaultMsg,
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
                vimPort.removeVirtualSwitch(nwSystem, virtualswitchid);
                System.out.println(" : Successful removing : " + virtualswitchid);
            } catch (HostConfigFaultFaultMsg ex) {
                System.out.println(" : Failed : Configuration falilures. ");
            } catch (NotFoundFaultMsg ex) {
                System.out.println("Failed : " + ex);
            } catch (ResourceInUseFaultMsg ex) {
                System.out.println(" : Failed removing switch " + virtualswitchid);
                System.out.println("There are virtual network adapters "
                        + "associated with the virtual switch.");
            } catch (RuntimeFaultFaultMsg ex) {
                System.out.println(" : Failed removing switch: " + virtualswitchid);
            } catch (SOAPFaultException sfe) {
                printSoapFaultException(sfe);
            }
        } else {
            System.out.println("Host not found");
        }
    }

    void printSoapFaultException(SOAPFaultException sfe) {
        System.out.println("SOAP Fault -");
        if (sfe.getFault().hasDetail()) {
            System.out.println(sfe.getFault().getDetail().getFirstChild()
                    .getLocalName());
        }
        if (sfe.getFault().getFaultString() != null) {
            System.out.println("\n Message: " + sfe.getFault().getFaultString());
        }
    }

    @Action
    public void run() throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        removeVirtualSwitch();
    }

}
