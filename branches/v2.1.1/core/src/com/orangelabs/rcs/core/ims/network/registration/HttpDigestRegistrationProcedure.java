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

import com.orangelabs.rcs.core.CoreException;
import com.orangelabs.rcs.core.ims.ImsModule;
import com.orangelabs.rcs.core.ims.network.sip.SipUtils;
import com.orangelabs.rcs.core.ims.protocol.sip.SipRequest;
import com.orangelabs.rcs.core.ims.protocol.sip.SipResponse;
import com.orangelabs.rcs.core.ims.security.HttpDigestMd5Authentication;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * HTTP Digest MD5 registration procedure
 * 
 * @author jexa7410
 */
public class HttpDigestRegistrationProcedure extends RegistrationProcedure {
	/**
	 * HTTP Digest MD5 agent
	 */
	private HttpDigestMd5Authentication digest = new HttpDigestMd5Authentication();

	/**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());

    /**
	 * Constructor
	 */
	public HttpDigestRegistrationProcedure() {
	}

	/**
	 * Returns the home domain name
	 * 
	 * @return Domain name
	 */
	public String getHomeDomain() {
		return ImsModule.IMS_USER_PROFILE.getHomeDomain();
	}

	/**
	 * Returns the public URI or IMPU
	 * 
	 * @return Public URI
	 */
	public String getPublicUri() {
		return ImsModule.IMS_USER_PROFILE.getPublicUri();
	}

	/**
	 * Write security header to REGISTER request
	 * 
	 * @param request Request
	 * @throws CoreException
	 */
	public void writeSecurityHeader(SipRequest request) throws CoreException {
		if ((digest.getRealm() == null) || (digest.getNextnonce() == null)) {
			return;
		}
		
		try {
	   		// Update nonce parameters
			digest.updateNonceParameters();
			
			// Calculate response
			String user = ImsModule.IMS_USER_PROFILE.getPrivateID();
			String password = ImsModule.IMS_USER_PROFILE.getPassword();
	   		String response = digest.calculateResponse(user,
	   				password,
	   				request.getMethod(),
	   				request.getRequestURI(),
					digest.buildNonceCounter(),
					request.getContent());				

	   		// Build the Authorization header
			String auth = "Authorization: Digest username=\"" + ImsModule.IMS_USER_PROFILE.getPrivateID() + "\"" +
					",uri=\"" + request.getRequestURI() + "\"" +
					",algorithm=MD5" +
					",realm=\"" + digest.getRealm() + "\"" +
					",nonce=\"" + digest.getNonce() + "\"" +
					",response=\"" + response + "\"";
			String qop = digest.getQop();
			if ((qop != null) && qop.startsWith("auth")) {	
				auth += ",nc=" + digest.buildNonceCounter() +
						",qop=" + qop +
						",cnonce=\"" + digest.getCnonce() + "\"";
			}
			
			// Set header in the SIP message 
			request.addHeader(auth);
		} catch(Exception e) {
			if (logger.isActivated()) {
				logger.error("Can't create the authorization header", e);
			}
			throw new CoreException("Can't write the security header");
		}
    }

	/**
	 * Read security header from REGISTER response
	 * 
	 * @param response SIP response
	 * @throws CoreException
	 */
	public void readSecurityHeader(SipResponse response) throws CoreException {
		String wwwHeader = response.getHeader("WWW-Authenticate");
		String infoHeader =  response.getHeader("Authentication-Info");

		if (wwwHeader != null) {
			// Retrieve data from the header WWW-Authenticate (401 response)
			try {
				// Get domain name
				digest.setRealm(response.getHeaderParameter("WWW-Authenticate", "realm"));
	
				// Get qop
		   		digest.setQop(response.getHeaderParameter("WWW-Authenticate", "qop"));
		   		
		   		// Get nonce to be used
		   		digest.setNextnonce(response.getHeaderParameter("WWW-Authenticate", "nonce"));
			} catch(Exception e) {
				if (logger.isActivated()) {
					logger.error("Can't read the WWW-Authenticate header", e);
				}
				throw new CoreException("Can't read the security header");
			}
		} else
		if (infoHeader != null) {
			// Retrieve data from the header Authentication-Info (200 OK response)
			try {
				// Get nextnonce to be used
		   		digest.setNextnonce(SipUtils.extractHeaderParamater(infoHeader, "nextnonce"));
			} catch(Exception e) {
				if (logger.isActivated()) {
					logger.error("Can't read the authentication-info header", e);
				}
				throw new CoreException("Can't read the security header");
			}
		}
	}
}
