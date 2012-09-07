/*******************************************************************************
 * Software Name : RCS IMS Stack
 *
 * Copyright (C) 2010 France Telecom S.A.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.orangelabs.rcs.core.ims.protocol.sip;

import com.orangelabs.rcs.platform.AndroidFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

/**
 * KeyStore manager for secure connection.
 * 
 * @author B. JOGUET
 */
public class KeyStoreManager {

    /**
     * Keystore name
     */
    public final static String KEYSTORE_NAME = "rcs_keystore.jks";

    /**
     * Keystore password
     */
    public final static String KEYSTORE_PASSWORD = "01RCSrcs";

    /**
     * Keystore type
     */
    public final static String KEYSTORE_TYPE = KeyStore.getDefaultType();

    /**
     * Private key alias
     */
    public final static String PRIVATE_KEY_ALIAS = "MyPrivateKey";

    /**
     * returns keystore path.
     * 
     * @return keystore path
     */
    public static String getKeystorePath() {
        return AndroidFactory.getApplicationContext().getFilesDir().getAbsolutePath() + "/"
                + KEYSTORE_NAME;
    }

    /**
     * Test if a keystore is created.
     * 
     * @return true if already created.
     */
    public static boolean isKeystoreExists(String path) throws KeyStoreManagerException {
        // Test file 
        File file = new File(path);
        if ((file == null) || (!file.exists()))
            return false;
        
        // Test keystore
        FileInputStream fis = null;
        boolean result = false;
        try {
            // Try to open the keystore
            fis = new FileInputStream(path);
            KeyStore ks = KeyStore.getInstance(KEYSTORE_TYPE);
            ks.load(fis, KEYSTORE_PASSWORD.toCharArray());
            result = true;
        } catch (FileNotFoundException e) {
            throw new KeyStoreManagerException(e.getMessage());
        } catch (Exception e) {
            result = false;
        } finally {
            try {
                if (fis != null)
                    fis.close();
            } catch (IOException e) {
                // Intentionally blank
            }
        }
        return result;
    }

