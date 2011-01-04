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
package com.orangelabs.rcs.service.api.server.messaging;

import java.util.List;

import com.orangelabs.rcs.core.Core;
import com.orangelabs.rcs.core.content.ContentManager;
import com.orangelabs.rcs.core.content.MmContent;
import com.orangelabs.rcs.core.ims.service.ImsServiceSession;
import com.orangelabs.rcs.core.ims.service.im.InstantMessage;
import com.orangelabs.rcs.core.ims.service.im.InstantMessageSession;
import com.orangelabs.rcs.core.ims.service.sharing.transfer.ContentSharingTransferSession;
import com.orangelabs.rcs.platform.file.FileDescription;
import com.orangelabs.rcs.platform.file.FileFactory;
import com.orangelabs.rcs.provider.messaging.RichMessaging;
import com.orangelabs.rcs.provider.messaging.RichMessagingData;
import com.orangelabs.rcs.service.api.client.messaging.IChatSession;
import com.orangelabs.rcs.service.api.client.messaging.IFileTransferSession;
import com.orangelabs.rcs.service.api.client.messaging.IInstantMessageSession;
import com.orangelabs.rcs.service.api.client.messaging.IMessagingApi;
import com.orangelabs.rcs.service.api.server.ServerApiException;
import com.orangelabs.rcs.service.api.server.ServerApiUtils;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Messaging API service
 * 
 * @author jexa7410
 */
public class MessagingApiService extends IMessagingApi.Stub {
	/**
	 * The logger
	 */
	private Logger logger = Logger.getLogger(this.getClass().getName());

	/**
	 * Constructor
	 */
	public MessagingApiService() {
		if (logger.isActivated()) {
			logger.info("Messaging API service is loaded");
		}
	}
	
	/**
	 * Close API
	 */
	public void close() {
	}

    /**
     * Transfer a file
     *
     * @param contact Contact
     * @param file File to be transfered
     * @param File transfer session
     * @throws ServerApiException
     */
    public IFileTransferSession transferFile(String contact, String file) throws ServerApiException {	
		if (logger.isActivated()) {
			logger.info("Transfer file " + file + " to " + contact);
		}

		// Test IMS connection
		ServerApiUtils.testIms();

		try {
			FileDescription desc = FileFactory.getFactory().getFileDescription(file);
			MmContent content = ContentManager.createMmContentFromUrl(file, desc.getSize());
			ContentSharingTransferSession session = Core.getInstance().getImService().transferFile(contact, content);

			String ftSessionId = session.getSessionID();
			String imSessionId = null;
			
			try {
				// Check if there is a chat session in progress
				IChatSession chatSession = this.getChatSessionWithContact(contact);
				imSessionId = (chatSession!=null)?chatSession.getSessionID():ftSessionId;
			} catch (Exception e) {
				if (logger.isActivated()) {
					logger.error("GetChatSession",e);
				}
				imSessionId = ftSessionId;
			}			
			
			RichMessaging.getInstance().addMessage(RichMessagingData.FILETRANSFER, imSessionId, ftSessionId, contact, file, RichMessagingData.OUTGOING, session.getContent().getEncoding(), session.getContent().getName(), session.getContent().getSize(), null, RichMessagingData.INVITING);
			return new FileTransferSession(session);
		} catch(Exception e) {
			throw new ServerApiException(e.getMessage());
		}
    }
    
	/**
	 * Get the file transfer session from its session id
	 * 
	 * @param id Session ID
	 * @return Session
	 * @throws ServerApiException
	 */
	public IFileTransferSession getFileTransferSession(String id) throws ServerApiException {
		// Test core availability
		ServerApiUtils.testCore();
		
		try {
			ImsServiceSession session = Core.getInstance().getImService().getSession(id);
			if ((session != null) && (session instanceof ContentSharingTransferSession)) {
				return new FileTransferSession((ContentSharingTransferSession)session);
			} else {
				return null;
			}
		} catch(Exception e) {
			throw new ServerApiException(e.getMessage());
		}
	}

	/**
	 * Send an instant message in short mode
	 * 
     * @param contact Contact
     * @param txt Text message
     * @return Boolean result
     * @throws ServerApiException
	 */
	public boolean sendShortIM(String contact, String txt) throws ServerApiException {
		if (logger.isActivated()) {
			logger.info("Send IM in short mode to " + contact);
		}

		// Test IMS connection
		ServerApiUtils.testIms();

		try {
			InstantMessage msg = new InstantMessage(contact, txt); 
			return Core.getInstance().getImService().sendPagerInstantMessage(msg);
		} catch(Exception e) {
			throw new ServerApiException(e.getMessage());
		}
	}

