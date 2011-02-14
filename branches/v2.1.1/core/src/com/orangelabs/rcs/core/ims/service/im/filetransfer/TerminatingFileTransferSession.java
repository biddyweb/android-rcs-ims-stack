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

import java.io.IOException;
import java.util.Vector;

import com.orangelabs.rcs.core.content.ContentManager;
import com.orangelabs.rcs.core.ims.ImsModule;
import com.orangelabs.rcs.core.ims.network.sip.SipManager;
import com.orangelabs.rcs.core.ims.network.sip.SipMessageFactory;
import com.orangelabs.rcs.core.ims.network.sip.SipUtils;
import com.orangelabs.rcs.core.ims.protocol.msrp.MsrpEventListener;
import com.orangelabs.rcs.core.ims.protocol.msrp.MsrpManager;
import com.orangelabs.rcs.core.ims.protocol.sdp.MediaAttribute;
import com.orangelabs.rcs.core.ims.protocol.sdp.MediaDescription;
import com.orangelabs.rcs.core.ims.protocol.sdp.SdpParser;
import com.orangelabs.rcs.core.ims.protocol.sdp.SdpUtils;
import com.orangelabs.rcs.core.ims.protocol.sip.SipRequest;
import com.orangelabs.rcs.core.ims.protocol.sip.SipResponse;
import com.orangelabs.rcs.core.ims.protocol.sip.SipTransactionContext;
import com.orangelabs.rcs.core.ims.service.ImsService;
import com.orangelabs.rcs.core.ims.service.ImsServiceSession;
import com.orangelabs.rcs.core.ims.service.sharing.ContentSharingError;
import com.orangelabs.rcs.core.ims.service.sharing.transfer.ContentSharingTransferSession;
import com.orangelabs.rcs.utils.NetworkRessourceManager;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Terminating file transfer session
 * 
 * @author jexa7410
 */
public class TerminatingFileTransferSession extends ContentSharingTransferSession implements MsrpEventListener {
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
	 * @param invite Initial INVITE request
	 */
	public TerminatingFileTransferSession(ImsService parent, SipRequest invite) {
		super(parent, ContentManager.createMmContentFromSdp(invite.getContent()), invite.getFrom());

		// Create dialog path
		createTerminatingDialogPath(invite);
	}
	
