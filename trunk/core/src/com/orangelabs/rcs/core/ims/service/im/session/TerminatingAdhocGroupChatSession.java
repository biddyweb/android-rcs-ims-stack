package com.orangelabs.rcs.core.ims.service.im.session;

import java.util.Vector;

import com.orangelabs.rcs.core.ims.ImsModule;
import com.orangelabs.rcs.core.ims.network.sip.SipManager;
import com.orangelabs.rcs.core.ims.network.sip.SipMessageFactory;
import com.orangelabs.rcs.core.ims.network.sip.SipUtils;
import com.orangelabs.rcs.core.ims.protocol.msrp.MsrpEventListener;
import com.orangelabs.rcs.core.ims.protocol.sdp.MediaAttribute;
import com.orangelabs.rcs.core.ims.protocol.sdp.MediaDescription;
import com.orangelabs.rcs.core.ims.protocol.sdp.SdpParser;
import com.orangelabs.rcs.core.ims.protocol.sdp.SdpUtils;
import com.orangelabs.rcs.core.ims.protocol.sip.SipRequest;
import com.orangelabs.rcs.core.ims.protocol.sip.SipResponse;
import com.orangelabs.rcs.core.ims.protocol.sip.SipTransactionContext;
import com.orangelabs.rcs.core.ims.service.ImsService;
import com.orangelabs.rcs.core.ims.service.ImsServiceSession;
import com.orangelabs.rcs.core.ims.service.im.InstantMessageError;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Terminating ad-hoc group chat session
 * 
 * @author jexa7410
 */
public class TerminatingAdhocGroupChatSession extends InstantMessageSession implements MsrpEventListener {
	/**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     * Constructor
     * 
	 * @param parent IMS service
	 * @param invite Initial INVITE request
	 */
	public TerminatingAdhocGroupChatSession(ImsService parent, SipRequest invite) {
		super(parent, invite.getFrom(), invite.getHeader("Subject"));

		// Create dialog path
		createTerminatingDialogPath(invite);
	}

