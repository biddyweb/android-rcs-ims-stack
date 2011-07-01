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

package com.orangelabs.rcs.provider.messaging;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.orangelabs.rcs.core.content.MmContent;
import com.orangelabs.rcs.core.ims.service.im.chat.ChatSession;
import com.orangelabs.rcs.core.ims.service.im.chat.ChatUtils;
import com.orangelabs.rcs.core.ims.service.im.chat.event.User;
import com.orangelabs.rcs.provider.settings.RcsSettings;
import com.orangelabs.rcs.service.api.client.eventslog.EventsLogApi;
import com.orangelabs.rcs.service.api.client.messaging.InstantMessage;
import com.orangelabs.rcs.utils.PhoneUtils;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Rich messaging history. This content provider removes old messages if there is no enough space.
 * 
 * @author mhsm6403
 */
public class RichMessaging {
	/**
	 * Current instance
	 */
	private static RichMessaging instance = null;

	/**
	 * Content resolver
	 */
	private ContentResolver cr;
	
	/**
	 * Database URI
	 */
	private Uri databaseUri = RichMessagingData.CONTENT_URI;
	
	/**
	 * The logger
	 */
	private Logger logger = Logger.getLogger(this.getClass().getName());
	
	/**
	 * Create instance
	 * 
	 * @param ctx Context
	 */
	public static synchronized void createInstance(Context ctx) {
		if (instance == null) {
			instance = new RichMessaging(ctx);
		}
	}
	
	/**
	 * Returns instance
	 * 
	 * @return Instance
	 */
	public static RichMessaging getInstance() {
		return instance;
	}
	
	/**
     * Constructor
     * 
     * @param ctx Application context
     */

	private RichMessaging(Context ctx) {
		super();
		
        this.cr = ctx.getContentResolver();
	}
	
	/**
	 * New event concerning a conference
	 * 
	 * @param contact
	 * @param sessionId
	 * @param state
	 * @return uri
	 */
	public Uri newConferenceEvent(String contact, String sessionId, String state ){
		if (state.equals(User.STATE_DISCONNECTED)){
			return addMessage(EventsLogApi.TYPE_GROUP_CHAT_SYSTEM_MESSAGE, sessionId, null, contact, null, InstantMessage.MIME_TYPE, null, 0, null, EventsLogApi.EVENT_LEFT_CHAT);
		}else if (state.equals(User.STATE_CONNECTED)){
			return addMessage(EventsLogApi.TYPE_GROUP_CHAT_SYSTEM_MESSAGE, sessionId, null, contact, null, InstantMessage.MIME_TYPE, null, 0, null, EventsLogApi.EVENT_JOINED_CHAT);
		}else{
			// We do not log events other than joining and leaving a session 
			return null;
		}
	}
	
	/**
	 * We were invited to a chat session
	 * 
	 * @param session
	 * @return uri
	 */
	public void addChatInvitation(ChatSession session){
		String sessionId = session.getSessionID();
		String inviter = PhoneUtils.extractNumberFromUri(session.getRemoteContact());
		String sessionSubject = session.getSubject();

		int type = EventsLogApi.TYPE_CHAT_SYSTEM_MESSAGE;
		if (session.isChatGroup()){
			type = EventsLogApi.TYPE_GROUP_CHAT_SYSTEM_MESSAGE;
		}
		addMessage(type, sessionId, null, inviter, sessionSubject, InstantMessage.MIME_TYPE, null, sessionSubject.length(), null, EventsLogApi.EVENT_INVITED);
		
		// Set the subject as the first message
		if ((sessionSubject != null) && (sessionSubject.length() > 0)) {
			String msgId = ChatUtils.getMessageId(session.getDialogPath().getInvite());
			addChatMessageReception(new InstantMessage(msgId, inviter, sessionSubject, RcsSettings.getInstance().isImReportsActivated()), session);

			// Mark the message as "requested for report" if needed
			if (msgId!=null && ChatUtils.isImdnDisplayedRequested(session.getDialogPath().getInvite())){
    			// Mark the message as waiting report, meaning we will have to send a report "displayed" when opening the message
    			setMessageDeliveryStatus(msgId, inviter, EventsLogApi.STATUS_REPORT_REQUESTED, session.getParticipants().getList().size());
			}
		}
	}
	
	/**
	 * We were invited to a file transfer session
	 * 
	 * @param inviter
	 * @param chatSessionId May be null if file transfer took place outside of a chat session
	 * @param sessionId
	 * @param content
	 * @return uri
	 */
	public Uri addFileTransferInvitation(String inviter, String chatSessionId, String ftSessionId, MmContent content){
		return addMessage(EventsLogApi.TYPE_INCOMING_FILE_TRANSFER, chatSessionId, ftSessionId, inviter, null, content.getEncoding(), content.getName(), content.getSize(), null, EventsLogApi.STATUS_STARTED);	
	}
	
