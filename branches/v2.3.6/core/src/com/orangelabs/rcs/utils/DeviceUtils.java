package com.orangelabs.rcs.utils;

import com.orangelabs.rcs.R;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.telephony.TelephonyManager;

import java.io.IOException;
import java.util.UUID;

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

    /**
     * Returns SIM country calling code
     * 
     * @param context application context
     * @return country calling code
     */
    public static String getSimCountryCode(Context context) {
        // Get SIM Country code 
        TelephonyManager mgr = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        String countryCodeIso = mgr.getSimCountryIso();
        if (countryCodeIso == null)
            return null;
        
        // Parse country table
        try {
            XmlResourceParser parser = context.getResources().getXml(R.xml.country_table);
            parser.next();
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if (parser.getName().equals("Data")) {
                        if (parser.getAttributeValue(null, "code").equalsIgnoreCase(countryCodeIso)) {
                            return parser.getAttributeValue(null, "cc");
                        }
                    }
                }
                eventType = parser.next();
            }
        } catch (XmlPullParserException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
        return null;
    }

}
