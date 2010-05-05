package com.orangelabs.rcs.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * RCS service shutdown on device shutdown
 * 
 * @author jexa7410
 */
public class RcsServiceShutdown extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		// Stop RCS core
        context.stopService(new Intent("com.orangelabs.rcs.SERVICE"));
	}
}
