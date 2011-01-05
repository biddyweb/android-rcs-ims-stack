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
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.CursorWrapper;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.orangelabs.rcs.core.ims.service.im.InstantMessage;
import com.orangelabs.rcs.core.ims.service.im.InstantMessageError;
import com.orangelabs.rcs.ri.R;
import com.orangelabs.rcs.ri.utils.Utils;
import com.orangelabs.rcs.service.api.client.messaging.IChatEventListener;
import com.orangelabs.rcs.service.api.client.messaging.IChatSession;
import com.orangelabs.rcs.service.api.client.messaging.MessagingApi;

/**
 * Initiate chat
 * 
 * @author jexa7410
 */
public class InitiateChat extends Activity {

    /**
	 * Messaging API
	 */
    private MessagingApi messagingApi;
    
    /**
     * Chat session 
     */
    private IChatSession chatSession = null;

    /**
     * UI handler
     */
    private Handler handler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set layout
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.messaging_initiate_chat);
        
        // Set title
        setTitle(R.string.menu_one_to_one_chat);
        
        // Set contact selector
        Spinner spinner = (Spinner)findViewById(R.id.contact);
        spinner.setAdapter(Utils.createContactListAdapter(this));
        
        // Set button callback
        Button inviteBtn = (Button)findViewById(R.id.invite_btn);
        inviteBtn.setOnClickListener(btnInviteListener);
               
        // Disable button if no contact available
        if (spinner.getAdapter().getCount() == 0) {
        	inviteBtn.setEnabled(false);
        }
        
        // Instanciate messaging API
        messagingApi = new MessagingApi(getApplicationContext());
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
        messagingApi.disconnectApi();
    }

    /**
     * Invite button listener
     */
    private OnClickListener btnInviteListener = new OnClickListener() {
        public void onClick(View v) {
            // Get remote contact
        	Spinner spinner = (Spinner)findViewById(R.id.contact);
            CursorWrapper cursor = (CursorWrapper)spinner.getSelectedItem();
            final String remote = cursor.getString(1);
            
            // Get chat subject
            EditText subjectEdit = (EditText)findViewById(R.id.subject);
            final String subject = subjectEdit.getText().toString();
            
            Thread thread = new Thread() {
            	public void run() {
                	try {
                		// Initiate the chat
	            		chatSession = messagingApi.initiateOne2OneChatSession(remote, subject);
	            		chatSession.addSessionListener(chatSessionListener);
	            	} catch(Exception e) {
	            		handler.post(new Runnable(){
	            			public void run(){
	            				Utils.showError(InitiateChat.this, getString(R.string.label_invitation_failed));		
	            			}
	            		});
	            	}
            	}
            };
            thread.start();
        }
    };
           
    /**
     * Chat session event listener
     */
    private IChatEventListener chatSessionListener = new IChatEventListener.Stub() {
		// Session is started
		public void handleSessionStarted() {
			try {
				// Display chat view
	        	Intent intent = new Intent(InitiateChat.this, ChatView.class);
	        	intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	        	intent.putExtra("contact", chatSession.getRemoteContact());
	        	intent.putExtra("sessionId", chatSession.getSessionID());
	        	startActivity(intent);
	        	
	        	// Exit activity
	        	finish();
			} catch(Exception e) {
				Utils.showError(InitiateChat.this, getString(R.string.label_invitation_failed));		
			}
		}
	
		// Session has been aborted
		public void handleSessionAborted() {
			handler.post(new Runnable(){
				public void run(){
					Utils.showError(InitiateChat.this, getString(R.string.label_invitation_declined));
				}
			});
		}
	    
		// Session has been terminated
		public void handleSessionTerminated() {
		}
	    
		// Session has been terminated by remote
		public void handleSessionTerminatedByRemote() {
			handler.post(new Runnable(){
				public void run(){
					Utils.showError(InitiateChat.this, getString(R.string.label_sharing_terminated_by_remote));
				}
			});
		}
		
		// New text message received
		public void handleReceiveMessage(final InstantMessage msg) {
		}		
		
		// Is composing event
		public void handleIsComposingEvent(String contact, boolean isComposing) {
		}
		
		// Chat error
		public void handleImError(final int error) {
			handler.post(new Runnable(){
				public void run(){
					if (error == InstantMessageError.SESSION_INITIATION_DECLINED) {
						Utils.showError(InitiateChat.this, getString(R.string.label_invitation_declined));
					} else {
						Utils.showError(InitiateChat.this, getString(R.string.label_invitation_failed));
					}
				}
			});
		}		
    };
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                Thread thread = new Thread() {
                	public void run() {
                    	try {
	                        if (chatSession != null) {
	                        	try {
	                        		chatSession.removeSessionListener(chatSessionListener);
	                        		chatSession.cancelSession();
	                        	} catch(Exception e) {
	                        	}
	                    		chatSession = null;
	                        }
                    	} catch(Exception e) {
                    	}
                	}
                };
                thread.start();
            	
                // Exit activity
    			finish();
                return true;
        }

        return super.onKeyDown(keyCode, event);
    }
}    
