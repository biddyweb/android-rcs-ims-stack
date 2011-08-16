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
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import android.content.Intent;
import android.os.IBinder;

import com.orangelabs.rcs.core.Core;
import com.orangelabs.rcs.core.content.ContentManager;
import com.orangelabs.rcs.core.content.MmContent;
import com.orangelabs.rcs.core.ims.protocol.sip.SipDialogPath;
import com.orangelabs.rcs.core.ims.service.im.chat.ChatSession;
import com.orangelabs.rcs.core.ims.service.im.chat.GroupChatSession;
import com.orangelabs.rcs.core.ims.service.im.chat.OneOneChatSession;
import com.orangelabs.rcs.core.ims.service.sharing.transfer.ContentSharingTransferSession;
import com.orangelabs.rcs.platform.AndroidFactory;
import com.orangelabs.rcs.platform.file.FileDescription;
import com.orangelabs.rcs.platform.file.FileFactory;
import com.orangelabs.rcs.provider.messaging.RichMessaging;
import com.orangelabs.rcs.service.api.client.messaging.IChatSession;
import com.orangelabs.rcs.service.api.client.messaging.IFileTransferSession;
import com.orangelabs.rcs.service.api.client.messaging.IMessagingApi;
import com.orangelabs.rcs.service.api.client.messaging.MessagingApiIntents;
import com.orangelabs.rcs.service.api.server.ServerApiException;
import com.orangelabs.rcs.service.api.server.ServerApiUtils;
import com.orangelabs.rcs.utils.PhoneUtils;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Messaging API service
 * 
 * @author jexa7410
 */
public class MessagingApiService extends IMessagingApi.Stub {
	/**
	 * List of chat sessions
	 */
	private static Hashtable<String, IChatSession> chatSessions = new Hashtable<String, IChatSession>();  
	
	/**
	 * List of file transfer sessions
	 */
	private static Hashtable<String, IFileTransferSession> ftSessions = new Hashtable<String, IFileTransferSession>();  

