package com.orangelabs.rcs.core.ims.protocol.sip;

import gov.nist.core.sip.message.Request;

/**
 * SIP request
 * 
 * @author jexa7410
 */
public class SipRequest extends SipMessage {
	
	/**
	 * Constructor
	 *
	 * @param request SIP stack request
	 */
	public SipRequest(Request request) {
		super(request);
	}

	/**
	 * Return the request object from stack API
	 * 
	 * @return Request object
	 */
	private Request getStackRequest() {
		return (Request)getStackMessage();
	}
	
	/**
	 * Returns the method value
	 * 
	 * @return Method name or null is case of response
	 */
	public String getMethod() {
		return getStackRequest().getMethod();
	}
	
	/**
	 * Return the request URI
	 * 
	 * @return String
	 */
	public String getRequestURI() {
		return getStackRequest().getRequestURI().toString();
	}

	/**
	 * Determine if the message is a SIP response
	 * 
	 * @return Returns True if it's a SIP response else returns False
	 */
	public boolean isSipResponse() {
		return false;
	}
}
