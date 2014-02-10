/*******************************************************************************
 * Software Name : RCS IMS Stack
 *
 * Copyright (C) 2010 France Telecom S.A.
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

package com.orangelabs.rcs.provisioning.local;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.orangelabs.rcs.R;
import com.orangelabs.rcs.provider.settings.RcsSettings;
import com.orangelabs.rcs.provider.settings.RcsSettingsData;
import com.orangelabs.rcs.provisioning.ProvisioningParser;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * End user profile parameters provisioning
 *
 * @author jexa7410
 */
public class ProfileProvisioning extends Activity {
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
	 * IMS authentication for Wi-Fi access
	 */
    private static final String[] GSMA_RELEASE = {
    	"Albatros", "Blackbird", "Crane"
    };
    
    private static Logger logger = Logger.getLogger(ProfileProvisioning.class.getSimpleName());
    private static final int FILE_SELECT_CODE = 0;
    private String mInputedUserPhoneNumber = null;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set layout
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.rcs_provisioning_profile);
        
		// Set buttons callback
        Button btn = (Button)findViewById(R.id.save_btn);
        btn.setOnClickListener(saveBtnListener);
        btn = (Button)findViewById(R.id.gen_btn);
        btn.setOnClickListener(genBtnListener);
	}

	@Override
	protected void onResume() {
		super.onResume();
		updateProfileProvisioningUI();
	}
	
	/**
	 * Update Profile Provisioning UI
	 */
	private void updateProfileProvisioningUI() {
		// Display parameters
		Spinner spinner = (Spinner)findViewById(R.id.ImsAuhtenticationProcedureForMobile);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(ProfileProvisioning.this, android.R.layout.simple_spinner_item, MOBILE_IMS_AUTHENT);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		if (RcsSettings.getInstance().getImsAuhtenticationProcedureForMobile().equals(MOBILE_IMS_AUTHENT[0])) {
			spinner.setSelection(0);
		} else {
			spinner.setSelection(1);
		}

		spinner = (Spinner)findViewById(R.id.ImsAuhtenticationProcedureForWifi);
		adapter = new ArrayAdapter<String>(ProfileProvisioning.this, android.R.layout.simple_spinner_item, WIFI_IMS_AUTHENT);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setSelection(0);

		EditText txt = (EditText)ProfileProvisioning.this.findViewById(R.id.ImsUsername);
		txt.setText(RcsSettings.getInstance().readParameter(RcsSettingsData.USERPROFILE_IMS_USERNAME));

		txt = (EditText)ProfileProvisioning.this.findViewById(R.id.ImsDisplayName);
		txt.setText(RcsSettings.getInstance().readParameter(RcsSettingsData.USERPROFILE_IMS_DISPLAY_NAME));

		txt = (EditText)ProfileProvisioning.this.findViewById(R.id.ImsHomeDomain);
		txt.setText(RcsSettings.getInstance().readParameter(RcsSettingsData.USERPROFILE_IMS_HOME_DOMAIN));

		txt = (EditText)ProfileProvisioning.this.findViewById(R.id.ImsPrivateId);
		txt.setText(RcsSettings.getInstance().readParameter(RcsSettingsData.USERPROFILE_IMS_PRIVATE_ID));

		txt = (EditText)ProfileProvisioning.this.findViewById(R.id.ImsPassword);
		txt.setText(RcsSettings.getInstance().readParameter(RcsSettingsData.USERPROFILE_IMS_PASSWORD));

		txt = (EditText)ProfileProvisioning.this.findViewById(R.id.ImsRealm);
		txt.setText(RcsSettings.getInstance().readParameter(RcsSettingsData.USERPROFILE_IMS_REALM));

        txt = (EditText)ProfileProvisioning.this.findViewById(R.id.ImsOutboundProxyAddrForMobile);
		txt.setText(RcsSettings.getInstance().readParameter(RcsSettingsData.IMS_PROXY_ADDR_MOBILE));

        txt = (EditText)ProfileProvisioning.this.findViewById(R.id.ImsOutboundProxyPortForMobile);
		txt.setText(RcsSettings.getInstance().readParameter(RcsSettingsData.IMS_PROXY_PORT_MOBILE));

		txt = (EditText)ProfileProvisioning.this.findViewById(R.id.ImsOutboundProxyAddrForWifi);
        txt.setText(RcsSettings.getInstance().readParameter(RcsSettingsData.IMS_PROXY_ADDR_WIFI));

		txt = (EditText)ProfileProvisioning.this.findViewById(R.id.ImsOutboundProxyPortForWifi);
        txt.setText(RcsSettings.getInstance().readParameter(RcsSettingsData.IMS_PROXY_PORT_WIFI));

        txt = (EditText)ProfileProvisioning.this.findViewById(R.id.XdmServerAddr);
        txt.setText(RcsSettings.getInstance().readParameter(RcsSettingsData.XDM_SERVER));

		txt = (EditText)ProfileProvisioning.this.findViewById(R.id.XdmServerLogin);
        txt.setText(RcsSettings.getInstance().readParameter(RcsSettingsData.XDM_LOGIN));

		txt = (EditText)ProfileProvisioning.this.findViewById(R.id.XdmServerPassword);
        txt.setText(RcsSettings.getInstance().readParameter(RcsSettingsData.XDM_PASSWORD));
        
        txt = (EditText)ProfileProvisioning.this.findViewById(R.id.FtHttpServerAddr);
        txt.setText(RcsSettings.getInstance().readParameter(RcsSettingsData.FT_HTTP_SERVER));

		txt = (EditText)ProfileProvisioning.this.findViewById(R.id.FtHttpServerLogin);
        txt.setText(RcsSettings.getInstance().readParameter(RcsSettingsData.FT_HTTP_LOGIN));

		txt = (EditText)ProfileProvisioning.this.findViewById(R.id.FtHttpServerPassword);
        txt.setText(RcsSettings.getInstance().readParameter(RcsSettingsData.FT_HTTP_PASSWORD));
        
		txt = (EditText)ProfileProvisioning.this.findViewById(R.id.ImConferenceUri);
		txt.setText(RcsSettings.getInstance().readParameter(RcsSettingsData.IM_CONF_URI));

        txt = (EditText)ProfileProvisioning.this.findViewById(R.id.EndUserConfReqUri);
		txt.setText(RcsSettings.getInstance().readParameter(RcsSettingsData.ENDUSER_CONFIRMATION_URI));

		txt = (EditText)ProfileProvisioning.this.findViewById(R.id.RcsApn);
		txt.setText(RcsSettings.getInstance().readParameter(RcsSettingsData.RCS_APN));

		txt = (EditText)ProfileProvisioning.this.findViewById(R.id.CountryCode);
		txt.setText(RcsSettings.getInstance().readParameter(RcsSettingsData.COUNTRY_CODE));

		txt = (EditText)ProfileProvisioning.this.findViewById(R.id.CountryAreaCode);
		txt.setText(RcsSettings.getInstance().readParameter(RcsSettingsData.COUNTRY_AREA_CODE));

		CheckBox box = (CheckBox)findViewById(R.id.image_sharing);
        box.setChecked(Boolean.parseBoolean(RcsSettings.getInstance().readParameter(RcsSettingsData.CAPABILITY_IMAGE_SHARING)));

        box = (CheckBox)findViewById(R.id.video_sharing);
        box.setChecked(Boolean.parseBoolean(RcsSettings.getInstance().readParameter(RcsSettingsData.CAPABILITY_VIDEO_SHARING)));

        box = (CheckBox)findViewById(R.id.file_transfer);
        box.setChecked(Boolean.parseBoolean(RcsSettings.getInstance().readParameter(RcsSettingsData.CAPABILITY_FILE_TRANSFER)));

        box = (CheckBox)findViewById(R.id.file_transfer_http);
        box.setChecked(Boolean.parseBoolean(RcsSettings.getInstance().readParameter(RcsSettingsData.CAPABILITY_FILE_TRANSFER_HTTP)));

        box = (CheckBox)findViewById(R.id.im);
        box.setChecked(Boolean.parseBoolean(RcsSettings.getInstance().readParameter(RcsSettingsData.CAPABILITY_IM_SESSION)));
       
        box = (CheckBox)findViewById(R.id.im_group);
        box.setChecked(Boolean.parseBoolean(RcsSettings.getInstance().readParameter(RcsSettingsData.CAPABILITY_IM_GROUP_SESSION)));
        
        box = (CheckBox)findViewById(R.id.ipvoicecall);
        box.setChecked(Boolean.parseBoolean(RcsSettings.getInstance().readParameter(RcsSettingsData.CAPABILITY_IP_VOICE_CALL)));
        
        box = (CheckBox)findViewById(R.id.ipvideocall);
        box.setChecked(Boolean.parseBoolean(RcsSettings.getInstance().readParameter(RcsSettingsData.CAPABILITY_IP_VIDEO_CALL)));

        box = (CheckBox)findViewById(R.id.cs_video);
        box.setChecked(Boolean.parseBoolean(RcsSettings.getInstance().readParameter(RcsSettingsData.CAPABILITY_CS_VIDEO)));

        box = (CheckBox)findViewById(R.id.presence_discovery);
        box.setChecked(Boolean.parseBoolean(RcsSettings.getInstance().readParameter(RcsSettingsData.CAPABILITY_PRESENCE_DISCOVERY)));

        box = (CheckBox)findViewById(R.id.social_presence);
        box.setChecked(Boolean.parseBoolean(RcsSettings.getInstance().readParameter(RcsSettingsData.CAPABILITY_SOCIAL_PRESENCE)));
        
        box = (CheckBox)findViewById(R.id.geolocation_push);
        box.setChecked(Boolean.parseBoolean(RcsSettings.getInstance().readParameter(RcsSettingsData.CAPABILITY_GEOLOCATION_PUSH)));
        
        box = (CheckBox)findViewById(R.id.file_transfer_thumbnail);
        box.setChecked(Boolean.parseBoolean(RcsSettings.getInstance().readParameter(RcsSettingsData.CAPABILITY_FILE_TRANSFER_THUMBNAIL)));	

        box = (CheckBox)findViewById(R.id.file_transfer_sf);
        box.setChecked(Boolean.parseBoolean(RcsSettings.getInstance().readParameter(RcsSettingsData.CAPABILITY_FILE_TRANSFER_SF)));	

        box = (CheckBox)findViewById(R.id.group_chat_sf);
        box.setChecked(Boolean.parseBoolean(RcsSettings.getInstance().readParameter(RcsSettingsData.CAPABILITY_GROUP_CHAT_SF)));
        
        box = (CheckBox)findViewById(R.id.sip_automata);
        box.setChecked(Boolean.parseBoolean(RcsSettings.getInstance().readParameter(RcsSettingsData.CAPABILITY_SIP_AUTOMATA)));
        
		spinner = (Spinner)findViewById(R.id.GsmaRelease);
		adapter = new ArrayAdapter<String>(ProfileProvisioning.this, android.R.layout.simple_spinner_item, GSMA_RELEASE);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setSelection(RcsSettings.getInstance().getGsmaRelease());	
	}

    /**
     * Save button listener
     */
    private OnClickListener saveBtnListener = new OnClickListener() {
        public void onClick(View v) {
	        // Save parameters
        	save();
        }
    };
    
    /**
     * Save parameters into RCS Settings provider
     */
    private void save() {
		Spinner spinner = (Spinner)findViewById(R.id.ImsAuhtenticationProcedureForMobile);
		RcsSettings.getInstance().writeParameter(RcsSettingsData.IMS_AUTHENT_PROCEDURE_MOBILE, (String)spinner.getSelectedItem());

		spinner = (Spinner)findViewById(R.id.ImsAuhtenticationProcedureForWifi);
		RcsSettings.getInstance().writeParameter(RcsSettingsData.IMS_AUTHENT_PROCEDURE_WIFI, (String)spinner.getSelectedItem());

		EditText txt = (EditText)this.findViewById(R.id.ImsUsername);
		RcsSettings.getInstance().writeParameter(RcsSettingsData.USERPROFILE_IMS_USERNAME, txt.getText().toString());

        txt = (EditText)this.findViewById(R.id.ImsDisplayName);
        RcsSettings.getInstance().writeParameter(RcsSettingsData.USERPROFILE_IMS_DISPLAY_NAME, txt.getText().toString());

        txt = (EditText)this.findViewById(R.id.ImsHomeDomain);
        RcsSettings.getInstance().writeParameter(RcsSettingsData.USERPROFILE_IMS_HOME_DOMAIN, txt.getText().toString());

        txt = (EditText)this.findViewById(R.id.ImsPrivateId);
        RcsSettings.getInstance().writeParameter(RcsSettingsData.USERPROFILE_IMS_PRIVATE_ID, txt.getText().toString());

        txt = (EditText)this.findViewById(R.id.ImsPassword);
        RcsSettings.getInstance().writeParameter(RcsSettingsData.USERPROFILE_IMS_PASSWORD, txt.getText().toString());

        txt = (EditText)this.findViewById(R.id.ImsRealm);
        RcsSettings.getInstance().writeParameter(RcsSettingsData.USERPROFILE_IMS_REALM, txt.getText().toString());

        txt = (EditText)this.findViewById(R.id.ImsOutboundProxyAddrForMobile);
        RcsSettings.getInstance().writeParameter(RcsSettingsData.IMS_PROXY_ADDR_MOBILE, txt.getText().toString());

        txt = (EditText)this.findViewById(R.id.ImsOutboundProxyPortForMobile);
        RcsSettings.getInstance().writeParameter(RcsSettingsData.IMS_PROXY_PORT_MOBILE, txt.getText().toString());

        txt = (EditText)this.findViewById(R.id.ImsOutboundProxyAddrForWifi);
        RcsSettings.getInstance().writeParameter(RcsSettingsData.IMS_PROXY_ADDR_WIFI, txt.getText().toString());

        txt = (EditText)this.findViewById(R.id.ImsOutboundProxyPortForWifi);
        RcsSettings.getInstance().writeParameter(RcsSettingsData.IMS_PROXY_PORT_WIFI, txt.getText().toString());

        txt = (EditText)this.findViewById(R.id.XdmServerAddr);
		RcsSettings.getInstance().writeParameter(RcsSettingsData.XDM_SERVER, txt.getText().toString());

        txt = (EditText)this.findViewById(R.id.XdmServerLogin);
        RcsSettings.getInstance().writeParameter(RcsSettingsData.XDM_LOGIN, txt.getText().toString());

        txt = (EditText)this.findViewById(R.id.XdmServerPassword);
        RcsSettings.getInstance().writeParameter(RcsSettingsData.XDM_PASSWORD, txt.getText().toString());

        txt = (EditText)this.findViewById(R.id.FtHttpServerAddr);
		RcsSettings.getInstance().writeParameter(RcsSettingsData.FT_HTTP_SERVER, txt.getText().toString());

        txt = (EditText)this.findViewById(R.id.FtHttpServerLogin);
        RcsSettings.getInstance().writeParameter(RcsSettingsData.FT_HTTP_LOGIN, txt.getText().toString());

        txt = (EditText)this.findViewById(R.id.FtHttpServerPassword);
        RcsSettings.getInstance().writeParameter(RcsSettingsData.FT_HTTP_PASSWORD, txt.getText().toString());

        
        txt = (EditText)this.findViewById(R.id.ImConferenceUri);
        RcsSettings.getInstance().writeParameter(RcsSettingsData.IM_CONF_URI, txt.getText().toString());

        txt = (EditText)this.findViewById(R.id.EndUserConfReqUri);
        RcsSettings.getInstance().writeParameter(RcsSettingsData.ENDUSER_CONFIRMATION_URI, txt.getText().toString());

        txt = (EditText)this.findViewById(R.id.RcsApn);
        RcsSettings.getInstance().writeParameter(RcsSettingsData.RCS_APN, txt.getText().toString());

        txt = (EditText)this.findViewById(R.id.CountryCode);
        RcsSettings.getInstance().writeParameter(RcsSettingsData.COUNTRY_CODE, txt.getText().toString());

        txt = (EditText)this.findViewById(R.id.CountryAreaCode);
        RcsSettings.getInstance().writeParameter(RcsSettingsData.COUNTRY_AREA_CODE, txt.getText().toString());

        // Save capabilities
        CheckBox box = (CheckBox)findViewById(R.id.image_sharing);
        RcsSettings.getInstance().writeParameter(RcsSettingsData.CAPABILITY_IMAGE_SHARING, Boolean.toString(box.isChecked()));

        box = (CheckBox)findViewById(R.id.video_sharing);
        RcsSettings.getInstance().writeParameter(RcsSettingsData.CAPABILITY_VIDEO_SHARING, Boolean.toString(box.isChecked()));

        box = (CheckBox)findViewById(R.id.file_transfer);
        RcsSettings.getInstance().writeParameter(RcsSettingsData.CAPABILITY_FILE_TRANSFER, Boolean.toString(box.isChecked()));

        box = (CheckBox)findViewById(R.id.file_transfer_http);
        RcsSettings.getInstance().writeParameter(RcsSettingsData.CAPABILITY_FILE_TRANSFER_HTTP, Boolean.toString(box.isChecked()));

        box = (CheckBox)findViewById(R.id.im);
        RcsSettings.getInstance().writeParameter(RcsSettingsData.CAPABILITY_IM_SESSION, Boolean.toString(box.isChecked()));

        box = (CheckBox)findViewById(R.id.im_group);
        RcsSettings.getInstance().writeParameter(RcsSettingsData.CAPABILITY_IM_GROUP_SESSION, Boolean.toString(box.isChecked()));
        
        box = (CheckBox)findViewById(R.id.ipvoicecall);
        RcsSettings.getInstance().writeParameter(RcsSettingsData.CAPABILITY_IP_VOICE_CALL, Boolean.toString(box.isChecked()));
        
        box = (CheckBox)findViewById(R.id.ipvideocall);
        RcsSettings.getInstance().writeParameter(RcsSettingsData.CAPABILITY_IP_VIDEO_CALL, Boolean.toString(box.isChecked()));
        
        box = (CheckBox)findViewById(R.id.cs_video);
        RcsSettings.getInstance().writeParameter(RcsSettingsData.CAPABILITY_CS_VIDEO, Boolean.toString(box.isChecked()));

        box = (CheckBox)findViewById(R.id.presence_discovery);
        RcsSettings.getInstance().writeParameter(RcsSettingsData.CAPABILITY_PRESENCE_DISCOVERY, Boolean.toString(box.isChecked()));

        box = (CheckBox)findViewById(R.id.social_presence);
        RcsSettings.getInstance().writeParameter(RcsSettingsData.CAPABILITY_SOCIAL_PRESENCE, Boolean.toString(box.isChecked()));

        box = (CheckBox)findViewById(R.id.geolocation_push);
        RcsSettings.getInstance().writeParameter(RcsSettingsData.CAPABILITY_GEOLOCATION_PUSH, Boolean.toString(box.isChecked()));
        
        box = (CheckBox)findViewById(R.id.file_transfer_thumbnail);
        RcsSettings.getInstance().writeParameter(RcsSettingsData.CAPABILITY_FILE_TRANSFER_THUMBNAIL, Boolean.toString(box.isChecked()));
        
        box = (CheckBox)findViewById(R.id.file_transfer_sf);
        RcsSettings.getInstance().writeParameter(RcsSettingsData.CAPABILITY_FILE_TRANSFER_SF, Boolean.toString(box.isChecked()));

        box = (CheckBox)findViewById(R.id.group_chat_sf);
        RcsSettings.getInstance().writeParameter(RcsSettingsData.CAPABILITY_GROUP_CHAT_SF, Boolean.toString(box.isChecked()));

        box = (CheckBox)findViewById(R.id.sip_automata);
        RcsSettings.getInstance().writeParameter(RcsSettingsData.CAPABILITY_SIP_AUTOMATA, Boolean.toString(box.isChecked()));
        
		spinner = (Spinner)findViewById(R.id.GsmaRelease);
		RcsSettings.getInstance().setGsmaRelease(""+spinner.getSelectedItemPosition() );
        
        Toast.makeText(this, getString(R.string.label_reboot_service), Toast.LENGTH_LONG).show();    	
    }
    
    /**
     * Generate profile button listener
     */
    private OnClickListener genBtnListener = new OnClickListener() {
        public void onClick(View v) {
	        // Load the user profile
        	loadProfile();
        }
    };
    
	/**
	 * Load the user profile
	 */
	private void loadProfile() {
		LayoutInflater factory = LayoutInflater.from(this);
		final View view = factory.inflate(R.layout.rcs_provisioning_generate_profile, null);
		EditText textEdit = (EditText) view.findViewById(R.id.msisdn);
		textEdit.setText(RcsSettings.getInstance().getCountryCode());

		AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle(R.string.label_generate_profile).setView(view)
				.setNegativeButton(R.string.label_cancel, null)
				.setPositiveButton(R.string.label_ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						EditText textEdit = (EditText) view.findViewById(R.id.msisdn);
						mInputedUserPhoneNumber = textEdit.getText().toString();
						showFileChooser();
					}
				});
		AlertDialog dialog = builder.create();
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
	}

	/**
	 * Select a text file for the provisioning
	 */
	private void showFileChooser() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("text/plain");
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		try {
			Toast.makeText(ProfileProvisioning.this, getString(R.string.label_choose_xml), Toast.LENGTH_LONG).show();
			startActivityForResult(Intent.createChooser(intent, "Select provisioning XML"), FILE_SELECT_CODE);
		} catch (android.content.ActivityNotFoundException ex) {
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case FILE_SELECT_CODE:
			if (resultCode == Activity.RESULT_OK) {
				String filePath = getFilePath(this, data.getData());
				String mXMLFileContent = getFileContent(filePath);
				if (mXMLFileContent != null) {
					if (logger.isActivated()) {
						logger.debug("Selection of provisioning file: "+filePath);
					}
					ProvisionTask mProvisionTask = new ProvisionTask();
					mProvisionTask.execute(mXMLFileContent, mInputedUserPhoneNumber);
				} else {
					Toast.makeText(ProfileProvisioning.this, getString(R.string.label_load_failed), Toast.LENGTH_LONG).show();
				}
			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * Read a text file and convert it into a string
	 * 
	 * @param filePath
	 *            the file path
	 * @return the result string
	 */
	private String getFileContent(String filePath) {
		if (filePath == null)
			return null;
		// Get the text file
		File file = new File(filePath);

		// Read text from file
		StringBuilder text = new StringBuilder();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(file));
			String line;

			while ((line = br.readLine()) != null) {
				text.append(line);
				text.append('\n');
			}
			return text.toString();

		} catch (Exception e) {
			if (logger.isActivated()) {
				logger.error("Error reading file content: " + e.getClass().getName() + " " + e.getMessage(), e);
			}
		} finally {
			if (br != null)
				try {
					br.close();
				} catch (IOException e) {
				}
		}
		return null;
	}

	/**
	 * Get the file path from URI
	 * 
	 * @param context
	 *            the context
	 * @param uri
	 *            the URI
	 * @return the File path
	 */
	private String getFilePath(Context context, Uri uri) {
		if ("content".equalsIgnoreCase(uri.getScheme())) {
			String[] projection = { "_data" };
			Cursor cursor = null;

			try {
				cursor = context.getContentResolver().query(uri, projection, null, null, null);
				int column_index = cursor.getColumnIndexOrThrow("_data");
				if (cursor.moveToFirst()) {
					return cursor.getString(column_index);
				}
			} catch (Exception e) {
			} finally {
				if (cursor != null)
					cursor.close();
			}
		} else if ("file".equalsIgnoreCase(uri.getScheme())) {
			return uri.getPath();
		}

		return null;
	}
	
	/**
	 * Asynchronous Tasks that loads the provisioning file.
	 */
	private class ProvisionTask extends AsyncTask<String, Void, Boolean> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(String... params) {
			String UserPhoneNumber = params[1];
			String mXMLFileContent = params[0];
			return createProvisioning(mXMLFileContent, UserPhoneNumber);
		}

		/**
		 * Parse the provisioning data then save it into RCS settings provider
		 * 
		 * @param mXMLFileContent
		 *            the XML file containing provisioning data
		 * @param userPhoneNumber
		 *            the user phone number
		 * @return true if loading the provisioning is successful
		 */
		private Boolean createProvisioning(String mXMLFileContent, String userPhoneNumber) {
			ProvisioningParser parser = new ProvisioningParser(mXMLFileContent);
			// Save GSMA release set into the provider
			int gsmaRelease = RcsSettings.getInstance().getGsmaRelease();
			if (parser.parse(gsmaRelease)) {
				// Customize provisioning data with user phone number
				RcsSettings.getInstance().writeParameter(RcsSettingsData.USERPROFILE_IMS_USERNAME, userPhoneNumber);
				RcsSettings.getInstance().writeParameter(RcsSettingsData.USERPROFILE_IMS_DISPLAY_NAME, userPhoneNumber);
				String homeDomain = RcsSettings.getInstance().readParameter(RcsSettingsData.USERPROFILE_IMS_HOME_DOMAIN);
				String sipUri = userPhoneNumber + "@" + homeDomain;
				RcsSettings.getInstance().writeParameter(RcsSettingsData.USERPROFILE_IMS_PRIVATE_ID, sipUri);
				RcsSettings.getInstance().writeParameter(RcsSettingsData.FT_HTTP_LOGIN, sipUri);
				return true;
			} else {
				if (logger.isActivated()) {
					logger.error("Can't parse provisioning document");
				}
				// Restore GSMA release saved before parsing of the provisioning
				RcsSettings.getInstance().setGsmaRelease("" + gsmaRelease);
				return false;
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			updateProfileProvisioningUI();
			if (result)
				Toast.makeText(ProfileProvisioning.this, getString(R.string.label_reboot_service), Toast.LENGTH_LONG).show();
			else
				Toast.makeText(ProfileProvisioning.this, getString(R.string.label_parse_failed), Toast.LENGTH_LONG).show();
		}
	}
}
