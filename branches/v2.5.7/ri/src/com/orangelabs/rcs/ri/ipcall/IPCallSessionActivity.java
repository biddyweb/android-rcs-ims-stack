package com.orangelabs.rcs.ri.ipcall;

import java.lang.reflect.Method;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.orangelabs.rcs.core.ims.protocol.rtp.codec.video.h264.H264Config;
import com.orangelabs.rcs.core.ims.protocol.rtp.format.video.CameraOptions;
import com.orangelabs.rcs.core.ims.protocol.rtp.format.video.Orientation;
import com.orangelabs.rcs.core.ims.service.ipcall.IPCallError;
import com.orangelabs.rcs.provider.settings.RcsSettings;
import com.orangelabs.rcs.ri.R;
import com.orangelabs.rcs.ri.richcall.VisioSharing;
import com.orangelabs.rcs.ri.utils.Utils;
import com.orangelabs.rcs.service.api.client.ClientApiListener;
import com.orangelabs.rcs.service.api.client.ImsEventListener;
import com.orangelabs.rcs.service.api.client.ipcall.IIPCallEventListener;
import com.orangelabs.rcs.service.api.client.ipcall.IIPCallSession;
import com.orangelabs.rcs.service.api.client.ipcall.IPCallApi;
import com.orangelabs.rcs.service.api.client.media.IAudioPlayer;
import com.orangelabs.rcs.service.api.client.media.IAudioRenderer;
import com.orangelabs.rcs.service.api.client.media.audio.AudioRenderer;
import com.orangelabs.rcs.service.api.client.media.audio.LiveAudioPlayer;
import com.orangelabs.rcs.service.api.client.media.video.LiveVideoPlayer;
import com.orangelabs.rcs.service.api.client.media.video.VideoRenderer;
import com.orangelabs.rcs.service.api.client.media.video.VideoSurfaceView;
import com.orangelabs.rcs.utils.logger.Logger;

@SuppressLint("NewApi")
public class IPCallSessionActivity extends Activity implements SurfaceHolder.Callback {

	/**
	 * UI handler
	 */
	public static final  Handler handler = new Handler();

	/**
	 * Activity is in state onResume
	 */ 
	private boolean  isVisible = false;

	/**
	 * Progress dialog
	 */
	private static Dialog outgoingProgressDialog = null;
	
	/**
	 * Progress dialog
	 */
	private AlertDialog addVideoInvitationDialog;

	/**
	 * HangUp button
	 */
	private static Button hangUpBtn = null;

	/**
	 * Set On Hold button
	 */
	private static Button setOnHoldBtn = null;

	/**
	 * Add Video button
	 */
	private static Button addVideoBtn = null;
	
	/**
	 * Switch camera button 
	 */
    private static Button switchCamBtn = null;

	/**
	 * Audio player
	 */
	private LiveAudioPlayer outgoingAudioPlayer = null;

	/**
	 * Audio renderer
	 */
	private AudioRenderer incomingAudioRenderer = null;

	/**
	 * Video player
	 */
	private LiveVideoPlayer outgoingVideoPlayer = null;

	/**
	 * Video renderer
	 */
	private VideoRenderer incomingVideoRenderer = null;
	
    /** Camera */
    private Camera camera = null;

    /** Opened camera id */
    private CameraOptions openedCameraId = CameraOptions.FRONT;

    /** Camera preview started flag */
    private boolean cameraPreviewRunning = false;

    /** Number of cameras */
    private int numberOfCameras = 1;
    
    /** Incoming Video preview */
    private VideoSurfaceView incomingVideoView = null;
    
    /** Incoming Video width */
    private int incomingWidth = 0;

    /** Incoming Video height */
    private int incomingHeight = 0;
    
    /** Outgoing Video preview */
    private VideoSurfaceView outgoingVideoView = null;
    
    /** Outgoing Video width */
    private int outgoingWidth = H264Config.QCIF_WIDTH;

    /** Outgoing Video height */
    private int outgoingHeight = H264Config.QCIF_HEIGHT;

    /** Outgoing Video surface holder */
    private SurfaceHolder surface;
    
    /** Preview surface view is created */
    private boolean isSurfaceCreated = false;