	/**
	 * We initiated a chat session
	 * 
	 * @param session Chat session 
	 */
	public void addChatInitiation(ChatSession session){
		String sessionId = session.getSessionID();
		String invited = "";
		int type = EventsLogApi.TYPE_CHAT_SYSTEM_MESSAGE;
		if (session.isChatGroup()){
			type = EventsLogApi.TYPE_GROUP_CHAT_SYSTEM_MESSAGE;
			for(String contact : session.getParticipants().getList()){
				if (contact!=null){
					invited += PhoneUtils.extractNumberFromUri(contact)+";";
				}
			}
		}else{
			invited = PhoneUtils.extractNumberFromUri(session.getRemoteContact());
		}
		
		addMessage(type, sessionId, null, invited, session.getSubject(), InstantMessage.MIME_TYPE, null, 0, null, EventsLogApi.EVENT_INITIATED);

		// Set the subject as the first message
		InstantMessage firstMessage = session.getFirstMessage();
		if (firstMessage != null) {
			addChatMessageInitiation(firstMessage, session);
		}		
	}
	
	/**
	 * We invited someone to a file transfer session
	 * 
	 * @param contact
	 * @param chatSessionId May be null if file transfer took place outside of a chat session
	 * @param sessionId
	 * @param filename
	 * @param content
	 * @return uri
	 */
	public Uri addFileTransferInitiation(String contact, String chatSessionId, String ftSessionId, String fileName, MmContent content){
		return addMessage(EventsLogApi.TYPE_OUTGOING_FILE_TRANSFER, chatSessionId, ftSessionId, contact, fileName, content.getEncoding(), content.getName(), content.getSize(), null, EventsLogApi.EVENT_INITIATED);	
	}
	
	/**
	 * We received a message during a chat session
	 * 
	 * @param instantMessage
	 * @param session
	 * @return uri
	 */
	public Uri addChatMessageReception(InstantMessage message, ChatSession session){
		int type = EventsLogApi.TYPE_INCOMING_CHAT_MESSAGE;
		if (session.isChatGroup()){
			type = EventsLogApi.TYPE_INCOMING_GROUP_CHAT_MESSAGE;
		}
		return addMessage(type, session.getSessionID(), message.getMessageId(), message.getRemote(), message.getTextMessage(), InstantMessage.MIME_TYPE, message.getRemote(), message.getTextMessage().length(), message.getDate(), EventsLogApi.STATUS_RECEIVED);			
	}
	
	/**
	 * We sent a message during a chat session
	 * 
	 * @param instantMessage
	 * @param sessionId
	 * @return uri
	 */
	public Uri addChatMessageInitiation(InstantMessage message, ChatSession session){
		int type = EventsLogApi.TYPE_OUTGOING_CHAT_MESSAGE;
		if (session.isChatGroup()){
			type = EventsLogApi.TYPE_OUTGOING_GROUP_CHAT_MESSAGE;
		}
		return addMessage(type, session.getSessionID(), message.getMessageId(), message.getRemote(), message.getTextMessage(), InstantMessage.MIME_TYPE, message.getRemote(), message.getTextMessage().length(), message.getDate(), EventsLogApi.STATUS_SENT);			
	}
	
	/**
	 * Set the delivery status for the given message
	 * 
	 * @param messageID
	 * @param contact
	 * @param status
	 * @param numberOfRecipients Number of persons participating to the session
	 */
	public void setMessageDeliveryStatus(String messageId, String contact, int status, int numberOfRecipients){
		contact = PhoneUtils.extractNumberFromUri(contact);
		
		ContentValues values = new ContentValues();
		
		String columnName = null;		
		// Update the contacts in the IMDN displayed and delivered columns 
		if (status == EventsLogApi.STATUS_DISPLAYED){
			columnName = RichMessagingData.KEY_CHAT_GROUP_IMDN_DISPLAYED;
		}else if (status == EventsLogApi.STATUS_DELIVERED){
			columnName = RichMessagingData.KEY_CHAT_GROUP_IMDN_DELIVERED;
		}
		
		if (columnName!=null){
			Cursor cursor = cr.query(databaseUri, 
					new String[]{columnName}, 
					RichMessagingData.KEY_MESSAGE_ID + " =\'" + messageId + "\'", 
					null, 
					null);
			if (cursor.moveToFirst()){
				String contacts = cursor.getString(0);
				
				// Append the new contact to the end of IMDN displayed column
				if (contacts==null || contacts.equalsIgnoreCase("null")){
					values.put(columnName, contact+";");
				}else if (!contacts.contains(contact)){
					values.put(columnName, contacts + contact+";");
				}
				cr.update(databaseUri, 
						values, 
						RichMessagingData.KEY_MESSAGE_ID + " = \'" + messageId + "\'", 
						null);
			}
			cursor.close();
		}
		
		if (status == EventsLogApi.STATUS_DISPLAYED){
			// In case of a displayed status, we check if everyone has displayed the message
			Cursor cursor = cr.query(databaseUri, 
					new String[]{RichMessagingData.KEY_CHAT_GROUP_IMDN_DISPLAYED}, 
					RichMessagingData.KEY_MESSAGE_ID + " =\'" + messageId + "\'", 
					null, 
					null);
			if (cursor.moveToFirst()){
				String contacts = cursor.getString(0);
				if (contacts.split(";").length == numberOfRecipients){
					// All participants have received the message
					// Instead of setting the status to EventsLogApi.STATUS_DISPLAYED, we set it to EventsLogApi.STATUS_ALL_DISPLAYED
					status = EventsLogApi.STATUS_ALL_DISPLAYED;
				}
			}
			cursor.close();
		}
		
		values = new ContentValues();
		values.put(RichMessagingData.KEY_STATUS, status);
		cr.update(databaseUri, 
				values, 
				RichMessagingData.KEY_MESSAGE_ID + " = \'" + messageId + "\'", 
				null);
		
		

	}
	
