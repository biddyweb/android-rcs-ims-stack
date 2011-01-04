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
package com.orangelabs.rcs.core.ims.service.im;

import java.io.ByteArrayInputStream;
import java.util.Timer;
import java.util.TimerTask;

import org.xml.sax.InputSource;

import com.orangelabs.rcs.core.ims.service.im.InstantMessageSession;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Is Composing manager which manages "is composing" events as per RFC3994. It handles the
 * status (idle ou active) of contact according to received messages and timers.
 */
public class IsComposingManager {
	
	/**
	 * Corresponds to the active state refresh interval (in seconds). This is the time before we
	 * assume the sender is idled.
	 */
	public static int DEFAULT_ACTIVE_TIMEOUT = 30;  //120 is too excessive, we use 30sec

    /**
     * Timer for expiration timeout
     */
    private Timer timer = new Timer();
    
    /**
     * Expiration timer task
     */
    private ExpirationTimer timerTask = null;

    /**
     * IM session
     */
    private InstantMessageSession session;
    
    /**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());
    
    /**
     * Constructor
     * 
     * @param session IM session
     */    
    public IsComposingManager(InstantMessageSession session) {
    	this.session = session;
    }
    
    /**
     * Receive is-composing event
     * 
     * @param contact Contact
     * @param event Event 
     */
    public void receiveIsComposingEvent(String contact, byte[] event) {
    	try {
        	// Parse received event
    		String data = new String(event);
			InputSource input = new InputSource(new ByteArrayInputStream(data.getBytes()));
			IsComposingParser parser = new IsComposingParser(input);
			IsComposingInfo isComposingInfo = parser.getIsComposingInfo();
			if ((isComposingInfo != null) && isComposingInfo.isStateActive()) {
				// Send status message to "active"
				session.getListener().handleContactIsComposing(contact, true);
				
				// Start the expiration timer
				if (isComposingInfo.getRefreshTime() != 0) {
					startExpirationTimer(isComposingInfo.getRefreshTime(), contact);
				} else {
					startExpirationTimer(DEFAULT_ACTIVE_TIMEOUT, contact);
				}
			} else {
				// Send status message to "idle"
				session.getListener().handleContactIsComposing(contact, false);

				// Stop the expiration timer
				stopExpirationTimer(contact);
			}					
    	} catch(Exception e) {
    		if (logger.isActivated()) {
    			logger.error("Can't parse is-composing event", e);
    		}
    	}
    }
    
	/**
	 * Receive is-composing event
	 * 
	 * @param contact Contact
	 * @param state State
	 */
	public void receiveIsComposingEvent(String contact, boolean state) {
    	// We just received an instant message, so if composing info was active, it must
		// be changed to idle. If it was already idle, no need to notify listener again
		session.getListener().handleContactIsComposing(contact, state);
				
		// Stop the expiration timer
		stopExpirationTimer(contact);
    }
   
    /**
     * Start the expiration timer for a given contact
     * 
     * @param duration Timer period
     * @param contact Contact
     */
    public void startExpirationTimer(long duration, String contact) {
    	// Remove old timer
		if (timerTask != null) {
			timerTask.cancel();
			timerTask = null;
		}

    	// Start timer
    	if (logger.isActivated()) {
    		logger.debug("Start timer: remote contact will be considered idle in " + duration +  "s");
    	}
    	timerTask = new ExpirationTimer(contact);
    	timer = new Timer(); // TODO: create a timer each time ?
    	timer.schedule(timerTask, duration*1000);
    }

    /**
     * Stop the expiration timer for a given contact
     * 
     * @param contact Contact
     */
    public void stopExpirationTimer(String contact) {
    	// Stop timer
    	if (logger.isActivated()) {
    		logger.debug("Stop timer");
    	}
    	
        // TODO : stop timer for a given contact
		if (timerTask != null) {
			timerTask.cancel();
			timerTask = null;
		}
    }
    
    /**
     * Internal expiration timer
     */
    private class ExpirationTimer extends TimerTask {
    	
    	private String contact = null;
    	
    	public ExpirationTimer(String contact){
    		this.contact = contact;
    	}
    	
        public void run() {
        	if (logger.isActivated()){
        		logger.debug("Timer has expired: " + contact + " is now considered idle");
        	}
        	
			// Send status message to "idle"
			session.getListener().handleContactIsComposing(contact, false);
        	
        	// Terminate the timer thread
        	// TODO: necessary
            timer.cancel();
        }
    }    
}
