/*
 * ******************************************************
 * Copyright VMware, Inc. 2014. All Rights Reserved.
 * ******************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.vmware.sample;

import com.vmware.utils.VMwareConnection;
import com.vmware.vim25.*;

import java.util.ArrayList;
import java.util.List;

/*
 * Coding Conventions Used Here:
 * 1. The VMwareConnection class (also in sample code) to set up and manage
 *    the connection to vCenter.  All classes that connect to vCenter take a VMwareConnection
 *    object as an argument, and use it.
 * 2. Methods all throw only RuntimeExceptions which means that the exceptions are ignored
 *    and printed out at the call stack.  If used in real development, exceptions should be caught
 *    and recovered from.
 *
 * Also: all classes found in sample code are declared with full path names, so it is obvious where
 * their source code.  For example "com.vmware.utils.VMwareConnection conn" rather than
 * "VMwareConnection conn".
 */

/**
 * Clones a VirtualMachine many times, and then make the same resource change to each new VM.
 * <p>
 * This is done in a four step process:
 * <ol>
 * <li>Get the original VM.
 * <li>Clone it to new VMs repeatedly.
 * <li>Make any desirable updates to each of the new VMs.
 * <li>Power on all the new VMs.
 * </ol>
 */
public class CloneAndChangeVM {

