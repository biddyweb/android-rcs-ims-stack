package com.orangelabs.rcs.utils;

import java.util.UUID;
import android.content.Context;
import android.telephony.TelephonyManager;

/***
 * Device utility functions
 * 
 * @author jexa7410
 */
public class DeviceUtils {
	/**
	 * UUID
	 */
	private static UUID uuid = null;

	/**
	 * Returns unique UUID of the device
	 * 
	 * @param context Context 
	 * @return UUID
	 */
	public static UUID getDeviceUUID(Context context) {
		if (context == null) {
			return null;
		}
		
		if (uuid == null) {
			TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
			String id = tm.getDeviceId();
			if (id != null) { 
				uuid = UUID.nameUUIDFromBytes(id.getBytes());
			}
		}
		
		return uuid;
	}
}
