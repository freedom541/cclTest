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
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.VimPortType;

/**
 * This class shows how to get a ManagedObjectReference to a host using its name or IP address.
 * While this class is designed for use by the CIM samples, it is generic enough to be useful
 * in other contexts. Other utilities in this class deal with the vim25 connection object
 * fed to it. This class is immutable.
 */
public class GetHost {
    public final Connection connection;

    /**
     * this class works with a connection object and is immutable against this object
     *
     * @param connection the connection we are working with
     */
    public GetHost(final Connection connection) {
        this.connection = connection;
    }

    /**
     * takes a candidate hostname or IP address and returns true if the address is useful
     * <p/>
     * NOTE: this is IPv6 compatible
     * <p/>
     *
     * @param value
     * @return true if is an address that is not multicast and not the local loopback
     */
    public boolean isIpAddress(final String value) {
        java.net.InetAddress address = address(value);
        return address != null && !address.isMulticastAddress() && !address.isLoopbackAddress();
    }

    /**
     * @param value a possible hostname or IP address
     * @return an inet address object build from this hostname or IP address
     * @see java.net.InetAddress
     */
    public static java.net.InetAddress address(final String value) {
        java.net.InetAddress address = null;
        try {
            address = java.net.InetAddress.getByName(value);
        } catch (java.net.UnknownHostException e) {
            address = null;
        }
        return address;
    }

    /**
     * uses the InetAddress API in Java to process either a hostname or an IP
     * the return result is a string delimited by "/" that holds the DNS name (if found)
     * and the IP address (if found) of the result. A string with just "/" is returned
     *
     * @param address a potential ip or hostname
     * @return a string "hostname/ipaddress" or "/" when nothing found
     */
    public static String resolve(final java.net.InetAddress address) {
        return (address != null) ? address.toString() : "/";
    }

    /**
     * takes a candidate ipaddress or host name and returns a validated IP address or hostname
     *
     * @param address - candidate value
     * @return the IP as a string or an empty string if no IP found
     */
    public static String ipAddress(final java.net.InetAddress address) {
        final String resolved = resolve(address);
        final String[] parts = resolved.split("/");
        // parts[0] will be the name, parts[1] will be the IP address
        return (parts != null && parts.length == 2) ? parts[1] : "";
    }

    /**
     * given a hostname or IP address, returns the managed object ref for that host using
     * the connection fed to the constructor.
     * <p/>
     *
     * @param hostname
     * @return the matching managed object reference or null
     */
    public ManagedObjectReference byName(String hostname) {
        ManagedObjectReference host = null;
        VimPortType vimPort = connection.getVimPort();
        ManagedObjectReference searchIndex = connection.getServiceContent().getSearchIndex();
        try {
            if (isIpAddress(hostname)) {
                final java.net.InetAddress inetAddress = address(hostname);
                final String ipAddress = ipAddress(inetAddress);
                if ("".equals(ipAddress)) {
                    throw new GetHostRuntimeFault(String.format(
                            "could not properly verify '%s' as an IP address", hostname
                    ));
                }
                // find by IP uses a subtly different method call, assuming you can resolve the IP
                // you could just use this method all the time... but you can't always reach a host
                // from your remote execution context ... and you may have the name.
                host = vimPort.findByIp(searchIndex, null, ipAddress, false);
            } else {
                // a host name could be anything in this scenario since we are talking about
                // a name that could potentially be an alias not actually reachable in our
                // local command-line context, we'll give it a shot and see if we find anything...
                host = vimPort.findByDnsName(searchIndex, null, hostname, false);
            }
        } catch (RuntimeFaultFaultMsg runtimeFaultFaultMsg) {
            throw new GetHostRuntimeFault(runtimeFaultFaultMsg);
        }

        return host;
    }

    /**
     * throws an IsNotvCenterCheck if we are NOT talking to a vCenter instance
     *
     * @return this instance of the GetHost object for command chaining
     * @see IsNotvCenterException
     */
    public GetHost vCenterCheck() {
        final String type = getType();
        if (!type.equals("VirtualCenter")) {
            throw new IsNotvCenterException(connection.getHost() + " is " + type);
        }
        return this;
    }

    /**
     * throws an IsNotHostAgentException if we are NOT talking to an ESX host
     *
     * @return this instance of the GetHost object for command chaining
     * @see IsNotHostAgentException
     */
    public GetHost hostCheck() {
        final String type = getType();
        if (type.equals("VirtualCenter")) {
            throw new IsNotHostAgentException(connection.getHost() + " is " + type);
        }
        return this;
    }

    /**
     * returns the connection type
     */
    public String getType() {
        String type;
        try {
            type = connection.connect().getServiceContent().getAbout().getApiType();
        } catch (Throwable t) {
            type = "";
        }
        return type;
    }

    /**
     * thrown when this class has trouble at run-time
     */
    private class GetHostRuntimeFault extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public GetHostRuntimeFault(Throwable throwable) {
            super(throwable);
        }

        public GetHostRuntimeFault(String message) {
            super(message);
        }

        public GetHostRuntimeFault(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * thrown when this class must assert that a vCenter connection is
     * in use.
     */
    public class IsNotvCenterException extends GetHostRuntimeFault {
        private static final long serialVersionUID = 1L;

        public IsNotvCenterException(String message) {
            super(message);
        }
    }

    /**
     * thrown when this class must assert that a Host is in use.
     */
    public class IsNotHostAgentException extends GetHostRuntimeFault {
        private static final long serialVersionUID = 1L;

        public IsNotHostAgentException(String message) {
            super(message);
        }
    }
}
