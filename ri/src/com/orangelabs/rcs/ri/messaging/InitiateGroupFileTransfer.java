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

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.orangelabs.rcs.core.ims.service.im.filetransfer.FileSharingError;
import com.orangelabs.rcs.platform.file.FileDescription;
import com.orangelabs.rcs.platform.file.FileFactory;
import com.orangelabs.rcs.provider.settings.RcsSettings;
import com.orangelabs.rcs.ri.R;
import com.orangelabs.rcs.ri.utils.Utils;
import com.orangelabs.rcs.service.api.client.messaging.IFileTransferEventListener;
import com.orangelabs.rcs.service.api.client.messaging.IFileTransferSession;
import com.orangelabs.rcs.service.api.client.messaging.MessagingApi;

/**
 * Initiate group chat
 * 
 * @author jexa7410
 */
public class InitiateGroupFileTransfer extends Activity implements OnItemClickListener {
	/**
	 * Activity result constants
	 */
	private final static int SELECT_IMAGE = 0;
	
	/**
	 * Activity result constants
	 */
	private final static int SELECT_CONTACTS = 1;
	
	/**
	 * UI handler
	 */
	private final Handler handler = new Handler();

	/**
	 * Selected filename
	 */
	private String filename;
	
	/**
	 * Selected filesize (kB)
	 */
	private long filesize = -1;
	
	/**
	 * Messaging API
	 */
    private MessagingApi messagingApi;

    /**
     * File transfer session
     */
    private IFileTransferSession transferSession = null;
    
    /**
     * Progress dialog
     */
    private Dialog progressDialog = null;
	
	
	/**
     * List of participants
     */
    private ArrayList<String> participants = new ArrayList<String>();
    
    /**
     * Invite button
     */
    private Button inviteBtn;
    
