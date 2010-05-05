package com.orangelabs.rcs.core.ims.network.registration;

import com.orangelabs.rcs.core.CoreException;
import com.orangelabs.rcs.core.ims.protocol.sip.SipRequest;
import com.orangelabs.rcs.core.ims.protocol.sip.SipResponse;

/**
 * Abstract registration procedure
 * 
 * @author jexa7410
 */
public abstract class RegistrationProcedure {
	/**
	 * Returns the home domain name
	 * 
	 * @return Domain name
	 */
	public abstract String getHomeDomain(); 
	
	/**
	 * Returns the public URI or IMPU
	 * 
	 * @return Public URI
	 */
	public abstract String getPublicUri();
	
	/**
	 * Write the security header to REGISTER request
	 * 
	 * @param request Request
	 * @throws CoreException
	 */
	public abstract void writeSecurityHeader(SipRequest request) throws CoreException;

	/**
	 * Read the security header from REGISTER response
	 * 
	 * @param request Request
	 * @throws CoreException
	 */
	public abstract void readSecurityHeader(SipResponse response)throws CoreException;
}
