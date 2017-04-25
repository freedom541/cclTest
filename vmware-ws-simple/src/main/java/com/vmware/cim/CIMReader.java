/*
 * *****************************************************
 * Copyright VMware, Inc. 2010-2012.  All Rights Reserved.
 * *****************************************************
 *
 * DISCLAIMER. THIS PROGRAM IS PROVIDED TO YOU "AS IS" WITHOUT
 * WARRANTIES OR CONDITIONS # OF ANY KIND, WHETHER ORAL OR WRITTEN,
 * EXPRESS OR IMPLIED. THE AUTHOR SPECIFICALLY # DISCLAIMS ANY IMPLIED
 * WARRANTIES OR CONDITIONS OF MERCHANTABILITY, SATISFACTORY # QUALITY,
 * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.vmware.cim;

import com.vmware.cim.helpers.GetCim;
import com.vmware.common.annotations.Action;
import com.vmware.common.annotations.Option;
import com.vmware.common.annotations.Sample;
import com.vmware.connection.ConnectedVimServiceBase;
import com.vmware.connection.helpers.ApiValidator;
import com.vmware.vim25.HostServiceTicket;

import javax.cim.CIMClass;
import javax.cim.CIMInstance;
import javax.cim.CIMObjectPath;
import javax.cim.CIMProperty;
import javax.wbem.CloseableIterator;
import javax.wbem.WBEMException;
import javax.wbem.client.WBEMClient;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import static java.lang.System.out;

/**
 * examples of how to work with CIM and vSphere API together.
 * <p/>
 * Requires the sblim-cim-client2-2.1.1.jar and the sblim-cim-client2.properties files
 * these must <strong>both</strong> be in the classpath. The sblim-cim-client2.properties should have the value
 * <code>sblim.wbem.httpMPOST=true</code> set so that the CIM client functions properly.
 * <p/>
 *<pre>
 *     cim-reader
 *
 *     allows you to walk the Common Information Model (CIM)
 *     data associated with an ESX Host You cannot read CIM data
 *     from a vCenter.
 *
 *     --url         &lt;url&gt;         [required]    full url to vcenter
 *     --username    &lt;username&gt;    [required]    username on remote system
 *     --password    &lt;password&gt;    [required]    password on remote system
 *     --host        &lt;host&gt;         optional
 *                              host name or IP to gather CIM
 *                              data from (only if not talking
                                directly to an ESX Host)
 *
 *     --instance    &lt;instance&gt;     optional
 *                              the CIM class instance to list
 *                              properties of, if none specified
 *                              then the command will list all
 *                              available instances of the specified
 *                              class

 *     --namespace   &lt;namespace&gt;    optional
 *                              the namespace to explore, if
 *                              none is specified, then the
 *                              command will list all namespaces
 *                              available at the target.
 *
 *     --port        &lt;port&gt;         optional
 *                          The port number on our remote
 *                          host that the CIM services are
 *                          running on. Defaults to 5989
 *                          if not specified.
 *
 *    --class       &lt;class&gt;        optional
 *                          the CIM class to explore, if
 *                          none specified, then the command
 *                          will list all available class
 *                          names under the namespace.
 *</pre>
 */
@Sample(
        name = "cim-reader",
        description =
                "allows you to walk the Common Information Model (CIM) data associated with an ESX Host " +
                        "You cannot read CIM data from a vCenter. This sample relies on classes in the javax.cim " +
                        "and javax.wbem packages. Implementations can be found in the sblim-cim-client2-2.1.1.jar " +
                        "or equivalent library. This sample allows you to drill down into CIM information by first " +
                        "connecting to a CIM service then exploring it's namespaces, classes, and class instances. "
)
public class CIMReader extends ConnectedVimServiceBase {
    private GetCim getCim;

    String host = null;

    @Option(name = "host",
            required = false,
            description = "host's entity name or IP to gather CIM data from (if not talking directly to" +
                    " the ESX host).")
    public void setHost(final String hostNameOrIp) {
        this.host = hostNameOrIp;
    }

    int cimPort = 5989; // note default value

    @Option(name = "port", required = false,
            description = "The port number on our remote host that the CIM services are running on. " +
                    "Defaults to 5989 if not specified.")
    public void setCimPort(final String port) {
        this.cimPort = (port != null && !"".equals(port))?Integer.parseInt(port):5989;
    }

    String namespace = null;

    @Option(name = "namespace", required = false,
            description = "the namespace to explore, if none is specified, " +
                    "then the command will list all namespaces available at the target.")
    public void setNamespace(final String namespace) {
        this.namespace = namespace;
    }

