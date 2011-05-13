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

package com.orangelabs.rcs.service.api.client.eventslog;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog.Calls;

import com.orangelabs.rcs.provider.eventlogs.EventLogData;
import com.orangelabs.rcs.provider.messaging.RichMessaging;
import com.orangelabs.rcs.provider.messaging.RichMessagingData;
import com.orangelabs.rcs.provider.sharing.RichCall;
import com.orangelabs.rcs.service.api.client.ClientApi;
import com.orangelabs.rcs.utils.PhoneUtils;

/**
 * Events log API
 */
public class EventsLogApi extends ClientApi {

	/**
	 * Row id in provider
	 */
	public static final int ID_COLUMN = 0;
	
	/**
	 * Entry type
	 */
	public static final int TYPE_COLUMN = 1;

	/**
	 * Id of the chat session 
	 */
	public static final int CHAT_SESSION_ID_COLUMN = 2;
	
	/**
	 * Timestamp for this entry
	 */
	public static final int DATE_COLUMN = 3;
	
	/**
	 * Contact this entry refers to
	 */
	public static final int CONTACT_COLUMN = 4;
	
	/**
	 * Status of this entry
	 */
	public static final int STATUS_COLUMN = 5;
	
	/**
	 * Entry data
	 * 
	 * <br>Holds text for chat/SMS messages, path to the file for file transfers/richcalls, duration for calls 
	 */
	public static final int DATA_COLUMN = 6;
	
	/**
	 * Message Id
	 * 
	 * <br>Holds IMDN id for chat messages or file transfer session id for file transfers
	 */
	public static final int MESSAGE_ID_COLUMN = 7;

	/**
	 * Mime-type of the content
	 * 
	 * <br>Only relevant for file transfer
	 */
	public static final int MIMETYPE_COLUMN = 8;
	
	/**
	 * Name of the content
	 * 
	 * <br>Only relevant for file transfer
	 */
	public static final int NAME_COLUMN = 9;
	
	/**
	 * Size already transfered
	 * 
	 * <br>Only relevant for file transfer or rich call
	 */
	public static final int SIZE_COLUMN = 10;
	
	/**
	 * Total size of the file
	 * 	 
	 * <br>Only relevant for file transfer or rich call
	 */
	public static final int TOTAL_SIZE_COLUMN = 11;
	
	/**
	 * List of contacts that have sent a "delivered" IMDN notification
	 * 	 
	 * <br>Only relevant for IM messages when IMDN is activated
	 */
	public static final int IMDN_DELIVERED_COLUMN = 12;
	
	/**
	 * List of contacts that have sent a "displayed" IMDN notification
	 * 	 
	 * <br>Only relevant for IM messages when IMDN is activated
	 */
	public static final int IMDN_DISPLAYED_COLUMN = 13;
	
	// Entry types
	// One to one chat
	public static final int TYPE_INCOMING_CHAT_MESSAGE = 0;
	public static final int TYPE_OUTGOING_CHAT_MESSAGE = 1;
	public static final int TYPE_CHAT_SYSTEM_MESSAGE = 2;
	// Group chat
	public static final int TYPE_INCOMING_GROUP_CHAT_MESSAGE = 3;
	public static final int TYPE_OUTGOING_GROUP_CHAT_MESSAGE = 4;
	public static final int TYPE_GROUP_CHAT_SYSTEM_MESSAGE = 5;
	// File transfer
	public static final int TYPE_INCOMING_FILE_TRANSFER = 6;
	public static final int TYPE_OUTGOING_FILE_TRANSFER = 7;
	// Rich call
	public static final int TYPE_INCOMING_RICH_CALL = 8;
	public static final int TYPE_OUTGOING_RICH_CALL = 9;	
	// SMS
	public static final int TYPE_INCOMING_SMS = 10;			// 
	public static final int TYPE_OUTGOING_SMS = 11;
	// Call
	public static final int TYPE_INCOMING_GSM_CALL = 12; 	// Calls.INCOMING_TYPE = 1, Calls.OUTGOING_TYPE = 2, Calls.MISSED_TYPE = 3
	public static final int TYPE_OUTGOING_GSM_CALL = 13;
	public static final int TYPE_MISSED_GSM_CALL = 14;
	
	// Possible status values
	// Sessions
	public static final int STATUS_STARTED = 0;
	public static final int STATUS_TERMINATED = 1;
	public static final int STATUS_FAILED = 2;
	public static final int STATUS_IN_PROGRESS = 3;
	// Messages
	public static final int STATUS_SENT = 4;
	public static final int STATUS_RECEIVED = 5;
	public static final int STATUS_MISSED = 6;
	// IMDN
	public static final int STATUS_DELIVERED = 7; // sender side
	public static final int STATUS_DISPLAYED = 8; // sender side
	public static final int STATUS_ALL_DISPLAYED = 9; // sender side
	public static final int STATUS_REPORT_REQUESTED = 10; // receiver side : the sender has requested a "displayed" report when the message will be displayed
	public static final int STATUS_REPORTED = 11; // receiver side : the "displayed" report has already been sent
	
	// Possible data for chat system event
	public static final int EVENT_JOINED_CHAT = 12;
	public static final int EVENT_LEFT_CHAT = 13;
	public static final int EVENT_INVITED = 14;		// We were invited
	public static final int EVENT_INITIATED = 15;	// We initiated the chat

	/**
	 * Mode One to one chat
	 */
	public static final int MODE_ONE_TO_ONE_CHAT = 32;

	/**
	 * Mode group chat
	 */
	public static final int MODE_GROUP_CHAT = 33;
	
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
     * Constructor
     * 
     * @param ctx Application context
     */
    public EventsLogApi(Context ctx) {
    	super(ctx);
    }

