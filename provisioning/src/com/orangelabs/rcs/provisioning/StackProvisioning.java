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

import javax.sip.ListeningPoint;

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
import android.widget.Toast;

import com.orangelabs.rcs.provider.settings.RcsSettings;
import com.orangelabs.rcs.provider.settings.RcsSettingsData;

/**
 * Stack parameters provisioning
 * 
 * @author jexa7410
 */
public class StackProvisioning extends Activity {
	/**
	 * SIP protocol
	 */
    private static final String[] SIP_PROTOCOL = {
    	ListeningPoint.UDP, ListeningPoint.TCP
    };
    
	/**
	 * Network access
	 */
    private static final String[] NETWORK_ACCESS = {
    	"All networks", "Mobile only", "Wi-Fi only"
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
        setContentView(R.layout.stack_provisioning);
        
    	// Set database content resolver
        this.cr = getContentResolver();

        
    }

    @Override
    protected void onResume() {
    	super.onResume();
    	
    	// Get settings from database
        Map<String, String> settings = RcsSettings.getInstance().dump();
        
        // Display stack parameters
        Spinner spinner = (Spinner)findViewById(R.id.NetworkAccess);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, NETWORK_ACCESS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        if (RcsSettings.getInstance().getNetworkAccess() == RcsSettingsData.WIFI_ACCESS) {
            spinner.setSelection(2);
        } else
        if (RcsSettings.getInstance().getNetworkAccess() == RcsSettingsData.MOBILE_ACCESS) {
            spinner.setSelection(1);
        } else {
            spinner.setSelection(0);
        }

        spinner = (Spinner)findViewById(R.id.SipDefaultProtocol);
        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, SIP_PROTOCOL);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        if (RcsSettings.getInstance().getSipDefaultProtocol().equalsIgnoreCase(SIP_PROTOCOL[0])) {
            spinner.setSelection(0);
        } else {
            spinner.setSelection(1);
        }

        EditText txt = (EditText)this.findViewById(R.id.ImsConnectionPollingPeriod);
        txt.setText(settings.get("ImsConnectionPollingPeriod"));

        txt = (EditText)this.findViewById(R.id.ImsServicePollingPeriod);
        txt.setText(settings.get("ImsServicePollingPeriod"));
        
        txt = (EditText)this.findViewById(R.id.SipListeningPort);
        txt.setText(settings.get("SipListeningPort"));
        
        txt = (EditText)this.findViewById(R.id.SipTransactionTimeout);
        txt.setText(settings.get("SipTransactionTimeout"));
        
        txt = (EditText)this.findViewById(R.id.DefaultMsrpPort);
        txt.setText(settings.get("DefaultMsrpPort"));

	    txt = (EditText)this.findViewById(R.id.DefaultRtpPort);
	    txt.setText(settings.get("DefaultRtpPort"));
            
		txt = (EditText)this.findViewById(R.id.MsrpTransactionTimeout);
		txt.setText(settings.get("MsrpTransactionTimeout"));
		
        txt = (EditText)this.findViewById(R.id.RegisterExpirePeriod);
        txt.setText(settings.get("RegisterExpirePeriod"));

	    txt = (EditText)this.findViewById(R.id.PublishExpirePeriod);
        txt.setText(settings.get("PublishExpirePeriod"));

        txt = (EditText)this.findViewById(R.id.RevokeTimeout);
        txt.setText(settings.get("RevokeTimeout"));
    		
        txt = (EditText)this.findViewById(R.id.RingingPeriod);
        txt.setText(settings.get("RingingPeriod"));
    		
        txt = (EditText)this.findViewById(R.id.SubscribeExpirePeriod);
        txt.setText(settings.get("SubscribeExpirePeriod"));
    		
        txt = (EditText)this.findViewById(R.id.IsComposingTimeout);
        txt.setText(settings.get("IsComposingTimeout"));
    		
        txt = (EditText)this.findViewById(R.id.SessionRefreshExpirePeriod);
        txt.setText(settings.get("SessionRefreshExpirePeriod"));
    	
        txt = (EditText)this.findViewById(R.id.CapabilityRefreshTimeout);
        txt.setText(settings.get("CapabilityRefreshTimeout"));
        
        txt = (EditText)this.findViewById(R.id.CapabilityExpiryTimeout);
        txt.setText(settings.get("CapabilityExpiryTimeout"));

        txt = (EditText)this.findViewById(R.id.CapabilityPollingPeriod);
        txt.setText(settings.get("CapabilityPollingPeriod"));

    	CheckBox check = (CheckBox)this.findViewById(R.id.PermanentState);
        check.setChecked(Boolean.parseBoolean(settings.get("PermanentState")));

    	check = (CheckBox)this.findViewById(R.id.TelUriFormat);
        check.setChecked(Boolean.parseBoolean(settings.get("TelUriFormat")));
        
    	check = (CheckBox)this.findViewById(R.id.ImAlwaysOn);
        check.setChecked(Boolean.parseBoolean(settings.get("ImAlwaysOn")));

