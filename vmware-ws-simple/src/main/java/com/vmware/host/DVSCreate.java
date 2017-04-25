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

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 * This sample demonstrates how to create/update Distributed Virtual Switch
 * <b>Parameters:</b>
 * url               [required]: url of the web service.
 * username          [required]: username for the authentication
 * Password          [required]: password for the authentication
 * option            [required]:
 *                       "createdvs" for creating a new DVS
 *                       "addportgroup" for adding a port group to DVS
 * dcname            : Datacenter name
 * dvsname           [required]: Distributed Virtual Switch name
 * dvsdesc           [optional]: Description string of the switch
 * dvsversion        : Distributed Virtual Switch either 4.0, 4.1.0, 5.0.0 or 5.1.0
 * numports          : Number of ports in the portgroup.
 * portgroupname     : Name of the port group.
 *
 * <b>Sample usage:</b>
 * Create DVS : run.bat com.vmware.host.DVSCreate --url [URLString] --username [User]
 *              --password [Password] --option createdvs --dcname [dcname]
 *              --dvsname [dvsname] --dvsversion [dvsversion]
 * Add PortGroup : run.bat com.vmware.host.DVSCreate --url [URLString] --username [User]
 *                 --password [Password] --option addportgroup --dvsname [dvsname]
 *                 --numports [numports] --portgroupname [portgroupname]
 * </pre>
 */
@Sample(name = "dvs-create", description = "This sample demonstrates how to create/update Distributed Virtual Switch")
public class DVSCreate extends ConnectedVimServiceBase {

    private String dcName = null;
    private String dvsName = null;
    private String dvsDesc = null;
    private String dvsVersion = null;
    private String noOfPorts;
    private String portGroupName = null;
    private String option = null;

    @Option(name = "option", description = "\"createdvs\" for creating a new DVS | \"addportgroup\" for adding a port group to DVS")
    public void setOption(String option) {
        this.option = option;
    }

    @Option(name = "dcname", required = false, description = "datacenter name")
    public void setDcName(String dcName) {
        this.dcName = dcName;
    }

    @Option(name = "dvsname", required = true, description = "Distributed Virtual Switch name")
    public void setDvsName(String dvsName) {
        this.dvsName = dvsName;
    }

    @Option(name = "dvsdesc", required = false, description = "Description string of the switch")
    public void setDvsDesc(String dvsDesc) {
        this.dvsDesc = dvsDesc;
    }

    @Option(name = "dvsversion", required = false, description = "Distributed Virtual Switch either 4.0, 4.1.0, 5.0.0 or 5.1.0")
    public void setDvsVersion(String dvsVersion) {
        this.dvsVersion = dvsVersion;
    }

    @Option(name = "numports", required = false, description = "Number of ports in the portgroup.")
    public void setNoOfPorts(String noOfPorts) {
        this.noOfPorts = noOfPorts;
    }

    @Option(name = "portgroupname", required = false, description = "name of port group")
    public void setPortGroupName(String portGroupName) {
        this.portGroupName = portGroupName;
    }

    // Get input parameters to run the sample
    void validate() {
        if (option != null) {
            if (!(option.equals("createdvs") || option.equals("addportgroup"))) {
                throw new IllegalArgumentException(
                        "Expected valid --option. createdvs" + " or addportgroup");
            }
        } else {
            throw new IllegalArgumentException(
                    "Expected --option argument. createdvs" + " or addportgroup");
        }
        if (option.equals("createdvs")) {
            if (dcName == null || dvsName == null) {
                throw new IllegalArgumentException(
                        "Expected --dcname and --dvsname arguments");
            }
        }
        if (option.equals("addportgroup")) {
            if (dvsName == null || noOfPorts == null || portGroupName == null) {
                throw new IllegalArgumentException(
                        "Expected --dvsname, --numports and --portgroupname arguments");
            }
        }
    }

    // Create DVSConfigSpec for creating a DVS.
    DVSConfigSpec getDVSConfigSpec(String dvsName, String dvsDesc) {
        DVSConfigSpec dvsConfigSpec = new DVSConfigSpec();
        dvsConfigSpec.setName(dvsName);
        if (dvsDesc != null) {
            dvsConfigSpec.setDescription(dvsDesc);
        }
        DVSPolicy dvsPolicy = new DVSPolicy();
        dvsPolicy.setAutoPreInstallAllowed(new Boolean(true));
        dvsPolicy.setAutoUpgradeAllowed(new Boolean(true));
        dvsPolicy.setPartialUpgradeAllowed(new Boolean(true));
        return dvsConfigSpec;
    }

    // Fetch DistributedVirtualSwitchProductSpec.
    DistributedVirtualSwitchProductSpec getDVSProductSpec(
            String version) throws RuntimeFaultFaultMsg {
        List<DistributedVirtualSwitchProductSpec> dvsProdSpec =
                vimPort.queryAvailableDvsSpec(serviceContent.getDvSwitchManager());
        DistributedVirtualSwitchProductSpec dvsSpec = null;
        if (version != null) {
            for (DistributedVirtualSwitchProductSpec prodSpec : dvsProdSpec) {
                if (version.equalsIgnoreCase(prodSpec.getVersion())) {
                    dvsSpec = prodSpec;
                }
            }
            if (dvsSpec == null) {
                throw new IllegalArgumentException("DVS Version " + version
                        + " not supported.");
            }
        } else {
            dvsSpec = dvsProdSpec.get(dvsProdSpec.size() - 1);
        }
        return dvsSpec;
    }

