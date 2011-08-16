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

package com.orangelabs.rcs.core.ims.service.richcall;

import com.orangelabs.rcs.core.CoreException;
import com.orangelabs.rcs.core.content.MmContent;
import com.orangelabs.rcs.core.content.VideoContent;
import com.orangelabs.rcs.core.ims.ImsModule;
import com.orangelabs.rcs.core.ims.network.sip.SipMessageFactory;
import com.orangelabs.rcs.core.ims.network.sip.SipUtils;
import com.orangelabs.rcs.core.ims.protocol.sip.SipRequest;
import com.orangelabs.rcs.core.ims.protocol.sip.SipResponse;
import com.orangelabs.rcs.core.ims.service.ImsService;
import com.orangelabs.rcs.core.ims.service.capability.CapabilityUtils;
import com.orangelabs.rcs.core.ims.service.sharing.streaming.ContentSharingStreamingSession;
import com.orangelabs.rcs.core.ims.service.sharing.transfer.ContentSharingTransferSession;
import com.orangelabs.rcs.service.api.client.media.IMediaPlayer;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Rich call service has in charge to monitor the GSM call in order to stop the current
 * content sharing when the call terminates, to process capability request from remote
 * and to request remote capabilities.
 *  
 * @author jexa7410
 */
public class RichcallService extends ImsService {
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
	public RichcallService(ImsModule parent, boolean activated) throws CoreException {
		super(parent, "richcall_service.xml", activated);
	}

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
	 * Initiate an image sharing session
	 * 
	 * @param contact Remote contact
	 * @param content Content to be shared 
	 * @return CSh session 
	 */
	public ContentSharingTransferSession initiateImageSharingSession(String contact, MmContent content) {
		// TODO: test we are in call with contact
		return getImsModule().getContentSharingService().initiateImageSharingSession(contact, content);
	}
	
	/**
	 * Initiate a pre-recorded video sharing session
	 * 
	 * @param contact Remote contact
	 * @param content Video content to share
	 * @param player Media player
	 * @return CSh session
	 */
	public ContentSharingStreamingSession initiatePreRecordedVideoSharingSession(String contact, VideoContent content, IMediaPlayer player) {
		// TODO: test we are in call with contact
		return getImsModule().getContentSharingService().initiatePreRecordedVideoSharingSession(contact, content, player);
	}
	
	/**
	 * Initiate a live video sharing session
	 * 
	 * @param contact Remote contact
	 * @param player Media player
	 * @return CSh session
	 */
	public ContentSharingStreamingSession initiateLiveVideoSharingSession(String contact, IMediaPlayer player) {
		return getImsModule().getContentSharingService().initiateLiveVideoSharingSession(contact, player);
	}
	
	/**
	 * Receive a video sharing invitation
	 * 
	 * @param invite Initial invite
	 */
	public void receiveVideoSharingInvitation(SipRequest invite) {	
		// Test if call is established
		if (!getImsModule().getCallManager().isConnected()) {
			if (logger.isActivated()) {
				logger.debug("Rich call not established: reject the invitation");
			}
			try {
				// Create a 606 Not Acceptable response
		    	if (logger.isActivated()) {
		    		logger.info("Send 606 Not Acceptable");
		    	}
		        SipResponse resp = SipMessageFactory.createResponse(invite, 606);
		        
		        // Send response
		        getImsModule().getSipManager().sendSipResponse(resp);
			} catch(Exception e) {
				if (logger.isActivated()) {
					logger.error("Can't send 606 Not Acceptable", e);
				}
			}
			return;
		}

		// Test number of session 
		if (getNumberOfSessions() > 0) {
			if (logger.isActivated()) {
				logger.debug("The max number of sharing sessions is achieved: reject the invitation");
			}
			try {
				// Send a 486 Busy response
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
		
		// Process the session invitation
		getImsModule().getContentSharingService().receiveVideoSharingInvitation(invite);
	}
	
	/**
	 * Receive an image sharing invitation
	 * 
	 * @param invite Initial invite
	 */
	public void receiveImageSharingInvitation(SipRequest invite) {
		if (logger.isActivated()) {
    		logger.info("Receive an image sharing session invitation");
    	}

		// Test if call is established
		if (!getImsModule().getCallManager().isConnected()) {
			if (logger.isActivated()) {
				logger.debug("Rich call not established: reject the invitation");
			}
			try {
				// Create a 606 Not Acceptable response
		    	if (logger.isActivated()) {
		    		logger.info("Send 606 Not Acceptable");
		    	}
		        SipResponse resp = SipMessageFactory.createResponse(invite, 606);
		        
		        // Send response
		        getImsModule().getSipManager().sendSipResponse(resp);
			} catch(Exception e) {
				if (logger.isActivated()) {
					logger.error("Can't send 606 Not Acceptable", e);
				}
			}
			return;
		}

		// Test number of session 
		if (getNumberOfSessions() > 0) {
			if (logger.isActivated()) {
				logger.debug("The max number of sharing sessions is achieved: reject the invitation");
			}
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
		
		// Process the session invitation
		getImsModule().getContentSharingService().receiveImageSharingInvitation(invite);
	}
	
    /**
     * Receive a capability request (options procedure)
     * 
     * @param options Received options message
     */
    public void receiveCapabilityRequest(SipRequest options) {
    	String contact = SipUtils.getAssertedIdentity(options);

    	if (logger.isActivated()) {
			logger.debug("OPTIONS request received during a call from " + contact);
		}

	    try {
	    	// Create 200 OK response
	    	String ipAddress = getImsModule().getCurrentNetworkInterface().getNetworkAccess().getIpAddress();
	        SipResponse resp = SipMessageFactory.create200OkOptionsResponse(options,
	        		getImsModule().getSipManager().getSipStack().getLocalContact(),
	        		CapabilityUtils.getSupportedFeatureTags(true),
	        		CapabilityUtils.buildSdp(ipAddress));

	        // Send 200 OK response
	        getImsModule().getSipManager().sendSipResponse(resp);
	    } catch(Exception e) {
        	if (logger.isActivated()) {
        		logger.error("Can't send 200 OK for OPTIONS", e);
        	}
	    }
	    
	    // Request also capabilities to the remote if it's an outgoing call
		if (getImsModule().getCallManager().isConnectedWith(contact) && !getImsModule().getCallManager().isIncomingCall()) {
			// Outgoing call is received: request capabilities
			getImsModule().getCapabilityService().requestContactCapabilities(contact);
		}
    }		
}
