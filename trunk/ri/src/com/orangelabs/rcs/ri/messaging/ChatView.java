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

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.BufferType;

import com.orangelabs.rcs.core.ims.service.im.InstantMessage;
import com.orangelabs.rcs.ri.R;
import com.orangelabs.rcs.ri.utils.SmileyParser;
import com.orangelabs.rcs.ri.utils.Smileys;
import com.orangelabs.rcs.ri.utils.Utils;
import com.orangelabs.rcs.service.api.client.ClientApiListener;
import com.orangelabs.rcs.service.api.client.messaging.IChatEventListener;
import com.orangelabs.rcs.service.api.client.messaging.IChatSession;
import com.orangelabs.rcs.service.api.client.messaging.MessagingApi;
import com.orangelabs.rcs.utils.PhoneUtils;

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
    
    /**
     * Utility class to manage IsComposing status
     */
	private IsComposingManager composingManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set layout
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.messaging_chat_view);
        
        // Set the message list adapter
        mAdapter = new ArrayAdapter<String>(this,  android.R.layout.simple_list_item_1, mStrings) {
        	@Override
        	public View getView(int position, View convertView, android.view.ViewGroup parent) {
        		TextView v = (TextView)super.getView(position, convertView, parent);
        		v.setText(formatMessage(v.getText().toString()), BufferType.SPANNABLE);
        		return v;
        	};
        };
        setListAdapter(mAdapter);
        
    	// Instanciate the composing manager
		composingManager = new IsComposingManager();
        
        // Set message composer callbacks
        mUserText = (EditText)findViewById(R.id.userText);
        mUserText.setOnClickListener(this);
        mUserText.setOnKeyListener(this);
        mUserText.addTextChangedListener(mUserTextWatcher);
        
        // Get chat session-ID
		sessionId = getIntent().getStringExtra("sessionId");
        
        // Get remote contact
		remote = getIntent().getStringExtra("contact");

		// Set UI title
        setTitle(getString(R.string.title_chat_view) + " " + PhoneUtils.extractNumberFromUri(remote));
		
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
        
        // Warn the composing manager that the message was sent
		composingManager.messageWasSent();
        try {
        	// Send the text to remote
        	chatSession.sendMessage(text);
        	
        	// Add text to the message history
            mAdapter.add(">> " + text);
            mUserText.setText(null);
        } catch(Exception e) {
        	Utils.showMessage(ChatView.this, getString(R.string.label_send_im_failed));
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
     * API disabled
     */
    public void handleApiDisabled() {
		handler.post(new Runnable() { 
			public void run() {
				Utils.showMessageAndExit(ChatView.this, getString(R.string.label_api_disabled));
			}
		});
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
	    			Utils.showMessageAndExit(ChatView.this, getString(R.string.label_api_failed));
	    		}
			}
		});
    }

    /**
     * API disconnected
     */
    public void handleApiDisconnected() {
		handler.post(new Runnable(){
			public void run(){
				Utils.showMessageAndExit(ChatView.this, getString(R.string.label_api_disconnected));
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
					// Session aborted
					Utils.showMessageAndExit(ChatView.this, getString(R.string.label_chat_aborted));
				}
			});
		}
	    
		// Session has been terminated
		public void handleSessionTerminated() {
			handler.post(new Runnable(){
				public void run(){
					// Exit activity
					finish();
				}
			});
		}
	    
		// Session has been terminated by remote
		public void handleSessionTerminatedByRemote() {
			handler.post(new Runnable(){
				public void run(){
					Utils.showMessageAndExit(ChatView.this, getString(R.string.label_chat_terminated));
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
		public void handleIsComposingEvent(String contact, final boolean isComposing) {
			handler.post(new Runnable(){
				public void run(){
					View view = (View)findViewById(R.id.isComposingText);
					if(isComposing){
						view.setVisibility(View.VISIBLE);
					} else {
						view.setVisibility(View.GONE);
					}
				}
			});
			
		}
		
		// Chat error
		public void handleImError(int error) {
			handler.post(new Runnable(){
				public void run(){
					Utils.showMessageAndExit(ChatView.this, getString(R.string.label_chat_failed));
				}
			});
		}		
    };
    
    /**
     * Format text message
     * 
	 * @param txt Text
	 * @return Formatted message
	 */
	private CharSequence formatMessage(String txt) {
		SpannableStringBuilder buf = new SpannableStringBuilder();
		if (!TextUtils.isEmpty(txt)) {
			Smileys smileyResources = new Smileys(this);
			SmileyParser smileyParser = new SmileyParser(txt, smileyResources);
			smileyParser.parse();
			buf.append(smileyParser.getSpannableString(this));
		}
		return buf;
	}
    
	/**
	 * Close the chat session
	 */
	private void closeSession() {
    	// TODO: add a thread
        if (chatSession != null) {
        	try {
        		chatSession.removeSessionListener(chatSessionListener);
        		chatSession.cancelSession();
        	} catch(Exception e) {
        	}
        	chatSession = null;
        }
	}
	
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
    			if (sessionId!=null){
    				AlertDialog.Builder builder = new AlertDialog.Builder(this);
    				builder.setTitle(getString(R.string.title_chat_exit));
    				builder.setPositiveButton(getString(R.string.label_ok), new DialogInterface.OnClickListener() {
    					public void onClick(DialogInterface dialog, int which) {
    		            	// Close the session
    		            	closeSession();
    		            	
    		            	// Exit activity
    		                finish();
    					}
    				});
    				builder.setNegativeButton(getString(R.string.label_cancel), null);
    				builder.setCancelable(true);
    				builder.show();
    			} else {
                	// Exit activity
    				finish();
    			}
                return true;
        }

        return super.onKeyDown(keyCode, event);
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater=new MenuInflater(getApplicationContext());
		inflater.inflate(R.menu.menu_chat, menu);
		return true;
	}
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_insert_smiley:
			Smileys.showSmileyDialog(
					this, 
					mUserText, 
					getResources(), 
					getString(R.string.menu_insert_smiley));
			break;
			
		case R.id.menu_add_participant:
			Toast.makeText(this, getString(R.string.label_not_implemented), Toast.LENGTH_LONG).show();
			break;
			
		case R.id.menu_close_session:
			if (sessionId!=null){
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(getString(R.string.title_chat_exit));
				builder.setPositiveButton(getString(R.string.label_ok), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
		            	// Close the session
		            	closeSession();
		            	
		            	// Exit activity
		                finish();
					}
				});
				builder.setNegativeButton(getString(R.string.label_cancel), null);
				builder.setCancelable(true);
				builder.show();
			} else {
            	// Exit activity
				finish();
			}
			break;
		}
		return true;
	}
    
    /**********************************************************************
     ******************	Deals with isComposing feature ********************
     **********************************************************************/
    
    private final TextWatcher mUserTextWatcher = new TextWatcher(){
		@Override
		public void afterTextChanged(Editable s) {
			// Check if the text is not null.
			// we do not wish to consider putting the edit text back to null (like when sending message), is having activity 
			if (s.length()>0){
				// Warn the composing manager that we have some activity
				composingManager.hasActivity();
			}
		}
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {}
    };
    
	public void setTypingStatus(boolean isTyping){
		if (chatSession!=null){
			try {
				chatSession.setIsComposingStatus(isTyping);
			} catch (RemoteException e) {
			}
		}
	}
	
    /**
	 * Utility class to handle is_typing timers (see RFC3994)  
	 */
	private class IsComposingManager{

		// Idle time out (in ms)
		final static int IDLE_TIME_OUT = 5*1000;

		// Active state refresh interval (in ms)
		final static int ACTIVE_STATE_REFRESH = 60*1000; 

		private ClockHandler handler = new ClockHandler();

		// Is composing state
		public boolean isComposing = false;

		private final static int IS_STARTING_COMPOSING = 1;
		private final static int IS_STILL_COMPOSING = 2;
		private final static int MESSAGE_WAS_SENT = 3;
		private final static int ACTIVE_MESSAGE_NEEDS_REFRESH = 4;
		private final static int IS_IDLE = 5;

		private class ClockHandler extends Handler{

			//TODO handle when several chat sessions are active
			public void handleMessage(Message msg){
				switch(msg.what){
					case IS_STARTING_COMPOSING :{
						// Send a typing status "active"
						ChatView.this.setTypingStatus(true);
	
						// In IDLE_TIME_OUT we will need to send a is-idle status message 
						handler.sendEmptyMessageDelayed(IS_IDLE, IDLE_TIME_OUT);
	
						// In ACTIVE_STATE_REFRESH we will need to send an active status message refresh
						handler.sendEmptyMessageDelayed(ACTIVE_MESSAGE_NEEDS_REFRESH, ACTIVE_STATE_REFRESH);
						break;
					}    			
					case IS_STILL_COMPOSING :{
						// Cancel the IS_IDLE messages in queue, if there was one
						handler.removeMessages(IS_IDLE);
	
						// In IDLE_TIME_OUT we will need to send a is-idle status message
						handler.sendEmptyMessageDelayed(IS_IDLE, IDLE_TIME_OUT);
						break;
					}
					case MESSAGE_WAS_SENT :{
						// We are now going to idle state
						composingManager.hasNoActivity();
	
						// Cancel the IS_IDLE messages in queue, if there was one
						handler.removeMessages(IS_IDLE);
	
						// Cancel the ACTIVE_MESSAGE_NEEDS_REFRESH messages in queue, if there was one
						handler.removeMessages(ACTIVE_MESSAGE_NEEDS_REFRESH);
						break;
					}	    			
					case ACTIVE_MESSAGE_NEEDS_REFRESH :{
						// We have to refresh the "active" state
						ChatView.this.setTypingStatus(true);
	
						// In ACTIVE_STATE_REFRESH we will need to send an active status message refresh
						handler.sendEmptyMessageDelayed(ACTIVE_MESSAGE_NEEDS_REFRESH, ACTIVE_STATE_REFRESH);
						break;
					}
					case IS_IDLE :{
						// End of typing
						composingManager.hasNoActivity();
	
						// Send a typing status "idle"
						ChatView.this.setTypingStatus(false);
	
						// Cancel the ACTIVE_MESSAGE_NEEDS_REFRESH messages in queue, if there was one
						handler.removeMessages(ACTIVE_MESSAGE_NEEDS_REFRESH);
						break;
					}
				}
			}
		}

		/**
		 * Edit text has activity
		 */
		public void hasActivity(){
			// We have activity on the edit text
			if (!isComposing){
				// If we were not already in isComposing state
				handler.sendEmptyMessage(IS_STARTING_COMPOSING);
				isComposing = true;
			}else{
				// We already were composing
				handler.sendEmptyMessage(IS_STILL_COMPOSING);
			}
		}

		/**
		 * Edit text has no activity anymore
		 */
		public void hasNoActivity(){
			isComposing = false;
		}

		/**
		 * The message was sent
		 */
		public void messageWasSent(){
			handler.sendEmptyMessage(MESSAGE_WAS_SENT);
		}
	}
}
