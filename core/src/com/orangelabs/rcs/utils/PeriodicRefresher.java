/*******************************************************************************
 * Software Name : RCS IMS Stack
 * Version : 2.0.0
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
package com.orangelabs.rcs.utils;

import java.util.Timer;
import java.util.TimerTask;

import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Periodic refresher.
 *
 * @author JM. Auffret
 */
public abstract class PeriodicRefresher {

    /**
     * Timer
     */
    private static Timer timer = new Timer();
    
    /**
     * Timer task
     */
    private RefresherTimerTask timerTask = null;

    /**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     * Periodic processing
     */
    public abstract void periodicProcessing();
    
    /**
     * Is timer started
     * 
     * @return Boolean
     */
    public boolean isTimerStarted() {
    	return (timerTask != null);
    }
    
    /**
     * Start the timer
     * 
     * @param expirePeriod Expiration period in seconds
     * @param delta Delta to apply on the expire period in percentage
     */
    public void startTimer(int expirePeriod, double delta) {
    	// Remove old timer
		if (timerTask != null) {
			timerTask.cancel();
			timerTask = null;
		}

		// Calculate the effective refresh period
    	int period = (int)(expirePeriod * delta);

    	// Start timer
    	if (logger.isActivated()) {
    		logger.debug("Start timer at period=" + period +  "s, expire at " + expirePeriod + "s");
    	}
    	timerTask = new RefresherTimerTask(this);
    	timer.scheduleAtFixedRate(timerTask, period * 1000, period * 1000);
    }

    /**
     * Stop the timer
     */
    public void stopTimer() {
    	// Stop timer
    	if (logger.isActivated()) {
    		logger.debug("Stop timer");
    	}
		if (timerTask != null) {
			timerTask.cancel();
			timerTask = null;
		}
    }
    
    /**
	 * Internal timer task
	 */
    private class RefresherTimerTask extends TimerTask {
    	/**
    	 * Parent
    	 */
		private PeriodicRefresher parent;

		/**
		 * Constructor
		 * 
		 * @param parent Parent
		 */
		RefresherTimerTask(PeriodicRefresher parent) {
			this.parent = parent;
		}
	
	    /**
	     * Periodic processing
	     */
		public final void run() {
	        parent.periodicProcessing();
		}
    }    
}
