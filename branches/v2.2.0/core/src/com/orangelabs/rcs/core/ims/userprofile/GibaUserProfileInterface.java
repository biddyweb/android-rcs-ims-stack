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
		// Hardcoded values
		String proxyAddr = RcsSettings.getInstance().getUserProfileImsProxy();
		String xdmServer = RcsSettings.getInstance().getUserProfileXdmServer();
		String xdmPassword = RcsSettings.getInstance().getUserProfileImsPassword();		

		// The user profile will be complete during the registration procedure
		return new UserProfile(
				null, // User name derived from GIBA procedure
				null, // Display name derived from GIBA procedure
				null, // No private ID with GIBA procedure
				null, // No password with GIBA procedure
				null, // Domain derived from GIBA procedure
				proxyAddr,
				xdmServer,
				null, // Login derived from GIBA procedure
				xdmPassword,
				null);
	}
}
