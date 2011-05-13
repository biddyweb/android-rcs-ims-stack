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
package com.orangelabs.rcs.ri.presence;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

import com.orangelabs.rcs.core.ims.service.capability.Capabilities;
import com.orangelabs.rcs.ri.R;
import com.orangelabs.rcs.ri.utils.Utils;
import com.orangelabs.rcs.service.api.client.presence.PresenceApi;
import com.orangelabs.rcs.service.api.client.presence.PresenceApiIntents;

/**
 * Anonymous fetch presence capabilities of a given contact
 * 
 * @author jexa7410
 */
public class AnonymousFetchPresenceCapabilities extends Activity {
	/**
	 * UI handler
	 */
	private final Handler handler = new Handler();
	
	/**
	 * Presence API
	 */
    private PresenceApi presenceApi;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set layout
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.presence_anonymous_fetch);
        
        // Set title
        setTitle(R.string.menu_anonymous_fetch);
        
        // Set the contact selector
        Spinner spinner = (Spinner)findViewById(R.id.contact);
        spinner.setAdapter(Utils.createContactListAdapter(this));
        spinner.setOnItemSelectedListener(listenerContact);
        
        // Instanciate presence API
        presenceApi = new PresenceApi(getApplicationContext());
        presenceApi.connectApi();
        
    	// Register intent receiver to receive the query result
        IntentFilter filter = new IntentFilter(PresenceApiIntents.CONTACT_CAPABILITIES);
		registerReceiver(capabilitiesIntentReceiver, filter, null, handler);

		// Set button callback
        Button refreshBtn = (Button)findViewById(R.id.refresh_btn);
        refreshBtn.setOnClickListener(btnRefreshListener);
        
        // Disable button if no contact available
        if (spinner.getAdapter().getCount() == 0) {
            refreshBtn.setEnabled(false);
        }
    }

    @Override
    public void onDestroy() {
    	super.onDestroy();
    	
    	// Unregister intent receiver
		unregisterReceiver(capabilitiesIntentReceiver);

		// Disconnect presence API
    	presenceApi.disconnectApi();
    }
    
    /**
     * Spinner contact listener
     */
    private OnItemSelectedListener listenerContact = new OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			// Read capabilities
			readCapabilities(getContactAtPosition(position));
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
		}
	};
	
    /**
     * Returns the contact at given position
     * 
     * @param position Position in the adapter
     * @return Contact
     */
    private String getContactAtPosition(int position) {
	    Spinner spinner = (Spinner)findViewById(R.id.contact);
	    MatrixCursor cursor = (MatrixCursor)spinner.getItemAtPosition(position);
	    return cursor.getString(1);
    }
    
    /**
     * Returns the selected contact
     * 
     * @param position Position in the adapter
     * @return Contact
     */
    private String getSelectedContact() {
	    Spinner spinner = (Spinner)findViewById(R.id.contact);
	    MatrixCursor cursor = (MatrixCursor)spinner.getSelectedItem();
	    return cursor.getString(1);
    }

    /**
     * Request button callback
     */
    private OnClickListener btnRefreshListener = new OnClickListener() {
        public void onClick(View v) {
    		// Read capabilities
    		readCapabilities(getSelectedContact());
        }
    };

    /**
     * Read capabilities of a given contact
     * 
     * @param contact Contact
     */
    private void readCapabilities(String contact) {
    	try {
	        // Read capabilities 
	        Capabilities capabilities = presenceApi.requestCapabilities(contact);

	        // Display info
	        Utils.displayLongToast(AnonymousFetchPresenceCapabilities.this, getString(R.string.label_request_in_background));

	        // Display capabilities
	        displayCapabilities(capabilities);
    	} catch(Exception e) {
    		// Display error
	    	Utils.showMessage(AnonymousFetchPresenceCapabilities.this, getString(R.string.label_request_ko));
    	}
    }
    
    /**
     * Display the capabilities
     * 
     * @param capabilities Capabilities
     */
    private void displayCapabilities(Capabilities capabilities) {
    	TextView lastCapabilitiesModified = (TextView)findViewById(R.id.last_capabilities_modified);
    	CheckBox imageCSh = (CheckBox)findViewById(R.id.image_sharing);
    	CheckBox videoCSh = (CheckBox)findViewById(R.id.video_sharing);
    	CheckBox ft = (CheckBox)findViewById(R.id.file_transfer);
    	CheckBox im = (CheckBox)findViewById(R.id.im);
    	CheckBox csVideo = (CheckBox)findViewById(R.id.cs_video);
    	
    	if (capabilities != null) {
    		lastCapabilitiesModified.setText(Utils.formatDateToString(capabilities.getTimestamp()));

    		imageCSh.setVisibility(View.VISIBLE);
    		imageCSh.setChecked(capabilities.isImageSharingSupported());

    		videoCSh.setVisibility(View.VISIBLE);
    		videoCSh.setChecked(capabilities.isVideoSharingSupported());

    		ft.setVisibility(View.VISIBLE);
    		ft.setChecked(capabilities.isFileTransferSupported());

    		im.setVisibility(View.VISIBLE);
    		im.setChecked(capabilities.isImSessionSupported());

    		csVideo.setVisibility(View.VISIBLE);
    		csVideo.setChecked(capabilities.isCsVideoSupported());
    	} else {
    		lastCapabilitiesModified.setText("");
    		imageCSh.setVisibility(View.GONE);
    		videoCSh.setVisibility(View.GONE);
    		ft.setVisibility(View.GONE);
    		im.setVisibility(View.GONE);
    		csVideo.setVisibility(View.GONE);
    	}
    }
    
    /**
     * Intent receiver for capabilities result
     */
    private BroadcastReceiver capabilitiesIntentReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, final Intent intent) {
			handler.post(new Runnable(){
				public void run(){
					// Read capabilities from intent
					Capabilities capabilities = new Capabilities(); 
					long timestamp = intent.getLongExtra("timestamp", 0L);
					capabilities.setTimestamp(timestamp);
					boolean cs = intent.getBooleanExtra(Capabilities.CS_VIDEO_CAPABILITY, false);
					capabilities.setCsVideoSupport(cs);
					boolean image = intent.getBooleanExtra(Capabilities.IMAGE_SHARING_CAPABILITY, false);
					capabilities.setImageSharingSupport(image);
					boolean video = intent.getBooleanExtra(Capabilities.VIDEO_SHARING_CAPABILITY, false);
					capabilities.setVideoSharingSupport(video);
					boolean ft = intent.getBooleanExtra(Capabilities.FILE_SHARING_CAPABILITY, false);
					capabilities.setFileTransferSupport(ft);
					boolean im = intent.getBooleanExtra(Capabilities.IM_SESSION_CAPABILITY, false);
					capabilities.setImSessionSupport(im);
					
					// Update UI
					displayCapabilities(capabilities);
				}
			});
		}
    };    
}