    String cimClass = null;

    @Option(name = "class", required = false,
            description = "the CIM class to explore, if none specified, " +
                    "then the command will list all available class names under the namespace.")
    public void setCimClass(final String cimClass) {
        this.cimClass = cimClass;
    }

    String instance = null;

    @Option(name = "instance", required = false,
            description = "the CIM class instance to list properties of, if none specified then" +
                    " the command will list all available instances of the specified class")
    public void setInstance(final String name) {
        this.instance = name;
    }

    /**
     * builds a base URL to use for CIMObjectPath objects based on the host and connection
     * objects already present in this object on initialization
     *
     * @return a URL to talk to the CIM server on
     */
    public URL cimBaseUrl() {
        URL url;

        try {

            String hostname = this.host != null ? host : connection.getHost();
            url = new URL("https", hostname, this.cimPort, "/");

        } catch (MalformedURLException e) {
            throw new CIMReaderIllegalArgumentException("check your CIM port & host parameters", e);
        }

        return check(url) ? url : null;
    }

    /**
     * checks a URL to see if we can open a connection to it
     *
     * @param url - to examine
     * @return true if we can talk to the host
     */
    public boolean check(final URL url) {
        boolean valid;

        try {
            url.openConnection();
            valid = true;
        } catch (IOException e) {
            throw new CIMReaderUnreachableHost(url, e);
        }

        return valid;
    }

    /**
     * creates a list of namespaces for a host, this is the same for ALL ESX hosts
     * @see CIMObjectPath
     * @return a collection of CIMObjectPath objects representing the namespaces on the host
     * @throws WBEMException
     */
    public Collection<CIMObjectPath> listNamespaces() throws WBEMException {
        final List<CIMObjectPath> results = new LinkedList<CIMObjectPath>();
        for (String namespace : GetCim.NAMESPACES) {
            CIMObjectPath path = getCim().baseObjectPath(cimBaseUrl(), namespace);
            results.add(path);
        }
        return results;
    }

    /**
     * creates a set of namespaces... based on a collection of objectPaths
     *
     * @param objectPaths the object paths to examine
     * @return a set of unique namespace names (strings)
     */
    public Collection<String> listNamespaces(Collection<CIMObjectPath> objectPaths) {
        final Set<String> results = new HashSet<String>();
        for (CIMObjectPath objectPath : objectPaths) {
            results.add(objectPath.getNamespace());
        }
        return results;
    }

    /**
     * lists all the classes at the client that are available
     *
     * @param client    an initialized client at the host
     * @param namespace the namespace to look at
     * @return a collection of CIMClass objects available at the host + namespace
     * @throws WBEMException
     */
    public Collection<CIMClass> listClasses(final WBEMClient client, final String namespace) throws WBEMException {
        CIMObjectPath objectPath = getCim().baseObjectPath(cimBaseUrl(), namespace);

        final List<CIMClass> results = new LinkedList<CIMClass>();
        CloseableIterator<CIMClass> enumeration = client.enumerateClasses(objectPath, true, true, true, true);
        while (enumeration.hasNext()) {
            results.add(enumeration.next());
        }

        return results;
    }

    /**
     * builds a list of instances of a particular class at the client
     *
     * @param client    initialized client to use
     * @param namespace the namespace to consider
     * @param classname the class to examine
     * @return a collection of CIMInstances that are of the specified class
     * @throws WBEMException
     */
    public Collection<CIMInstance> listInstances(final WBEMClient client, final String namespace, final String classname) throws WBEMException {
        CIMObjectPath objectPath = getCim().objectPath(cimBaseUrl(), namespace, classname);

        final List<CIMInstance> results = new LinkedList<CIMInstance>();
        CloseableIterator<CIMInstance> enumeration = client.enumerateInstances(objectPath, true, true, true, null);
        while (enumeration.hasNext()) {
            results.add(enumeration.next());
        }

        return results;
    }

    /**
     * gets a specific instance of a CIM class. Remember to command line escape or otherwise
     * properly encode the string representing the URI to the CIM object. The path often contains
     * quotes and other special characters that confuse many simple-minded parsers.
     * <p/>
     *
     * @param client     a configured and initialized client to use
     * @param objectPath as a URI to a specific instance
     * @return the instance specified by the objectPath
     * @throws WBEMException
     */
    public CIMInstance getInstance(final WBEMClient client, final String objectPath) throws WBEMException {
        CIMObjectPath cimObjectPath = new CIMObjectPath(objectPath);
        return getInstance(client, cimObjectPath);
    }

