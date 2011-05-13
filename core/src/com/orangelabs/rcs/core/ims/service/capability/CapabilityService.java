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

package com.orangelabs.rcs.core.ims.service.capability;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;

import com.orangelabs.rcs.addressbook.AddressBookEventListener;
import com.orangelabs.rcs.core.CoreException;
import com.orangelabs.rcs.core.ims.ImsModule;
import com.orangelabs.rcs.core.ims.protocol.sip.SipRequest;
import com.orangelabs.rcs.core.ims.service.ImsService;
import com.orangelabs.rcs.platform.AndroidFactory;
import com.orangelabs.rcs.provider.eab.ContactsManager;
import com.orangelabs.rcs.provider.settings.RcsSettings;
import com.orangelabs.rcs.service.api.client.capability.Capabilities;
import com.orangelabs.rcs.utils.PhoneUtils;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Capability discovery service
 * 
 * @author jexa7410
 */
public class CapabilityService extends ImsService implements AddressBookEventListener {
	/**
	 * Capability refresh timeout in seconds
	 */
	private static final int CAPABILITY_REFRESH_PERIOD = RcsSettings.getInstance().getCapabilityRefreshTimeout();

	/**
	 * Options manager
	 */
	private OptionsManager optionsManager;
	
	/**
	 * Anonymous fetch manager
	 */
	private AnonymousFetchManager anonymousFetchManager;
	
	/**
	 * Polling manager
	 */
	private PollingManager pollingManager;
	
	/**
	 * Flag: set during the address book changed procedure, if we are notified of a change 
	 */
	private boolean isRecheckNeeded = false;
	
	/**
	 * Flag indicating if a check procedure is in progress 
	 */
	private boolean isCheckInProgress = false;
	
	/**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());

    /**
	 * Constructor
	 * 
	 * @param parent IMS module
	 * @throws CoreException
	 */
	public CapabilityService(ImsModule parent) throws CoreException {
		super(parent, "capabilities_service.xml", true);
		
    	// Instanciate the polling manager
        pollingManager = new PollingManager(this);
        
    	// Instanciate the options manager
		optionsManager = new OptionsManager(parent);

    	// Instanciate the anonymous fetch manager
    	anonymousFetchManager = new AnonymousFetchManager(parent);
	}

	/**
	 * Start the IMS service
	 */
	public synchronized void start() {
		if (isServiceStarted()) {
			// Already started
			return;
		}
		setServiceStarted(true);
		
		// Listen to address book changes
		getImsModule().getCore().getAddressBookManager().addAddressBookListener(this);
		
		// Start polling
		pollingManager.start();
		
		// Force a capability check
		handleAddressBookHasChanged();
	}

	/**
	 * Stop the IMS service 
	 */
	public synchronized void stop() {
		if (!isServiceStarted()) {
			// Already stopped
			return;
		}
		setServiceStarted(false);
		
		// Stop polling
		pollingManager.stop();

		// Stop listening to address book changes
		getImsModule().getCore().getAddressBookManager().removeAddressBookListener(this);
	}
    
	/**
	 * Check the IMS service 
	 */
	public void check() {
	}
	
	/**
	 * Get the options manager
	 * 
	 * @return Options manager
	 */
	public OptionsManager getOptionsManager() {
		return optionsManager;
	}

	/**
	 * Get the options manager
	 * 
	 * @return Options manager
	 */
	public AnonymousFetchManager getAnonymousFetchManager() {
		return anonymousFetchManager;
	}

	/**
	 * Request capabilities for a list of contacts
	 * 
	 * @param contactList Contact list
	 * @return Capabilities
	 */
	public void requestContactCapabilities(List<String> contactList) {
		for (int i=0; i < contactList.size(); i++) {
			String contact = contactList.get(i);
			requestContactCapabilities(contact);
		}	
	}
	
