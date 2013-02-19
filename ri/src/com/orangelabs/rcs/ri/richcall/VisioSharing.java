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

package com.orangelabs.rcs.ri.richcall;

import java.lang.reflect.Method;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.orangelabs.rcs.core.ims.protocol.rtp.codec.video.h264.H264Config;
import com.orangelabs.rcs.core.ims.service.richcall.ContentSharingError;
import com.orangelabs.rcs.provider.settings.RcsSettings;
import com.orangelabs.rcs.ri.R;
import com.orangelabs.rcs.ri.utils.CpuMonitor;
import com.orangelabs.rcs.ri.utils.Utils;
import com.orangelabs.rcs.service.api.client.ClientApiListener;
import com.orangelabs.rcs.service.api.client.ImsEventListener;
import com.orangelabs.rcs.service.api.client.media.MediaCodec;
import com.orangelabs.rcs.service.api.client.media.video.LiveVideoPlayer;
import com.orangelabs.rcs.service.api.client.media.video.PrerecordedVideoPlayer;
import com.orangelabs.rcs.service.api.client.media.video.VideoPlayerEventListener;
import com.orangelabs.rcs.service.api.client.media.video.VideoRenderer;
import com.orangelabs.rcs.service.api.client.media.video.VideoSurfaceView;
import com.orangelabs.rcs.service.api.client.richcall.IVideoSharingEventListener;
import com.orangelabs.rcs.service.api.client.richcall.IVideoSharingSession;
import com.orangelabs.rcs.service.api.client.richcall.RichCallApi;
import com.orangelabs.rcs.service.api.client.richcall.RichCallApiIntents;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Visio sharing activity - two half duplex live video sharing
 *
 * @author hlxn7157
 */
public class VisioSharing extends Activity implements SurfaceHolder.Callback, ClientApiListener, ImsEventListener {

	/**
	 * UI handler
	 */
	private final Handler handler = new Handler();

	/**
	 * Rich call API
	 */
	private RichCallApi callApi = null;

    /**
     * Rich call API connected
     */
	private boolean isCallApiConnected = false;

	/**
	 * Outgoing video sharing session
	 */
	private IVideoSharingSession outgoingCshSession = null;

	/**
	 * Video player
	 */
	private LiveVideoPlayer outgoingPlayer;

	/**
	 * Prerecorded video player
	 */
	private PrerecordedVideoPlayer outgoingPrerecordedPlayer;

	/**
	 * Name of the file to be played
	 */
	private String filename = null;

	/**
	 * Flag indicating if the outgoing session is prerecorded or live
	 */
	private boolean isPrerecordedSession = false;

	/**
	 * Video preview
	 */
	private VideoSurfaceView outgoingVideoView = null;

	/**
	 * Video surface holder
	 */
	private SurfaceHolder surface;

	/**
	 * Camera
	 */
	private Camera camera = null;

	/**
	 * Camera preview started flag
	 */
	private boolean cameraPreviewRunning = false;

	/**
	 * Outgoing first launch
	 */
	private Boolean fisrtLaunchDone = false;

    /**
     * Progress dialog
     */
	private Dialog outgoingProgressDialog = null;

	/**
	 * CPU monitoring
	 */
	private CpuMonitor cpu = new CpuMonitor();

	/**
	 * Incoming video sharing session
	 */
	private IVideoSharingSession incomingCshSession = null;

	/**
	 * Incoming session ID
	 */
	private String incomingSessionId = null;

	/**
	 * RemoteContact
	 */
	private String remoteContact;

	/**
	 * Video renderer
	 */
	private VideoRenderer incomingRenderer = null;

	/**
	 * Video preview
	 */
	private VideoSurfaceView incomingVideoView = null;

	/**
	 * Direction
	 */
	private Boolean isIncoming;

	/**
	 * Wait API connected to do getIncomingSession
	 */
	private Boolean getIncomingSessionWhenApiConnected = false;

