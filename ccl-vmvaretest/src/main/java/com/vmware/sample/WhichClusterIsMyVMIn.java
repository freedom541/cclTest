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
 * Originally from a perl script in William Lam's VirtuallyGhetto blog.  More info:
 * http://communities.vmware.com/docs/DOC-10439
 * http://www.virtuallyghetto.com/
 */

package com.vmware.sample;

import com.vmware.utils.ObjectUtils;
import com.vmware.utils.VMwareConnection;
import com.vmware.vim25.ArrayOfManagedObjectReference;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.ObjectContent;

import java.util.List;

/*
 * Coding Conventions Used Here:
 * 1. The VMwareConnection class (also in sample code) to set up and manage
 *    the connection to vCenter.  All classes that connect to vCenter take a VMwareConnection
 *    object as an argument, and use it.
 * 2. Many methods are listed as "throws Exception" which means that the exceptions are ignored
 *    and printed out at the call stack.  If used in real development, exceptions should be caught
 *    and recovered from.
 *
 * Also: Full path names are used for all java classes when they are first used (for declarations
 * or to call static methods).  This makes it easier to find their source code, so you can understand
 * it.  For example "com.vmware.utils.VMwareConnection conn" rather than "VMwareConnection conn".
 */

/**
 * Finds the cluster that a virtual machine belongs to.
 */
public class WhichClusterIsMyVMIn {

    /**
     * Returns the cluster name that a virtual machine (vm) belongs to.
     * <p>
     * Four steps process:
     * <ol>
     * <li>Get the vm.
     * <li>Get the host from that vm.
     * <li>Get all the clusters.
     * <li>See which cluster has a host which matches the one from the vm.
     * </ol>
     *
     * @param conn
     *            the connection with vCenter
     * @param vmName
     *            the name of a virtual machine.
     * @throws Exception
     *             if the virtual machine was not found, or if the host information was not found,
     *             or if the clusters were not found
     */
    public static String vm2cluster(com.vmware.utils.VMwareConnection conn, String vmName)
            throws Exception {

        // Step 1: Get an ObjectContents which has the Managed Object Reference for the virtual
        // machine
        // we are about, and also the host for that vm. The host is found under the runtime
        // structure.
        com.vmware.vim25.ObjectContent vm = conn.findObject("VirtualMachine", vmName, "runtime.host");

        if (vm == null) {
            throw new Exception("No VirtualMachine named " + vmName + " was found");
        }

        // Step 2: Get the ManagedObjectReference from the OC we just got.
        com.vmware.vim25.ManagedObjectReference myHost = (ManagedObjectReference) ObjectUtils.getPropertyObject(vm,
                "runtime.host");

        if (myHost == null) {
            throw new Exception("No host information was found for VM " + vmName);
        }

        // This is just troubleshooting output. Uncomment it if you need it.
        // com.vmware.utils.ObjectUtils.printMOR("host looked for",myHost);

        // Step 3: Get a list all the clusters that vCenter knows about, and for each one, also get
        // the host
        // data structure for it.
        List<ObjectContent> clusters =
                conn.findAllObjects("ClusterComputeResource", "host", "name");
        if (clusters == null) {
            throw new Exception("No clusters were found, so this VM can not be part of a cluster.");
        }

        // Step 4: Loop through all clusters that exist (which we got in step 3), and loop through
        // each host
        // of that cluster, and see if that host matches the host we got in step 2 as the host of
        // the vm.
        // If we find it, return it, otherwise we return null.
        for (ObjectContent cluster : clusters) {
            // Another troubleshooting output line. Uncomment if you want it.
            // com.vmware.utils.ObjectUtils.printOC("clusters",cluster);

            // Remember the name of the current cluster, so if we find the host here, we use this
            // name.
            String clusterName = (String) ObjectUtils.getPropertyObject(cluster, "name");

            // Get the list (actually an ArrayOfManagedObjectReference) of hosts in this cluster
            // We specified this data in step 3 when we got the cluster from the vCenter server,
            // which is why it is
            // waiting for us here.
            com.vmware.vim25.ArrayOfManagedObjectReference hosts =
                    (ArrayOfManagedObjectReference) ObjectUtils.getPropertyObject(cluster, "host");

            // convert the Java Object hosts to an actual Java array called arrayOfHosts
            List<ManagedObjectReference> listOfHosts = hosts.getManagedObjectReference();

            for (ManagedObjectReference host : listOfHosts) {
                if (host.getValue().equals(myHost.getValue())) {
                    return clusterName;
                }
            }
        }

        return null;
    }

    /**
     * Runs the GetAndAcknowledgeDatacenterAlarms sample code, which takes a VM's name and prints
     * out the cluster it belongs to.
     *
     * <p>
     * Run with a command similar to this:<br>
     * <code>java -cp vim25.jar;Example.jar com.vmware.complete.WhichClusterIsMyVMIn <ip_or_name> <user> <password> <vm_name></code><br>
     * <code>java -cp vim25.jar;Example.jar com.vmware.complete.WhichClusterIsMyVMIn 10.20.30.40 JoeUser JoePassword myVM</code>
     *
     * @param args
     *            the ip_or_name, user, password, and virtual machine name
     * @throws Exception
     *            if an exception occurred
     */
    public static void main(String[] args) throws Exception {

        // arglist variables
        String serverName = "10.200.6.92:443";
        String userName = "administrator@vsphere.local";
        String password = "Wb1234==";
        String virtualMachineName = "ppt";

        com.vmware.utils.VMwareConnection conn = null;
        try {
            // Create a connection to vCenter, using the name, user, and password passed in.
            conn = new VMwareConnection(serverName, userName, password);

            // Do the lookup of the cluster for this VM and print out the results
            String clusterName = WhichClusterIsMyVMIn.vm2cluster(conn, virtualMachineName);
            System.out.printf("VM %s is in cluster %s.%n", virtualMachineName, clusterName);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // close connection to vCenter
            if (conn != null) {
                conn.close();
            }
        }
    }
}