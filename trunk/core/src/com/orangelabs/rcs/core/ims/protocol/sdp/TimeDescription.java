package com.orangelabs.rcs.core.ims.protocol.sdp;

import java.io.ByteArrayInputStream;
import java.util.Vector;

public class TimeDescription extends Parser {
	// Values:
	public String timeActive;

	public Vector<String> repeatTimes;

	public TimeDescription(ByteArrayInputStream bin) {
		// Time the session is active:
		timeActive = getLine(bin);

		// Repeat Times:
		repeatTimes = new Vector<String>();

		boolean found = getToken(bin, "r=", false);

		while (found) {
			String repeatTime = getLine(bin);

			repeatTimes.addElement(repeatTime);

			found = getToken(bin, "r=", false);
		}
	}
}
