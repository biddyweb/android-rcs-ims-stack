package com.orangelabs.rcs.core.ims;

/**
 * IMS error
 * 
 * @author jexa7410
 */
public class ImsError extends Error {
	static final long serialVersionUID = 1L;
	
	/**
	 * Unexpected exception occurs in the module (e.g. internal exception)
	 */
	public final static int UNEXPECTED_EXCEPTION = 0x01;
	
	/**
	 * Registration has failed
	 */
	public final static int REGISTRATION_FAILED = 0x02;

	/**
	 * Error code
	 */
	private int code;
	
	/**
	 * Constructor
	 * 
	 * @param code Error code
	 */
	public ImsError(int code) {
		super();
		
		this.code = code;
	}
	
	/**
	 * Constructor
	 * 
	 * @param code Error code
	 * @param msg Detail message 
	 */
	public ImsError(int code, String msg) {
		super(msg);
		
		this.code = code;
	}

	/**
	 * Returns the error code
	 * 
	 * @return Error code
	 */
	public int getErrorCode() {
		return code; 
	}
}
