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
package com.orangelabs.rcs.ri.richcall;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.CursorWrapper;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Spinner;

import com.orangelabs.rcs.core.ims.service.sharing.ContentSharingError;
import com.orangelabs.rcs.ri.utils.Utils;
import com.orangelabs.rcs.ri.R;
import com.orangelabs.rcs.service.api.client.richcall.IVideoSharingEventListener;
import com.orangelabs.rcs.service.api.client.richcall.IVideoSharingSession;
import com.orangelabs.rcs.service.api.client.richcall.RichCallApi;

/**
 * Initiate live video sharing
 * 
 * @author jexa7410
 */
public class InitiateLiveVideoSharing extends Activity implements SurfaceHolder.Callback {
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
	private LiveVideoPlayer player = null;
	
	/**
	 * Camera preview started flag
	 */
	private boolean previewRunning = false;

	/**
	 * Video surface holder
	 */
	private SurfaceHolder surface;
	
	/**
	 * Camera
	 */
    private Camera camera = null;

    /**
     * Video sharing session
     */
    private IVideoSharingSession cshSession = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set layout
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.richcall_initiate_live_video_sharing);
        
        // Set title
        setTitle(R.string.menu_initiate_livevideo_sharing);
        
        // Set the contact selector
        Spinner spinner = (Spinner)findViewById(R.id.contact);
        spinner.setAdapter(Utils.createContactListAdapter(this));

        // Set button callback
        Button inviteBtn = (Button)findViewById(R.id.invite_btn);
        inviteBtn.setOnClickListener(btnInviteListener);
        Button dialBtn = (Button)findViewById(R.id.dial_btn);
        dialBtn.setOnClickListener(btnDialListener);
               
        // Disable button if no contact available
        if (spinner.getAdapter().getCount() == 0) {
        	dialBtn.setEnabled(false);
        	inviteBtn.setEnabled(false);
        }
        
        // Set the video preview
        videoView = (VideoSurfaceView)findViewById(R.id.video_preview);
        videoView.setAspectRatio(176, 144);
        surface = videoView.getHolder();
        surface.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);        
        surface.addCallback(this);

        // Create the live video player
        player = new LiveVideoPlayer();

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
        	} catch(Exception e) {
        	}
        }
        
        // Release the camera
        if (camera != null) {
        	camera.setPreviewCallback(null);
        	camera.stopPreview();
        	camera.release();
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
                		cshSession = callApi.initiateLiveVideoSharing(remote, player);
                		cshSession.addSessionListener(cshSessionListener);
	            	} catch(Exception e) {
                		e.printStackTrace();
	    		    	Utils.showMessageAndExit(InitiateLiveVideoSharing.this, getString(R.string.label_invitation_failed));
	            	}
            	}
            };
            thread.start();
        }
    };
    
    /**
     * Surface has been changed
     * 
	 * @param holder Surface holder
	 * @param format Format
	 * @param w Width
	 * @param h Height
     */
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		if (camera == null) {
			return;
		}

		if (previewRunning) {
            previewRunning = false;
            camera.stopPreview();
        }
        
        Camera.Parameters p = camera.getParameters();
        p.setPreviewSize(176, 144);        
// TODO       p.setPictureFormat(PixelFormat.YCbCr_420_SP);
        p.setPreviewFormat(PixelFormat.YCbCr_420_SP);
// TODO       p.setPreviewFrameRate(8);
        camera.setParameters(p);

        try {
        	camera.setPreviewDisplay(holder);
        	camera.startPreview();        
	        previewRunning = true;
        } catch(Exception e) {
        	camera = null;
        }
    }

    /**
     * Surface has been created
     * 
	 * @param holder Surface holder
     */
	public void surfaceCreated(SurfaceHolder holder) {
		if (camera != null) {
			return;
		}
		
		// Start camera preview
		camera = Camera.open();
		camera.setPreviewCallback(player);	
	}	

	/**
	 * Surface has been destroyed
	 * 
	 * @param holder Surface holder
	 */
	public void surfaceDestroyed(SurfaceHolder holder) {
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
					Utils.showMessageAndExit(InitiateLiveVideoSharing.this, getString(R.string.label_sharing_aborted));
				}
			});
		}
	    
		// Session has been terminated
		public void handleSessionTerminated() {
			handler.post(new Runnable() { 
				public void run() {
					Utils.showMessageAndExit(InitiateLiveVideoSharing.this, getString(R.string.label_sharing_terminated));
				}
			});
		}
	    
		// Session has been terminated by remote
		public void handleSessionTerminatedByRemote() {
			handler.post(new Runnable() { 
				public void run() {
					Utils.showMessageAndExit(InitiateLiveVideoSharing.this, getString(R.string.label_sharing_terminated_by_remote));
				}
			});
		}
	
		// Content sharing error
		public void handleSharingError(final int error) {
			handler.post(new Runnable() { 
				public void run() {
					if (error == ContentSharingError.SESSION_INITIATION_DECLINED) {
    					Utils.showMessageAndExit(InitiateLiveVideoSharing.this, getString(R.string.label_invitation_declined));
					} else {
    					Utils.showMessageAndExit(InitiateLiveVideoSharing.this, getString(R.string.label_csh_failed, error));
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
	                        if (cshSession != null) {
	                        	try {
	                        		cshSession.removeSessionListener(cshSessionListener);
	                        		cshSession.cancelSession();
	                        	} catch(Exception e) {
	                        	}
	                        	cshSession = null;
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