    /**
     * Find the VM which will be the starting point of all the cloning.
     *
     * @param conn
     *            the previously opened and logged in vCenter connection to use
     * @param vmName
     *            the name of a VirtualMachine.
     *
     * @return the starting point VM
     */
    public ManagedObjectReference getVmRef(com.vmware.utils.VMwareConnection conn,
                                           String vmName) {

        ObjectContent vm = null;
        try {
            vm = conn.findObject("VirtualMachine", vmName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (vm == null) {
            System.out.printf("No VirtualMachine named %s was found.%n", vmName);
            System.exit(0);
        }

        //
        return vm.getObj();
    }

    /**
     * Make several clones of a virtual machine.
     *
     * @param conn
     *            The previously opened and logged in vCenter connection to use
     * @param vmRef
     *            The virtual machine to be cloned
     * @param name
     *            The base name for each of the new clones. The actual name will be nameN, where
     *            name is here, and N is the number of the clone
     * @param count
     *            The number of clones to create
     */
    public List<ManagedObjectReference> cloneVM(com.vmware.utils.VMwareConnection conn,
                                                ManagedObjectReference vmRef, String name, int count) {

        ManagedObjectReference cloneTask = null;
        List<ManagedObjectReference> newClones = new ArrayList<ManagedObjectReference>();

        // After you have created the clone managed object, create a clone specification.
        // Use default values whenever possible.
        com.vmware.vim25.VirtualMachineCloneSpec cloneSpec = new VirtualMachineCloneSpec();
        com.vmware.vim25.VirtualMachineRelocateSpec vmrs = new VirtualMachineRelocateSpec();
        cloneSpec.setLocation(vmrs);
        // cloneSpec.setPowerOn(true);
        cloneSpec.setTemplate(false);

        // Get the destination folder for the clone virtual machines (VirtualMachine.parent).
        // The clones will be created in the same folder that contains the specified virtual
        // machine (vmName).
        ManagedObjectReference folder = conn.getRelatedRef(vmRef, "VirtualMachine", "parent");

        // Create two clone virtual machines.
        try {
            for (int cloneNum = 1; cloneNum <= count; cloneNum++) {
                String newName = name + cloneNum;
                System.out.printf("Cloning VM %s%n", newName);
                cloneTask = conn.getVimPort().cloneVMTask(vmRef, folder, newName, cloneSpec);
                com.vmware.utils.TaskUtils
                        .getTaskResultAfterDone(conn.getVimPort(), cloneTask);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        for (int cloneNum = 1; cloneNum <= count; cloneNum++) {
            String newName = name + cloneNum;
            ObjectContent newClone = null;
            try {
                newClone = conn.findObject("VirtualMachine", newName);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            newClones.add(newClone.getObj());
        }
        return newClones;
    }

    /**
     * Updates all the virtual machines in the list passed in.
     *
     * @param conn
     *            the previously opened and logged in vCenter connection to use
     */
    public void updateClones(com.vmware.utils.VMwareConnection conn,
                             List<ManagedObjectReference> newVmRefs) {

        // First set up the spec for what we want to change.
        // Normally, you would read in this data from a config file, database, or the command
        // line, but for demo purposes, these are just hard coded.

        com.vmware.vim25.LatencySensitivity latencySensitivity = new LatencySensitivity();
        latencySensitivity.setLevel(LatencySensitivitySensitivityLevel.HIGH);

        com.vmware.vim25.VirtualMachineConfigSpec virtualMachineConfigSpec = new VirtualMachineConfigSpec();
        virtualMachineConfigSpec.setCpuHotAddEnabled(true);
        virtualMachineConfigSpec.setCpuHotRemoveEnabled(true);
        virtualMachineConfigSpec.setLatencySensitivity(latencySensitivity);

        // And then apply it to each new clone.
        try {
            for (ManagedObjectReference newVmRef : newVmRefs) {
                System.out.printf("Updating new VM %s%n", newVmRef.getValue());
                ManagedObjectReference updateTask = conn.getVimPort().reconfigVMTask(newVmRef,
                        virtualMachineConfigSpec);
                com.vmware.utils.TaskUtils.getTaskResultAfterDone(conn.getVimPort(),
                        updateTask);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Power on a list of virtual machines. (There is a task to power on many VMs at once, but that
     * is not used here.)
     *
     * @param conn
     *            The previously opened and logged in vCenter connection to use
     */
    public void powerOn(com.vmware.utils.VMwareConnection conn,
                        List<ManagedObjectReference> newVmRefs) {

        for (ManagedObjectReference newVmRef : newVmRefs) {
            System.out.printf("Powering up new VM %s%n", newVmRef.getValue());
            try {
                ManagedObjectReference powerTask = conn.getVimPort().powerOnVMTask(newVmRef, null);
                com.vmware.utils.TaskUtils
                        .getTaskResultAfterDone(conn.getVimPort(), powerTask);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Runs the CloneAndChangeVM sample code, which shows how to take a VM and clone it several
     * times.
     * <p>
     * Run with a command similar to this:<br>
     * <code>java -cp vim25.jar com.vmware.complete.CloneAndChangeVM <i>ip_or_name</i> <i>user</i> <i>password</i> <i>vm_name</i> <i>count</i> <i>newName</i></code>
     * <br>
     * <code>java -cp vim25.jar com.vmware.complete.CloneAndChangeVM 10.20.30.40 JoeUser JoePasswd baseDbServer 3 dbServer</code>
     * <br>
     * More details in the CloneAndChangeVM_ReadMe.txt file.
     */
    public static void main(String[] args) throws Exception {

        // arglist variables
        String serverName = "10.200.6.92:443";
        String userName = "administrator@vsphere.local";
        String password = "Wb1234==";
        String virtualMachineName = "sdf";
        String newName = "sf";
        int numberOfNewVms = 1;

        // Create a connection to vCenter, using the name, user, and password passed in.
        com.vmware.utils.VMwareConnection conn = new VMwareConnection(serverName, userName,
                password);
        try {
            // Create the engine to do the work.
            CloneAndChangeVM engine = new CloneAndChangeVM();

            // Find the VM that is going to be cloned
            ManagedObjectReference vmRef = engine.getVmRef(conn, virtualMachineName);

            // Create the new VMs, by cloning the first one.
            List<ManagedObjectReference> newVmRefs = engine.cloneVM(conn, vmRef, newName,
                    numberOfNewVms);

            // Update each new clone so it is setup the way desired.
            engine.updateClones(conn, newVmRefs);

            // Turn on all the new VMs.
            engine.powerOn(conn, newVmRefs);
        } catch (RuntimeException re) {
            // In this sample, everything becomes a RuntimeException, and is printed here.
            re.printStackTrace();
        } finally {
            // Need to be sure to close the connection in the event of an error.
            conn.close();
        }
    }
}