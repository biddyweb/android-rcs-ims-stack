/*******************************************************************************
 * Software Name : RCS IMS Stack
 *
 * Copyright Â© 2010 France Telecom S.A.
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
		/*
	    When a participant in a one to one session wants to extend the session to an Ad-hoc conference session, the IM Client:
		- 1. SHALL generate an initial SIP INVITE request according to rules and procedures of [RFC3261] and with the
		additional clarification as specified in section 7.1.1.1 "General";
		- 2. SHALL set the Request-URI of the SIP INVITE request to the Conference-factory-URI for the IM service in the
		Home Network of the IM User;
		- 3. SHALL add the invited user(s) in a MIME resource-list body according to [draft-URI-list], including also the identity
		of the original invited user;
		- a) SHALL for the originally invited user identity in the MIME resource list, include a Replaces header with the
		original session identity according to rules and procedures of [RFC3891] as illustrated in Appendix L “Extending a
		one to one session to a conference”;
		- 4. The IM Client SHALL check that the number of Invited IM Users on the URI-list does not exceed the maximum
		number of Participants allowed in an Ad-hoc IM Group Session as indicated in “MAX-ADHOC-GROUP-SIZE”
		parameter provisioned for IM Client as described in Appendix I “The parameters to be provisioned for IM service”. If
		exceeded, the IM Client SHOULD notify the IM User. Otherwise, continue with the rest of the steps;
		- 5. SHALL insert in the SIP INVITE request a Content-Type header with multipart/mixed as specified in [RFC2046];
		- 6. SHALL include in the SIP INVITE request a MIME SDP body as a SDP offer according to rules and procedures of
		[RFC3264], [RFC 4566] and [MSRP] with the following additional clarification, the IM Client:
		- a) SHALL set the SDP “accept-types” attribute to a = accept-types : message/cpim; and
		- b) MAY list other formats or use ‘*’ as defined in [MSRP];
		- 7. SHALL send the SIP INVITE request towards the controlling IM Server according to rules and procedures of SIP/IP
		Core.
		On receiving a SIP 200 "OK" response to the SIP INVITE request the IM Client:
		- 1. SHALL store the IM Session Identity if received in the Contact header as described in [draft-URI-list]; and,
		- 2. SHALL interact with the User Plane.
		NOTE: The BYE request received as a result of the Replaces header is handled as described in 7.1.2.3 “IM Client
		Receiving a session release request”.
	*/
		// TODO
	}	
}
