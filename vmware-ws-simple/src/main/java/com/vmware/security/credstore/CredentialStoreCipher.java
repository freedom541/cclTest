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
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * This class uses the JVM provided classes in javax.crypto.* to encrypt and decrypt
 * text. It uses the RC4 symmetric cipher by default allowing you to store text
 * and decrypt the same text for review later.
 * <p/>
 * The SecretKey is a vital private key used for encrypting and decrypting the cipher
 * text produced by this class. If you lose the secret key, then the data is lost.
 * <p/>
 * See Appendix A in the Java Cryptography Extension Reference Guide for more information.
 * <p/>
 * @see SecretKey
 * @see Cipher
 * @see CredentialStoreImpl
 */
public class CredentialStoreCipher {
    /**
     * The RC4 encryption algorithm is supplied by the JVM. If your JVM does not support RC4 you
     * may have to specify a different algorithm name here.
     * <p/>
     * See Appendix A in the Java Cryptography Extension Reference Guide for more information.
     */
    public final static String ALGORITHM = "RC4";

    final Cipher cipher;

    public CredentialStoreCipher(final SecretKey secretKey)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
    }

    /**
     * This helper method will generate a fresh secret key for you to use. If this key is lost, then
     * the information encrypted with this cipher is also lost.
     * @return a key object for use with this class
     * @throws NoSuchAlgorithmException - thrown if your JVM does not support RC4 encryption
     */
    public static SecretKey generateSecretKey() throws NoSuchAlgorithmException {
        final KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
        return keyGen.generateKey();
    }

    /**
     * Uses the RC4 Encrypt and Decrypt routines found in the javax.crypto package.
     */
    public byte[] encrypt(final byte[] input) {
        try {
            return cipher.doFinal(input);
        } catch (GeneralSecurityException e) {
            throw new CryptoGraphicException(e);
        }
    }

    public byte[] decrypt(final byte[] input) {
        try {
            return cipher.doFinal(input);
        } catch (GeneralSecurityException e) {
            throw new CryptoGraphicException(e);
        }
    }

    private class CryptoGraphicException extends IllegalStateException {
		private static final long serialVersionUID = 1L;
        public CryptoGraphicException(GeneralSecurityException e) {
            super(e);
        }
    }
}
