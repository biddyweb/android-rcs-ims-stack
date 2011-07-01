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
	package com.orangelabs.rcs.core.ims.service.im.filetransfer;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Vector;

import com.orangelabs.rcs.core.content.MmContent;
import com.orangelabs.rcs.core.ims.network.sip.SipManager;
import com.orangelabs.rcs.core.ims.network.sip.SipMessageFactory;
import com.orangelabs.rcs.core.ims.network.sip.SipUtils;
import com.orangelabs.rcs.core.ims.protocol.msrp.MsrpEventListener;
import com.orangelabs.rcs.core.ims.protocol.msrp.MsrpManager;
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
import com.orangelabs.rcs.core.ims.service.im.InstantMessagingService;
import com.orangelabs.rcs.core.ims.service.sharing.ContentSharingError;
import com.orangelabs.rcs.core.ims.service.sharing.transfer.ContentSharingTransferSession;
import com.orangelabs.rcs.platform.file.FileFactory;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Originating file transfer session
 * 
 * @author jexa7410
 */
public class OriginatingFileTransferSession extends ContentSharingTransferSession implements MsrpEventListener {
	/**
	 * MSRP manager
	 */
	private MsrpManager msrpMgr = null;

    /**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());

	/**
	 * Constructor
	 * 
	 * @param parent IMS service
	 * @param content Content to be shared
	 * @param contact Remote contact
	 */
	public OriginatingFileTransferSession(ImsService parent, MmContent content, String contact) {
		super(parent, content, contact);
		
		// Create dialog path
		createOriginatingDialogPath();
	}

