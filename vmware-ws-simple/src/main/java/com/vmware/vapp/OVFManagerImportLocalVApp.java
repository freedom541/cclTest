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

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;
import java.io.*;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.*;

/**
 * <pre>
 * OVFManagerImportLocalVApp
 *
 * This class can be used import or deploy an OVF Appliance from the Local drive.
 *
 * Due to some issue with JAX-WS deserialization, "HttpNfcLeaseState" is deserialized as
 * an XML Element and the Value is returned in the ObjectContent as the First Child of Node
 * ObjectContent[0]->ChangeSet->ElementData[0]->val->firstChild so correct value of HttpNfcLeaseState
 * must be extracted from firstChild node
 *
 * <b>Parameters:</b>
 * host      [required] Name of the host system
 * localpath [required] OVFFile LocalPath
 * vappname  [required] New vApp Name
 * datastore [optional] Name of the datastore to be used
 *
 * <b>Command Line:</b>
 * run.bat com.vmware.vapp.OVFManagerImportLocalVApp --url [webserviceurl]
 * --username [username] --password  [password] --host [hostname]
 * --localpath [OVFFile LocalPath] --vappname [New vApp Name]
 * </pre>
 */
@Sample(
        name = "ovf-manager-import-local-vapp",
        description =
                "This class can be used import or deploy an OVF Appliance from the Local drive.\n" +
                        "Due to some issue with JAX-WS deserialization, \"HttpNfcLeaseState\" is deserialized as\n" +
                        "an XML Element and the Value is returned in the ObjectContent as the First Child of Node\n" +
                        "ObjectContent[0]->ChangeSet->ElementData[0]->val->firstChild so correct value of HttpNfcLeaseState\n" +
                        " must be extracted from firstChild node\n"
)
public class OVFManagerImportLocalVApp extends ConnectedVimServiceBase {

    class HttpNfcLeaseExtender implements Runnable {
        private ManagedObjectReference httpNfcLease = null;
        private VimPortType vimPort = null;
        private int progressPercent = 0;

        public HttpNfcLeaseExtender(ManagedObjectReference mor,
                                    VimPortType vimport) {
            httpNfcLease = mor;
            vimPort = vimport;
        }

