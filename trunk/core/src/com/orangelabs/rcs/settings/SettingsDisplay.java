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

import java.util.List;

import com.orangelabs.rcs.R;
import com.orangelabs.rcs.provider.settings.RcsSettings;
import com.orangelabs.rcs.service.api.client.ClientApiUtils;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

/**
 * Settings display
 * 
 * @author jexa7410
 */
public class SettingsDisplay extends PreferenceActivity {
    // Dialog IDs
    private final static int SERVICE_ACTIVATION_CONFIRMATION_DIALOG = 1;
    private final static int SERVICE_DEACTIVATION_CONFIRMATION_DIALOG = 2;

    private CheckBoxPreference rcsCheckbox;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        addPreferencesFromResource(R.xml.rcs_settings_preferences);
        setTitle(R.string.title_settings);

        int baseNumberOfPreferences = getPreferenceScreen().getPreferenceCount();
        
        // Set default value
    	rcsCheckbox = (CheckBoxPreference)getPreferenceScreen().findPreference("rcs_activation");
    	rcsCheckbox.setChecked(isRcsServiceStarted());
    	
    	//Dynamic discovery for apps that listen to com.orangelabs.rcs.EXT_SETTINGS
    	Intent intent = new Intent(ClientApiUtils.RCS_SETTINGS);
    	addPreferencesFromIntent(intent);
    	
    	// Modify the intents so the activities can be launched even if not defined in this application
    	int totalNumberOfPreferences = getPreferenceScreen().getPreferenceCount();
    	for (int i=baseNumberOfPreferences;i<totalNumberOfPreferences;i++){
    		// We have to change the intents for all preferences discovered via the addPreferencesFromIntent method
        	Preference dynamicPref = getPreferenceScreen().getPreference(i);
        	Intent dynamicPrefIntent = dynamicPref.getIntent();
        	dynamicPrefIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        	dynamicPref.setIntent(dynamicPrefIntent);
    	}
    	
    }

    @Override
    protected void onResume() {
    	super.onResume();
    	
    	// Restore service state
		rcsCheckbox.setChecked(isRcsServiceStarted());
    }

    
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == rcsCheckbox) {
        	boolean state = rcsCheckbox.isChecked();
        	rcsCheckbox.setChecked(!state);
        	if (state) {
        		showDialog(SERVICE_ACTIVATION_CONFIRMATION_DIALOG);
        	} else {
        		showDialog(SERVICE_DEACTIVATION_CONFIRMATION_DIALOG);
        	}
        	return true;
        } else {
        	return super.onPreferenceTreeClick(preferenceScreen, preference);
    	}
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case SERVICE_ACTIVATION_CONFIRMATION_DIALOG:
                return new AlertDialog.Builder(this)
		        		.setIcon(android.R.drawable.ic_dialog_info)
		        		.setTitle(R.string.label_confirm)
                        .setMessage(R.string.label_rcs_service_startup)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(android.R.string.ok, startupDialogListener)
                        .setCancelable(false)
                        .create();
            case SERVICE_DEACTIVATION_CONFIRMATION_DIALOG:
                return new AlertDialog.Builder(this)
                		.setIcon(android.R.drawable.ic_dialog_info)
                		.setTitle(R.string.label_confirm)
                        .setMessage(R.string.label_rcs_service_shutdown)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(android.R.string.ok, shutdownDialogListener)
                        .setCancelable(false)
                        .create();
        }
        return super.onCreateDialog(id);
    }
    
	private DialogInterface.OnClickListener startupDialogListener = new DialogInterface.OnClickListener() {
	    public void onClick(DialogInterface dialog, int button) {
	    	// Start RCS service
	    	startService(new Intent("com.orangelabs.rcs.SERVICE"));
        	rcsCheckbox.setChecked(true);
        	RcsSettings.getInstance().setServiceActivated(true);
	    }
	};

    private DialogInterface.OnClickListener shutdownDialogListener = new DialogInterface.OnClickListener() {
	    public void onClick(DialogInterface dialog, int button) {
	    	// Stop RCS service
            stopService(new Intent("com.orangelabs.rcs.SERVICE"));
        	rcsCheckbox.setChecked(false);
        	RcsSettings.getInstance().setServiceActivated(false);
	    }
	};

	private boolean isRcsServiceStarted() {
		ActivityManager activityManager = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
		List<ActivityManager.RunningServiceInfo> serviceList = activityManager.getRunningServices(Integer.MAX_VALUE);
		 for(int i = 0; i < serviceList.size(); i++) {
			 ActivityManager.RunningServiceInfo serviceInfo = serviceList.get(i);
			 ComponentName serviceName = serviceInfo.service;
			 if (serviceName.getClassName().equals("com.orangelabs.rcs.service.RcsCoreService")) {
				 if (serviceInfo.pid != 0) {
					 return true;
				 } else {
					 return false;
				 }
			 }
		 }
		 return false;
	}
}
