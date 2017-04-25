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

import com.vmware.vim25.PropertyFilterSpec;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.ws.BindingProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/*
 * Coding Conventions Used Here:
 * 1. The connection to vCenter is managed from within the ListObjects class.
 * 2. All the code needed to make a connection to vCenter is in this one file.
 *
 * Also: Full path names are used for all java classes when they are first used (for declarations
 * or to call static methods).  This makes it easier to find their source code, so you can understand
 * it.  For example "com.vmware.utils.VMwareConnection conn" rather than "VMwareConnection conn".
 */

/**
 * Connects to a vCenter server, and prints out the name of all the objects of a given
 * type. Usually, this is the VirtualMachine, or the HostSystem objects. It is
 * designed to show the basics of connecting, working with managed objects and object contents.
 */
public class ListObjects {

    /**
     * Collects all objects of a given type (usually Virtualmachine), and prints the property value
     * for each object (for example "name" property).
     *
     * @param vimPort
     *            the opened connection to vCenter/ESX
     * @param serviceContent
     *            the ServiceContent object from that same <code>vimPort</code> connection
     * @param objectType
     *            the type of the objects that will be collected
     * @param property
     *            the name of the property to print out
     * @throws Exception
     *             if an exception occurred
     */
    private static void collectProperties(com.vmware.vim25.VimPortType vimPort,
                                          com.vmware.vim25.ServiceContent serviceContent, String objectType, String property)
            throws Exception {

        // Get references to the ViewManager and PropertyCollector
        com.vmware.vim25.ManagedObjectReference viewMgrRef = serviceContent.getViewManager();
        com.vmware.vim25.ManagedObjectReference propColl = serviceContent.getPropertyCollector();

        // use a container view for virtual machines to define the traversal
        // - invoke the VimPortType method createContainerView (corresponds
        // to the ViewManager method) - pass the ViewManager MOR and
        // the other parameters required for the method invocation
        // (use a List<String> for the type parameter's string[])
        List<String> typeList = new ArrayList<String>();
        typeList.add(objectType);

        com.vmware.vim25.ManagedObjectReference cViewRef = null;
        try {
            cViewRef = vimPort.createContainerView(viewMgrRef, serviceContent.getRootFolder(),
                    typeList, true);
        } catch (javax.xml.ws.soap.SOAPFaultException soapException) {
            System.out.printf("Caught an SOAPFaultException.  That often means that the type you passed in (%s) was not a valid type.%n",
                    objectType);
            soapException.printStackTrace();
            return;
        }

        // create an object spec to define the beginning of the traversal;
        // container view is the root object for this traversal
        com.vmware.vim25.ObjectSpec oSpec = new com.vmware.vim25.ObjectSpec();
        oSpec.setObj(cViewRef);
        oSpec.setSkip(true);

        // create a traversal spec to select all objects in the view
        com.vmware.vim25.TraversalSpec tSpec = new com.vmware.vim25.TraversalSpec();
        tSpec.setName("traverseEntities");
        tSpec.setPath("view");
        tSpec.setSkip(false);
        tSpec.setType("ContainerView");

        // add the traversal spec to the object spec;
        // the accessor method (getSelectSet) returns a reference
        // to the mapped XML representation of the list; using this
        // reference to add the spec will update the selectSet list
        oSpec.getSelectSet().add(tSpec);

        // specify the properties for retrieval
        // (virtual machine name, network summary accessible, rp runtime props);
        // the accessor method (getPathSet) returns a reference to the mapped
        // XML representation of the list; using this reference to add the
        // property names will update the pathSet list
        com.vmware.vim25.PropertySpec pSpec = new com.vmware.vim25.PropertySpec();
        pSpec.setType(objectType);
        pSpec.getPathSet().add(property);

        // create a PropertyFilterSpec and add the object and
        // property specs to it; use the getter methods to reference
        // the mapped XML representation of the lists and add the specs
        // directly to the objectSet and propSet lists
        com.vmware.vim25.PropertyFilterSpec fSpec = new com.vmware.vim25.PropertyFilterSpec();
        fSpec.getObjectSet().add(oSpec);
        fSpec.getPropSet().add(pSpec);

        // Create a list for the filters and add the spec to it
        List<PropertyFilterSpec> fSpecList = new ArrayList<PropertyFilterSpec>();
        fSpecList.add(fSpec);

        // get the data from the server
        com.vmware.vim25.RetrieveOptions retrieveOptions = new com.vmware.vim25.RetrieveOptions();
        com.vmware.vim25.RetrieveResult props = vimPort.retrievePropertiesEx(propColl, fSpecList,
                retrieveOptions);

        // go through the returned list and print out the data
        if (props != null) {
            for (com.vmware.vim25.ObjectContent oc : props.getObjects()) {
                String value = null;
                String path = null;
                List<com.vmware.vim25.DynamicProperty> dps = oc.getPropSet();
                if (dps != null) {
                    for (com.vmware.vim25.DynamicProperty dp : dps) {
                        path = dp.getName();
                        if (path.equals(property)) {
                            // Because name is a String, only a cast is needed, but this will not
                            // work for more complex data types.
                            value = (String) dp.getVal();
                        }
                        System.out.println(path + " = " + value);
                    }
                }
            }
        }
    } // end collectProperties()

