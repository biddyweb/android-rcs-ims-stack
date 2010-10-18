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
package com.orangelabs.rcs.samples.intents;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.CursorWrapper;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.orangelabs.rcs.samples.R;
import com.orangelabs.rcs.samples.utils.Utils;

/**
 * Broadcast a SIP intent locally on the device to see which
 * activities or broadcast receivers can catch it
 *  
 * @author jexa7410
 */
public class BroadcastSipIntent extends Activity {

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.intents_broadcast_sip_intent);
        
        // Set UI title
        setTitle(R.string.menu_broadcast_sip_intent);

        // set contact selector
        Spinner spinner = (Spinner)findViewById(R.id.from);
        spinner.setAdapter(Utils.createContactListAdapter(this));

        // Set default intent parameters
        EditText methodEdit = (EditText)findViewById(R.id.method);
		methodEdit.setText("MESSAGE");
		EditText mimeEdit = (EditText)findViewById(R.id.mime);
		mimeEdit.setText("text/plain");
		EditText contentEdit = (EditText)findViewById(R.id.content);
		contentEdit.setText("Hello world !");

		// Set button callback
		Button btn = (Button)findViewById(R.id.send);
        btn.setOnClickListener(btnSendListener);
    }

    @Override
    public void onDestroy() {
    	super.onDestroy();
    }
    
    /**
     * Send button listener
     */
    private OnClickListener btnSendListener = new OnClickListener() {
        public void onClick(View v) {
        	// Get intent parameters and create a SIP intent
    		EditText methodEdit = (EditText)findViewById(R.id.method); 
    		String action = "com.orangelabs.rcs.intent." + methodEdit.getText().toString();
    		Intent intent = new Intent(action);
    		intent.addCategory(Intent.CATEGORY_DEFAULT);
    		
    		// Set the mime type
    		EditText mimeEdit = (EditText)findViewById(R.id.mime); 
			intent.setType(mimeEdit.getText().toString());
    		EditText contentEdit = (EditText)findViewById(R.id.content);
			intent.putExtra("Content", contentEdit.getText().toString());
			
			// Set the remote contact
            Spinner spinner = (Spinner)findViewById(R.id.from);
            CursorWrapper cursor = (CursorWrapper)spinner.getSelectedItem();
            String from = cursor.getString(1);
			intent.putExtra("From", from);
			
			// set the call-ID
			intent.putExtra("CallId", System.currentTimeMillis() + "@domain.com");
			
    		try {
				// Send intent to broadcast receivers
	    		getApplicationContext().sendBroadcast(intent);

	    		// Send intent to activities
	    		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    		getApplicationContext().startActivity(intent);
		    } catch(ActivityNotFoundException e) {
		    	// Nothing to do
		    } catch(Exception e) {
		    	Utils.showError(BroadcastSipIntent.this, getString(R.string.label_intent_failed));
		    }
        }
    };
}
