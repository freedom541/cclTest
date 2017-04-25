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

package com.vmware.httpfileaccess;

import com.vmware.common.annotations.Action;
import com.vmware.common.annotations.Option;
import com.vmware.common.annotations.Sample;
import com.vmware.connection.ConnectedVimServiceBase;
import com.vmware.vim25.*;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <pre>
 * GetVMFiles
 *
 * This sample gets all the config files, snapshots files,
 * logs files, virtual disk files to the local system.
 * Use with PutVMFiles.
 *
 * <b>Parameters:</b>
 * url          [required] : url of the web service.
 * username     [required] : username for the authentication
 * password     [required] : password for the authentication
 * vmname       [required] : Name of the virtual machine
 * localpath    [required] : localpath to copy files into
 *
 * <b>Command Line:</b>
 * To get the virtual machine files on local disk
 * run.bat com.vmware.httpfileaccess.GetVMFiles
 * --url [webserviceurl] --username [username] --password [password]
 * --vmname [vmname] --localpath [localpath]
 * </pre>
 */
@Sample(
        name = "get-vm-files",
        description = "gets all the config files, snapshots files, " +
                "logs files, virtual disk files to the local system."
)
public class GetVMFiles extends ConnectedVimServiceBase {
    Map<String, String> downloadedDir =
            new HashMap<String, String>();

    String cookieValue = "";

    String vmName = null;
    String localPath = null;

    @Option(name = "vmname", description = "Name of the virtual machine")
    public void setVmName(String vmName) {
        this.vmName = vmName;
    }

