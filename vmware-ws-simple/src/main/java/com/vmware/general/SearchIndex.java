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

package com.vmware.general;

import com.vmware.common.annotations.Action;
import com.vmware.common.annotations.Option;
import com.vmware.common.annotations.Sample;
import com.vmware.connection.ConnectedVimServiceBase;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.RuntimeFaultFaultMsg;

import javax.xml.ws.soap.SOAPFaultException;
import java.util.Map;

/**
 * <pre>
 * SearchIndex
 *
 * This sample demonstrates the SearchIndex API
 *
 * <b>Parameters:</b>
 * url          [required] : url of the web service
 * username     [required] : username for the authentication
 * password     [required] : password for the authentication
 * dcname       [required] : name of the datacenter
 * vmdnsname    [optional] : Dns of a virtual machine
 * hostdnsname  [optional] : Dns of the ESX host
 * vmpath       [optional] : Inventory path of a virtual machine
 * vmip         [optional] : IP Address of a virtual machine
 *
 * <b>Command Line:</b>
 * Run the search index with dcName myDatacenter
 * run.bat com.vmware.general.SearchIndex --url [webserviceurl]
 * --username [username] --password [password] --dcName myDatacenter
 *
 * Run the search index with dcName myDatacenter and vmpath to virtual machine named Test
 * run.bat com.vmware.general.SearchIndex --url [webserviceurl]
 * --username [username] --password [password] --dcName myDatacenter
 * --vmpath //DatacenterName//vm//Test
 *
 * Run the search index with dcName myDatacenter and hostdns 'abc.bcd.com'
 * run.bat com.vmware.general.SearchIndex --url [webserviceurl]
 * --username [username] --password [password]
 * --dcName myDatacenter --hostDns abc.bcd.com
 *
 * Run the search index with dcName myDatacenter and ip of the vm as 111.123.155.21
 * run.bat com.vmware.general.SearchIndex --url [webserviceurl]
 * --username [username] --password [password]
 * --dcName myDatacenter --vmIP 111.123.155.21
 * </pre>
 */
@Sample(name = "search-index", description = "This sample demonstrates the SearchIndex API")
public class SearchIndex extends ConnectedVimServiceBase {
    public final String SVC_INST_NAME = "ServiceInstance";
    public final String PROP_ME_NAME = "name";

    private String dcName;
    private String vmDnsName;
    private String vmPath;
    private String hostDnsName;
    private String vmIP;

    @Option(name = "dcname", description = "name of the datacenter")
    public void setDcName(String dcName) {
        this.dcName = dcName;
    }

    @Option(name = "vmdnsname", required = false, description = "Dns of a virtual machine")
    public void setVmDnsName(String vmDnsName) {
        this.vmDnsName = vmDnsName;
    }

    @Option(name = "hostdnsname", required = false, description = "Dns of the ESX host")
    public void setHostDnsName(String hostDnsName) {
        this.hostDnsName = hostDnsName;
    }

    @Option(name = "vmpath", required = false, description = "Inventory path of a virtual machine")
    public void setVmPath(String vmPath) {
        this.vmPath = vmPath;
    }

    @Option(name = "vmip", required = false, description = "IP Address of a virtual machine")
    public void setVmIP(String vmIP) {
        this.vmIP = vmIP;
    }

    public void printSoapFaultException(SOAPFaultException sfe) {
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
    public void action() throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        Map<String, ManagedObjectReference> entities =
                getMOREFs.inFolderByType(serviceContent.getRootFolder(),
                        "Datacenter");
        ManagedObjectReference dcMoRef = entities.get(dcName);

        if (dcMoRef != null) {
            System.out.println("Found Datacenter with name: " + dcName
                    + ", MoRef: " + dcMoRef.getValue());
        } else {
            System.out.println("Datacenter not Found with name: " + dcName);
            return;
        }

        if (vmDnsName != null) {

            ManagedObjectReference vmMoRef = null;
            try {
                vmMoRef =
                        vimPort.findByDnsName(serviceContent.getSearchIndex(),
                                dcMoRef, vmDnsName, true);
            } catch (SOAPFaultException sfe) {
                printSoapFaultException(sfe);
            } catch (RuntimeFaultFaultMsg ex) {
                System.out.println("Error Encountered: " + ex);
            }
            if (vmMoRef != null) {
                System.out.println("Found VirtualMachine with DNS name: "
                        + vmDnsName + ", MoRef: " + vmMoRef.getValue());
            } else {
                System.out.println("VirtualMachine not Found with DNS name: "
                        + vmDnsName);
            }
        }
        if (vmPath != null) {
            ManagedObjectReference vmMoRef = null;
            try {
                vmMoRef =
                        vimPort.findByInventoryPath(
                                serviceContent.getSearchIndex(), vmPath);
            } catch (SOAPFaultException sfe) {
                printSoapFaultException(sfe);
            } catch (RuntimeFaultFaultMsg ex) {
                System.out.println("Error Encountered: " + ex);
            }
            if (vmMoRef != null) {
                System.out.println("Found VirtualMachine with Path: " + vmPath
                        + ", MoRef: " + vmMoRef.getValue());

            } else {
                System.out.println("VirtualMachine not found with vmPath "
                        + "address: " + vmPath);
            }
        }
        if (vmIP != null) {
            ManagedObjectReference vmMoRef = null;
            try {
                vmMoRef =
                        vimPort.findByIp(serviceContent.getSearchIndex(), dcMoRef,
                                vmIP, true);
            } catch (SOAPFaultException sfe) {
                printSoapFaultException(sfe);
            } catch (RuntimeFaultFaultMsg ex) {
                System.out.println("Error Encountered: " + ex);
            }
            if (vmMoRef != null) {
                System.out.println("Found VirtualMachine with IP " + "address "
                        + vmIP + ", MoRef: " + vmMoRef.getValue());
            } else {
                System.out.println("VirtualMachine not found with IP "
                        + "address: " + vmIP);
            }
        }
        if (hostDnsName != null) {
            ManagedObjectReference hostMoRef = null;
            try {
                hostMoRef =
                        vimPort.findByDnsName(serviceContent.getSearchIndex(),
                                null, hostDnsName, false);
            } catch (SOAPFaultException sfe) {
                printSoapFaultException(sfe);
            } catch (RuntimeFaultFaultMsg ex) {
                System.out.println("Error Encountered: " + ex);
            }
            if (hostMoRef != null) {
                System.out.println("Found HostSystem with DNS name "
                        + hostDnsName + ", MoRef: " + hostMoRef.getValue());
            } else {
                System.out.println("HostSystem not Found with DNS name:"
                        + hostDnsName);
            }
        }
    }
}
