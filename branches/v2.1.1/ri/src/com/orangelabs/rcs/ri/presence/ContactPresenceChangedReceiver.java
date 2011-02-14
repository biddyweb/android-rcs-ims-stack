package com.orangelabs.rcs.ri.presence;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.orangelabs.rcs.ri.R;
import com.orangelabs.rcs.utils.PhoneUtils;

/**
 * Contact presence info update event receiver
 */
public class ContactPresenceChangedReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		// Get invitation info
		String contact = PhoneUtils.extractNumberFromUri(intent.getStringExtra("contact"));
		
		// Display a toast
		Toast.makeText(context, context.getString(R.string.label_presence_info_changed, contact), Toast.LENGTH_LONG).show();
	}
}
