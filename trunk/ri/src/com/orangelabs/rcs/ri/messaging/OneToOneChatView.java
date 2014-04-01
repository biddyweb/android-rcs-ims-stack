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

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.widget.Toast;

import com.orangelabs.rcs.provider.messaging.RichMessagingData;
import com.orangelabs.rcs.provider.settings.RcsSettings;
import com.orangelabs.rcs.ri.R;
import com.orangelabs.rcs.ri.utils.Utils;
import com.orangelabs.rcs.service.api.client.eventslog.EventsLogApi;
import com.orangelabs.rcs.service.api.client.messaging.InstantMessage;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * 1-1 chat view
 */
public class OneToOneChatView extends ChatView {
	
	private final static Logger logger = Logger.getLogger(OneToOneChatView.class.getSimpleName());
	
	private String contact;
	
	private InstantMessage firstmessage;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        contact = getIntent().getStringExtra("contact");
        history = getIntent().getBooleanExtra("history", false);
        firstmessage = getIntent().getParcelableExtra("firstMessage");
        if (firstmessage != null)
			displayReceivedMessage(firstmessage);
        // Set title
		setTitle(getString(R.string.title_chat_view_oneone) + " " +contact);	

        // Set the message composer max length
		InputFilter[] filterArray = new InputFilter[1];
		filterArray[0] = new InputFilter.LengthFilter(RcsSettings.getInstance().getMaxChatMessageLength());
		composeText.setFilters(filterArray);
		if (logger.isActivated()) {
			logger.info("onCreate (contact=" + contact + ") (history=" + history + ") (firsmessage="+firstmessage+")");
		}
    }

    /**
     * Init session
     */
    public void initSession() {
    	// Nothing to do
    }    

    /**
     * Load history
     */
	public void loadHistory() {
		try {
			if (logger.isActivated()) {
				logger.info("loadHistory (contact=" + contact + ")");
			}
			EventsLogApi log = new EventsLogApi(this);
			Uri uri = log.getOneToOneChatLogContentProviderUri();
			Cursor cursor = getContentResolver().query(
					uri,
					new String[] { RichMessagingData.KEY_CONTACT, RichMessagingData.KEY_DATA, RichMessagingData.KEY_TIMESTAMP,
							RichMessagingData.KEY_STATUS, RichMessagingData.KEY_TYPE },
					RichMessagingData.KEY_CONTACT + "='" + contact + "'", null, RichMessagingData.KEY_TIMESTAMP + " ASC");

			// The system message are not loaded
			if (cursor != null) {
				while (cursor.moveToNext()) {
					int messageMessageType = cursor.getInt(EventsLogApi.TYPE_COLUMN);
					switch (messageMessageType) {
					case EventsLogApi.TYPE_OUTGOING_CHAT_MESSAGE:
					case EventsLogApi.TYPE_INCOMING_CHAT_MESSAGE:
					case EventsLogApi.TYPE_OUTGOING_GEOLOC:
					case EventsLogApi.TYPE_INCOMING_GEOLOC:
						updateView(cursor);
						break;
					}
				}
				cursor.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }    
    
    /***
     * Send message
     * 
     * @param msg Message
     */
    public void sendMessage(final String msg) {
        // Test if the session has been created or not
		if (chatSession == null) {
			// Initiate the chat session in background
			new Thread() {
				public void run() {
					try {
						chatSession = messagingApi.initiateOne2OneChatSession(participants.get(0), msg);
						chatSession.addSessionListener(chatSessionListener);
					} catch (Exception e) {
						if (logger.isActivated()) {
							logger.error("Exception occurred", e);
						}
						handler.post(new Runnable() {
							public void run() {
								Utils.showMessageAndExit(OneToOneChatView.this, getString(R.string.label_invitation_failed));
							}
						});
					}
				}
			}.start();

	        // Display a progress dialog
	        progressDialog = Utils.showProgressDialog(OneToOneChatView.this, getString(R.string.label_command_in_progress));
	        progressDialog.setOnCancelListener(new OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					Toast.makeText(OneToOneChatView.this, getString(R.string.label_chat_initiation_canceled), Toast.LENGTH_SHORT).show();
					quitSession();
				}
			});
        } else {
    		// Send message
        	super.sendMessage(msg);
        }    	
    }
    
	/**
	 * Send an intent to start one to one chat view activity
	 * 
	 * @param activity
	 *            the {@code activity} that starts the chat view
	 * @param contact
	 *            the {@code contact}
	 * @param sessionId
	 *            the {@code sessionId} or null
	 * @param firstMsg
	 *            the  {@code firstMsg} or null
	 */
	public static void startOneToOneChatView(final Activity activity, final String contact, final String sessionId,
			final InstantMessage firstMsg) {
		boolean history = false;
		// Display chat view
		Intent intent = new Intent(activity, OneToOneChatView.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		if (sessionId != null)
			intent.putExtra("sessionId", sessionId);
		intent.putExtra("contact", contact);
		if (firstMsg != null)
			intent.putExtra("firstMessage", firstMsg);
		if (activity instanceof ChatList)
			history = true;
		intent.putExtra("history", history);
		if (logger.isActivated()) {
			logger.info("startOneToOneChatView (contact=" + contact + ") (history=" + history + ")");
		}
		activity.startActivity(intent);
	}
}
