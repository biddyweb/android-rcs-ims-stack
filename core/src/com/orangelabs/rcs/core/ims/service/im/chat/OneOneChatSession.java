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
import com.orangelabs.rcs.core.ims.service.im.chat.iscomposing.IsComposingInfo;
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
	 * @param subject Subject of the conference
	 */
	public OneOneChatSession(ImsService parent, String contact, String subject) {
		super(parent, contact, subject);
		
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
	 * Send a text message
	 * 
	 * @param msg Message
	 * @param id Message-id
	 * @param imdnActivated If true, add IMDN headers to the request
	 */
	public void sendTextMessage(String msg, String msgId, boolean imdnActivated) {
		String content = StringUtils.encodeUTF8(msg);
		if (imdnActivated){
			// Send using CPIM + IMDN headers
			String from = ImsModule.IMS_USER_PROFILE.getPublicUri();
			String to = getRemoteContact();
			String cpim = ChatUtils.buildCpimMessageWithIMDN(from, to, msgId, content, InstantMessage.MIME_TYPE);
			sendDataChunks(cpim, CpimMessage.MIME_TYPE, msgId);
		}else{
			// Send using plain text
			sendDataChunks(content, InstantMessage.MIME_TYPE, msgId);
		}
	}
	
	/**
	 * Close media session
	 */
	public void closeMediaSession() {
		// Close MSRP session
		closeMsrpSession();
	}
	
	/**
	 * Send is composing status
	 * 
	 * @param status Status
	 */
	public void sendIsComposingStatus(boolean status) {
		String msg = IsComposingInfo.buildIsComposingInfo(status);
		sendDataChunks(msg, IsComposingInfo.MIME_TYPE, null);
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
