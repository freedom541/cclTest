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

import static java.lang.Integer.parseInt;

/**
 * <pre>
 *  This sample demonstrates how to add/modify NetworkResourcePool to Distributed
 *  Virtual Switch:
 *
 * <b>Parameters:</b>
 *  url               [required]: url of the web service.
 *  username          [required]: username for authentication
 *  Password          [required]: password for authentication
 *  option            [required]:
 *                       "enablenioc" for enabling network I/O control
 *                       "addnrp" for adding Network Resource Pool
 *                       "listnrp" for Listing existing DVSNetworkResourcePool
 *                       "modifynrp" for reconfiguring NetworkResourcePool
 *                       "nrptoportgroup" for adding NetworkResourcePool key to DVS Port Group
 *  dvsname           [required]: Distributed Virtual Switch name
 *  enablenioc        : If true, enables I/O control. If false, disables network I/O control
 *  nrpsharelevel     : The allocation level. The level is a simplified view of shares.
 *                      Levels map to a pre-determined set of numeric values for shares. If the
 *                      Shares value does not map to a predefined size, then the level is set as custom.
 *  nrphostlimit      : Numeric value: The maximum allowed usage for network clients
 *                      belonging to this resource pool per host.
 *  nrpprioritytag    : Numeric value: The 802.1p tag to be used for this resource pool.
 *                      Its value should be between 0-7
 *  nrpname           : The user defined name for the resource pool.
 *  nrpdesc           : [optional] The user defined description for the resource pool.
 *  dvpgname          : The name of the portgroup.
 *
 * <b>Sample usage:</b>
 *  Enable Network I/O Control:
 *  run.bat com.vmware.host.NIOCForDVS --url [URLString] --username [User] --password [Password]
 *  --option enablenioc --dvsname [dvsname] --enablenioc [enablenioc]
 *
 *  Add NetworkResourcePool:
 *  run.bat com.vmware.host.NIOCForDVS --url [URLString] --username [User] --password [Password]
 *  --option addnrp --dvsname [dvsname] --nrpsharelevel [nrpsharelevel] --nrphostlimit [nrphostlimit]
 *  --nrpprioritytag [nrpprioritytag] --nrpname [nrpname] --nrpdesc [nrpdesc]
 *
 *  List NetworkResourcePool:
 *  run.bat com.vmware.host.NIOCForDVS --url [URLString] --username [User] --password [Password]
 *  --option listnrp --dvsname [dvsname]
 *
 *  Reconfiguring NetworkResourcePool:
 *  run.bat com.vmware.host.NIOCForDVS --url [URLString] --username [User] --password [Password]
 *  --option modifynrp --dvsname [dvsname] --nrpsharelevel [nrpsharelevel] --nrphostlimit [nrphostlimit]
 *  --nrpprioritytag [nrpprioritytag] --nrpname [nrpname]
 *
 *  Associate DVS Port Group to NetworkResourcePool :
 *  run.bat com.vmware.host.NIOCForDVS --url [URLString] --username [User] --password [Password]
 *  --option nrptoportgroup --dvsname [dvsname] --nrpname [nrpname] --dvpgname [dvpgname]
 *
 *  Note: Sample only works for DVS 5.0 onwards.
 * </pre>
 */
@Sample(
        name = "nico-for-dvs",
        description = "demonstrates how to add/modify NetworkResourcePool to Distributed Virtual Switch"
)
public class NIOCForDVS extends ConnectedVimServiceBase {

    private String dvsname = null;
    private String dvPGName = null;
    private String nrpName = null;
    private String nrpAllocationShareLevel = null;
    private String prioritytag = null;
    private String hostLimit = null;
    private int noOfShares = -1; // set in validate method
    private String nrpDesc = null;
    private String enableNIOC = null;
    private String option = null;