	/**
	 * A one to one chat session was stopped
	 * 
	 * @param contact
	 * @param sessionId
	 * @return uri
	 */
	public Uri addOneToOneChatTermination(String contact, String sessionId){
		return addMessage(EventsLogApi.TYPE_CHAT_SYSTEM_MESSAGE, sessionId, null, contact, null, InstantMessage.MIME_TYPE, null, 0, new Date(), EventsLogApi.STATUS_TERMINATED);
	}
	
	/**
	 * A group chat session was stopped
	 * 
	 * @param participants
	 * @param sessionId
	 * @return uri
	 */
	public Uri addGroupChatTermination(List<String> participants, String sessionId){
		String contacts = "";
		for(String contact : participants){
			contacts += contact+";";
		}
		return addMessage(EventsLogApi.TYPE_GROUP_CHAT_SYSTEM_MESSAGE, sessionId, null, contacts, null, InstantMessage.MIME_TYPE, null, 0, new Date(), EventsLogApi.STATUS_TERMINATED);
	}
	
	/**
	 * A one to one chat session had an error
	 * 
	 * @param contact
	 * @param sessionId
	 * @return uri
	 */
	public Uri addOneToOneChatError(String contact, String sessionId){
		return addMessage(EventsLogApi.TYPE_CHAT_SYSTEM_MESSAGE, sessionId, null, contact, null, InstantMessage.MIME_TYPE, null, 0, new Date(), EventsLogApi.STATUS_FAILED);
	}
	
	/**
	 * A group chat session had an error
	 * 
	 * @param participants
	 * @param sessionId
	 * @return uri
	 */
	public Uri addGroupChatError(List<String> participants, String sessionId){
		String contacts = "";
		for(String contact : participants){
			contacts += contact+";";
		}
		return addMessage(EventsLogApi.TYPE_GROUP_CHAT_SYSTEM_MESSAGE, sessionId, null, contacts, null, InstantMessage.MIME_TYPE, null, 0, new Date(), EventsLogApi.STATUS_FAILED);
	}
	
	/**
	 * A session initiation failed and the message was not delivered
	 * 
	 * @param sessionId
	 */
	public void markMessageFailedForSession(String sessionId){
		ContentValues values = new ContentValues();
		values.put(RichMessagingData.KEY_STATUS, EventsLogApi.STATUS_FAILED);
		cr.update(databaseUri, 
				values, 
				RichMessagingData.KEY_CHAT_SESSION_ID +" = \""+sessionId+"\"" +" AND " + RichMessagingData.KEY_TYPE + " = " + EventsLogApi.TYPE_OUTGOING_CHAT_MESSAGE, 
				null);
	}
	
	/**
	 * A message could not be sent
	 * 
	 * @param msgId
	 */
	public void markMessageFailed(String msgId){
		ContentValues values = new ContentValues();
		values.put(RichMessagingData.KEY_STATUS, EventsLogApi.STATUS_FAILED);
		cr.update(databaseUri, 
				values, 
				RichMessagingData.KEY_MESSAGE_ID +" = \""+msgId+"\"" +" AND " + RichMessagingData.KEY_TYPE + " = " + EventsLogApi.TYPE_OUTGOING_CHAT_MESSAGE, 
				null);
	}
	
	/**
	 * Mark a message as spam message or remove it from spam
	 * 
	 * @param message
	 */
	public void addSpamMsg(InstantMessage message){
		ContentValues values = new ContentValues();
		values.put(RichMessagingData.KEY_IS_SPAM, EventsLogApi.MESSAGE_IS_SPAM);
		int type = EventsLogApi.TYPE_INCOMING_CHAT_MESSAGE;
		addMessage(type, ""+System.currentTimeMillis(), message.getMessageId(), message.getRemote(), message.getTextMessage(), InstantMessage.MIME_TYPE, message.getRemote(), message.getTextMessage().length(), message.getDate(), EventsLogApi.STATUS_RECEIVED);
		markMessageAsSpam(message.getMessageId(), true);
	}
	
