package com.vmware.sample;

/**
 * Created by ccl on 17/2/17.
 */
import com.vmware.vim25.*;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.SOAPFaultException;
import java.util.*;

/**
 * <pre>
 * QueryVm
 *
 * This sample prints managed entity, its type, reference value,
 * property name, Property Value, Inner Object Type, its Inner Reference Value
 * and inner property value
 *
 * <b>Parameters:</b>
 * url         [required] : url of the web service
 * username    [required] : username for the authentication
 * password    [required] : password for the authentication
 *
 * <b>Command Line:</b>
 * run.bat com.vmware.general.QueryVm --url [webserviceurl]
 * --username [username] --password [password]
 * </pre>
 */

public class QueryVm {

    private static class TrustAllTrustManager implements
            javax.net.ssl.TrustManager, javax.net.ssl.X509TrustManager {

        @Override
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        @Override
        public void checkServerTrusted(
                java.security.cert.X509Certificate[] certs, String authType)
                throws java.security.cert.CertificateException {
            return;
        }

        @Override
        public void checkClientTrusted(
                java.security.cert.X509Certificate[] certs, String authType)
                throws java.security.cert.CertificateException {
            return;
        }
    }

    private static final ManagedObjectReference SVC_INST_REF =
            new ManagedObjectReference();
    private static final String SVC_INST_NAME = "ServiceInstance";
    private static final String PROP_ME_NAME = "name";
    private static ServiceContent serviceContent;
    private static ManagedObjectReference rootRef;
    private static VimService vimService;
    private static VimPortType vimPort;
    private static String url;
    private static String userName;
    private static String password;
    private static boolean help = false;
    private static boolean isConnected = false;

