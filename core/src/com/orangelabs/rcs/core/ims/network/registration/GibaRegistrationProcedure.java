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
package com.orangelabs.rcs.core.ims.network.registration;

import android.content.Context;
import android.telephony.TelephonyManager;

import com.orangelabs.rcs.core.ims.ImsModule;
import com.orangelabs.rcs.core.ims.network.sip.SipUtils;
import com.orangelabs.rcs.core.ims.protocol.sip.SipRequest;
import com.orangelabs.rcs.core.ims.protocol.sip.SipResponse;
import com.orangelabs.rcs.platform.AndroidFactory;

/**
 * GIBA or early-IMS registration procedure
 * 
 * @author jexa7410
 */
public class GibaRegistrationProcedure extends RegistrationProcedure {
	/**
	 * IMSI
	 */
	private String imsi;
	
	/**
	 * MNC
	 */
	private String mnc;
	
	/**
	 * MCC
	 */
	private String mcc;
	
	/**
	 * Constructor
	 */
	public GibaRegistrationProcedure() {
		TelephonyManager mgr = (TelephonyManager)AndroidFactory.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
		imsi = mgr.getSubscriberId();
		String mcc_mnc = mgr.getSimOperator();
		mcc = mcc_mnc.substring(0, 3);
		mnc = mcc_mnc.substring(3);
		if (mcc_mnc.length() == 5) { 
			mnc = "0" + mnc;
		}
	}
	
	/**
	 * Returns the home domain name
	 * 
	 * @return Domain name
	 */
	public String getHomeDomain() {
		return "ims.mnc" + mnc + ".mcc" + mcc + ".3gppnetwork.org";
	}
	
	/**
	 * Returns the public URI or IMPU
	 * 
	 * @return Public URI
	 */
	public String getPublicUri() {
		// Derived IMPU from IMSI: <IMSI>@mnc<MNC>.mcc<MCC>.3gppnetwork.org
		return "sip:" + imsi + "@" + getHomeDomain();
	}

	/**
	 * Write the security header to REGISTER request
	 * 
	 * @param request Request
	 */
	public void writeSecurityHeader(SipRequest request) {
		// Nothing to do here
	}

	/**
	 * Read the security header from REGISTER response
	 * 
	 * @param request Request
	 */
	public void readSecurityHeader(SipResponse response) {
		// Read the associated-URI from the 200 OK response
		String addr = response.getHeader("P-Associated-URI");
		// TODO : to be tested with a Tel-URI
		
		// Parse the SIP-URI
		String domain = SipUtils.extractDomainFromAddress(addr);
		String username = SipUtils.extractUsernameFromAddress(addr);		
		
		// Update the user profile
		ImsModule.IMS_USER_PROFILE.setUsername(username);
		ImsModule.IMS_USER_PROFILE.setDisplayName(username);
		ImsModule.IMS_USER_PROFILE.setHomeDomain(domain);
		ImsModule.IMS_USER_PROFILE.setXdmServerLogin("sip:"+ username + "@" + domain);
	}
}
