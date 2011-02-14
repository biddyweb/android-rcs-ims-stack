package com.orangelabs.rcs.service.api.client.messaging;

import com.orangelabs.rcs.service.api.client.messaging.IFileTransferSession;
import com.orangelabs.rcs.service.api.client.messaging.IInstantMessageSession;
import com.orangelabs.rcs.service.api.client.messaging.IChatSession;

/**
 * Messaging API
 */
interface IMessagingApi {

	// Send an instant message in short mode
	boolean sendShortIM(in String contact, in String txt);

	// Send an instant message in large mode
	IInstantMessageSession sendLargeIM(in String contact, in String txt);

	// Transfer a file
	IFileTransferSession transferFile(in String contact, in String file);

	// Get a file transfer session from its session ID
	IFileTransferSession getFileTransferSession(in String id);

	// Get list of file transfer sessions
	List getFileTransferSessions();

	// Initiate a one-to-one chat session
	IChatSession initiateOne2OneChatSession(in String contact, in String subject);

	// Initiate an ad-hoc group chat session
	IChatSession initiateAdhocGroupChatSession(in String subject, in List<String> participants);

	// Get a chat session from its session ID
	IChatSession getChatSession(in String id);
	
	// Get a chat session related to a given contact
	IChatSession getChatSessionWithContact(in String contact);

	// Get list of chat sessions
	List getChatSessions();
}