	/**
	 * Background processing
	 */
	public void run() {
		try {
	    	if (logger.isActivated()) {
	    		logger.info("Initiate a new sharing session as originating");
	    	}
	    	
    		// Set setup mode
	    	String localSetup = "active";
	    	
	    	// Set local port
	    	int localMsrpPort = 9; // See RFC4145, Page 4
	    	
			// Create the MSRP manager
			String localIpAddress = getImsService().getImsModule().getCurrentNetworkInterface().getNetworkAccess().getIpAddress();
			msrpMgr = new MsrpManager(localIpAddress, localMsrpPort);
	    	
			// Build SDP part
	    	String ntpTime = SipUtils.constructNTPtime(System.currentTimeMillis());
	    	String sdp =
	    		"v=0" + SipUtils.CRLF +
	            "o=- " + ntpTime + " " + ntpTime + " IN IP4 " + getDialogPath().getSipStack().getLocalIpAddress() + SipUtils.CRLF +
	            "s=-" + SipUtils.CRLF +
				"c=IN IP4 " + getDialogPath().getSipStack().getLocalIpAddress() + SipUtils.CRLF +
	            "t=0 0" + SipUtils.CRLF +			
	            "m=message " + localMsrpPort + " TCP/MSRP *" + SipUtils.CRLF +
	            "a=path:" + msrpMgr.getLocalMsrpPath() + SipUtils.CRLF +
	            "a=connection:new" + SipUtils.CRLF +
	            "a=setup:" + localSetup + SipUtils.CRLF +
	            "a=accept-types: " + getContent().getEncoding() + SipUtils.CRLF +
	            "a=max-size:" + ContentSharingTransferSession.MAX_CONTENT_SIZE + SipUtils.CRLF +
	    		"a=file-transfer-id:" + getFileTransferId() + SipUtils.CRLF +
	    		"a=file-disposition:attachment" + SipUtils.CRLF +
	    		"a=sendonly" + SipUtils.CRLF;
	    	
	    	// Set File-selector attribute
	    	String selector = getFileSelectorAttribute();
	    	if (selector != null) {
	    		sdp += "a=file-selector:" + selector + SipUtils.CRLF;
	    	}

	    	// Set File-location attribute
	    	String location = getFileLocationAttribute();
	    	if (location != null) {
	    		sdp += "a=file-location:" + location + SipUtils.CRLF;
	    	}
	   
			// Set the local SDP part in the dialog path
	    	getDialogPath().setLocalContent(sdp);

	        // Create an INVITE request
	        if (logger.isActivated()) {
	        	logger.info("Send INVITE");
	        }
	        SipRequest invite = SipMessageFactory.createInvite(getDialogPath(),
	        		InstantMessagingService.FT_FEATURE_TAGS, sdp);
	        
	        // Set initial request in the dialog path
	        getDialogPath().setInvite(invite);
	        
	        // Send INVITE request
	        sendInvite(invite);	        
		} catch(Exception e) {
        	if (logger.isActivated()) {
        		logger.error("Session initiation has failed", e);
        	}

        	// Unexpected error
			handleError(new ContentSharingError(ContentSharingError.UNEXPECTED_EXCEPTION,
					e.getMessage()));
		}
		
		if (logger.isActivated()) {
    		logger.debug("End of thread");
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
                	handleError(new ContentSharingError(ContentSharingError.SESSION_INITIATION_DECLINED,
        					ctx.getReasonPhrase()));
                } else
                if (ctx.getStatusCode() == 487) {
                	handleError(new ContentSharingError(ContentSharingError.SESSION_INITIATION_CANCELLED,
        					ctx.getReasonPhrase()));
                } else {
                	handleError(new ContentSharingError(ContentSharingError.SESSION_INITIATION_FAILED,
                			ctx.getStatusCode() + " " + ctx.getReasonPhrase()));
                }
            }
        } else {
    		if (logger.isActivated()) {
        		logger.debug("No response received for INVITE");
        	}
    		
    		// No response received: timeout
        	handleError(new ContentSharingError(ContentSharingError.SESSION_INITIATION_FAILED));
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
	        getDialogPath().setRemoteContent(resp.getContent());
	        
	        // Parse the remote SDP part
        	SdpParser parser = new SdpParser(getDialogPath().getRemoteContent().getBytes());
        	Vector<MediaDescription> media = parser.getMediaDescriptions();
    		MediaDescription mediaDesc = media.elementAt(0);
    		MediaAttribute attr = mediaDesc.getMediaAttribute("path");
    		String remoteMsrpPath = attr.getValue();
    		String remoteHost = SdpUtils.extractRemoteHost(parser.sessionDescription.connectionInfo);
    		int remotePort = mediaDesc.port;

	        // Send ACK request
	        if (logger.isActivated()) {
	        	logger.info("Send ACK");
	        }
	        getImsService().getImsModule().getSipManager().sendSipAck(getDialogPath());
	   		
        	// The session is established
	        getDialogPath().sessionEstablished();

        	// Create the MSRP client session
	        MsrpSession session = msrpMgr.createMsrpClientSession(remoteHost, remotePort, remoteMsrpPath, this);
			session.setFailureReportOption(false);
			session.setSuccessReportOption(true);
			
			// Open the MSRP session
			msrpMgr.openMsrpSession();
	        

        	// Start session timer
        	if (getSessionTimerManager().isSessionTimerActivated(resp)) {
        		getSessionTimerManager().start(resp.getSessionTimerRefresher(), resp.getSessionTimerExpire());
        	}
			
	        // Notify listener
	        if (getListener() != null) {
	        	getListener().handleSessionStarted();
	        }

			// Start sending data chunks
			byte[] data = getContent().getData();
			InputStream stream; 
			if (data == null) {
				// Load data from URL
				stream = FileFactory.getFactory().openFileInputStream(getContent().getUrl());
			} else {
				// Load data from memory
				stream = new ByteArrayInputStream(data);
			}
			msrpMgr.sendChunks(stream, getContent().getEncoding(), getContent().getSize());
			
		} catch(Exception e) {
        	if (logger.isActivated()) {
        		logger.error("Session initiation has failed", e);
        	}

        	// Unexpected error
			handleError(new ContentSharingError(ContentSharingError.UNEXPECTED_EXCEPTION,
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
	
	        // Create a second INVITE request with the right token
	        if (logger.isActivated()) {
	        	logger.info("Send second INVITE");
	        }
	        SipRequest invite = SipMessageFactory.createInvite(
	        		getDialogPath(),
	        		InstantMessagingService.FT_FEATURE_TAGS,
					getDialogPath().getLocalContent());
	               
	        // Reset initial request in the dialog path
	        getDialogPath().setInvite(invite);
	        
	        // Set the Proxy-Authorization header
	        getAuthenticationAgent().setProxyAuthorizationHeader(invite);
	
	        // Send INVITE request
	        sendInvite(invite);
	        
        } catch(Exception e) {
        	if (logger.isActivated()) {
        		logger.error("Session initiation has failed", e);
        	}

        	// Unexpected error
			handleError(new ContentSharingError(ContentSharingError.UNEXPECTED_EXCEPTION,
					e.getMessage()));
        }
	}

	/**
	 * Handle error 
	 * 
	 * @param error Error
	 */
	public void handleError(ContentSharingError error) {
		if (isInterrupted()) {
			return;
		}
		
		// Error	
    	if (logger.isActivated()) {
    		logger.info("Session error: " + error.getErrorCode() + ", reason=" + error.getMessage());
    	}

		// Close MSRP session
    	closeMsrpSession();
    	
    	// Remove the current session
    	getImsService().removeSession(this);

		// Notify listener
        if (getListener() != null) {
        	getListener().handleSharingError(error);
        }
	}

	/**
	 * Data has been transfered
	 */
	public void msrpDataTransfered() {
    	if (logger.isActivated()) {
    		logger.info("Data transfered");
    	}

    	// Close the MSRP session
    	closeMsrpSession();
		
		// Terminate session
		terminateSession();
	   	
    	// Remove the current session
    	getImsService().removeSession(this);

    	// Notify listener
        if (getListener() != null) {
        	getListener().handleContentTransfered(getContent().getUrl());
        }
	}
	
	/**
	 * Data has been received
	 * 
	 * @param data Received data
	 * @param mimeType Data mime-type
	 */
	public void msrpDataReceived(byte[] data, String mimeType) {
		// Not used
	}
    
	/**
	 * MSRP transfer indicator event
	 * 
	 * @param currentSize Current transfered size in bytes
	 * @param totalSize Total size in bytes
	 */
	public void msrpTransferProgress(long currentSize, long totalSize) {
		// Notify listener
        if (getListener() != null) {
        	getListener().handleSharingProgress(currentSize, totalSize);
        }
	}	

	/**
	 * MSRP transfer aborted
	 */
	public void msrpTransferAborted() {
    	if (logger.isActivated()) {
    		logger.info("Data transfer aborted");
    	}
	}	

	/**
	 * MSRP transfer error
	 * 
	 * @param error Error
	 */
	public void msrpTransferError(String error) {
		if (isInterrupted()) {
			return;
		}

		if (logger.isActivated()) {
    		logger.info("Data transfer error: " + error);
    	}
    	
		// Close MSRP session
		closeMsrpSession();
			
		// Terminate session
		terminateSession();
	   	
    	// Remove the current session
    	getImsService().removeSession(this);

    	if (!isInterrupted()) {
	    	// Notify listener
	        if (getListener() != null) {
	        	getListener().handleSharingError(new ContentSharingError(ContentSharingError.MEDIA_TRANSFER_FAILED, error));
	        }
    	}
	}

	/**
	 * Close the MSRP session
	 */
	private void closeMsrpSession() {
    	if (msrpMgr != null) {
    		msrpMgr.closeSession();
    	}
		if (logger.isActivated()) {
			logger.debug("MSRP session has been closed");
		}
	}	

	/**
	 * Close media session
	 */
	public void closeMediaSession() {
		// Close MSRP session
		closeMsrpSession();
	}
}
