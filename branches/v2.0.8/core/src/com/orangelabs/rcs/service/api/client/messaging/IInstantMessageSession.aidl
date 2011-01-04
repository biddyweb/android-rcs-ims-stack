package com.orangelabs.rcs.service.api.client.messaging;

/**
 * IM session interface
 */
interface IInstantMessageSession {
	// Get session ID
	String getSessionID();

	// Accept the session
	void acceptSession();

	// Reject the session
	void rejectSession();

	// Cancel the session
	void cancelSession();
}
