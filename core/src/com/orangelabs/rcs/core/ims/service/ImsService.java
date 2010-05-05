package com.orangelabs.rcs.core.ims.service;

import java.util.Enumeration;
import java.util.Hashtable;

import com.orangelabs.rcs.core.CoreException;
import com.orangelabs.rcs.core.ims.ImsModule;
import com.orangelabs.rcs.utils.Config;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Abstract IMS service
 * 
 * @author jexa7410
 */
public abstract class ImsService {
	/**
	 * Presence service
	 */	
	public static final int PRESENCE_SERVICE = 0;

	/**
	 * Capability service
	 */	
	public static final int CAPABILITY_SERVICE = 1;

	/**
	 * Instant Messaging service
	 */	
	public static final int IM_SERVICE = 2;

	/**
	 * Content sharing service
	 */	
	public static final int CONTENT_SHARING_SERVICE = 3;

	/**
	 * VoIP service
	 */	
	public static final int VOIP_SERVICE = 4;

	/**
	 * ToIP service
	 */	
	public static final int TOIP_SERVICE = 5;

	/**
	 * Configuration
	 */
	private Config config;
	
	/**
	 * Activation flag
	 */
	private boolean activated = true;
	
	/**
	 * IMS module
	 */
	private ImsModule imsModule;
	
    /**
     * List of managed sessions
     */
    private Hashtable<String, ImsServiceSession> sessions = new Hashtable<String, ImsServiceSession>();

	/**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());

    /**
	 * Constructor
	 * 
	 * @param parent IMS module
	 * @param configfile Config filename
	 * @param activated Activation flag
	 * @throws CoreException
	 */
	public ImsService(ImsModule parent, String configfile, boolean activated) throws CoreException {
		this.imsModule = parent;
		this.activated = activated;
		this.config = new Config(configfile);
	}

	/**
	 * Returns the configuration of the service
	 * 
	 * @return Config
	 */
	public Config getConfig() {
		return config;
	}

	/**
	 * Is service activated
	 * 
	 * @return Boolean
	 */
	public boolean isActivated() {
		return activated;
	}

	/**
	 * Change the activation flag of the service
	 * 
	 * @param activated Activation flag
	 */
	public void setActivated(boolean activated) {
		this.activated = activated;
	}

	/**
	 * Returns the IMS module
	 * 
	 * @return IMS module
	 */
	public ImsModule getImsModule() {
		return imsModule;
	}
	
	/**
	 * Returns a session
	 * 
	 * @param id Session ID
	 * @return Session
	 */
	public ImsServiceSession getSession(String id) {
		return (ImsServiceSession)sessions.get(id);
	}	

	/**
	 * Returns the list of sessions
	 * 
	 * @return List of sessions
	 */
	public Enumeration<ImsServiceSession> getSessions() {
		return sessions.elements();
	}	

	/**
	 * Returns the number of sessions in progress
	 * 
	 * @return Number of sessions
	 */
	public int getNumberOfSessions() {
		return sessions.size();
	}	

	/**
	 * Add a session
	 * 
	 * @param session Session
	 */
	public void addSession(ImsServiceSession session) {
		if (logger.isActivated()) {
			logger.debug("Add new session " + session.getSessionID());
		}
		sessions.put(session.getSessionID(), session);
	}	

	/**
	 * Remove a session
	 * 
	 * @param session Session
	 */
	public void removeSession(ImsServiceSession session) {
		if (logger.isActivated()) {
			logger.debug("Remove session " + session.getSessionID());
		}
		sessions.remove(session.getSessionID());
	}	

	/**
	 * Remove a session
	 * 
	 * @param id Session ID
	 */
	public void removeSession(String id) {
		if (logger.isActivated()) {
			logger.debug("Remove session " + id);
		}
		sessions.remove(id);
	}	

	/**
	 * Start the IMS service 
	 */
	public abstract void start();

	/**
	 * Stop the IMS service 
	 */
	public abstract void stop();
	
	/**
	 * Check the IMS service 
	 */
	public abstract void check();
}