	/**
	 * The logger
	 */
	private static Logger logger = Logger.getLogger(MessagingApiService.class.getName());

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
		// Clear lists of sessions
		chatSessions.clear();
		ftSessions.clear();
	}

	/**
	 * Add a chat session in the list
	 * 
	 * @param session Chat session
	 */
	protected static void addChatSession(ImSession session) {
		if (logger.isActivated()) {
			logger.debug("Add a chat session in the list (size=" + chatSessions.size() + ")");
		}
		chatSessions.put(session.getSessionID(), session);
	}

	/**
	 * Remove a chat session from the list
	 * 
	 * @param sessionId Session ID
	 */
	protected static void removeChatSession(String sessionId) {
		if (logger.isActivated()) {
			logger.debug("Remove a chat session from the list (size=" + chatSessions.size() + ")");
		}
		chatSessions.remove(sessionId);
	}
	
	/**
	 * Add a file transfer session in the list
	 * 
	 * @param session File transfer session
	 */
	protected static void addFileTransferSession(FileTransferSession session) {
		if (logger.isActivated()) {
			logger.debug("Add a file transfer session in the list (size=" + ftSessions.size() + ")");
		}
		ftSessions.put(session.getSessionID(), session);
	}

	/**
	 * Remove a file transfer session from the list
	 * 
	 * @param sessionId Session ID
	 */
	protected static void removeFileTransferSession(String sessionId) {
		if (logger.isActivated()) {
			logger.debug("Remove a file transfer session from the list (size=" + ftSessions.size() + ")");
		}
		ftSessions.remove(sessionId);
	}

	/**
	 * Receive a new file transfer invitation
	 * 
	 * @param session File transfer session
	 */
    public void receiveFileTransferInvitation(ContentSharingTransferSession session) {
		if (logger.isActivated()) {
			logger.info("Receive file transfer invitation from " + session.getRemoteContact());
		}

		// Extract number from contact 
		String number = PhoneUtils.extractNumberFromUri(session.getRemoteContact());

		// Set the file transfer session ID from the chat session if a chat already exist
		String ftSessionId = session.getSessionID();
		String chatSessionId = ftSessionId;
		Vector<ChatSession> chatSessions = Core.getInstance().getImService().getImSessionsWith(number);
		if (chatSessions.size() > 0) {
			ChatSession chatSession = chatSessions.lastElement();
			chatSessionId = chatSession.getSessionID();
		}
		
		// Update rich messaging history
    	RichMessaging.getInstance().addFileTransferInvitation(number, chatSessionId, ftSessionId, session.getContent());
    	
		// Add session in the list
		FileTransferSession sessionApi = new FileTransferSession(session);
		MessagingApiService.addFileTransferSession(sessionApi);
    	
        // Notify event listeners
    	Intent intent = new Intent(MessagingApiIntents.FILE_TRANSFER_INVITATION);
    	intent.putExtra("contact", number);
    	intent.putExtra("contactDisplayname", session.getRemoteDisplayName());
    	intent.putExtra("sessionId", session.getSessionID());
    	if (chatSessions.size() > 0) {
    		intent.putExtra("chatSessionId", chatSessionId);
    	}
    	intent.putExtra("filename", session.getContent().getName());
    	intent.putExtra("filesize", session.getContent().getSize());
    	intent.putExtra("filetype", session.getContent().getEncoding());
    	AndroidFactory.getApplicationContext().sendBroadcast(intent);
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

    	// Check permission
		ServerApiUtils.testPermission();

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

			// Add session in the list
			FileTransferSession sessionApi = new FileTransferSession(session);
			addFileTransferSession(sessionApi);
			return sessionApi;
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
		if (logger.isActivated()) {
			logger.info("Get file transfer session " + id);
		}

		// Check permission
		ServerApiUtils.testPermission();

		// Test core availability
		ServerApiUtils.testCore();
		
		// Return a session instance
		return ftSessions.get(id);
	}
	
	/**
	 * Get list of file transfer sessions with a contact
	 * 
	 * @param contact Contact
	 * @return List of sessions
	 * @throws ServerApiException
	 */
	public List<IBinder> getFileTransferSessionsWith(String contact) throws ServerApiException {
		if (logger.isActivated()) {
			logger.info("Get file transfer sessions with " + contact);
		}

		// Check permission
		ServerApiUtils.testPermission();

		// Test core availability
		ServerApiUtils.testCore();
		
		try {
			Vector<ContentSharingTransferSession> list = Core.getInstance().getImService().getFileTransferSessionsWith(contact);
			ArrayList<IBinder> result = new ArrayList<IBinder>(list.size());
			for(int i=0; i < list.size(); i++) {
				ContentSharingTransferSession session = list.elementAt(i);
				IFileTransferSession sessionApi = ftSessions.get(session.getSessionID());
				if (sessionApi != null) {
					result.add(sessionApi.asBinder());
				}
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
		if (logger.isActivated()) {
			logger.info("Get file transfer sessions");
		}

		// Check permission
		ServerApiUtils.testPermission();

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
					IFileTransferSession sessionApi = ftSessions.get(session.getSessionID());
					if (sessionApi != null) {
						result.add(sessionApi.asBinder());
					}
				}
			}
			return result;
		} catch(Exception e) {
			throw new ServerApiException(e.getMessage());
		}
	}
	
	/**
	 * Receive a new chat invitation
	 * 
	 * @param session Chat session
	 */
    public void receiveOneOneChatInvitation(OneOneChatSession session) {
		if (logger.isActivated()) {
			logger.info("Receive chat invitation from " + session.getRemoteContact());
		}

		// Extract number from contact 
		String number = PhoneUtils.extractNumberFromUri(session.getRemoteContact());

		// Update rich messaging history
		RichMessaging.getInstance().addChatInvitation(session);

		// Add session in the list
		ImSession sessionApi = new ImSession(session);
		MessagingApiService.addChatSession(sessionApi);

		// Broadcast intent related to the received invitation
    	Intent intent = new Intent(MessagingApiIntents.CHAT_INVITATION);
    	intent.putExtra("contact", number);
    	intent.putExtra("contactDisplayname", session.getRemoteDisplayName());
    	intent.putExtra("subject", session.getSubject());
    	intent.putExtra("sessionId", session.getSessionID());
    	intent.putExtra("isChatGroup", false);
    	AndroidFactory.getApplicationContext().sendBroadcast(intent);
    }
    
	/**
	 * Extend a 1-1 chat session
	 * 
     * @param groupSession Group chat session
     * @param oneoneSession 1-1 chat session
	 */
    public void extendOneOneChatSession(GroupChatSession groupSession, OneOneChatSession oneoneSession) {
		if (logger.isActivated()) {
			logger.info("Extend a 1-1 chat session");
		}

		// Add session in the list
		ImSession sessionApi = new ImSession(groupSession);
		MessagingApiService.addChatSession(sessionApi);
		
		// Broadcast intent related to the received invitation
		Intent intent = new Intent(MessagingApiIntents.CHAT_SESSION_REPLACED);
		intent.putExtra("sessionId", groupSession.getSessionID());
		intent.putExtra("replacedSessionId", oneoneSession.getSessionID());
		AndroidFactory.getApplicationContext().sendBroadcast(intent);
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

    	// Check permission
		ServerApiUtils.testPermission();

		// Test IMS connection
		ServerApiUtils.testIms();
		
		try {
			// Initiate the session
			ChatSession session = Core.getInstance().getImService().initiateOne2OneChatSession(contact, subject);
			
			// Update rich messaging history
			RichMessaging.getInstance().addChatInitiation(session);
			
			// Add session in the list
			ImSession sessionApi = new ImSession(session);
			MessagingApiService.addChatSession(sessionApi);
			return sessionApi;
		} catch(Exception e) {
			throw new ServerApiException(e.getMessage());
		}
	}

	/**
	 * Receive a new group chat invitation
	 * 
	 * @param session Chat session
	 */
    public void receiveGroupChatInvitation(GroupChatSession session) {
		if (logger.isActivated()) {
			logger.info("Receive chat invitation from " + session.getRemoteContact());
		}

		// Extract number from contact 
		String number = PhoneUtils.extractNumberFromUri(session.getRemoteContact());

		// Update rich messaging history
		RichMessaging.getInstance().addChatInvitation(session);

		// Add session in the list
		ImSession sessionApi = new ImSession(session);
		MessagingApiService.addChatSession(sessionApi);

		// Broadcast intent related to the received invitation
    	Intent intent = new Intent(MessagingApiIntents.CHAT_INVITATION);
    	intent.putExtra("contact", number);
    	intent.putExtra("contactDisplayname", session.getRemoteDisplayName());
    	intent.putExtra("subject", session.getSubject());
    	intent.putExtra("sessionId", session.getSessionID());
    	intent.putExtra("isChatGroup", true);
    	intent.putExtra("replacedSessionId", session.getReplacedSessionId());
    	AndroidFactory.getApplicationContext().sendBroadcast(intent);
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
		
    	// Check permission
		ServerApiUtils.testPermission();

		// Test IMS connection
		ServerApiUtils.testIms();

		try {
			// Initiate the session
			ChatSession session = Core.getInstance().getImService().initiateAdhocGroupChatSession(participants, subject);

			// Update rich messaging history
			RichMessaging.getInstance().addChatInitiation(session);
			
			// Add session in the list
			ImSession sessionApi = new ImSession(session);
			MessagingApiService.addChatSession(sessionApi);
			return sessionApi;
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
		if (logger.isActivated()) {
			logger.info("Get chat session " + id);
		}

		// Check permission
		ServerApiUtils.testPermission();

		// Test core availability
		ServerApiUtils.testCore();

		// Return a session instance
		return chatSessions.get(id);
	}
	
	/**
	 * Get list of chat sessions with a contact
	 * 
	 * @param contact Contact
	 * @return Session
	 * @throws ServerApiException
	 */
	public List<IBinder> getChatSessionsWith(String contact) throws ServerApiException {
		if (logger.isActivated()) {
			logger.info("Get chat sessions with " + contact);
		}

		// Check permission
		ServerApiUtils.testPermission();

		// Test core availability
		ServerApiUtils.testCore();
		
		try {
			Vector<ChatSession> list = Core.getInstance().getImService().getImSessionsWith(contact);
			ArrayList<IBinder> result = new ArrayList<IBinder>(list.size());
			for(int i=0; i < list.size(); i++) {
				ChatSession session = list.elementAt(i);
				IChatSession sessionApi = chatSessions.get(session.getSessionID());
				if (sessionApi != null) {
					result.add(sessionApi.asBinder());
				}
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
		if (logger.isActivated()) {
			logger.info("Get chat sessions");
		}

		// Check permission
		ServerApiUtils.testPermission();

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
						IChatSession sessionApi = chatSessions.get(session.getSessionID());
						if (sessionApi != null) {
							result.add(sessionApi.asBinder());
						}
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
