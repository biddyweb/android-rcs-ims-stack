package com.orangelabs.rcs.core.ims.protocol.sdp;

import java.io.ByteArrayInputStream;
import java.util.Vector;

/**
 * SDP parser
 * 
 * @author jexa7410
 */
public class SdpParser extends Parser {
	public SessionDescription sessionDescription = null;

	public Vector<MediaDescription> mediaDescriptions = null;

	public SdpParser(byte data[]) {
		init();
		ByteArrayInputStream bin = new ByteArrayInputStream(data);
		parseData(bin);
	}

	public void parseData(ByteArrayInputStream bin) {
		if (getToken(bin, "v=", true)) {
			sessionDescription = new SessionDescription(bin);
			mediaDescriptions = new Vector<MediaDescription>();

			boolean found = getToken(bin, "m=", false);
			while (found) {
				MediaDescription mediaDescription = new MediaDescription(bin,
						sessionDescription.connectionIncluded);

				mediaDescriptions.addElement(mediaDescription);
				found = getToken(bin, "m=", false);
			}
		}
	}

	public MediaAttribute getSessionAttribute(String name) {
		MediaAttribute attribute = null;

		if (sessionDescription != null) {
			attribute = sessionDescription.getSessionAttribute(name);
		}

		return attribute;
	}

	public MediaDescription getMediaDescription(String name) {
		MediaDescription description = null;

		if (mediaDescriptions != null) {
			for (int i = 0; i < mediaDescriptions.size(); i++) {
				MediaDescription entry = (MediaDescription)mediaDescriptions.elementAt(i);
				if (entry.name.equals(name)) {
					description = entry;
					break;
				}
			}
		}

		return description;
	}

	public Vector<MediaDescription> getMediaDescriptions() {
		return mediaDescriptions;
	}
}
