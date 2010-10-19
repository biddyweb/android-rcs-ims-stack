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
package com.orangelabs.rcs.utils;

import java.util.ArrayList;

import com.orangelabs.rcs.provider.eab.RichAddressBook;
import com.orangelabs.rcs.provider.eab.RichAddressBookData;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.provider.Contacts.People;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;


/**
 * Contacts utility functions
 */
public class ContactUtils {

	/**
	 * Returns the contact display name
	 *  
	 * @param context Context
	 * @param uro Contact URI (SIP or Tel)
	 * @return Display name in the address book or the username part of the URI
	 */
	public static String getContactDisplayName(ContentResolver cr, String contact){
		String result = contact;
		
		// Get the util number part from contact
		contact = extractNumberFromContact(contact);
		
    	// Get all contacts
    	Cursor c = cr.query(Contacts.Phones.CONTENT_URI, new String[]{Contacts.Phones.DISPLAY_NAME, Contacts.Phones.NUMBER}, null, null, null);
    	while(c.moveToNext()){
    		String numberInAddressBook = extractNumberFromContact(c.getString(1));
    		if (numberInAddressBook.equalsIgnoreCase(contact)){
    			result = c.getString(0);
    		}
       	}
       	c.close();
        return result;
	}

	/**
	 * Get the number associated to the contact, ie without suffix or prefix
	 * <br>For example "tel:+33612345651@orange.net" becomes "+33612345651"
	 * <br> Then we get only the util part of the number:
	 * <br>if number begins with '+', international format, we remove first three digits
	 * <br>else, we assume it is national and we have to remove the first digit
	 * 
	 * @param contact
	 * @return util part of number
	 */
	public static String extractNumberFromContact(String contact){
		contact = extractNumberFromUri(contact);
		
		if (contact.indexOf("+")!=-1){
			// If number is international, remove three first digits
    		contact = contact.substring(3);
		}else{
			// Else we assume it is national and we remove first digit
    		contact = contact.substring(1);
		}

		return contact;
	}
	
	/**
	 * Get the number associated to the contact, ie without suffix or prefix
	 * <br>For example "tel:+33612345651@orange.net" becomes "+33612345651"
	 * 
	 * @param uri uri
	 * @return number part
	 */
	public static String extractNumberFromUri(String uri){
		if ((uri.indexOf("sip:")!=-1) 
				|| (uri.indexOf("tel:")!=-1)){
			// If contact has prefix "sip:" or "tel:", delete it
			uri = uri.substring(4);
		}

		if (uri.indexOf("@")!=-1){
			// If contact has suffix @homedomain, delete it
			uri = uri.substring(0,uri.indexOf("@"));
		}
		
		return uri;
	}
    
	/**
	 * Get the contact id from the raw contact id
	 * 
	 * @param cr
	 * @param rawContactId
	 * @return contact id
	 */
    public static long queryForContactId(ContentResolver cr, long rawContactId) {
        Cursor contactIdCursor = null;
        long contactId = -1;
        try {
            contactIdCursor = cr.query(RawContacts.CONTENT_URI,
                    new String[] {RawContacts.CONTACT_ID},
                    RawContacts._ID + "=" + rawContactId, null, null);
            if (contactIdCursor != null && contactIdCursor.moveToFirst()) {
                contactId = contactIdCursor.getLong(0);
            }
        } finally {
            if (contactIdCursor != null) {
                contactIdCursor.close();
            }
        }
        return contactId;
    }
    
    /**
     * Get a rawContact id from a contact id
     * 
     * @param cr
     * @param contactId
     * @return rawContact id
     */
    public static long queryForRawContactId(ContentResolver cr, long contactId) {
        Cursor rawContactIdCursor = null;
        long rawContactId = -1;
        try {
            rawContactIdCursor = cr.query(RawContacts.CONTENT_URI,
                    new String[] {RawContacts._ID},
                    RawContacts.CONTACT_ID + "=" + contactId, null, null);
            if (rawContactIdCursor != null && rawContactIdCursor.moveToFirst()) {
                // Just return the first one.
                rawContactId = rawContactIdCursor.getLong(0);
            }
        } finally {
            if (rawContactIdCursor != null) {
                rawContactIdCursor.close();
            }
        }
        return rawContactId;
    }
    
