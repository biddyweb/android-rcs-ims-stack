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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

import com.orangelabs.rcs.R;
import com.orangelabs.rcs.core.ims.network.registration.RegistrationManager;
import com.orangelabs.rcs.provider.settings.RcsSettings;
import com.orangelabs.rcs.service.api.client.ClientApi;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Settings display
 * 
 * @author jexa7410
 */
public class SettingsDisplay extends PreferenceActivity {
    // Dialog IDs
    private final static int SERVICE_DEACTIVATION_CONFIRMATION_DIALOG = 1;
    private final static int ROAMING_DEACTIVATION_CONFIRMATION_DIALOG = 2;
    
    /**
     * Service flag
     */
    private CheckBoxPreference rcsCheckbox;
    
    /**
     * Roaming flag
     */
    private CheckBoxPreference roamingCheckbox;

	/**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());
    
    private Handler handler=new Handler();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
		// Set title
        setTitle(R.string.rcs_settings_title_settings);
        addPreferencesFromResource(R.xml.rcs_settings_preferences);

        // Instanciate the settings provider
        RcsSettings.createInstance(getApplicationContext());
        
        // Set defualt value
    	rcsCheckbox = (CheckBoxPreference)getPreferenceScreen().findPreference("rcs_activation");
		rcsCheckbox.setChecked(RcsSettings.getInstance().isServiceActivated());
		roamingCheckbox = (CheckBoxPreference)getPreferenceScreen().findPreference("rcs_roaming");
		roamingCheckbox.setChecked(RcsSettings.getInstance().isRoamingAuthorized());
        
        // Hide user profile folder if GIBA activated
    	if (RegistrationManager.isGibaAuthent()) {
	    	Preference userProfilePref = (Preference)getPreferenceScreen().findPreference("user_profile");
	    	getPreferenceScreen().removePreference(userProfilePref);
    	}
    	
    	// Modify the intents so the activities can be launched even if not defined in this application
    	// Needed for, typically, RCS downloadable packaging
    	int totalNumberOfPreferences = getPreferenceScreen().getPreferenceCount();
    	for (int i=2; i<totalNumberOfPreferences; i++) {
    		// We start at one, the first preference is the rcsCheckbox that does not define any class name
    		// We have to change the intents for all preferences discovered via the addPreferencesFromIntent method
        	Preference preference = getPreferenceScreen().getPreference(i);
        	Intent preferenceIntent = preference.getIntent();
        	String className = preferenceIntent.getComponent().getClassName();
        	String packageName = getApplicationContext().getPackageName();
        	preferenceIntent.setClassName(packageName, className);
        	preferenceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        	preference.setIntent(preferenceIntent);
    	}
    }
    
    /**
     * Start RCS service in background
     */
    private void startRcsService() {
    	new StartServiceTask().execute();
    }
    
    /**
     * Stop RCS service in background
     */
    private void stopRcsService() {
    	new StopServiceTask().execute();
    }
    
    /**
     * Stop service thread
     */
    private class StopServiceTask extends AsyncTask<Void, Void, Void> {
    	protected void onPreExecute() {
    		super.onPreExecute();
    		handler.post(new Runnable() {
    			public void run() {
					rcsCheckbox.setEnabled(false);
					dismissDialog(SERVICE_DEACTIVATION_CONFIRMATION_DIALOG);
    			}
    		});
    	}
    	
