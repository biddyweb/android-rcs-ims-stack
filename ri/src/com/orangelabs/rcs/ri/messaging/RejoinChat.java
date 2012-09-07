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

import java.util.Vector;

import android.app.Activity;
import android.app.Dialog;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.orangelabs.rcs.provider.messaging.RichMessagingData;
import com.orangelabs.rcs.ri.R;
import com.orangelabs.rcs.ri.utils.Utils;
import com.orangelabs.rcs.service.api.client.eventslog.EventsLogApi;
import com.orangelabs.rcs.service.api.client.messaging.IChatEventListener;
import com.orangelabs.rcs.service.api.client.messaging.IChatSession;
import com.orangelabs.rcs.service.api.client.messaging.InstantMessage;
import com.orangelabs.rcs.service.api.client.messaging.MessagingApi;

/**
 * Rejoin a group chat session
 */
public class RejoinChat extends Activity {
    /**
     * UI handler
     */
    private Handler handler = new Handler();

    /**
     * Progress dialog
     */
    private Dialog progressDialog = null;

    /**
	 * Session ID to rejoin
	 */
	private String sessionId = null;
	
	/**
	 * Rejoined chat session
	 */
	private IChatSession chatSession = null; 
	
    /**
	 * Messaging API
	 */
    private MessagingApi messagingApi;

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set layout
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.messaging_rejoin);
        
        // Set title
        setTitle(R.string.menu_rejoin_chat);
        
        // Get chat ID to rejoin
        Vector<String> list = getLastGroupChatSessions();
        Spinner spinner = (Spinner)findViewById(R.id.session);
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        for(int i=0; i < list.size(); i++) {
        	adapter.add(list.elementAt(i));
        }
        spinner.setOnItemSelectedListener(listenerChatId);        
    	
    	// Instanciate messaging API
        messagingApi = new MessagingApi(getApplicationContext());
        messagingApi.connectApi();

        // Get IMS connection status
        boolean connected = false;
        try {
        	connected = messagingApi.isImsConnected(this);
        } catch(Exception e) {
        	connected = false;
        }
        
        // Set button callback
        Button rejoinBtn = (Button)findViewById(R.id.rejoin_btn);
        rejoinBtn.setOnClickListener(btnRejoinListener);
    	if ((!connected) || (sessionId == null) || (sessionId.length() == 0)) {
    		rejoinBtn.setEnabled(false);
    	} else {
    		rejoinBtn.setEnabled(true);
    	}
	}
    
    @Override
    public void onDestroy() {
    	super.onDestroy();

        // Remove session listener
        if (chatSession != null) {
        	try {
        		chatSession.removeSessionListener(chatSessionListener);
        	} catch(Exception e) {
        	}
        }

        // Disconnect messaging API
        messagingApi.disconnectApi();
    }

    /**
     * Spinner chat ID listener
     */
    private OnItemSelectedListener listenerChatId = new OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
	        Spinner spinner = (Spinner)findViewById(R.id.session);
	        sessionId = (String)spinner.getSelectedItem();
	        Button rejoinBtn = (Button)findViewById(R.id.rejoin_btn);
	    	if ((sessionId == null) || (sessionId.length() == 0)) {
	    		rejoinBtn.setEnabled(false);
	    	} else {
	    		rejoinBtn.setEnabled(true);
	    	}
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
		}
	};

	/**
     * Request button callback
     */
    private OnClickListener btnRejoinListener = new OnClickListener() {
        public void onClick(View v) {
    		// Rejoin the last group chat session
    		rejoin();
        }
    };
	
    /**
     * Rejoin the session
     */
    private void rejoin() {
		// Stop session
        Thread thread = new Thread() {
        	public void run() {
            	try {
            		chatSession = messagingApi.rejoinChatGroupSession(sessionId);
            		chatSession.addSessionListener(chatSessionListener);
            	} catch(Exception e) {
            		handler.post(new Runnable(){
            			public void run(){
            				Utils.showMessageAndExit(RejoinChat.this, getString(R.string.label_rejoin_chat_failed));		
            			}
            		});
            	}
        	}
        };
        thread.start();
    }    

    /**
     * Chat session event listener
     */
    private IChatEventListener chatSessionListener = new IChatEventListener.Stub() {
		// Session is started
		public void handleSessionStarted() {
			handler.post(new Runnable() { 
				public void run() {
					// Hide progress dialog
					hideProgressDialog();

					// Display message
					Utils.showMessageAndExit(RejoinChat.this, getString(R.string.label_rejoin_chat_success));
				}
			});
		}
	
		// Chat error
		public void handleImError(final int error) {
			handler.post(new Runnable() {
				public void run() {
					// Display error
					Utils.showMessageAndExit(RejoinChat.this, getString(R.string.label_rejoin_chat_failed, error));
				}
			});
		}	
		
		// Session has been aborted
		public void handleSessionAborted() {
			// Not used here
		}
	    
		// Session has been terminated by remote
		public void handleSessionTerminatedByRemote() {
			// Not used here
		}

		// New text message received
		public void handleReceiveMessage(final InstantMessage msg) {
			// Not used here
		}		

		// Is composing event
		public void handleIsComposingEvent(String contact, final boolean isComposing) {
			// Not used here
		}

		// Conference event
	    public void handleConferenceEvent(final String contact, final String contactDisplayname, final String state) {
			// Not used here
		}
	    
		// Message delivery status
		public void handleMessageDeliveryStatus(final String msgId, final String status) {
			// Not used here
		}
		
		// Request to add participant is successful
		public void handleAddParticipantSuccessful() {
			// Not used here
		}
	    
		// Request to add participant has failed
		public void handleAddParticipantFailed(String reason) {
			// Not used here
		}
    };

	/**
	 * Hide progress dialog
	 */
    public void hideProgressDialog() {
    	if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
			progressDialog = null;
		}
    }
    
    /**
     * Returns the list of last group chat session
     * 
     * @return List of session ID
     */
    private Vector<String> getLastGroupChatSessions() {
    	Vector<String> result = new Vector<String>();
    	Cursor cursor = getContentResolver().query(RichMessagingData.CONTENT_URI, 
    			new String[] {
    				RichMessagingData.KEY_CHAT_SESSION_ID
    			},
    			"(" + RichMessagingData.KEY_TYPE + "=" + EventsLogApi.TYPE_GROUP_CHAT_SYSTEM_MESSAGE + ") AND (" +
    				RichMessagingData.KEY_CHAT_ID + " NOT NULL)", 
    			null, 
    			RichMessagingData.KEY_TIMESTAMP + " DESC");
    	while(cursor.moveToNext()) {
    		result.addElement(cursor.getString(0));
    	}
    	cursor.close();
    	return result;
    }
}
