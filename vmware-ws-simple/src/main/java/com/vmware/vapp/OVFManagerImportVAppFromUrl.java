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
import com.vmware.common.ssl.TrustAll;
import com.vmware.connection.ConnectedVimServiceBase;
import com.vmware.vim25.*;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.soap.SOAPFaultException;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * <pre>
 * This class can be used import or deploy an OVF Appliance from the specified URL.
 *
 * Due to some issue with Jax WS deserialization, "HttpNfcLeaseState" is deserialized as
 * an XML Element and the Value is returned in the ObjectContent as the First Child of Node
 * ObjectContent[0]->ChangeSet->ElementData[0]->val->firstChild so correct value of HttpNfcLeaseState
 * must be extracted from firstChild node
 *
 * <b>Parameters:</b>
 * host      [required] Name of the host system
 * urlpath   [required] OVFFile urlpath
 * vappname  [required] New vApp Name
 * datastore [optional] Name of the datastore to be used
 *
 * <b>Command Line:</b>
 * run.bat com.vmware.vapp.OVFManagerImportFromUrl --url [webserviceurl]
 * --username [username] --password  [password] --host [hostname]
 * --urlpath [OVFFile URL Path] --vappname [New vApp Name]
 * </pre>
 */

@Sample(
        name = "ovf-manager-import-vapp-from-url",
        description = "This class can be used import or deploy an OVF Appliance from the specified URL.\n" +
                "Due to some issue with Jax WS deserialization, \"HttpNfcLeaseState\" is deserialized as\n" +
                "an XML Element and the Value is returned in the ObjectContent as the First Child of Node\n" +
                "ObjectContent[0]->ChangeSet->ElementData[0]->val->firstChild so correct value of HttpNfcLeaseState\n" +
                "must be extracted from firstChild node\n"
)
public class OVFManagerImportVAppFromUrl extends ConnectedVimServiceBase {

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
                    } catch (SOAPFaultException sfe) {
                        printSoapFaultException(sfe);
                    }
                }
            } catch (SOAPFaultException sfe) {
                printSoapFaultException(sfe);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("rawtypes")
    Map headers = new HashMap();
    String cookieValue = "";

    /*
    *Connection input parameters
    */
    String datastore = null;

    String host = null;
    String urlPath = null;
    String vappName = null;

    @Option(name = "host", description = "Name of the host system")
    public void setHost(String host) {
        this.host = host;
    }

    @Option(name = "urlpath", description = "OVFFile urlpath")
    public void setUrlPath(String urlPath) {
        this.urlPath = urlPath;
    }

    @Option(name = "vappname", description = "New vApp Name")
    public void setVappName(String vappName) {
        this.vappName = vappName;
    }

    @Option(name = "datastore", required = false, description = "Name of the datastore to be used ")
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

            String ovfDescriptor = getOvfDescriptorFromUrl(urlPath);
            if (ovfDescriptor == null || ovfDescriptor.isEmpty()) {
                return;
            }

            OvfCreateImportSpecResult ovfImportResult =
                    vimPort.createImportSpec(serviceContent.getOvfManager(),
                            ovfDescriptor, rpMor, dsMor, importSpecParams);
            if (ovfImportResult.getError() == null
                    || ovfImportResult.getError().isEmpty()) {
                List<OvfFileItem> fileItemArr = ovfImportResult.getFileItem();
                if (fileItemArr != null) {
                    for (OvfFileItem fi : fileItemArr) {
                        printOvfFileItem(fi);
                        TOTAL_BYTES += fi.getSize();
                    }
                }
                System.out.println("Total bytes: " + TOTAL_BYTES);
                if (ovfImportResult != null) {
                    ManagedObjectReference httpNfcLease =
                            vimPort.importVApp(rpMor, ovfImportResult.getImportSpec(),
                                    vmFolder, hostMor);
                    Object[] result =
                            waitForValues.wait(httpNfcLease, new String[]{"state"},
                                    new String[]{"state"},
                                    new Object[][]{new Object[]{
                                            HttpNfcLeaseState.READY,
                                            HttpNfcLeaseState.ERROR}});
                    if (result[0].equals(HttpNfcLeaseState.READY)) {
                        System.out.println("HttpNfcLeaseState: " + result[0]);
                        HttpNfcLeaseInfo httpNfcLeaseInfo =
                                (HttpNfcLeaseInfo) getMOREFs.entityProps(httpNfcLease,
                                        new String[]{"info"}).get("info");
                        printHttpNfcLeaseInfo(httpNfcLeaseInfo);
                        leaseExtender =
                                new OVFManagerImportVAppFromUrl().new HttpNfcLeaseExtender(
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
                                            urlPath.substring(0, urlPath.lastIndexOf("/"));
                                    absoluteFile =
                                            absoluteFile + "/" + ovfFileItem.getPath();
                                    System.out.println("Absolute path: " + absoluteFile);

                                    getVMDKFile(ovfFileItem.isCreate(), absoluteFile,
                                            deviceUrl.getUrl().replace("*", host),
                                            ovfFileItem.getSize());
                                    System.out
                                            .println("Completed uploading the VMDK file");
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
                }
            } else {
                System.out.println("Cannot import the vAPP because of following:");
                for (LocalizedMethodFault fault : ovfImportResult.getError()) {
                    System.out.println(fault.getLocalizedMessage());
                }
            }
        } catch (SOAPFaultException sfe) {
            printSoapFaultException(sfe);
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

    String getOvfDescriptorFromUrl(String ovfDescriptorUrl) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        StringBuffer strContent = new StringBuffer("");
        int x;
        HttpURLConnection connn = getHTTPConnection(ovfDescriptorUrl);
        InputStream fis = connn.getInputStream();
        while ((x = fis.read()) != -1) {
            strContent.append((char) x);
        }
        return strContent + "";
    }

    HttpURLConnection getHTTPConnection(String urlString) throws NoSuchAlgorithmException, KeyManagementException, IOException {
        TrustAll.trust();
        URL url = new URL(urlString);
        HttpURLConnection httpConnection =
                (HttpURLConnection) url.openConnection();
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

        httpConnection.setDoInput(true);
        httpConnection.setDoOutput(true);
        httpConnection.setAllowUserInteraction(true);
        httpConnection.setRequestProperty("Cookie", cookie);
        httpConnection.connect();
        return httpConnection;
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

    void getVMDKFile(boolean put, String readFileLocation,
                     String writeFileLocation, long diskCapacity) {
        HttpsURLConnection writeConnection = null;
        BufferedOutputStream writeBufferedOutputStream = null;

        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 64 * 1024;

        try {
            System.out.println("Destination host URL: " + writeFileLocation);
            HostnameVerifier hv = new HostnameVerifier() {
                @Override
                public boolean verify(String urlHostName, SSLSession session) {
                    System.out.println("Warning: URL Host: " + urlHostName + " vs. "
                            + session.getPeerHost());
                    return true;
                }
            };
            HttpsURLConnection.setDefaultHostnameVerifier(hv);
            URL url = new URL(writeFileLocation);
            writeConnection = (HttpsURLConnection) url.openConnection();

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

            writeConnection.setDoInput(true);
            writeConnection.setDoOutput(true);
            writeConnection.setUseCaches(false);
            writeConnection.setChunkedStreamingMode(maxBufferSize);
            if (put) {
                writeConnection.setRequestMethod("PUT");
                System.out.println("HTTP method: PUT");
            } else {
                writeConnection.setRequestMethod("POST");
                System.out.println("HTTP method: POST");
            }
            writeConnection.setRequestProperty("Cookie", cookie);
            writeConnection.setRequestProperty("Connection", "Keep-Alive");
            writeConnection.setRequestProperty("Content-Type",
                    "application/x-vnd.vmware-streamVmdk");
            writeConnection.setRequestProperty("Content-Length",
                    String.valueOf(diskCapacity));

            writeBufferedOutputStream =
                    new BufferedOutputStream(writeConnection.getOutputStream());
            System.out.println("Local file path: " + readFileLocation);
            HttpURLConnection readConnection = getHTTPConnection(readFileLocation);
            InputStream readInputStream = readConnection.getInputStream();
            BufferedInputStream readBufferedInputStream =
                    new BufferedInputStream(readInputStream);
            bytesAvailable = readBufferedInputStream.available();
            System.out.println("vmdk available bytes: " + bytesAvailable);
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            bytesRead = readBufferedInputStream.read(buffer, 0, bufferSize);
            long bytesWrote = bytesRead;
            TOTAL_BYTES_WRITTEN += bytesRead;
            while (bytesRead >= 0) {
                writeBufferedOutputStream.write(buffer, 0, bufferSize);
                writeBufferedOutputStream.flush();
                System.out.println("Bytes Wrote: " + bytesWrote);
                bytesAvailable = readBufferedInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesWrote += bufferSize;
                System.out.println("Total bytes written: " + TOTAL_BYTES_WRITTEN);
                TOTAL_BYTES_WRITTEN += bufferSize;
                buffer = null;
                buffer = new byte[bufferSize];
                bytesRead = readBufferedInputStream.read(buffer, 0, bufferSize);
                System.out.println("Bytes Read: " + bytesRead);
                if ((bytesRead == 0) && (bytesWrote >= diskCapacity)) {
                    System.out
                            .println("Total bytes written: " + TOTAL_BYTES_WRITTEN);
                    bytesRead = -1;
                }
            }
            try {
                DataInputStream dis =
                        new DataInputStream(writeConnection.getInputStream());
                dis.close();
            } catch (SocketTimeoutException stex) {
                System.out.println("From (ServerResponse): " + stex);
            } catch (IOException ioex) {
                System.out.println("From (ServerResponse): " + ioex);
            }
            System.out.println("Writing vmdk to the output stream done");
            readBufferedInputStream.close();
        } catch (MalformedURLException ex) {
            throw new CheckedExceptionWrapper(ex);
        } catch (IOException ioe) {
            throw new CheckedExceptionWrapper(ioe);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("this is supposed to be impossible", e);
        } catch (KeyManagementException e) {
            throw new CheckedExceptionWrapper(e);
        } finally {
            try {
                writeBufferedOutputStream.flush();
                writeBufferedOutputStream.close();
                writeConnection.disconnect();
            } catch (SOAPFaultException sfe) {
                printSoapFaultException(sfe);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
    public void run() {
        serviceContent.getPropertyCollector();
        importVApp();
    }

    private class CheckedExceptionWrapper extends RuntimeException {
		private static final long serialVersionUID = 1L;
        public CheckedExceptionWrapper(Throwable cause) {
            super(cause);
        }
    }
}
