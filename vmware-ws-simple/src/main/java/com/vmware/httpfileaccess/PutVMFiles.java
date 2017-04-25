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
import com.vmware.connection.KeepAlive;
import com.vmware.vim25.*;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.*;

/**
 * <pre>
 * PutVMFiles
 *
 * This sample puts VM files in specified Datacenter and
 * Datastore and register and reconfigure the particular VM
 *
 * <b>Parameters:</b>
 * url              [required]: url of the web service.
 * username         [required]: username for the authentication
 * password         [required]: password for the authentication
 * vmname           [required]: Name of the virtual machine
 * localpath        [required]: Local path containing virtual machine files
 * datacentername   [required]: Name of the target datacenter
 * datastorename    [required]: Name of the target datastore
 *
 * <b>Command Line:</b>
 * run.bat com.vmware.httpfileaccess.PutVMFiles
 * --url [URLString] --username [username] --password [password]
 * --vmname [VM name] --localpath [local path]
 * --datacentername [datacenter name]
 * --datastorename [datastore name]
 * </pre>
 */
@Sample(
        name = "put-vm-files",
        description = "This sample puts VM files in specified Datacenter and Datastore" +
                " and register and reconfigure the particular VM. The VM you use, should be downloaded" +
                " from the vSphere you are uploading to. The name of the VM, VM folder, and VM disk files" +
                " should all be the same. The name of the VM should be unique and unused on the Host." +
                " This works best if you use a VM you obtained through GetVMFiles."
)
public class PutVMFiles extends ConnectedVimServiceBase {
    private String cookieValue = "";
    private ManagedObjectReference registeredVMRef = null;

    boolean verbose = true;

    String vmName = null;
    String localPath = null;
    String datacenter = null;
    String datastore = null;

    @Option(
            name = "vmname",
            description = "Name of the virutal machine to upload. " +
                    "Should be unique and unused. Should be the same as the name of the vm folder and vm file names.")
    public void setVmName(final String vmName) {
        this.vmName = vmName;
    }

    @Option(
            name = "localpath",
            description =
                    "Local path from which files will be copied. " +
                            "This should be the path holding the virtual machine folder but not the vm folder itself."
    )
    public void setLocalPath(final String localPath) {
        // the program will look in here for a virtualmachine.vm folder
        this.localPath = localPath;
    }

    @Option(name = "datacentername", description = "Name of the target datacenter")
    public void setDatacenter(final String datacenter) {
        this.datacenter = datacenter;
    }

    @Option(name = "datastorename", description = "Name of the target datastore")
    public void setDatastore(final String datastore) {
        this.datastore = datastore;
    }

    @Option(name = "verbose", required = false, description = "" +
            "defaults to 'true' and prints more information, " +
            "set to 'false' to print less.")
    public void setVerbose(final Boolean verbosity) {
        this.verbose = verbosity;
    }

    boolean customValidation() {
        boolean validate = false;

        try {
            if (datacenter != null && datacenter.length() != 0
                    && datastore != null && datastore.length() != 0 ) {
                ManagedObjectReference dcmor =
                        getMOREFs.inContainerByType(serviceContent.getRootFolder(),
                                "Datacenter").get(datacenter);
                if (dcmor != null) {
                    ManagedObjectReference ds =
                            getMOREFs.inContainerByType(dcmor, "Datastore").get(datastore);
                    if (ds == null) {
                        System.out.println("Specified Datastore with name " + datastore
                                + " was not" + " found in specified Datacenter");
                        return validate;
                    }
                    validate = true;
                } else {
                    System.out.println("Specified Datacenter with name " + datacenter
                            + " not Found");
                    return validate;
                }
            }
        } catch (RuntimeFaultFaultMsg runtimeFaultFaultMsg) {
            throw new PutVMFilesException(runtimeFaultFaultMsg);
        } catch (InvalidPropertyFaultMsg invalidPropertyFaultMsg) {
            throw new PutVMFilesException(invalidPropertyFaultMsg);
        }

        return validate;
    }

    /**
     * Lists out the subdirectories and files under the localDir you specified.
     *
     * @param dir - place on the file system to look
     * @return - list of files under that location
     */
    String[] getDirFiles(final File dir) {
        if (dir.exists() && dir.isDirectory()) {
            return dir.list();
        } else {
            throw new RuntimeException("Local Path Doesn't Exist: " + dir.toString());

        }
    }

