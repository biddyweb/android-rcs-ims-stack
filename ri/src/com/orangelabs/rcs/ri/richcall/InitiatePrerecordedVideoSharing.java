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
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.orangelabs.rcs.core.ims.protocol.rtp.codec.video.h263.decoder.NativeH263Decoder;
import com.orangelabs.rcs.core.ims.service.sharing.ContentSharingError;
import com.orangelabs.rcs.platform.file.FileDescription;
import com.orangelabs.rcs.platform.file.FileFactory;
import com.orangelabs.rcs.ri.R;
import com.orangelabs.rcs.ri.utils.CpuMonitor;
import com.orangelabs.rcs.ri.utils.Utils;
import com.orangelabs.rcs.service.api.client.media.video.PrerecordedVideoPlayer;
import com.orangelabs.rcs.service.api.client.media.video.VideoPlayerEventListener;
import com.orangelabs.rcs.service.api.client.media.video.VideoSurfaceView;
import com.orangelabs.rcs.service.api.client.richcall.IVideoSharingEventListener;
import com.orangelabs.rcs.service.api.client.richcall.IVideoSharingSession;
import com.orangelabs.rcs.service.api.client.richcall.RichCallApi;

/**
 * Initiate pre-recorded video sharing
 *
 * @author jexa7410
 */
public class InitiatePrerecordedVideoSharing extends Activity {
	/**
	 * Activity result constants
	 */
	private final static int SELECT_VIDEO = 0;

	/**
	 * UI handler
	 */
	private final Handler handler = new Handler();

    /**
	 * Rich call API
	 */
    private RichCallApi callApi;

	/**
     * Outgoing video preview
     */
	private VideoSurfaceView outgoingVideoView;

	/**
	 * Video player
	 */
	private PrerecordedVideoPlayer player = null;

	/**
	 * Video filename
	 */
	private String filename;

    /**
     * Outgoing video sharing session
     */
    private IVideoSharingSession outgoingSession = null;

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
        setContentView(R.layout.richcall_initiate_prerecorded_video_sharing);

        // Set title
        setTitle(R.string.menu_initiate_livevideo_sharing);

        // Set the contact selector
        Spinner spinner = (Spinner)findViewById(R.id.contact);
        spinner.setAdapter(Utils.createContactListAdapter(this));

        // Set buttons callback
        Button inviteBtn = (Button)findViewById(R.id.invite_btn);
        inviteBtn.setOnClickListener(btnInviteListener);
        inviteBtn.setEnabled(false);
        Button dialBtn = (Button)findViewById(R.id.dial_btn);
        dialBtn.setOnClickListener(btnDialListener);
        Button selectBtn = (Button)findViewById(R.id.select_btn);
        selectBtn.setOnClickListener(btnSelectListener);

        // Disable button if no contact available
        if (spinner.getAdapter().getCount() == 0) {
        	dialBtn.setEnabled(false);
        	selectBtn.setEnabled(false);
        }

        // Set the video preview
        outgoingVideoView = (VideoSurfaceView)findViewById(R.id.video_prerecorded_preview);
        outgoingVideoView.setAspectRatio(176, 144);

        // Instanciate rich call API
		callApi = new RichCallApi(getApplicationContext());
		callApi.connectApi();