	/**
	 * Mark a message as spam message or remove it from spam
	 * 
	 * @param msgId
	 * @param isSpam
	 */
	public void markMessageAsSpam(String msgId, boolean isSpam){
		ContentValues values = new ContentValues();
		if (isSpam){
			values.put(RichMessagingData.KEY_IS_SPAM, EventsLogApi.MESSAGE_IS_SPAM);
		}else{
			values.put(RichMessagingData.KEY_IS_SPAM, EventsLogApi.MESSAGE_IS_NOT_SPAM);
		}
		cr.update(databaseUri, 
				values, 
				RichMessagingData.KEY_MESSAGE_ID +" = \""+msgId+"\"" +" AND " + RichMessagingData.KEY_TYPE + " = " + EventsLogApi.TYPE_INCOMING_CHAT_MESSAGE, 
				null);
	}
	
	/**
	 * Delete all spams
	 */
	public void deleteAllSpams(){
		// We use delete im item method to recyclate the message number available for contacts
		Cursor c = cr.query(databaseUri,
				new String[]{RichMessagingData.KEY_ID},
				RichMessagingData.KEY_IS_SPAM + " = \"" + EventsLogApi.MESSAGE_IS_SPAM +"\"",
				null,
				null);
		while (c.moveToNext()){
			long rowId = c.getLong(0);
			deleteImItem(rowId);
		}
		c.close();
	}

	/**
	 * Add a new message (chat message, file transfer, large IM, short IM)
	 * 
	 * @param discriminator The type of message sent (RichData.FILETRANSFER or RichData.INSTANTMESSAGING)
	 * @param sessionId Session Id of the chat session for a chat message, or a file transfer which occurred during a chat session.
	 * 		  If the file transfer didn't occur during a chat session, sessionId is an auto generated Id for a unique file transfer chat session 
	 * 		  (basically the same as the file transfer sessionId).
	 *        It can also be a sessionId for a LargeIM session.
	 * @param messageId Session Id of a file transfer or message Id of a chat message 
	 * @param contact Contact number
	 * @param data Content of the message (could be an Uri in FT, or a simple text in IM) obviously null if INCOMING
	 * @param mimeType Mime type of the file transfer
	 * @param name Name of the transfered file, or null if this is a IM message
	 * @param size Size of the  transfered file
	 * @param status Status of the message or the session
	 */
	private Uri addMessage(int discriminator, String sessionId, String messageId, String contact, String data, String mimeType, String name, long size, Date date, int status) {
		if(logger.isActivated()){
			logger.debug("Adding message entry in provider type : "+ discriminator +" contact : "+contact+ " status : "+status);
		}
		contact = PhoneUtils.extractNumberFromUri(contact);
		ContentValues values = new ContentValues();
		values.put(RichMessagingData.KEY_TYPE, discriminator);
		values.put(RichMessagingData.KEY_CHAT_SESSION_ID, sessionId);
		values.put(RichMessagingData.KEY_MESSAGE_ID, messageId);
		values.put(RichMessagingData.KEY_CONTACT, contact);
		values.put(RichMessagingData.KEY_MIME_TYPE, mimeType);
		values.put(RichMessagingData.KEY_TOTAL_SIZE, size);
		values.put(RichMessagingData.KEY_NAME, name);
		values.put(RichMessagingData.KEY_DATA, data);
		values.put(RichMessagingData.KEY_STATUS, status);
		values.put(RichMessagingData.KEY_NUMBER_MESSAGES, recycler(contact)+1);
		values.put(RichMessagingData.KEY_IS_SPAM, EventsLogApi.MESSAGE_IS_NOT_SPAM);
		if(date == null) {
			values.put(RichMessagingData.KEY_TIMESTAMP, Calendar.getInstance().getTimeInMillis());
		} else {
			values.put(RichMessagingData.KEY_TIMESTAMP, date.getTime());
		}
		return cr.insert(databaseUri, values);
	}

