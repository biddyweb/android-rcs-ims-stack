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

package com.orangelabs.rcs.core.ims.service.toip;

import com.orangelabs.rcs.core.CoreException;
import com.orangelabs.rcs.core.ims.ImsModule;
import com.orangelabs.rcs.core.ims.network.sip.SipMessageFactory;
import com.orangelabs.rcs.core.ims.protocol.sip.SipRequest;
import com.orangelabs.rcs.core.ims.protocol.sip.SipResponse;
import com.orangelabs.rcs.core.ims.service.ImsService;
import com.orangelabs.rcs.service.api.client.media.IMediaPlayer;
import com.orangelabs.rcs.service.api.client.media.IMediaRenderer;
import com.orangelabs.rcs.utils.PhoneUtils;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * ToIP service
 * 
 * @author jexa7410
 */
public class ToIpService extends ImsService {
	/**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());

    /**
	 * Constructor
	 * 
	 * @param parent IMS module
	 * @param activated Activation flag
	 * @throws CoreException
	 */
	public ToIpService(ImsModule parent, boolean activated) throws CoreException {
		super(parent, "toip_service.xml", activated);
	}
    
    /**
    /**
	 * Start the IMS service 
	 */
	public synchronized void start() {
		if (isServiceStarted()) {
			// Already started
			return;
		}
		setServiceStarted(true);
	}

	/**
	 * Stop the IMS service 
	 */
	public synchronized void stop() {
		if (!isServiceStarted()) {
			// Already stopped
			return;
		}
		setServiceStarted(false);
	}
	
	/**
	 * Check the IMS service 
	 */
	public void check() {
	}

	/**
	 * Initiate a ToIP call
	 * 
	 * @param contact Remote contact
	 * @param player Media player
	 * @param renderer Media renderer
	 * @return ToIP session 
	 */
	public ToIpSession initiateCall(String contact, IMediaPlayer player, IMediaRenderer renderer) {
		if (logger.isActivated()) {
			logger.info("Call contact " + contact);
		}
			
		// Create a new session
		OriginatingToIpSession session = new OriginatingToIpSession(
				this,
				player,
				renderer,
				PhoneUtils.formatNumberToSipAddress(contact));
		
		// Start the session
		session.startSession();
		return session;
	}

	/**
	 * Reveive a ToIP call
	 * 
	 * @param invite Initial invite
	 */
	public void receiveCall(SipRequest invite) {
		// Test if there is already a CS session 
		if (getNumberOfSessions() > 0) {
			try {
				// Create a 486 Busy response
		    	if (logger.isActivated()) {
		    		logger.info("Send 486 Busy here");
		    	}
		        SipResponse resp = SipMessageFactory.createResponse(invite, 486);

		        // Send response
		        getImsModule().getSipManager().sendSipResponse(resp);
			} catch(Exception e) {
				if (logger.isActivated()) {
					logger.error("Can't send 486 Busy here", e);
				}
			}
			return;
		}

		// Create a new session
    	TerminatingToIpSession session = new TerminatingToIpSession(
					this,
					invite);
		
		// Start the session
		session.startSession();

		// Notify listener
		getImsModule().getCore().getListener().handleToIpCallInvitation(session);
	}
}
