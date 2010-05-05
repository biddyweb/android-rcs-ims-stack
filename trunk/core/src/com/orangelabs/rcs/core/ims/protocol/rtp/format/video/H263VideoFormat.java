package com.orangelabs.rcs.core.ims.protocol.rtp.format.video;

/**
 * H263-2000 (h263++) video format
 * 
 * @author jexa7410
 */
public class H263VideoFormat extends VideoFormat {

	/**
	 * Encoding name
	 */
	public static final String ENCODING = "h263-2000";
	
	/**
	 * Payload type
	 */
	public static final int PAYLOAD = 96;
	
	/**
	 * Constructor
	 */
	public H263VideoFormat() {
		super(ENCODING, PAYLOAD);
	}
}
