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

import gov.nist.core.sip.address.Address;
import gov.nist.core.sip.address.SipURI;
import gov.nist.core.sip.address.TelURL;
import gov.nist.core.sip.address.URI;
import android.content.Context;
import android.telephony.TelephonyManager;

import com.orangelabs.rcs.core.ims.ImsModule;
import com.orangelabs.rcs.core.ims.network.sip.SipUtils;
import com.orangelabs.rcs.core.ims.protocol.sip.SipRequest;
import com.orangelabs.rcs.core.ims.protocol.sip.SipResponse;
import com.orangelabs.rcs.platform.AndroidFactory;
import com.orangelabs.rcs.utils.logger.Logger;

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
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());

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
		try {
			// Read the associated-URI from the 200 OK response
			String sipAddr = response.getHeader("P-Associated-URI");
			
			// Parse the SIP-URI
			Address addr = SipUtils.ADDR_FACTORY.createAddress(sipAddr);
			String domain = addr.getHost();
			String username = null;
			URI uri = addr.getURI();
			if (uri instanceof SipURI) {
				SipURI sip = (SipURI)addr.getURI();
				username = sip.getAuthority().getUserInfo().getUser();				
			} else
			if (uri instanceof TelURL) {
				TelURL tel = (TelURL)addr.getURI();
				username = tel.getPhoneNumber();
				if (tel.isGlobal()) {
					username = "+"+username;
				}
			}
			
			// Update the user profile
			ImsModule.IMS_USER_PROFILE.setUsername(username);
			ImsModule.IMS_USER_PROFILE.setDisplayName(username);
			ImsModule.IMS_USER_PROFILE.setHomeDomain(domain);
			ImsModule.IMS_USER_PROFILE.setXdmServerLogin("sip:"+ username + "@" + domain);
		} catch(Exception e) {
			if (logger.isActivated()) {
				logger.error("Can't create the P-Associated-URI header", e);
			}
		}
	}
}
