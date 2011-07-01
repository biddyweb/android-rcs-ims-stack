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
import android.content.ContentResolver;
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
import java.util.Map;

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

    /**
	 * Content resolver
	 */
	private ContentResolver cr;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set layout
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.profile_provisioning);

        // Set database content resolver
        this.cr = getContentResolver();

	}

	@Override
	protected void onResume() {
		super.onResume();

		// Get settings from database
		Map<String, String> settings = RcsSettings.getInstance().dump();

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
		txt.setText(settings.get("ImsUsername"));

		txt = (EditText)this.findViewById(R.id.ImsDisplayName);
		txt.setText(settings.get("ImsDisplayName"));

		txt = (EditText)this.findViewById(R.id.ImsPrivateId);
		txt.setText(settings.get("ImsPrivateId"));

		txt = (EditText)this.findViewById(R.id.ImsPassword);
		txt.setText(settings.get("ImsPassword"));

		txt = (EditText)this.findViewById(R.id.ImsHomeDomain);
		txt.setText(settings.get("ImsHomeDomain"));

		txt = (EditText)this.findViewById(R.id.ImsOutboundProxyAddr);
		txt.setText(settings.get("ImsOutboundProxyAddrForMobile"));

		txt = (EditText)this.findViewById(R.id.XdmServerAddr);
        txt.setText(settings.get("XdmServerAddr"));

		txt = (EditText)this.findViewById(R.id.XdmServerLogin);
        txt.setText(settings.get("XdmServerLogin"));

		txt = (EditText)this.findViewById(R.id.XdmServerPassword);
        txt.setText(settings.get("XdmServerPassword"));

		txt = (EditText)this.findViewById(R.id.ImConferenceUri);
		txt.setText(settings.get("ImConferenceUri"));

		txt = (EditText)this.findViewById(R.id.CountryCode);
		txt.setText(settings.get("CountryCode"));

		txt = (EditText)this.findViewById(R.id.RcsApn);
		txt.setText(settings.get("RcsApn"));

		// Display capabilities
        CheckBox box = (CheckBox)findViewById(R.id.image_sharing);
        box.setChecked(Boolean.parseBoolean(settings.get("CapabilityImageShare")));

        box = (CheckBox)findViewById(R.id.video_sharing);
        box.setChecked(Boolean.parseBoolean(settings.get("CapabilityVideoShare")));

        box = (CheckBox)findViewById(R.id.file_transfer);
        box.setChecked(Boolean.parseBoolean(settings.get("CapabilityFileTransfer")));

        box = (CheckBox)findViewById(R.id.im);
        box.setChecked(Boolean.parseBoolean(settings.get("CapabilityImSession")));

        box = (CheckBox)findViewById(R.id.cs_video);
        box.setChecked(Boolean.parseBoolean(settings.get("CapabilityCsVideo")));

        box = (CheckBox)findViewById(R.id.presence_discovery);
        box.setChecked(Boolean.parseBoolean(settings.get("CapabilityPresenceDiscovery")));

        box = (CheckBox)findViewById(R.id.social_presence);
        box.setChecked(Boolean.parseBoolean(settings.get("CapabilitySocialPresence")));
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
		        // Save UI parameters
				Spinner spinner = (Spinner)findViewById(R.id.ImsAuhtenticationProcedureForMobile);
				Provisioning.writeParameter(cr, "ImsAuhtenticationProcedureForMobile", (String)spinner.getSelectedItem());

				spinner = (Spinner)findViewById(R.id.ImsAuhtenticationProcedureForWifi);
				Provisioning.writeParameter(cr, "ImsAuhtenticationProcedureForWifi", (String)spinner.getSelectedItem());

				EditText txt = (EditText)this.findViewById(R.id.ImsUsername);
				Provisioning.writeParameter(cr, "ImsUsername", txt.getText().toString());

		        txt = (EditText)this.findViewById(R.id.ImsDisplayName);
				Provisioning.writeParameter(cr, "ImsDisplayName", txt.getText().toString());

		        txt = (EditText)this.findViewById(R.id.ImsPrivateId);
				Provisioning.writeParameter(cr, "ImsPrivateId", txt.getText().toString());

		        txt = (EditText)this.findViewById(R.id.ImsPassword);
				Provisioning.writeParameter(cr, "ImsPassword", txt.getText().toString());

		        txt = (EditText)this.findViewById(R.id.ImsHomeDomain);
				Provisioning.writeParameter(cr, "ImsHomeDomain", txt.getText().toString());

		        txt = (EditText)this.findViewById(R.id.ImsOutboundProxyAddr);
				Provisioning.writeParameter(cr, "ImsOutboundProxyAddrForMobile", txt.getText().toString());
				Provisioning.writeParameter(cr, "ImsOutboundProxyAddrForWifi", txt.getText().toString());

				txt = (EditText)this.findViewById(R.id.XdmServerAddr);
				Provisioning.writeParameter(cr, "XdmServerAddr", txt.getText().toString());

		        txt = (EditText)this.findViewById(R.id.XdmServerLogin);
				Provisioning.writeParameter(cr, "XdmServerLogin", txt.getText().toString());

		        txt = (EditText)this.findViewById(R.id.XdmServerPassword);
				Provisioning.writeParameter(cr, "XdmServerPassword", txt.getText().toString());

		        txt = (EditText)this.findViewById(R.id.ImConferenceUri);
		        Provisioning.writeParameter(cr, "ImConferenceUri", txt.getText().toString());

		        txt = (EditText)this.findViewById(R.id.CountryCode);
		        String value = txt.getText().toString();
		        if ((value.length() > 0) && !value.startsWith("+")) {
		        	value = "+" + value;
		        }
		        Provisioning.writeParameter(cr, "CountryCode", value);

		        txt = (EditText)this.findViewById(R.id.RcsApn);
		        Provisioning.writeParameter(cr, "RcsApn", txt.getText().toString());

		        // Save capabilities
		        CheckBox box = (CheckBox)findViewById(R.id.image_sharing);
		        Provisioning.writeParameter(cr, "CapabilityImageShare", Boolean.toString(box.isChecked()));

		        box = (CheckBox)findViewById(R.id.video_sharing);
		        Provisioning.writeParameter(cr, "CapabilityVideoShare", Boolean.toString(box.isChecked()));

		        box = (CheckBox)findViewById(R.id.file_transfer);
		        Provisioning.writeParameter(cr, "CapabilityFileTransfer", Boolean.toString(box.isChecked()));

		        box = (CheckBox)findViewById(R.id.im);
		        Provisioning.writeParameter(cr, "CapabilityImSession", Boolean.toString(box.isChecked()));

		        box = (CheckBox)findViewById(R.id.cs_video);
		        Provisioning.writeParameter(cr, "CapabilityCsVideo", Boolean.toString(box.isChecked()));

		        box = (CheckBox)findViewById(R.id.presence_discovery);
	        	Provisioning.writeParameter(cr, "CapabilityPresenceDiscovery", Boolean.toString(box.isChecked()));

		        box = (CheckBox)findViewById(R.id.social_presence);
		        Provisioning.writeParameter(cr, "CapabilitySocialPresence", Boolean.toString(box.isChecked()));

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
                        "Your platform"
	            };
	            Spinner spinner = (Spinner)view.findViewById(R.id.ims);
	            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
	                    android.R.layout.simple_spinner_item, platforms);
	            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	            spinner.setAdapter(adapter);

	            return new AlertDialog.Builder(this)
	                .setTitle(R.string.rcs_settings_title_generate_profile)
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
	            			String imsProxy;
	            			String xdms;
	            			String xdmsPwd;
	            			String xdmsLogin;
	            			String chatConfUri;
	                        switch(index) {
	                        	case 0: // Add your own platform config here
			            			homeDomain = "domain.com";
		            				sipUri = number + "@" + homeDomain;
			            			imsPwd = "";
			            			imsProxy = "127.0.0.1:5060";
			            			xdms = "127.0.0.1:8080/services";
			            			xdmsPwd = "";
			            			xdmsLogin = "sip:" + number + "@" + homeDomain;
			            			chatConfUri  = "conference-factory";
			            		default:
			            			homeDomain = "domain.com";
		            				sipUri = number + "@" + homeDomain;
			            			imsPwd = "";
			            			imsProxy = "127.0.0.1:5060";
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
	        		        txt = (EditText)ProfileProvisioning.this.findViewById(R.id.ImsOutboundProxyAddr);
                            txt.setText(imsProxy);
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
