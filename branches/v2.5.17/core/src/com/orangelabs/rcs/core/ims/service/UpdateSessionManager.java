package com.orangelabs.rcs.core.ims.service;

import javax2.sip.Dialog;

import com.orangelabs.rcs.core.CoreException;
import com.orangelabs.rcs.core.ims.network.sip.SipMessageFactory;
import com.orangelabs.rcs.core.ims.protocol.sip.SipException;
import com.orangelabs.rcs.core.ims.protocol.sip.SipRequest;
import com.orangelabs.rcs.core.ims.protocol.sip.SipResponse;
import com.orangelabs.rcs.core.ims.protocol.sip.SipTransactionContext;
import com.orangelabs.rcs.core.ims.service.ipcall.IPCallError;
import com.orangelabs.rcs.provider.settings.RcsSettings;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Update session manager
 * 
 * @author O. Magnon
 */
public class UpdateSessionManager {
	
	/**
	 * Session to be renegociated
	 */
	private ImsServiceSession session;	
	
	/**
	 * Re-Invite invitation status
	 */
	private int reInviteStatus = ImsServiceSession.INVITATION_NOT_ANSWERED;

	/**
	 * Wait user answer for reInvite invitation
	 */
	private Object waitUserAnswer = new Object();
	
	/**
     * Ringing period (in seconds)
     */
    private int ringingPeriod = RcsSettings.getInstance().getRingingPeriod();
	
	/**
	 * The logger
	 */
	private final Logger logger = Logger.getLogger(this.getClass().getName());
	
	/**
	 * Constructor
	 * 
	 * @param session Session to be refreshed
	 */
	public UpdateSessionManager(ImsServiceSession mysession) {
		this.session  =  mysession;	
	}

	
	/**
	 * Create ReInvite
	 * 
	 * @param featureTags featureTags to set in reInvite
	 * @param content reInvite content
	 * @return reInvite request
	 */
	public SipRequest createReInvite(String[] featureTags, String content) {
		if (logger.isActivated()) {
			logger.debug("createReInvite()");
		}
		
		SipRequest reInvite = null;
		
		try {
			// Increment the Cseq number of the dialog path
			session.getDialogPath().incrementCseq();

			// Increment internal stack CSeq (NIST stack issue?)
			Dialog dlg = session.getDialogPath().getStackDialog();
			while ((dlg != null) && (dlg.getLocalSeqNumber()< session.getDialogPath().getCseq())){
					dlg.incrementLocalSequenceNumber();			
			}
			
			// create ReInvite
			reInvite = SipMessageFactory.createReInvite(session.getDialogPath(), featureTags, content);			

			// Set the Authorization header
			session.getAuthenticationAgent().setAuthorizationHeader(reInvite);

			// Set the Proxy-Authorization header
            session.getAuthenticationAgent().setProxyAuthorizationHeader(reInvite);
            
		} catch (SipException e) {
			// Unexpected error
			session.handleError(new IPCallError(IPCallError.UNEXPECTED_EXCEPTION,
					e.getMessage()));
			if (logger.isActivated()) {
				logger.error("Create ReInvite has failed", e);
			}
		} catch (CoreException e) {
			// Unexpected error
			session.handleError(new IPCallError(IPCallError.UNEXPECTED_EXCEPTION,
					e.getMessage()));
			if (logger.isActivated()) {
				logger.error("Create ReInvite has failed", e);
			}
		}
		
		return reInvite;
		
	}
	
	
	/**
	 * Send ReInvite
	 * 
	 * @param request ReInvite request
	 * @param sessionManagerObj  Update session events listener
	 */
	public void sendReInvite(SipRequest request, UpdateSessionManagerListener sessionManagerObj) {
		if (logger.isActivated()) {
			logger.debug("sendReInvite()");
		}

		final SipRequest reInvite = request;
		final UpdateSessionManagerListener sessionMngrObj = sessionManagerObj;

		Thread thread = new Thread() {
			public void run() {
				SipTransactionContext ctx;
				try {
					// Send ReINVITE request
					ctx = session
							.getImsService()
							.getImsModule()
							.getSipManager()
							.sendSipMessageAndWait(reInvite,
									session.getResponseTimeout());

					if (ctx.isSipResponse()) { // Analyze the received response
						if (ctx.getStatusCode() == 200) { 														
							// set received sdp response as remote sdp content
							session.getDialogPath().setRemoteContent(ctx.getSipResponse().getSdpContent());	
							
							// send SIP ACK
							session.getImsService().getImsModule()
									.getSipManager()
									.sendSipAck(session.getDialogPath());
							
							// notify session with 200OK response
							sessionMngrObj.handleReInviteResponse(200, ctx.getSipResponse());

						} else if (ctx.getStatusCode() == 603) {
							// notify session with 603 response
							sessionMngrObj.handleReInviteResponse(ImsServiceSession.INVITATION_REJECTED, ctx.getSipResponse());
						
						} else if (ctx.getStatusCode() == 408) {
							// notify session with 408 response
							sessionMngrObj.handleReInviteResponse(
									ImsServiceSession.TERMINATION_BY_TIMEOUT, ctx.getSipResponse());
						
						} else if (ctx.getStatusCode() == 407) {
							// notify session with 407 Proxy Authent required
							session.handleReInvite407ProxyAuthent(ctx.getSipResponse(), sessionMngrObj);
						
						} else {
							// Other error response => generate call error
							session.handleError(new ImsSessionBasedServiceError(
									ImsSessionBasedServiceError.UNEXPECTED_EXCEPTION,
									ctx.getSipResponse().getStatusCode()
											+ " "
											+ ctx.getSipResponse()
													.getReasonPhrase()));
						}
					} else {
						// No response received: timeout => notify session						
						sessionMngrObj.handleReInviteResponse(ImsServiceSession.TERMINATION_BY_TIMEOUT, ctx.getSipResponse());
					}
				} catch (SipException e) {
					// Unexpected error => generate call error
					if (logger.isActivated()) {
						logger.error("Send ReInvite has failed", e);
					}
					session.handleError(new ImsSessionBasedServiceError(
							ImsSessionBasedServiceError.UNEXPECTED_EXCEPTION, e
									.getMessage()));
				}
			}
		};
		thread.start();

	}
	
