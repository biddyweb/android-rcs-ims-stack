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

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.orangelabs.rcs.core.ims.service.im.filetransfer.FileSharingError;
import com.orangelabs.rcs.provider.settings.RcsSettings;
import com.orangelabs.rcs.ri.R;
import com.orangelabs.rcs.ri.utils.Utils;
import com.orangelabs.rcs.service.api.client.contacts.ContactsApi;
import com.orangelabs.rcs.service.api.client.messaging.IFileTransferEventListener;
import com.orangelabs.rcs.service.api.client.messaging.IFileTransferSession;
import com.orangelabs.rcs.service.api.client.messaging.MessagingApi;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Initiate file transfer
 * 
 * @author jexa7410
 */
public class InitiateFileTransfer extends Activity {
	/**
	 * Activity result constants
	 */
	private final static int SELECT_IMAGE = 0;
	private final static int SELECT_CONTACTS = 1;
	private final static int SELECT_TEXT_FILE = 2;
	
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
	 * Contact API
	 */
    private ContactsApi contactsApi;
    
    /**
     * File transfer session
     */
    private IFileTransferSession transferSession = null;
    
    /**
     * Progress dialog
     */
    private Dialog progressDialog = null;
    
    /** The logger */
    private final static Logger logger = Logger.getLogger(InitiateFileTransfer.class.getSimpleName());

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RcsSettings.createInstance(getApplicationContext());

