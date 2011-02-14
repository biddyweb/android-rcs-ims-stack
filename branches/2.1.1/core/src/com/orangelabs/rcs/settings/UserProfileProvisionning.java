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
package com.orangelabs.rcs.settings;

import com.orangelabs.rcs.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * User profile provisionning
 * 
 * @author jexa7410
 */
public class UserProfileProvisionning extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set title
        setTitle(R.string.rcs_settings_title_provisionning);
        
        // Set layout
        setContentView(R.layout.rcs_settings_provisioning_layout);

        // Set button listeners
        Button button = (Button)findViewById(R.id.btn_continue);        
        button.setOnClickListener(continueListener);
        button = (Button)findViewById(R.id.btn_exit);        
        button.setOnClickListener(exitListener);
    }
    
    private OnClickListener continueListener = new OnClickListener() {
        public void onClick(View v) {
	    	Intent intent = new Intent(UserProfileProvisionning.this, UserProfileSettingsDisplay.class);
        	startActivity(intent);
        	UserProfileProvisionning.this.finish();
        }
    };
    
    private OnClickListener exitListener = new OnClickListener() {
        public void onClick(View v) {
        	UserProfileProvisionning.this.finish();
        }
    };
}
