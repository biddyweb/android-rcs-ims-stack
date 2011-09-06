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
import android.widget.EditText;
import android.widget.Spinner;

import com.orangelabs.rcs.provider.settings.RcsSettings;

/**
 * Service parameters provisioning
 * 
 * @author jexa7410
 */
public class ServiceProvisioning extends Activity {
	/**
	 * IM session start modes
	 */
    private static final String[] IM_SESSION_START_MODES = {
    	"1", "2"
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
        setContentView(R.layout.service_provisioning);
        
        // Set database content resolver
        this.cr = getContentResolver();
        
		
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		// Get settings from database
        Map<String, String> settings = RcsSettings.getInstance().dump();
        
        // Display UI parameters

        EditText txt = (EditText)this.findViewById(R.id.MaxPhotoIconSize);
        txt.setText(settings.get("MaxPhotoIconSize"));

        txt = (EditText)this.findViewById(R.id.MaxFreetextLength);
        txt.setText(settings.get("MaxFreetextLength"));

        txt = (EditText)this.findViewById(R.id.MaxChatParticipants);
        txt.setText(settings.get("MaxChatParticipants"));

        txt = (EditText)this.findViewById(R.id.MaxChatMessageLength);
        txt.setText(settings.get("MaxChatMessageLength"));

        txt = (EditText)this.findViewById(R.id.ChatIdleDuration);
        txt.setText(settings.get("ChatIdleDuration"));

        txt = (EditText)this.findViewById(R.id.MaxFileTransferSize);
        txt.setText(settings.get("MaxFileTransferSize"));

        txt = (EditText)this.findViewById(R.id.WarnFileTransferSize);
        txt.setText(settings.get("WarnFileTransferSize"));

        txt = (EditText)this.findViewById(R.id.MaxImageShareSize);
        txt.setText(settings.get("MaxImageShareSize"));

        txt = (EditText)this.findViewById(R.id.MaxVideoShareDuration);
        txt.setText(settings.get("MaxVideoShareDuration"));

        txt = (EditText)this.findViewById(R.id.MaxChatSessions);
        txt.setText(settings.get("MaxChatSessions"));

        txt = (EditText)this.findViewById(R.id.MaxFileTransferSessions);
        txt.setText(settings.get("MaxFileTransferSessions"));

        txt = (EditText)this.findViewById(R.id.MaxChatLogEntries);
        txt.setText(settings.get("MaxChatLogEntries"));

        txt = (EditText)this.findViewById(R.id.MaxRichcallLogEntries);
        txt.setText(settings.get("MaxRichcallLogEntries"));

        Spinner spinner = (Spinner)findViewById(R.id.ImSessionStart);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, IM_SESSION_START_MODES);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		if (RcsSettings.getInstance().getImSessionStartMode() == 1) {
			spinner.setSelection(0);
		} else {
			spinner.setSelection(1);
		}
        
        CheckBox check = (CheckBox)this.findViewById(R.id.SmsFallbackService);
        check.setChecked(Boolean.parseBoolean(settings.get("SmsFallbackService")));

        check = (CheckBox)this.findViewById(R.id.StoreForwardServiceWarning);
        check.setChecked(Boolean.parseBoolean(settings.get("StoreForwardServiceWarning")));
        
		check = (CheckBox)this.findViewById(R.id.UsePresenceService);
        check.setChecked(Boolean.parseBoolean(settings.get("UsePresenceService")));        

        check = (CheckBox)this.findViewById(R.id.UseRichcallService);
        check.setChecked(Boolean.parseBoolean(settings.get("UseRichcallService")));        
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
		        // Save UI parameters
		        
		        EditText txt = (EditText)this.findViewById(R.id.MaxPhotoIconSize);
				Provisioning.writeParameter(cr, "MaxPhotoIconSize", txt.getText().toString());

		        txt = (EditText)this.findViewById(R.id.MaxFreetextLength);
				Provisioning.writeParameter(cr, "MaxFreetextLength", txt.getText().toString());

		        txt = (EditText)this.findViewById(R.id.MaxChatParticipants);
				Provisioning.writeParameter(cr, "MaxChatParticipants", txt.getText().toString());

		        txt = (EditText)this.findViewById(R.id.MaxChatMessageLength);
				Provisioning.writeParameter(cr, "MaxChatMessageLength", txt.getText().toString());

		        txt = (EditText)this.findViewById(R.id.ChatIdleDuration);
				Provisioning.writeParameter(cr, "ChatIdleDuration", txt.getText().toString());

		        txt = (EditText)this.findViewById(R.id.MaxFileTransferSize);
				Provisioning.writeParameter(cr, "MaxFileTransferSize", txt.getText().toString());

		        txt = (EditText)this.findViewById(R.id.WarnFileTransferSize);
				Provisioning.writeParameter(cr, "WarnFileTransferSize", txt.getText().toString());

				txt = (EditText)this.findViewById(R.id.MaxImageShareSize);
				Provisioning.writeParameter(cr, "MaxImageShareSize", txt.getText().toString());

		        txt = (EditText)this.findViewById(R.id.MaxVideoShareDuration);
				Provisioning.writeParameter(cr, "MaxVideoShareDuration", txt.getText().toString());

		        txt = (EditText)this.findViewById(R.id.MaxChatSessions);
				Provisioning.writeParameter(cr, "MaxChatSessions", txt.getText().toString());

		        txt = (EditText)this.findViewById(R.id.MaxFileTransferSessions);
				Provisioning.writeParameter(cr, "MaxFileTransferSessions", txt.getText().toString());

		        txt = (EditText)this.findViewById(R.id.MaxChatLogEntries);
				Provisioning.writeParameter(cr, "MaxChatLogEntries", txt.getText().toString());

		        txt = (EditText)this.findViewById(R.id.MaxRichcallLogEntries);
				Provisioning.writeParameter(cr, "MaxRichcallLogEntries", txt.getText().toString());
				
				Spinner spinner = (Spinner)findViewById(R.id.ImSessionStart);
				if (spinner.getSelectedItemId() == 0) {
					Provisioning.writeParameter(cr, "ImSessionStart", "1");
				} else {
					Provisioning.writeParameter(cr, "ImSessionStart", "2");
				}
				
		        CheckBox check = (CheckBox)this.findViewById(R.id.SmsFallbackService);
				Provisioning.writeParameter(cr, "SmsFallbackService", Boolean.toString(check.isChecked()));

		        check = (CheckBox)this.findViewById(R.id.StoreForwardServiceWarning);
				Provisioning.writeParameter(cr, "StoreForwardServiceWarning", Boolean.toString(check.isChecked()));

				check = (CheckBox)this.findViewById(R.id.UsePresenceService);
				Provisioning.writeParameter(cr, "UsePresenceService", Boolean.toString(check.isChecked()));
				
				check = (CheckBox)this.findViewById(R.id.UseRichcallService);
				Provisioning.writeParameter(cr, "UseRichcallService", Boolean.toString(check.isChecked()));
				
				break;
		}
		return true;
	}
}
