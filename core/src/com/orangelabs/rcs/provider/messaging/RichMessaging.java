/*******************************************************************************
 * Software Name : RCS IMS Stack
 *
 * Copyright (C) 2010 France Telecom S.A.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.orangelabs.rcs.core.content.MmContent;
import com.orangelabs.rcs.core.ims.service.SessionIdGenerator;
import com.orangelabs.rcs.core.ims.service.im.chat.ChatSession;
import com.orangelabs.rcs.core.ims.service.im.chat.event.User;
import com.orangelabs.rcs.core.ims.service.im.chat.imdn.ImdnDocument;
import com.orangelabs.rcs.provider.settings.RcsSettings;
import com.orangelabs.rcs.service.api.client.eventslog.EventsLogApi;
import com.orangelabs.rcs.service.api.client.messaging.GeolocMessage;
import com.orangelabs.rcs.service.api.client.messaging.GeolocPush;
import com.orangelabs.rcs.service.api.client.messaging.GroupChatInfo;
import com.orangelabs.rcs.service.api.client.messaging.InstantMessage;
import com.orangelabs.rcs.utils.PhoneUtils;
import com.orangelabs.rcs.utils.StringUtils;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Rich messaging history. This content provider removes old messages if there is no enough space.
 * 
 * @author mhsm6403
 * @author Deutsche Telekom AG
 * @author yplo6403
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
	 * Max log entries
	 */
	private int maxLogEntries;

	/**
	 * The logger
	 */
	private Logger logger = Logger.getLogger(this.getClass().getName());

	/**
	 * Create instance
	 * 
	 * @param ctx
	 *            Context
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
		this.maxLogEntries = RcsSettings.getInstance().getMaxChatLogEntriesPerContact();
	}

	/**
	 * Get list of participants into a string
	 * 
	 * @param participants List of participants
	 * @return String (contacts are semicolon separated)
	 */
	private static String getParticipants(List<String> participants) {
		if (participants == null)
			return null;
		StringBuffer result = new StringBuffer();
		boolean firstiteration = true;
		for (String contact : participants) {
			String number = PhoneUtils.extractNumberFromUri(contact);
			if (PhoneUtils.isGlobalPhoneNumber(number)) {
				if (!firstiteration) {
					result.append(";");
				}
				result.append(number);
				firstiteration = false;
			}
		}
		if (result.length() == 0)
			return null;
		return result.toString();
	}

	/**
	 * Get list of participants into a string
	 * 
	 * @param session Chat session
	 * @return String (contacts are semicolon separated)
	 */
	private static String getParticipants(ChatSession session) {
		StringBuffer participants = new StringBuffer();
		if (session.isGroupChat()) {
			return getParticipants(session.getParticipants().getList());
		} else {
			participants.append(PhoneUtils.extractNumberFromUri(session.getRemoteContact()));
			return participants.toString();
		}
	}

	/**
     * Get list of participants into a string
     *
     * @param session Chat session
     * @return list of participants
	 */
	private static List<String> getListParticipants(ChatSession session) {
		if (session.isGroupChat()) {
			return session.getParticipants().getList();
		} else {
			List<String> participants = new ArrayList<String>();
			participants.add(PhoneUtils.extractNumberFromUri(session.getRemoteContact()));
			return participants;
		}
	}

	/**
	 * Get list of participants from a string
	 * 
	 * @param String participants (contacts are comma separated)
	 * @return String[] contacts or null if
	 */
	private static String[] getParticipants(String participants) {
		String[] res = new String[] {};
		if (participants != null && participants.trim().length() > 0) {
			// In groupChat, "participants" is a concatenation of all contacts' numbers, separated by ';' and terminated by a ';'
			// We call split with the param limit set to 0 for empty strings to be automatically trimmed out
			res = participants.split(";", 0);
		}
		return res;
	}

	/**
	 * Get type corresponding to a chat system event
	 * 
	 * @param session Chat session
	 * @return Type
	 */
	private int getChatSystemEventType(ChatSession session) {
		if (session.isGroupChat()) {
			return EventsLogApi.TYPE_GROUP_CHAT_SYSTEM_MESSAGE;
		} else {
			return EventsLogApi.TYPE_CHAT_SYSTEM_MESSAGE;
		}
	}

	/**
	 * Add a new conference event
	 * 
	 * @param session Chat session
	 * @param contact Contact
	 * @param state Conference state
	 */
	public void addConferenceEvent(ChatSession session, String contact, String state) {
		int event = getEventLogValue(state);

		if (event != -1) {
			addEntry(EventsLogApi.TYPE_GROUP_CHAT_SYSTEM_MESSAGE, session.getSessionID(), session.getContributionID(), null, contact, null, null,
					null, null, 0, null, event);
		}
	}
	
	/**
	 * Get state event log value from conference state name
	 * 
	 * @param state Conference state
	 * @return event log value 
	 * 
	 */
	private int getEventLogValue(String state){
		int event = -1;
		if (state.equals(User.STATE_BOOTED)) {
			// Contact has lost the session and may rejoin the session after
			event = EventsLogApi.EVENT_DISCONNECT_CHAT;
		} else if (state.equals(User.STATE_DEPARTED)) {
			// Contact has left voluntary the session
			event = EventsLogApi.EVENT_LEFT_CHAT;
		} else if (state.equals(User.STATE_DISCONNECTED)) {
			// Contact has left voluntary the session
			event = EventsLogApi.EVENT_LEFT_CHAT;
		} else if (state.equals(User.STATE_CONNECTED)) {
			// Contact has joined the session
			event = EventsLogApi.EVENT_JOINED_CHAT;
		} else if (state.equals(User.STATE_BUSY)) {
			// Contact is busy
			event = EventsLogApi.EVENT_BUSY;
		} else if (state.equals(User.STATE_PENDING)) {
			// Contact is busy
			event = EventsLogApi.EVENT_PENDING;
		} else if (state.equals(User.STATE_DECLINED)) {
			// Contact has declined the invitation
			event = EventsLogApi.EVENT_DECLINED;
		} else if (state.equals(User.STATE_FAILED)) {
			// Any SIP error related to the contact invitation
			event = EventsLogApi.EVENT_FAILED;
		}
		
		return event;
	}

    /**
     * Add an incoming chat session
     * 
     * @param session Chat session
     */
	public void addIncomingChatSession(ChatSession session) {
		// Add session entry
		String sessionId = session.getSessionID();
		String chatId = session.getContributionID();
		List<String> participants = RichMessaging.getListParticipants(session);
		String subject = session.getSubject();
		int type = getChatSystemEventType(session);
		addEntry(type, sessionId, chatId, null, participants, null, subject, null, null, 0, null, EventsLogApi.EVENT_INVITED,false);

		// Add first message entry
		InstantMessage firstMsg = session.getFirstMessage();
		if (firstMsg != null) {
			addIncomingChatMessage(firstMsg, session);
		}
	}

	/**
	 * Add an incoming chat session (created by FT HTTP)
	 * 
	 * @param session Chat session
	 */
	public void addIncomingChatSessionByFtHttp(ChatSession session) {
		// Add session entry
		String sessionId = session.getSessionID();
		String chatId = session.getContributionID();
		List<String> participants = RichMessaging.getListParticipants(session);
		int type = getChatSystemEventType(session);
		addEntry(type, sessionId, chatId, null, participants, null, null, null, null, 0, null, EventsLogApi.EVENT_INVITED, false);
	}

	/**
	 * Add an incoming file transfer
	 * 
	 * @param contact Contact
	 * @param chatSessionId Chat session ID which may be null if file transfer is outside of a chat session
	 * @param ftSessionId File transfer session ID
	 * @param content File content
	 */
	public void addIncomingFileTransfer(String contact, String chatSessionId, String ftSessionId, MmContent content) {
		// Add session entry
		addEntry(EventsLogApi.TYPE_INCOMING_FILE_TRANSFER, ftSessionId, chatSessionId, null, contact, null, null, content.getEncoding(),
				content.getName(), content.getSize(), null, EventsLogApi.EVENT_INITIATED);
	}

	/**
	 * Add outgoing chat session
	 * 
	 * @param session Chat session
	 */
	public void addOutgoingChatSession(ChatSession session) {
		// Add session entry
		String sessionId = session.getSessionID();
		String chatId = session.getContributionID();
		List<String> participants = RichMessaging.getListParticipants(session);
		String subject = session.getSubject();
		int type = getChatSystemEventType(session);
		addEntry(type, sessionId, chatId, null, participants, null, subject, null, null, 0, null, EventsLogApi.EVENT_INITIATED,
				false);

		// Add first message entry
		InstantMessage firstMsg = session.getFirstMessage();
		if (firstMsg != null) { // TODO not save if subject used for HTTP file transfer
			addOutgoingChatMessage(firstMsg, session);
		}

		// Add rejoin ID
		if (session.isGroupChat()) {
			ContentValues values = new ContentValues();
			values.put(RichMessagingData.KEY_CHAT_REJOIN_ID, session.getImSessionIdentity());
			cr.update(databaseUri, values, "(" + RichMessagingData.KEY_CHAT_SESSION_ID + " = \"" + session.getSessionID() + "\") AND ("
					+ RichMessagingData.KEY_TYPE + " =" + type + ")", null);
		}
	}

	/**
	 * Add outgoing chat session (created by FT HTTP)
	 * 
	 * @param session Chat session
	 */
	public void addOutgoingChatSessionByFtHttp(ChatSession session) {
		// Add session entry
		String sessionId = session.getSessionID();
		String chatId = session.getContributionID();
		List<String> participants = RichMessaging.getListParticipants(session);
		int type = getChatSystemEventType(session);
		addEntry(type, sessionId, chatId, null, participants, null, null, null, null, 0, null, EventsLogApi.EVENT_INITIATED, false);
	}

	/**
	 * Add outgoing file transfer
	 * 
	 * @param contact Contact
	 * @param chatSessionId Chat session ID which may be null if file transfer took place outside of a chat session
	 * @param sessionId Session ID
	 * @param filename Filename
	 * @param content File content
	 */
	public void addOutgoingFileTransfer(String contact, String chatSessionId, String ftSessionId, String fileName, MmContent content) {
		// Add session entry
		addEntry(EventsLogApi.TYPE_OUTGOING_FILE_TRANSFER, ftSessionId, chatSessionId, null, contact, null, fileName, content.getEncoding(),
				content.getName(), content.getSize(), null, EventsLogApi.EVENT_INITIATED);
	}

	/**
	 * Add outgoing file transfer to a group
	 * 
	 * @param contactList List of contacts
	 * @param chatSessionId Chat session ID which may be null if file transfer took place outside of a chat session
	 * @param sessionId Session ID
	 * @param filename Filename
	 * @param content File content
	 */
	public void addOutgoingGroupFileTransfer(List<String> contactList, String chatSessionId, String ftSessionId, String fileName, MmContent content) {
		// Add session entry
		addEntry(EventsLogApi.TYPE_OUTGOING_FILE_TRANSFER, ftSessionId, chatSessionId, null, contactList, null, fileName,
				content.getEncoding(), content.getName(), content.getSize(), null, EventsLogApi.EVENT_INITIATED, false);
	}

	/**
	 * Add incoming chat message
	 * 
	 * @param msg Chat message
	 * @param session Chat session
	 */
	public void addIncomingChatMessage(InstantMessage msg, ChatSession session) {
		// Add message entry
		int type;
		if (session.isGroupChat()) {
			type = EventsLogApi.TYPE_INCOMING_GROUP_CHAT_MESSAGE;
		} else {
			type = EventsLogApi.TYPE_INCOMING_CHAT_MESSAGE;
		}
		int status = EventsLogApi.STATUS_RECEIVED;
		if (msg.isImdnDisplayedRequested() && RcsSettings.getInstance().isImDisplayedNotificationActivated()) {
			status = EventsLogApi.STATUS_REPORT_REQUESTED;
		}
		String number = PhoneUtils.extractNumberFromUri(msg.getRemote());
		addEntry(type, session.getSessionID(), session.getContributionID(), msg.getMessageId(), number,
				msg.getDisplayName(), msg.getTextMessage(), InstantMessage.MIME_TYPE, number, msg.getTextMessage()
						.getBytes().length, msg.getDate(), status);
	}

	/**
	 * Add outgoing chat message
	 * 
	 * @param msg Chat message
	 * @param session Chat session
	 */
	public void addOutgoingChatMessage(InstantMessage msg, ChatSession session) {
		// Add session entry
		int type;
		if (session.isGroupChat()) {
			type = EventsLogApi.TYPE_OUTGOING_GROUP_CHAT_MESSAGE;
		} else {
			type = EventsLogApi.TYPE_OUTGOING_CHAT_MESSAGE;
		}
		String number = PhoneUtils.extractNumberFromUri(msg.getRemote());
		addEntry(type, session.getSessionID(), session.getContributionID(), msg.getMessageId(), number,
				msg.getDisplayName(), msg.getTextMessage(), InstantMessage.MIME_TYPE, number, msg.getTextMessage()
						.getBytes().length, msg.getDate(), EventsLogApi.STATUS_SENT);
	}

	/**
	 * Add incoming geoloc message
	 * 
	 * @param geoloc Geoloc message
	 * @param session Chat session
	 */
	public void addIncomingGeoloc(GeolocMessage geoloc, ChatSession session) {
		// Add message entry
		int type;
		if (session.isGroupChat()) {
			type = EventsLogApi.TYPE_INCOMING_GROUP_GEOLOC;
		} else {
			type = EventsLogApi.TYPE_INCOMING_GEOLOC;
		}
		int status = EventsLogApi.STATUS_RECEIVED;
		if (geoloc.isImdnDisplayedRequested() && RcsSettings.getInstance().isImDisplayedNotificationActivated()) {
			status = EventsLogApi.STATUS_REPORT_REQUESTED;
		}
		String geolocData = GeolocPush.formatGeolocToStr(geoloc.getGeoloc());
		String number =  PhoneUtils.extractNumberFromUri(geoloc.getRemote());
		addEntry(type, session.getSessionID(), session.getContributionID(), geoloc.getMessageId(), number,
				geoloc.getDisplayName(), geolocData, GeolocMessage.MIME_TYPE, number, geolocData.length(),
				geoloc.getDate(), status);
	}

    /**
     * Add incoming geoloc message out of a session
     * 
     * @param geoloc Geoloc message
     */
	public void addIncomingGeoloc(GeolocMessage geoloc) {
		// Add message entry
		int type = EventsLogApi.TYPE_INCOMING_GEOLOC;
		int status = EventsLogApi.STATUS_RECEIVED;
		String geolocData = GeolocPush.formatGeolocToStr(geoloc.getGeoloc());
		String number =  PhoneUtils.extractNumberFromUri(geoloc.getRemote());
		addEntry(type, null, null, geoloc.getMessageId(), number, geoloc.getDisplayName(), geolocData,
				GeolocMessage.MIME_TYPE, number, geolocData.length(), geoloc.getDate(), status);
	}

    /**
     * Add outgoing geoloc message
     * 
     * @param geoloc Geoloc message
     * @param session Chat session
     */
	public void addOutgoingGeoloc(GeolocMessage geoloc, ChatSession session) {
		// Add session entry
		int type;
		if (session.isGroupChat()) {
			type = EventsLogApi.TYPE_OUTGOING_GROUP_GEOLOC;
		} else {
			type = EventsLogApi.TYPE_OUTGOING_GEOLOC;
		}
		String geolocData = GeolocPush.formatGeolocToStr(geoloc.getGeoloc());
		String number =  PhoneUtils.extractNumberFromUri(geoloc.getRemote());
		addEntry(type, session.getSessionID(), session.getContributionID(), geoloc.getMessageId(), number,
				geoloc.getDisplayName(), geolocData, GeolocMessage.MIME_TYPE, number, geolocData.length(),
				geoloc.getDate(), EventsLogApi.STATUS_SENT);
	}

    /**
     * Add outgoing geoloc message
     * 
     * @param geoloc Geoloc message
     */
	public void addOutgoingGeoloc(GeolocMessage geoloc) {
		// Add session entry
		int type = EventsLogApi.TYPE_OUTGOING_GEOLOC;
		String geolocData = GeolocPush.formatGeolocToStr(geoloc.getGeoloc());
		String number =  PhoneUtils.extractNumberFromUri(geoloc.getRemote());
		addEntry(type, null, null, geoloc.getMessageId(), number, geoloc.getDisplayName(), geolocData,
				GeolocMessage.MIME_TYPE, number, geolocData.length(), geoloc.getDate(), EventsLogApi.STATUS_SENT);
	}

    /**
     * Set the delivery status of a chat message
     * 
     * @param msgId Message ID
     * @param status Status
     * @param contact contact who notified status
     */
	public void setChatMessageDeliveryStatus(String msgId, String status, String contact) {
		contact = PhoneUtils.extractNumberFromUri(contact);
		if (status.equalsIgnoreCase(ImdnDocument.DELIVERY_STATUS_DISPLAYED)) {
			setChatMessageDeliveryStatus(msgId, EventsLogApi.STATUS_DISPLAYED, contact);
		} else if (status.equalsIgnoreCase(ImdnDocument.DELIVERY_STATUS_DELIVERED)) {
			setChatMessageDeliveryStatus(msgId, EventsLogApi.STATUS_DELIVERED, contact);
		} else if ((status.equalsIgnoreCase(ImdnDocument.DELIVERY_STATUS_ERROR)) || (status.equalsIgnoreCase(ImdnDocument.DELIVERY_STATUS_FAILED))
				|| (status.equalsIgnoreCase(ImdnDocument.DELIVERY_STATUS_FORBIDDEN))) {
			setChatMessageDeliveryStatus(msgId, EventsLogApi.STATUS_FAILED);
		}
	}

    /**
     * A delivery report "displayed" is requested for a given chat message
     * 
     * @param msgId Message ID
     */
	public void setChatMessageDeliveryRequested(String msgId) {
		setChatMessageDeliveryStatus(msgId, EventsLogApi.STATUS_REPORT_REQUESTED);
	}

    /**
     * Get set of contacts whom chat message is delivered
     * 
     * @param msgId the chat message ID
     * @return set of contacts
     */
	public Set<String> getContactsChatDelivered(String msgId) {
		return getContactsChat(msgId, RichMessagingData.KEY_IMDN_DELIVERED_LIST);
	}

    /**
     * Get set of contacts who displayed the chat message
     * 
     * @param msgId the chat message ID
     * @return set of contacts
     */
	public Set<String> getContactsChatDisplayed(String msgId) {
		return getContactsChat(msgId, RichMessagingData.KEY_IMDN_DISPLAYED_LIST);
	}

    /**
     * Get set of contacts for chat message
     * 
     * @param msgId the chat message ID
     * @param columnName the column name to query
     * @return a set of contacts
     */
	private Set<String> getContactsChat(final String msgId, final String columnName) {
		Set<String> contactSet = new HashSet<String>();
		Cursor cursor = cr.query(databaseUri, new String[] { columnName }, RichMessagingData.KEY_MESSAGE_ID + " =\'" + msgId + "\'", null, null);
		if (cursor.moveToFirst()) {
			String contactList = cursor.getString(0);
			if (contactList != null) {
				String[] separated = contactList.split(";");
				for (String contact : separated) {
					contactSet.add(contact);
				}
			}
		}
		cursor.close();
		return contactSet;
	}

    /**
     * Set the delivery status of a chat message
     * 
     * @param msgId the chat Message ID
     * @param status Status
     */
	private void setChatMessageDeliveryStatus(String msgId, int status) {
		ContentValues values = new ContentValues();
		values.put(RichMessagingData.KEY_STATUS, status);
		cr.update(databaseUri, values, RichMessagingData.KEY_MESSAGE_ID + " = \'" + msgId + "\'", null);
	}

    /**
     * Set the delivery status and list of contacts for a chat message
     * 
     * @param msgId message Id
     * @param status delivery status
     * @param contacts set of contacts
     * @param column column to update with contact list (i.e. a string with ";" delimiter)
     */
	private void setChatMessageDeliveryStatus(String msgId, int status, final Set<String> contacts, String column) {
		ContentValues values = new ContentValues();
		values.put(RichMessagingData.KEY_STATUS, status);
		// Build the list of contacts in a string with separator ";"
		String contactList = StringUtils.join(contacts, ";");
		values.put(column, contactList);
		// Update
		cr.update(databaseUri, values, RichMessagingData.KEY_MESSAGE_ID + " = \'" + msgId + "\'", null);
	}

    /**
     * Set the delivery status of a chat message
     * 
     * @param msgId Message ID
     * @param status Status
     * @param contact the remote contact
     */
	private void setChatMessageDeliveryStatus(String msgId, int status, String contact) {
		if (contact == null) {
			if (logger.isActivated())
				logger.warn("Contact is missing for messageId " + msgId);
			setChatMessageDeliveryStatus(msgId, status);
			return;
		}
		// Update the contacts in the IMDN displayed and delivered columns
		switch (status) {
    		case EventsLogApi.STATUS_DISPLAYED: {
    			// Query set of contact who displayed chat message
    			Set<String> contacts = getContactsChatDisplayed(msgId);
    			// Add current contact to set
    			contacts.add(contact);
    			// Update
    			setChatMessageDeliveryStatus(msgId, status, contacts, RichMessagingData.KEY_IMDN_DISPLAYED_LIST);
    		}
			break;
    		case EventsLogApi.STATUS_DELIVERED: {
    			// Query set of contact whom chat message is delivered
    			Set<String> contacts = getContactsChatDelivered(msgId);
    			// Add current contact to set
    			contacts.add(contact);
    			// Update
    			setChatMessageDeliveryStatus(msgId, status, contacts, RichMessagingData.KEY_IMDN_DELIVERED_LIST);
    		}
			break;
    		default:
    			setChatMessageDeliveryStatus(msgId, status);
    	}
	}

    /**
     * Add chat session termination
     * 
     * @param session Chat session
     */
	public void addChatSessionTermination(ChatSession session) {
		String sessionId = session.getSessionID();
		String chatId = session.getContributionID();
		int type = getChatSystemEventType(session);
		List<String> participants = RichMessaging.getListParticipants(session);
		addEntry(type, sessionId, chatId, null, participants, null, null, null, null, 0, new Date(),
				EventsLogApi.STATUS_TERMINATED, false);
	}

    /**
     * Add chat session termination by end user
     * 
     * @param session Chat session
     */
	public void addChatSessionTerminationByUser(ChatSession session) {
		String sessionId = session.getSessionID();
		String chatId = session.getContributionID();
		List<String> participants = RichMessaging.getListParticipants(session);
		int type = getChatSystemEventType(session);
		addEntry(type, sessionId, chatId, null, participants, null, null, null, null, 0, new Date(),
				EventsLogApi.STATUS_TERMINATED_BY_USER, false);
	}

    /**
     * Add chat session termination by remote
     * 
     * @param session Chat session
     */
	public void addChatSessionTerminationByRemote(ChatSession session) {
		String sessionId = session.getSessionID();
		String chatId = session.getContributionID();
		List<String> participants = RichMessaging.getListParticipants(session);
		int type = getChatSystemEventType(session);
		addEntry(type, sessionId, chatId, null, participants, null, null, null, null, 0, new Date(),
				EventsLogApi.STATUS_TERMINATED_BY_REMOTE, false);
	}

    /**
     * Add a chat session error
     * 
     * @param session Chat session
     */
	public void addChatSessionError(ChatSession session) {
		String sessionId = session.getSessionID();
		String chatId = session.getContributionID();
		List<String> participants = RichMessaging.getListParticipants(session);
		int type = getChatSystemEventType(session);
		addEntry(type, sessionId, chatId, null, participants, null, null, null, null, 0, new Date(), EventsLogApi.STATUS_FAILED,
				false);
	}

    /**
     * Mark a chat session as started
     * 
     * @param session Chat session
     */
	public void markChatSessionStarted(ChatSession session) {
		int type = getChatSystemEventType(session);
		String participants = RichMessaging.getParticipants(session);
		ContentValues values = new ContentValues();
		values.put(RichMessagingData.KEY_CHAT_REJOIN_ID, session.getImSessionIdentity());
		values.put(RichMessagingData.KEY_CONTACT, participants);
		cr.update(databaseUri, values, "(" + RichMessagingData.KEY_CHAT_SESSION_ID + " = \"" + session.getSessionID() + "\") AND ("
				+ RichMessagingData.KEY_TYPE + " =" + type + ")", null);
	}

    /**
     * Mark a chat message as failed
     * 
     * @param msgId Message ID
     */
	public void markChatMessageFailed(String msgId) {
		ContentValues values = new ContentValues();
		values.put(RichMessagingData.KEY_STATUS, EventsLogApi.STATUS_FAILED);
		cr.update(databaseUri, values, RichMessagingData.KEY_MESSAGE_ID + " = \"" + msgId + "\"", null);
	}

    /**
     * Mark a chat message as read or not
     * 
     * @param msgId Message ID
     * @param isRead Read flag
     */
	public void markChatMessageAsRead(String msgId, boolean isRead) {
		ContentValues values = new ContentValues();
		if (isRead) {
			values.put(RichMessagingData.KEY_STATUS, EventsLogApi.STATUS_DISPLAYED);
		} else {
			values.put(RichMessagingData.KEY_STATUS, EventsLogApi.STATUS_RECEIVED);
		}
		cr.update(databaseUri, values, RichMessagingData.KEY_MESSAGE_ID + " = \"" + msgId + "\"", null);
	}

    /**
     * Add a spam message
     * 
     * @param msg Chat message
     */
	public void addSpamMessage(InstantMessage msg) {
		// TODO: 2 queries may be avoided
		String id = SessionIdGenerator.getNewId();
		String number =  PhoneUtils.extractNumberFromUri(msg.getRemote());
		addEntry(EventsLogApi.TYPE_INCOMING_CHAT_MESSAGE, id, id, msg.getMessageId(), number, msg.getDisplayName(),
				msg.getTextMessage(), InstantMessage.MIME_TYPE, number, msg.getTextMessage().getBytes().length,
				msg.getDate(), EventsLogApi.STATUS_RECEIVED);
		markChatMessageAsSpam(msg.getMessageId(), true);
	}

    /**
     * Add incoming chat message
     * 
     * @param msg Chat message
     * @param chatId Chat ID
     */
	public void addIncomingChatMessage(InstantMessage msg, String chatId) {
		int status = EventsLogApi.STATUS_RECEIVED;
		if (msg.isImdnDisplayedRequested() && RcsSettings.getInstance().isImDisplayedNotificationActivated()) {
			status = EventsLogApi.STATUS_REPORT_REQUESTED;
		}
		String number =  PhoneUtils.extractNumberFromUri(msg.getRemote());
		addEntry(EventsLogApi.TYPE_INCOMING_CHAT_MESSAGE, SessionIdGenerator.getNewId(), chatId, msg.getMessageId(),
				number, msg.getDisplayName(), msg.getTextMessage(), InstantMessage.MIME_TYPE, msg.getRemote(), msg
						.getTextMessage().getBytes().length, msg.getDate(), status);
	}

    /**
     * Mark a chat message as spam or not
     * 
     * @param msgId Message ID
     * @param isSpam Spam flag
     */
	public void markChatMessageAsSpam(String msgId, boolean isSpam) {
		ContentValues values = new ContentValues();
		if (isSpam) {
			values.put(RichMessagingData.KEY_IS_SPAM, EventsLogApi.MESSAGE_IS_SPAM);
		} else {
			values.put(RichMessagingData.KEY_IS_SPAM, EventsLogApi.MESSAGE_IS_NOT_SPAM);
		}
		cr.update(databaseUri, values, RichMessagingData.KEY_MESSAGE_ID + " = \"" + msgId + "\"" + " AND " + RichMessagingData.KEY_TYPE + " = "
				+ EventsLogApi.TYPE_INCOMING_CHAT_MESSAGE, null);
	}

	/**
	 * Delete all spam messages
	 */
	public void deleteAllSpams() {
		Cursor c = cr.query(databaseUri, new String[] { RichMessagingData.KEY_ID }, RichMessagingData.KEY_IS_SPAM + " = \""
				+ EventsLogApi.MESSAGE_IS_SPAM + "\"", null, null);
		while (c.moveToNext()) {
			long rowId = c.getLong(0);
			deleteEntry(rowId);
		}
		c.close();
	}

	/**
	 * Delete spam message
	 * 
	 * @param Message ID
	 */
	public void deleteSpamMessage(String msgId) {
		Cursor c = cr.query(databaseUri, new String[] { RichMessagingData.KEY_ID }, RichMessagingData.KEY_MESSAGE_ID + " = \"" + msgId + "\""
				+ " AND " + RichMessagingData.KEY_IS_SPAM + " = \"" + EventsLogApi.MESSAGE_IS_SPAM + "\"", null, null);
		if (c != null) {
			if (c.moveToFirst()) {
				long rowId = c.getLong(0);
				if (logger.isActivated()) {
					logger.debug("Deleting spam message { msgId: " + msgId + " }");
				}
				deleteEntry(rowId);
			}
			c.close();
		}
	}

	/**
	 * Clear spam messages for a given contact
	 * 
	 * @param contact Contact
	 */
	public void clearSpamMessages(String contact) {
		int deletedRows = cr.delete(databaseUri, RichMessagingData.KEY_CONTACT + "= \"" + contact + "\"" + " AND " + RichMessagingData.KEY_IS_SPAM
				+ " = \"" + EventsLogApi.MESSAGE_IS_SPAM + "\"", null);
		if (logger.isActivated()) {
			logger.debug("Clear spam messages of contact " + contact + ": deleted rows =" + deletedRows);
		}
	}

    /**
     * Add a new entry (chat event, chat message or file transfer)
     * 
     * @param type Type of entry
     * @param sessionId Session ID of a chat session or a file transfer session
     * @param chatId Chat ID of a chat session
     * @param messageId Message ID of a chat message
     * @param contact Contact phone number
     * @param displayName the display name of the remote contact
     * @param data Content of the entry (an URI for FT or a simple text for IM)
     * @param mimeType MIME type for a file transfer
     * @param name Name of the transfered file
     * @param size Size of the transfered file
     * @param status Status of the entry
     * @return URI of the new entry
     */
	private Uri addEntry(int type, String sessionId, String chatId, String messageId, String contact, String displayName, String data, String mimeType, String name,
			long size, Date date, int status) {
		contact = PhoneUtils.extractNumberFromUri(contact);
		List<String> participants = new ArrayList<String>();
		participants.add(contact);
		return addEntry(type, sessionId, chatId, messageId, participants, displayName, data, mimeType, name, size, date, status,
				false);
	}

    /**
     * Add a new entry (chat event, chat message or file transfer)
     * 
     * @param type Type of entry
     * @param sessionId Session ID of a chat session or a file transfer session
     * @param chatId Chat ID of a chat session
     * @param messageId Message ID of a chat message
     * @param contacts List of contacts
     * @param displayName the display name of the remote contact
     * @param data Content of the entry (an URI for FT or a simple text for IM)
     * @param mimeType MIME type for a file transfer
     * @param name Name of the transfered file
     * @param size Size of the transfered file
     * @param status Status of the entry
     * @param rejectGC if true next GC invitation will be rejected
     * @return URI of the new entry
     *
     */
	private Uri addEntry(int type, String sessionId, String chatId, String messageId, List<String> contacts, String displayName, String data, String mimeType,
			String name, long size, Date date, int status, boolean rejectGC ) {
		String participants = getParticipants(contacts);
		if (logger.isActivated()) {
			logger.debug("Add new entry: type=" + type + ", sessionID=" + sessionId + ", chatID=" + chatId + ", messageID=" + messageId
					+ ", contact=" + participants + ", display=" + displayName + ", MIME=" + mimeType + ", status=" + status);
		}
		ContentValues values = new ContentValues();
		values.put(RichMessagingData.KEY_TYPE, type);
		values.put(RichMessagingData.KEY_CHAT_SESSION_ID, sessionId);
		values.put(RichMessagingData.KEY_CHAT_ID, chatId);
		values.put(RichMessagingData.KEY_MESSAGE_ID, messageId);
		values.put(RichMessagingData.KEY_CONTACT, participants);
		values.put(RichMessagingData.KEY_DISPLAY_NAME, displayName);
		values.put(RichMessagingData.KEY_MIME_TYPE, mimeType);
		values.put(RichMessagingData.KEY_TOTAL_SIZE, size);
		values.put(RichMessagingData.KEY_NAME, name);
		values.put(RichMessagingData.KEY_DATA, data);
		values.put(RichMessagingData.KEY_STATUS, status);
		values.put(RichMessagingData.KEY_NUMBER_MESSAGES, recycler(participants) + 1);
		values.put(RichMessagingData.KEY_IS_SPAM, EventsLogApi.MESSAGE_IS_NOT_SPAM);
		values.put(RichMessagingData.KEY_REJECT_GC, rejectGC ? 1 : 0);
		if (date == null) {
			values.put(RichMessagingData.KEY_TIMESTAMP, Calendar.getInstance().getTimeInMillis());
		} else {
			values.put(RichMessagingData.KEY_TIMESTAMP, date.getTime());
		}

		Uri uri = cr.insert(databaseUri, values);
		return uri;
	}

    /**
     * Manage the max size of the history for a given contact
     * 
     * @param contact Contact
     * @return History size
     */
	private int recycler(String contact) {
		// Get first and last message dates for the contact
		Cursor extrem = cr.query(databaseUri, new String[] { "min(" + RichMessagingData.KEY_TIMESTAMP + ")",
				"max(" + RichMessagingData.KEY_TIMESTAMP + ")" }, RichMessagingData.KEY_CONTACT + " = \'" + contact + "\'", null, null);
		long minDate = -1, maxDate = -1;
		if (extrem.moveToFirst()) {
			minDate = extrem.getLong(0);
			maxDate = extrem.getLong(1);
		}
		extrem.close();
		if (logger.isActivated()) {
			logger.debug("Recycler : minDate = " + minDate + " maxDate " + maxDate);
		}

		// If no entry for this contact return 0
		if (minDate == -1 && maxDate == -1) {
			return 0;
		}

		Cursor c = cr.query(databaseUri, new String[] { RichMessagingData.KEY_NUMBER_MESSAGES, RichMessagingData.KEY_CHAT_SESSION_ID,
				RichMessagingData.KEY_TIMESTAMP }, RichMessagingData.KEY_CONTACT + " = \'" + contact + "\'" + " AND ("
				+ RichMessagingData.KEY_TIMESTAMP + " = " + minDate + " OR " + RichMessagingData.KEY_TIMESTAMP + " = " + maxDate + ")", null,
				RichMessagingData.KEY_TIMESTAMP + " ASC");
		int numberOfMessagesForContact = 0;
		long dateForLastMessage = 0;
		if (c.moveToLast()) {
			numberOfMessagesForContact = c.getInt(0);
			if (logger.isActivated()) {
				logger.debug("Recycler : number of messages for this contact = " + numberOfMessagesForContact);
			}
			if (numberOfMessagesForContact < maxLogEntries) {
				// Enough place for another message... do nothing return
				if (logger.isActivated()) {
					logger.debug("Recycler : Enough place for another message, do nothing return");
				}
				c.close();
				return numberOfMessagesForContact;
			}
			if (logger.isActivated()) {
				logger.debug("Recycler : Not enough place for another message, we will have to remove something");
			}
			// Not enough place for another message... we will have to remove something
			dateForLastMessage = c.getLong(2);
			if (logger.isActivated()) {
				logger.debug("Recycler : dateForLastMessage =" + new Date(dateForLastMessage).toString() + " [" + dateForLastMessage + "]");
			}
		}
		int removedMessages = 0;
		if (c.moveToFirst()) {
			// Remove the first message and all the associated messages from its session
			String sessionId = c.getString(1);
			long firstDate = c.getLong(2);
			if (logger.isActivated()) {
				logger.debug("Recycler : deleting entries for (the first) sessionID : " + sessionId + " for the date : "
						+ new Date(firstDate).toString() + " [" + firstDate + "]");
			}
			removedMessages = cr.delete(databaseUri, RichMessagingData.KEY_CHAT_SESSION_ID + " = \'" + sessionId + "\'", null);
			if (logger.isActivated()) {
				logger.debug("Recycler : messages removed : " + removedMessages);
			}

			// We also will have to set the new number of message after removing, for the last entry
			if (logger.isActivated()) {
				logger.debug("Recycler : set the new number of messages after removing...");
			}
			ContentValues values = new ContentValues();
			numberOfMessagesForContact -= removedMessages;
			if (logger.isActivated()) {
				logger.debug("Recycler : new number of message after deletion : " + numberOfMessagesForContact);
			}
			values.put(RichMessagingData.KEY_NUMBER_MESSAGES, numberOfMessagesForContact);
			int updatedRows = cr.update(databaseUri, values, RichMessagingData.KEY_CONTACT + " = \'" + contact + "\' AND "
					+ RichMessagingData.KEY_TIMESTAMP + " = " + dateForLastMessage, null);
			if (logger.isActivated()) {
				logger.debug("Recycler : updated rows for the contact (must be 1) : " + updatedRows);
			}
		}
		c.close();
		return numberOfMessagesForContact;
	}

    /**
     * Get set of contacts who displayed file
     * 
     * @param sessionId session ID
     * @return set of contacts
     */
	public Set<String> getContactsFileTransferDisplayed(String sessionId) {
		return getContactsFileTransfer(sessionId, RichMessagingData.KEY_IMDN_DISPLAYED_LIST);
	}

    /**
     * Get set of contacts whom file is delivered
     * 
     * @param sessionId session ID
     * @return set of contacts
     */
	public Set<String> getContactsFileTransferDelivered(String sessionId) {
		return getContactsFileTransfer(sessionId, RichMessagingData.KEY_IMDN_DELIVERED_LIST);
	}

    /**
     * Get set of contacts for file transfer
     * 
     * @param sessionId the session ID
     * @param columnName the column to query
     * @return set of contacts
     */
	private Set<String> getContactsFileTransfer(String sessionId, String columnName) {
		Set<String> contactSet = new HashSet<String>();
		Cursor cursor = cr.query(databaseUri, new String[] { columnName }, RichMessagingData.KEY_CHAT_SESSION_ID + " =\'" + sessionId + "\'", null,
				null);
		if (cursor.moveToFirst()) {
			String contactList = cursor.getString(0);
			if (contactList != null) {
				String[] separated = contactList.split(";");
				for (String contact : separated) {
					contactSet.add(contact);
				}
			}
		}
		cursor.close();
		return contactSet;
	}

    /**
     * Update status and contact list for a file transfer
     * 
     * @param sessionId
     * @param status
     * @param contacts set of contacts
     * @param column the column to update with set of contacts
     */
	private void updateFileTransferStatus(String sessionId, int status, final Set<String> contacts, String column) {
		ContentValues values = new ContentValues();
		values.put(RichMessagingData.KEY_STATUS, status);
		// Build the list of contacts in a string with separator ";"
		String contactList = StringUtils.join(contacts, ";");
		values.put(column, contactList);
		// Update
		cr.update(databaseUri, values, RichMessagingData.KEY_CHAT_SESSION_ID + " = " + sessionId, null);
	}

    /**
     * Update file transfer status
     * 
     * @param sessionId Session Id
     * @param status New status
     * @param contact the contact
     */
	public void updateFileTransferStatus(String sessionId, int status, String contact) {
		if (contact == null) {
			updateFileTransferStatus(sessionId, status);
			return;
		}
		// Update the contacts in the IMDN displayed and delivered columns
		switch (status) {
    		case EventsLogApi.STATUS_DISPLAYED: {
    			// Query set of contacts who displayed FT
    			Set<String> contacts = getContactsFileTransferDisplayed(sessionId);
    			// Add current contact to that set
    			contacts.add(contact);
    			// Update
    			updateFileTransferStatus(sessionId, status, contacts, RichMessagingData.KEY_IMDN_DISPLAYED_LIST);
    			break;
    		}
    		case EventsLogApi.STATUS_DELIVERED: {
    			// Query set of contacts whom FT was delivered
    			Set<String> contacts = getContactsFileTransferDelivered(sessionId);
    			// Add current contact to that set
    			contacts.add(contact);
    			// Update
    			updateFileTransferStatus(sessionId, status, contacts, RichMessagingData.KEY_IMDN_DELIVERED_LIST);
    			break;
    		}
    		default:
    			updateFileTransferStatus(sessionId, status);
    	}
	}

    /**
     * Update file transfer status
     * 
     * @param sessionId Session Id
     * @param status New status
     */
	private void updateFileTransferStatus(String sessionId, int status) {
		ContentValues values = new ContentValues();
		values.put(RichMessagingData.KEY_STATUS, status);
		cr.update(databaseUri, values, RichMessagingData.KEY_CHAT_SESSION_ID + " = " + sessionId, null);
	}

    /**
     * Update file transfer ChatId
     * 
     * @param sessionId Session Id
     * @param chatId chat Id
     * @param msgId msgId of the corresponding chat
     */
	public void updateFileTransferChatId(String sessionId, String chatId, String msgId) {
		ContentValues values = new ContentValues();
		values.put(RichMessagingData.KEY_CHAT_ID, chatId);
		values.put(RichMessagingData.KEY_MESSAGE_ID, msgId);
		cr.update(databaseUri, values, RichMessagingData.KEY_CHAT_SESSION_ID + " = " + sessionId, null);
	}

    /**
     * Get Message info from msgId
     * 
     * @param msgId Message Id
     * @return message info
     */
	public MessageInfo getMessageInfo(String msgId) {
		if (logger.isActivated()) {
			logger.debug("RichMessaging getMessageInfo(" + msgId + ")");
		}
		MessageInfo result = null;
		Cursor cursor = cr.query(databaseUri, new String[] { RichMessagingData.KEY_CONTACT, RichMessagingData.KEY_TYPE,
				RichMessagingData.KEY_CHAT_SESSION_ID, RichMessagingData.KEY_STATUS}, RichMessagingData.KEY_MESSAGE_ID + "='" + msgId + "'", null,
				RichMessagingData.KEY_TIMESTAMP + " DESC");
		if (cursor.moveToFirst()) {
			result = new MessageInfo(msgId, cursor.getString(0), cursor.getInt(1), cursor.getString(2), cursor.getInt(3));
		}
		cursor.close();
		return result;
	}

    /**
     * Update file transfer download progress
     * 
     * @param sessionId Session Id
     * @param size Downloaded size
     * @param totalSize Total size to download
     */
	public void updateFileTransferProgress(String sessionId, long size, long totalSize) {
		ContentValues values = new ContentValues();

		Cursor cursor = cr.query(RichMessagingData.CONTENT_URI, new String[] { RichMessagingData.KEY_SIZE }, RichMessagingData.KEY_CHAT_SESSION_ID
				+ "='" + sessionId + "'", null, null);
		if (cursor.moveToFirst()) {
			long downloadedSize = cursor.getLong(cursor.getColumnIndexOrThrow(RichMessagingData.KEY_SIZE));
			if ((size >= downloadedSize + totalSize / 10) || size == totalSize) {
				// Update size if we have done at least 10 more percent from total size since last update
				// Or if we are at the end of the download (ensure we update when transfer is finished)
				// This is to avoid too much updates, as the ui refreshes each time
				values.put(RichMessagingData.KEY_SIZE, size);
				values.put(RichMessagingData.KEY_STATUS, EventsLogApi.STATUS_IN_PROGRESS);
				cr.update(databaseUri, values, RichMessagingData.KEY_MESSAGE_ID + " = " + sessionId, null);
			}
		}
		cursor.close();
	}

    /**
     * Update file transfer URL
     * 
     * @param sessionId Session Id
     * @param url File URL
     */
	public void updateFileTransferUrl(String sessionId, String url) {
		ContentValues values = new ContentValues();
		values.put(RichMessagingData.KEY_DATA, url);
		values.put(RichMessagingData.KEY_STATUS, EventsLogApi.STATUS_TERMINATED);
		cr.update(databaseUri, values, RichMessagingData.KEY_CHAT_SESSION_ID + " = " + sessionId, null);
	}

    /**
     * Delete a file transfer session
     * 
     * @param sessionId Session ID
     * @param contact Contact
     */
	public void deleteFileTransferSession(String sessionId, String contact) {
		// Count entries to be deleted
		Cursor count = cr.query(databaseUri, null, RichMessagingData.KEY_CHAT_SESSION_ID + " = " + sessionId, null, null);
		int toBeDeletedRows = count.getCount();
		if (logger.isActivated()) {
			logger.debug("Delete " + toBeDeletedRows + " rows");
		}
		count.close();
		if (toBeDeletedRows == 0) {
			return;
		}

		// Manage recycling
		Cursor c = cr.query(databaseUri, new String[] { RichMessagingData.KEY_TIMESTAMP, RichMessagingData.KEY_NUMBER_MESSAGES,
				RichMessagingData.KEY_MESSAGE_ID }, RichMessagingData.KEY_CONTACT + " = \'" + contact + "\'", null, RichMessagingData.KEY_TIMESTAMP
				+ " DESC");
		if (c.moveToFirst()) {
			long maxDate = c.getLong(0);
			int numberForLast = c.getInt(1);
			String lastSessionId = c.getString(2);

			ContentValues values = new ContentValues();
			values.put(RichMessagingData.KEY_NUMBER_MESSAGES, numberForLast - toBeDeletedRows);
			// If last entry for this contact equals to this file transfer message
			if (sessionId.equals(lastSessionId)) {
				// Update the previous one
				if (c.moveToNext()) {
					maxDate = c.getLong(0);
				}
			}
			/*
			 * TODO : If no more message exists after deleting this one for this contact, the update is useless because it will be
			 * made on the message to be deleted.
			 */
			int updatedRows = cr.update(databaseUri, values, RichMessagingData.KEY_TIMESTAMP + " = " + maxDate + " AND "
					+ RichMessagingData.KEY_CONTACT + " = \'" + contact + "\'", null);
			if (logger.isActivated()) {
				logger.debug("DeleteFileTransfer : recycling updated rows (should be 1) : " + updatedRows);
			}
		}
		c.close();

		/* Delete entry */
		int deletedRows = cr.delete(databaseUri, RichMessagingData.KEY_MESSAGE_ID + " = " + sessionId, null);
		if (logger.isActivated()) {
			logger.debug("DeleteFileTransfer : deleted rows (should be 1) : " + deletedRows);
		}
	}

    /**
     * Delete history associated to a contact
     * 
     * @param contact Contact
     */
	public void deleteContactHistory(String contact) {
		String excludeGroupChat = RichMessagingData.KEY_TYPE + "<>" + EventsLogApi.TYPE_GROUP_CHAT_SYSTEM_MESSAGE + " AND "
				+ RichMessagingData.KEY_TYPE + "<>" + EventsLogApi.TYPE_INCOMING_GROUP_CHAT_MESSAGE + " AND " + RichMessagingData.KEY_TYPE + "<>"
				+ EventsLogApi.TYPE_OUTGOING_GROUP_CHAT_MESSAGE;

		// Delete entries
		int deletedRows = cr.delete(databaseUri, RichMessagingData.KEY_CONTACT + " = \'" + contact + "\' AND " + excludeGroupChat, null);
		if (logger.isActivated()) {
			logger.debug("DeleteSession: deleted rows : " + deletedRows);
		}
	}

    /**
     * Delete a group chat conversation
     * 
     * @param chatId chat ID
     */
	public void deleteGroupChatConversation(String chatId) {
		// Delete entry
		int deletedRows = cr.delete(databaseUri, RichMessagingData.KEY_CHAT_ID + "=\"" + chatId + "\"", null);
		if (logger.isActivated()) {
			logger.debug("Delete group chat conversation: " + deletedRows + " rows deleted");
		}
	}

	/**
	 * Delete a chat session. If file transfer is enclosed in the session, it will also be deleted.
	 * 
	 * @param sessionId Session ID
	 */
	public void deleteChatSession(String sessionId) {
		// Count entries to be deleted
		Cursor count = cr.query(databaseUri, null, RichMessagingData.KEY_CHAT_SESSION_ID + " = " + sessionId, null, null);
		int toBeDeletedRows = count.getCount();

		String contact = null;

		boolean isGroupChat = false;
		if (count.moveToFirst()) {
			contact = count.getString(count.getColumnIndex(RichMessagingData.KEY_CONTACT));
			int type = count.getInt(count.getColumnIndex(RichMessagingData.KEY_TYPE));
			if (type >= EventsLogApi.TYPE_GROUP_CHAT_SYSTEM_MESSAGE && type <= EventsLogApi.TYPE_OUTGOING_GROUP_CHAT_MESSAGE) {
				isGroupChat = true;
			}
		}
		if (logger.isActivated()) {
			logger.debug("DeleteSession: rows to be deleted : " + toBeDeletedRows);
		}
		count.close();
		if (toBeDeletedRows == 0) {
			return;
		}

		if (!isGroupChat) {
			// Manage recycling
			Cursor c = cr.query(databaseUri, new String[] { RichMessagingData.KEY_TIMESTAMP, RichMessagingData.KEY_NUMBER_MESSAGES,
					RichMessagingData.KEY_CHAT_SESSION_ID }, RichMessagingData.KEY_CONTACT + " = \'" + contact + "\'", null,
					RichMessagingData.KEY_TIMESTAMP + " DESC");
			if (c.moveToFirst()) {
				long maxDate = c.getLong(0);
				int numberForLast = c.getInt(1);
				String lastSessionId = c.getString(2);
				ContentValues values = new ContentValues();
				values.put(RichMessagingData.KEY_NUMBER_MESSAGES, numberForLast - toBeDeletedRows);

				if (sessionId.equals(lastSessionId)) {
					// Find the last message from another session for the same contact
					if (logger.isActivated()) {
						logger.debug("DeleteSession : the deleted session is the last one... looking for the previous one for this contact");
					}
					while (c.moveToNext()) {
						if (!sessionId.equals(c.getString(2))) {
							maxDate = c.getLong(0);
							if (logger.isActivated()) {
								logger.debug("DeleteSession : find the previous session with date " + maxDate);
							}
							break;
						}
					}
				}
				if (logger.isActivated()) {
					logger.debug("DeleteSession : updating the row of date " + maxDate);
				}
				/*
				 * TODO : If no more session exists after deleting this one for this contact, the update is useless because it will
				 * be made on the session to be deleted.
				 */
				int updatedRows = cr.update(databaseUri, values, RichMessagingData.KEY_TIMESTAMP + " = " + maxDate + " AND "
						+ RichMessagingData.KEY_CONTACT + " = \'" + contact + "\'", null);
				if (logger.isActivated()) {
					logger.debug("DeleteSession : recycling updated rows (should be 1) : " + updatedRows);
				}
			}
			c.close();
		}

		// Delete entry
		int deletedRows = cr.delete(databaseUri, RichMessagingData.KEY_CHAT_SESSION_ID + " = " + sessionId, null);
		if (logger.isActivated()) {
			logger.debug("DeleteSession: deleted rows : " + deletedRows);
		}
	}

    /**
     * Clear the history for a given contact
     * 
     * @param contact Contact
     */
	public void clearHistory(String contact) {
		int deletedRows = cr.delete(databaseUri, RichMessagingData.KEY_CONTACT + "='" + contact + "'", null);
		if (logger.isActivated()) {
			logger.debug("Clear history of contact " + contact + ": deleted rows =" + deletedRows);
		}
	}

    /**
     * Delete an entry (chat message or file transfer) from its row id
     * 
     * @param rowId Row ID
     */
	public void deleteEntry(long rowId) {
		Cursor count = cr.query(Uri.withAppendedPath(databaseUri, "" + rowId), null, null, null, null);
		if (count.getCount() == 0) {
			count.close();
			return;
		}
		count.moveToFirst();
		String contactNumber = count.getString(count.getColumnIndexOrThrow((RichMessagingData.KEY_CONTACT)));

		// Manage recycling
		Cursor c = cr.query(databaseUri, new String[] { RichMessagingData.KEY_TIMESTAMP, RichMessagingData.KEY_NUMBER_MESSAGES,
				RichMessagingData.KEY_CHAT_SESSION_ID, RichMessagingData.KEY_DATA }, RichMessagingData.KEY_CONTACT + " = \'" + contactNumber + "\'",
				null, RichMessagingData.KEY_TIMESTAMP + " DESC");

		// Get the first last entry for this contact
		if (c.moveToFirst()) {
			long maxDate = c.getLong(0);
			int numberForLast = c.getInt(1);
			String lastSessionId = c.getString(2);

			// We are going to delete one message
			ContentValues values = new ContentValues();
			values.put(RichMessagingData.KEY_NUMBER_MESSAGES, numberForLast - 1);

			// Check if this message has the same sessionID, timestamp and content as the one to be deleted
			String sessionId = count.getString(count.getColumnIndexOrThrow(RichMessagingData.KEY_CHAT_SESSION_ID));
			long date = count.getLong(count.getColumnIndexOrThrow(RichMessagingData.KEY_TIMESTAMP));
			String message = "" + count.getString(count.getColumnIndexOrThrow(RichMessagingData.KEY_DATA));
			if (sessionId.equals(lastSessionId) && (date == maxDate) && message.equals("" + c.getString(3))) {
				/*
				 * It's the lastest message for this contact, find the previous message for the same contact
				 */
				if (logger.isActivated()) {
					logger.debug("DeleteSession : the deleted message is the last one... looking for the previous one for this contact");
				}
				// Find the date of the previous message
				if (c.moveToNext()) {
					maxDate = c.getLong(0);
					if (logger.isActivated()) {
						logger.debug("DeleteSession : find the previous message with date " + maxDate);
					}
				}
			}
			if (logger.isActivated()) {
				logger.debug("DeleteSession : updating the row of date " + maxDate);
			}
			/*
			 * Update the first message or the previous one with the new number of message for this contact.
			 * 
			 * TODO : If the first message is the message to be deleted and no more messages are available for the same contact,
			 * then the update is useless because it will be made on the message to be deleted.
			 */
			int updatedRows = cr.update(databaseUri, values, RichMessagingData.KEY_TIMESTAMP + " = " + maxDate + " AND "
					+ RichMessagingData.KEY_CONTACT + " = \'" + contactNumber + "\'", null);
			if (logger.isActivated()) {
				logger.debug("DeleteSession : recycling updated rows (should be 1) : " + updatedRows);
			}
		}
		count.close();
		c.close();

		// Delete entry
		int deletedRows = cr.delete(Uri.withAppendedPath(databaseUri, "" + rowId), null, null);
		if (logger.isActivated()) {
			logger.debug("DeleteSession: deleted rows : " + deletedRows);
		}
	}

    /**
     * Check if a session is terminated
     * 
     * @param sessionId Session ID
     * @return Boolean
     */
	public boolean isSessionTerminated(String sessionId) {
		Cursor cursor = cr.query(databaseUri, new String[] { RichMessagingData.KEY_STATUS }, RichMessagingData.KEY_CHAT_SESSION_ID + " = "
				+ sessionId, null, RichMessagingData.KEY_TIMESTAMP + " DESC");
		if (cursor.moveToFirst()) {
			int status = cursor.getInt(0);
			if ((status == EventsLogApi.STATUS_TERMINATED) || (status == EventsLogApi.STATUS_TERMINATED_BY_REMOTE)
					|| (status == EventsLogApi.STATUS_TERMINATED_BY_USER)) {
				cursor.close();
				return true;
			}
		}
		cursor.close();
		return false;
	}

    /**
     * Check if it's a new message
     * 
     * @param chatId chat ID
     * @param msgId message ID
     * @return true if new message
     */
	public boolean isNewMessage(String chatId, String msgId) {
		Cursor cursor = cr.query(databaseUri, new String[] { RichMessagingData.KEY_MESSAGE_ID }, "(" + RichMessagingData.KEY_CHAT_ID + " = '"
				+ chatId + "') AND (" + RichMessagingData.KEY_MESSAGE_ID + " = \"" + msgId + "\")", null, null);
		if (cursor.moveToFirst()) {
			cursor.close();
			return false;
		}
		cursor.close();
		return true;
	}

    /**
     * doesMessageIdAlreadyExist
     * 
     * @param msgId message ID
     * @return true if the message ID already exists
     */
	public boolean doesMessageIdAlreadyExist(String msgId) {
		Cursor cursor = null;
		try {
			cursor = cr.query(databaseUri, new String[] { RichMessagingData.KEY_MESSAGE_ID }, "(" + RichMessagingData.KEY_MESSAGE_ID + " = \""
					+ msgId + "\")", null, null);
			int messagesNumber = cursor.getCount();
			return messagesNumber != 0;
		} catch (Exception e) {
			return false;
		} finally {
			if (cursor != null)
				cursor.close();
		}
	}

    /**
     * Get all outgoing messages still marked undisplayed for a given contact
     * 
     * @param contact Contact
     * @return list of ids of the undisplayed messages
     */
	public List<String> getAllOutgoingUndisplayedMessages(String contact) {
		List<String> msgIds = new ArrayList<String>();
		Cursor cursor = cr.query(
				databaseUri,
				new String[] { RichMessagingData.KEY_MESSAGE_ID },
				RichMessagingData.KEY_CONTACT + "=?" + " AND " + RichMessagingData.KEY_TYPE + "=?" + " AND (" + RichMessagingData.KEY_STATUS + "=?"
						+ " OR " + RichMessagingData.KEY_STATUS + "=?)",
				new String[] { contact, String.valueOf(EventsLogApi.TYPE_OUTGOING_CHAT_MESSAGE), String.valueOf(EventsLogApi.STATUS_SENT),
						String.valueOf(EventsLogApi.STATUS_DELIVERED) }, null);
		while (cursor.moveToNext()) {
			msgIds.add(cursor.getString(0));
		}
		cursor.close();
		return msgIds;
	}

    /**
     * Get the group chat ID from its session ID
     * 
     * @param sessionId Session ID
     * @result Chat ID or null
     */
	public String getGroupChatId(String sessionId) {
		String result = null;
		Cursor cursor = cr.query(databaseUri, new String[] { RichMessagingData.KEY_CHAT_ID }, "(" + RichMessagingData.KEY_CHAT_SESSION_ID + "='"
				+ sessionId + "') AND (" + RichMessagingData.KEY_TYPE + "=" + EventsLogApi.TYPE_GROUP_CHAT_SYSTEM_MESSAGE + ") AND ("
				+ RichMessagingData.KEY_CHAT_ID + " NOT NULL)", null, RichMessagingData.KEY_TIMESTAMP + " DESC");
		if (cursor.moveToFirst()) {
			result = cursor.getString(0);
		}
		cursor.close();
		return result;
	}

    /**
     * Get the group chat rejoin ID
     * 
     * @param chatId Chat ID
     * @result Rejoin ID or null
     */
	public String getGroupChatRejoinId(String chatId) {
		String result = null;
		Cursor cursor = cr.query(databaseUri, new String[] { RichMessagingData.KEY_CHAT_REJOIN_ID }, "(" + RichMessagingData.KEY_CHAT_ID + "='"
				+ chatId + "') AND (" + RichMessagingData.KEY_TYPE + "=" + EventsLogApi.TYPE_GROUP_CHAT_SYSTEM_MESSAGE + ") AND ("
				+ RichMessagingData.KEY_CHAT_REJOIN_ID + " NOT NULL)", null, RichMessagingData.KEY_TIMESTAMP + " DESC");
		if (cursor.moveToFirst()) {
			result = cursor.getString(0);
		}
		cursor.close();
		return result;
	}

    /**
     * Get the group chat info
     * 
     * @param chatId Chat ID
     * @result Group chat info
     */
	public GroupChatInfo getGroupChatInfo(String chatId) {
		GroupChatInfo result = null;
		Cursor cursor = cr.query(databaseUri, new String[] { RichMessagingData.KEY_CHAT_SESSION_ID, RichMessagingData.KEY_CHAT_REJOIN_ID,
				RichMessagingData.KEY_CONTACT, RichMessagingData.KEY_DATA }, "(" + RichMessagingData.KEY_CHAT_ID + "='" + chatId + "') AND ("
				+ RichMessagingData.KEY_TYPE + "=" + EventsLogApi.TYPE_GROUP_CHAT_SYSTEM_MESSAGE + ") AND ((" + RichMessagingData.KEY_STATUS + "="
				+ EventsLogApi.EVENT_INITIATED + ") OR (" + RichMessagingData.KEY_STATUS + "=" + EventsLogApi.EVENT_INVITED + "))", null,
				RichMessagingData.KEY_TIMESTAMP + " DESC");

		if (cursor.moveToFirst()) {
			String participants = cursor.getString(2);
			List<String> list = Arrays.asList(RichMessaging.getParticipants(participants));
			result = new GroupChatInfo(cursor.getString(0), cursor.getString(1), chatId, list, cursor.getString(3));
		}
		cursor.close();
		return result;
	}

    /**
     * Get chat Session ID associated to message ID
     * 
     * @param msgId message Id
     * @return the session ID or null if message ID does not exist
     */
	public String getChatSessionIdForMessageId(String msgId) {
		Cursor cursor = null;
		try {
			cursor = cr.query(databaseUri, new String[] { RichMessagingData.KEY_CHAT_SESSION_ID }, "(" + RichMessagingData.KEY_MESSAGE_ID + " = \"" + msgId
					+ "\")", null, null);
			if (cursor.moveToFirst()) {
				return cursor.getString(0);
			}
		} catch (Exception e) {
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return null;
	}
	
    /**
     * Get group chat status
     * 
     * @param chatId Chat ID
     * @return Status
     */
	public int getGroupChatStatus(String chatId) {
		int result = -1;
		Cursor cursor = cr.query(databaseUri, new String[] { RichMessagingData.KEY_STATUS }, "(" + RichMessagingData.KEY_CHAT_ID + "='" + chatId
				+ "') AND (" + RichMessagingData.KEY_TYPE + "=" + EventsLogApi.TYPE_GROUP_CHAT_SYSTEM_MESSAGE + ")", null,
				RichMessagingData.KEY_TIMESTAMP + " DESC");
		if (cursor.moveToFirst()) {
			result = cursor.getInt(0);
		}
		cursor.close();
		return result;
	}

    /**
     * Get the group chat participants who have been connected to the chat
     * 
     * @param chatId Chat ID
     * @result List of contacts
     */
	public List<String> getGroupChatConnectedParticipants(String chatId) {
		List<String> result = new ArrayList<String>();

		List<String> alreadyTreated = new ArrayList<String>();
		Cursor cursor = cr.query(databaseUri,
		// We take the statuses for each contact, in descending order
				new String[] { RichMessagingData.KEY_CONTACT, RichMessagingData.KEY_STATUS }, RichMessagingData.KEY_CHAT_ID + "= ?" + " AND "
						+ RichMessagingData.KEY_TYPE + "=" + EventsLogApi.TYPE_GROUP_CHAT_SYSTEM_MESSAGE + " AND (" + RichMessagingData.KEY_STATUS
						+ "=" + EventsLogApi.EVENT_BUSY + " OR " + RichMessagingData.KEY_STATUS + "=" + EventsLogApi.EVENT_DISCONNECT_CHAT + " OR "
						+ RichMessagingData.KEY_STATUS + "=" + EventsLogApi.EVENT_INITIATED + " OR " + RichMessagingData.KEY_STATUS + "="
						+ EventsLogApi.EVENT_INVITED + " OR " + RichMessagingData.KEY_STATUS + "=" + EventsLogApi.EVENT_JOINED_CHAT + " OR "
						+ RichMessagingData.KEY_STATUS + "=" + EventsLogApi.EVENT_PENDING + " OR " + RichMessagingData.KEY_STATUS + "="
						+ EventsLogApi.EVENT_LEFT_CHAT + " OR " + RichMessagingData.KEY_STATUS + "=" + EventsLogApi.EVENT_FAILED + " OR "
						+ RichMessagingData.KEY_STATUS + "=" + EventsLogApi.EVENT_DECLINED + " OR " + RichMessagingData.KEY_STATUS + "="
						+ EventsLogApi.STATUS_TERMINATED_BY_USER + ")", new String[] { chatId }, RichMessagingData.KEY_TIMESTAMP + " DESC");
		while (cursor.moveToNext()) {
			String contact = cursor.getString(0);
			int status = cursor.getInt(1);
			for (String participant : RichMessaging.getParticipants(contact)) {
				// Only consider history until user left voluntary the group chat
				if (status == EventsLogApi.STATUS_TERMINATED_BY_USER) {
					cursor.close();
					return result;
				}
				// If we already have a status for this contact, do not take it (we only want the last one)
				if (!alreadyTreated.contains(participant) && status != EventsLogApi.EVENT_LEFT_CHAT && status != EventsLogApi.EVENT_FAILED
						&& status != EventsLogApi.EVENT_DECLINED && status != EventsLogApi.EVENT_BUSY) {
					result.add(participant);
				}
				alreadyTreated.add(participant);
			}
		}
		cursor.close();
		return result;
	}

    /**
     * Get the group chat subject
     * 
     * @param chatId Chat ID
     * @result Subject or null
     */
	public String getGroupChatSubject(String chatId) {
		String result = null;
		Cursor cursor = cr.query(databaseUri, new String[] { RichMessagingData.KEY_DATA }, "(" + RichMessagingData.KEY_CHAT_ID + "='" + chatId
				+ "') AND (" + RichMessagingData.KEY_TYPE + "=" + EventsLogApi.TYPE_GROUP_CHAT_SYSTEM_MESSAGE + ") AND ("
				+ RichMessagingData.KEY_STATUS + "=" + EventsLogApi.EVENT_INITIATED + " OR " + RichMessagingData.KEY_STATUS + "="
				+ EventsLogApi.EVENT_INVITED + ") AND (" + RichMessagingData.KEY_DATA + "!='')", null, RichMessagingData.KEY_TIMESTAMP + " DESC");
		if (cursor.moveToFirst()) {
			result = cursor.getString(0);
		}
		cursor.close();
		return result;
	}
	

	/**
	 * Quit idle group chat
	 * 
	 * @param sessionId
	 * @param chatId
	 */
	public void quitIdleGroupChat(String sessionId, String chatId) {
		List<String> participants = getGroupChatConnectedParticipants(chatId);
		addEntry(EventsLogApi.TYPE_GROUP_CHAT_SYSTEM_MESSAGE, sessionId, chatId, null, participants, null, null, null, null, 0L,
				null, EventsLogApi.STATUS_TERMINATED_BY_USER, true);
		if (logger.isActivated()) {
			logger.debug("quitIdleGroup (sessionID=" + sessionId + ") (chatID=" + chatId + ") (contact="
					+ getParticipants(participants) + ")");
		}
	}
	
	/**
	 * @param chatId
	 */
	public void acceptGroupChatNextInvitation(String chatId) {
		ContentValues values = new ContentValues();
		values.put(RichMessagingData.KEY_REJECT_GC, "0");
		String selection = RichMessagingData.KEY_CHAT_ID + " = ? AND " //
				+ RichMessagingData.KEY_TYPE + " = ? AND "//
				+ RichMessagingData.KEY_STATUS + " = ? AND "//
				+ RichMessagingData.KEY_REJECT_GC + " = 1";
		String[] selectionArgs = { chatId, "" + EventsLogApi.TYPE_GROUP_CHAT_SYSTEM_MESSAGE,
				"" + EventsLogApi.STATUS_TERMINATED_BY_USER };
		cr.update(databaseUri, values, selection, selectionArgs);
		if (logger.isActivated()) {
			logger.debug("acceptGroupChatNextInvitation (chatID=" + chatId+")");
		}
	}
	
    /**
     * Is group chat next Invite rejected
     * 
     * @param chatId Chat ID
     * @return true if next GC should be rejected
     */
	public boolean isGroupChatNextInviteRejected(String chatId) {
		boolean result = false;
		String selection = RichMessagingData.KEY_CHAT_ID + " = ? AND " //
				+ RichMessagingData.KEY_TYPE + " = ? AND "//
				+ RichMessagingData.KEY_STATUS + " = ? AND "//
				+ RichMessagingData.KEY_REJECT_GC + " = 1";
		String[] selectionArgs = { chatId, "" + EventsLogApi.TYPE_GROUP_CHAT_SYSTEM_MESSAGE,
				"" + EventsLogApi.STATUS_TERMINATED_BY_USER };
		Cursor cursor = cr.query(databaseUri, null, selection, selectionArgs, RichMessagingData.KEY_TIMESTAMP + " DESC");
		if (cursor.getCount() != 0) {
			result = true;
		}
		cursor.close();
		return result;
	}
	
	/**
	 * Has the last known state changed for a participant
	 * 
	 * @param chatId
	 * @param participant
	 * @param lastState
	 * @return true if the state has changed for the participant since the last time
	 */
	public boolean hasLastKnownStateForParticipantChanged(String chatId, String participant, String lastState){
		int lastKnownState = -1;
		String selection = RichMessagingData.KEY_CHAT_ID + " = ? AND " //
				+ RichMessagingData.KEY_TYPE + " = ? AND "//
				+ RichMessagingData.KEY_CONTACT + " = ? ";
		String[] selectionArgs = { chatId, 
				"" + EventsLogApi.TYPE_GROUP_CHAT_SYSTEM_MESSAGE,
				participant };
		Cursor cursor = cr.query(databaseUri, 
				new String[]{RichMessagingData.KEY_STATUS}, 
				selection, 
				selectionArgs, 
				RichMessagingData.KEY_TIMESTAMP + " DESC");
		if (cursor.moveToNext()){
			lastKnownState = cursor.getInt(0);
		}
		cursor.close();
	
		return (lastKnownState==-1 // There was no known state yet 
				||	getEventLogValue(lastState)!=lastKnownState); // Or the state has changed 
	}
	
}
