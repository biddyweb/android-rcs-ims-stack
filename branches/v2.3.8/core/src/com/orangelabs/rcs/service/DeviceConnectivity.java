package com.orangelabs.rcs.service;

import com.orangelabs.rcs.provider.settings.RcsSettings;
import com.orangelabs.rcs.provider.settings.RcsSettingsData;
import com.orangelabs.rcs.utils.logger.Logger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Device connectivity event: automatically starts the RCS service if the
 * device was in airplane mode during last startup
 * 
 * @author jexa7410
 */
public class DeviceConnectivity extends BroadcastReceiver {
	/**
	 * The logger
	 */
    private Logger logger = Logger.getLogger(this.getClass().getName());

    @Override
    public void onReceive(Context context, Intent intent) {
        // Instantiate the settings manager
        RcsSettings.createInstance(context);
        if (RcsSettings.getInstance().getAutoConfigMode() != RcsSettingsData.HTTPS_AUTO_CONFIG) {
            // Try to start the service only if a data connectivity is available
            NetworkInfo info = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            if ((info != null) && info.isConnected()) {
                if (logger.isActivated()) {
                    logger.debug("Device connected");
                }
                // Start the RCS service
                LauncherUtils.launchRcsCoreService(context);
            }
        }
    }
}