    @Option(name = "option",
            description = "\n" +
                    "\t\"enablenioc\" for enabling network I/O control\n" +
                    "\t\"addnrp\" for adding Network Resource Pool\n" +
                    "\t\"listnrp\" for Listing existing DVSNetworkResourcePool\n" +
                    "\t\"modifynrp\" for reconfiguring NetworkResourcePool\n" +
                    "\t\"nrptoportgroup\" for adding NetworkResourcePool key to DVS Port Group\n"
    )
    public void setOption(String option) {
        this.option = option;
    }

    @Option(name = "dvsname", description = "Distributed Virtual Switch name")
    public void setDvsname(String dvsname) {
        this.dvsname = dvsname;
    }

    @Option(name = "enablenioc", required = false, description = "If true, enables I/O control. If false, disables network I/O control")
    public void setEnableNIOC(String enableNIOC) {
        this.enableNIOC = enableNIOC;
    }

    @Option(name = "nrpsharelevel", required = false, description = "high, normal, low or numeric\n" +
            "\tThe allocation level. The level is a simplified view of shares.\n" +
            "\tLevels map to a pre-determined set of numeric values for shares. If the\n" +
            "\tShares value does not map to a predefined size, then the level is set as custom.\n"
    )
    public void setNrpAllocationShareLevel(String nrpAllocationShareLevel) {
        this.nrpAllocationShareLevel = nrpAllocationShareLevel;
    }

    @Option(name = "nrphostlimit", required = false, description = "\n" +
            "\tNumeric value: The maximum allowed usage for network clients\n" +
            "\tbelonging to this resource pool per host.\n"
    )
    public void setHostLimit(String hostLimit) {
        this.hostLimit = hostLimit;
    }

    @Option(name = "nrpprioritytag",
            required = false,
            description = "\n" +
                    "\tNumeric value: The 802.1p tag to be used for this resource pool.\n" +
                    "\tIts value should be between 0-7\n")
    public void setPrioritytag(String prioritytag) {
        this.prioritytag = prioritytag;
    }

    @Option(name = "nrpname", required = false, description = "The user defined name for the resource pool.")
    public void setNrpName(String nrpName) {
        this.nrpName = nrpName;
    }

    @Option(name = "nrpdesc", required = false, description = "The user defined description for the resource pool.")
    public void setNrpDesc(String nrpDesc) {
        this.nrpDesc = nrpDesc;
    }

    @Option(name = "dvpgname", required = false, description = "The name of the portgroup.")
    public void setDvPGName(String dvPGName) {
        this.dvPGName = dvPGName;
    }

    // Get input parameters to run the sample
    void validate() {
        if (option != null) {
            if (!(option.equalsIgnoreCase("enablenioc")
                    || option.equalsIgnoreCase("addnrp")
                    || option.equalsIgnoreCase("listnrp")
                    || option.equalsIgnoreCase("modifynrp") || option
                    .equalsIgnoreCase("nrptoportgroup"))) {
                throw new IllegalArgumentException(
                        "Expected valid --option. enablenioc,"
                                + " addnrp, listnrp, modifynrp or nrptoportgroup");
            }
        } else {
            throw new IllegalArgumentException(
                    "Expected --option argument. enablenioc,"
                            + " addnrp, listnrp, modifynrp or nrptoportgroup");
        }
        if (option.equalsIgnoreCase("enablenioc")) {
            if (dvsname == null || enableNIOC == null) {
                throw new IllegalArgumentException(
                        "Expected --dvsname and --enablenioc arguments");
            }
        }
        if (option.equalsIgnoreCase("listnrp")) {
            if (dvsname == null) {
                throw new IllegalArgumentException("Expected --dvsname arguments");
            }
        }
        if (option.equalsIgnoreCase("addnrp")
                || option.equalsIgnoreCase("modifynrp")) {
            if (dvsname == null || nrpName == null) {
                throw new IllegalArgumentException(
                        "Expected --dvsname and --nrpname arguments");
            }
            if (nrpAllocationShareLevel != null) {
                if (nrpAllocationShareLevel.matches("\\d+")) {
                    noOfShares = parseInt(nrpAllocationShareLevel);
                } else if (!(nrpAllocationShareLevel
                        .equalsIgnoreCase(SharesLevel.HIGH.toString())
                        || nrpAllocationShareLevel
                        .equalsIgnoreCase(SharesLevel.NORMAL.toString()) || nrpAllocationShareLevel
                        .equalsIgnoreCase(SharesLevel.LOW.toString()))) {
                    throw new IllegalArgumentException(
                            "Expected --nrpsharelevel arguments. "
                                    + "high, normal, low or numeric");
                }
            }
            if (prioritytag != null) {
                if ((parseInt(prioritytag) < 0)
                        || (parseInt(prioritytag) > 7)) {
                    throw new IllegalArgumentException(
                            "Expected --nrpprioritytag integer between 0-7");
                }
            }
        }
        if (option.equalsIgnoreCase("nrptoportgroup")) {
            if (dvPGName == null || dvsname == null || nrpName == null) {
                throw new IllegalArgumentException(
                        "Expected --dvsname, --nrpName and --dvpgname arguments");
            }
        }
    }