	/**
	 * The contact was accepted by user after an presence invitation was received
	 * <br>We create an entry in normal address book for him if needed
	 * 
	 * @param context
	 * @param contact The contact that was accepted
	 */
	public static void acceptRcsContact(Context context, String contact){
		// Extract the number associated from the contact
		String number = PhoneUtils.extractNumberFromUri(contact);

		// Create entry in normal address book
		createRcsContactIfNeeded(context, number);
	}
	
	/**
	 * Create a RCS contact if the given contact is not already present in the address book
	 * 
	 * @param context
	 * @param contact Rcs contact number
	 * @return uri of the newly created contact, or the uri of the corresponding contact if there is already a match
	 */
	private static Uri createRcsContactIfNeeded(Context context, String number){
		// Check if contact is already in address book
		RichAddressBook.createInstance(context);
		int contactId = RichAddressBook.getInstance().getContactId(number);
		
		if (contactId==-1){
			// If the contact is not present in address book, create an entry with this number
			ContentValues values = new ContentValues();

			values.putNull(ContactsContract.Contacts.DISPLAY_NAME);
			values.put(Phone.NUMBER, number);
			values.put(Phone.TYPE, Phone.TYPE_MOBILE);
			
			Uri newPersonUri = createContact(context, values);
			
			return newPersonUri;
		}else{
			// Contact already in address book
			return ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
		}
	}

	/**
	 * Create a contact in address book
	 * <br>This is done with Contacts 2.0 API, and new contact is a "Phone" contact, not associated with any particular account type
	 * 
	 * @param context
	 * @param values
	 * @return
	 */
	public static Uri createContact(Context context, ContentValues values) {
		ContentResolver mResolver = context.getContentResolver();
		
		// We will associate the newly created contact to the null contact account (Phone)
		
		ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
        
		int backRefIndex = 0;
		operations.add(ContentProviderOperation.newInsert(RawContacts.CONTENT_URI)
					.withValue(RawContacts.ACCOUNT_TYPE, null)
					.withValue(RawContacts.ACCOUNT_NAME, null)
					.build());   

		// Set the name
        operations.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
        		.withValueBackReference(Data.RAW_CONTACT_ID, backRefIndex)
        		.withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
        		.withValue(StructuredName.DISPLAY_NAME, values.get(ContactsContract.Contacts.DISPLAY_NAME))
        		.build());
        
