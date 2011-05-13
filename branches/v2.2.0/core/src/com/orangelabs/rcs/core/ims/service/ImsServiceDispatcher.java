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
package com.orangelabs.rcs.core.ims.service;

import java.util.Enumeration;

import javax.sip.message.Request;

import com.orangelabs.rcs.core.ims.ImsModule;
import com.orangelabs.rcs.core.ims.network.sip.SipMessageFactory;
import com.orangelabs.rcs.core.ims.network.sip.SipUtils;
import com.orangelabs.rcs.core.ims.protocol.sip.SipRequest;
import com.orangelabs.rcs.core.ims.protocol.sip.SipResponse;
import com.orangelabs.rcs.utils.FifoBuffer;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * IMS service dispatcher
 * 
 * @author jexa7410
 */
public class ImsServiceDispatcher extends Thread {
    /**
     * IMS module
     */
    private ImsModule imsModule;

    /**
     * Intent manager
     */
    private SipIntentManager intentMgr = new SipIntentManager();

    /**
	 * Buffer of messages
	 */
	private FifoBuffer buffer = new FifoBuffer();

	/**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());

    /**
	 * Constructor
	 * 
	 * @param imsModule IMS module
	 */
	public ImsServiceDispatcher(ImsModule imsModule) {
		super("SipDispatcher");
		
        this.imsModule = imsModule;
        
		if (logger.isActivated()) {
			logger.info("SIP dispatcher is started");
		}
	}
	
    /**
     * Terminate the SIP dispatcher
     */
    public void terminate() {
    	if (logger.isActivated()) {
    		logger.info("Terminate the multi-session manager");
    	}
        buffer.close();
        if (logger.isActivated()) {
        	logger.info("Multi-session manager has been terminated");
        }
    }
    
	/**
	 * Post a SIP request in the buffer
	 * 
     * @param request SIP request
	 */
	public void postSipRequest(SipRequest request) {
		buffer.addObject(request);
	}
    
	/**
	 * Background processing
	 */
	public void run() {
		if (logger.isActivated()) {
			logger.info("Start background processing");
		}
		SipRequest request = null; 
		while((request = (SipRequest)buffer.getObject()) != null) {
			try {
				// Dispatch the received SIP request
				dispatch(request);
			} catch(Exception e) {
				if (logger.isActivated()) {
					logger.error("Unexpected exception", e);
				}
			}
		}
		if (logger.isActivated()) {
			logger.info("End of background processing");
		}
	}
    
