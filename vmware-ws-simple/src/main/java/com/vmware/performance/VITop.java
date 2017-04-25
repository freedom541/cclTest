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
import com.vmware.connection.Connection;
import com.vmware.performance.widgets.StatsTable;
import com.vmware.vim25.*;

import javax.swing.*;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.soap.SOAPFaultException;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.*;
import java.util.Timer;

/**
 * <pre>
 * VITop
 *
 * This sample is an ESX-Top-like application that lets administrators specify
 * the CPU and memory counters by name to obtain metrics for a specified host
 *
 * <b>Parameters:</b>
 * url        [required] : url of the web service
 * username   [required] : username for the authentication
 * password   [required] : password for the authentication
 * host       [required] : name of the host
 * cpu        [required] : CPU counter name
 *                         e.g. usage, ready, guaranteed
 * memory     [required] : memory counter name
 *                         e.g. usage, granted
 *
 * <b>Command Line:</b>
 * run.bat com.vmware.performance.VITop
 * --url [webservice url] --username [user] --password [password]
 * --host [FQDN_host_name]
 * --cpu [cpu_counter_name] --memory [mem_counter_name]
 * </pre>
 */
@Sample(
        name = "vi-top",
        description = "an ESX-Top-like application that lets " +
                "administrators specify the CPU and memory " +
                "counters by name to obtain metrics for a specified host"
)
public class VITop {
    Connection connection;
    String hostname;
    String cpu;
    String memory;

    @Option(name = "connection", type = Connection.class)
    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    @Option(name = "host", description = "name of the host")
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    @Option(name = "cpu", description = "CPU counter name [usage|ready|guaranteed]")
    public void setCpu(String cpu) {
        this.cpu = cpu;
    }

    @Option(name = "memory", description = "memory counter name [usage|granted]")
    public void setMemory(String memory) {
        this.memory = memory;
    }

    ManagedObjectReference propCollectorRef;
    VimService vimService;
    VimPortType vimPort;
    ServiceContent serviceContent;

    StatsTable statsTable;
    ManagedObjectReference perfManager;
    PerfQuerySpec querySpec;

    /**
     * Uses the new RetrievePropertiesEx method to emulate the now deprecated
     * RetrieveProperties method.
     *
     * @param listpfs
     * @return list of object content
     * @throws Exception
     */
    List<ObjectContent> retrievePropertiesAllObjects(
            List<PropertyFilterSpec> listpfs) {

        RetrieveOptions propObjectRetrieveOpts = new RetrieveOptions();

        List<ObjectContent> listobjcontent = new ArrayList<ObjectContent>();

        try {
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
        } catch (SOAPFaultException sfe) {
            printSoapFaultException(sfe);
        } catch (Exception e) {
            System.out.println(" : Failed Getting Contents");
            e.printStackTrace();
        }

        return listobjcontent;
    }

