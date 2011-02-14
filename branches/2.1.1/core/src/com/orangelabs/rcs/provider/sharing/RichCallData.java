package com.orangelabs.rcs.provider.sharing;

import com.orangelabs.rcs.provider.eventlogs.EventLogData;

import android.net.Uri;

/**
 * Rich call history data constants
 * 
 * @author mhsm6403
 */
public class RichCallData {
	// Database URI
	public static final Uri CONTENT_URI = Uri.parse("content://com.orangelabs.rcs.csh/csh");
	
	// Column names
	public static final String KEY_ID = "_id";
	public static final String KEY_CONTACT_NUMBER = "contact_number";
	public static final String KEY_DESTINATION = "destination";
	public static final String KEY_MIME_TYPE = "mime_type";
	public static final String KEY_NAME = "name";
	public static final String KEY_SIZE = "size";
	public static final String KEY_DATA = "_data";
	public static final String KEY_TRANSFER_DATE = "_date";
	public static final String KEY_NUMBER_MESSAGES ="number_of_messages";
	public static final String KEY_STATUS = "status";
	public static final String KEY_SESSION_ID = "sessionId";
	
	// Column indexes
	public static final int COLUMN_KEY_ID = 0;
	public static final int COLUMN_CONTACT_NUMBER = 1;
	public static final int COLUMN_DESTINATION = 2;
	public static final int COLUMN_MIME_TYPE = 3;
	public static final int COLUMN_NAME = 4;
	public static final int COLUMN_SIZE = 5;
	public static final int COLUMN_DATA = 6;
	public static final int COLUMN_DATE = 7;
	public static final int COLUMN_STATUS = 8;
	public static final int COLUMN_SESSION_ID = 9;
	
	// Event direction
	public static final int EVENT_OUTGOING = EventLogData.VALUE_EVENT_DEST_OUTGOING;
	public static final int EVENT_INCOMING = EventLogData.VALUE_EVENT_DEST_INCOMING;
	
	// Status values
	public static final int STATUS_STARTED = EventLogData.VALUE_EVENT_STATUS_STARTED; 
	public static final int STATUS_TRANSFERED = EventLogData.VALUE_EVENT_STATUS_TERMINATED;
	public static final int STATUS_FAILED = EventLogData.VALUE_EVENT_STATUS_FAILED;
	
	// The maximum number of entries per contact in the database
	public static final int MAX_ENTRIES_PER_CONTACT = 200;
}
