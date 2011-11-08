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

package com.orangelabs.rcs.ri;

import android.app.ListActivity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.orangelabs.rcs.ri.presence.BlockedContactList;
import com.orangelabs.rcs.ri.presence.GrantedContactList;
import com.orangelabs.rcs.ri.presence.ManageContactList;
import com.orangelabs.rcs.ri.presence.PublishPresenceInfo;
import com.orangelabs.rcs.ri.presence.RevokedContactList;
import com.orangelabs.rcs.ri.presence.ShowEab;

/**
 * Presence RI
 * 
 * @author jexa7410
 */
public class PresenceRI extends ListActivity {
	
	boolean isInSocialPresenceMode = false;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set layout
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
        // Set items
        String[] presenceItems = {
    		getString(R.string.menu_presence_info),
    		getString(R.string.menu_eab),
    		getString(R.string.menu_manage_contacts),
    		getString(R.string.menu_granted_contacts),
    		getString(R.string.menu_blocked_contacts),
    		getString(R.string.menu_revoked_contacts)    		
        };
        setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, presenceItems));
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	switch(position) {
    	case 0:
    		startActivity(new Intent(this, PublishPresenceInfo.class));
    		break;
    	case 1:
    		startActivity(new Intent(this, ShowEab.class));
    		break;
    	case 2:
    		startActivity(new Intent(this, ManageContactList.class));
    		break;
    	case 3:
    		startActivity(new Intent(this, GrantedContactList.class));
    		break;
    	case 4:
    		startActivity(new Intent(this, BlockedContactList.class));
    		break;
    	case 5:
    		startActivity(new Intent(this, RevokedContactList.class));
    		break;
    	}
    }
}
