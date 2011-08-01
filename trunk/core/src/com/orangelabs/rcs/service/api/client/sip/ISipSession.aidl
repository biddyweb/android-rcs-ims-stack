package com.orangelabs.rcs.service.api.client.sip;

import com.orangelabs.rcs.service.api.client.sip.ISipSessionEventListener;

/**
 * SIP session interface
 */
interface ISipSession {
	// Get session ID
	String getSessionID();

	// Get remote contact
	String getRemoteContact();
	
	// Get feature tags of the service
	String[] getFeatureTags();

	// Accept the session invitation
	void acceptSession();

	// Reject the session invitation
	void rejectSession();

	// Cancel the session
	void cancelSession();

	// Add session listener
	void addSessionListener(in ISipSessionEventListener listener);

	// Remove session listener
	void removeSessionListener(in ISipSessionEventListener listener);
}

