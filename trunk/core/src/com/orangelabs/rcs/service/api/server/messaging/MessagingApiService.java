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

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import android.os.IBinder;

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
			// Initiate the session
			FileDescription desc = FileFactory.getFactory().getFileDescription(file);
			MmContent content = ContentManager.createMmContentFromUrl(file, desc.getSize());
			ContentSharingTransferSession session = Core.getInstance().getImService().initiateFileTransferSession(contact, content);

			// Check if there is a chat session in progress with the same contact
			String ftSessionId = session.getSessionID();
			String chatSessionId = ftSessionId;
			InstantMessageSession chatSession = Core.getInstance().getImService().getImSession(contact);
			if (chatSession != null) {
				chatSessionId = chatSession.getSessionID();
			}
			
			// Update rich messaging history
			RichMessaging.getInstance().addMessage(RichMessagingData.TYPE_FILETRANSFER, chatSessionId, ftSessionId, contact, file, RichMessagingData.EVENT_OUTGOING, session.getContent().getEncoding(), session.getContent().getName(), session.getContent().getSize(), null, RichMessagingData.STATUS_STARTED);

			return new FileTransferSession(session);
		} catch(Exception e) {
			throw new ServerApiException(e.getMessage());
		}
    }
    
	/**
	 * Get a file transfer session from its session id
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
	 * Get list of file transfer sessions
	 * 
	 * @return List of sessions
	 * @throws ServerApiException
	 */
	public List<IBinder> getFileTransferSessions() throws ServerApiException {
		// Test core availability
		ServerApiUtils.testCore();
		
		try {
			ArrayList<IBinder> result = new ArrayList<IBinder>();
			Vector<ContentSharingTransferSession> list = Core.getInstance().getImService().getFileTransferSessions();
			for(int i=0; i < list.size(); i++) {
				ContentSharingTransferSession session = list.elementAt(i);
				FileTransferSession apiSession = new FileTransferSession(session);
				result.add(apiSession);
			}
			return result;
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
			// Initiate the session
			InstantMessageSession session = Core.getInstance().getImService().initiateOne2OneChatSession(contact, subject);
			
			// Update rich messaging history
			RichMessaging.getInstance().addMessage(RichMessagingData.TYPE_CHAT, session.getSessionID(), null, contact, subject, RichMessagingData.EVENT_OUTGOING, "text/plain", null, subject.length(), null, RichMessagingData.STATUS_STARTED);
			
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
			// Initiate the session
			InstantMessageSession session = Core.getInstance().getImService().initiateAdhocGroupChatSession(subject, participants);

			// Update rich messaging history
			String contacts = "";
			for(String contact : participants){
				contacts += contact+";";
			}
			RichMessaging.getInstance().addMessage(RichMessagingData.TYPE_CHAT, session.getSessionID(), null, contacts, subject, RichMessagingData.EVENT_OUTGOING, "text/plain", null, subject.length(), null, RichMessagingData.STATUS_STARTED);
			
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
	 * Get a chat session related to a given contact
	 * 
	 * @param contact Contact
	 * @return Session
	 * @throws ServerApiException
	 */
	public IChatSession getChatSessionWithContact(String contact) throws ServerApiException {
		// Test core availability
		ServerApiUtils.testCore();
		
		try {
			InstantMessageSession session = Core.getInstance().getImService().getImSession(contact);
			if (session != null) {
				return new ChatSession(session);
			} else {
				return null;
			}
		} catch(Exception e) {
			throw new ServerApiException(e.getMessage());
		}
	}
	
	/**
	 * Get list of chat sessions
	 * 
	 * @return List of sessions
	 * @throws ServerApiException
	 */
	public List<IBinder> getChatSessions() throws ServerApiException {
		// Test core availability
		ServerApiUtils.testCore();
		
		try {
			try {
				ArrayList<IBinder> result = new ArrayList<IBinder>();
				Vector<InstantMessageSession> list = Core.getInstance().getImService().getImSessions();
				for(int i=0; i < list.size(); i++) {
					InstantMessageSession session = list.elementAt(i);
					ChatSession apiSession = new ChatSession(session);
					result.add(apiSession);
				}
				return result;
			} catch(Exception e) {
				throw new ServerApiException(e.getMessage());
			}
		} catch(Exception e) {
			throw new ServerApiException(e.getMessage());
		}
	}	
}