	private int recycler(String contact){
		/* Get first and last message dates for the contact */
		Cursor extrem = cr.query(databaseUri, 
				new String[]{"min("+RichMessagingData.KEY_TIMESTAMP+")", "max("+RichMessagingData.KEY_TIMESTAMP+")"}, 
				RichMessagingData.KEY_CONTACT +" = \'"+contact+"\'", 
				null, 
				null);
		long minDate = -1 ,maxDate = -1;
		if(extrem.moveToFirst()){
			minDate = extrem.getLong(0);
			maxDate = extrem.getLong(1);
		}
		extrem.close();
		if(logger.isActivated()){
			logger.debug("Recycler : minDate = "+minDate+" maxDate "+ maxDate);
		}
		
		// If no entry for this contact return 0
		if(minDate == -1 && maxDate == -1){
			return 0;
		}
		
		Cursor c = cr.query(databaseUri, 
				new String[] { RichMessagingData.KEY_NUMBER_MESSAGES, RichMessagingData.KEY_CHAT_SESSION_ID,	RichMessagingData.KEY_TIMESTAMP },
				RichMessagingData.KEY_CONTACT + " = \'" + contact + "\'"+
						" AND (" + RichMessagingData.KEY_TIMESTAMP+ " = " + minDate + 
						" OR "+ RichMessagingData.KEY_TIMESTAMP + " = " + maxDate+ ")",
				null, 
				RichMessagingData.KEY_TIMESTAMP + " ASC");
		int numberOfMessagesForContact = 0;
		long dateForLastMessage = 0;
		if(c.moveToLast()){
			numberOfMessagesForContact = c.getInt(0);
			if(logger.isActivated()){
				logger.debug("Recycler : number of messages for this contact = "+numberOfMessagesForContact);
			}
			if(numberOfMessagesForContact < RichMessagingData.MAX_ENTRIES_PER_CONTACT){
				// Enough place for another message... do nothing return
				if(logger.isActivated()){
					logger.debug("Recycler : Enough place for another message, do nothing return");
				}
				c.close();
				return numberOfMessagesForContact;
			}
			if(logger.isActivated()){
				logger.debug("Recycler : Not enough place for another message, we will have to remove something");
			}
			// Not enough place for another message... we will have to remove something
			dateForLastMessage = c.getLong(2);
			if(logger.isActivated()){
				logger.debug("Recycler : dateForLastMessage ="+new Date(dateForLastMessage).toString()+" ["+dateForLastMessage+"]");
			}
		}
		int removedMessages = 0;
		if(c.moveToFirst()){
			// Remove the first message and all the associated messages from its session
			String sessionId = c.getString(1);
			long firstDate = c.getLong(2);
			if(logger.isActivated()){
				logger.debug("Recycler : deleting entries for (the first) sessionID : "+sessionId + " for the date : "+new Date(firstDate).toString()+" ["+firstDate+"]");
			}
			removedMessages = cr.delete(databaseUri, 
					RichMessagingData.KEY_CHAT_SESSION_ID + " = \'" + sessionId+ "\'", 
					null);
			if(logger.isActivated()){
				logger.debug("Recycler : messages removed : "+removedMessages);
			}
			
			// We also will have to set the new number of message after removing, for the last entry
			if(logger.isActivated()){
				logger.debug("Recycler : set the new number of messages after removing...");
			}
			ContentValues values = new ContentValues();
			numberOfMessagesForContact -= removedMessages;
			if(logger.isActivated()){
				logger.debug("Recycler : new number of message after deletion : "+numberOfMessagesForContact);
			}
			values.put(RichMessagingData.KEY_NUMBER_MESSAGES, numberOfMessagesForContact);
			int updatedRows = cr.update(databaseUri, 
					values, 
					RichMessagingData.KEY_CONTACT +" = \'"+contact+"\' AND "+RichMessagingData.KEY_TIMESTAMP+ " = "+dateForLastMessage, 
					null);
			if(logger.isActivated()){
				logger.debug("Recycler : updated rows for the contact (must be 1) : "+updatedRows);
			}
		}
		c.close();
		return numberOfMessagesForContact;
	}
	
	/**
	 * Update file transfer status
	 * 
	 * @param sessionId Session Id
	 * @param status New status
	 */
	public void updateFileTransferStatus(String sessionId, int status) {
		ContentValues values = new ContentValues();
		values.put(RichMessagingData.KEY_STATUS, status);
		cr.update(databaseUri, 
				values, 
				RichMessagingData.KEY_MESSAGE_ID + " = " + sessionId, 
				null);
	}
	
	/**
	 * Update last instant message status for sessionId.
	 * 
	 * @param sessionId Session Id
	 * @param status New status
	 */
	public void updateLastMessageStatus(String sessionId, int status) {
		Cursor extrem = cr.query(databaseUri, new String[]{
				"max("+RichMessagingData.KEY_TIMESTAMP+")"}, 
				RichMessagingData.KEY_CHAT_SESSION_ID +" = "+sessionId+"", null, null);
		long maxDate = -1;
		if(extrem.moveToFirst()){
			maxDate = extrem.getLong(0);
		}
		extrem.close();
		ContentValues values = new ContentValues();
		values.put(RichMessagingData.KEY_STATUS, status);
		int i = cr.update(databaseUri, 
				values, 
				RichMessagingData.KEY_CHAT_SESSION_ID + " = " + sessionId + " AND " + RichMessagingData.KEY_TIMESTAMP+ " = " + maxDate, 
				null);
		if (logger.isActivated()){
			logger.debug("Updated Rows : "+i);
		}
	}
	