    @SuppressWarnings("unchecked")
    void putVMFiles(final String remoteFilePath, final File localFile) {
        final String url = connection.getUrl();
        final String serviceUrl = url.substring(0, url.lastIndexOf("sdk") - 1);
        String httpUrl =
                serviceUrl + "/folder" + remoteFilePath + "?dcPath=" + datacenter
                        + "&dsName=" + datastore;
        httpUrl = httpUrl.replaceAll("\\ ", "%20");
        System.out.printf("%nPutting VM File %s ", httpUrl);

        final URL fileURL;
        final HttpURLConnection conn;
        try {
            fileURL = new URL(httpUrl);
            conn = (HttpURLConnection) fileURL.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setAllowUserInteraction(true);
        } catch (MalformedURLException e) {
            throw new PutVMFilesException(e);
        } catch (IOException e) {
            throw new PutVMFilesException(e);
        }

        // Maintain session
        final List<String> cookies = (List<String>) headers.get("Set-cookie");
        cookieValue = (cookies != null)?cookies.get(0):"";
        final StringTokenizer tokenizer = new StringTokenizer(cookieValue, ";");
        cookieValue = tokenizer.nextToken();
        final String path = "$" + tokenizer.nextToken();
        final String cookie = "$Version=\"1\"; " + cookieValue + "; " + path;

        // set the cookie in the new request header
        final Map<String, List<String>> map = new HashMap<String, List<String>>();
        map.put("Cookie", Collections.singletonList(cookie));
        ((BindingProvider) vimPort).getRequestContext().put(
                MessageContext.HTTP_REQUEST_HEADERS, map);

        conn.setRequestProperty("Cookie", cookie);
        conn.setRequestProperty("Content-Type", "application/octet-stream");
        try {
            conn.setRequestMethod("PUT");
        } catch (ProtocolException e) {
            throw new PutVMFilesException(e);
        }
        conn.setRequestProperty("Content-Length", "1024");
        long fileLen = localFile.length();
        System.out.println("File size is: " + fileLen);

        // setChunkedStreamingMode to -1 turns off chunked mode
        // setChunkedStreamingMode to 0 asks for system default
        // NOTE:
        // larger values mean faster connections at the
        // expense of more heap consumption.
        conn.setChunkedStreamingMode(0);

        OutputStream out = null;
        InputStream in = null;
        try {
            out = conn.getOutputStream();
            in = new BufferedInputStream(new FileInputStream(localFile));
            int bufLen = 9 * 1024;
            byte[] buf = new byte[bufLen];
            byte[] tmp = null;
            int len = 0;
            // this can take a very long time, so we do a keep-alive here.
            Thread keepAlive = KeepAlive.keepAlive(vimPort, getServiceInstanceReference());
            keepAlive.start();
            final String[] spinner = new String[] {"\u0008/", "\u0008-", "\u0008\\", "\u0008|" };
            System.out.printf(".");
            int i = 0;
            while ((len = in.read(buf, 0, bufLen)) != -1) {
                tmp = new byte[len];
                System.arraycopy(buf, 0, tmp, 0, len);
                out.write(tmp, 0, len);
                if (verbose) {
                	System.out.printf("%s", spinner[i++ % spinner.length]);
                }
            }
            System.out.printf("\u0008");
            keepAlive.interrupt();
        } catch (FileNotFoundException e) {
            throw new PutVMFilesException(e);
        } catch (IOException e) {
            throw new PutVMFilesException(e);
        } finally {
            try {
                if(in!=null) in.close();
                if(out!=null) out.close();
                conn.getResponseCode();
            } catch (IOException e) {
                throw new PutVMFilesException(e);
            }
            conn.disconnect();
        }

    }

    /**
     * Copy contents of this directory up to the datastore
     *
     * @param dirName
     * @throws IOException
     */
    public void copyDir(String dirName) {
        System.out.print("Copying The Virtual Machine To Host...");
        File dir = new File(localPath,dirName);
        String[] listOfFiles = getDirFiles(dir);
        for (int i = 0; i < listOfFiles.length; i++) {
            String remoteFilePath;
            File localFile = new File(dir,listOfFiles[i]);
            if (localFile.getAbsolutePath().indexOf("vdisk") != -1) {
                remoteFilePath =
                        "/" + vmName + "/" + datastore + "/" + listOfFiles[i];
            } else {
                remoteFilePath = "/" + vmName + "/" + listOfFiles[i];
            }
            putVMFiles(remoteFilePath, localFile);
            if (verbose) {
                System.out.print("*");
            }
        }
        System.out.println("...Done");
    }

