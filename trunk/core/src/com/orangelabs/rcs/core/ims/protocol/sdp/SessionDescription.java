package com.orangelabs.rcs.core.ims.protocol.sdp;

import java.io.ByteArrayInputStream;
import java.util.Vector;

public class SessionDescription extends Parser {
	public Vector<TimeDescription> timeDescriptions;

	public Vector<MediaAttribute> sessionAttributes;

	public boolean connectionIncluded;

	// Values:
	public String version;

	public String origin;

	public String sessionName;

	public String sessionInfo;

	public String uri;

	public String email;

	public String phone;

	public String connectionInfo;

	public String bandwidthInfo;

	public String timezoneAdjustment;

	public String encryptionKey;

	public SessionDescription(ByteArrayInputStream bin) {
		connectionIncluded = false;

		// Protocol Version:
		version = getLine(bin);

		// Origin:
		if (getToken(bin, "o=", true)) {
			origin = getLine(bin);
		}

		// Session Name:
		if (getToken(bin, "s=", true)) {
			sessionName = getLine(bin);
		}

		// Session and Media Information:
		if (getToken(bin, "i=", false)) {
			sessionInfo = getLine(bin);
		}

		// URI:
		if (getToken(bin, "u=", false)) {
			uri = getLine(bin);
		}

		// E-Mail:
		if (getToken(bin, "e=", false)) {
			email = getLine(bin);
		}

		// Try a second E-Mail (Bug in PRISS protocol):
		if (getToken(bin, "e=", false)) {
			email = getLine(bin);
		}

		// phone number:
		if (getToken(bin, "p=", false)) {
			phone = getLine(bin);
		}

		// connection information:
		if (getToken(bin, "c=", false)) {
			connectionIncluded = true;
			connectionInfo = getLine(bin);
		}

		// bandwidth information:
		if (getToken(bin, "b=", false)) {
			bandwidthInfo = getLine(bin);
		}

		// time description:
		timeDescriptions = new Vector<TimeDescription>();

		boolean found = getToken(bin, "t=", true);

		while (found) {
			TimeDescription timeDescription = new TimeDescription(bin);

			timeDescriptions.addElement(timeDescription);

			found = getToken(bin, "t=", false);
		}

		// time zone adjustments:
		if (getToken(bin, "z=", false)) {
			timezoneAdjustment = getLine(bin);
		}

		// encryption key:
		if (getToken(bin, "k=", false)) {
			encryptionKey = getLine(bin);
		}

		// session attributes:
		sessionAttributes = new Vector<MediaAttribute>();

		found = getToken(bin, "a=", false);

		while (found) {
			String sessionAttribute = getLine(bin);

			int index = sessionAttribute.indexOf(':');

			if (index > 0) {
				String name = sessionAttribute.substring(0, index);
				String value = sessionAttribute.substring(index + 1);

				MediaAttribute attribute = new MediaAttribute(name, value);

				sessionAttributes.addElement(attribute);
			}

			found = getToken(bin, "a=", false);
		}
	}

	public MediaAttribute getSessionAttribute(String name) {
		MediaAttribute attribute = null;

		if (sessionAttributes != null) {
			for (int i = 0; i < sessionAttributes.size(); i++) {
				MediaAttribute entry = (MediaAttribute) sessionAttributes.elementAt(i);
				if (entry.getName().equals(name)) {
					attribute = entry;
					break;
				}
			}
		}

		return attribute;
	}
}
