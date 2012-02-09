package com.orangelabs.rcs.core.ims.service.capability;

import com.orangelabs.rcs.core.ims.service.ImsServiceError;

/**
 * Capability error
 * 
 * @author jexa7410
 */
public class CapabilityError extends ImsServiceError {
	static final long serialVersionUID = 1L;
	
	/**
	 * Unexpected exception occurs in the module (e.g. internal exception)
	 */
	public final static int UNEXPECTED_EXCEPTION = 0x01;
	
	/**
	 * Options has failed
	 */
	public final static int OPTIONS_FAILED = 0x02;	
	
	/**
	 * Constructor
	 * 
	 * @param code Error code
	 */
	public CapabilityError(int code) {
		super(code);
	}
	
	/**
	 * Constructor
	 * 
	 * @param code Error code
	 * @param msg Detail message 
	 */
	public CapabilityError(int code, String msg) {
		super(code, msg);
	}
}