	/**
	 * Wait API connected to do startOutgoingSession
	 */
	private Boolean startOutgoingSessionWhenApiConnected = false;

	/**
	 * Switch camera button
	 */
	private Button switchCamBtn = null;

	/**
	 * Opened camera id
	 */
	private int openedCameraId = 0;

	/**
	 * Surface holder for video preview
	 */
	private SurfaceHolder video_holder = null;

	/**
	 * Number of cameras
	 */
	private int cam_num = 1;

    /**
     * Start outgoing button
     */
	private Button startOutgoingBtn = null;

	/**
	 * Stop outgoing button
	 */
	private Button stopOutgoingBtn = null;

	/**
	 * Stop incoming button
	 */
	private Button stopIncomingBtn = null;

	/**
	 * Video width
	 */
	private int videoWidth;

	/**
	 * Video height
	 */
	private int videoHeight;

    /**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set layout
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.richcall_visio_sharing);

        // Set title
        setTitle(R.string.title_video_sharing);

        // Check if Prerecorded
        filename = getIntent().getStringExtra("filename");
        isPrerecordedSession = (filename!=null);

        // Video size
        videoWidth = getIntent().getIntExtra("videowidth", 0);
        videoHeight = getIntent().getIntExtra("videoheight", 0);
        if (videoHeight == 0 || videoWidth == 0) {
            videoWidth = H264Config.VIDEO_WIDTH;
            videoHeight = H264Config.VIDEO_HEIGHT;
        }

        // Texts and buttons
        if (switchCamBtn == null && !isPrerecordedSession) {
            switchCamBtn = (Button)findViewById(R.id.switch_cam_btn);
            Method method = getCameraNumberOfCamerasMethod();
            if (method != null) {
                try {
                    Integer ret = (Integer)method.invoke(null, (Object[])null);
                    cam_num = ret.intValue();
                } catch (Exception e) {
                    cam_num = 1;
                }
            } else {
                cam_num = 1;
            }

            if (cam_num > 1) {
                switchCamBtn.setOnClickListener(btnSwitchCamListener);
                switchCamBtn.setEnabled(true);
            } else {
                switchCamBtn.setVisibility(View.INVISIBLE);
            }
        }

        if (isPrerecordedSession){
        	// Hide switch camera button
        	switchCamBtn = (Button)findViewById(R.id.switch_cam_btn);
        	switchCamBtn.setVisibility(View.GONE);
        }

        if (startOutgoingBtn == null) {
            startOutgoingBtn = (Button)findViewById(R.id.start_outgoing_btn);
            startOutgoingBtn.setOnClickListener(btnStartOutgoingListener);
            startOutgoingBtn.setEnabled(true);
        }
        if (stopOutgoingBtn == null) {
            stopOutgoingBtn = (Button)findViewById(R.id.stop_outgoing_btn);
            stopOutgoingBtn.setOnClickListener(btnStopOutgoingListener);
            stopOutgoingBtn.setEnabled(false);
        }
        if (stopIncomingBtn == null) {
            stopIncomingBtn = (Button)findViewById(R.id.stop_incoming_btn);
            stopIncomingBtn.setOnClickListener(btnStopIncomingListener);
            stopIncomingBtn.setEnabled(false);
        }

        // Set the video preview
        if (outgoingVideoView == null) {
            outgoingVideoView = (VideoSurfaceView)findViewById(R.id.outgoing_video_preview);
        }
        if (!isPrerecordedSession){
        	// Create the live video player
            if (VideoSettings.isCodecsManagedByStack(getApplicationContext())) {
                outgoingPlayer = new LiveVideoPlayer();
            } else {
                outgoingPlayer = new LiveVideoPlayer(createSupportedCodecList(VideoSettings.getCodecsList(getApplicationContext())));
            }

            outgoingVideoView.setAspectRatio(videoWidth, videoHeight);
            surface = outgoingVideoView.getHolder();
            surface.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            surface.addCallback(this);
        }else{
        	// Create the prerecorded video player
        	outgoingPrerecordedPlayer = new PrerecordedVideoPlayer("h263-2000", filename, playerEventListener);
        	outgoingPrerecordedPlayer.setVideoSurface(outgoingVideoView);
        	// Force ratio for pre-recorded video
        	outgoingVideoView.setAspectRatio(176, 144);
        }

        // Set incoming video preview
        if (incomingVideoView == null) {
            incomingVideoView = (VideoSurfaceView)findViewById(R.id.incoming_video_view);
            incomingVideoView.setAspectRatio(videoWidth, videoHeight);
            if (VideoSettings.isCodecsManagedByStack(getApplicationContext())) {
                incomingRenderer = new VideoRenderer();
            } else {
                incomingRenderer = new VideoRenderer(createSupportedCodecList(VideoSettings.getCodecsList(getApplicationContext())));
            }
            incomingRenderer.setVideoSurface(incomingVideoView);
        }

        // Instantiate rich call API
        if (callApi == null) {
            callApi = new RichCallApi(getApplicationContext());
            callApi.addApiEventListener(this);
            callApi.addImsEventListener(this);
            callApi.connectApi();
        }

        // Start CPU monitoring
        cpu.start();
    }

    @Override
    public void onResume() {
        super.onResume();

        // Set broadcast receiver
        IntentFilter filter = new IntentFilter(RichCallApiIntents.VIDEO_SHARING_INVITATION);
        registerReceiver(sharingIntentReceiver, filter, null, handler);

        // Get invitation info
        String lastIncomingSessionId = incomingSessionId;
        incomingSessionId = getIntent().getStringExtra("sessionId");
        remoteContact = getIntent().getStringExtra("contact");
        isIncoming = getIntent().getBooleanExtra("incoming", false);
        if (incomingSessionId != null) {
            removeVideoSharingNotification(getApplicationContext(), incomingSessionId);
        }
    	TextView fromTxt = (TextView)findViewById(R.id.visio_with_txt);
        fromTxt.setText(getString(R.string.label_video_sharing_with, remoteContact));

        if ((isIncoming) && (lastIncomingSessionId != incomingSessionId)) {
            if (isCallApiConnected) {
                getIncomingSession();
            } else {
                getIncomingSessionWhenApiConnected = true;
            }
        }
        if ((!isIncoming) && (!fisrtLaunchDone)) {
            if (isCallApiConnected) {
                startOutgoingSession();
            } else {
                startOutgoingSessionWhenApiConnected = true;
            }
        }
        fisrtLaunchDone = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        
        // Unregister intent receiver
        try {
        	unregisterReceiver(sharingIntentReceiver);
        } catch (IllegalArgumentException e) {
        	// Nothing to do
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Stop CPU monitoring
        cpu.stop();

        // hide ProgressDialog
        hideProgressDialog();

        // Remove session listener
        if (incomingCshSession != null) {
            try {
                incomingCshSession.removeSessionListener(incomingSessionEventListener);
                incomingCshSession.cancelSession();
            } catch (Exception e) {
            }
        }
        if (outgoingCshSession != null) {
            try {
                outgoingCshSession.removeSessionListener(outgoingSessionEventListener);
                outgoingCshSession.cancelSession();
            } catch (Exception e) {
            }
        }

        // Release the camera
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
        }

        // Disconnect rich call API
        callApi.removeApiEventListener(this);
        callApi.disconnectApi();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                // Quit session
                Thread thread = new Thread() {
                    public void run() {
                        stopIncomingSession();
                        stopOutgoingSession();
                        exitActivityIfNoSession(null);
                    }
                };
                thread.start();
                return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    /**
     * API disabled
     */
    public void handleApiDisabled() {
        isCallApiConnected = false;

        handler.post(new Runnable() {
            public void run() {
                Utils.showMessageAndExit(VisioSharing.this, getString(R.string.label_api_disabled));
            }
        });
    }

