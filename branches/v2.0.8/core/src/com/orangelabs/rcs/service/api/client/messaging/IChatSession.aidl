package com.orangelabs.rcs.service.api.client.messaging;

import com.orangelabs.rcs.service.api.client.messaging.IChatEventListener;

/**
 * Chat session interface
 */
interface IChatSession {
	// Get session ID
	String getSessionID();

	// Get remote contact
	String getRemoteContact();
	
	// Get subject
	String getSubject();

	// Accept the session invitation
	void acceptSession();

	// Reject the session invitation
	void rejectSession();

	// Cancel the session
	void cancelSession();

	// Send a text message
	void sendMessage(in String text);

	// Set the is composing status
	void setIsComposingStatus(in boolean status);

	// Add session listener
	void addSessionListener(in IChatEventListener listener);

	// Remove session listener
	void removeSessionListener(in IChatEventListener listener);	
}
