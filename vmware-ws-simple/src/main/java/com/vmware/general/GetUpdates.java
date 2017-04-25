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

import javax.xml.datatype.XMLGregorianCalendar;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * <pre>
 * GetUpdates
 *
 * This sample demonstrates how to use the PropertyCollector to monitor one or more
 * properties of one or more managed objects.
 * In particular this sample monitors all or one Virtual Machine
 * and all or one Host for changes to some basic properties
 *
 * <b>Parameters:</b>
 * url          [required] : url of the web service
 * username     [required] : username for the authentication
 * password     [required] : password for the authentication
 * vmname       [required] : name of the virtual machine
 *
 * <b>Command Line:</b>
 * run.bat com.vmware.general.GetUpdates --url [webserviceurl]
 * --username [username] --password [password]
 * --vmname  [vm name]
 * </pre>
 */
@Sample(name = "get-updates",
        description = "This sample demonstrates how to use the PropertyCollector to monitor one or more " +
                "properties of one or more managed objects.")
public class GetUpdates extends ConnectedVimServiceBase {
    private String virtualmachinename;
    private ManagedObjectReference propCollectorRef;

    @Option(name = "vmname", description = "name of the virtual machine")
    public void setVmname(String name) {
        this.virtualmachinename = name;
    }

    /**
     * @return An array of SelectionSpec covering all the entities that provide
     *         performance statistics. The entities that provide performance
     *         statistics are VM, Host, Resource pool, Cluster Compute Resource
     *         and Datastore.
     */
    private static SelectionSpec[] buildFullTraversal() {
        // Terminal traversal specs
        // RP -> VM
        TraversalSpec rpToVm = new TraversalSpec();
        rpToVm.setName("rpToVm");
        rpToVm.setType("ResourcePool");
        rpToVm.setPath("vm");
        rpToVm.setSkip(Boolean.FALSE);

        // vApp -> VM
        TraversalSpec vAppToVM = new TraversalSpec();
        vAppToVM.setName("vAppToVM");
        vAppToVM.setType("VirtualApp");
        vAppToVM.setPath("vm");

        // HostSystem -> VM
        TraversalSpec hToVm = new TraversalSpec();
        hToVm.setType("HostSystem");
        hToVm.setPath("vm");
        hToVm.setName("HToVm");
        hToVm.setSkip(Boolean.FALSE);

        // DC -> DS
        TraversalSpec dcToDs = new TraversalSpec();
        dcToDs.setType("Datacenter");
        dcToDs.setPath("datastore");
        dcToDs.setName("dcToDs");
        dcToDs.setSkip(Boolean.FALSE);

        // For RP -> RP recursion
        SelectionSpec rpToRpSpec = new SelectionSpec();
        rpToRpSpec.setName("rpToRp");

        // Recurse through all ResourcePools
        TraversalSpec rpToRp = new TraversalSpec();
        rpToRp.setType("ResourcePool");
        rpToRp.setPath("resourcePool");
        rpToRp.setSkip(Boolean.FALSE);
        rpToRp.setName("rpToRp");
        SelectionSpec[] sspecs = new SelectionSpec[]{rpToRpSpec};
        rpToRp.getSelectSet().addAll(Arrays.asList(sspecs));

        TraversalSpec crToRp = new TraversalSpec();
        crToRp.setType("ComputeResource");
        crToRp.setPath("resourcePool");
        crToRp.setSkip(Boolean.FALSE);
        crToRp.setName("crToRp");
        SelectionSpec[] sspecarrayrptorprtptovm = new SelectionSpec[]{rpToRp};
        crToRp.getSelectSet().addAll(Arrays.asList(sspecarrayrptorprtptovm));

        TraversalSpec crToH = new TraversalSpec();
        crToH.setSkip(Boolean.FALSE);
        crToH.setType("ComputeResource");
        crToH.setPath("host");
        crToH.setName("crToH");
        crToH.getSelectSet().add(hToVm);

        // For Folder -> Folder recursion
        SelectionSpec sspecvfolders = new SelectionSpec();
        sspecvfolders.setName("VisitFolders");

        TraversalSpec dcToHf = new TraversalSpec();
        dcToHf.setSkip(Boolean.FALSE);
        dcToHf.setType("Datacenter");
        dcToHf.setPath("hostFolder");
        dcToHf.setName("dcToHf");
        dcToHf.getSelectSet().add(sspecvfolders);

        TraversalSpec vAppToRp = new TraversalSpec();
        vAppToRp.setName("vAppToRp");
        vAppToRp.setType("VirtualApp");
        vAppToRp.setPath("resourcePool");
        SelectionSpec[] vAppToVMSS = new SelectionSpec[]{rpToRpSpec};
        vAppToRp.getSelectSet().addAll(Arrays.asList(vAppToVMSS));

        TraversalSpec dcToVmf = new TraversalSpec();
        dcToVmf.setType("Datacenter");
        dcToVmf.setSkip(Boolean.FALSE);
        dcToVmf.setPath("vmFolder");
        dcToVmf.setName("dcToVmf");
        dcToVmf.getSelectSet().add(sspecvfolders);

        TraversalSpec visitFolders = new TraversalSpec();
        visitFolders.setType("Folder");
        visitFolders.setPath("childEntity");
        visitFolders.setSkip(Boolean.FALSE);
        visitFolders.setName("VisitFolders");
        List<SelectionSpec> sspecarrvf = new ArrayList<SelectionSpec>();
        sspecarrvf.add(crToRp);
        sspecarrvf.add(crToH);
        sspecarrvf.add(dcToVmf);
        sspecarrvf.add(dcToHf);
        sspecarrvf.add(vAppToRp);
        sspecarrvf.add(vAppToVM);
        sspecarrvf.add(dcToDs);
        sspecarrvf.add(rpToVm);
        sspecarrvf.add(sspecvfolders);
        visitFolders.getSelectSet().addAll(sspecarrvf);
        return new SelectionSpec[]{visitFolders};
    }

