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

import java.util.List;
import java.util.Vector;

import com.orangelabs.rcs.core.ims.ImsModule;
import com.orangelabs.rcs.core.ims.network.sip.SipManager;
import com.orangelabs.rcs.core.ims.network.sip.SipMessageFactory;
import com.orangelabs.rcs.core.ims.protocol.sip.SipDialogPath;
import com.orangelabs.rcs.core.ims.protocol.sip.SipRequest;
import com.orangelabs.rcs.core.ims.protocol.sip.SipTransactionContext;
import com.orangelabs.rcs.core.ims.service.ImsService;
import com.orangelabs.rcs.core.ims.service.SessionAuthenticationAgent;
import com.orangelabs.rcs.core.ims.service.im.chat.cpim.CpimMessage;
import com.orangelabs.rcs.core.ims.service.im.chat.event.ConferenceEventSubscribeManager;
import com.orangelabs.rcs.core.ims.service.im.chat.iscomposing.IsComposingInfo;
import com.orangelabs.rcs.service.api.client.messaging.InstantMessage;
import com.orangelabs.rcs.utils.PhoneUtils;
import com.orangelabs.rcs.utils.StringUtils;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Abstract Group chat session
 * 
 * @author jexa7410
 */
public abstract class GroupChatSession extends ChatSession {
	/**
	 * SIP REFER uses the same dialog as the SIP INVITE 
	 */
	private boolean referUseSameDialog = true;

	/**
	 * Conference event subscribe manager
	 */
	private ConferenceEventSubscribeManager conferenceSubscriber; 
	
	/**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());
    
    /**
	 * Constructor for originating side
	 * 
	 * @param parent IMS service
	 * @param conferenceId Conference id
	 * @param subject Subject of the conference
	 * @param participants List of participants
	 */
	public GroupChatSession(ImsService parent, String conferenceId, String subject, ListOfParticipant participants) {
		super(parent, conferenceId, subject, participants);
		
		// Instanciate the subscribe manager for conference event 
		conferenceSubscriber = new ConferenceEventSubscribeManager(this); 		
	}

	/**
	 * Constructor for terminating side
	 * 
	 * @param parent IMS service
	 * @param contact Remote contact
	 * @param subject Subject of the conference
	 */
	public GroupChatSession(ImsService parent, String contact, String subject) {
		super(parent, contact, subject);
		
		// Instanciate the subscribe manager for conference event 
		conferenceSubscriber = new ConferenceEventSubscribeManager(this); 		
	}
	
	/**
	 * Is chat group
	 * 
	 * @return Boolean
	 */
	public boolean isChatGroup() {
		return true;
	}
	
	/**
	 * Get replaced session ID
	 * 
	 * @return Session ID
	 */
	public String getReplacedSessionId() {
		// TODO: use "Replaces" header
		String content = getDialogPath().getRemoteContent();
		String result = null;
		if (content != null) {
			int index1 = content.indexOf("Session-Replaces=");
			if (index1 != -1) {
				int index2 = content.indexOf("\"", index1);
				result = content.substring(index1+17, index2);
			}
		}
		return result;
	}
	
	/**
	 * Returns the conference event subscriber
	 * 
	 * @return Subscribe manager
	 */
	public ConferenceEventSubscribeManager getConferenceEventSubscriber() {
		return conferenceSubscriber;
	}		

	/**
	 * Close media session
	 */
	public void closeMediaSession() {
		// Stop conference subscription
		conferenceSubscriber.terminate();
		
		// Close MSRP session
		closeMsrpSession();
	}

	/**
	 * Send a plain text message with IMDN headers
	 * 
	 * @param msg Message
	 * @param id Message-id
	 * @param imdnActivated If true, add IMDN headers to the request
	 */ 
	public void sendTextMessage(String msg, String msgId, boolean imdnActivated) {
		String content = StringUtils.encodeUTF8(msg);
		String from = ImsModule.IMS_USER_PROFILE.getPublicUri();
		String to = getImSessionIdentity();
		String cpim = null;
		if (imdnActivated){
			cpim = ChatUtils.buildCpimMessageWithIMDN(from, to, msgId, content, InstantMessage.MIME_TYPE);
		}else{
			cpim = ChatUtils.buildCpimMessage(from, to, content, InstantMessage.MIME_TYPE);
		}
		sendDataChunks(cpim, CpimMessage.MIME_TYPE, msgId);
	}
	
	/**
	 * Send is composing status
	 * 
	 * @param status Status
	 */
	public void sendIsComposingStatus(boolean status) {
		String content = IsComposingInfo.buildIsComposingInfo(status);
		String from = ImsModule.IMS_USER_PROFILE.getPublicUri();
		String to = getImSessionIdentity();
		String cpim = ChatUtils.buildCpimMessage(from, to, content, IsComposingInfo.MIME_TYPE);
		sendDataChunks(cpim, CpimMessage.MIME_TYPE, null);	
	}
	