	/**
	 * Update file transfer downloaded size, also set the transfer status to 'IN_PROGRESS'
	 * 
	 * @param sessionId Session Id
	 * @param size New downloaded size
	 * @param totalSize 
	 */
	public void updateFileTransferDownloadedSize(String sessionId, long size, long totalSize) {
		ContentValues values = new ContentValues();
		
		Cursor cursor = cr.query(RichMessagingData.CONTENT_URI, 
				new String[]{RichMessagingData.KEY_SIZE}, 
				RichMessagingData.KEY_MESSAGE_ID + "='" + sessionId + "'",
				null, 
				null);
		if (cursor.moveToFirst()) {
			long downloadedSize = cursor.getLong(cursor.getColumnIndexOrThrow(RichMessagingData.KEY_SIZE));
			if ((size >= downloadedSize + totalSize / 10) || size == totalSize)  {
				// Update size if we have done at least 10 more percent from total size since last update
				// Or if we are at the end of the download (ensure we update when transfer is finished)
				// This is to avoid too much updates, as the ui refreshes each time
				values.put(RichMessagingData.KEY_SIZE, size);
				values.put(RichMessagingData.KEY_STATUS, EventsLogApi.STATUS_IN_PROGRESS);
				cr.update(
						databaseUri,
						values,
						RichMessagingData.KEY_MESSAGE_ID + " = " + sessionId,
						null);
			}
		}
		cursor.close();
	}

	/**
	 * Update file transfer url, this also means that the transfer is finished.
	 * 
	 * @param sessionId Session Id
	 * @param data File url
	 */
	public void updateFileTransferUrl(String sessionId, String data) {
		ContentValues values = new ContentValues();
		values.put(RichMessagingData.KEY_DATA, data);
		values.put(RichMessagingData.KEY_STATUS, EventsLogApi.STATUS_TERMINATED);
		cr.update(databaseUri, 
				values, 
				RichMessagingData.KEY_MESSAGE_ID + " = " + sessionId, 
				null);
	}
	
	/**
	 * Update Last instant message data for sessionId.
	 * 
	 * @param sessionId Session Id
	 * @param data IM text
	 */
	public void updateLastMessage(String sessionId, String data) {
		Cursor extrem = cr.query(databaseUri, 
				new String[]{"max("+RichMessagingData.KEY_TIMESTAMP+")"}, 
				RichMessagingData.KEY_CHAT_SESSION_ID +" = "+sessionId+"", 
				null, 
				null);
		long maxDate = -1;
		if(extrem.moveToFirst()){
			maxDate = extrem.getLong(0);
		}
		extrem.close();
		ContentValues values = new ContentValues();
		values.put(RichMessagingData.KEY_DATA, data);
		values.put(RichMessagingData.KEY_STATUS, EventsLogApi.STATUS_TERMINATED);
		cr.update(databaseUri,
				values, 
				RichMessagingData.KEY_CHAT_SESSION_ID + " = " + sessionId+ " AND " +	RichMessagingData.KEY_TIMESTAMP+ " = " + maxDate, 
				null);
	}
	
	/**
	 * Delete a file transfer
	 * 
	 * @param sessionId file transfer Session Id (should be unique)
	 */
	public void deleteFileTransferSession(String ftSessionId, String contact) {
		/* Count entries to be deleted */
		Cursor count = cr.query(databaseUri, null, RichMessagingData.KEY_MESSAGE_ID + " = " + ftSessionId, null, null);
		int toBeDeletedRows = count.getCount();
		if(logger.isActivated()){
			logger.debug("DeleteSession: rows to be deleted : "+toBeDeletedRows);
		}	
		count.close();
		if(toBeDeletedRows==0){
			return;
		}
		
		// Manage recycling
		Cursor c  = cr.query(databaseUri, 
				new String[]{RichMessagingData.KEY_TIMESTAMP, RichMessagingData.KEY_NUMBER_MESSAGES, RichMessagingData.KEY_MESSAGE_ID}, 
				RichMessagingData.KEY_CONTACT+" = \'"+contact+"\'", 
				null, 
				RichMessagingData.KEY_TIMESTAMP + " DESC");
		if(c.moveToFirst()){
			long maxDate = c.getLong(0);
			int numberForLast = c.getInt(1);
			String lastSessionId = c.getString(2);

			ContentValues values = new ContentValues();
			values.put(RichMessagingData.KEY_NUMBER_MESSAGES, numberForLast-toBeDeletedRows);
			// If last entry for this contact equals to this file transfer message
			if(ftSessionId.equals(lastSessionId)){
				// Update the previous one
				if(c.moveToNext()){
					maxDate = c.getLong(0);
				}
			}
			/*
			 * TODO : 
			 * If no more message exists after deleting this one for this contact,
			 * the update is useless because it will be made on the message to be deleted.
			 */
			int updatedRows = cr.update(databaseUri, 
					values, 
					RichMessagingData.KEY_TIMESTAMP+ " = "+maxDate+" AND "+RichMessagingData.KEY_CONTACT+" = \'"+contact+"\'", 
					null);
			if(logger.isActivated()){
				logger.debug("DeleteFileTransfer : recycling updated rows (should be 1) : "+updatedRows);
			}
		}
		c.close();
		
		/* Delete entry */
		int deletedRows = cr.delete(databaseUri, 
				RichMessagingData.KEY_MESSAGE_ID + " = " + ftSessionId, 
				null);
		if(logger.isActivated()){
			logger.debug("DeleteFileTransfer : deleted rows (should be 1) : "+deletedRows);
		}
	}
	
