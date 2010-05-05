package com.orangelabs.rcs.core.ims.service.presence.xdm;


/**
 * HTTP utility functions
 */
public class HttpUtils {
	/**
	 * CRLF constant
	 */
	public final static String CRLF = "\r\n";
	
	/**
	 * Encode special characters in URL
	 * 
	 * @param url URL to be encoded
	 * @return Encoded URL
	 */
	public static String encodeURL(String url) {
		StringBuffer res = new StringBuffer(url.length());
		char readChar;
		for (int i = 0; i < url.length(); i++) {
			readChar = url.charAt(i);
			switch (readChar) {
			case '+':
				res.append("%2B");
				break;
			case '@':
				res.append("%40");
				break;
			case ':':
				res.append("%3A");
				break;
			default:
				res.append(readChar);
				break;
			}
		}
		return res.toString();
	}
}