	/**
     * Send RE-INVITE Response 
     * 
     * @param request RE-INVITE request received
     * @param featureTags featureTags to set in response
     * @param sessionManagerObj  Update session events listener
     */
   public void send200OkReInviteResp(SipRequest request, String[] featureTags, String sdpResponse, UpdateSessionManagerListener sessionManagerObj) {	
        if (logger.isActivated()) {
            logger.debug("send200OkReInviteResp()");
        }
        
    	final SipRequest reInvite = request;
    	final String sdp = sdpResponse;
    	final UpdateSessionManagerListener sessionMngrObj = sessionManagerObj;
    	final String[] respFeatureTags = featureTags;

    	Thread thread = new Thread() {
    			public void run(){
    		        try {  		        	   		               		            
    		            //Create 200 OK response
    		            SipResponse resp = SipMessageFactory.create200OkReInviteResponse(session.getDialogPath(), reInvite, respFeatureTags, sdp);
    		            
    		            // Send 200 OK response
    		            SipTransactionContext ctx = session.getImsService().getImsModule().getSipManager().sendSipMessageAndWait(resp);

    		            // Analyze the received response 
    		            if (ctx.isSipAck()) {// ACK received     		            	
    		                //notify local listener 
    		            	sessionMngrObj.handleReInviteAck(200) ;
    		            
    		            } else { // No ACK received     	
    		               // generate call error for local client
    		                session.handleError(new ImsSessionBasedServiceError(ImsSessionBasedServiceError.UNEXPECTED_EXCEPTION, "ack not received"));
    		            }
    		        } catch(Exception e) { 
    		        	// Unexpected error => generate call error for local client
    		            if (logger.isActivated()) {
    		                logger.error("Session ReInvite Response has failed", e);
    		            }	         
    		            session.handleError(new ImsSessionBasedServiceError(ImsSessionBasedServiceError.UNEXPECTED_EXCEPTION,
    		                    e.getMessage()));
    		        }    		        
    			}   	
    	}; 	
    	thread.start() ;
   }
    
