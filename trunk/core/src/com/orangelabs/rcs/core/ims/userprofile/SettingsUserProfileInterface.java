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
import com.orangelabs.rcs.provider.settings.RcsSettings;

/**
 * User profile read from RCS settings database
 * 
 * @author JM. Auffret
 */
public class SettingsUserProfileInterface extends UserProfileInterface {
	/**
	 * Constructor
	 */
	public SettingsUserProfileInterface() {
		super();
	}
	
	/**
	 * Read the user profile
	 * 
	 * @return User profile
	 * @throws CoreException
	 */
	public UserProfile read() throws CoreException {
		String username = RcsSettings.getInstance().getUserProfileUserName(); 
		String displayName = RcsSettings.getInstance().getUserProfileDisplayName();
		String privateID = RcsSettings.getInstance().getUserProfilePrivateId();
		String password = RcsSettings.getInstance().getUserProfilePassword();
		
		String homeDomain = RcsSettings.getInstance().getUserProfileDomain();
		String proxyAddr = RcsSettings.getInstance().getUserProfileProxy();
		
		String xdmServer = RcsSettings.getInstance().getUserProfileXdmServer();
		String xdmLogin = RcsSettings.getInstance().getUserProfileXdmLogin();
		String xdmPassword = RcsSettings.getInstance().getUserProfileXdmPassword();
		
		String imConfUri = RcsSettings.getInstance().getUserProfileImConferenceUri();

		return new UserProfile(username, displayName, privateID, password,
				homeDomain, proxyAddr,
				xdmServer, xdmLogin, xdmPassword,
				imConfUri);
	}
}
