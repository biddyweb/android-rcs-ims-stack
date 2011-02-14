/*******************************************************************************
 * Software Name : RCS IMS Stack
 * Version : 2.0
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
package com.orangelabs.rcs.core.ims.service.presence.xdm;


/**
 * HTTP utility functions
 */
public class HttpUtils {
	/**
	 * CRLF constant
	 */
	public final static String CRLF = "\r\n";
	
	/**
	 * Encode special characters in URL
	 * 
	 * @param url URL to be encoded
	 * @return Encoded URL
	 */
	public static String encodeURL(String url) {
		StringBuffer res = new StringBuffer(url.length());
		char readChar;
		for (int i = 0; i < url.length(); i++) {
			readChar = url.charAt(i);
			switch (readChar) {
			case '+':
				res.append("%2B");
				break;
			case '@':
				res.append("%40");
				break;
			case ':':
				res.append("%3A");
				break;
			default:
				res.append(readChar);
				break;
			}
		}
		return res.toString();
	}
}
