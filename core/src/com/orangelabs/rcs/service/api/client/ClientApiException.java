package com.orangelabs.rcs.service.api.client;

/**
 * Client API exception
 * 
 * @author jexa7410
 */
public class ClientApiException extends java.lang.Exception {
	static final long serialVersionUID = 1L;
	
	/**
	 * Constructor
	 *
	 * @param error Error message
	 */
	public ClientApiException(String error) {
		super(error);
	}
}