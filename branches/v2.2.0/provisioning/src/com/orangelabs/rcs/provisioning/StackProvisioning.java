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

        // Get settings from database
        Map<String, String> settings = RcsSettings.getInstance().dump();
        
        // Display stack parameters
        Spinner spinner = (Spinner)findViewById(R.id.SipDefaultProtocol);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
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

        txt = (EditText)this.findViewById(R.id.AnonymousFetchRefrehTimeout);
        txt.setText(settings.get("AnonymousFetchRefrehTimeout"));

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
    	
    	CheckBox check = (CheckBox)this.findViewById(R.id.Richcall);
        check.setChecked(Boolean.parseBoolean(settings.get("Richcall")));

    	check = (CheckBox)this.findViewById(R.id.PermanentState);
        check.setChecked(Boolean.parseBoolean(settings.get("PermanentState")));

    	check = (CheckBox)this.findViewById(R.id.TelUriFormat);
        check.setChecked(Boolean.parseBoolean(settings.get("TelUriFormat")));
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
				String value = (String)spinner.getSelectedItem();
				Provisioning.writeParameter(cr, "SipDefaultProtocol", value);

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

		        txt = (EditText)this.findViewById(R.id.AnonymousFetchRefrehTimeout);
				Provisioning.writeParameter(cr, "AnonymousFetchRefrehTimeout", txt.getText().toString());

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
		    	
		    	CheckBox check = (CheckBox)this.findViewById(R.id.Richcall);
				Provisioning.writeParameter(cr, "Richcall", Boolean.toString(check.isChecked()));

		    	check = (CheckBox)this.findViewById(R.id.PermanentState);
				Provisioning.writeParameter(cr, "PermanentState", Boolean.toString(check.isChecked()));

		    	check = (CheckBox)this.findViewById(R.id.TelUriFormat);
				Provisioning.writeParameter(cr, "TelUriFormat", Boolean.toString(check.isChecked()));
				
				Toast.makeText(this, getString(R.string.label_reboot_service), Toast.LENGTH_LONG).show();				
				break;
		}
		return true;
	}
}