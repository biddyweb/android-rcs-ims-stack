package com.orangelabs.rcs.core.ims.protocol.sip;

/**
 * SIP listener interface
 * 
 * @author jexa7410
 */
public interface SipListener {
	/**
	 * Receive SIP request
	 * 
	 * @param request SIP request
	 */
	void receiveSipRequest(SipRequest request);
}
