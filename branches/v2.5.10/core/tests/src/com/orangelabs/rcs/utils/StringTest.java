package com.orangelabs.rcs.utils;

import android.test.AndroidTestCase;

public class StringTest extends AndroidTestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testSplitVersion() {
		String version = "2.3.1";
		String[] items = version.split("\\.");
		assertEquals(items.length, 3);

		String subversion = items[0] + "." + items[1];
		assertEquals(subversion, "2.3");
	}
}
