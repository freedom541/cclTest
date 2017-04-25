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

package com.vmware.connection;

import com.vmware.common.annotations.After;
import com.vmware.common.annotations.Before;
import com.vmware.common.annotations.Option;
import com.vmware.connection.helpers.GetMOREF;
import com.vmware.connection.helpers.WaitForValues;
import com.vmware.vim25.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * An abstract base class, extend this class if your common needs to
 * open a connection to the virtual center server before it can do anything useful.
 * <p/>
 * Example: The simplest possible extension class merely forms a connection and
 * specifies it's own common action.
 * <p/>
 * <pre>
 *     &#064;Sample(name = "connect")
 *     public class Connect extends ConnectedVimServiceBase {
 *          &#064;Action
 *          public void action() {
 *               System.out.println("currently connected: " + this.isConnected());
 *          }
 *     }
 * </pre>
 * <p/>
 * This is provided as an alternative to extending the Connection class directly.
 * <p/>
 * For a very simple connected sample:
 *
 * @see com.vmware.general.GetCurrentTime
 */
public abstract class ConnectedVimServiceBase {
    public static final String PROP_ME_NAME = "name";
    public static final String SVC_INST_NAME = "ServiceInstance";
    protected Connection connection;
    protected VimPortType vimPort;
    protected ServiceContent serviceContent;
    protected ManagedObjectReference rootRef;
    @SuppressWarnings("rawtypes")
    protected Map headers;
    protected WaitForValues waitForValues;
    protected GetMOREF getMOREFs;

    // By default assume we are talking to a vCenter
    Boolean hostConnection = Boolean.FALSE;

    @Option(
            name = "basic-connection",
            required = false,
            description =
                    "Turn off the use of SSO for connections. Useful for connecting to ESX or ESXi hosts.",
            parameter = false
    )
    public void setHostConnection(final Boolean value) {
        // NOTE: the common framework will insert a "Boolean.TRUE" object on
        // options that have parameter = false set. This indicates they
        // are boolean flag options not string parameter options.
        this.hostConnection = value;
    }

    /**
     * Use this method to get a reference to the service instance itself.
     * <p/>
     *
     * @return a managed object reference referring to the service instance itself.
     */
    public ManagedObjectReference getServiceInstanceReference() {
        return connection.getServiceInstanceReference();
    }

    /**
     * A method for dependency injection of the connection object.
     * <p/>
     *
     * @param connect the connection object to use for this POJO
     * @see Connection
     */
    @Option(name = "connection", type = Connection.class)
    public void setConnection(Connection connect) {
        this.connection = connect;
    }

    /**
     * connects this object, returns itself to allow for method chaining
     *
     * @return a connected reference to itself.
     * @throws Exception
     */
    @Before
    public Connection connect() {

        if(hostConnection) {
            // construct a BasicConnection object to use for
            connection = basicConnectionFromConnection(connection);
        }

        //if( validateConnection() ) {
        if(true) {
            connection.connect();
            this.waitForValues = new WaitForValues(connection);
            this.getMOREFs = new GetMOREF(connection);
            this.headers = connection.getHeaders();
            this.vimPort = connection.getVimPort();
            this.serviceContent = connection.getServiceContent();
            this.rootRef = serviceContent.getRootFolder();
        }
        else {
            // not the best form, but without a connection these samples are pointless.
            System.err.println("No valid connection available. Exiting now.");
            System.exit(0);
        }
        return connection;
    }

    public boolean validateConnection() {
        if (connection instanceof SsoConnection) {
            SsoConnection conn = (SsoConnection) connection;
            return validateSsoConnection(conn);
        }
        else {
            return validateOtherConnection(connection);
        }
    }

    public static boolean validateOtherConnection(final Connection connection) {
        URL url = null;
        try {
            url = new URL(connection.getUrl());
        } catch (MalformedURLException e) {
            System.err.printf("The value used for --url %s " +
                    "is not a properly formed URL.", connection.getUrl());
        }
        return validateUrl(url);
    }

