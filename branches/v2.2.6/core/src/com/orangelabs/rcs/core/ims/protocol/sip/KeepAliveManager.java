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

package com.orangelabs.rcs.core.ims.protocol.sip;

import com.orangelabs.rcs.utils.PeriodicRefresher;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Keep-alive manager (see RFC 5626)
 *
 * @author BJ
 */
public class KeepAliveManager extends PeriodicRefresher {
    /**
     * Default keep-alive period (in seconds)
     */
    public static int SIP_KEEPALIVE_PERIOD = 60;
	
    /**
     * SIP interface
     */
    private SipInterface sip;
    
	/**
	 * The logger
	 */
	private Logger logger = Logger.getLogger(this.getClass().getName());

	/**
	 * Constructor
	 */
	public KeepAliveManager(SipInterface sip) {
		this.sip = sip;
	}
	
	/**
	 * Start
	 */
	public void start() {
		if (logger.isActivated()) {
			logger.debug("Start keep-alive");
		}
		startTimer(SIP_KEEPALIVE_PERIOD, 1.0);
	}
	
	/**
	 * Start
	 */
	public void stop() {
		if (logger.isActivated()) {
			logger.debug("Stop keep-alive");
		}
		stopTimer();
	}
	
	/**
     * Keep-alive processing
     */
    public void periodicProcessing() {
        try {
    		if (logger.isActivated()) {
    			logger.debug("Send keep-alive");
    		}

    		// Send a double-CRLF
        	sip.getDefaultSipProvider().getListeningPoints()[0].sendHeartbeat(sip.getOutboundProxyAddr(), sip.getOutboundProxyPort());
        	
        	// Restart timer
    		startTimer(SIP_KEEPALIVE_PERIOD, 1.0);
        } catch(Exception e) {
            if (logger.isActivated()) {
                logger.error("SIP heartbeat has failed", e);
            }
        }
    }
}