    /**
     * register the vmx (virtual machine file) we just placed.
     *
     * @return
     * @throws RuntimeFaultFaultMsg
     * @throws InvalidPropertyFaultMsg
     * @throws OutOfBoundsFaultMsg
     * @throws DuplicateNameFaultMsg
     * @throws NotFoundFaultMsg
     * @throws VmConfigFaultFaultMsg
     * @throws InsufficientResourcesFaultFaultMsg
     *
     * @throws AlreadyExistsFaultMsg
     * @throws InvalidDatastoreFaultMsg
     * @throws FileFaultFaultMsg
     * @throws InvalidStateFaultMsg
     * @throws InvalidNameFaultMsg
     * @throws InvalidCollectorVersionFaultMsg
     *
     */
    boolean registerVirtualMachine() throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg, OutOfBoundsFaultMsg, DuplicateNameFaultMsg, NotFoundFaultMsg, VmConfigFaultFaultMsg, InsufficientResourcesFaultFaultMsg, AlreadyExistsFaultMsg, InvalidDatastoreFaultMsg, FileFaultFaultMsg, InvalidStateFaultMsg, InvalidNameFaultMsg, InvalidCollectorVersionFaultMsg {
        boolean registered = false;
        System.out.print("Registering The Virtual Machine ...");
        ManagedObjectReference hostmor = null;
        // Get the Datacenter
        final ManagedObjectReference dcmor =
                getMOREFs.inContainerByType(serviceContent.getRootFolder(),
                        "Datacenter").get(datacenter);

        // Get the Datastore
        final ManagedObjectReference dsmor =
                getMOREFs.inContainerByType(dcmor, "Datastore").get(datastore);

        final List<DatastoreHostMount> hostmounts =
                ((ArrayOfDatastoreHostMount) getMOREFs.entityProps(dsmor,
                        new String[]{"host"}).get("host")).getDatastoreHostMount();

        for (DatastoreHostMount datastoreHostMount : hostmounts) {
            if (datastoreHostMount == null) {
                throw new PutVMFilesException("datastore " + datastore + " has no host mounts!");
            }
            HostMountInfo mountInfo = datastoreHostMount.getMountInfo();
            if (mountInfo == null) {
                throw new PutVMFilesException("datastoreHostMount on " + datastore + " has no info!");
            }

            final Boolean accessible = mountInfo.isAccessible();
            // the values "accessible" and "mounted" need not be set by the server.
            final Boolean mounted = mountInfo.isMounted();
            // if mounted is not set, assume it is true
            if ((accessible != null && accessible) && (mounted == null || mounted)) {
                hostmor = datastoreHostMount.getKey();
                break;
            }
            if (verbose) {
                System.out.print(".");
            }
        }
        if (hostmor == null) {
            throw new PutVMFilesException("No host connected to the datastore "
                    + datastore);
        }

        final ManagedObjectReference crmor =
                (ManagedObjectReference) getMOREFs.entityProps(hostmor,
                        new String[]{"parent"}).get("parent");

        final ManagedObjectReference resourcePoolRef =
                (ManagedObjectReference) getMOREFs.entityProps(crmor,
                        new String[]{"resourcePool"}).get("resourcePool");

        final ManagedObjectReference vmFolderMor =
                (ManagedObjectReference) getMOREFs.entityProps(dcmor,
                        new String[]{"vmFolder"}).get("vmFolder");

        // Get The vmx path
        final String vmxPath = "[" + datastore + "] " + vmName + "/" + vmName + ".vmx";

        System.out.printf("...trying to register: %s ...", vmxPath);
        // Registering The Virtual machine
        final ManagedObjectReference taskmor =
                vimPort.registerVMTask(vmFolderMor, vmxPath, vmName, false,
                        resourcePoolRef, hostmor);

        if (getTaskResultAfterDone(taskmor)) {
            System.out.print("*");
            registered = true;
            registeredVMRef =
                    (ManagedObjectReference) getMOREFs.entityProps(taskmor,
                            new String[]{"info.result"}).get("info.result");
            System.out.print("VM registered with value "
                    + registeredVMRef.getValue());
            System.out.println("...Done.");
        } else {
            System.out.print("Some Exception While Registering The VM");
            registered = false;
            System.out.println(" FAILED!");
        }

        return registered;
    }

    /**
     * Reconfigure the virtual machine we placed on the datastore
     *
     * @throws RuntimeFaultFaultMsg
     * @throws InvalidPropertyFaultMsg
     * @throws InvalidCollectorVersionFaultMsg
     *
     * @throws DuplicateNameFaultMsg
     * @throws TaskInProgressFaultMsg
     * @throws VmConfigFaultFaultMsg
     * @throws InsufficientResourcesFaultFaultMsg
     *
     * @throws InvalidDatastoreFaultMsg
     * @throws FileFaultFaultMsg
     * @throws ConcurrentAccessFaultMsg
     * @throws InvalidStateFaultMsg
     * @throws InvalidNameFaultMsg
     */
    void reconfigVirtualMachine() {
        try {
            System.out.println("ReConfigure The Virtual Machine ..........");
            VirtualMachineFileInfo vmFileInfo = new VirtualMachineFileInfo();
            vmFileInfo.setLogDirectory("[" + datastore + "] " + vmName);
            vmFileInfo.setSnapshotDirectory("[" + datastore + "] " + vmName);
            vmFileInfo.setSuspendDirectory("[" + datastore + "] " + vmName);
            vmFileInfo.setVmPathName("[" + datastore + "] " + vmName + "/" + vmName
                    + ".vmx");

            VirtualMachineConfigSpec vmConfigSpec = new VirtualMachineConfigSpec();
            vmConfigSpec.setFiles(vmFileInfo);

            ManagedObjectReference taskmor =
                    vimPort.reconfigVMTask(registeredVMRef, vmConfigSpec);

            if (getTaskResultAfterDone(taskmor)) {
                System.out.println("ReConfigure The Virtual Machine .......... Done");
            } else {
                System.out.println("Some Exception While Reconfiguring The VM ");
            }
        } catch (Exception e) {
            throw new PutVMFilesException(e);
        }
    }

