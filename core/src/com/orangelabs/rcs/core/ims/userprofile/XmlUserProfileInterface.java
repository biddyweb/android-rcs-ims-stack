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
		
		String imConfUri = config.getString("ImConferenceUri");

		return new UserProfile(username, displayName, privateID, password,
				homeDomain, proxyAddr,
				xdmServer, xdmLogin, xdmPassword,
				imConfUri);
	}
}
