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

import java.util.ArrayList;
import java.util.List;

import com.orangelabs.rcs.core.ims.ImsModule;
import com.orangelabs.rcs.core.ims.service.ImsService;
import com.orangelabs.rcs.core.ims.service.im.chat.cpim.CpimMessage;
import com.orangelabs.rcs.core.ims.service.im.chat.imdn.ImdnDocument;
import com.orangelabs.rcs.core.ims.service.im.chat.iscomposing.IsComposingInfo;
import com.orangelabs.rcs.provider.messaging.RichMessaging;
import com.orangelabs.rcs.service.api.client.messaging.InstantMessage;
import com.orangelabs.rcs.utils.StringUtils;

/**
 * Abstract 1-1 chat session
 * 
 * @author jexa7410
 */
public abstract class OneOneChatSession extends ChatSession {
	/**
	 * Constructor
	 * 
	 * @param parent IMS service
	 * @param contact Remote contact
	 * @param msg First message of the session
	 */
	public OneOneChatSession(ImsService parent, String contact, String msg) {
		super(parent, contact, msg);
		
		// Set list of participants
		ListOfParticipant participants = new ListOfParticipant();
		participants.addParticipant(contact);
		setParticipants(participants);		
	}
	
	/**
	 * Is chat group
	 * 
	 * @return Boolean
	 */
	public boolean isChatGroup() {
		return false;
	}
	
	/**
	 * Returns the list of participants currently connected to the session
	 * 
	 * @return List of participants
	 */
    public ListOfParticipant getConnectedParticipants() {
		return getParticipants();
	}
    
	/**
	 * Send a text message
	 * 
	 * @param id Message-ID
	 * @param msg Message
	 */
	public void sendTextMessage(String msgId, String msg) {
		boolean useImdn = getImdnManager().isImdnActivated();
		String content;
		String mime;
		if (useImdn) {
			// Send message in CPIM + IMDN headers
			String from = ImsModule.IMS_USER_PROFILE.getPublicUri();
			String to = getRemoteContact();
			content = ChatUtils.buildCpimMessageWithImdn(from, to, msgId, StringUtils.encodeUTF8(msg), InstantMessage.MIME_TYPE);
			mime = CpimMessage.MIME_TYPE;
		} else {
			// Send message in plain text
			content = StringUtils.encodeUTF8(msg);
			mime = InstantMessage.MIME_TYPE;
		}

		// Send content
		boolean result = sendDataChunks(msgId, content, mime);

		// Update rich messaging history
		RichMessaging.getInstance().addOutgoingChatMessage(new InstantMessage(msgId, getRemoteContact(), msg, useImdn), this);

		// Check if message has been sent with success or not
		if (!result) {
			// Update rich messaging history
			RichMessaging.getInstance().markChatMessageFailed(msgId);
			
			// Notify listeners
	    	for(int i=0; i < getListeners().size(); i++) {
	    		((ChatSessionListener)getListeners().get(i)).handleMessageDeliveryStatus(msgId, ImdnDocument.DELIVERY_STATUS_FAILED);
			}
		}
	}
	
	/**
	 * Close media session
	 */
	public void closeMediaSession() {
		// Stop the activity manager
		getActivityManager().stop();		

		// Close MSRP session
		closeMsrpSession();
	}
	
	/**
	 * Send is composing status
	 * 
	 * @param status Status
	 */
	public void sendIsComposingStatus(boolean status) {
		String content = IsComposingInfo.buildIsComposingInfo(status);
		String msgId = ChatUtils.generateMessageId();
		sendDataChunks(msgId, content, IsComposingInfo.MIME_TYPE);
	}
	
	/**
	 * Add a participant to the session
	 * 
	 * @param participant Participant
	 */
	public void addParticipant(String participant) {
		ArrayList<String> participants = new ArrayList<String>();
		participants.add(participant);
		addParticipants(participants);
	}

	/**
	 * Add a list of participants to the session
	 * 
	 * @param participants List of participants
	 */
	public void addParticipants(List<String> participants) {
		// Build the list of participants
    	String existingParticipant = getParticipants().getList().get(0);
    	participants.add(existingParticipant);
		
		// Create a new session
		ExtendOneOneChatSession session = new ExtendOneOneChatSession(
			getImsService(),
			ImsModule.IMS_USER_PROFILE.getImConferenceUri(),
			this,
			new ListOfParticipant(participants));
		
		// Start the session
		session.startSession();
	}	
}
