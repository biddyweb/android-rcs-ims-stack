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
package com.orangelabs.rcs.core.ims.service.presence.xdm;

import com.orangelabs.rcs.core.CoreException;
import com.orangelabs.rcs.core.ims.ImsModule;
import com.orangelabs.rcs.core.ims.security.HttpDigestMd5Authentication;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * HTTP Digest MD5 authentication agent
 * 
 * @author JM. Auffret
 */
public class HttpAuthenticationAgent {

	/**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());

	/**
	 * HTTP Digest MD5 agent
	 */
	private HttpDigestMd5Authentication digest = new HttpDigestMd5Authentication();

	/**
	 * Constructor
	 */
	public HttpAuthenticationAgent() {
	}

	/**
	 * Generate the authorization header
	 * 
	 * @param method Method used
	 * @param requestUri Request Uri
	 * @param body Entity body
	 * @return authorizationHeader Authorization header
	 * @throws CoreException
	 */
	public String generateAuthorizationHeader(String method, String requestUri, String body) throws CoreException {
		try {
	   		// Update nonce parameters
			digest.updateNonceParameters();
			
			// Calculate response
			String user = ImsModule.IMS_USER_PROFILE.getXdmServerLogin();
			String password = ImsModule.IMS_USER_PROFILE.getXdmServerPassword();
	   		String response = digest.calculateResponse(user, password,
	   				method,
	   				requestUri,
					digest.buildNonceCounter(),
					body);				

	   		// Build the Authorization header
			String auth = "Authorization: Digest username=\"" + ImsModule.IMS_USER_PROFILE.getXdmServerLogin() + "\"" +
					",realm=\"" + digest.getRealm() + "\"" +
					",nonce=\"" + digest.getNonce() + "\"" +
					",uri=\"" + requestUri + "\"";
			String qop = digest.getQop();
			if ((qop != null) && qop.startsWith("auth")) {	
				auth += ",qop=\"" + qop + "\"" +
						",nc=" + digest.buildNonceCounter() +						
						",cnonce=\"" + digest.getCnonce() + "\""+
						",response=\"" + response + "\"";
			}
			return auth;
		} catch(Exception e) {
			if (logger.isActivated()) {
				logger.error("Can't create the authorization header", e);
			}
			throw new CoreException("Can't create the authorization header");
		}
    }

	/**
	 * Read the WWW-Authenticate header
	 * 
	 * @param header WWW-Authenticate header
	 */
	public void readWwwAuthenticateHeader(String header) {		
		if (header != null) {
	   		// Get domain name
			int realmBegin = header.toLowerCase().indexOf("realm=\"")+7;
			int realmEnd = -1;
			if (realmBegin != -1){
				realmEnd = header.substring(realmBegin).indexOf("\"");
			}			
			String realm = null;
			if (realmEnd != -1){
				realmEnd += realmBegin;
				realm = header.substring(realmBegin, realmEnd);
			}
			digest.setRealm(realm);

			// Get qop
			int qopBegin = header.toLowerCase().indexOf("qop=\"");
			if (qopBegin != -1) {
				qopBegin += 5;
				int qopEnd = header.indexOf("\"", qopBegin);
				String qop = header.substring(qopBegin, qopEnd);
				int index = qop.indexOf(",");
				if (index != -1) {
					qop = qop.substring(0, index);
				}
				digest.setQop(qop);
			}

			// Get nonce to be used
			int nextnonceBegin = header.toLowerCase().indexOf("nonce=\"")+7;
			int nextnonceEnd = -1;
			if (nextnonceBegin != -1){
				nextnonceEnd = header.substring(nextnonceBegin).indexOf("\"");
			}
			String nextnonce = null;
			if (nextnonceEnd != -1){
				nextnonceEnd += nextnonceBegin;
				nextnonce = header.substring(nextnonceBegin, nextnonceEnd);
			}
			digest.setNextnonce(nextnonce);
		}
	}
}
