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

package com.vmware.guest;

import com.vmware.common.annotations.Action;
import com.vmware.common.annotations.Option;
import com.vmware.common.annotations.Sample;
import com.vmware.connection.ConnectedVimServiceBase;
import com.vmware.vim25.*;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

/**
 * <pre>
 * UploadGuestFile
 *
 * This sample uploads a file from the client machine to a
 * specified location inside the guest.
 *
 * <b>Parameters:</b>
 * url             [required] : url of the web service
 * username        [required] : username for the authentication
 * password        [required] : password for the authentication
 * vmname          [required] : name of the virtual machine
 * guestusername   [required] : username in the guest
 * guestpassword   [required] : password in the guest
 * guestfilepath   [required] : path of the file in the guest
 * localfilepath   [required] : local file path to upload
 * guesttype       [required] : Type of the guest. (windows or posix)
 * overwrite       [optional] : whether to overwrite the file in the guest
 *
 * <b>Command Line:</b>
 * run.bat com.vmware.general.UploadGuestFile --url [webserviceurl]
 * --username [username] --password [password] --vmname [vmname]
 * --guestusername [guest user] --guestpassword [guest password]
 * --guestfilepath [path of the file inside the guest]
 * --localfilepath [local file path to upload]
 * --guesttype [windows or posix]
 * [--overwrite]
 * </pre>
 */

@Sample(
        name = "upload-guest-file",
        description = "This sample uploads a file from the client machine to a\n" +
                "specified location inside the guest. Since vSphere API 5.0"
)
public class UploadGuestFile extends ConnectedVimServiceBase {
    private GuestConnection guestConnection;
    private Boolean overwrite = Boolean.FALSE;

    @Option(name = "guestConnection", type = GuestConnection.class)
    public void setGuestConnection(GuestConnection guestConnection) {
        this.guestConnection = guestConnection;
    }

    String guestFilePath;
    String localFilePath;
    String guestType;

    @Option(name = "guestfilepath", description = "path of the file in the guest")
    public void setGuestFilePath(String path) {
        this.guestFilePath = path;
    }

    @Option(name = "localfilepath", description = "local file path to upload")
    public void setLocalFilePath(String path) {
        this.localFilePath = path;
    }

    @Option(name = "guesttype", description = "Type of the guest. (windows or posix)")
    public void setGuestType(String type) {
        this.guestType = type;
    }

    @Option(name = "overwrite", required = false, description = "whether to overwrite the file in the guest")
    public void setOverwrite(String overwrite) {
        this.overwrite = Boolean.valueOf(overwrite);
    }

    VirtualMachinePowerState powerState;

    ManagedObjectReference hostMOR;
    long fileSize;
    X509Certificate x509CertificateToTrust = null;

    void uploadData(String urlString, String fileName) throws IOException {
        HttpURLConnection conn = null;
        URL urlSt = new URL(urlString);
        conn = (HttpURLConnection) urlSt.openConnection();
        conn.setDoInput(true);
        conn.setDoOutput(true);

        conn.setRequestProperty("Content-Type", "application/octet-stream");
        conn.setRequestMethod("PUT");
        conn.setRequestProperty("Content-Length", Long.toString(fileSize));
        OutputStream out = conn.getOutputStream();
        InputStream in = new FileInputStream(fileName);
        byte[] buf = new byte[102400];
        int len = 0;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();

        int returnErrorCode = conn.getResponseCode();
        conn.disconnect();
        if (HttpsURLConnection.HTTP_OK != returnErrorCode) {
            throw new UploadException("File Upload is unsuccessful");
        }
    }

