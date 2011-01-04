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
package com.orangelabs.rcs.samples;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.orangelabs.rcs.samples.im.InitiateChat;
import com.orangelabs.rcs.samples.im.InitiateChatGroup;
import com.orangelabs.rcs.samples.im.InitiateFileTransfer;
import com.orangelabs.rcs.samples.im.SendLargeIm;
import com.orangelabs.rcs.samples.im.SendShortIm;

/**
 * Messaging API demos
 * 
 * @author jexa7410
 */
public class MessagingApiDemos extends ListActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        String[] items = {
        		getString(R.string.menu_short_im),
        		getString(R.string.menu_large_im),
        		getString(R.string.menu_transfer_file),
        		getString(R.string.menu_one_to_one_chat),
        		getString(R.string.menu_adhoc_group_chat)
        };
        setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items));
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        switch(position) {
	        case 0:
            	startActivity(new Intent(this, SendShortIm.class));
                break;
	        case 1:
            	startActivity(new Intent(this, SendLargeIm.class));
	            break;
	        case 2:
            	startActivity(new Intent(this, InitiateFileTransfer.class));
	            break;
	        case 3:
            	startActivity(new Intent(this, InitiateChat.class));
	            break;
	        case 4:
            	startActivity(new Intent(this, InitiateChatGroup.class));
	            break;
        }
    }
}
