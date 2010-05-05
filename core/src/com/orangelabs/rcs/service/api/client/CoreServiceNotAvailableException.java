package com.orangelabs.rcs.service.api.client;

/**
 * Core service not available exception
 * 
 * @author jexa7410
 */
public class CoreServiceNotAvailableException extends ClientApiException {
	static final long serialVersionUID = 1L;
	
	/**
	 * Constructor
	 */
	public CoreServiceNotAvailableException() {
		super("Core service not available");
	}
}