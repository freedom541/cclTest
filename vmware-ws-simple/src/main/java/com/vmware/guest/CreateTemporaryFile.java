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

import java.util.Map;

/**
 * <pre>
 * CreateTemporaryFile
 *
 * This sample creates a temporary file inside a virtual machine.
 *
 * <b>Parameters:</b>
 * url             [required] : url of the web service
 * username        [required] : username for the authentication
 * password        [required] : password for the authentication
 * vmname          [required] : name of the virtual machine
 * guestusername   [required] : username in the guest
 * guestpassword   [required] : password in the guest
 * prefix          [optional] : prefix to be added to the file name
 * suffix          [optional] : suffix to be added to the file name
 * directorypath   [optional] : path to the directory inside the guest.
 *
 * <b>Command Line:</b>
 * run.bat com.vmware.general.CreateTemporaryFile --url [webserviceurl]
 * --username [username] --password [password] --vmname [vmname]
 * --guestusername [guest user] --guestpassword [guest password]
 * --prefix [prefix] --suffix [suffix] --directorypath [directory path]
 * </pre>
 */
@Sample(name = "create-temp-file", description = "creates a temporary file inside a virtual machine. Since vSphere API 5.0")
public class CreateTemporaryFile extends ConnectedVimServiceBase {
    private VirtualMachinePowerState powerState;
    private GuestConnection guestConnection;

    private String prefix = "";
    private String suffix = "";
    private String directoryPath = "";

    @Option(name = "guestConnection", type = GuestConnection.class)
    public void setGuestConnection(GuestConnection guestConnection) {
        this.guestConnection = guestConnection;
    }

    @Option(name = "prefix", required = false, description = "prefix to be added to the file name")
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @Option(name = "suffix", required = false, description = "suffix to be added to the file name")
    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    @Option(name = "directorypath", required = false, description = "path to the directory inside the guest")
    public void setDirectoryPath(String directoryPath) {
        this.directoryPath = directoryPath;
    }

    @Action
    public void run() throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg, InvalidCollectorVersionFaultMsg, TaskInProgressFaultMsg, FileFaultFaultMsg, InvalidStateFaultMsg, GuestOperationsFaultFaultMsg {
        serviceContent.getPropertyCollector();

        Map<String, ManagedObjectReference> vms =
                getMOREFs.inFolderByType(serviceContent.getRootFolder(),
                        "VirtualMachine");
        ManagedObjectReference vmMOR = vms.get(guestConnection.vmname);
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

        String[] opts = new String[]{"guest.guestOperationsReady"};
        String[] opt = new String[]{"guest.guestOperationsReady"};
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

        System.out.println("Executing CreateTemporaryFile guest operation");
        String result =
                vimPort.createTemporaryFileInGuest(fileManagerRef, vmMOR, auth,
                        prefix, suffix, directoryPath);
        System.out.println("Temporary file was successfully created at: "
                + result + " inside the guest");
        // A Temporary file is created inside the guest. The user can
        // use the file and delete it.
    }
}
