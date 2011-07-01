package com.orangelabs.rcs.service.api.client.messaging;

import com.orangelabs.rcs.service.api.client.messaging.IFileTransferSession;
import com.orangelabs.rcs.service.api.client.messaging.IChatSession;

/**
 * Messaging API
 */
interface IMessagingApi {
	// Transfer a file
	IFileTransferSession transferFile(in String contact, in String file);

	// Get a file transfer session from its session ID
	IFileTransferSession getFileTransferSession(in String id);

	// Get list of file transfer sessions with a contact
	List<IBinder> getFileTransferSessionsWith(in String contact);

	// Get list of current established  file transfer sessions
	List<IBinder> getFileTransferSessions();

	// Initiate a one-to-one chat session
	IChatSession initiateOne2OneChatSession(in String contact, in String subject);

	// Initiate an ad-hoc group chat session
	IChatSession initiateAdhocGroupChatSession(in List<String> participants, in String subject);

	// Get a chat session from its session ID
	IChatSession getChatSession(in String id);
	
	// Get list of chat sessions with a contact
	List<IBinder> getChatSessionsWith(in String contact);

	// Get list of current established chat sessions
	List<IBinder> getChatSessions();
}
