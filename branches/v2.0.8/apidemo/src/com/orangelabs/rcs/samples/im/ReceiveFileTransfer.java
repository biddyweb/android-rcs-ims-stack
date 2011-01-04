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

import java.io.File;
import java.io.FileInputStream;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.orangelabs.rcs.core.ims.service.sharing.ContentSharingError;
import com.orangelabs.rcs.provider.messaging.RichMessagingData;
import com.orangelabs.rcs.samples.R;
import com.orangelabs.rcs.samples.utils.Utils;
import com.orangelabs.rcs.service.api.client.ClientApiListener;
import com.orangelabs.rcs.service.api.client.messaging.IFileTransferEventListener;
import com.orangelabs.rcs.service.api.client.messaging.IFileTransferSession;
import com.orangelabs.rcs.service.api.client.messaging.MessagingApi;

/**
 * Received file transfer
 * 
 * @author jexa7410
 */
public class ReceiveFileTransfer extends Activity implements ClientApiListener {
	/**
	 * Messaging API
	 */
	private MessagingApi messagingApi;
    
	/**
	 * Session ID
	 */
    private String sessionId;
    
    /**
     * File transfer session
     */
    private IFileTransferSession transferSession = null;
    
    /**
     * UI handler
     */
    private final Handler handler = new Handler();
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.messaging_receive_filetransfer);
        
        // Set UI title
        setTitle(R.string.title_recv_file_transfer);
        
        // Get file transfer session ID
		sessionId = getIntent().getStringExtra("sessionId");
        
		// Display remote conatct
        TextView from = (TextView)findViewById(R.id.from);
		String remoteContact = getIntent().getStringExtra("contact");
        from.setText(getString(R.string.label_from) + " " + remoteContact);
        
        // Display filename properties
        String fileTransferName = "";
        long fileTransferSize = 0;
    	Cursor sessionRequest = managedQuery(RichMessagingData.CONTENT_URI, 
    			new String[]{RichMessagingData.KEY_NAME, RichMessagingData.KEY_SIZE}, 
    			RichMessagingData.KEY_SESSION_ID + " = " + sessionId, 
    			null, 
    			null);
    	if (sessionRequest.moveToFirst()){
    		fileTransferName = sessionRequest.getString(0);
    		fileTransferSize = sessionRequest.getLong(1);
    	}

    	TextView name = (TextView)findViewById(R.id.image_name);
    	name.setText(getString(R.string.label_image_name, fileTransferName));

    	TextView size = (TextView)findViewById(R.id.image_size);
    	size.setText(getString(R.string.label_image_size, " "+((fileTransferSize-1)/1024 +1), " KB"));

    	// Set buttons callback
        Button acceptButton = (Button)findViewById(R.id.acceptBtn);
        acceptButton.setOnClickListener(acceptBtnListener);
        Button declineButton = (Button)findViewById(R.id.declineBtn);
        declineButton.setOnClickListener(declineBtnListener);

        // Instanciate messaging API
		messagingApi = new MessagingApi(getApplicationContext());
		messagingApi.addApiEventListener(this);
		messagingApi.connectApi();
    }

    /**
     * API connected
     */
    public void handleApiConnected() {
		try {
			// Get the file transfer session
    		transferSession = messagingApi.getFileTransferSession(sessionId);
			transferSession.addSessionListener(fileTransferSessionListener);
		} catch(Exception e) {
    		e.printStackTrace();
			Utils.showError(ReceiveFileTransfer.this, getString(R.string.api_failed)+": "+e);
		}
    }

    /**
     * API disconnected
     */
    public void handleApiDisconnected() {
    	// Service has been disconnected
		handler.post(new Runnable(){
			public void run(){
				Utils.showInfo(ReceiveFileTransfer.this, getString(R.string.api_failed));
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
            			transferSession.acceptSession();
	            	} catch(Exception e) {
	    		    	Utils.showError(ReceiveFileTransfer.this, getString(R.string.label_invitation_failed));
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
            			transferSession.rejectSession();
	            	} catch(Exception e) {
	    		    	Utils.showError(ReceiveFileTransfer.this, getString(R.string.label_invitation_failed));
	            	}
            	}
            };
            thread.start();
        }
    };
    
    /**
     * File transfer session event listener
     */
    private IFileTransferEventListener fileTransferSessionListener = new IFileTransferEventListener.Stub() {
		// Session is started
		public void handleSessionStarted() {
			handler.post(new Runnable() { 
				public void run() {
					TextView statusView = (TextView)findViewById(R.id.progress_status);
					statusView.setText("started");
				}
			});
		}
	
		// Session has been aborted
		public void handleSessionAborted() {
			handler.post(new Runnable() { 
				public void run() {
					TextView statusView = (TextView)findViewById(R.id.progress_status);
					statusView.setText("aborted");
					Utils.showInfo(ReceiveFileTransfer.this, getString(R.string.label_sharing_aborted));
				}
			});
		}
	    
		// Session has been terminated
		public void handleSessionTerminated() {
			handler.post(new Runnable() { 
				public void run() {
					TextView statusView = (TextView)findViewById(R.id.progress_status);
					statusView.setText("terminated");
				}
			});
		}
	    
		// Session has been terminated by remote
		public void handleSessionTerminatedByRemote() {
			handler.post(new Runnable() { 
				public void run() {
					TextView statusView = (TextView)findViewById(R.id.progress_status);
					statusView.setText("terminated");
					Utils.showInfo(ReceiveFileTransfer.this, getString(R.string.label_sharing_terminated_by_remote));
				}
			});
		}
	
		// File transfer progress
		public void handleTransferProgress(final long currentSize, final long totalSize) {
			handler.post(new Runnable() { 
    			public void run() {
    				updateProgressBar(currentSize, totalSize);
    			}
    		});
		}
		
		// File transfer error
		public void handleTransferError(final int error) {
			handler.post(new Runnable() { 
				public void run() {
					TextView statusView = (TextView)findViewById(R.id.progress_status);
					statusView.setText("error");
					
					if (error == ContentSharingError.SESSION_INITIATION_DECLINED) {
    					Utils.showInfo(ReceiveFileTransfer.this, getString(R.string.label_invitation_declined));
					} else {
    					Utils.showError(ReceiveFileTransfer.this, getString(R.string.label_invitation_failed));
					}
				}
			});
		}
		
		// File has been transfered
		public void handleFileTransfered(final String filename) {
			handler.post(new Runnable() { 
				public void run() {
					TextView statusView = (TextView)findViewById(R.id.progress_status);
					statusView.setText("transfered");
					
					// Make sure progress bar is at the end
			        ProgressBar progressBar = (ProgressBar)findViewById(R.id.progress_bar);
			        progressBar.setProgress(progressBar.getMax());
			        
			        // Show the transfered image
			        try {
			        	ImageView image = (ImageView)findViewById(R.id.image_view);
			        	File file = new File(filename);
			        	FileInputStream stream =  new FileInputStream(file);
			        	Bitmap bitmap = BitmapFactory.decodeStream(stream);
			        	image.setImageBitmap(bitmap);
			        } catch(Exception e) {
			        	Utils.showError(ReceiveFileTransfer.this, getString(R.string.label_out_of_memory));
			        }
				}
			});
		}
    };    

    /**
     * Show the transfer progress
     * 
     * @param currentSize Current size transfered
     * @param totalSize Total size to be transfered
     */
    private void updateProgressBar(long currentSize, long totalSize) {
    	TextView statusView = (TextView)findViewById(R.id.progress_status);
        ProgressBar progressBar = (ProgressBar)findViewById(R.id.progress_bar);
    	
		String value = "" + (currentSize/1024);
		if (totalSize != 0) {
			value += "/" + (totalSize/1024);
		}
		value += " Kb";
		statusView.setText(value);
	    
	    if (currentSize != 0) {
	    	double position = ((double)currentSize / (double)totalSize)*100.0;
	    	progressBar.setProgress((int)position);
	    } else {
	    	progressBar.setProgress(0);
	    }
    }
}
