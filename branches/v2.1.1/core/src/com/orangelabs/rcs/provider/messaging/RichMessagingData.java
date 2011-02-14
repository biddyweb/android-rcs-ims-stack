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

import com.orangelabs.rcs.provider.eventlogs.EventLogData;

import android.net.Uri;

/**
 * Rich messaging history data constants
 * 
 * @author mhsm6403
 */
public class RichMessagingData {
	// Database URI
	public static final Uri CONTENT_URI = Uri.parse("content://com.orangelabs.rcs.messaging/messaging");
	
	// Column names
	public static final String KEY_ID = "_id";
	public static final String KEY_TYPE_DISCRIMINATOR = "type_discriminator";
	public static final String KEY_SESSION_ID = "session_id";
	public static final String KEY_FT_SESSION_ID = "ft_session_id";
	public static final String KEY_CONTACT_NUMBER = "contact_number";
	public static final String KEY_DESTINATION = "destination";
	public static final String KEY_MIME_TYPE = "mime_type";
	public static final String KEY_NAME = "name";
	public static final String KEY_SIZE = "size";
	public static final String KEY_DATA = "_data";
	public static final String KEY_TRANSFER_STATUS = "transfer_status";
	public static final String KEY_TRANSFER_DATE = "_date";
	public static final String KEY_DOWNLOADED_SIZE = "downloaded_size";
	public static final String KEY_NUMBER_MESSAGES ="number_of_messages";
	
	// Column indexes
	public static final int COLUMN_KEY_ID = 0;
	public static final int COLUMN_TYPE_DISCRIMINATOR = 1;
	public static final int COLUMN_SESSION_ID = 2;
	public static final int COLUMN_FT_SESSION_ID = 3;
	public static final int COLUMN_CONTACT_NUMBER = 4;
	public static final int COLUMN_DESTINATION = 5;
	public static final int COLUMN_MIME_TYPE = 6;
	public static final int COLUMN_NAME = 7;
	public static final int COLUMN_SIZE = 8;
	public static final int COLUMN_DATA = 9;
	public static final int COLUMN_TRANSFER_STATUS = 10;
	public static final int COLUMN_TRANSFER_DATE = 11;
	public static final int COLUMN_DOWNLOADED_SIZE = 12;
	public static final int COLUMN_NUMBER_MESSAGES = 13;
	
	// Event direction
	public static final int EVENT_OUTGOING = EventLogData.VALUE_EVENT_DEST_OUTGOING;
	public static final int EVENT_INCOMING = EventLogData.VALUE_EVENT_DEST_INCOMING;
	
	// Transfer status values
	public static final int STATUS_STARTED = EventLogData.VALUE_EVENT_STATUS_STARTED;
	public static final int STATUS_TERMINATED = EventLogData.VALUE_EVENT_STATUS_TERMINATED;
	public static final int STATUS_FAILED = EventLogData.VALUE_EVENT_STATUS_FAILED;
	public static final int STATUS_IN_PROGRESS = EventLogData.VALUE_EVENT_STATUS_IN_PROGRESS;
	public static final int STATUS_SENT = EventLogData.VALUE_EVENT_STATUS_SENT;
	public static final int STATUS_RECEIVED = EventLogData.VALUE_EVENT_STATUS_RECEIVED;
	
	// Type discriminator values
	public static final int TYPE_SHORT_IM = EventLogData.VALUE_EVENT_TYPE_SHORT_IM;
	public static final int TYPE_LARGE_IM = EventLogData.VALUE_EVENT_TYPE_LARGE_IM;
	public static final int TYPE_FILETRANSFER = EventLogData.VALUE_EVENT_TYPE_FILETRANSFER;
	public static final int TYPE_CHAT = EventLogData.VALUE_EVENT_TYPE_CHAT;
	
	// The maximum number of entries per contact in the database
	public static final int MAX_ENTRIES_PER_CONTACT = 200;
}
