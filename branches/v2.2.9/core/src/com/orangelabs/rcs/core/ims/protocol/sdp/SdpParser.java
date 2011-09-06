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
				MediaDescription mediaDescription = new MediaDescription(bin, sessionDescription.connectionIncluded);
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

	public Vector<MediaDescription> getMediaDescriptions(String name) {
		Vector<MediaDescription> result = new Vector<MediaDescription>();
		if (mediaDescriptions != null) {
			for (int i = 0; i < mediaDescriptions.size(); i++) {
				MediaDescription entry = (MediaDescription)mediaDescriptions.elementAt(i);
				if (entry.name.equals(name)) {
					result.add(entry);
				}
			}
		}
		return result;
	}

	public Vector<MediaDescription> getMediaDescriptions() {
		return mediaDescriptions;
	}
}