        @Override
        public void run() {
            try {
                while (!vmdkFlag) {
                    System.out.println("\n\n#####################vmdk flag: "
                            + vmdkFlag + "\n\n");
                    progressPercent =
                            (int) ((TOTAL_BYTES_WRITTEN * 100) / (TOTAL_BYTES));
                    try {
                        vimPort.httpNfcLeaseProgress(httpNfcLease, progressPercent);
                        Thread.sleep(290000000);
                    } catch (InterruptedException e) {
                        System.out
                                .println("********************** Thread interrupted *******************");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private Map headers = new HashMap();
    private String cookieValue = "";

    String datastore = null;

    /* End Server Connection and common code */

    /* Start Sample functional code */

    String host = null;
    String localPath = null;
    String vappName = null;

    @Option(name = "host", description = "Name of the host system")
    public void setHost(String host) {
        this.host = host;
    }

    @Option(name = "localpath", description = "OVFFile LocalPath")
    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    @Option(name = "vappname", description = "New vApp Name")
    public void setVappName(String vappName) {
        this.vappName = vappName;
    }

    @Option(name = "datastore", required = false, description = "Name of the datastore to be used")
    public void setDatastore(String datastore) {
        this.datastore = datastore;
    }


    boolean vmdkFlag = false;
    volatile long TOTAL_BYTES = 0;
    volatile long TOTAL_BYTES_WRITTEN = 0;
    HttpNfcLeaseExtender leaseExtender;


    /**
     * @return An array of SelectionSpec to navigate from the Datastore and move
     *         upwards to reach the Datacenter
     */
    List<SelectionSpec> buildTraversalSpecForDatastoreToDatacenter() {
        // For Folder -> Folder recursion
        SelectionSpec sspecvfolders = new SelectionSpec();
        sspecvfolders.setName("VisitFolders");

        TraversalSpec visitFolders = new TraversalSpec();
        visitFolders.setType("Folder");
        visitFolders.setPath("parent");
        visitFolders.setSkip(Boolean.FALSE);
        visitFolders.setName("VisitFolders");
        visitFolders.getSelectSet().add(sspecvfolders);

        TraversalSpec datastoreToFolder = new TraversalSpec();
        datastoreToFolder.setType("Datastore");
        datastoreToFolder.setPath("parent");
        datastoreToFolder.setSkip(Boolean.FALSE);
        datastoreToFolder.setName("DatastoreToFolder");
        datastoreToFolder.getSelectSet().add(sspecvfolders);

        List<SelectionSpec> speclist = new ArrayList<SelectionSpec>();
        speclist.add(datastoreToFolder);
        speclist.add(visitFolders);
        return speclist;
    }

    /**
     * Method to retrieve the Datacenter under which the specified datastore
     * resides
     *
     * @param datastore {@link ManagedObjectReference} of the VM
     * @return {@link ManagedObjectReference} of the datacenter that contains the
     *         datastore.
     * @throws InvalidPropertyFaultMsg
     * @throws RuntimeFaultFaultMsg
     */
    ManagedObjectReference getDatacenterOfDatastore(
            ManagedObjectReference datastore) throws InvalidPropertyFaultMsg,
            RuntimeFaultFaultMsg {
        ManagedObjectReference datacenter = null;

        // Create Property Spec
        PropertySpec propertySpec = new PropertySpec();
        propertySpec.setAll(Boolean.FALSE);
        propertySpec.setType("Datacenter");
        propertySpec.getPathSet().add("name");

        // Now create Object Spec
        ObjectSpec objectSpec = new ObjectSpec();
        objectSpec.setObj(datastore);
        objectSpec.setSkip(Boolean.TRUE);
        objectSpec.getSelectSet().addAll(
                buildTraversalSpecForDatastoreToDatacenter());

        // Create PropertyFilterSpec using the PropertySpec and ObjectPec
        // created above.
        PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
        propertyFilterSpec.getPropSet().add(propertySpec);
        propertyFilterSpec.getObjectSet().add(objectSpec);

        List<PropertyFilterSpec> propertyFilterSpecs =
                new ArrayList<PropertyFilterSpec>();
        propertyFilterSpecs.add(propertyFilterSpec);

        List<ObjectContent> oCont =
                vimPort.retrievePropertiesEx(serviceContent.getPropertyCollector(),
                        propertyFilterSpecs, null).getObjects();
        if (oCont != null) {
            for (ObjectContent oc : oCont) {
                datacenter = oc.getObj();
                break;
            }
        }
        return datacenter;
    }

    void importVApp() {
        try {
            ManagedObjectReference dsMor = null;
            ManagedObjectReference rpMor = null;
            ManagedObjectReference hostMor =
                    getMOREFsInContainerByType(serviceContent.getRootFolder(),
                            "HostSystem").get(host);
            if (hostMor == null) {
                throw new RuntimeException("Host System " + host + " Not Found.");
            }


            Map<String, Object> hostProps =
                    getMOREFs.entityProps(hostMor, new String[]{"datastore", "parent"});
            List<ManagedObjectReference> dsList =
                    ((ArrayOfManagedObjectReference) hostProps.get("datastore"))
                            .getManagedObjectReference();
            if (dsList.isEmpty()) {
                throw new RuntimeException("No Datastores accesible from host "
                        + host);
            }
            if (datastore == null) {
                dsMor = dsList.get(0);
            } else {
                for (ManagedObjectReference ds : dsList) {
                    if (datastore.equalsIgnoreCase((String) getMOREFs.entityProps(ds,
                            new String[]{"name"}).get("name"))) {
                        dsMor = ds;
                        break;
                    }
                }
            }
            if (dsMor == null) {
                if (datastore != null) {
                    throw new RuntimeException("No Datastore by name " + datastore
                            + " is accessible from host " + host);
                }
                throw new RuntimeException("No Datastores accesible from host "
                        + host);
            }
            rpMor =
                    (ManagedObjectReference) getMOREFs.entityProps(
                            (ManagedObjectReference) hostProps.get("parent"),
                            new String[]{"resourcePool"}).get("resourcePool");

            ManagedObjectReference dcMor = getDatacenterOfDatastore(dsMor);
            ManagedObjectReference vmFolder =
                    (ManagedObjectReference) getMOREFs.entityProps(dcMor,
                            new String[]{"vmFolder"}).get("vmFolder");

            OvfCreateImportSpecParams importSpecParams =
                    createImportSpecParams(hostMor, vappName);
            String ovfDescriptor = getOvfDescriptorFromLocal(localPath);
            if (ovfDescriptor == null || ovfDescriptor.isEmpty()) {
                return;
            }
            OvfCreateImportSpecResult ovfImportResult =
                    vimPort.createImportSpec(serviceContent.getOvfManager(),
                            ovfDescriptor, rpMor, dsMor, importSpecParams);
            List<OvfFileItem> fileItemArr = ovfImportResult.getFileItem();
            if (fileItemArr != null) {
                for (OvfFileItem fi : fileItemArr) {
                    printOvfFileItem(fi);
                    TOTAL_BYTES += fi.getSize();
                }
            }
            System.out.println("Total bytes: " + TOTAL_BYTES);
            ManagedObjectReference httpNfcLease =
                    vimPort.importVApp(rpMor, ovfImportResult.getImportSpec(),
                            vmFolder, hostMor);
            Object[] result =
                    waitForValues.wait(httpNfcLease, new String[]{"state"},
                            new String[]{"state"}, new Object[][]{new Object[]{
                            HttpNfcLeaseState.READY, HttpNfcLeaseState.ERROR}});
            if (result[0].equals(HttpNfcLeaseState.READY)) {
                System.out.println("HttpNfcLeaseState: " + result[0]);
                HttpNfcLeaseInfo httpNfcLeaseInfo =
                        (HttpNfcLeaseInfo) getMOREFs.entityProps(httpNfcLease,
                                new String[]{"info"}).get("info");
                printHttpNfcLeaseInfo(httpNfcLeaseInfo);
                leaseExtender =
                        new OVFManagerImportLocalVApp().new HttpNfcLeaseExtender(
                                httpNfcLease, vimPort);
                Thread t = new Thread(leaseExtender);
                t.start();
                List<HttpNfcLeaseDeviceUrl> deviceUrlArr =
                        httpNfcLeaseInfo.getDeviceUrl();
                for (HttpNfcLeaseDeviceUrl deviceUrl : deviceUrlArr) {
                    String deviceKey = deviceUrl.getImportKey();
                    for (OvfFileItem ovfFileItem : fileItemArr) {
                        if (deviceKey.equals(ovfFileItem.getDeviceId())) {
                            System.out.println("Import key: " + deviceKey);
                            System.out.println("OvfFileItem device id: "
                                    + ovfFileItem.getDeviceId());
                            System.out.println("HTTP Post file: "
                                    + ovfFileItem.getPath());
                            String absoluteFile =
                                    localPath.substring(0, localPath.lastIndexOf("\\"));
                            absoluteFile = absoluteFile + "/" + ovfFileItem.getPath();
                            System.out.println("Absolute path: " + absoluteFile);
                            getVMDKFile(ovfFileItem.isCreate(), absoluteFile,
                                    deviceUrl.getUrl().replace("*", host),
                                    ovfFileItem.getSize());
                            System.out.println("Completed uploading the VMDK file");
                        }
                    }
                }
                vmdkFlag = true;
                t.interrupt();
                vimPort.httpNfcLeaseProgress(httpNfcLease, 100);
                vimPort.httpNfcLeaseComplete(httpNfcLease);
            } else {
                System.out.println("HttpNfcLeaseState not ready");
                for (Object o : result) {
                    System.out.println("HttpNfcLeaseState: " + o);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    OvfCreateImportSpecParams createImportSpecParams(
            ManagedObjectReference host, String newVmName) {
        OvfCreateImportSpecParams importSpecParams =
                new OvfCreateImportSpecParams();
        importSpecParams.setHostSystem(host);
        importSpecParams.setLocale("");
        importSpecParams.setEntityName(newVmName);
        importSpecParams.setDeploymentOption("");
        return importSpecParams;
    }

    void getVMDKFile(boolean put, String fileName, String uri,
                     long diskCapacity) {
        HttpsURLConnection conn = null;
        BufferedOutputStream bos = null;

        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 64 * 1024;

        try {
            System.out.println("Destination host URL: " + uri);
            HostnameVerifier hv = new HostnameVerifier() {
                @Override
                public boolean verify(String urlHostName, SSLSession session) {
                    System.out.println("Warning: URL Host: " + urlHostName + " vs. "
                            + session.getPeerHost());
                    return true;
                }
            };
            HttpsURLConnection.setDefaultHostnameVerifier(hv);
            URL url = new URL(uri);
            conn = (HttpsURLConnection) url.openConnection();


            // Maintain session
            @SuppressWarnings("unchecked")
            List<String> cookies = (List<String>) headers.get("Set-cookie");
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
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setChunkedStreamingMode(maxBufferSize);
            if (put) {
                conn.setRequestMethod("PUT");
                System.out.println("HTTP method: PUT");
            } else {
                conn.setRequestMethod("POST");
                System.out.println("HTTP method: POST");
            }
            conn.setRequestProperty("Cookie", cookie);
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Content-Type",
                    "application/x-vnd.vmware-streamVmdk");
            conn.setRequestProperty("Content-Length", String.valueOf(diskCapacity));
            conn.setRequestProperty("Expect", "100-continue");
            bos = new BufferedOutputStream(conn.getOutputStream());
            System.out.println("Local file path: " + fileName);
            InputStream io = new FileInputStream(fileName);
            BufferedInputStream bis = new BufferedInputStream(io);
            bytesAvailable = bis.available();
            System.out.println("vmdk available bytes: " + bytesAvailable);
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];
            bytesRead = bis.read(buffer, 0, bufferSize);
            long bytesWrote = bytesRead;
            TOTAL_BYTES_WRITTEN += bytesRead;
            while (bytesRead >= 0) {
                bos.write(buffer, 0, bufferSize);
                bos.flush();
                System.out.println("Bytes Wrote: " + bytesWrote);
                bytesAvailable = bis.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesWrote += bufferSize;
                System.out.println("Total bytes written: " + TOTAL_BYTES_WRITTEN);
                TOTAL_BYTES_WRITTEN += bufferSize;
                buffer = null;
                buffer = new byte[bufferSize];
                bytesRead = bis.read(buffer, 0, bufferSize);
                System.out.println("Bytes Read: " + bytesRead);
                if ((bytesRead == 0) && (bytesWrote >= diskCapacity)) {
                    System.out
                            .println("Total bytes written: " + TOTAL_BYTES_WRITTEN);
                    bytesRead = -1;
                }
            }
            try {
                DataInputStream dis = new DataInputStream(conn.getInputStream());
                dis.close();
            } catch (SocketTimeoutException stex) {
                System.out.println("From (ServerResponse): " + stex);
            } catch (IOException ioex) {
                System.out.println("From (ServerResponse): " + ioex);
            }
            System.out.println("Writing vmdk to the output stream done");
            bis.close();
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                bos.flush();
                bos.close();
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    String getOvfDescriptorFromLocal(String ovfDescriptorUrl)
            throws IOException {
        StringBuffer strContent = new StringBuffer("");
        int x;
        try {
            InputStream fis = new FileInputStream(ovfDescriptorUrl);
            if (fis != null) {
                while ((x = fis.read()) != -1) {
                    strContent.append((char) x);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Invalid local file path");
        }

        return strContent + "";
    }

    void printOvfFileItem(OvfFileItem fi) {
        System.out
                .println("##########################################################");
        System.out.println("OvfFileItem");
        System.out.println("chunkSize: " + fi.getChunkSize());
        System.out.println("create: " + fi.isCreate());
        System.out.println("deviceId: " + fi.getDeviceId());
        System.out.println("path: " + fi.getPath());
        System.out.println("size: " + fi.getSize());
        System.out
                .println("##########################################################");
    }

    void printHttpNfcLeaseInfo(HttpNfcLeaseInfo info) {
        System.out
                .println("########################################################");
        System.out.println("HttpNfcLeaseInfo");
        // System.out.println("cookie: " + info.getCookie());
        List<HttpNfcLeaseDeviceUrl> deviceUrlArr = info.getDeviceUrl();
        for (HttpNfcLeaseDeviceUrl durl : deviceUrlArr) {
            System.out.println("Device URL Import Key: " + durl.getImportKey());
            System.out.println("Device URL Key: " + durl.getKey());
            System.out.println("Device URL : " + durl.getUrl());
            System.out.println("Updated device URL: "
                    + durl.getUrl().replace("*", "10.20.140.58"));
        }
        System.out.println("Lease Timeout: " + info.getLeaseTimeout());
        System.out.println("Total Disk capacity: "
                + info.getTotalDiskCapacityInKB());
        System.out
                .println("########################################################");
    }

    @Action
    public void run() {
        importVApp();
    }
}