    /**
     * Clear the history of a given contact
     * 
     * @param contact
     */
    public void clearHistoryForContact(String contact){
    	RichMessaging.getInstance().clearHistory(PhoneUtils.formatNumberToInternational(contact));
    }
    
    /**
     * Delete a given log entry
     * 
     * @param item id
     */
    public void deleteLogEntry(long id){
    	RichMessaging.getInstance().deleteImItem(id);
    }

    /**
     * Delete a SMS entry
     * 
     * @param item id
     */
    public void deleteSmsEntry(long id){
    	ctx.getContentResolver().delete(ContentUris.withAppendedId(EventLogData.SMS_URI, id),null, null);
    }
    
    /**
     * Delete a MMS entry
     * 
     * @param item id
     */
    public void deleteMmsEntry(long id){
    	ctx.getContentResolver().delete(ContentUris.withAppendedId(EventLogData.MMS_URI, id),null, null);
    }
   
    /**
     * Delete a call entry
     * 
     * @param item id
     */
    public void deleteCallEntry(long id){
    	ctx.getContentResolver().delete(Calls.CONTENT_URI, Calls._ID+" IN ("+id+")", null);
    }

    /**
     * Delete a rich call entry
     * 
     * @param contact
     * @param date
     */
    public void deleteRichCallEntry(String contact, long date){
		if (RichCall.getInstance()==null){
			RichCall.createInstance(ctx);
		}
		RichCall.getInstance().removeCall(contact, date);
    }

    
    /**
     * Delete an IM entry
     * 
     * @param item id
     */
    public void deleteImEntry(long rowId){
    	if (RichMessaging.getInstance()==null){
    		RichMessaging.createInstance(ctx);
    	}
    	RichMessaging.getInstance().deleteImItem(rowId);
    }
    
    /**
     * Delete the chat and file transfer log associated to a contact
     * 
     * @param contact
     */
    public void deleteMessagingLogForContact(String contact){
    	if (RichMessaging.getInstance()==null){
    		RichMessaging.createInstance(ctx);
    	}
    	RichMessaging.getInstance().deleteMessagingLogForContact(contact);
    }
    
    /**
     * Delete an IM session
     * 
     * @param sessionId
     */
    public void deleteImSessionEntry(String sessionId){
    	if (RichMessaging.getInstance()==null){
    		RichMessaging.createInstance(ctx);
    	}
    	RichMessaging.getInstance().deleteImSession(sessionId);
    }
    
    /**
     * Get a cursor on the given chat session
     * 
     * @param sessionId
     * @return cursor
     */
    public Cursor getChatSessionCursor(String sessionId){
    	// Do not take the chat terminated messages
    	String chatTerminatedExcludedSelection = " AND NOT(("+RichMessagingData.KEY_TYPE + "=="+ TYPE_CHAT_SYSTEM_MESSAGE +") AND ("+RichMessagingData.KEY_STATUS+"== "+STATUS_TERMINATED+"))";
    	chatTerminatedExcludedSelection +=" AND NOT(("+RichMessagingData.KEY_TYPE + "=="+ TYPE_GROUP_CHAT_SYSTEM_MESSAGE +") AND ("+RichMessagingData.KEY_STATUS+"== "+STATUS_TERMINATED+"))";

    	return ctx.getContentResolver().query(RichMessagingData.CONTENT_URI, 
				null,
				RichMessagingData.KEY_CHAT_SESSION_ID + "='" + sessionId + "'"+chatTerminatedExcludedSelection, 
				null,
				RichMessagingData.KEY_TIMESTAMP + " ASC");
    }
    
    /**
     * Get a cursor on the given chat contact
     * 
     * @param contact
     * @return cursor
     */
    public Cursor getChatContactCursor(String contact){
    	// Do not take the chat terminated messages
    	String chatTerminatedExcludedSelection = " AND NOT(("+RichMessagingData.KEY_TYPE + "=="+ TYPE_CHAT_SYSTEM_MESSAGE +") AND ("+RichMessagingData.KEY_STATUS+"== "+STATUS_TERMINATED+"))";
    	// Do not take the group chat entries concerning this contact
    	chatTerminatedExcludedSelection +=" AND NOT("+RichMessagingData.KEY_TYPE + "=="+ TYPE_GROUP_CHAT_SYSTEM_MESSAGE +")";
    	    	
		// take all concerning this contact
		return ctx.getContentResolver().query(RichMessagingData.CONTENT_URI, null,
				RichMessagingData.KEY_CONTACT + "='"
				+ PhoneUtils.formatNumberToInternational(contact) + "'"+chatTerminatedExcludedSelection, null,
				RichMessagingData.KEY_TIMESTAMP + " ASC");
    }
    
    /**
     * Get the events log content provider base uri
     * 
     * @param mode
     * @return uri
     */
    public Uri getEventLogContentProviderUri(int mode){
    	return ContentUris.withAppendedId(EventLogData.CONTENT_URI, mode); 
    }
    
    /**
     * Get one to one chat log
     * 
     * @return uri
     */
    public Uri getOneToOneChatLogContentProviderUri(){
    	return ContentUris.withAppendedId(EventLogData.CONTENT_URI, MODE_ONE_TO_ONE_CHAT);
    }
    
    
    /**
     * Get group chat log
     * 
     * @return uri
     */
    public Uri getGroupChatLogContentProviderUri(){
    	return ContentUris.withAppendedId(EventLogData.CONTENT_URI, MODE_GROUP_CHAT);
    }
    
}
