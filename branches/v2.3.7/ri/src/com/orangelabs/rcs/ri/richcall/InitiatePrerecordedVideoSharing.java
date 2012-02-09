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

import com.orangelabs.rcs.core.ims.protocol.rtp.codec.video.h263.decoder.NativeH263Decoder;
import com.orangelabs.rcs.platform.file.FileDescription;
import com.orangelabs.rcs.platform.file.FileFactory;
import com.orangelabs.rcs.ri.R;
import com.orangelabs.rcs.ri.utils.Utils;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

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
	 * Video filename
	 */
	private String filename;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set layout
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.richcall_initiate_prerecorded_video_sharing);

        // Set title
        setTitle(R.string.menu_initiate_prerecorded_video_sharing);

        // Set the contact selector
        Spinner spinner = (Spinner)findViewById(R.id.contact);
        spinner.setAdapter(Utils.createRcsContactListAdapter(this));

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
	}

    @Override
    public void onDestroy() {
    	super.onDestroy();
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
                    Intent intent = new Intent(getApplicationContext(), VisioSharing.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("contact", remote);
                    intent.putExtra("incoming", false);
                    intent.putExtra("filename", filename);
                    getApplicationContext().startActivity(intent);
            	}
            };
            thread.start();
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
}
