package com.orangelabs.rcs.core.ims.userprofile;

import com.orangelabs.rcs.core.CoreException;
import com.orangelabs.rcs.utils.Config;

/**
 * User profile read from XML file user_profile.xml
 * 
 * @author JM. Auffret
 */
public class XmlUserProfileInterface extends UserProfileInterface {
	/**
	 * Constructor
	 */
	public XmlUserProfileInterface() {
		super();
	}
	
	/**
	 * Read the user profile
	 * 
	 * @return User profile
	 * @throws CoreException
	 */
	public UserProfile read() throws CoreException {
		Config config = new Config("user_profile.xml");
		
		String username = config.getString("Username"); 
		String displayName = config.getString("DisplayName");
		String privateID = config.getString("PrivateId");
		String password = config.getString("Password");
		
		String homeDomain = config.getString("HomeDomain");
		String proxyAddr = config.getString("OutboundProxyAddr");
		
		String xdmServer = config.getString("XdmServerAddr");
		String xdmLogin = config.getString("XdmServerLogin");
		String xdmPassword = config.getString("XdmServerPassword");
		
		return new UserProfile(username, displayName, privateID, password,
				homeDomain, proxyAddr,
				xdmServer, xdmLogin, xdmPassword);
	}
}
