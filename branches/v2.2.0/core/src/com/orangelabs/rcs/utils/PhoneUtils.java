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

import android.content.Context;
import android.telephony.TelephonyManager;

import com.orangelabs.rcs.core.ims.ImsModule;

/**
 * Phone utility functions
 * 
 * @author jexa7410
 */
public class PhoneUtils {
	/**
	 * Tel-URI format supported by the platform
	 */
	public static boolean TEL_URI_SUPPORTED = false;
	
	/**
	 * Country code
	 */
	public static String COUNTRY_CODE = "33";

	/**
	 * Set the country code
	 * 
	 * @param context Context 
	 */
	public static void setCountryCode(Context context) {
		if (context == null) {
			return;
		}

		TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		String cc = tm.getSimCountryIso();
		if (cc.startsWith("+")) {
			COUNTRY_CODE = cc.substring(1); 
		} else {
			if (cc.equalsIgnoreCase("fr")) {
				COUNTRY_CODE = "33"; 
			} else
			if (cc.equalsIgnoreCase("cn")) {
				COUNTRY_CODE = "86"; 
			} else
			if (cc.equalsIgnoreCase("es")) {
				COUNTRY_CODE = "34"; 
			} else
			if (cc.equalsIgnoreCase("fi")) {
				COUNTRY_CODE = "358"; 
			} else
			if (cc.equalsIgnoreCase("us")) {
				COUNTRY_CODE = "1"; 
			}
			// TODO: how to generalize the mapping table
		}
	}

	/**
	 * Format a phone number to international format
	 * 
	 * @param number Phone number
	 * @return International number
	 */
	public static String formatNumberToInternational(String number) {
		if (number == null) {
			return null;
		}
		
		// Remove space
		number = number.trim();

		// Strip all non-numbers
		String phoneNumber = "";
		for(int i=0; i < number.length(); i++) {
			char c = number.charAt(i);
			if (Character.isDigit(c) || (c == '+')) {
				phoneNumber += c;
			}
		}

		// TODO: see RFC to format into international number
		if (phoneNumber.startsWith("0")) {
			phoneNumber = "+" + COUNTRY_CODE + phoneNumber.substring(1);
		} else
		if (!phoneNumber.startsWith("+")) {
			if (phoneNumber.startsWith(COUNTRY_CODE)) {
				phoneNumber = "+" + phoneNumber;
			} else {
				phoneNumber = "+" + COUNTRY_CODE + phoneNumber;
			}
		}
		return phoneNumber;
	}
	
	/**
	 * Format a phone number to a SIP address (SIP-URI or Tel-URI)
	 * 
	 * @param number Phone number
	 * @return SIP address
	 */
	public static String formatNumberToSipAddress(String number) {
		if (number == null) {
			return null;
		}

		number = number.trim();
		
		if (number.startsWith("tel:")) {
			number = number.substring(4);
		} else		
		if (number.startsWith("sip:")) {
			number = number.substring(4, number.indexOf("@"));
		}
		
		if (TEL_URI_SUPPORTED) {
			return "tel:" + formatNumberToInternational(number);
		} else {
			return "sip:" + formatNumberToInternational(number) + "@" +
				ImsModule.IMS_USER_PROFILE.getHomeDomain() + ";user=phone";
		}
	}

	/**
	 * Extract a user part number from a SIP-URI or Tel-URI
	 * 
	 * @param uri SIP or Tel URI
	 * @return Number or null in case of error
	 */
	public static String extractNumberFromUri(String uri) {
		if (uri == null) {
			return null;
		}

		try {
			// Extract URI from address
			int index0 = uri.indexOf("<");
			if (index0 != -1) {
				uri = uri.substring(index0+1, uri.indexOf(">", index0));
			}			
			
			// Extract a Tel-URI
			int index1 = uri.indexOf("tel:");
			if (index1 != -1) {
				uri = uri.substring(index1+4);
			}
			
			// Extract a SIP-URI
			index1 = uri.indexOf("sip:");
			if (index1 != -1) {
				int index2 = uri.indexOf("@", index1);
				uri = uri.substring(index1+4, index2);
			}
			
			// Format the extracted number (username part of the URI)
			return formatNumberToInternational(uri);
		} catch(Exception e) {
			return null;
		}
	}

	/**
	 * Compare phone number between two contacts
	 * 
	 * @param contact1 First contact
	 * @param contact2 Second contact
	 * @return Returns true if numbers are equals
	 */
	public static boolean compareNumbers(String contact1, String contact2) {
		if ((contact1 == null) || (contact2 == null)) {
			return false;
		}
		
		String number1 = PhoneUtils.extractNumberFromUri(contact1);
		String number2 = PhoneUtils.extractNumberFromUri(contact2);
		if ((number1 == null) || (number2 == null)) {
			return false;
		}

		return number1.equals(number2);
	}
}
