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

package com.vmware.cim.helpers;

import com.vmware.connection.Connection;
import com.vmware.vim25.HostServiceTicket;

import javax.cim.CIMObjectPath;
import javax.security.auth.Subject;
import javax.wbem.WBEMException;
import javax.wbem.client.UserPrincipal;
import javax.wbem.client.WBEMClient;
import javax.wbem.client.WBEMClientFactory;
import java.net.URL;
import java.util.Locale;

/**
 * provides helper functions for getting objects needed to work with CIM related services from the initial vSphere
 * connection. This class is designed to be as stand-alone as possible.
 */
public class GetCim {
    public final static String WBEMCLIENT_FORMAT = "CIM-XML";
    public final static String INTEROP_NAMESPACE = "root/interop";
    public final static String IMPL_NAMESPACE = "root/cimv2";

    /**
     * Namespaces are static per server. You may query available namespaces using a
     * service location protocol (SLP) agent or simply use the hard coded namespaces
     * known to be hard coded onto the ESX and ESXi hosts.
     */
    public final static String[] NAMESPACES = new String[] {INTEROP_NAMESPACE,IMPL_NAMESPACE};

    // these values are immutable so they can be public
    public final Connection connection;
    public final String cimHostname;
    public final GetHost getHost;

    /**
     * if you already constructed a getHost utility use this constructor, appropriate for
     * connecting to a vCenter and a managed host.
     * @param connection
     * @param cimHostname
     * @param getHost
     */
    public GetCim(final Connection connection, final String cimHostname, final GetHost getHost) {
        if(cimHostname == null || "".equals(cimHostname)) {
            throw new GetCimRuntimeFault("The CIM host name was not properly set!");
        }
        this.connection = connection;
        this.cimHostname = cimHostname;
        this.getHost = getHost;
    }

    /**
     * Appropriate for connections that are to a vCenter then to a managed host.
     * @param connection
     * @param cimHostname
     */
    public GetCim(final Connection connection, final String cimHostname) {
        this(connection, cimHostname, new GetHost(connection));
    }

    /**
     * Only appropriate for direct connections to ESX hosts
     * @param connection
     */
    public GetCim(final Connection connection) {
        this(connection,connection.getHost(),new GetHost(connection));
    }

    /**
     * works only for vCenter, this grants us a ticket to access a ESX host controlled by our vCenter
     * @return a valid ticket to use for login
     * @see HostServiceTicket
     */
    public HostServiceTicket ticket() {
        getHost.vCenterCheck(); // die if not connected to a vCenter server
        HostServiceTicket ticket = null;

        try {

            ticket = connection
                    .connect()
                    .getVimPort()
                    .acquireCimServicesTicket(
                            getHost.byName(cimHostname)
                    );

        } catch (Throwable t) {
            throw new GetCimRuntimeFault(t);
        }

        return ticket;
    }

    /**
     * Note base + namespace must make sense. There are various formula for creating a valid CIMObjectPath
     * ultimately, these are URI when represented in string form.
     *
     * @param base
     * @param namespace
     * @return object path representing the query
     * @see CIMObjectPath
     */
    public CIMObjectPath baseObjectPath(final URL base, final String namespace) {
        return new CIMObjectPath(base.toString() + namespace);
    }

    /**
     * builds a CIMObjectPath based on URI elements
     * @param base - base URL of the CIM server
     * @param namespace - namespace to read
     * @param cimclassname - name of the class to look at
     * @return fully constructed but CIMObjectPath (will not have properties since it was locally constructed)
     * @see CIMObjectPath
     */
    public CIMObjectPath objectPath(final URL base, final String namespace, final String cimclassname) {
        return new CIMObjectPath(base.toString() + namespace + ":" + cimclassname);
    }

    /**
     * Creates a web client using the HostServiceTicket object.
     * <p/>
     * NOTE: you must be connected to a vCenter for this to make sense
     * @param ticket granted by vCenter
     * @param path the initial path for the client to talk to
     * @return a WBEMClient
     * @see WBEMClient
     */
    public WBEMClient client(final HostServiceTicket ticket, final CIMObjectPath path) {
        return client(
                ticket.getSessionId(), // use sessionId as username
                ticket.getSessionId(), // use sessionId as password
                path
        );
    }

    /**
     * Creates a web client using username, password, and a path
     * @param username
     * @param password
     * @param path
     * @return a web client
     * @see WBEMClient
     */
    public WBEMClient client(final String username, final String password, final CIMObjectPath path) {
        return client(subject(username, password), path);
    }

