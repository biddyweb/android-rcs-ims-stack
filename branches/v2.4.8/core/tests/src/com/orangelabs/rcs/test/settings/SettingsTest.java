package com.orangelabs.rcs.test.settings;

import android.test.AndroidTestCase;

import com.orangelabs.rcs.provider.settings.RcsSettings;
import com.orangelabs.rcs.provider.settings.RcsSettingsData;

public class SettingsTest extends AndroidTestCase {

	protected void setUp() throws Exception {
		super.setUp();
		
		RcsSettings.createInstance(getContext());		
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testGroupchatSettings() {
		RcsSettings.getInstance().writeParameter(RcsSettingsData.IM_CONF_URI, "");
		assertEquals(false, RcsSettings.getInstance().isGroupChatActivated());

		RcsSettings.getInstance().writeParameter(RcsSettingsData.IM_CONF_URI, "sip:foo@bar");
		assertEquals(false, RcsSettings.getInstance().isGroupChatActivated());

		RcsSettings.getInstance().writeParameter(RcsSettingsData.IM_CONF_URI, "sip:Conference-Factory@sip.mobistar.com");
		assertEquals(true, RcsSettings.getInstance().isGroupChatActivated());
	}
}