    /**
     * gets a specific CIM instance based on a cimObjectPath instance
     * <p/>
     *
     * @param client        a configured and initialized client to use
     * @param cimObjectPath a properly constructed CIMObjectPath representing the instance's location
     * @return an instance object representing the instance
     * @throws WBEMException
     */
    public CIMInstance getInstance(final WBEMClient client, final CIMObjectPath cimObjectPath) throws WBEMException {
        return client.getInstance(cimObjectPath, true, true, null);
    }

    /**
     * builds a list of properties as a map based on an instance, instance objects hold local copies
     * of the properties associated with the server-side instance. This may not be the whole set
     * of properties.
     * <p/>
     * NOTE: be sure to use "getInstance(path,true,true,null)" if you want to see <i>all</i> the
     *
     * @param instance the instance to examine (holds a local subset of properties)
     * @return a map of the properties available in the instance
     * @throws WBEMException
     */
    public Map<String, Object> listProperties(CIMInstance instance) throws WBEMException {
        final Map<String, Object> results = new LinkedHashMap<String, Object>();
        for (CIMProperty<?> property : instance.getProperties()) {
            results.put(property.getName(), property.getValue());
        }
        return results;
    }

    public GetCim getCim() {
        if (getCim == null) {
            // use the specified host name if it is set
            final String hostname;
            if(isDirectConnection()) {
                hostname = connection.getHost();
            }
            else {
                hostname = host;
            }
            this.getCim = new GetCim(connection, hostname);
        }
        return getCim;
    }

    int detectParams(String[] params) {
        int found = -1;
        int count = 0;
        while (count < params.length) {
            if (params[count] != null) {
                found = count;
            }
            count++;
        }
        return found + 1;
    }

    /**
     * Two ways to build a client, either with a username and password for direct login to the ESX host or using
     * a ticket.
     *
     * @param namespace
     * @return
     */
    public WBEMClient client(String namespace) {
        WBEMClient client;
        if (isDirectConnection()) {
            // this must mean we have a direct ESX connection, use this formula:
            client = getCim().client(connection.getUsername(), connection.getPassword(), cimBaseUrl(), namespace);
        } else {
            // we will need to obtain a ticket for our host and connect via vCenter
            HostServiceTicket ticket = getCim().ticket();
            client = getCim().client(ticket, cimBaseUrl(), namespace);
        }
        return client;
    }

    /**
     * if no hostname is specified, then look for a direct host connection
     * otherwise we should be talking to a vCenter.
     */
    boolean checkTypeAndHost() {
        final ApiValidator apiValidator = new ApiValidator(connection);
        final String type = apiValidator.getApiType();

        boolean good = false;
        if(host == null) {
            good |= ApiValidator.HOST_API_TYPE.equals(type);
            if(ApiValidator.VCENTER_API_TYPE.equals(type)) {
                System.out.println(
                        String.format(
                                "when talking to a '%s' you must specify a --host <name> to talk to",
                                ApiValidator.VCENTER_API_TYPE
                        )
                );
            }
        }
        else {
            good |= ApiValidator.VCENTER_API_TYPE.equals(type);
        }
        return good;
    }

    public Boolean isDirectConnection() {
        return isDirectConnection(getConnectionType());
    }

    public String getConnectionType() {
        final ApiValidator apiValidator = new ApiValidator(connection);
        return apiValidator.getApiType();
    }

    public Boolean isDirectConnection(final String type) {
        return ApiValidator.HOST_API_TYPE.equals(type);
    }

    private String alias(final Object item, final boolean prettyprint) {
        if (item instanceof CIMClass) {
            return alias((CIMClass) item);
        }
        if (item instanceof CIMInstance) {
            CIMInstance inst = (CIMInstance) item;
            String path = inst.getObjectPath().toString();
            return String.format("\t'%s'\n", path);
        }
        if (item instanceof CIMObjectPath) {
            CIMObjectPath objectPath = (CIMObjectPath) item;
            String path = objectPath.toString(); // alias(objectPath);
            return String.format("\"%s\"", path.replaceAll("\"","\\\""));
        }
        if (item instanceof CIMProperty<?>[]) {
            return cimProperties((CIMProperty<?>[]) item, (prettyprint) ? "\t%s=\"%s\"\n" : "%s=\"%s\",");
        }
        if (item instanceof Object[]) {
            return listing((Object[]) item);
        }
        return (item != null) ? item.toString() : "";
    }

