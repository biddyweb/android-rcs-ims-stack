package com.orangelabs.rcs.ri;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.TextView;

/**
 * About RI
 * 
 * @author jexa7410
 */
public class AboutRI extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set layout
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.app_about);

        // Set title
        setTitle(R.string.menu_about);
        
        // Display release number
        TextView textView = (TextView)findViewById(R.id.release);
        textView.setText(getString(R.string.label_about_release) + " " +
        		getString(R.string.release_number));
    }
}
