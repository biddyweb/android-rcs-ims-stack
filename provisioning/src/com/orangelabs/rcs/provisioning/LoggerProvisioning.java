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

package com.orangelabs.rcs.provisioning;

import java.util.Map;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

import com.orangelabs.rcs.provider.settings.RcsSettings;

/**
 * End user profile parameters provisioning
 * 
 * @author jexa7410
 */
public class LoggerProvisioning extends Activity {
	/**
	 * Trace level
	 */
    private static final String[] TRACE_LEVEL = {
        "DEBUG", "INFO", "WARN", "ERROR", "FATAL" 
    };

    /**
	 * Content resolver
	 */
	private ContentResolver cr;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set layout
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.logger_provisioning);
        
        // Set database content resolver
        this.cr = getContentResolver();
        
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		// Get settings from database
        Map<String, String> settings = RcsSettings.getInstance().dump();
        
        // Display logger parameters
    	CheckBox check = (CheckBox)this.findViewById(R.id.TraceActivated);
        check.setChecked(Boolean.parseBoolean(settings.get("TraceActivated")));
        
    	check = (CheckBox)this.findViewById(R.id.SipTraceActivated);
        check.setChecked(Boolean.parseBoolean(settings.get("SipTraceActivated")));

    	check = (CheckBox)this.findViewById(R.id.MediaTraceActivated);
        check.setChecked(Boolean.parseBoolean(settings.get("MediaTraceActivated")));

        Spinner spinner = (Spinner)findViewById(R.id.TraceLevel);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, TRACE_LEVEL);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        if (RcsSettings.getInstance().getTraceLevel().equals(TRACE_LEVEL[0])) {
            spinner.setSelection(0);
        } else if (RcsSettings.getInstance().getTraceLevel().equals(TRACE_LEVEL[1])) {
            spinner.setSelection(1);
        } else if (RcsSettings.getInstance().getTraceLevel().equals(TRACE_LEVEL[2])) {
            spinner.setSelection(2);
        } else if (RcsSettings.getInstance().getTraceLevel().equals(TRACE_LEVEL[3])) {
            spinner.setSelection(3);
        } else if (RcsSettings.getInstance().getTraceLevel().equals(TRACE_LEVEL[4])) {
            spinner.setSelection(4);
        }
	}

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater=new MenuInflater(getApplicationContext());
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_save:
		        // Save logger parameters
		        CheckBox check = (CheckBox)this.findViewById(R.id.TraceActivated);
				Provisioning.writeParameter(cr, "TraceActivated", Boolean.toString(check.isChecked()));

		        check = (CheckBox)this.findViewById(R.id.SipTraceActivated);
				Provisioning.writeParameter(cr, "SipTraceActivated", Boolean.toString(check.isChecked()));

		        check = (CheckBox)this.findViewById(R.id.MediaTraceActivated);
				Provisioning.writeParameter(cr, "MediaTraceActivated", Boolean.toString(check.isChecked()));

				Spinner spinner = (Spinner)findViewById(R.id.TraceLevel);
				String value = (String)spinner.getSelectedItem();
				Provisioning.writeParameter(cr, "TraceLevel", value);

		        Toast.makeText(this, getString(R.string.label_reboot_service), Toast.LENGTH_LONG).show();				
		        break;
		}
		return true;
	}
}
