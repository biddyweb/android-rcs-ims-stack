package com.orangelabs.rcs.ri.ipcall;

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
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.orangelabs.rcs.core.ims.service.ipcall.IPCallError;
import com.orangelabs.rcs.provider.settings.RcsSettings;
import com.orangelabs.rcs.ri.R;
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
import com.orangelabs.rcs.utils.logger.Logger;

public class IPCallSessionActivity extends Activity implements
		ClientApiListener, ImsEventListener {

	/**
	 * UI handler
	 */
	private final Handler handler = new Handler();

	/**
	 * IP call API
	 */
	private IPCallApi callApi = null;

	/**
	 * IP call API connected
	 */
	private static boolean isCallApiConnected = false;

	/**
	 * IP call session
	 */
	private IIPCallSession ipCallSession = null;

	/**
	 * session ID
	 */
	private static String sessionId = null;

	/**
	 * store previous incoming session ID
	 */
	private static String lastIncomingSessionId = null;

	/**
	 * Progress dialog
	 */
	private static Dialog outgoingProgressDialog = null;
	
	/**
	 * Progress dialog
	 */
	private AlertDialog addVideoInvitationDialog;

	/**
	 * IP Call IDLE
	 */
	private static final int IDLE = 0;
	
	/**
	 * IP Call released
	 */
	private static final int DISCONNECTED = 4;

	/**
	 * IP Call established
	 */
	private static final int CONNECTED = 2;

	/**
	 * IP call connecting
	 */
	private static final int CONNECTING = 1;

	/**
	 * IP Call on Hold
	 */
	private static final int ON_HOLD = 6;

	/**
	 * Audio Call State
	 */
	private static int audioCallState = IDLE;

	/**
	 * Video Call State
	 */
	private static int videoCallState = IDLE;

	/**
	 * RemoteContact
	 */
	private static String remoteContact;

	/**
	 * Direction
	 */
	private static String direction;
	
	/**
	 * Video option
	 */
	private boolean videoOption = false;

	/**
	 * Wait API connected to launch recoverSessions()
	 */
	private boolean recoverSessionsWhenApiConnected = false;

	/**
	 * Wait API connected to do getIncomingSession
	 */
	private boolean getIncomingSessionWhenApiConnected = false;

	/**
	 * Wait API connected to do startOutgoingSession
	 */
	private boolean startOutgoingSessionWhenApiConnected = false;

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
	 * Audio player
	 */
	private IAudioPlayer outgoingAudioPlayer = null;

	/**
	 * Audio renderer
	 */
	private IAudioRenderer incomingAudioRenderer = null;

	/**
	 * Video player
	 */
	private LiveVideoPlayer outgoingVideoPlayer = null;

	/**
	 * Video renderer
	 */
	private VideoRenderer incomingVideoRenderer = null;

	/**
	 * The logger
	 */
	private static Logger logger = Logger.getLogger("IPCallSessionActivity");

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

		// instantiate Audio Player and Renderer
		outgoingAudioPlayer = new LiveAudioPlayer();
		incomingAudioRenderer = new AudioRenderer();

		// instantiate Audio Player and Renderer
		outgoingVideoPlayer = new LiveVideoPlayer();
		incomingVideoRenderer = new VideoRenderer();

		// Instantiate IP call API
		if (callApi == null) {
			callApi = new IPCallApi(getApplicationContext());
			callApi.addApiEventListener(this);
			callApi.addImsEventListener(this);
			callApi.connectApi();
		}

		// IP call already established or in connecting state - recover existing sessions
		if ((sessionId!= null)&&((audioCallState == CONNECTING) || (audioCallState == CONNECTED)
				|| (audioCallState == ON_HOLD))) {
			if (isCallApiConnected) {
				recoverSessions();
			} else {
				recoverSessionsWhenApiConnected = true;
			}
		} else if (audioCallState == IDLE) {
			// Get intent info
			remoteContact = getIntent().getStringExtra("contact");
			direction = getIntent().getAction();
			if (direction.equals("incoming")) {
				lastIncomingSessionId = sessionId;
				sessionId = getIntent().getStringExtra("sessionId");
				//set call state
				audioCallState = CONNECTING;
				// remove notification
				if (sessionId != null) {
					removeIPCallNotification(getApplicationContext(), sessionId);
				}
				// get incoming ip call
				if ((lastIncomingSessionId != sessionId)) {
					if (isCallApiConnected) {
						getIncomingSession();
					} else {
						getIncomingSessionWhenApiConnected = true;
					}
				}
			}
			// initiate a new outgoing ip call
			if (direction.equals("outgoing")) {
				audioCallState = CONNECTING;
				remoteContact = getIntent().getStringExtra("contact");
				videoOption = getIntent().getBooleanExtra("video", false);
				if (isCallApiConnected) {
					startOutgoingSession(videoOption);
				} else {
					startOutgoingSessionWhenApiConnected = true;
				}
			}
		}
	}

	public void onResume() {
		super.onResume();
		if (logger.isActivated()) {
			logger.info("onResume()");
		}
		// set contact number
		TextView fromTxt = (TextView) findViewById(R.id.ipcall_with_txt);
		fromTxt.setText(getString(R.string.label_audio_with, remoteContact));
		
		// display API state - call state - session and session Id
		if (logger.isActivated()) {
			logger.info("isCallApiConnected =" + isCallApiConnected);
			logger.info("audioCallState =" + audioCallState);
			logger.info("videoCallState =" + videoCallState);
			logger.info("sessionId =" + sessionId);
			logger.info("ipCallSession =" + ipCallSession);
		}
		
		// IP call already established or in connecting state - recover existing
		// session(s)

		if (audioCallState == CONNECTING) {
			//displayProgressDialog();
		} else if ((audioCallState == CONNECTED) || (audioCallState == ON_HOLD)) {
			//hideProgressDialog();
		} else if (audioCallState == IDLE){ 
			// nothing to do
		}
		else if (audioCallState == DISCONNECTED){//DISCONNECTED state 
			// error msg displayer nothing to do
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (logger.isActivated()) {
			logger.info("onPause()");
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (logger.isActivated()) {
			logger.info("onNewIntent()");
		}
		setIntent(intent);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (logger.isActivated()) {
			logger.info("onDestroy()");
		}

		// hide ProgressDialog
		//hideProgressDialog();

		// Disconnect ip call API
		callApi.removeApiEventListener(this);
		callApi.disconnectApi();
		isCallApiConnected = false;
	}


	/**
	 * Accept button listener
	 */
	private OnClickListener acceptBtnListener = new OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			removeIPCallNotification(getApplicationContext(), sessionId);
			acceptIncomingSession();
		}
	};

	/**
	 * Reject button listener
	 */
	private OnClickListener declineBtnListener = new OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			removeIPCallNotification(getApplicationContext(), sessionId);
			declineIncomingSession();
		}
	};

	/**
	 * Set On Hold button listener
	 */
	private View.OnClickListener btnOnHoldListener = new View.OnClickListener() {
		public void onClick(View v) {

			if (audioCallState != ON_HOLD) {
				try {
					ipCallSession.setCallHold(true);
				} catch (Exception e) {
				}
				Button holdBtn = (Button) findViewById(R.id.set_onhold_btn);
				holdBtn.setText(R.string.label_resume_btn);
				// audioCallState = ON_HOLD ;
			} else {
				try {
					ipCallSession.setCallHold(false);
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
					exitActivityIfNoSession(null);
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
				logger.info("videoCallState =" + videoCallState);
				
			}
			if (videoCallState == IDLE) {
				addVideoBtn.setEnabled(false);
				addVideoBtn.setText(R.string.label_remove_video_btn);
				if (logger.isActivated()) {	
					logger.info("videoCallState =" + videoCallState);
					logger.info("AddVideoBtn.setText(R.string.label_remove_video_btn)");
					logger.info("AddVideoBtn.setEnabled = false");
				}

				Thread thread = new Thread() {
					public void run() {
						addVideo();
					}
				};
				thread.start();

			} else if (videoCallState == CONNECTED) {
				addVideoBtn.setEnabled(false);
				addVideoBtn.setText(R.string.label_add_video_btn);
				videoCallState = IDLE;
				if (logger.isActivated()) {
					logger.info("videoCallState =" + videoCallState);
					logger.info("AddVideoBtn.setText(R.string.label_add_video_btn)");
					logger.info("AddVideoBtn.setEnabled = false");
				}
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
	 * Outgoing IP call session event listener
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
					audioCallState = CONNECTED;
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
					if (direction.equals("outgoing")){
						exitActivityIfNoSession(getString(R.string.label_outgoing_ipcall_aborted));
					}
					else if (direction.equals("incoming")){
						exitActivityIfNoSession(getString(R.string.label_incoming_ipcall_aborted));
					}
					else {exitActivityIfNoSession(getString(R.string.label_ipcall_failed));}
					

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
					if ((direction != null) && (direction.equals("outgoing"))){
						exitActivityIfNoSession(getString(R.string.label_outgoing_ipcall_terminated_by_remote));
					}
					else if ((direction != null) && (direction.equals("incoming"))){
						exitActivityIfNoSession(getString(R.string.label_incoming_ipcall_terminated_by_remote));
					}	
					else { exitActivityIfNoSession(null);}
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
						exitActivityIfNoSession(getString(R.string.label_ipcall_invitation_declined));
					}
					else {
						exitActivityIfNoSession(getString(R.string.label_ipcall_failed));
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
					exitActivityIfNoSession(getString(R.string.label_ipcall_called_is_busy));
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
					getAddVideoInvitation();	
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
					videoCallState = IDLE;
					addVideoBtn.setText(R.string.label_add_video_btn);
					addVideoBtn.setEnabled(true);
					
					if (addVideoInvitationDialog != null){
						addVideoInvitationDialog.dismiss();
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
					videoCallState = CONNECTED;
					addVideoBtn.setText(R.string.label_remove_video_btn);
					addVideoBtn.setEnabled(true);
				}
			});
			if (logger.isActivated()) {
				logger.info("videoCallState =" + videoCallState);
				logger.info("AddVideoBtn.setText(R.string.label_remove_video_btn)");
				logger.info("AddVideoBtn.setEnabled = true");
			}
		}

		@Override
		public void handleRemoveVideoInvitation() throws RemoteException {
			if (logger.isActivated()) {
				logger.info("sessionEventListener - handleRemoveVideoInvitation()");
			}
			handler.post(new Runnable() {
				public void run() {
					videoCallState = IDLE;
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
					videoCallState = IDLE;
					addVideoBtn.setText(R.string.label_add_video_btn);
					addVideoBtn.setEnabled(true);
				}
			});
		}

		public void handleRemoveVideoAborted(int arg0) throws RemoteException {
			if (logger.isActivated()) {
				logger.info("sessionEventListener - handleRemoveVideoAborted()");
			}
			handler.post(new Runnable() {
				public void run() {
					videoCallState = CONNECTED;
					addVideoBtn.setText(R.string.label_remove_video_btn);
					addVideoBtn.setEnabled(true);
				}
			});
			
		}
	};

	
	/**
	 * Client is connected to the IMS
	 */
	@Override
	public void handleImsConnected() {
		if (logger.isActivated()) {
			logger.debug("IMS, connected");
		}
		// nothing to do
	}

	/**
	 * Client is disconnected from the IMS
	 * 
	 * @param reason
	 *            Disconnection reason
	 */
	@Override
	public void handleImsDisconnected(int arg0) {
		if (logger.isActivated()) {
			logger.debug("IMS, disconnected");
		}
		// IMS has been disconnected
		handler.post(new Runnable() {
			public void run()  {
				exitActivityOnError(getString(R.string.label_ims_disconnected));
//				Utils.showMessageAndExit(IPCallSessionActivity.this,
//						getString(R.string.label_ims_disconnected));
			}
		});
	}

	@Override
	public void handleApiConnected() {
		if (logger.isActivated()) {
			logger.debug("API, connected");
		}
		isCallApiConnected = true;
		if (getIncomingSessionWhenApiConnected) {
			getIncomingSession();
			getIncomingSessionWhenApiConnected = false;
		}
		if (startOutgoingSessionWhenApiConnected) {
			startOutgoingSession(videoOption);
			startOutgoingSessionWhenApiConnected = false;
		}
		if (recoverSessionsWhenApiConnected) {
			recoverSessions();
			recoverSessionsWhenApiConnected = false;
		}
	}

	@Override
	public void handleApiDisabled() {
		if (logger.isActivated()) {
			logger.debug("API, disabled");
		}
		isCallApiConnected = false;

		handler.post(new Runnable() {
			public void run() {
				exitActivityOnError(getString(R.string.label_api_disabled));
//				Utils.showMessageAndExit(IPCallSessionActivity.this,
//						getString(R.string.label_api_disabled));
			}
		});
	}

	@Override
	public void handleApiDisconnected() {
		if (logger.isActivated()) {
			logger.debug("API, disconnected");
		}
		isCallApiConnected = false;

		// Service has been disconnected
		handler.post(new Runnable() {
			public void run() {
				exitActivityOnError(getString(R.string.label_api_disconnected));
//				Utils.showMessageAndExit(IPCallSessionActivity.this,
//						getString(R.string.label_api_disconnected));
			}
		});
	}


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
					ipCallSession = callApi.getSession(sessionId);
					ipCallSession.addSessionListener(sessionEventListener);
					// construct AlertDialog to accept/reject call if incoming call
					AlertDialog.Builder builder = new AlertDialog.Builder(IPCallSessionActivity.this);
					builder.setTitle(R.string.title_recv_ipcall_audio);
					builder.setMessage(getString(R.string.label_from) + " " + remoteContact);
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
							exitActivityOnError(getString(R.string.label_incoming_ipcall_failed)+" - error:"+e.getMessage());
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
		Thread thread = new Thread() {
			public void run() {
				try {
					// Initiate IP call outgoing session
					if (videoOption) {
						ipCallSession = callApi.initiateCall(remoteContact,
								outgoingAudioPlayer, incomingAudioRenderer,
								outgoingVideoPlayer, incomingVideoRenderer);
					} else {
						ipCallSession = callApi.initiateCall(remoteContact,
								outgoingAudioPlayer, incomingAudioRenderer);
					}
					ipCallSession.addSessionListener(sessionEventListener);
					sessionId = ipCallSession.getSessionID();
				} catch (final Exception e) {
					exitActivityOnError(getString(R.string.label_outgoing_ipcall_failed)
							+ " - error:" + e.getMessage());
				}
			}
		};
		thread.start();
		displayProgressDialog();
	}

	
	/**
	 * Recover incoming/outgoing sessions.
	 */
	private void recoverSessions() {
		if (logger.isActivated()) {
			logger.debug("recoverSessions()");
		}

		// enables buttons
		if (audioCallState== CONNECTED)                   {							
			setOnHoldBtn.setText(R.string.label_set_onhold_btn);
			hangUpBtn.setEnabled(true);
			setOnHoldBtn.setEnabled(true);
			addVideoBtn.setEnabled(true);
			if (videoCallState == CONNECTED){							
				addVideoBtn.setText(R.string.label_remove_video_btn);
			}
			else {addVideoBtn.setText(R.string.label_add_video_btn);}
			
		}
		
		Thread thread = new Thread() {
			public void run() {
				try {
					if (sessionId != null) {
						// get session and set listener
						ipCallSession = callApi.getSession(sessionId);
						ipCallSession.addSessionListener(sessionEventListener);
						if (logger.isActivated()) {
							logger.info("incomingIPCallSession= "
									+ ipCallSession);
						}									
					} else {
						exitActivityIfNoSession("no session established");
					}
				} catch (final Exception e) {
					exitActivityOnError("Recover sessions failed - error:"
							+ e.getMessage());
				}
			}
		};
		thread.start();
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
					ipCallSession.setAudioPlayer(outgoingAudioPlayer);
					ipCallSession.setAudioRenderer(incomingAudioRenderer);					
					ipCallSession.acceptSession(true, false);
					audioCallState = CONNECTED;
				} catch (final Exception e) {
					exitActivityOnError(getString(R.string.label_invitation_failed)
							+ " - error:" + e.getMessage());
				}
			}
		};
		thread.start();
	}

	/**
	 * Decline incoming session.
	 */
	private void declineIncomingSession() {
		Thread thread = new Thread() {
			public void run() {
				try {
					// Reject the invitation
					ipCallSession.removeSessionListener(sessionEventListener);
					ipCallSession.rejectSession();
					ipCallSession = null;

					audioCallState = IDLE;

					// Exit activity
					exitActivityIfNoSession(null);
				} catch (final Exception e) {
					exitActivityOnError(getString(R.string.label_invitation_failed)
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

		if (ipCallSession != null) {
			try {
				ipCallSession.removeSessionListener(sessionEventListener);
				ipCallSession.cancelSession();
			} catch (Exception e) {
				e.printStackTrace();
			}

			ipCallSession = null;
			sessionId = null;		
		}
	}

	
	
	/**
	 * Add video to Audio session
	 */
	private void addVideo() {
		if (logger.isActivated()) {
			logger.debug("addVideo()");
		}

		if (ipCallSession != null) {
			videoCallState = CONNECTING;			
			try {				
				ipCallSession.addVideo(outgoingVideoPlayer,incomingVideoRenderer);
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
		
		if (ipCallSession != null) {
			videoCallState = IDLE;
			try {
				ipCallSession.removeVideo();
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
			logger.debug("IPCallSessionActivity.this :"+IPCallSessionActivity.this);
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
			ipCallSession.setVideoPlayer(outgoingVideoPlayer);
			ipCallSession.setVideoRenderer(incomingVideoRenderer);
			ipCallSession.acceptAddVideo();
			videoCallState = CONNECTED;
			addVideoBtn.setText(R.string.label_remove_video_btn);
			addVideoBtn.setEnabled(true);
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
			ipCallSession.rejectAddVideo();
			videoCallState = IDLE;
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
	private void exitActivityIfNoSession(String message) {
		if (logger.isActivated()) {
			logger.info("exitActivityIfNoSession()");
		}
		// Disconnect ip call API
		hideProgressDialog();
		callApi.removeApiEventListener(this);
		callApi.disconnectApi();
		isCallApiConnected = false;
		audioCallState = IDLE;
		videoCallState = IDLE;
		sessionId = null;

		if (ipCallSession == null) {
			if (message == null) {
				this.finish();
			} else {
				Utils.showMessageAndExit(IPCallSessionActivity.this, message);
			}
		} else {
			if (message != null) {
				Utils.showMessageAndExit(IPCallSessionActivity.this, message);
			} else {
				this.finish();
			}
		}
	}

	private void exitActivityOnError(final String errorMsg) {
		if (logger.isActivated()) {
			logger.info("exitActivityOnError()");
		}
		handler.post(new Runnable() {
			public void run() {
				hideProgressDialog();
				audioCallState = DISCONNECTED;
				videoCallState = DISCONNECTED;								
				AlertDialog alert =Utils.showMessage(IPCallSessionActivity.this,
							errorMsg);				
				alert.setButton("OK",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								IPCallSessionActivity.this.exitActivityIfNoSession(null);
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

	/**
	 * Add IP call notification
	 * 
	 * @param context
	 *            Context
	 * @param Intent
	 *            invitation
	 */
	public static void addIPCallInvitationNotification(Context context,
			Intent invitation) {
		if (logger.isActivated()) {
			logger.debug("API, connected");
		}
		// Initialize settings
		RcsSettings.createInstance(context);

		// Create notification
		Intent intent = new Intent(invitation);
		intent.setClass(context, IPCallSessionActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction("incoming");
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
		String notifTitle = context.getString(R.string.title_recv_ipcall_audio);

		Notification notif = new Notification(R.drawable.ri_notif_csh_icon,
				notifTitle, System.currentTimeMillis());
		notif.setLatestEventInfo(
				context,
				notifTitle,
				context.getString(R.string.label_from) + " "
						+ Utils.formatCallerId(invitation), contentIntent);

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
		String sessionId = invitation.getStringExtra("sessionId");
		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(Utils.NOTIF_ID_IPCALL);
		notificationManager.notify(sessionId, Utils.NOTIF_ID_IPCALL, notif);
	}

	/**
	 * Remove IP call notification
	 * 
	 * @param context
	 *            Context
	 * @param sessionId
	 *            Session ID
	 */
	public static void removeIPCallNotification(Context context,
			String sessionId) {
		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(sessionId, Utils.NOTIF_ID_IPCALL);
	}

}
