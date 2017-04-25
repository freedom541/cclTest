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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <pre>
 * RunProgram
 *
 * This sample runs a specified program inside a virtual machine with
 * output re-directed to a temporary file inside the guest and
 * downloads the output file.
 *
 * <b>Parameters:</b>
 * url                 [required] : url of the web service
 * username            [required] : username for the authentication
 * password            [required] : password for the authentication
 * vmname              [required] : name of the virtual machine
 * guestusername       [required] : username in the guest
 * guestpassword       [required] : password in the guest
 * guestprogrampath    [required] : Fully qualified path of the program
 *                                  inside the guest.
 * localoutputfilepath [required] : Path to the local file to store the
 *                                  output.
 * interactivesession  [optional] : Run the program within an
 *                                  interactive session inside the guest.
 *
 * <b>Command Line:</b>
 * run.bat com.vmware.general.RunProgram --url [webserviceurl]
 * --username [username] --password [password] --vmname [vmname]
 * --guestusername [guest user] --guestpassword [guest password]
 * --guestprogrampath [Fully qualified path of the program in the guest]
 * --localoutputfilepath [Path to the local file to store the output]
 * [--interactivesession]
 * </pre>
 */

@Sample(
        name = "run-program",
        description = "This sample runs a specified program inside a virtual machine with\n" +
                "output re-directed to a temporary file inside the guest and\n" +
                "downloads the output file. Since vSphere API 5.0\n"
)
public class RunProgram extends ConnectedVimServiceBase {
    private ManagedObjectReference vmMOR;
    private ManagedObjectReference fileManagerRef;
    private ManagedObjectReference processManagerRef;
    private ManagedObjectReference propCollector;
    private String tempFilePath;

    GuestConnection guestConnection;
    VirtualMachinePowerState powerState;
    Map<String, String> optionsmap =
            new HashMap<String, String>();
    NamePasswordAuthentication auth = null;

    String guestProgramPath;
    String localOutputFilePath;
    boolean interactive;

    @Option(name = "guestConnection", type = GuestConnection.class)
    public void setGuestConnection(GuestConnection guestConnection) {
        this.guestConnection = guestConnection;
    }

    @Option(name = "guestprogrampath", description = "Fully qualified path of the program inside the guest.")
    public void setGuestProgramPath(String guestProgramPath) {
        this.guestProgramPath = guestProgramPath;
    }

    @Option(name = "localoutputfilepath", description = "Path to the local file to store the output.")
    public void setLocalOutputFilePath(String localOutputFilePath) {
        this.localOutputFilePath = localOutputFilePath;
    }

    @Option(
            name = "interactivesession",
            required = false,
            description = "Run the program within an interactive session inside the guest."
    )
    public void setInteractive(String interactive) {
        this.interactive = Boolean.valueOf(interactive) || "yes".equalsIgnoreCase(interactive);
    }

    /*
    * This method calls all the initialization methods required in order.
    */
    void initAll() {
        initPropertyCollector();
        initRootFolder();
    }

    void initPropertyCollector() {
        if (propCollector == null) {
            propCollector = serviceContent.getPropertyCollector();
            if (propCollector == null) {
                throw new RunProgramException("Could not get Property Collector");
            }
        }
    }

    void initRootFolder() {
        if (rootRef == null) {
            rootRef = serviceContent.getRootFolder();
            if (rootRef == null) {
                throw new RunProgramException("Could not get Root Folder");
            }
        }
    }

