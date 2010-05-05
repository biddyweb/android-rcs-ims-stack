package com.orangelabs.rcs.settings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.orangelabs.rcs.R;
import com.orangelabs.rcs.provider.settings.RcsSettings;

/**
 * RCS settings application which permits to edit application
 * settings, access to user guide and so on.
 * 
 * @author jexa7410
 */
public class RcsSettingsApplication extends Activity {
    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        
		// Set title
        setTitle(R.string.title_settings);
        setContentView(R.layout.rcs_settings_layout);
        
        // Set the release number
        TextView releaseView = (TextView)findViewById(R.id.settings_label_release);
        String relLabel = getString(R.string.label_release);
        String relNumber = getString(R.string.release_number);
        releaseView.setText(relLabel + " " + relNumber);
        
        // Instanciate the settings manager
        RcsSettings.createInstance(getApplicationContext());
    }
        
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		new MenuInflater(getApplication()).inflate(R.menu.rcs_settings_main_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int i = item.getItemId();
		if (i == R.id.menu_settings) {
	    	Intent intent = new Intent(this, SettingsDisplay.class);
	    	startActivity(intent);
            return true;
		} else
		if (i == R.id.menu_about) {
	    	Intent intent = new Intent(this, AboutDisplay.class);
	    	startActivity(intent);
			return true;
		} else
		if (i == R.id.menu_help) {
			Toast.makeText(this, R.string.label_not_yet_implemented, Toast.LENGTH_LONG).show();
			return true;
		}		
		return super.onOptionsItemSelected(item);
	}
}