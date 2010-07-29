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

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.orangelabs.rcs.samples.R;
import com.orangelabs.rcs.samples.utils.Utils;

/**
 * Test which activities or broadcast receivers can catch a given SIP intent
 *  
 * @author jexa7410
 */
public class MatchSipIntent extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.intents_match_sip_intent);
        
        // Set UI title
        setTitle(R.string.menu_match_sip_intent);

        // Set default intent parameters
		EditText methodEdit = (EditText)findViewById(R.id.method);
		methodEdit.setText("MESSAGE");
		EditText mimeEdit = (EditText)findViewById(R.id.mime);
		mimeEdit.setText("text/plain");

		// Set button callback
		Button btn = (Button)findViewById(R.id.query);
        btn.setOnClickListener(btnQueryListener);
    }

    @Override
    public void onDestroy() {
    	super.onDestroy();
    }

    /**
     * Send button listener
     */
    private OnClickListener btnQueryListener = new OnClickListener() {
        public void onClick(View v) {
        	// Set the SIP request to query
    		EditText methodEdit = (EditText)findViewById(R.id.method); 
    		String action = "com.orangelabs.rcs.intent." + methodEdit.getText().toString();
    		Intent intent = new Intent(action);
    		intent.addCategory(Intent.CATEGORY_DEFAULT);
    		
    		// Set the mime type to query
    		EditText mimeEdit = (EditText)findViewById(R.id.mime); 
			intent.setType(mimeEdit.getText().toString());

			// Make a query
			String result = "";
    		PackageManager packageManager = getApplicationContext().getPackageManager();
    		List<ResolveInfo> list1 = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
    		for(int i=0; i< list1.size(); i++) {
    			ResolveInfo info = list1.get(i);
    			result += "-> " + info.activityInfo.name + "\n";
    		}
    		
    		packageManager = getApplicationContext().getPackageManager();
    		List<ResolveInfo> list2 = packageManager.queryBroadcastReceivers(intent, PackageManager.MATCH_DEFAULT_ONLY);
    		for(int i=0; i< list2.size(); i++) {
    			ResolveInfo info = list2.get(i);
    			result += "-> " + info.activityInfo.name + "\n";
    		}
 
    		// Display the query result
    		if (result.length() == 0) {
				Utils.showInfo(MatchSipIntent.this, getString(R.string.label_intent_not_match));
    		} else { 
				Utils.showInfo(MatchSipIntent.this, result);
        	}
        }
    };
}