    /*
     * Authentication is handled by using a TrustManager and supplying a host name verifier method.
     * (The host name verifier is declared in the main function.)
     *
     * <b>Do not use this in production code! It is only for samples.</b>
     */
    private static class FakeTrustManager implements javax.net.ssl.TrustManager,
            javax.net.ssl.X509TrustManager {

        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        @SuppressWarnings("unused")
        public boolean isServerTrusted(java.security.cert.X509Certificate[] certs) {
            return true;
        }

        @SuppressWarnings("unused")
        public boolean isClientTrusted(java.security.cert.X509Certificate[] certs) {
            return true;
        }

        public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType)
                throws java.security.cert.CertificateException {
            return;
        }

        public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType)
                throws java.security.cert.CertificateException {
            return;
        }
    }

    /**
     * Runs the ListObjects sample code, which shows how to get a list of virtual machines, and
     * print out their names.<br>
     * <p>
     * Run with a command similar to this:<br>
     * <code>java -cp vim25.jar;ListObjects.jar com.vmware.complete.ListObjects <i>ip_or_name</i> <i>user</i> <i>password</i> <i>objectType</i></code><br>
     * <code>java -cp vim25.jar;ListObjects.jar com.vmware.complete.ListObjects 10.20.30.40 JoeUser JoePasswd VirtualMachine</code>
     * <br>
     * More details in the ListObjects_ReadMe.txt file.
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
        String objectType = "HostSystem";//"VirtualMachine", "Datacenter", "Folder","HostSystem", "ResourcePool", "ComputeResource", "ClusterComputeResource"
        String property = "name";
        String url = "https://" + serverName + "/sdk/vimService";

        // Variables of the following types for access to the API methods
        // and to the vSphere inventory.
        com.vmware.vim25.VimService vimService;
        com.vmware.vim25.VimPortType vimPort;
        com.vmware.vim25.ServiceContent serviceContent;

        // Set up the manufactured managed object reference for the ServiceInstance
        com.vmware.vim25.ManagedObjectReference serviceInstance = new com.vmware.vim25.ManagedObjectReference();
        serviceInstance.setType("ServiceInstance");
        serviceInstance.setValue("ServiceInstance");

        // Declare a host name verifier that will automatically enable
        // the connection. The host name verifier is invoked during
        // the SSL handshake.
        javax.net.ssl.HostnameVerifier hostnameVerifier = new HostnameVerifier() {
            public boolean verify(String urlHostName, SSLSession session) {
                return true;
            }
        };

        // Create the trust manager.
        javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];
        javax.net.ssl.TrustManager tm = new FakeTrustManager();
        trustAllCerts[0] = tm;

        // Create the SSL context
        javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext.getInstance("SSL");

        // Create the session context
        javax.net.ssl.SSLSessionContext sslsc = sc.getServerSessionContext();

        // Initialize the contexts; the session context takes the trust manager.
        sslsc.setSessionTimeout(0);
        sc.init(null, trustAllCerts, null);

        // Use the default socket factory to create the socket for the secure connection
        javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        // Set the default host name verifier to enable the connection.
        HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);

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
        serviceContent = vimPort.retrieveServiceContent(serviceInstance);
        vimPort.login(serviceContent.getSessionManager(), userName, password, null);

        // retrieve data
        collectProperties(vimPort, serviceContent, objectType, property);

        // close the connection
        vimPort.logout(serviceContent.getSessionManager());
    }
}
