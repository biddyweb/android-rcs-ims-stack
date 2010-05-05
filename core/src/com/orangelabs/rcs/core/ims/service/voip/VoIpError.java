package com.orangelabs.rcs.core.ims.service.voip;

/**
 * VoIP error
 * 
 * @author jexa7410
 */
public class VoIpError extends Error {
	static final long serialVersionUID = 1L;
	
	/**
	 * Unexpected exception occurs in the module (e.g. internal exception)
	 */
	public final static int UNEXPECTED_EXCEPTION = 0x01;
	
	/**
	 * Session initiation has failed (e.g. 408 timeout)
	 */
	public final static int SESSION_INITIATION_FAILED = 0x02;
	
	/**
	 * Session initiation has been declines (e.g. 603 Decline)
	 */
	public final static int SESSION_INITIATION_DECLINED = 0x03;	

	/**
	 * Session initiation has been cancelled (e.g. 487 Session terminated)
	 */
	public final static int SESSION_INITIATION_CANCELLED = 0x04;	
	
	/**
	 * Media has failed
	 */
	public final static int MEDIA_FAILED = 0x05;
	
	/**
	 * Unsupported media type (e.g. codec not supported)
	 */
	public final static int UNSUPPORTED_MEDIA_TYPE = 0x06;
	
	/**
	 * Media player is not initialized
	 */
	public final static int MEDIA_PLAYER_NOT_INITIALIZED = 0x07;

	/**
	 * Media renderer is not initialized
	 */
	public final static int MEDIA_RENDERER_NOT_INITIALIZED = 0x08;

	/**
	 * Error code
	 */
	private int code;
	
	/**
	 * Constructor
	 * 
	 * @param code Error code
	 */
	public VoIpError(int code) {
		super();
		
		this.code = code;
	}
	
	/**
	 * Constructor
	 * 
	 * @param code Error code
	 * @param msg Detail message 
	 */
	public VoIpError(int code, String msg) {
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
