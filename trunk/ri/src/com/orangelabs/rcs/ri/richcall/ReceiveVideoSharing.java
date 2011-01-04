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
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.content.DialogInterface.OnClickListener;
import android.widget.TextView;

import com.orangelabs.rcs.core.ims.service.sharing.ContentSharingError;
import com.orangelabs.rcs.provider.settings.RcsSettings;
import com.orangelabs.rcs.ri.utils.Utils;
import com.orangelabs.rcs.ri.R;
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
     * RemoteContact
     */
    private String remoteContact;
    
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
        
		// Get invitation info
        sessionId = getIntent().getStringExtra("sessionId");
		remoteContact = getIntent().getStringExtra("contact");

		// Remove the notification
		ReceiveVideoSharing.removeVideoSharingNotification(this, sessionId);

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
     * API disabled
     */
    public void handleApiDisabled() {
		handler.post(new Runnable() { 
			public void run() {
				Utils.showError(ReceiveVideoSharing.this, getString(R.string.label_api_disabled));
			}
		});
    }
    
    /**
     * API connected
     */
    public void handleApiConnected() {
		try {
			// Get the video sharing session
			cshSession = callApi.getVideoSharingSession(sessionId);
			cshSession.addSessionListener(cshStreamingEventListener);
			
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.title_recv_video_sharing);
			builder.setMessage(getString(R.string.label_from) + " " + remoteContact);
			builder.setCancelable(false);
			builder.setIcon(R.drawable.ri_notif_csh);
			builder.setPositiveButton(getString(R.string.label_accept), acceptBtnListener);
			builder.setNegativeButton(getString(R.string.label_decline), declineBtnListener);
			builder.show();   
		} catch(Exception e) {
			Utils.showError(ReceiveVideoSharing.this, getString(R.string.label_api_failed));
		}
    }

    /**	
     * API disconnected
     */
    public void handleApiDisconnected() {
    	// Service has been disconnected
		handler.post(new Runnable(){
			public void run(){
				Utils.showInfo(ReceiveVideoSharing.this, getString(R.string.label_api_disconnected));
			}
		});
    }
	
    /**
     * Accept button listener
     */
    private OnClickListener acceptBtnListener = new OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
        	// Set layout
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            setContentView(R.layout.richcall_receive_video_sharing);
            
            // Set title
            setTitle(R.string.title_recv_video_sharing);
            // Set video preview
            videoSurface = (VideoSurfaceView)findViewById(R.id.video_view);
            videoSurface.setAspectRatio(176, 144);
    		renderer = new VideoRenderer();
    		renderer.setRenderer(videoSurface);
    		
    		// Display the remote contact
            TextView from = (TextView)findViewById(R.id.from);
            from.setText(getString(R.string.label_from) + " " + remoteContact);
            
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
        public void onClick(DialogInterface dialog, int which) {
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
    
    /**
     * Add video share notification
     * 
     * @param context Context
     * @param contact Contact
     * @param sessionId Session ID
     */
    public static void addVideoSharingInvitationNotification(Context context, String contact, String sessionId) {
		// Create notification
		Intent intent = new Intent(context, ReceiveVideoSharing.class);
		intent.putExtra("contact", contact);
		intent.putExtra("sessionId", sessionId);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        String notifTitle = context.getString(R.string.title_recv_video_sharing);
        Notification notif = new Notification(R.drawable.ri_notif_csh,
        		notifTitle,
        		System.currentTimeMillis());
        notif.flags = Notification.FLAG_NO_CLEAR;
        notif.setLatestEventInfo(context,
        		notifTitle,
        		context.getString(R.string.label_from)+" "+contact,
        		contentIntent);
        
        // Set ringtone
        String ringtone = RcsSettings.getInstance().getCShInvitationRingtone();
        if (!TextUtils.isEmpty(ringtone)) {
			notif.sound = Uri.parse(ringtone);
        }
        
        // Set vibration
        if (RcsSettings.getInstance().isPhoneVibrateForCShInvitation()) {
        	notif.defaults |= Notification.DEFAULT_VIBRATE;
        }
        
        // Send notification
		NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(Integer.parseInt(sessionId), notif);
	}
	
    /**
     * Remove video share notification
     * 
     * @param context Context
     * @param sessionId Session ID
     */
    public static void removeVideoSharingNotification(Context context, String sessionId) {
		NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(Integer.parseInt(sessionId));
	}
}
