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
package com.orangelabs.rcs.samples.im;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.orangelabs.rcs.samples.R;
import com.orangelabs.rcs.samples.utils.Utils;
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
     * Chat session
     */
    private IChatSession chatSession = null;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.messaging_receive_chat);
        
        // Set UI title
        setTitle(R.string.title_recv_chat);
        
        // Get chat session-ID
		sessionId = getIntent().getStringExtra("sessionId");
        
		// Display remote contact
        TextView from = (TextView)findViewById(R.id.from);
		String remoteContact = getIntent().getStringExtra("contact");
        from.setText(getString(R.string.label_from) + " " + remoteContact);
        
		// Set buttons callback
        Button acceptButton = (Button)findViewById(R.id.acceptBtn);
        acceptButton.setOnClickListener(acceptBtnListener);
        Button declineButton = (Button)findViewById(R.id.declineBtn);
        declineButton.setOnClickListener(declineBtnListener);
        
		// Instanciate messaging API
		messagingApi = new MessagingApi(getApplicationContext());
		messagingApi.connectApi();
		messagingApi.addApiEventListener(this);
    }

    /**
     * API connected
     */
    public void handleApiConnected() {
		try{
			// Get the chat session
			chatSession = messagingApi.getChatSession(sessionId);
		} catch(Exception e) {
			Utils.showError(ReceiveChat.this, getString(R.string.api_failed)+": "+e);
		}
    }

    /**
     * API disconnected
     */
    public void handleApiDisconnected() {
    	// Service has been disconnected
		handler.post(new Runnable(){
			public void run(){
				Utils.showInfo(ReceiveChat.this, getString(R.string.api_failed));
			}
		});
    }
    
    /**
     * Accept button listener
     */
    private OnClickListener acceptBtnListener = new OnClickListener() {
        public void onClick(View v) {
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
        public void onClick(View v) {
            Thread thread = new Thread() {
            	public void run() {
                	try {
                		// Reject the invitation
            			chatSession.rejectSession();
	            	} catch(Exception e) {
	    		    	Utils.showError(ReceiveChat.this, getString(R.string.label_invitation_failed));
	            	}
            	}
            };
            thread.start();
        }
    };
}

