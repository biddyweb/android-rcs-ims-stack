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
package com.orangelabs.rcs.core;

import android.content.Context;
import android.telephony.TelephonyManager;

import com.orangelabs.rcs.core.ims.network.registration.RegistrationManager;
import com.orangelabs.rcs.core.ims.userprofile.UserProfileNotProvisionnedException;
import com.orangelabs.rcs.platform.AndroidFactory;
import com.orangelabs.rcs.platform.registry.RegistryFactory;
import com.orangelabs.rcs.provider.eab.RichAddressBookProvider;
import com.orangelabs.rcs.provider.settings.RcsSettings;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * User account manager
 * 
 * @author jexa7410
 */
public class UserAccountManager {
	
	/**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());
    
    /**
	 * Constructor
	 * 
	 * @throws CoreException
	 */
	public UserAccountManager() throws CoreException {
		// Read the last used end user account
		String lastUserAccount = RegistryFactory.getFactory().readString("LastUserAccount", null);

		// Current user account is either read from RCS settings (not GIBA) or IMSI related (GIBA)
		String currentUserAccount = null;
		
		if (RegistrationManager.isGibaAuthent()) {
			// Read the current IMSI
			TelephonyManager mgr = (TelephonyManager)AndroidFactory.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
			String imsi = mgr.getSubscriberId();
			if (imsi == null) {
				if (logger.isActivated()) {
					logger.error("No valid SIM card");
				}
				throw new CoreException("Unable to read IMSI");
			}

			if (logger.isActivated()) {
				logger.info("My IMSI is " + imsi);
			}
			
			currentUserAccount = imsi;
		} else {
			// Test if user profile provisionning has been done or not
			// HTTP Digest authent based on user settings
            String values[] = new String[6];
    		values[0] = RcsSettings.getInstance().getUserProfileImsUserName();
    		values[1] = RcsSettings.getInstance().getUserProfileImsPrivateId();
    		values[2] = RcsSettings.getInstance().getUserProfileImsDomain();
    		values[3] = RcsSettings.getInstance().getUserProfileImsProxy();
    		values[4] = RcsSettings.getInstance().getUserProfileXdmServer();
    		values[5] = RcsSettings.getInstance().getUserProfileXdmLogin();
    		for(int i=0; i < values.length; i++) {
    			if ((values[i] == null) || (values[i].length() == 0)) {
    				throw new UserProfileNotProvisionnedException("User profile attribute " + i + " is not initialized");
    			}        
    		}
    		// User profile name is +.., we need to remove the '+' to have only digits
    		currentUserAccount = values[0].substring(1);
		}
		
		// We may have to change the current user if we are using GIBA, hence using IMSI for end user profile
		RichAddressBookProvider.getInstance().setCurrentUser(currentUserAccount, lastUserAccount);
		
		// Save the current end user account
		RegistryFactory.getFactory().writeString("LastUserAccount", currentUserAccount);
     }

}
