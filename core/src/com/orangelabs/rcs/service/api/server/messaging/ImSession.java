package com.orangelabs.rcs.service.api.server.messaging;

import com.orangelabs.rcs.core.ims.service.ImsServiceSession;
import com.orangelabs.rcs.service.api.client.messaging.IInstantMessageSession;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Instant message session
 * 
 * @author jexa7410
 */
public class ImSession extends IInstantMessageSession.Stub {
	
	/**
	 * Core session
	 */
	private ImsServiceSession session;

    /**
	 * The logger
	 */
	private Logger logger = Logger.getLogger(this.getClass().getName());

	/**
	 * Constructor
	 * 
	 * @param session Session
	 */
	public ImSession(ImsServiceSession session) {
		this.session = session;
	}

	/**
	 * Get session ID
	 * 
	 * @return Session ID
	 */
	public String getSessionID() {
		return session.getSessionID();
	}
	
	/**
	 * Accept the session invitation
	 */
	public void acceptSession() {
		if (logger.isActivated()) {
			logger.info("Accept session invitation");
		}
		
		// Remove the notification
		// TODO
		
		// Accept invitation
		session.acceptSession();

		// Update IM provider
  		// TODO
	}
	
	/**
	 * Reject the session invitation
	 */
	public void rejectSession() {
		if (logger.isActivated()) {
			logger.info("Reject session invitation");
		}
		
		// Remove the notification
		// TODO

        // Reject invitation
		session.rejectSession();

		// Update IM provider
  		// TODO
	}

	/**
	 * Cancel the session
	 */
	public void cancelSession() {
		if (logger.isActivated()) {
			logger.info("Abort session");
		}
		
		// Abort the session
		session.abortSession();

		// Update IM provider
  		// TODO
	}
}