    private static void trustAllHttpsCertificates() throws Exception {
        // Create a trust manager that does not validate certificate chains:
        javax.net.ssl.TrustManager[] trustAllCerts =
                new javax.net.ssl.TrustManager[1];
        javax.net.ssl.TrustManager tm = new TrustAllTrustManager();
        trustAllCerts[0] = tm;
        javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext.getInstance("SSL");
        javax.net.ssl.SSLSessionContext sslsc = sc.getServerSessionContext();
        sslsc.setSessionTimeout(0);
        sc.init(null, trustAllCerts, null);
        javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc
                .getSocketFactory());
    }

    // get common parameters
    private static void getConnectionParameters(String[] args)
            throws IllegalArgumentException {
        url = "https://10.200.6.92:443/sdk/vimService";
        userName = "administrator@vsphere.local";
        password = "Wb1234==";
        if (url == null || userName == null || password == null) {
            throw new IllegalArgumentException(
                    "Expected --url, --username, --password arguments.");
        }
    }

    /**
     * Establishes session with the vCenter server server.
     *
     * @throws Exception
     *            the exception
     */
    private static void connect() throws Exception {

        HostnameVerifier hv = new HostnameVerifier() {
            @Override
            public boolean verify(String urlHostName, SSLSession session) {
                return true;
            }
        };
        trustAllHttpsCertificates();
        HttpsURLConnection.setDefaultHostnameVerifier(hv);

        SVC_INST_REF.setType(SVC_INST_NAME);
        SVC_INST_REF.setValue(SVC_INST_NAME);

        vimService = new VimService();
        vimPort = vimService.getVimPort();
        Map<String, Object> ctxt =
                ((BindingProvider) vimPort).getRequestContext();

        ctxt.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url);
        ctxt.put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);

        serviceContent = vimPort.retrieveServiceContent(SVC_INST_REF);
        vimPort.login(serviceContent.getSessionManager(), userName, password,
                null);
        isConnected = true;

        rootRef = serviceContent.getRootFolder();
    }

    /**
     * Disconnects the user session.
     *
     * @throws Exception
     */
    private static void disconnect() throws Exception {
        if (isConnected) {
            vimPort.logout(serviceContent.getSessionManager());
        }
        isConnected = false;
    }

    /**
     * Returns all the MOREFs of the specified type that are present under the
     * folder
     *
     * @param folder
     *           {@link ManagedObjectReference} of the folder to begin the search
     *           from
     * @param morefType
     *           Type of the managed entity that needs to be searched
     *
     * @return Map of name and MOREF of the managed objects present. If none
     *         exist then empty Map is returned
     *
     * @throws InvalidPropertyFaultMsg
     * @throws RuntimeFaultFaultMsg
     */
    private static Map<String, ManagedObjectReference> getMOREFsInFolderByType(
            ManagedObjectReference folder, String morefType)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
        ManagedObjectReference viewManager = serviceContent.getViewManager();
        ManagedObjectReference containerView =
                vimPort.createContainerView(viewManager, folder,
                        Arrays.asList(morefType), true);

        Map<String, ManagedObjectReference> tgtMoref =
                new HashMap<String, ManagedObjectReference>();

        // Create Property Spec
        PropertySpec propertySpec = new PropertySpec();
        propertySpec.setAll(Boolean.FALSE);
        propertySpec.setType(morefType);
        propertySpec.getPathSet().add(PROP_ME_NAME);

        TraversalSpec ts = new TraversalSpec();
        ts.setName("view");
        ts.setPath("view");
        ts.setSkip(false);
        ts.setType("ContainerView");

        // Now create Object Spec
        ObjectSpec objectSpec = new ObjectSpec();
        objectSpec.setObj(containerView);
        objectSpec.setSkip(Boolean.TRUE);
        objectSpec.getSelectSet().add(ts);

        // Create PropertyFilterSpec using the PropertySpec and ObjectPec
        // created above.
        PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
        propertyFilterSpec.getPropSet().add(propertySpec);
        propertyFilterSpec.getObjectSet().add(objectSpec);

        List<PropertyFilterSpec> propertyFilterSpecs =
                new ArrayList<PropertyFilterSpec>();
        propertyFilterSpecs.add(propertyFilterSpec);

        List<ObjectContent> oCont =
                vimPort.retrieveProperties(serviceContent.getPropertyCollector(),
                        propertyFilterSpecs);
        if (oCont != null) {
            for (ObjectContent oc : oCont) {
                ManagedObjectReference mr = oc.getObj();
                String entityNm = null;
                List<DynamicProperty> dps = oc.getPropSet();
                if (dps != null) {
                    for (DynamicProperty dp : dps) {
                        entityNm = (String) dp.getVal();
                    }
                }
                tgtMoref.put(entityNm, mr);
            }
        }
        return tgtMoref;
    }

    private static List<String> printInventory() throws InvalidPropertyFaultMsg,
            RuntimeFaultFaultMsg {
        Map<String, ManagedObjectReference> inventory =
                getMOREFsInFolderByType(rootRef, "ManagedEntity");
        List<String> list = new ArrayList<String>();
        for (String entityName : inventory.keySet()) {
            list.add("> " + inventory.get(entityName).getType() + ":\t"
                    + inventory.get(entityName).getValue() + "\t{" + entityName + "}");
//         System.out.println("> " + inventory.get(entityName).getType() + ":"
//               + inventory.get(entityName).getValue() + "{" + entityName + "}");
        }
        return list;
    }

    private static void printSoapFaultException(SOAPFaultException sfe) {
        System.out.println("SOAP Fault -");
        if (sfe.getFault().hasDetail()) {
            System.out.println(sfe.getFault().getDetail().getFirstChild()
                    .getLocalName());
        }
        if (sfe.getFault().getFaultString() != null) {
            System.out.println("\n Message: " + sfe.getFault().getFaultString());
        }
    }

    private static void printUsage() {
        System.out
                .println("This sample prints managed entity, its type, reference value,");
        System.out.println("property name, Property Value, Inner Object Type,");
        System.out.println("its Inner Reference Value and inner property value");
        System.out.println("\nParameters:");
        System.out.println("url         [required] : url of the web service");
        System.out
                .println("username    [required] : username for the authentication");
        System.out
                .println("password    [required] : password for the authentication");
        System.out.println("\nCommand:");
        System.out
                .println("run.bat com.vmware.general.QueryVm --url [webserviceurl] "
                        + "--username [username] --password [password]");
    }

    public static void main(String[] args) {
        List<String> list = new ArrayList<String>();
        try {
            getConnectionParameters(args);
//         if (help) {
//            printUsage();
//            return;
//         }
            connect();
            list = printInventory();
            for (String value : list){
                System.out.println(value);
            }
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            printUsage();
        } catch (SOAPFaultException sfe) {
            printSoapFaultException(sfe);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                disconnect();
            } catch (SOAPFaultException sfe) {
                printSoapFaultException(sfe);
            } catch (Exception e) {
                System.out.println("Failed to disconnect - " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}