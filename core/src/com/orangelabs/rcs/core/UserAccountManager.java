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

import android.accounts.Account;
import android.content.Context;
import android.telephony.TelephonyManager;

import com.orangelabs.rcs.R;
import com.orangelabs.rcs.addressbook.AuthenticationService;
import com.orangelabs.rcs.core.ims.network.registration.RegistrationManager;
import com.orangelabs.rcs.core.ims.userprofile.UserProfileNotProvisionnedException;
import com.orangelabs.rcs.platform.AndroidFactory;
import com.orangelabs.rcs.platform.registry.RegistryFactory;
import com.orangelabs.rcs.provider.settings.RcsSettings;
import com.orangelabs.rcs.service.RcsCoreService;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * User account manager
 * 
 * @author jexa7410
 */
public class UserAccountManager {
	/**
	 * First launch flag
	 */
	private static final String REGISTRY_FIRST_LAUNCH = "FirstLaunch";
	
	/**
	 * Last user account used
	 */
	private static final String REGISTRY_LAST_USER_ACCOUNT = "LastUserAccount";
	
	/**
	 * Current account
	 */
	private String currentUserAccount;
	
	/**
	 * Last previously used account
	 */
	private String lastUserAccount;
	
	/**
	 * First launch
	 */
	private boolean isFirstLaunch;
	
	/**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());
    
    /**
	 * Constructor
	 */
	public UserAccountManager() throws CoreException {
		if (logger.isActivated()) {
			logger.info("Address book manager is created");
		}
		
		// Read the first launched 
		isFirstLaunch = RegistryFactory.getFactory().readBoolean(REGISTRY_FIRST_LAUNCH, true);
		
		// Read the last used end user account
		lastUserAccount = RegistryFactory.getFactory().readString(REGISTRY_LAST_USER_ACCOUNT, null);

		// Current user account is either read from RCS settings (not GIBA) or IMSI related (GIBA)
		currentUserAccount = null;
		
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
		
		// Save the current end user account
		RegistryFactory.getFactory().writeString(REGISTRY_LAST_USER_ACCOUNT, currentUserAccount);
		
		// Check that the RCS account exists
		boolean rcsAccountExists = false;
		Account mAccount = AuthenticationService.getAccount(RcsCoreService.CONTEXT, RcsCoreService.CONTEXT.getString(R.string.rcs_core_account_username));
		if (mAccount!=null){
			rcsAccountExists = true;
		}
		
		// If it is the first launch or if the RCS account does not exist, create a new RCS account
		if (isFirstLaunch || !rcsAccountExists){

	        if (logger.isActivated()){
	        	logger.debug("Creating a new RCS account for "+currentUserAccount);
	        }
	        
	        AuthenticationService.createRcsAccount(RcsCoreService.CONTEXT, RcsCoreService.CONTEXT.getString(R.string.rcs_core_account_username), true, true);
			
			// Set the value to false in the registry for subsequent calls
			RegistryFactory.getFactory().writeBoolean(REGISTRY_FIRST_LAUNCH, false);
		}else if (hasChangedAccount()){
			// Else we have changed account, delete the current RCS account and create a new one
	        if (logger.isActivated()){
	        	logger.debug("Deleting the old RCS account for "+lastUserAccount);
	        }
	        AuthenticationService.removeRcsAccount(RcsCoreService.CONTEXT, lastUserAccount);

	        if (logger.isActivated()){
	        	logger.debug("Creating a new RCS account for "+currentUserAccount);
	        }
	        AuthenticationService.createRcsAccount(RcsCoreService.CONTEXT, RcsCoreService.CONTEXT.getString(R.string.rcs_core_account_username), true, true);
		}
		
     }
	
	/**
	 * Check if it is the first time we launch the service
	 * 
	 * @return true if this is the first launch
	 */
	public boolean isFirstLaunch(){
		return isFirstLaunch;
	}
	
	/**
	 * Check if we changed account since the last time we started the service
	 * 
	 * @return true if the active account was changed
	 */
	public boolean hasChangedAccount(){
		if (currentUserAccount==null){
			return (lastUserAccount!=null);
		}else{
			return (!currentUserAccount.equalsIgnoreCase(lastUserAccount));
		}
	}

}
