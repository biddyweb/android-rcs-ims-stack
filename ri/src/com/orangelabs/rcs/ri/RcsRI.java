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
package com.orangelabs.rcs.ri;

import android.app.ListActivity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.orangelabs.rcs.platform.logger.AndroidAppender;
import com.orangelabs.rcs.ri.eventlog.EventLog;
import com.orangelabs.rcs.ri.utils.Utils;
import com.orangelabs.rcs.service.api.client.management.ManagementApiIntents;
import com.orangelabs.rcs.utils.PhoneUtils;
import com.orangelabs.rcs.utils.logger.Appender;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * RCS RI application
 * 
 * @author jexa7410
 */
public class RcsRI extends ListActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set title
        setTitle(getString(R.string.app_name));

        // Set layout
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Set logger appenders
		Appender[] appenders = new Appender[] { 
				new AndroidAppender()
			};
		Logger.setAppenders(appenders);
        
        // Set the country code
		PhoneUtils.setCountryCode(getApplicationContext());

		// Set items
        String[] items = {
        		getString(R.string.menu_address_book),
        		getString(R.string.menu_presence),
        		getString(R.string.menu_messaging),
        		getString(R.string.menu_richcall),
        		getString(R.string.menu_eventlog),
        		getString(R.string.menu_settings),
        		getString(R.string.menu_about)
        };
        setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items));
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        switch(position) {
	        case 0:
	        	try {
	        		startActivity(new Intent("com.android.contacts.action.LIST_DEFAULT"));
	        	} catch(ActivityNotFoundException e) {
	        		Utils.showInfo(this, getString(R.string.label_ab_not_found));
	        	}
	            break;
	        case 1:
            	startActivity(new Intent(this, PresenceRI.class));
                break;
	        case 2:
            	startActivity(new Intent(this, MessagingRI.class));
	            break;
	        case 3:
            	startActivity(new Intent(this, RichCallRI.class));
	            break;
	        case 4:
	        	startActivity(new Intent(this, EventLog.class));
	        	break;
	        case 5:
	            startActivity(new Intent(ManagementApiIntents.RCS_SETTINGS));
	            break;
	        case 6:
	        	startActivity(new Intent(this, AboutRI.class));
	            break;
        }
    }
}