    	check = (CheckBox)this.findViewById(R.id.ImUseReports);
        check.setChecked(Boolean.parseBoolean(settings.get("ImUseReports")));
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
		        // Save stack parameters
				Spinner spinner = (Spinner)findViewById(R.id.SipDefaultProtocol);
				Provisioning.writeParameter(cr, "SipDefaultProtocol", (String)spinner.getSelectedItem());

				spinner = (Spinner)findViewById(R.id.NetworkAccess);
				int index = spinner.getSelectedItemPosition();
				switch(index) {
					case 0:
						Provisioning.writeParameter(cr, "NetworkAccess", ""+RcsSettingsData.ANY_ACCESS);
						break;
					case 1:
						Provisioning.writeParameter(cr, "NetworkAccess", ""+RcsSettingsData.MOBILE_ACCESS);
						break;
					case 2:
						Provisioning.writeParameter(cr, "NetworkAccess", ""+RcsSettingsData.WIFI_ACCESS);
						break;
				}					
				
				EditText txt = (EditText)this.findViewById(R.id.ImsConnectionPollingPeriod);
				Provisioning.writeParameter(cr, "ImsConnectionPollingPeriod", txt.getText().toString());
				
		        txt = (EditText)this.findViewById(R.id.SipListeningPort);
				Provisioning.writeParameter(cr, "SipListeningPort", txt.getText().toString());
		        
		        txt = (EditText)this.findViewById(R.id.SipTransactionTimeout);
				Provisioning.writeParameter(cr, "SipTransactionTimeout", txt.getText().toString());
		        
		        txt = (EditText)this.findViewById(R.id.DefaultMsrpPort);
				Provisioning.writeParameter(cr, "DefaultMsrpPort", txt.getText().toString());

			    txt = (EditText)this.findViewById(R.id.DefaultRtpPort);
				Provisioning.writeParameter(cr, "DefaultRtpPort", txt.getText().toString());
		            
				txt = (EditText)this.findViewById(R.id.MsrpTransactionTimeout);
				Provisioning.writeParameter(cr, "MsrpTransactionTimeout", txt.getText().toString());
				
		        txt = (EditText)this.findViewById(R.id.RegisterExpirePeriod);
				Provisioning.writeParameter(cr, "RegisterExpirePeriod", txt.getText().toString());

			    txt = (EditText)this.findViewById(R.id.PublishExpirePeriod);
				Provisioning.writeParameter(cr, "PublishExpirePeriod", txt.getText().toString());

		        txt = (EditText)this.findViewById(R.id.RevokeTimeout);
				Provisioning.writeParameter(cr, "RevokeTimeout", txt.getText().toString());
		    		
		        txt = (EditText)this.findViewById(R.id.RingingPeriod);
				Provisioning.writeParameter(cr, "RingingPeriod", txt.getText().toString());
		    		
		        txt = (EditText)this.findViewById(R.id.SubscribeExpirePeriod);
				Provisioning.writeParameter(cr, "SubscribeExpirePeriod", txt.getText().toString());
		    		
		        txt = (EditText)this.findViewById(R.id.IsComposingTimeout);
				Provisioning.writeParameter(cr, "IsComposingTimeout", txt.getText().toString());
		    		
		        txt = (EditText)this.findViewById(R.id.SessionRefreshExpirePeriod);
				Provisioning.writeParameter(cr, "SessionRefreshExpirePeriod", txt.getText().toString());
		    	
		        txt = (EditText)this.findViewById(R.id.CapabilityRefreshTimeout);
				Provisioning.writeParameter(cr, "CapabilityRefreshTimeout", txt.getText().toString());

		        txt = (EditText)this.findViewById(R.id.CapabilityExpiryTimeout);
				Provisioning.writeParameter(cr, "CapabilityExpiryTimeout", txt.getText().toString());

				txt = (EditText)this.findViewById(R.id.CapabilityPollingPeriod);
				Provisioning.writeParameter(cr, "CapabilityPollingPeriod", txt.getText().toString());

		    	CheckBox check = (CheckBox)this.findViewById(R.id.PermanentState);
				Provisioning.writeParameter(cr, "PermanentState", Boolean.toString(check.isChecked()));

		    	check = (CheckBox)this.findViewById(R.id.TelUriFormat);
				Provisioning.writeParameter(cr, "TelUriFormat", Boolean.toString(check.isChecked()));

				check = (CheckBox)this.findViewById(R.id.ImAlwaysOn);
				Provisioning.writeParameter(cr, "ImAlwaysOn", Boolean.toString(check.isChecked()));

		    	check = (CheckBox)this.findViewById(R.id.ImUseReports);
				Provisioning.writeParameter(cr, "ImUseReports", Boolean.toString(check.isChecked()));
				
				Toast.makeText(this, getString(R.string.label_reboot_service), Toast.LENGTH_LONG).show();				
				break;
		}
		return true;
	}
}