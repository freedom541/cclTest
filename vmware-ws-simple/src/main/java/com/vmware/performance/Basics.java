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

import com.vmware.common.Main;
import com.vmware.common.annotations.Action;
import com.vmware.common.annotations.Option;
import com.vmware.common.annotations.Sample;
import com.vmware.connection.ConnectedVimServiceBase;
import com.vmware.vim25.*;

import javax.xml.datatype.DatatypeConfigurationException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <pre>
 * Basics
 *
 * This sample displays available performance counters or other data
 * for an requested ESX system. Output is in following report style:
 *
 * Performance Interval:
 *    Interval Name
 *    Interval Period
 *    Interval Length
 *    Performance counters:
 *    Host perf capabilities:
 *    Summary supported
 *    Current supported
 *    Current refresh rate
 *
 * <b>Parameters:</b>
 * url          [required] : url of the web service
 * username     [required] : username for the authentication
 * password     [required] : password for the authentication
 * info         [required] : requested info - [interval|counter|host]
 * hostname     [optional] : required when 'info' is 'host'
 *
 * <b>Command Line:</b>
 * Display name and description of all perf counters on VCenter
 * run.bat com.vmware.performance.Basics --url [webserviceurl]
 * --username [username] --password [password]
 * --info [interval|counter|host] --hostname [VC hostname]
 *
 * Display counter names, sampling period, length of all intervals
 * run.bat com.vmware.performance.Basics --url [webserviceurl]
 * --username [username] --password [password]
 * --info interval --hostname [VC or ESX hostname]
 *
 * Display name and description of all perf counters on ESX
 *  run.bat com.vmware.performance.Basics --url [webserviceurl]
 *  --username [username] --password [password]
 * --info counter --hostname [ESX hostname]
 *
 * Display name, description and metrics of all perf counters on ESX
 * run.bat com.vmware.performance.Basics --url [webserviceurl]
 * --username [username] --password [password]
 * --info host  --hostname [ESX hostname]
 * </pre>
 */
@Sample(name = "performance-basics", description = "displays available performance counters or other data")
public class Basics extends ConnectedVimServiceBase {
    private ManagedObjectReference propCollectorRef;
    private ManagedObjectReference perfManager;

    private String info;
    private String hostname;

    @Option(name = "info",description = "requested info - [interval|counter|host]")
    public void setInfo(String info) {
        this.info = info;
    }