    void createAndShowGUI(String firstColumnName,
                          List<String> statNames) {
        try {
            String lookAndFeel = UIManager.getSystemLookAndFeelClassName();
            UIManager.setLookAndFeel(lookAndFeel);
            JFrame.setDefaultLookAndFeelDecorated(true);
        } catch (SOAPFaultException sfe) {
            printSoapFaultException(sfe);
        } catch (Exception e) {
            e.printStackTrace();
        }

        JFrame frame = new JFrame("VITop");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {
            }

            @Override
            public void windowIconified(WindowEvent e) {
            }

            @Override
            public void windowDeiconified(WindowEvent e) {
            }

            @Override
            public void windowDeactivated(WindowEvent e) {
            }

            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    connection.disconnect();
                } catch (SOAPFaultException sfe) {
                    printSoapFaultException(sfe);
                } catch (Exception ex) {
                    System.out.println("Failed to disconnect - " + ex.getMessage());
                    ex.printStackTrace();
                }
            }

            @Override
            public void windowClosed(WindowEvent e) {
            }

            @Override
            public void windowActivated(WindowEvent e) {
            }
        });

        String[] columnNamesArray = new String[statNames.size() + 1];
        columnNamesArray[0] = firstColumnName;
        for (int i = 0; i < statNames.size(); i++) {
            columnNamesArray[i + 1] = statNames.get(i);
        }
        statsTable = new StatsTable(columnNamesArray);
        statsTable.setOpaque(true);
        frame.setContentPane(statsTable);

        frame.pack();
        frame.setVisible(true);
    }

    String getEntityName(ManagedObjectReference moRef)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
        String ret = null;
        try {
            ret = (String) getDynamicProperty(moRef, "name");
        } catch (SOAPFaultException sfe) {
            printSoapFaultException(sfe);
        } catch (Exception e) {
            return "<Unknown Entity>";
        }

        if (ret != null) {
            return ret;
        } else {
            return "<Unknown Entity>";
        }
    }

    /**
     * @param midList
     * @param compMetric
     * @return
     * @throws RuntimeException
     * @throws RemoteException
     * @throws InvalidPropertyFaultMsg
     * @throws RuntimeFaultFaultMsg
     */
    XMLGregorianCalendar displayStats(List<PerfMetricId> midList,
                                      PerfCompositeMetric compMetric) throws RuntimeException,
            RemoteException, InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
        if (compMetric == null || (compMetric.getEntity() == null)) {
            return null;
        }

        List<Object[]> data = new ArrayList<Object[]>();
        PerfEntityMetric entityMetric = (PerfEntityMetric) compMetric.getEntity();
        PerfMetricIntSeries intSeries =
                (PerfMetricIntSeries) entityMetric.getValue().get(0);
        int numSamples = entityMetric.getSampleInfo().size();

        XMLGregorianCalendar timeStamp =
                entityMetric.getSampleInfo().get(numSamples - 1).getTimestamp();
        long overallUsage = intSeries.getValue().get(numSamples - 1);
        System.out.println("Info Updated");
        int numColumns = midList.size() + 1;
        List<PerfEntityMetricBase> listpemb = compMetric.getChildEntity();
        List<PerfEntityMetricBase> childEntityMetric = listpemb;
        for (int childNum = 0; childNum < childEntityMetric.size(); childNum++) {
            PerfEntityMetric childStats =
                    (PerfEntityMetric) childEntityMetric.get(childNum);
            String childName = getEntityName(childStats.getEntity());
            int numChildSamples = childStats.getSampleInfo().size();
            Object[] tableData = new Object[numColumns];
            tableData[0] = childName;

            for (int i = 0; i < childStats.getValue().size(); i++) {
                PerfMetricIntSeries childSeries =
                        (PerfMetricIntSeries) childStats.getValue().get(i);
                int col = findStatsIndex(midList, childSeries.getId());
                if (col >= 0) {
                    long statsVal = childSeries.getValue().get(numChildSamples - 1);
                    tableData[col + 1] = new Long(statsVal);
                }
            }
            data.add(tableData);
        }

        if (statsTable != null) {
            statsTable.setData(timeStamp.toGregorianCalendar(), overallUsage,
                    "Mhz", data);
        }
        return timeStamp;
    }

    int findStatsIndex(List<PerfMetricId> midList,
                       PerfMetricId mid) {
        int count = 0;
        for (PerfMetricId pmid : midList) {
            if ((pmid.getCounterId() == mid.getCounterId())
                    && pmid.getInstance().equals(mid.getInstance())) {
                return count;
            }
            ++count;
        }
        return -1;
    }

    PerfCounterInfo getCounterInfo(
            List<PerfCounterInfo> counterInfo, String groupName, String counterName) {
        for (PerfCounterInfo info : counterInfo) {
            if (info.getGroupInfo().getKey().equals(groupName)
                    && info.getNameInfo().getKey().equals(counterName)) {
                return info;
            }
        }
        return null;
    }

    /**
     * @return
     * @throws Exception
     */
    String[][] getCounters() {

        String[] cpuCounters = cpu.split(",");
        String[] memCounters = memory.split(",");
        String[][] ret = new String[(cpuCounters.length + memCounters.length)][2];

        for (int i = 0; i < cpuCounters.length; i++) {
            ret[i] = new String[]{"cpu", cpuCounters[i]};
        }

        for (int i = 0; i < memCounters.length; i++) {
            ret[(cpuCounters.length + i)] = new String[]{"mem", memCounters[i]};
        }
        return ret;
    }

    /**
     *
     */
    void refreshStats() {
        try {
            PerfCompositeMetric metric =
                    vimPort.queryPerfComposite(perfManager, querySpec);
            XMLGregorianCalendar latestTs =
                    displayStats(querySpec.getMetricId(), metric);
            if (latestTs != null) {
                querySpec.setStartTime(latestTs);
            }
        } catch (SOAPFaultException sfe) {
            printSoapFaultException(sfe);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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
    ManagedObjectReference getHostByHostName(String hostName) {
        ManagedObjectReference retVal = null;
        ManagedObjectReference rootFolder = serviceContent.getRootFolder();
        try {
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
        } catch (SOAPFaultException sfe) {
            printSoapFaultException(sfe);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retVal;
    }

    /**
     * @throws Exception
     */
    void displayStats() throws RuntimeFaultFaultMsg, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        String[][] statsList = getCounters();
        ManagedObjectReference hostmor = getHostByHostName(hostname);
        if (hostmor == null) {
            System.out.println("Host Not Found");
            return;
        }

        @SuppressWarnings("unchecked")
        List<PerfCounterInfo> props =
                (List<PerfCounterInfo>) getDynamicProperty(perfManager,
                        "perfCounter");

        List<PerfMetricId> midVector = new ArrayList<PerfMetricId>();
        List<String> statNames = new ArrayList<String>();
        for (int i = 0; i < statsList.length; i++) {
            PerfCounterInfo counterInfo =
                    getCounterInfo(props, statsList[i][0], statsList[i][1]);
            if (counterInfo == null) {
                System.out.println("Warning: Unable to find stat "
                        + statsList[i][0] + " " + statsList[i][1]);
                continue;
            }
            String counterName = counterInfo.getNameInfo().getLabel();
            statNames.add(counterName);

            PerfMetricId pmid = new PerfMetricId();
            pmid.setCounterId(counterInfo.getKey());
            pmid.setInstance("");
            midVector.add(pmid);
        }
        List<PerfMetricId> midList = new ArrayList<PerfMetricId>(midVector);
        Collections.copy(midList, midVector);


        PerfProviderSummary perfProviderSummary =
                vimPort.queryPerfProviderSummary(perfManager, hostmor);
        PerfQuerySpec spec = new PerfQuerySpec();
        spec.setEntity(hostmor);
        spec.getMetricId().addAll(midList);
        spec.setIntervalId(perfProviderSummary.getRefreshRate());
        querySpec = spec;

        final List<String> statNames2 = statNames;
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                createAndShowGUI("VM Name", statNames2);
            }
        });

        Timer timer = new Timer(true);
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                refreshStats();
            }
        }, 1000, 21000);
    }

    Object getDynamicProperty(ManagedObjectReference mor,
                              String propertyName) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        ObjectContent[] objContent =
                getObjectProperties(mor, new String[]{propertyName});

        Object propertyValue = null;
        if (objContent != null) {
            List<DynamicProperty> listdp = objContent[0].getPropSet();
            if (listdp != null) {
                /*
                * Check the dynamic propery for ArrayOfXXX object
                */
                Object dynamicPropertyVal = listdp.get(0).getVal();
                String dynamicPropertyName =
                        dynamicPropertyVal.getClass().getName();
                if (dynamicPropertyName.indexOf("ArrayOf") != -1) {
                    String methodName =
                            dynamicPropertyName.substring(
                                    dynamicPropertyName.indexOf("ArrayOf")
                                            + "ArrayOf".length(),
                                    dynamicPropertyName.length());
                    /*
                    * If object is ArrayOfXXX object, then get the XXX[] by
                    * invoking getXXX() on the object.
                    * For Ex:
                    * ArrayOfManagedObjectReference.getManagedObjectReference()
                    * returns ManagedObjectReference[] array.
                    */
                    if (methodExists(dynamicPropertyVal, "get" + methodName, null)) {
                        methodName = "get" + methodName;
                    } else {
                        /*
                        * Construct methodName for ArrayOf primitive types
                        * Ex: For ArrayOfInt, methodName is get_int
                        */
                        methodName = "get_" + methodName.toLowerCase();
                    }
                    Method getMorMethod =
                            dynamicPropertyVal.getClass().getDeclaredMethod(
                                    methodName, (Class[]) null);
                    propertyValue =
                            getMorMethod.invoke(dynamicPropertyVal, (Object[]) null);
                } else if (dynamicPropertyVal.getClass().isArray()) {
                    /*
                    * Handle the case of an unwrapped array being deserialized.
                    */
                    propertyValue = dynamicPropertyVal;
                } else {
                    propertyValue = dynamicPropertyVal;
                }
            }
        }
        return propertyValue;
    }

    /**
     * Retrieve contents for a single object based on the property collector
     * registered with the service.
     *
     * @param mobj       Managed Object Reference to get contents for
     * @param properties names of properties of object to retrieve
     * @return retrieved object contents
     */
    ObjectContent[] getObjectProperties(
            ManagedObjectReference mobj, String[] properties) {
        if (mobj == null) {
            return null;
        }
        PropertyFilterSpec spec = new PropertyFilterSpec();
        spec.getPropSet().add(new PropertySpec());
        if ((properties == null || properties.length == 0)) {
            spec.getPropSet().get(0).setAll(Boolean.TRUE);
        } else {
            spec.getPropSet().get(0).setAll(Boolean.FALSE);
        }
        spec.getPropSet().get(0).setType(mobj.getType());
        spec.getPropSet().get(0).getPathSet().addAll(Arrays.asList(properties));
        spec.getObjectSet().add(new ObjectSpec());
        spec.getObjectSet().get(0).setObj(mobj);
        spec.getObjectSet().get(0).setSkip(Boolean.FALSE);
        List<PropertyFilterSpec> listpfs = new ArrayList<PropertyFilterSpec>(1);
        listpfs.add(spec);
        List<ObjectContent> listobjcont = retrievePropertiesAllObjects(listpfs);
        return listobjcont.toArray(new ObjectContent[listobjcont.size()]);
    }

    /**
     * Determines of a method 'methodName' exists for the Object 'obj'.
     *
     * @param obj            The Object to check
     * @param methodName     The method name
     * @param parameterTypes Array of Class objects for the parameter types
     * @return true if the method exists, false otherwise
     */
    @SuppressWarnings("rawtypes")
    boolean methodExists(Object obj, String methodName,
                         Class[] parameterTypes) {
        boolean exists = false;
        try {
            Method method = obj.getClass().getMethod(methodName, parameterTypes);
            if (method != null) {
                exists = true;
            }
        } catch (SOAPFaultException sfe) {
            printSoapFaultException(sfe);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return exists;
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
    public void run() throws RuntimeFaultFaultMsg, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        connection.connect();
        vimPort = connection.getVimPort();
        vimService = connection.getVimService();
        serviceContent = connection.getServiceContent();
        propCollectorRef = serviceContent.getPropertyCollector();

        perfManager = serviceContent.getPerfManager();

        displayStats();
    }
}
