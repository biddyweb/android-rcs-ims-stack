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
package com.orangelabs.rcs.core.ims.service.voip;

import com.orangelabs.rcs.core.CoreException;
import com.orangelabs.rcs.core.ims.ImsModule;
import com.orangelabs.rcs.core.ims.network.sip.SipMessageFactory;
import com.orangelabs.rcs.core.ims.protocol.sip.SipRequest;
import com.orangelabs.rcs.core.ims.protocol.sip.SipResponse;
import com.orangelabs.rcs.core.ims.service.ImsService;
import com.orangelabs.rcs.core.media.MediaPlayer;
import com.orangelabs.rcs.core.media.MediaRenderer;
import com.orangelabs.rcs.utils.PhoneUtils;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * VoIP service
 * 
 * @author jexa7410
 */
public class VoIpService extends ImsService {
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
	public VoIpService(ImsModule parent, boolean activated) throws CoreException {
		super(parent, "voip_service.xml", activated);
	}
    
    /**
    /**
	 * Start the IMS service 
	 */
	public void start() {
	}

	/**
	 * Stop the IMS service 
	 */
	public void stop() {
	}
	
	/**
	 * Check the IMS service 
	 */
	public void check() {
	}

	/**
	 * Initiate a VoIp call
	 * 
	 * @param contact Remote contact
	 * @param player Media player
	 * @param renderer Media renderer
	 * @return VoIp session 
	 */
	public VoIpSession initiateCall(String contact, MediaPlayer player, MediaRenderer renderer) {
		if (logger.isActivated()) {
			logger.info("Call contact " + contact);
		}
			
		// Create a new session
		OriginatingVoIpSession session = new OriginatingVoIpSession(
				this,
				player,
				renderer,
				PhoneUtils.formatNumberToSipAddress(contact));
		
		// Start the session
		session.startSession();
		return session;
	}

	/**
	 * Reveive a VoIp call
	 * 
	 * @param invite Initial invite
	 */
	public void receiveCall(SipRequest invite) {
		// Test if there is already a CS session 
		if (getNumberOfSessions() > 0) {
			try {
				// Send a 486 Busy response
		    	if (logger.isActivated()) {
		    		logger.info("Send 486 Busy here");
		    	}
		        SipResponse resp = SipMessageFactory.createResponse(invite, 486);
		        getImsModule().getSipManager().sendSipMessage(resp);
			} catch(Exception e) {
				if (logger.isActivated()) {
					logger.error("Can't send 486 Busy here", e);
				}
			}
			return;
		}

		// Create a new session
    	TerminatingVoIpSession session = new TerminatingVoIpSession(
					this,
					invite);
		
		// Start the session
		session.startSession();

		// Notify listener
		getImsModule().getCore().getListener().handleVoIpCallInvitation(session);
	}
}
