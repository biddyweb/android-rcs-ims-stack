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
package com.orangelabs.rcs.samples.presence;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.CursorWrapper;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Spinner;

import com.orangelabs.rcs.core.ims.service.capability.Capabilities;
import com.orangelabs.rcs.samples.R;
import com.orangelabs.rcs.samples.utils.Utils;
import com.orangelabs.rcs.service.api.client.presence.PresenceApi;
import com.orangelabs.rcs.service.api.client.presence.PresenceApiIntents;

/**
 * Request presence capabilities of a given contact
 * 
 * @author jexa7410
 */
public class RequestPresenceCapabilities extends Activity {
	/**
	 * UI handler
	 */
	private final Handler handler = new Handler();
	
	/**
	 * Presence API
	 */
    public static PresenceApi presenceApi;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.presence_anonymous_fetch);
        
        // Set UI title
        setTitle(R.string.menu_anonymous_fetch);
        
        // Set the contact selector
        Spinner spinner = (Spinner)findViewById(R.id.contact);
        spinner.setAdapter(Utils.createContactListAdapter(this));
        
        // Set button callback
        Button btn = (Button)findViewById(R.id.request);
        btn.setOnClickListener(btnRequestListener);
        
        // Instanciate presence API
        presenceApi = new PresenceApi(getApplicationContext());
        presenceApi.connectApi();
        
    	// Register intent reciver to receive the query result
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
                presenceApi.requestCapabilities(remote);
        	} catch(Exception e) {
		    	Utils.showError(RequestPresenceCapabilities.this, getString(R.string.label_request_ko));
        	}
        }
    };
    
    private BroadcastReceiver capabilitiesIntentReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, final Intent intent) {
			handler.post(new Runnable(){
				public void run(){
					String result = "";
					String contact = intent.getStringExtra("contact");
					
					boolean cs = intent.getBooleanExtra(Capabilities.CS_VIDEO_CAPABILITY, false);
					result += Capabilities.CS_VIDEO_CAPABILITY + ": " + cs + "\n";

					boolean image = intent.getBooleanExtra(Capabilities.IMAGE_SHARING_CAPABILITY, false);
					result += Capabilities.IMAGE_SHARING_CAPABILITY + ": " + image + "\n";
					
					boolean video = intent.getBooleanExtra(Capabilities.VIDEO_SHARING_CAPABILITY, false);
					result += Capabilities.VIDEO_SHARING_CAPABILITY + ": " + video + "\n";
					
					boolean ft = intent.getBooleanExtra(Capabilities.FILE_SHARING_CAPABILITY, false);
					result += Capabilities.FILE_SHARING_CAPABILITY + ": " + ft + "\n";
					
					boolean im = intent.getBooleanExtra(Capabilities.IM_SESSION_CAPABILITY, false);
					result += Capabilities.IM_SESSION_CAPABILITY + ": " + im + "\n";
					
			    	Utils.showInfo(RequestPresenceCapabilities.this, getString(R.string.label_supported_capabilities) + " " + contact, result);            	
				}
			});
		}
    };
    
}
