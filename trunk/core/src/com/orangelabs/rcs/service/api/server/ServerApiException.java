package com.orangelabs.rcs.service.api.server;

import android.os.RemoteException;

/**
 * Server API exception
 * 
 * @author jexa7410
 */
public class ServerApiException extends RemoteException {
	static final long serialVersionUID = 1L;
	
	/**
	 * Constructor
	 * 
	 * @param e Exception
	 */
	public ServerApiException(Exception e) {
		setStackTrace(e.getStackTrace());
	}

	/**
	 * Constructor
	 * 
	 * @param error Error message
	 */
	public ServerApiException(String error) {
		Exception e = new Exception(error);
		this.setStackTrace(e.getStackTrace());		
	}
}
