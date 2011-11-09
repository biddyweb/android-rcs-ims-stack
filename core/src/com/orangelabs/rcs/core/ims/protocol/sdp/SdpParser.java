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

			parseMediaDescriptions(bin);
		}
	}

    /**
     * Parse the media descriptions part
     * 
     * @param bin Input stream
     */
    private void parseMediaDescriptions(ByteArrayInputStream bin) {
        boolean found = getToken(bin, "m=", false);
        while (found) {
            Vector<MediaDescription> descs = new Vector<MediaDescription>();
            
            // Media Name and Transport Address:
            String line = getLine(bin);
            int end = line.indexOf(' ');
            String name = line.substring(0, end);

            int start = end + 1;
            end = line.indexOf(' ', start);
            int port = Integer.parseInt(line.substring(start, end));

            start = end + 1;
            end = line.indexOf(' ', start);
            String protocol = line.substring(start, end);

            String payload;
            start = end + 1;
            end = line.indexOf(' ', start);
            while (end != -1) {
                payload = line.substring(start, end);
                descs.addElement(new MediaDescription(name, port, protocol, payload));
                start = end + 1;
                end = line.indexOf(' ', start);
            }
            payload = line.substring(start);
            descs.addElement(new MediaDescription(name, port, protocol, payload));

            // Session and Media Information:
            if (getToken(bin, "i=", false)) {
                String mediaTitle = getLine(bin);
                for (int i = 0; i < descs.size(); i++) {
                    descs.elementAt(i).mediaTitle = mediaTitle;
                }
            }
            
            // Connection Information:
            boolean mandatory = true;
            if (sessionDescription.connectionIncluded) {
                mandatory = false;
            }
            if (getToken(bin, "c=", mandatory)) {
                String connectionInfo = getLine(bin);
                for (int i = 0; i < descs.size(); i++) {
                    descs.elementAt(i).connectionInfo = connectionInfo;
                }
            }
            
            // Bandwidth Information:
            if (getToken(bin, "b=", false)) {
                String bandwidthInfo = getLine(bin);
                for (int i = 0; i < descs.size(); i++) {
                    descs.elementAt(i).bandwidthInfo = bandwidthInfo;
                }
            }

            // Encryption Key:
            if (getToken(bin, "k=", false)) {
                String encryptionKey = getLine(bin);
                for (int i = 0; i < descs.size(); i++) {
                    descs.elementAt(i).encryptionKey = encryptionKey;
                }
            }

            // Media Attributes:
            boolean found2 = getToken(bin, "a=", false);
            while (found2) {
                line = getLine(bin);
                int index = line.indexOf(':');
                if (index > 0) {
                    String nameAttribute = line.substring(0, index);
                    String valueAttribute = line.substring(index + 1);
                    MediaAttribute attribute = new MediaAttribute(nameAttribute, valueAttribute);
                    // Dispatch for specific payload
                    if ((nameAttribute.equalsIgnoreCase("rtpmap")) && (valueAttribute.indexOf(' ') != -1)) {
                        // add the attribute only for same payload
                        for (int i = 0; i < descs.size(); i++) {
                            if (valueAttribute.startsWith(descs.elementAt(i).payload)) {
                                descs.elementAt(i).mediaAttributes.addElement(attribute);
                            }
                        }
                    } else {
                        for (int i = 0; i < descs.size(); i++) {
                            descs.elementAt(i).mediaAttributes.addElement(attribute);
                        }
                    }
                }
                found2 = getToken(bin, "a=", false);
            }
            
            // Copy in mediaDescriptions
            for (int i = 0; i < descs.size(); i++) {
                mediaDescriptions.addElement((MediaDescription)descs.elementAt(i));
            }
            found = getToken(bin, "m=", false);
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
