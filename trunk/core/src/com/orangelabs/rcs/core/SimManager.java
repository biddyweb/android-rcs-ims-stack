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

import com.orangelabs.rcs.platform.AndroidFactory;
import com.orangelabs.rcs.platform.registry.RegistryFactory;
import com.orangelabs.rcs.provider.eab.RichAddressBook;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * SIM card manager
 * 
 * @author jexa7410
 */
public class SimManager {
	/**
	 * Current IMSI
	 */
	private String imsi = null;
	
	/**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());
    
    /**
	 * Constructor
	 * 
	 * @throws CoreException
	 */
	public SimManager() throws CoreException {
		// Read the current IMSI
		TelephonyManager mgr = (TelephonyManager)AndroidFactory.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
		imsi = mgr.getSubscriberId();
		if (imsi == null) {
	        if (logger.isActivated()) {
	        	logger.error("No valid SIM card");
	        }
			throw new CoreException("Unable to read IMSI");
		}

		// Read the last used SIM
		String lastImsi = RegistryFactory.getFactory().readString("LastImsi", null);

		// Check if the SIM card has changed
		if ((lastImsi != null) && !lastImsi.equals(imsi)) {
	        if (logger.isActivated()) {
	        	logger.info("SIM card has changed: reset the rich address book");
	        }
			RichAddressBook.getInstance().flushAllData();
		}
		
		// Save the current IMSI
		RegistryFactory.getFactory().writeString("LastImsi", imsi);

		if (logger.isActivated()) {
        	logger.info("My IMSI is " + imsi);
        }
     }

	/**
	 * Returns the IMSI from the SIM card
	 * 
	 * @return IMSI
	 */
    public String getImsi() {
		return imsi;
	}
}