    /**
     * Create the RCS keystore.
     * 
     * @throws Exception
     */
    public static void createKeyStore() throws KeyStoreManagerException {
        File file = new File(getKeystorePath());
        if ((file == null) || (!file.exists())) {
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(getKeystorePath());
                // Build empty keystore
                KeyStore ks = KeyStore.getInstance(KEYSTORE_TYPE);
                ks.load(null, KEYSTORE_PASSWORD.toCharArray());
                // Export keystore in a file
                ks.store(fos, KEYSTORE_PASSWORD.toCharArray());
            } catch (Exception e) {
                throw new KeyStoreManagerException(e.getMessage());
            } finally {
                try {
                    if (fos != null)
                        fos.close();
                } catch (IOException e) {
                    // Intentionally blank
                }
            }
        }
    }

    /**
     * Check if a certificate is in the keystore.
     * 
     * @param path certificate path
     * @return true if available
     * @throws Exception
     */
    public static boolean isCertificateEntry(String path) throws KeyStoreManagerException {
        FileInputStream fis = null;
        boolean result = false;
        if (KeyStoreManager.isKeystoreExists(getKeystorePath())) {
            try {
                fis = new FileInputStream(getKeystorePath());
                // Open the existing keystore
                KeyStore ks = KeyStore.getInstance(KEYSTORE_TYPE);
                ks.load(fis, KEYSTORE_PASSWORD.toCharArray());
                // isCertificateEntry
                result = ks.isCertificateEntry(buildCertificateAlias(path));
            } catch (Exception e) {
                throw new KeyStoreManagerException(e.getMessage());
            } finally {
                try {
                    if (fis != null)
                        fis.close();
                } catch (IOException e) {
                    // Intentionally blank
                }
            }
        } 
        return result;
    }

    /**
     * Add a certificate in the keystore
     * 
     * @param alias certificate alias
     * @param path certificate path
     * @throws Exception
     */
    public static void addCertificate(String path) throws KeyStoreManagerException {
        if (KeyStoreManager.isKeystoreExists(getKeystorePath())) {
            FileInputStream fis = null;
            FileOutputStream fos = null;
            try {
                // Open the existing keystore
                fis = new FileInputStream(getKeystorePath());
                KeyStore ks = KeyStore.getInstance(KEYSTORE_TYPE);
                ks.load(fis, KEYSTORE_PASSWORD.toCharArray());
    
                // Get certificate and add in keystore
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                InputStream inStream = new FileInputStream(path);
                X509Certificate cert = (X509Certificate) cf.generateCertificate(inStream);
                inStream.close();
                ks.setCertificateEntry(buildCertificateAlias(path), cert);
    
                // save the keystore
                fos = new FileOutputStream(getKeystorePath());
                ks.store(fos, KEYSTORE_PASSWORD.toCharArray());
            } catch (Exception e) {
                throw new KeyStoreManagerException(e.getMessage());
            } finally {
                try {
                    if (fis != null)
                        fis.close();
                } catch (IOException e) {
                    // Intentionally blank
                }
                try {
                    if (fos != null)
                        fos.close();
                } catch (IOException e) {
                    // Intentionally blank
                }
            }
        }
    }

    /**
     * Build alias from path
     * 
     * @param path file path
     * @return the alias
     */
    private static String buildCertificateAlias(String path) {
        String alias = "";
        File file = new File(path);
        String filename = file.getName();
        long lastModified = file.lastModified();
        int lastDotPosition = filename.lastIndexOf('.');
        if (lastDotPosition > 0)
            alias = filename.substring(0, lastDotPosition) + lastModified;
        else
            alias = filename + lastModified;
        return alias;
    }

    // /**
    // * Initialize a private key with self signed certificate.
    // *
    // * @throws Exception
    // */
    // @SuppressWarnings("deprecation")
    // public static void initPrivateKeyAndSelfsignedCertificate() throws
    // Exception {
    // if (KeyStoreManager.isKeystoreExists(getKeystorePath())) {
    // // Open the existing keystore
    // KeyStore ks = KeyStore.getInstance(KEYSTORE_TYPE);
    // ks.load(new FileInputStream(getKeystorePath()),
    // KEYSTORE_PASSWORD.toCharArray());
    //
    // // is Private Key not exists
    // if (!ks.isKeyEntry(PRIVATE_KEY_ALIAS)) {
    // // Generate Key
    // KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
    // kpg.initialize(1024);
    // KeyPair kp = kpg.generateKeyPair();
    //
    // // Generate certificate
    // long currentTime = System.currentTimeMillis();
    // X509V3CertificateGenerator v3CertGen = new X509V3CertificateGenerator();
    // v3CertGen.setSerialNumber(new BigInteger(Long.toString(currentTime)));
    // v3CertGen.setIssuerDN(new X509Principal(
    // "CN=OrangeLabs, OU=None, O=None, L=None, C=None"));
    // v3CertGen.setNotBefore(new Date(currentTime - 1000L * 60 * 60 * 24 *
    // 30));
    // v3CertGen.setNotAfter(new Date(currentTime + (1000L * 60 * 60 * 24 * 365
    // * 10)));
    // v3CertGen.setSubjectDN(new X509Principal(
    // "CN=OrangeLabs, OU=None, O=None, L=None, C=None"));
    // v3CertGen.setPublicKey(kp.getPublic());
    // v3CertGen.setSignatureAlgorithm("MD5WithRSAEncryption");
    // X509Certificate cert =
    // v3CertGen.generateX509Certificate(kp.getPrivate());
    //
    // // Add the private key with cert in keystore
    // ks.setKeyEntry(PRIVATE_KEY_ALIAS, kp.getPrivate(),
    // KEYSTORE_PASSWORD.toCharArray(),
    // new Certificate[] {
    // cert
    // });
    //
    // // save the keystore
    // ks.store(new FileOutputStream(getKeystorePath()),
    // KEYSTORE_PASSWORD.toCharArray());
    // }
    // }
    // }
}
