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
import com.vmware.performance.widgets.LineChart;
import com.vmware.vim25.*;

import javax.swing.*;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.soap.SOAPFaultException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.List;

/**
 * <pre>
 * VIUsage
 *
 * This sample creates a GUI for graphical representation of the counters
 *
 * <b>Parameters:</b>
 * url        [required] : url of the web service
 * username   [required] : username for the authentication
 * password   [required] : password for the authentication
 * host       [required] : Name of the host
 * counter    [required] : Full counter name in dotted notation
 *                         (group.counter.rollup)
 *                         e.g. cpu.usage.none
 *
 * <b>Command Line:</b>
 * run.bat com.vmware.performance.VIUsage --url [webserviceurl]
 * --username [username] --password [password]
 * --host [host name] --counter [Counter_type {group.counter.rollup}]
 * </pre>
 */

@Sample(name = "vi-usage", description = "This sample creates a GUI for graphical representation of the counters")
public class VIUsage implements ActionListener {
    private VimPortType vimPort;
    private ServiceContent serviceContent;
    private ManagedObjectReference perfManager;
    private ManagedObjectReference propCollectorRef;

    private Connection connection;
    private String hostname;
    private String countername;

    @Option(name = "connection", type = Connection.class)
    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    @Option(name = "hostname")
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    @Option(name = "counter")
    public void setCountername(String countername) {
        this.countername = countername;
    }

    private PerfInterval[] intervals;
    private LineChart chart;
    private JPanel mainPanel, selectPanel, displayPanel;
    private JComboBox intervalBox = null;
    private JLabel chartLabel = null;
    private String stats;
    private ManagedObjectReference hostmor;
    private JFrame frame;

