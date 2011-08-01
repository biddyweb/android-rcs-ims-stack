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

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract.Data;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.orangelabs.rcs.core.ims.service.im.chat.ChatError;
import com.orangelabs.rcs.ri.R;
import com.orangelabs.rcs.ri.utils.Utils;
import com.orangelabs.rcs.service.api.client.messaging.IChatEventListener;
import com.orangelabs.rcs.service.api.client.messaging.IChatSession;
import com.orangelabs.rcs.service.api.client.messaging.InstantMessage;
import com.orangelabs.rcs.service.api.client.messaging.MessagingApi;

/**
 * Initiate chat
 * 
 * @author jexa7410
 */
public class InitiateChat extends Activity {

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
       
        // Select the corresponding contact from the intent
        Intent intent = getIntent();
        Uri contactUri = intent.getData();
    	if (contactUri != null) {
	        Cursor cursor = managedQuery(contactUri, null, null, null, null);
	        if (cursor.moveToNext()) {
	        	String selectedContact = cursor.getString(cursor.getColumnIndex(Data.DATA1));
	            if (selectedContact != null) {
	    	        for (int i=0;i<spinner.getAdapter().getCount();i++) {
	    	        	MatrixCursor cursor2 = (MatrixCursor)spinner.getAdapter().getItem(i);
	    	        	if (selectedContact.equalsIgnoreCase(cursor2.getString(1))) {
	    	        		// Select contact
	    	                spinner.setSelection(i);
	    	                spinner.setEnabled(false);
	    	                break;
	    	        	}
	    	        }
	            }
	        }
	        cursor.close();
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
     * Invite button listener
     */
    private OnClickListener btnInviteListener = new OnClickListener() {
        public void onClick(View v) {
            // Get remote contact
        	Spinner spinner = (Spinner)findViewById(R.id.contact);
        	MatrixCursor cursor = (MatrixCursor)spinner.getSelectedItem();
            final String remote = cursor.getString(1);

            EditText firstMessageText = (EditText)findViewById(R.id.firstMessage);
            final String firstMessage = firstMessageText.getText().toString().trim();
            
    		// Initiate the chat session in background
            Thread thread = new Thread() {
            	public void run() {
                	try {
	            		chatSession = messagingApi.initiateOne2OneChatSession(remote, firstMessage);
	            		chatSession.addSessionListener(chatSessionListener);
	            	} catch(Exception e) {
	            		handler.post(new Runnable(){
	            			public void run(){
	            				Utils.showMessageAndExit(InitiateChat.this, getString(R.string.label_invitation_failed));		
	            			}
	            		});
	            	}
            	}
            };
            thread.start();

            // Display a progress dialog
            progressDialog = Utils.showProgressDialog(InitiateChat.this, getString(R.string.label_command_in_progress));            
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
				
				// Get subject
	            EditText firstMessageText = (EditText)findViewById(R.id.firstMessage);
	            final String firstMessage = firstMessageText.getText().toString().trim();
	            // Get remote contact
	        	Spinner spinner = (Spinner)findViewById(R.id.contact);
	        	MatrixCursor cursor = (MatrixCursor)spinner.getSelectedItem();
	            final String remote = cursor.getString(1);

				// Display chat view
	        	Intent intent = new Intent(InitiateChat.this, ChatView.class);
	        	intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	        	intent.putExtra("sessionId", chatSession.getSessionID());
	        	intent.putExtra("subject", firstMessage);
	        	intent.putExtra("originating", true);
	        	intent.putExtra("contact", remote);
	        	startActivity(intent);
	        	
	        	// Exit activity
	        	finish();
			} catch(Exception e) {
				handler.post(new Runnable(){
					public void run(){
						Utils.showMessageAndExit(InitiateChat.this, getString(R.string.label_api_failed));
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
					Utils.showMessageAndExit(InitiateChat.this, getString(R.string.label_invitation_declined));
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
					Utils.showMessageAndExit(InitiateChat.this, getString(R.string.label_sharing_terminated_by_remote));
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
						Utils.showMessageAndExit(InitiateChat.this, getString(R.string.label_invitation_declined));
					} else {
						Utils.showMessageAndExit(InitiateChat.this, getString(R.string.label_invitation_failed));
					}
				}
			});
		}		
		
		// Is composing event
		public void handleIsComposingEvent(String contact, boolean isComposing) {
		}

		// Conference event
	    public void handleConferenceEvent(String contact,  String contactDisplayname, String state) {
		}
	    
		// Message delivery status
		public void handleMessageDeliveryStatus(String msgId, String contact, String status) {
		}
		
		// Request to add participant is successful
		public void handleAddParticipantSuccessful() {
		}
	    
		// Request to add participant has failed
		public void handleAddParticipantFailed(String reason) {
		}
    };
    
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
