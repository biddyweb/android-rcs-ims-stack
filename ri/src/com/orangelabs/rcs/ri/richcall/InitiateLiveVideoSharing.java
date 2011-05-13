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
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.MatrixCursor;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Spinner;
import com.orangelabs.rcs.core.ims.service.sharing.ContentSharingError;
import com.orangelabs.rcs.ri.R;
import com.orangelabs.rcs.ri.utils.CpuMonitor;
import com.orangelabs.rcs.ri.utils.Utils;
import com.orangelabs.rcs.service.api.client.media.video.LiveVideoPlayer;
import com.orangelabs.rcs.service.api.client.media.video.VideoSurfaceView;
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
     * Outgoing video sharing session
     */
    private IVideoSharingSession outgoingVideoSession = null;

	/**
	 * Video player
	 */
	private LiveVideoPlayer player = null;

	/**
     * Outgoing video preview
     */
	private VideoSurfaceView outgoingVideoView = null;

	/**
	 * Camera preview started flag
	 */
	private boolean cameraPreviewRunning = false;

	/**
	 * Outgoing video surface holder
	 */
	private SurfaceHolder outgoingVideoSurface;

	/**
	 * Camera
	 */
    private Camera camera = null;

    /**
     * Progress dialog
     */
    private Dialog progressDialog = null;

    /**
     * CPU monitoring
     */
    private CpuMonitor cpu = new CpuMonitor();

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
        outgoingVideoView = (VideoSurfaceView)findViewById(R.id.video_preview);
        outgoingVideoView.setAspectRatio(176, 144);
        outgoingVideoSurface = outgoingVideoView.getHolder();
        outgoingVideoSurface.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        outgoingVideoSurface.addCallback(this);

        // Create the live video player
        player = new LiveVideoPlayer("h263-2000");

        // Instanciate rich call API
		callApi = new RichCallApi(getApplicationContext());
		callApi.connectApi();

		// Start CPU monitoring
		cpu.start();
    }

    @Override
    public void onDestroy() {
    	super.onDestroy();

		// Stop CPU monitoring
		cpu.stop();

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
            MatrixCursor cursor = (MatrixCursor)spinner.getSelectedItem();
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
            MatrixCursor cursor = (MatrixCursor)spinner.getSelectedItem();
            final String remote = cursor.getString(1);

            Thread thread = new Thread() {
            	public void run() {
                	try {
                        // Initiate sharing
                		outgoingVideoSession = callApi.initiateLiveVideoSharing(remote, player);
                		outgoingVideoSession.addSessionListener(outgoingSessionEventListener);
	            	} catch(Exception e) {
                        handler.post(new Runnable() {
	    					public void run() {
	    						Utils.showMessageAndExit(InitiateLiveVideoSharing.this, getString(R.string.label_invitation_failed));
	    					}
	    				});
	            	}
            	}
            };
            thread.start();

            // Display a progress dialog
            progressDialog = Utils.showProgressDialog(InitiateLiveVideoSharing.this,
                    getString(R.string.label_command_in_progress));

            // Hide buttons
            Button inviteBtn = (Button)findViewById(R.id.invite_btn);
        	inviteBtn.setVisibility(View.INVISIBLE);
            Button dialBtn = (Button)findViewById(R.id.dial_btn);
            dialBtn.setVisibility(View.INVISIBLE);
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

		if (cameraPreviewRunning) {
            cameraPreviewRunning = false;
            camera.stopPreview();
        }

		// Set camera orientation
        try {
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            int degrees = 0;
            switch (rotation) {
                case Surface.ROTATION_0:
                    degrees = 0;
                    break;
                case Surface.ROTATION_90:
                    degrees = 90;
                    break;
                case Surface.ROTATION_180:
                    degrees = 180;
                    break;
                case Surface.ROTATION_270:
                    degrees = 270;
                    break;
            }
            // 90 because we are in portrait mode
            int result = (90 - degrees + 360) % 360;
            camera.setDisplayOrientation(result);
        } catch (java.lang.NoSuchMethodError e) {
            // no rotation
		}

        Camera.Parameters p = camera.getParameters();
        p.setPreviewSize(176, 144);
        p.setPreviewFormat(PixelFormat.YCbCr_420_SP);
        // TODO: p.setPictureFormat(PixelFormat.YCbCr_420_SP); -> not supported by some devices
        // TODO: p.setPreviewFrameRate(8); -> not supported by some devices
        camera.setParameters(p);

        try {
        	camera.setPreviewDisplay(holder);
            camera.startPreview();
	        cameraPreviewRunning = true;
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
	 * Hide progress dialog
	 */
    public void hideProgressDialog() {
		if (progressDialog != null) {
			progressDialog.dismiss();
			progressDialog = null;
		}
    }

	/**
     * Outgoing video sharing session event listener
     */
    private IVideoSharingEventListener outgoingSessionEventListener = new IVideoSharingEventListener.Stub() {
		// Session is started
		public void handleSessionStarted() {
			// Hide progress dialog
			hideProgressDialog();
		}

		// Session has been aborted
		public void handleSessionAborted() {
            handler.post(new Runnable() {
				public void run() {
					// Hide progress dialog
					hideProgressDialog();

					// Display message
					Utils.showMessageAndExit(InitiateLiveVideoSharing.this, getString(R.string.label_sharing_aborted));
				}
			});
		}

		// Session has been terminated by remote
		public void handleSessionTerminatedByRemote() {
            handler.post(new Runnable() {
				public void run() {
					// Hide progress dialog
					hideProgressDialog();

					// Display message
					Utils.showMessageAndExit(InitiateLiveVideoSharing.this, getString(R.string.label_sharing_terminated_by_remote));
				}
			});
		}

		// Content sharing error
		public void handleSharingError(final int error) {
            handler.post(new Runnable() {
				public void run() {
					// Hide progress dialog
					hideProgressDialog();

					// Display error
					if (error == ContentSharingError.SESSION_INITIATION_DECLINED) {
    					Utils.showMessageAndExit(InitiateLiveVideoSharing.this, getString(R.string.label_invitation_declined));
					} else {
    					Utils.showMessageAndExit(InitiateLiveVideoSharing.this, getString(R.string.label_csh_failed, error));
					}
				}
			});
		}
    };

    /**
     * Quit the session
     */
    private void quitSession() {
		// Stop session
	    Thread thread = new Thread() {
	    	public void run() {
	        	try {
	                if (outgoingVideoSession != null) {
	                	try {
	                		outgoingVideoSession.removeSessionListener(outgoingSessionEventListener);
	                		outgoingVideoSession.cancelSession();
	                	} catch(Exception e) {
	                	}
	                	outgoingVideoSession = null;
	                }
	        	} catch(Exception e) {
	        	}
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
		inflater.inflate(R.menu.menu_outgoing_livevideo, menu);
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
