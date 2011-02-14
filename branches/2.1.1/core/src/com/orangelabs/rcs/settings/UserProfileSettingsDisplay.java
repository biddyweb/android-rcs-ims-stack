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
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.orangelabs.rcs.R;
import com.orangelabs.rcs.provider.settings.RcsSettings;
import com.orangelabs.rcs.utils.PhoneUtils;

/**
 * User profile settings display
 * 
 * @author jexa7410
 */
public class UserProfileSettingsDisplay extends PreferenceActivity implements Preference.OnPreferenceChangeListener {
	private static final int DIALOG_GENERATE_VALUE = 1;
	
	private EditTextPreference username;
	private EditTextPreference displayName;
	private EditTextPreference privateId;
	private EditTextPreference userPassword;
	private EditTextPreference domain;
	private EditTextPreference proxyAddr;
	private EditTextPreference xdmAddr;
	private EditTextPreference xdmLogin;
	private EditTextPreference xdmPassword;
	private EditTextPreference imConferenceUri;
	private boolean modified = false;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        
        addPreferencesFromResource(R.xml.rcs_settings_provisioning_preferences);
        setTitle(R.string.rcs_settings_title_settings);
        
        // Instanciate the settings manager
        RcsSettings.createInstance(getApplicationContext());

        // Set the country code
		PhoneUtils.setCountryCode(getApplicationContext());
        
        // Display current settings
        username = (EditTextPreference)findPreference("username");
        username.setPersistent(false);
        username.setOnPreferenceChangeListener(this);
		String user = RcsSettings.getInstance().getUserProfileImsUserName();
		if ((user != null) && (user.length() > 0)) {
			username.setSummary(user);
			username.setText(user);
		} else {
			username.setSummary(getString(R.string.rcs_settings_label_mandatory_value));
		}

        displayName = (EditTextPreference)findPreference("display_name");
        displayName.setPersistent(false);
        displayName.setOnPreferenceChangeListener(this);
		String name = RcsSettings.getInstance().getUserProfileImsDisplayName();
		if ((name != null) && (name.length() > 0)) {
			displayName.setSummary(name);
			displayName.setText(name);
		} else {
			displayName.setSummary(getString(R.string.rcs_settings_label_empty_displayname));
		}
       
        privateId = (EditTextPreference)findPreference("private_uri");
        privateId.setPersistent(false);
        privateId.setOnPreferenceChangeListener(this);
		String privateName = RcsSettings.getInstance().getUserProfileImsPrivateId();
		if ((privateName != null) && (privateName.length() > 0)) {
			privateId.setSummary(privateName);
			privateId.setText(privateName);
		} else {
			privateId.setSummary(getString(R.string.rcs_settings_label_mandatory_value));
		}

        userPassword = (EditTextPreference)findPreference("user_password");
        userPassword.setPersistent(false);
        userPassword.setOnPreferenceChangeListener(this);
		String pwd = RcsSettings.getInstance().getUserProfileImsPassword();
		if ((pwd != null) && (pwd.length() > 0)) {
	        userPassword.setSummary(pwd);
	        userPassword.setText(pwd);
		} else {
			userPassword.setSummary(getString(R.string.rcs_settings_label_mandatory_value));
		}
		
        domain = (EditTextPreference)findPreference("domain");
        domain.setPersistent(false);
        domain.setOnPreferenceChangeListener(this);
		String home = RcsSettings.getInstance().getUserProfileImsDomain();
		if ((home != null) && (home.length() > 0)) {
	        domain.setSummary(home);
	        domain.setText(home);
		} else {
			domain.setSummary(getString(R.string.rcs_settings_label_mandatory_value));
		}
		
        proxyAddr = (EditTextPreference)findPreference("sip_proxy_addr");
        proxyAddr.setPersistent(false);
        proxyAddr.setOnPreferenceChangeListener(this);
		String proxy = RcsSettings.getInstance().getUserProfileImsProxy();
		if ((proxy != null) && (proxy.length() > 0)) {
	        proxyAddr.setSummary(proxy);
	        proxyAddr.setText(proxy);
		} else {
			proxyAddr.setSummary(getString(R.string.rcs_settings_label_mandatory_value));
		}
		
        xdmAddr = (EditTextPreference)findPreference("xdm_server_addr");
        xdmAddr.setPersistent(false);
        xdmAddr.setOnPreferenceChangeListener(this);
		String server = RcsSettings.getInstance().getUserProfileXdmServer();
		if ((server != null) && (server.length() > 0)) {
	        xdmAddr.setSummary(server);
	        xdmAddr.setText(server);
		} else {
			xdmAddr.setSummary(getString(R.string.rcs_settings_label_mandatory_value));
		}
		
