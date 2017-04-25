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

import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.TaskInfoState;

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
 * Retrieves support bundle URL from a vCenter or ESX host
 *
 * <p>
 * This is done in a three step process:
 * <ol>
 * <li>Connect to a vCenter</li>
 * <li>Retrieve support bundle URL</li>
 * <li>Display output on the console</li>
 * </ol>
 */
public class GetSupportBundleUrlFromVcOrEsx {

    /**
     * Retrieves support bundle URL
     *
     * <p>
     * Four steps process:
     * <ol>
     * <li>Create the task that will generate the support bundle</li>
     * <li>Wait for the Task to finish</li>
     * <li>Check the task completion state (success or error)</li>
     * <li>Get the task result (bundle URL)</li>
     * </ol>
     *
     * @param conn
     *            the connection with vCenter
     * @param esxHostRef
     *            the object representing a ESX host, null treated as a command to retrieve the
     *            vCenter support bundle
     * @return the support bundle URL
     * @throws Exception
     *             if it is not able to retrieve support bundle URL
     */
    private static String retrieveBundle(com.vmware.utils.VMwareConnection conn,
                                         com.vmware.vim25.ManagedObjectReference esxHostRef) throws Exception {
        String urlResult = "";
        com.vmware.vim25.ServiceContent serviceContent = conn.getServiceContent();
        ManagedObjectReference dia = serviceContent.getDiagnosticManager();
        com.vmware.vim25.VimPortType vimPort = conn.getVimPort();

        // 1. Create the task that will generate the support bundle
        ManagedObjectReference taskRef;
        if (esxHostRef != null) {
            // there is a request to retrieve ESX bundle
            ArrayList<ManagedObjectReference> listMoRef = new ArrayList<ManagedObjectReference>();
            listMoRef.add(esxHostRef);
            taskRef = vimPort.generateLogBundlesTask(dia, false, listMoRef);
        } else {
            // there is a request to retrieve vCenter bundle
            taskRef = vimPort.generateLogBundlesTask(dia, true, null);
        }

        // 2. Wait for the Task to finish
        System.out
                .printf("Generating the support bundle...  (depending on the bundle size, it may take several minutes)%n");
        Object[] objectList = com.vmware.utils.TaskUtils.wait(vimPort, taskRef, new String[] {
                        "info.state", "info.error", "info.result" }, new String[] { "state" },
                new Object[][] { new Object[] { TaskInfoState.SUCCESS, TaskInfoState.ERROR } });

        for (Object object : objectList) {
            if (object instanceof com.vmware.vim25.TaskInfoState) {
                // 3. Check the task completion state (success or error)
                TaskInfoState state = (TaskInfoState) object;
                if (TaskInfoState.SUCCESS.equals(state)) {
                    System.out.printf("Success in retrieving the support bundle%n");
                } else if (TaskInfoState.ERROR.equals(state)) {
                    throw new Exception("Unable to retrieve the support bundle");
                }
            } else if (object instanceof com.vmware.vim25.ArrayOfDiagnosticManagerBundleInfo) {
                // 4. Get the task result (bundle URL)
                com.vmware.vim25.ArrayOfDiagnosticManagerBundleInfo arrayBundle = (com.vmware.vim25.ArrayOfDiagnosticManagerBundleInfo) object;
                List<com.vmware.vim25.DiagnosticManagerBundleInfo> listBundle = arrayBundle
                        .getDiagnosticManagerBundleInfo();
                com.vmware.vim25.DiagnosticManagerBundleInfo bundleInfo = listBundle.get(0);
                urlResult = bundleInfo.getUrl();
            }
        }
        return urlResult;
    }

    /**
     * Retrieves support bundle URL from vCenter
     *
     * @param conn
     *            the connection with vCenter
     * @param vcIP
     *            the IP address of vCenter
     * @return URL the support bundle URL
     * @throws Exception
     *             if an exception occurred
     */
    public static String retrieveBundleFromVC(com.vmware.utils.VMwareConnection conn,
                                              String vcIP) throws Exception {
        String url = retrieveBundle(conn, null);
        // in some cases the url will miss the ip address (e.g.)
        url = url.replaceAll("\\*", vcIP);
        return url;
    }

    /**
     * Retrieves support bundle URL from ESX host
     *
     * @param conn
     *            the connection with vCenter
     * @param esxHostIP
     *            the IP address of ESX host
     * @return URL the support bundle URL
     * @throws Exception
     *             if an exception occurred
     */
    public static String retrieveBundleFromEsxHost(com.vmware.utils.VMwareConnection conn,
                                                   String esxHostIP) throws Exception {
        com.vmware.vim25.ServiceContent serviceContent = conn.getServiceContent();
        com.vmware.vim25.VimPortType vimPort = conn.getVimPort();
        ManagedObjectReference esxHostRef = vimPort.findByIp(serviceContent.getSearchIndex(), null,
                esxHostIP, false);
        String url = retrieveBundle(conn, esxHostRef);
        // in some cases the url will miss the ip address
        url = url.replaceAll("\\*", esxHostIP);
        return url;
    }

    /**
     * Runs the GetSupportBundleUrlFromVcOrEsx sample code, which retrieves support bundle URL from
     * a vCenter or ESX host
     *
     * <p>
     * Run with a command similar to this:
     *
     * <p>
     * 1. To retrieve VC support bundle URL:<br>
     * <code>java -cp vim25.jar com.vmware.general.GetSupportBundleUrlFromVcOrEsx <ip_or_name> <user> <password></code><br>
     * <code>java -cp vim25.jar com.vmware.general.GetSupportBundleUrlFromVcOrEsx 10.20.30.40 JoeUser JoePasswd</code>
     *
     * <p>
     * 2. To Retrieve ESX support bundle URL:<br>
     * <code>java -cp vim25.jar com.vmware.general.GetSupportBundleUrlFromVcOrEsx <ip_or_name> <user> <password> <esxHostIP></code><br>
     * <code>java -cp vim25.jar com.vmware.general.GetSupportBundleUrlFromVcOrEsx 10.20.30.40 JoeUser JoePasswd 10.20.30.41</code>
     *
     * @param args
     *            the ip_or_name, user, password, and optionally ESX IP
     * @throws Exception
     *             if an exception occurred
     */
    public static void main(String[] args) throws Exception {

        // handle required input info
        String serverName = "10.200.6.92:443";
        String userName = "administrator@vsphere.local";
        String password = "Wb1234==";
        String esxHostIP = "10.200.6.81";
        // Step-1 Create a connection to vCenter, using the name, user, and password
        com.vmware.utils.VMwareConnection conn = new com.vmware.utils.VMwareConnection(
                serverName, userName, password);

        // Step-2 Retrieve support bundle URL
        String url = "";
        try {
            if (!"".equals(esxHostIP)) {
                // optional argument. If provided, there is a request to retrieve the ESX bundle
                // instead of VC bundle
                url = retrieveBundleFromEsxHost(conn, esxHostIP);
            } else { // there is a request to retrieve vCenter bundle
                url = retrieveBundleFromVC(conn, serverName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Step-3. Display output on the console
        System.out.printf("You can download the support bundle using the following URL: %s", url);

        // close connection to vCenter
        conn.close();
    }

}
