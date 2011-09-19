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

package com.orangelabs.rcs.core.ims.protocol.rtp.format;

/**
 * Geoloc format 
 * 
 * @author jexa7410
 */
public class GeolocFormat extends Format {
	
	/**
	 * Encoding name
	 */
	public static final String ENCODING = "geoloc";
	
	/**
	 * Payload type
	 */
	public static final int PAYLOAD = 98;

	/**
	 * Constructor
	 */
	public GeolocFormat() {
		super(ENCODING, PAYLOAD);
	}
	
    /**
     * Constructor
     * 
     * @param codec Codec
     * @param payload Payload type
     */
    public GeolocFormat(String codec, int payload) {
    	super(codec, payload);
    }	
}