    /**
     * Put files onto remote datastore
     *
     * @throws DuplicateNameFaultMsg
     * @throws RuntimeFaultFaultMsg
     * @throws TaskInProgressFaultMsg
     * @throws InsufficientResourcesFaultFaultMsg
     *
     * @throws VmConfigFaultFaultMsg
     * @throws InvalidDatastoreFaultMsg
     * @throws InvalidPropertyFaultMsg
     * @throws FileFaultFaultMsg
     * @throws ConcurrentAccessFaultMsg
     * @throws InvalidStateFaultMsg
     * @throws InvalidCollectorVersionFaultMsg
     *
     * @throws InvalidNameFaultMsg
     * @throws OutOfBoundsFaultMsg
     * @throws NotFoundFaultMsg
     * @throws AlreadyExistsFaultMsg
     * @throws IOException
     */
    void putVMFiles() throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        boolean validated = customValidation();

        if (getMOREFs.inContainerByType(serviceContent.getRootFolder(), "VirtualMachine").containsKey(vmName)) {
            throw new PutVMFilesException(String.format("A VM with the name %s already exists!", vmName));
        }

        if (validated) {
            int found = 0;
            String[] listOfDir = getDirFiles(new File(localPath));
            if (listOfDir != null && listOfDir.length != 0) {
                // Dumping All The Data
                for (int i = 0; i < listOfDir.length; i++) {
                    if (!validateDir(listOfDir[i], localPath)) {
                        continue;
                    }

                    // made it here, we found something to upload
                    found++;

                    // go ahead and copy this up to the server
                    copyDir(listOfDir[i]);

                    // Register The Virtual Machine
                    boolean reconFlag = false;
                    try {
                        reconFlag = registerVirtualMachine();
                        //Reconfigure the disks
                        if (reconFlag) {
                            reconfigVirtualMachine();
                        }
                    } catch (Exception e) {
                        throw new PutVMFilesException(e);
                    }
                }
            }
            if (found == 0) {
                System.out.printf(
                        "There are no suitable VM Directories available at location %s " +
                                "did you use GetVMFiles first?",
                        this.localPath
                );
                System.out.println();
            }
        }
    }

    /**
     * Checks a directory name against rules.
     *
     *
     * @param directoryName - directory to examine
     * @param localPath
     * @return true if usable, false if not
     */
    boolean validateDir(final String directoryName, final String localPath) {
        // short-circut this method if no name set
        if (directoryName == null) {
            return false;
        }

        // using data-structure to avoid repeated calls
        int message = 0;
        final String[] messages = {
                "",
                String.format("The directory %s does not contain a matching %s.vmx file to register.%n",
                        directoryName, vmName),
                String.format("Skipping: %s is a hidden name", directoryName),
                String.format("Skipping: %s is not a directory.", directoryName),
                String.format("Skipping: Name %s does not contain the --vmname %s", directoryName, vmName),
        };

        message = (!new File(new File(localPath,directoryName), String.format("%s.vmx", vmName)).exists()) ? 1 : message;
        message = (directoryName.startsWith(".")) ? 2 : message;
        message = (!new File(localPath,directoryName).isDirectory()) ? 3 : message;
        message = (!directoryName.contains(vmName)) ? 4 : message;

        if (verbose) {
            System.out.println(messages[message]);
        }

        return message == 0;
    }

    /**
     * main method
     *
     * @throws RuntimeFaultFaultMsg
     * @throws InvalidPropertyFaultMsg
     */
    @Action
    public void run() throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        try {
            putVMFiles();
        } catch (PutVMFilesException cme) {
            System.out.println(cme.getMessage());
        }
    }

    /**
     * For exceptions thrown internal to this sample only. Specifically for internal error handling.
     */
    class PutVMFilesException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public PutVMFilesException(final String message) {
            super(message);
        }

        public PutVMFilesException(final Throwable throwable) {
            super(throwable);
        }
    }
}