	/**
	 * Background processing
	 */
	public void run() {
		try {
	    	if (logger.isActivated()) {
	    		logger.info("Initiate a new ad-hoc group chat session as terminating");
	    	}
	    		
	    	// Send a 180 Ringing
			send180Ringing(getDialogPath().getInvite(), getDialogPath().getLocalTag());
			
			// Wait invitation answer
	    	int answer = waitInvitationAnswer();
			if (answer == ImsServiceSession.INVITATION_REJECTED) {
				if (logger.isActivated()) {
					logger.debug("Session has been rejected by user");
				}
				
		    	// Remove the current session
		    	getImsService().removeSession(this);

		    	// Notify listener
		        if (getListener() != null) {
	        		getListener().handleSessionAborted();
		        }
				return;
			} else
			if (answer == ImsServiceSession.INVITATION_NOT_ANSWERED) {
				if (logger.isActivated()) {
					logger.debug("Session has been rejected on timeout");
				}

				// Ringing period timeout
				send603Decline(getDialogPath().getInvite(), getDialogPath().getLocalTag());
				
		    	// Remove the current session
		    	getImsService().removeSession(this);

		    	// Notify listener
		        if (getListener() != null) {
	        		getListener().handleSessionAborted();
		        }
				return;
			}

        	// Parse the remote SDP part
        	SdpParser parser = new SdpParser(getDialogPath().getRemoteSdp().getBytes());
    		Vector<MediaDescription> media = parser.getMediaDescriptions();
			MediaDescription desc = media.elementAt(0);
			MediaAttribute attr1 = desc.getMediaAttribute("path");
            String remotePath = attr1.getValue();
    		String remoteHost = SdpUtils.extractRemoteHost(parser.sessionDescription.connectionInfo);
    		int remotePort = desc.port;
			
            // Extract the "setup" parameter
            String remoteSetup = "active";
			MediaAttribute attr4 = desc.getMediaAttribute("setup");
			if (attr4 != null) {
				remoteSetup = attr4.getValue();
			}
            if (logger.isActivated()){
				logger.debug("Remote setup attribute is " + remoteSetup);
			}
            
    		// Create the MSRP session
            String localSetup = "passive";
            if (remoteSetup.equals("active")) {
            	// Passive mode: the terminal should wait a media connection
            	getMsrpMgr().createMsrpServerSession(remotePath, this);
    			localSetup = "passive";
            } else 
            if (remoteSetup.equals("passive")) {
            	// Active mode: the terminal should initiate a media connection
            	getMsrpMgr().createMsrpClientSession(remoteHost, remotePort, remotePath, this);
    			localSetup = "active";
            } else {
            	// The terminal decide to be in passive mode by default
            	getMsrpMgr().createMsrpServerSession(remotePath, this);
    			localSetup = "passive";
            }
            if (logger.isActivated()){
				logger.debug("Local setup attribute is " + localSetup);
			}

			// Build SDP part
	    	String ntpTime = SipUtils.constructNTPtime(System.currentTimeMillis());
	    	String sdp =
	    		"v=0" + SipUtils.CRLF +
	            "o=" + ImsModule.IMS_USER_PROFILE.getUsername() + " "
						+ ntpTime + " " + ntpTime + " IN IP4 "
						+ getDialogPath().getSipStack().getLocalIpAddress() + SipUtils.CRLF +
	            "s=-" + SipUtils.CRLF +
				"c=IN IP4 " + getDialogPath().getSipStack().getLocalIpAddress() + SipUtils.CRLF +
	            "t=0 0" + SipUtils.CRLF +			
	            "m=message " + getMsrpMgr().getLocalMsrpPort() + " TCP/MSRP *" + SipUtils.CRLF +
	            "a=connexion:new" + SipUtils.CRLF +
	            "a=setup:" + localSetup + SipUtils.CRLF +
	            "a=accept-types:message/cpim" + SipUtils.CRLF +
	            "a=path:" + getMsrpMgr().getLocalMsrpPath() + SipUtils.CRLF +
	    		"a=sendrecv" + SipUtils.CRLF;

	    	// Set the local SDP part in the dialog path
	        getDialogPath().setLocalSdp(sdp);

	        // Test if the session should be interrupted
			if (isInterrupted()) {
				if (logger.isActivated()) {
					logger.debug("Session has been interrupted: end of processing");
				}
				return;
			}
	        
	        // Send a 200 OK response
        	if (logger.isActivated()) {
        		logger.info("Send 200 OK");
        	}
            SipResponse resp = SipMessageFactory.create200OkInviteResponse(getDialogPath(), sdp);

	        // Set feature tags
	        String[] tags = {SipUtils.FEATURE_OMA_IM};
	        SipUtils.setFeatureTags(getDialogPath().getInvite(), tags);

	        // Send the response
            SipTransactionContext ctx = getImsService().getImsModule().getSipManager().sendSipMessageAndWait(resp);
    		
	        // The signalisation is established
	        getDialogPath().sigEstablished();

            // Wait response
            ctx.waitResponse(SipManager.TIMEOUT);
            
            // Analyze the received response 
            if (ctx.isSipAck()) {
    	        // ACK received
    			if (logger.isActivated()) {
    				logger.info("ACK request received");
    			}

    	        // The session is established
    	        getDialogPath().sessionEstablished();

    	        // Notify listener
    	        if (getListener() != null) {
    	        	getListener().handleSessionStarted();
    	        }
            } else {
        		if (logger.isActivated()) {
            		logger.debug("No ACK received for INVITE");
            	}

        		// No response received: timeout
            	handleError(new InstantMessageError(InstantMessageError.SESSION_INITIATION_FAILED));
            }
		} catch(Exception e) {
        	if (logger.isActivated()) {
        		logger.error("Session initiation has failed", e);
        	}

        	// Unexpected error
			handleError(new InstantMessageError(InstantMessageError.UNEXPECTED_EXCEPTION,
					e.getMessage()));
		}		
	}
}
