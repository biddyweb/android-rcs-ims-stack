/*******************************************************************************
 * Software Name : RCS IMS Stack
 * Version : 2.0.0
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

/**
 * Phone utility functions
 * 
 * @author jexa7410
 */
public class PhoneUtils {
	/**
	 * International prefix
	 */
	public final static String INTERNATIONAL_PREFIX = "+33";

	/**
	 * Format a phone number to international format
	 * 
	 * @param number Phone number
	 * @return International number
	 */
	public static String formatNumberToInternational(String number) {
		String formattedNumber = "";
		for(int i=0; i < number.length(); i++) {
			char c = number.charAt(i);
			if (c != '-') {
				formattedNumber += c;
			}
		}
		if (!formattedNumber.startsWith("+")) {
			formattedNumber = INTERNATIONAL_PREFIX + formattedNumber.substring(1);
		} else
		if (formattedNumber.startsWith("00")) {
			formattedNumber = INTERNATIONAL_PREFIX + formattedNumber.substring(2);
		}
		return formattedNumber;
	}
	
	/**
	 * Format a phone number to international Tel-URI
	 * 
	 * @param number Phone number
	 * @return Tel-URI
	 */
	public static String formatNumberToTelUri(String number) {
		if (number.startsWith("tel:")) {
			number = number.substring(4);
		} else		
		if (number.startsWith("sip:")) {
			number = number.substring(4, number.indexOf("@"));
		}
		return "tel:" + formatNumberToInternational(number);
	}

	/**
	 * Extract a phone number from a SIP-URI or Tel-URI
	 * 
	 * @param uri SIP or Tel URI
	 * @return Number
	 */
	public static String extractNumberFromUri(String uri) {
		if (uri.startsWith("tel:")) {
			uri = uri.substring(4);
		} else		
		if (uri.startsWith("sip:")) {
			uri = uri.substring(4, uri.indexOf("@"));
		}
		return formatNumberToInternational(uri);
	}
}
