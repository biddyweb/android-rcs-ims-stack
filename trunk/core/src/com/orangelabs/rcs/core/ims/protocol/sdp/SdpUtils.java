package com.orangelabs.rcs.core.ims.protocol.sdp;

/**
 * SDP utility functions
 * 
 * @author jexa7410
 */
public class SdpUtils {
	/**
	 * Extract the remote host address from the connection info item
	 * 
	 * @param connectionInfo Connection info
	 * @return Address
	 */
	public static String extractRemoteHost(String connectionInfo) {
		// c=IN IP4 172.20.138.145
		int index = connectionInfo.indexOf(" ", 3);
		if (index != -1)
			return connectionInfo.substring(index+1);
		else
			return null;
	}
}
