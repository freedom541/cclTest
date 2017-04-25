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
import com.vmware.vim25.*;

import java.util.Map;

import static java.lang.System.out;

/**
 * <pre>
 * Create
 *
 * This sample creates managed entity like Host-Standalone Cluster
 * Datacenter, and folder
 *
 * <b>Parameters:</b>
 * url          [required] : url of the web service
 * username     [required] : username for the authentication
 * password     [required] : password for the authentication
 * parentname   [required] : specifies the name of the parent folder
 * itemtype     [required] : Type of the object to be added
 *                           e.g. Host-Standalone | Cluster | Datacenter | Folder
 * itemname     [required]   : Name of the item added
 *
 * <b>Command Line:</b>
 * Create a folder named myFolder under root folder Root:
 * run.bat com.vmware.general.Create --url [webserviceurl]
 * --username [username] --password [password]
 * --parentName [Root] --itemType [Folder] --itemName [myFolder]
 *
 * Create a datacenter named myDatacenter under root folder Root:
 * run.bat com.vmware.general.Create --url [webserviceurl]
 * --username [username] --password [password]
 * --parentName [Root] --itemType [Datacenter] --itemName [myDatacenter]
 *
 * Create a cluster named myCluster under root folder Root:
 * run.bat com.vmware.general.Create --url [webserviceurl]
 * --username [username] --password [password]
 * --parentName [Root] --itemType [Cluster] --itemName [myCluster]
 * </pre>
 */

@Sample(
        name = "create",
        description = "This sample creates managed entity like Host-Standalone," +
                " Cluster, Datacenter, and folder"
)
public class Create extends ConnectedVimServiceBase {
    private String licenseKey;
    private String parentName;
    private String itemType;
    private String itemName;

    @Option(name = "parentname", description = "specifies the name of the parent folder")
    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    @Option(name = "itemtype", description = "Type of the object to be added, e.g. Host-Standalone | Cluster | Datacenter | Folder")
    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    @Option(name = "itemname", description = "Name of the item added")
    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    @Option(name = "licensekey", required = false)
    public void setLicenseKey(String licenseKey) {
        this.licenseKey = licenseKey;
    }

    @Action
    public void create() throws DuplicateNameFaultMsg,
            InvalidNameFaultMsg, RuntimeFaultFaultMsg, InvalidPropertyFaultMsg, HostConnectFaultFaultMsg, InvalidLoginFaultMsg, InvalidCollectorVersionFaultMsg {

        ManagedObjectReference taskMoRef = null;
        Map<String, ManagedObjectReference> folders =
                getMOREFs.inFolderByType(serviceContent.getRootFolder(), "Folder");
        if (folders.containsKey(parentName)) {
            ManagedObjectReference folderMoRef = folders.get(parentName);
            if (itemType.equals("Folder")) {
                vimPort.createFolder(folderMoRef, itemName);
                out.println("Sucessfully created::" + itemName);
            } else if (itemType.equals("Datacenter")) {
                vimPort.createDatacenter(folderMoRef, itemName);
                out.println("Sucessfully created::" + itemName);
            } else if (itemType.equals("Cluster")) {
                ClusterConfigSpec clusterSpec = new ClusterConfigSpec();
                vimPort.createCluster(folderMoRef, itemName, clusterSpec);
                out.println("Sucessfully created::" + itemName);
            } else if (itemType.equals("Host-Standalone")) {
                HostConnectSpec hostSpec = new HostConnectSpec();
                hostSpec.setHostName(itemName);
                hostSpec.setUserName(connection.getUsername());
                hostSpec.setPassword(connection.getPassword());
                hostSpec.setPort(connection.getPort());
                ComputeResourceConfigSpec crcs = new ComputeResourceConfigSpec();
                crcs.setVmSwapPlacement(VirtualMachineConfigInfoSwapPlacementType.VM_DIRECTORY
                        .value());
                taskMoRef =
                        vimPort.addStandaloneHostTask(folderMoRef, hostSpec, crcs,
                                true, this.licenseKey);

                if (getTaskResultAfterDone(taskMoRef)) {
                    out.println("Sucessfully created::" + itemName);
                } else {
                    out.println("Host'" + itemName + " not created::");
                }
            } else {
                out.println("Unknown Type. Allowed types are:");
                out.println(" Host-Standalone");
                out.println(" Cluster");
                out.println(" Datacenter");
                out.println(" Folder");
            }
        } else {
            out.println("Parent folder '" + parentName + "' not found");
        }

    }

}