/*******************************************************************************
 * Software Name : RCS IMS Stack
 *
 * Copyright © 2010 France Telecom S.A.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

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