    /**
     * API connected
     */
    public void handleApiConnected() {
        isCallApiConnected = true;
        if (getIncomingSessionWhenApiConnected) {
            getIncomingSession();
            getIncomingSessionWhenApiConnected = false;
        }
        if (startOutgoingSessionWhenApiConnected) {
            startOutgoingSession();
            startOutgoingSessionWhenApiConnected = false;
        }
    }

    /**
     * API disconnected
     */
    public void handleApiDisconnected() {
        isCallApiConnected = false;

        // Service has been disconnected
        handler.post(new Runnable() {
            public void run() {
                Utils.showMessageAndExit(VisioSharing.this,
                        getString(R.string.label_api_disconnected));
            }
        });
    }

    /**
     * Client is connected to the IMS
     */
    public void handleImsConnected() {
    }

    /**
     * Client is disconnected from the IMS
     * 
     * @param reason Disconnection reason
     */
    public void handleImsDisconnected(int reason) {
        // IMS has been disconnected
        handler.post(new Runnable() {
            public void run() {
                Utils.showMessageAndExit(VisioSharing.this,
                        getString(R.string.label_ims_disconnected));
            }
        });
    }

    /**
     * Surface has been changed
     *
     * @param holder Surface holder
     * @param format Format
     * @param w Width
     * @param h Height
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        video_holder = holder;
        if (camera != null) {
            if (cameraPreviewRunning) {
                cameraPreviewRunning = false;
                camera.stopPreview();
            }
        }
        if (!isPrerecordedSession){
        	startCameraPreview();
        }
    }

    /**
     * Surface has been created
     *
     * @param holder Surface holder
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (camera == null && !isPrerecordedSession) {
            // Start camera preview
            if (cam_num > 1) {
                OpenCamera(1);
            } else {
                camera = Camera.open();
                openedCameraId = 0;
            }
            camera.setPreviewCallback(outgoingPlayer);
        }
    }

    /**
     * Surface has been destroyed
     *
     * @param holder Surface holder
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    /**
     * Accept button listener
     */
    private OnClickListener acceptBtnListener = new OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            removeVideoSharingNotification(getApplicationContext(), incomingSessionId);
            acceptIncomingSession();
        }
    };

    /**
     * Reject button listener
     */
    private OnClickListener declineBtnListener = new OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            removeVideoSharingNotification(getApplicationContext(), incomingSessionId);
            declineIncomingSession();
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
            if (openedCameraId == 0) {
                OpenCamera(1);
            } else {
                OpenCamera(0);
            }

            // Restart the preview
            startCameraPreview();
            if (camera != null) {
                camera.setPreviewCallback(outgoingPlayer);
            }
        }
    };

    /**
     * Open a camera
     *
     * @param cameraId
     */
    private void OpenCamera(int cameraId) {
        Method method = getCameraOpenMethod();
        if (method != null) {
            try {
                camera = (Camera)method.invoke(camera, new Object[] {
                    cameraId
                });
                openedCameraId = cameraId;
            } catch (Exception e) {
                camera = Camera.open();
                openedCameraId = 0;
            }
        } else {
            camera = Camera.open();
            openedCameraId = 0;
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
     * Start outgoing session button listener
     */
    private View.OnClickListener btnStartOutgoingListener = new View.OnClickListener() {
        public void onClick(View v) {
            startOutgoingSession();
        }
    };

    /**
     * Stop outgoing session button listener
     */
    private View.OnClickListener btnStopOutgoingListener = new View.OnClickListener() {
        public void onClick(View v) {
            Thread thread = new Thread() {
                public void run() {
                    stopOutgoingSession();
                    exitActivityIfNoSession(null);

                    recreateVideoPlayer();
                }
            };
            thread.start();
            startOutgoingBtn.setEnabled(true);
            stopOutgoingBtn.setEnabled(false);
        }
    };

    /**
     * Recreate the video player
     */
    private void recreateVideoPlayer(){
    	if (!isPrerecordedSession) {
    		// Create the live video player
            if (VideoSettings.isCodecsManagedByStack(getApplicationContext())) {
                outgoingPlayer = new LiveVideoPlayer();
            } else {
                outgoingPlayer = new LiveVideoPlayer(createSupportedCodecList(VideoSettings.getCodecsList(getApplicationContext())));
            }
            outgoingVideoView.setAspectRatio(videoWidth, videoHeight);
            surface = outgoingVideoView.getHolder();
            surface.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            surface.addCallback(this);
    		camera.setPreviewCallback(outgoingPlayer);
        } else {
    		// Create the prerecorded video player
    		outgoingPrerecordedPlayer = new PrerecordedVideoPlayer("h263-2000", filename, playerEventListener);
    		outgoingPrerecordedPlayer.setVideoSurface(outgoingVideoView);
    	}
    }

    /**
     * Stop incoming session button listener
     */
    private View.OnClickListener btnStopIncomingListener = new View.OnClickListener() {
        public void onClick(View v) {
            Thread thread = new Thread() {
                public void run() {
                    stopIncomingSession();
                    exitActivityIfNoSession(null);
                }
            };
            thread.start();
            stopIncomingBtn.setEnabled(false);
        }
    };

    /**
     * Outgoing video sharing session event listener
     */
    private IVideoSharingEventListener outgoingSessionEventListener = new IVideoSharingEventListener.Stub() {
        // Session is started
        public void handleSessionStarted() {
            handler.post(new Runnable() {
                public void run() {
                    // Update Camera
                    videoHeight = outgoingPlayer.getMediaCodecHeight();
                    videoWidth = outgoingPlayer.getMediaCodecWidth();
                    reStartCameraPreview();

                    startOutgoingBtn.setEnabled(false);
                    stopOutgoingBtn.setEnabled(true);
                    hideProgressDialog();
                }
            });
        }

        // Session has been aborted
        public void handleSessionAborted(int reason) {
            handler.post(new Runnable() {
                public void run() {
                    startOutgoingBtn.setEnabled(true);
                    stopOutgoingBtn.setEnabled(false);
                    hideProgressDialog();
                    stopOutgoingSession();
                    exitActivityIfNoSession(getString(R.string.label_outgoing_sharing_aborted));
                    recreateVideoPlayer();
                }
            });
        }

        // Session has been terminated by remote
        public void handleSessionTerminatedByRemote() {
            handler.post(new Runnable() {
                public void run() {
                    startOutgoingBtn.setEnabled(true);
                    stopOutgoingBtn.setEnabled(false);
                    hideProgressDialog();
                    stopOutgoingSession();
                    exitActivityIfNoSession(getString(R.string.label_outgoing_sharing_terminated_by_remote));
                    recreateVideoPlayer();
                }
            });
        }

        // Content sharing error
        public void handleSharingError(final int error) {
            handler.post(new Runnable() {
                public void run() {
                    startOutgoingBtn.setEnabled(true);
                    stopOutgoingBtn.setEnabled(false);
                    hideProgressDialog();
                    stopOutgoingSession();
                    if (error == ContentSharingError.SESSION_INITIATION_DECLINED) {
                        exitActivityIfNoSession(getString(R.string.label_invitation_declined));
                    } else {
                        exitActivityIfNoSession(getString(R.string.label_csh_failed, error));
                    }
                    recreateVideoPlayer();
                }
            });
        }
    };

    /**
     * Incoming video sharing session event listener
     */
    private IVideoSharingEventListener incomingSessionEventListener = new IVideoSharingEventListener.Stub() {
        // Session is started
        public void handleSessionStarted() {
            handler.post(new Runnable() {
                public void run() {
                    stopIncomingBtn.setEnabled(true);
                }
            });
        }

        // Session has been aborted
        public void handleSessionAborted(int reason) {
            handler.post(new Runnable() {
                public void run() {
                    stopIncomingBtn.setEnabled(false);
                    stopIncomingSession();
                    exitActivityIfNoSession(getString(R.string.label_incoming_sharing_aborted));
                }
            });
        }

        // Session has been terminated by remote
        public void handleSessionTerminatedByRemote() {
            handler.post(new Runnable() {
                public void run() {
                    stopIncomingBtn.setEnabled(false);
                    stopIncomingSession();
                    exitActivityIfNoSession(getString(R.string.label_incoming_sharing_terminated_by_remote));
                }
            });
        }

        // Sharing error
        public void handleSharingError(final int error) {
            handler.post(new Runnable() {
                public void run() {
                    stopIncomingBtn.setEnabled(false);
                    stopIncomingSession();
                    exitActivityIfNoSession(getString(R.string.label_csh_failed, error));
                }
            });

        }
    };

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
     * Get incoming session.
     */
    private void getIncomingSession() {
        try {
            // Get the video sharing session
            incomingCshSession = callApi.getVideoSharingSession(incomingSessionId);
            incomingCshSession.addSessionListener(incomingSessionEventListener);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.title_recv_video_sharing);
            builder.setMessage(getString(R.string.label_from) + " " + remoteContact);
            builder.setCancelable(false);
            builder.setIcon(R.drawable.ri_notif_csh_icon);
            builder.setPositiveButton(getString(R.string.label_accept), acceptBtnListener);
            builder.setNegativeButton(getString(R.string.label_decline), declineBtnListener);
            builder.show();
        } catch (Exception e) {
            Utils.showMessageAndExit(VisioSharing.this, getString(R.string.label_api_failed));
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
                    incomingCshSession.setMediaRenderer(incomingRenderer);
                    incomingCshSession.acceptSession();
                } catch (Exception e) {
                    handler.post(new Runnable() {
    					public void run() {
    						Utils.showMessageAndExit(VisioSharing.this, getString(R.string.label_invitation_failed));
    					}
    				});
                }
            }
        };
        thread.start();

        stopIncomingBtn.setEnabled(true);
    }

    /**
     * Decline incoming session.
     */
    private void declineIncomingSession() {
        Thread thread = new Thread() {
            public void run() {
                try {
                    // Reject the invitation
                    incomingCshSession.removeSessionListener(incomingSessionEventListener);
                    incomingCshSession.rejectSession();
                    incomingCshSession = null;

                    // Exit activity
                    exitActivityIfNoSession(null);
                } catch (Exception e) {
                }
            }
        };
        thread.start();
    }

    /**
     * Exit activity if all sessions are stopped.
     *
     * @param message the message to display. Can be null for no message.
     */
    private void exitActivityIfNoSession(String message) {
        if ((outgoingCshSession == null) && (incomingCshSession == null)) {
            if (message == null) {
                finish();
            } else {
                Utils.showMessageAndExit(VisioSharing.this, message);
            }
        } else {
            if (message != null) {
                Utils.showMessage(VisioSharing.this, message);
            }
        }
    }

    /**
     * Stop the incoming session
     */
    private void stopIncomingSession() {
        // Stop sessions
        if (incomingCshSession != null) {
            try {
                incomingCshSession.removeSessionListener(incomingSessionEventListener);
                incomingCshSession.cancelSession();
            } catch (Exception e) {
                e.printStackTrace();
            }
            incomingCshSession = null;
        }
    }

    /**
     * Start the outgoing session
     */
    private void startOutgoingSession() {
        Thread thread = new Thread() {
            public void run() {
                try {
                    // Initiate sharing
                	if (!isPrerecordedSession) {
                        outgoingCshSession = callApi.initiateLiveVideoSharing(remoteContact, outgoingPlayer);
                	}else{
                		outgoingCshSession = callApi.initiateVideoSharing(remoteContact, filename, outgoingPrerecordedPlayer);
                	}
                    outgoingCshSession.addSessionListener(outgoingSessionEventListener);
                } catch (Exception e) {
                    handler.post(new Runnable() {
    					public void run() {
    						Utils.showMessageAndExit(VisioSharing.this, getString(R.string.label_invitation_failed));
    					}
    				});
                }
            }
        };
        thread.start();
        startOutgoingBtn.setEnabled(false);
        // Display a progress dialog
        outgoingProgressDialog = Utils.showProgressDialog(VisioSharing.this, getString(R.string.label_command_in_progress));
        outgoingProgressDialog.setOnCancelListener(new OnCancelListener() {
			public void onCancel(DialogInterface dialog) {
				Toast.makeText(VisioSharing.this, getString(R.string.label_video_sharing_canceled), Toast.LENGTH_SHORT).show();
				finish();
			}
		});
    }

    /**
     * Stop the outgoing session
     */
    private void stopOutgoingSession() {
        // Stop sessions
        if (outgoingCshSession != null) {
            try {
                outgoingCshSession.removeSessionListener(outgoingSessionEventListener);
                outgoingCshSession.cancelSession();
            } catch (Exception e) {
                e.printStackTrace();
            }
            outgoingCshSession = null;
        }
    }

    /**
     * Hide progress dialog
     */
    public void hideProgressDialog() {
        if (outgoingProgressDialog != null && outgoingProgressDialog.isShowing()) {
            outgoingProgressDialog.dismiss();
            outgoingProgressDialog = null;
        }
    }

    /**
     * Start the camera preview
     */
    private void reStartCameraPreview() {
        if (camera != null) {
            releaseCamera();
            outgoingVideoView.setAspectRatio(videoWidth, videoHeight);
            OpenCamera(openedCameraId);
        }
        startCameraPreview();
        if (camera != null) {
            camera.setPreviewCallback(outgoingPlayer);
        }
    }

    /**
     * Start the camera preview
     */
    private void startCameraPreview() {
        if (camera != null) {
            // Camera settings
            Camera.Parameters p = camera.getParameters();
            p.setPreviewFormat(PixelFormat.YCbCr_420_SP); //ImageFormat.NV21);

            // Camera size
            List<Camera.Size> sizes = p.getSupportedPreviewSizes();
            if (contains(sizes, videoWidth, videoHeight)) {
                p.setPreviewSize(videoWidth, videoHeight);
                outgoingPlayer.setScalingFactor((float) 1);
                if (logger.isActivated()) {
                    logger.info("Camera preview initialized with size " + videoWidth + "x" + videoHeight + " with a 1 scale factor");
                }
            } else {
                // Try to select double size and initialize scaling 0.5
                if (contains(sizes, 2*videoWidth, 2*videoHeight)) {
                    p.setPreviewSize(2*videoWidth, 2*videoHeight);
                    outgoingPlayer.setScalingFactor((float) 0.5);
                    if (logger.isActivated()) {
                        logger.info("Camera preview initialized with size " + 2*videoWidth + "x" + 2*videoHeight + " with a 0.5 scale factor");
                    }
                } else {
                    // Try to set front camera if back camera doesn't support size
                    String cam_id = p.get("camera-id");
                    if (cam_id != null) {
                        p.set("camera-id", 2);
                        p.setRotation(270);
                        p.setPreviewSize(videoWidth, videoHeight);
                        outgoingPlayer.setScalingFactor((float) 1);
                        if (logger.isActivated()) {
                            logger.info("Camera preview initialized on front camera with size " + videoWidth + "x" + videoHeight + " with a 1 scale factor");
                        }
                    } else {
                        // Error
                        if (logger.isActivated()) {
                            logger.warn("Camera preview can't be initialized with size " + videoWidth + "x" + videoHeight);
                        }
                        camera = null;
                        return;
                    }
                }
            }
            camera.setParameters(p);
            try {
                camera.setPreviewDisplay(video_holder);
                camera.startPreview();
                cameraPreviewRunning = true;
            } catch (Exception e) {
                camera = null;
            }
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
    private boolean contains(List<Camera.Size> list, int width, int height) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).width == width && list.get(i).height == height) {
                return true;
            }
        }
        return false;
    }

    private BroadcastReceiver sharingIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, final Intent intent) {
            handler.post(new Runnable() {
                public void run() {
                    // Get invitation info
                    String lastIncomingSessionId = incomingSessionId;
                    incomingSessionId = intent.getStringExtra("sessionId");
                    remoteContact = intent.getStringExtra("contact");
                    isIncoming = true;

                    if (incomingSessionId != null) {
                        removeVideoSharingNotification(getApplicationContext(), incomingSessionId);
                    }
                	TextView fromTxt = (TextView)findViewById(R.id.visio_with_txt);
                    fromTxt.setText(getString(R.string.label_video_sharing_with, remoteContact));
                    if (lastIncomingSessionId != incomingSessionId) {
                        if (isCallApiConnected) {
                            getIncomingSession();
                        } else {
                            getIncomingSessionWhenApiConnected = true;
                        }
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
    public static void addVideoSharingInvitationNotification(Context context, Intent invitation) {
		// Initialize settings
		RcsSettings.createInstance(context);

        // Create notification
        Intent intent = new Intent(invitation);
        intent.setClass(context, VisioSharing.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("incoming", true);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        String notifTitle = context.getString(R.string.title_recv_video_sharing);
        Notification notif = new Notification(R.drawable.ri_notif_csh_icon, notifTitle,
                System.currentTimeMillis());
        notif.flags = Notification.FLAG_AUTO_CANCEL;
        notif.setLatestEventInfo(context,
        		notifTitle,
        		context.getString(R.string.label_from) + " " + Utils.formatCallerId(invitation),
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
		String sessionId = invitation.getStringExtra("sessionId");
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(sessionId, Utils.NOTIF_ID_VIDEO_SHARE, notif);
    }

    /**
     * Remove video share notification
     *
     * @param context Context
     * @param sessionId Session ID
     */
    public static void removeVideoSharingNotification(Context context, String sessionId) {
        NotificationManager notificationManager = (NotificationManager)context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(sessionId, Utils.NOTIF_ID_VIDEO_SHARE);
    }

    /**
     * Player event listener for prerecorded sessions
     */
    private VideoPlayerEventListener playerEventListener = new VideoPlayerEventListener() {
	    /**
	     * End of video stream event
	     */
		public void endOfStream() {
			handler.post(new Runnable() {
				public void run() {
					Utils.displayToast(VisioSharing.this, getString(R.string.label_end_of_media));
                    startOutgoingBtn.setEnabled(true);
                    stopOutgoingBtn.setEnabled(false);
				}
			});
			recreateVideoPlayer();
		}

	    /**
	     * Video stream progress event
	     *
	     * @param progress Progress
	     */
		public void updateDuration(long progress) {
		}
    };
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater=new MenuInflater(getApplicationContext());
		inflater.inflate(R.menu.menu_video_sharing, menu);
		return true;
	}
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_close_session:
				// Quit sessions
				stopOutgoingSession();
                stopIncomingSession();
                exitActivityIfNoSession(null);
				break;
		}
		return true;
	}

    /**
     * Create a list of supported video codecs
     *
     * @return codecs list
     */
    private MediaCodec[] createSupportedCodecList(boolean[] codecs) {
        // Set number of codecs
        int size = 0;
        for (int i = 0; i < VideoSettings.CODECS_SIZE + 1; i++) {
            size += ((codecs[i]) ? 1 : 0);
        }
        if (size == 0) {
            return null;
        }

        // Add codecs settings (preferred in first)
        MediaCodec[] supportedMediaCodecs = new MediaCodec[size];
        for (int i = 0; i < VideoSettings.CODECS_SIZE; i++) {
            if (codecs[i]) {
                supportedMediaCodecs[--size] = VideoSettings.CODECS[i];
            }
        }
        if (codecs[VideoSettings.CODECS_SIZE]) {
            supportedMediaCodecs[--size] = VideoSettings.getCustomCodec(getApplicationContext());
        }
        return supportedMediaCodecs;
    }
}
