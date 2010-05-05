package com.orangelabs.rcs.core.ims.service.im;

/**
 * Instant message error
 * 
 * @author jexa7410
 */
public class InstantMessageError extends Error {
	static final long serialVersionUID = 1L;
	
	/**
	 * Unexpected exception occurs in the module (e.g. internal exception)
	 */
	public final static int UNEXPECTED_EXCEPTION = 0x01;
	
	/**
	 * IM pager mode has failed (e.g. 404 not found)
	 */
	public final static int IM_PAGER_FAILED = 0x02;
	
	/**
	 * IM session initiation has failed (e.g. 408 timeout)
	 */
	public final static int SESSION_INITIATION_FAILED = 0x03;

	/**
	 * IM session has been terminated by the remote (e.g. incoming BYE)
	 */
	public final static int SESSION_TERMINATED_BY_REMOTE = 0x04;

	/**
	 * Message transfer has failed (e.g. MSRP failure)
	 */
	public final static int MSG_TRANSFER_FAILED = 0x05;

	/**
	 * Error code
	 */
	private int code;
	
	/**
	 * Constructor
	 * 
	 * @param code Error code
	 */
	public InstantMessageError(int code) {
		super();
		
		this.code = code;
	}
	
	/**
	 * Constructor
	 * 
	 * @param code Error code
	 * @param msg Detail message 
	 */
	public InstantMessageError(int code, String msg) {
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
