package com.orangelabs.rcs.service.api.client.toip;

import com.orangelabs.rcs.service.api.client.toip.IToIpEventListener;
import com.orangelabs.rcs.service.api.client.media.IMediaPlayer;
import com.orangelabs.rcs.service.api.client.media.IMediaRenderer;

/**
 * ToIP session interface
 */
interface IToIpSession {
	// Get session ID
	String getSessionID();

	// Get remote contact
	String getRemoteContact();
	
	// Accept the session invitation
	void acceptSession();

	// Reject the session invitation
	void rejectSession();

	// Cancel the session
	void cancelSession();

	// Set the media player
	void setMediaPlayer(in IMediaPlayer player);

	// Set the media renderer
	void setMediaRenderer(in IMediaRenderer renderer);

	// Add session listener
	void addSessionListener(in IToIpEventListener listener);

	// Remove session listener
	void removeSessionListener(in IToIpEventListener listener);
}