	/**
	 * Delete the chat and file transfer log associated to a contact
	 * 
	 * @param contact Contact
	 */
	public void deleteMessagingLogForContact(String contact) {
		String excludeGroupChat = RichMessagingData.KEY_TYPE + "<>" + EventsLogApi.TYPE_GROUP_CHAT_SYSTEM_MESSAGE + " AND " +
			RichMessagingData.KEY_TYPE + "<>" + EventsLogApi.TYPE_INCOMING_GROUP_CHAT_MESSAGE + " AND " +
			RichMessagingData.KEY_TYPE + "<>" + EventsLogApi.TYPE_OUTGOING_GROUP_CHAT_MESSAGE;
		
		
		/* Delete entries */
		int deletedRows = cr.delete(databaseUri, 
				RichMessagingData.KEY_CONTACT + " = \'" + contact + "\' AND " + excludeGroupChat, 
				null);
		if(logger.isActivated()){
			logger.debug("DeleteSession: deleted rows : "+deletedRows);
		}	
	}
	
	/**
	 * Delete an Im session
	 * If file transfer is enclosed in the session, it will also be deleted.
	 * 
	 * @param sessionId Im Session Id
	 */
	public void deleteImSession(String sessionId) {
		// Count entries to be deleted
		Cursor count = cr.query(databaseUri, 
				null, 
				RichMessagingData.KEY_CHAT_SESSION_ID + " = " + sessionId, 
				null, 
				null);
		int toBeDeletedRows = count.getCount();
		
		String contact = null;
		
		boolean isChatGroup = false;
		if (count.moveToFirst()){
			contact = count.getString(count.getColumnIndex(RichMessagingData.KEY_CONTACT));
			int type = count.getInt(count.getColumnIndex(RichMessagingData.KEY_TYPE));
			if (type>=EventsLogApi.TYPE_GROUP_CHAT_SYSTEM_MESSAGE && type <=EventsLogApi.TYPE_OUTGOING_GROUP_CHAT_MESSAGE){
				isChatGroup = true;
			}
		}
		if (logger.isActivated()){
			logger.debug("DeleteSession: rows to be deleted : "+toBeDeletedRows);
		}	
		count.close();
		if (toBeDeletedRows == 0){
			return;
		}
		
		if (!isChatGroup){
			// Manage recycling
			Cursor c  = cr.query(databaseUri, new String[]{
					RichMessagingData.KEY_TIMESTAMP,
					RichMessagingData.KEY_NUMBER_MESSAGES, 
					RichMessagingData.KEY_CHAT_SESSION_ID}, 
					RichMessagingData.KEY_CONTACT+" = \'"+contact+"\'", 
					null, 
					RichMessagingData.KEY_TIMESTAMP + " DESC");
			if(c.moveToFirst()){
				long maxDate = c.getLong(0);
				int numberForLast = c.getInt(1);
				String lastSessionId = c.getString(2);
				ContentValues values = new ContentValues();
				values.put(RichMessagingData.KEY_NUMBER_MESSAGES, numberForLast-toBeDeletedRows);

				if(sessionId.equals(lastSessionId)){
					// Find the last message from another session for the same contact
					if(logger.isActivated()){
						logger.debug("DeleteSession : the deleted session is the last one... looking for the previous one for this contact");
					}
					while(c.moveToNext()){
						if(!sessionId.equals(c.getString(2))){
							maxDate = c.getLong(0);
							if(logger.isActivated()){
								logger.debug("DeleteSession : find the previous session with date "+maxDate);
							}
							break;
						}
					}
				}
				if(logger.isActivated()){
					logger.debug("DeleteSession : updating the row of date "+maxDate);
				}
				/*
				 * TODO : 
				 * If no more session exists after deleting this one for this contact,
				 * the update is useless because it will be made on the session to be deleted.
				 */
				int updatedRows = cr.update(databaseUri, 
						values, 
						RichMessagingData.KEY_TIMESTAMP+ " = "+maxDate +" AND "+RichMessagingData.KEY_CONTACT+" = \'"+contact+"\'", 
						null);
				if(logger.isActivated()){
					logger.debug("DeleteSession : recycling updated rows (should be 1) : "+updatedRows);
				}
			}
			c.close();
		}
		
		/* Delete entry */
		int deletedRows = cr.delete(databaseUri, 
				RichMessagingData.KEY_CHAT_SESSION_ID + " = " + sessionId, 
				null);
		if(logger.isActivated()){
			logger.debug("DeleteSession: deleted rows : "+deletedRows);
		}	
	}
	