    /**
     * Invite button
     */
    private Button selectBtn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set layout
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.messaging_initiate_group_file_transfer);
        
        // Set title
        setTitle(R.string.menu_group_transfer_file);
        
        // Set contact selector
        ListView contactList = (ListView)findViewById(R.id.contacts);
        contactList.setAdapter(Utils.createMultiContactImCapableListAdapter(this));
        contactList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        contactList.setOnItemClickListener(this);
        
        // Set button callback
        inviteBtn = (Button)findViewById(R.id.invite_btn);
        inviteBtn.setOnClickListener(btnInviteListener);
    	inviteBtn.setEnabled(false);
    	selectBtn = (Button)findViewById(R.id.select_btn);
        selectBtn.setOnClickListener(btnSelectListener);

        // Disable thumbnail option if not supported
        CheckBox ftThumb = (CheckBox)findViewById(R.id.ft_thumb);
        if (!RcsSettings.getInstance().isFileTransferThumbnailSupported()) {
        	ftThumb.setEnabled(false);
        }

        // Disable button if FT HTTP not supported
        // TODO: to remove when FT group MSP available
        if (!RcsSettings.getInstance().isFileTransferHttpSupported()) {
            selectBtn.setEnabled(false);
            Toast.makeText(getApplicationContext(), "FT HTTP not supported", Toast.LENGTH_LONG).show();
        }

        // Instantiate messaging API
        messagingApi = new MessagingApi(getApplicationContext());
        messagingApi.connectApi();
    }
    
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
    	if (filesize < 0 || participants.size() == 0) {
    		inviteBtn.setEnabled(false);
		} else {
			inviteBtn.setEnabled(true);
		}		
	}
	

	 /**
     * Invite button listener
     */
    private OnClickListener btnInviteListener = new OnClickListener() {
        public void onClick(View v) {
        	int warnSize = RcsSettings.getInstance().getWarningMaxFileTransferSize();
            if ((warnSize > 0) && (filesize >= warnSize)) {
				// Display a warning message
            	AlertDialog.Builder builder = new AlertDialog.Builder(InitiateGroupFileTransfer.this);
            	builder.setMessage(getString(R.string.label_sharing_warn_size, filesize));
            	builder.setCancelable(false);
            	builder.setPositiveButton(getString(R.string.label_yes), new DialogInterface.OnClickListener() {
                	public void onClick(DialogInterface dialog, int position) {
                		initiateTransfer();
                	}
        		});	                    			
            	builder.setNegativeButton(getString(R.string.label_no), null);
                AlertDialog alert = builder.create();
            	alert.show();
            } else {
            	initiateTransfer();
            }
    	}
	};
      
	/**
	 * Initiate transfer
	 */
    private void initiateTransfer() {
        // Get thumbnail option
        CheckBox ftThumb = (CheckBox)findViewById(R.id.ft_thumb);
        final boolean thumbnail = ftThumb.isChecked();

        // Initiate session in background
        Thread thread = new Thread() {
        	public void run() {
            	try {
            		// Initiate transfer
            		transferSession = messagingApi.transferFileGroup(
            		    	participants, filename, thumbnail);

        	        transferSession.addSessionListener(cshSessionListener);
            	} catch(Exception e) {
            		e.printStackTrace();
					handler.post(new Runnable(){
						public void run(){
							Utils.showMessageAndExit(InitiateGroupFileTransfer.this, getString(R.string.label_invitation_failed));
						}
					});
            	}
        	}
        };
        thread.start();
        
        // Display a progress dialog
        progressDialog = Utils.showProgressDialog(InitiateGroupFileTransfer.this, getString(R.string.label_command_in_progress));
        progressDialog.setOnCancelListener(new OnCancelListener() {
			public void onCancel(DialogInterface dialog) {
				Toast.makeText(InitiateGroupFileTransfer.this, getString(R.string.label_ft_initiation_canceled), Toast.LENGTH_SHORT).show();
				quitSession();
			}
		});            

        // Disable UI
        ftThumb.setEnabled(false);

        // Hide buttons
        Button inviteBtn = (Button)findViewById(R.id.invite_btn);
    	inviteBtn.setVisibility(View.INVISIBLE);
        Button selectBtn = (Button)findViewById(R.id.select_btn);
        selectBtn.setVisibility(View.INVISIBLE);
    }
       
    /**
     * Select file button listener
     */
    private OnClickListener btnSelectListener = new OnClickListener() {
        public void onClick(View v) {
        	startDialog();
        }
    };
    
    /**
     * Display a alert dialog to select the kind of file to transfer
     */
    private void startDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.label_select_file);
        builder.setCancelable(true);
        builder.setItems(R.array.select_filetotransfer, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch(which) {
					case 0:
						Intent pictureShareIntent = new Intent(Intent.ACTION_GET_CONTENT, null);
						pictureShareIntent.setType("image/*");
						startActivityForResult(pictureShareIntent, SELECT_IMAGE);
						break;
					case 1:
						Intent contactsShareIntent = new Intent(Intent.ACTION_GET_CONTENT);
						contactsShareIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
						startActivityForResult(contactsShareIntent, SELECT_CONTACTS);
						break;
				}
			}
		});
        AlertDialog alert = builder.create();
    	alert.show();
    }
    
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
	    	case SELECT_IMAGE: {
	    		if ((data != null) && (data.getData() != null)) {
	
	    			// Get selected photo URI
	    			Uri uri = data.getData();
	
	    			// Get image filename
	    			Cursor cursor = getContentResolver().query(uri, new String[] {MediaStore.Images.ImageColumns.DATA}, null, null, null); 
	    			cursor.moveToFirst();
	    			filename = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));
	    			cursor.close();     	    		
	    			
	    			// Display the selected filename attribute
	    			TextView uriEdit = (TextView)findViewById(R.id.uri);
	    			try {
	    				FileDescription desc = FileFactory.getFactory().getFileDescription(filename);
	    				filesize = desc.getSize()/1024;
	    				uriEdit.setText(filesize + " KB");
	    			} catch(Exception e) {
	    				filesize = -1;
	    				uriEdit.setText(filename);
	    			}
	
	    			// Show invite button
	    			if(participants.size() > 0)
	    			{
		    			Button inviteBtn = (Button)findViewById(R.id.invite_btn);
		    			inviteBtn.setEnabled(true);
	    			}
	    		}
	    	}
	    	break;
    	}
    }

	/**
	 * Hide progress dialog
	 */
    public void hideProgressDialog() {
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
			progressDialog = null;
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
					// Hide progress dialog
					hideProgressDialog();
					
					// Display session status
					TextView statusView = (TextView)findViewById(R.id.progress_status);
					statusView.setText("started");
				}
			});
		}
	
		// Session has been aborted
		public void handleSessionAborted(int reason) {
			handler.post(new Runnable() { 
				public void run() {
					// Hide progress dialog
					hideProgressDialog();

					// Display message
					Utils.showMessageAndExit(InitiateGroupFileTransfer.this, getString(R.string.label_sharing_aborted));
				}
			});
		}
	    
		// Session has been terminated by remote
		public void handleSessionTerminatedByRemote() {
			handler.post(new Runnable() { 
				public void run() {
					// Hide progress dialog
					hideProgressDialog();
					
					// Display session status
					Utils.showMessageAndExit(InitiateGroupFileTransfer.this, getString(R.string.label_sharing_terminated_by_remote));
				}
			});
		}
	
		// Transfer progress
		public void handleTransferProgress(final long currentSize, final long totalSize) {
			handler.post(new Runnable() { 
    			public void run() {
					// Display transfer progress
    				updateProgressBar(currentSize, totalSize);

                    if (currentSize >= totalSize) {
                        TextView statusView = (TextView)findViewById(R.id.progress_status);
                        statusView.setText("uploaded");
                    }
    			}
    		});
		}
	
		// Transfer error
		public void handleTransferError(final int error) {
			handler.post(new Runnable() { 
				public void run() {
					// Hide progress dialog
					hideProgressDialog();
					
					// Display error
					if (error == FileSharingError.MEDIA_TRANSFER_FAILED) {
						TextView statusView = (TextView)findViewById(R.id.progress_status);
						statusView.setText("error");
					} else
					if (error == FileSharingError.SESSION_INITIATION_DECLINED) {
						Utils.showMessageAndExit(InitiateGroupFileTransfer.this, getString(R.string.label_invitation_declined));
					} else
                    if (error == FileSharingError.MEDIA_SIZE_TOO_BIG) {
                        Utils.showMessageAndExit(InitiateGroupFileTransfer.this, getString(R.string.label_transfer_failed_too_big));
                    } else {
						Utils.showMessageAndExit(InitiateGroupFileTransfer.this, getString(R.string.label_transfer_failed, error));
					}
				}
			});
		}
	
		// File has been transfered
		public void handleFileTransfered(String filename) {
			handler.post(new Runnable() { 
				public void run() {
					// Hide progress dialog
					hideProgressDialog();

					// Display transfer progress
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

    /**
     * Quit the session
     */
    private void quitSession() {
		// Stop session
        Thread thread = new Thread() {
        	public void run() {
            	try {
                    if (transferSession != null) {
                		transferSession.removeSessionListener(cshSessionListener);
                		transferSession.cancelSession();
                    }
            	} catch(Exception e) {
            	}
            	transferSession = null;
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

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater=new MenuInflater(getApplicationContext());
		inflater.inflate(R.menu.menu_ft, menu);
		return true;
	}
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_close_session:
				// Quit the session
				quitSession();
				break;
		}
		return true;
	}
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();

        // Remove session listener
        if (transferSession != null) {
        	try {
        		transferSession.removeSessionListener(cshSessionListener);
        	} catch(Exception e) {
        	}
        }

        // Disconnect messaging API
        messagingApi.disconnectApi();
    }
}    
