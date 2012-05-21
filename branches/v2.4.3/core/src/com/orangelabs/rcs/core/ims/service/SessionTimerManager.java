/*******************************************************************************
 * Software Name : RCS IMS Stack
 *
 * Copyright (C) 2010 France Telecom S.A.
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

import com.orangelabs.rcs.core.ims.network.sip.SipManager;
import com.orangelabs.rcs.core.ims.network.sip.SipMessageFactory;
import com.orangelabs.rcs.core.ims.protocol.sip.SipMessage;
import com.orangelabs.rcs.core.ims.protocol.sip.SipRequest;
import com.orangelabs.rcs.core.ims.protocol.sip.SipResponse;
import com.orangelabs.rcs.core.ims.protocol.sip.SipTransactionContext;
import com.orangelabs.rcs.utils.PeriodicRefresher;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Session timer manager (see RFC 4028)
 * 
 * @author jexa7410
 */
public class SessionTimerManager extends PeriodicRefresher {
	/**
	 * UAC role
	 */
	public final static String UAC_ROLE = "uac";
	
	/**
	 * UAC role
	 */
	public final static String UAS_ROLE = "uas";

	/**
	 * Session to be refreshed
	 */
	private ImsServiceSession session;

	/**
	 * Expire period
	 */
	private int expirePeriod;
	
	/**
	 * Refresher
	 */
	private String refresher = "uas";
	
	/**
	 * Last session refresh time
	 */
	private long lastSessionRefresh;
	
	/**
	 * The logger
	 */
	private final Logger logger = Logger.getLogger(this.getClass().getName());
	
	/**
	 * Constructor
	 * 
	 * @param session Session to be refreshed
	 */
	public SessionTimerManager(ImsServiceSession session) {
		this.session = session;
	}
	
	/**
	 * Is session timer activated
	 * 
	 * @param msg SIP message
	 * @return Boolean
	 */
	public boolean isSessionTimerActivated(SipMessage msg) {
		// Check the Session-Expires header
		int expire = msg.getSessionTimerExpire();
		if (expire < 90) {
			if (logger.isActivated()) {
				logger.debug("Session timer not supported: no expire value");
			}
			return false;
		}

		// Check if the UPDATE method is supported in Allow header
	/* TODO: not supported by our platform 
	 	AllowHeader allowHeader = (AllowHeader)msg.getHeader(AllowHeader.NAME);
		if ((allowHeader != null) && !allowHeader.getMethod().contains("UPDATE")) {
			if (logger.isActivated()) {
				logger.debug("Session timer not supported: UPDATE method not allowed");
			}
			return false;
		}*/
		
		return true;
	}
	
	/**
	 * Start the session timer
	 * 
	 * @param refresher Refresher role
	 * @param expirePeriod Expire period
	 */
	public void start(String refresher, int expirePeriod) {
		if (logger.isActivated()) {
			logger.debug("Start session timer for session " + session.getId() + " (role=" + refresher + ", expire=" + expirePeriod + ")");
		}

		// Set refresher role
		this.refresher = refresher;
		
		// Set expire period
		this.expirePeriod = expirePeriod;

		// Reset last session refresh time
		lastSessionRefresh = System.currentTimeMillis();			

		// Start processing
		if (refresher.equals(UAC_ROLE)) {
			startTimer(expirePeriod, 0.5);
		} else {
			startTimer(expirePeriod, 1);
		}
	}
	
	/**
	 * Stop the session timer
	 */
	public void stop() {
		if (logger.isActivated()) {
			logger.debug("Stop session timer for session " + session.getId());
		}
		stopTimer();
	}
	
	/**
	 * Periodic session timer processing
	 */
	public void periodicProcessing() {
		if (refresher.equals(UAC_ROLE)) {
			// Refresher role
			sessionRefreshForUAC();
		} else {
			// Refreshee role
			sessionRefreshForUAS();
		}
	}
   
	/**
	 * Session refresh processing for UAC role. If the refresher never sends a
	 * session refresh request then the session should be terminated.
	 */
    private void sessionRefreshForUAC() {
		try {
			if (logger.isActivated()) {
				logger.debug("Session timer refresh (UAC role)");
			}
			
			// Increment the Cseq number of the dialog path
			session.getDialogPath().incrementCseq();

	        // Send UPDATE request
	        SipRequest update = SipMessageFactory.createUpdate(session.getDialogPath());
    		SipTransactionContext ctx = session.getImsService().getImsModule().getSipManager().sendSubsequentRequest(session.getDialogPath(), update);
	        
	        // Wait response
	        ctx.waitResponse(SipManager.TIMEOUT);

			// Analyze the received response 
	        if (ctx.getStatusCode() == 200) {
	        	// Success
				if (logger.isActivated()) {
					logger.debug("Session timer refresh with success");
				}

				// Update last session refresh time
				lastSessionRefresh = System.currentTimeMillis();			
				
				// Restart timer
	    		startTimer(expirePeriod, 0.5);
	        } else
	        if (ctx.getStatusCode() == 405) {
				if (logger.isActivated()) {
					logger.debug("Session timer refresh not supported");
				}	        
	        } else {
				if (logger.isActivated()) {
					logger.debug("Session timer refresh has failed: close the session");
				}
	        
				// Close the session
	        	session.abortSession();

	        	// Request capabilities to the remote
				session.getImsService().getImsModule().getCapabilityService().requestContactCapabilities(session.getDialogPath().getRemoteParty());
	        }	        
        } catch (Exception e) {
			if (logger.isActivated()) {
				logger.error("Session timer refresh has failed", e);
			}
        }
    }
	
    /**
	 * Session refresh processing for UAS role. If the refresher never gets a
	 * response from the remote then the session should be terminated.
	 */
    private void sessionRefreshForUAS() {
		try {
			if (logger.isActivated()) {
				logger.debug("Session timer refresh (UAS role)");
			}
			
			if (((System.currentTimeMillis()-lastSessionRefresh)/1000) >= expirePeriod) {
				// Session has expired
				if (logger.isActivated()) {
					logger.debug("Session timer refresh has failed: close the session");
				}

				// Close the session
		    	session.abortSession();
		    	
	        	// Request capabilities to the remote
				session.getImsService().getImsModule().getCapabilityService().requestContactCapabilities(session.getDialogPath().getRemoteParty());
			} else {
	        	// Success
				if (logger.isActivated()) {
					logger.debug("Session timer refresh with success");
				}

				// Restart timer
	    		startTimer(expirePeriod, 1);
			}			
			
	    } catch (Exception e) {
			if (logger.isActivated()) {
				logger.error("Session timer refresh has failed", e);
			}
			
	    	// Close the session
	    	session.abortSession();
	    }
    }

    /**
	 * Receive UPDATE request 
	 * 
	 * @param update UPDATE request
	 */
	public void receiveUpdate(SipRequest update) {
		try {
        	if (logger.isActivated()) {
        		logger.debug("Session refresh request received");
        	}

        	// Update last session refresh time
			lastSessionRefresh = System.currentTimeMillis();			

			// Send 200 OK response
        	if (logger.isActivated()) {
        		logger.debug("Send 200 OK");
        	}
	        SipResponse resp = SipMessageFactory.create200OkUpdateResponse(session.getDialogPath(), update);
	        session.getImsService().getImsModule().getSipManager().sendSipResponse(resp);
		} catch(Exception e) {
			if (logger.isActivated()) {
				logger.error("Session timer refresh has failed", e);
			}
		}
	}
}