        operations.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
        		.withValueBackReference(Data.RAW_CONTACT_ID, backRefIndex)
        		.withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
        		.withValue(Phone.NUMBER, values.get(Phone.NUMBER))
        		.withValue(Phone.TYPE, values.get(Phone.TYPE))
        		.build());
        
        long rawContactId = 0;
        try {
			ContentProviderResult[] result = mResolver.applyBatch(ContactsContract.AUTHORITY, operations);
			rawContactId = ContentUris.parseId(result[1].uri);
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (OperationApplicationException e) {
			e.printStackTrace();
		}
		long contactId = 0;
		// Search the corresponding contact id
		Cursor c = mResolver.query(Data.CONTENT_URI,          
				new String[]{Data.CONTACT_ID},          
				Data._ID + "=?",          
				new String[] {String.valueOf(rawContactId)}, 
				null);
		if (c.moveToFirst()){
			contactId = c.getLong(0);
		}
		c.close();
		
		// Return the resulting contact uri
		Uri resultUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId); 
		return resultUri;
	}
	
	
	/**
	 * The contact was refused by user
	 * We delete its entry in the RCS address book
	 * 
	 * @param context
	 * @param contact The contact that was refused
	 */
	public static void refuseRcsContact(Context context, String contact){
		// Nothing to do
	}
	
	/**
	 * The contact was ignored by user
	 * 
	 * @param context
	 * @param contact The contact that was refused
	 */
	public static void ignoreRcsContact(Context context, String contact){
		// Nothing to do
	}
	
	/**
	 * Get all phone numbers for a given contact
	 * 
	 * @param context
	 * @param contactId
	 * @return list List of all phone numbers for the contact
	 */
	public static ArrayList<PhoneNumber> getContactNumbers(Context context, long contactId) {
		ArrayList<PhoneNumber> contactNumbers = new ArrayList<PhoneNumber>();
		//  Get all phone numbers                
		Cursor phones = context.getContentResolver().query(Phone.CONTENT_URI, 
				null, 
				Phone.CONTACT_ID + " = " + contactId, 
				null, 
				null);        
		while (phones.moveToNext()) {
			String number = phones.getString(phones.getColumnIndex(Phone.NUMBER));
			String label = phones.getString(phones.getColumnIndex(Phone.LABEL));
			if (label==null){
				// Label is not custom, get the string corresponding to the phone type
				int type = phones.getInt(phones.getColumnIndex(Phone.TYPE));
				label = context.getString(Phone.getTypeLabelResource(type));
			}
			contactNumbers.add(new PhoneNumber(number, label));
		}
		phones.close();
		return contactNumbers;
	}
	
	/**
	 * Get all RCS active or invited phone numbers from a contact
	 * 
	 * @param context
	 * @param contactId
	 * @return list List of all RCS phone numbers for the contact
	 */
	public static ArrayList<PhoneNumber> getRcsActiveOrInvitedContactNumbers(Context context, long contactId) {
		ArrayList<PhoneNumber> rcsContactNumbers = new ArrayList<PhoneNumber>();
		if (RichAddressBook.getInstance()==null){
			RichAddressBook.createInstance(context);
		}
		
		//  Get all phone numbers                
		Cursor phones = context.getContentResolver().query(Phone.CONTENT_URI, 
				null, 
				Phone.CONTACT_ID + " = " + contactId, 
				null, 
				null);        
		while (phones.moveToNext()) {
			String number = phones.getString(phones.getColumnIndex(Phone.NUMBER));
			String label = phones.getString(phones.getColumnIndex(Phone.LABEL));
			if (label==null){
				// Label is not custom, get the string corresponding to the phone type
				int type = phones.getInt(phones.getColumnIndex(Phone.TYPE));
				label = context.getString(Phone.getTypeLabelResource(type));
			}
			// Check if this number is RCS
			String status = RichAddressBook.getInstance().getContactSharingStatus(number);
			if (status!=null
					&& (status.equalsIgnoreCase(RichAddressBookData.STATUS_ACTIVE) || status.equalsIgnoreCase(RichAddressBookData.STATUS_INVITED))){
				rcsContactNumbers.add(new PhoneNumber(number, label));
			}
		}
		phones.close();
		return rcsContactNumbers;
	}
	
	/**
	 * Get all RCS active phone numbers from a contact
	 * 
	 * @param context
	 * @param contactId
	 * @return list List of all RCS phone numbers for the contact
	 */
	public static ArrayList<PhoneNumber> getRcsActiveContactNumbers(Context context, long contactId) {
		ArrayList<PhoneNumber> rcsContactNumbers = new ArrayList<PhoneNumber>();
		if (RichAddressBook.getInstance()==null){
			RichAddressBook.createInstance(context);
		}
		
		//  Get all phone numbers                
		Cursor phones = context.getContentResolver().query(Phone.CONTENT_URI, 
				null, 
				Phone.CONTACT_ID + " = " + contactId, 
				null, 
				null);        
		while (phones.moveToNext()) {
			String number = phones.getString(phones.getColumnIndex(Phone.NUMBER));
			String label = phones.getString(phones.getColumnIndex(Phone.LABEL));
			if (label==null){
				// Label is not custom, get the string corresponding to the phone type
				int type = phones.getInt(phones.getColumnIndex(Phone.TYPE));
				label = context.getString(Phone.getTypeLabelResource(type));
			}
			// Check if this number is RCS
			String status = RichAddressBook.getInstance().getContactSharingStatus(number);
			if (status!=null
					&& (status.equalsIgnoreCase(RichAddressBookData.STATUS_ACTIVE))){
				rcsContactNumbers.add(new PhoneNumber(number, label));
			}
		}
		phones.close();
		return rcsContactNumbers;
	}
	
	/**
	 * Get all RCS related phone numbers from a contact
	 * 
	 * @param context
	 * @param contactId
	 * @return list List of all RCS phone numbers for the contact
	 */
	public static ArrayList<PhoneNumber> getRcsContactNumbers(Context context, long contactId) {
		ArrayList<PhoneNumber> rcsContactNumbers = new ArrayList<PhoneNumber>();
		if (RichAddressBook.getInstance()==null){
			RichAddressBook.createInstance(context);
		}
		
		//  Get all phone numbers                
		Cursor phones = context.getContentResolver().query(Phone.CONTENT_URI, 
				null, 
				Phone.CONTACT_ID + " = " + contactId, 
				null, 
				null);        
		while (phones.moveToNext()) {
			String number = phones.getString(phones.getColumnIndex(Phone.NUMBER));
			String label = phones.getString(phones.getColumnIndex(Phone.LABEL));
			if (label==null){
				// Label is not custom, get the string corresponding to the phone type
				int type = phones.getInt(phones.getColumnIndex(Phone.TYPE));
				label = context.getString(Phone.getTypeLabelResource(type));
			}
			// Check if this number is RCS
			String status = RichAddressBook.getInstance().getContactSharingStatus(number);
			if (status!=null){
				rcsContactNumbers.add(new PhoneNumber(number, label));
			}
		}
		phones.close();
		return rcsContactNumbers;
	}
	
	/**
	 * Get the primary number of a contact, return null if no one was set
	 * 
	 * @param context
	 * @param contactId
	 * @return number Primary number of the contact, or null if there isn't
	 */
	public static String getPrimaryNumber(Context context, long contactId) {
		Cursor c = context.getContentResolver().query(Data.CONTENT_URI,
				 new String[] {Data._ID, Phone.NUMBER, Phone.TYPE, Phone.LABEL, Data.IS_SUPER_PRIMARY},
				 Data.CONTACT_ID + "=?" + " AND " + Data.MIMETYPE + "='" + Phone.CONTENT_ITEM_TYPE + "'",          
				 new String[] {String.valueOf(contactId)}, null); 
		while (c.moveToNext()){
			if (c.getInt(4)!=0){
				// This phone is primary, so stop here
				return c.getString(1);
			}
		}
		c.close();
		return null;
	}
	
	/**
	 * Get the display name associated with the given contact id
	 * 
	 * @param context
	 * @param contactId
	 * @return
	 */
    public static String getContactDisplayName(Context context, long contactId) {
        String contactName = null;
        Cursor c = context.getContentResolver().query(
                ContentUris.withAppendedId(android.provider.ContactsContract.Contacts.CONTENT_URI, contactId),
                new String[] {android.provider.ContactsContract.Contacts.DISPLAY_NAME}, null, null, null);
        try {
            if (c != null && c.moveToFirst()) {
                contactName = c.getString(0);
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }

        if (contactName == null) {
            contactName = "";
        }

        return contactName;
    }
    
    public static class PhoneNumber {
    	
    	public String number;
    	public String label;
    	
    	public PhoneNumber(String number, String label){
    		this.number = number;
    		this.label = label;
    	}

    }

    
}
