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
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.orangelabs.rcs.core.ims.service.sharing.ContentSharingError;
import com.orangelabs.rcs.samples.R;
import com.orangelabs.rcs.samples.utils.Utils;
import com.orangelabs.rcs.service.api.client.ClientApiListener;
import com.orangelabs.rcs.service.api.client.richcall.IVideoSharingEventListener;
import com.orangelabs.rcs.service.api.client.richcall.IVideoSharingSession;
import com.orangelabs.rcs.service.api.client.richcall.RichCallApi;

/**
 * Received video sharing
 * 
 * @author jexa7410
 */
public class ReceiveVideoSharing extends Activity implements ClientApiListener {

    /**
     * UI handler
     */
    private final Handler handler = new Handler();

    /**
	 * Rich call API
	 */
    private RichCallApi callApi;
    
    /**
     * Video sharing session
     */
    private IVideoSharingSession cshSession = null;

    /**
     * Session ID
     */
    private String sessionId = null;
    
    /**
     * Video renderer
     */
    private VideoRenderer renderer = null;
    
    /**
     * Video preview
     */
    private VideoSurfaceView videoSurface;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.richcall_receive_video);
        
        // Set UI title
        setTitle(R.string.title_recv_video_sharing);
        
        // Get the sessionID
		sessionId = getIntent().getStringExtra("sessionId");
        
		// Display the remote contact
        TextView from = (TextView)findViewById(R.id.from);
		String remoteContact = getIntent().getStringExtra("contact");
        from.setText(getString(R.string.label_from) + " " + remoteContact);

        // Set video preview
        videoSurface = (VideoSurfaceView)findViewById(R.id.video_view);
        videoSurface.setAspectRatio(176, 144);
		renderer = new VideoRenderer();
		renderer.setRenderer(videoSurface);
        
        // Set buttons callback
        Button acceptButton = (Button)findViewById(R.id.acceptBtn);
        acceptButton.setOnClickListener(acceptBtnListener);
        Button declineButton = (Button)findViewById(R.id.declineBtn);
        declineButton.setOnClickListener(declineBtnListener);

        // Instanciate richcall API
        callApi = new RichCallApi(getApplicationContext());
        callApi.connectApi();
        callApi.addApiEventListener(this);
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();

        // Remove session listener
        if (cshSession != null) {
        	try {
	    		cshSession.removeSessionListener(cshStreamingEventListener);
	    		cshSession.cancelSession();
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
		try {
			// Get the video sharing session
			cshSession = callApi.getVideoSharingSession(sessionId);
			cshSession.addSessionListener(cshStreamingEventListener);
		} catch(Exception e) {
			Utils.showError(ReceiveVideoSharing.this, getString(R.string.api_failed)+": "+e);
		}
    }

    /**	
     * API disconnected
     */
    public void handleApiDisconnected() {
    	// Service has been disconnected
		handler.post(new Runnable(){
			public void run(){
				Utils.showInfo(ReceiveVideoSharing.this, getString(R.string.api_failed));
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
            			cshSession.setMediaRenderer(renderer);
	    				cshSession.acceptSession();
	            	} catch(Exception e) {
	    		    	Utils.showError(ReceiveVideoSharing.this, getString(R.string.label_invitation_failed));
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
               			cshSession.rejectSession();
	            	} catch(Exception e) {
	    		    	Utils.showError(ReceiveVideoSharing.this, getString(R.string.label_invitation_failed));
	            	}
            	}
            };
            thread.start();
        }
    };
    
    /**
     * Video sharing session event listener
     */
    private IVideoSharingEventListener cshStreamingEventListener = new IVideoSharingEventListener.Stub() {
		// Session is started
		public void handleSessionStarted() {
		}
	
		// Session has been aborted
		public void handleSessionAborted() {
			handler.post(new Runnable() { 
				public void run() {
					Utils.showInfo(ReceiveVideoSharing.this, getString(R.string.label_sharing_aborted));
				}
			});
		}
	    
		// Session has been terminated
		public void handleSessionTerminated() {
		}
	    
		// Session has been terminated by remote
		public void handleSessionTerminatedByRemote() {
			handler.post(new Runnable() { 
				public void run() {
					Utils.showInfo(ReceiveVideoSharing.this, getString(R.string.label_sharing_terminated_by_remote));
				}
			});
		}
			
		// Sharing error
		public void handleSharingError(final int error) {
			handler.post(new Runnable() { 
				public void run() {
					if (error == ContentSharingError.SESSION_INITIATION_DECLINED) {
    					Utils.showInfo(ReceiveVideoSharing.this, getString(R.string.label_invitation_declined));
					} else {
    					Utils.showError(ReceiveVideoSharing.this, getString(R.string.label_invitation_failed));
					}
				}
			});
		}
    };            
}
