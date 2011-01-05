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
package com.orangelabs.rcs.ri.messaging;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;

import com.orangelabs.rcs.provider.settings.RcsSettings;
import com.orangelabs.rcs.ri.R;
import com.orangelabs.rcs.ri.utils.Utils;
import com.orangelabs.rcs.service.api.client.ClientApiListener;
import com.orangelabs.rcs.service.api.client.messaging.IChatSession;
import com.orangelabs.rcs.service.api.client.messaging.MessagingApi;

/**
 * Receive chat
 */
public class ReceiveChat extends Activity implements ClientApiListener  {
	/**
     * UI handler
     */
    private Handler handler = new Handler();
    
    /**
	 * Messaging API 
	 */
	private MessagingApi messagingApi;
    
	/**
	 * Session ID
	 */
	private String sessionId;
	
	/**
	 * Contact
	 */
	private String remoteContact;
	
    /**
     * Chat session
     */
    private IChatSession chatSession = null;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set title
        setTitle(R.string.title_recv_chat);
        
		// Get invitation info
        sessionId = getIntent().getStringExtra("sessionId");
		remoteContact = getIntent().getStringExtra("contact");
        
		// Remove the notification
		ReceiveChat.removeChatNotification(this, sessionId);

		// Instanciate messaging API
		messagingApi = new MessagingApi(getApplicationContext());
		messagingApi.connectApi();
		messagingApi.addApiEventListener(this);
    }

	@Override
	protected void onDestroy() {
		super.onDestroy();

        // Remove the listener and disconnect from the API
		messagingApi.removeApiEventListener(this);
		messagingApi.disconnectApi();
	}
	
    /**
     * API disabled
     */
    public void handleApiDisabled() {
		handler.post(new Runnable() { 
			public void run() {
				Utils.showError(ReceiveChat.this, getString(R.string.label_api_disabled));
			}
		});
    }
    
    /**
     * API connected
     */
    public void handleApiConnected() {
		try{
			// Get the chat session
			chatSession = messagingApi.getChatSession(sessionId);
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.title_recv_chat);
			builder.setMessage(getString(R.string.label_from) + " " + remoteContact);
			builder.setCancelable(false);
			builder.setIcon(R.drawable.ri_notif_chat_icon);
			builder.setPositiveButton(getString(R.string.label_accept), acceptBtnListener);
			builder.setNegativeButton(getString(R.string.label_decline), declineBtnListener);
			builder.show();
		} catch(Exception e) {
			Utils.showError(ReceiveChat.this, getString(R.string.label_api_failed));
		}
    }

    /**
     * API disconnected
     */
    public void handleApiDisconnected() {
		handler.post(new Runnable(){
			public void run(){
				Utils.showError(ReceiveChat.this, getString(R.string.label_api_disconnected));
			}
		});
    }
    
    /**
     * Accept button listener
     */
    private OnClickListener acceptBtnListener = new OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            Thread thread = new Thread() {
            	public void run() {
                	try {
                		// Accept the invitation
            			chatSession.acceptSession();
            			
        				// Display chat view
        	        	Intent intent = new Intent(ReceiveChat.this, ChatView.class);
        	        	intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        	        	intent.putExtra("contact", chatSession.getRemoteContact());
        	        	intent.putExtra("sessionId", chatSession.getSessionID());
        	        	startActivity(intent);

        	        	// Exit activity
        	        	finish();        	        	
	            	} catch(Exception e) {
	            		Utils.showError(ReceiveChat.this, getString(R.string.label_invitation_failed));
	            	}
            	}
            };
            thread.start();
        }
    };

    /**
     * Reject button listener
     */
    private OnClickListener declineBtnListener = new OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            Thread thread = new Thread() {
            	public void run() {
                	try {
                		// Reject the invitation
            			chatSession.rejectSession();
	            	} catch(Exception e) {
	            	}
            	}
            };
            thread.start();

            // Exit activity
			finish();
        }
    };
    
    /**
     * Add chat notification
     * 
     * @param context Context
     * @param contact Contact
     * @param sessionId Session ID
     */
    public static void addChatInvitationNotification(Context context, String contact, String sessionId) {
		// Create notification
		Intent intent = new Intent(context, ReceiveChat.class);
		intent.putExtra("contact", contact);
		intent.putExtra("sessionId", sessionId);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        String notifTitle = context.getString(R.string.title_recv_chat);
        Notification notif = new Notification(R.drawable.ri_notif_chat_icon,
        		notifTitle,
        		System.currentTimeMillis());
        notif.flags = Notification.FLAG_NO_CLEAR;
        notif.setLatestEventInfo(context,
        		notifTitle,
        		context.getString(R.string.label_from)+" "+contact,
        		contentIntent);
        
        // Set ringtone
        String ringtone = RcsSettings.getInstance().getChatInvitationRingtone();
        if (!TextUtils.isEmpty(ringtone)) {
			notif.sound = Uri.parse(ringtone);
        }
        
        // Set vibration
        if (RcsSettings.getInstance().isPhoneVibrateForChatInvitation()) {
        	notif.defaults |= Notification.DEFAULT_VIBRATE;
        }
        
        // Send notification
		NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify((int)Long.parseLong(sessionId), notif);
    }
    
    /**
     * Remove chat notification
     * 
     * @param context Context
     * @param sessionId Session ID
     */
    public static void removeChatNotification(Context context, String sessionId) {
		NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel((int)Long.parseLong(sessionId));
    }
}

