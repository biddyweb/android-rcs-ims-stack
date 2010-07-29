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
package com.orangelabs.rcs.utils;

import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * String utility functions
 */
public class StringUtils {
	/**
	 * Encode string into UTF-8
	 * 
	 * @param text Input text
	 * @return Result
	 */
	public static String encodeUTF8(String text) {
		String result;
		try {
			result = URLEncoder.encode(text, "UTF-8");
		} catch(Exception e) {
			result = text;
		}
		return result;		
	}

	/**
	 * Decode UTF-8 string
	 * 
	 * @param text Input text
	 * @return Result
	 */
	public static String decodeUTF8(String text) {
		String result;
		try {
			result = URLDecoder.decode(text, "UTF-8");
		} catch(Exception e) {
			result = text;
		}
		return result;		
	}
}
