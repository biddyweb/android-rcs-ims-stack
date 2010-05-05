package com.orangelabs.rcs.service.api.server;

import com.orangelabs.rcs.core.Core;

/**
 * Server API utils
 * 
 * @author jexa7410
 */
public class ServerApiUtils {
	/**
	 * Test core
	 * 
	 * @throws ServerApiException
	 */
	public static void testCore() throws ServerApiException {
		if (Core.getInstance() == null) {
			throw new ServerApiException("Core is not instanciated");
		}
	}
	
	/**
	 * Test IMS connection
	 * 
	 * @throws ServerApiException
	 */
	public static void testIms() throws ServerApiException {
		if ((Core.getInstance() == null) ||
			(Core.getInstance().getImsModule().getCurrentNetworkInterface() == null) ||
			(!Core.getInstance().getImsModule().getCurrentNetworkInterface().isRegistered())) { 
			throw new ServerApiException("Core is not connected to IMS"); 
		}
	}
}