	/**
	 * Add a participant to the session
	 * 
	 * @param participant Participant
	 */
	public void addParticipant(String participant) {
		try {
        	if (logger.isActivated()) {
        		logger.debug("Add one participant (" + participant + ") to the session");
        	}
    		
        	SipDialogPath dialogPath;
    		SessionAuthenticationAgent authenticationAgent;
        	if (referUseSameDialog) {
        		// Re-use INVITE dialog path
        		dialogPath = getDialogPath();
        		authenticationAgent = getAuthenticationAgent();
        		
                // Increment the Cseq number of the dialog path
            	dialogPath.incrementCseq();
        	} else {
	    		// Create a dialog path if necessary
	    		authenticationAgent = new SessionAuthenticationAgent();
	
	    		// Set Call-Id
	        	String callId = getImsService().getImsModule().getSipManager().getSipStack().generateCallId();
	
	        	// Set target
	        	String target = getImSessionIdentity();
	
	            // Set local party
	        	String localParty = ImsModule.IMS_USER_PROFILE.getPublicUri();
	
	            // Set remote party
	        	String remoteParty = getImSessionIdentity();
	
	        	// Set the route path
	        	Vector<String> route = getImsService().getImsModule().getSipManager().getSipStack().getServiceRoutePath();
	
	        	// Create a dialog path
	            dialogPath = new SipDialogPath(
	            		getImsService().getImsModule().getSipManager().getSipStack(),
	            		callId,
	            		1,
	            		target,
	            		localParty,
	            		remoteParty,
	            		route);    		
        	}
        	
	        // Send REFER request
    		if (logger.isActivated()) {
        		logger.debug("Send REFER");
        	}
    		String contactUri = PhoneUtils.formatNumberToSipUri(participant);
	        SipRequest refer = SipMessageFactory.createRefer(dialogPath, contactUri);
	        SipTransactionContext ctx = getImsService().getImsModule().getSipManager().sendSipMessageAndWait(refer);
	
	        // Wait response
        	if (logger.isActivated()) {
        		logger.debug("Wait response");
        	}
	        ctx.waitResponse(SipManager.TIMEOUT);
	
	        // Analyze received message
            if (ctx.getStatusCode() == 407) {
                // 407 response received
            	if (logger.isActivated()) {
            		logger.debug("407 response received");
            	}

    	        // Set the Proxy-Authorization header
            	authenticationAgent.readProxyAuthenticateHeader(ctx.getSipResponse());

                // Increment the Cseq number of the dialog path
            	dialogPath.incrementCseq();

                // Create a second REFER request with the right token
                if (logger.isActivated()) {
                	logger.info("Send second REFER");
                }
    	        refer = SipMessageFactory.createRefer(dialogPath, contactUri);
                
    	        // Set the Authorization header
    	        authenticationAgent.setProxyAuthorizationHeader(refer);
                
                // Send REFER request
    	        ctx = getImsService().getImsModule().getSipManager().sendSipMessageAndWait(refer);

                // Wait response
                if (logger.isActivated()) {
                	logger.debug("Wait response");
                }
                ctx.waitResponse(SipManager.TIMEOUT);

                // Analyze received message
                if ((ctx.getStatusCode() >= 200) && (ctx.getStatusCode() < 300)) {
                    // 200 OK response
                	if (logger.isActivated()) {
                		logger.debug("200 OK response received");
                	}
                	
        			// Notify listeners
        	    	for(int i=0; i < getListeners().size(); i++) {
        	    		((ChatSessionListener)getListeners().get(i)).handleAddParticipantSuccessful();
        	        }
                } else {
                    // Error
                    if (logger.isActivated()) {
                    	logger.debug("REFER has failed (" + ctx.getStatusCode() + ")");
                    }
                    
        			// Notify listeners
        	    	for(int i=0; i < getListeners().size(); i++) {
        	    		((ChatSessionListener)getListeners().get(i)).handleAddParticipantFailed(ctx.getReasonPhrase());
        	        }
                }
            } else
            if ((ctx.getStatusCode() >= 200) && (ctx.getStatusCode() < 300)) {
	            // 200 OK received
            	if (logger.isActivated()) {
            		logger.debug("200 OK response received");
            	}
            	
    			// Notify listeners
    	    	for(int i=0; i < getListeners().size(); i++) {
    	    		((ChatSessionListener)getListeners().get(i)).handleAddParticipantSuccessful();
    	        }
	        } else {
	            // Error responses
            	if (logger.isActivated()) {
            		logger.debug("No response received");
            	}
            	
    			// Notify listeners
    	    	for(int i=0; i < getListeners().size(); i++) {
    	    		((ChatSessionListener)getListeners().get(i)).handleAddParticipantFailed(ctx.getReasonPhrase());
    	        }
	        }
        } catch(Exception e) {
        	if (logger.isActivated()) {
        		logger.error("REFER request has failed", e);
        	}
        	
			// Notify listeners
	    	for(int i=0; i < getListeners().size(); i++) {
	    		((ChatSessionListener)getListeners().get(i)).handleAddParticipantFailed(e.getMessage());
	        }
        }
	}

