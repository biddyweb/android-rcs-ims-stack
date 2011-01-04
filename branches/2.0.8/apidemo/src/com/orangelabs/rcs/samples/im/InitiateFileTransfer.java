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
import android.database.Cursor;
import android.database.CursorWrapper;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.orangelabs.rcs.core.ims.service.sharing.ContentSharingError;
import com.orangelabs.rcs.samples.R;
import com.orangelabs.rcs.samples.utils.Utils;
import com.orangelabs.rcs.service.api.client.messaging.IFileTransferEventListener;
import com.orangelabs.rcs.service.api.client.messaging.IFileTransferSession;
import com.orangelabs.rcs.service.api.client.messaging.MessagingApi;

/**
 * Initiate file transfer
 * 
 * @author jexa7410
 */
public class InitiateFileTransfer extends Activity {

	/**
	 * UI handler
	 */
	private final Handler handler = new Handler();

    /**
	 * Messaging API
	 */
    private MessagingApi messagingApi;
    
    /**
     * File transfer session
     */
    private IFileTransferSession transferSession = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.messaging_initiate_filetransfer);
        
        // Set UI title
        setTitle(R.string.menu_transfer_file);
        
        // Set contact selector
        Spinner spinner = (Spinner)findViewById(R.id.contact);
        spinner.setAdapter(Utils.createContactListAdapter(this));

        // Set buttons callback
        Button btn = (Button)findViewById(R.id.invite);
        btn.setOnClickListener(btnInviteListener);
        btn = (Button)findViewById(R.id.select);
        btn.setOnClickListener(btnSelectListener);
               
        // Instanciate messaging API
        messagingApi = new MessagingApi(getApplicationContext());
        messagingApi.connectApi();
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();

        // Remove session listener
        if (transferSession != null) {
        	try {
        		transferSession.removeSessionListener(cshSessionListener);
        	} catch(Exception e) {
        	}
        }

        // Disconnect rich call API
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
            
            // Get URI to transfer
            TextView uriEdit = (TextView)findViewById(R.id.uri);
            final String uri = uriEdit.getText().toString();
 
            Thread thread = new Thread() {
            	public void run() {
                	try {
                        // Initiate transfer
	            		transferSession = messagingApi.transferFile(remote, uri);
	        	        transferSession.addSessionListener(cshSessionListener);
	            	} catch(Exception e) {
	    		    	Utils.showError(InitiateFileTransfer.this, getString(R.string.label_invitation_failed));
	            	}
            	}
            };
            thread.start();
        }
    };
       
    /**
     * Select file button listener
     */
    private OnClickListener btnSelectListener = new OnClickListener() {
        public void onClick(View v) {
        	// Select a picture (may any other type of file)
        	Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            Intent wrapperIntent = Intent.createChooser(intent, null);
            startActivityForResult(wrapperIntent, 0);
        }
    };

    /**
     * On activity result
     * 
     * @param requestCode Request code
     * @param resultCode Result code
     * @param data Data
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (resultCode != RESULT_OK) {
    		return;
    	}
    	
        switch(requestCode) {
            case 0: {
            	if ((data != null) && (data.getData() != null)) {
            		// Get selected photo URI
            		Uri uri = data.getData();
            		
                    // Get image filename
                    Cursor cursor = getContentResolver().query(uri, new String[] {MediaStore.Images.ImageColumns.DATA}, null, null, null); 
                    cursor.moveToFirst();
                    String filename = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));
                    cursor.close();     
            		
                    // Display the selected filename
                    TextView uriEdit = (TextView)findViewById(R.id.uri);
                    uriEdit.setText(filename);
            	}
	    	}             
            break;
        }
    }
    
    /**
     * File transfer session event listener
     */
    private IFileTransferEventListener cshSessionListener = new IFileTransferEventListener.Stub() {
		// Session is started
		public void handleSessionStarted() {
			handler.post(new Runnable() { 
				public void run() {
					// Display session status
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
					Utils.showInfo(InitiateFileTransfer.this, getString(R.string.label_sharing_aborted));
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
					Utils.showInfo(InitiateFileTransfer.this, getString(R.string.label_sharing_terminated_by_remote));
				}
			});
		}
	
		// Transfer progress
		public void handleTransferProgress(final long currentSize, final long totalSize) {
			handler.post(new Runnable() { 
    			public void run() {
    				updateProgressBar(currentSize, totalSize);
    			}
    		});
		}
	
		// Transfer error
		public void handleTransferError(final int error) {
			handler.post(new Runnable() { 
				public void run() {
					TextView statusView = (TextView)findViewById(R.id.progress_status);
					statusView.setText("error");
					
					if (error == ContentSharingError.SESSION_INITIATION_DECLINED) {
    					Utils.showInfo(InitiateFileTransfer.this, getString(R.string.label_invitation_declined));
					} else {
    					Utils.showError(InitiateFileTransfer.this, getString(R.string.label_invitation_failed));
					}
				}
			});
		}
	
		// File has been transfered
		public void handleFileTransfered(String filename) {
			handler.post(new Runnable() { 
				public void run() {
					TextView statusView = (TextView)findViewById(R.id.progress_status);
					statusView.setText("transfered");
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
                if (transferSession != null) {
                	try {
                		transferSession.cancelSession();
                	} catch(Exception e) {
                		e.printStackTrace();
                	}
                }
                finish();
                return true;
        }

        return super.onKeyDown(keyCode, event);
    }
}    