    /**
     * pulls data from a url and puts it to the fileName
     */
    void getData(String urlString, String fileName)
            throws IOException {
        HttpURLConnection conn = null;
        URL urlSt = new URL(urlString);
        conn = (HttpURLConnection) urlSt.openConnection();
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/octet-stream");
        conn.setRequestMethod("GET");
        InputStream in = conn.getInputStream();
        OutputStream out = new FileOutputStream(fileName);
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
            throw new RunProgramException("File Download is unsuccessful");
        }
    }

    boolean verifyInputArguments() {
        List<String> vinput = new ArrayList<String>();
        vinput.add(guestProgramPath);
        vinput.add(localOutputFilePath);
        for (String s : vinput) {
            if (s == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * main method annotated with "Action"
     */
    @Action
    public void run() throws Exception, InvalidPropertyFaultMsg {
        Map<String, ManagedObjectReference> vms =
                getMOREFs.inFolderByType(serviceContent.getRootFolder(),
                        "VirtualMachine");
        vmMOR = vms.get(guestConnection.vmname);
        if (vmMOR != null) {
            System.out.println("Virtual Machine " + guestConnection.vmname
                    + " found");
            powerState =
                    (VirtualMachinePowerState) getMOREFs.entityProps(vmMOR,
                            new String[]{"runtime.powerState"}).get(
                            "runtime.powerState");
            if (!powerState.equals(VirtualMachinePowerState.POWERED_ON)) {
                System.out.println("VirtualMachine: " + guestConnection.vmname
                        + " needs to be powered on");
                return;
            }
        } else {
            System.out.println("Virtual Machine " + guestConnection.vmname
                    + " not found.");
            return;
        }

        boolean useInteractiveSession = interactive;
        String[] opts;
        String[] opt;
        if (useInteractiveSession) {
            opts = new String[]{"guest.interactiveGuestOperationsReady"};
            opt = new String[]{"guest.interactiveGuestOperationsReady"};
        } else {
            opts = new String[]{"guest.guestOperationsReady"};
            opt = new String[]{"guest.guestOperationsReady"};
        }
        waitForValues.wait(vmMOR, opts, opt,
                new Object[][]{new Object[]{true}});

        System.out.println("Guest Operations are ready for the VM");
        ManagedObjectReference guestOpManger =
                serviceContent.getGuestOperationsManager();
        Map<String, Object> guestOpMgr =
                getMOREFs.entityProps(guestOpManger, new String[]{"processManager",
                        "fileManager"});
        fileManagerRef =
                (ManagedObjectReference) guestOpMgr.get("fileManager");
        processManagerRef =
                (ManagedObjectReference) guestOpMgr.get("processManager");
        auth = new NamePasswordAuthentication();
        auth.setUsername(guestConnection.username);
        auth.setPassword(guestConnection.password);
        auth.setInteractiveSession(useInteractiveSession);

        System.out.println("Executing CreateTemporaryFile guest operation");
        tempFilePath =
                vimPort.createTemporaryFileInGuest(fileManagerRef, vmMOR, auth,
                        "", "", "");
        System.out.println("Successfully created a temporary file at: "
                + tempFilePath + " inside the guest");

        GuestProgramSpec spec = new GuestProgramSpec();
        spec.setProgramPath(guestProgramPath);
        spec.setArguments("> " + tempFilePath + " 2>&1");
        System.out.println("Starting the specified program inside the guest");
        long pid =
                vimPort
                        .startProgramInGuest(processManagerRef, vmMOR, auth, spec);
        System.out
                .println("Process ID of the program started is: " + pid + "");

        List<Long> pidsList = new ArrayList<Long>();
        pidsList.add(pid);
        List<GuestProcessInfo> procInfo = null;
        do {
            System.out.println("Waiting for the process to finish running.");
            procInfo =
                    vimPort.listProcessesInGuest(processManagerRef, vmMOR, auth,
                            pidsList);
            Thread.sleep(5 * 1000);
        } while (procInfo.get(0).getEndTime() == null);

        System.out.println("Exit code of the program is "
                + procInfo.get(0).getExitCode());

        FileTransferInformation fileTransferInformation = null;
        fileTransferInformation =
                vimPort.initiateFileTransferFromGuest(fileManagerRef, vmMOR,
                        auth, tempFilePath);
        String fileDownloadUrl =
                fileTransferInformation.getUrl().replaceAll("\\*",
                        connection.getHost());
        System.out.println("Downloading the output file from :"
                + fileDownloadUrl);
        getData(fileDownloadUrl, localOutputFilePath);
        System.out.println("Successfully downloaded the file");
    }

    private class RunProgramException extends RuntimeException {
        public RunProgramException(String message) {
            super(message);
        }
    }
}

