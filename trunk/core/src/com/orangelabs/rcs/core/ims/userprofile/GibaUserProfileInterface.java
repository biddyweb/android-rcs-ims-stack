package com.orangelabs.rcs.core.ims.userprofile;

import com.orangelabs.rcs.core.CoreException;

/**
 * User profile derived from IMSI
 * 
 * @author JM. Auffret
 */
public class GibaUserProfileInterface extends UserProfileInterface {
	/**
	 * Constructor
	 */
	public GibaUserProfileInterface() {
		super();	
	}
	
	/**
	 * Read the user profile
	 * 
	 * @return User profile
	 * @throws CoreException
	 */
	public UserProfile read() throws CoreException {
		String username = null; 
		String displayName = null;
		String privateID = null;
		String password = null;
		String homeDomain = null;
		
		// IMS proxy is hardcoded
		// TODO: to be retrieved via DHCP
		String proxyAddr = "80.12.197.168:5060";

		// XDMS is hardcoded
		String xdmServer = "10.194.117.38:8080/services";
		String xdmLogin = null;
		String xdmPassword = "nsnims2008";
		
		return new UserProfile(username, displayName,
				privateID, password,
				homeDomain, proxyAddr,
				xdmServer, xdmLogin, xdmPassword);
	}
}
