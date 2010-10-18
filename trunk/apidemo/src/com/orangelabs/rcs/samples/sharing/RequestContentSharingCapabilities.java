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
package com.orangelabs.rcs.samples.sharing;

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

import com.orangelabs.rcs.samples.R;
import com.orangelabs.rcs.samples.utils.Utils;
import com.orangelabs.rcs.service.api.client.richcall.RichCallApi;
import com.orangelabs.rcs.service.api.client.richcall.RichCallApiIntents;

/**
 * Request content sharing capabilities of a given contact
 * 
 * @author jexa7410
 */
public class RequestContentSharingCapabilities extends Activity {
	/**
	 * UI handler
	 */
	private final Handler handler = new Handler();

    /**
	 * Rich call API
	 */
    private RichCallApi callApi;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.richcall_request_capabilities);
        
        // Set UI title
        setTitle(R.string.menu_request_capabilities);
        
        // Set the contact selector
        Spinner spinner = (Spinner)findViewById(R.id.contact);
        spinner.setAdapter(Utils.createContactListAdapter(this));

        // Set button callback
        Button btn = (Button)findViewById(R.id.request);
        btn.setOnClickListener(btnRequestListener);
               
    	// Register intent reciver to receive the query result
        IntentFilter filter = new IntentFilter(RichCallApiIntents.SHARING_CAPABILITIES);
		registerReceiver(capabilitiesIntentReceiver, filter, null, handler);

		// Instanciate rich call API
		callApi = new RichCallApi(getApplicationContext());
		callApi.connectApi();
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();

    	// Unregister intent receiver
		unregisterReceiver(capabilitiesIntentReceiver);

		// Disconnect rich call API
        callApi.disconnectApi();
    }
    	
    /**
     * Request button listener
     */
    private OnClickListener btnRequestListener = new OnClickListener() {
        public void onClick(View v) {
        	// Get the remote contact
            Spinner spinner = (Spinner)findViewById(R.id.contact);
            CursorWrapper cursor = (CursorWrapper)spinner.getSelectedItem();
            String remote = cursor.getString(1);
            
            // Request the capabilities
            try {
            	callApi.requestContentSharingCapabilities(remote);
            } catch(Exception e) {
		    	Utils.showError(RequestContentSharingCapabilities.this, getString(R.string.label_invitation_failed));            	
            }
        }
    };

    /**
     * Broadcast receiver in order to display the capabilities
     */
	private final BroadcastReceiver capabilitiesIntentReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
		    String result = "";
		    boolean image = intent.getBooleanExtra("image", false);
		    if (image) {
		    	result += "image\n";
		    }
		    boolean video = intent.getBooleanExtra("video", false);
		    if (video) {
		    	result += "video\n";
		    }
	    	Utils.showInfo(RequestContentSharingCapabilities.this, getString(R.string.label_supported_media), result);            	
		}
	};    
}    
