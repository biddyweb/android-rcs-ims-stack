/*******************************************************************************
 * Software Name : RCS IMS Stack
 * Version : 2.0
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
package com.orangelabs.rcs.core.ims.service.presence;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import com.orangelabs.rcs.core.ims.service.presence.PresenceService;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Hyper-availability (or poke) manager
 * 
 * @author jexa7410
 */
public class PokeManager {
	
	/**
	 * Poke period in seconds
	 */
	private int pokePeriod = 60;

	/**
     * Presence service
     */
    private PresenceService presenceService;
    
    /**
     * Timer
     */
    private static Timer timer = new Timer();
    
    /**
     * Timer task
     */
    private PokeTimerTask timerTask = null;

    /**
     * Current poke expiration date
     */
    private long pokeExpireDate = 0L;    
    
    /**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     * Constructor
     * 
     * @param parent Publish manager
     */
    public PokeManager(PresenceService parent, int pokePeriod) {
        this.presenceService = parent;
        this.pokePeriod = pokePeriod;

        if (logger.isActivated()) {
        	logger.info("Poke manager started");
        }
    }

	/**
	 * Generate the next poke expiration date
	 * 
	 * @return Expiration time in milliseconds
	 */
	public long generateNextPokeExpireDate() {
		Calendar expireDate = Calendar.getInstance();
		expireDate.add(Calendar.SECOND, pokePeriod);
		pokeExpireDate = expireDate.getTimeInMillis();
		return pokeExpireDate;
	}

	/**
	 * Returns the poke expire date
	 * 
	 *  @return Expiration time in milliseconds
	 */
	public long getPokeExpireDate() {
		return pokeExpireDate;
	}

	/**
	 * Is poke activated
	 * 
	 * @return Boolean
	 */
	public boolean isPokeActivated() {
		return (timerTask != null);
	}
	
	/**
	 * Set the poke up
	 */
	public void pokeUp() {
		if (isPokeActivated()) {
			// Poke already activated
			return;
		}
				
		if (logger.isActivated()) {
    		logger.debug("Set the poke up");
    	}
		
		// Start the timer
		startTimer();

		// Notify listener
		presenceService.getImsModule().getCore().getListener().handlePokePeriodStarted(pokeExpireDate);
	}
	
	/**
	 * Set the poke down
	 */
	public void pokeDown() {
		if (!isPokeActivated()) {
			// Poke already terminated
			return;
		}
		
    	if (logger.isActivated()) {
    		logger.debug("Set the poke down");
    	}
				
		// Stop the timer
		stopTimer();

		// Reset the expiration date
		pokeExpireDate = 0L;

		// Notify listener
		presenceService.getImsModule().getCore().getListener().handlePokePeriodTerminated();
	}
	
	/**
     * Start the timer
     */
    public void startTimer() {
    	if (logger.isActivated()) {
    		logger.debug("Start poke timer: period=" + pokePeriod +  " s");
    	}
		timerTask = new PokeTimerTask();
		timer.schedule(timerTask, pokePeriod * 1000);
    }

    /**
     * Stop the timer
     */
    public void stopTimer() {
    	if (logger.isActivated()) {
    		logger.debug("Stop poke timer");
    	}
		timerTask.cancel();
		timerTask = null;
    }
    
    /**
	 * Internal timer task
	 */
    private class PokeTimerTask extends TimerTask {
	    /**
	     * Periodic processing
	     */
		public final void run() {
	    	if (logger.isActivated()) {
	    		logger.debug("Poke has expired");
	    	}
			
			// Publish the end of poke period
	        presenceService.pokePeriodHasExpired();
		}
    }

	/**
	 * Time has changed
	 */
	public void timeHasChanged() {
		if (!isPokeActivated()) {
			// Poke already terminated
			return;
		}

		if (logger.isActivated()) {
    		logger.debug("Time has changed: force poke down");
    	}
		
		// Publish the end of poke period
        presenceService.pokePeriodHasExpired();
	}
}
