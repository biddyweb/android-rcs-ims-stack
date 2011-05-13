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
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.orangelabs.rcs.provider.settings.RcsSettings;

/**
 * End user profile parameters provisioning
 * 
 * @author jexa7410
 */
public class ProfileProvisioning extends Activity {
	/**
	 * IMS authent
	 */
    private static final String[] IMS_AUTHENT = {
        "GIBA", "DIGEST"
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
        
		// Get settings from database
        Map<String, String> settings = RcsSettings.getInstance().dump();
        
        // Display profile parameters
        Spinner spinner = (Spinner)findViewById(R.id.ImsAuhtenticationProcedure);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, IMS_AUTHENT);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        if (RcsSettings.getInstance().getImsAuhtenticationProcedure().equals(IMS_AUTHENT[0])) {
            spinner.setSelection(0);
        } else {
            spinner.setSelection(1);
        }

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
        txt.setText(settings.get("ImsOutboundProxyAddr"));

        txt = (EditText)this.findViewById(R.id.XdmServerAddr);
        txt.setText(settings.get("XdmServerAddr"));        
        
        txt = (EditText)this.findViewById(R.id.XdmServerLogin);
        txt.setText(settings.get("XdmServerLogin"));        
        
        txt = (EditText)this.findViewById(R.id.XdmServerPassword);
        txt.setText(settings.get("XdmServerPassword"));        
        
        txt = (EditText)this.findViewById(R.id.ImConferenceUri);
        txt.setText(settings.get("ImConferenceUri"));
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
				Spinner spinner = (Spinner)findViewById(R.id.ImsAuhtenticationProcedure);
				String value = (String)spinner.getSelectedItem();
				Provisioning.writeParameter(cr, "ImsAuhtenticationProcedure", value);

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
				Provisioning.writeParameter(cr, "ImsOutboundProxyAddr", txt.getText().toString());

		        txt = (EditText)this.findViewById(R.id.XdmServerAddr);
				Provisioning.writeParameter(cr, "XdmServerAddr", txt.getText().toString());
		        
		        txt = (EditText)this.findViewById(R.id.XdmServerLogin);
				Provisioning.writeParameter(cr, "XdmServerLogin", txt.getText().toString());
		        
		        txt = (EditText)this.findViewById(R.id.XdmServerPassword);
				Provisioning.writeParameter(cr, "XdmServerPassword", txt.getText().toString());
		        
		        txt = (EditText)this.findViewById(R.id.ImConferenceUri);
		        Provisioning.writeParameter(cr, "ImConferenceUri", txt.getText().toString());

		        Toast.makeText(this, getString(R.string.label_reboot_service), Toast.LENGTH_LONG).show();				
		        break;
		}
		return true;
	}
}
