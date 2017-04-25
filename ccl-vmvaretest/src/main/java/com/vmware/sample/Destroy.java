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

import javax.xml.ws.BindingProvider;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
 * Destroys vCenter object, usually a VirtualMachine, but more generally this class shows how to run
 * a task on a specific object.
 */
public class Destroy {

    /**
     * Destroys vCenter object, usually a VirtualMachine. This code also works with these types:
     * Datacenter, Folder, HostSystem, ResourcePool, ComputeResource, and ClusterComputeResource.
     * <p>Three step process:
     * <ol>
     * <li>Get the Managed Object Reference for the object you want to destroy.
     * <li>Send the Destroy Task.
     * <li>Wait for the task to complete, and it's results.
     * </ol>
     * <br>Run with a command similar to this:<br>
     * <code>java -cp vim25.jar com.vmware.general.Destroy <i>ip_or_name</i> <i>user</i> <i>password</i> <i>type</i> <i>name_of_object</i></code><br>
     * <code>java -cp vim25.jar com.vmware.general.Destroy 10.20.30.40 JoeUser JoePasswd VirtualMachine vm1</code>
     * <br>More details in the Destroy_ReadMe.txt file.
     *
     * @param args
     *            the ip_or_name, user, password, and objectType
     * @throws Exception
     *             if an exception occurred
     */
    public static void main(String[] args) throws Exception {

        // arglist variables
        String serverName = "10.200.6.92:443";
        String userName = "administrator@vsphere.local";
        String password = "Wb1234==";
        String objectType = "VirtualMachine";
        String name = "sf1";
        // This is the URL to connect to a vCenter server
        String url = "https://" + serverName + "/sdk/vimService";

        // We can only destroy these types of objects, so check that we got this type.
        List<String> supportedTypes = Arrays.asList("VirtualMachine", "Datacenter", "Folder",
                "HostSystem", "ResourcePool", "ComputeResource", "ClusterComputeResource");

        if (!supportedTypes.contains(objectType)) {
            System.err.printf("Bad type passed in.  Type %s can not be Destroyed using Destroy_Task.%n", args[3]);
            System.exit(1);
        }

        // Variables of the following types for access to the API methods
        // and to the vSphere inventory.
        // -- ManagedObjectReference for the ServiceInstance on the Server
        // -- VimService for access to the vSphere Web service
        // -- VimPortType for access to methods
        // -- ServiceContent for access to managed object services
        com.vmware.vim25.ManagedObjectReference serviceInstanceRef = new ManagedObjectReference();
        com.vmware.vim25.VimService vimService;
        com.vmware.vim25.VimPortType vimPort;
        com.vmware.vim25.ServiceContent serviceContent;

        // This sets up trust management for examples, only. Do not use this code in production.
        com.vmware.utils.FakeTrustManager.setupTrust();

        // Set up the manufactured managed object reference for the ServiceInstance
        serviceInstanceRef.setType("ServiceInstance");
        serviceInstanceRef.setValue("ServiceInstance");

        // Create a VimService object to obtain a VimPort binding provider.
        // The BindingProvider provides access to the protocol fields
        // in request/response messages. Retrieve the request context
        // which will be used for processing message requests.
        vimService = new com.vmware.vim25.VimService();
        vimPort = vimService.getVimPort();
        Map<String, Object> ctxt = ((BindingProvider) vimPort).getRequestContext();

        // Store the Server URL in the request context and specify true
        // to maintain the connection between the client and server.
        // The client API will include the Server's HTTP cookie in its
        // requests to maintain the session. If you do not set this to true,
        // the Server will start a new session with each request.
        ctxt.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url);
        ctxt.put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);
        // Retrieve the ServiceContent object and login
        serviceContent = vimPort.retrieveServiceContent(serviceInstanceRef);
        vimPort.login(serviceContent.getSessionManager(), userName, password, null);
        // Find the object to delete
        ManagedObjectReference mor = com.vmware.utils.FindObjects.findObject(vimPort,
                serviceContent, objectType, name);
        if (mor == null) {
            System.out.printf("Did not find a %s named %s, so nothing was deleted.", objectType, name);
        } else {
            // Some actions are "tasks" which are asynchronous, others are "methods which are
            // synchronous.
            // Since deleting is a task, the Java method below will return a Managed Object
            // Reference which
            // can be used later to query the result of the action.
            ManagedObjectReference taskRef = vimPort.destroyTask(mor);
            // This function waits for the asyncronous "delete" task to complete, and returns a
            // status.
            if (com.vmware.utils.TaskUtils.getTaskResultAfterDone(vimPort, taskRef)) {
                System.out.println("Success");
            } else {
                System.err.printf("Failure destoying [ %s ] ", name);
                System.exit(1);
            }
        }

        // close the connection
        vimPort.logout(serviceContent.getSessionManager());
    }
}
