package com.orangelabs.rcs.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.orangelabs.rcs.provider.settings.RcsSettings;

/**
 * Automatically starts the RCS service (e.g. after data connection recovery)
 * 
 * @author jexa7410
 */
public class RcsServiceStartup extends BroadcastReceiver {
    @Override
	public void onReceive(Context context, Intent intent) {
    	// Try to start the service only if a data connectivity is available
    	NetworkInfo info = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
		if ((info != null) && info.isConnected()) {
			// Instanciate the settings manager if needed
	    	RcsSettings.createInstance(context);

	    	// Try to start the service only if service is enabled in settings
	    	if (RcsSettings.getInstance().isServiceActivated()) {
    			// Start the service. If service already started this action has no effect.
    			context.startService(new Intent(RcsCoreService.SERVICE_NAME));
			}
		}
	}
}
