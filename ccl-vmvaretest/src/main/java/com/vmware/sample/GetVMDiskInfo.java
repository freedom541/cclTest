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
 * https://github.com/lamw/vghetto-scripts/blob/master/perl/getVMDiskInfo.pl
 * http://www.virtuallyghetto.com/
 */

package com.vmware.sample;

import java.util.ArrayList;
import java.util.List;

/*
 * Coding Conventions Used Here:
 * 1. The connection to vCenter is managed with in the "main" method of this class.
 * 2. Many methods are listed as "throws Exception" which means that the exceptions are ignored
 *    and printed out at the call stack.  If used in real development, exceptions should be caught
 *    and recovered from.
 * 3. Managed Object Reference variables are named ending with "Ref".
 *
 * Also: Full path names are used for all java classes when they are first used (for declarations
 * or to call static methods).  This makes it easier to find their source code, so you can understand
 * it.  For example "com.vmware.utils.VMwareConnection conn" rather than "VMwareConnection conn".
 */

/**
 * Retrieves the virtual machine's disk properties
 *
 * <p>
 * This is done in a three step process::
 * <ol>
 * <li>Connect to a vCenter</li>
 * <li>Retrieve a list of virtual disk for a given VM(IP address)</li>
 * <li>Display some virtual disk properties</li>
 * </ol>
 */
public class GetVMDiskInfo {

    /**
     * Retrieves virtual disks for the given virtual machine
     *
     * @param conn
     *            the connection with vCenter
     * @param vmIP
     *            the virtual machine IP address
     * @return List of virtual disk associated with the input virtual machine
     * @throws Exception
     *             if unable to find the virtual machine, or if unable to find the virtual machine
     *             device list
     */
    public static List<com.vmware.vim25.VirtualDisk> getVmDiskInfo(
            com.vmware.utils.VMwareConnection conn, String vmIP)
            throws Exception {

        List<com.vmware.vim25.VirtualDisk> resultList = new ArrayList<com.vmware.vim25.VirtualDisk>();
        com.vmware.vim25.VimPortType vimPort = conn.getVimPort();
        com.vmware.vim25.ServiceContent serviceContent = conn.getServiceContent();
        // find a virtual machine by IP address (in all datacenters)
        com.vmware.vim25.ManagedObjectReference vmMoRef = vimPort.findByIp(
                serviceContent.getSearchIndex(), null, vmIP, true);
        if (vmMoRef==null) {
            throw new Exception("Unable to find the virtual machine: " + vmIP);
        }
        // retrieve the list of virtual devices associated with the virtual machine
        com.vmware.vim25.ObjectContent vmContent = conn.findObject(vmMoRef, "config.hardware.device");
        if (vmContent == null) {
            throw new Exception("Unable to find the virtual machine device list");
        }
        List<com.vmware.vim25.DynamicProperty> dps = vmContent.getPropSet();
        for (com.vmware.vim25.DynamicProperty dp : dps) {
            List<com.vmware.vim25.VirtualDevice> deviceList = ((com.vmware.vim25.ArrayOfVirtualDevice) dp
                    .getVal()).getVirtualDevice();
            // go through the list of virtual devices to get the virtual disks
            for (com.vmware.vim25.VirtualDevice device : deviceList) {
                if (device instanceof com.vmware.vim25.VirtualDisk) {
                    com.vmware.vim25.VirtualDisk disk = (com.vmware.vim25.VirtualDisk) device;
                    resultList.add(disk);
                }
            }
        }
        return resultList;
    }

    /**
     * Runs the GetVMDiskInfo sample code, which shows how to retrieves the virtual machine's disk
     * properties
     *
     * <p>
     * Run with a command similar to this: <br>
     * <code>java -cp vim25.jar com.vmware.general.GetVMDiskInfo <ip_or_name> <user> <password> <vm_ip></code><br>
     * <code>java -cp vim25.jar com.vmware.general.GetVMDiskInfo 10.20.30.40 JoeUser 10.20.30.41</code>
     *
     * @param args
     *            the ip_or_name, user, password, and vm IP address
     * @throws Exception
     *             if an exception occurred
     */
    public static void main(String[] args) throws Exception {

        // handle input info
        String serverName = "10.200.6.92:443";
        String userName = "administrator@vsphere.local";
        String password = "Wb1234==";
        String vmIP = "10.200.6.81";

        com.vmware.utils.VMwareConnection conn = null;
        try {
            // Step-1 Create a connection to vCenter, using the name, user, and password
            conn = new com.vmware.utils.VMwareConnection(serverName, userName, password);

            // Step-2 get the list of virtual disk for a given VM
            List<com.vmware.vim25.VirtualDisk> vmDiskList = getVmDiskInfo(conn, vmIP);

            // Step-3 output the virtual disk info
            displayVmDiskInfo(vmIP, vmDiskList);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // close connection to vCenter
            if (conn != null) {
                conn.close();
            }
        }
    }

    /* Utility methods */

    /**
     * Displays the virtual disk properties
     *
     * @param vmIP
     *
     * @param vmDiskList
     *            a list of virtual disk
     */
    private static void displayVmDiskInfo(String vmIP, List<com.vmware.vim25.VirtualDisk> vmDiskList) {
        System.out.printf("Here follows the virtual disks that belongs to the Virtual Machine: %s%n", vmIP);
        for (com.vmware.vim25.VirtualDisk virtualDisk : vmDiskList) {
            com.vmware.vim25.Description deviceInfo = virtualDisk.getDeviceInfo();
            String diskName = deviceInfo.getLabel();
            String capacity = deviceInfo.getSummary();
            System.out.printf("%s - capacity: %s%n", diskName, capacity);
        }
    }

}