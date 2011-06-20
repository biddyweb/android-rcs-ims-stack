package com.orangelabs.rcs.core.ims.service.im.chat.imdn;

import com.orangelabs.rcs.core.ims.ImsModule;
import com.orangelabs.rcs.core.ims.network.sip.SipManager;
import com.orangelabs.rcs.core.ims.network.sip.SipMessageFactory;
import com.orangelabs.rcs.core.ims.protocol.sip.SipDialogPath;
import com.orangelabs.rcs.core.ims.protocol.sip.SipRequest;
import com.orangelabs.rcs.core.ims.protocol.sip.SipTransactionContext;
import com.orangelabs.rcs.core.ims.service.ImsServiceSession;
import com.orangelabs.rcs.core.ims.service.SessionAuthenticationAgent;
import com.orangelabs.rcs.core.ims.service.im.chat.cpim.CpimMessage;
import com.orangelabs.rcs.provider.settings.RcsSettings;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * IMDN manager (see RFC5438)
 * 
 * @author jexa7410
 */
public class ImdnManager {
	/**
	 * Activation flag
	 */
	private boolean activated;
	
    /**
     * IM session
     */
    private ImsServiceSession session;
    
    /**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());
    
    /**
     * Constructor
     * 
     * @param session IM session
     */    
    public ImdnManager(ImsServiceSession session) {
    	this.session = session;
    	this.activated = RcsSettings.getInstance().isImReportsActivated();
    }    
    
    /**
     * Add IMDN headers
     * 
     * @param invite INVITE request 
     * @param msgId Message id
     */
    public void addImdnHeaders(SipRequest invite, String msgId) {
    	if (!activated) {
    		return;
    	}

		invite.addHeader(CpimMessage.HEADER_NS, ImdnDocument.IMDN_NAMESPACE);
	    invite.addHeader(ImdnUtils.HEADER_IMDN_DISPO_NOTIF, ImdnDocument.POSITIVE_DELIVERY + ", " + ImdnDocument.NEGATIVE_DELIVERY + ", " + ImdnDocument.DISPLAY);
	    invite.addHeader(ImdnUtils.HEADER_IMDN_MSG_ID, msgId);
    }
    
	/**
	 * Send message delivery status via SIP MESSAGE
	 * 
	 * @param msgId Message ID
	 * @param status Status
	 */
	public void sendSipMessageDeliveryStatus(String msgId, String status) {
		try {
			if (logger.isActivated()) {
       			logger.debug("Send delivery status " + status + " for message " + msgId);
       		}

       		// Create IDMN document
			String imdn = ImdnDocument.buildImdnDocument(msgId, status);

		    // Create authentication agent 
       		SessionAuthenticationAgent authenticationAgent = new SessionAuthenticationAgent();
       		
       		// Create a dialog path
        	SipDialogPath dialogPath = new SipDialogPath(
					session.getImsService().getImsModule().getSipManager().getSipStack(),
					session.getImsService().getImsModule().getSipManager().getSipStack().generateCallId(),
    				1,
    				session.getRemoteContact(),
    				ImsModule.IMS_USER_PROFILE.getPublicUri(),
    				session.getRemoteContact(),
    				session.getImsService().getImsModule().getSipManager().getSipStack().getServiceRoutePath());        	
        	
	        // Create MESSAGE request
        	if (logger.isActivated()) {
        		logger.info("Send first MESSAGE");
        	}
	        SipRequest msg = SipMessageFactory.createMessage(dialogPath, ImdnDocument.MIME_TYPE, imdn);
	        
	        // Add IMDN headers
	        msg.addHeader(CpimMessage.HEADER_NS, ImdnDocument.IMDN_NAMESPACE);
	        msg.addHeader(ImdnUtils.HEADER_IMDN_MSG_ID, msgId);
	        msg.addHeader(CpimMessage.HEADER_CONTENT_DISPOSITION, ImdnDocument.NOTIFICATION);
	        
	        // Send MESSAGE request
	        SipTransactionContext ctx = session.getImsService().getImsModule().getSipManager().sendSipMessageAndWait(msg);
	
	        // Wait response
        	if (logger.isActivated()) {
        		logger.info("Wait response");
        	}
	        ctx.waitResponse(SipManager.TIMEOUT);
	
	        // Analyze received message
            if (ctx.getStatusCode() == 407) {
                // 407 response received
            	if (logger.isActivated()) {
            		logger.info("407 response received");
            	}

    	        // Set the Proxy-Authorization header
            	authenticationAgent.readProxyAuthenticateHeader(ctx.getSipResponse());

                // Increment the Cseq number of the dialog path
                dialogPath.incrementCseq();

                // Create a second MESSAGE request with the right token
                if (logger.isActivated()) {
                	logger.info("Send second MESSAGE");
                }
    	        msg = SipMessageFactory.createMessage(dialogPath, ImdnDocument.MIME_TYPE, imdn);
    	        
    	        // Add IMDN headers
    	        msg.addHeader(CpimMessage.HEADER_NS, ImdnDocument.IMDN_NAMESPACE);
    	        msg.addHeader(ImdnUtils.HEADER_IMDN_MSG_ID, msgId);
    	        msg.addHeader(CpimMessage.HEADER_CONTENT_DISPOSITION, ImdnDocument.NOTIFICATION);
    	        
    	        // Set the Authorization header
    	        authenticationAgent.setProxyAuthorizationHeader(msg);
                
                // Send MESSAGE request
    	        ctx = session.getImsService().getImsModule().getSipManager().sendSipMessageAndWait(msg);

                // Wait response
                if (logger.isActivated()) {
                	logger.info("Wait response");
                }
                ctx.waitResponse(SipManager.TIMEOUT);

                // Analyze received message
                if (ctx.getStatusCode() == 200) {
                    // 200 OK response
                	if (logger.isActivated()) {
                		logger.info("200 OK response received");
                	}
                } else {
                    // Error
                	if (logger.isActivated()) {
                		logger.info("Delivery report has failed: " + ctx.getStatusCode()
    	                    + " response received");
                	}
                }
            } else if (ctx.getStatusCode() == 200) {
	            // 200 OK received
            	if (logger.isActivated()) {
            		logger.info("200 OK response received");
            	}
	        } else {
	            // Error responses
            	if (logger.isActivated()) {
            		logger.info("Delivery report has failed: " + ctx.getStatusCode()
	                    + " response received");
            	}
	        }
        } catch(Exception e) {
        	if (logger.isActivated()) {
        		logger.error("Delivery report has failed", e);
        	}
        }
	}
}
