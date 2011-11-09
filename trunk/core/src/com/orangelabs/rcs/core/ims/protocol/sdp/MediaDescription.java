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

	/**
	 * Constructor
	 * 
	 * @param name Media name
	 * @param port Media port
	 * @param protocol Media protocol
	 * @param payload Media payload
	 */
    public MediaDescription(String name, int port, String protocol, String payload) {
        // Media Name and Transport Address:
        this.name = name;
        this.port = port;
        this.protocol = protocol;
        this.payload = payload;
        try {
            this.payload_type = Integer.parseInt(payload);
        } catch (Exception e) {
            this.payload_type = -1;
        }
        this.mediaAttributes = new Vector<MediaAttribute>();
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
