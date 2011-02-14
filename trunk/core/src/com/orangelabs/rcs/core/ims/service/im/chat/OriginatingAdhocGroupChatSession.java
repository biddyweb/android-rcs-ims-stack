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
package com.orangelabs.rcs.core.ims.service.im.chat;

import java.util.List;
import java.util.Vector;

import javax.sip.header.SubjectHeader;

import com.orangelabs.rcs.core.ims.network.sip.SipManager;
import com.orangelabs.rcs.core.ims.network.sip.SipMessageFactory;
import com.orangelabs.rcs.core.ims.network.sip.SipUtils;
import com.orangelabs.rcs.core.ims.protocol.msrp.MsrpSession;
import com.orangelabs.rcs.core.ims.protocol.sdp.MediaAttribute;
import com.orangelabs.rcs.core.ims.protocol.sdp.MediaDescription;
import com.orangelabs.rcs.core.ims.protocol.sdp.SdpParser;
import com.orangelabs.rcs.core.ims.protocol.sdp.SdpUtils;
import com.orangelabs.rcs.core.ims.protocol.sip.SipException;
import com.orangelabs.rcs.core.ims.protocol.sip.SipRequest;
import com.orangelabs.rcs.core.ims.protocol.sip.SipResponse;
import com.orangelabs.rcs.core.ims.protocol.sip.SipTransactionContext;
import com.orangelabs.rcs.core.ims.service.ImsService;
import com.orangelabs.rcs.core.ims.service.ImsServiceSession;
import com.orangelabs.rcs.core.ims.service.im.InstantMessageError;
import com.orangelabs.rcs.core.ims.service.im.InstantMessageSession;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Originating ad-hoc group chat session
 * 
 * @author jexa7410
 */
public class OriginatingAdhocGroupChatSession extends InstantMessageSession {
	/**
	 * List of contacts
	 */
	private List<String> contacts;
	
    /**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());

    /**
	 * Constructor
	 * 
	 * @param parent IMS service
	 * @param contact Conference id
	 * @param subject Subject of the conference
	 * @param contacts List of contacts
	 */
	public OriginatingAdhocGroupChatSession(ImsService parent, String contact, String subject, List<String> contacts) {
		super(parent, contact, subject);

		// Create dialog path
		createOriginatingDialogPath();

		// Set the list of contacts
		this.contacts = contacts;
	}
	
