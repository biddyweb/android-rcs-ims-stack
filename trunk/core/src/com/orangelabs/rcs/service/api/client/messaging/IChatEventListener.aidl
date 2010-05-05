package com.orangelabs.rcs.service.api.client.messaging;

import com.orangelabs.rcs.core.ims.service.im.InstantMessage;

/**
 * Chat event listener
 */
interface IChatEventListener {
	// Session is started
	void handleSessionStarted();

	// Session has been aborted
	void handleSessionAborted();
    
	// Session has been terminated
	void handleSessionTerminated();
    
	// Session has been terminated by remote
	void handleSessionTerminatedByRemote();

	// New text message received
	void handleReceiveMessage(in InstantMessage msg);
}