	/**
	 * Add a list of participants to the session
	 * 
	 * @param participants List of participants
	 */
	public void addParticipants(List<String> participants) {
		try {
        	if (logger.isActivated()) {
        		logger.debug("Add " + participants.size()+ " participants to the session");
        	}
    		
        	SipDialogPath dialogPath;
    		SessionAuthenticationAgent authenticationAgent;
        	if (referUseSameDialog) {
        		// Re-use INVITE dialog path
        		dialogPath = getDialogPath();
        		authenticationAgent = getAuthenticationAgent();
        		
                // Increment the Cseq number of the dialog path
            	dialogPath.incrementCseq();
        	} else {
	    		// Create a dialog path if necessary
	    		authenticationAgent = new SessionAuthenticationAgent();
	
	    		// Set Call-Id
	        	String callId = getImsService().getImsModule().getSipManager().getSipStack().generateCallId();
	
	        	// Set target
	        	String target = getImSessionIdentity();
	
	            // Set local party
	        	String localParty = ImsModule.IMS_USER_PROFILE.getPublicUri();
	
	            // Set remote party
	        	String remoteParty = getImSessionIdentity();
	
	        	// Set the route path
	        	Vector<String> route = getImsService().getImsModule().getSipManager().getSipStack().getServiceRoutePath();
	
	        	// Create a dialog path
	            dialogPath = new SipDialogPath(
	            		getImsService().getImsModule().getSipManager().getSipStack(),
	            		callId,
	            		1,
	            		target,
	            		localParty,
	            		remoteParty,
	            		route);    		
        	}
        	
	        // Send REFER request
    		if (logger.isActivated()) {
        		logger.debug("Send REFER");
        	}
	        SipRequest refer = SipMessageFactory.createRefer(dialogPath, participants);
	        SipTransactionContext ctx = getImsService().getImsModule().getSipManager().sendSipMessageAndWait(refer);
	
	        // Wait response
        	if (logger.isActivated()) {
        		logger.debug("Wait response");
        	}
	        ctx.waitResponse(SipManager.TIMEOUT);
	
	        // Analyze received message
            if (ctx.getStatusCode() == 407) {
                // 407 response received
            	if (logger.isActivated()) {
            		logger.debug("407 response received");
            	}

    	        // Set the Proxy-Authorization header
            	authenticationAgent.readProxyAuthenticateHeader(ctx.getSipResponse());

                // Increment the Cseq number of the dialog path
            	dialogPath.incrementCseq();

    			// Create a second REFER request with the right token
                if (logger.isActivated()) {
                	logger.info("Send second REFER");
                }
    	        refer = SipMessageFactory.createRefer(dialogPath, participants);
                
    	        // Set the Authorization header
    	        authenticationAgent.setProxyAuthorizationHeader(refer);
                
                // Send REFER request
    	        ctx = getImsService().getImsModule().getSipManager().sendSipMessageAndWait(refer);

                // Wait response
                if (logger.isActivated()) {
                	logger.debug("Wait response");
                }
                ctx.waitResponse(SipManager.TIMEOUT);

                // Analyze received message
                if ((ctx.getStatusCode() >= 200) && (ctx.getStatusCode() < 300)) {
                    // 200 OK response
                	if (logger.isActivated()) {
                		logger.debug("20x OK response received");
                	}
                	
        			// Notify listeners
        	    	for(int i=0; i < getListeners().size(); i++) {
        	    		((ChatSessionListener)getListeners().get(i)).handleAddParticipantSuccessful();
        	        }
                } else {
                    // Error
                    if (logger.isActivated()) {
                    	logger.debug("REFER has failed (" + ctx.getStatusCode() + ")");
                    }
                    
        			// Notify listeners
        	    	for(int i=0; i < getListeners().size(); i++) {
        	    		((ChatSessionListener)getListeners().get(i)).handleAddParticipantFailed(ctx.getReasonPhrase());
        	        }
                }
            } else
            if ((ctx.getStatusCode() >= 200) && (ctx.getStatusCode() < 300)) {
	            // 200 OK received
            	if (logger.isActivated()) {
            		logger.debug("20x OK response received");
            	}
            	
    			// Notify listeners
    	    	for(int i=0; i < getListeners().size(); i++) {
    	    		((ChatSessionListener)getListeners().get(i)).handleAddParticipantSuccessful();
    	        }
	        } else {
	            // Error responses
            	if (logger.isActivated()) {
            		logger.debug("No response received");
            	}
            	
    			// Notify listeners
    	    	for(int i=0; i < getListeners().size(); i++) {
    	    		((ChatSessionListener)getListeners().get(i)).handleAddParticipantFailed(ctx.getReasonPhrase());
    	        }
	        }
        } catch(Exception e) {
        	if (logger.isActivated()) {
        		logger.error("REFER request has failed", e);
        	}
        	
			// Notify listeners
	    	for(int i=0; i < getListeners().size(); i++) {
	    		((ChatSessionListener)getListeners().get(i)).handleAddParticipantFailed(e.getMessage());
	        }
        }
	}
}
