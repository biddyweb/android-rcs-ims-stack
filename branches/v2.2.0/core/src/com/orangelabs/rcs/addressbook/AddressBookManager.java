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
package com.orangelabs.rcs.addressbook;

import java.util.ArrayList;
import java.util.List;
import android.content.ContentResolver;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.CommonDataKinds.Phone;

import com.orangelabs.rcs.core.Core;
import com.orangelabs.rcs.core.CoreException;
import com.orangelabs.rcs.core.ims.ImsModule;
import com.orangelabs.rcs.provider.eab.RichAddressBookData;
import com.orangelabs.rcs.provider.settings.RcsSettings;
import com.orangelabs.rcs.service.RcsCoreService;
import com.orangelabs.rcs.utils.ContactUtils;
import com.orangelabs.rcs.utils.PhoneUtils;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Address book manager
 * <br>This manager is responsible of the synchronization between the native address book and the RCS contacts.
 * <br>It observes the modifications done to the ContactsContract provider and revokes the missing contacts.
 * <br>It also is responsible for creating the contacts if missing at first launch of the service. 
 * <br>For more information, see the corresponding chapter in specifications.
 */
public class AddressBookManager {
	
	/**
	 * Core instance
	 */
	private Core core;

	/**
	 * Content resolver
	 */
	private ContentResolver contentResolver;
	
    /**
     * Cursor used to observe ContactsContract
     */
    private Cursor contactsContractCursor;
    
    /**
     * Content observer
     */
    private ContactsContractObserver contactsContractObserver;
    
    /**
     * Check handler
     */
    private CheckHandler checkHandler = new CheckHandler();
    
    /**
     * Check message
     * <br>When received by handler, triggers the check
     */
    private final static int CHECK_MESSAGE = 5765;
    
    /**
     * Minimum period awaited before we do the checking
     */
    private final static int MINIMUM_CHECK_PERIOD = 1*1000;
    
    /**
     * Content observer registered flag
     */
    private boolean observerIsRegistered = false; 
    
	/**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());
    
    /**
     * Constructor
     * 
     * @param core Core
     */
	public AddressBookManager(Core core) throws CoreException {
		if (logger.isActivated()) {
			logger.info("Address book manager is created");
		}
		this.core = core;
		this.contentResolver = RcsCoreService.CONTEXT.getContentResolver();
	}
	
	/**
	 * Start address book monitoring 
	 */
	public void startAddressBookMonitoring() {
		if (logger.isActivated()) {
			logger.info("Start address book monitoring");
		}
		
		// Instanciate content observer
		contactsContractObserver = new ContactsContractObserver(new Handler());

		// Query contactContracts phone database
		contactsContractCursor = contentResolver.query(Phone.CONTENT_URI, 
				null, 
				null, 
				null, 
				null);
		
		// Register content observer
		contactsContractCursor.registerContentObserver(contactsContractObserver);
		observerIsRegistered = true;
		
		// Check if it is the first service launch or if the account has changed
		if(core.getAccountManager().isFirstLaunch() 
				|| core.getAccountManager().hasChangedAccount()){
			// Do nothing, we would not want to revoke contacts that are not present at first launch on this account 
		}else{
			// Check the changes that may have been done while the service was not running
			checkChanges();
		}
		
	}
	
	/**
	 * Resume the address book monitoring
	 */
	public void resumeAddressBookMonitoring() {
		if (logger.isActivated()) {
			logger.info("Resume the address book monitoring");
		}
		if (!observerIsRegistered){
			// Register content observer
			contactsContractCursor.registerContentObserver(contactsContractObserver);
			observerIsRegistered = true;
		}
	}
	
	/**
	 * Pause the address book monitoring
	 */
	public void pauseAddressBookMonitoring() {
		if (logger.isActivated()) {
			logger.info("Pause the address book monitoring");
		}
		
		if (observerIsRegistered){
			// Unregister content observer
			contactsContractCursor.unregisterContentObserver(contactsContractObserver);
			observerIsRegistered = false;
		}
	}
	
