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

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.text.format.DateUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.orangelabs.rcs.provider.messaging.RichMessagingData;
import com.orangelabs.rcs.ri.R;
import com.orangelabs.rcs.ri.utils.Utils;
import com.orangelabs.rcs.service.api.client.ClientApiListener;
import com.orangelabs.rcs.service.api.client.eventslog.EventsLogApi;
import com.orangelabs.rcs.service.api.client.messaging.IChatSession;
import com.orangelabs.rcs.service.api.client.messaging.MessagingApi;

/**
 * List of current chat sessions and blocked contacts
 */
public class ChatList extends Activity implements ClientApiListener {
	/**
	 * Messaging API
	 */
	private MessagingApi messagingApi;
    
    /**
	 * Chat log adapter
	 */
	private ChatLogAdapter chatLogAdapter; 
   
	/**
	 * Rejoin chat manager
	 */
	private RejoinChat rejoinChat = null;

	/**
	 * API connection state
	 */
	private boolean apiEnabled = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set layout
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.messaging_chat_list);

		// Set UI title
        setTitle(getString(R.string.menu_chat_list));

        // Instantiate messaging API
		messagingApi = new MessagingApi(getApplicationContext());
		messagingApi.addApiEventListener(this);
		messagingApi.connectApi();
        
        // Set list adapter
		chatLogAdapter = new ChatLogAdapter(this);
        ListView view = (ListView)findViewById(android.R.id.list);
        TextView emptyView = (TextView)findViewById(android.R.id.empty);
        view.setEmptyView(emptyView);
        view.setAdapter(chatLogAdapter);		

		// Update data list
		updateDataSet();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if (rejoinChat != null) {
			rejoinChat.stop();
		}
		
		if (messagingApi != null) {
			messagingApi.removeApiEventListener(this);
			messagingApi.disconnectApi();
		}
	}
		
	/**
	 * Update data
	 */
	private void updateDataSet() {
		// Get all chat sessions
		Uri uri = RichMessagingData.CONTENT_URI;
		String where = "(" + RichMessagingData.KEY_TYPE + "=" + EventsLogApi.TYPE_GROUP_CHAT_SYSTEM_MESSAGE + " OR " + RichMessagingData.KEY_TYPE + "=" + EventsLogApi.TYPE_CHAT_SYSTEM_MESSAGE +
			") AND (" +
			RichMessagingData.KEY_STATUS + "=" + EventsLogApi.EVENT_INITIATED + " OR " + RichMessagingData.KEY_STATUS + "=" + EventsLogApi.EVENT_INVITED + ")"; 
		Cursor result = getContentResolver().query(uri,	null, where, null, null);
		chatLogAdapter.changeCursor(result);
		
		// Notify list adapter
		chatLogAdapter.notifyDataSetChanged();
	}
	
    /**
     * API disabled
     */
	public void handleApiDisabled() {
		apiEnabled = false;
	}

    /**
     * API connected
     */
	public void handleApiConnected() {
		apiEnabled = true;
	}

    /**
     * API disconnected
     */
	public void handleApiDisconnected() {
		apiEnabled = false;
	}
	
    /**
     * Chat log adapter
     */
    private class ChatLogAdapter extends ResourceCursorAdapter {
    	private Drawable mDrawableChat;
    	
    	public ChatLogAdapter(Context context) {
    		super(context, R.layout.messaging_chat_list_item, null);
    		
    		// Load the drawables
    		mDrawableChat = context.getResources().getDrawable(R.drawable.ri_eventlog_chat);
    	}
    	
    	@Override
    	public void bindView(View view, Context context, Cursor cursor) {
            final ChatListItemCache tag = (ChatListItemCache)view.getTag();
    		TextView line1View = tag.line1View;; 
    		TextView numberView = tag.numberView;
    		TextView dateView = tag.dateView;
    		ImageView eventIconView = tag.eventIconView;

    		// Get database value
    		tag.sessionId = cursor.getString(EventsLogApi.CHAT_SESSION_ID_COLUMN);
    		tag.chatId = cursor.getString(EventsLogApi.CHAT_ID_COLUMN);
    		tag.type = cursor.getInt(EventsLogApi.TYPE_COLUMN);
    		tag.number = cursor.getString(EventsLogApi.CONTACT_COLUMN);
    		
    		// Set icon
			eventIconView.setImageDrawable(mDrawableChat);

			// Set the date/time field by mixing relative and absolute times
    		long date = cursor.getLong(EventsLogApi.DATE_COLUMN);		
    		dateView.setText(DateUtils.getRelativeTimeSpanString(date,
    				System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS,
    				DateUtils.FORMAT_ABBREV_RELATIVE));
    		
			// Set the label
    		switch(tag.type) {
    			case EventsLogApi.TYPE_CHAT_SYSTEM_MESSAGE:
    			case EventsLogApi.TYPE_OUTGOING_CHAT_MESSAGE:
	    		case EventsLogApi.TYPE_INCOMING_CHAT_MESSAGE:
	    			tag.isGroupChat = false;
	    			line1View.setText(R.string.label_eventlog_chat);
	        		numberView.setText(tag.number);
	        		numberView.setVisibility(View.VISIBLE);
	    			break;
				case EventsLogApi.TYPE_GROUP_CHAT_SYSTEM_MESSAGE:
	    		case EventsLogApi.TYPE_INCOMING_GROUP_CHAT_MESSAGE:
	    		case EventsLogApi.TYPE_OUTGOING_GROUP_CHAT_MESSAGE:
	    			tag.isGroupChat = true;
	    			line1View.setText(R.string.label_eventlog_group_chat);
	        		String subject = cursor.getString(EventsLogApi.DATA_COLUMN);
	        		numberView.setText(subject);
	        		numberView.setVisibility(View.VISIBLE);
	    			break;
    		}
    	}
    	
    	@Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = super.newView(context, cursor, parent);
            view.setOnClickListener(clickItemListener);

            ChatListItemCache cache = new ChatListItemCache();
            cache.line1View = (TextView)view.findViewById(R.id.line1); 
            cache.numberView = (TextView)view.findViewById(R.id.number);
            cache.dateView = (TextView)view.findViewById(R.id.date);
            cache.eventIconView = (ImageView)view.findViewById(R.id.call_icon);            
            view.setTag(cache);

            return view;
        }
    }

    /**
     * Is group chat active
     * 
     * @param chatId Chat ID
     * @return Boolean
     */
    private IChatSession isGroupChatActive(String chatId) {
		try {
			List<IBinder> chatSessionsBinder = messagingApi.getChatSessions();
			for (IBinder binder : chatSessionsBinder) {
				IChatSession chatSession = IChatSession.Stub.asInterface(binder);
				if (chatSession.isGroupChat() && chatSession.getChatID().equals(chatId)) {
					return chatSession;
				}	
			}
			return null;
		} catch(Exception e) {
			return null;
		}
    }
    
    /**
     * Is chat session active
     * 
     * @param sessionId Session ID
     * @return Boolean
     */
    private IChatSession isChatSessionActive(String sessionId) {
		try {
			return messagingApi.getChatSession(sessionId);
		} catch(Exception e) {
			return null;
		}
    }

    /**
     * Is RCS service available
     * 
     * @return Boolean
     */
    private boolean isServiceAvailable() {
    	boolean result = false;
    	try {
			if (apiEnabled && messagingApi.isImsConnected(getApplicationContext())) {
				result = true;
			}
    	} catch(Exception e) {
        	result = false;
    	}
    	return result;
    }
    
    /**
     * Onclick list listener
     */
    private OnClickListener clickItemListener = new OnClickListener() {
		public void onClick(View v) {
			if (!isServiceAvailable()) {
				Utils.showMessage(ChatList.this, getString(R.string.label_continue_chat_failed));
				return;
			}
			
			// Get selected item
			final ChatListItemCache tag = (ChatListItemCache)v.getTag();

			if (tag.isGroupChat) {
				// Group chat
				IChatSession session = isGroupChatActive(tag.chatId);
				if (session != null) {
					// Session already active on the device: just reload it in the UI
					try {
						Intent intent = new Intent(ChatList.this, GroupChatView.class);
			        	intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		            	intent.putExtra("subject", session.getSubject());
			    		intent.putExtra("sessionId", session.getSessionID());
			    		startActivity(intent);				
					} catch(Exception e) {
						Utils.showMessage(ChatList.this, getString(R.string.label_api_failed));
					}
				} else {
					// Session terminated on the device: try to rejoin the session
					rejoinChat = new RejoinChat(ChatList.this, messagingApi, tag.chatId);
					rejoinChat.start();
				}
			} else {
				// 1-1 chat
				IChatSession session = isChatSessionActive(tag.sessionId);
				if (session != null) {
					// Session already active on the device: just reload it in the UI
					try {
			    		Intent intent = new Intent(ChatList.this, OneToOneChatView.class);
			        	intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		            	intent.putExtra("contact", session.getRemoteContact());
			    		intent.putExtra("sessionId", session.getSessionID());
			    		startActivity(intent);
					} catch(Exception e) {
						Utils.showMessage(ChatList.this, getString(R.string.label_api_failed));
					}
				} else {
					// Session terminated on the device: create a new one on the first message
		    		Intent intent = new Intent(ChatList.this, OneToOneChatView.class);
		        	intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            	intent.putExtra("contact", tag.number);
		    		startActivity(intent);
				}
			}
		}
    };

    /**
     * Chat list item
     */
	private class ChatListItemCache {
		public TextView line1View; 
		public TextView numberView;
		public TextView dateView;
		public ImageView eventIconView; 

		public String number;
		public String sessionId;
		public String chatId;
		public int type;
		public boolean isGroupChat;
	}
}