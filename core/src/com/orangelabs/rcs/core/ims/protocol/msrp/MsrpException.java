package com.orangelabs.rcs.core.ims.protocol.msrp;

/**
 * MSRP exception
 * 
 * @author jexa7410
 */
public class MsrpException extends Exception {
	static final long serialVersionUID = 1L;
	
	/**
	 * Constructor
	 *
	 * @param error Error message
	 */
	public MsrpException(String error) {
		super(error);
	}
}