	/**
	 * Send an instant message in large message
	 * 
     * @param contact Contact
     * @param txt Text message
     * @return IM session
     * @throws ServerApiException
	 */
	public IInstantMessageSession sendLargeIM(String contact, String txt) throws ServerApiException {
		if (logger.isActivated()) {
			logger.info("Send IM in large mode to " + contact);
		}

		// Test IMS connection
		ServerApiUtils.testIms();

		try {
			InstantMessage msg = new InstantMessage(contact, txt); 
			ImsServiceSession session = Core.getInstance().getImService().sendLargeInstantMessage(msg);
			return new ImSession(session);
		} catch(Exception e) {
			throw new ServerApiException(e.getMessage());
		}
	}
	
	/**
	 * Initiate a one-to-one chat session
	 * 
     * @param contact Remote contact
     * @param subject Subject of the conference
	 * @return Chat session
     * @throws ServerApiException
	 */
	public IChatSession initiateOne2OneChatSession(String contact, String subject) throws ServerApiException {
		if (logger.isActivated()) {
			logger.info("Initiate a 1-1 chat session with " + contact);
		}

		// Test IMS connection
		ServerApiUtils.testIms();

		try {
			InstantMessageSession session = Core.getInstance().getImService().initiateOne2OneChatSession(contact, subject);
			
			RichMessaging.getInstance().addMessage(RichMessagingData.CHAT, session.getSessionID(), null, contact, subject, RichMessagingData.OUTGOING, "text/plain", null, subject.length(), null, RichMessagingData.INVITING);
			
			return new ChatSession(session);
		} catch(Exception e) {
			throw new ServerApiException(e.getMessage());
		}
	}
	
	/**
	 * Initiate an ad-hoc group chat session
	 * 
     * @param subject Subject of the conference
     * @param participants List of participants
	 * @return Chat session
     * @throws ServerApiException
	 */
	public IChatSession initiateAdhocGroupChatSession(String subject, List<String> participants) throws ServerApiException {
		if (logger.isActivated()) {
			logger.info("Initiate an ad-hoc group chat session");
		}
		
		// Test IMS connection
		ServerApiUtils.testIms();

		try {
			// TODO: use subject
			InstantMessageSession session = Core.getInstance().getImService().initiateAdhocGroupChatSession(subject, participants);
			String contacts = "";
			for(String contact : participants){
				contacts += contact+";";
			}
			RichMessaging.getInstance().addMessage(RichMessagingData.CHAT, session.getSessionID(), null, contacts, subject, RichMessagingData.OUTGOING, "text/plain", null, subject.length(), null, RichMessagingData.INVITING);
			
			return new ChatSession(session);
		} catch(Exception e) {
			throw new ServerApiException(e.getMessage());
		}
	}
	
	/**
	 * Get a chat session from its session id
	 * 
	 * @param id Session ID
	 * @return Session
	 * @throws ServerApiException
	 */
	public IChatSession getChatSession(String id) throws ServerApiException {
		// Test core availability
		ServerApiUtils.testCore();
		
		try {
			ImsServiceSession session = Core.getInstance().getImService().getSession(id);
			if ((session != null) && (session instanceof InstantMessageSession)) {
				return new ChatSession((InstantMessageSession)session);
			} else {
				return null;
			}
		} catch(Exception e) {
			throw new ServerApiException(e.getMessage());
		}
	}
	
	/**
	 * Get the chat session with given contact or returns null if no session is in progress
	 * 
	 * @param contact Contact
	 * @return Session
	 * @throws ServerApiException
	 */
	public IChatSession getChatSessionWithContact(String contact) throws ServerApiException {
		// Test core availability
		ServerApiUtils.testCore();
		
		try {
			ImsServiceSession session = Core.getInstance().getImService().getSessionWithContact(contact);
			if ((session != null) && (session instanceof InstantMessageSession)) {
				return new ChatSession((InstantMessageSession)session);
			} else {
				return null;
			}
		} catch(Exception e) {
			throw new ServerApiException(e.getMessage());
		}
	}
}
