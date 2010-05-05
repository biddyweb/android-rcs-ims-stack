package com.orangelabs.rcs.core.ims.service;

import com.orangelabs.rcs.core.CoreException;
import com.orangelabs.rcs.core.ims.ImsModule;
import com.orangelabs.rcs.core.ims.protocol.sip.SipRequest;
import com.orangelabs.rcs.core.ims.protocol.sip.SipResponse;
import com.orangelabs.rcs.core.ims.security.HttpDigestMd5Authentication;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * HTTP Digest MD5 authentication agent for sessions
 * 
 * @author JM. Auffret
 */
public class SessionAuthenticationAgent {
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
	public SessionAuthenticationAgent() {
		super();
	}

	/**
	 * Set the proxy authorization header on the INVITE request
	 * 
	 * @param request SIP request
	 * @throws CoreException
	 */
	public void setProxyAuthorizationHeader(SipRequest request) throws CoreException {
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
			String auth = "Proxy-Authorization: " +
				"Digest username=\"" + ImsModule.IMS_USER_PROFILE.getPrivateID() +
				"\",uri=\"" + request.getRequestURI() +
				"\",algorithm=MD5,realm=\"" + digest.getRealm() +
				"\",nc=" + digest.buildNonceCounter() +
				",nonce=\"" + digest.getNonce() +
				"\",response=\"" + response +
				"\",cnonce=\"" + digest.getCnonce() + "\"";
			String qop = digest.getQop();
			if (qop != null) {
				auth += ",qop=" + qop;
			}
			
			// Set header in the SIP message 
			request.addHeader(auth);

		} catch(Exception e) {
			if (logger.isActivated()) {
				logger.error("Can't create the proxy authorization header", e);
			}
			throw new CoreException("Can't create the proxy authorization header");
		}
    }

	/**
	 * Read parameters of the Proxy-Authenticate header
	 * 
	 * @param response SIP response
	 */
	public void readProxyAuthenticateHeader(SipResponse response) {
		// Retrieve data from the header Proxy-Authenticate
		String header = response.getHeader("Proxy-Authenticate");
		if (header != null) {
	   		// Get domain name
			digest.setRealm(response.getHeaderParameter("Proxy-Authenticate", "realm"));

			// Get qop
			digest.setQop(response.getHeaderParameter("Proxy-Authenticate", "qop"));
	   		
	   		// New nonce to be used
			digest.setNextnonce(response.getHeaderParameter("Proxy-Authenticate", "nonce"));
		}
	}	
}
