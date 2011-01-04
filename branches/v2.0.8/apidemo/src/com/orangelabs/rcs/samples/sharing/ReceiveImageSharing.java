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
package com.orangelabs.rcs.samples.sharing;

import java.io.File;
import java.io.FileInputStream;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.orangelabs.rcs.core.ims.service.sharing.ContentSharingError;
import com.orangelabs.rcs.samples.R;
import com.orangelabs.rcs.samples.utils.Utils;
import com.orangelabs.rcs.service.api.client.ClientApiListener;
import com.orangelabs.rcs.service.api.client.richcall.IImageSharingEventListener;
import com.orangelabs.rcs.service.api.client.richcall.IImageSharingSession;
import com.orangelabs.rcs.service.api.client.richcall.RichCallApi;

/**
 * Receive image sharing
 * 
 * @author jexa7410
 */
public class ReceiveImageSharing extends Activity implements ClientApiListener {

    /**
	 * Rich call API
	 */
    private RichCallApi callApi;
    
    /**
     * Image sharing session
     */
    private IImageSharingSession sharingSession = null;

    /**
     * VoIP session ID
     */
    private String sessionId = null;
    
    /**
     * UI handler
     */
    private final Handler handler = new Handler();
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.richcall_receive_image);
        
        // Set UI title
        setTitle(R.string.title_recv_image_sharing);
        
		// Get the session ID
		sessionId = getIntent().getStringExtra("sessionId");
        
		// Display the remote contact
		String remoteContact = getIntent().getStringExtra("contact");
        TextView from = (TextView)findViewById(R.id.from);
        from.setText(getString(R.string.label_from) + " " + remoteContact);

        // Set buttons callback
        Button acceptButton = (Button)findViewById(R.id.acceptBtn);
        acceptButton.setOnClickListener(acceptBtnListener);
        Button declineButton = (Button)findViewById(R.id.declineBtn);
        declineButton.setOnClickListener(declineBtnListener);

        // Instanciate messaging API
        callApi = new RichCallApi(getApplicationContext());
        callApi.connectApi();
        callApi.addApiEventListener(this);
    }

    @Override
    public void onDestroy() {
    	super.onDestroy();

        // Remove session listener
        if (sharingSession != null) {
        	try {
        		sharingSession.removeSessionListener(imageSharingEventListener);
        	} catch(Exception e) {
        	}
        }

        // Disconnect rich call API
        callApi.disconnectApi();
    }
    
    /**
     * API connected
     */
    public void handleApiConnected() {
    	if (sessionId != null) {
    		try{
    			// Get the image sharing session
    			sharingSession = callApi.getImageSharingSession(sessionId);
    			sharingSession.addSessionListener(imageSharingEventListener);
    			
    			// Display the filename attributes to be shared
    			TextView name = (TextView)findViewById(R.id.image_name);
    	    	name.setText(getString(R.string.label_image_name, sharingSession.getFilename()));
    	    	TextView size = (TextView)findViewById(R.id.image_size);
    	    	size.setText(getString(R.string.label_image_size, " " + (sharingSession.getFilesize()/1024), " Kb"));
    		} catch(Exception e) {
    			Utils.showError(ReceiveImageSharing.this, getString(R.string.api_failed)+": "+e);
			}
    	}
    }

    /**
     * API disconnected
     */
    public void handleApiDisconnected() {
    	// Service has been disconnected
		handler.post(new Runnable(){
			public void run(){
				Utils.showInfo(ReceiveImageSharing.this, getString(R.string.api_failed));
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
            			sharingSession.acceptSession();
	            	} catch(Exception e) {
	    		    	Utils.showError(ReceiveImageSharing.this, getString(R.string.label_invitation_failed));
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
            			sharingSession.rejectSession();
	            	} catch(Exception e) {
	    		    	Utils.showError(ReceiveImageSharing.this, getString(R.string.label_invitation_failed));
	            	}
            	}
            };
            thread.start();
        }
    };
    
    /**
     * Image sharing session event listener
     */
    private IImageSharingEventListener imageSharingEventListener = new IImageSharingEventListener.Stub() {
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
					Utils.showInfo(ReceiveImageSharing.this, getString(R.string.label_sharing_aborted));
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

					Utils.showInfo(ReceiveImageSharing.this, getString(R.string.label_sharing_terminated_by_remote));
				}
			});
		}
	
		// Sharing progress
		public void handleSharingProgress(final long currentSize, final long totalSize) {
			handler.post(new Runnable() { 
    			public void run() {
    				updateProgressBar(currentSize, totalSize);
    			}
    		});
		}
		
		// Sharing error
		public void handleSharingError(final int error) {
			handler.post(new Runnable() { 
				public void run() {
					TextView statusView = (TextView)findViewById(R.id.progress_status);
					statusView.setText("error");
					
					if (error == ContentSharingError.SESSION_INITIATION_DECLINED) {
    					Utils.showInfo(ReceiveImageSharing.this, getString(R.string.label_invitation_declined));
					} else {
    					Utils.showError(ReceiveImageSharing.this, getString(R.string.label_invitation_failed));
					}
				}
			});
		}
		
		// Image has been transfered
		public void handleImageTransfered(final String filename) {
			handler.post(new Runnable() { 
				public void run() {
					TextView statusView = (TextView)findViewById(R.id.progress_status);
					statusView.setText("transfered");
					
					// Make sure progress bar is at the end
			        ProgressBar progressBar = (ProgressBar)findViewById(R.id.progress_bar);
			        progressBar.setProgress(progressBar.getMax());
					
			        // Show the shared image
			        try {
			        	ImageView image = (ImageView)findViewById(R.id.image_view);
			        	File file = new File(filename);
			        	FileInputStream stream =  new FileInputStream(file);
			        	Bitmap bitmap = BitmapFactory.decodeStream(stream);
			        	image.setImageBitmap(bitmap);
			        } catch(Exception e) {
			        	Utils.showError(ReceiveImageSharing.this, getString(R.string.label_out_of_memory));
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
            	// Stop the session
                if (sharingSession != null) {
                	try {
                		sharingSession.cancelSession();
                		sharingSession.removeSessionListener(imageSharingEventListener);
                	} catch(Exception e) {
                	}
                }
                finish();
                return true;
        }

        return super.onKeyDown(keyCode, event);
    }
}
