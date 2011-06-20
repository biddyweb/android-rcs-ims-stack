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

package com.orangelabs.rcs.service.api.server.messaging;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import android.os.IBinder;

import com.orangelabs.rcs.core.Core;
import com.orangelabs.rcs.core.content.ContentManager;
import com.orangelabs.rcs.core.content.MmContent;
import com.orangelabs.rcs.core.ims.protocol.sip.SipDialogPath;
import com.orangelabs.rcs.core.ims.service.ImsServiceSession;
import com.orangelabs.rcs.core.ims.service.im.chat.ChatSession;
import com.orangelabs.rcs.core.ims.service.sharing.transfer.ContentSharingTransferSession;
import com.orangelabs.rcs.platform.file.FileDescription;
import com.orangelabs.rcs.platform.file.FileFactory;
import com.orangelabs.rcs.provider.messaging.RichMessaging;
import com.orangelabs.rcs.service.api.client.messaging.IChatSession;
import com.orangelabs.rcs.service.api.client.messaging.IFileTransferSession;
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

			// Set the file transfer session ID from the chat session if a chat already exist
			String ftSessionId = session.getSessionID();
			String chatSessionId = ftSessionId;
			Vector<ChatSession> chatSessions = Core.getInstance().getImService().getImSessionsWith(contact);
			if (chatSessions.size() > 0) {
				ChatSession chatSession = chatSessions.lastElement();
				chatSessionId = chatSession.getSessionID();
			}
			
			// Update rich messaging history
			RichMessaging.getInstance().addFileTransferInitiation(contact, chatSessionId, ftSessionId, file, session.getContent());

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
	 * Get list of file transfer sessions with a contact
	 * 
	 * @param contact Contact
	 * @return List of sessions
	 * @throws ServerApiException
	 */
	public List<IBinder> getFileTransferSessionsWith(String contact) throws ServerApiException {
		// Test core availability
		ServerApiUtils.testCore();
		
		try {
			Vector<ContentSharingTransferSession> list = Core.getInstance().getImService().getFileTransferSessionsWith(contact);
			ArrayList<IBinder> result = new ArrayList<IBinder>(list.size());
			for(int i=0; i < list.size(); i++) {
				ContentSharingTransferSession session = list.elementAt(i);
				FileTransferSession apiSession = new FileTransferSession(session);
				result.add(apiSession.asBinder());
			}
			return result;
		} catch(Exception e) {
			throw new ServerApiException(e.getMessage());
		}		
	}	

	/**
	 * Get list of current established file transfer sessions
	 * 
	 * @return List of sessions
	 * @throws ServerApiException
	 */
	public List<IBinder> getFileTransferSessions() throws ServerApiException {
		// Test core availability
		ServerApiUtils.testCore();
		
		try {
			Vector<ContentSharingTransferSession> list = Core.getInstance().getImService().getFileTransferSessions();
			ArrayList<IBinder> result = new ArrayList<IBinder>(list.size());
			for(int i=0; i < list.size(); i++) {
				ContentSharingTransferSession session = list.elementAt(i);
				SipDialogPath dialog = session.getDialogPath();
				if ((dialog != null) && (dialog.isSigEstablished())) {
					// Returns only sessions which are established
					FileTransferSession apiSession = new FileTransferSession(session);
					result.add(apiSession.asBinder());
				}
			}
			return result;
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
			ChatSession session = Core.getInstance().getImService().initiateOne2OneChatSession(contact, subject);
			
			// Update rich messaging history
			RichMessaging.getInstance().addChatInitiation(session);
			
			return new ImSession(session);
		} catch(Exception e) {
			throw new ServerApiException(e.getMessage());
		}
	}
	
	/**
	 * Initiate an ad-hoc group chat session
	 * 
     * @param participants List of participants
     * @param subject Subject of the conference
	 * @return Chat session
     * @throws ServerApiException
	 */
	public IChatSession initiateAdhocGroupChatSession(List<String> participants, String subject) throws ServerApiException {
		if (logger.isActivated()) {
			logger.info("Initiate an ad-hoc group chat session");
		}
		
		// Test IMS connection
		ServerApiUtils.testIms();

		try {
			// Initiate the session
			ChatSession session = Core.getInstance().getImService().initiateAdhocGroupChatSession(participants, subject);

			// Update rich messaging history
			RichMessaging.getInstance().addChatInitiation(session);
			
			return new ImSession(session);
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
			if ((session != null) && (session instanceof ChatSession)) {
				return new ImSession((ChatSession)session);
			} else {
				return null;
			}
		} catch(Exception e) {
			throw new ServerApiException(e.getMessage());
		}
	}
	
	/**
	 * Get list of chat sessions with a contact
	 * 
	 * @param contact Contact
	 * @return Session
	 * @throws ServerApiException
	 */
	public List<IBinder> getChatSessionsWith(String contact) throws ServerApiException {
		// Test core availability
		ServerApiUtils.testCore();
		
		try {
			Vector<ChatSession> list = Core.getInstance().getImService().getImSessionsWith(contact);
			ArrayList<IBinder> result = new ArrayList<IBinder>(list.size());
			for(int i=0; i < list.size(); i++) {
				ChatSession session = list.elementAt(i);
				ImSession apiSession = new ImSession(session);
				result.add(apiSession.asBinder());
			}
			return result;
		} catch(Exception e) {
			throw new ServerApiException(e.getMessage());
		}
	}
	
	/**
	 * Get list of current established chat sessions
	 * 
	 * @return List of sessions
	 * @throws ServerApiException
	 */
	public List<IBinder> getChatSessions() throws ServerApiException {
		// Test core availability
		ServerApiUtils.testCore();
		
		try {
			try {
				Vector<ChatSession> list = Core.getInstance().getImService().getImSessions();
				ArrayList<IBinder> result = new ArrayList<IBinder>(list.size());
				for(int i=0; i < list.size(); i++) {
					ChatSession session = list.elementAt(i);
					SipDialogPath dialog = session.getDialogPath();
					if ((dialog != null) && (dialog.isSigEstablished())) {
						// Returns only sessions which are established
						ImSession apiSession = new ImSession(session);
						result.add(apiSession.asBinder());
					}
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