	/**
	 * Request contact capabilities
	 * 
	 * @param contact Contact
	 * @return Capabilities
	 */
	public synchronized Capabilities requestContactCapabilities(String contact) {
    	if (logger.isActivated()) {
    		logger.debug("Request capabilities to " + contact);
    	}

		// Read capabilities from the database
		Capabilities capabilities = ContactsManager.getInstance().getContactCapabilities(contact);
		if (capabilities == null) {
	    	if (logger.isActivated()) {
	    		logger.debug("No capability exist for " + contact);
	    	}

            // New contact: request capabilities from the network
    		optionsManager.requestCapabilities(contact);
		} else {
	    	if (logger.isActivated()) {
	    		logger.debug("Capabilities exist for " + contact);
	    	}
			long delta = (System.currentTimeMillis()-capabilities.getTimestamp())/1000;
			if ((delta >= CAPABILITY_REFRESH_PERIOD) || (delta < 0)) {
		    	if (logger.isActivated()) {
		    		logger.debug("Capabilities have expired for " + contact);
		    	}

		    	// Capabilities are too old: request capabilities from the network
	    		optionsManager.requestCapabilities(contact);
			}
		}
		return capabilities;
	}	
	
    /**
     * Receive a capability request (options procedure)
     * 
     * @param options Received options message
     */
    public void receiveCapabilityRequest(SipRequest options) {
    	optionsManager.receiveCapabilityRequest(options);
    }

	/**
     * Receive a notification (anonymous fecth procedure)
     * 
     * @param notify Received notify
     */
    public void receiveNotification(SipRequest notify) {
    	anonymousFetchManager.receiveNotification(notify);
    }

	/**
	 * Address book content has changed
	 */
	public void handleAddressBookHasChanged() {
		// Update capabilities for the contacts that have never been queried
		if (isCheckInProgress){
			isRecheckNeeded = true;
			return;
		}
		
		// We are beginning the check procedure
		isCheckInProgress = true;

		// Reset recheck flag
		isRecheckNeeded = false;
		
		// Check all phone numbers and query only the new ones
		Cursor phonesCursor = AndroidFactory.getApplicationContext().getContentResolver().query(
				ContactsContract.CommonDataKinds.Phone.CONTENT_URI, 
				new String[]{Phone._ID, Phone.NUMBER, Phone.RAW_CONTACT_ID}, 
				null, 
				null, 
				null);

		// List of unique number that will have to be queried for capabilities
		ArrayList<String> toBeTreatedNumbers = new ArrayList<String>();
		
		// List of unique number that have already been queried
		ArrayList<String> alreadyRcsOrInvalidNumbers = new ArrayList<String>();
		
		// We add "My number" to the numbers that are already RCS, so we don't query it if it is present in the address book
		alreadyRcsOrInvalidNumbers.add(PhoneUtils.extractNumberFromUri(ImsModule.IMS_USER_PROFILE.getPublicUri()));	

		while(phonesCursor.moveToNext()) {
			// Keep a trace of already treated row. Key is (phone number in international format)
			String phoneNumber = PhoneUtils.formatNumberToInternational(phonesCursor.getString(1));
			long rawContactId = phonesCursor.getLong(2);
			if (!alreadyRcsOrInvalidNumbers.contains(phoneNumber)) {
				// If this number is not considered RCS valid or has already an entry with RCS, skip it
				if (ContactsManager.getInstance().isRCSValidNumber(phoneNumber) 
						&& !ContactsManager.getInstance().isRawContactRcsAssociated(rawContactId)) {
					// This entry is valid and not already has a RCS raw contact, it can be treated
					toBeTreatedNumbers.add(phoneNumber);
				} else {
					// This entry is either not valid or already RCS, this number is already done
					alreadyRcsOrInvalidNumbers.add(phoneNumber);
					toBeTreatedNumbers.remove(phoneNumber);
				}
			}
		}
		phonesCursor.close();
		
		// Get the capabilities for the numbers that haven't got a RCS associated contact
		requestContactCapabilities(toBeTreatedNumbers);
		
		// End of the check procedure
		isCheckInProgress = false;

		// Check if we have to make another check
		if (isRecheckNeeded) {
			handleAddressBookHasChanged();
		}
	}
}