		protected Void doInBackground(Void... params) {
			handler.post(new Runnable() {
				public void run() {
					stopService(new Intent("com.orangelabs.rcs.SERVICE"));
				}
			});
			return null;
		}
    	
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			handler.post(new Runnable() {
				public void run() {
					rcsCheckbox.setEnabled(true);
				}
			});
		}
    }
    
    /**
     * Start service thread
     */
    private class StartServiceTask extends AsyncTask<Void, Void, Void> {
    	@Override
    	protected void onPreExecute() {
    		super.onPreExecute();
    		handler.post(new Runnable() {
    			public void run() {
					rcsCheckbox.setEnabled(false);
    			}
    		});
    	}
    	
		@Override
		protected Void doInBackground(Void... params) {
			handler.post(new Runnable() {
				public void run() {
					startService(new Intent("com.orangelabs.rcs.SERVICE"));
				}
			});
			return null;
		}
    	
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			handler.post(new Runnable() {
				public void run() {
					rcsCheckbox.setEnabled(true);
				}
			});
		}
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == rcsCheckbox) {
        	if (rcsCheckbox.isChecked()) {
        		// Deactivate service
        		if (logger.isActivated()) {
        			logger.debug("Start the service");
        		}
	        	RcsSettings.getInstance().setServiceActivationState(true);
	        	startRcsService();
        	} else {
        		// Activate service. If service is running, ask a confirmation 
        		if (ClientApi.isServiceStarted(getApplicationContext())) {
        			showDialog(SERVICE_DEACTIVATION_CONFIRMATION_DIALOG);
				}
        	}
        	return true;
        } else
        if (preference == roamingCheckbox) {
			if (roamingCheckbox.isChecked()) {
				// Authorize roaming
        		if (logger.isActivated()) {
        			logger.debug("Authorize roaming");
        		}
				RcsSettings.getInstance().setRoamingAuthorizationState(true);
				
				// Start the service if necessary 
				if (rcsCheckbox.isChecked() && !ClientApi.isServiceStarted(getApplicationContext())) {
	        		if (logger.isActivated()) {
	        			logger.debug("Start the service");
	        		}
	        		startRcsService();
				}
			} else {
				// Unauthorize roaming. If the service is started and we are in roaming, ask a confirmation 
				if (ClientApi.isServiceStarted(getApplicationContext()) &&
						isMobileRoaming()) {
					showDialog(ROAMING_DEACTIVATION_CONFIRMATION_DIALOG);
				}
			}
        	return true;
        } else {
        	return super.onPreferenceTreeClick(preferenceScreen, preference);
    	}
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case SERVICE_DEACTIVATION_CONFIRMATION_DIALOG:
                return new AlertDialog.Builder(this)
                		.setIcon(android.R.drawable.ic_dialog_info)
                		.setTitle(R.string.rcs_settings_label_confirm)
                        .setMessage(R.string.rcs_settings_label_rcs_service_shutdown)
                        .setNegativeButton(R.string.rcs_settings_label_cancel, new DialogInterface.OnClickListener() {
                        	public void onClick(DialogInterface dialog, int button) {
                        		rcsCheckbox.setChecked(!rcsCheckbox.isChecked());
						    }
						})                        
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
							public void onCancel(DialogInterface dialog) {
								rcsCheckbox.setChecked(!rcsCheckbox.isChecked());
							}
						})
                        .setPositiveButton(R.string.rcs_settings_label_ok, new DialogInterface.OnClickListener() {
						    public void onClick(DialogInterface dialog, int button) {
						    	// Stop running service
				        		if (logger.isActivated()) {
				        			logger.debug("Stop the service");
				        		}
				        		stopRcsService();
					        	RcsSettings.getInstance().setServiceActivationState(false);
						    }
						})
                        .setCancelable(true)
                        .create();
                
            case ROAMING_DEACTIVATION_CONFIRMATION_DIALOG:
                return new AlertDialog.Builder(this)
                		.setIcon(android.R.drawable.ic_dialog_info)
                		.setTitle(R.string.rcs_settings_label_confirm)
                        .setMessage(R.string.rcs_settings_label_rcs_service_shutdown)
                        .setNegativeButton(R.string.rcs_settings_label_cancel, new DialogInterface.OnClickListener() {
						    public void onClick(DialogInterface dialog, int button) {
						    	roamingCheckbox.setChecked(!roamingCheckbox.isChecked());
						    }
					    })
                        .setCancelable(true)
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
							public void onCancel(DialogInterface dialog) {
								roamingCheckbox.setChecked(!roamingCheckbox.isChecked());
							}
						})
                        .setPositiveButton(R.string.rcs_settings_label_ok, new DialogInterface.OnClickListener() {
						    public void onClick(DialogInterface dialog, int button) {
						    	// Stop running service
				        		if (logger.isActivated()) {
				        			logger.debug("Stop the service in roaming");
				        		}
								stopRcsService();
								RcsSettings.getInstance().setRoamingAuthorizationState(false);
						    }
						})                        
                        .create();
        }
        return super.onCreateDialog(id);
    }
    
	/**
	 * Is mobile connected in roaming
	 * 
	 * @return Boolean
	 */
	private boolean isMobileRoaming() {
		boolean result = false;
		try {
			ConnectivityManager connectivityMgr = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo netInfo = connectivityMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			return netInfo.isRoaming();
		} catch(Exception e) {
		}
		return result;
	}
}
