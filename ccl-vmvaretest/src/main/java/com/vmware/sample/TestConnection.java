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

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.ws.BindingProvider;
import java.util.Map;

/*
 * Coding Conventions Used Here:
 * 1. The connection to vCenter is managed from within the TestConnection class.
 * 2. All the code needed to make a connection to vCenter is in this one file.
 *
 * Also: Full path names are used for all java classes when they are first used (for declarations
 * or to call static methods).  This makes it easier to find their source code, so you can understand
 * it.  For example "com.vmware.utils.VMwareConnection conn" rather than "VMwareConnection conn".
 */

/**
 * Connects to a vCenter server, and prints out a few server info such as the product name, server
 * type, and product version. It is designed to show connection basics, and to test that the server
 * really is up and running.
 */
public class TestConnection {

    /*
     * Authentication is handled by using a TrustManager and supplying a host name verifier method.
     * (The host name verifier is declared in the main function.)
     *
     * Do not use this in production code! It is only for samples.
     */
    private static class TrustAllTrustManager implements javax.net.ssl.TrustManager,
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
     * Runs the TestConnect sample code, which establishes a connection to a vCenter or ESX server
     * and prints out a little information about that server.
     * <p>
     * Run with a command similar to this:<br>
     * <code>java -cp vim25.jar com.vmware.complete.TestConnection <i>ip_or_name</i> <i>user</i> <i>password</i></code><br>
     * <code>java -cp vim25.jar com.vmware.complete.TestConnection 10.20.30.40 JoeUser JoePasswd</code>
     * <br>More details in the TestConnection_ReadMe.txt file.
     *
     * @param args
     *            the ip_or_name, user, and password
     * @throws Exception
     *             if an exception occurred
     */
    public static void main(String[] args) {

        // Server URL and credentials.
        String serverName = "10.200.6.92:443";
        String userName = "administrator@vsphere.local";
        String password = "Wb1234==";
        String url = "https://" + serverName + "/sdk/vimService"; // ** check for bad serverName

        com.vmware.vim25.VimPortType vimPort = null;
        com.vmware.vim25.ServiceContent serviceContent = null;

        try {
            // Variables of the following types for access to the API methods
            // and to the vSphere inventory.
            // -- ManagedObjectReference for the ServiceInstance on the Server
            // -- VimService for access to the vSphere Web service
            // -- VimPortType for access to methods
            // -- ServiceContent for access to managed object services
            com.vmware.vim25.ManagedObjectReference servicesInstance = new com.vmware.vim25.ManagedObjectReference();
            com.vmware.vim25.VimService vimService;

            // Declare a host name verifier that will automatically enable
            // the connection. The host name verifier is invoked during
            // the SSL handshake.
            HostnameVerifier verifier = new HostnameVerifier() {
                public boolean verify(String urlHostName, SSLSession session) {
                    return true;
                }
            };
            // Create the trust manager.
            javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];
            javax.net.ssl.TrustManager trustManager = new TrustAllTrustManager();
            trustAllCerts[0] = trustManager;

            // Create the SSL context
            javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext.getInstance("SSL");

            // Create the session context
            javax.net.ssl.SSLSessionContext sslsc = sc.getServerSessionContext();

            // Initialize the contexts; the session context takes the trust manager.
            sslsc.setSessionTimeout(0);
            sc.init(null, trustAllCerts, null);

            // Use the default socket factory to create the socket for the secure connection
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            // Set the default host name verifier to enable the connection.
            HttpsURLConnection.setDefaultHostnameVerifier(verifier);

            // Set up the manufactured managed object reference for the ServiceInstance
            servicesInstance.setType("ServiceInstance");
            servicesInstance.setValue("ServiceInstance");

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
            serviceContent = vimPort.retrieveServiceContent(servicesInstance);
            try {
                vimPort.login(serviceContent.getSessionManager(), userName, password, null);
            } catch (com.vmware.vim25.InvalidLoginFaultMsg ilfm) {
                System.out
                        .printf("Either your username (%s) was wrong, or the password (%s) was not the right one.",
                                userName, password);
                System.out.println(ilfm.getMessage());
                System.exit(0);
            }
            // Notice that all other exceptions are caught down below, and are handled very
            // genericly.

            // print out the product name, server type, and product version
            System.out.println(serviceContent.getAbout().getFullName());
            System.out.printf("Server type is %s", serviceContent.getAbout().getApiType());
            System.out.printf("API version is %s", serviceContent.getAbout().getVersion());

        } catch (Exception e) {
            System.err.println("Sample code failed ");
            e.printStackTrace();
            System.exit(1);
        } finally {
            // Always close connectionl, even if errors occure.
            if (vimPort != null && serviceContent != null) {
                try {
                    vimPort.logout(serviceContent.getSessionManager());
                } catch (com.vmware.vim25.RuntimeFaultFaultMsg rffm) {
                    System.out.println("Sample code failed while logging out after a previous failure.");
                    rffm.printStackTrace();
                }
            }
        }

    }// end main()
}// end class TestConnection