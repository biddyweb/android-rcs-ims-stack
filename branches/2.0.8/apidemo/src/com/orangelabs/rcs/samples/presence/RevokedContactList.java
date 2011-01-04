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

import java.util.List;

import android.app.ListActivity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ArrayAdapter;

import com.orangelabs.rcs.samples.R;
import com.orangelabs.rcs.samples.utils.Utils;
import com.orangelabs.rcs.service.api.client.ClientApiListener;
import com.orangelabs.rcs.service.api.client.presence.PresenceApi;

/**
 * Show revoked contacts
 * 
 * @author jexa7410
 */
public class RevokedContactList extends ListActivity implements ClientApiListener {
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
                       
        // Set UI title
        setTitle(R.string.menu_revoked_contacts);

        // Instanciate presence API
        presenceApi = new PresenceApi(getApplicationContext());
        presenceApi.addApiEventListener(this);
        presenceApi.connectApi();
    }

    @Override
    public void onDestroy() {
    	super.onDestroy();

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
			        // Query XDM server to get the list of blocked contacts
        			final List<String> list = presenceApi.getRevokedContacts();

        			// Display result
            		handler.post(new Runnable() { 
            			public void run() {
                			if (list.size() == 0) {
                				Utils.showInfo(RevokedContactList.this, getString(R.string.label_list_empty));
                				return;
                			}
                			setListAdapter(new ArrayAdapter<String>(RevokedContactList.this,
            		                android.R.layout.simple_list_item_1, list));
            		        getListView().setTextFilterEnabled(true);
            			}
                	});
        		} catch(Exception e) {
            		handler.post(new Runnable() { 
            			public void run() {
            				Utils.showError(RevokedContactList.this, getString(R.string.label_read_list_ko));
            			}
                	});
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
}

