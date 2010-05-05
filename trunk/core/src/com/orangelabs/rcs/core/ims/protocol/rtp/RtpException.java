package com.orangelabs.rcs.core.ims.protocol.rtp;

/**
 * RTP exception
 * 
 * @author JM. Auffret
 */
public class RtpException extends java.lang.Exception {
	static final long serialVersionUID = 1L;
	
	/**
	 * Constructor
	 *
	 * @param error Error message
	 */
	public RtpException(String error) {
		super(error);
	}
}