    public static boolean validateSsoConnection(final SsoConnection conn) {
        URL url = null;
        try {
            url = conn.getSsoUrl();
        } catch (MalformedURLException e) {
            System.err.printf(
                    "A critical system setting holds a malformed URL. " +
                            "Check connection.properties file for property vimService.url and " +
                            "make sure it is a valid value. " +
                            "Check to make sure sso.url is either: NOT set or is set to a valid " +
                            "and reachable IP " +
                            "address and port number." +
                            "%n"
            );
        }
        final Boolean valid = validateUrl(url);
        if(!valid) {
            System.err.printf(
                    "Note: SSO connections only work on vCenter SSO enabled products.%n");
            System.err.printf("Use the --basic-connection switch if you know this is an ESX host.%n");
            System.err.printf("See the connection.properties file for more instructions.%n");
        }
        return valid;
    }

    public static boolean validateUrl(final URL url) {
        boolean good;
        try {
            good = testURL(url);
            if( !good ) {
                System.err.printf("The server at %s did not respond as expected. Is this a valid URL?", url);
            }
        } catch (IOException e) {
            System.err.printf(
                    "Could not connect to %s due to %s%n",
                    url,
                    e.getLocalizedMessage()
            );
            good = false;
        }
        return good;
    }

    public static boolean testURL(final URL sourceUrl) throws IOException {
        URL url = new URL(sourceUrl.getProtocol(),sourceUrl.getHost(),sourceUrl.getPort(),"/");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.connect();
        return conn.getResponseCode() == 200;
    }

    public BasicConnection basicConnectionFromConnection(final Connection original) {
        BasicConnection connection = new BasicConnection();
        connection.setUrl(original.getUrl());
        connection.setUsername(original.getUsername());
        connection.setPassword(original.getPassword());
        return connection;
    }

    /**
     * disconnects this object and returns a reference to itself for method chaining
     *
     * @return a disconnected reference to itself
     * @throws Exception
     */
    @After
    public Connection disconnect() {
        this.waitForValues = null;
        try {
            connection.disconnect();
        } catch (Throwable t) {
            throw new ConnectionException(t);
        }
        return connection;
    }

    public class ConnectionException extends RuntimeException {
        public ConnectionException(Throwable cause) {
            super(cause);
        }

        public ConnectionException(String message) {
            super(message);
        }
    }

    public boolean getTaskResultAfterDone(ManagedObjectReference task)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg,
            InvalidCollectorVersionFaultMsg {

        boolean retVal = false;

        // info has a property - state for state of the task
        Object[] result =
                waitForValues.wait(task, new String[]{"info.state", "info.error"},
                        new String[]{"state"}, new Object[][]{new Object[]{
                                TaskInfoState.SUCCESS, TaskInfoState.ERROR}});

        if (result[0].equals(TaskInfoState.SUCCESS)) {
            retVal = true;
        }
        if (result[1] instanceof LocalizedMethodFault) {
            throw new RuntimeException(
                    ((LocalizedMethodFault) result[1]).getLocalizedMessage());
        }
        return retVal;
    }

    /**
     * Returns all the MOREFs of the specified type that are present under the
     * container
     *
     * @param folder    {@link ManagedObjectReference} of the container to begin the
     *                  search from
     * @param morefType Type of the managed entity that needs to be searched
     * @return Map of name and MOREF of the managed objects present. If none
     * exist then empty Map is returned
     * @throws InvalidPropertyFaultMsg
     * @throws RuntimeFaultFaultMsg
     */
    public Map<String, ManagedObjectReference> getMOREFsInContainerByType(
            ManagedObjectReference folder, String morefType)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
        String PROP_ME_NAME = "name";
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

        RetrieveResult rslts =
                vimPort.retrievePropertiesEx(serviceContent.getPropertyCollector(),
                        propertyFilterSpecs, new RetrieveOptions());
        List<ObjectContent> listobjcontent = new ArrayList<ObjectContent>();
        if (rslts != null && rslts.getObjects() != null
                && !rslts.getObjects().isEmpty()) {
            listobjcontent.addAll(rslts.getObjects());
        }
        String token = null;
        if (rslts != null && rslts.getToken() != null) {
            token = rslts.getToken();
        }
        while (token != null && !token.isEmpty()) {
            rslts =
                    vimPort.continueRetrievePropertiesEx(
                            serviceContent.getPropertyCollector(), token);
            token = null;
            if (rslts != null) {
                token = rslts.getToken();
                if (rslts.getObjects() != null && !rslts.getObjects().isEmpty()) {
                    listobjcontent.addAll(rslts.getObjects());
                }
            }
        }
        for (ObjectContent oc : listobjcontent) {
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
        return tgtMoref;
    }
}