        // Set layout
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.messaging_initiate_filetransfer);
        
        // Set title
        setTitle(R.string.menu_transfer_file);
        
        // Set contact selector
        Spinner spinner = (Spinner)findViewById(R.id.contact);
        spinner.setAdapter(Utils.createRcsContactListAdapter(this));

        // Set buttons callback
        Button inviteBtn = (Button)findViewById(R.id.invite_btn);
        inviteBtn.setOnClickListener(btnInviteListener);
    	inviteBtn.setEnabled(false);
        Button selectBtn = (Button)findViewById(R.id.select_btn);
        selectBtn.setOnClickListener(btnSelectListener);
        Button pauseBtn = (Button)findViewById(R.id.pause_btn);
        pauseBtn.setOnClickListener(btnPauseListener);
        pauseBtn.setEnabled(false);
        Button resumeBtn = (Button)findViewById(R.id.resume_btn);
        resumeBtn.setOnClickListener(btnResumeListener);
        resumeBtn.setEnabled(false);
               
        // Disable button if no contact available
        if (spinner.getAdapter().getCount() == 0) {
        	selectBtn.setEnabled(false);
        }
        	        
        // Disable thumbnail option if not supported
        CheckBox ftThumb = (CheckBox)findViewById(R.id.ft_thumb);
        if (!RcsSettings.getInstance().isFileTransferThumbnailSupported()) {
        	ftThumb.setEnabled(false);
        }        	

        // Instanciate messaging API
        messagingApi = new MessagingApi(getApplicationContext());
        messagingApi.connectApi();
        
        // Instantiate contact API
        contactsApi = new ContactsApi(getApplicationContext());
        
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
        if (transferSession != null) {
        	try {
        		transferSession.removeSessionListener(cshSessionListener);
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
        	int warnSize = RcsSettings.getInstance().getWarningMaxFileTransferSize();
            if ((warnSize > 0) && (filesize >= warnSize)) {
				// Display a warning message
            	AlertDialog.Builder builder = new AlertDialog.Builder(InitiateFileTransfer.this);
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
     * Pause button listener
     */
    private OnClickListener btnPauseListener = new OnClickListener() {
        public void onClick(View v) {
        	Button resumeBtn = (Button)findViewById(R.id.resume_btn);
        	resumeBtn.setEnabled(true);
        	Button pauseBtn = (Button)findViewById(R.id.pause_btn);
        	pauseBtn.setEnabled(false);
        	
        	try {
				transferSession.pauseSession();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				handler.post(new Runnable(){
					public void run(){
						Utils.showMessageAndExit(InitiateFileTransfer.this, getString(R.string.label_invitation_failed));
					}
				});
			}
    	}
	};
      
	
    /**
     * Resume button listener
     */
    private OnClickListener btnResumeListener = new OnClickListener() {
        public void onClick(View v) {
        	Button resumeBtn = (Button)findViewById(R.id.resume_btn);
        	resumeBtn.setEnabled(false);
        	Button pauseBtn = (Button)findViewById(R.id.pause_btn);
        	pauseBtn.setEnabled(true);
        	
        	try {
				transferSession.resumeSession();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				handler.post(new Runnable(){
					public void run(){
						Utils.showMessageAndExit(InitiateFileTransfer.this, getString(R.string.label_invitation_failed));
					}
				});
			}
    	}
	};
	/**
	 * Initiate transfer
	 */
    private void initiateTransfer() {
    	// Get remote contact
        Spinner spinner = (Spinner)findViewById(R.id.contact);
        MatrixCursor cursor = (MatrixCursor)spinner.getSelectedItem();
        final String remote = cursor.getString(1);

        // Get thumbnail option
        CheckBox ftThumb = (CheckBox)findViewById(R.id.ft_thumb);
        final boolean thumbnail = ftThumb.isChecked();

        // Initiate session in background
        Thread thread = new Thread() {
        	public void run() {
            	try {
            		// Initiate transfer
            		transferSession = messagingApi.transferFile(remote, filename, thumbnail);
        	        transferSession.addSessionListener(cshSessionListener);
            	} catch(Exception e) {
					handler.post(new Runnable(){
						public void run(){
							Utils.showMessageAndExit(InitiateFileTransfer.this, getString(R.string.label_invitation_failed));
						}
					});
            	}
        	}
        };
        thread.start();

        Button pauseBtn = (Button)findViewById(R.id.pause_btn);
        pauseBtn.setEnabled(true);
        
        // Display a progress dialog
        progressDialog = Utils.showProgressDialog(InitiateFileTransfer.this, getString(R.string.label_command_in_progress));
        progressDialog.setOnCancelListener(new OnCancelListener() {
			public void onCancel(DialogInterface dialog) {
				Toast.makeText(InitiateFileTransfer.this, getString(R.string.label_ft_initiation_canceled), Toast.LENGTH_SHORT).show();
				quitSession();
			}
		});            

        // Disable UI
        spinner.setEnabled(false);
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
				switch (which) {
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
				case 2:
					Intent fileIntent = new Intent(Intent.ACTION_GET_CONTENT, null);
					fileIntent.setType("text/plain");
					fileIntent.addCategory(Intent.CATEGORY_OPENABLE);
					startActivityForResult(fileIntent, SELECT_TEXT_FILE);
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
		if (resultCode != RESULT_OK || (data == null) || (data.getData() == null)) {
			return;
		}
		Uri uri =  data.getData();
		TextView uriEdit = (TextView) findViewById(R.id.uri);
		TextView sizeEdit = (TextView) findViewById(R.id.size);
		Button inviteBtn = (Button) findViewById(R.id.invite_btn);
		switch (requestCode) {
		case SELECT_CONTACTS:
			// Get vCard filename
			filename = contactsApi.getVisitCard(uri);
			// Display the selected filename attribute
			uriEdit.setText(filename);
			// Show invite button
			inviteBtn.setEnabled(true);
			break;

		case SELECT_IMAGE:
		case SELECT_TEXT_FILE:
			if (logger.isActivated()) {
				logger.debug("Selected file uri:" +uri);
			}
			// Get filename
			filename = getFilePath(this, uri);
			// Display the selected filename attribute
			try {
				File file = new File(filename);
				filesize = file.length() / 1024;
				sizeEdit.setText(filesize + " KB");
				uriEdit.setText( file.getName());
			} catch (Exception e) {
				if (logger.isActivated()) {
					logger.error("Exception occurred",e);
				}
				filesize = -1;
				sizeEdit.setText("Unknown");
				uriEdit.setText(filename);
			}
			// Show invite button
			inviteBtn.setEnabled(true);
			break;
		}
	}

    /**
     * Get the file path from URI
     *
     * @param context
     *            the context
     * @param uri
     *            the URI
     * @return the File path
     */
	public static String getFilePath(final Context context, final Uri uri) {
		final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

		// DocumentProvider
		if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
			// ExternalStorageProvider
			if (isExternalStorageDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				if ("primary".equalsIgnoreCase(type)) {
					return Environment.getExternalStorageDirectory() + "/" + split[1];
				}

				// TODO handle non-primary volumes
			}
			// DownloadsProvider
			else if (isDownloadsDocument(uri)) {

				final String id = DocumentsContract.getDocumentId(uri);
				final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),
						Long.valueOf(id));

				return getDataColumn(context, contentUri, null, null);
			}
			// MediaProvider
			else if (isMediaDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				Uri contentUri = null;
				if ("image".equals(type)) {
					contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				} else if ("video".equals(type)) {
					contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
				} else if ("audio".equals(type)) {
					contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				}

				final String selection = "_id=?";
				final String[] selectionArgs = new String[] { split[1] };

				return getDataColumn(context, contentUri, selection, selectionArgs);
			}
		}
		if ("content".equalsIgnoreCase(uri.getScheme())) {
			return getDataColumn(context, uri, null, null);
		} else if ("file".equalsIgnoreCase(uri.getScheme())) {
			return uri.getPath();
		}
		return null;
	}
	
	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is ExternalStorageProvider.
	 */
	public static boolean isExternalStorageDocument(Uri uri) {
	    return "com.android.externalstorage.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is DownloadsProvider.
	 */
	public static boolean isDownloadsDocument(Uri uri) {
	    return "com.android.providers.downloads.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is MediaProvider.
	 */
	public static boolean isMediaDocument(Uri uri) {
	    return "com.android.providers.media.documents".equals(uri.getAuthority());
	}
	
	/**
	 * Get the value of the data column for this Uri. This is useful for MediaStore Uris, and other file-based ContentProviders.
	 * 
	 * @param context
	 *            The context.
	 * @param uri
	 *            The Uri to query.
	 * @param selection
	 *            (Optional) Filter used in the query.
	 * @param selectionArgs
	 *            (Optional) Selection arguments used in the query.
	 * @return The value of the _data column, which is typically a file path.
	 */
	public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
		Cursor cursor = null;
		final String column = "_data";
		final String[] projection = { column };

		try {
			cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
			if (cursor != null && cursor.moveToFirst()) {
				final int column_index = cursor.getColumnIndexOrThrow(column);
				return cursor.getString(column_index);
			}
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return null;
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
					Utils.showMessageAndExit(InitiateFileTransfer.this, getString(R.string.label_sharing_aborted));
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
					Utils.showMessageAndExit(InitiateFileTransfer.this, getString(R.string.label_sharing_terminated_by_remote));
				}
			});
		}
	
		// Transfer progress
		public void handleTransferProgress(final long currentSize, final long totalSize) {
			handler.post(new Runnable() { 
    			public void run() {
					// Display transfer progress
    				updateProgressBar(currentSize, totalSize);
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
						Utils.showMessageAndExit(InitiateFileTransfer.this, getString(R.string.label_invitation_declined));
					} else
                    if (error == FileSharingError.MEDIA_SIZE_TOO_BIG) {
                        Utils.showMessageAndExit(InitiateFileTransfer.this, getString(R.string.label_transfer_failed_too_big));
                    }  if (error == FileSharingError.NOT_ENOUGH_STORAGE_SPACE) {
                        Utils.showMessageAndExit(InitiateFileTransfer.this, getString(R.string.label_transfer_failed_capacity_too_small));
                    } else {
						Utils.showMessageAndExit(InitiateFileTransfer.this, getString(R.string.label_transfer_failed, error));
					}
				}
			});
		}
	
		// File has been transferred
		public void handleFileTransfered(String filename) {
			handler.post(new Runnable() { 
				public void run() {
					// Hide progress dialog
					hideProgressDialog();
					updateProgressBar(filesize, filesize);
					// Display transfer progress
					TextView statusView = (TextView)findViewById(R.id.progress_status);
					statusView.setText("transferred");
				}
			});
		}


		public void handleFileTransferPaused() throws RemoteException {
			handler.post(new Runnable() { 
				public void run() {
					// Hide progress dialog
					hideProgressDialog();
					
					// Display session status
					TextView statusView = (TextView)findViewById(R.id.progress_status);
					statusView.setText("paused");
				}
			});
		}

		@Override
		public void handleFileTransferResumed() throws RemoteException {
			handler.post(new Runnable() { 
				public void run() {
					// Hide progress dialog
					hideProgressDialog();
					
					// Display session status
					TextView statusView = (TextView)findViewById(R.id.progress_status);
					statusView.setText("Resumed");
				}
			});
		}
    };
    
    /**
     * Show the transfer progress
     * 
     * @param currentSize Current size transferred
     * @param totalSize Total size to be transferred
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
}
