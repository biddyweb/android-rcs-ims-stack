package com.orangelabs.rcs.settings;

import com.orangelabs.rcs.R;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;


/**
 * About the application display
 * 
 * @author jexa7410
 */
public class AboutDisplay extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		// Set title
        setTitle(R.string.title_about);
        setContentView(R.layout.rcs_settings_about_layout);
        
        // Set the release number
        TextView releaseView = (TextView)findViewById(R.id.settings_label_release);
        String relLabel = getString(R.string.label_release);
        String relNumber = getString(R.string.release_number);
        releaseView.setText(relLabel + " " + relNumber);
    }
}
