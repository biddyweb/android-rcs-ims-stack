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

package com.orangelabs.rcs.core.ims.service;

import java.util.Enumeration;

import javax.sip.header.EventHeader;
import javax.sip.message.Request;

import com.orangelabs.rcs.core.ims.ImsModule;
import com.orangelabs.rcs.core.ims.network.sip.FeatureTags;
import com.orangelabs.rcs.core.ims.network.sip.SipMessageFactory;
import com.orangelabs.rcs.core.ims.network.sip.SipUtils;
import com.orangelabs.rcs.core.ims.protocol.sip.SipException;
import com.orangelabs.rcs.core.ims.protocol.sip.SipRequest;
import com.orangelabs.rcs.core.ims.protocol.sip.SipResponse;
import com.orangelabs.rcs.core.ims.service.im.chat.ChatUtils;
import com.orangelabs.rcs.core.ims.service.im.chat.standfw.StoreAndForwardManager;
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
	 * Buffer of messages
	 */
	private FifoBuffer buffer = new FifoBuffer();

	/**
	 * SIP intent manager
	 */
	private SipIntentManager intentMgr = new SipIntentManager(); 
	
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

	    if (request.getMethod().equals(Request.INVITE)) {
	    	// INVITE received
	    	ImsServiceSession session = searchSession(request.getCallId());
	    	if (session != null) {
	    		// Subsequent request received
	    		session.receiveReInvite(request);
	    		return;
	    	}
	    	
			// Send a 100 Trying response
			send100Trying(request);
			
    		// Extract the SDP part
	    	String sdp = request.getContent().toLowerCase();

	    	// New incoming session invitation
	    	if (SipUtils.getAssertedIdentity(request).contains(StoreAndForwardManager.SERVICE_URI)) {
    			// Store & Forward session
	    		if (logger.isActivated()) {
	    			logger.debug("Store & Forward session invitation");
	    		}
    			imsModule.getInstantMessagingService().receiveStoredAndForwardInvitation(request);
	    	} else
	    	if (isTagPresent(sdp, "rtp") &&
	    			SipUtils.isFeatureTagPresent(request, FeatureTags.FEATURE_RCSE_VIDEO_SHARE)) {
	    		// Video streaming
	    		if (logger.isActivated()) {
	    			logger.debug("Video content sharing streaming invitation");
	    		}
	    		if (imsModule.isRichcallServiceActivated()) {
	    			imsModule.getRichcallService().receiveVideoSharingInvitation(request);
	    		}
	    	} else
	    	if (isTagPresent(sdp, "msrp") &&
	    			SipUtils.isFeatureTagPresent(request, FeatureTags.FEATURE_RCSE_VIDEO_SHARE) &&
	    				SipUtils.isFeatureTagPresent(request, FeatureTags.FEATURE_RCSE_IMAGE_SHARE)) {
	    		// Image sharing
	    		if (logger.isActivated()) {
	    			logger.debug("Image content sharing transfer invitation");
	    		}
	    		if (imsModule.isRichcallServiceActivated()) {
	    			imsModule.getRichcallService().receiveImageSharingInvitation(request);
	    		}
	    	} else
	    	if (isTagPresent(sdp, "msrp") &&
	    			SipUtils.isFeatureTagPresent(request, FeatureTags.FEATURE_OMA_IM) &&
	    				isTagPresent(sdp, "file-selector")) {
		        // File transfer
	    		if (logger.isActivated()) {
	    			logger.debug("File transfer invitation");
	    		}
    			imsModule.getInstantMessagingService().receiveFileTransferInvitation(request);
	    	} else
	    	if (isTagPresent(sdp, "msrp") &&
	    			SipUtils.isFeatureTagPresent(request, FeatureTags.FEATURE_OMA_IM) &&
	    				isTagPresent(sdp, "resource-lists+xml")) {
		        // Ad-hoc group chat session
	    		if (logger.isActivated()) {
	    			logger.debug("Ad-hoc group chat session invitation");
	    		}
    			imsModule.getInstantMessagingService().receiveAdhocGroupChatSession(request);
	    	} else
    		if (isTagPresent(sdp, "msrp") &&
    				SipUtils.isFeatureTagPresent(request, FeatureTags.FEATURE_OMA_IM)) {
		        // 1-1 chat session
	    		if (logger.isActivated()) {
	    			logger.debug("1-1 chat session invitation");
	    		}
    			imsModule.getInstantMessagingService().receiveOne2OneChatSession(request);
    		} else {
	    		if (intentMgr.isSipIntentResolved(request)) {
	    			// Generic SIP session
		    		if (logger.isActivated()) {
		    			logger.debug("Generic SIP session invitation");
		    		}
	    			imsModule.getSipService().receiveSessionInvitation(request);
		    	} else {
					// Unknown service: reject the invitation with a 606 Not Acceptable
					if (logger.isActivated()) {
						logger.debug("Unknown invitation: automatically rejected");
					}
					sendFinalResponse(request, 606);
		    	}
    		}
		} else
    	if (request.getMethod().equals(Request.MESSAGE)) {
	        // MESSAGE received
	    	if (ChatUtils.isImdnService(request)) {
	    		// IMDN service
				imsModule.getInstantMessagingService().receiveMessageDeliveryStatus(request);
	    	}
		} else
	    if (request.getMethod().equals(Request.NOTIFY)) {
	    	// NOTIFY received
	    	dispatchNotify(request);
	    } else
	    if (request.getMethod().equals(Request.OPTIONS)) {
	    	// OPTIONS received
	    	if (imsModule.isRichcallServiceActivated() && imsModule.getCallManager().isConnected()) { 
		    	// Rich call service
	    		imsModule.getRichcallService().receiveCapabilityRequest(request);
	    	} else {
	    		// Capability discovery service
	    		imsModule.getCapabilityService().receiveCapabilityRequest(request);
	    	}		    	
	    } else		
		if (request.getMethod().equals(Request.BYE)) {
	        // BYE received
			
			// Send a 200 OK response
			try {
				if (logger.isActivated()) {
					logger.info("Send 200 OK");
				}
		        SipResponse response = SipMessageFactory.createResponse(request, 200);
				imsModule.getSipManager().sendSipResponse(response);
			} catch(Exception e) {
		       	if (logger.isActivated()) {
		    		logger.error("Can't send 200 OK response", e);
		    	}
			}

    		ImsServiceSession session = searchSession(request.getCallId());
        	if (session != null) {
        		session.receiveBye(request);
        	}
		} else    	
		if (request.getMethod().equals(Request.CANCEL)) {
	        // CANCEL received
			
			// Send a 200 OK
	    	try {
		    	if (logger.isActivated()) {
		    		logger.info("Send 200 OK");
		    	}
		        SipResponse cancelResp = SipMessageFactory.createResponse(request, 200);
		        imsModule.getSipManager().sendSipResponse(cancelResp);
			} catch(Exception e) {
		    	if (logger.isActivated()) {
		    		logger.error("Can't send 200 OK response", e);
		    	}
			}
			
	    	ImsServiceSession session = searchSession(request.getCallId());
	    	if (session != null) {
	    		session.receiveCancel(request);
	    	}
    	} else
    	if (request.getMethod().equals(Request.UPDATE)) {
	        // UPDATE received
    		ImsServiceSession session = searchSession(request.getCallId());
        	if (session != null) {
        		session.receiveUpdate(request);
        	}
		} else {
			// Unknown request received
			if (logger.isActivated()) {
				logger.debug("Unknown request " + request.getMethod());
			}
		}
    }

    /**
     * Dispatch the received SIP NOTIFY
     * 
     * @param notify SIP request
     */
    private void dispatchNotify(SipRequest notify) {
	    try {
	    	// Create 200 OK response
	        SipResponse resp = SipMessageFactory.createResponse(notify, 200);

	        // Send 200 OK response
	        imsModule.getSipManager().sendSipResponse(resp);
	    } catch(SipException e) {
        	if (logger.isActivated()) {
        		logger.error("Can't send 200 OK for NOTIFY", e);
        	}
	    }
    	
	    // Get the event type
	    EventHeader eventHeader = (EventHeader)notify.getHeader(EventHeader.NAME);
	    if (eventHeader == null) {
        	if (logger.isActivated()) {
        		logger.debug("Unknown notification event type");
        	}
	    	return;
	    }
	    
	    // Dispatch the notification to the corresponding service
	    if (eventHeader.getEventType().equalsIgnoreCase("presence.winfo")) {
	    	// Presence service
	    	if (imsModule.isPresenceServiceActivated() && imsModule.getPresenceService().isServiceStarted()) {
	    		imsModule.getPresenceService().getWatcherInfoSubscriber().receiveNotification(notify);
	    	}
	    } else
	    if (eventHeader.getEventType().equalsIgnoreCase("presence")) {
	    	if (notify.getTo().indexOf("anonymous") != -1) {
		    	// Capability service
		    	if (imsModule.isCapabilityServiceActivated() && imsModule.getCapabilityService().isServiceStarted()) {
		    		imsModule.getCapabilityService().receiveNotification(notify);
		    	}
	    	} else {
		    	// Presence service
		    	if (imsModule.isPresenceServiceActivated() && imsModule.getPresenceService().isServiceStarted()) {
		    		imsModule.getPresenceService().getPresenceSubscriber().receiveNotification(notify);
		    	}
	    	}
	    } else
	    if (eventHeader.getEventType().equalsIgnoreCase("conference")) {
	    	// IM service
	    	if (imsModule.isInstantMessagingServiceActivated() && imsModule.getInstantMessagingService().isServiceStarted()) {
	    		imsModule.getInstantMessagingService().receiveConferenceNotification(notify);
	    	}
		} else {
			// Not supported service
        	if (logger.isActivated()) {
        		logger.debug("Not supported notification event type");
        	}
		}
    }
    
    /**
     * Test a tag is present or not in SIP message
     * 
     * @param message Message or message part
     * @param tag Tag to be searched
     * @return Boolean
     */
    private boolean isTagPresent(String message, String tag) {
    	if ((message != null) && (tag != null) && (message.toLowerCase().indexOf(tag) != -1)) {
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
	    	// Send a 100 Trying response
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
