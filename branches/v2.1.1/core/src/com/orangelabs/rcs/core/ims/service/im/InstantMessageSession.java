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
package com.orangelabs.rcs.core.ims.service.im;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;

import android.os.RemoteException;

import com.orangelabs.rcs.core.content.ContentManager;
import com.orangelabs.rcs.core.content.FileContent;
import com.orangelabs.rcs.core.content.PhotoContent;
import com.orangelabs.rcs.core.content.VideoContent;
import com.orangelabs.rcs.core.ims.network.sip.SipMessageFactory;
import com.orangelabs.rcs.core.ims.protocol.msrp.MsrpEventListener;
import com.orangelabs.rcs.core.ims.protocol.msrp.MsrpManager;
import com.orangelabs.rcs.core.ims.protocol.sip.SipRequest;
import com.orangelabs.rcs.core.ims.protocol.sip.SipResponse;
import com.orangelabs.rcs.core.ims.service.ImsService;
import com.orangelabs.rcs.core.ims.service.ImsServiceSession;
import com.orangelabs.rcs.core.ims.service.im.InstantMessage;
import com.orangelabs.rcs.core.ims.service.im.InstantMessageError;
import com.orangelabs.rcs.platform.file.FileFactory;
import com.orangelabs.rcs.utils.MimeManager;
import com.orangelabs.rcs.utils.NetworkRessourceManager;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * IM session
 * 
 * @author jexa7410
 */
public abstract class InstantMessageSession extends ImsServiceSession implements MsrpEventListener {
	/**
	 * Subject of the conference
	 */
	private String subject = null;

	/**
	 * MSRP manager
	 */
	private MsrpManager msrpMgr = null;

	/**
	 * Is composing manager
	 */
	private IsComposingManager isComposingMgr = new IsComposingManager(this);
	
    /**
	 * IM event listener
	 */
	private InstantMessageSessionListener listener = null;

    /**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());

    /**
	 * Constructor
	 * 
	 * @param parent IMS service
	 * @param contact Remote contact
	 * @param subject Subject of the conference
	 */
	public InstantMessageSession(ImsService parent, String contact, String subject) {
		super(parent, contact);

		this.subject = subject;
		
		// Create the MSRP manager
		int localMsrpPort = NetworkRessourceManager.generateLocalMsrpPort();
		String localIpAddress = getImsService().getImsModule().getCurrentNetworkInterface().getNetworkAccess().getIpAddress();
		msrpMgr = new MsrpManager(localIpAddress, localMsrpPort);
	}
	
