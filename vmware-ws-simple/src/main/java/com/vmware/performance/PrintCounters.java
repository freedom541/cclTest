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

package com.vmware.performance;

import com.vmware.common.annotations.Action;
import com.vmware.common.annotations.Option;
import com.vmware.common.annotations.Sample;
import com.vmware.connection.ConnectedVimServiceBase;
import com.vmware.vim25.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <pre>
 * PrintCounters
 *
 * This sample writes available VM, Hostsystem or ResourcePool
 * perf counters into the file specified
 *
 * <b>Parameters:</b>
 * url          [required] : url of the web service
 * username     [required] : username for the authentication
 * password     [required] : password for the authentication
 * entitytype   [required] : Managed entity
 *                          [HostSystem|VirtualMachine|ResourcePool]
 * entityname   [required] : name of the managed entity
 * filename     [required] : Full path of filename to write to
 *
 * <b>Command Line:</b>
 * Save counters available for a host
 * run.bat com.vmware.performance.PrintCounters
 * --url https://myHost.com/sdk
 * --username [user]  --password [password] --entitytype HostSystem
 * --entityname myHost.com --filename myHostCounters
 * </pre>
 */
@Sample(
        name = "print-counters",
        description = "This sample writes available VM, Hostsystem or ResourcePool " +
                "perf counters into the file specified"
)
public class PrintCounters extends ConnectedVimServiceBase {
    private ManagedObjectReference perfManager;
    private ManagedObjectReference propCollectorRef;

    String filename;
    String entityname;
    String entitytype;

    @Option(name = "entitytype", description = "Managed entity [HostSystem|VirtualMachine|ResourcePool]")
    public void setEntitytype(String entitytype) {
        this.entitytype = entitytype;
    }

    @Option(name = "entityname", description = "name of the managed entity")
    public void setEntityname(String entityname) {
        this.entityname = entityname;
    }

    @Option(name = "filename", description = "Full path of filename to write to")
    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * Uses the new RetrievePropertiesEx method to emulate the now deprecated
     * RetrieveProperties method.
     *
     * @param listpfs
     * @return list of object content
     * @throws Exception
     */
    List<ObjectContent> retrievePropertiesAllObjects(
            List<PropertyFilterSpec> listpfs) throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {

        RetrieveOptions propObjectRetrieveOpts = new RetrieveOptions();

        List<ObjectContent> listobjcontent = new ArrayList<ObjectContent>();

        RetrieveResult rslts =
                vimPort.retrievePropertiesEx(propCollectorRef, listpfs,
                        propObjectRetrieveOpts);
        if (rslts != null && rslts.getObjects() != null
                && !rslts.getObjects().isEmpty()) {
            listobjcontent.addAll(rslts.getObjects());
        }
        String token = null;
        if (rslts != null && rslts.getToken() != null) {
            token = rslts.getToken();
        }
        while (token != null && !token.isEmpty()) {
            rslts =
                    vimPort.continueRetrievePropertiesEx(propCollectorRef, token);
            token = null;
            if (rslts != null) {
                token = rslts.getToken();
                if (rslts.getObjects() != null && !rslts.getObjects().isEmpty()) {
                    listobjcontent.addAll(rslts.getObjects());
                }
            }
        }

        return listobjcontent;
    }

    void printCounters() throws RuntimeFaultFaultMsg, IOException, InvalidPropertyFaultMsg {
        String entityType = entitytype;

        if (entityType.equalsIgnoreCase("HostSystem")) {
            printEntityCounters("HostSystem");
        } else if (entityType.equalsIgnoreCase("VirtualMachine")) {
            printEntityCounters("VirtualMachine");
        } else if (entityType.equals("ResourcePool")) {
            printEntityCounters("ResourcePool");
        } else {
            System.out.println("Entity Argument must be "
                    + "[HostSystem|VirtualMachine|ResourcePool]");
        }
    }

    /**
     * This method initializes all the performance counters available on the
     * system it is connected to. The performance counters are stored in the
     * hashmap counters with group.counter.rolluptype being the key and id being
     * the value.
     */
    List<PerfCounterInfo> getPerfCounters() throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        List<PerfCounterInfo> pciArr = null;
        // Create Property Spec
        PropertySpec propertySpec = new PropertySpec();
        propertySpec.setAll(Boolean.FALSE);
        propertySpec.getPathSet().add("perfCounter");
        propertySpec.setType("PerformanceManager");
        List<PropertySpec> propertySpecs = new ArrayList<PropertySpec>();
        propertySpecs.add(propertySpec);

        // Now create Object Spec
        ObjectSpec objectSpec = new ObjectSpec();
        objectSpec.setObj(perfManager);
        List<ObjectSpec> objectSpecs = new ArrayList<ObjectSpec>();
        objectSpecs.add(objectSpec);