	/**
	 * Background processing
	 */
	public void run() {
		try {
	    	if (logger.isActivated()) {
	    		logger.info("Initiate a new ad-hoc group chat session as originating");
	    	}

			// Build SDP part
	    	String ntpTime = SipUtils.constructNTPtime(System.currentTimeMillis());
	    	String sdp =
	    		"--boundary1" + SipUtils.CRLF +
	    		"Content-Type: application/sdp" + SipUtils.CRLF +
	    		"" + SipUtils.CRLF +
	    		"v=0" + SipUtils.CRLF +
	            "o=- " + ntpTime + " " + ntpTime + " IN IP4 " + getDialogPath().getSipStack().getLocalIpAddress() + SipUtils.CRLF +
	            "s=-" + SipUtils.CRLF +
				"c=IN IP4 " + getDialogPath().getSipStack().getLocalIpAddress() + SipUtils.CRLF +
	            "t=0 0" + SipUtils.CRLF +			
	            "m=message " + getMsrpMgr().getLocalMsrpPort() + " TCP/MSRP *" + SipUtils.CRLF +
	            "a=path:" + getMsrpMgr().getLocalMsrpPath() + SipUtils.CRLF +
	            "a=connection:new" + SipUtils.CRLF +
	            "a=setup:active" + SipUtils.CRLF +
	    		"a=accept-types:message/cpim" + SipUtils.CRLF +
	    		"a=sendrecv" + SipUtils.CRLF + SipUtils.CRLF;

	    	String uriList = "";
	    	for(int i=0; i < contacts.size(); i++) {
	    		String contact = contacts.get(i);
	    		uriList += " <entry uri=\"tel:" + contact + "\"/>" + SipUtils.CRLF;
	    	}
	    	String xml =
	    		"--boundary1" + SipUtils.CRLF +
	    		"Content-Type: application/resource-lists+xml" + SipUtils.CRLF +
	    		"Content-Disposition: recipient-list" + SipUtils.CRLF +
	    		"" + SipUtils.CRLF +
	    		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + SipUtils.CRLF +
	    		"<resource-lists xmlns=\"urn:ietf:params:xml:ns:resource-lists\" xmlns:cp=\"urn:ietf:params:xml:ns:copyControl\">" +
	    		"<list>" + SipUtils.CRLF +
	    		uriList +
	    		"</list></resource-lists>" + SipUtils.CRLF +
	    		"--boundary1--";
	    	
	    	String multipart = sdp + xml;

			// Set the local SDP part in the dialog path
	    	getDialogPath().setLocalSdp(multipart);

	        // Send an INVITE
	        if (logger.isActivated()) {
	        	logger.info("Send INVITE");
	        }
	        SipRequest invite = SipMessageFactory.createMultipartInvite(getDialogPath(), -1, multipart, "boundary1");
	        
	        // Add a subject header
	        String subject = getSubject();
	        if (subject != null) {
	        	invite.addHeader(SubjectHeader.NAME, subject);
	        }

	        // Set feature tags
	        String[] tags = {SipUtils.FEATURE_OMA_IM};
	        SipUtils.setFeatureTags(invite, tags);
	        
	        // Set initial request in the dialog path
	        getDialogPath().setInvite(invite);
	        
	        // Send the INVITE request
	        sendInvite(invite);	        
		} catch(Exception e) {
        	if (logger.isActivated()) {
        		logger.error("Session initiation has failed", e);
        	}

        	// Unexpected error
			handleError(new InstantMessageError(InstantMessageError.UNEXPECTED_EXCEPTION,
					e.getMessage()));
		}		
	}
	
	/**
	 * Send INVITE message
	 * 
	 * @param invite SIP INVITE
	 * @throws SipException
	 */
	private void sendInvite(SipRequest invite) throws SipException {
		// Send INVITE request
		SipTransactionContext ctx = getImsService().getImsModule().getSipManager().sendSipMessageAndWait(invite);
		
        // Wait response
        ctx.waitResponse(ImsServiceSession.RINGING_PERIOD + SipManager.TIMEOUT);

		// Analyze the received response 
        if (ctx.isSipResponse()) {
        	// A response has been received
            if (ctx.getStatusCode() == 200) {
            	// 200 OK
            	handle200OK(ctx.getSipResponse());
            } else
            if (ctx.getStatusCode() == 407) {
            	// 407 Proxy Authentication Required
            	handle407Authentication(ctx.getSipResponse());
            } else {           	
            	// Error response
                if (ctx.getStatusCode() == 603) {
                	handleError(new InstantMessageError(InstantMessageError.SESSION_INITIATION_DECLINED,
        					ctx.getReasonPhrase()));
                } else
                if (ctx.getStatusCode() == 487) {
                	handleError(new InstantMessageError(InstantMessageError.SESSION_INITIATION_CANCELLED,
        					ctx.getReasonPhrase()));
                } else {
	    			handleError(new InstantMessageError(InstantMessageError.SESSION_INITIATION_FAILED,
	    					ctx.getStatusCode() + " " + ctx.getReasonPhrase()));
                }
            }
        } else {
    		if (logger.isActivated()) {
        		logger.debug("No response received for INVITE");
        	}

    		// No response received: timeout
        	handleError(new InstantMessageError(InstantMessageError.SESSION_INITIATION_FAILED));
        }
	}	

