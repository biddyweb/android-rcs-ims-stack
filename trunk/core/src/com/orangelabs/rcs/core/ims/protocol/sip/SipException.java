package com.orangelabs.rcs.core.ims.protocol.sip;

/**
 * SIP module exception
 * 
 * @author JM. Auffret
 */
public class SipException extends java.lang.Exception {
	static final long serialVersionUID = 1L;
	
	/**
	 * Constructor
	 *
	 * @param error Error message
	 */
	public SipException(String error) {
		super(error);
	}
}