	/**
	 * Stop address book monitoring
	 */
	public void stopAddressBookMonitoring() {
		if (logger.isActivated()) {
			logger.info("Stop address book monitoring");
		}
		// Remove the messages that may still be scheduled
		checkHandler.removeMessages(CHECK_MESSAGE);
		
		// Unregister content observer
		contactsContractCursor.unregisterContentObserver(contactsContractObserver);
		observerIsRegistered = false;
		// Close cursor
		contactsContractCursor.close();
	}
	
	/**
	 * ContactsContract observer
	 */
	private class ContactsContractObserver extends ContentObserver  {

		public ContactsContractObserver(Handler handler) {
			super(handler);
		}
		
		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			// Something changed in the address book
			if (!checkHandler.hasMessages(CHECK_MESSAGE)){
				// If we do not have a check already scheduled, schedule a new one
				checkHandler.sendEmptyMessageDelayed(CHECK_MESSAGE, MINIMUM_CHECK_PERIOD);
				if (logger.isActivated()){
					logger.debug("New address book checking scheduled in " + MINIMUM_CHECK_PERIOD + " ms");
				}				
			}
		}
		
	};
	
    /**
     * Handler used to avoid too many checks
     */
    private class CheckHandler extends Handler{
    	
    	@Override
    	public void handleMessage(Message msg) {
    		super.handleMessage(msg);

    		if (msg.what == CHECK_MESSAGE){
        		if (logger.isActivated()){
        			logger.debug("Minimum check period elapsed, do the address book check procedure");
        		}
        		checkChanges();
    		}
    	}
    };
	
	/**
	 * Check the changes
	 * <br>This is where we query the address book to check what has changed
	 */
	private void checkChanges(){

		if (logger.isActivated()){
			logger.debug("Doing the address book change check procedure.");
		}
		
		// Get a list of all RCS numbers
		List<String> rcsNumbers = ContactsManager.getInstance().getRcsContacts();
		for(int i=0;i<rcsNumbers.size(); i++){
			// For each RCS number
			String rcsNumber = rcsNumbers.get(i);
			if (!isNumberInAddressBook(rcsNumber)){
				// If it is not present in the address book
				if (logger.isActivated()){
					logger.debug("The RCS number " + rcsNumber + " was not found in the address book any more.");
				}

				if (ContactsManager.getInstance().isNumberShared(rcsNumber)
						|| ContactsManager.getInstance().isNumberInvited(rcsNumber)){
					// Active or Invited
					if (logger.isActivated()){
						logger.debug(rcsNumber + " is either active or invited");
						logger.debug("We remove it from the buddy list");
					}
					// We revoke it
					boolean result = core.getPresenceService().revokeSharedContact(rcsNumber);
					if (result){
						// The contact should be automatically unrevoked after a given timeout. Here the
						// timeout period is 0, so the contact can receive invitations again now
						result = core.getPresenceService().removeRevokedContact(rcsNumber);
						if (result){
							// Remove entry from rich address book provider
							try {
								ContactsManager.getInstance().removeContact(rcsNumber);
							} catch (ContactsManagerException e) {
								if (logger.isActivated()){
									logger.error("Something went wrong when removing entry from rich address book provider",e);
								}
							}
						}else{
							if (logger.isActivated()){
								logger.error("Something went wrong when revoking shared contact");
							}
						}
					}
				}else if (ContactsManager.getInstance().isNumberBlocked(rcsNumber)){
					// Blocked
					if (logger.isActivated()){
						logger.debug(rcsNumber + " is blocked");
						logger.debug("We remove it from the blocked list");
					}
					// We unblock it
					boolean result = core.getPresenceService().removeBlockedContact(rcsNumber);
					if (result){
						// Remove entry from rich address book provider
						try {
							ContactsManager.getInstance().removeContact(rcsNumber);
						} catch (ContactsManagerException e) {
							if (logger.isActivated()){
								logger.error("Something went wrong when removing entry from rich address book provider",e);
							}
						}
					}else{
						if (logger.isActivated()){
							logger.error("Something went wrong when removing blocked contact");
						}
					}
				}else if (ContactsManager.getInstance().isNumberWilling(rcsNumber)){
					// Willing
					if (logger.isActivated()){
						logger.debug(rcsNumber + " is willing");
						logger.debug("Nothing to do");
					}
				}else if (ContactsManager.getInstance().isNumberCancelled(rcsNumber)){
					// Cancelled
					if (logger.isActivated()){
						logger.debug(rcsNumber + " is cancelled");
						logger.debug("We remove it from rich address book provider");
					}
					// Remove entry from rich address book provider
					try {
						ContactsManager.getInstance().removeContact(rcsNumber);
					} catch (ContactsManagerException e) {
						if (logger.isActivated()){
							logger.error("Something went wrong when removing entry from rich address book provider",e);
						}
					}
				}
			}
		}

		// Clean RCS entries associated to numbers that have been removed or modified
		ContactsManager.getInstance().cleanRCSEntries();		

		if (RcsSettings.getInstance().getAnonymousFetchRefrehTimeout()!=-1){
			// Make an anonymous fetch for all numbers that have no RCS entry yet
			queryForMissingCapabilities();
		}
	}
	
	/**
	 * Make an anonymous fetch for all numbers that have no RCS entry yet
	 */
	private void queryForMissingCapabilities(){

		// Check all phone numbers and do anonymous fetch queries on the new ones
		Cursor phonesCursor = contentResolver.query(
				ContactsContract.CommonDataKinds.Phone.CONTENT_URI, 
				new String[]{Phone._ID, Phone.NUMBER, Phone.RAW_CONTACT_ID}, 
				null, 
				null, 
				null);

		ArrayList<String> toBeTreatedNumbers = new ArrayList<String>();		// List of unique number that will have to be queried for capabilities
		ArrayList<String> alreadyRCSOrInvalidNumbers = new ArrayList<String>();		// List of unique number that have already been queried
		// We add "My number" to the numbers that are already RCS, so we do not do anonymous fetches to it if it is present in the address book
		alreadyRCSOrInvalidNumbers.add(PhoneUtils.extractNumberFromUri(ImsModule.IMS_USER_PROFILE.getPublicUri()));	

		while (phonesCursor.moveToNext()) {
			// Keep a trace of already treated row. Key is (phone number in international format)
			String phoneNumber = PhoneUtils.formatNumberToInternational(phonesCursor.getString(1));
			long rawContactId = phonesCursor.getLong(2);
			if (!alreadyRCSOrInvalidNumbers.contains(phoneNumber)){
				// If this number is not considered RCS valid or has already an entry with RCS, skip it
				if (ContactsManager.getInstance().isRCSValidNumber(phoneNumber) 
						&& !ContactsManager.getInstance().isRawContactRcsAssociated(rawContactId)){
					// This entry is valid and not already has a RCS raw contact, it can be treated
					toBeTreatedNumbers.add(phoneNumber);
				}else{
					// This entry is either not valid or already RCS, this number is already done
					alreadyRCSOrInvalidNumbers.add(phoneNumber);
					toBeTreatedNumbers.remove(phoneNumber);
				}
			}
		}
		phonesCursor.close();

		// For the number that haven't got a RCS associated contact
		// and if terminal connected to IMS then request capabilities
		if (core.getImsModule().getCurrentNetworkInterface().isRegistered()) {
			if (toBeTreatedNumbers.size()==1){
				// Only one number to request
				core.getPresenceService().requestCapabilities(toBeTreatedNumbers.get(0));
			}else if (toBeTreatedNumbers.size()>1){
				// Many numbers to request
				core.getPresenceService().requestCapabilities(toBeTreatedNumbers);
			}
		}
	}
	
	
	
	/**
	 * Check if the given number is present in the address book
	 * 
	 * @param number Number to be checked
	 * @return boolean indicating if number is present in the address book or not
	 */
	private boolean isNumberInAddressBook(String number){
		String[] projection = { Data.RAW_CONTACT_ID };
        String selection = Data.MIMETYPE + "=? AND PHONE_NUMBERS_EQUAL(" + Phone.NUMBER + ", ?)";
        String[] selectionArgs = { Phone.CONTENT_ITEM_TYPE, number };
	    String sortOrder = Data.RAW_CONTACT_ID;

	    // Starting query phone_numbers_equal
	    Cursor cur = contentResolver.query(Data.CONTENT_URI, 
	    		projection, 
	    		selection, 
	    		selectionArgs,
	    		sortOrder);
	    
	    if (cur != null) {
	    	int count = cur.getCount();
	    	cur.close();
	    	// We found at least one data with this number
	    	if (count>0){
	    		return true;
	    	}
	    }

	    // No match found using LOOSE equals, try using STRICT equals.
	    String selectionStrict = Data.MIMETYPE + "=? AND (NOT PHONE_NUMBERS_EQUAL(" + Phone.NUMBER
	    + ", ?) AND PHONE_NUMBERS_EQUAL(" + Phone.NUMBER + ", ?, 1))";
	    String[] selectionArgsStrict = {
	    		Phone.CONTENT_ITEM_TYPE, number, number
	    };
	    cur = contentResolver.query(Data.CONTENT_URI, 
	    		projection, 
	    		selectionStrict, 
	    		selectionArgsStrict,
	    		sortOrder);
	    if (cur != null) {
	    	int count = cur.getCount();
	    	cur.close();
	    	// We found at least one data with this number
	    	if (count>0){
	    		return true;
	    	}
	    }
	    
	    // We found no contact with this number
	    return false;
	}
	
	/**
	 * First launch or account changed check
	 * 
	 * <br>Check done at first launch of the service on the phone after install of the application or when the user account changed
	 * <br>We query the lists on the XDM server and create a new contact for each RCS number that is not already existing
	 * @param list of granted contacts
	 * @param list of blocked contacts
	 */
	public void firstLaunchOrAccountChangedCheck(List<String> grantedContacts, List<String> blockedContacts){

		if (logger.isActivated()){
			logger.debug("Doing the first launch or account change check procedure.");
		}

		pauseAddressBookMonitoring();
		
		// Flush the address book provider
		ContactsManager.getInstance().flushContactProvider();

		// Treat the buddy list
		for(int i=0;i<grantedContacts.size(); i++){
			String me = ImsModule.IMS_USER_PROFILE.getPublicUri();
			String contact = grantedContacts.get(i);
			if (!contact.equalsIgnoreCase(me)){
				// For each RCS granted contact, except me
				String rcsNumber = PhoneUtils.extractNumberFromUri(contact);
				if (!isNumberInAddressBook(rcsNumber)){
					// If it is not present in the address book
					if (logger.isActivated()){
						logger.debug("The RCS number " + rcsNumber + " was not found in the address book.");
						logger.debug("Let's add it.");
					}

					// => We create the entry in the regular address book
					try {
						ContactUtils.createRcsContactIfNeeded(RcsCoreService.CONTEXT, rcsNumber);
					} catch (Exception e) {
						if (logger.isActivated()){
							logger.error("Something went wrong when creating contact "+rcsNumber,e);
						}
					}				
				}

				// Add the contact to the rich address book provider
				ContactsManager.getInstance().addOrModifyRcsContactInProvider(rcsNumber, RichAddressBookData.STATUS_INVITED);
			}
		}

		// Treat the blocked contact list
		for(int i=0;i<blockedContacts.size(); i++){
			// For each RCS blocked contact 
			String rcsNumber = PhoneUtils.extractNumberFromUri(blockedContacts.get(i));
			if (!isNumberInAddressBook(rcsNumber)){
				// If it is not present in the address book
				if (logger.isActivated()){
					logger.debug("The RCS number " + rcsNumber + " was not found in the address book.");
					logger.debug("Let's add it.");
				}

				// => We create the entry in the regular address book
				try {
					ContactUtils.createRcsContactIfNeeded(RcsCoreService.CONTEXT, rcsNumber);
				} catch (Exception e) {
					if (logger.isActivated()){
						logger.error("Something went wrong when creating contact "+rcsNumber,e);
					}
				}				
				// Set the presence sharing status to blocked
				try {
					ContactsManager.getInstance().blockContact(rcsNumber);
				} catch (ContactsManagerException e) {
					if (logger.isActivated()){
						logger.error("Something went wrong when blocking contact "+rcsNumber,e);
					}
				}

				// Add the contact to the rich address book provider
				ContactsManager.getInstance().addOrModifyRcsContactInProvider(rcsNumber, RichAddressBookData.STATUS_BLOCKED);
			}
		}
		
		resumeAddressBookMonitoring();
	}
	
}
