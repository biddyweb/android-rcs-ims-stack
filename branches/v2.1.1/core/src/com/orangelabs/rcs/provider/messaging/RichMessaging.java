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
package com.orangelabs.rcs.provider.messaging;

import java.util.Calendar;
import java.util.Date;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

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
	 * Add a new message (chat message, file transfer, large IM, short IM)
	 * 
	 * @param discriminator The type of message sent (RichData.FILETRANSFER or RichData.INSTANTMESSAGING)
	 * @param sessionId Session Id of the chat session for a chat message, or a file transfer which occurred during a chat session.
	 * 		  If the file transfer didn't occur during a chat session, sessionId is an auto generated Id for a unique file transfer chat session 
	 * 		  (basically the same as the file transfer sessionId).
	 *        It can also be a sessionId for a LargeIM session.
	 * @param ftSessionId Session Id of the File Transfer if this is one, else null 
	 * @param contact Contact number
	 * @param data Content of the message (could be an Uri in FT, or a simple text in IM) obviously null if INCOMING
	 * @param destination Destination of the file transfer (RichData.OUTGOING or RichData.INCOMING)
	 * @param mimeType Mime type of the file transfer
	 * @param name Name of the transfered file, or null if this is a IM message
	 * @param size Size of the  transfered file
	 * @param status Status of the message or the session
	 */
	public Uri addMessage(int discriminator, String sessionId, String ftSessionId, String contact, String data, int destination, String mimeType, String name, long size, Date date, int status) {
		if(logger.isActivated()){
			logger.debug("Adding message entry in provider contact : "+contact+ " status : "+status);
		}
		contact = PhoneUtils.extractNumberFromUri(contact);
		ContentValues values = new ContentValues();
		values.put(RichMessagingData.KEY_TYPE_DISCRIMINATOR, discriminator);
		values.put(RichMessagingData.KEY_SESSION_ID, sessionId);
		values.put(RichMessagingData.KEY_FT_SESSION_ID, ftSessionId);
		values.put(RichMessagingData.KEY_CONTACT_NUMBER, contact);
		values.put(RichMessagingData.KEY_DESTINATION, destination);
		values.put(RichMessagingData.KEY_MIME_TYPE, mimeType);
		values.put(RichMessagingData.KEY_SIZE, size);
		values.put(RichMessagingData.KEY_NAME, name);
		values.put(RichMessagingData.KEY_DATA, data);
		values.put(RichMessagingData.KEY_TRANSFER_STATUS, status);
		values.put(RichMessagingData.KEY_NUMBER_MESSAGES, recycler(contact)+1);
		if(date == null) {
			values.put(RichMessagingData.KEY_TRANSFER_DATE, Calendar.getInstance().getTimeInMillis());
		} else {
			values.put(RichMessagingData.KEY_TRANSFER_DATE, date.getTime());
		}
		return cr.insert(databaseUri, values);
	}

	private int recycler(String contact){
		/* Get first and last message dates for the contact */
		Cursor extrem = cr.query(databaseUri, 
				new String[]{"min("+RichMessagingData.KEY_TRANSFER_DATE+")", "max("+RichMessagingData.KEY_TRANSFER_DATE+")"}, 
				RichMessagingData.KEY_CONTACT_NUMBER +" = \'"+contact+"\'", 
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
				new String[] { RichMessagingData.KEY_NUMBER_MESSAGES, RichMessagingData.KEY_SESSION_ID,	RichMessagingData.KEY_TRANSFER_DATE },
				RichMessagingData.KEY_CONTACT_NUMBER + " = \'" + contact + "\'"+
						" AND (" + RichMessagingData.KEY_TRANSFER_DATE+ " = " + minDate + 
						" OR "+ RichMessagingData.KEY_TRANSFER_DATE + " = " + maxDate+ ")",
				null, 
				RichMessagingData.KEY_TRANSFER_DATE + " ASC");
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
					RichMessagingData.KEY_SESSION_ID + " = \'" + sessionId+ "\'", 
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
					RichMessagingData.KEY_CONTACT_NUMBER +" = \'"+contact+"\' AND "+RichMessagingData.KEY_TRANSFER_DATE+ " = "+dateForLastMessage, 
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
		values.put(RichMessagingData.KEY_TRANSFER_STATUS, status);
		cr.update(databaseUri, 
				values, 
				RichMessagingData.KEY_FT_SESSION_ID + " = " + sessionId, 
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
				"max("+RichMessagingData.KEY_TRANSFER_DATE+")"}, 
				RichMessagingData.KEY_SESSION_ID +" = "+sessionId+"", null, null);
		long maxDate = -1;
		if(extrem.moveToFirst()){
			maxDate = extrem.getLong(0);
		}
		extrem.close();
		ContentValues values = new ContentValues();
		values.put(RichMessagingData.KEY_TRANSFER_STATUS, status);
		int i = cr.update(databaseUri, 
				values, 
				RichMessagingData.KEY_SESSION_ID + " = " + sessionId + " AND " + RichMessagingData.KEY_TRANSFER_DATE+ " = " + maxDate, 
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
				new String[]{RichMessagingData.KEY_DOWNLOADED_SIZE}, 
				RichMessagingData.KEY_FT_SESSION_ID + "='" + sessionId + "'",
				null, 
				null);
		if (cursor.moveToFirst()) {
			long downloadedSize = cursor.getLong(cursor.getColumnIndexOrThrow(RichMessagingData.KEY_DOWNLOADED_SIZE));
			if ((size >= downloadedSize + totalSize / 10) || size == totalSize)  {
				// Update size if we have done at least 10 more percent from total size since last update
				// Or if we are at the end of the download (ensure we update when transfer is finished)
				// This is to avoid too much updates, as the ui refreshes each time
				values.put(RichMessagingData.KEY_DOWNLOADED_SIZE, size);
				values.put(RichMessagingData.KEY_TRANSFER_STATUS, RichMessagingData.STATUS_IN_PROGRESS);
				cr.update(
						databaseUri,
						values,
						RichMessagingData.KEY_FT_SESSION_ID + " = " + sessionId,
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
		values.put(RichMessagingData.KEY_TRANSFER_STATUS, RichMessagingData.STATUS_TERMINATED);
		cr.update(databaseUri, 
				values, 
				RichMessagingData.KEY_FT_SESSION_ID + " = " + sessionId, 
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
				new String[]{"max("+RichMessagingData.KEY_TRANSFER_DATE+")"}, 
				RichMessagingData.KEY_SESSION_ID +" = "+sessionId+"", 
				null, 
				null);
		long maxDate = -1;
		if(extrem.moveToFirst()){
			maxDate = extrem.getLong(0);
		}
		extrem.close();
		ContentValues values = new ContentValues();
		values.put(RichMessagingData.KEY_DATA, data);
		values.put(RichMessagingData.KEY_TRANSFER_STATUS, RichMessagingData.STATUS_TERMINATED);
		cr.update(databaseUri,
				values, 
				RichMessagingData.KEY_SESSION_ID + " = " + sessionId+ " AND " +	RichMessagingData.KEY_TRANSFER_DATE+ " = " + maxDate, 
				null);
	}
	
	/**
	 * Delete a file transfer
	 * 
	 * @param sessionId file transfer Session Id (should be unique)
	 */
	public void deleteFileTransferSession(String ftSessionId, String contact) {
		/* Count entries to be deleted */
		Cursor count = cr.query(databaseUri, null, RichMessagingData.KEY_FT_SESSION_ID + " = " + ftSessionId, null, null);
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
				new String[]{RichMessagingData.KEY_TRANSFER_DATE, RichMessagingData.KEY_NUMBER_MESSAGES, RichMessagingData.KEY_FT_SESSION_ID}, 
				RichMessagingData.KEY_CONTACT_NUMBER+" = \'"+contact+"\'", 
				null, 
				RichMessagingData.KEY_TRANSFER_DATE + " DESC");
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
					RichMessagingData.KEY_TRANSFER_DATE+ " = "+maxDate+" AND "+RichMessagingData.KEY_CONTACT_NUMBER+" = \'"+contact+"\'", 
					null);
			if(logger.isActivated()){
				logger.debug("DeleteFileTransfer : recycling updated rows (should be 1) : "+updatedRows);
			}
		}
		c.close();
		
		/* Delete entry */
		int deletedRows = cr.delete(databaseUri, 
				RichMessagingData.KEY_FT_SESSION_ID + " = " + ftSessionId, 
				null);
		if(logger.isActivated()){
			logger.debug("DeleteFileTransfer : deleted rows (should be 1) : "+deletedRows);
		}
	}
	
	/**
	 * Delete an Im session
	 * If file transfer is enclosed in the session, it will also be deleted.
	 * 
	 * @param sessionId Im Session Id
	 * @param contact
	 */
	public void deleteImSession(String sessionId, String contact) {
		// Count entries to be deleted
		Cursor count = cr.query(databaseUri, 
				null, 
				RichMessagingData.KEY_SESSION_ID + " = " + sessionId, 
				null, 
				null);
		int toBeDeletedRows = count.getCount();
		if (logger.isActivated()){
			logger.debug("DeleteSession: rows to be deleted : "+toBeDeletedRows);
		}	
		count.close();
		if (toBeDeletedRows == 0){
			return;
		}
		
		// Manage recycling
		Cursor c  = cr.query(databaseUri, new String[]{
				RichMessagingData.KEY_TRANSFER_DATE,
				RichMessagingData.KEY_NUMBER_MESSAGES, 
				RichMessagingData.KEY_SESSION_ID}, 
				RichMessagingData.KEY_CONTACT_NUMBER+" = \'"+contact+"\'", 
				null, 
				RichMessagingData.KEY_TRANSFER_DATE + " DESC");
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
					RichMessagingData.KEY_TRANSFER_DATE+ " = "+maxDate +" AND "+RichMessagingData.KEY_CONTACT_NUMBER+" = \'"+contact+"\'", 
					null);
			if(logger.isActivated()){
				logger.debug("DeleteSession : recycling updated rows (should be 1) : "+updatedRows);
			}
		}
		c.close();
		
		/* Delete entry */
		int deletedRows = cr.delete(databaseUri, 
				RichMessagingData.KEY_SESSION_ID + " = " + sessionId, 
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
		int deletedRows = cr.delete(databaseUri, RichMessagingData.KEY_CONTACT_NUMBER+"='"+contact+"'", null);
		if (logger.isActivated()) {
			logger.debug("Clear history of contact"+contact+", deleted rows : " + deletedRows);
		}
	}
	
	/**
	 * 
	 * @param sessionId
	 * @param contact
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
		String contactNumber = count.getString(count.getColumnIndexOrThrow((RichMessagingData.KEY_CONTACT_NUMBER)));
		// Manage recycling
		Cursor c  = cr.query(databaseUri, new String[]{
				RichMessagingData.KEY_TRANSFER_DATE,
				RichMessagingData.KEY_NUMBER_MESSAGES, 
				RichMessagingData.KEY_SESSION_ID, 
				RichMessagingData.KEY_DATA}, 
				RichMessagingData.KEY_CONTACT_NUMBER + " = \'"+contactNumber + "\'", 
				null, 
				RichMessagingData.KEY_TRANSFER_DATE + " DESC");
		
		// Get the first last entry for this contact
		if(c.moveToFirst()){
			long maxDate = c.getLong(0);
			int numberForLast = c.getInt(1);
			String lastSessionId = c.getString(2);
			
			/* We are going to delete one message */
			ContentValues values = new ContentValues();
			values.put(RichMessagingData.KEY_NUMBER_MESSAGES, numberForLast-1);
			
			/* Check if this message has the same sessionID, timestamp and content as the one to be deleted */
			String sessionId = count.getString(count.getColumnIndexOrThrow(RichMessagingData.KEY_SESSION_ID));
			long date = count.getLong(count.getColumnIndexOrThrow(RichMessagingData.KEY_TRANSFER_DATE));
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
					RichMessagingData.KEY_TRANSFER_DATE+ " = "+maxDate
					+" AND "+RichMessagingData.KEY_CONTACT_NUMBER+" = \'"
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
				new String[]{RichMessagingData.KEY_TRANSFER_STATUS}, 
				RichMessagingData.KEY_SESSION_ID + " = " + sessionId, 
				null, 
				RichMessagingData.KEY_TRANSFER_DATE + " DESC");
		if(cursor.moveToFirst()){
			int status = cursor.getInt(0);
			if(status==RichMessagingData.STATUS_TERMINATED){
				cursor.close();
				return true;
			}
		}
		cursor.close();
		return false;
	}
}
