package com.orangelabs.rcs.geoloc;

import com.orangelabs.rcs.service.api.client.messaging.GeolocPush;
import com.orangelabs.rcs.utils.logger.Logger;

import android.test.AndroidTestCase;

public class GeolocTest extends AndroidTestCase {
	
	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public final void testGeolocPushFormat() {
		// GeolocPush g = new GeolocPush(label, latitude, longitude, altitude, expiration);
		GeolocPush g = new GeolocPush("label", 1.0, 2.0, 3.0, 0, 100);
		String str = GeolocPush.formatGeolocToStr(g);
		assertEquals(str, "label,1.0,2.0,3.0,0,100.0");
		
		g = new GeolocPush("label", 1.0, 2.0, 3.0, 0);
		str = GeolocPush.formatGeolocToStr(g);
		assertEquals(str, "label,1.0,2.0,3.0,0,0.0");

		g = new GeolocPush("", 1.0, 2.0, 3.0, 0, 100);
		str = GeolocPush.formatGeolocToStr(g);
		assertEquals(str, ",1.0,2.0,3.0,0,100.0");
		
		g = new GeolocPush(null, 1.0, 2.0, 3.0, 0, 100);
		str = GeolocPush.formatGeolocToStr(g);
		assertEquals(str, ",1.0,2.0,3.0,0,100.0");
	}

}
