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
import java.util.Date;
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
	 * @param msg First message of the session
	 */
	public ChatSession(ImsService parent, String contact, String msg) {
		super(parent, contact);

		// Set the first message
		if ((msg != null) && (msg.length() > 0)) {
			firstMessage = new InstantMessage(ChatUtils.generateMessageId(),
					contact, StringUtils.decodeUTF8(msg), imdnMgr.isImdnActivated());
		}
		
		// Create the MSRP manager
		int localMsrpPort = NetworkRessourceManager.generateLocalMsrpPort();
		String localIpAddress = getImsService().getImsModule().getCurrentNetworkInterface().getNetworkAccess().getIpAddress();
		msrpMgr = new MsrpManager(localIpAddress, localMsrpPort);
	}
    
    /**
	 * Constructor
	 * 
	 * @param parent IMS service
	 * @param contact Remote contact
	 * @param msg First message of the session
	 * @param participants List of participants
	 */
	public ChatSession(ImsService parent, String contact, String msg, ListOfParticipant participants) {
		this(parent, contact, msg);

		// Set the session participants
		setParticipants(participants);
	}
	
	/**
	 * Returns the IMDN manager
	 * 
	 * @return IMDN manager
	 */
	public ImdnManager getImdnManager() {
		return imdnMgr;
	}
	
	/**
	 * Returns the session activity manager
	 * 
	 * @return Activity manager
	 */
	public ChatActivityManager getActivityManager() {
		return activityMgr;
	}
	
	/**
	 * Return the contribution ID
	 * 
	 * @return Contribution ID
	 */
	public String getContributionID() {
		return getSessionID();
	}	
	
	/**
	 * Return the first message of the session
	 * 
	 * @return Instant message
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

		// Close media session
    	closeMediaSession();

    	// Remove the current session
    	getImsService().removeSession(this);

		// Notify listeners
		if (!isInterrupted()) {
	    	for(int i=0; i < getListeners().size(); i++) {
	    		((ChatSessionListener)getListeners().get(i)).handleImError(error);
	        }
		}
	}

	/**
	 * Data has been transfered
	 * 
	 * @param msgId Message ID
	 */
	public void msrpDataTransfered(String msgId) {
    	if (logger.isActivated()) {
    		logger.info("Data transfered");
    	}
    	
		// Update the activity manager
		activityMgr.updateActivity();
	}
	
	/**
	 * Data transfer has been received
	 * 
	 * @param msgId Message ID
	 * @param data Received data
	 * @param mimeType Data mime-type 
	 */
	public void msrpDataReceived(String msgId, byte[] data, String mimeType) {
    	if (logger.isActivated()) {
    		logger.info("Data received (type " + mimeType + ")");
    	}
    	
		// Update the activity manager
		activityMgr.updateActivity();
		
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
			receiveText(getRemoteContact(), StringUtils.decodeUTF8(new String(data)), null, false, new Date());
		} else
		if (ChatUtils.isMessageCpimType(mimeType)) {
	    	// Receive a CPIM message
			try {
    			CpimParser cpimParser = new CpimParser(data);
				CpimMessage cpimMsg = cpimParser.getCpimMessage();
				if (cpimMsg != null) {
			    	String from = cpimMsg.getHeader(CpimMessage.HEADER_FROM);
			    	String contentType = cpimMsg.getContentHeader(CpimMessage.HEADER_CONTENT_TYPE);
			    	if (ChatUtils.isTextPlainType(contentType)) {
				    	// Text message
		    			boolean imdnDisplayedRequested = false;
			    		
				    	// Check if the message contains an IMDN Disposition-Notification header
				    	String dispositionNotification = cpimMsg.getHeader(ImdnUtils.HEADER_IMDN_DISPO_NOTIF);
				    	if (dispositionNotification != null) {
				    		if (dispositionNotification.contains(ImdnDocument.POSITIVE_DELIVERY)){
				    			// Positive delivery requested, send MSRP message with status "delivered" 
				    			sendMsrpMessageDeliveryStatus(msgId, from, ImdnDocument.DELIVERY_STATUS_DELIVERED);
				    		}
				    		if (dispositionNotification.contains(ImdnDocument.DISPLAY)){
				    			imdnDisplayedRequested = true;
				    		}			    		
				    	}
				    	Date date = new Date();
		    			receiveText(from, StringUtils.decodeUTF8(cpimMsg.getMessageContent()), msgId, imdnDisplayedRequested, date);

		    			// Mark the message as waiting report, meaning we will have to send a report "displayed" when opening the message
		    			if (imdnDisplayedRequested){
		    				RichMessaging.getInstance().setChatMessageDeliveryStatus(msgId, EventsLogApi.STATUS_REPORT_REQUESTED);
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
	 * Data transfer in progress
	 * 
	 * @param currentSize Current transfered size in bytes
	 * @param totalSize Total size in bytes
	 */
	public void msrpTransferProgress(long currentSize, long totalSize) {
		// Not used by chat
	}

	/**
	 * Data transfer has been aborted
	 */
	public void msrpTransferAborted() {
    	// Not used by chat
	}	

	/**
	 * Data transfer error
	 * 
	 * @param error Error
	 */
	public void msrpTransferError(String error) {
    	if (logger.isActivated()) {
    		logger.info("Data transfer error: " + error);
    	}
    	
		// Notify listeners
    	for(int i=0; i < getListeners().size(); i++) {
    		((ChatSessionListener)getListeners().get(i)).handleImError(new ChatError(ChatError.MEDIA_SESSION_FAILED));
		}
    }

	/**
	 * Receive text message
	 * 
	 * @param contact Contact
	 * @param txt Text message
	 * @param msgId Message Id
	 * @param flag indicating that an IMDN "displayed" is requested for this message
	 * @param date Date at which the message was emitted
	 */
	private void receiveText(String contact, String txt, String msgId, boolean imdnDisplayedRequested, Date date) {
		// Is composing event is reset
	    isComposingMgr.receiveIsComposingEvent(contact, false);
	    
	    // Notify listeners
    	for(int i=0; i < getListeners().size(); i++) {
    		((ChatSessionListener)getListeners().get(i)).handleReceiveMessage(new InstantMessage(msgId, contact, txt, imdnDisplayedRequested, date));
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
	 * @param msgId Message ID
	 * @param data Data
	 * @param mime MIME type
	 * @return Boolean result
	 */
	public boolean sendDataChunks(String msgId, String data, String mime) {
		try {
			ByteArrayInputStream stream = new ByteArrayInputStream(data.getBytes()); 
			msrpMgr.sendChunks(stream, msgId, mime, data.getBytes().length);
			return true;
		} catch(Exception e) {
			// Error
	   		if (logger.isActivated()) {
	   			logger.error("Problem while sending data chunks", e);
	   		}
			return false;
		}
	}
	
	/**
	 * Is chat group
	 * 
	 * @return Boolean
	 */
	public abstract boolean isChatGroup();
	
	/**
	 * Send a text message
	 * 
	 * @param msgId Message-ID
	 * @param msg Message
	 * @return Boolean result
	 */
	public abstract void sendTextMessage(String msgId, String msg);
	
	/**
	 * Send is composing status
	 * 
	 * @param status Status
	 */
	public abstract void sendIsComposingStatus(boolean status);
	
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
	public void sendSipMessageDeliveryStatus(SipRequest request, final String status) {
		// Check notification disposition
		if (ChatUtils.isImdnDeliveredRequested(getDialogPath().getInvite())){
			final String msgId = ChatUtils.getMessageId(request);
			if (msgId != null) {
				// Send message delivery status via a SIP MESSAGE
				Thread t = new Thread() {
					public void run() {
						imdnMgr.sendSipMessageDeliveryStatus(getDialogPath().getRemoteParty(), msgId, status);
					}
				};
				t.start();
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
		// Send status in CPIM + IMDN headers
		String from = ImsModule.IMS_USER_PROFILE.getPublicUri();
		String to = contact;
		String content = ChatUtils.buildCpimDeliveryStatus(from, to, msgId, ImdnDocument.buildImdnDocument(msgId, status), ImdnDocument.MIME_TYPE);
		
		// Send data
		sendDataChunks(msgId, content, CpimMessage.MIME_TYPE);
	}
		
	/**
     * Receive a message delivery status (SIP message)
     * 
     * @param message Received message
     */
    public void receiveMessageDeliveryStatus(SipRequest message) {
    	try {
    		// Try to parse the content as CPIM
    		String content = message.getContent();
    		CpimParser cpimParser = new CpimParser(content);
    		CpimMessage cpimMsg = cpimParser.getCpimMessage();
    		if (cpimMsg != null) {
    			String from = cpimMsg.getHeader(CpimMessage.HEADER_FROM);
    			String contentType = cpimMsg.getContentHeader(CpimMessage.HEADER_CONTENT_TYPE);

    			// Check if the content is a IMDN message    		
    			if (ChatUtils.isMessageImdnType(contentType)) {
    				// Parse the IMDN document
    				InputSource input = new InputSource(new ByteArrayInputStream(cpimMsg.getMessageContent().getBytes()));
    				ImdnParser parser = new ImdnParser(input);
    				ImdnDocument imdn = parser.getImdnDocument();
    				if ((imdn != null) && (imdn.getMsgId() != null) && (imdn.getStatus() != null)) {
    					// Notify listeners
    			    	for(int i=0; i < getListeners().size(); i++) {
    			    		((ChatSessionListener)getListeners().get(i)).handleMessageDeliveryStatus(imdn.getMsgId(), from, imdn.getStatus());
    					}
    				}
    			}
    		}
    	} catch(Exception e) {
    		if (logger.isActivated()) {
    			logger.error("Can't parse SIP message as IMDN document", e);
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
		    	// Notify listeners
		    	for(int i=0; i < getListeners().size(); i++) {
		    		((ChatSessionListener)getListeners().get(i)).handleMessageDeliveryStatus(imdn.getMsgId(), contact, imdn.getStatus());
				}
			}
    	} catch(Exception e) {
    		if (logger.isActivated()) {
    			logger.error("Can't parse IMDN document", e);
    		}
    	}
    }
}
