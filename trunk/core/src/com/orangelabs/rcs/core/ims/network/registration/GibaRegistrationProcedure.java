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

		// Parse the SIP-URI
		String domain = SipUtils.extractDomainFromAddress(addr);
		String username = SipUtils.extractUsernameFromAddress(addr);		
		
		// Update the user profile
		ImsModule.IMS_USER_PROFILE.setHomeDomain(domain);
		ImsModule.IMS_USER_PROFILE.setUsername(username);
		ImsModule.IMS_USER_PROFILE.setDisplayName(username);
		ImsModule.IMS_USER_PROFILE.setXdmServerLogin("sip:"+ username + "@sip.ofr.com");
	}
}
