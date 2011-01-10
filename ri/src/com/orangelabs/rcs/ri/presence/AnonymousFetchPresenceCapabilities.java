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

import java.util.Vector;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.database.CursorWrapper;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Spinner;

import com.orangelabs.rcs.core.ims.service.capability.Capabilities;
import com.orangelabs.rcs.ri.R;
import com.orangelabs.rcs.ri.utils.Utils;
import com.orangelabs.rcs.service.api.client.presence.PresenceApi;
import com.orangelabs.rcs.service.api.client.presence.PresenceApiIntents;
import com.orangelabs.rcs.utils.PhoneUtils;

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

        // Set button callback
        Button requestBtn = (Button)findViewById(R.id.request_btn);
        requestBtn.setOnClickListener(btnRequestListener);
        
        // Disable button if no contact available
        if (spinner.getAdapter().getCount() == 0) {
            requestBtn.setEnabled(false);
        }

        // Instanciate presence API
        presenceApi = new PresenceApi(getApplicationContext());
        presenceApi.connectApi();
        
    	// Register intent receiver to receive the query result
        IntentFilter filter = new IntentFilter(PresenceApiIntents.CONTACT_CAPABILITIES);
		registerReceiver(capabilitiesIntentReceiver, filter, null, handler);
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
     * Request button callback
     */
    private OnClickListener btnRequestListener = new OnClickListener() {
        public void onClick(View v) {
        	try {
        		// Get the remote contact
                Spinner spinner = (Spinner)findViewById(R.id.contact);
                CursorWrapper cursor = (CursorWrapper)spinner.getSelectedItem();
                String remote = cursor.getString(1);
                
                // Send the message
                Capabilities capabilities = presenceApi.requestCapabilities(remote);
                // TODO: display capabilities already in the cache
                
                // Display info
                Utils.displayLongToast(AnonymousFetchPresenceCapabilities.this, getString(R.string.label_request_in_background));
        	} catch(Exception e) {
		    	Utils.showMessageAndExit(AnonymousFetchPresenceCapabilities.this, getString(R.string.label_request_ko));
        	}
        }
    };
    
    /**
     * Intent receiver for capabilities result
     */
    private BroadcastReceiver capabilitiesIntentReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, final Intent intent) {
			handler.post(new Runnable(){
				public void run(){
					Vector<String> list = new Vector<String>();
					boolean cs = intent.getBooleanExtra(Capabilities.CS_VIDEO_CAPABILITY, false);
					if (cs) {
						list.addElement(getString(R.string.label_capabilities_cs_video));
					}
					boolean image = intent.getBooleanExtra(Capabilities.IMAGE_SHARING_CAPABILITY, false);
					if (image) {
						list.addElement(getString(R.string.label_capabilities_image));
					}
					boolean video = intent.getBooleanExtra(Capabilities.VIDEO_SHARING_CAPABILITY, false);
					if (video) {
						list.addElement(getString(R.string.label_capabilities_video));
					}
					boolean ft = intent.getBooleanExtra(Capabilities.FILE_SHARING_CAPABILITY, false);
					if (ft) {
						list.addElement(getString(R.string.label_capabilities_ft));
					}
					boolean im = intent.getBooleanExtra(Capabilities.IM_SESSION_CAPABILITY, false);
					if (im) {
						list.addElement(getString(R.string.label_capabilities_im));
					}
					
					if (list.size() > 0) {
						CharSequence[] items = new CharSequence[list.size()];
						list.toArray(items);
						String contact = PhoneUtils.extractNumberFromUri(intent.getStringExtra("contact"));
					    Utils.showList(AnonymousFetchPresenceCapabilities.this,
					    		getString(R.string.label_capabilities) + " " + contact,
					    		 items);
					} else {
					    Utils.showMessage(AnonymousFetchPresenceCapabilities.this, getString(R.string.label_no_capability));
					}
				}
			});
		}
    };    
}
