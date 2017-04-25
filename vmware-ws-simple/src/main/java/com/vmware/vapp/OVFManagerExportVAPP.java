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

package com.vmware.vapp;

import com.vmware.common.annotations.Action;
import com.vmware.common.annotations.Option;
import com.vmware.common.annotations.Sample;
import com.vmware.connection.ConnectedVimServiceBase;
import com.vmware.vim25.*;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;
import java.io.*;
import java.net.URL;
import java.util.*;

/**
 * <pre>
 * OVFManagerExportVAPP
 *
 * This sample demonstrates OVFManager.
 * Exports VMDK's and OVF Descriptor of all VM's in the vApps
 *
 * <b>Parameters:</b>
 * username         [required]: username for the authentication
 * password         [required]: password for the authentication
 * host             [required]: Name of the host system
 * vapp             [required]: Name of the vapp
 * localpath        [required]: local System Folder path
 *
 * <b>Command Line:</b>
 * run.bat com.vmware.httpfileaccess.OVFManagerExportVAPP
 * --url [URLString] --username [username] --password [password]
 * --host [Host name] --vapp [Vapp Name] --localpath [Local Path]
 * </pre>
 */
@Sample(
        name = "ovf-manager-export-vapp",
        description = "This sample demonstrates OVFManager.\n" +
                "Exports VMDK's and OVF Descriptor of all VM's in the vApps"
)
public class OVFManagerExportVAPP extends ConnectedVimServiceBase {
    private ManagedObjectReference propCollectorRef;

    private volatile long TOTAL_BYTES = 0;
    private volatile long TOTAL_BYTES_WRITTEN = 0;
    private volatile HttpNfcLeaseExtender leaseExtender;
    private volatile boolean vmdkFlag;
    private String cookieValue;
    private Map<String, List<String>> headers = new HashMap<String, List<String>>();

    String host;

    String vApp = null;
    String localpath = null;

    @Option(name = "vapp", description = "Name of the vapp")
    public void setvApp(String vApp) {
        this.vApp = vApp;
    }

    @Option(name = "localpath", description = "local System Folder path")
    public void setLocalpath(String localpath) {
        this.localpath = localpath;
    }

    /**
     * The Class HttpNfcLeaseExtender.
     */
    private class HttpNfcLeaseExtender implements Runnable {
        private ManagedObjectReference httpNfcLease = null;

        public HttpNfcLeaseExtender(ManagedObjectReference mor,
                                    VimPortType VIM_PORT) {
            httpNfcLease = mor;
            vimPort = VIM_PORT;
        }

