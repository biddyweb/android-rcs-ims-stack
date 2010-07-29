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

import java.util.Vector;

import com.orangelabs.rcs.core.ims.ImsModule;
import com.orangelabs.rcs.core.ims.network.sip.SipManager;
import com.orangelabs.rcs.core.ims.network.sip.SipMessageFactory;
import com.orangelabs.rcs.core.ims.network.sip.SipUtils;
import com.orangelabs.rcs.core.ims.protocol.sip.SipDialogPath;
import com.orangelabs.rcs.core.ims.protocol.sip.SipRequest;
import com.orangelabs.rcs.core.ims.protocol.sip.SipResponse;
import com.orangelabs.rcs.core.ims.protocol.sip.SipTransactionContext;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * IMS service session
 * 
 * @author jexa7410
 */
public abstract class ImsServiceSession extends Thread {
	/**
	 * Ringing period (in seconds)
	 */
	public static int RINGING_PERIOD = 30;

	/**
	 * Session invitation status
	 */
	public final static int INVITATION_NOT_ANSWERED = 0; 
	public final static int INVITATION_ACCEPTED = 1; 
	public final static int INVITATION_REJECTED = 2; 
	
    /**
     * IMS service
     */
    private ImsService imsService;
    
    /**
     * Session ID
     */
    private String sessionId = "" + System.currentTimeMillis();

	/**
	 * Remote contact
	 */
	private String contact;

    /**
	 * Dialog path
	 */
    private SipDialogPath dialogPath = null;

	/**
	 * Authentication agent
	 */
	private SessionAuthenticationAgent authenticationAgent = new SessionAuthenticationAgent();

	/**
	 * Session invitation status
	 */
	private int invitationStatus = INVITATION_NOT_ANSWERED;
	
	/**
	 * Wait user answer for session invitation
	 */
	private Object waitUserAnswer = new Object();

	/**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());

    /**
	 * Constructor
	 * 
	 * @param parent IMS service
	 * @param contact Remote contact
	 */
	public ImsServiceSession(ImsService imsService, String contact) {
        this.imsService = imsService;
		this.contact = contact;
	}

		/**
	 * Create originating dialog path
	 */
	public void createOriginatingDialogPath() {
        // Set Call-Id
    	String callId = getImsService().getImsModule().getSipManager().generateCallId();

    	// Set the route path
    	Vector<String> route = getImsService().getImsModule().getSipManager().getSipStack().getDefaultRoutePath();
    	
       	// Create a dialog path
    	dialogPath = new SipDialogPath(
    			getImsService().getImsModule().getSipManager().getSipStack(),
				callId,
				1,
				getRemoteContact(),
				ImsModule.IMS_USER_PROFILE.getNameAddress(),
				getRemoteContact(),
				route);
    	
    	// Set the authentication agent in the dialog path 
    	dialogPath.setAuthenticationAgent(getAuthenticationAgent());
	}
	
	/**
	 * Create terminating dialog path
	 * 
	 * @param invite Incoming invite
	 */
	public void createTerminatingDialogPath(SipRequest invite) {
	    // Set the call-id
		String callId = invite.getCallId();
	
	    // Set target
	    String target = invite.getContactURI();
	
	    // Set local party
	    String localParty = invite.getTo();
	
	    // Set remote party
	    String remoteParty = invite.getFrom();
	
	    // Get the CSeq value
	    int cseq = invite.getCSeq();
	    
	    // Set the route path with the Record-Route 
	    Vector<String> route = SipUtils.routeProcessing(invite, false);
	    
	   	// Create a dialog path
		dialogPath = new SipDialogPath(
				getImsService().getImsModule().getSipManager().getSipStack(),
				callId,
				cseq,
				target,
				localParty,
				remoteParty,
				route);
	
	    // Set the INVITE request
		dialogPath.setInvite(invite);
	
	    // Set the remote tag
		dialogPath.setRemoteTag(SipUtils.extractTag(invite.getFrom()));
	
	    // Set the remote SDP part
		dialogPath.setRemoteSdp(invite.getContent());
	}
	
	/**
	 * Start the session in background
	 */
	public void startSession() {
		// Add the session in the session manager
		imsService.addSession(this);
		
		// Start the session
		start();
	}
	
	/**
	 * Return the IMS service
	 * 
	 * @return IMS service
	 */
	public ImsService getImsService() {
		return imsService;
	}
	
	/**
	 * Return the session ID
	 * 
	 * @return Session ID
	 */
	public String getSessionID() {
		return sessionId;
	}

