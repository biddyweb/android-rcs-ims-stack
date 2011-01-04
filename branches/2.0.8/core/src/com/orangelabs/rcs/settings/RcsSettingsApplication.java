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

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import com.orangelabs.rcs.R;
import com.orangelabs.rcs.provider.settings.RcsSettings;

/**
 * RCS settings application which permits to edit application
 * settings, access to user guide and so on.
 * 
 * @author jexa7410
 */
public class RcsSettingsApplication extends Activity {
    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        
		// Set title
        setTitle(R.string.rcs_settings_title_settings);
        setContentView(R.layout.rcs_settings_layout);
        
        // Set the release number
        TextView releaseView = (TextView)findViewById(R.id.settings_label_release);
        String relLabel = getString(R.string.rcs_settings_label_release);
        String relNumber = getString(R.string.rcs_core_release_number);
        releaseView.setText(relLabel + " " + relNumber);
        
        // Instanciate the settings manager
        RcsSettings.createInstance(getApplicationContext());
    }
        
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		new MenuInflater(getApplication()).inflate(R.menu.rcs_settings_main_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int i = item.getItemId();
		if (i == R.id.menu_settings) {
	    	Intent intent = new Intent(this, SettingsDisplay.class);
	    	startActivity(intent);
            return true;
		} else
		if (i == R.id.menu_about) {
	    	Intent intent = new Intent(this, AboutDisplay.class);
	    	startActivity(intent);
			return true;
		} else
		if (i == R.id.menu_help) {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse("http://172.20.14.55/index.php?option=com_content&view=category&id=2&Itemid=4"));
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
			return true;
		}		
		return super.onOptionsItemSelected(item);
	}
}
