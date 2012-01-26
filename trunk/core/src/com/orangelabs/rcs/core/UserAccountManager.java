/*******************************************************************************
 * Software Name : RCS IMS Stack
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

import com.orangelabs.rcs.R;
import com.orangelabs.rcs.addressbook.AccountChangedReceiver;
import com.orangelabs.rcs.addressbook.AuthenticationService;
import com.orangelabs.rcs.platform.AndroidFactory;
import com.orangelabs.rcs.platform.registry.RegistryFactory;
import com.orangelabs.rcs.provider.eab.ContactsManager;
import com.orangelabs.rcs.provider.settings.RcsSettings;
import com.orangelabs.rcs.utils.DeviceUtils;
import com.orangelabs.rcs.utils.logger.Logger;

import android.accounts.Account;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Handler;
import android.telephony.TelephonyManager;

/**
 * User account manager
 * 
 * @author jexa7410
 */
public class UserAccountManager {
	/**
	 * Last user account used
	 */
	public static final String REGISTRY_LAST_USER_ACCOUNT = "LastUserAccount";
	
	/**
	 * Current account
	 */
	private String currentUserAccount = null;
	
	/**
	 * Last previously used account
	 */
	private String lastUserAccount = RegistryFactory.getFactory().readString(REGISTRY_LAST_USER_ACCOUNT, null);
	
	/**
	 * Account changed broadcast receiver
	 */
	private AccountChangedReceiver accountChangedReceiver;

	/**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());
    
    /**
	 * Constructor
	 * 
     * @param core Core
   	 */
	public UserAccountManager(Core core) throws CoreException {
        // Read the last used end user account
		lastUserAccount = RegistryFactory.getFactory().readString(REGISTRY_LAST_USER_ACCOUNT, null);
		if (logger.isActivated()) {
			logger.info("Last user account is " + lastUserAccount);
		}
		
        // Read the IMSI
		TelephonyManager mgr = (TelephonyManager)AndroidFactory.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
		String imsi = mgr.getSubscriberId();
		if (logger.isActivated()) {
			logger.info("My IMSI is " + imsi);
		}
		
		// Check IMSI
		if (imsi == null) {
			if (isFirstLaunch()) {
				// If it's a first launch the IMSI is necessary to initialize the service the first time
				throw new UserAccountException("IMSI not found");
			} else {
				// Set the user account ID from the last used IMSI
				currentUserAccount = lastUserAccount;
			}
		} else {
			// Set the user account ID from the IMSI
			currentUserAccount = imsi;
		}

        // On the first launch and if SIM card has changed
        if (isFirstLaunch() || hasChangedAccount()) {
            // Set the country code
            setCountryCode();
        }

        if (logger.isActivated()) {
			logger.info("My user account is " + currentUserAccount);
		}
		
		// Save the current end user account
		RegistryFactory.getFactory().writeString(REGISTRY_LAST_USER_ACCOUNT, currentUserAccount);

		// Check if the RCS account exists
		Account account = AuthenticationService.getAccount(AndroidFactory.getApplicationContext(),
				AndroidFactory.getApplicationContext().getString(R.string.rcs_core_account_username));
		if (account == null) {
			// No account exists 
	        if (logger.isActivated()) {
	        	logger.debug("The RCS account does not exist");
	        }
			if (AccountChangedReceiver.isAccountResetByEndUser()) {
				// It was manually destroyed by the user:  
		        if (logger.isActivated()) {
		        	logger.debug("It was manually destroyed by the user, we do not recreate it");
		        }
			} else {
		        if (logger.isActivated()) {
		        	logger.debug("Recreate a new RCS account");
		        }				
				AuthenticationService.createRcsAccount(AndroidFactory.getApplicationContext(),
	        		AndroidFactory.getApplicationContext().getString(R.string.rcs_core_account_username), true, true);
			}
		} else {
			// Account exists: checks if it has changed
			if (hasChangedAccount()) {
				// Account has changed (i.e. new SIM card): delete the current account and create a new one
		        if (logger.isActivated()) {
		        	logger.debug("Deleting the old RCS account for " + lastUserAccount);
		        }
		        ContactsManager.getInstance().deleteRCSEntries();
		        AuthenticationService.removeRcsAccount(AndroidFactory.getApplicationContext(), null);
	
		        if (logger.isActivated()) {
		        	logger.debug("Creating a new RCS account for " + currentUserAccount);
		        }
		        AuthenticationService.createRcsAccount(AndroidFactory.getApplicationContext(),
		        		AndroidFactory.getApplicationContext().getString(R.string.rcs_core_account_username), true, true);
			}		
		}		
	}
	
	/**
	 * Check if it is the first time we launch the service
	 * 
	 * @return Boolean
	 */
	public boolean isFirstLaunch() {
		return (lastUserAccount == null);
	}
	
	/**
	 * Check if RCS account has changed since the last time we started the service
	 * 
	 * @return true if the active account was changed
	 */
	public boolean hasChangedAccount() {
		if (lastUserAccount == null) {
			return false;
		} else {
			return (!currentUserAccount.equalsIgnoreCase(lastUserAccount));
		}
	}

	/**
	 * Start the user account manager
	 */
	public void start(){
		// Create user account change receiver
		accountChangedReceiver = new AccountChangedReceiver();
		
		// Register account changed broadcast receiver after a timeout of 2s (This is not done immediately, as we do not want to catch
		// the removal of the account (creating and removing accounts is done asynchronously). We can reasonably assume that no
		// RCS account deletion will be done by user during this amount of time, as he just started his service.
		Handler handler = new Handler();
		handler.postDelayed(new Runnable(){
			public void run(){
				AndroidFactory.getApplicationContext().registerReceiver(accountChangedReceiver, new IntentFilter("android.accounts.LOGIN_ACCOUNTS_CHANGED"));
			}
		},
		2000);
	}
	
	/**
	 * Stop the user account manager
	 */
	public void stop(){
		try {
			// Unregister account changed broadcast receiver
			AndroidFactory.getApplicationContext().unregisterReceiver(accountChangedReceiver);
		} catch (IllegalArgumentException e) {
			if (logger.isActivated()){
				logger.error("Receiver not registered");
			}
		}
	}
	
    /**
     * Set the country code
     */
    private void setCountryCode() {
        String countryCode = DeviceUtils.getSimCountryCode(AndroidFactory.getApplicationContext());
        if (countryCode != null) {
            if (!countryCode.startsWith("+")) {
                countryCode = "+" + countryCode;
            }
            if (logger.isActivated()) {
                logger.info("Set country code to " + countryCode);
            }
            RcsSettings.getInstance().setCountryCode(countryCode);
        }
    }

}
