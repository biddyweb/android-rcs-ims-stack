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

package com.orangelabs.rcs.ri.capabilities;

import java.util.List;

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
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import com.orangelabs.rcs.ri.R;
import com.orangelabs.rcs.ri.utils.Utils;
import com.orangelabs.rcs.service.api.client.capability.Capabilities;
import com.orangelabs.rcs.service.api.client.capability.CapabilityApi;
import com.orangelabs.rcs.service.api.client.capability.CapabilityApiIntents;
import com.orangelabs.rcs.utils.PhoneUtils;

/**
 * Anonymous fetch contacts capabilities of a given contact
 * 
 * @author jexa7410
 */
public class CapabilitiesDiscovery extends Activity {
	/**
	 * UI handler
	 */
	private final Handler handler = new Handler();
	
    /**
	 * Capability API
	 */
    private CapabilityApi capabilityApi;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set layout
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.capabilities_request);
        
        // Set title
        setTitle(R.string.menu_capabilities);
        
        // Set the contact selector
        Spinner spinner = (Spinner)findViewById(R.id.contact);
        spinner.setAdapter(Utils.createContactListAdapter(this));
        spinner.setOnItemSelectedListener(listenerContact);
        
    	// Register intent receiver to receive the query result
        IntentFilter filter = new IntentFilter(CapabilityApiIntents.CONTACT_CAPABILITIES);
		registerReceiver(capabilitiesIntentReceiver, filter, null, handler);

		// Set button callback
        Button refreshBtn = (Button)findViewById(R.id.refresh_btn);
        refreshBtn.setOnClickListener(btnRefreshListener);
        
        // Disable button if no contact available
        if (spinner.getAdapter().getCount() == 0) {
            refreshBtn.setEnabled(false);
        }
        
        // Instanciate capability API
        capabilityApi = new CapabilityApi(getApplicationContext());
        capabilityApi.connectApi();
    }

    @Override
    public void onDestroy() {
    	super.onDestroy();
    	
    	// Unregister intent receiver
		unregisterReceiver(capabilitiesIntentReceiver);

		// Disconnect contacts API
    	capabilityApi.disconnectApi();
    }
    
    /**
     * Spinner contact listener
     */
    private OnItemSelectedListener listenerContact = new OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			String contact = getContactAtPosition(position);
			
			// Get current capabilities
			Capabilities currentCapabilities = capabilityApi.getContactCapabilities(contact);

			// Display default capabilities
	        displayCapabilities(currentCapabilities);
			
			// Update capabilities
			updateCapabilities(contact);
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
    		// Update capabilities
    		updateCapabilities(getSelectedContact());
        }
    };

    /**
     * Update capabilities of a given contact
     * 
     * @param contact Contact
     */
    private void updateCapabilities(final String contact) {
        // Display info
        Utils.displayLongToast(CapabilitiesDiscovery.this, getString(R.string.label_request_in_background, contact));

        Thread t = new Thread() {
    		public void run() {
		    	try {
			        // Request new capabilities 
			        capabilityApi.requestCapabilities(contact);
		    	} catch(Exception e) {
		    		// Display error
					handler.post(new Runnable(){
						public void run(){
					    	Utils.showMessage(CapabilitiesDiscovery.this, getString(R.string.label_request_ko));
						}
					});
		    	}
    		}
    	};
    	t.start();
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
    	CheckBox presenceDiscovery = (CheckBox)findViewById(R.id.presence_discovery);
    	CheckBox socialPresence = (CheckBox)findViewById(R.id.social_presence);
    	TextView extensions = (TextView)findViewById(R.id.extensions);
    	
    	if (capabilities != null) {
    		// Set timestamp
    		lastCapabilitiesModified.setText(Utils.formatDateToString(capabilities.getTimestamp()));

    		// Set capabilities
    		imageCSh.setVisibility(View.VISIBLE);
    		videoCSh.setVisibility(View.VISIBLE);
    		ft.setVisibility(View.VISIBLE);
    		im.setVisibility(View.VISIBLE);
    		csVideo.setVisibility(View.VISIBLE);
    		presenceDiscovery.setVisibility(View.VISIBLE);
    		socialPresence.setVisibility(View.VISIBLE);
    		imageCSh.setChecked(capabilities.isImageSharingSupported());
    		videoCSh.setChecked(capabilities.isVideoSharingSupported());
    		ft.setChecked(capabilities.isFileTransferSupported());
    		im.setChecked(capabilities.isImSessionSupported());
    		csVideo.setChecked(capabilities.isCsVideoSupported());
    		presenceDiscovery.setChecked(capabilities.isPresenceDiscoverySupported());
    		socialPresence.setChecked(capabilities.isSocialPresenceSupported());
    		
            // Set extensions
    		extensions.setVisibility(View.VISIBLE);
            StringBuffer stringBuff = new StringBuffer();
            List<String> extensionList = capabilities.getSupportedExtensions();
            for(int i=0;i<extensionList.size();i++) {
            	stringBuff.append(extensionList.get(i) + "\n");
            }
            extensions.setText(stringBuff.toString());    		
    	} else {
    		lastCapabilitiesModified.setText("");
    		imageCSh.setVisibility(View.GONE);
    		videoCSh.setVisibility(View.GONE);
    		ft.setVisibility(View.GONE);
    		im.setVisibility(View.GONE);
    		csVideo.setVisibility(View.GONE);
    		presenceDiscovery.setVisibility(View.GONE);
    		socialPresence.setVisibility(View.GONE);
    		extensions.setVisibility(View.GONE);
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
					Capabilities capabilities = intent.getParcelableExtra("capabilities");
					
					// Check if this intent concerns the current selected contact					
					if (PhoneUtils.compareNumbers(getSelectedContact(), intent.getStringExtra("contact"))){
						// Update UI
						displayCapabilities(capabilities);
					}
				}
			});
		}
    };    
}