    /**
     * Create a Distributed Virtual Switch.
     *
     * @param dcName  The Datacenter name.
     * @param dvsName The DVS name.
     * @param dvsDesc The DVS description.
     * @param version Dot-separated version string.
     * @throws InvalidCollectorVersionFaultMsg
     *
     * @throws RuntimeFaultFaultMsg
     * @throws InvalidPropertyFaultMsg
     * @throws NotFoundFaultMsg
     * @throws InvalidNameFaultMsg
     * @throws DvsNotAuthorizedFaultMsg
     * @throws DvsFaultFaultMsg
     * @throws DuplicateNameFaultMsg
     */
    void createDVS(String dcName, String dvsName, String dvsDesc,
                   String version) throws InvalidCollectorVersionFaultMsg,
            RuntimeFaultFaultMsg, InvalidPropertyFaultMsg, DuplicateNameFaultMsg,
            DvsFaultFaultMsg, DvsNotAuthorizedFaultMsg, InvalidNameFaultMsg,
            NotFoundFaultMsg {
        DistributedVirtualSwitchProductSpec dvsProdSpec =
                getDVSProductSpec(version);
        ManagedObjectReference dcmor =
                getMOREFs.inFolderByType(serviceContent.getRootFolder(),
                        "Datacenter").get(dcName);
        if (dcmor == null) {
            System.out.println("Datacenter " + dcName + " not found.");
            return;
        }
        ManagedObjectReference networkmor =
                (ManagedObjectReference) getMOREFs.entityProps(dcmor,
                        new String[]{"networkFolder"}).get("networkFolder");

        DVSCreateSpec dvsspec = new DVSCreateSpec();
        List<DistributedVirtualSwitchHostProductSpec> dvsHostProdSpec =
                vimPort.queryDvsCompatibleHostSpec(
                        serviceContent.getDvSwitchManager(), dvsProdSpec);
        DVSCapability dvsCapability = new DVSCapability();
        dvsCapability.getCompatibleHostComponentProductInfo().addAll(
                dvsHostProdSpec);
        dvsspec.setCapability(dvsCapability);
        dvsspec.setConfigSpec(getDVSConfigSpec(dvsName, dvsDesc));
        dvsspec.setProductInfo(dvsProdSpec);

        ManagedObjectReference taskmor =
                vimPort.createDVSTask(networkmor, dvsspec);

        if (getTaskResultAfterDone(taskmor)) {
            System.out.printf("Success: Creating Distributed Virtual Switch");
        } else {
            throw new RuntimeException(
                    "Failure: Creating Distributed Virtual Switch");
        }
    }

    /**
     * Add a DistributedVirtualPortgroup to the switch.
     *
     * @param dvSwitchName  The DVS name.
     * @param numOfPorts    Number of ports in the portgroup.
     * @param portGroupName The name of the portgroup.
     * @throws RemoteException
     * @throws RuntimeFaultFaultMsg
     * @throws InvalidPropertyFaultMsg
     * @throws InvalidNameFaultMsg
     * @throws DvsFaultFaultMsg
     * @throws DuplicateNameFaultMsg
     * @throws InvalidCollectorVersionFaultMsg
     *
     * @throws Exception
     */
    void addPortGroup(String dvSwitchName, int numOfPorts,
                      String portGroupName) throws RemoteException, InvalidPropertyFaultMsg,
            RuntimeFaultFaultMsg, DuplicateNameFaultMsg, DvsFaultFaultMsg,
            InvalidNameFaultMsg, InvalidCollectorVersionFaultMsg {
        ManagedObjectReference dvsMor =
                getMOREFs.inFolderByType(serviceContent.getRootFolder(),
                        "VmwareDistributedVirtualSwitch").get(dvSwitchName);
        if (dvsMor != null) {
            DVPortgroupConfigSpec portGroupConfigSpec =
                    new DVPortgroupConfigSpec();

            portGroupConfigSpec.setName(portGroupName);
            portGroupConfigSpec.setNumPorts(numOfPorts);
            portGroupConfigSpec.setType("earlyBinding");

            List<DVPortgroupConfigSpec> listDVSPortConfigSpec =
                    new ArrayList<DVPortgroupConfigSpec>();
            listDVSPortConfigSpec.add(portGroupConfigSpec);

            ManagedObjectReference taskmor =
                    vimPort.addDVPortgroupTask(dvsMor, listDVSPortConfigSpec);

            if (getTaskResultAfterDone(taskmor)) {
                System.out.printf("Success: Adding Port Group");
            } else {
                throw new RuntimeException("Failure: Adding Port Group");
            }
        } else {
            System.out.println("DVS Switch " + dvSwitchName + " Not Found");
            return;
        }
    }

    @Action
    public void run() throws DuplicateNameFaultMsg, RuntimeFaultFaultMsg, InvalidPropertyFaultMsg, DvsFaultFaultMsg, InvalidCollectorVersionFaultMsg, InvalidNameFaultMsg, RemoteException, NotFoundFaultMsg, DvsNotAuthorizedFaultMsg {
        validate();
        if (option.equals("createdvs")) {
            createDVS(dcName, dvsName, dvsDesc, dvsVersion);
        } else if (option.equals("addportgroup")) {
            addPortGroup(dvsName, Integer.parseInt(noOfPorts), portGroupName);
        } else {
            throw new IllegalArgumentException("unknown option: " + option);
        }
    }
}
