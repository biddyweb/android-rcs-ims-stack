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

package com.orangelabs.rcs.ri.richcall;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.orangelabs.rcs.provider.settings.RcsSettings;
import com.orangelabs.rcs.ri.R;
import com.orangelabs.rcs.ri.utils.CpuMonitor;
import com.orangelabs.rcs.ri.utils.Utils;
import com.orangelabs.rcs.service.api.client.ClientApiListener;
import com.orangelabs.rcs.service.api.client.ImsEventListener;
import com.orangelabs.rcs.service.api.client.media.video.VideoRenderer;
import com.orangelabs.rcs.service.api.client.media.video.VideoSurfaceView;
import com.orangelabs.rcs.service.api.client.richcall.IVideoSharingEventListener;
import com.orangelabs.rcs.service.api.client.richcall.IVideoSharingSession;
import com.orangelabs.rcs.service.api.client.richcall.RichCallApi;

/**
 * Received video sharing
 * 
 * @author jexa7410
 */
public class ReceiveVideoSharing extends Activity implements ClientApiListener, ImsEventListener {
    /**
     * UI handler
     */
    private final Handler handler = new Handler();

    /**
	 * Rich call API
	 */
    private RichCallApi callApi;
    
    /**
     * Session ID
     */
    private String incomingSessionId = null;
    
    /**
     * RemoteContact
     */
    private String remoteContact;
    
    /**
     * Incoming video sharing session
     */
    private IVideoSharingSession incomingSession = null;

    /**
     * Video renderer
     */
    private VideoRenderer renderer = null;
    
    /**
     * Incoming video preview
     */
    private VideoSurfaceView incomingVideoView;
    
    /**
     * CPU monitoring
     */
    private CpuMonitor cpu = new CpuMonitor();
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
    	// Set layout
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Get invitation info
        incomingSessionId = getIntent().getStringExtra("sessionId");
		remoteContact = getIntent().getStringExtra("contact");

		// Remove the notification
		ReceiveVideoSharing.removeVideoSharingNotification(this, incomingSessionId);

        // Instanciate richcall API
        callApi = new RichCallApi(getApplicationContext());
        callApi.addApiEventListener(this);
        callApi.addImsEventListener(this);
        callApi.connectApi();

        // Start CPU monitoring
		cpu.start();
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();

		// Stop CPU monitoring
		cpu.stop();

