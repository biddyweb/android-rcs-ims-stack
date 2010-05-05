package com.orangelabs.rcs.core.ims.protocol.rtp.core;

/**
 * RTP source
 * 
 * @author jexa7410
 */
public class RtpSource {
	/**
	 * CNAME value
	 */
    public static String CNAME = "anonymous@127.0.0.1";

	/**
	 * SSRC
	 */
    public static int SSRC = RtpSource.generateSSRC();

    /**
     * Generate a unique SSRC value
     * 
     * @return Integer
     */
    private static int generateSSRC() {
        return (int)System.currentTimeMillis();
    }
}