	/**
	 * Handle 200 0K response 
	 * 
	 * @param resp 200 OK response
	 */
	public void handle200OK(SipResponse resp) {
		try {
	        // 200 OK received
			if (logger.isActivated()) {
				logger.info("200 OK response received");
			}

	        // The signalisation is established
	        getDialogPath().sigEstablished();

	        // Set the remote tag
	        getDialogPath().setRemoteTag(resp.getToTag());
        
	        // Set the target
	        getDialogPath().setTarget(resp.getContactURI());
	
	        // Set the route path with the Record-Route header
	        Vector<String> newRoute = SipUtils.routeProcessing(resp, true);
			getDialogPath().setRoute(newRoute);
			
	        // Set the remote SDP part
	        getDialogPath().setRemoteSdp(resp.getContent());
	        
	        // Parse the remote SDP part
        	SdpParser parser = new SdpParser(getDialogPath().getRemoteSdp().getBytes());
        	Vector<MediaDescription> media = parser.getMediaDescriptions();
    		MediaDescription desc = media.elementAt(0);
    		MediaAttribute attr = desc.getMediaAttribute("path");
    		String remoteMsrpPath = attr.getValue();
    		String remoteHost = SdpUtils.extractRemoteHost(parser.sessionDescription.connectionInfo);
    		int remotePort = desc.port;

	        // Send ACK message
	        if (logger.isActivated()) {
	        	logger.info("Send ACK");
	        }
	        getImsService().getImsModule().getSipManager().sendSipAck(getDialogPath());
	        
        	// The session is established
	        getDialogPath().sessionEstablished();

	        // Test if the session should be interrupted
			if (isInterrupted()) {
				if (logger.isActivated()) {
					logger.debug("Session has been interrupted: end of processing");
				}
				return;
			}
	        
        	// Create the MSRP session
			MsrpSession session = getMsrpMgr().createMsrpClientSession(remoteHost, remotePort, remoteMsrpPath, this);
			session.setFailureReportOption(false);
			session.setSuccessReportOption(false);
			
			// Open the MSRP session
			getMsrpMgr().openMsrpSession();
			
			// Notify listener
	        if (getListener() != null) {
	        	getListener().handleSessionStarted();
	        }

	        // Send an empty packet
        	sendEmptyPacket();
		} catch(Exception e) {
        	if (logger.isActivated()) {
        		logger.error("Session initiation has failed", e);
        	}

        	// Unexpected error
			handleError(new InstantMessageError(InstantMessageError.UNEXPECTED_EXCEPTION,
					e.getMessage()));
        }
	}
	
	/**
	 * Handle 407 Proxy Authentication Required 
	 * 
	 * @param resp 407 response
	 */
	public void handle407Authentication(SipResponse resp) {
		try {
	        if (logger.isActivated()) {
	        	logger.info("407 response received");
	        }
	
	        // Set the remote tag
	        getDialogPath().setRemoteTag(resp.getToTag());
	
	        // Update the authentication agent
	    	getAuthenticationAgent().readProxyAuthenticateHeader(resp);            
	
	        // Increment the Cseq number of the dialog path
	        getDialogPath().incrementCseq();
	
	        // Send a second INVITE with the right token
	        if (logger.isActivated()) {
	        	logger.info("Send second INVITE");
	        }
	        SipRequest invite = SipMessageFactory.createMultipartInvite(
	        		getDialogPath(),
					-1,
					getDialogPath().getLocalSdp(),
					"boundary1");
	               
	        // Add a subject header
	        String subject = getSubject();
	        if (subject != null) {
	        	invite.addHeader(SubjectHeader.NAME, subject);
	        }

	        // Set feature tags
	        String[] tags = {SipUtils.FEATURE_OMA_IM};
	        SipUtils.setFeatureTags(invite, tags);

	        // Reset initial request in the dialog path
	        getDialogPath().setInvite(invite);
	        
	        // Set the Proxy-Authorization header
	        getAuthenticationAgent().setProxyAuthorizationHeader(invite);
	
	        // Send the second INVITE request
	        sendInvite(invite);
	        
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