    @Option(name="hostname", required = false,description = "required when 'info' is 'host'")
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    void validateTheInput() {
        if (info.equalsIgnoreCase("host")) {
            if (hostname == null) {
                throw new Main.SampleInputValidationException("Must specify the --hostname"
                        + " parameter when --info is host");
            }
        }
        return;
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

    /**
     * @return TraversalSpec specification to get to the HostSystem managed
     *         object.
     */
    TraversalSpec getHostSystemTraversalSpec() {
        // Create a traversal spec that starts from the 'root' objects
        // and traverses the inventory tree to get to the Host system.
        // Build the traversal specs bottoms up
        SelectionSpec ss = new SelectionSpec();
        ss.setName("VisitFolders");

        // Traversal to get to the host from ComputeResource
        TraversalSpec computeResourceToHostSystem = new TraversalSpec();
        computeResourceToHostSystem.setName("computeResourceToHostSystem");
        computeResourceToHostSystem.setType("ComputeResource");
        computeResourceToHostSystem.setPath("host");
        computeResourceToHostSystem.setSkip(false);
        computeResourceToHostSystem.getSelectSet().add(ss);

        // Traversal to get to the ComputeResource from hostFolder
        TraversalSpec hostFolderToComputeResource = new TraversalSpec();
        hostFolderToComputeResource.setName("hostFolderToComputeResource");
        hostFolderToComputeResource.setType("Folder");
        hostFolderToComputeResource.setPath("childEntity");
        hostFolderToComputeResource.setSkip(false);
        hostFolderToComputeResource.getSelectSet().add(ss);

        // Traversal to get to the hostFolder from DataCenter
        TraversalSpec dataCenterToHostFolder = new TraversalSpec();
        dataCenterToHostFolder.setName("DataCenterToHostFolder");
        dataCenterToHostFolder.setType("Datacenter");
        dataCenterToHostFolder.setPath("hostFolder");
        dataCenterToHostFolder.setSkip(false);
        dataCenterToHostFolder.getSelectSet().add(ss);

        //TraversalSpec to get to the DataCenter from rootFolder
        TraversalSpec traversalSpec = new TraversalSpec();
        traversalSpec.setName("VisitFolders");
        traversalSpec.setType("Folder");
        traversalSpec.setPath("childEntity");
        traversalSpec.setSkip(false);

        List<SelectionSpec> sSpecArr = new ArrayList<SelectionSpec>();
        sSpecArr.add(ss);
        sSpecArr.add(dataCenterToHostFolder);
        sSpecArr.add(hostFolderToComputeResource);
        sSpecArr.add(computeResourceToHostSystem);
        traversalSpec.getSelectSet().addAll(sSpecArr);
        return traversalSpec;
    }

    /**
     * Retrieves the MOREF of the host.
     *
     * @param hostName :
     * @return
     */
    ManagedObjectReference getHostByHostName(String hostName) throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        ManagedObjectReference retVal = null;
        ManagedObjectReference rootFolder = serviceContent.getRootFolder();
        TraversalSpec tSpec = getHostSystemTraversalSpec();
        // Create Property Spec
        PropertySpec propertySpec = new PropertySpec();
        propertySpec.setAll(Boolean.FALSE);
        propertySpec.getPathSet().add("name");
        propertySpec.setType("HostSystem");

        // Now create Object Spec
        ObjectSpec objectSpec = new ObjectSpec();
        objectSpec.setObj(rootFolder);
        objectSpec.setSkip(Boolean.TRUE);
        objectSpec.getSelectSet().add(tSpec);

        // Create PropertyFilterSpec using the PropertySpec and ObjectPec
        // created above.
        PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
        propertyFilterSpec.getPropSet().add(propertySpec);
        propertyFilterSpec.getObjectSet().add(objectSpec);
        List<PropertyFilterSpec> listpfs =
                new ArrayList<PropertyFilterSpec>(1);
        listpfs.add(propertyFilterSpec);
        List<ObjectContent> listobjcont =
                retrievePropertiesAllObjects(listpfs);

        if (listobjcont != null) {
            for (ObjectContent oc : listobjcont) {
                ManagedObjectReference mr = oc.getObj();
                String hostnm = null;
                List<DynamicProperty> listDynamicProps = oc.getPropSet();
                DynamicProperty[] dps =
                        listDynamicProps
                                .toArray(new DynamicProperty[listDynamicProps.size()]);
                if (dps != null) {
                    for (DynamicProperty dp : dps) {
                        hostnm = (String) dp.getVal();
                    }
                }
                if (hostnm != null && hostnm.equals(hostName)) {
                    retVal = mr;
                    break;
                }
            }
        } else {
            System.out.println("The Object Content is Null");
        }
        return retVal;
    }

    void displayBasics() throws RuntimeFaultFaultMsg,
            DatatypeConfigurationException, InvalidPropertyFaultMsg {

        if (info.equalsIgnoreCase("interval")) {
            getIntervals(perfManager, vimPort);
        } else if (info.equalsIgnoreCase("counter")) {
            getCounters(perfManager, vimPort);
        } else if (info.equalsIgnoreCase("host")) {
            ManagedObjectReference hostmor = getHostByHostName(hostname);
            if (hostmor == null) {
                System.out.println("Host " + hostname + " not found");
                return;
            }
            getQuerySummary(perfManager, hostmor, vimPort);
            getQueryAvailable(perfManager, hostmor, vimPort);
        } else {
            System.out.println("Invalid info argument [host|counter|interval]");
        }
    }

    void getIntervals(ManagedObjectReference perfMgr,
                      VimPortType service) throws InvalidPropertyFaultMsg,
            RuntimeFaultFaultMsg {
        Object property = getProperty(service, perfMgr, "historicalInterval");
        ArrayOfPerfInterval arrayInterval = (ArrayOfPerfInterval) property;
        List<PerfInterval> intervals = arrayInterval.getPerfInterval();
        System.out.println("Performance intervals (" + intervals.size() + "):");
        System.out.println("---------------------");

        int count = 0;
        for (PerfInterval interval : intervals) {
            System.out.print((++count) + ": " + interval.getName());
            System.out.print(" -- period = " + interval.getSamplingPeriod());
            System.out.println(", length = " + interval.getLength());
        }
        System.out.println();
    }

