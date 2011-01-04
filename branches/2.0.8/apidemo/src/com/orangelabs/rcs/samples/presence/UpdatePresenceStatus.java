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

import java.util.Date;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.orangelabs.rcs.core.ims.service.presence.PresenceInfo;
import com.orangelabs.rcs.provider.eab.RichAddressBook;
import com.orangelabs.rcs.samples.R;
import com.orangelabs.rcs.service.api.client.ClientApiListener;
import com.orangelabs.rcs.service.api.client.presence.PresenceApi;
import com.orangelabs.rcs.service.api.client.presence.PresenceApiIntents;

/**
 * Update my hyper-availability status
 * 
 * @author jexa7410
 */
public class UpdatePresenceStatus extends Activity implements ClientApiListener {
	/**
	 * UI handler
	 */
	private final Handler handler = new Handler();

	/**
	 * Presence info
	 */
	private PresenceInfo presenceInfo = null;

	/**
	 * Presence API
	 */
    public static PresenceApi presenceApi;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.presence_publish_poke);
        
        // Set UI title
        setTitle(R.string.menu_publish_poke);
        
        // Set button callback
        ToggleButton btn = (ToggleButton)findViewById(R.id.poke);
        btn.setOnClickListener(btnPublishPokeListener);

        // Instanciate the RCS contacts content provider
        RichAddressBook.createInstance(getApplicationContext());

		// Display the current presence info from the RCS contacts content provider
    	presenceInfo = RichAddressBook.getInstance().getMyPresenceInfo();
    	btn.setChecked(presenceInfo.isHyperavailable());

    	// Register to receive presence status update event
		IntentFilter filter = new IntentFilter(PresenceApiIntents.MY_PRESENCE_STATUS_CHANGED);
		registerReceiver(presenceStatusChangedIntentReceiver, filter, null, handler);

		// Instanciate presence API
        presenceApi = new PresenceApi(getApplicationContext());
        presenceApi.addApiEventListener(this);
        presenceApi.connectApi();

    }

    @Override
    public void onDestroy() {
    	super.onDestroy();
    	
    	// Unregister intent receiver
		unregisterReceiver(presenceStatusChangedIntentReceiver);

		// Disconnect presence API
    	presenceApi.disconnectApi();
    }
 
    /**
     * API connected
     */
    public void handleApiConnected() {
		Thread t = new Thread() {
        	public void run() {
				try {
					// Display the expiration time if the hyper-availability period is not yet terminated
			        ToggleButton btn = (ToggleButton)findViewById(R.id.poke);
					if (btn.isChecked()) {
	        			long expireAt = presenceApi.getHyperAvailabilityExpiration();
	        			final Date date = new Date(expireAt);
	        			handler.post(new Runnable() { 
	            			public void run() {
	        			        TextView expireAt = (TextView)findViewById(R.id.expire_at);
	        			        expireAt.setText(date.toLocaleString());
	            			}
	                	});
					}
        		} catch(Exception e) {
        		}
	    	}
	    };
	    t.start();
    }

    /**
     * API disconnected
     */
    public void handleApiDisconnected() {
    }
    
    /**
     * Publish button listener
     */
    private OnClickListener btnPublishPokeListener = new OnClickListener() {
        public void onClick(View v) {
	        ToggleButton btn = (ToggleButton)findViewById(R.id.poke);
        	try {
        		// Publish the new hyper-availability status
        		if (presenceApi.setMyHyperAvailabilityStatus(btn.isChecked())) {
    				Toast.makeText(UpdatePresenceStatus.this, R.string.label_publish_ok, Toast.LENGTH_SHORT).show();
        		} else {
    				Toast.makeText(UpdatePresenceStatus.this, R.string.label_publish_ko, Toast.LENGTH_SHORT).show();
    				btn.setChecked(false);
        		}
        	} catch(Exception e) {
				Toast.makeText(UpdatePresenceStatus.this, R.string.label_publish_ko, Toast.LENGTH_SHORT).show();
				btn.setChecked(false);
        	}
        }
    };
    
    /**
     * Broadcast receiver in order to display in real time the hyper-availability status 
     */
	private final BroadcastReceiver presenceStatusChangedIntentReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			boolean status = intent.getBooleanExtra("status", false);
			if (status) {
				// Hyper-availabitity period started
				long expireAt = intent.getLongExtra("expireAt", 0L);
				final Date date = new Date(expireAt);
				handler.post(new Runnable() {
					public void run() {
				        TextView expireTxt = (TextView)findViewById(R.id.expire_at);
				        expireTxt.setText(date.toLocaleString());
					}
				});
			} else {
				// Hyper-availabitity period terminated
				handler.post(new Runnable() {
					public void run() {
				        TextView expireTxt = (TextView)findViewById(R.id.expire_at);
				        expireTxt.setText("");
				        ToggleButton btn = (ToggleButton)findViewById(R.id.poke);
				        btn.setChecked(false);
					}
				});
			}
		}
	};    
}