	/**
	 * Add a listener for receiving events
	 * 
	 * @param listener Listener
	 */
	public void addListener(InstantMessageSessionListener listener) {
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
	public InstantMessageSessionListener getListener() {
		return listener;
	}

	/**
	 * Return the subject of the conference
	 * 
	 * @return Subject
	 */
	public String getSubject() {
		return subject;
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
	 * Returns the MSRP manager
	 * 
	 * @return MSRP manager
	 */
	public MsrpManager getMsrpMgr() {
		return msrpMgr;
	}
	
	/**
	 * Close the MSRP session
	 */
	public void closeMsrpSession() {
    	if (getMsrpMgr() != null) {
    		getMsrpMgr().closeSession();
			if (logger.isActivated()) {
				logger.debug("MSRP session has been closed");
			}
    	}
	}
	
	/**
	 * Handle error 
	 * 
	 * @param error Error
	 */
	public void handleError(InstantMessageError error) {
        // Error	
    	if (logger.isActivated()) {
    		logger.info("Session error: " + error.getErrorCode() + ", reason=" + error.getMessage());
    	}

		// Close MSRP session
    	closeMsrpSession();

    	// Remove the current session
    	getImsService().removeSession(this);

		if (!isInterrupted()) {
			// Notify listener
			listener.handleImError(error);
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
	   			logger.error("Can't close correctly the IM session", e);
	   		}
	   	}

    	// Remove the current session
    	getImsService().removeSession(this);

    	// Notify listener
    	listener.handleSessionAborted();	
	}

	/**
	 * Receive BYE request 
	 * 
	 * @param bye BYE request
	 */
	public void receiveBye(SipRequest bye) {
		try {
	    	if (logger.isActivated()) {
	    		logger.info("Receive a BYE message from the remote");
	    	}
	
	    	// Update the dialog path status
			getDialogPath().sessionTerminated();
			
			// Close the MSRP session
			closeMsrpSession();
	        
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

    	// Notify listener
    	listener.handleSessionTerminatedByRemote();
    }
	
	/**
	 * Receive CANCEL request 
	 * 
	 * @param cancel CANCEL request
	 */
	public void receiveCancel(SipRequest cancel) {
		try {
	    	if (logger.isActivated()) {
	    		logger.info("Receive a CANCEL message from the remote");
	    	}

			if (getDialogPath().isSigEstablished()) {
		    	if (logger.isActivated()) {
		    		logger.info("Ignore the received CANCEL message from the remote (session already established)");
		    	}
				return;
			}
			
			// Update dialog path
			getDialogPath().sessionCancelled();

			// Send a 200 OK
	    	if (logger.isActivated()) {
	    		logger.info("Send 200 OK");
	    	}
	        SipResponse cancelResp = SipMessageFactory.createResponse(cancel, 200);
	        getImsService().getImsModule().getSipManager().sendSipMessage(cancelResp);
		} catch(Exception e) {
	    	if (logger.isActivated()) {
	    		logger.error("Session has been cancelled", e);
	    	}
		}
		
    	// Remove the current session
    	getImsService().removeSession(this);

    	// Notify listener
    	listener.handleSessionTerminatedByRemote();			
    }

	/**
	 * MSRP transfer indicator event
	 * 
	 * @param currentSize Current transfered size in bytes
	 * @param totalSize Total size in bytes
	 */
	public void msrpTransferProgress(long currentSize, long totalSize){
		// Do nothing
	}
	
	/**
	 * Data has been transfered
	 */
	public void msrpDataTransfered() {
    	if (logger.isActivated()) {
    		logger.info("Data transfered");
    	}

	    // Notify listener
    	listener.handleMessageTransfered();			
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
	
    	if ((data == null) || (data.length == 0)) {
    		// By-pass empty data
        	if (logger.isActivated()) {
        		logger.debug("By-pass received empty data");
        	}
    		return;
    	}

    	try {
		    // Notify listener
	    	// TODO : how to get remote id in case of 1-n session ?
    		Date date = Calendar.getInstance().getTime();
    		long random = (date.getTime()%1000);
    		String ext = MimeManager.getMimeExtension(mimeType);
    		if (mimeType.equalsIgnoreCase(IsComposingInfo.MIME_TYPE)){
			    // Is composing event
			    isComposingMgr.receiveIsComposingEvent(getRemoteContact(), data);
    		} else
    		if (MimeManager.isTextType(mimeType)){
		    	// Create message content 
		    	String txtMessage = new String(data);
		    	InstantMessage message = new InstantMessage(getRemoteContact(), txtMessage);
		    	
	    		// Is composing event is reset
			    isComposingMgr.receiveIsComposingEvent(getRemoteContact(), false);
			    
			    // Notify listener
	    		listener.handleReceiveMessage(message);			    
    		} else
	    	if (MimeManager.isPictureType(mimeType)){
	    		// Create image content
	    		String path = FileFactory.getFactory().getPhotoRootDirectory() + "photo" + random + "." + ext;
	    		PhotoContent content = new PhotoContent(path, mimeType, data.length);
	    		content.setData(data);
	    		
	    		// Save content on phone
	    		ContentManager.saveContent(content);
	    		
		    	// Notify listener
	    		listener.handleReceivePicture(getRemoteContact(), date, content);	    		
	    	} else
	    	if (MimeManager.isVideoType(mimeType)){
	    		// Create video content
	    		String path = FileFactory.getFactory().getVideoRootDirectory() + "video" + random + "." + ext;
	    		VideoContent content = new VideoContent(path, mimeType, data.length);
	    		content.setData(data);

	    		// Save content on phone
	    		ContentManager.saveContent(content);
	    		
		    	// Notify listener
	    		listener.handleReceiveVideo(getRemoteContact(), date, content);
	    	} else {
	    		// Create file content
	    		String path = FileFactory.getFactory().getFileRootDirectory() + "file" + random + "." + ext;
	    		FileContent content = new FileContent(path, data.length);
	    		content.setData(data);

	    		// Save content on phone
	    		ContentManager.saveContent(content);
	    		
		    	// Notify listener
	    		listener.handleReceiveFile(getRemoteContact(), date, content);
	    	}
    	} catch(Exception e) {
	   		if (logger.isActivated()) {
	   			logger.error("Problem while receiving data", e);
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
    	
    	// Notify listener
    	listener.handleImError(new InstantMessageError(InstantMessageError.MSG_TRANSFER_FAILED));
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
    	
    	// Notify listener
    	listener.handleImError(new InstantMessageError(InstantMessageError.MSG_TRANSFER_FAILED, error));
    }
	
	/**
	 * Send a plain text message
	 * 
	 * @param msg Message
	 */
	public void sendMessage(String msg) {
		try {
			ByteArrayInputStream stream = new ByteArrayInputStream(msg.getBytes()); 
			msrpMgr.sendChunks(stream, "text/plain", msg.getBytes().length);
			
		} catch(Exception e) {
	   		if (logger.isActivated()) {
	   			logger.error("Problem while sending data", e);
	   		}
	   		
	    	// Notify listener
	    	listener.handleImError(new InstantMessageError(InstantMessageError.MSG_TRANSFER_FAILED, e.getMessage()));
		}
	}
	
	/**
	 * Send an empty packet
	 */
	public void sendEmptyPacket() {
		try {
			msrpMgr.sendEmptyPacket();
		} catch(Exception e) {
	   		if (logger.isActivated()) {
	   			logger.error("Problem while sending empty packet", e);
	   		}
		}
	}

	/**
	 * Send a message with the specified mime type to the remote contact
	 * 
	 * @param msg Message
	 * @param contentType mime type of content
	 */
	public void sendMessage(String msg, String contentType) {
		try {
			ByteArrayInputStream stream = new ByteArrayInputStream(msg.getBytes()); 
			msrpMgr.sendChunks(stream, contentType, msg.getBytes().length);
			
		} catch(Exception e) {
	   		if (logger.isActivated()) {
	   			logger.error("Problem while sending data", e);
	   		}
	   		
	    	// Notify listener
	    	listener.handleImError(new InstantMessageError(InstantMessageError.MSG_TRANSFER_FAILED, e.getMessage()));
		}
	}
	
	/**
	 * Send a content to the remote contact
	 * 
	 * @param data Data stream to transfer
	 * @param mimeType Mime-type of the data
	 * @param length of the stream
	 */
	public void sendContent(InputStream dataStream, String mimeType, long dataLength){
		try {
			msrpMgr.sendChunks(dataStream, mimeType, dataLength);
		} catch(Exception e) {
	   		if (logger.isActivated()) {
	   			logger.error("Problem while sending data", e);
	   		}
	   		
	    	// Notify listener
	    	listener.handleImError(new InstantMessageError(InstantMessageError.MSG_TRANSFER_FAILED, e.getMessage()));
		}	
	}
	
	/**
	 * Set the is composing status
	 * 
	 * @param status Status
	 */
	public void setIsComposingStatus(boolean status) throws RemoteException {
		String msg = IsComposingInfo.buildIsComposingInfo(status);
		sendMessage(msg, IsComposingInfo.MIME_TYPE);
	}
}
