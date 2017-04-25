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

import javax.crypto.*;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.Set;

/**
 * Implementation class for CredentialStoreAdmin.
 */
class CredentialStoreImpl implements CredentialStore {
    private CredentialStoreCipher cypher;

    private File cachePath;
    private boolean defaultCachePath;

    private String vmwareDirName = ".vmware";
    private String credstoreDirName = "credstore";
    private String credstoreFileName = "vicredentials.xml";

    private File getDefaultFilePath() {
        String path;
        if (File.separatorChar == '/') {
            // Linux
            path = System.getenv("HOME");
            if (path == null) {
                throw new IllegalStateException("HOME not set.");
            }
            path +=
                    "/" + vmwareDirName + "/" + credstoreDirName + "/"
                            + credstoreFileName;
        } else if (File.separatorChar == '\\') {
            // Windows
            path = System.getenv("APPDATA");
            if (path == null) {
                throw new IllegalStateException("APPDATA not set.");
            }
            path += "\\VMware\\credstore\\vicredentials.xml";
        } else {
            throw new IllegalStateException("Unknown Operating System");
        }
        return new File(path);
    }

    public CredentialStoreImpl() {
        synchronized (this) {
            cachePath = getDefaultFilePath();
            defaultCachePath = true;
            initialize();
        }
    }

    public CredentialStoreImpl(File file) {
        synchronized (this) {
            if (file == null) {
                cachePath = getDefaultFilePath();
                defaultCachePath = true;
            } else {
                cachePath = file;
                defaultCachePath = false;
            }
            initialize();
        }
    }

    /**
     * Initializes the credential store encryption class.
     * @return
     */
    public CredentialStoreCipher initialize() {
        try {
            CredentialStoreStorage.createCache(cachePath,defaultCachePath);
            SecretKey secretKey = initializeSecretKey(cachePath);
            this.cypher = new CredentialStoreCipher(secretKey);
        } catch (Exception e) {
            throw new CredentialStoreInitializeException(e);
        }
        return this.cypher;
    }

    /**
     * using the supplied path, this method initializes and stores a secret key for use in encrypting
     * the passwords you save. Without the secret key, the password file is useless.
     * @param cachePath
     * @return
     */
    public SecretKey initializeSecretKey(final File cachePath) {
        SecretKey secretKey;
        File keyFile = new File(cachePath.getAbsolutePath()+".key");
        if(keyFile.exists()) {
            try {
                secretKey = loadSecretKey(keyFile);
            } catch (Exception e) {
                throw new CredentialStoreInitializeException(e);
            }
        }
        else {
            try {
                System.err.println("initializing new secret key for encryption");
                if( !keyFile.createNewFile() ) {
                     throw new CredentialStoreInitializeException(
                             "could not create the file " + keyFile.getAbsolutePath()
                     );
                }
                secretKey = CredentialStoreCipher.generateSecretKey();
                saveSecretKey(secretKey,keyFile);
            } catch (NoSuchAlgorithmException e) {
                throw new CredentialStoreInitializeException(e);
            } catch (IOException e) {
                throw new CredentialStoreInitializeException(e);
            }
        }

        return secretKey;
    }

    /**
     * saves the key file for this credential store
     *
     * @param secretKey - the key to save
     * @param keyFile - the place to save it
     * @return - this, for command chaining
     * @throws IOException - if there is a problem loading the file
     */
    public CredentialStoreImpl saveSecretKey(final SecretKey secretKey, final File keyFile) throws IOException {
        OutputStream file = new FileOutputStream(keyFile.getAbsolutePath());
        OutputStream buffer = new BufferedOutputStream(file);
        ObjectOutput output = new ObjectOutputStream(buffer);
        try {
            output.writeObject(secretKey);
        } finally {
            output.close();
        }
        return this;
    }

    /**
     * Loads the key file for this credential store.
     *
     * @param keyFile
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public SecretKey loadSecretKey(File keyFile) throws IOException, ClassNotFoundException {
        InputStream file = new FileInputStream(keyFile.getAbsolutePath());
        InputStream buffer = new BufferedInputStream(file);
        ObjectInput input = new ObjectInputStream(buffer);
        SecretKey key = (SecretKey) input.readObject();
        input.close();
        return key;
    }

    /**
     * Gets the password for a given host and username.
     *
     * @return the password, or {@code null} if none is found
     */
    public synchronized char[] getPassword(String host, String username)
            throws IOException, IllegalBlockSizeException, BadPaddingException {
        CredentialStoreStorage store =
                new CredentialStoreStorage(cachePath, defaultCachePath);

        char[] pwd = store.getPassword(host, username);

        if (pwd == null) {
            return null;
        }
        String cipherText = new String(pwd);
        byte[] plainText = cypher.decrypt(cipherText.getBytes());
        return new String(plainText).toCharArray();
    }

    /**
     * Stores the password for a given host and username. If a password already
     * exists for that host and username, it is overwritten.
     *
     * @return {@code true} if a password for this host and username did not
     *         already exist
     */
    public synchronized boolean addPassword(String host, String username,
                                            char[] password) throws IOException {
        CredentialStoreStorage store =
                new CredentialStoreStorage(cachePath, defaultCachePath);
        String plainText = new String(password);
        byte[] cipherText = cypher.encrypt(plainText.getBytes());
        String cipherString = new String(cipherText);
        return store.addEntry(host, username, cipherString.toCharArray());
    }

    /**
     * Removes the password for a given host and username. If no such password
     * exists, this method has no effect.
     *
     * @return {@code true} if the password existed and was removed
     */
    public synchronized boolean removePassword(String host, String username)
            throws IOException {
        CredentialStoreStorage store =
                new CredentialStoreStorage(cachePath, defaultCachePath);

        return store.deleteEntry(host, username);
    }

    /**
     * Removes all passwords.
     */
    public synchronized void clearPasswords() throws IOException {
        CredentialStoreStorage store =
                new CredentialStoreStorage(cachePath, defaultCachePath);

        store.clearPasswords();
    }

    /**
     * Returns all hosts that have entries in the credential store.
     */
    public synchronized Set<String> getHosts() throws IOException {
        CredentialStoreStorage store =
                new CredentialStoreStorage(cachePath, defaultCachePath);

        return store.getHosts();
    }

    /**
     * Returns all usernames that have passwords stored for a given host.
     */
    public synchronized Set<String> getUsernames(String host) throws IOException {
        CredentialStoreStorage store =
                new CredentialStoreStorage(cachePath, defaultCachePath);

        return store.getUserNames(host);
    }

    /**
     * Closes this credential store and frees all resources associated with it.
     * No further {@code CredentialStore} methods may be invoked on this object.
     */
    public synchronized void close() {
        cachePath = null;
    }

    private class CredentialStoreInitializeException extends RuntimeException {
		private static final long serialVersionUID = 1L;
        public CredentialStoreInitializeException(Throwable cause) {
            super(cause.getMessage(),cause.getCause());
        }

        public CredentialStoreInitializeException(String message) {
            super(message);
        }
    }
}
