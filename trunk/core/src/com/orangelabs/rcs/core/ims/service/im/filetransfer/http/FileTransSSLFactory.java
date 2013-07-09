package com.orangelabs.rcs.core.ims.service.im.filetransfer.http;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * SSL Factory created for file tranfer
 * 
 * @author hhff3235
 */
public class FileTransSSLFactory {

    static private SSLContext sslcontext = null;
    
    static
    {
    	try {
    		sslcontext = SSLContext.getInstance("TLS");
		} catch (NoSuchAlgorithmException e) {
			// TODO Warn that the context init has failed
			e.printStackTrace();
		}
    }
    
    /**
     * Get a SSL context generated with a trust all manager
     *
     * @return SSLContext
     * @return XML result or null if fails
     */
    static public SSLContext getFileTransferSSLContext()
    {
    	try {
			sslcontext.init(null, new TrustManager[]{ new AllTrustManager()}, new SecureRandom());
		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		return sslcontext;
    }
    
    public static class AllTrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
            // TODO Auto-generated method stub

        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
            // TODO Auto-generated method stub

        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            // TODO Auto-generated method stub
            return new X509Certificate[0];
        }

    }
    
}
