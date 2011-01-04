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
import android.net.Uri;

import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Rich messaging content provider
 * 
 * @author jexa7410
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
	public void addMessage(int discriminator, String sessionId, String ftSessionId, String contact, String data, int destination, String mimeType, String name, long size, Date date, int status) {
		if(logger.isActivated()){
			logger.error("Adding message entry in provider "+status);
		}
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
		if(date == null) {
			values.put(RichMessagingData.KEY_TRANSFER_DATE, Calendar.getInstance().getTimeInMillis());
		} else {
			values.put(RichMessagingData.KEY_TRANSFER_DATE, date.getTime());
		}
		cr.insert(databaseUri, values);
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
		cr.update(databaseUri, values, RichMessagingData.KEY_FT_SESSION_ID + " = " + sessionId, null);
	}
	
	/**
	 * Update instant message status
	 * 
	 * @param sessionId Session Id
	 * @param status New status
	 */
	public void updateMessageStatus(String sessionId, int status) {
		ContentValues values = new ContentValues();
		values.put(RichMessagingData.KEY_TRANSFER_STATUS, status);
		cr.update(databaseUri, values, RichMessagingData.KEY_SESSION_ID + " = " + sessionId, null);
	}
	

	/**
	 * Update file transfer downloaded size, also set the transfer status to 'IN_PROGRESS'
	 * 
	 * @param sessionId Session Id
	 * @param size New downloaded size
	 */
	public void updateFileTransferDownloadedSize(String sessionId, long size) {
		ContentValues values = new ContentValues();
		values.put(RichMessagingData.KEY_DOWNLOADED_SIZE, size);
		values.put(RichMessagingData.KEY_TRANSFER_STATUS, RichMessagingData.IN_PROGRESS);
		cr.update(databaseUri, values, RichMessagingData.KEY_FT_SESSION_ID + " = " + sessionId, null);
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
		values.put(RichMessagingData.KEY_TRANSFER_STATUS, RichMessagingData.FINISHED);
		cr.update(databaseUri, values, RichMessagingData.KEY_FT_SESSION_ID + " = " + sessionId, null);
	}
	
	/**
	 * Update instant message data, this also means that the message has been received.
	 * 
	 * @param sessionId Session Id
	 * @param data IM text
	 */
	public void updateMessage(String sessionId, String data) {
		ContentValues values = new ContentValues();
		values.put(RichMessagingData.KEY_DATA, data);
		values.put(RichMessagingData.KEY_TRANSFER_STATUS, RichMessagingData.FINISHED);
		cr.update(databaseUri, values, RichMessagingData.KEY_SESSION_ID + " = " + sessionId, null);
	}
	
	/**
	 * Delete a file transfer
	 * 
	 * @param sessionId Session Id
	 */
	public void deleteFileTransfer(String sessionId) {
		cr.delete(databaseUri, RichMessagingData.KEY_FT_SESSION_ID + " = " + sessionId, null);
	}
	
	/**
	 * Delete a instant message
	 * 
	 * @param sessionId Session Id
	 */
	public void deleteMessage(String sessionId) {
		cr.delete(databaseUri, RichMessagingData.KEY_SESSION_ID + " = " + sessionId, null);
	}
}
