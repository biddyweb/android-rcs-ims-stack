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

package com.orangelabs.rcs.core.ims.service.im.chat;

import java.util.Timer;
import java.util.TimerTask;

import com.orangelabs.rcs.core.ims.service.ImsServiceSession;
import com.orangelabs.rcs.provider.settings.RcsSettings;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Chat activity manager which manages the idle state of the session. It maintains a timer
 * that is canceled and restarted when the session has activity, ie when MSRP chunks are
 * received or emitted. If the timer expires, the session is aborted.
 */
public class ChatActivityManager {
    /**
     * Timer for expiration timeout
     */
    private Timer timer = new Timer();
    
    /**
     * Expiration timer task
     */
    private ExpirationTimer timerTask = null;

    /**
     * Session timeout (in seconds)
     */
    private int timeout;
    
    /**
     * IM session
     */
    private ImsServiceSession session;
    
    /**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());
    
    /**
     * Constructor
     * 
     * @param session IM session
     */    
    public ChatActivityManager(ImsServiceSession session) {
    	this.session = session;
    	this.timeout = RcsSettings.getInstance().getChatIdleDuration();
    }
    
    /**
     * Restart the inactivity timer
     */
    public void restartInactivityTimer() {
    	// Remove old timer
		if (timerTask != null) {
			timerTask.cancel();
			timerTask = null;
		}

    	// Start a new one
    	timerTask = new ExpirationTimer();
    	timer = new Timer();
    	timer.schedule(timerTask, timeout*1000);	
    }
    
    /**
     * Internal expiration timer
     */
    private class ExpirationTimer extends TimerTask {
    	
    	public ExpirationTimer(){
    	}
    	
        public void run() {
        	if (logger.isActivated()){
        		logger.debug("Timer has expired: the session is now considered idle");
        	}
        	
			// Abort the session
			session.abortSession();
        	
        	// Terminate the timer thread
            timer.cancel();
        }
    }    
}
