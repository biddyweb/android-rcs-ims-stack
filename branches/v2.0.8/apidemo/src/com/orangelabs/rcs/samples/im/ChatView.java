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

import java.util.ArrayList;

import android.app.ListActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import com.orangelabs.rcs.core.ims.service.im.InstantMessage;
import com.orangelabs.rcs.samples.R;
import com.orangelabs.rcs.samples.utils.Utils;
import com.orangelabs.rcs.service.api.client.ClientApiListener;
import com.orangelabs.rcs.service.api.client.messaging.IChatEventListener;
import com.orangelabs.rcs.service.api.client.messaging.IChatSession;
import com.orangelabs.rcs.service.api.client.messaging.MessagingApi;

/**
 * Chat view
 */
public class ChatView extends ListActivity implements OnClickListener, OnKeyListener, ClientApiListener  {
	/**
	 * Message composer
	 */
    private EditText mUserText;
    
    /**
     * Message history adapter
     */
    private ArrayAdapter<String> mAdapter;
    
    /**
     * Message history
     */
    private ArrayList<String> mStrings = new ArrayList<String>();
    
    /**
	 * Messaging API
	 */
    private MessagingApi messagingApi;
    
    /**
     * Chat session 
     */
    private IChatSession chatSession = null;

    /**
     * Session ID 
     */
    private String sessionId;
    
    /**
     * Remote contact 
     */
    private String remote;

    /**
     * UI handler
     */
    private Handler handler = new Handler();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.messaging_chat_view);
        
        // Set the message list adapter
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mStrings);
        setListAdapter(mAdapter);
        
        // Set message composer callbacks
        mUserText = (EditText)findViewById(R.id.userText);
        mUserText.setOnClickListener(this);
        mUserText.setOnKeyListener(this);
        
        // Get chat session-ID
		sessionId = getIntent().getStringExtra("sessionId");
        
        // Get remote contact
		remote = getIntent().getStringExtra("contact");

		// Set UI title
        setTitle(getString(R.string.title_chat_view) + " " + remote);
		
		// Instanciate messaging API
        messagingApi = new MessagingApi(getApplicationContext());
        messagingApi.addApiEventListener(this);
		messagingApi.connectApi();
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
        messagingApi.removeApiEventListener(this);
        messagingApi.disconnectApi();
    }
    
    /**
     * Message composer listener
     * 
     * @param v View
     */
    public void onClick(View v) {
        sendText();
    }

    /**
     * Message composer listener
     * 
     * @param v View
     * @param keyCode Key code
     * @event Key event
     */
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_CENTER:
                case KeyEvent.KEYCODE_ENTER:
                    sendText();
                    return true;
            }
        }
        return false;
    }
    
    /**
     * Send a text and display it
     */
    private void sendText() {
    	if (chatSession ==  null) {
    		return;
    	}
    	
        String text = mUserText.getText().toString();
        if ((text == null) || (text.length() == 0)) {
        	return;
        }
        
        try {
        	// Send the text to remote
        	chatSession.sendMessage(text);
        	
        	// Add text to the message history
            mAdapter.add(">> " + text);
            mUserText.setText(null);
        } catch(Exception e) {
			Utils.showInfo(ChatView.this, getString(R.string.label_chat_failed));
        }
    }

    /**
     * Receive a text and display it
     * 
     * @param txt Text
     */
    private void receiveText(String text) {
    	// Add text to the message history
        mAdapter.add("<< " + text);
    }
    
    /**
     * API connected
     */
    public void handleApiConnected() {
		handler.post(new Runnable() { 
			public void run() {
	    		try {
	    			// Register to receive session events
					chatSession = messagingApi.getChatSession(sessionId);    			
					chatSession.addSessionListener(chatSessionListener);
	    		} catch(Exception e) {
					Utils.showInfo(ChatView.this, getString(R.string.label_chat_failed));
	    		}
			}
		});
    }

    /**
     * API disconnected
     */
    public void handleApiDisconnected() {
    	// Service has been disconnected
		handler.post(new Runnable(){
			public void run(){
				Utils.showInfo(ChatView.this, getString(R.string.api_failed));
			}
		});
    }  
    
    /**
     * Chat session event listener
     */
    private IChatEventListener chatSessionListener = new IChatEventListener.Stub() {
		// Session is started
		public void handleSessionStarted() {
		}
	
		// Session has been aborted
		public void handleSessionAborted() {
			handler.post(new Runnable(){
				public void run(){
					Utils.showInfo(ChatView.this, getString(R.string.label_chat_aborted));
				}
			});
		}
	    
		// Session has been terminated
		public void handleSessionTerminated() {
			handler.post(new Runnable(){
				public void run(){
					Utils.showInfo(ChatView.this, getString(R.string.label_chat_terminated));
				}
			});
		}
	    
		// Session has been terminated by remote
		public void handleSessionTerminatedByRemote() {
			handler.post(new Runnable(){
				public void run(){
					Utils.showInfo(ChatView.this, getString(R.string.label_chat_terminated));
				}
			});
		}
		
		// New text message received
		public void handleReceiveMessage(final InstantMessage msg) {
			handler.post(new Runnable() { 
				public void run() {
					receiveText(msg.getTextMessage());
				}
			});
		}		
		
		// Is composing event
		public void handleIsComposingEvent(String contact, boolean isComposing) {
			// TODO: display something on the UI
		}
		
		// Chat error
		public void handleImError(int error) {
			handler.post(new Runnable(){
				public void run(){
					Utils.showInfo(ChatView.this, getString(R.string.label_chat_failed));
				}
			});
		}		
    };
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
            	// Stop the session
                if (chatSession != null) {
                	try {
                		chatSession.cancelSession();
                		chatSession.removeSessionListener(chatSessionListener);
                	} catch(Exception e) {
                	}
                }
                finish();
                return true;
        }

        return super.onKeyDown(keyCode, event);
    }
}