    /**
     * Enable/Disable network I/O control on the vSphere Distributed Switch.
     *
     * @param dvSwitchName VmwareDistributedVirtualSwitch name
     * @param enableNIOC   boolean; true, enables I/O control. false, disables network I/O
     *                     control.
     * @throws DvsFaultFaultMsg        Thrown if the enabling/disabling fails.
     * @throws RuntimeFaultFaultMsg    Thrown if any type of runtime fault is thrown that is not
     *                                 covered by the other faults; for example, a communication
     *                                 error.
     * @throws InvalidPropertyFaultMsg
     */
    void enableNIOC(String dvSwitchName, boolean enableNIOC)
            throws DvsFaultFaultMsg, RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        ManagedObjectReference dvsMor =
                getMOREFs.inFolderByType(serviceContent.getRootFolder(),
                        "VmwareDistributedVirtualSwitch").get(dvSwitchName);
        if (dvsMor != null) {
            vimPort.enableNetworkResourceManagement(dvsMor, enableNIOC);
            System.out.printf("Set network I/O control");
        } else {
            System.out.println("DVS Switch " + dvSwitchName + " Not Found");
            return;
        }
    }

    /**
     * Add a network resource pool
     *
     * @param dvSwitchName VmwareDistributedVirtualSwitch name.
     * @param nrpName      The user defined name for the resource pool.
     * @param nrpDesc      The user defined description for the resource pool.
     * @param noOfShares   The number of shares allocated. Used to determine resource
     *                     allocation in case of resource contention. This value is only
     *                     set if level is set to custom. If level is not set to custom,
     *                     this value is ignored.
     * @param level        The allocation level. The level is a simplified view of shares.
     *                     Levels map to a pre-determined set of numeric values for shares.
     * @param prioritytag  The 802.1p tag to be used for this resource pool.
     * @param hostLimit    The maximum allowed usage for network clients belonging to this
     *                     resource pool per host.
     * @throws DvsFaultFaultMsg        Thrown if the enabling/disabling fails.
     * @throws InvalidNameFaultMsg
     * @throws RuntimeFaultFaultMsg    Thrown if any type of runtime fault is thrown that is not
     *                                 covered by the other faults; for example, a communication
     *                                 error.
     * @throws InvalidPropertyFaultMsg
     */
    void addNetworkResourcePool(String dvSwitchName,
                                String nrpName, String nrpDesc, int noOfShares, String level,
                                String prioritytag, String hostLimit) throws DvsFaultFaultMsg,
            InvalidNameFaultMsg, RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        ManagedObjectReference dvsMor =
                getMOREFs.inFolderByType(serviceContent.getRootFolder(),
                        "VmwareDistributedVirtualSwitch").get(dvSwitchName);
        if (dvsMor != null) {
            List<DVSNetworkResourcePoolConfigSpec> dvsNetworkRPConfigSpecs =
                    new ArrayList<DVSNetworkResourcePoolConfigSpec>();
            DVSNetworkResourcePoolConfigSpec dvsNetworkRPConfigSpec =
                    new DVSNetworkResourcePoolConfigSpec();
            DVSNetworkResourcePoolAllocationInfo allocationInfo =
                    new DVSNetworkResourcePoolAllocationInfo();
            if (level != null) {
                SharesInfo shares = new SharesInfo();
                if (noOfShares != -1) {
                    shares.setLevel(SharesLevel.CUSTOM);
                    shares.setShares(noOfShares);
                } else {
                    shares.setLevel(SharesLevel.valueOf(level.toUpperCase()));
                }
                allocationInfo.setShares(shares);
            }
            if (hostLimit != null) {
                allocationInfo.setLimit(Long.parseLong(hostLimit));
            }
            if (prioritytag != null) {
                allocationInfo.setPriorityTag(parseInt(prioritytag));
            }
            dvsNetworkRPConfigSpec.setConfigVersion("0");
            if (nrpDesc != null) {
                dvsNetworkRPConfigSpec.setDescription(nrpDesc);
            } else {
                dvsNetworkRPConfigSpec.setDescription(nrpName);
            }
            dvsNetworkRPConfigSpec.setName(nrpName);
            dvsNetworkRPConfigSpec.setAllocationInfo(allocationInfo);
            dvsNetworkRPConfigSpec.setKey("");
            dvsNetworkRPConfigSpecs.add(dvsNetworkRPConfigSpec);
            vimPort.addNetworkResourcePool(dvsMor, dvsNetworkRPConfigSpecs);
            System.out.printf("Added NetworkResourcePool successfully");
        } else {
            System.out.println("DVS Switch " + dvSwitchName + " Not Found");
            return;
        }
    }

    /**
     * List NetworkResourcePool in a particular DVS switch.
     *
     * @param dvSwitchName VmwareDistributedVirtualSwitch name.
     * @throws Exception
     */
    void listNetworkResourcePool(String dvSwitchName) throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        ManagedObjectReference dvsMor =
                getMOREFs.inFolderByType(serviceContent.getRootFolder(),
                        "VmwareDistributedVirtualSwitch").get(dvSwitchName);
        if (dvsMor != null) {
            List<DVSNetworkResourcePool> nrpList =
                    ((ArrayOfDVSNetworkResourcePool) getMOREFs.entityProps(dvsMor,
                            new String[]{"networkResourcePool"}).get(
                            "networkResourcePool")).getDVSNetworkResourcePool();
            if (nrpList != null) {
                System.out.println("Existing DVSNetworkResourcePool:");
                for (DVSNetworkResourcePool dvsNrp : nrpList) {
                    String nrp = "System defined DVSNetworkResourcePool";
                    if (dvsNrp.getKey().startsWith("NRP")) {
                        nrp = "User defined DVSNetworkResourcePool";
                    }
                    System.out.println(dvsNrp.getName()
                            + " : networkResourcePool[\"" + dvsNrp.getKey() + "\"] : "
                            + nrp);
                }
            } else {
                System.out.println("No NetworkResourcePool found for DVS Switch "
                        + dvSwitchName);
                return;
            }
        } else {
            System.out.println("DVS Switch " + dvSwitchName + " Not Found");
        }
    }

    /**
     * Update the network resource pool configuration.
     *
     * @param dvSwitchName VmwareDistributedVirtualSwitch name.
     * @param nrpName      name of the NetworkResourcePool to be updated.
     * @param noOfShares   The number of shares allocated. Used to determine resource
     *                     allocation in case of resource contention. This value is only
     *                     set if level is set to custom. If level is not set to custom,
     *                     this value is ignored.
     * @param level        The allocation level. The level is a simplified view of shares.
     *                     Levels map to a pre-determined set of numeric values for shares.
     * @param prioritytag  The 802.1p tag to be used for this resource pool.
     * @param hostLimit    The maximum allowed usage for network clients belonging to this
     *                     resource pool per host.
     * @throws Exception
     */
    void modifyNetworkResourcePool(String dvSwitchName,
                                   String nrpName, int noOfShares, String level, String prioritytag,
                                   String hostLimit) throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg, NotFoundFaultMsg, DvsFaultFaultMsg, ConcurrentAccessFaultMsg, InvalidNameFaultMsg {
        ManagedObjectReference dvsMor =
                getMOREFs.inFolderByType(serviceContent.getRootFolder(),
                        "VmwareDistributedVirtualSwitch").get(dvSwitchName);
        if (dvsMor != null) {
            List<DVSNetworkResourcePool> nrpList =
                    ((ArrayOfDVSNetworkResourcePool) getMOREFs.entityProps(dvsMor,
                            new String[]{"networkResourcePool"}).get(
                            "networkResourcePool")).getDVSNetworkResourcePool();
            if (nrpList != null) {
                String configVersion = null;
                String nrpKey = null;
                for (DVSNetworkResourcePool dvsNrp : nrpList) {
                    if (dvsNrp.getName().equalsIgnoreCase(nrpName)) {
                        nrpKey = dvsNrp.getKey();
                        configVersion = dvsNrp.getConfigVersion();
                    }
                }
                if (nrpKey == null) {
                    System.out.println("NetworkResource Pool " + nrpName
                            + " Not Found");
                    return;
                }
                List<DVSNetworkResourcePoolConfigSpec> dvsNetworkRPConfigSpecs =
                        new ArrayList<DVSNetworkResourcePoolConfigSpec>();
                DVSNetworkResourcePoolConfigSpec dvsNetworkRPConfigSpec =
                        new DVSNetworkResourcePoolConfigSpec();
                DVSNetworkResourcePoolAllocationInfo allocationInfo =
                        new DVSNetworkResourcePoolAllocationInfo();
                if (level != null) {
                    SharesInfo shares = new SharesInfo();
                    if (noOfShares != -1) {
                        shares.setLevel(SharesLevel.CUSTOM);
                        shares.setShares(noOfShares);
                    } else {
                        shares.setLevel(SharesLevel.valueOf(level.toUpperCase()));
                    }
                    allocationInfo.setShares(shares);
                }
                if (hostLimit != null) {
                    allocationInfo.setLimit(Long.parseLong(hostLimit));
                }
                if (prioritytag != null) {
                    allocationInfo.setPriorityTag(parseInt(prioritytag));
                }
                dvsNetworkRPConfigSpec.setConfigVersion(configVersion);
                dvsNetworkRPConfigSpec.setAllocationInfo(allocationInfo);
                dvsNetworkRPConfigSpec.setKey(nrpKey);
                dvsNetworkRPConfigSpecs.add(dvsNetworkRPConfigSpec);
                vimPort.updateNetworkResourcePool(dvsMor, dvsNetworkRPConfigSpecs);
                System.out.printf("Modified NetworkResourcePool successfully");
            } else {
                System.out.println("No NetworkResourcePool found for DVS Switch "
                        + dvSwitchName);
                return;
            }
        } else {
            System.out.println("DVS Switch " + dvSwitchName + " Not Found");
            return;
        }
    }

    /**
     * Reconfigure DVS PortGroup to associate it with a NetworkResourcePool.
     *
     * @param dvSwitchName    The name of VmwareDistributedVirtualSwitch having
     *                        NetworkResourcePool to be associated with port group.
     * @param nrpName         The name of NetworkResourcePool to be associated with port
     *                        group.
     * @param dvPortGroupName The name of the portgroup.
     * @throws RemoteException
     * @throws RuntimeFaultFaultMsg
     * @throws InvalidPropertyFaultMsg
     * @throws InvalidNameFaultMsg
     * @throws DvsFaultFaultMsg
     * @throws DuplicateNameFaultMsg
     * @throws ConcurrentAccessFaultMsg
     * @throws InvalidCollectorVersionFaultMsg
     *
     * @throws Exception
     */
    void reconfigureDVSPG(String dvSwitchName, String nrpName,
                          String dvPortGroupName) throws RemoteException,
            InvalidPropertyFaultMsg, RuntimeFaultFaultMsg,
            ConcurrentAccessFaultMsg, DuplicateNameFaultMsg, DvsFaultFaultMsg,
            InvalidNameFaultMsg, InvalidCollectorVersionFaultMsg {

        ManagedObjectReference dvsMor =
                getMOREFs.inFolderByType(serviceContent.getRootFolder(),
                        "VmwareDistributedVirtualSwitch").get(dvSwitchName);
        if (dvsMor != null) {
            List<DVSNetworkResourcePool> nrpList =
                    ((ArrayOfDVSNetworkResourcePool) getMOREFs.entityProps(dvsMor,
                            new String[]{"networkResourcePool"}).get(
                            "networkResourcePool")).getDVSNetworkResourcePool();
            String nrpKey = null;
            if (nrpList != null) {
                for (DVSNetworkResourcePool dvsNrp : nrpList) {
                    if (dvsNrp.getName().equalsIgnoreCase(nrpName)) {
                        nrpKey = dvsNrp.getKey();
                    }
                }
                if (nrpKey == null) {
                    System.out.println("NetworkResourcePool " + nrpName
                            + " Not Found");
                    return;
                }
            } else {
                System.out.println("No NetworkResourcePool found for DVS Switch "
                        + dvSwitchName);
                return;
            }
            ManagedObjectReference dvspgMor =
                    getMOREFs.inFolderByType(serviceContent.getRootFolder(),
                            "DistributedVirtualPortgroup").get(dvPortGroupName);
            if (dvspgMor != null) {
                DVPortgroupConfigInfo configInfo =
                        (DVPortgroupConfigInfo) getMOREFs.entityProps(dvspgMor,
                                new String[]{"config"}).get("config");
                String dvPortGroupConfigVersion = configInfo.getConfigVersion();
                DVPortgroupConfigSpec dvPortGConfigSpec =
                        new DVPortgroupConfigSpec();
                DVPortSetting portSetting = new DVPortSetting();
                StringPolicy networkResourcePoolKey = new StringPolicy();
                networkResourcePoolKey.setValue(nrpKey);
                networkResourcePoolKey.setInherited(false);
                portSetting.setNetworkResourcePoolKey(networkResourcePoolKey);
                dvPortGConfigSpec.setName(dvPortGroupName);
                dvPortGConfigSpec.setConfigVersion(dvPortGroupConfigVersion);
                dvPortGConfigSpec.setDefaultPortConfig(portSetting);
                ManagedObjectReference taskmor =
                        vimPort.reconfigureDVPortgroupTask(dvspgMor,
                                dvPortGConfigSpec);
                if (getTaskResultAfterDone(taskmor)) {
                    System.out.printf("Success: Reconfiguring Port Group");
                } else {
                    throw new RuntimeException("Failure: Reconfiguring Port Group");
                }
            } else {
                System.out.println("DVS port group " + dvPortGroupName
                        + " Not Found");
                return;
            }
        } else {
            System.out.println("DVS Switch " + dvSwitchName + " Not Found");
        }
    }

    @Action
    public void run() throws InvalidPropertyFaultMsg, DvsFaultFaultMsg, InvalidNameFaultMsg, RuntimeFaultFaultMsg, NotFoundFaultMsg, ConcurrentAccessFaultMsg, DuplicateNameFaultMsg, InvalidCollectorVersionFaultMsg, RemoteException {
        if (option.equalsIgnoreCase("enablenioc")) {
            enableNIOC(dvsname, Boolean.parseBoolean(enableNIOC));
        } else if (option.equalsIgnoreCase("addnrp")) {
            addNetworkResourcePool(dvsname, nrpName, nrpDesc, noOfShares,
                    nrpAllocationShareLevel, prioritytag, hostLimit);
        } else if (option.equalsIgnoreCase("listnrp")) {
            listNetworkResourcePool(dvsname);
        } else if (option.equalsIgnoreCase("modifynrp")) {
            modifyNetworkResourcePool(dvsname, nrpName, noOfShares,
                    nrpAllocationShareLevel, prioritytag, hostLimit);
        } else if (option.equalsIgnoreCase("nrptoportgroup")) {
            reconfigureDVSPG(dvsname, nrpName, dvPGName);
        }
    }
}