        @Override
        public void run() {
            try {
                System.out.println("---------------------- Thread for "
                        + "Checking the HTTP NFCLEASE vmdkFlag: " + vmdkFlag
                        + "----------------------");
                while (!vmdkFlag) {
                    System.out.println("#### TOTAL_BYTES_WRITTEN "
                            + TOTAL_BYTES_WRITTEN);
                    System.out.println("#### TOTAL_BYTES " + TOTAL_BYTES);
                    try {
                        vimPort.httpNfcLeaseProgress(httpNfcLease, 0);
                        Thread.sleep(290000000);
                    } catch (InterruptedException e) {
                        System.out
                                .println("---------------------- Thread interrupted "
                                        + "----------------------");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    TraversalSpec getVappTraversalSpec() {
        TraversalSpec dataCenterToVMFolder = new TraversalSpec();
        dataCenterToVMFolder.setName("DataCenterToVMFolder");
        dataCenterToVMFolder.setType("Datacenter");
        dataCenterToVMFolder.setPath("vmFolder");
        dataCenterToVMFolder.setSkip(false);
        SelectionSpec sSpec = new SelectionSpec();
        sSpec.setName("VisitFolders");
        List<SelectionSpec> sSpecs = new ArrayList<SelectionSpec>();
        sSpecs.add(sSpec);
        dataCenterToVMFolder.getSelectSet().addAll(sSpecs);

        TraversalSpec traversalSpec = new TraversalSpec();
        traversalSpec.setName("VisitFolders");
        traversalSpec.setType("Folder");
        traversalSpec.setPath("childEntity");
        traversalSpec.setSkip(false);
        List<SelectionSpec> sSpecArr = new ArrayList<SelectionSpec>();
        sSpecArr.add(sSpec);
        sSpecArr.add(dataCenterToVMFolder);
        traversalSpec.getSelectSet().addAll(sSpecArr);
        return traversalSpec;
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retVal;
    }

    ManagedObjectReference getVAPPByName(String vmName) {
        ManagedObjectReference retVal = null;
        try {
            TraversalSpec tSpec = getVappTraversalSpec();
            PropertySpec propertySpec = new PropertySpec();
            propertySpec.setAll(Boolean.FALSE);
            propertySpec.getPathSet().add(new String("name"));
            propertySpec.setType("VirtualApp");
            List<PropertySpec> propertySpecs = new ArrayList<PropertySpec>();
            propertySpecs.add(propertySpec);

            ObjectSpec objectSpec = new ObjectSpec();
            objectSpec.setObj(serviceContent.getRootFolder());
            objectSpec.setSkip(Boolean.TRUE);
            objectSpec.getSelectSet().add(tSpec);

            List<ObjectSpec> objectSpecs = new ArrayList<ObjectSpec>();
            objectSpecs.add(objectSpec);

            PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
            propertyFilterSpec.getPropSet().addAll(propertySpecs);
            propertyFilterSpec.getObjectSet().addAll(objectSpecs);

            List<PropertyFilterSpec> propertyFilterSpecs =
                    new ArrayList<PropertyFilterSpec>();
            propertyFilterSpecs.add(propertyFilterSpec);

            List<ObjectContent> oCont =
                    vimPort
                            .retrievePropertiesEx(serviceContent.getPropertyCollector(),
                                    propertyFilterSpecs, null).getObjects();
            if (oCont != null) {
                boolean flag = false;
                for (ObjectContent oc : oCont) {
                    ManagedObjectReference mr = oc.getObj();
                    List<DynamicProperty> dps = oc.getPropSet();
                    if (dps != null) {
                        for (DynamicProperty dp : dps) {
                            if (dp.getName().equalsIgnoreCase("name")) {
                                String vmnm = (String) dp.getVal();
                                if (vmnm.equalsIgnoreCase(vmName)) {
                                    retVal = mr;
                                    flag = true;
                                    break;
                                }
                            }
                        }
                    }
                    if (flag) {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retVal;
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

    void printHttpNfcLeaseInfo(HttpNfcLeaseInfo info,
                               String hostString) {
        System.out
                .println("########################################################");
        System.out.println("HttpNfcLeaseInfo");
        System.out.println("Lease Timeout: " + info.getLeaseTimeout());
        System.out.println("Total Disk capacity: "
                + info.getTotalDiskCapacityInKB());
        List<HttpNfcLeaseDeviceUrl> deviceUrlArr = info.getDeviceUrl();
        if (deviceUrlArr != null) {
            int deviceUrlCount = 1;
            for (HttpNfcLeaseDeviceUrl durl : deviceUrlArr) {
                System.out.println("HttpNfcLeaseDeviceUrl : " + deviceUrlCount++);
                System.out.println("   Device URL Import Key: "
                        + durl.getImportKey());
                System.out.println("   Device URL Key: " + durl.getKey());
                System.out.println("   Device URL : " + durl.getUrl());
                System.out.println("   Updated device URL: "
                        + durl.getUrl().replace("*", hostString));
                System.out
                        .println("   SSL Thumbprint : " + durl.getSslThumbprint());
            }
        } else {
            System.out.println("No Device URLS Found");
            System.out
                    .println("########################################################");
        }
    }

    long writeVMDKFile(String absoluteFile, String urlString) throws IOException {
        URL urlCon = new URL(urlString);
        HttpsURLConnection conn = (HttpsURLConnection) urlCon.openConnection();

        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setAllowUserInteraction(true);

        // Maintain session
        List<String> cookies = headers.get("Set-cookie");
        cookieValue = cookies.get(0);
        StringTokenizer tokenizer = new StringTokenizer(cookieValue, ";");
        cookieValue = tokenizer.nextToken();
        String path = "$" + tokenizer.nextToken();
        String cookie = "$Version=\"1\"; " + cookieValue + "; " + path;

        // set the cookie in the new request header
        Map<String, List<String>> map = new HashMap<String, List<String>>();
        map.put("Cookie", Collections.singletonList(cookie));
        ((BindingProvider) vimPort).getRequestContext().put(
                MessageContext.HTTP_REQUEST_HEADERS, map);

        conn.setRequestProperty("Cookie", cookie);
        conn.setRequestProperty("Content-Type", "application/octet-stream");
        conn.setRequestProperty("Expect", "100-continue");
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-Length", "1024");

        InputStream in = conn.getInputStream();
        String _localpath = localpath + "/" + absoluteFile;
        OutputStream out = new FileOutputStream(new File(_localpath));
        byte[] buf = new byte[102400];
        int len = 0;
        long written = 0;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
            written = written + len;
        }
        System.out.println("   Exported File " + absoluteFile + " : " + written);
        in.close();
        out.close();
        return written;
    }

    void exportVApp() throws RuntimeFaultFaultMsg, TaskInProgressFaultMsg, FileFaultFaultMsg, InvalidStateFaultMsg, InvalidPowerStateFaultMsg, InvalidPropertyFaultMsg, InvalidCollectorVersionFaultMsg, IOException, TimedoutFaultMsg, VmConfigFaultFaultMsg, ConcurrentAccessFaultMsg {
        File file = new File(localpath);
        if (!file.exists()) {
            System.out.println("Wrong or invalid path " + localpath);
            return;
        }
        ManagedObjectReference hostRef = getHostByHostName(host);
        if (hostRef == null) {
            System.out.println("Host Not Found");
        } else {
            ManagedObjectReference vAppMoRef = getVAPPByName(vApp);
            if (vAppMoRef != null) {
                OvfCreateDescriptorParams ovfCreateDescriptorParams =
                        new OvfCreateDescriptorParams();
                ManagedObjectReference httpNfcLease =
                        vimPort.exportVApp(vAppMoRef);
                System.out.println("Getting the HTTP NFCLEASE for the vApp: "
                        + vApp);

                Object[] result =
                        waitForValues.wait(httpNfcLease, new String[]{"state"},
                                new String[]{"state"},
                                new Object[][]{new Object[]{
                                        HttpNfcLeaseState.READY,
                                        HttpNfcLeaseState.ERROR}});
                if (result[0].equals(HttpNfcLeaseState.READY)) {

                    HttpNfcLeaseInfo httpNfcLeaseInfo =
                            (HttpNfcLeaseInfo) getMOREFs.entityProps(httpNfcLease,
                                    new String[]{"info"}).get("info");

                    httpNfcLeaseInfo.setLeaseTimeout(300000000);
                    printHttpNfcLeaseInfo(httpNfcLeaseInfo, host);
                    long diskCapacity =
                            (httpNfcLeaseInfo.getTotalDiskCapacityInKB()) * 1024;
                    System.out.println("************ " + diskCapacity);

                    TOTAL_BYTES = diskCapacity;
                    leaseExtender =
                            new OVFManagerExportVAPP().new HttpNfcLeaseExtender(
                                    httpNfcLease, vimPort);
                    Thread t = new Thread(leaseExtender);
                    t.start();

                    List<HttpNfcLeaseDeviceUrl> deviceUrlArr =
                            httpNfcLeaseInfo.getDeviceUrl();
                    if (deviceUrlArr != null) {
                        List<OvfFile> ovfFiles = new ArrayList<OvfFile>();
                        for (int i = 0; i < deviceUrlArr.size(); i++) {
                            System.out.println("Downloading Files:");
                            String deviceId = deviceUrlArr.get(i).getKey();
                            String deviceUrlStr = deviceUrlArr.get(i).getUrl();
                            String absoluteFile =
                                    deviceUrlStr.substring(deviceUrlStr
                                            .lastIndexOf("/") + 1);
                            System.out.println("   Absolute File Name: "
                                    + absoluteFile);
                            System.out.println("   VMDK URL: "
                                    + deviceUrlStr.replace("*", host));
                            long writtenSize =
                                    writeVMDKFile(absoluteFile,
                                            deviceUrlStr.replace("*", host));
                            OvfFile ovfFile = new OvfFile();
                            ovfFile.setPath(absoluteFile);
                            ovfFile.setDeviceId(deviceId);
                            ovfFile.setSize(writtenSize);
                            ovfFiles.add(ovfFile);
                        }
                        ovfCreateDescriptorParams.getOvfFiles().addAll(ovfFiles);
                        OvfCreateDescriptorResult ovfCreateDescriptorResult =
                                vimPort.createDescriptor(
                                        serviceContent.getOvfManager(), vAppMoRef,
                                        ovfCreateDescriptorParams);
                        System.out.println();
                        String outOVF = localpath + "/" + vApp + ".ovf";
                        File outFile = new File(outOVF);
                        FileWriter out = new FileWriter(outFile);
                        out.write(ovfCreateDescriptorResult.getOvfDescriptor());
                        out.close();
                        System.out.println("OVF Desriptor Written to file " + vApp
                                + ".ovf");
                        System.out.println("DONE");
                        if (!ovfCreateDescriptorResult.getError().isEmpty()) {
                            System.out.println("SOME ERRORS");
                        }
                        if (!ovfCreateDescriptorResult.getWarning().isEmpty()) {
                            System.out.println("SOME WARNINGS");
                        }
                    } else {
                        System.out.println("No Device URLS");
                    }
                    System.out.println("Completed Downloading the files");
                    vmdkFlag = true;
                    t.interrupt();
                    vimPort.httpNfcLeaseProgress(httpNfcLease, 100);
                    vimPort.httpNfcLeaseComplete(httpNfcLease);
                } else {
                    System.out.println("HttpNfcLeaseState not ready");
                    System.out.println("HttpNfcLeaseState: " + result);
                }
            } else {
                System.out.println("vApp Not Found");
            }
        }
    }

    @Action
    public void run() throws RuntimeFaultFaultMsg, TaskInProgressFaultMsg, VmConfigFaultFaultMsg, IOException, InvalidPropertyFaultMsg, FileFaultFaultMsg, ConcurrentAccessFaultMsg, InvalidStateFaultMsg, InvalidCollectorVersionFaultMsg, InvalidPowerStateFaultMsg, TimedoutFaultMsg {
        host = connection.getHost();
        propCollectorRef = serviceContent.getPropertyCollector();
        exportVApp();
    }

}