        xdmLogin = (EditTextPreference)findPreference("xdm_login");
        xdmLogin.setPersistent(false);
        xdmLogin.setOnPreferenceChangeListener(this);
		String login = RcsSettings.getInstance().getUserProfileXdmLogin();
		if ((login != null) && (login.length() > 0)) {
	        xdmLogin.setSummary(login);
	        xdmLogin.setText(login);
		} else {
			xdmLogin.setSummary(getString(R.string.rcs_settings_label_mandatory_value));
		}
		
        xdmPassword = (EditTextPreference)findPreference("xdm_password");
        xdmPassword.setPersistent(false);
        xdmPassword.setOnPreferenceChangeListener(this);
		String serverPwd = RcsSettings.getInstance().getUserProfileXdmPassword();
		if ((serverPwd != null) && (serverPwd.length() > 0)) {
	        xdmPassword.setSummary(serverPwd);
	        xdmPassword.setText(serverPwd);
		} else {
			xdmPassword.setSummary(getString(R.string.rcs_settings_label_mandatory_value));
		}

		imConferenceUri = (EditTextPreference)findPreference("im_conference_uri");
		imConferenceUri.setPersistent(false);
		imConferenceUri.setOnPreferenceChangeListener(this);
		String imConfUri = RcsSettings.getInstance().getUserProfileImConferenceUri();
		if ((imConfUri != null) && (imConfUri.length() > 0)) {
			imConferenceUri.setSummary(imConfUri);
			imConferenceUri.setText(imConfUri);
		} else {
			imConferenceUri.setSummary(getString(R.string.rcs_settings_label_mandatory_value));
		}
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        
        if (modified) {
    		Toast.makeText(UserProfileSettingsDisplay.this, getString(R.string.rcs_settings_label_warning), Toast.LENGTH_LONG).show();
    	}
    }
    
    public boolean onPreferenceChange(Preference preference, Object objValue) {
    	String value = (String)objValue;
        if (preference.getKey().equals("username")) {
        	RcsSettings.getInstance().setUserProfileImsUserName(value);
	        username.setSummary(value);
	        modified = true;
        } else        	
        if (preference.getKey().equals("display_name")) {
        	RcsSettings.getInstance().setUserProfileImsDisplayName(value);
	        displayName.setSummary(value);
	        modified = true;
        } else        	
        if (preference.getKey().equals("private_uri")) {
        	RcsSettings.getInstance().setUserProfileImsPrivateId(value);
	        privateId.setSummary(value);
	        modified = true;
        } else        	
        if (preference.getKey().equals("user_password")) {
        	RcsSettings.getInstance().setUserProfileImsPassword(value);
	        userPassword.setSummary(value);
	        modified = true;
        } else
        if (preference.getKey().equals("domain")) {
        	RcsSettings.getInstance().setUserProfileImsDomain(value);
    		domain.setSummary(value);
	        modified = true;
        } else
        if (preference.getKey().equals("sip_proxy_addr")) {
        	RcsSettings.getInstance().setUserProfileImsProxy(value);
	        proxyAddr.setSummary(value);
	        modified = true;
        } else
        if (preference.getKey().equals("xdm_server_addr")) {
        	RcsSettings.getInstance().setUserProfileXdmServer(value);
	        xdmAddr.setSummary(value);
	        modified = true;
    	} else
        if (preference.getKey().equals("xdm_login")) {
        	RcsSettings.getInstance().setUserProfileXdmLogin(value);
	        xdmLogin.setSummary(value);
	        modified = true;
    	} else
        if (preference.getKey().equals("xdm_password")) {
        	RcsSettings.getInstance().setUserProfileXdmPassword(value);
	        xdmPassword.setSummary(value);
	        modified = true;
    	} else
        if (preference.getKey().equals("im_conference_uri")) {
        	RcsSettings.getInstance().setUserProfileImConferenceUri(value);
        	imConferenceUri.setSummary(value);
	        modified = true;
        }
        return true;
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		new MenuInflater(getApplication()).inflate(R.menu.rcs_settings_provisioning_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int i = item.getItemId();
		if (i == R.id.menu_default_profile) {
			showDialog(DIALOG_GENERATE_VALUE);			
			return true;
		}		
		return super.onOptionsItemSelected(item);
	}
	
    @Override
    protected Dialog onCreateDialog(int id) {
        switch(id) {
	        case DIALOG_GENERATE_VALUE:
				LayoutInflater factory = LayoutInflater.from(this);
		    	String countryCode = PhoneUtils.COUNTRY_CODE;
	            final View view = factory.inflate(R.layout.rcs_settings_generate_profile_layout, null);
				EditText textEdit = (EditText)view.findViewById(R.id.msisdn);
	            textEdit.setText("+" + countryCode);
	            
	            final String[] platforms = {
	                "Test fest"
	            };
	            Spinner spinner = (Spinner)view.findViewById(R.id.ims);
	            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
	                    android.R.layout.simple_spinner_item, platforms);
	            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	            spinner.setAdapter(adapter);
	            
	            return new AlertDialog.Builder(this)
	                .setTitle(R.string.rcs_settings_title_generate_profile)
	                .setView(view)
	                .setPositiveButton(R.string.rcs_settings_label_ok, new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int whichButton) {
	            	        // Generate default settings
	            			EditText textEdit = (EditText)view.findViewById(R.id.msisdn);
	            			String number = textEdit.getText().toString();
	        	            Spinner spinner = (Spinner)view.findViewById(R.id.ims);
	        	            int index = spinner.getSelectedItemPosition();
	            			
	            			String sipPublicUri;
	            			String imsPwd;
	            			String homeDomain;
	            			String imsProxy;
	            			String xdms;
	            			String xdmsPwd;
	            			String xdmsLogin;
	            			String chatConfUri;
	                        switch(index) {
	                        	case 0: // Test fest
			            			if (number.startsWith("+")) {
			            				number = number.substring(1);
			            			}
			            			homeDomain = "tb2.mchm.nsn-rdnet.com";
		            				sipPublicUri = number + "@" + homeDomain;
			            			imsPwd = "ims3-pwd";
			            			imsProxy = "213.146.177.134:5060";
			            			xdms = "213.146.177.172:8080/services";
			            			xdmsPwd = "password";
			            			xdmsLogin = "sip:"+ number + "@" + homeDomain;
			            			chatConfUri  = "sip:conference-factory@" + homeDomain;
			            			break;
			            		default:
			            			homeDomain = "domain.com";
		            				sipPublicUri = number + "@" + homeDomain;
			            			imsPwd = "";
			            			imsProxy = "127.0.0.1:5060";
			            			xdms = "127.0.0.1:8080/services";
			            			xdmsPwd = "";
			            			xdmsLogin = "sip:"+ number + "@" + homeDomain;
			            			chatConfUri  = "sip:conference-factory@" + homeDomain;
	            			}
	                        
	            			// Update UI & save date
	                    	RcsSettings.getInstance().setUserProfileImsUserName(number);
	                    	username.setSummary(number);
	            			username.setText(number);
	                    	RcsSettings.getInstance().setUserProfileImsDisplayName(number);
	            			displayName.setSummary(number);
	            			displayName.setText(number);
	            			RcsSettings.getInstance().setUserProfileImsPrivateId(sipPublicUri);
	            			privateId.setSummary(sipPublicUri);
	            			privateId.setText(sipPublicUri);
	                    	RcsSettings.getInstance().setUserProfileImsPassword(imsPwd);
	            	        userPassword.setSummary(imsPwd);
	            	        userPassword.setText(imsPwd);
	                    	RcsSettings.getInstance().setUserProfileImsDomain(homeDomain);
	            	        domain.setSummary(homeDomain);
	            	        domain.setText(homeDomain);
	                    	RcsSettings.getInstance().setUserProfileImsProxy(imsProxy);
	            	        proxyAddr.setSummary(imsProxy);
	            	        proxyAddr.setText(imsProxy);
	                    	RcsSettings.getInstance().setUserProfileXdmServer(xdms);
	            	        xdmAddr.setSummary(xdms);
	            	        xdmAddr.setText(xdms);
	                    	RcsSettings.getInstance().setUserProfileXdmLogin(xdmsLogin);
	            	        xdmLogin.setSummary(xdmsLogin);
	            	        xdmLogin.setText(xdmsLogin);
	                    	RcsSettings.getInstance().setUserProfileXdmPassword(xdmsPwd);
	            	        xdmPassword.setSummary(xdmsPwd);
	            	        xdmPassword.setText(xdmsPwd);
	                    	RcsSettings.getInstance().setUserProfileImConferenceUri(chatConfUri);
	            	        imConferenceUri.setSummary(chatConfUri);
	            	        imConferenceUri.setText(chatConfUri);
	            	        modified = true;
            	        }
	                })
	                .setNegativeButton(R.string.rcs_settings_label_cancel, null)
	                .create();
        }
        return null;
    }    
}