    /**
     * Establishes session with the virtual center server.
     *
     * @throws Exception the exception
     */
    void init() {
        connection.connect();
        serviceContent = connection.getServiceContent();
        vimPort = connection.getVimPort();
        propCollectorRef = serviceContent.getPropertyCollector();
        perfManager = serviceContent.getPerfManager();
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

    /**
     * @throws Exception
     */
    void populateData() throws DatatypeConfigurationException {
        createMainPanel();
        initChart();
        updateChart();
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
    public void displayUsage() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        stats = countername;
        hostmor = getHostByHostName(hostname);
        if (hostmor == null) {
            System.out.println("Host " + hostname + " Not Found");
            return;
        }
        @SuppressWarnings("unchecked")
        List<PerfInterval> historicalInterval =
                (List<PerfInterval>) getDynamicProperty(perfManager,
                        "historicalInterval");

        intervals = new PerfInterval[historicalInterval.size()];
        for (int i = 0; i < historicalInterval.size(); i++) {
            intervals[i] = historicalInterval.get(i);
        }


        if (intervals.length == 0) {
            System.out.println("No historical intervals");
            return;
        }
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                try {
                    createAndShowGUI();
                } catch (SOAPFaultException sfe) {
                    printSoapFaultException(sfe);
                } catch (Exception ex) {
                    System.out.println("Exception -: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });
    }

    Object getDynamicProperty(ManagedObjectReference mor,
                              String propertyName) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        ObjectContent[] objContent =
                getObjectProperties(mor, new String[]{propertyName});

        Object propertyValue = null;
        if (objContent != null) {
            List<DynamicProperty> listdp = objContent[0].getPropSet();
            if (listdp != null && !listdp.isEmpty()) {
                /*
                * Check the dynamic property for ArrayOfXXX object
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
    private static boolean methodExists(Object obj, String methodName,
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

    /**
     *
     */
    private void initChart() {
        PerfInterval interval = intervals[intervalBox.getSelectedIndex()];
        int period = interval.getSamplingPeriod();
        int tickInterval;
        String format;
        if (period <= 300) {
            tickInterval = 3600 / period;
            format = "{3}:{4}";
        } else if (period <= 3600) {
            tickInterval = 6 * 3600 / period;
            format = "{1}/{2} {3}:{4}";
        } else {
            tickInterval = 24 * 3600 / period;
            format = "{1}/{2}";
        }
        int movingAvg = tickInterval;
        if (chart != null) {
            displayPanel.remove(chart);
        }
        chart =
                new LineChart(tickInterval, period * 1000, format, format,
                        movingAvg, true);
        chart.setPreferredSize(new Dimension(600, 150));
        displayPanel.add(chart);
        if (frame != null) {
            frame.pack();
        }
    }

    @SuppressWarnings("unchecked")
    private void updateChart() throws DatatypeConfigurationException {
        List<PerfCounterInfo> counterInfoList = new ArrayList<PerfCounterInfo>();
        try {
            counterInfoList =
                    (List<PerfCounterInfo>) getDynamicProperty(perfManager,
                            "perfCounter");
        } catch (SOAPFaultException sfe) {
            printSoapFaultException(sfe);
        } catch (Exception x) {
            System.out.println("Error in getting perfCounter property: " + x);
            return;
        }
        if (counterInfoList != null && !counterInfoList.isEmpty()) {
            Map<String, PerfCounterInfo> counterMap =
                    new HashMap<String, PerfCounterInfo>();
            for (PerfCounterInfo counterInfo : counterInfoList) {
                String group = counterInfo.getGroupInfo().getKey();
                String counter = counterInfo.getNameInfo().getKey();
                String rollup = counterInfo.getRollupType().value();
                String key = group + "." + counter + "." + rollup;
                counterMap.put(key, counterInfo);
            }

            List<PerfMetricId> metricIds = new ArrayList<PerfMetricId>();
            String[] statNames = new String[1];
            String key = stats;
            if (counterMap.containsKey(key)) {
                PerfCounterInfo counterInfo = counterMap.get(key);
                statNames[0] = counterInfo.getNameInfo().getLabel();
                String instance = "";
                PerfMetricId pmfids = new PerfMetricId();
                pmfids.setCounterId(counterInfo.getKey());
                pmfids.setInstance(instance);
                metricIds.add(pmfids);

            } else {
                System.out.println("Unknown counter " + key);
                for (PerfCounterInfo counterInfo : counterInfoList) {
                    String group = counterInfo.getGroupInfo().getKey();
                    String counter = counterInfo.getNameInfo().getKey();
                    String rollup = counterInfo.getRollupType().value();
                    System.out.println("Counter " + group + "." + counter + "."
                            + rollup);
                }
                System.out.println("Select The Counter From This list");
                System.exit(1);
            }

            PerfInterval interval = intervals[intervalBox.getSelectedIndex()];
            XMLGregorianCalendar endTime =
                    DatatypeFactory.newInstance().newXMLGregorianCalendar(
                            new GregorianCalendar());
            PerfQuerySpec querySpec = new PerfQuerySpec();
            querySpec.setEntity(hostmor);
            querySpec.setFormat("csv");
            querySpec.setIntervalId(interval.getSamplingPeriod());
            //querySpec.setEndTime(endTime);
            querySpec.getMetricId().addAll(metricIds);
            List<PerfEntityMetricBase> metrics =
                    new ArrayList<PerfEntityMetricBase>();
            try {
                List<PerfQuerySpec> listpqspecs =
                        Arrays.asList(new PerfQuerySpec[]{querySpec});
                List<PerfEntityMetricBase> listpemb =
                        vimPort.queryPerf(perfManager, listpqspecs);
                metrics = listpemb;
            } catch (SOAPFaultException sfe) {
                printSoapFaultException(sfe);
            } catch (Exception x) {
                System.out.println("Error in queryPerf: " + x);
                return;
            }
            if (metrics == null || metrics.size() == 0) {
                System.out.println("queryPerf returned no entity metrics");
                return;
            }
            PerfEntityMetricBase metric = metrics.get(0);
            PerfEntityMetricCSV csvMetric = (PerfEntityMetricCSV) metric;
            List<PerfMetricSeriesCSV> lipmscsv = csvMetric.getValue();
            List<PerfMetricSeriesCSV> csvSeriesList = lipmscsv;
            if (csvSeriesList.size() == 0) {
                System.out.println("queryPerf returned no CSV series");
                return;
            }
            String statName = statNames[0];
            PerfMetricSeriesCSV csvSeries = csvSeriesList.get(0);
            String[] strValues = csvSeries.getValue().split(",");
            int[] values = new int[strValues.length];
            for (int i = 0; i < strValues.length; ++i) {
                values[i] = Integer.parseInt(strValues[i]);
            }
            chart.setValues(values, endTime.getMillisecond());
            displayPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder(statName),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        }
    }

    private void createMainPanel() {
        selectPanel = new JPanel();
        displayPanel = new JPanel();

        chartLabel = new JLabel();
        chartLabel.setHorizontalAlignment(JLabel.CENTER);
        chartLabel.setVerticalAlignment(JLabel.CENTER);
        chartLabel.setVerticalTextPosition(JLabel.CENTER);
        chartLabel.setHorizontalTextPosition(JLabel.CENTER);

        String[] intervalNames = new String[intervals.length];
        for (int i = 0; i < intervals.length; ++i) {
            intervalNames[i] = intervals[i].getName();
        }
        intervalBox = new JComboBox(intervalNames);
        selectPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Interval"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        displayPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Metric Name"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        displayPanel.add(chartLabel);
        selectPanel.add(intervalBox);
        intervalBox.addActionListener(this);
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        mainPanel.add(selectPanel);
        mainPanel.add(displayPanel);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if ("comboBoxChanged".equals(event.getActionCommand())) {
            System.out.println("Updating interval");
            initChart();
            try {
                updateChart();
            } catch (SOAPFaultException sfe) {
                printSoapFaultException(sfe);
            } catch (DatatypeConfigurationException ex) {
                System.out.println("Error encountered: " + ex);
            }
        }
    }

    private void createAndShowGUI() throws DatatypeConfigurationException {
        try {
            String lookAndFeel = UIManager.getSystemLookAndFeelClassName();
            UIManager.setLookAndFeel(lookAndFeel);
            JFrame.setDefaultLookAndFeelDecorated(true);
        } catch (Exception x) {
            x.printStackTrace();
        }
        populateData();
        frame = new JFrame("VIUsage");
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


        frame.setContentPane(mainPanel);
        frame.pack();
        frame.setVisible(true);
    }

    private static void printSoapFaultException(SOAPFaultException sfe) {
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
    public void run() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        init();
        displayUsage();
    }
}
