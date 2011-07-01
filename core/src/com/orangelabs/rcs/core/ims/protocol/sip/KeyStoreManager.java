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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
     * Keystore path
     */
    public final static String PATH = "/sdcard/rcs_keystore.jks";

    /**
     * Keystore password
     */
    public final static String PASSWORD = "01RCSrcs";

    /**
     * Keystore type
     */
    public final static String TYPE = KeyStore.getDefaultType();

    /**
     * Test if the RCS keystore is created.
     * 
     * @return true if already created.
     */
    public static boolean exists() {
        File file = new File(PATH);
        if ((file == null) || (!file.exists()))
            return false;
        try {
            KeyStore ks = KeyStore.getInstance(TYPE);
            ks.load(new FileInputStream(PATH), PASSWORD.toCharArray());
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * Create the RCS keystore.
     * 
     * @throws Exception
     */
    public static void createKeyStore() throws Exception {
        File file = new File(PATH);
        if ((file == null) || (!file.exists())) {
            // Build empty keystore
            KeyStore ks = KeyStore.getInstance(TYPE);
            ks.load(null, PASSWORD.toCharArray());

            // Export keystore in a file
            ks.store(new FileOutputStream(PATH), PASSWORD.toCharArray());
        }
    }

    /**
     * Check if a certificate is in the keystore.
     * 
     * @param path certificate path
     * @return true if available
     * @throws Exception
     */
    public static boolean isCertificateEntry(String path) throws Exception {
        if (KeyStoreManager.exists()) {
            // Open the existing keystore
            KeyStore ks = KeyStore.getInstance(TYPE);
            ks.load(new FileInputStream(PATH), PASSWORD.toCharArray());
            // isCertificateEntry
            return ks.isCertificateEntry(buildAlias(path));
        }
        return false;
    }

    /**
     * Add a certificate in the keystore
     * 
     * @param alias certificate alias
     * @param path certificate path
     * @throws Exception
     */
    public static void addCertificate(String path) throws Exception {
        if (KeyStoreManager.exists()) {
            // Open the existing keystore
            KeyStore ks = KeyStore.getInstance(TYPE);
            ks.load(new FileInputStream(PATH), PASSWORD.toCharArray());

            // Get certificate and add in keystore
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            InputStream inStream = new FileInputStream(path);
            X509Certificate cert = (X509Certificate) cf.generateCertificate(inStream);
            inStream.close();
            ks.setCertificateEntry(buildAlias(path), cert);

            // save the keystore
            ks.store(new FileOutputStream(PATH), PASSWORD.toCharArray());
        }
    }

    /**
     * Build alias from path
     * 
     * @param path file path
     * @return the alias
     */
    private static String buildAlias(String path) {
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

}
