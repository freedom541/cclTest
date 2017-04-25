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

package com.vmware.security.credstore;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.IOException;
import java.util.Set;

/**
 * A credential store.
 * <p/>
 * <p/>
 * For each method with a {@code host} parameter, the host may be specified as a
 * DNS domain name, an IPv4 address, or an IPv6 address. When looking up a
 * username or password, the host must be specified in the same manner (DNS,
 * IPv4, or IPv6) as when it was stored.
 * <p/>
 * <p/>
 * The methods in this interface may be invoked concurrently by multiple
 * threads.
 *
 * @see CredentialStoreFactory
 */
public interface CredentialStore {

    /**
     * Gets the password for a given host and username.
     *
     * @return the password, or {@code null} if none is found
     */
    char[] getPassword(String host, String username) throws IOException, IllegalBlockSizeException, BadPaddingException;

    /**
     * Stores the password for a given host and username. If a password already
     * exists for that host and username, it is overwritten.
     *
     * @return {@code true} if a password for this host and username did not
     *         already exist
     */
    boolean addPassword(String host, String username, char[] password)
            throws IOException;

    /**
     * Removes the password for a given host and username. If no such password
     * exists, this method has no effect.
     *
     * @return {@code true} if the password existed and was removed
     */
    boolean removePassword(String host, String username) throws IOException;

    /**
     * Removes all passwords.
     */
    void clearPasswords() throws IOException;

    /**
     * Returns all hosts that have entries in the credential store.
     */
    Set<String> getHosts() throws IOException;

    /**
     * Returns all usernames that have passwords stored for a given host.
     */
    Set<String> getUsernames(String host) throws IOException;

    /**
     * Closes this credential store and frees all resources associated with it.
     * No further {@code CredentialStore} methods may be invoked on this object.
     */
    void close() throws IOException;
}