    /**
     * Dispatch the received SIP request
     * 
     * @param request SIP request
     */
    private void dispatch(SipRequest request) {
		if (logger.isActivated()) {
			logger.debug("Receive " + request.getMethod() + " request");
		}

    	// Subsequent requests
		// -------------------
    	ImsServiceSession session = searchSession(request.getCallId());
    	if (session != null) {
	    	if (request.getMethod().equals(Request.UPDATE)) {
	    		session.receiveUpdate(request);
    		} else    	
    		if (request.getMethod().equals(Request.BYE)) {
		    	session.receiveBye(request);
    		} else    	
    		if (request.getMethod().equals(Request.CANCEL)) {
	    		session.receiveCancel(request);
	    	} else
    		if (request.getMethod().equals(Request.INVITE)) {
		    	session.receiveReInvite(request);
	    	} else {
	    		// Unknown subsequent request
		    	if (logger.isActivated()) {
		    		logger.debug("Unknown subsequent request " + request.getMethod() + " " + request.getCallId());
		    	}
	    	}
	    	return;
	    }
    	
		// Initial requests
		// ----------------
		if (request.getMethod().equals(Request.MESSAGE)) {
	        // IM service
			if (imsModule.isInstantMessagingServiceActivated()) {
				imsModule.getInstantMessagingService().receivePagerInstantMessage(request);
			}
		} else
	    if (request.getMethod().equals(Request.NOTIFY)) {
		    // Presence service
	    	if (imsModule.isPresenceServiceActivated()) {
	    		imsModule.getPresenceService().receiveNotification(request);
	    	}
	    } else
	    if (request.getMethod().equals(Request.OPTIONS)) {
	    	// Terminal capabilities service
	    	if (imsModule.isCapabilityServiceActivated()) { 
	    		imsModule.getCapabilityService().receiveCapabilityRequest(request);
	    	}
	    } else		
	    if (request.getMethod().equals(Request.INVITE)) {
			// Send a 100 Trying
			send100Trying(request);
			
    		// Extract the SDP part
	    	String sdp = request.getContent().toLowerCase();

	    	// New incoming session invitation
	    	if (isTagPresent(sdp, "rtp") && SipUtils.isFeatureTagPresent(request, "3gpp.cs-voice")) {
	    		// Video streaming
	    		if (logger.isActivated()) {
	    			logger.debug("Video content sharing streaming invitation");
	    		}
	    		if (imsModule.isContentSharingServiceActivated()) {
	    			imsModule.getContentSharingService().receiveVideoSharingInvitation(request);
	    		}
	    	} else
	    	if (isTagPresent(sdp, "msrp") && SipUtils.isFeatureTagPresent(request, "3gpp-application.ims.iari.gsma-is")) {
	    		// Image sharing
	    		if (logger.isActivated()) {
	    			logger.debug("Image content sharing transfer invitation");
	    		}
	    		if (imsModule.isContentSharingServiceActivated()) {
	    			imsModule.getContentSharingService().receiveImageSharingInvitation(request);
	    		}
	    	} else
	    	if (isTagPresent(sdp, "msrp") && SipUtils.isFeatureTagPresent(request, "g.oma.sip-im") &&
	    			isTagPresent(sdp, "file-selector")) {
		        // File transfer
	    		if (logger.isActivated()) {
	    			logger.debug("File transfer invitation");
	    		}
	    		if (imsModule.isInstantMessagingServiceActivated()) {
	    			imsModule.getInstantMessagingService().receiveFileTransferInvitation(request);
	    		}
	    	} else
	    	if (isTagPresent(sdp, "msrp") && SipUtils.isFeatureTagPresent(request, "g.oma.sip-im.large-message")) {
		        // IM large mode
	    		if (logger.isActivated()) {
	    			logger.debug("IM large mode invitation");
	    		}
	    		if (imsModule.isInstantMessagingServiceActivated()) {
	    			imsModule.getInstantMessagingService().receiveLargeInstantMessage(request);
	    		}
	    	} else
	    	if (isTagPresent(sdp, "msrp") && SipUtils.isFeatureTagPresent(request, "g.oma.sip-im") &&
	    			isTagPresent(sdp, "resource-lists+xml") && isTagPresent(sdp, "message/cpim")) {
		        // Ad-hoc group chat session
	    		if (logger.isActivated()) {
	    			logger.debug("Ad-hoc group chat session invitation");
	    		}
	    		if (imsModule.isInstantMessagingServiceActivated()) {
	    			imsModule.getInstantMessagingService().receiveAdhocGroupChatSession(request);
	    		}
	    	} else
    		if (isTagPresent(sdp, "msrp") && SipUtils.isFeatureTagPresent(request, "g.oma.sip-im")) {
		        // 1-1 chat session
	    		if (logger.isActivated()) {
	    			logger.debug("1-1 chat session invitation");
	    		}
	    		if (imsModule.isInstantMessagingServiceActivated()) {
	    			imsModule.getInstantMessagingService().receiveOne2OneChatSession(request);
	    		}
	    	} else {
				// Broadcast the request to external activity via intent
		    	boolean resolved = intentMgr.broadcastRequest(request);
		    	if (!resolved) {
					// Unknown service: reject the invitation with a 606 Not Acceptable
					if (logger.isActivated()) {
						logger.debug("Unknown invitation: automatically rejected");
					}
					sendFinalResponse(request, 606);
		    	}
		    	
		    	// Exit here to avoid two broadcasts
				return;
	    	}
		}

		// Broadcast the request to external activity via intent
		intentMgr.broadcastRequest(request);
    }

    /**
     * Test a tag is present or not in SIP message
     * 
     * @param message Message or message part
     * @param tag Tag to be searched
     * @return Boolean
     */
    private boolean isTagPresent(String message, String tag) {
    	if ((message != null) && (message.toLowerCase().indexOf(tag) != -1)) {
    		return true;
    	} else {
    		return false;
    	}
    }
    	
    /**
     * Search the IMS session that corresponds to a given call-ID
     *  
     * @param callId Call-ID
     * @return IMS session
     */
    private ImsServiceSession searchSession(String callId) {
    	ImsService[] list = imsModule.getImsServices();
    	for(int i=0; i< list.length; i++) {
    		for(Enumeration<ImsServiceSession> e = list[i].getSessions(); e.hasMoreElements();) {
	    		ImsServiceSession session = (ImsServiceSession)e.nextElement();
	    		if ((session != null) && session.getDialogPath().getCallId().equals(callId)) {
	    			return session;
	    		}
    		}
    	}    	
    	return null;
    }


    /**
     * Send a 100 Trying response to the remote party
     * 
     * @param request SIP request
     */
    private void send100Trying(SipRequest request) {
    	try {
	    	// Send a 100 Trying
	    	SipResponse trying = SipMessageFactory.createResponse(request, null, 100);
	    	imsModule.getCurrentNetworkInterface().getSipManager().sendSipResponse(trying);
    	} catch(Exception e) {
    		if (logger.isActivated()) {
    			logger.error("Can't send a 100 Trying response");
    		}
    	}
    }

    /**
     * Send a final response
     * 
     * @param request SIP request
     * @param code Response code
     */
    private void sendFinalResponse(SipRequest request, int code) {
    	try {
	    	SipResponse resp = SipMessageFactory.createResponse(request, code);
	    	imsModule.getCurrentNetworkInterface().getSipManager().sendSipResponse(resp);
    	} catch(Exception e) {
    		if (logger.isActivated()) {
    			logger.error("Can't send a " + code + " response");
    		}
    	}
    }
}
