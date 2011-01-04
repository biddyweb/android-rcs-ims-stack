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
import android.widget.Spinner;
import android.widget.TextView;

import com.orangelabs.rcs.core.ims.service.sharing.ContentSharingError;
import com.orangelabs.rcs.samples.R;
import com.orangelabs.rcs.samples.utils.Utils;
import com.orangelabs.rcs.service.api.client.richcall.IVideoSharingEventListener;
import com.orangelabs.rcs.service.api.client.richcall.IVideoSharingSession;
import com.orangelabs.rcs.service.api.client.richcall.RichCallApi;

/**
 * Pre-recorded video sharing (only H.263 176x144 frames are supported) 
 * 
 * @author jexa7410
 */
public class InitiatePrerecordedVideoSharing extends Activity implements PrerecordedVideoPreviewListener {
	/**
	 * UI handler
	 */
	private final Handler handler = new Handler();

    /**
	 * Rich call API
	 */
    private RichCallApi callApi;
    
	/**
     * Video preview
     */
	private VideoSurfaceView videoView;

	/**
	 * Video player
	 */
	private PrerecordedVideoPlayer player = null;
	
	/**
	 * Video filename
	 */
	private String filename;
	
    /**
     * Video sharing session
     */
    private IVideoSharingSession cshSession = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.richcall_initiate_prerecorded_video_sharing);
        
        // Set UI title
        setTitle(R.string.menu_initiate_livevideo_sharing);
        
        // Set the contact selector
        Spinner spinner = (Spinner)findViewById(R.id.contact);
        spinner.setAdapter(Utils.createContactListAdapter(this));

        // Set buttons callback
        Button btn = (Button)findViewById(R.id.invite);
        btn.setOnClickListener(btnInviteListener);
        btn = (Button)findViewById(R.id.dial);
        btn.setOnClickListener(btnDialListener);
        btn = (Button)findViewById(R.id.select);
        btn.setOnClickListener(btnSelectListener);
               
        // Set the video preview
        videoView = (VideoSurfaceView)findViewById(R.id.video_prerecorded_preview);
        videoView.setAspectRatio(176, 144);
        
        // Instanciate rich call API
		callApi = new RichCallApi(getApplicationContext());
		callApi.connectApi();
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();

        // Remove session listener
        if (cshSession != null) {
        	try {
        		cshSession.removeSessionListener(cshSessionListener);
        		cshSession.cancelSession();
        	} catch(Exception e) {}
        }

        // Disconnect rich call API
        callApi.disconnectApi();
    }
    
    /**
     * Dial button listener
     */
    private OnClickListener btnDialListener = new OnClickListener() {
        public void onClick(View v) {
        	// Get the remote contact
            Spinner spinner = (Spinner)findViewById(R.id.contact);
            CursorWrapper cursor = (CursorWrapper)spinner.getSelectedItem();
            String remote = cursor.getString(1);

            // Initiate a GSM call before to be able to share content
            Intent intent = new Intent(Intent.ACTION_CALL);
        	intent.setData(Uri.parse("tel:"+remote));
            startActivity(intent);
        }
    };
    
    /**
     * Invite button listener
     */
    private OnClickListener btnInviteListener = new OnClickListener() {
        public void onClick(View v) {
        	// Get the remote contact
            Spinner spinner = (Spinner)findViewById(R.id.contact);
            CursorWrapper cursor = (CursorWrapper)spinner.getSelectedItem();
            final String remote = cursor.getString(1);
            
            Thread thread = new Thread() {
            	public void run() {
                	try {
                        // Initiate sharing
                		player = new PrerecordedVideoPlayer(filename, videoView, InitiatePrerecordedVideoSharing.this);
                		cshSession = callApi.initiateVideoSharing(remote, filename, player);
                		cshSession.addSessionListener(cshSessionListener);
	            	} catch(Exception e) {
	    		    	Utils.showError(InitiatePrerecordedVideoSharing.this, getString(R.string.label_invitation_failed));
	            	}
            	}
            };
            thread.start();
        }
    };
    
    /**
     * Select video button listener
     */
    private OnClickListener btnSelectListener = new OnClickListener() {
        public void onClick(View v) {
        	// Select a video from the gallery
        	Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("video/*");
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (resultCode != RESULT_OK) {
    		return;
    	}
    	
        switch(requestCode) {
            case 0: {
            	if ((data != null) && (data.getData() != null)) {
            		// Get selected photo URI
            		Uri videoUri = data.getData();
            		
                    // Get image filename
            		Cursor cursor = getContentResolver().query(videoUri, new String[] {MediaStore.Video.VideoColumns.DATA}, null, null, null); 
                    cursor.moveToFirst();
                    filename = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));
                    cursor.close();     
            		
                    // Display the selected filename
                    TextView uriEdit = (TextView)findViewById(R.id.filename);
                    uriEdit.setText(filename);
            	}
	    	}             
            break;
        }
    }

    /**
     * End of video stream event
     */
	public void endOfStream() {
		// TODO: display something on the UI
	}

    /**
     * Video stream progress event
     * 
     * @param progress Progress
     */
	public void updateDuration(long progress) {
		// TODO: display something on the UI
	}
    
	/**
     * Video sharing event listener
     */
    private IVideoSharingEventListener cshSessionListener = new IVideoSharingEventListener.Stub() {
		// Session is started
		public void handleSessionStarted() {
		}
	
		// Session has been aborted
		public void handleSessionAborted() {
			handler.post(new Runnable() { 
				public void run() {
					Utils.showInfo(InitiatePrerecordedVideoSharing.this, getString(R.string.label_sharing_aborted));
				}
			});
		}
	    
		// Session has been terminated
		public void handleSessionTerminated() {
			handler.post(new Runnable() { 
				public void run() {
					Utils.showInfo(InitiatePrerecordedVideoSharing.this, getString(R.string.label_sharing_terminated));
				}
			});
		}
	    
		// Session has been terminated by remote
		public void handleSessionTerminatedByRemote() {
			handler.post(new Runnable() { 
				public void run() {
					Utils.showInfo(InitiatePrerecordedVideoSharing.this, getString(R.string.label_sharing_terminated_by_remote));
				}
			});
		}
	
		// Content sharing error
		public void handleSharingError(final int error) {
			handler.post(new Runnable() { 
				public void run() {
					if (error == ContentSharingError.SESSION_INITIATION_DECLINED) {
    					Utils.showInfo(InitiatePrerecordedVideoSharing.this, getString(R.string.label_invitation_declined));
					} else {
    					Utils.showError(InitiatePrerecordedVideoSharing.this, getString(R.string.label_invitation_failed));
					}
				}
			});
		}
    };
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
            	// Stop the session
                if (cshSession != null) {
                	try {
                		cshSession.cancelSession();
                		cshSession.removeSessionListener(cshSessionListener);
                	} catch(Exception e) {}
                }
                finish();
                return true;
        }

        return super.onKeyDown(keyCode, event);
    }
}    
