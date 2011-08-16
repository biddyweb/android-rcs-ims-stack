/*******************************************************************************
 * Software Name : RCS IMS Stack
 *
 * Copyright © 2010 France Telecom S.A.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.orangelabs.rcs.core.ims.service;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import com.orangelabs.rcs.core.Config;
import com.orangelabs.rcs.core.CoreException;
import com.orangelabs.rcs.core.ims.ImsModule;
import com.orangelabs.rcs.utils.PhoneUtils;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Abstract IMS service
 * 
 * @author jexa7410
 */
public abstract class ImsService {
	/**
	 * Capability service
	 */	
	public static final int CAPABILITY_SERVICE = 0;

	/**
	 * Instant Messaging service
	 */	
	public static final int IM_SERVICE = 1;

	/**
	 * Content sharing service
	 */	
	public static final int CONTENT_SHARING_SERVICE = 2;

	/**
	 * Richcall service
	 */	
	public static final int RICHCALL_SERVICE = 3;

	/**
	 * Presence service
	 */	
	public static final int PRESENCE_SERVICE = 4;

	/**
	 * SIP service
	 */	
	public static final int SIP_SERVICE = 5;

	/**
	 * Configuration
	 */
	private Config config;
	
	/**
	 * Activation flag
	 */
	private boolean activated = true;
	
	/**
	 * Service state
	 */
	private boolean started = false;

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
	 * Returns sessions associated to a contact
	 * 
	 * @param contact Contact number 
	 * @return List of sessions
	 */
	public Enumeration<ImsServiceSession> getSessions(String contact) {
		Vector<ImsServiceSession> result = new Vector<ImsServiceSession>();
		Enumeration<ImsServiceSession> list = sessions.elements();
		while(list.hasMoreElements()) {
			ImsServiceSession session = list.nextElement();
			if (PhoneUtils.compareNumbers(session.getRemoteContact(), contact)){
				result.add(session);
			}
		}
		
		return result.elements();
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
	 * Is service started
	 * 
	 * @return Boolean
	 */
	public boolean isServiceStarted() {
		return started;
	}
	
	/**
	 * Set service state
	 * 
	 * @param state State
	 */
	public void setServiceStarted(boolean state) {
		started = state;
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
