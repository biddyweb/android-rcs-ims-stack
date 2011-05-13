package com.orangelabs.rcs.provisioning;

import java.util.Map;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.EditText;

import com.orangelabs.rcs.provider.settings.RcsSettings;

/**
 * Service parameters provisioning
 * 
 * @author jexa7410
 */
public class ServiceProvisioning extends Activity {
	/**
	 * Content resolver
	 */
	private ContentResolver cr;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set layout
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.ui_provisioning);
        
        // Set database content resolver
        this.cr = getContentResolver();
        
		// Get settings from database
        Map<String, String> settings = RcsSettings.getInstance().dump();
        
        // Display UI parameters

        EditText txt = (EditText)this.findViewById(R.id.MaxRcsContacts);
        txt.setText(settings.get("MaxRcsContacts"));
        
        txt = (EditText)this.findViewById(R.id.MaxPhotoIconSize);
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

        txt = (EditText)this.findViewById(R.id.MaxImageShareSize);
        txt.setText(settings.get("MaxImageShareSize"));

        txt = (EditText)this.findViewById(R.id.MaxVideoShareDuration);
        txt.setText(settings.get("MaxVideoShareDuration"));

        txt = (EditText)this.findViewById(R.id.MaxChatSessions);
        txt.setText(settings.get("MaxChatSessions"));

        txt = (EditText)this.findViewById(R.id.MaxFileTransferSessions);
        txt.setText(settings.get("MaxFileTransferSessions"));

        CheckBox check = (CheckBox)this.findViewById(R.id.AnonymousFetchService);
        check.setChecked(Boolean.parseBoolean(settings.get("AnonymousFetchService")));

        check = (CheckBox)this.findViewById(R.id.ChatService);
        check.setChecked(Boolean.parseBoolean(settings.get("ChatService")));

        check = (CheckBox)this.findViewById(R.id.SmsFallbackService);
        check.setChecked(Boolean.parseBoolean(settings.get("SmsFallbackService")));
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
		        EditText txt = (EditText)this.findViewById(R.id.MaxRcsContacts);
				Provisioning.writeParameter(cr, "MaxRcsContacts", txt.getText().toString());
		        
		        txt = (EditText)this.findViewById(R.id.MaxPhotoIconSize);
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

		        txt = (EditText)this.findViewById(R.id.MaxImageShareSize);
				Provisioning.writeParameter(cr, "MaxImageShareSize", txt.getText().toString());

		        txt = (EditText)this.findViewById(R.id.MaxVideoShareDuration);
				Provisioning.writeParameter(cr, "MaxVideoShareDuration", txt.getText().toString());

		        txt = (EditText)this.findViewById(R.id.MaxChatSessions);
				Provisioning.writeParameter(cr, "MaxChatSessions", txt.getText().toString());

		        txt = (EditText)this.findViewById(R.id.MaxFileTransferSessions);
				Provisioning.writeParameter(cr, "MaxFileTransferSessions", txt.getText().toString());

		        CheckBox check = (CheckBox)this.findViewById(R.id.AnonymousFetchService);
				Provisioning.writeParameter(cr, "AnonymousFetchService", Boolean.toString(check.isChecked()));

		        check = (CheckBox)this.findViewById(R.id.ChatService);
				Provisioning.writeParameter(cr, "ChatService", Boolean.toString(check.isChecked()));

		        check = (CheckBox)this.findViewById(R.id.SmsFallbackService);
				Provisioning.writeParameter(cr, "SmsFallbackService", Boolean.toString(check.isChecked()));
				break;
		}
		return true;
	}
}
