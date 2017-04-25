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

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * <pre>
 * OVFManagerExportVMDK
 *
 * This is a demonstrates OVFManager.Exports VMDK's of a VM to the localSystem.
 *
 * <b>Parameters:</b>
 * url              [required] : url of the web service.
 * username         [required] : username for the authentication
 * password         [required] : password for the authentication
 * vmname           [required] : Name of the virtual machine
 * host             [required] : Name of Host System
 * localPath        [required] : Absolute path of localSystem folder
 *
 * <b>Command Line:</b>
 * run.bat com.vmware.vapp.OVFManagerExportVMDK
 * --url [webserviceurl] --username [username] --password [password]--vmname [VM name]
 * --host [Name of Host]
 * </pre>
 */

@Sample(
        name = "ovf-manager-export-vmdk",
        description = "This is a demonstrates OVFManager.Exports VMDK's of a VM to the localSystem."
)
public class OVFManagerExportVMDK extends ConnectedVimServiceBase {

    volatile long TOTAL_BYTES = 0;
    HttpNfcLeaseExtender leaseExtender;
    volatile long TOTAL_BYTES_WRITTEN = 0;
    boolean vmdkFlag = false;

    String vmname = null;
    String host = null;
    String localPath = null;

    @Option(name = "vmname", description = "Name of the virtual machine")
    public void setVmname(String vmname) {
        this.vmname = vmname;
    }

    @Option(name = "host", description = "")
    public void setHost(String host) {
        this.host = host;
    }

    @Option(name = "localpath", description = "Absolute path of localSystem folder")
    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    void exportVM() throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        File file = new File(localPath);
        if (!file.exists()) {
            System.out.println("Wrong or invalid path " + localPath);
            return;
        }
        ManagedObjectReference srcMOR =
                getMOREFs.inFolderByType(serviceContent.getRootFolder(),
                        "HostSystem").get(host);
        if (srcMOR == null) {
            throw new RuntimeException(" Source Host " + host + " Not Found.");
        }
        ManagedObjectReference vmMoRef =
                getMOREFs.inFolderByType(srcMOR, "VirtualMachine").get(vmname);

        if (vmMoRef == null) {
            throw new RuntimeException("Virtual Machine " + vmname + " Not Found.");
        }
        System.out.println("Getting the HTTP NFCLEASE for the VM: " + vmname);
        try {
            ManagedObjectReference httpNfcLease = vimPort.exportVm(vmMoRef);

            Object[] result =
                    waitForValues.wait(httpNfcLease, new String[]{"state"},
                            new String[]{"state"}, new Object[][]{new Object[]{
                            HttpNfcLeaseState.READY, HttpNfcLeaseState.ERROR}});
            if (result[0].equals(HttpNfcLeaseState.READY)) {
                System.out.println("HttpNfcLeaseState: " + result[0]);
                HttpNfcLeaseInfo httpNfcLeaseInfo =
                        (HttpNfcLeaseInfo) getMOREFs.entityProps(httpNfcLease,
                                new String[]{"info"}).get("info");
                httpNfcLeaseInfo.setLeaseTimeout(300000000);
                printHttpNfcLeaseInfo(httpNfcLeaseInfo, host);
                long diskCapacity =
                        (httpNfcLeaseInfo.getTotalDiskCapacityInKB()) * 1024;
                TOTAL_BYTES = diskCapacity;
                leaseExtender =
                        new OVFManagerExportVMDK().new HttpNfcLeaseExtender(
                                httpNfcLease, vimPort);
                Thread t = new Thread(leaseExtender);
                t.start();
                List<HttpNfcLeaseDeviceUrl> deviceUrlArr =
                        httpNfcLeaseInfo.getDeviceUrl();
                for (HttpNfcLeaseDeviceUrl deviceUrl : deviceUrlArr) {
                    System.out.println("Downloading Files:");
                    String deviceUrlStr = deviceUrl.getUrl();
                    String absoluteFile =
                            deviceUrlStr.substring(deviceUrlStr.lastIndexOf("/") + 1);
                    System.out.println("Absolute File Name: " + absoluteFile);
                    System.out.println("VMDK URL: "
                            + deviceUrlStr.replace("*", host));
                    writeVMDKFile(absoluteFile, deviceUrlStr.replace("*", host),
                            diskCapacity, vmname);
                }
                System.out.println("Completed Downloading the files");
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

    void printHttpNfcLeaseInfo(HttpNfcLeaseInfo info,
                               String hostName) {
        System.out
                .println("########################################################");
        System.out.println("HttpNfcLeaseInfo");
        System.out.println("Lease Timeout: " + info.getLeaseTimeout());
        System.out.println("Total Disk capacity: "
                + info.getTotalDiskCapacityInKB());
        List<HttpNfcLeaseDeviceUrl> deviceUrlArr = info.getDeviceUrl();
        int deviceUrlCount = 1;
        for (HttpNfcLeaseDeviceUrl durl : deviceUrlArr) {
            System.out.println("HttpNfcLeaseDeviceUrl : " + deviceUrlCount++);
            System.out.println("   Device URL Import Key: " + durl.getImportKey());
            System.out.println("   Device URL Key: " + durl.getKey());
            System.out.println("   Device URL : " + durl.getUrl());
            System.out.println("   Updated device URL: "
                    + durl.getUrl().replace("*", hostName));
            System.out.println("   SSL Thumbprint : " + durl.getSslThumbprint());
        }
        System.out
                .println("########################################################");
    }

    /**
     * The Class HttpNfcLeaseExtender.
     */
    public class HttpNfcLeaseExtender implements Runnable {
        private ManagedObjectReference httpNfcLease = null;
        private VimPortType vimPort = null;

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

    void writeVMDKFile(String absoluteFile, String string,
                       long diskCapacity, String vmName) throws IOException {
        HttpURLConnection conn = getHTTPConnection(string);
        InputStream in = conn.getInputStream();
        String fileName = localPath + "/" + vmName + "-" + absoluteFile;
        OutputStream out = new FileOutputStream(new File(fileName));
        byte[] buf = new byte[102400];
        int len = 0;
        long written = 0;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
            written = written + len;
        }
        System.out.println("Exported File " + vmName + "-" + absoluteFile + " : "
                + written);
        in.close();
        out.close();
    }

    HttpURLConnection getHTTPConnection(String urlString) throws IOException {
        URL urlStr = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) urlStr.openConnection();

        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setAllowUserInteraction(true);
        conn.connect();
        return conn;
    }

    @Action
    public void run() throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        exportVM();
    }
}
