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

package com.orangelabs.rcs.utils;

import android.content.Context;
import android.telephony.PhoneNumberUtils;

import com.orangelabs.rcs.core.ims.ImsModule;
import com.orangelabs.rcs.provider.settings.RcsSettings;

/**
 * Phone utility functions
 * 
 * @author jexa7410
 */
public class PhoneUtils {
	/**
	 * Tel-URI format
	 */
	private static boolean TEL_URI_SUPPORTED = true;

	/**
	 * Country code
	 */
	private static String COUNTRY_CODE = "+33";
	
	/**
	 * Set the country code
	 * 
	 * @param context Context
	 */
	public static synchronized void initialize(Context context) {
		RcsSettings.createInstance(context);
		TEL_URI_SUPPORTED = RcsSettings.getInstance().isTelUriFormatUsed();
		COUNTRY_CODE = RcsSettings.getInstance().getCountryCode();
	}

	/**
	 * Returns the country code
	 * 
	 * @return Country code
	 */
	public static String getCountryCode() {
		return COUNTRY_CODE;
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
		
		// Remove spaces
		number = number.trim();

		// Strip all non digits
		String phoneNumber = PhoneNumberUtils.stripSeparators(number);		

		// Format into international
		if (phoneNumber.startsWith("0033")) {
			phoneNumber = COUNTRY_CODE + phoneNumber.substring(4);
		} else
		if (COUNTRY_CODE.equals("+33") && phoneNumber.startsWith("0")) {
			phoneNumber = COUNTRY_CODE + phoneNumber.substring(1);
		} else
		if (!phoneNumber.startsWith("+")) {
			phoneNumber = COUNTRY_CODE + phoneNumber;
		}
		return phoneNumber;
	}
	
	/**
	 * Format a phone number to a SIP URI
	 * 
	 * @param number Phone number
	 * @return SIP URI
	 */
	public static String formatNumberToSipUri(String number) {
		if (number == null) {
			return null;
		}

		// Remove spaces
		number = number.trim();
		
		// Extract username part
		if (number.startsWith("tel:")) {
			number = number.substring(4);
		} else		
		if (number.startsWith("sip:")) {
			number = number.substring(4, number.indexOf("@"));
		}
		
		if (TEL_URI_SUPPORTED) {
			// Tel-URI format
			return "tel:" + formatNumberToInternational(number);
		} else {
			// SIP-URI format
			return "sip:" + formatNumberToInternational(number) + "@" +
				ImsModule.IMS_USER_PROFILE.getHomeDomain() + ";user=phone";	 
		}
	}

	/**
	 * Format a phone number to a SIP address
	 * 
	 * @param number Phone number
	 * @return SIP address
	 */
	public static String formatNumberToSipAddress(String number) {
		String addr = formatNumberToSipUri(number);	 
		String displayName = RcsSettings.getInstance().getUserProfileImsDisplayName();
		if ((displayName != null) && (displayName.length() > 0)) {
			addr = "\"" + displayName + "\" <" + addr + ">"; 
		}
		return addr;
	}
	
	/**
	 * Extract user part phone number from a SIP-URI or Tel-URI or SIP address
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
			
			// Remove URI parameters
			int index2 = uri.indexOf(";"); 
			if (index2 != -1) {
				uri = uri.substring(0, index2);
			}
			
			// Format the extracted number (username part of the URI)
			return formatNumberToInternational(uri);
		} catch(Exception e) {
			return null;
		}
	}

	/**
	 * Extract display name from URI
	 * 
	 * @param uri URI
	 * @return Display name or null
	 */
	public static String extractDisplayNameFromUri(String uri) {
		if (uri == null) {
			return null;
		}

		try {
			int index0 = uri.indexOf("\"");
			if (index0 != -1) {
				int index1 = uri.indexOf("\"", index0+1);
				if (index1 > 0) {
					return uri.substring(index0+1, index1);
				}
			}			
			
			return null;
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
		String number1 = PhoneUtils.extractNumberFromUri(contact1);
		String number2 = PhoneUtils.extractNumberFromUri(contact2);
		if ((number1 == null) || (number2 == null)) {
			return false;
		}
		return number1.equals(number2);
	}
}
