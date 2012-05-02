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
import android.app.Dialog;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.orangelabs.rcs.ri.R;
import com.orangelabs.rcs.ri.utils.Registry;
import com.orangelabs.rcs.ri.utils.Utils;
import com.orangelabs.rcs.service.api.client.ImsEventListener;
import com.orangelabs.rcs.service.api.client.messaging.IChatEventListener;
import com.orangelabs.rcs.service.api.client.messaging.IChatSession;
import com.orangelabs.rcs.service.api.client.messaging.InstantMessage;
import com.orangelabs.rcs.service.api.client.messaging.MessagingApi;

/**
 * Rejoin a group chat session
 */
public class RejoinChat extends Activity implements ImsEventListener {
	/**
	 * Last group chat session id
	 */
	public final static String REGISTRY_CHAT_ID = "ChatID";
	
    /**
     * UI handler
     */
    private Handler handler = new Handler();

    /**
     * Registry
     */
	private Registry registry;

    /**
     * Progress dialog
     */
    private Dialog progressDialog = null;

    /**
	 * Chat ID to rejoin
	 */
	private String chatId = null;
	
	/**
	 * Rejoined chta session
	 */
	private IChatSession chatSession = null; 
	
    /**
	 * Messaging API
	 */
    private MessagingApi messagingApi;

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	    // Instanciate registry
		registry = new Registry(this);

		// Set layout
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.messaging_rejoin);
        
        // Set title
        setTitle(R.string.menu_rejoin_chat);
        
        // Display chat ID to rejoin
		chatId = registry.readString(REGISTRY_CHAT_ID, null);
    	TextView idEdit = (TextView)findViewById(R.id.session);
    	idEdit.setText(chatId);

    	// Instanciate messaging API
        messagingApi = new MessagingApi(getApplicationContext());
        messagingApi.connectApi();
        messagingApi.addImsEventListener(this);

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
    	if ((!connected) || (chatId == null) || (chatId.length() == 0)) {
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
     * IMS connected
     */
	public void handleImsConnected() {
		handler.post(new Runnable(){
			public void run(){
		        Button rejoinBtn = (Button)findViewById(R.id.rejoin_btn);
		    	if ((chatId == null) || (chatId.length() == 0)) {
		    		rejoinBtn.setEnabled(false);
		    	} else {
		    		rejoinBtn.setEnabled(true);
		    	}
			}
		});
	}

    /**
     * IMS disconnected
     */
	public void handleImsDisconnected() {
		handler.post(new Runnable(){
			public void run(){
		        Button rejoinBtn = (Button)findViewById(R.id.rejoin_btn);
		        rejoinBtn.setEnabled(false);
			}
		});
	}
	
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
            		chatSession = messagingApi.rejoinChatGroupSession(chatId);
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
					
					// Reset the chat ID
					registry.writeString(REGISTRY_CHAT_ID, "");
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
}