	/**
	 * Clear the im history of a contact
	 * @param contact the contact
	 */
	public void clearHistory(String contact){
		int deletedRows = cr.delete(databaseUri, RichMessagingData.KEY_CONTACT+"='"+contact+"'", null);
		if (logger.isActivated()) {
			logger.debug("Clear history of contact"+contact+", deleted rows : " + deletedRows);
		}
	}
	
	/**
	 * Delete an Chat message or a File transfer from its row id in messaging table. 
	 * @param rowId of the message
	 */
	public void deleteImItem(long rowId) {
		/* TODO: must be 1, otherwise the parameters are not valid...
		 * 
		 * Count entries to be deleted, should be 1*/
		
		Cursor count = cr.query(Uri.withAppendedPath(databaseUri, ""+rowId),
				null,
				null,
				null,
				null);
		if(count.getCount()==0){
			count.close();
			return;
		}
		count.moveToFirst();
		String contactNumber = count.getString(count.getColumnIndexOrThrow((RichMessagingData.KEY_CONTACT)));
		// Manage recycling
		Cursor c  = cr.query(databaseUri, new String[]{
				RichMessagingData.KEY_TIMESTAMP,
				RichMessagingData.KEY_NUMBER_MESSAGES, 
				RichMessagingData.KEY_CHAT_SESSION_ID, 
				RichMessagingData.KEY_DATA}, 
				RichMessagingData.KEY_CONTACT + " = \'"+contactNumber + "\'", 
				null, 
				RichMessagingData.KEY_TIMESTAMP + " DESC");
		
		// Get the first last entry for this contact
		if(c.moveToFirst()){
			long maxDate = c.getLong(0);
			int numberForLast = c.getInt(1);
			String lastSessionId = c.getString(2);
			
			/* We are going to delete one message */
			ContentValues values = new ContentValues();
			values.put(RichMessagingData.KEY_NUMBER_MESSAGES, numberForLast-1);
			
			/* Check if this message has the same sessionID, timestamp and content as the one to be deleted */
			String sessionId = count.getString(count.getColumnIndexOrThrow(RichMessagingData.KEY_CHAT_SESSION_ID));
			long date = count.getLong(count.getColumnIndexOrThrow(RichMessagingData.KEY_TIMESTAMP));
			String message = count.getString(count.getColumnIndexOrThrow(RichMessagingData.KEY_DATA));
			if(sessionId.equals(lastSessionId) && (date == maxDate) && message.equals(c.getString(3))){
				/* It's the lastest message for this contact, 
				 * find the previous message for the same contact */
				if(logger.isActivated()){
					logger.debug("DeleteSession : the deleted message is the last one... looking for the previous one for this contact");
				}
				/* Find the date of the previous message */
				if(c.moveToNext()){
					maxDate = c.getLong(0);
					if (logger.isActivated()) {
						logger.debug("DeleteSession : find the previous message with date "+ maxDate);
					}
				}
			}
			if(logger.isActivated()){
				logger.debug("DeleteSession : updating the row of date "+maxDate);
			}
			/* Update the first message or the previous one with the new number of message for this contact. 
			 * 
			 * TODO :
			 * If the first message is the message to be deleted and no more messages are available for the same contact, 
			 * then the update is useless because it will be made on the message to be deleted.
			 */
			int updatedRows = cr.update(databaseUri, values, 
					RichMessagingData.KEY_TIMESTAMP+ " = "+maxDate
					+" AND "+RichMessagingData.KEY_CONTACT+" = \'"
					+contactNumber
					+"\'", 
					null);
			if(logger.isActivated()){
				logger.debug("DeleteSession : recycling updated rows (should be 1) : "+updatedRows);
			}
		}
		count.close();
		c.close();
		
		/* Delete entry */
		int deletedRows = cr.delete(Uri.withAppendedPath(
				databaseUri, ""+rowId), 
				null, 
				null);
		if(logger.isActivated()){
			logger.debug("DeleteSession: deleted rows : "+deletedRows);
		}	
	}
	
	/**
	 * Check if the IM session is already terminated.
	 * @param sessionId
	 * @return
	 */
	public boolean isSessionTerminated(String sessionId){
		Cursor cursor = cr.query(databaseUri, 
				new String[]{RichMessagingData.KEY_STATUS}, 
				RichMessagingData.KEY_CHAT_SESSION_ID + " = " + sessionId, 
				null, 
				RichMessagingData.KEY_TIMESTAMP + " DESC");
		if(cursor.moveToFirst()){
			int status = cursor.getInt(0);
			if(status==EventsLogApi.STATUS_TERMINATED){
				cursor.close();
				return true;
			}
		}
		cursor.close();
		return false;
	}
}
