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
 * Based on the a script in William Lam's VirtuallyGhetto blog.  More info:
 * https://github.com/lamw/vghetto-scripts/blob/master/perl/getNumofvCPUInCluster.pl
 * http://www.virtuallyghetto.com/
 */

package com.vmware.sample;

import com.vmware.utils.VMwareConnection;
import com.vmware.vim25.DynamicProperty;

import java.util.*;
import java.util.Map.Entry;

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
 * Retrieves a list of cluster ordered by the number of CPUs
 *
 * <p>
 * This is done in a four step process:
 * <ol>
 * <li>Connect to a vCenter</li>
 * <li>Retrieve number of cpu per cluster</li>
 * <li>Sort clusters by number of cpu (descendent)</li>
 * <li>Display output on the console</li>
 * </ol>
 */
public class GetNumOfCpuPerCluster {

    /**
     * Retrieves a map where each pair is (cluster name, number of CPUs)
     *
     * @param conn
     *            the connection with vCenter
     * @return the map with cluster name and number of CPUs
     * @throws Exception
     *             if the system has zero clusters
     */
    public static Map<String, Short> retrieveNumberOfCpuPerCluster(
            com.vmware.utils.VMwareConnection conn) throws Exception {
        Map<String, Short> resultList = new HashMap<String, Short>();
        List<com.vmware.vim25.ObjectContent> clusterList = conn.findAllObjects(
                "ClusterComputeResource", "summary", "name");
        if (clusterList == null) {
            throw new Exception("Unable to find any clusters in this system");
        }
        for (com.vmware.vim25.ObjectContent cluster : clusterList) {
            String clusterName = "";
            short cpu = 0;
            List<DynamicProperty> propSet = cluster.getPropSet();
            for (DynamicProperty dynamicProperty : propSet) {
                String propertyName = dynamicProperty.getName();
                if ("name".equals(propertyName)) {
                    clusterName = (String) dynamicProperty.getVal();
                } else if ("summary".equals(propertyName)) {
                    com.vmware.vim25.ComputeResourceSummary summary = (com.vmware.vim25.ComputeResourceSummary) dynamicProperty
                            .getVal();
                    cpu = summary.getNumCpuCores();
                }
            }
            resultList.put(clusterName, new Short(cpu));
        }
        return resultList;
    }

    /**
     * Runs the GetNumOfCpuPerCluster sample code, which retrieves a list of cluster ordered by the
     * number of CPUs
     *
     * <p>
     * Run with a command similar to this:<br>
     * <code>java -cp vim25.jar com.vmware.general.GetNumOfCpuPerCluster <ip_or_name> <user> <password></code><br>
     * <code>java -cp vim25.jar com.vmware.general.GetNumOfCpuPerCluster 10.20.30.40 JoeUser JoePasswd</code>
     *
     * @param args
     *            the ip_or_name, user, and password
     * @throws Exception
     *             if an exception occurred
     */
    public static void main(String[] args) throws Exception {

        // handle input info
        String serverName = "10.200.6.92:443";
        String userName = "administrator@vsphere.local";
        String password = "Wb1234==";

        com.vmware.utils.VMwareConnection conn = null;
        try {
            // Step-1 Create a connection to vCenter, using the name, user, and password
            conn = new VMwareConnection(serverName, userName, password);

            // Step-2. Retrieve number of cpu per cluster
            Map<String, Short> clusterMap = retrieveNumberOfCpuPerCluster(conn);

            // Step-3. Sort clusters by number of cpu (descendent)
            List<Entry<String, Short>> clusterList = entriesSortedByValues(clusterMap);

            // Step-4. Display output on the console
            displayNumberOfCpuPerCluster(clusterList);
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
     * Sorts the map in a descending order
     *
     * @param map
     *            the map to be sorted
     * @return the list of <entry,value> ordered by value
     */
    private static <K, V extends Comparable<? super V>> List<Entry<K, V>> entriesSortedByValues(
            Map<K, V> map) {

        List<Entry<K, V>> sortedEntries = new ArrayList<Entry<K, V>>(map.entrySet());

        Collections.sort(sortedEntries, new Comparator<Entry<K, V>>() {
            @Override
            public int compare(Entry<K, V> e1, Entry<K, V> e2) {
                return e2.getValue().compareTo(e1.getValue());
            }
        });

        return sortedEntries;
    }

    /**
     * Displays the number of CPUs per cluster
     *
     * @param clusterList
     *            a list of virtual disk
     */
    private static void displayNumberOfCpuPerCluster(List<Entry<String, Short>> clusterList) {
        System.out.println("Here are the list of clusters ordered by number of CPUs:");
        for (Entry<String, Short> clusterEntry : clusterList) {
            System.out.printf("%s - Num of Cpu: %d%n", clusterEntry.getKey(),
                    clusterEntry.getValue());
        }
    }

}