    /**
     * Wait user answer and send RE-INVITE response
     * 
     * @param request RE-INVITE request received
     * @param featureTags featureTags to set in response
     * @param sessionManagerObj  Update session events listener
     */
	public void waitUserAckAndSendReInviteResp(SipRequest request, String[] featureTags, UpdateSessionManagerListener sessionManagerObj) {
		if (logger.isActivated()) {
			logger.debug("waitUserAckAndSendReInviteResp()");
		}
		
		reInviteStatus = ImsServiceSession.INVITATION_NOT_ANSWERED;
    	final SipRequest reInvite = request;
    	final UpdateSessionManagerListener sessionMngrObj = sessionManagerObj;
    	final String[] respFeatureTags = featureTags;

    	Thread thread = new Thread() {
			public void run() {
				try {
					// wait user answer
					int answer = waitInvitationAnswer();

					if (answer == ImsServiceSession.INVITATION_REJECTED) { 					
						// send error to remote client
						session.sendErrorResponse(reInvite, session.getDialogPath().getLocalTag(), 603);	
						sessionMngrObj.handleReInviteUserAnswer(ImsServiceSession.INVITATION_REJECTED);
					
					} else if (answer == ImsServiceSession.INVITATION_NOT_ANSWERED) { 
						// send error to remote client						
						session.sendErrorResponse(reInvite, session.getDialogPath().getLocalTag(), 603);
						
						// notify local client
						sessionMngrObj.handleReInviteUserAnswer(ImsServiceSession.INVITATION_NOT_ANSWERED) ;
					
					} else if (answer == ImsServiceSession.INVITATION_ACCEPTED){
						// build sdp response
						String sdp = sessionMngrObj.buildReInviteSdpResponse(reInvite);
						
						if (sdp == null) { // sdp null -  terminate session and send error
								session.handleError(new ImsSessionBasedServiceError(
										ImsSessionBasedServiceError.UNEXPECTED_EXCEPTION,
										"error on sdp building, sdp is null "));
								
								return;
						 }
						
						// set sdp response as local content
						session.getDialogPath().setLocalContent(sdp);
						
						// session.handleReInviteUserAnswer(ImsServiceSession.INVITATION_ACCEPTED, reInviteContext);
						sessionMngrObj.handleReInviteUserAnswer(ImsServiceSession.INVITATION_ACCEPTED);
						
						//create 200OK response
						SipResponse resp = SipMessageFactory
								.create200OkReInviteResponse(
										session.getDialogPath(), reInvite, respFeatureTags, sdp);

						// Send response
						SipTransactionContext ctx = session.getImsService()
								.getImsModule().getSipManager()
								.sendSipMessageAndWait(resp);

						// Analyze the received response
						if (ctx.isSipAck()) {// ACK received						
							//notify local listener  
    		            	sessionMngrObj.handleReInviteAck(200);
						
						} else {// No ACK  received: send error	
							session.handleError(new ImsSessionBasedServiceError(
									ImsSessionBasedServiceError.UNEXPECTED_EXCEPTION,
									"ack not received"));
						}	
					} else {session.handleError(new ImsSessionBasedServiceError(
							ImsSessionBasedServiceError.UNEXPECTED_EXCEPTION,
							"ack not received"));
					}
				} catch (Exception e) {
					if (logger.isActivated()) {
						logger.error("Session update refresh has failed", e);
					}
					// Unexpected error
		            session.handleError(new ImsSessionBasedServiceError(
							ImsSessionBasedServiceError.UNEXPECTED_EXCEPTION,
		                    e.getMessage()));
				}
			}
		};
		thread.start();
	}
    
    
    /**
	 * Reject the invitation
	 * 
	 * @param code Error code
	 */
	public void rejectReInvite(int code) {
		if (logger.isActivated()) {
			logger.debug("ReInvite  has been rejected");
		}
		
		synchronized(waitUserAnswer) {
			reInviteStatus = ImsServiceSession.INVITATION_REJECTED;
			
			// Unblock semaphore
			waitUserAnswer.notifyAll();
		}

		// Decline the invitation
		//session.sendErrorResponse(session.getDialogPath().getInvite(), session.getDialogPath().getLocalTag(), code);			
	}	
	
	
	/**
	 * Accept the invitation
	 */
	public void acceptReInvite() {
		if (logger.isActivated()) {
			logger.debug("ReInvite has been accepted");
		}
		
		synchronized(waitUserAnswer) {
			reInviteStatus = ImsServiceSession.INVITATION_ACCEPTED;
			
			// Unblock semaphore
			waitUserAnswer.notifyAll();
		}
	}
	
	
	/**
	 * Wait invitation answer
	 * 
	 * @return Answer
	 */
	public int waitInvitationAnswer() {
		if (reInviteStatus != ImsServiceSession.INVITATION_NOT_ANSWERED) {
			return reInviteStatus;
		}
		
		if (logger.isActivated()) {
			logger.debug("Wait session invitation answer");
		}
		
		try {
			synchronized(waitUserAnswer) {
				// Wait until received response or received timeout
				waitUserAnswer.wait(ringingPeriod * 500);
			}
		} catch(InterruptedException e) {
			
		}
		
		return reInviteStatus;
	}
	
}
