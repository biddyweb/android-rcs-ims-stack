package com.orangelabs.rcs.ri.ipcall;

import com.orangelabs.rcs.platform.AndroidFactory;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class IPCallInvitationReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		
		// Set application context
		AndroidFactory.setApplicationContext(context); //TODO: use context at player level

		// Display invitation notification
		InitiateIPCallActivity.addIPCallInvitationNotification(context, intent);
		
	}

}
