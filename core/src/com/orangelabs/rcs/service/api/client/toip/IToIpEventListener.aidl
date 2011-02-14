package com.orangelabs.rcs.service.api.client.toip;

/**
 * ToIP event listener
 */
interface IToIpEventListener {

	// Session is started
	void handleSessionStarted();

	// Session has been aborted
	void handleSessionAborted();
    
	// Session has been terminated
	void handleSessionTerminated();
    
	// Session has been terminated by remote
	void handleSessionTerminatedByRemote();

	// ToIP error
	void handleToIpError(in int error);
}
