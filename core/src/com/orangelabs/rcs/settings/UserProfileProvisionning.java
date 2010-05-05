package com.orangelabs.rcs.settings;

import com.orangelabs.rcs.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * User profile provisionning
 * 
 * @author jexa7410
 */
public class UserProfileProvisionning extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set title
        setTitle(R.string.title_provisionning);
        
        // Set layout
        setContentView(R.layout.rcs_settings_provisioning_layout);

        // Set button listeners
        Button button = (Button)findViewById(R.id.btn_continue);        
        button.setOnClickListener(continueListener);
        button = (Button)findViewById(R.id.btn_exit);        
        button.setOnClickListener(exitListener);
    }
    
    private OnClickListener continueListener = new OnClickListener() {
        public void onClick(View v) {
	    	Intent intent = new Intent(UserProfileProvisionning.this, UserProfileSettingsDisplay.class);
        	startActivity(intent);
        	UserProfileProvisionning.this.finish();
        }
    };
    
    private OnClickListener exitListener = new OnClickListener() {
        public void onClick(View v) {
        	UserProfileProvisionning.this.finish();
        }
    };
}
