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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import org.xml.sax.InputSource;

import com.orangelabs.rcs.core.ims.ImsModule;
import com.orangelabs.rcs.core.ims.protocol.msrp.MsrpEventListener;
import com.orangelabs.rcs.core.ims.protocol.msrp.MsrpManager;
import com.orangelabs.rcs.core.ims.protocol.sip.SipRequest;
import com.orangelabs.rcs.core.ims.service.ImsService;
import com.orangelabs.rcs.core.ims.service.ImsServiceSession;
import com.orangelabs.rcs.core.ims.service.im.chat.cpim.CpimMessage;
import com.orangelabs.rcs.core.ims.service.im.chat.cpim.CpimParser;
import com.orangelabs.rcs.core.ims.service.im.chat.imdn.ImdnDocument;
import com.orangelabs.rcs.core.ims.service.im.chat.imdn.ImdnManager;
import com.orangelabs.rcs.core.ims.service.im.chat.imdn.ImdnParser;
import com.orangelabs.rcs.core.ims.service.im.chat.imdn.ImdnUtils;
import com.orangelabs.rcs.core.ims.service.im.chat.iscomposing.IsComposingManager;
import com.orangelabs.rcs.provider.messaging.RichMessaging;
import com.orangelabs.rcs.service.api.client.eventslog.EventsLogApi;
import com.orangelabs.rcs.service.api.client.messaging.InstantMessage;
import com.orangelabs.rcs.utils.NetworkRessourceManager;
import com.orangelabs.rcs.utils.PhoneUtils;
import com.orangelabs.rcs.utils.StringUtils;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Chat session
 * 
 * @author jexa7410
 */
public abstract class ChatSession extends ImsServiceSession implements MsrpEventListener {
    /**
	 * List of participants
	 */
	private ListOfParticipant participants = new ListOfParticipant();
	
	/**
	 * Subject of the conference
	 */
	private String subject;

	/**
	 * First message
	 */
	private InstantMessage firstMessage = null;
	
	/**
	 * MSRP manager
	 */
	private MsrpManager msrpMgr = null;

	/**
	 * Is composing manager
	 */
	private IsComposingManager isComposingMgr = new IsComposingManager(this);

	/**
	 * Chat activity manager
	 */
	private ChatActivityManager activityMgr = new ChatActivityManager(this);
	
	/**
	 * IMDN manager
	 */
	private ImdnManager imdnMgr = new ImdnManager(this);
	
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
	public ChatSession(ImsService parent, String contact, String subject) {
		super(parent, contact);

		// Set the session subject
		this.subject = subject;
		
		// Set the first message
		if ((subject != null) && (subject.length() > 0)) {
			firstMessage = new InstantMessage(ChatUtils.generateMessageId(), contact, StringUtils.decodeUTF8(subject), imdnMgr.isImdnActivated());
		}
		
		// Create the MSRP manager
		int localMsrpPort = NetworkRessourceManager.generateLocalMsrpPort();
		String localIpAddress = getImsService().getImsModule().getCurrentNetworkInterface().getNetworkAccess().getIpAddress();
		msrpMgr = new MsrpManager(localIpAddress, localMsrpPort);
		
		// Start the session idle timer
		activityMgr.restartInactivityTimer();
	}
    
    /**
	 * Constructor
	 * 
	 * @param parent IMS service
	 * @param contact Remote contact
	 * @param subject Subject of the conference
	 * @param participants List of participants
	 */
	public ChatSession(ImsService parent, String contact, String subject, ListOfParticipant participants) {
		this(parent, contact, subject);

		// Set the session participants
		setParticipants(participants);
	}
	
