/*******************************************************************************
 * Software Name : RCS IMS Stack
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
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.orangelabs.rcs.core.ims.service.im.chat.ChatError;
import com.orangelabs.rcs.ri.R;
import com.orangelabs.rcs.ri.utils.Utils;
import com.orangelabs.rcs.service.api.client.messaging.IChatEventListener;
import com.orangelabs.rcs.service.api.client.messaging.IChatSession;
import com.orangelabs.rcs.service.api.client.messaging.InstantMessage;
import com.orangelabs.rcs.service.api.client.messaging.MessagingApi;

/**
 * Initiate chat group
 * 
 * @author jexa7410
 */
public class InitiateChatGroup extends Activity implements OnItemClickListener {

    /**
     * UI handler
     */
    private Handler handler = new Handler();

    /**
	 * Messaging API
	 */
    private MessagingApi messagingApi;
    
    /**
     * Chat session 
     */
    private IChatSession chatSession = null;

    /**
     * Progress dialog
     */
    private Dialog progressDialog = null;
    
    /**
     * List of participants
     */
    final List<String> participants = new ArrayList<String>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set layout
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.messaging_initiate_group_chat);
        
        // Set title
        setTitle(R.string.menu_adhoc_group_chat);
        
        // Set contact selector
        ListView contactList = (ListView)findViewById(R.id.contacts);
        contactList.setAdapter(Utils.createMultiContactListAdapter(this));
        contactList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        contactList.setOnItemClickListener(this);
        
        // Set button callback
        Button inviteBtn = (Button)findViewById(R.id.invite_btn);
        inviteBtn.setOnClickListener(btnInviteListener);
    	inviteBtn.setEnabled(false);
               
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
    		// Initiate the chat session in background
            Thread thread = new Thread() {
            	public void run() {
                	try {
	            		chatSession = messagingApi.initiateAdhocGroupChatSession(participants, "");
	            		chatSession.addSessionListener(chatSessionListener);
	            	} catch(Exception e) {
	            		handler.post(new Runnable(){
	            			public void run(){
	            				Utils.showMessageAndExit(InitiateChatGroup.this, getString(R.string.label_invitation_failed));		
	            			}
	            		});
	            	}
            	}
            };
            thread.start();

            // Display a progress dialog
            progressDialog = Utils.showProgressDialog(InitiateChatGroup.this, getString(R.string.label_command_in_progress));            
        }
    };
           
	/**
	 * Hide progress dialog
	 */
    public void hideProgressDialog() {
		if (progressDialog != null) {
			progressDialog.dismiss();
			progressDialog = null;
		}
    }    
    
    /**
     * Chat session event listener
     */
    private IChatEventListener chatSessionListener = new IChatEventListener.Stub() {
		// Session is started
		public void handleSessionStarted() {
			try {
				handler.post(new Runnable() { 
					public void run() {
						// Hide progress dialog
						hideProgressDialog();
					}
				});
				
				// Display chat view
	        	Intent intent = new Intent(InitiateChatGroup.this, ChatView.class);
	        	intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	        	intent.putExtra("sessionId", chatSession.getSessionID());
	        	startActivity(intent);
	        	
	        	// Exit activity
	        	finish();
			} catch(Exception e) {
				handler.post(new Runnable() { 
					public void run() {
						Utils.showMessageAndExit(InitiateChatGroup.this, getString(R.string.label_api_failed));
					}
				});
			}
		}
	
		// Session has been aborted
		public void handleSessionAborted() {
			handler.post(new Runnable(){
				public void run(){
					// Hide progress dialog
					hideProgressDialog();

					// Show message
					Utils.showMessageAndExit(InitiateChatGroup.this, getString(R.string.label_invitation_declined));
				}
			});
		}
	    
		// Session has been terminated by remote
		public void handleSessionTerminatedByRemote() {
			handler.post(new Runnable(){
				public void run(){
					// Hide progress dialog
					hideProgressDialog();

					// Show message
					Utils.showMessageAndExit(InitiateChatGroup.this, getString(R.string.label_sharing_terminated_by_remote));
				}
			});
		}
		
		// New text message received
		public void handleReceiveMessage(final InstantMessage msg) {
		}		
		
		// Chat error
		public void handleImError(final int error) {
			handler.post(new Runnable(){
				public void run(){
					// Hide progress dialog
					hideProgressDialog();

					// Show error
					if (error == ChatError.SESSION_INITIATION_DECLINED) {
						Utils.showMessageAndExit(InitiateChatGroup.this, getString(R.string.label_invitation_declined));
					} else {
						Utils.showMessageAndExit(InitiateChatGroup.this, getString(R.string.label_invitation_failed));
					}
				}
			});
		}		
		
		// Is composing event
		public void handleIsComposingEvent(String contact, boolean isComposing) {
		}

		// Conference event
	    public void handleConferenceEvent(String contact, String state) {
		}
	    
		// Message delivery status
		public void handleMessageDeliveryStatus(String msgId, String status) {
		}
		
		// Request to add participant is successful
		public void handleAddParticipantSuccessful() {
		}
	    
		// Request to add participant has failed
		public void handleAddParticipantFailed(String reason) {
		}
    };
    
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
		// Check if number is in participants list
		String number = (String)view.getTag();
		if (participants.contains(number)){
			// Number is in list, we remove it
			participants.remove(number);
		} else {
			// Number is not in list, add it
			participants.add(number);
		}
		
		// Disable the invite button if no contact selected
        Button inviteBtn = (Button)findViewById(R.id.invite_btn);
        if (participants.size() == 0) {
        	inviteBtn.setEnabled(false);
		} else {
        	inviteBtn.setEnabled(true);
        }		
	}
	
    /**
     * Quit the session
     */
    private void quitSession() {
		// Stop session
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
    }    

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
				// Quit session
            	quitSession();
                return true;
        }

        return super.onKeyDown(keyCode, event);
    }
}    
