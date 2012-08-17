/*******************************************************************************
 * Software Name : RCS IMS Stack
 *
 * Copyright (C) 2010 France Telecom S.A.
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

package com.orangelabs.rcs.connector;

import android.app.ListActivity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.orangelabs.rcs.connector.utils.Utils;
import com.orangelabs.rcs.platform.AndroidFactory;
import com.orangelabs.rcs.provider.settings.RcsSettings;
import com.orangelabs.rcs.service.api.client.gsma.GsmaUiConnector;
import com.orangelabs.rcs.utils.PhoneUtils;

/**
 * Test UI connector API
 * 
 * @author jexa7410
 */
public class TestUiConnector extends ListActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
		// Set application context
		AndroidFactory.setApplicationContext(getApplicationContext());

		// Set title
        setTitle(getString(R.string.app_name));

        // Set layout
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
       
        // Instanciate the settings manager
        RcsSettings.createInstance(getApplicationContext());		
		
        // Initialize the country code
		PhoneUtils.initialize(getApplicationContext());
		
		// Set items
        String[] items = {
    		getString(R.string.menu_rcs_status),
    		getString(R.string.menu_my_capabilities),
    		getString(R.string.menu_contact_capabilities),
    		getString(R.string.menu_initiate_chat),
    		getString(R.string.menu_view_chat),
    		getString(R.string.menu_initiate_group_chat),
    		getString(R.string.menu_view_group_chat),
    		getString(R.string.menu_initiate_ft),
    		getString(R.string.menu_view_ft)
        };
    	setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items));
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	switch(position) {
			case 0:
				// Get RCS status
				startActivity(new Intent(this, GetRcsStatus.class));
				break;
				
        	case 1:
        		// Get my capabilities
        		startActivity(new Intent(this, GetMyCapabilities.class));
        		break;
        		
        	case 2:
        		// Get contact capabilities
        		startActivity(new Intent(this, GetContactCapabilities.class));
        		break;
        		
        	case 3:
        		// Initiate a chat
        		try {
		        	startActivity(new Intent(GsmaUiConnector.ACTION_INITIATE_CHAT));
		    	} catch(ActivityNotFoundException e) {
    				Utils.showMessage(this, getString(R.string.label_ui_not_found));
		    	}
        		break;
        		
        	case 4:
        		// View a chat
        		try {
        			startActivity(new Intent(GsmaUiConnector.ACTION_VIEW_CHAT));
            	} catch(ActivityNotFoundException e) {
    				Utils.showMessage(this, getString(R.string.label_ui_not_found));
            	}
        		break;
        		
        	case 5:
        		// Initiate a group chat
        		try {
        			startActivity(new Intent(GsmaUiConnector.ACTION_INITIATE_CHAT_GROUP));
            	} catch(ActivityNotFoundException e) {
    				Utils.showMessage(this, getString(R.string.label_ui_not_found));
            	}
        		break;
        		
        	case 6:
        		// View a group chat
        		try {
        			startActivity(new Intent(GsmaUiConnector.ACTION_VIEW_CHAT_GROUP));
            	} catch(ActivityNotFoundException e) {
    				Utils.showMessage(this, getString(R.string.label_ui_not_found));
            	}
        		break;
        		
        	case 7:
        		// Initiate a FT
        		try {
        			startActivity(new Intent(GsmaUiConnector.ACTION_INITIATE_FT));
            	} catch(ActivityNotFoundException e) {
    				Utils.showMessage(this, getString(R.string.label_ui_not_found));
            	}
        		break;
        		
        	case 8:
        		// View a FT
        		try {
        			startActivity(new Intent(GsmaUiConnector.ACTION_VIEW_FT));
            	} catch(ActivityNotFoundException e) {
    				Utils.showMessage(this, getString(R.string.label_ui_not_found));
            	}
        		break;
    	}
    }
}