	/**
	 * Returns the event listener
	 * 
	 * @return Listener
	 */
	public ChatSessionListener getListener() {
		return (ChatSessionListener)super.getListener();
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
	 * Return the first message of the session
	 * 
	 * @return Subject
	 */
	public InstantMessage getFirstMessage() {
		return firstMessage;
	}
	
	/**
	 * Returns the list of participants
	 * 
	 * @return List of participants
	 */
    public ListOfParticipant getParticipants() {
		return participants;
	}
    
	/**
	 * Set the list of participants
	 * 
	 * @param participants List of participants
	 */
    public void setParticipants(ListOfParticipant participants) {
		this.participants = participants;
	}
    
    /**
	 * Returns the IM session identity
	 * 
	 * @return Identity (e.g. SIP-URI)
	 */
	public String getImSessionIdentity() {
		if (getDialogPath() != null) {
			return getDialogPath().getTarget();
		} else {
			return null;
		}
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
	public void handleError(ChatError error) {
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
			if (getListener() != null) {
				getListener().handleImError(error);
			}
		}
	}

	/**
	 * MSRP transfer indicator event
	 * 
	 * @param currentSize Current transfered size in bytes
	 * @param totalSize Total size in bytes
	 */
	public void msrpTransferProgress(long currentSize, long totalSize){
		// Restart the session idle timer
		activityMgr.restartInactivityTimer();
	}
	
	/**
	 * Data has been transfered
	 */
	public void msrpDataTransfered() {
    	if (logger.isActivated()) {
    		logger.info("Data transfered");
    	}
    	
		// Restart the session idle timer
		activityMgr.restartInactivityTimer();

	    // Notify listener
		if (getListener() != null) {
			getListener().handleMessageTransfered();
		}
	}
	
	/**
	 * Data has been received
	 * 
	 * @param data Received data
	 * @param mimeType Data mime-type
	 */
	public void msrpDataReceived(byte[] data, String mimeType) {
    	if (logger.isActivated()) {
    		logger.info("Data received (type " + mimeType + ")");
    	}
    	
		// Restart the session idle timer
		activityMgr.restartInactivityTimer();
	
    	if ((data == null) || (data.length == 0)) {
    		// By-pass empty data
        	if (logger.isActivated()) {
        		logger.debug("By-pass received empty data");
        	}
    		return;
    	}

		if (ChatUtils.isApplicationIsComposingType(mimeType)) {
		    // Is composing event
			receiveIsComposing(getRemoteContact(), data);
		} else
		if (ChatUtils.isTextPlainType(mimeType)) {
	    	// Text message
			receiveText(getRemoteContact(), StringUtils.decodeUTF8(new String(data)), null, false);
		} else
		if (ChatUtils.isMessageCpimType(mimeType)) {
	    	// Receive a CPIM message
			try {
    			CpimParser cpimParser = new CpimParser(data);
				CpimMessage cpimMsg = cpimParser.getCpimMessage();
				if (cpimMsg != null) {
			    	String from = cpimMsg.getHeader(CpimMessage.HEADER_FROM);
			    	String contentType = cpimMsg.getContentHeader(CpimMessage.HEADER_CONTENT_TYPE);
			    	String msgId = cpimMsg.getHeader(ImdnUtils.HEADER_IMDN_MSG_ID);
			    	if (ChatUtils.isTextPlainType(contentType)) {
				    	// Text message
		    			boolean imdnDisplayedRequested = false;
			    		
				    	// Check if the message contains an IMDN Disposition-Notification header
				    	String dispositionNotification = cpimMsg.getHeader(ImdnUtils.HEADER_IMDN_DISPO_NOTIF);
				    	if (dispositionNotification!=null){
				    		if (dispositionNotification.contains(ImdnDocument.POSITIVE_DELIVERY)){
				    			// Positive delivery requested, send MSRP message with status "delivered" 
				    			sendMsrpMessageDeliveryStatus(msgId, from, ImdnDocument.DELIVERY_STATUS_DELIVERED);
				    		}
				    		if (dispositionNotification.contains(ImdnDocument.DISPLAY)){
				    			imdnDisplayedRequested = true;
				    		}			    		
				    	}
		    			receiveText(from, StringUtils.decodeUTF8(cpimMsg.getMessageContent()), msgId, imdnDisplayedRequested);
		    			// Mark the message as waiting report, meaning we will have to send a report "displayed" when opening the message
		    			if (imdnDisplayedRequested){
		    				RichMessaging.getInstance().setMessageDeliveryStatus(msgId, from, EventsLogApi.STATUS_REPORT_REQUESTED, getParticipants().getList().size());
		    			}
			    	} else
		    		if (ChatUtils.isApplicationIsComposingType(contentType)) {
					    // Is composing event
		    			receiveIsComposing(from, cpimMsg.getMessageContent().getBytes());
			    	} else
			    	if (ChatUtils.isMessageImdnType(contentType)) {
						// Receive an IMDN report
				    	String to = cpimMsg.getHeader(CpimMessage.HEADER_TO);
						String me = ImsModule.IMS_USER_PROFILE.getPublicUri();
						
				    	// Check if this IMDN message is for me
						if (PhoneUtils.compareNumbers(me, to)) {
							receiveMessageDeliveryStatus(new String(cpimMsg.getMessageContent().getBytes()), from);
						}
			    	}
				}
	    	} catch(Exception e) {
		   		if (logger.isActivated()) {
		   			logger.error("Can't parse the CPIM message", e);
		   		}
		   	}
		} else {
			// Not supported content
        	if (logger.isActivated()) {
        		logger.debug("Not supported content " + mimeType + " in chat session");
        	}
		}
	}
    
	/**
	 * Receive text message
	 * 
	 * @param contact Contact
	 * @param txt Text message
	 * @param msgId Message Id
	 * @param flag indicating that an IMDN "displayed" is requested for this message
	 */
	private void receiveText(String contact, String txt, String msgId, boolean imdnDisplayedRequested) {
		// Is composing event is reset
	    isComposingMgr.receiveIsComposingEvent(contact, false);
	    
	    // Notify listener
		if (getListener() != null) {
			getListener().handleReceiveMessage(new InstantMessage(msgId, contact, txt, imdnDisplayedRequested));
		}
	}
	
	/**
	 * Receive is composing event
	 * 
	 * @param contact Contact
	 * @param event Event
	 */
	private void receiveIsComposing(String contact, byte[] event) {
	    isComposingMgr.receiveIsComposingEvent(contact, event);
	}

	/**
	 * MSRP transfer aborted
	 */
	public void msrpTransferAborted() {
    	if (logger.isActivated()) {
    		logger.info("Data transfer aborted");
    	}
    	
    	// Notify listener
		if (getListener() != null) {
			getListener().handleImError(new ChatError(ChatError.MSG_TRANSFER_FAILED));
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
    	
    	// Notify listener
		if (getListener() != null) {
			getListener().handleImError(new ChatError(ChatError.MSG_TRANSFER_FAILED, error));
		}
    }
	
	/**
	 * Send an empty data chunk
	 */
	public void sendEmptyDataChunk() {
		try {
			msrpMgr.sendEmptyChunk();
		} catch(Exception e) {
	   		if (logger.isActivated()) {
	   			logger.error("Problem while sending empty data chunk", e);
	   		}
		}
	}

	/**
	 * Send data chunk with a specified MIME type
	 * 
	 * @param data Data
	 * @param mime MIME type
	 * @param msgId Id of the message that is sent, to be used in case of error
	 */
	public void sendDataChunks(String data, String mime, String msgId) {
		// Restart the session idle timer
		activityMgr.restartInactivityTimer();

		try {
			ByteArrayInputStream stream = new ByteArrayInputStream(data.getBytes()); 
			msrpMgr.sendChunks(stream, mime, data.getBytes().length);			
		} catch(Exception e) {
	   		if (logger.isActivated()) {
	   			logger.error("Problem while sending data chunks", e);
	   		}
	   		
			// Mark the message that was sent as failed
			RichMessaging.getInstance().markMessageFailed(msgId);
	   		
	    	// Notify listener
			if (getListener() != null) {
				getListener().handleImError(new ChatError(ChatError.MSG_TRANSFER_FAILED, e.getMessage()));
			}
		}
	}
	
	/**
	 * Is chat group
	 * 
	 * @return Boolean
	 */
	public abstract boolean isChatGroup();
	
	/**
	 * Send a text
	 * 
	 * @param msg Message
	 * @param id Message-id
	 * @param imdnActivated If true, add IMDN headers to the request
	 */
	public abstract void sendTextMessage(String msg, String msgId, boolean imdnActivated);
	
	/**
	 * Send is composing status
	 * 
	 * @param status Status
	 */
	public abstract void sendIsComposingStatus(boolean status);
	
	/**
	 * Send a content to the remote contact
	 * 
	 * @param data Data stream to transfer
	 * @param mimeType MIME type of the data
	 * @param length of the stream
	 */
	public void sendContent(InputStream dataStream, String mimeType, long dataLength) {
		try {
			msrpMgr.sendChunks(dataStream, mimeType, dataLength);
		} catch(Exception e) {
	   		if (logger.isActivated()) {
	   			logger.error("Problem while sending data", e);
	   		}
	   		
	    	// Notify listener
			if (getListener() != null) {
				getListener().handleImError(new ChatError(ChatError.MSG_TRANSFER_FAILED, e.getMessage()));
			}
		}	
	}
	
	/**
	 * Add a participant to the session
	 * 
	 * @param participant Participant
	 */
	public abstract void addParticipant(String participant);
	
	/**
	 * Add a list of participants to the session
	 * 
	 * @param participants List of participants
	 */
	public abstract void addParticipants(List<String> participants);

    /**
     * Add IMDN headers
     * 
     * @param invite INVITE request
     * @param msgId Message 
     */
    public void addImdnHeaders(SipRequest invite, String msgId) {
		imdnMgr.addImdnHeaders(invite, msgId);
    }
    
	/**
	 * Send message delivery status via SIP MESSAGE
	 * 
	 * @param request Request
	 * @param status Status
	 */
	public void sendSipMessageDeliveryStatus(SipRequest request, String status) {
		// Check notification disposition
		if (ChatUtils.isImdnDeliveredRequested(getDialogPath().getInvite())){
			String msgId = ChatUtils.getMessageId(request);
			if (msgId != null) {
				// Send message delivery status via a SIP MESSAGE
				imdnMgr.sendSipMessageDeliveryStatus(msgId, status);
			}
		}
	}
	
	/**
	 * Send message delivery status via MSRP
	 * 
	 * @param msgId Message ID
	 * @param contact Contact that requested the delivery status
	 * @param status Status
	 */
	public void sendMsrpMessageDeliveryStatus(String msgId, String contact, String status) {
   		// Create IDMN document
		String content = ImdnDocument.buildImdnDocument(msgId, status);
		String from = ImsModule.IMS_USER_PROFILE.getPublicUri();
		String to = contact;
		String cpim = ChatUtils.buildCpimMessageWithImdnPlusXml(from, to, msgId, content, ImdnDocument.MIME_TYPE);
		
		// Send IMDN
		sendDataChunks(cpim, CpimMessage.MIME_TYPE, null);
	}
		
	/**
     * Receive a message delivery status (SIP message)
     * 
     * @param message Received message
     */
    public void receiveMessageDeliveryStatus(SipRequest message) {
    	try {
	    	// Parse the IMDN document
			InputSource input = new InputSource(new ByteArrayInputStream(message.getContentBytes()));
			ImdnParser parser = new ImdnParser(input);
			ImdnDocument imdn = parser.getImdnDocument();
			if ((imdn != null) && (imdn.getMsgId() != null) && (imdn.getStatus() != null)) {
		    	// Notify listener
				if (getListener() != null) {
					getListener().handleMessageDeliveryStatus(imdn.getMsgId(), message.getFromUri(), imdn.getStatus());
				}
			}
    	} catch(Exception e) {
    		if (logger.isActivated()) {
    			logger.error("Can't parse IMDN document", e);
    		}
    	}
    }
    
	/**
     * Receive a message delivery status (XML document)
     * 
     * @param message Received message
     * @param contact Contact that sent the delivery status
     */
    public void receiveMessageDeliveryStatus(String xml, String contact) {
    	try {
	    	// Parse the IMDN document
			InputSource input = new InputSource(new ByteArrayInputStream(xml.getBytes()));
			ImdnParser parser = new ImdnParser(input);
			ImdnDocument imdn = parser.getImdnDocument();
			if ((imdn != null) && (imdn.getMsgId() != null) && (imdn.getStatus() != null)) {
		    	// Notify listener
				if (getListener() != null) {
					getListener().handleMessageDeliveryStatus(imdn.getMsgId(), contact, imdn.getStatus());
				}
			}
    	} catch(Exception e) {
    		if (logger.isActivated()) {
    			logger.error("Can't parse IMDN document", e);
    		}
    	}
    }
}