    @Option(name = "localpath", description = "localpath to copy files into")
    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }


    /**
     * @return An array of SelectionSpec to navigate from the VM and move upwards
     *         to reach the Datacenter
     */
    List<SelectionSpec> buildTraversalSpecForVMToDatacenter() {

        // For Folder -> Folder recursion
        SelectionSpec sspecvfolders = new SelectionSpec();
        sspecvfolders.setName("VisitFolders");

        TraversalSpec visitFolders = new TraversalSpec();
        visitFolders.setType("Folder");
        visitFolders.setPath("parent");
        visitFolders.setSkip(Boolean.FALSE);
        visitFolders.setName("VisitFolders");
        visitFolders.getSelectSet().add(sspecvfolders);

        // For vApp -> vApp recursion
        SelectionSpec sspecvApp = new SelectionSpec();
        sspecvApp.setName("vAppToVApp");

        SelectionSpec sspecvAppToFolder = new SelectionSpec();
        sspecvAppToFolder.setName("vAppToFolder");

        TraversalSpec vAppToFolder = new TraversalSpec();
        vAppToFolder.setType("VirtualApp");
        vAppToFolder.setPath("parentFolder");
        vAppToFolder.setSkip(Boolean.FALSE);
        vAppToFolder.setName("vAppToFolder");
        vAppToFolder.getSelectSet().add(sspecvfolders);

        TraversalSpec vAppToVApp = new TraversalSpec();
        vAppToVApp.setType("VirtualApp");
        vAppToVApp.setPath("parentVApp");
        vAppToVApp.setSkip(Boolean.FALSE);
        vAppToVApp.setName("vAppToVApp");
        vAppToVApp.getSelectSet().add(sspecvApp);
        vAppToVApp.getSelectSet().add(sspecvAppToFolder);

        TraversalSpec vmTovApp = new TraversalSpec();
        vmTovApp.setType("VirtualMachine");
        vmTovApp.setPath("parentVApp");
        vmTovApp.setSkip(Boolean.FALSE);
        vmTovApp.setName("vmTovApp");
        vmTovApp.getSelectSet().add(vAppToVApp);
        vmTovApp.getSelectSet().add(vAppToFolder);

        TraversalSpec vmToFolder = new TraversalSpec();
        vmToFolder.setType("VirtualMachine");
        vmToFolder.setPath("parent");
        vmToFolder.setSkip(Boolean.FALSE);
        vmToFolder.setName("vmToFolder");
        vmToFolder.getSelectSet().add(sspecvfolders);

        List<SelectionSpec> speclist = new ArrayList<SelectionSpec>();
        speclist.add(vmToFolder);
        speclist.add(vmTovApp);
        speclist.add(visitFolders);
        return speclist;
    }

    /**
     * Method to retrieve the Datacenter under which the specified VM resides
     *
     * @param vmMor {@link ManagedObjectReference} of the VM
     * @return {@link String} name of the datacenter that contains the VM.
     * @throws InvalidPropertyFaultMsg
     * @throws RuntimeFaultFaultMsg
     */
    String getDatacenterOfVM(ManagedObjectReference vmMor)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
        String datacenterName = "";

        // Create Property Spec
        PropertySpec propertySpec = new PropertySpec();
        propertySpec.setAll(Boolean.FALSE);
        propertySpec.setType("Datacenter");
        propertySpec.getPathSet().add("name");

        // Now create Object Spec
        ObjectSpec objectSpec = new ObjectSpec();
        objectSpec.setObj(vmMor);
        objectSpec.setSkip(Boolean.TRUE);
        objectSpec.getSelectSet().addAll(buildTraversalSpecForVMToDatacenter());

        // Create PropertyFilterSpec using the PropertySpec and ObjectPec
        // created above.
        PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
        propertyFilterSpec.getPropSet().add(propertySpec);
        propertyFilterSpec.getObjectSet().add(objectSpec);

        List<PropertyFilterSpec> propertyFilterSpecs =
                new ArrayList<PropertyFilterSpec>();
        propertyFilterSpecs.add(propertyFilterSpec);

        RetrieveOptions options = new RetrieveOptions();

        RetrieveResult results = vimPort.retrievePropertiesEx(serviceContent.getPropertyCollector(),
                propertyFilterSpecs, options);

        List<ObjectContent> oCont = results.getObjects();

        if (oCont != null) {
            for (ObjectContent oc : oCont) {
                List<DynamicProperty> dps = oc.getPropSet();
                if (dps != null) {
                    for (DynamicProperty dp : dps) {
                        datacenterName = (String) dp.getVal();
                    }
                }
                System.out.println("VM is present under " + datacenterName
                        + " Datacenter");
                break;
            }
        }
        return datacenterName;
    }

    void getVM() throws IllegalArgumentException, RuntimeFaultFaultMsg, InvalidPropertyFaultMsg, IOException {
        File file = new File(localPath);
        if (!file.exists()) {
            System.out.println("Wrong or invalid path " + localPath);
            return;
        }
        ManagedObjectReference vmRef =
                getMOREFs.inContainerByType(serviceContent.getRootFolder(),
                        "VirtualMachine").get(vmName);
        if (vmRef != null) {
            System.out.println("vmRef: " + vmRef.getValue());
            getDiskSizeInKB(vmRef);
            String dataCenterName = getDatacenterOfVM(vmRef);
            String[] vmDirectory = getVmDirectory(vmRef);
            if (vmDirectory[0] != null) {
                System.out.println("vmDirectory-0: " + vmDirectory[0]
                        + " datacenter as : " + dataCenterName);
                System.out
                        .println("Downloading Virtual Machine Configuration Directory");
                String dataStoreName =
                        vmDirectory[0].substring(vmDirectory[0].indexOf("[") + 1,
                                vmDirectory[0].lastIndexOf("]"));
                String configurationDir =
                        vmDirectory[0].substring(vmDirectory[0].indexOf("]") + 2,
                                vmDirectory[0].lastIndexOf("/"));
                boolean success =
                        new File(localPath + "/" + configurationDir + "#vm#"
                                + dataStoreName).mkdir();
                if (!success) {
                    System.out.println("Could not create " + localPath + "/"
                            + configurationDir + "#vm#" + dataStoreName + "directory");
                }
                downloadDirectory(configurationDir, configurationDir + "#vm#"
                        + dataStoreName, dataStoreName, dataCenterName);
                downloadedDir.put(configurationDir + "#vm#" + dataStoreName,
                        "Directory");
                System.out.println("Downloading Virtual Machine"
                        + " Configuration Directory Complete");
            }

            if (vmDirectory[1] != null) {
                System.out.println("Downloading Virtual Machine "
                        + "Snapshot / Suspend / Log Directory");
                for (int i = 1; i < vmDirectory.length; i++) {
                    String dataStoreName =
                            vmDirectory[i].substring(vmDirectory[i].indexOf("[") + 1,
                                    vmDirectory[i].lastIndexOf("]"));
                    String configurationDir = "";
                    String apiType = serviceContent.getAbout().getApiType();
                    if (apiType.equalsIgnoreCase("VirtualCenter")) {
                        configurationDir =
                                vmDirectory[i].substring(
                                        vmDirectory[i].indexOf("]") + 2,
                                        vmDirectory[i].length() - 1);
                    } else {
                        configurationDir =
                                vmDirectory[i]
                                        .substring(vmDirectory[i].indexOf("]") + 2);
                    }
                    if (!downloadedDir.containsKey(configurationDir + "#vm#"
                            + dataStoreName)) {
                        boolean success =
                                new File(localPath + "/" + configurationDir + "#vm#"
                                        + dataStoreName).mkdir();
                        if (!success) {
                            System.out.println("Could not create " + localPath + "/"
                                    + configurationDir + "#vm#" + dataStoreName
                                    + "directory");
                        }
                        downloadDirectory(configurationDir, configurationDir + "#vm#"
                                + dataStoreName, dataStoreName, dataCenterName);
                        downloadedDir.put(configurationDir + "#vm#" + dataStoreName,
                                "Directory");
                    } else {
                        System.out.println("Already Downloaded");
                    }
                }
                System.out.println("Downloading Virtual Machine Snapshot"
                        + " / Suspend / Log Directory Complete");
            }

            String[] virtualDiskLocations = getVDiskLocations(vmRef);
            if (virtualDiskLocations != null) {
                System.out.println("Downloading Virtual Disks");
                for (int i = 0; i < virtualDiskLocations.length; i++) {
                    if (virtualDiskLocations[i] != null) {
                        String dataStoreName =
                                virtualDiskLocations[i].substring(
                                        virtualDiskLocations[i].indexOf("[") + 1,
                                        virtualDiskLocations[i].lastIndexOf("]"));
                        String configurationDir =
                                virtualDiskLocations[i].substring(
                                        virtualDiskLocations[i].indexOf("]") + 2,
                                        virtualDiskLocations[i].lastIndexOf("/"));
                        if (!downloadedDir.containsKey(configurationDir + "#vm#"
                                + dataStoreName)) {
                            boolean success =
                                    new File(localPath + "/" + configurationDir
                                            + "#vdisk#" + dataStoreName).mkdir();
                            if (!success) {
                                System.out.println("Could not create " + localPath
                                        + "/" + configurationDir + "#vdisk#"
                                        + dataStoreName + "directory");
                            }
                            downloadDirectory(configurationDir, configurationDir
                                    + "#vdisk#" + dataStoreName, dataStoreName,
                                    dataCenterName);
                            downloadedDir.put(configurationDir + "#vdisk#"
                                    + dataStoreName, "Directory");
                        } else {
                            System.out.println("Already Downloaded");
                        }
                    } else {
                        System.out.println("Already Downloaded");
                    }
                }
                System.out.println("Downloading Virtual Disks Complete");
            } else {
                System.out.println("Downloading Virtual Disks Complete");
            }
        } else {
            throw new IllegalArgumentException("Virtual Machine " + vmName
                    + " Not Found.");
        }
    }

    String[] getVmDirectory(ManagedObjectReference vmmor) throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        String[] vmDir = new String[4];
        VirtualMachineConfigInfo vmConfigInfo =
                (VirtualMachineConfigInfo) getMOREFs.entityProps(vmmor,
                        new String[]{"config"}).get("config");
        if (vmConfigInfo != null) {
            vmDir[0] = vmConfigInfo.getFiles().getVmPathName();
            vmDir[1] = vmConfigInfo.getFiles().getSnapshotDirectory();
            vmDir[2] = vmConfigInfo.getFiles().getSuspendDirectory();
            vmDir[3] = vmConfigInfo.getFiles().getLogDirectory();
        } else {
            System.out.println("Connot Restore VM. Not Able "
                    + "To Find The Virtual Machine Config Info");
        }
        return vmDir;
    }

    void getDiskSizeInKB(ManagedObjectReference vmMor) throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        VirtualMachineConfigInfo vmConfigInfo =
                (VirtualMachineConfigInfo) getMOREFs.entityProps(vmMor,
                        new String[]{"config"}).get("config");
        if (vmConfigInfo != null) {
            List<VirtualDevice> livd = vmConfigInfo.getHardware().getDevice();
            for (VirtualDevice virtualDevice : livd) {
                if (virtualDevice instanceof VirtualDisk) {
                    System.out.println("Disk size in kb: "
                            + ((VirtualDisk) virtualDevice).getCapacityInKB());
                }
            }
        }
    }

    String[] getVDiskLocations(ManagedObjectReference vmmor) throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        VirtualMachineConfigInfo vmConfigInfo =
                (VirtualMachineConfigInfo) getMOREFs.entityProps(vmmor,
                        new String[]{"config"}).get("config");
        System.out.println("vmconfig info : " + vmConfigInfo);
        if (vmConfigInfo != null) {
            List<VirtualDevice> livd = vmConfigInfo.getHardware().getDevice();
            VirtualDevice[] vDevice = livd.toArray(new VirtualDevice[livd.size()]);
            int count = 0;
            String[] virtualDisk = new String[vDevice.length];

            for (int i = 0; i < vDevice.length; i++) {
                if (vDevice[i].getClass().getCanonicalName()
                        .equalsIgnoreCase("com.vmware.vim25.VirtualDisk")) {
                    try {
                        long size = ((VirtualDisk) vDevice[i]).getCapacityInKB();
                        System.out.println("Disk size in kb: " + size);
                        VirtualDeviceFileBackingInfo backingInfo =
                                (VirtualDeviceFileBackingInfo) vDevice[i].getBacking();
                        virtualDisk[count] = backingInfo.getFileName();
                        System.out.println("virtualDisk : " + virtualDisk[count]);
                        count++;
                    } catch (ClassCastException e) {
                        System.out.println("Got Exception : " + e);
                    }
                }
            }
            return virtualDisk;
        } else {
            System.out.println("Connot Restore VM. Not Able To"
                    + " Find The Virtual Machine Config Info");
            return null;
        }
    }

    void downloadDirectory(String directoryName,
                           String localDirectory, String dataStoreName, String dataCenter) throws IOException {
        String serviceUrl = connection.getUrl();
        serviceUrl = serviceUrl.substring(0, serviceUrl.lastIndexOf("sdk") - 1);
        String httpUrl =
                serviceUrl + "/folder/" + directoryName + "?dcPath=" + dataCenter
                        + "&dsName=" + dataStoreName;
        httpUrl = httpUrl.replaceAll("\\ ", "%20");
        System.out.println("httpUrl : " + httpUrl);
        String[] linkMap = getListFiles(httpUrl);
        for (int i = 0; i < linkMap.length; i++) {
            System.out.println("Downloading VM File " + linkMap[i]);
            String urlString = serviceUrl + linkMap[i];
            String fileName =
                    localDirectory
                            + "/"
                            + linkMap[i].substring(linkMap[i].lastIndexOf("/"),
                            linkMap[i].lastIndexOf("?"));
            urlString = urlString.replaceAll("\\ ", "%20");
            getData(urlString, fileName);
        }
    }

    @SuppressWarnings("unchecked")
    String[] getListFiles(String urlString) throws IOException {
        HttpURLConnection conn = null;
        URL urlSt = new URL(urlString);
        conn = (HttpURLConnection) urlSt.openConnection();
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setAllowUserInteraction(true);

        // Maintain session
        String cookie = "$Version=\"1\"; ";
        List<String> cookies = (List<String>) headers.get("Set-cookie");
        if (cookies.size() > 0) {
            cookieValue = cookies.get(0);
            StringTokenizer tokenizer = new StringTokenizer(cookieValue, ";");
            cookieValue = tokenizer.nextToken();
            String pathData = "$" + tokenizer.nextToken();
            cookie += cookieValue + "; " + pathData;
        }

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
        String line = null;
        String xmlString = "";
        BufferedReader in =
                new BufferedReader(new InputStreamReader(conn.getInputStream()));
        while ((line = in.readLine()) != null) {
            xmlString = xmlString + line;
        }
        xmlString = xmlString.replaceAll("&amp;", "&");
        xmlString = xmlString.replaceAll("%2e", ".");
        xmlString = xmlString.replaceAll("%2d", "-");
        xmlString = xmlString.replaceAll("%5f", "_");
        ArrayList<String> list = getFileLinks(xmlString);
        String[] linkMap = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            linkMap[i] = list.get(i);
        }
        return linkMap;
    }

    ArrayList<String> getFileLinks(String xmlString) {
        ArrayList<String> linkMap = new ArrayList<String>();
        Pattern regex = Pattern.compile("<a href=\".*?\">");
        Matcher regexMatcher = regex.matcher(xmlString);
        while (regexMatcher.find()) {
            String data = regexMatcher.group();
            int ind = data.indexOf("\"") + 1;
            int lind = data.lastIndexOf("\"");
            data = data.substring(ind, lind);
            if (data.indexOf("folder?") == -1) {
                System.out.println("fileLinks data : " + data);
                linkMap.add(data);
            }
        }
        return linkMap;
    }

    @SuppressWarnings("unchecked")
    void getData(String urlString, String fileName) throws IOException {
        HttpURLConnection conn = null;
        URL urlSt = new URL(urlString);
        conn = (HttpURLConnection) urlSt.openConnection();
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setAllowUserInteraction(true);
        // Maintain session
        List<String> cookies = (List<String>) headers.get("Set-cookie");
        cookieValue = cookies.get(0);
        StringTokenizer tokenizer = new StringTokenizer(cookieValue, ";");
        cookieValue = tokenizer.nextToken();
        String pathData = "$" + tokenizer.nextToken();
        String cookie = "$Version=\"1\"; " + cookieValue + "; " + pathData;

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
        int leng = fileName.lastIndexOf("/");
        String dir = fileName.substring(0, leng - 1);
        String fName = fileName.substring(leng + 1);
        fName = fName.replace("%20", " ");
        dir = replaceSpecialChar(dir);
        fileName = localPath + "/" + dir + "/" + fName;
        OutputStream out =
                new BufferedOutputStream(new FileOutputStream(fileName));
        int bufLen = 9 * 1024;
        byte[] buf = new byte[bufLen];
        byte[] tmp = null;
        int len = 0;
        @SuppressWarnings("unused")
        int bytesRead = 0;
        while ((len = in.read(buf, 0, bufLen)) != -1) {
            bytesRead += len;
            tmp = new byte[len];
            System.arraycopy(buf, 0, tmp, 0, len);
            out.write(tmp, 0, len);
        }
        in.close();
        out.close();
    }

    String replaceSpecialChar(String fileName) {
        fileName = fileName.replace(':', '_');
        fileName = fileName.replace('*', '_');
        fileName = fileName.replace('<', '_');
        fileName = fileName.replace('>', '_');
        fileName = fileName.replace('|', '_');
        return fileName;
    }

    @Action
    public void run() throws RuntimeFaultFaultMsg, IOException, InvalidPropertyFaultMsg {
        getVM();
    }

}