        // Create PropertyFilterSpec using the PropertySpec and ObjectPec
        // created above.
        PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
        propertyFilterSpec.getPropSet().add(propertySpec);
        propertyFilterSpec.getObjectSet().add(objectSpec);
        List<PropertyFilterSpec> propertyFilterSpecs =
                new ArrayList<PropertyFilterSpec>();
        propertyFilterSpecs.add(propertyFilterSpec);
        List<PropertyFilterSpec> listpfs =
                new ArrayList<PropertyFilterSpec>(1);
        listpfs.add(propertyFilterSpec);
        List<ObjectContent> listobjcont =
                retrievePropertiesAllObjects(listpfs);

        if (listobjcont != null) {
            for (ObjectContent oc : listobjcont) {
                List<DynamicProperty> dps = oc.getPropSet();
                if (dps != null) {
                    for (DynamicProperty dp : dps) {
                        List<PerfCounterInfo> pcinfolist =
                                ((ArrayOfPerfCounterInfo) dp.getVal())
                                        .getPerfCounterInfo();
                        pciArr = pcinfolist;
                    }
                }
            }
        }
        return pciArr;
    }

    void printEntityCounters(String entityType) throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg, IOException {

        ManagedObjectReference mor =
                getMOREFs.inFolderByType(serviceContent.getRootFolder(), entityType)
                        .get(entityname);

        List<PerfCounterInfo> cInfo = getPerfCounters();

        if (mor != null) {
            Set<?> ids = getPerfIdsAvailable(perfManager, mor);
            PrintWriter out =
                    new PrintWriter(new BufferedWriter(new FileWriter(filename)));
            if (cInfo != null) {
                out.println("<perf-counters>");
                for (int c = 0; c < cInfo.size(); ++c) {
                    PerfCounterInfo pci = cInfo.get(c);
                    Integer id = new Integer(pci.getKey());
                    if (ids.contains(id)) {
                        out.print("  <perf-counter key=\"");
                        out.print(id);
                        out.print("\" ");

                        out.print("rollupType=\"");
                        out.print(pci.getRollupType());
                        out.print("\" ");

                        out.print("statsType=\"");
                        out.print(pci.getStatsType());
                        out.println("\">");
                        printElementDescription(out, "groupInfo", pci.getGroupInfo());
                        printElementDescription(out, "nameInfo", pci.getNameInfo());
                        printElementDescription(out, "unitInfo", pci.getUnitInfo());

                        out.println("    <entity type=\"" + entityType + "\"/>");
                        List<Integer> listint = pci.getAssociatedCounterId();
                        int[] ac = new int[listint.size()];
                        for (int i = 0; i < listint.size(); i++) {
                            ac[i] = listint.get(i);
                        }
                        if (ac != null) {
                            for (int a = 0; a < ac.length; ++a) {
                                out.println("    <associatedCounter>" + ac[a]
                                        + "</associatedCounter>");
                            }
                        }
                        out.println("  </perf-counter>");
                    }
                }
                out.println("</perf-counters>");
                out.flush();
                out.close();
            }
            System.out.println("Check " + filename + " for Print Counters");
        } else {
            System.out.println(entityType + " " + entityname + " not found.");
        }
    }

    void printElementDescription(PrintWriter out, String name,
                                 ElementDescription ed) {
        out.print("   <" + name + "-key>");
        out.print(ed.getKey());
        out.println("</" + name + "-key>");

        out.print("   <" + name + "-label>");
        out.print(ed.getLabel());
        out.println("</" + name + "-label>");

        out.print("   <" + name + "-summary>");
        out.print(ed.getSummary());
        out.println("</" + name + "-summary>");
    }

    Set<Integer> getPerfIdsAvailable(
            ManagedObjectReference perfMoRef, ManagedObjectReference entityMoRef) throws RuntimeFaultFaultMsg {
        Set<Integer> ret = new HashSet<Integer>();
        if (entityMoRef != null) {
            List<PerfMetricId> listpermids =
                    vimPort.queryAvailablePerfMetric(perfMoRef, entityMoRef, null,
                            null, new Integer(300));
            if (listpermids != null) {
                for (int i = 0; i < listpermids.size(); ++i) {
                    ret.add(new Integer(listpermids.get(i).getCounterId()));
                }
            }
        }
        return ret;
    }

    @Action
    public void run() throws RuntimeFaultFaultMsg, IOException, InvalidPropertyFaultMsg {
        propCollectorRef = serviceContent.getPropertyCollector();
        perfManager = serviceContent.getPerfManager();
        printCounters();
    }
}