    /**
     * @see WBEMClient
     * @param subject - a security subject built for authentication on the CIM server
     * @param path    - initial search path
     * @return a client ready to work with CIM data
     * @throws GetCimWBEMException if we cannot initialize the client
     */
    public WBEMClient client(final Subject subject, final CIMObjectPath path) {
        WBEMClient client = WBEMClientFactory.getClient(WBEMCLIENT_FORMAT);

        try {

            client.initialize(path, subject, new Locale[]{new Locale("en")});

        } catch (WBEMException e) {
            throw new GetCimWBEMException(e);
        }

        return client;
    }

    /**
     * creates a web client using all string based parameters
     * @param username
     * @param password
     * @param url
     * @param namespace
     * @param path
     * @return a web client
     * @see WBEMClient
     */
    public WBEMClient client(final String username, final String password, final URL url, final String namespace, final String path) {
        return client(subject(username, password), url, namespace, path);
    }

    /**
     * creates a web client from a ticket, a URL, and a namespace and classname to query
     * @param ticket
     * @param url
     * @param namespace
     * @param classname
     * @return a web client
     * @see WBEMClient
     */
    public WBEMClient client(final HostServiceTicket ticket, final URL url, final String namespace, final String classname) {
        return client(subject(ticket), url, namespace, classname);
    }

    /**
     * if you know how to build your own security subject, use this method instead
     * @param subject
     * @param url
     * @param namespace
     * @param classname
     * @return a client
     * @see WBEMClient
     */
    public WBEMClient client(final Subject subject, final URL url, final String namespace, final String classname) {
        return client(subject, objectPath(url, namespace, classname));
    }

    /**
     * A subject is an authentication construct. The vSphere API allows you to obtain a ticket
     * from the vSphere SDK and use that ticket to construct a subject based on it.
     * <p/>
     * @see WBEMClient
     * @see Subject
     * @return the subject built with the ticket for use in WBEMClient
     */
    public Subject subject(final HostServiceTicket ticket) {
        return subject(ticket.getSessionId(), ticket.getSessionId());
    }

    /**
     * Builds a security subject object based on the principal name and credential you pass.
     * <p/>
     * @param principal
     * @param passwordCredential
     * @return a subject for use in the WBEMClient
     */
    public Subject subject(final String principal, final String passwordCredential) {
        final Subject subject = new Subject();

        subject
                .getPrincipals()
                .add(
                        new UserPrincipal(principal)
                );

        subject
                .getPrivateCredentials()
                .add(
                        new PasswordCredential(passwordCredential)
                );

        return subject;

    }

    public WBEMClient client(HostServiceTicket ticket, URL baseUrl, String namespace) {
        return client(ticket,baseObjectPath(baseUrl,namespace));
    }

    public WBEMClient client(String username, String password, URL baseUrl, String namespace) {
        return client(subject(username,password),baseObjectPath(baseUrl,namespace));
    }

    /**
     * thrown whenever the GetCim helper can't resolve an issue on it's own all exceptions originating from this class
     * are based on this parent exception.
     */
    private class GetCimException extends RuntimeException {
		private static final long serialVersionUID = 1L;
		public GetCimException(Throwable t) {
            super(t);
        }

        public GetCimException(String message) {
            super(message);
        }
    }

    /**
     * Thrown when there was an issue in WBEMClient initialization that was not easily conveyed to a method signature
     */
    private class GetCimWBEMException extends GetCimException {
		private static final long serialVersionUID = 1L;
        public GetCimWBEMException(WBEMException e) {
            super(e);
        }
    }

    /**
     * thrown when the WBEMClient throws an exception we can't deal with locally and need to pass the fault to the user.
     */
    private class GetCimRuntimeFault extends GetCimException {
		private static final long serialVersionUID = 1L;
        public GetCimRuntimeFault(Throwable cause) {
            super(cause);
        }

        public GetCimRuntimeFault(String message) {
            super(message);
        }
    }

    /**
     * The default PasswordCredential will prevent us from using sessionId's that
     * can be over 16 characters in length. Instead use  inheritance to force
     * the PasswordCredential class to hold values longer than 16 chars.
     * <p/>
     * @see javax.wbem.client.PasswordCredential
     */
    public static class PasswordCredential extends javax.wbem.client.PasswordCredential {
        private final String longPassword;
        public PasswordCredential(String userPassword) {
            super("fake password"); // the parent class' password is ignored
            longPassword = userPassword;
        }
        @Override
        public char[] getUserPassword() {
            return longPassword.toCharArray(); // use our long password instead
        }
    }
}
