package com.orangelabs.rcs.service;

import com.orangelabs.rcs.core.Core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Time has changed on device
 * 
 * @author jexa7410
 */
public class TimeChangedReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		if ((Core.getInstance() != null) &&
				(Core.getInstance().getPresenceService() != null) &&
					(Core.getInstance().getPresenceService().getPokeManager() != null)) {
			// Publish presence to avoid problems with poke
			Core.getInstance().getPresenceService().getPokeManager().timeHasChanged();
		}
	}
}
