/*******************************************************************************
 * Software Name : RCS IMS Stack
 * Version : 2.0.0
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
	 * Add a new file transfer
	 * 
	 * @param sessionId Session Id
	 * @param contact Contact number
	 * @param file Filename of the file to transfer
	 * @param destination Destination of the file transfer
	 * @param mimeType Mime type of the file transfer
	 * @param name Name of the transfered file
	 * @param size Size of the  transfered file
	 */
	public void addFileTransfer(String sessionId, String contact, String file, int destination, String mimeType, String name, long size) {
		ContentValues values = new ContentValues();
		if (destination == RichMessagingData.OUTGOING){
			// User is initiator
			values.put(RichMessagingData.KEY_TRANSFER_STATUS, "inviting");
			values.put(RichMessagingData.KEY_ADDRESS, "Me");
		}else{
			// User is receptor
			values.put(RichMessagingData.KEY_TRANSFER_STATUS, "invited");
			values.put(RichMessagingData.KEY_ADDRESS, contact);
		}		
		values.put(RichMessagingData.KEY_TYPE_DISCRIMINATOR, "file_transfer");
		values.put(RichMessagingData.KEY_SESSION_ID, sessionId);
		values.put(RichMessagingData.KEY_CONTACT_NUMBER, contact);
		values.put(RichMessagingData.KEY_MEDIA_URI, file);
		values.put(RichMessagingData.KEY_DESTINATION, destination);
		values.put(RichMessagingData.KEY_MIME_TYPE, mimeType);
		values.put(RichMessagingData.KEY_SIZE, size);
		values.put(RichMessagingData.KEY_NAME, name);
		values.put(RichMessagingData.KEY_TRANSFER_DATE, Calendar.getInstance().getTimeInMillis());
		cr.insert(databaseUri, values);
	}

	/**
	 * Update file transfer status
	 * 
	 * @param sessionId Session Id
	 * @param status New status
	 */
	public void updateFileTransferStatus(String sessionId, String status) {
		ContentValues values = new ContentValues();
		values.put(RichMessagingData.KEY_TRANSFER_STATUS, status);
		cr.update(databaseUri, values, RichMessagingData.KEY_SESSION_ID + " = " + sessionId, null);
	}

	/**
	 * Update file transfer downloaded size
	 * 
	 * @param sessionId Session Id
	 * @param size New downloaded size
	 */
	public void updateFileTransferDownloadedSize(String sessionId, long size) {
		ContentValues values = new ContentValues();
		values.put(RichMessagingData.KEY_DOWNLOADED_SIZE, size);
		cr.update(databaseUri, values, RichMessagingData.KEY_SESSION_ID + " = " + sessionId, null);
	}

	/**
	 * Update file transfer url
	 * 
	 * @param sessionId Session Id
	 * @param filename File url
	 */
	public void updateFileTransferUrl(String sessionId, String filename) {
		ContentValues values = new ContentValues();
		values.put(RichMessagingData.KEY_MEDIA_URI, filename);
		cr.update(databaseUri, values, RichMessagingData.KEY_SESSION_ID + " = " + sessionId, null);
	}
	
	/**
	 * Delete a file transfer
	 * 
	 * @param sessionId Session Id
	 */
	public void deleteFileTransfer(String sessionId) {
		cr.delete(databaseUri, RichMessagingData.KEY_SESSION_ID + " = " + sessionId, null);
	}
}