	/**
	 * Background processing
	 */
	public void run() {
		try {
	    	if (logger.isActivated()) {
	    		logger.info("Initiate a new sharing session as terminating");
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

	        // Create the MSRP manager
			int localMsrpPort = NetworkRessourceManager.generateLocalMsrpPort();
			String localIpAddress = getImsService().getImsModule().getCurrentNetworkInterface().getNetworkAccess().getIpAddress();
			msrpMgr = new MsrpManager(localIpAddress, localMsrpPort);

			// Parse the remote SDP part
        	SdpParser parser = new SdpParser(getDialogPath().getRemoteSdp().getBytes());
    		Vector<MediaDescription> media = parser.getMediaDescriptions();
			MediaDescription desc = media.elementAt(0);
			MediaAttribute attr1 = desc.getMediaAttribute("file-selector");
            String fileSelector = attr1.getName() + ":" + attr1.getValue();
			MediaAttribute attr2 = desc.getMediaAttribute("file-transfer-id");
            String fileTransferId = attr2.getName() + ":" + attr2.getValue();
			MediaAttribute attr3 = desc.getMediaAttribute("path");
            String remotePath = attr3.getValue();
    		String remoteHost = SdpUtils.extractRemoteHost(parser.sessionDescription.connectionInfo);
    		int remotePort = desc.port;
			
            // Extract the "setup" parameter
            String remoteSetup = "passive";
			MediaAttribute attr4 = desc.getMediaAttribute("setup");
			if (attr4 != null) {
				remoteSetup = attr4.getValue();
			}
            if (logger.isActivated()){
				logger.debug("Remote setup attribute is " + remoteSetup);
			}
            
    		// Set setup mode
            String localSetup = "passive";
            if (remoteSetup.equals("active")) {
            	// Passive mode: the terminal should wait a media connection
    			localSetup = "passive";
            } else 
            if (remoteSetup.equals("passive")) {
            	// Active mode: the terminal should initiate a media connection
    			localSetup = "active";
            } else {
            	// The terminal is active by default
    			localSetup = "active";
            }
            if (logger.isActivated()){
				logger.debug("Local setup attribute is " + localSetup);
			}

	        // Create a content object
    		if (getContent() == null) {
    			if (logger.isActivated()){
    				logger.debug("MIME type is not supported");
    			}

    			// Send a 415 Unsupported media type response
				send415Error(getDialogPath().getInvite());

				// Unsupported media type
				handleError(new ContentSharingError(ContentSharingError.UNSUPPORTED_MEDIA_TYPE));
        		return;
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
	            "m=message " + msrpMgr.getLocalMsrpPort() + " TCP/MSRP *" + SipUtils.CRLF +
	            "a=" + fileSelector + SipUtils.CRLF +
	    		"a=" + fileTransferId + SipUtils.CRLF +
	            "a=max-size:" + ContentSharingTransferSession.MAX_CONTENT_SIZE + SipUtils.CRLF +
	            "a=accept-types:" + getContent().getEncoding() + SipUtils.CRLF +
	            "a=connection:new" + SipUtils.CRLF +
	            "a=setup:" + localSetup + SipUtils.CRLF +
	            "a=path:" + msrpMgr.getLocalMsrpPath() + SipUtils.CRLF +
	    		"a=recvonly" + SipUtils.CRLF;

	    	// Set the local SDP part in the dialog path
	        getDialogPath().setLocalSdp(sdp);

    		// Create the MSRP server session
            if (localSetup.equals("passive")) {
            	// Passive mode: client wait a connection
            	msrpMgr.createMsrpServerSession(remotePath, this);
            	
    			// Open the connection
    			Thread thread = new Thread(){
    				public void run(){
    					try {
							// Open the MSRP session
    						msrpMgr.openMsrpSession(ContentSharingTransferSession.DEFAULT_SO_TIMEOUT);
						} catch (IOException e) {
							if (logger.isActivated()) {
				        		logger.error("Can't create the MSRP server session", e);
				        	}
						}		
    				}
    			};
    			thread.start();
            }

            // Send a 200 OK response
        	if (logger.isActivated()) {
        		logger.info("Send 200 OK");
        	}
            SipResponse resp = SipMessageFactory.create200OkInviteResponse(getDialogPath(), sdp);
            
	        // Set feature tags
	        String[] tags = {SipUtils.FEATURE_OMA_IM};
	        SipUtils.setFeatureTags(resp, tags);
            
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

        		// Create the MSRP client session
                if (localSetup.equals("active")) {
                	// Active mode: client should connect
                	msrpMgr.createMsrpClientSession(remoteHost, remotePort, remotePath, this);

					// Open the MSRP session
					msrpMgr.openMsrpSession(ContentSharingTransferSession.DEFAULT_SO_TIMEOUT);
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
            	handleError(new ContentSharingError(ContentSharingError.SESSION_INITIATION_FAILED));
            }
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

    	// Close the MSRP session
    	closeMsrpSession();

    	// Remove the current session
    	getImsService().removeSession(this);

		// Notify listener
        if (getListener() != null) {
        	getListener().handleSharingError(error);
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

    	// Interrupt session
		interruptSession();

		// Close the MSRP session
    	closeMsrpSession();

    	// Close SIP session
    	try {
	    	// Update the dialog path status
			getDialogPath().sessionTerminated();
			
	    	// Send a 200 OK response
			if (logger.isActivated()) {
				logger.info("Send 200 OK");
			}
	        SipResponse resp = SipMessageFactory.createResponse(bye, 200);
	        getImsService().getImsModule().getSipManager().sendSipMessage(resp);
		} catch(Exception e) {
	       	if (logger.isActivated()) {
        		logger.error("Session termination has failed", e);
        	}
		}
		
    	// Remove the current session
    	getImsService().removeSession(this);

    	// Notify listener only if the data transfer is not terminated
    	if (getContent().getData() == null) {
	        if (getListener() != null) {
	        	getListener().handleSessionTerminatedByRemote();
	        }
    	}
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

		// Interrupt session
		interruptSession();
		
		// Close MSRP session
		closeMsrpSession();

		// Close SIP session
    	try {
	    	// Update dialog path
			getDialogPath().sessionCancelled();

			// Send a 200 OK
	    	if (logger.isActivated()) {
	    		logger.info("Send 200 OK");
	    	}
	        SipResponse cancelResp = SipMessageFactory.createResponse(cancel, 200);
	        getImsService().getImsModule().getSipManager().sendSipMessage(cancelResp);
	        
			// Send a 487 Request terminated
	    	if (logger.isActivated()) {
	    		logger.info("Send 487 Request terminated");
	    	}
	        SipResponse terminatedResp = SipMessageFactory.createResponse(getDialogPath().getInvite(), 487);
	        getImsService().getImsModule().getSipManager().sendSipMessage(terminatedResp);
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
	}

	/**
	 * Data has been transfered
	 */
	public void msrpDataTransfered() {
		// Not used
	}
	
	/**
	 * Data has been received
	 * 
	 * @param data Received data
	 * @param mimeType Data mime-type
	 */
	public void msrpDataReceived(byte[] data, String mimeType) {
    	if (logger.isActivated()) {
    		logger.info("Data received");
    	}
	
    	try {
					
	    	// Update the content with the received data 
	    	getContent().setData(data);

	    	// Save data into a filename
	    	ContentManager.saveContent(getContent());
	    	
	    	// Notify listener
	        if (getListener() != null) {
	        	getListener().handleContentTransfered(getContent().getUrl());
	        }
	   	} catch(Exception e) {
	   		// 	Notify listener
	   		if (getListener() != null) {
	        	getListener().handleSharingError(new ContentSharingError(ContentSharingError.MEDIA_SAVING_FAILED,e.getMessage()));
	        }
	   		if (logger.isActivated()) {
	   			logger.error("Can't close correctly the CSh session", e);
	   		}
	   	}
	}
    
	/**
	 * MSRP transfer indicator event
	 * 
	 * @param currentSize Current transfered size in bytes
	 * @param totalSize Total size in bytes
	 */
	public void msrpTransferProgress(long currentSize, long totalSize) {	
		if (!isInterrupted()) {
			// Notify listener
	        if (getListener() != null) {
	        	getListener().handleSharingProgress(currentSize, totalSize);
	        }
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
    	if (logger.isActivated()) {
    		logger.info("Data transfer error: " + error);
    	}

    	try {
			// Close the MSRP session
			closeMsrpSession();
				
			// Terminate session
			terminateSession();
	   	} catch(Exception e) {
	   		if (logger.isActivated()) {
	   			logger.error("Can't close correctly the CSh session", e);
	   		}
	   	}

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
			if (logger.isActivated()) {
				logger.debug("MSRP session has been closed");
			}
    	}
	}
	
	/**
	 * Abort the session
	 */
	public void abortSession(){
	   	if (logger.isActivated()) {
    		logger.info("Abort the session");
    	}
	   	
	   	try {
			// Interrupt the session
			interruptSession();
	
			// Close the MSRP session
			closeMsrpSession();
			
			// Terminate session
			terminateSession();
	   	} catch(Exception e) {
	   		if (logger.isActivated()) {
	   			logger.error("Can't close correctly the CSh session", e);
	   		}
	   	}

    	// Remove the current session
    	getImsService().removeSession(this);

    	// Notify listener
        if (getListener() != null) {
        	getListener().handleSessionAborted();
        }
	}
}