		// Remove session listener
        if (incomingSession != null) {
        	try {
	    		incomingSession.removeSessionListener(incomingSessionEventListener);
	    		incomingSession.cancelSession();
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
				Utils.showMessageAndExit(ReceiveVideoSharing.this, getString(R.string.label_api_disabled));
			}
		});
    }
        
    /**
     * API connected
     */
    public void handleApiConnected() {
		try {
			// Get the video sharing session
			incomingSession = callApi.getVideoSharingSession(incomingSessionId);
			if (incomingSession == null) {
				// Session not found or expired
				Utils.showMessageAndExit(ReceiveVideoSharing.this, getString(R.string.label_session_has_expired));
				return;
			}
			incomingSession.addSessionListener(incomingSessionEventListener);
			
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.title_recv_video_sharing);
			builder.setMessage(getString(R.string.label_from) + " " + remoteContact);
			builder.setCancelable(false);
			builder.setIcon(R.drawable.ri_notif_csh_icon);
			builder.setPositiveButton(getString(R.string.label_accept), acceptBtnListener);
			builder.setNegativeButton(getString(R.string.label_decline), declineBtnListener);
			builder.show();   
		} catch(Exception e) {
			Utils.showMessageAndExit(ReceiveVideoSharing.this, getString(R.string.label_api_failed));
		}
    }

    /**	
     * API disconnected
     */
    public void handleApiDisconnected() {
    	// Service has been disconnected
		handler.post(new Runnable(){
			public void run(){
				Utils.showMessageAndExit(ReceiveVideoSharing.this, getString(R.string.label_api_disconnected));
			}
		});
    }
    
    /**
     * IMS connected
     */
	public void handleImsConnected() {
	}

    /**
     * IMS disconnected
     */
	public void handleImsDisconnected() {
    	// IMS has been disconnected
		handler.post(new Runnable(){
			public void run(){
				Utils.showMessageAndExit(ReceiveVideoSharing.this, getString(R.string.label_ims_disconnected));
			}
		});
	}
	
    /**
     * Accept button listener
     */
    private OnClickListener acceptBtnListener = new OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
        	// Set layout
            setContentView(R.layout.richcall_receive_video_sharing);
            
            // Set title
            setTitle(R.string.title_recv_video_sharing);
            
            // Set incoming video preview
            incomingVideoView = (VideoSurfaceView)findViewById(R.id.incoming_video_view);
            incomingVideoView.setAspectRatio(176, 144);
    		renderer = new VideoRenderer("h263-2000");
    		renderer.setVideoSurface(incomingVideoView);
    		
    		// Display the remote contact
            TextView from = (TextView)findViewById(R.id.from);
            from.setText(getString(R.string.label_from) + " " + remoteContact);
            
            // Accept the session in background
            Thread thread = new Thread() {
            	public void run() {
                	try {
                		// Accept the invitation
            			incomingSession.setMediaRenderer(renderer);
	    				incomingSession.acceptSession();
	            	} catch(Exception e) {
	    				handler.post(new Runnable() { 
	    					public void run() {
	    						Utils.showMessageAndExit(ReceiveVideoSharing.this, getString(R.string.label_invitation_failed));
	    					}
	    				});
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
                		incomingSession.removeSessionListener(incomingSessionEventListener);
                		incomingSession.rejectSession();
	            	} catch(Exception e) {
	            	}
            	}
            };
            thread.start();
            
            // Exit activity
			finish();
		}
    };
    
    /**
     * Incoming video sharing session event listener
     */
    private IVideoSharingEventListener incomingSessionEventListener = new IVideoSharingEventListener.Stub() {
		// Session is started
		public void handleSessionStarted() {
		}
	
		// Session has been aborted
		public void handleSessionAborted() {
			handler.post(new Runnable() { 
				public void run() {
					Utils.showMessageAndExit(ReceiveVideoSharing.this, getString(R.string.label_sharing_aborted));
				}
			});
		}
	    
		// Session has been terminated by remote
		public void handleSessionTerminatedByRemote() {
			handler.post(new Runnable() { 
				public void run() {
					Utils.showMessageAndExit(ReceiveVideoSharing.this, getString(R.string.label_sharing_terminated_by_remote));
				}
			});
		}
			
		// Sharing error
		public void handleSharingError(final int error) {
			handler.post(new Runnable() { 
				public void run() {
					Utils.showMessageAndExit(ReceiveVideoSharing.this, getString(R.string.label_csh_failed, error));
				}
			});
		}
    };   
    
    /**
     * Quit the session
     */
    private void quitSession() {
		// Stop sessions
	    Thread thread = new Thread() {
	    	public void run() {
	        	try {
	                if (incomingSession != null) {
	                	try {
	                		incomingSession.removeSessionListener(incomingSessionEventListener);
	                		incomingSession.cancelSession();
	                	} catch(Exception e) {
	                	}
	                	incomingSession = null;
	                }
	        	} catch(Exception e) {
	        	}
	    	}
	    };
	    thread.start();
		
	    // Exit activity
		finish();
    }
    
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
        Notification notif = new Notification(R.drawable.ri_notif_csh_icon,
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
        notificationManager.notify((int)Long.parseLong(sessionId), notif);
	}
	
    /**
     * Remove video share notification
     * 
     * @param context Context
     * @param sessionId Session ID
     */
    public static void removeVideoSharingNotification(Context context, String sessionId) {
		NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel((int)Long.parseLong(sessionId));
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
		inflater.inflate(R.menu.menu_incoming_livevideo, menu);
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
