package com.orangelabs.rcs.core.ims.protocol.sdp;

import java.io.ByteArrayInputStream;
import java.util.Vector;

public class MediaDescription extends Parser {
	// Values:
	public String name;

	public int port;

	public String protocol;

	public int payload_type;

	public String payload;

	public String mediaTitle;

	public String connectionInfo;

	public String bandwidthInfo;

	public String encryptionKey;

	public Vector<MediaAttribute> mediaAttributes;

	public MediaDescription(ByteArrayInputStream bin, boolean connectionIncluded) {
		// Media Name and Transport Address:
		String line = getLine(bin);
		int end = line.indexOf(' ');
		name = line.substring(0, end);

		int start = end + 1;
		end = line.indexOf(' ', start);
		port = Integer.parseInt(line.substring(start, end));

		start = end + 1;
		end = line.indexOf(' ', start);
		protocol = line.substring(start, end);

		start = end + 1;
		payload = line.substring(start);
		try {
			payload_type = Integer.parseInt(payload);
		} catch (Exception e) {
			payload_type = -1;
		}

		// Session and Media Information:
		if (getToken(bin, "i=", false)) {
			mediaTitle = getLine(bin);
		}

		// Connection Information:
		boolean mandatory = true;

		if (connectionIncluded) {
			mandatory = false;
		}

		if (getToken(bin, "c=", mandatory)) {
			connectionInfo = getLine(bin);
		}

		// Bandwidth Information:
		if (getToken(bin, "b=", false)) {
			bandwidthInfo = getLine(bin);
		}

		// Encryption Key:
		if (getToken(bin, "k=", false)) {
			encryptionKey = getLine(bin);
		}

		// Media Attributes:
		mediaAttributes = new Vector<MediaAttribute>();

		boolean found = getToken(bin, "a=", false);
		while (found) {
			String mediaAttribute = getLine(bin);

			int index = mediaAttribute.indexOf(':');

			if (index > 0) {
				String name = mediaAttribute.substring(0, index);
				String value = mediaAttribute.substring(index + 1);

				MediaAttribute attribute = new MediaAttribute(name, value);
				mediaAttributes.addElement(attribute);
			}

			found = getToken(bin, "a=", false);
		}
	}

	public MediaAttribute getMediaAttribute(String name) {
		MediaAttribute attribute = null;
		if (mediaAttributes != null) {
			for (int i = 0; i < mediaAttributes.size(); i++) {
				MediaAttribute entry = (MediaAttribute)mediaAttributes.elementAt(i);
				if (entry.getName().equals(name)) {
					attribute = entry;
					break;
				}
			}
		}

		return attribute;
	}
}
