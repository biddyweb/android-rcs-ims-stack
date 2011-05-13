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

import java.util.Vector;

import com.orangelabs.rcs.core.ims.ImsModule;
import com.orangelabs.rcs.core.ims.network.sip.SipMessageFactory;
import com.orangelabs.rcs.core.ims.network.sip.SipUtils;
import com.orangelabs.rcs.core.ims.protocol.sip.SipDialogPath;
import com.orangelabs.rcs.core.ims.protocol.sip.SipRequest;
import com.orangelabs.rcs.core.ims.protocol.sip.SipResponse;
import com.orangelabs.rcs.utils.PhoneUtils;
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
	 * Session listener
	 */
	private ImsSessionListener listener = null;

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
    	String callId = getImsService().getImsModule().getSipManager().getSipStack().generateCallId();

    	// Set the route path
    	Vector<String> route = getImsService().getImsModule().getSipManager().getSipStack().getServiceRoutePath();

    	// Create a dialog path
    	dialogPath = new SipDialogPath(
    			getImsService().getImsModule().getSipManager().getSipStack(),
    			callId,
				1,
				getRemoteContact(),
				// TODO: remove format number when bug corrected on IMS
				PhoneUtils.formatNumberToSipAddress(ImsModule.IMS_USER_PROFILE.getPublicUri()),
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
	    long cseq = invite.getCSeq();
	    
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
		dialogPath.setRemoteTag(invite.getFromTag());
	
	    // Set the remote content part
		dialogPath.setRemoteContent(invite.getContent());
	}
	
	/**
	 * Add a listener for receiving events
	 * 
	 * @param listener Listener
	 */
	public void addListener(ImsSessionListener listener) {
		this.listener = listener;
	}

	/**
	 * Remove the listener
	 */
	public void removeListener() {
		listener = null;
	}

	/**
	 * Returns the event listener
	 * 
	 * @return Listener
	 */
	public ImsSessionListener getListener() {
		return listener;
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
			
			if (!isInterrupted()) {
				// Interrupt thread
				interrupt();
			}
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
	 * Abort the session
	 */
	public void abortSession(){
    	if (logger.isActivated()) {
    		logger.info("Abort the session");
    	}
    	
    	// Interrupt the session
    	interruptSession();

    	// Close media session
    	closeMediaSession();
    	
        // Terminate session
		terminateSession();

    	// Remove the current session
    	getImsService().removeSession(this);

    	// Notify listener
        if (getListener() != null) {
        	getListener().handleSessionAborted();
        }
	}

	/**
	 * Terminate session 
	 */
	public void terminateSession() {
		if (logger.isActivated()) {
			logger.debug("Terminate the session");
		}
		
		if (dialogPath.isSessionTerminated()) {
			// Already terminated
			return;
		}

		// Update dialog path
		dialogPath.sessionTerminated();

		// Unblock semaphore (used for terminating side only)
		synchronized(waitUserAnswer) {
			waitUserAnswer.notifyAll();
		}

		try {
			// Terminate the session
        	if (dialogPath.isSigEstablished()) {
		        // Increment the Cseq number of the dialog path
		        getDialogPath().incrementCseq();
		        // TODO: necessary ?
	
		        // Send BYE without waiting a response
		        getImsService().getImsModule().getSipManager().sendSipBye(getDialogPath());
        	} else {
		        // Send CANCEL without waiting a response
		        getImsService().getImsModule().getSipManager().sendSipCancel(getDialogPath());
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
	 * Receive BYE request 
	 * 
	 * @param bye BYE request
	 */
	public void receiveBye(SipRequest bye) {
    	if (logger.isActivated()) {
    		logger.info("Receive a BYE message from the remote");
    	}

    	// Close media session
    	closeMediaSession();
    	
        // Update the dialog path status
		getDialogPath().sessionTerminated();
	
		// Send a 200 OK response
		try {
			if (logger.isActivated()) {
				logger.info("Send 200 OK");
			}
	        SipResponse response = SipMessageFactory.createResponse(bye, 200);
			getImsService().getImsModule().getSipManager().sendSipResponse(response);
		} catch(Exception e) {
	       	if (logger.isActivated()) {
	    		logger.error("Can't send 200 OK response", e);
	    	}
		}

    	// Remove the current session
    	getImsService().removeSession(this);
	
        // Notify listener
        if (getListener() != null) {
        	getListener().handleSessionTerminatedByRemote();
        }
        
        // Request capabilities to the remote
        getImsService().getImsModule().getCapabilityService().requestContactCapabilities(getDialogPath().getRemoteParty());
	}
	
	/**
	 * Receive CANCEL request 
	 * 
	 * @param cancel CANCEL request
	 */
	public void receiveCancel(SipRequest cancel) {
    	if (logger.isActivated()) {
    		logger.info("Receive a CANCEL message from the remote");
    	}

		if (getDialogPath().isSigEstablished()) {
	    	if (logger.isActivated()) {
	    		logger.info("Ignore the received CANCEL message from the remote (session already established)");
	    	}
			return;
		}

    	// Close media session
    	closeMediaSession();
    	
    	// Update dialog path
		getDialogPath().sessionCancelled();

		// Send a 200 OK
    	try {
	    	if (logger.isActivated()) {
	    		logger.info("Send 200 OK");
	    	}
	        SipResponse cancelResp = SipMessageFactory.createResponse(cancel, 200);
	        getImsService().getImsModule().getSipManager().sendSipResponse(cancelResp);
	        
			// Send a 487 Request terminated
	    	if (logger.isActivated()) {
	    		logger.info("Send 487 Request terminated");
	    	}
	        SipResponse terminatedResp = SipMessageFactory.createResponse(getDialogPath().getInvite(), 487);
	        getImsService().getImsModule().getSipManager().sendSipResponse(terminatedResp);
		} catch(Exception e) {
	    	if (logger.isActivated()) {
	    		logger.error("Session has been cancelled", e);
	    	}
		}
		
    	// Remove the current session
    	getImsService().removeSession(this);

		// Notify listener
        if (getListener() != null) {
        	getListener().handleSessionTerminatedByRemote();
        }
        
        // Request capabilities to the remote
        getImsService().getImsModule().getCapabilityService().requestContactCapabilities(getDialogPath().getRemoteParty());
	}

	/**
	 * Receive re-INVITE request 
	 * 
	 * @param reInvite re-INVITE request
	 */
	public void receiveReInvite(SipRequest reInvite) {
		send405Error(reInvite);		
	}

	/**
	 * Receive UPDATE request 
	 * 
	 * @param update UPDATE request
	 */
	public void receiveUpdate(SipRequest update) {
		send405Error(update);		
	}

	/**
	 * Close media session
	 */
	public abstract void closeMediaSession();

	/**
     * Send a 180 Ringing response to the remote party
     * 
     * @param request SIP request
     * @param localTag Local tag
     */
	public void send180Ringing(SipRequest request, String localTag) {
    	try {
	    	SipResponse progress = SipMessageFactory.createResponse(request, localTag, 180);
            getImsService().getImsModule().getSipManager().sendSipResponse(progress);
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
	        getImsService().getImsModule().getSipManager().sendSipResponse(resp);
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
	        SipUtils.buildAllowHeader(resp.getStackMessage());
	        getImsService().getImsModule().getSipManager().sendSipResponse(resp);
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
	        getImsService().getImsModule().getSipManager().sendSipResponse(resp);
		} catch(Exception e) {
			if (logger.isActivated()) {
				logger.error("Can't send 405 error response", e);
			}
		}
	}
}
