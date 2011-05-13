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

import com.orangelabs.rcs.platform.registry.RegistryFactory;
import com.orangelabs.rcs.provider.settings.RcsSettings;
import com.orangelabs.rcs.utils.PeriodicRefresher;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Session timer manager
 * 
 * @author JM. Auffret
 */
public class SessionTimerManager extends PeriodicRefresher {
	/**
	 * Last min expire period (in seconds)
	 */
	private static final String REGISTRY_MIN_EXPIRE_PERIOD = "MinSessionRefreshExpirePeriod";
	
	/**
	 * Session to be refreshed
	 */
	private ImsServiceSession session;
	
	/**
	 * Default expire period
	 */
	private int defaultExpirePeriod;
	
    /**
     * Expire period
     */
    private int expirePeriod;
    
	/**
	 * The logger
	 */
	private final Logger logger = Logger.getLogger(this.getClass().getName());
	
	/**
	 * Constructor
	 * 
	 * @param session Session to be refreshed
	 */
	public SessionTimerManager(ImsServiceSession session, int expirePeriod) {
		this.session = session;
		
		// TODO: remove attribut defaultExpirePeriod
    	int defaultExpirePeriod = RcsSettings.getInstance().getSessionRefreshExpirePeriod();
    	int minExpireValue = RegistryFactory.getFactory().readInteger(REGISTRY_MIN_EXPIRE_PERIOD, -1);
    	if ((minExpireValue != -1) && (defaultExpirePeriod < minExpireValue)) {
        	this.expirePeriod = minExpireValue;
    	} else {
    		this.expirePeriod = defaultExpirePeriod;
    	}
	}
	
	/**
	 * Terminate the manager
	 */
	public void terminate() {
		stop();
	}
	
	/**
	 * Start the session timer
	 */
	public void start() {
		if (logger.isActivated()) {
			logger.info("Start session timer for session " + session.getId());
		}
		startTimer(expirePeriod, 0.5);
	}
	
	/**
	 * Stop the session timer
	 */
	public void stop() {
		if (logger.isActivated()) {
			logger.info("Stop session timer for session " + session.getId());
		}
		stopTimer();
	}
	
	/**
	 * Set the expire period
	 * 
	 * @param period Expire period in seconds
	 */
	public void setExpirePeriod(int period) {
		if (period != -1)
			expirePeriod = period;
		else
			expirePeriod = defaultExpirePeriod;
	}
	
	/**
	 * Return the expire period
	 * 
	 * @return Expire period in seconds
	 */
	public int getExpirePeriod() {
		return expirePeriod;
	}
	
	/**
	 * Periodic session refresh request
	 */
	public void periodicProcessing() {
		try {
			if (logger.isActivated()) {
				logger.info("Send re-INVITE");
			}
		} catch(Exception e) {
			if (logger.isActivated()) {
				logger.error("Re-INVITE processing has failed", e);
			}
		}
	}
}