    void getCounters(ManagedObjectReference perfMgr,
                     VimPortType service) throws InvalidPropertyFaultMsg,
            RuntimeFaultFaultMsg {
        Object property = getProperty(service, perfMgr, "perfCounter");
        ArrayOfPerfCounterInfo arrayCounter = (ArrayOfPerfCounterInfo) property;
        List<PerfCounterInfo> counters = arrayCounter.getPerfCounterInfo();
        System.out.println("Performance counters (averages only):");
        System.out.println("-------------------------------------");
        for (PerfCounterInfo counter : counters) {
            if (counter.getRollupType() == PerfSummaryType.AVERAGE) {
                ElementDescription desc = counter.getNameInfo();
                System.out.println(desc.getLabel() + ": " + desc.getSummary());
            }
        }
        System.out.println();
    }

    void getQuerySummary(ManagedObjectReference perfMgr,
                         ManagedObjectReference hostmor, VimPortType service)
            throws RuntimeFaultFaultMsg {
        PerfProviderSummary summary =
                service.queryPerfProviderSummary(perfMgr, hostmor);
        System.out.println("Host perf capabilities:");
        System.out.println("----------------------");
        System.out
                .println("  Summary supported: " + summary.isSummarySupported());
        System.out
                .println("  Current supported: " + summary.isCurrentSupported());
        if (summary.isCurrentSupported()) {
            System.out.println("  Current refresh rate: "
                    + summary.getRefreshRate());
        }
        System.out.println();
    }

    void getQueryAvailable(ManagedObjectReference perfMgr,
                           ManagedObjectReference hostmor, VimPortType service)
            throws DatatypeConfigurationException, RuntimeFaultFaultMsg {

        PerfProviderSummary perfProviderSummary =
                service.queryPerfProviderSummary(perfMgr, hostmor);
        List<PerfMetricId> pmidlist =
                service.queryAvailablePerfMetric(perfMgr, hostmor, null, null,
                        perfProviderSummary.getRefreshRate());

        List<Integer> idslist = new ArrayList<Integer>();

        for (int i = 0; i != pmidlist.size(); ++i) {
            idslist.add(pmidlist.get(i).getCounterId());
        }

        List<PerfCounterInfo> pcinfolist =
                service.queryPerfCounter(perfMgr, idslist);
        System.out.println("Available real-time metrics for host ("
                + pmidlist.size() + "):");
        System.out.println("--------------------------");
        for (int i = 0; i != pmidlist.size(); ++i) {
            String label = pcinfolist.get(i).getNameInfo().getLabel();
            String instance = pmidlist.get(i).getInstance();
            System.out.print("   " + label);
            if (instance.length() != 0) {
                System.out.print(" [" + instance + "]");
            }
            System.out.println();
        }
        System.out.println();
    }

    List<Object> getProperties(VimPortType service,
                               ManagedObjectReference moRef, List<String> properties)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
        PropertySpec pSpec = new PropertySpec();
        pSpec.setType(moRef.getType());
        pSpec.getPathSet().addAll(properties);
        ObjectSpec oSpec = new ObjectSpec();
        oSpec.setObj(moRef);
        PropertyFilterSpec pfSpec = new PropertyFilterSpec();
        pfSpec.getPropSet().addAll(Arrays.asList(new PropertySpec[]{pSpec}));
        pfSpec.getObjectSet().addAll(Arrays.asList(new ObjectSpec[]{oSpec}));
        List<PropertyFilterSpec> listpfs = new ArrayList<PropertyFilterSpec>();
        listpfs.add(pfSpec);
        List<ObjectContent> listobjcont = retrievePropertiesAllObjects(listpfs);
        List<Object> ret = new ArrayList<Object>();
        if (listobjcont != null) {
            for (int i = 0; i < listobjcont.size(); ++i) {
                ObjectContent oc = listobjcont.get(i);
                List<DynamicProperty> listdps = oc.getPropSet();
                if (listdps != null) {
                    for (int j = 0; j < listdps.size(); ++j) {
                        DynamicProperty dp = listdps.get(j);
                        for (int p = 0; p < properties.size(); ++p) {
                            if (properties.get(p).equals(dp.getName())) {
                                ret.add(dp.getVal());
                            }
                        }
                    }
                }
            }
        }
        return ret;
    }

    Object getProperty(VimPortType service,
                       ManagedObjectReference moRef, String prop)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
        List<String> property = new ArrayList<String>();
        property.add(prop);
        List<Object> props = getProperties(service, moRef, property);
        if (props.size() > 0) {
            return props.get(0);
        } else {
            return null;
        }
    }

    @Action
    public void run() throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg, DatatypeConfigurationException {
        validateTheInput();
        propCollectorRef = serviceContent.getPropertyCollector();
        perfManager = serviceContent.getPerfManager();
        displayBasics();
    }
}