    private String listing(final Object... item) {
        StringBuilder out = new StringBuilder();

        for (int i = 0; i < item.length; i++) {
            out.append(item[i]);
            if (i + 1 < item.length) {
                out.append(",");
            }
        }

        return out.toString();
    }

    /**
     * create an "alias" string for an object path, this is intended for display
     * purposes and not really for looking up the object path later. If you want
     * to provide a canonical representation that can be used to find an instance
     * use the CIMObjectPath toString method to produce a URI. The URI cannot
     * necessarily specify an instance if the properties are too short.
     *
     * @param objectPath
     * @return a pretty print string representing the object for display
     * @see CIMObjectPath review the section on keys
     */
    public String alias(CIMObjectPath objectPath) {
        String cimClass = objectPath.getObjectName();
        String properties = cimProperties(objectPath.getKeys(), "%s=\"%s\",");
        return String.format("%s(%s);", cimClass, properties);
    }

    /**
     * pretty print a CIM class
     *
     * @param cimClass
     * @return the name of the class
     */
    public String alias(CIMClass cimClass) {
        return cimClass.getName();
    }

    /**
     * produces a pretty print alias for an instance
     *
     * @param instance to examine
     * @return string representation of the CIMInstance
     */
    public String alias(CIMInstance instance) {
        return alias(instance.getObjectPath());
    }

    /**
     * pretty print the properties of an instance
     *
     * @param instance examine this
     * @return a string pretty print of the instance's properties
     */
    public String instanceProperties(CIMInstance instance) {
        final CIMProperty<?>[] properties = instance.getProperties();
        return cimProperties(properties, "%s=\"%s\",");
    }

    /**
     * Knows how to read CIMProperty objects and put them in a string format
     *
     * @param properties to list out pretty print
     * @param format     to use in pretty print, like "%s='%s',"
     * @return a string representing the properties with the format string repeatedly applied
     */
    public String cimProperties(final CIMProperty<?>[] properties, final String format) {
        StringBuffer out = new StringBuffer();
        for (CIMProperty<?> property : properties) {
            String name = property.getName();
            Object value = property.getValue();
            if (value instanceof Object[]) {
                value = listing((Object[]) value);
            }
            out.append(String.format(format, name, (value != null) ? value : ""));
        }
        return out.toString();
    }

    private void browseCIMData() throws WBEMException {
        out.println();

        // never allow a null list, null lists confuse API consumers and frustrate code
        Collection results = new LinkedList<Object>();
        switch (detectParams(new String[]{namespace, cimClass, instance})) {
            case 0: // nothing
                out.println("choose a namespace using --namespace ");
                results = (Collection) listNamespaces(listNamespaces());
                break;
            case 1: // namespace
                out.println("choose a class using --class ");
                results = (Collection) listClasses(client(namespace), namespace);
                break;
            case 2: // namespace,class
                out.println("listing instances look inside an instance using --instance a URL from below ");
                results = (Collection) listInstances(client(namespace), namespace, cimClass);
                break;
            case 3: // namespace,class,instance
                out.println("listing properties");
                results.add(
                        getInstance(
                                client(namespace),
                                instance // a URI pointing to a sample
                        ).getProperties()
                );
                break;
        }
        out.println("--");

        for (Object item : results) {
            System.out.println(alias(item, true));
        }

        if (results.size() == 0) {
            System.out.print(System.getProperty("cim.results.empty", "\tno results to display"));
        }

        out.println("--");
    }

    /**
     * The primary action entry point for this sample, presumes that all the property setters have
     * been called and connection and other settings have been injected. The sample switches logically
     * on the provided arguments set by the setter.
     * <p/>
     *
     * @throws WBEMException
     */
    @Action
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void run() throws WBEMException {
        if(checkTypeAndHost()) {
            browseCIMData();
        }
    }

    /**
     * for general CIMReader related Exceptions.
     */
    private class CIMReaderException extends RuntimeException {
		private static final long serialVersionUID = 3798539954464044318L;

		public CIMReaderException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * When we get a command line argument that makes no sense
     */
    private class CIMReaderIllegalArgumentException extends CIMReaderException {
		private static final long serialVersionUID = -3208774982004613509L;

		public CIMReaderIllegalArgumentException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * thrown when a host is unreachable
     */
    private class CIMReaderUnreachableHost extends CIMReaderException {
		private static final long serialVersionUID = 1890494682069661937L;

		public CIMReaderUnreachableHost(URL url, IOException e) {
            super("could not talk to: " + url.toString(), e);
        }
    }
}
