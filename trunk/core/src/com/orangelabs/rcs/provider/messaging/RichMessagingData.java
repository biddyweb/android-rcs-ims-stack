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

import android.net.Uri;

/**
 * Rich messaging data constants
 * 
 * @author jexa7410
 */
public class RichMessagingData {
	// Database URI
	public static final Uri CONTENT_URI = Uri.parse("content://com.orangelabs.rcs.messaging/filetransfer");
	
	// Column names
	public static final String KEY_ID = "_id";
	public static final String KEY_SESSION_ID = "session_id";
	public static final String KEY_CONTACT_NUMBER = "contact_number";
	public static final String KEY_DESTINATION = "destination";
	public static final String KEY_NAME = "name";
	public static final String KEY_SIZE = "size";
	public static final String KEY_MIME_TYPE = "mime_type";
	public static final String KEY_MEDIA_URI = "media_uri";
	public static final String KEY_TRANSFER_STATUS = "transfer_status";
	public static final String KEY_TRANSFER_DATE = "date";
	public static final String KEY_TYPE_DISCRIMINATOR = "transport_type";
	public static final String KEY_ADDRESS = "address";
	public static final String KEY_DOWNLOADED_SIZE = "downloaded_size";
	// Column indexes
	public static final int COLUMN_SESSION_ID = 1;
	public static final int COLUMN_CONTACT_NUMBER = 2;
	public static final int COLUMN_DESTINATION = 3;
	public static final int COLUMN_NAME = 4;
	public static final int COLUMN_SIZE = 5;
	public static final int COLUMN_MIME_TYPE = 6;
	public static final int COLUMN_MEDIA_URI = 7;
	public static final int COLUMN_TRANSFER_STATUS = 8;
	public static final int COLUMN_TRANSFER_DATE = 9;
	public static final int COLUMN_TYPE_DISCRIMINATOR = 10;
	public static final int COLUMN_ADDRESS = 11;
	public static final int COLUMN_DOWNLOADED_SIZE = 12;
	
	// Values
	public static final int OUTGOING = 4;
	public static final int INCOMING = 1;
	public static final int FAILED = 5;
	// Corresponds to android.provider.Telephony.TextBasedSmsColumns values
}