    public void getUpdates() throws RuntimeFaultFaultMsg, IOException, InvalidCollectorVersionFaultMsg, InvalidPropertyFaultMsg {
        ManagedObjectReference vmRef = getMOREFs.vmByVMname(virtualmachinename, this.propCollectorRef);


        if (vmRef == null) {
            vmRef = getMOREFs.vmByVMUUID(virtualmachinename, this.propCollectorRef);
            if (vmRef == null){
                System.out.println("Virtual Machine " + virtualmachinename + " Not Found");
                return;
            }
        }
        String[][] typeInfo =
                {new String[]{"VirtualMachine", "name", "summary.quickStats","guest", "config", "summary", "rootSnapshot",
                        "runtime","summary.config.instanceUuid","config.hardware.device","datastore", "snapshot",
                        "config.cpuAllocation.shares.shares","config.memoryAllocation.shares.shares"}};

        List<PropertySpec> pSpecs = buildPropertySpecArray(typeInfo);
        List<ObjectSpec> oSpecs = new ArrayList<ObjectSpec>();
        boolean oneOnly = vmRef != null;
        ObjectSpec os = new ObjectSpec();
        os.setObj(oneOnly ? vmRef : rootRef);
        os.setSkip(new Boolean(!oneOnly));
        if (!oneOnly) {
            os.getSelectSet().addAll(Arrays.asList(buildFullTraversal()));
        }
        oSpecs.add(os);
        PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
        propertyFilterSpec.getPropSet().addAll(pSpecs);
        propertyFilterSpec.getObjectSet().addAll(oSpecs);
        ManagedObjectReference propFilter =
                vimPort.createFilter(propCollectorRef, propertyFilterSpec, false);

        BufferedReader console =
                new BufferedReader(new InputStreamReader(System.in));

        String version = "";
        do {
            UpdateSet update = vimPort.waitForUpdatesEx(propCollectorRef, version, null);
            if (update != null && update.getFilterSet() != null) {
                handleUpdate(update);
                version = update.getVersion();
            } else {
                System.out.println("No update is present!");
            }
            if (update.isTruncated() == null || !update.isTruncated()) {
                break;
            }
        } while (true);
        vimPort.destroyPropertyFilter(propFilter);
    }

    void handleUpdate(UpdateSet update) {
        List<ObjectUpdate> vmUpdates = new ArrayList<ObjectUpdate>();
        List<ObjectUpdate> hostUpdates = new ArrayList<ObjectUpdate>();
        List<PropertyFilterUpdate> pfus = update.getFilterSet();

        for (PropertyFilterUpdate pfu : pfus) {
            List<ObjectUpdate> listobup = pfu.getObjectSet();
            for (ObjectUpdate oup : listobup) {
                if (oup.getObj().getType().equals("VirtualMachine")) {
                    vmUpdates.add(oup);
                } else if (oup.getObj().getType().equals("HostSystem")) {
                    hostUpdates.add(oup);
                }
            }
        }

        if (vmUpdates.size() > 0) {
            System.out.println("Virtual Machine updates:");
            for (ObjectUpdate up : vmUpdates) {
                handleObjectUpdate(up);
            }
        }
        if (hostUpdates.size() > 0) {
            System.out.println("Host updates:");
            for (ObjectUpdate up : hostUpdates) {
                handleObjectUpdate(up);
            }
        }
    }