	/**
	 * Returns the remote contact
	 * 
	 * @return String
	 */
	public String getRemoteContact() {
		return contact;
	}

	/**
	 * Get the dialog path of the session
	 * 
	 * @return Dialog path object
	 */
	public SipDialogPath getDialogPath() {
		return dialogPath;
	}

	/**
	 * Set the dialog path of the session
	 * 
	 * @param dialog Dialog path
	 */
	public void setDialogPath(SipDialogPath dialog) {
		dialogPath = dialog;
	}
	
    /**
     * Returns the authentication agent
     * 
     * @return Authentication agent
     */
	public SessionAuthenticationAgent getAuthenticationAgent() {
		return authenticationAgent;
	}

	/**
	 * Reject the session invitation
	 */
	public void rejectSession() {
		if (logger.isActivated()) {
			logger.debug("Session invitation has been rejected");
		}
		invitationStatus = INVITATION_REJECTED;

		// Unblock semaphore
		synchronized(waitUserAnswer) {
			waitUserAnswer.notifyAll();
		}

		// Decline the invitation
		send603Decline(getDialogPath().getInvite(),	getDialogPath().getLocalTag());
			
		// Remove the session in the session manager
		imsService.removeSession(this);
	}	
	
	/**
	 * Accept the session invitation
	 */
	public void acceptSession() {
		if (logger.isActivated()) {
			logger.debug("Session invitation has been accepted");
		}
		invitationStatus = INVITATION_ACCEPTED;

		// Unblock semaphore
		synchronized(waitUserAnswer) {
			waitUserAnswer.notifyAll();
		}
	}
		
	/**
	 * Wait session invitation answer
	 * 
	 * @return Answer
	 */
	public int waitInvitationAnswer() {
		if (invitationStatus != INVITATION_NOT_ANSWERED) {
			return invitationStatus;
		}
		
		if (logger.isActivated()) {
			logger.debug("Wait session invitation answer");
		}
		
		// Wait until received response or received timeout
		try {
			synchronized(waitUserAnswer) {
				waitUserAnswer.wait(RINGING_PERIOD * 1000);
			}
		} catch(InterruptedException e) {
			// Nothing to do
		}
		
		return invitationStatus;
	}
	
	/**
	 * Send ACK corresponding to a response
	 * 
	 * @param dialog SIP dialog path
	 */
	public void sendAck(SipDialogPath dialog) {
		try {
			SipRequest ack = SipMessageFactory.createAck(dialog);
			imsService.getImsModule().getSipManager().sendSipMessage(ack);
		} catch(Exception e) {
			if (logger.isActivated()) {
				logger.error("Can't send SIP ACK", e);
			}
		}
	}

	/**
     * Send a 180 Ringing response to the remote party
     * 
     * @param request SIP request
     * @param localTag Local tag
     */
	public void send180Ringing(SipRequest request, String localTag) {
    	try {
	    	SipResponse progress = SipMessageFactory.createResponse(request, localTag, 180);
            getImsService().getImsModule().getSipManager().sendSipMessage(progress);
    	} catch(Exception e) {
    		if (logger.isActivated()) {
    			logger.error("Can't send a 180 Ringing response");
    		}
    	}
    }
	
    /**
     * Send a 603 "Decline" to the remote party
     * 
     * @param request SIP request
     * @param localTag Local tag
     */
	public void send603Decline(SipRequest request, String localTag) {
		try {
	        // Send a 603 Decline error
	    	if (logger.isActivated()) {
	    		logger.info("Send 603 Decline");
	    	}
	        SipResponse resp = SipMessageFactory.createResponse(request, localTag, 603);
	        getImsService().getImsModule().getSipManager().sendSipMessage(resp);
		} catch(Exception e) {
			if (logger.isActivated()) {
				logger.error("Can't send 603 Decline response", e);
			}
		}
	}
	
    /**
     * Send a 405 "Method Not Allowed" to the remote party
     * 
     * @param request SIP request
     */
	public void send405Error(SipRequest request) {
		try {
	        // Send a 405 error
	    	if (logger.isActivated()) {
	    		logger.info("Send 405 Method Not Allowed");
	    	}
	        SipResponse resp = SipMessageFactory.createResponse(request, 405);
	        // TODO: set Allow header
	        getImsService().getImsModule().getSipManager().sendSipMessage(resp);
		} catch(Exception e) {
			if (logger.isActivated()) {
				logger.error("Can't send 405 error response", e);
			}
		}
	}
	
