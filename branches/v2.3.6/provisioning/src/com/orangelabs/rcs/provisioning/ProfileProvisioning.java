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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.orangelabs.rcs.provider.settings.RcsSettings;
import com.orangelabs.rcs.provider.settings.RcsSettingsData;

/**
 * End user profile parameters provisioning
 *
 * @author jexa7410
 */
public class ProfileProvisioning extends Activity {
	/**
	 * Dialog ID
	 */
	private static final int DIALOG_GENERATE_VALUE = 1;

	/**
	 * IMS authentication for mobile access
	 */
    private static final String[] MOBILE_IMS_AUTHENT = {
    	RcsSettingsData.GIBA_AUTHENT, RcsSettingsData.DIGEST_AUTHENT
    };

	/**
	 * IMS authentication for Wi-Fi access
	 */
    private static final String[] WIFI_IMS_AUTHENT = {
    	RcsSettingsData.DIGEST_AUTHENT
    };

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set layout
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.profile_provisioning);
	}

	@Override
	protected void onResume() {
		super.onResume();

		// Display profile parameters
		Spinner spinner = (Spinner)findViewById(R.id.ImsAuhtenticationProcedureForMobile);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, MOBILE_IMS_AUTHENT);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		if (RcsSettings.getInstance().getImsAuhtenticationProcedureForMobile().equals(MOBILE_IMS_AUTHENT[0])) {
			spinner.setSelection(0);
		} else {
			spinner.setSelection(1);
		}

		spinner = (Spinner)findViewById(R.id.ImsAuhtenticationProcedureForWifi);
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, WIFI_IMS_AUTHENT);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setSelection(0);

		EditText txt = (EditText)this.findViewById(R.id.ImsUsername);
		txt.setText(RcsSettings.getInstance().readParameter(RcsSettingsData.USERPROFILE_IMS_USERNAME));

		txt = (EditText)this.findViewById(R.id.ImsDisplayName);
		txt.setText(RcsSettings.getInstance().readParameter(RcsSettingsData.USERPROFILE_IMS_DISPLAY_NAME));

		txt = (EditText)this.findViewById(R.id.ImsPrivateId);
		txt.setText(RcsSettings.getInstance().readParameter(RcsSettingsData.USERPROFILE_IMS_PRIVATE_ID));

		txt = (EditText)this.findViewById(R.id.ImsPassword);
		txt.setText(RcsSettings.getInstance().readParameter(RcsSettingsData.USERPROFILE_IMS_PASSWORD));

		txt = (EditText)this.findViewById(R.id.ImsHomeDomain);
		txt.setText(RcsSettings.getInstance().readParameter(RcsSettingsData.USERPROFILE_IMS_HOME_DOMAIN));

        txt = (EditText) this.findViewById(R.id.ImsOutboundProxyAddrForMobile);
		txt.setText(RcsSettings.getInstance().readParameter(RcsSettingsData.USERPROFILE_IMS_PROXY_MOBILE));

        txt = (EditText) this.findViewById(R.id.ImsOutboundProxyAddrForWifi);
        txt.setText(RcsSettings.getInstance().readParameter(RcsSettingsData.USERPROFILE_IMS_PROXY_WIFI));

		txt = (EditText)this.findViewById(R.id.XdmServerAddr);
        txt.setText(RcsSettings.getInstance().readParameter(RcsSettingsData.USERPROFILE_XDM_SERVER));

		txt = (EditText)this.findViewById(R.id.XdmServerLogin);
        txt.setText(RcsSettings.getInstance().readParameter(RcsSettingsData.USERPROFILE_XDM_LOGIN));

		txt = (EditText)this.findViewById(R.id.XdmServerPassword);
        txt.setText(RcsSettings.getInstance().readParameter(RcsSettingsData.USERPROFILE_XDM_PASSWORD));

		txt = (EditText)this.findViewById(R.id.ImConferenceUri);
		txt.setText(RcsSettings.getInstance().readParameter(RcsSettingsData.USERPROFILE_IM_CONF_URI));

		txt = (EditText)this.findViewById(R.id.RcsApn);
		txt.setText(RcsSettings.getInstance().readParameter(RcsSettingsData.RCS_APN));

        CheckBox box = (CheckBox)findViewById(R.id.image_sharing);
        box.setChecked(Boolean.parseBoolean(RcsSettings.getInstance().readParameter(RcsSettingsData.CAPABILITY_IMAGE_SHARING)));

        box = (CheckBox)findViewById(R.id.video_sharing);
        box.setChecked(Boolean.parseBoolean(RcsSettings.getInstance().readParameter(RcsSettingsData.CAPABILITY_VIDEO_SHARING)));

        box = (CheckBox)findViewById(R.id.file_transfer);
        box.setChecked(Boolean.parseBoolean(RcsSettings.getInstance().readParameter(RcsSettingsData.CAPABILITY_FILE_TRANSFER)));

        box = (CheckBox)findViewById(R.id.im);
        box.setChecked(Boolean.parseBoolean(RcsSettings.getInstance().readParameter(RcsSettingsData.CAPABILITY_IM_SESSION)));

        box = (CheckBox)findViewById(R.id.cs_video);
        box.setChecked(Boolean.parseBoolean(RcsSettings.getInstance().readParameter(RcsSettingsData.CAPABILITY_CS_VIDEO)));

        box = (CheckBox)findViewById(R.id.presence_discovery);
        box.setChecked(Boolean.parseBoolean(RcsSettings.getInstance().readParameter(RcsSettingsData.CAPABILITY_PRESENCE_DISCOVERY)));

        box = (CheckBox)findViewById(R.id.social_presence);
        box.setChecked(Boolean.parseBoolean(RcsSettings.getInstance().readParameter(RcsSettingsData.CAPABILITY_SOCIAL_PRESENCE)));	
	}

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater=new MenuInflater(getApplicationContext());
		inflater.inflate(R.menu.profile_menu, menu);
		return true;
	}

    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_generate_profile:
                showDialog(DIALOG_GENERATE_VALUE);
				break;

			case R.id.menu_save:
		        // Save profile parameters
				Spinner spinner = (Spinner)findViewById(R.id.ImsAuhtenticationProcedureForMobile);
				RcsSettings.getInstance().writeParameter(RcsSettingsData.IMS_AUTHENT_PROCEDURE_MOBILE, (String)spinner.getSelectedItem());

				spinner = (Spinner)findViewById(R.id.ImsAuhtenticationProcedureForWifi);
				RcsSettings.getInstance().writeParameter(RcsSettingsData.IMS_AUTHENT_PROCEDURE_WIFI, (String)spinner.getSelectedItem());

				EditText txt = (EditText)this.findViewById(R.id.ImsUsername);
				RcsSettings.getInstance().writeParameter(RcsSettingsData.USERPROFILE_IMS_USERNAME, txt.getText().toString());

		        txt = (EditText)this.findViewById(R.id.ImsDisplayName);
		        RcsSettings.getInstance().writeParameter(RcsSettingsData.USERPROFILE_IMS_DISPLAY_NAME, txt.getText().toString());

		        txt = (EditText)this.findViewById(R.id.ImsPrivateId);
		        RcsSettings.getInstance().writeParameter(RcsSettingsData.USERPROFILE_IMS_PRIVATE_ID, txt.getText().toString());

		        txt = (EditText)this.findViewById(R.id.ImsPassword);
		        RcsSettings.getInstance().writeParameter(RcsSettingsData.USERPROFILE_IMS_PASSWORD, txt.getText().toString());

		        txt = (EditText)this.findViewById(R.id.ImsHomeDomain);
		        RcsSettings.getInstance().writeParameter(RcsSettingsData.USERPROFILE_IMS_HOME_DOMAIN, txt.getText().toString());

		        txt = (EditText)this.findViewById(R.id.ImsOutboundProxyAddrForMobile);
		        RcsSettings.getInstance().writeParameter(RcsSettingsData.USERPROFILE_IMS_PROXY_MOBILE, txt.getText().toString());

                txt = (EditText)this.findViewById(R.id.ImsOutboundProxyAddrForWifi);
                RcsSettings.getInstance().writeParameter(RcsSettingsData.USERPROFILE_IMS_PROXY_WIFI, txt.getText().toString());

				txt = (EditText)this.findViewById(R.id.XdmServerAddr);
				RcsSettings.getInstance().writeParameter(RcsSettingsData.USERPROFILE_XDM_SERVER, txt.getText().toString());

		        txt = (EditText)this.findViewById(R.id.XdmServerLogin);
		        RcsSettings.getInstance().writeParameter(RcsSettingsData.USERPROFILE_XDM_LOGIN, txt.getText().toString());

		        txt = (EditText)this.findViewById(R.id.XdmServerPassword);
		        RcsSettings.getInstance().writeParameter(RcsSettingsData.USERPROFILE_XDM_PASSWORD, txt.getText().toString());

		        txt = (EditText)this.findViewById(R.id.ImConferenceUri);
		        RcsSettings.getInstance().writeParameter(RcsSettingsData.USERPROFILE_IM_CONF_URI, txt.getText().toString());

		        txt = (EditText)this.findViewById(R.id.RcsApn);
		        RcsSettings.getInstance().writeParameter(RcsSettingsData.RCS_APN, txt.getText().toString());

		        // Save capabilities
		        CheckBox box = (CheckBox)findViewById(R.id.image_sharing);
		        RcsSettings.getInstance().writeParameter(RcsSettingsData.CAPABILITY_IMAGE_SHARING, Boolean.toString(box.isChecked()));

		        box = (CheckBox)findViewById(R.id.video_sharing);
		        RcsSettings.getInstance().writeParameter(RcsSettingsData.CAPABILITY_VIDEO_SHARING, Boolean.toString(box.isChecked()));

		        box = (CheckBox)findViewById(R.id.file_transfer);
		        RcsSettings.getInstance().writeParameter(RcsSettingsData.CAPABILITY_FILE_TRANSFER, Boolean.toString(box.isChecked()));

		        box = (CheckBox)findViewById(R.id.im);
		        RcsSettings.getInstance().writeParameter(RcsSettingsData.CAPABILITY_IM_SESSION, Boolean.toString(box.isChecked()));

		        box = (CheckBox)findViewById(R.id.cs_video);
		        RcsSettings.getInstance().writeParameter(RcsSettingsData.CAPABILITY_CS_VIDEO, Boolean.toString(box.isChecked()));

		        box = (CheckBox)findViewById(R.id.presence_discovery);
		        RcsSettings.getInstance().writeParameter(RcsSettingsData.CAPABILITY_PRESENCE_DISCOVERY, Boolean.toString(box.isChecked()));

		        box = (CheckBox)findViewById(R.id.social_presence);
		        RcsSettings.getInstance().writeParameter(RcsSettingsData.CAPABILITY_SOCIAL_PRESENCE, Boolean.toString(box.isChecked()));

                Toast.makeText(this, getString(R.string.label_reboot_service), Toast.LENGTH_LONG)
                        .show();
		        break;
		}
		return true;
	}

    @Override
    protected Dialog onCreateDialog(int id) {
        switch(id) {
	        case DIALOG_GENERATE_VALUE:
				LayoutInflater factory = LayoutInflater.from(this);
	            final View view = factory.inflate(R.layout.generate_profile_layout, null);
				EditText textEdit = (EditText)view.findViewById(R.id.msisdn);
	            textEdit.setText(RcsSettings.getInstance().getCountryCode());

	            final String[] platforms = {
                        "Default"
	            };
	            Spinner spinner = (Spinner)view.findViewById(R.id.ims);
	            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
	                    android.R.layout.simple_spinner_item, platforms);
	            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	            spinner.setAdapter(adapter);

	            return new AlertDialog.Builder(this)
	                .setTitle(R.string.label_generate_profile)
	                .setView(view)
	                .setPositiveButton(R.string.label_ok, new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int whichButton) {
	            	        // Generate default settings
	            			EditText textEdit = (EditText)view.findViewById(R.id.msisdn);
	            			String number = textEdit.getText().toString();
	        	            Spinner spinner = (Spinner)view.findViewById(R.id.ims);
	        	            int index = spinner.getSelectedItemPosition();

	            			String sipUri;
	            			String imsPwd;
	            			String homeDomain;
	            			String imsProxyForMobile;
                            String imsProxyForWifi;
	            			String xdms;
	            			String xdmsPwd;
	            			String xdmsLogin;
	            			String chatConfUri;
	                        switch(index) {
	                        	case 0:
			            			homeDomain = "domain.com";
		            				sipUri = number + "@" + homeDomain;
			            			imsPwd = "";
			            			imsProxyForMobile = "127.0.0.1:5060";
			            			imsProxyForWifi = "127.0.0.1:5060";
			            			xdms = "127.0.0.1:8080/services";
			            			xdmsPwd = "";
			            			xdmsLogin = "sip:" + number + "@" + homeDomain;
			            			chatConfUri  = "conference-factory";
			            		default:
			            			homeDomain = "domain.com";
		            				sipUri = number + "@" + homeDomain;
			            			imsPwd = "";
			            			imsProxyForMobile = "127.0.0.1:5060";
			            			imsProxyForWifi = "127.0.0.1:5060";
			            			xdms = "127.0.0.1:8080/services";
			            			xdmsPwd = "";
			            			xdmsLogin = "sip:" + number + "@" + homeDomain;
			            			chatConfUri  = "conference-factory";
	            			}

	            			// Update UI
	        				EditText txt = (EditText)ProfileProvisioning.this.findViewById(R.id.ImsUsername);
	        				txt.setText(number);
	        		        txt = (EditText)ProfileProvisioning.this.findViewById(R.id.ImsDisplayName);
	        				txt.setText(number);
	        		        txt = (EditText)ProfileProvisioning.this.findViewById(R.id.ImsPrivateId);
                            txt.setText(sipUri);
	        		        txt = (EditText)ProfileProvisioning.this.findViewById(R.id.ImsPassword);
	        		        txt.setText(imsPwd);
	        		        txt = (EditText)ProfileProvisioning.this.findViewById(R.id.ImsHomeDomain);
	        		        txt.setText(homeDomain);
	        		        txt = (EditText)ProfileProvisioning.this.findViewById(R.id.ImsOutboundProxyAddrForMobile);
                            txt.setText(imsProxyForMobile);
                            txt = (EditText)ProfileProvisioning.this.findViewById(R.id.ImsOutboundProxyAddrForWifi);
                            txt.setText(imsProxyForWifi);
	        				txt = (EditText)ProfileProvisioning.this.findViewById(R.id.XdmServerAddr);
	        				txt.setText(xdms);
	        		        txt = (EditText)ProfileProvisioning.this.findViewById(R.id.XdmServerLogin);
	        		        txt.setText(xdmsLogin);
	        		        txt = (EditText)ProfileProvisioning.this.findViewById(R.id.XdmServerPassword);
	        		        txt.setText(xdmsPwd);
	        		        txt = (EditText)ProfileProvisioning.this.findViewById(R.id.ImConferenceUri);
	        		        txt.setText(chatConfUri);
            	        }
	                })
	                .setNegativeButton(R.string.label_cancel, null)
	                .create();
        }
        return null;
    }
}