		// Start CPU monitoring
		cpu.start();    }

    @Override
    public void onDestroy() {
    	super.onDestroy();

		// Stop CPU monitoring
		cpu.stop();

		// Remove session listener
        if (outgoingSession != null) {
        	try {
        		outgoingSession.removeSessionListener(outgoingSessionEventListener);
        		outgoingSession.cancelSession();
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
                		player = new PrerecordedVideoPlayer("h263-2000", filename, playerEventListener);
                		player.setVideoSurface(outgoingVideoView);
                		outgoingSession = callApi.initiateVideoSharing(remote, filename, player);
                		outgoingSession.addSessionListener(outgoingSessionEventListener);
	            	} catch(Exception e) {
	    				handler.post(new Runnable() { 
	    					public void run() {
	    						Utils.showMessageAndExit(InitiatePrerecordedVideoSharing.this, getString(R.string.label_invitation_failed));
	    					}
	    				});
	            	}
            	}
            };
            thread.start();

            // Display a progress dialog
            progressDialog = Utils.showProgressDialog(InitiatePrerecordedVideoSharing.this, getString(R.string.label_command_in_progress));

            // Hide buttons
            Button inviteBtn = (Button)findViewById(R.id.invite_btn);
        	inviteBtn.setVisibility(View.INVISIBLE);
            Button selectBtn = (Button)findViewById(R.id.select_btn);
            selectBtn.setVisibility(View.INVISIBLE);
            Button dialBtn = (Button)findViewById(R.id.dial_btn);
            dialBtn.setVisibility(View.INVISIBLE);
        }
    };

    /**
     * Is video format supported
     *
     * @param uri URI
     * @return Boolean
     */
    public boolean isVideoFormatSupported(Uri uri) {
    	boolean supported = true;
    	try {
        	// Get video filename
	    	Cursor cursor = getApplicationContext().getContentResolver().query(uri,
	    			new String[] {MediaStore.MediaColumns.DATA}, null, null, null);
	        cursor.moveToFirst();
			String filename = cursor.getString(0);
			cursor.close();

	    	// Get video properties
    		int result = NativeH263Decoder.InitParser(filename);
	    	if (result != 0) {
		    	String encoding = NativeH263Decoder.getVideoCoding();
		    	int width = NativeH263Decoder.getVideoWidth();
		    	int height = NativeH263Decoder.getVideoHeight();
		    	if (width != 176) {
		    		supported = false;
		    	}
		    	if (height != 144) {
		    		supported = false;
		    	}
		    	if (encoding.indexOf("263") == -1) {
		    		supported = false;
		    	}
	    	}

    		NativeH263Decoder.DeinitDecoder();
    	} catch(Exception e) {
    		supported = false;
    	}
    	return supported;
    }

    /**
     * Select video button listener
     */
    private OnClickListener btnSelectListener = new OnClickListener() {
        public void onClick(View v) {
        	// Select a video from the gallery
        	Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("video/*");
            Intent wrapperIntent = Intent.createChooser(intent, null);
            startActivityForResult(wrapperIntent, SELECT_VIDEO);
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
            case SELECT_VIDEO: {
            	if ((data != null) && (data.getData() != null)) {
            		// Get selected photo URI
            		Uri videoUri = data.getData();

            		// Check if the video format is supported or not
            		if (!isVideoFormatSupported(videoUri)) {
            			Utils.showMessage(InitiatePrerecordedVideoSharing.this, getString(R.string.label_video_format_not_supported));
            			return;
            		}

                    // Get image filename
            		Cursor cursor = getContentResolver().query(videoUri, new String[] {MediaStore.Video.VideoColumns.DATA}, null, null, null);
                    cursor.moveToFirst();
                    filename = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));
                    cursor.close();

                    // Display the selected filename attribute
                    TextView uriEdit = (TextView)findViewById(R.id.uri);
                    try {
                    	FileDescription desc = FileFactory.getFactory().getFileDescription(filename);
	                    uriEdit.setText(getString(R.string.label_selected_video) + " " + (desc.getSize()/1024) + " KB");
                    } catch(Exception e) {
	                    uriEdit.setText(getString(R.string.label_selected_video) + " " + filename);
                    }

                    // Enable invite button
                    Button inviteBtn = (Button)findViewById(R.id.invite_btn);
                	inviteBtn.setEnabled(true);
                }
	    	}
            break;
        }
    }

    /**
     * Player event listener
     */
    private VideoPlayerEventListener playerEventListener = new VideoPlayerEventListener() {
	    /**
	     * End of video stream event
	     */
		public void endOfStream() {
			handler.post(new Runnable() {
				public void run() {
					Utils.displayToast(InitiatePrerecordedVideoSharing.this, getString(R.string.label_end_of_media));
				}
			});
		}

	    /**
	     * Video stream progress event
	     *
	     * @param progress Progress
	     */
		public void updateDuration(long progress) {
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
					// Hide progress dialog
					hideProgressDialog();
				}
			});
		}

		// Session has been aborted
		public void handleSessionAborted() {
			handler.post(new Runnable() {
				public void run() {
					// Hide progress dialog
					hideProgressDialog();

					// Show message
					Utils.showMessageAndExit(InitiatePrerecordedVideoSharing.this, getString(R.string.label_sharing_aborted));
				}
			});
		}

		// Session has been terminated by remote
		public void handleSessionTerminatedByRemote() {
			handler.post(new Runnable() {
				public void run() {
					// Hide progress dialog
					hideProgressDialog();

					// Show message
					Utils.showMessageAndExit(InitiatePrerecordedVideoSharing.this, getString(R.string.label_sharing_terminated_by_remote));
				}
			});
		}

		// Content sharing error
		public void handleSharingError(final int error) {
			handler.post(new Runnable() {
				public void run() {
					// Hide progress dialog
					hideProgressDialog();

					// Show error
					if (error == ContentSharingError.SESSION_INITIATION_DECLINED) {
    					Utils.showMessageAndExit(InitiatePrerecordedVideoSharing.this, getString(R.string.label_invitation_declined));
					} else {
    					Utils.showMessageAndExit(InitiatePrerecordedVideoSharing.this, getString(R.string.label_csh_failed, error));
					}
				}
			});
		}
    };

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
     * Quit the session
     */
    private void quitSession() {
		// Stop sessions
	    Thread thread = new Thread() {
	    	public void run() {
	        	try {
	                if (outgoingSession != null) {
	                	try {
	                		outgoingSession.removeSessionListener(outgoingSessionEventListener);
	                		outgoingSession.cancelSession();
	                	} catch(Exception e) {
	                	}
	                	outgoingSession = null;
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
		inflater.inflate(R.menu.menu_outgoing_prerecordedvideo, menu);
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