    /**
     * Send a 415 "Unsupported Media Type" to the remote party
     * 
     * @param request SIP request
     */
	public void send415Error(SipRequest request) {
		try {
	        // Send a 415 error
	    	if (logger.isActivated()) {
	    		logger.info("Send 415 Unsupported Media Type");
	    	}
	        SipResponse resp = SipMessageFactory.createResponse(request, 415);
	        // TODO: set Accept-Encoding header
	        getImsService().getImsModule().getSipManager().sendSipMessage(resp);
		} catch(Exception e) {
			if (logger.isActivated()) {
				logger.error("Can't send 405 error response", e);
			}
		}
	}
	
	/**
	 * Interrupt session
	 */
	public void interruptSession() {
		if (logger.isActivated()) {
			logger.debug("Interrupt the session");
		}

		try {
			// Unblock semaphore
			synchronized(waitUserAnswer) {
				waitUserAnswer.notifyAll();
			}
			
			// Interrupt thread
			interrupt();
		} catch (Exception e) {
        	if (logger.isActivated()) {
        		logger.error("Can't interrupt the session correctly", e);
        	}
		}
    	if (logger.isActivated()) {
    		logger.debug("Session has been interrupted");
    	}
	}
	
	/**
	 * Terminate session
	 */
	public void terminateSession() {
		if (logger.isActivated()) {
			logger.debug("Terminate the session");
		}

		// Unblock semaphore (used for terminating side only)
		synchronized(waitUserAnswer) {
			waitUserAnswer.notifyAll();
		}

		try {
			// Terminate the session
        	if (dialogPath.isSigEstablished()) {
    	        // Increment the Cseq number of the dialog path
    	        getDialogPath().incrementCseq();

    	        // Create BYE
            	if (logger.isActivated()) {
            		logger.info("Send BYE");
            	}
		        SipRequest bye = SipMessageFactory.createBye(getDialogPath());
		        
				// Set the Proxy-Authorization header
		        if (getDialogPath().getAuthenticationAgent() != null) {
		        	getDialogPath().getAuthenticationAgent().setProxyAuthorizationHeader(bye);
		        }
		        
    	        // Send BYE
		        SipTransactionContext ctx = getImsService().getImsModule().getSipManager().sendSipMessageAndWait(bye);

		        // Update dialog path
				dialogPath.sessionTerminated();

		        // Wait response
		        ctx.waitResponse(SipManager.TIMEOUT);
		        if (ctx.isSipResponse()) {		        
					if (logger.isActivated()) {
						logger.info("BYE response received: resp=" + ctx.getStatusCode());
					}
		        } else {
					if (logger.isActivated()) {
						logger.info("No response received for BYE");
					}
		        }
        	} else {
   				// Create a CANCEL
            	if (logger.isActivated()) {
            		logger.info("Send CANCEL");
            	}
    	        SipRequest cancel = SipMessageFactory.createCancel(getDialogPath());

				// Set the Proxy-Authorization header
		        if (getDialogPath().getAuthenticationAgent() != null) {
		        	getDialogPath().getAuthenticationAgent().setProxyAuthorizationHeader(cancel);
		        }
    	        
    	        // Send CANCEL
		        SipTransactionContext ctx = getImsService().getImsModule().getSipManager().sendSipMessageAndWait(cancel);
    	        
		        // Update dialog path
    			dialogPath.sessionCancelled();

    			// Wait response
		        ctx.waitResponse(SipManager.TIMEOUT);
		        if (ctx.isSipResponse()) {		        
					if (logger.isActivated()) {
						logger.info("CANCEL response received: resp=" + ctx.getStatusCode());
					}
		        } else {
					if (logger.isActivated()) {
						logger.info("No response received for CANCEL");
					}
		        }
        	}

        	if (logger.isActivated()) {
        		logger.debug("SIP session has been terminated");
        	}
		} catch(Exception e) { 
        	if (logger.isActivated()) {
        		logger.error("Session termination has failed", e);
        	}
		}
	}


	/**
	 * Abort the session
	 */
	public abstract void abortSession();
		
	/**
	 * Receive re-INVITE request 
	 * 
	 * @param reInvite re-INVITE request
	 */
	public abstract void receiveReInvite(SipRequest reInvite);

	/**
	 * Receive UPDATE request 
	 * 
	 * @param update UPDATE request
	 */
	public abstract void receiveUpdate(SipRequest update);

	/**
	 * Receive BYE request 
	 * 
	 * @param bye BYE request
	 */
	public abstract void receiveBye(SipRequest bye);

	/**
	 * Receive CANCEL request 
	 * 
	 * @param cancel CANCEL request
	 */
	public abstract void receiveCancel(SipRequest cancel);
}