    void handleObjectUpdate(ObjectUpdate oUpdate) {
        List<PropertyChange> pc = oUpdate.getChangeSet();
        if (oUpdate.getKind() == ObjectUpdateKind.ENTER) {
            System.out.println(" New Data:");
            handleChanges(pc);
        } else if (oUpdate.getKind() == ObjectUpdateKind.LEAVE) {
            System.out.println(" Removed Data:");
            handleChanges(pc);
        } else if (oUpdate.getKind() == ObjectUpdateKind.MODIFY) {
            System.out.println(" Changed Data:");
            handleChanges(pc);
        }

    }

    void handleChanges(List<PropertyChange> changes) {
        for (int pci = 0; pci < changes.size(); ++pci) {
            String name = changes.get(pci).getName();
            Object value = changes.get(pci).getVal();
            PropertyChangeOp op = changes.get(pci).getOp();
            if (op != PropertyChangeOp.REMOVE) {
                System.out.println("  -----------------  Property Name: " + name);
                if ("summary.quickStats".equals(name)) {
                    if (value instanceof VirtualMachineQuickStats) {
                        VirtualMachineQuickStats vmqs =
                                (VirtualMachineQuickStats) value;
                        String cpu =
                                vmqs.getOverallCpuUsage() == null ? "unavailable"
                                        : vmqs.getOverallCpuUsage().toString();
                        String memory =
                                vmqs.getHostMemoryUsage() == null ? "unavailable"
                                        : vmqs.getHostMemoryUsage().toString();
                        System.out.println("   Guest Status: "
                                + vmqs.getGuestHeartbeatStatus().toString());
                        System.out.println("   CPU Load %: " + cpu);
                        System.out.println("   Memory Load %: " + memory);
                    } else if (value instanceof HostListSummaryQuickStats) {
                        HostListSummaryQuickStats hsqs =
                                (HostListSummaryQuickStats) value;
                        String cpu =
                                hsqs.getOverallCpuUsage() == null ? "unavailable"
                                        : hsqs.getOverallCpuUsage().toString();
                        String memory =
                                hsqs.getOverallMemoryUsage() == null ? "unavailable"
                                        : hsqs.getOverallMemoryUsage().toString();
                        System.out.println("   CPU Load %: " + cpu);
                        System.out.println("   Memory Load %: " + memory);
                    }
                } else if ("runtime".equals(name)) {
                    if (value instanceof VirtualMachineRuntimeInfo) {
                        VirtualMachineRuntimeInfo vmri = (VirtualMachineRuntimeInfo) value;
                        System.out.println("   Power State: "
                                + vmri.getPowerState().toString());
                        System.out.println("   Connection State: "
                                + vmri.getConnectionState().toString());
                        XMLGregorianCalendar bTime = vmri.getBootTime();
                        if (bTime != null) {
                            System.out.println("   Boot Time: "
                                    + bTime.toGregorianCalendar().getTime());
                        }
                        Long mOverhead = vmri.getMemoryOverhead();
                        if (mOverhead != null) {
                            System.out.println("   Memory Overhead: " + mOverhead);
                        }
                        ManagedObjectReference hostmrf = vmri.getHost();
                        ManagedObjectReference crmor = null;
                        if (hostmrf != null) {
                            crmor = getMOREFs.getParent(hostmrf,"Datacenter");
                            if (crmor != null){
                                try {
                                    String center = (String) getMOREFs.entityProps(crmor, new String[]{"name"}).get("name");
                                    System.out.println("Datacenter : " + center);
                                } catch (InvalidPropertyFaultMsg invalidPropertyFaultMsg) {
                                    invalidPropertyFaultMsg.printStackTrace();
                                } catch (RuntimeFaultFaultMsg runtimeFaultFaultMsg) {
                                    runtimeFaultFaultMsg.printStackTrace();
                                }
                            }
                        }
                    } else if (value instanceof HostRuntimeInfo) {
                        HostRuntimeInfo hri = (HostRuntimeInfo) value;
                        System.out.println("   Connection State: "
                                + hri.getConnectionState().toString());
                        XMLGregorianCalendar bTime = hri.getBootTime();
                        if (bTime != null) {
                            System.out.println("   Boot Time: "
                                    + bTime.toGregorianCalendar().getTime());
                        }
                    }
                } else if ("name".equals(name)) {
                    System.out.println("name : " + value);
                }else if ("config.cpuAllocation.shares.shares".equals(name)) {
                    System.out.println("config.cpuAllocation.shares.shares : " + value);
                }else if ("config.memoryAllocation.shares.shares".equals(name)) {
                    System.out.println("config.memoryAllocation.shares.shares : " + value);
                }else if("config.hardware.device".equals(name)){
                    if(value instanceof ArrayOfVirtualDevice){
                        List<VirtualDevice> deviceList = ((ArrayOfVirtualDevice) value).getVirtualDevice();
                        for (VirtualDevice device : deviceList) {
                            if (device instanceof VirtualDisk) {
                                VirtualDisk disk = (VirtualDisk) device;
                                System.out.println("Disk id : " + disk.getDiskObjectId());
                                Description description = disk.getDeviceInfo();
                                System.out.println("Disk label : " + description.getLabel());
                                System.out.println("Disk summary : " + description.getSummary());
                                System.out.println("----------------------------");
                            }
                        }
                    }
                } else if("datastore".equals(name)){
                    if(value instanceof ArrayOfManagedObjectReference){
                        ArrayOfManagedObjectReference reference = (ArrayOfManagedObjectReference) value;
                        List<ManagedObjectReference> datastores = reference.getManagedObjectReference();
                        for (ManagedObjectReference datastore : datastores){
                            DatastoreSummary ds = null;
                            try {
                                ds = (DatastoreSummary) getMOREFs.entityProps(datastore, new String[]{"summary"}).get("summary");
                            } catch (InvalidPropertyFaultMsg invalidPropertyFaultMsg) {
                                invalidPropertyFaultMsg.printStackTrace();
                            } catch (RuntimeFaultFaultMsg runtimeFaultFaultMsg) {
                                runtimeFaultFaultMsg.printStackTrace();
                            }
                            if (ds != null){

                                System.out.println("name: " +ds.getName() +
                                        ", freeSpace: " + ds.getFreeSpace() + "KB" + ", total: " + ds.getCapacity() + "KB" +
                                        ", Type: " + ds.getType());
                            }
                        }
                    }
                }else if ("guest".equals(name)){
                    if (value instanceof GuestInfo){
                        GuestInfo guestInfo = (GuestInfo) value;
                        System.out.println("IpAddress: " + guestInfo.getIpAddress());
                        if (guestInfo != null){
                            //vmInfo.setIpAddress(guestInfo.getIpAddress());
                        }
                    }
                }else {
                    System.out.println(name + " : " + value);
                }
            } else {
                System.out.println("Property Name: " + name + " value removed.");
            }
        }
    }

