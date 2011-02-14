package com.orangelabs.rcs.provisioning;

import android.app.TabActivity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TabHost;

import com.orangelabs.rcs.platform.logger.AndroidAppender;
import com.orangelabs.rcs.provider.settings.RcsSettings;
import com.orangelabs.rcs.utils.logger.Appender;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Main
 * 
 * @author jexa7410
 */
public class Provisioning extends TabActivity {

	/**
	 * Database URI
	 */
	private static final Uri databaseUri = Uri.parse("content://com.orangelabs.rcs.settings/settings"); 
	
	/**
	 * Column name
	 */
    private static final String KEY_KEY = "key";
	
	/**
	 * Column name
	 */
    private static final String KEY_VALUE = "value";
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Instanciate the settings manager
        RcsSettings.createInstance(getApplicationContext());
        
		// Set logger appenders
		Appender[] appenders = new Appender[] { 
				new AndroidAppender()
			};        
		Logger.setAppenders(appenders);
		Logger.activationFlag = Logger.TRACE_ON;

		// Set layout
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Set tabs
        final TabHost tabHost = getTabHost();
        tabHost.addTab(tabHost.newTabSpec("profile")
                .setIndicator("Profile", getResources().getDrawable(R.drawable.param_icon))
                .setContent(new Intent(this, ProfileProvisioning.class)));
        tabHost.addTab(tabHost.newTabSpec("stack")
                .setIndicator("Stack", getResources().getDrawable(R.drawable.param_icon))
                .setContent(new Intent(this, StackProvisioning.class)));
        tabHost.addTab(tabHost.newTabSpec("ui")
                .setIndicator("Service", getResources().getDrawable(R.drawable.param_icon))
                .setContent(new Intent(this, ServiceProvisioning.class)));
        tabHost.addTab(tabHost.newTabSpec("logger")
                .setIndicator("Logger", getResources().getDrawable(R.drawable.param_icon))
                .setContent(new Intent(this, LoggerProvisioning.class)));
    }
	
	/**
	 * Write a String parameter
	 * 
	 * @param cr Content resolver
	 * @param key Key
	 * @param value Value
	 */
	public static void writeParameter(ContentResolver cr, String key, String value) {
        ContentValues values = new ContentValues();
        values.put(KEY_VALUE, value);
        String where = KEY_KEY + "='" + key + "'";
        cr.update(databaseUri, values, where, null);        
	}
	
	/**
	 * Read a parameter
	 * 
	 * @param cr Content resolver
	 * @param key Key
	 * @return Value
	 */
	public static String readParameter(ContentResolver cr, String key) {
		String result = null;
        Cursor c = cr.query(databaseUri, null, KEY_KEY + "='" + key + "'", null, null);
        if ((c != null) && (c.getCount() > 0)) {
	        if (c.moveToFirst()) {
	        	result = c.getString(2);
	        }
	        c.close();
        }
        return result;
	}
}