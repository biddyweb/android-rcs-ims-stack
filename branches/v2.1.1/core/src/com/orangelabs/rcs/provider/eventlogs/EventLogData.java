package com.orangelabs.rcs.provider.eventlogs;

import android.net.Uri;
import android.provider.CallLog.Calls;

/**
 * Event log data constants
 * 
 * @author mhsm6403
 */
public class EventLogData {
	
	// Virtual Database URI
	public static final Uri CONTENT_URI = Uri.parse("content://com.orangelabs.rcs.eventlogs/");

	/**
	 * Each mode below is valued according to the binary representation of a variable representing the selected mode.
	 * *****************************
	 * Bit representation of the selected mode variable value :
	 * sms/mms 			(SMS) 	= bit0
	 * calls 			(CALL)	= bit1
	 * File Transfer	(FT) 	= bit2
	 * Chat				(CHAT)	= bit3
	 * ContentSharing 	(RC) 	= bit4
	 * *****************************
	 * For exemple if selected modes are Chat and File Transfer, the mode value will be 01100 => 12 which is MODE_CHAT_FT
	 */
	public static final int MODE_RC_CHAT_FT_CALL_SMS = 31;
	public static final int MODE_RC_CHAT_FT_CALL = 30;
	public static final int MODE_RC_CHAT_FT_SMS = 29;
	public static final int MODE_RC_CHAT_FT = 28;
	public static final int MODE_RC_CHAT_CALL_SMS = 27;
	public static final int MODE_RC_CHAT_CALL = 26;
	public static final int MODE_RC_CHAT_SMS = 25;
	public static final int MODE_RC_CHAT = 24;
	public static final int MODE_RC_FT_CALL_SMS = 23;
	public static final int MODE_RC_FT_CALL = 22;
	public static final int MODE_RC_FT_SMS = 21;
	public static final int MODE_RC_FT = 20;
	public static final int MODE_RC_CALL_SMS = 19;
	public static final int MODE_RC_CALL = 18;
	public static final int MODE_RC_SMS = 17;
	public static final int MODE_RC = 16;
	public static final int MODE_CHAT_FT_CALL_SMS = 15;
	public static final int MODE_CHAT_FT_CALL = 14;
	public static final int MODE_CHAT_FT_SMS = 13;
	public static final int MODE_CHAT_FT = 12;
	public static final int MODE_CHAT_CALL_SMS = 11;
	public static final int MODE_CHAT_CALL = 10;
	public static final int MODE_CHAT_SMS = 9;
	public static final int MODE_CHAT = 8;
	public static final int MODE_FT_CALL_SMS = 7;
	public static final int MODE_FT_CALL = 6;
	public static final int MODE_FT_SMS = 5;
	public static final int MODE_FT = 4;
	public static final int MODE_CALL_SMS = 3;
	public static final int MODE_CALL = 2;
	public static final int MODE_SMS = 1;
	public static final int MODE_NONE = 0;
	
	/**
	 * Values representing the type of the event in output result. 
	 */
	public static final int VALUE_EVENT_TYPE_SHORT_IM = 0;
	public static final int VALUE_EVENT_TYPE_LARGE_IM = 1;
	public static final int VALUE_EVENT_TYPE_FILETRANSFER = 2;
	public static final int VALUE_EVENT_TYPE_CHAT = 3;
	public static final int VALUE_EVENT_TYPE_RICH_CALL = 4;
	public static final int VALUE_EVENT_TYPE_CALL = 5;
	public static final int VALUE_EVENT_TYPE_MMS_SMS = 6;
	
	/**
	 * Values representing the direction of the event.
	 * Values are based on Calls table values:
	 * Calls.INCOMING_TYPE, Calls.OUTGOING_TYPE, Calls.MISSED_TYPE
	 */
	public static final int VALUE_EVENT_DEST_INCOMING = Calls.INCOMING_TYPE;
	public static final int VALUE_EVENT_DEST_OUTGOING = Calls.OUTGOING_TYPE;
	public static final int VALUE_EVENT_DEST_MISSED = Calls.MISSED_TYPE;
	
	/**
	 * Values representing the status of the event.
	 */
	public static final int VALUE_EVENT_STATUS_STARTED = 0;
	public static final int VALUE_EVENT_STATUS_TERMINATED = 1;
	public static final int VALUE_EVENT_STATUS_FAILED = 2;
	public static final int VALUE_EVENT_STATUS_IN_PROGRESS = 3;
	public static final int VALUE_EVENT_STATUS_SENT = 4;
	public static final int VALUE_EVENT_STATUS_RECEIVED = 5; 
	
	
	public static final String GSM_MIMETYPE = "call/gsm";
	public static final String SMS_MIMETYPE = "sms/text";
	public static final String MMS_MIMETYPE = "mms/text";
	
	public static Uri SMS_URI = Uri.parse("content://sms");
	public static Uri MMS_URI = Uri.parse("content://mms");
	
	/**
	 * Metadata of the event cursor. 
	 */
	public static final String KEY_EVENT_ROW_ID = "_id";
	public static final String KEY_EVENT_DATE = "event_date";
	public static final String KEY_EVENT_MIMETYPE = "event_mimetype";
	public static final String KEY_EVENT_DATA = "event_data";
	public static final String KEY_EVENT_DESTINATION = "event_dest";
	public static final String KEY_EVENT_STATUS = "event_status";
	public static final String KEY_EVENT_PHONE_NUMBER = "event_number";
	public static final String KEY_EVENT_TYPE = "event_type";
	public static final String KEY_EVENT_SESSION_ID = "event_session_id";
	
	public static final int COLUMN_EVENT_ROW_ID = 0;
	public static final int COLUMN_EVENT_DATE = 1;
	public static final int COLUMN_EVENT_MIMETYPE = 2;
	public static final int COLUMN_EVENT_DATA = 3;
	public static final int COLUMN_EVENT_DESTINATION = 4;
	public static final int COLUMN_EVENT_STATUS = 5;	
	public static final int COLUMN_EVENT_PHONE_NUMBER = 6;
	public static final int COLUMN_EVENT_TYPE = 7;
	public static final int COLUMN_EVENT_SESSION_ID = 8;
}