    @Action
    public void run() throws InvalidPropertyFaultMsg, InvalidCollectorVersionFaultMsg, TaskInProgressFaultMsg, FileFaultFaultMsg, InvalidStateFaultMsg, GuestOperationsFaultFaultMsg, IOException, CertificateException, RuntimeFaultFaultMsg, DatatypeConfigurationException {
        serviceContent.getPropertyCollector();
        String virtualMachineName = guestConnection.vmname;
        Map<String, ManagedObjectReference> vms =
                getMOREFs.inFolderByType(serviceContent.getRootFolder(),
                        "VirtualMachine");
        ManagedObjectReference vmMOR = vms.get(virtualMachineName);
        if (vmMOR != null) {
            System.out.println("Virtual Machine " + virtualMachineName
                    + " found");
            powerState =
                    (VirtualMachinePowerState) getMOREFs.entityProps(vmMOR,
                            new String[]{"runtime.powerState"}).get(
                            "runtime.powerState");
            if (!powerState.equals(VirtualMachinePowerState.POWERED_ON)) {
                System.out.println("VirtualMachine: " + virtualMachineName
                        + " needs to be powered on");
                return;
            }
        } else {
            System.out.println("Virtual Machine " + virtualMachineName
                    + " not found.");
            return;
        }

        String[] opts = new String[]{"guest.guestOperationsReady"};
        String[] opt = new String[]{"guest.guestOperationsReady"};
        Object[] results =
                waitForValues.wait(vmMOR, opts, opt,
                        new Object[][]{new Object[]{true}});

        System.out.println("Guest Operations are ready for the VM");
        ManagedObjectReference guestOpManger =
                serviceContent.getGuestOperationsManager();
        ManagedObjectReference fileManagerRef =
                (ManagedObjectReference) getMOREFs.entityProps(guestOpManger,
                        new String[]{"fileManager"}).get("fileManager");
        NamePasswordAuthentication auth = new NamePasswordAuthentication();
        auth.setUsername(guestConnection.username);
        auth.setPassword(guestConnection.password);
        auth.setInteractiveSession(false);

        GuestFileAttributes guestFileAttributes = null;
        if (guestType.equalsIgnoreCase("windows")) {
            guestFileAttributes = new GuestWindowsFileAttributes();
        } else {
            guestFileAttributes = new GuestPosixFileAttributes();
        }

        guestFileAttributes.setAccessTime(DatatypeFactory.newInstance()
                .newXMLGregorianCalendar(new GregorianCalendar()));

        guestFileAttributes.setModificationTime(DatatypeFactory.newInstance()
                .newXMLGregorianCalendar(new GregorianCalendar()));
        System.out.println("Executing UploadGuestFile guest operation");

        File file = new File(localFilePath);
        if (!file.exists()) {
            System.out.println("Error finding the file: " + localFilePath);
            return;
        }

        if (file.isDirectory()) {
            System.out.println("Local file path points to a directory");
            return;
        }

        fileSize = file.length();
        System.out.println("Size of the file is :" + fileSize + "");
        System.out.println("Executing UploadFile guest operation");
        String fileUploadUrl =
                vimPort.initiateFileTransferToGuest(fileManagerRef, vmMOR, auth,
                        guestFilePath, guestFileAttributes, fileSize,
                        overwrite);
        URL tempUrlObject = new URL(connection.getUrl());
        fileUploadUrl =
                fileUploadUrl.replaceAll("\\*", tempUrlObject.getHost());
        System.out.println("Uploading the file to :" + fileUploadUrl + "");

        if (hostMOR != null) {
            opts = new String[]{"config.certificate"};
            opt = new String[]{"config.certificate"};
            results = waitForValues.wait(hostMOR, opts, opt, null);
            List<Byte> certificate = ((ArrayOfByte) results[0]).getByte();
            byte[] certificateBytes = new byte[certificate.size()];
            int index = 0;
            for (Byte b : certificate) {
                certificateBytes[index++] = b.byteValue();
            }

            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            x509CertificateToTrust =
                    (X509Certificate) cf
                            .generateCertificate(new ByteArrayInputStream(
                                    certificateBytes));
            System.out
                    .println("Certificate of the host is successfully retrieved");
        }

        uploadData(fileUploadUrl, localFilePath);
        System.out.println("Successfully uploaded the file");
    }

    private class UploadException extends RuntimeException {
		private static final long serialVersionUID = 1L;
        public UploadException(String message) {
            super(message);
        }
    }
}
