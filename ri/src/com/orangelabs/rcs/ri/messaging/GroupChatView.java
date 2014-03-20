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

package com.orangelabs.rcs.ri.messaging;

import java.util.ArrayList;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.widget.Toast;

import com.orangelabs.rcs.provider.messaging.RichMessagingData;
import com.orangelabs.rcs.provider.settings.RcsSettings;
import com.orangelabs.rcs.ri.R;
import com.orangelabs.rcs.ri.utils.Utils;
import com.orangelabs.rcs.service.api.client.eventslog.EventsLogApi;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Group chat view
 */
public class GroupChatView extends ChatView {
	/**
	 * Subject
	 */
	private String subject;
	
	/**
	 * Chat Identifier
	 */
	private String chatId;
	
	private final static Logger logger = Logger.getLogger(GroupChatView.class.getSimpleName());
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get subject
        subject = getIntent().getStringExtra("subject");
        // Get the chat ID
        chatId= getIntent().getStringExtra("chatId");
        // Get the history flag
        history = getIntent().getBooleanExtra("history", false);
        // Get the list of participants
        participants = getIntent().getStringArrayListExtra("participants");
        // Set title
        if ((subject == null) || (subject.length() == 0)) {
        	setTitle(getString(R.string.title_chat_view_group));
        } else  {
        	setTitle(getString(R.string.title_chat_view_group) + " " + subject);
        }
        
        // Set the message composer max length
		InputFilter[] filterArray = new InputFilter[1];
		filterArray[0] = new InputFilter.LengthFilter(RcsSettings.getInstance().getMaxGroupChatMessageLength());
		composeText.setFilters(filterArray);
		if (logger.isActivated()) {
			logger.info("onCreate (subject=" + subject + ") (history=" + history + ") (chatId=" + chatId + ") (participants="
					+ participants + ")");
		}
    }
    
    /**
     * Init session
     */
    public void initSession() {
		// Initiate the chat session in background
		new Thread() {
			public void run() {
				try {
					chatSession = messagingApi.initiateAdhocGroupChatSession(participants, subject);
					chatSession.addSessionListener(chatSessionListener);
				} catch (Exception e) {
					if (logger.isActivated()) {
						logger.error("Exception occurred",e);
					}
					if (!isInBackground) {
						Utils.ShowDialogAndFinish(GroupChatView.this, getString(R.string.label_invitation_failed));
					}
				}
			}
		}.start();

        // Display a progress dialog
        progressDialog = Utils.showProgressDialog(GroupChatView.this, getString(R.string.label_command_in_progress));
        progressDialog.setOnCancelListener(new OnCancelListener() {
			public void onCancel(DialogInterface dialog) {
				Toast.makeText(GroupChatView.this, getString(R.string.label_chat_initiation_canceled), Toast.LENGTH_SHORT).show();
				if (logger.isActivated()) {
					logger.debug("initSession: cancel session");
				}
				quitSession();
			}
		});
    }
    
    /**
     * Load history
     * 
     * @param session Chat session
     */
	public void loadHistory() {
		if (chatId == null)
			return;
		try {
			if (logger.isActivated()) {
				logger.info("loadHistory ChatID=" + chatId);
			}
			EventsLogApi log = new EventsLogApi(this);
			Uri uri = log.getGroupChatLogContentProviderUri();
			Cursor cursor = getContentResolver().query(
					uri,
					new String[] { RichMessagingData.KEY_CONTACT, RichMessagingData.KEY_DATA, RichMessagingData.KEY_TIMESTAMP,
							RichMessagingData.KEY_STATUS, RichMessagingData.KEY_TYPE },
					RichMessagingData.KEY_CHAT_ID + "='" + chatId + "'", null, RichMessagingData.KEY_TIMESTAMP + " ASC");
			if (cursor != null) {
				// The system message are not loaded
				while (cursor.moveToNext()) {
					int messageMessageType = cursor.getInt(EventsLogApi.TYPE_COLUMN);
					switch (messageMessageType) {
					case EventsLogApi.TYPE_OUTGOING_GROUP_CHAT_MESSAGE:
					case EventsLogApi.TYPE_INCOMING_GROUP_CHAT_MESSAGE:
					case EventsLogApi.TYPE_OUTGOING_GROUP_GEOLOC:
					case EventsLogApi.TYPE_INCOMING_GROUP_GEOLOC:
						updateView(cursor);
						break;
					}
				}
				cursor.close();
			}
			;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
    
	/**
	 * Send an intent to start GC view activity
	 * 
	 * @param activity
	 *            the {@code activity} that starts the GC view
	 * @param subject
	 *            the {@code subject}
	 * @param sessionId
	 *            the {@code sessionId} or null
	 * @param chatId
	 *            the {@code chatId} or null
	 * @param participants
	 *            the list of {@code participants} or null
	 */
	public static void startGroupChatView(final Activity activity, final String subject, final String sessionId, final String chatId, final ArrayList<String> participants) {
		boolean history = false;
		Intent intent = new Intent(activity, GroupChatView.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		if (!TextUtils.isEmpty(subject))
			intent.putExtra("subject", subject);
		if (sessionId != null)
			intent.putExtra("sessionId", sessionId);
		if (chatId != null)
			intent.putExtra("chatId", chatId);
		if (activity instanceof ChatList)
			history = true;
		intent.putExtra("history", history);
		if (logger.isActivated()) {
			logger.info("startGroupChatView (chatId=" + chatId + ") (history=" + history + ")");
		}
		if (participants != null) {
			intent.putStringArrayListExtra("participants", participants);
		}
		activity.startActivity(intent);
	}
}
