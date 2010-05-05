package com.orangelabs.rcs.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * RCS service startup on device boot
 * 
 * @author jexa7410
 */
public class RcsServiceStartup extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		// Start RCS core
		context.startService(new Intent("com.orangelabs.rcs.SERVICE"));
	}
}