    /**
     * This code takes an array of [typename, property, property, ...] and
     * converts it into a PropertySpec[]. handles case where multiple references
     * to the same typename are specified.
     *
     * @param typeinfo 2D array of type and properties to retrieve
     * @return Array of container filter specs
     */
    List<PropertySpec> buildPropertySpecArray(String[][] typeinfo) {
        // Eliminate duplicates
        HashMap<String, Set<String>> tInfo = new HashMap<String, Set<String>>();
        for (int ti = 0; ti < typeinfo.length; ++ti) {
            Set<String> props = tInfo.get(typeinfo[ti][0]);
            if (props == null) {
                props = new HashSet<String>();
                tInfo.put(typeinfo[ti][0], props);
            }
            boolean typeSkipped = false;
            for (int pi = 0; pi < typeinfo[ti].length; ++pi) {
                String prop = typeinfo[ti][pi];
                if (typeSkipped) {
                    props.add(prop);
                } else {
                    typeSkipped = true;
                }
            }
        }

        // Create PropertySpecs
        ArrayList<PropertySpec> pSpecs = new ArrayList<PropertySpec>();

        for (String type : tInfo.keySet()) {
            PropertySpec pSpec = new PropertySpec();
            Set<String> props = tInfo.get(type);
            pSpec.setType(type);
            pSpec.setAll(props.isEmpty() ? Boolean.TRUE : Boolean.FALSE);
            for (Iterator<?> pi = props.iterator(); pi.hasNext(); ) {
                String prop = (String) pi.next();
                pSpec.getPathSet().add(prop);
            }
            pSpecs.add(pSpec);
        }

        return pSpecs;
    }

    @Action
    public void action() throws RuntimeFaultFaultMsg, IOException, InvalidPropertyFaultMsg, InvalidCollectorVersionFaultMsg {
        this.propCollectorRef = serviceContent.getPropertyCollector();
        getUpdates();
    }
}