	/**
	 * The logger
	 */
	private static Logger logger = Logger.getLogger("IPCallSessionActivity");		

	
    /* *****************************************
     *                Activity
     ***************************************** */

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (logger.isActivated()) {
			logger.info("onCreate()");
		}
		// Set layout
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.ipcall_session);

		// Set title
		setTitle(R.string.title_audio_call);

		// set buttons
		setOnHoldBtn = (Button) findViewById(R.id.set_onhold_btn);
		setOnHoldBtn.setOnClickListener(btnOnHoldListener);
		setOnHoldBtn.setEnabled(false);

		hangUpBtn = (Button) findViewById(R.id.hangup_btn);
		hangUpBtn.setOnClickListener(btnHangUpListener);
		hangUpBtn.setEnabled(false);

		addVideoBtn = (Button) findViewById(R.id.add_video_btn);
		addVideoBtn.setOnClickListener(btnAddVideoListener);
		addVideoBtn.setEnabled(false);

		switchCamBtn = (Button) findViewById(R.id.switch_cam_btn);
		switchCamBtn.setVisibility(View.GONE);

		outgoingVideoView = (VideoSurfaceView) findViewById(R.id.outgoing_ipcall_video_preview);
		outgoingVideoView.setVisibility(View.GONE);

		incomingVideoView = (VideoSurfaceView) findViewById(R.id.incoming_ipcall_video_view);
		incomingVideoView.setVisibility(View.GONE);

		// instantiate Audio Player and Renderer
		incomingAudioRenderer = new AudioRenderer(this);
		outgoingAudioPlayer = new LiveAudioPlayer(incomingAudioRenderer);

		// instantiate Video Player and Renderer
		incomingVideoRenderer = new VideoRenderer();
		outgoingVideoPlayer = new LiveVideoPlayer(incomingVideoRenderer);

		if (logger.isActivated()) {
			logger.info("IPCallSessionData.audioCallState ="
					+ IPCallSessionsData.audioCallState);
		}

		String action = getIntent().getAction();
		if (logger.isActivated()) {
			logger.info("action =" + action);
		}

		if (action != null){
			
			// IP call already established or in connecting state - recover existing
			// sessions
			if (action.equals("recover")) {
				if (IPCallSessionsData.isCallApiConnected) {
					recoverSessions();
				} else {
					IPCallSessionsData.recoverSessionsWhenApiConnected = true;
				}
			} else if ((action.equals("incoming")||(action.equals("outgoing")))) {
				// Get remote contact from intent
				IPCallSessionsData.remoteContact = getIntent().getStringExtra("contact");

				// set direction of call
				IPCallSessionsData.direction = action ;

				// reset session and sessionid 
				IPCallSessionsData.session = null;
				IPCallSessionsData.sessionId = "";
				
				// set call state
				IPCallSessionsData.audioCallState = IPCallSessionsData.IDLE;
				IPCallSessionsData.videoCallState = IPCallSessionsData.IDLE;
				
				// incoming ip call
				if (action.equals("incoming")){
					// Get intent info
					IPCallSessionsData.sessionId = getIntent().getStringExtra("sessionId");
					
					// remove notification
					if (!IPCallSessionsData.sessionId.equals(null)) {
						InitiateIPCallActivity.removeIPCallNotification(
								getApplicationContext(), IPCallSessionsData.sessionId);
					}
					
					// get incoming ip call
					if (IPCallSessionsData.isCallApiConnected) {
						getIncomingSession();
					} else {
						IPCallSessionsData.getIncomingSessionWhenApiConnected = true;
					}
				}
				// initiate an outgoing ip call
				else if (action.equals("outgoing")) {
					// Get intent info
					Boolean videoOption = getIntent().getBooleanExtra("video", false);

					// set call state
					IPCallSessionsData.audioCallState = IPCallSessionsData.IDLE;
					IPCallSessionsData.videoCallState = IPCallSessionsData.IDLE;

					if (IPCallSessionsData.isCallApiConnected) {
						startOutgoingSession(videoOption);
					} else {
						IPCallSessionsData.startOutgoingSessionWhenApiConnected = true;
					}
				}		
			}
		}
	}
	

	public void onResume() {
		super.onResume();
		if (logger.isActivated()) {
			logger.info("onResume()");
		}
		// activity is displayed
		isVisible = true; 
		
		// set contact number
		TextView fromTxt = (TextView) findViewById(R.id.ipcall_with_txt);
		fromTxt.setText(getString(R.string.label_audio_with, IPCallSessionsData.remoteContact));
	}

	@Override
	public void onPause() {
		super.onPause();
		if (logger.isActivated()) {
			logger.info("onPause()");
		}
		
		isVisible = false; 
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (logger.isActivated()) {
			logger.info("onNewIntent()");
		}
		if (intent.getAction().equals("ExitActivity")) {
			final String msg = intent.getExtras().getString("message") ;

				handler.post(new Runnable() {
					public void run()  {
						displayErrorAndExitActivity(msg);
					}
				});

		
		}
		// other actions
		else {setIntent(intent);}
	}

	 @Override
	    public boolean onKeyDown(int keyCode, KeyEvent event) {
	        switch (keyCode) {
	            case KeyEvent.KEYCODE_BACK:
	                // save current session data
	            	if (logger.isActivated()) {
	        			logger.info("onKeyBack()");
	        		}
	                IPCallSessionData sessionData = new IPCallSessionData(
	                		IPCallSessionsData.audioCallState, 
	                		IPCallSessionsData.videoCallState, 
	                		IPCallSessionsData.remoteContact,
	                		IPCallSessionsData.direction,
	                		IPCallSessionsData.sessionEventListener) {
					};
					IPCallSessionsData.saveSessionData(IPCallSessionsData.sessionId, sessionData);
					this.finish();
	                return true;
	        }

	        return super.onKeyDown(keyCode, event);
	    }
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (logger.isActivated()) {
			logger.info("onDestroy()");
		}
	}

	
	/* *****************************************
     *              Button listeners
     ***************************************** */

	/**
	 * Accept button listener
	 */
	private OnClickListener acceptBtnListener = new OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			InitiateIPCallActivity.removeIPCallNotification(getApplicationContext(), IPCallSessionsData.sessionId);
			acceptIncomingSession();
			
		}
	};

	/**
	 * Reject button listener
	 */
	private OnClickListener declineBtnListener = new OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			InitiateIPCallActivity.removeIPCallNotification(getApplicationContext(), IPCallSessionsData.sessionId);
			declineIncomingSession();
		}
	};

	/**
	 * Set On Hold button listener
	 */
	private View.OnClickListener btnOnHoldListener = new View.OnClickListener() {
		public void onClick(View v) {

			if (IPCallSessionsData.audioCallState != IPCallSessionsData.ON_HOLD) {
				try {
					// TODO
				} catch (Exception e) {
				}
				Button holdBtn = (Button) findViewById(R.id.set_onhold_btn);
				holdBtn.setText(R.string.label_resume_btn);
				// audioCallState = ON_HOLD ;
			} else {
				try {
					// TODO
				} catch (Exception e) {
				}
				Button holdBtn = (Button) findViewById(R.id.set_onhold_btn);
				holdBtn.setText(R.string.label_set_onhold_btn);
				// audioCallState = CONNECTED ;
			}

		}
	};

	/**
	 * Hang Up button listener
	 */
	private View.OnClickListener btnHangUpListener = new View.OnClickListener() {
		public void onClick(View v) {
			Thread thread = new Thread() {
				public void run() {
					stopSession();
					exitActivity(null);
				}
			};
			thread.start();
		}
	};

	/**
	 * Add Video button listener
	 */
	private View.OnClickListener btnAddVideoListener = new View.OnClickListener() {
		public void onClick(View v) {
			if (logger.isActivated()) {
				logger.info("btnAddVideoListener.onClick()");
				logger.info("videoCallState =" + IPCallSessionsData.videoCallState);
				
			}
			if (IPCallSessionsData.videoCallState == IPCallSessionsData.IDLE) {
				addVideoBtn.setEnabled(false);
				addVideoBtn.setText(R.string.label_remove_video_btn);
				outgoingVideoView.setVisibility(View.VISIBLE);
				incomingVideoView.setVisibility(View.VISIBLE);
				switchCamBtn.setVisibility(View.VISIBLE);

				Thread thread = new Thread() {
					public void run() {
						addVideo();
					}
				};
				thread.start();

			} else if (IPCallSessionsData.videoCallState == IPCallSessionsData.CONNECTED) {
				addVideoBtn.setEnabled(false);
				addVideoBtn.setText(R.string.label_add_video_btn);
				IPCallSessionsData.videoCallState = IPCallSessionsData.IDLE;

				Thread thread = new Thread() {
					public void run() {
						removeVideo();
					}
				};
				thread.start();
			}
		}
	};

	/**
     * Switch camera button listener
     */
    private View.OnClickListener btnSwitchCamListener = new View.OnClickListener() {
        public void onClick(View v) {
            // Release camera
            releaseCamera();

            // Open the other camera
            if (openedCameraId.getValue() == CameraOptions.BACK.getValue()) {
                OpenCamera(CameraOptions.FRONT);
            } else {
                OpenCamera(CameraOptions.BACK);
            }

            // Restart the preview
            camera.setPreviewCallback(outgoingVideoPlayer);
            startCameraPreview();
        }
    };
	
	/* *****************************************
     *              session events listener
     ***************************************** */
	
	/**
	 * IP call session event listener
	 */
	private IIPCallEventListener sessionEventListener = new IIPCallEventListener.Stub() {
		// Session is started
		public void handleSessionStarted() {
			if (logger.isActivated()) {
				logger.info("sessionEventListener - handleSessionStarted()");
			}
			handler.post(new Runnable() {
				public void run() {
					hangUpBtn.setEnabled(true);
					setOnHoldBtn.setEnabled(true);
					addVideoBtn.setEnabled(true);
					IPCallSessionsData.audioCallState = IPCallSessionsData.CONNECTED;
					hideProgressDialog();
				}
			});
		}

		// Session has been aborted
		public void handleSessionAborted(int reason) {
			if (logger.isActivated()) {
				logger.info("sessionEventListener - handleSessionAborted()");
			}
			Thread thread = new Thread() {
				public void run() {
					stopSession();
				}
			};
			thread.start();
			handler.post(new Runnable() {
				public void run() {
					setOnHoldBtn.setEnabled(false);
					hangUpBtn.setEnabled(false);
					//hideProgressDialog();
					if (IPCallSessionsData.direction.equals("outgoing")){
						exitActivity(getString(R.string.label_outgoing_ipcall_aborted));
					}
					else if (IPCallSessionsData.direction.equals("incoming")){
						exitActivity(getString(R.string.label_incoming_ipcall_aborted));
					}
					else {exitActivity(getString(R.string.label_ipcall_failed));}
					

				}
			});
		}

		// Session has been terminated by remote
		public void handleSessionTerminatedByRemote() {
			if (logger.isActivated()) {
				logger.info("sessionEventListener - handleSessionTerminatedByRemote()");
			}
			handler.post(new Runnable() {
				public void run() {
					setOnHoldBtn.setEnabled(false);
					hangUpBtn.setEnabled(false);
					//hideProgressDialog();
					stopSession();
					if ((IPCallSessionsData.direction != null) && (IPCallSessionsData.direction.equals("outgoing"))){
						exitActivity(getString(R.string.label_outgoing_ipcall_terminated_by_remote));
					}
					else if ((IPCallSessionsData.direction != null) && (IPCallSessionsData.direction.equals("incoming"))){
						exitActivity(getString(R.string.label_incoming_ipcall_terminated_by_remote));
					}	
					else { exitActivity(null);}
				}
			});
		}

		// IP call error
		public void handleCallError(final int error) {
			if (logger.isActivated()) {
				logger.info("sessionEventListener - handleCallError()");
			}		
			Thread thread = new Thread() {
				public void run() {
					stopSession();
				}
			};
			thread.start();
			handler.post(new Runnable() {
				public void run() {
					setOnHoldBtn.setEnabled(false);
					hangUpBtn.setEnabled(false);
					addVideoBtn.setEnabled(false);
					//hideProgressDialog();
					
					if (error == IPCallError.SESSION_INITIATION_DECLINED) {
						exitActivity(getString(R.string.label_ipcall_invitation_declined));
					}
					else {
						exitActivity(getString(R.string.label_ipcall_failed));
					}
				}
			});	
		}

		@Override
		public void handleMediaResized(int arg0, int arg1)
				throws RemoteException {
			// nothing to do

		}

		@Override
		public void handle486Busy() throws RemoteException {
			if (logger.isActivated()) {
				logger.info("sessionEventListener - handle486Busy()");
			}
			Thread thread = new Thread() {
				public void run(){
					stopSession();
				}
			};
			thread.start();
			handler.post(new Runnable() {
				public void run() {
					setOnHoldBtn.setEnabled(false);
					hangUpBtn.setEnabled(false);
					//hideProgressDialog();				
					exitActivity(getString(R.string.label_ipcall_called_is_busy));
				}
			});
		}

		@Override
		public void handleAddVideoInvitation(String arg0, int arg1, int arg2)
				throws RemoteException {
			if (logger.isActivated()) {
				logger.info("sessionEventListener - handleAddVideoInvitation()");
			}
			handler.post(new Runnable() {
				public void run() {
					if ((IPCallSessionActivity.this != null) && (isVisible)) {
						getAddVideoInvitation();
					} else {
						rejectAddVideo();
					}
				}
			});
		}

		@Override
		public void handleAddVideoAborted(int arg0) throws RemoteException {
			if (logger.isActivated()) {
				logger.info("sessionEventListener - handleAddVideoAborted()");
			}
			handler.post(new Runnable() {
				public void run() {
					IPCallSessionsData.videoCallState = IPCallSessionsData.IDLE;
					addVideoBtn.setText(R.string.label_add_video_btn);
					addVideoBtn.setEnabled(true);
					
					if (addVideoInvitationDialog != null){
						addVideoInvitationDialog.dismiss();
						outgoingVideoView.setVisibility(View.GONE);
						incomingVideoView.setVisibility(View.GONE);
						switchCamBtn.setVisibility(View.GONE);
					}
				}
			});
		}

		@Override
		public void handleAddVideoAccepted() throws RemoteException {
			if (logger.isActivated()) {
				logger.info("sessionEventListener - handleAddVideoAccepted()");
			}
			handler.post(new Runnable() {
				public void run() {
					IPCallSessionsData.videoCallState = IPCallSessionsData.CONNECTED;
					addVideoBtn.setText(R.string.label_remove_video_btn);
					addVideoBtn.setEnabled(true);
				}
			});
		}

		@Override
		public void handleRemoveVideoInvitation() throws RemoteException {
			if (logger.isActivated()) {
				logger.info("sessionEventListener - handleRemoveVideoInvitation()");
			}
			handler.post(new Runnable() {
				public void run() {
					IPCallSessionsData.videoCallState = IPCallSessionsData.IDLE;
					addVideoBtn.setText(R.string.label_add_video_btn);
					addVideoBtn.setEnabled(false);
				}
			});
		}

		public void handleRemoveVideoAccepted() throws RemoteException {
			if (logger.isActivated()) {
				logger.info("sessionEventListener - handleRemoveVideoAccepted()");
			}
			handler.post(new Runnable() {
				public void run() {
					IPCallSessionsData.videoCallState = IPCallSessionsData.IDLE;
					addVideoBtn.setText(R.string.label_add_video_btn);
					addVideoBtn.setEnabled(true);
					outgoingVideoView.setVisibility(View.GONE);
					incomingVideoView.setVisibility(View.GONE);
					switchCamBtn.setVisibility(View.GONE);
				}
			});
		}

		public void handleRemoveVideoAborted(int arg0) throws RemoteException {
			if (logger.isActivated()) {
				logger.info("sessionEventListener - handleRemoveVideoAborted()");
			}
			handler.post(new Runnable() {
				public void run() {
					IPCallSessionsData.videoCallState = IPCallSessionsData.CONNECTED;
					addVideoBtn.setText(R.string.label_remove_video_btn);
					addVideoBtn.setEnabled(true);
					
				}
			});
			
		}
	};


	
	/* *****************************************
     *              session methods
     ***************************************** */
	/**
	 * Get incoming session.
	 */
	private void getIncomingSession() {
		if (logger.isActivated()) {
			logger.debug("getIncomingSession()");
		}

		
		handler.post(new Runnable() {
			public void run() {
				try {
					IPCallSessionsData.session = IPCallSessionsData.callApi.getSession(IPCallSessionsData.sessionId);
					IPCallSessionsData.sessionEventListener = IPCallSessionActivity.this.sessionEventListener ;
					IPCallSessionsData.session.addSessionListener(IPCallSessionsData.sessionEventListener);
					
					// construct AlertDialog to accept/reject call if incoming call
					AlertDialog.Builder builder = new AlertDialog.Builder(IPCallSessionActivity.this);
					builder.setTitle(R.string.title_recv_ipcall_audio);
					builder.setMessage(getString(R.string.label_from) + " " + IPCallSessionsData.remoteContact);
					builder.setCancelable(false);
					builder.setIcon(R.drawable.ri_notif_csh_icon);
					builder.setPositiveButton(getString(R.string.label_accept),
							acceptBtnListener);
					builder.setNegativeButton(getString(R.string.label_decline),
							declineBtnListener);
					builder.show();
				} catch (final Exception e) {
					handler.post(new Runnable() {
						public void run() {
							displayErrorAndExitActivity(getString(R.string.label_incoming_ipcall_failed)+" - error:"+e.getMessage());
						}
					});
				}
			}
		});
	
	}
	
	
	private void startOutgoingSession(boolean video) {
		if (logger.isActivated()) {
			logger.debug("startOutgoingSession()");
		}
		final Boolean   videoOption = video;
		
		Thread thread = new Thread() {
			public void run() {
				try {
					// Initiate IP call outgoing session
					if (videoOption) {
						IPCallSessionsData.session = IPCallSessionsData.callApi.initiateCall(IPCallSessionsData.remoteContact,
								outgoingAudioPlayer, incomingAudioRenderer,
								outgoingVideoPlayer, incomingVideoRenderer);
					} else {
						IPCallSessionsData.session = IPCallSessionsData.callApi.initiateCall(IPCallSessionsData.remoteContact,
								outgoingAudioPlayer, incomingAudioRenderer);
					}
					IPCallSessionsData.sessionEventListener = IPCallSessionActivity.this.sessionEventListener ;
					IPCallSessionsData.session.addSessionListener(IPCallSessionsData.sessionEventListener);
					IPCallSessionsData.sessionId = IPCallSessionsData.session.getSessionID();
				} catch (final Exception e) {
					displayErrorAndExitActivity(getString(R.string.label_outgoing_ipcall_failed)
							+ " - error:" + e.getMessage());
				}
			}
		};
		thread.start();
		IPCallSessionsData.audioCallState = IPCallSessionsData.CONNECTING ;
		displayProgressDialog();
	}

	
	/**
	 * Recover incoming/outgoing sessions.
	 */
	private void recoverSessions() {
		if (logger.isActivated()) {
			logger.debug("recoverSessions()");
		}
		if (logger.isActivated()) {
			logger.debug("IPCallSessionsData.session ="+IPCallSessionsData.session);
			logger.debug("IPCallSessionsData.sessionId ="+IPCallSessionsData.sessionId);
			logger.debug("IPCallSessionsData.audioCallState ="+IPCallSessionsData.audioCallState);
		}
		
		if (IPCallSessionsData.audioCallState != IPCallSessionsData.CONNECTED) {
			exitActivity("no session established");
		}
		
		else  {	// session established	
			// enables buttons
			setOnHoldBtn.setText(R.string.label_set_onhold_btn);
			hangUpBtn.setEnabled(true);
			setOnHoldBtn.setEnabled(true);
			addVideoBtn.setEnabled(true);
			if (IPCallSessionsData.videoCallState == IPCallSessionsData.CONNECTED){							
				addVideoBtn.setText(R.string.label_remove_video_btn);
			}
			else {addVideoBtn.setText(R.string.label_add_video_btn);}
			
			
			Thread thread = new Thread() {
				public void run() {
					try {
						if (IPCallSessionsData.session != null) {
							if (logger.isActivated()) {
								logger.info("incomingIPCallSession= "
										+ IPCallSessionsData.session);
							// remove "old" session event listener
							IPCallSessionsData.session.removeSessionListener(IPCallSessionsData.sessionEventListener);
							//store "new" session event listener in IPCallSessionsData class
							IPCallSessionsData.sessionEventListener = sessionEventListener ;
							//add "new" session event listener on session
							IPCallSessionsData.session.addSessionListener(IPCallSessionsData.sessionEventListener);

							}									
						} else {
							exitActivity("no session established");
						}
					} catch (final Exception e) {
						displayErrorAndExitActivity("Recover sessions failed - error:"
								+ e.getMessage());
					}
				}
			};
			thread.start();
		}
	}


	/**
	 * Accept incoming session.
	 */
	private void acceptIncomingSession() {
		// Accept the session in background
		Thread thread = new Thread() {
			public void run() {
				try {
					// Accept the invitation
					IPCallSessionsData.session.setAudioPlayer(outgoingAudioPlayer);
					IPCallSessionsData.session.setAudioRenderer(incomingAudioRenderer);					
					IPCallSessionsData.session.acceptSession(true, false);
					IPCallSessionsData.audioCallState = IPCallSessionsData.CONNECTING ;					
				} catch (final Exception e) {
					displayErrorAndExitActivity(getString(R.string.label_invitation_failed)
							+ " - error:" + e.getMessage());
				}
				
			}
		};
		thread.start();	
		
		// display lodaer
		displayProgressDialog();		
	}

	/**
	 * Decline incoming session.
	 */
	private void declineIncomingSession() {
		Thread thread = new Thread() {
			public void run() {
				try {
					// Reject the invitation
					IPCallSessionsData.session.removeSessionListener(IPCallSessionsData.sessionEventListener);
					IPCallSessionsData.session.rejectSession();
					IPCallSessionsData.session = null;
					IPCallSessionsData.sessionEventListener = null;

					IPCallSessionsData.audioCallState = IPCallSessionsData.IDLE;
					IPCallSessionsData.videoCallState = IPCallSessionsData.IDLE;

					// Exit activity
					exitActivity(null);
				} catch (final Exception e) {
					displayErrorAndExitActivity(getString(R.string.label_invitation_failed)
							+ " - error:" + e.getMessage());
				}
			}
		};
		thread.start();
	}
	
	/**
	 * Stop the outgoing session
	 */
	private void stopSession() {
		if (logger.isActivated()) {
			logger.debug("stopSession()");
		}

		if (IPCallSessionsData.session != null) {
			try {
				IPCallSessionsData.session.removeSessionListener(sessionEventListener);
				IPCallSessionsData.session.cancelSession();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	
	
	/**
	 * Add video to Audio session
	 */
	private void addVideo() {
		if (logger.isActivated()) {
			logger.debug("addVideo()");
		}

		if (IPCallSessionsData.session != null) {
			IPCallSessionsData.videoCallState = IPCallSessionsData.CONNECTING;			
			try {				
				IPCallSessionsData.session.addVideo(outgoingVideoPlayer,incomingVideoRenderer);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * remove video from session.
	 */
	private void removeVideo() {
		if (logger.isActivated()) {
			logger.debug("removeVideo()");
		}
		
		if (IPCallSessionsData.session != null) {
			IPCallSessionsData.videoCallState = IPCallSessionsData.IDLE;
			try {
				IPCallSessionsData.session.removeVideo();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * get Add Video Invitation and display Dialog Box
	 */
	private void getAddVideoInvitation() {
		if (logger.isActivated()) {
			logger.debug("getAddVideoInvitation()");
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(
				IPCallSessionActivity.this).setTitle("Add Video Invitation");
		if (logger.isActivated()) {
			logger.debug("IPCallSessionActivity.this :"
					+ IPCallSessionActivity.this);
		}

		// Add the buttons
		builder.setPositiveButton(R.string.label_accept,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						acceptAddVideo();
					}
				});
		builder.setNegativeButton(R.string.label_decline,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						rejectAddVideo();
					}
				});

		addVideoInvitationDialog = builder.create();
		addVideoInvitationDialog.setCancelable(false);
		addVideoInvitationDialog.show();

	}
	
	/**
	 * Accept Add Video Invitation 
	 */
	private void acceptAddVideo() {
		if (logger.isActivated()) {
			logger.info("acceptAddVideo()");
		}
		try {
			IPCallSessionsData.session.setVideoPlayer(outgoingVideoPlayer);
			IPCallSessionsData.session.setVideoRenderer(incomingVideoRenderer);
			IPCallSessionsData.session.acceptAddVideo();
			IPCallSessionsData.videoCallState = IPCallSessionsData.CONNECTED;
			addVideoBtn.setText(R.string.label_remove_video_btn);
			addVideoBtn.setEnabled(true);
			outgoingVideoView.setVisibility(View.VISIBLE);
			incomingVideoView.setVisibility(View.VISIBLE);
			switchCamBtn.setVisibility(View.VISIBLE);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Reject Add Video Invitation 
	 */
	private void rejectAddVideo() {
		if (logger.isActivated()) {
			logger.info("rejectAddVideo()");
		}
		try {
			IPCallSessionsData.session.rejectAddVideo();
			IPCallSessionsData.videoCallState = IPCallSessionsData.IDLE;
			addVideoBtn.setText(R.string.label_add_video_btn);
			addVideoBtn.setEnabled(true);
		} catch (RemoteException e) {
			e.printStackTrace();
		}		
	}
	

	/**
	 * Exit activity if all sessions are stopped.
	 * 
	 * @param message
	 *            the message to display. Can be null for no message.
	 */
	private void exitActivity(String message) {
		if (logger.isActivated()) {
			logger.info("exitActivity()");
		}

		hideProgressDialog();
		IPCallSessionsData.audioCallState = IPCallSessionsData.IDLE;
		IPCallSessionsData.videoCallState = IPCallSessionsData.IDLE;
		IPCallSessionsData.removeSessionData(IPCallSessionsData.sessionId);
		IPCallSessionsData.session = null;
		IPCallSessionsData.sessionId = null;
		IPCallSessionsData.sessionEventListener = null;

		if (message == null) {
			this.finish();
		} else {
			Utils.showMessageAndExit(IPCallSessionActivity.this, message);
		}
	}

	private void displayErrorAndExitActivity(final String errorMsg) {
		if (logger.isActivated()) {
			logger.info("displayErrorAndExitActivity()");
		}
		handler.post(new Runnable() {
			public void run() {
				hideProgressDialog();
				IPCallSessionsData.audioCallState = IPCallSessionsData.DISCONNECTED;
				IPCallSessionsData.videoCallState = IPCallSessionsData.DISCONNECTED;								
				AlertDialog alert =Utils.showMessage(IPCallSessionActivity.this,
							errorMsg);				
				alert.setButton("OK",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								IPCallSessionActivity.this.exitActivity(null);
							}
						});
			}
		});
	}
	
	/**
	 * Display progress dialog
	 */
	private void displayProgressDialog() {
		if (logger.isActivated()) {
			logger.debug("displayProgressDialog()");
			logger.debug("outgoingProgressDialog= "+outgoingProgressDialog);
		}
		// Display a progress dialog
		outgoingProgressDialog = Utils.showProgressDialog(
				IPCallSessionActivity.this,
				getString(R.string.label_command_in_progress));
		outgoingProgressDialog.setCancelable(false);
	}

	/**
	 * Hide progress dialog
	 */
	public void hideProgressDialog() {
		if (logger.isActivated()) {
			logger.debug("hideProgressDialog()");
			logger.debug("outgoingProgressDialog= "+outgoingProgressDialog);
		}
		if (outgoingProgressDialog != null
				&& outgoingProgressDialog.isShowing()) {
			outgoingProgressDialog.dismiss();
			outgoingProgressDialog = null;
		}
	}

	

    /* *****************************************
     *                Camera
     ***************************************** */

    /**
     * Get Camera "open" Method
     *
     * @return Method
     */
    private Method getCameraOpenMethod() {
        ClassLoader classLoader = VisioSharing.class.getClassLoader();
        Class cameraClass = null;
        try {
            cameraClass = classLoader.loadClass("android.hardware.Camera");
            try {
                return cameraClass.getMethod("open", new Class[] {
                    int.class
                });
            } catch (NoSuchMethodException e) {
                return null;
            }
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    /**
     * Get Camera "numberOfCameras" Method
     *
     * @return Method
     */
    private Method getCameraNumberOfCamerasMethod() {
        ClassLoader classLoader = VisioSharing.class.getClassLoader();
        Class cameraClass = null;
        try {
            cameraClass = classLoader.loadClass("android.hardware.Camera");
            try {
                return cameraClass.getMethod("getNumberOfCameras", (Class[])null);
            } catch (NoSuchMethodException e) {
                return null;
            }
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    /**
     * Get number of cameras
     *
     * @return number of cameras
     */
    private int getNumberOfCameras() {
        Method method = getCameraNumberOfCamerasMethod();
        if (method != null) {
            try {
                Integer ret = (Integer)method.invoke(null, (Object[])null);
                return ret.intValue();
            } catch (Exception e) {
                return 1;
            }
        } else {
            return 1;
        }
    }

    /**
     * Open a camera
     *
     * @param cameraId
     */
    private void OpenCamera(CameraOptions cameraId) {
        Method method = getCameraOpenMethod();
        if (numberOfCameras > 1 && method != null) {
            try {
                camera = (Camera)method.invoke(camera, new Object[] {
                    cameraId.getValue()
                });
                openedCameraId = cameraId;
            } catch (Exception e) {
                camera = Camera.open();
                openedCameraId = CameraOptions.BACK;
            }
        } else {
            camera = Camera.open();
            openedCameraId = CameraOptions.BACK;
        }
        if (outgoingVideoPlayer != null) {
            outgoingVideoPlayer.setCameraId(openedCameraId.getValue());
        }
    }

    /**
     * Check if good camera sizes are available for encoder.
     * Must be used only before open camera.
     * 
     * @param cameraId
     * @return false if the camera don't have the good preview size for the encoder
     */
    private boolean checkCameraSize(CameraOptions cameraId) {
        boolean sizeAvailable = false;

        // Open the camera
        OpenCamera(cameraId);

        // Check common sizes
        Parameters param = camera.getParameters();
        List<Camera.Size> sizes = param.getSupportedPreviewSizes();
        for (Camera.Size size:sizes) {
            if (    (size.width == H264Config.QVGA_WIDTH && size.height == H264Config.QVGA_HEIGHT) ||
                    (size.width == H264Config.CIF_WIDTH && size.height == H264Config.CIF_HEIGHT) ||
                    (size.width == H264Config.VGA_WIDTH && size.height == H264Config.VGA_HEIGHT)) {
                sizeAvailable = true;
                break;
            }
        }

        // Release camera
        releaseCamera();

        return sizeAvailable;
    }

    /**
     * Start the camera preview
     */
	private void startCameraPreview() {
        if (camera != null) {
            // Camera settings
            Camera.Parameters p = camera.getParameters();
            p.setPreviewFormat(PixelFormat.YCbCr_420_SP); //ImageFormat.NV21);

            // Orientation
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
                Display display = ((WindowManager)getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
                switch (display.getRotation()) {
                    case Surface.ROTATION_0:
                        if (logger.isActivated()) {
                            logger.debug("ROTATION_0");
                        }
                        if (openedCameraId == CameraOptions.FRONT) {
                            outgoingVideoPlayer.setOrientation(Orientation.ROTATE_90_CCW);
                        } else {
                            outgoingVideoPlayer.setOrientation(Orientation.ROTATE_90_CW);
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
                            camera.setDisplayOrientation(90);
                        } else {
                            p.setRotation(90);
                        }
                        break;
                    case Surface.ROTATION_90:
                        if (logger.isActivated()) {
                            logger.debug("ROTATION_90");
                        }
                        outgoingVideoPlayer.setOrientation(Orientation.NONE);
                        break;
                    case Surface.ROTATION_180:
                        if (logger.isActivated()) {
                            logger.debug("ROTATION_180");
                        }
                        if (openedCameraId == CameraOptions.FRONT) {
                            outgoingVideoPlayer.setOrientation(Orientation.ROTATE_90_CW);
                        } else {
                            outgoingVideoPlayer.setOrientation(Orientation.ROTATE_90_CCW);
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
                            camera.setDisplayOrientation(270);
                        } else {
                            p.setRotation(270);
                        }
                        break;
                    case Surface.ROTATION_270:
                        if (logger.isActivated()) {
                            logger.debug("ROTATION_270");
                        }
                        if (openedCameraId == CameraOptions.FRONT) {
                            outgoingVideoPlayer.setOrientation(Orientation.ROTATE_180);
                        } else {
                            outgoingVideoPlayer.setOrientation(Orientation.ROTATE_180);
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
                            camera.setDisplayOrientation(180);
                        } else {
                            p.setRotation(180);
                        }
                        break;
                }
            } else {
                // getRotation not managed under Froyo
                outgoingVideoPlayer.setOrientation(Orientation.NONE);
            }

            // Camera size
            List<Camera.Size> sizes = p.getSupportedPreviewSizes();
            if (sizeContains(sizes, outgoingWidth, outgoingHeight)) {
                // Use the existing size without resizing
                p.setPreviewSize(outgoingWidth, outgoingHeight);
                outgoingVideoPlayer.activateResizing(outgoingWidth, outgoingHeight); // same size = no resizing
                if (logger.isActivated()) {
                    logger.info("Camera preview initialized with size " + outgoingWidth + "x" + outgoingHeight);
                }
            } else {
                // Check if can use a other known size (QVGA, CIF or VGA)
                int w = 0;
                int h = 0;
                for (Camera.Size size:sizes) {
                    w = size.width;
                    h = size.height;
                    if (    (w == H264Config.QVGA_WIDTH && h == H264Config.QVGA_HEIGHT) ||
                            (w == H264Config.CIF_WIDTH && h == H264Config.CIF_HEIGHT) ||
                            (w == H264Config.VGA_WIDTH && h == H264Config.VGA_HEIGHT)) {
                        break;
                    }
                }

                if (w != 0) {
                    p.setPreviewSize(w, h);
                    outgoingVideoPlayer.activateResizing(w, h);
                    if (logger.isActivated()) {
                        logger.info("Camera preview initialized with size " + w + "x" + h + " with a resizing to " + outgoingWidth + "x" + outgoingHeight);
                    }
                } else {
                    // The camera don't have known size, we can't use it
                    if (logger.isActivated()) {
                        logger.warn("Camera preview can't be initialized with size " + outgoingWidth + "x" + outgoingHeight);
                    }
                    camera = null;
                    return;
                }
            }

            camera.setParameters(p);
            try {
                camera.setPreviewDisplay(outgoingVideoView.getHolder());
                camera.startPreview();
                cameraPreviewRunning = true;
            } catch (Exception e) {
                camera = null;
            }
        }
    }

    /**
     * Release the camera
     */
    private void releaseCamera() {
        if (camera != null) {
            camera.setPreviewCallback(null);
            if (cameraPreviewRunning) {
                cameraPreviewRunning = false;
                camera.stopPreview();
            }
            camera.release();
            camera = null;
        }
    }

    /**
     * Test if size is in list.
     * Can't use List.contains because it doesn't work with some devices.
     *
     * @param list
     * @param width
     * @param height
     * @return boolean
     */
    private boolean sizeContains(List<Camera.Size> list, int width, int height) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).width == width && list.get(i).height == height) {
                return true;
            }
        }
        return false;
    }

    /**
     * Start the camera
     */
    private void startCamera() {
        if (camera == null) {
            // Open camera
            OpenCamera(openedCameraId);
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
                outgoingVideoView.setAspectRatio(outgoingWidth, outgoingHeight);
            } else {
                outgoingVideoView.setAspectRatio(outgoingHeight, outgoingWidth);
            }
            // Start camera
            camera.setPreviewCallback(outgoingVideoPlayer);
            startCameraPreview();
        } else {
            if (logger.isActivated()) {
                logger.error("Camera is not null");
            }
        }
    }

    /**
     * ReStart the camera
     */
    private void reStartCamera() {
        if (camera != null) {
            releaseCamera();
        }
        startCamera();
    }

    /* *****************************************
     *          SurfaceHolder.Callback
     ***************************************** */

    @Override
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
        isSurfaceCreated = true;
    }

    @Override
    public void surfaceCreated(SurfaceHolder arg0) {
        isSurfaceCreated = true;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder arg0) {
        isSurfaceCreated = false;
    }
    
    
    /* *****************************************
    * Video session methods
    ***************************************** */
    
    public void prepareVideoSession(){
    	if (logger.isActivated()) {
			logger.info("prepareVideoSession()");
		}
    	
    	numberOfCameras = getNumberOfCameras();
    	
        if (numberOfCameras > 1) {
            boolean backAvailable = checkCameraSize(CameraOptions.BACK);
            boolean frontAvailable = checkCameraSize(CameraOptions.FRONT);
            if (frontAvailable && backAvailable) {
                switchCamBtn.setOnClickListener(btnSwitchCamListener);
            } else if (frontAvailable) {
                openedCameraId = CameraOptions.FRONT;
                switchCamBtn.setVisibility(View.INVISIBLE);
            } else if (backAvailable) {
                openedCameraId = CameraOptions.BACK;
                switchCamBtn.setVisibility(View.INVISIBLE);
            } else {
                // TODO: Error - no camera available for encoding
            }
        } else {
            if (checkCameraSize(CameraOptions.FRONT)) {
                switchCamBtn.setVisibility(View.INVISIBLE);
            } else {
                // TODO: Error - no camera available for encoding
            }
        }
        
        // Set incoming video preview
        incomingVideoView = (VideoSurfaceView)findViewById(R.id.incoming_ipcall_video_view);
        if (incomingWidth != 0 && incomingHeight != 0) {
            incomingVideoView.setAspectRatio(incomingWidth, incomingHeight);
        }
        
        // Create the live video player
        outgoingVideoView = (VideoSurfaceView)findViewById(R.id.outgoing_ipcall_video_preview);
        if (outgoingWidth == 0 || outgoingHeight == 0) {
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
                outgoingVideoView.setAspectRatio(H264Config.QCIF_WIDTH, H264Config.QCIF_HEIGHT);
            } else {
                outgoingVideoView.setAspectRatio(H264Config.QCIF_HEIGHT, H264Config.QCIF_WIDTH);
            }
        } else {
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
                outgoingVideoView.setAspectRatio(outgoingWidth, outgoingHeight);
            } else {
                outgoingVideoView.setAspectRatio(outgoingHeight, outgoingWidth);
            }
        }
        surface = outgoingVideoView.getHolder();
        surface.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surface.setKeepScreenOn(true);
        surface.addCallback(this);
    }
}
