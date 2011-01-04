package com.orangelabs.rcs.service.api.client.voip;

/**
 * VoIP event listener
 */
interface IVoIpEventListener {

	// Session is started
	void handleSessionStarted();

	// Session has been aborted
	void handleSessionAborted();
    
	// Session has been terminated
	void handleSessionTerminated();
    
	// Session has been terminated by remote
	void handleSessionTerminatedByRemote();

	// VoIP error
	void handleVoIpError(in int error);
}
