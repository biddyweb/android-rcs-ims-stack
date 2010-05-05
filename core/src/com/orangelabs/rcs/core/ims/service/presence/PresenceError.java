package com.orangelabs.rcs.core.ims.service.presence;

/**
 * Presence error
 * 
 * @author jexa7410
 */
public class PresenceError extends Error {
	static final long serialVersionUID = 1L;
	
	/**
	 * Unexpected exception occurs in the module (e.g. internal exception)
	 */
	public final static int UNEXPECTED_EXCEPTION = 0x01;
	
	/**
	 * Subscription has failed (e.g. 404 not found)
	 */
	public final static int SUBSCRIBE_FAILED = 0x02;
	
	/**
	 * Publish has failed (e.g. 408 timeout)
	 */
	public final static int PUBLISH_FAILED = 0x03;
	
	/**
	 * Error code
	 */
	private int code;
	
	/**
	 * Constructor
	 * 
	 * @param code Error code
	 */
	public PresenceError(int code) {
		super();
		
		this.code = code;
	}
	
	/**
	 * Constructor
	 * 
	 * @param code Error code
	 * @param msg Detail message 
	 */
	public PresenceError(int code, String msg) {
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
