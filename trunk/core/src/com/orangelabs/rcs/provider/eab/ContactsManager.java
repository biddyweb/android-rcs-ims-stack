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

package com.orangelabs.rcs.provider.eab;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.accounts.AccountManager;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.AggregationExceptions;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.CommonDataKinds.Website;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.StatusUpdates;
import android.text.TextUtils;

import com.orangelabs.rcs.R;
import com.orangelabs.rcs.addressbook.AuthenticationService;
import com.orangelabs.rcs.provider.settings.RcsSettings;
import com.orangelabs.rcs.service.api.client.capability.Capabilities;
import com.orangelabs.rcs.service.api.client.contacts.ContactInfo;
import com.orangelabs.rcs.service.api.client.presence.PhotoIcon;
import com.orangelabs.rcs.service.api.client.presence.PresenceInfo;
import com.orangelabs.rcs.utils.PhoneUtils;
import com.orangelabs.rcs.utils.logger.Logger;


/**
 * Contains utility methods for interfacing with the Android SDK ContactsProvider.
 */
public final class ContactsManager {

	/**
	 * Current instance
	 */
	private static ContactsManager instance = null;
	
	/**
	 * Context
	 */
	private Context ctx;
	
    /** 
     * Constant for invalid id. 
     */
	private static final int INVALID_ID = -1;

    /** 
     * MIME type for contact number
     */
    private static final String MIMETYPE_NUMBER = "vnd.android.cursor.item/com.orangelabs.rcs.number";

    /** 
     * MIME type for RCS status 
     */
    private static final String MIMETYPE_RCS_STATUS = "vnd.android.cursor.item/com.orangelabs.rcs.rcs-status";

    /** 
     * MIME type for RCS registration state 
     */
    private static final String MIMETYPE_REGISTRATION_STATE = "vnd.android.cursor.item/com.orangelabs.rcs.registration-state";
    
    /** 
     * MIME type for RCS status timestamp
     */
    private static final String MIMETYPE_RCS_STATUS_TIMESTAMP = "vnd.android.cursor.item/com.orangelabs.rcs.rcs-status.timestamp";
    
    /**
     * MIME type for presence status
     */
    private static final String MIMETYPE_PRESENCE_STATUS = "vnd.android.cursor.item/com.orangelabs.rcs.presence-status";

    /**
     * MIME type for free text
     */
    private static final String MIMETYPE_FREE_TEXT = "vnd.android.cursor.item/com.orangelabs.rcs.free-text";
    
    /** 
     * MIME type for web link 
     */
    private static final String MIMETYPE_WEBLINK = "vnd.android.cursor.item/com.orangelabs.rcs.weblink";

    /** 
     * MIME type for photo icon 
     */
    private static final String MIMETYPE_PHOTO = ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE;

    /** 
     * MIME type for photo icon etag 
     */
    private static final String MIMETYPE_PHOTO_ETAG = "vnd.android.cursor.item/com.orangelabs.rcs.photo-etag";

    /** 
     * MIME type for presence timestamp 
     */
    private static final String MIMETYPE_PRESENCE_TIMESTAMP = "vnd.android.cursor.item/com.orangelabs.rcs.presence.timestamp";
    
    /** 
     * MIME type for capability timestamp 
     */
    private static final String MIMETYPE_CAPABILITY_TIMESTAMP = "vnd.android.cursor.item/com.orangelabs.rcs.capability.timestamp";
    
    /** 
     * MIME type for CS_VIDEO capability
     */
    private static final String MIMETYPE_CAPABILITY_CS_VIDEO = "vnd.android.cursor.item/com.orangelabs.rcs.capability.cs-video";
    private static final String MIMETYPE_CAPABILITY_CS_VIDEO_DEACTIVATED = "vnd.android.cursor.item/com.orangelabs.rcs.capability.cs-video.deactivated";

    /** 
     * MIME type for GSMA_CS_IMAGE (image sharing) capability 
     */
    private static final String MIMETYPE_CAPABILITY_IMAGE_SHARING = "vnd.android.cursor.item/com.orangelabs.rcs.capability.image-sharing";
    private static final String MIMETYPE_CAPABILITY_IMAGE_SHARING_DEACTIVATED = "vnd.android.cursor.item/com.orangelabs.rcs.capability.image-sharing.deactivated";
    
    /** 
     * MIME type for 3GPP_CS_VOICE (video sharing) capability 
     */
    private static final String MIMETYPE_CAPABILITY_VIDEO_SHARING = "vnd.android.cursor.item/com.orangelabs.rcs.capability.video-sharing";
    private static final String MIMETYPE_CAPABILITY_VIDEO_SHARING_DEACTIVATED = "vnd.android.cursor.item/com.orangelabs.rcs.capability.video-sharing.deactivated";

    /** 
     * MIME type for RCS_IM (IM session) capability 
     */
    private static final String MIMETYPE_CAPABILITY_IM_SESSION = "vnd.android.cursor.item/com.orangelabs.rcs.capability.im-session";
    private static final String MIMETYPE_CAPABILITY_IM_SESSION_DEACTIVATED = "vnd.android.cursor.item/com.orangelabs.rcs.capability.im-session.deactivated";

    /** 
     * MIME type for RCS_FT (file transfer) capability 
     */
    private static final String MIMETYPE_CAPABILITY_FILE_TRANSFER = "vnd.android.cursor.item/com.orangelabs.rcs.capability.file-transfer";
    private static final String MIMETYPE_CAPABILITY_FILE_TRANSFER_DEACTIVATED = "vnd.android.cursor.item/com.orangelabs.rcs.capability.file-transfer.deactivated";

    /** 
     * MIME type for presence discovery capability 
     */
    private static final String MIMETYPE_CAPABILITY_PRESENCE_DISCOVERY = "vnd.android.cursor.item/com.orangelabs.rcs.capability.presence-discovery";

    /** 
     * MIME type for social presence capability 
     */
    private static final String MIMETYPE_CAPABILITY_SOCIAL_PRESENCE = "vnd.android.cursor.item/com.orangelabs.rcs.capability.social-presence";

    /** 
     * MIME type for RCS extensions 
     */
    private static final String MIMETYPE_CAPABILITY_EXTENSIONS = "vnd.android.cursor.item/com.orangelabs.rcs.capability.extensions";
    
    /** 
     * MIME type for my CS_VIDEO capability
     */
    private static final String MIMETYPE_MY_CAPABILITY_CS_VIDEO = "vnd.android.cursor.item/com.orangelabs.rcs.my-capability.cs-video";

    /** 
     * MIME type for my image sharing capability 
     */
    private static final String MIMETYPE_MY_CAPABILITY_IMAGE_SHARING = "vnd.android.cursor.item/com.orangelabs.rcs.my-capability.image-sharing";
    
    /** 
     * MIME type for my video sharing capability 
     */
    private static final String MIMETYPE_MY_CAPABILITY_VIDEO_SHARING = "vnd.android.cursor.item/com.orangelabs.rcs.my-capability.video-sharing";

    /** 
     * MIME type for my im_session capability 
     */
    private static final String MIMETYPE_MY_CAPABILITY_IM_SESSION = "vnd.android.cursor.item/com.orangelabs.rcs.my-capability.im-session";

    /** 
     * MIME type for my file transfer capability 
     */
    private static final String MIMETYPE_MY_CAPABILITY_FILE_TRANSFER = "vnd.android.cursor.item/com.orangelabs.rcs.my-capability.file-transfer";

    /** 
     * MIME type for my presence discovery capability 
     */
    private static final String MIMETYPE_MY_CAPABILITY_PRESENCE_DISCOVERY = "vnd.android.cursor.item/com.orangelabs.rcs.my-capability.presence-discovery";

    /** 
     * MIME type for my social presence capability 
     */
    private static final String MIMETYPE_MY_CAPABILITY_SOCIAL_PRESENCE = "vnd.android.cursor.item/com.orangelabs.rcs.my-capability.social-presence";
    
    /** 
     * MIME type for my RCS extensions 
     */
    private static final String MIMETYPE_MY_CAPABILITY_EXTENSIONS = "vnd.android.cursor.item/com.orangelabs.rcs.my-capability.extensions";

    /** 
     * MIME type for seeing my profile 
     */
    private static final String MIMETYPE_SEE_MY_PROFILE = "vnd.android.cursor.item/com.orangelabs.rcs.my-profile";
    
    /** 
     * MIME type for a RCS contact 
     */
    private static final String MIMETYPE_RCS_CONTACT = "vnd.android.cursor.item/com.orangelabs.rcs.rcs-contact";

    /** 
     * MIME type for a RCS capable contact 
     */
    private static final String MIMETYPE_RCS_CAPABLE_CONTACT = "vnd.android.cursor.item/com.orangelabs.rcs.rcs-capable-contact";
    
    /** 
     * MIME type for a non RCS contact 
     */
    private static final String MIMETYPE_NOT_RCS_CONTACT = "vnd.android.cursor.item/com.orangelabs.rcs.not-rcs-contact";

    /** 
     * MIME type for event log 
     */
    private static final String MIMETYPE_EVENT_LOG = "vnd.android.cursor.item/com.orangelabs.rcs.event-log";
    
    /** 
     * MIME type for block IM status 
     */
    private static final String MIMETYPE_IM_BLOCKED = "vnd.android.cursor.item/com.orangelabs.rcs.im-blocked";

    /** 
     * MIME type for weblink updated status 
     */
    private static final String MIMETYPE_WEBLINK_UPDATED = "vnd.android.cursor.item/com.orangelabs.rcs.weblink.updated";
    
    /**
     * ONLINE available status
     */
    private static final int PRESENCE_STATUS_ONLINE = 5; //StatusUpdates.AVAILABLE;

    /**
     * OFFLINE available status
     */
    private static final int PRESENCE_STATUS_OFFLINE = 0; //StatusUpdates.OFFLINE;
    
    /**
     * NOT SET available status
     */
    private static final int PRESENCE_STATUS_NOT_SET = 1; //StatusUpdates.INVISIBLE;


    /**
     * Registration status unknown
     */
    public static final int REGISTRATION_STATUS_UNKNOWN = 0;
    
    /**
     * Registration status offline
     */
    public static final int REGISTRATION_STATUS_OFFLINE = 1;
    
    /**
     * Registration status online
     */
    public static final int REGISTRATION_STATUS_ONLINE = 2;
    
    /**
     * Contact for "Me"
     */
    private static final String MYSELF = "myself";
    
	/**
	 * The logger
	 */
	private Logger logger = Logger.getLogger(getClass().getName());
    
	/**
	 * Create instance
	 * 
	 * @param ctx Context
	 */
	public static synchronized void createInstance(Context ctx) {
		if (instance == null) {
			instance = new ContactsManager(ctx);
		}
	}

	/**
	 * Returns instance
	 * 
	 * @return Instance
	 */
	public static ContactsManager getInstance() {
		return instance;
	}
	
    /**
     * Constructor
     *      
     * @param ctx Application context
     */
    private ContactsManager(Context ctx) {
    	this.ctx = ctx;
    }
    
    /**
	 * Set my presence info in the EAB
	 * 
	 * @param info Presence info
	 * @throws ContactsManagerException
	 */
	public void setMyInfo(PresenceInfo info) throws ContactsManagerException {
		if (logger.isActivated()) {
			logger.info("Set my presence info");
		}
		if (!RcsSettings.getInstance().isSocialPresenceSupported()){
			return;
		}

		// Set the availability status
		int availability = PRESENCE_STATUS_NOT_SET;
		if (info.isOnline()){
			availability = PRESENCE_STATUS_ONLINE;	
		}else if (info.isOffline()){
			availability = PRESENCE_STATUS_OFFLINE;
		}
		
		// Set the presence status 
		setContactPresenceStatus(MYSELF, availability);

		// Set the free text 
		setContactFreeText(MYSELF, info.getFreetext());
		
		// Set the web link
		setContactWeblink(MYSELF, info.getFavoriteLinkUrl(), true);
		
		// Set the capabilities
		setMyCapabilities(RcsSettings.getInstance().getMyCapabilities(), true, REGISTRATION_STATUS_ONLINE);
		
		// Set the photo-icon
		setContactPhotoIcon(MYSELF, info.getPhotoIcon());

		// Set the timestamp
		setContactPresenceTimestamp(MYSELF, info.getTimestamp());
	}
	
	/**
	 * Set my photo-icon in the EAB
	 * 
	 * @param photo Photo
	 * @throws ContactsManagerException
	 */
	public void setMyPhotoIcon(PhotoIcon photo)	throws ContactsManagerException {
		if (logger.isActivated()) {
			logger.info("Set my photo-icon");
		}
		
		if (!RcsSettings.getInstance().isSocialPresenceSupported()){
			return;
		}

		try {
			setContactPhotoIcon(MYSELF, photo);
		} catch (Exception e) {
			if (logger.isActivated()) {
				logger.error("Internal exception", e);
			}
			throw new ContactsManagerException(e.getMessage());
		}
	}

	/**
	 * Remove my photo-icon in the EAB
	 * 
	 * @throws ContactsManagerException
	 */
	public void removeMyPhotoIcon() throws ContactsManagerException {
		if (logger.isActivated()) {
			logger.info("Remove my photo-icon");
		}
		
		if (!RcsSettings.getInstance().isSocialPresenceSupported()){
			return;
		}

		try {
			setContactPhotoIcon(MYSELF, null);
		} catch (Exception e) {
			if (logger.isActivated()) {
				logger.error("Internal exception", e);
			}
			throw new ContactsManagerException(e.getMessage());
		}
	}

	/**
	 * Returns my presence info from the EAB
	 * 
	 * @return Presence info or null in case of error
	 */
	public PresenceInfo getMyPresenceInfo() {
		if (logger.isActivated()) {
			logger.info("Get my presence info");
		}
		if (!RcsSettings.getInstance().isPresenceServiceActivated()){
			return new PresenceInfo();
		}
		
		long rawContactId = getRawContactIdForMe();
		
		Cursor cursor = getRawContactDataCursor(rawContactId);
		
		return getContactInfoFromCursor(cursor, rawContactId).getPresenceInfo();
	}

	/**
	 * Set the presence info of a contact
	 * 
	 * @param contact Contact
	 * @param info Presence info
	 * @throws ContactsManagerException
	 */
	public void setContactInfo(String contact, ContactInfo info) throws ContactsManagerException {
		if (logger.isActivated()) {
			logger.info("Set contact info for " + contact);
		}
		
		// May be called from outside the core, so be sure the number format is international before doing the queries 
		contact = PhoneUtils.extractNumberFromUri(contact);
		
		// Set the availability status
		int availability = PRESENCE_STATUS_NOT_SET;
		if (info.getPresenceInfo().isOnline()){
			availability = PRESENCE_STATUS_ONLINE;	
		}else if (info.getPresenceInfo().isOffline()){
			availability = PRESENCE_STATUS_OFFLINE;
		}

		// Set the presence status 
		setContactPresenceStatus(contact, availability);

		// Set the free text 
		setContactFreeText(contact, info.getPresenceInfo().getFreetext());
		
		// Set the registration state
		setContactRegistrationState(contact, info.isRegistered());
		
		// Set the web link
		// Check if it has changed and position the flag accordingly
		String newWebLink = info.getPresenceInfo().getFavoriteLinkUrl();
		String currentWebLink = getContactWeblink(contact);
		if ((newWebLink!=null) && (!newWebLink.equalsIgnoreCase(currentWebLink))
				|| (newWebLink==null && currentWebLink!=null)){
			setWeblinkUpdatedFlag(contact, true);
		}		
		setContactWeblink(contact, info.getPresenceInfo().getFavoriteLinkUrl(), true);
		
		// Set the photo-icon
		setContactPhotoIcon(contact, info.getPresenceInfo().getPhotoIcon());
		
		// Set the timestamp
		setContactPresenceTimestamp(contact, info.getPresenceInfo().getTimestamp());
	}

	/**
	 * Set the photo-icon of a contact in the EAB
	 * 
	 * @param contact Contact
	 * @param photoIcon PhotoIcon
	 * @throws ContactsManagerException
	 */
	public void setContactPhotoIcon(String contact, PhotoIcon photoIcon) throws ContactsManagerException {
		if (logger.isActivated()) {
			logger.info("Set photo-icon for contact " + contact);
		}

		if (!contact.equalsIgnoreCase(MYSELF)){
			// May be called from outside the core, so be sure the number format is international before doing the queries 
			contact = PhoneUtils.extractNumberFromUri(contact);
		}
		
		long rawContactId = getRcsRawContactIdFromContact(contact);
		
		try {
			setContactPhoto(rawContactId, photoIcon, true);
		} catch (Exception e) {
			if (logger.isActivated()) {
				logger.error("Internal exception", e);
			}
			throw new ContactsManagerException(e.getMessage());
		}
		
		// Set the timestamp
		setContactPresenceTimestamp(contact, System.currentTimeMillis());
	}
	
	/**
	 * Remove the photo-icon of a contact in the EAB
	 * 
	 * @param contact Contact
	 * @param photoIcon PhotoIcon
	 * @throws ContactsManagerException
	 */
	public void removeContactPhotoIcon(String contact) throws ContactsManagerException {
		if (logger.isActivated()) {
			logger.info("Remove the photo-icon for contact " + contact);
		}

		if (!contact.equalsIgnoreCase(MYSELF)){
			// May be called from outside the core, so be sure the number format is international before doing the queries 
			contact = PhoneUtils.extractNumberFromUri(contact);
		}
		
		long rawContactId = getRcsRawContactIdFromContact(contact);
		
		String[] projection = { Data._ID };
        String selection = Data.RAW_CONTACT_ID + "=? AND " + Data.MIMETYPE + "=?";
        String[] selectionArgs = { Long.toString(rawContactId), Photo.CONTENT_ITEM_TYPE };
        String sortOrder = Data._ID + " DESC";

        Cursor cur = ctx.getContentResolver().query(Data.CONTENT_URI, 
        		projection, 
        		selection,
                selectionArgs, 
                sortOrder);
        if (cur!=null && cur.moveToNext()) {
        	long dataId = cur.getLong(cur.getColumnIndex(Data._ID));
        	ctx.getContentResolver().delete(Data.CONTENT_URI, 
        			Data._ID + "=" + dataId,
        			null);
        }
		
		// Set the timestamp
		setContactPresenceTimestamp(contact, System.currentTimeMillis());
	}

	/**
	 * Get the infos of a contact in the EAB
	 *  	
	 * @param contact Contact
	 * @return Contact info
	 */
	public ContactInfo getContactInfo(String contact) {
		if (logger.isActivated()) {
			logger.info("Get contact info for " + contact);
		}

		// May be called from outside the core, so be sure the number format is international before doing the queries 
		contact = PhoneUtils.extractNumberFromUri(contact);
		
		long rawContactId = getRcsRawContactIdFromContact(contact);
		
		if (!isRawContactRcs(rawContactId)){
			if (logger.isActivated()){
				logger.debug("Contact is not RCS");
			}
			ContactInfo info = new ContactInfo();
			info.setRcsStatus(ContactInfo.NOT_RCS);
			info.setRcsStatusTimestamp(System.currentTimeMillis());
			info.setContact(contact);		
			
			return info;
		}
		
		Cursor cursor = getRawContactDataCursor(rawContactId);
		
		return getContactInfoFromCursor(cursor, rawContactId);		
	}

	/**
	 * Set the sharing status of a contact in the EAB
	 * 
	 * @param contact Contact
	 * @param status Sharing status
	 * @param reason Reason associated to the status
	 * @throws ContactsManagerException
	 */
	public void setContactSharingStatus(String contact, String status, String reason) throws ContactsManagerException {
		if (logger.isActivated()) {
			logger.info("Set sharing status for contact " + contact + " to "+status+ " with reason "+reason);
		}
		// May be called from outside the core, so be sure the number format is international before doing the queries 
		contact= PhoneUtils.extractNumberFromUri(contact);
		
		if (!isRCSValidNumber(contact)){
			if (logger.isActivated()){
				logger.debug(contact +" is not a RCS valid number");
			}
			return;
		}
		
		try {
			// Get the current contact sharing status EAB database, if there is one
			String currentStatus = getContactSharingStatus(contact);
			if (currentStatus!=null && !currentStatus.equalsIgnoreCase(PresenceInfo.RCS_CANCELLED)){
				// We already are in a given RCS state, different from cancelled
				/**
				 * INVITED STATE
				 */
				if (currentStatus.equalsIgnoreCase(PresenceInfo.RCS_PENDING_OUT)){
					// State: we have invited the remote contact
					// We leave this state only on a "terminated/rejected" or an "active" status
					if(status.equalsIgnoreCase("terminated") &&
							(reason != null) && reason.equalsIgnoreCase("rejected")){
						// We have ended our profile sharing, destroy entry in EAB
						removeContact(contact);
						if (logger.isActivated()) {
							logger.debug(contact + " has passed from \"pending_out\" to \"terminated/rejected\" state");
							logger.debug("=> Remove contact entry from EAB");
						}
						return;
					}else if (status.equalsIgnoreCase("active")){
						// Contact has accepted our invitation, we are now active
						// Update entry in EAB
						setContactRcsPresenceStatus(contact, PresenceInfo.RCS_ACTIVE);
						// Set contact type from RCS-capable to RCS
						setContactType(contact, PresenceInfo.RCS_ACTIVE);
						if (logger.isActivated()) {
							logger.debug(contact + " has passed from \"pending_out\" to \"active\" state");
						}
						return;
					}
				}
					
				/**
				 * WILLING STATE
				 */
				if (currentStatus.equalsIgnoreCase(PresenceInfo.RCS_PENDING)){
					// State: we have been invited by the remote contact
					if(status.equalsIgnoreCase("active")){
						// We have accepted the invitation, we are now active
						// Update entry in EAB
						setContactRcsPresenceStatus(contact, PresenceInfo.RCS_ACTIVE);
						// Set contact type from RCS-capable to RCS
						setContactType(contact, PresenceInfo.RCS_ACTIVE);
						if (logger.isActivated()) {
							logger.debug(contact + " has passed from \"pending\" to \"active\" state");
						}
						return;
					}else if (status.equalsIgnoreCase("terminated") &&
							(reason != null) && reason.equalsIgnoreCase("giveup")){
						// Contact has cancelled its invitation
						// Destroy entry in EAB
						removeContact(contact);
						if (logger.isActivated()) {
							logger.debug(contact + " has passed from \"pending\" to \"terminated/giveup\" state");
							logger.debug("=> Remove contact entry from EAB");
						}
						return;
					}else if (status.equalsIgnoreCase(PresenceInfo.RCS_BLOCKED)){
						// We have declined the invitation
						// Update entry in EAB
						setContactRcsPresenceStatus(contact, PresenceInfo.RCS_BLOCKED);
						if (logger.isActivated()) {
							logger.debug(contact + " has passed from \"pending\" to \"blocked\" state");
						}
						return;
					}
				}
				
				/**
				 * ACTIVE STATE
				 */
				if (currentStatus.equalsIgnoreCase(PresenceInfo.RCS_ACTIVE)){
					// State: we have shared our profile with contact
					// We leave this state on a "terminated/rejected" status
					if(status.equalsIgnoreCase("terminated") &&
							(reason != null) && reason.equalsIgnoreCase("rejected")){
						// We have ended our profile sharing, destroy entry in EAB
						removeContact(contact);
						if (logger.isActivated()) {
							logger.debug(contact + " has passed from \"active\" to \"terminated/rejected\" state");
							logger.debug("=> Remove contact entry from EAB");
						}
						return;
					}
					// Or if we revoked the contact
					if (status.equalsIgnoreCase(PresenceInfo.RCS_REVOKED)){
						removeContact(contact);
						if (logger.isActivated()) {
							logger.debug(contact + " has passed from \"active\" to \"revoked\" state");
							logger.debug("=> Remove contact entry from EAB");
						}
						return;
					}
				}
				
				/**
				 * BLOCKED STATE
				 */
				if (currentStatus.equalsIgnoreCase(PresenceInfo.RCS_BLOCKED)){
					// State: we have blocked this contact
					// We leave this state only when user unblocks, ie destroys the entry
						return;
				}
				
			}else if (currentStatus==null
					|| (currentStatus.equalsIgnoreCase(PresenceInfo.RCS_CANCELLED))){
				// We have no entry for contact in EAB or it was in cancelled state
				/**
				 * NO ENTRY IN EAB
				 */
				
				if (status.equalsIgnoreCase(PresenceInfo.RCS_PENDING_OUT)){
					// Create an entry in EAB (if it already exists an entry, re-use it)
					createRcsContact(null, contact, status);
					// We invite contact to share presence
					// Contact has accepted our invitation, we are now active
					// Update entry in EAB
					setContactRcsPresenceStatus(contact, PresenceInfo.RCS_PENDING_OUT);
					if (logger.isActivated()) {
						logger.debug(contact + " has been added to the EAB with the \"pending_out\" state");
					}
					return;
				}
				
				if (status.equalsIgnoreCase(PresenceInfo.RCS_ACTIVE)){
					// Create an entry in EAB (if it already exists an entry, re-use it)
					createRcsContact(null, contact, status);
					// We received an active state from the contact, but we have no entry for him in EAB yet
					// It may occur if the number was deleted from native EAB, or if there was an error when we deleted/modified it
					// or if we logged on this RCS account on a new phone
					// Update entry in EAB
					setContactRcsPresenceStatus(contact, PresenceInfo.RCS_ACTIVE);
					if (logger.isActivated()) {
						logger.debug(contact + " has been added to the EAB with the \"active\" state");
					}
					return;
				}

				if (status.equalsIgnoreCase(PresenceInfo.RCS_PENDING)){
					// We received a "pending" notification => contact has invited us 
					// Update entry in EAB
					setContactRcsPresenceStatus(contact, PresenceInfo.RCS_PENDING);
					if (logger.isActivated()) {
						logger.debug(contact + " has been added to the EAB with the \"pending\" state");
					}
					return;
				}
				
				if (status.equalsIgnoreCase(PresenceInfo.RCS_BLOCKED)){
					// Create an entry in EAB (if it already exists an entry, re-use it)
					createRcsContact(null, contact, status);
					// We block the contact to prevent invitations from him
					// Update entry in EAB
					setContactRcsPresenceStatus(contact, PresenceInfo.RCS_BLOCKED);
					if (logger.isActivated()) {
						logger.debug(contact + " has been added to the EAB with the \"blocked\" state");
					}
					return;
				}
			}

		} catch (Exception e) {
			if (logger.isActivated()) {
				logger.error("Internal exception", e);
			}
			throw new ContactsManagerException(e.getMessage());
		}
	}
	
	/**
	 * Get sharing status of a contact
	 *  
	 * @param contact Contact
	 * @return Status or null if contact not found or in case of error
	 */
	public String getContactSharingStatus(String contact) {
		if (logger.isActivated()) {
			logger.info("Get sharing status for contact " + contact);
		}
		
		// May be called from outside the core, so be sure the number format is international before doing the queries 
		contact = PhoneUtils.extractNumberFromUri(contact);
		
		String result = null;
		try {
			
			// Get this number status in address book provider
			Cursor cursor = ctx.getContentResolver().query(RichAddressBookData.CONTENT_URI, 
					new String[]{RichAddressBookData.KEY_PRESENCE_SHARING_STATUS}, 
					RichAddressBookData.KEY_CONTACT_NUMBER + "=?", 
					new String[]{contact},
					null);
			if (cursor.moveToFirst()){
				result = cursor.getString(0);
			}
			cursor.close();
			
	        if (logger.isActivated()) {
				logger.debug("Sharing status is " + result);
			}			
		} catch (Exception e) {
			if (logger.isActivated()) {
				logger.error("Internal exception", e);
			}
		}
		return result;
	}

	/**
	 * Get the timestamp of last sharing status modification for a contact
	 *  
	 * @param contact Contact
	 * @return Timestamp
	 */
	public long getContactSharingTimestamp(String contact) {
		if (logger.isActivated()) {
			logger.info("Get last sharing status timestamp for contact " + contact);
		}
		
		// May be called from outside the core, so be sure the number format is international before doing the queries 
		contact = PhoneUtils.extractNumberFromUri(contact);
		
		long result = 0L;
		try {
			
			// Get this number status in address book provider
			Cursor cursor = ctx.getContentResolver().query(RichAddressBookData.CONTENT_URI, 
					new String[]{RichAddressBookData.KEY_TIMESTAMP}, 
					RichAddressBookData.KEY_CONTACT_NUMBER + "=?", 
					new String[]{contact},
					null);
			if (cursor.moveToFirst()){
				result = cursor.getLong(0);
			}
			cursor.close();
			
	        if (logger.isActivated()) {
				logger.debug("Last sharing timestamp is " + result);
			}			
		} catch (Exception e) {
			if (logger.isActivated()) {
				logger.error("Internal exception", e);
			}
		}
		return result;
	}
	
	/**
	 * Set the weblink visited status for the given contact
	 * 
	 * @param contact Contact
	 * @param updated Updated flag
	 */
	public void setWeblinkUpdatedFlag(String contact, boolean updated){
		if (logger.isActivated()) {
			logger.info("Set weblink updated flag for contact " + contact + " to "+updated);
		}
		
		// May be called from outside the core, so be sure the number format is international before doing the queries
		if (!contact.equalsIgnoreCase(MYSELF)){
			contact = PhoneUtils.extractNumberFromUri(contact);
		}

		// Update the database
		setCapabilityForContact(contact, MIMETYPE_WEBLINK_UPDATED, updated);
	}
	
	/**
	 * Get the weblink updated status for the given contact
	 * 
	 * @param contact Contact
	 * @return updated Updated flag
	 */
	public boolean getWeblinkUpdatedFlag(String contact){
		if (logger.isActivated()) {
			logger.info("Get updated flag for contact " + contact);
		}
		
		// May be called from outside the core, so be sure the number format is international before doing the queries 
		if (!contact.equalsIgnoreCase(MYSELF)){
			contact = PhoneUtils.extractNumberFromUri(contact);
		}

		// Query the database
		return getCapabilityForContact(contact, MIMETYPE_WEBLINK_UPDATED);
	}
	
	/**
	 * Revoke a contact
	 * 
	 * @param contact Contact
	 * @throws ContactsManagerException
	 */
	public void revokeContact(String contact) throws ContactsManagerException {
		if (logger.isActivated()) {
			logger.info("Revoke contact " + contact);
		}

		// May be called from outside the core, so be sure the number format is international before doing the queries 
		contact = PhoneUtils.extractNumberFromUri(contact);
		
		try {
			setContactRcsPresenceStatus(contact, PresenceInfo.RCS_REVOKED);
		} catch (Exception e) {
			if (logger.isActivated()) {
				logger.error("Internal exception", e);
			}
			throw new ContactsManagerException(e.getMessage());
		}
	}

	/**
	 * Unrevoke a contact
	 * 
	 * @param contact Contact
	 * @throws ContactsManagerException
	 */
	public void unrevokeContact(String contact) throws ContactsManagerException {
		if (logger.isActivated()) {
			logger.info("Unrevoke contact " + contact);
		}

		// May be called from outside the core, so be sure the number format is international before doing the queries 
		contact = PhoneUtils.extractNumberFromUri(contact);

		try {
			// Delete from the EAB database
			removeContact(contact);
		} catch (Exception e) {
			if (logger.isActivated()) {
				logger.error("Internal exception", e);
			}
			throw new ContactsManagerException(e.getMessage());
		}
	}

	/**
	 * Block a contact
	 * 
	 * @param contact Contact
	 * @throws ContactsManagerException
	 */	
	public void blockContact(String contact) throws ContactsManagerException {
		if (logger.isActivated()) {
			logger.info("Block contact " + contact);
		}

		// May be called from outside the core, so be sure the number format is international before doing the queries 
		contact = PhoneUtils.extractNumberFromUri(contact);

		try {
			setContactRcsPresenceStatus(contact, PresenceInfo.RCS_BLOCKED);
		} catch (Exception e) {
			if (logger.isActivated()) {
				logger.error("Internal exception", e);
			}
			throw new ContactsManagerException(e.getMessage());
		}
	}

	/**
	 * Unblock a contact
	 * 
	 * @param contact Contact
	 * @throws ContactsManagerException
	 */	
	public void unblockContact(String contact) throws ContactsManagerException {
		if (logger.isActivated()) {
			logger.info("Unblock contact " + contact);
		}

		// May be called from outside the core, so be sure the number format is international before doing the queries 
		contact = PhoneUtils.extractNumberFromUri(contact);

		try {
			removeContact(contact);
		} catch (Exception e) {
			if (logger.isActivated()) {
				logger.error("Internal exception", e);
			}
			throw new ContactsManagerException(e.getMessage());
		}
	}

	/**
	 * Flush the rich address book provider
	 */
	public void flushContactProvider(){
		String where = RichAddressBookData.KEY_CONTACT_NUMBER +"<> NULL";
		ctx.getContentResolver().delete(RichAddressBookData.CONTENT_URI, where, null);
	}
	
	/**
	 * Add or modify a contact number to the rich address book provider
	 * 
	 * @param contact
	 * @param RCS status
	 */
	public void addOrModifyRcsContactInProvider(String contact, String rcsStatus){
		// Check if an add or a modify must be done
		Cursor cursor = ctx.getContentResolver().query(RichAddressBookData.CONTENT_URI, 
				new String[]{RichAddressBookData.KEY_ID}, 
				RichAddressBookData.KEY_CONTACT_NUMBER + "=?", 
				new String[]{contact}, 
				null);
		long contactRowID = INVALID_ID;
		if (cursor.moveToFirst()){
			contactRowID = cursor.getLong(0);
		}
		cursor.close();

		ContentValues values = new ContentValues();
		values.put(RichAddressBookData.KEY_CONTACT_NUMBER, contact);
		values.put(RichAddressBookData.KEY_PRESENCE_SHARING_STATUS, rcsStatus);
		values.put(RichAddressBookData.KEY_TIMESTAMP, System.currentTimeMillis());
		
		if (contactRowID==INVALID_ID){
			// Contact not present in provider, insert
			ctx.getContentResolver().insert(RichAddressBookData.CONTENT_URI, values);
		}else{
			// Contact already present, update
			ctx.getContentResolver().update(RichAddressBookData.CONTENT_URI, 
					values,
					RichAddressBookData.KEY_CONTACT_NUMBER +"=?",
					new String[]{contact});
		}
	}
	
	/**
	 * Get the RCS contacts in the rich address book provider, ie which have a presence relationship with the user
	 * 
	 * @return list containing all RCS contacts, "Me" item excluded 
	 */
	public List<String> getRcsContactsWithSocialPresence(){
		List<String> rcsNumbers = new ArrayList<String>();
		Cursor c = ctx.getContentResolver().query(RichAddressBookData.CONTENT_URI, 
				new String[] { RichAddressBookData.KEY_CONTACT_NUMBER}, 
				null, 
				null, 
				null);
		while (c.moveToNext()) {
			rcsNumbers.add(c.getString(0));
		}
		c.close();
		return rcsNumbers;
	}
	
	/**
	 * Get the RCS contacts in the contact contract provider
	 * 
	 * @return list containing all RCS contacts 
	 */
	public List<String> getRcsContacts(){
		List<String> rcsNumbers = new ArrayList<String>();
		String[] projection = {
                Data._ID, 
                Data.MIMETYPE, 
                Data.DATA1, 
                Data.DATA2          
        };

        // Filter the mime types 
        String selection = "(" + Data.MIMETYPE + "=? OR " 
                + Data.MIMETYPE + "=?)";
        String[] selectionArgs = {
                MIMETYPE_RCS_CAPABLE_CONTACT,
                MIMETYPE_RCS_CONTACT
        };

        Cursor cur = ctx.getContentResolver().query(Data.CONTENT_URI, 
        		projection, 
        		selection, 
        		selectionArgs, 
        		null);
		
		while (cur.moveToNext()) {
			String rcsNumber = cur.getString(2);
			if (!rcsNumbers.contains(rcsNumber)){
				rcsNumbers.add(rcsNumber);
			}
		}
		cur.close();
		return rcsNumbers;
	}
	
	/**
	 * Get all the contacts in the contact contract provider that have a RCS raw contact
	 * 
	 * @return list containing all contacts that have been at least queried once for capabilities 
	 */
	public List<String> getAllContacts(){
		List<String> numbers = new ArrayList<String>();
		String[] projection = {
                Data._ID, 
                Data.MIMETYPE, 
                Data.DATA1, 
                Data.DATA2          
        };

        // Filter the mime types 
        String selection = Data.MIMETYPE + "=?";
        String[] selectionArgs = {
        		MIMETYPE_NUMBER
        };

        Cursor cur = ctx.getContentResolver().query(Data.CONTENT_URI, 
        		projection, 
        		selection, 
        		selectionArgs, 
        		null);
		
		while (cur.moveToNext()) {
			String number = cur.getString(2);
			if (!numbers.contains(number)){
				numbers.add(number);
			}
		}
		cur.close();
		return numbers;
	}
	
	/**
	 * Get the RCS contacts in the rich address book provider
	 * 
	 * @return list containing all RCS blocked contacts
	 */
	public List<String> getRcsBlockedContacts(){
		List<String> rcsNumbers = new ArrayList<String>();
		Cursor c = ctx.getContentResolver().query(RichAddressBookData.CONTENT_URI, 
				new String[] { RichAddressBookData.KEY_CONTACT_NUMBER}, 
				RichAddressBookData.KEY_PRESENCE_SHARING_STATUS + "=\""+PresenceInfo.RCS_BLOCKED+"\"", 
				null, 
				null);
		while (c.moveToNext()) {
			rcsNumbers.add(c.getString(0));
		}
		c.close();
		return rcsNumbers;
	}
	
	/**
	 * Get the RCS contacts in the rich address book provider
	 * 
	 * @return list containing all RCS invited contacts
	 */
	public List<String> getRcsInvitedContacts(){
		List<String> rcsNumbers = new ArrayList<String>();
		Cursor c = ctx.getContentResolver().query(RichAddressBookData.CONTENT_URI, 
				new String[] { RichAddressBookData.KEY_CONTACT_NUMBER}, 
				RichAddressBookData.KEY_PRESENCE_SHARING_STATUS + "=\""+PresenceInfo.RCS_PENDING_OUT+"\"", 
				null, 
				null);
		while (c.moveToNext()) {
			rcsNumbers.add(c.getString(0));
		}
		c.close();
		return rcsNumbers;
	}
	
	/**
	 * Get the RCS contacts in the rich address book provider
	 * 
	 * @return list containing all RCS willing contacts
	 */
	public List<String> getRcsWillingContacts(){
		List<String> rcsNumbers = new ArrayList<String>();
		Cursor c = ctx.getContentResolver().query(RichAddressBookData.CONTENT_URI, 
				new String[] { RichAddressBookData.KEY_CONTACT_NUMBER}, 
				RichAddressBookData.KEY_PRESENCE_SHARING_STATUS + "=\""+PresenceInfo.RCS_PENDING+"\"", 
				null, 
				null);
		while (c.moveToNext()) {
			rcsNumbers.add(c.getString(0));
		}
		c.close();
		return rcsNumbers;
	}
	
	/**
	 * Get the RCS contacts in the rich address book provider
	 * 
	 * @return list containing all RCS cancelled contacts
	 */
	public List<String> getRcsCancelledContacts(){
		List<String> rcsNumbers = new ArrayList<String>();
		Cursor c = ctx.getContentResolver().query(RichAddressBookData.CONTENT_URI, 
				new String[] { RichAddressBookData.KEY_CONTACT_NUMBER}, 
				RichAddressBookData.KEY_PRESENCE_SHARING_STATUS + "=\""+PresenceInfo.RCS_CANCELLED+"\"", 
				null, 
				null);
		while (c.moveToNext()) {
			rcsNumbers.add(c.getString(0));
		}
		c.close();
		return rcsNumbers;
	}
	
	/**
	 * Remove a cancelled invitation in the rich address book provider
	 * 
	 * @param contact
	 */
	public void removeCancelledPresenceInvitation(String contact){
        // Remove entry from rich address book provider
		ctx.getContentResolver().delete(RichAddressBookData.CONTENT_URI, 
				RichAddressBookData.KEY_CONTACT_NUMBER +"=?" + " AND " + RichAddressBookData.KEY_PRESENCE_SHARING_STATUS + "=?",
				new String[]{contact, PresenceInfo.RCS_CANCELLED});
	}
	
	/**
	 * Is the number in the RCS blocked list
	 * 
	 * @param number Number to check
	 * @return boolean
	 */
	public boolean isNumberBlocked(String number) {
		// Get this number status in address book
		String status = getContactSharingStatus(number);
		if (status!=null && status.equalsIgnoreCase(PresenceInfo.RCS_BLOCKED)){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * Is the number in the RCS buddy list
	 * 
	 * @param number Number to check
	 * @return boolean
	 */
	public boolean isNumberShared(String number) {
		// Get this number status in address book provider
		String status = null;
		Cursor cursor = ctx.getContentResolver().query(RichAddressBookData.CONTENT_URI, 
				new String[]{RichAddressBookData.KEY_PRESENCE_SHARING_STATUS}, 
				RichAddressBookData.KEY_CONTACT_NUMBER + "=?", 
				new String[]{number},
				null);
		if (cursor.moveToFirst()){
			status = cursor.getString(0);
		}
		cursor.close();
		if (status!=null && status.equalsIgnoreCase(PresenceInfo.RCS_ACTIVE)){
			return true;
		}else{
			return false;
		}
	}

	/**
	 * Has the number been invited to RCS
	 * 
	 * @param number Number to check
	 * @return boolean
	 */
	public boolean isNumberInvited(String number) {
		// Get this number status in address book provider
		String status = null;
		Cursor cursor = ctx.getContentResolver().query(RichAddressBookData.CONTENT_URI, 
				new String[]{RichAddressBookData.KEY_PRESENCE_SHARING_STATUS}, 
				RichAddressBookData.KEY_CONTACT_NUMBER + "=?", 
				new String[]{number},
				null);
		if (cursor.moveToFirst()){
			status = cursor.getString(0);
		}
		cursor.close();
		if (status!=null && status.equalsIgnoreCase(PresenceInfo.RCS_PENDING)){
			return true;
		}else{
			return false;
		}
	}

	/**
	 * Has the number invited us to RCS
	 * 
	 * @param number Number to check
	 * @return boolean
	 */
	public boolean isNumberWilling(String number) {
		// Get this number status in address book provider
		String status = null;
		Cursor cursor = ctx.getContentResolver().query(RichAddressBookData.CONTENT_URI, 
				new String[]{RichAddressBookData.KEY_PRESENCE_SHARING_STATUS}, 
				RichAddressBookData.KEY_CONTACT_NUMBER + "=?", 
				new String[]{number},
				null);
		if (cursor.moveToFirst()){
			status = cursor.getString(0);
		}
		cursor.close();
		if (status!=null && status.equalsIgnoreCase(PresenceInfo.RCS_PENDING_OUT)){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * Has the number invited us to RCS then be cancelled
	 * 
	 * @param number Number to check
	 * @return boolean
	 */
	public boolean isNumberCancelled(String number) {
		// Get this number status in address book provider
		String status = null;
		Cursor cursor = ctx.getContentResolver().query(RichAddressBookData.CONTENT_URI, 
				new String[]{RichAddressBookData.KEY_PRESENCE_SHARING_STATUS}, 
				RichAddressBookData.KEY_CONTACT_NUMBER + "=?", 
				new String[]{number},
				null);
		if (cursor.moveToFirst()){
			status = cursor.getString(0);
		}
		cursor.close();
		if (status!=null && status.equalsIgnoreCase(PresenceInfo.RCS_CANCELLED)){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * Get the corresponding capability for the contact
	 * 
	 * @param number
	 * @param mimeType Mime type associated to the capability
	 * @return True if the contact is capable to handle the given capability, else false
	 */
	private boolean getCapabilityForContact(String number, String mimeType){
		long rawContactId = getRcsRawContactIdFromContact(number);
    	
        long dataId = getDataIdForRawContact(rawContactId, mimeType);
        if (dataId == INVALID_ID) {
            return false;
        }else{
        	return true;
        }
	}
	
	/**
	 * Check if number provided is a valid number for RCS
	 * <br>It is not valid if :
	 * <li>it is not well formatted (not digits only or '+')
	 * <li>it is an emergency number
	 * <li>it has not at least 12 digits (french numbers in international format) 
	 * 
	 * @param phoneNumber
	 * @return true if it is a RCS valid number
	 */
    public boolean isRCSValidNumber(String phoneNumber){
        return android.telephony.PhoneNumberUtils.isGlobalPhoneNumber(phoneNumber) 
        	&& !android.telephony.PhoneNumberUtils.isEmergencyNumber(phoneNumber)
        	&& phoneNumber.length()>11; //TODO generalize for all international formats
    }
	
	/**
	 * Set the corresponding capability for the contact
	 * 
	 * @param number
	 * @param mimeType Mime type associated to the capability
	 * @param isCapable If true, add the capability, else remove it
	 */
	public void setCapabilityForContact(String number, String mimeType, boolean isCapable){
		if (!isRCSValidNumber(number)&&(!number.equalsIgnoreCase(MYSELF))){
			if (logger.isActivated()){
				logger.debug(number +" is not a RCS valid number");
			}
			return;
		}
		
		long rawContactId = getRcsRawContactIdFromContact(number);
		
		if (rawContactId == INVALID_ID){
			// Could not find a RCS rawContact for this number, create one
			rawContactId = createRcsContact(null, number, ContactInfo.NOT_RCS);
		}
		
        long dataId = getDataIdForRawContact(rawContactId, mimeType);
        
        if (dataId == INVALID_ID) {
        	// The capability is not present for now
        	if (isCapable){
        		// We have to add it
        		ContentValues values = new ContentValues();
        		values.put(Data.RAW_CONTACT_ID, rawContactId); 
        		values.put(Data.MIMETYPE, mimeType);
        		values.put(Data.DATA1, number);
        		String mimeTypeDescription = getMimeTypeDescription(mimeType);
        		if (mimeTypeDescription!=null){
        			// We add these information only if the 
	        		values.put(Data.DATA2, mimeTypeDescription);
	        		values.put(Data.DATA3, number);
        		}
        		ctx.getContentResolver().insert(Data.CONTENT_URI, values);
        	}
        }else{
        	// The capability is present
        	if (!isCapable){
        		// We have to remove it
        		ctx.getContentResolver().delete(Data.CONTENT_URI, 
        				Data._ID + "=?", 
        				new String[] {String.valueOf(dataId)});
        	}
        }
	}
	
	/**
	 * Set the RCS extensions capability for the contact
	 * 
	 * @param number
	 * @param mimeType Mime type associated to the capability
	 * @param extensions Extensions capabilities 
	 */
	public void setExtensionsCapabilityForContact(String number, String mimeType, ArrayList<String> extensions){
		if (!isRCSValidNumber(number)&&(!number.equalsIgnoreCase(MYSELF))){
			if (logger.isActivated()){
				logger.debug(number +" is not a RCS valid number");
			}
			return;
		}
		
		long rawContactId = getRcsRawContactIdFromContact(number);
		
		if (rawContactId == INVALID_ID){
			// Could not find a RCS rawContact for this number, create one
			rawContactId = createRcsContact(null, number, ContactInfo.NOT_RCS);
		}
		
        long dataId = getDataIdForRawContact(rawContactId, mimeType);
        
        if (dataId == INVALID_ID) {
        	// No extensions capability for now
        	if (extensions.size()>0){
        		// We have to add a new one
        		ContentValues values = new ContentValues();
        		values.put(Data.RAW_CONTACT_ID, rawContactId); 
        		values.put(Data.MIMETYPE, mimeType);
        		values.put(Data.DATA1, number);
        		StringBuffer extension = new StringBuffer();
        		for (int i=0;i<extensions.size();i++){
        			extension.append(extensions.get(i)+";");
        		}
        		values.put(Data.DATA2, extension.toString());
        			
        		ctx.getContentResolver().insert(Data.CONTENT_URI, values);
        	}
        }else{
        	// The extensions capability is present
        	
        	if (extensions.size()==0){
        		// We no longer have extensions, so remove it
        		ctx.getContentResolver().delete(Data.CONTENT_URI, 
        				Data._ID + "=?", 
        				new String[] {String.valueOf(dataId)});
        	}else{
        		// We have extensions, so modify the old one
        		ContentValues values = new ContentValues();
        		values.put(Data.RAW_CONTACT_ID, rawContactId); 
        		values.put(Data.MIMETYPE, mimeType);
        		values.put(Data.DATA1, number);
        		StringBuffer extension = new StringBuffer();
        		for (int i=0;i<extensions.size();i++){
        			extension.append(extensions.get(i)+";");
        		}
        		values.put(Data.DATA2, extension.toString());
        		
        		ctx.getContentResolver().update(Data.CONTENT_URI, 
        				values,
        				Data._ID + "=?", 
        				new String[] {String.valueOf(dataId)});
        	}
        }
	}
	
	/**
	 * Get description associated to a MIME type. This string will be visible in the contact card
	 * 
	 * @param mimeType MIME type
	 * @return String
	 */
	private String getMimeTypeDescription(String mimeType){
		if (mimeType.equalsIgnoreCase(MIMETYPE_CAPABILITY_FILE_TRANSFER)) {
			return ctx.getString(R.string.rcs_core_contact_file_transfer);
		} else
		if (mimeType.equalsIgnoreCase(MIMETYPE_CAPABILITY_IM_SESSION)) {
			return ctx.getString(R.string.rcs_core_contact_im_session);
		} else
		if (mimeType.equalsIgnoreCase(MIMETYPE_CAPABILITY_CS_VIDEO)) {
			return ctx.getString(R.string.rcs_core_contact_cs_video);
		} else
		if (mimeType.equalsIgnoreCase(MIMETYPE_EVENT_LOG)) {
			return ctx.getString(R.string.rcs_core_contact_event_log);
		} else 
			return null;
	}
	
	/**
	 * Get a list of numbers supporting Instant Messaging Session capability
	 * 
	 * @return list containing all contacts that are "IM capable" 
	 */
	public List<String> getImSessionCapableContacts() {
		List<String> IMCapableNumbers = new ArrayList<String>();
        String[] projection = {Data.DATA1, Data.MIMETYPE};
        String selectionImCapOff = Data.MIMETYPE + "=?";
        String[] selectionArgsImCapOff = { MIMETYPE_CAPABILITY_IM_SESSION };
    	Cursor c =  ctx.getContentResolver().query(Data.CONTENT_URI, 
        		projection, 
        		selectionImCapOff, 
        		selectionArgsImCapOff, 
        		null);
		
		while (c.moveToNext()) {
			String imCapableNumber = c.getString(0);
			if (!IMCapableNumbers.contains(imCapableNumber)){
				IMCapableNumbers.add(imCapableNumber);
			}
		}
		c.close();
		return IMCapableNumbers;
	}
	
	/**
	 * Get a list of numbers that can use richcall features
	 * <li>These are the contacts that can do either image sharing, video sharing, or both 
	 * 
	 * @return list containing all contacts that can use richcall features 
	 */
	public List<String> getRichcallCapableContacts() {
		List<String> richcallCapableNumbers = new ArrayList<String>();
        String[] projection = {Data.DATA1, Data.MIMETYPE};

        String selection = "( " + Data.MIMETYPE + "=? OR " + Data.MIMETYPE + "=? )";
        
        // Get all image sharing or video sharing capable contacts
        String[] selectionArgs = { MIMETYPE_CAPABILITY_IMAGE_SHARING, MIMETYPE_CAPABILITY_VIDEO_SHARING };
        Cursor c = ctx.getContentResolver().query(Data.CONTENT_URI, 
        		projection, 
        		selection, 
        		selectionArgs, 
        		null);
		
		while (c.moveToNext()) {
			String richcallCapableNumber = c.getString(0);
			if (!richcallCapableNumbers.contains(richcallCapableNumber)){
				richcallCapableNumbers.add(richcallCapableNumber);
			}
		}
		c.close();
		
		return richcallCapableNumbers;
	}
	
	/**
	 * Set contact type
	 * 
	 * @param contact
	 * @param type
	 */
	public void setContactType(String contact, String type){
		contact = PhoneUtils.extractNumberFromUri(contact);
		
		if (logger.isActivated()){
			logger.debug("Setting contact type for "+contact +" to "+type);
		}
		
		if (!isRCSValidNumber(contact)){
			if (logger.isActivated()){
				logger.debug(contact +" is not a RCS valid number");
			}
			return;
		}
		
		long rawContactId = getRcsRawContactIdFromContact(contact);
		
		if (rawContactId == INVALID_ID){
			// Could not find a RCS rawContact for this number, create one
			rawContactId = createRcsContact(null, contact, type);
		}
		
        long dataIdNotRcsCapable = getDataIdForRawContact(rawContactId, MIMETYPE_NOT_RCS_CONTACT);
        
        long dataIdRcsCapable = getDataIdForRawContact(rawContactId, MIMETYPE_RCS_CAPABLE_CONTACT);
        
        long dataIdRcsActive = getDataIdForRawContact(rawContactId, MIMETYPE_RCS_CONTACT);

        long dataIdEventLogRow = getDataIdForRawContact(rawContactId, MIMETYPE_EVENT_LOG);
        
    	ContentValues values = new ContentValues();
  		values.put(Data.RAW_CONTACT_ID, rawContactId); 
        values.put(Data.DATA1, contact);
        
        if (type.equalsIgnoreCase(ContactInfo.NOT_RCS)){
        	
        	// Remove mime-type capable and mime-type active if present
        	if (dataIdRcsCapable!=INVALID_ID){
        		ctx.getContentResolver().delete(Data.CONTENT_URI, 
        				Data._ID + "=?",
        				new String[]{Long.toString(dataIdRcsCapable)});
        	}
        	if (dataIdRcsActive!=INVALID_ID){
        		ctx.getContentResolver().delete(Data.CONTENT_URI, 
        				Data._ID + "=?",
        				new String[]{Long.toString(dataIdRcsActive)});
        	}
        	
        	// Add mime-type not capable
        	if (dataIdNotRcsCapable==INVALID_ID){
        		values.put(Data.MIMETYPE, MIMETYPE_NOT_RCS_CONTACT);        	
        		ctx.getContentResolver().insert(Data.CONTENT_URI, values);
        	}
        	
        	// Remove event log row if present
        	if (dataIdEventLogRow!=INVALID_ID){
        		ctx.getContentResolver().delete(Data.CONTENT_URI, 
        				Data._ID + "=?",
        				new String[]{Long.toString(dataIdEventLogRow)});
        	}
        	
        }else if (type.equalsIgnoreCase(PresenceInfo.RCS_ACTIVE)){

        	// Remove mime-type not capable and mime-type capable if present
        	if (dataIdNotRcsCapable!=INVALID_ID){
        		ctx.getContentResolver().delete(Data.CONTENT_URI, 
        				Data._ID + "=?",
        				new String[]{Long.toString(dataIdNotRcsCapable)});
        	}
        	if (dataIdRcsCapable!=INVALID_ID){
        		ctx.getContentResolver().delete(Data.CONTENT_URI, 
        				Data._ID + "=?",
        				new String[]{Long.toString(dataIdRcsCapable)});
        	}
        	
        	// Add mime-type RCS active
        	if (dataIdRcsActive==INVALID_ID){
        		values.put(Data.MIMETYPE, MIMETYPE_RCS_CONTACT);        	
        		ctx.getContentResolver().insert(Data.CONTENT_URI, values);
        	}
        	
            // Create event log row if not present
        	if (dataIdEventLogRow==INVALID_ID){
        		values.put(Data.MIMETYPE, MIMETYPE_EVENT_LOG);
        		values.put(Data.DATA2, getMimeTypeDescription(MIMETYPE_EVENT_LOG));
        		values.put(Data.DATA3, contact);
        		ctx.getContentResolver().insert(Data.CONTENT_URI, values);
        	}

        }else {
        	// Other types : contact is RCS capable
        	// Remove mime-type not capable and mime-type RCS active if present
        	if (dataIdNotRcsCapable!=INVALID_ID){
        		ctx.getContentResolver().delete(Data.CONTENT_URI, 
        				Data._ID + "=?",
        				new String[]{Long.toString(dataIdNotRcsCapable)});
        	}
        	if (dataIdRcsActive!=INVALID_ID){
        		ctx.getContentResolver().delete(Data.CONTENT_URI, 
        				Data._ID + "=?",
        				new String[]{Long.toString(dataIdRcsActive)});
        	}
        	
        	// Add mime-type RCS capable
        	if (dataIdRcsCapable==INVALID_ID){
        		values.put(Data.MIMETYPE, MIMETYPE_RCS_CAPABLE_CONTACT);        	
        		ctx.getContentResolver().insert(Data.CONTENT_URI, values);
        	}
        	
            // Create event log row if not present
        	if (dataIdEventLogRow==INVALID_ID){
        		values.put(Data.MIMETYPE, MIMETYPE_EVENT_LOG);
        		values.put(Data.DATA2, getMimeTypeDescription(MIMETYPE_EVENT_LOG));
        		values.put(Data.DATA3, contact);
        		ctx.getContentResolver().insert(Data.CONTENT_URI, values);
        	}
        }
        
        // Update the RCS status row
        setContactRcsPresenceStatus(contact, type);
        
	}

	/**
	 * Is the contact RCS active
	 * 
	 * @param contact
	 * @return boolean
	 */
	private boolean isContactRcsActive(String contact){
		contact = PhoneUtils.extractNumberFromUri(contact);
		if (isNumberShared(contact)){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * Is the contact RCS capable
	 * 
	 * @param contact
	 * @return boolean
	 */
	public boolean isContactRcsCapable(String contact){
		contact = PhoneUtils.extractNumberFromUri(contact);
		long rawContactId = getRcsRawContactIdFromContact(contact);
		
		if (getDataIdForRawContact(rawContactId, MIMETYPE_RCS_CAPABLE_CONTACT)!=INVALID_ID){
			return true;
		}else if (getDataIdForRawContact(rawContactId, MIMETYPE_RCS_CONTACT)!=INVALID_ID){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * Set my capabilities
	 * 
	 * @param capabilities Capabilities
	 * @param isSuccessfulQuery This flag is true if the capability discovery was complete, else false (e.g. contact is not RCS capable)
	 * @param registrationState Three possible values : 
	 */
	public void setMyCapabilities(Capabilities capabilities, boolean isSuccessfulQuery, int registrationState) {
		// This is for myself			
		// Cs Video
		setCapabilityForContact(MYSELF, MIMETYPE_MY_CAPABILITY_CS_VIDEO, capabilities.isCsVideoSupported());
		// File transfer
		setCapabilityForContact(MYSELF, MIMETYPE_MY_CAPABILITY_FILE_TRANSFER, capabilities.isFileTransferSupported());
		// Image sharing
		setCapabilityForContact(MYSELF, MIMETYPE_MY_CAPABILITY_IMAGE_SHARING, capabilities.isImageSharingSupported());
		// IM session
		setCapabilityForContact(MYSELF, MIMETYPE_MY_CAPABILITY_IM_SESSION, capabilities.isImSessionSupported());
		// Video sharing
		setCapabilityForContact(MYSELF, MIMETYPE_MY_CAPABILITY_VIDEO_SHARING, capabilities.isVideoSharingSupported());
		// Presence discovery
		setCapabilityForContact(MYSELF, MIMETYPE_MY_CAPABILITY_PRESENCE_DISCOVERY, capabilities.isPresenceDiscoverySupported());
		// Social presence
		setCapabilityForContact(MYSELF, MIMETYPE_MY_CAPABILITY_SOCIAL_PRESENCE, capabilities.isSocialPresenceSupported());
		// RCS extensions
		setExtensionsCapabilityForContact(MYSELF, MIMETYPE_MY_CAPABILITY_EXTENSIONS, capabilities.getSupportedExtensions());

		// Contact capabilities timestamp
		setContactCapabilitiesTimestamp(MYSELF, capabilities.getTimestamp());

		// Set the contact registration state
		switch (registrationState){
		case REGISTRATION_STATUS_ONLINE:
			setContactRegistrationState(MYSELF, true);
			break;
		case REGISTRATION_STATUS_OFFLINE:
			setContactRegistrationState(MYSELF, false);
			break;
		case REGISTRATION_STATUS_UNKNOWN:
			// Nothing to change
		default:
			break;
		}
	}

	/**
	 * Set contact capabilities
	 * 
	 * @param contact Contact
	 * @param capabilities Capabilities
	 * @param isSuccessfulQuery This flag is true if the capability discovery was complete, else false (e.g. contact is not RCS capable)
	 * @param registrationState Three possible values : 
	 */
	public void setContactCapabilities(String contact, Capabilities capabilities, boolean isSuccessfulQuery, int registrationState) {
		
		contact = PhoneUtils.extractNumberFromUri(contact);

		// Update capabilities in database, only if the number is in the address book
		if (!isNumberInAddressBook(contact)) {
			if (logger.isActivated()){
				logger.debug(contact +" is not a number in the address book, we do not save its capabilities");
			}
			return;
		}

		// Set the contact type 
		if (!isSuccessfulQuery || !capabilities.isImSessionSupported()){
			// The query was a failure or the IM tag was not present, so the contact is considered not RCS capable
			setContactType(contact, ContactInfo.NOT_RCS);
		}else{
			// The query was successful, the contact is at least RCS capable				
			if (isContactRcsActive(contact)){
				// The contact is even RCS active (we shared our presence)
				setContactType(contact, PresenceInfo.RCS_ACTIVE);
			}else{
				// Contact is RCS capable (we do not share our presence yet)
				setContactType(contact, ContactInfo.RCS_CAPABLE);
			}
		}			

		// Cs Video
		setCapabilityForContact(contact, MIMETYPE_CAPABILITY_CS_VIDEO, capabilities.isCsVideoSupported());
		// File transfer
		setCapabilityForContact(contact, MIMETYPE_CAPABILITY_FILE_TRANSFER, capabilities.isFileTransferSupported());
		// Image sharing
		setCapabilityForContact(contact, MIMETYPE_CAPABILITY_IMAGE_SHARING, capabilities.isImageSharingSupported());
		// IM session
		setCapabilityForContact(contact, MIMETYPE_CAPABILITY_IM_SESSION, capabilities.isImSessionSupported());
		// Video sharing
		setCapabilityForContact(contact, MIMETYPE_CAPABILITY_VIDEO_SHARING, capabilities.isVideoSharingSupported());
		// Presence discovery
		setCapabilityForContact(contact, MIMETYPE_CAPABILITY_PRESENCE_DISCOVERY, capabilities.isPresenceDiscoverySupported());
		// Social presence
		setCapabilityForContact(contact, MIMETYPE_CAPABILITY_SOCIAL_PRESENCE, capabilities.isSocialPresenceSupported());
		// RCS extensions
		setExtensionsCapabilityForContact(contact, MIMETYPE_CAPABILITY_EXTENSIONS, capabilities.getSupportedExtensions());

		// Contact capabilities timestamp
		setContactCapabilitiesTimestamp(contact, capabilities.getTimestamp());
		
		// Set the contact registration state
		switch (registrationState){
		case REGISTRATION_STATUS_ONLINE:
			setContactRegistrationState(contact, true);
			break;
		case REGISTRATION_STATUS_OFFLINE:
			setContactRegistrationState(contact, false);
			break;
		case REGISTRATION_STATUS_UNKNOWN:
			// Nothing to change
		default:
			break;
		}
	}
	
	/**
	 * Set registration state for a contact
	 * 
	 * @param contact Contact
	 * @param flag True if contact is actually registered, else false
	 */
	private void setContactRegistrationState(String contact, boolean flag){
		ContactInfo info = getContactInfo(contact);
		if (info!=null){
			// Check if the registration state has changed
			if ((info.isRegistered() && flag) || (!info.isRegistered() && !flag)){
				// No change in state, so nothing to do.
				return;
			}else{
				String freeText = null;
				if (info.getPresenceInfo()!=null){
					freeText = info.getPresenceInfo().getFreetext();
				}
				if (flag){
					//TODO uncomment following if OPTIONS refresh is done on QuickContact badge opening
					//activateContactCapabilities(contact, info.getCapabilities(), true);
					setContactFreeTextAndAvailability(contact, freeText, PRESENCE_STATUS_ONLINE, info.getCapabilities().getTimestamp());
					setCapabilityForContact(contact, MIMETYPE_REGISTRATION_STATE, true);
				}else{
					//TODO uncomment following if OPTIONS refresh is done on QuickContact badge opening
					//activateContactCapabilities(contact, info.getCapabilities(), false);
					setContactFreeTextAndAvailability(contact, freeText, PRESENCE_STATUS_OFFLINE, info.getCapabilities().getTimestamp());
					setCapabilityForContact(contact, MIMETYPE_REGISTRATION_STATE, false);
				}
			}
		}
	}
	
	/**
	 * Get contact capabilities
	 * <br>If contact has never been enriched with capability, returns null
	 * 
	 * @param contact
	 * @return capabilities
	 */
	public Capabilities getContactCapabilities(String contact){
		if (isRawContactRcs(getRcsRawContactIdFromContact(contact))){
			return getContactInfo(contact).getCapabilities();
		}else{
			return null;
		}
	}
	
	/**
	 * Set contact presence timestamp
	 * 
	 * @param contact
	 * @param timestamp
	 */
	private void setContactPresenceTimestamp(String contact, long timestamp){
		long rawContactId = getRcsRawContactIdFromContact(contact);
		
        long dataId = getDataIdForRawContact(rawContactId, MIMETYPE_PRESENCE_TIMESTAMP);
        
        if (dataId == INVALID_ID) {
       		// Invalid id, should never happen
        	if (logger.isActivated()){
        		logger.debug("Could not find data id for presence timestamp");
        	}
        }else{
    		ContentValues values = new ContentValues();
    		values.put(Data.RAW_CONTACT_ID, rawContactId); 
    		values.put(Data.MIMETYPE, MIMETYPE_PRESENCE_TIMESTAMP);
    		values.put(Data.DATA1, contact);
    		values.put(Data.DATA2, timestamp);
    		
    		ctx.getContentResolver().update(Data.CONTENT_URI, 
    				values,
    				Data._ID + "=?",
    				new String[] {String.valueOf(dataId)});
        }
	}
	
	/**
	 * Set contact capabilities timestamp
	 * 
	 * @param contact
	 * @param timestamp
	 */
	public void setContactCapabilitiesTimestamp(String contact, long timestamp){
		if (!contact.equalsIgnoreCase(MYSELF)){
			contact = PhoneUtils.extractNumberFromUri(contact);
			if (!isRCSValidNumber(contact)){
				if (logger.isActivated()){
					logger.debug(contact +" is not a RCS valid number");
				}
				return;
			}
		}
		
    	// Update capabilities in database, only if the number is in the address book
    	if (!isNumberInAddressBook(contact)) {
			if (logger.isActivated()){
				logger.debug(contact +" is not a number in the address book, we do not save its capabilities timestamp");
			}
    		return;
    	}
		
		long rawContactId = getRcsRawContactIdFromContact(contact);
		
		if (rawContactId == INVALID_ID){
			rawContactId = createRcsContact(null, contact, ContactInfo.NOT_RCS);
		}
		
        long dataId = getDataIdForRawContact(rawContactId, MIMETYPE_CAPABILITY_TIMESTAMP);
        
        if (dataId == INVALID_ID) {
       		// Invalid id, should never happen
        	if (logger.isActivated()){
        		logger.debug("Could not find data id for capability timestamp");
        	}
        }else{
    		ContentValues values = new ContentValues();
    		values.put(Data.RAW_CONTACT_ID, rawContactId); 
    		values.put(Data.MIMETYPE, MIMETYPE_CAPABILITY_TIMESTAMP);
    		values.put(Data.DATA1, contact);
    		values.put(Data.DATA2, timestamp);
    		
    		ctx.getContentResolver().update(Data.CONTENT_URI, 
    				values,
    				Data._ID + "=?",
    				new String[] {String.valueOf(dataId)});
        }
	}
	
    /**
     * Utility method to delete a RCS contact.
     *
     * @param phoneNumber The phone number as entered by the user / stored by
     *            Android.
     */
    public void removeContact(final String phoneNumber)throws ContactsManagerException{
        if (logger.isActivated()) {
            logger.debug("Remove RCS contact for " + phoneNumber);
        }

        // Check if there is still a contact with this number in the address book, as it may have been deleted by the user
        // If there is none, then we do nothing
        if (getRawContactIdFromPhoneNumber(phoneNumber)!=INVALID_ID){
        	// Set the RCS status
        	setContactRcsPresenceStatus(phoneNumber, ContactInfo.RCS_CAPABLE);

        	// Set the free text and availability
        	setContactFreeTextAndAvailability(phoneNumber, null, PRESENCE_STATUS_NOT_SET, 0L);

        	// Set the web link
        	setWeblinkUpdatedFlag(phoneNumber, false);
        	setContactWeblink(phoneNumber, null, true);

        	// Set the capabilities : they do not change

        	// Remove the photo-icon
        	removeContactPhotoIcon(phoneNumber);

        	// Set the timestamp
        	setContactPresenceTimestamp(phoneNumber, 0L);

        	// Set the contact type : it is now RCS capable
        	setContactType(phoneNumber, ContactInfo.RCS_CAPABLE);
        }
        
        // Remove entry from rich address book provider
		ctx.getContentResolver().delete(RichAddressBookData.CONTENT_URI, 
				RichAddressBookData.KEY_CONTACT_NUMBER +"=?",
				new String[]{phoneNumber});

    }

    /**
     * Utility method to create a new "RCS" contact, that aggregates
     * with other raw contact sources if found (by Android).
     *
     * @param displayName The displayed name of a contact. Should only be set
     *            when contact has no aggregation target, set to null as
     *            default!
     * @param phoneNumber The phone number as entered by the user / stored by
     *            Android.
     * @return the rawContactId of the newly created contact, may return
     *         INVALID_ID if contact could not be created
     */
    private long createRcsContact(final String displayName,
            String phoneNumber, 
            final String rcsStatus) {

        if (logger.isActivated()) {
        	logger.debug("displayName=" + displayName + " phoneNumber=" + phoneNumber);
        }
        
        // Make sure that no other RCS contact with phoneNumber exists
        long rawContactId = getRcsRawContactIdFromPhoneNumber(phoneNumber);
        if (rawContactId != INVALID_ID) {
        	if (logger.isActivated()){
        		logger.debug("RCS contact already exists");
        	}
        	// It may already exist because if we create it after a capability discovery or because we "IM blocked" it.
        	// In this case, we do not create another instance, just return the existing one
        	// If the contact is now active, we add the default photoIcon
        	if (rcsStatus.equalsIgnoreCase(PresenceInfo.RCS_ACTIVE)){
        		setContactPhoto(rawContactId, null, true);
        	}
        	
            // Intentionally silence
            return rawContactId;
        }
        
        // If phone number can't be loosely compared with itself then we don't
        // make the phone number RCS.
        if (!phoneNumbersEqual(phoneNumber, phoneNumber, false)) {
        	if (logger.isActivated()){
        		logger.debug("RCS contact could not be created loose comparison failed");
        	}
            return INVALID_ID;
        }

        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

        //Create rawcontact for RCS
        int rawContactRefIms = ops.size();
        ops.add(ContentProviderOperation.newInsert(RawContacts.CONTENT_URI)
        		 .withValue(RawContacts.AGGREGATION_MODE, RawContacts.AGGREGATION_MODE_SUSPENDED)
                 .withValue(RawContacts.ACCOUNT_TYPE, AuthenticationService.ACCOUNT_MANAGER_TYPE)
                 .withValue(RawContacts.ACCOUNT_NAME, ctx.getString(R.string.rcs_core_account_username))
                 .build());

        // Insert number
        ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                 .withValueBackReference(Data.RAW_CONTACT_ID, rawContactRefIms)
                 .withValue(Data.MIMETYPE, MIMETYPE_NUMBER)
                 .withValue(Data.DATA1, phoneNumber)
                 .build());
        
        // Insert a name
        ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                .withValueBackReference(Data.RAW_CONTACT_ID, rawContactRefIms)
                .withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
                .withValue(StructuredName.DISPLAY_NAME, phoneNumber)
                .build());

        // Create RCS status row
        ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                .withValueBackReference(Data.RAW_CONTACT_ID, rawContactRefIms)
                .withValue(Data.MIMETYPE, ContactsManager.MIMETYPE_RCS_STATUS)
                .withValue(Data.DATA1, phoneNumber)
                .withValue(Data.DATA2, rcsStatus)
                .build());

        // Create RCS status timestamp row
        ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                .withValueBackReference(Data.RAW_CONTACT_ID, rawContactRefIms)
                .withValue(Data.MIMETYPE, ContactsManager.MIMETYPE_RCS_STATUS_TIMESTAMP)
                .withValue(Data.DATA1, phoneNumber)
                .withValue(Data.DATA2, System.currentTimeMillis())
                .build());
        
        // Insert presence timestamp
        ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                .withValueBackReference(Data.RAW_CONTACT_ID, rawContactRefIms)
                .withValue(Data.MIMETYPE, MIMETYPE_PRESENCE_TIMESTAMP)
                .withValue(Data.DATA1, phoneNumber)
                .withValue(Data.DATA2, System.currentTimeMillis())
                .build());
        
        // Insert presence free text
        ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                .withValueBackReference(Data.RAW_CONTACT_ID, rawContactRefIms)
                .withValue(Data.MIMETYPE, MIMETYPE_FREE_TEXT)
                .withValue(Data.DATA1, phoneNumber)
                .withValue(Data.DATA2, "")
                .build());
        
        // Insert presence status
        ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                .withValueBackReference(Data.RAW_CONTACT_ID, rawContactRefIms)
                .withValue(Data.MIMETYPE, MIMETYPE_PRESENCE_STATUS)
                .withValue(Data.DATA1, phoneNumber)
                .withValue(Data.DATA2, PRESENCE_STATUS_NOT_SET)
                .build());

        // Insert capabilities timestamp
        ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                .withValueBackReference(Data.RAW_CONTACT_ID, rawContactRefIms)
                .withValue(Data.MIMETYPE, MIMETYPE_CAPABILITY_TIMESTAMP)
                .withValue(Data.DATA1, phoneNumber)
                .withValue(Data.DATA2, System.currentTimeMillis())
                .build());
        

        // Insert contact type, it is either RCS active, RCS capable or not RCS
        if (rcsStatus.equalsIgnoreCase(PresenceInfo.RCS_ACTIVE)){
            ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                    .withValueBackReference(Data.RAW_CONTACT_ID, rawContactRefIms)
                    .withValue(Data.MIMETYPE, MIMETYPE_RCS_CONTACT)
                    .withValue(Data.DATA1, phoneNumber)
                    .build());
        }else if (rcsStatus.equalsIgnoreCase(ContactInfo.NOT_RCS)){
            ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                    .withValueBackReference(Data.RAW_CONTACT_ID, rawContactRefIms)
                    .withValue(Data.MIMETYPE, MIMETYPE_NOT_RCS_CONTACT)
                    .withValue(Data.DATA1, phoneNumber)
                    .build());        	
        }else {
        	// In all other cases, contact is RCS capable
            ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                    .withValueBackReference(Data.RAW_CONTACT_ID, rawContactRefIms)
                    .withValue(Data.MIMETYPE, MIMETYPE_RCS_CAPABLE_CONTACT)
                    .withValue(Data.DATA1, phoneNumber)
                    .build());        	
        }
        
        // Insert default avatar, only if status is "active"
        // (we do not want a default RCS picture if we do not share our presence profile yet)
        if (rcsStatus.equalsIgnoreCase(PresenceInfo.RCS_ACTIVE)){
        	Bitmap rcsAvatar = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.rcs_core_default_portrait_icon);
        	byte[] iconData = convertBitmapToBytes(rcsAvatar);
        	ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
        			.withValueBackReference(Data.RAW_CONTACT_ID, rawContactRefIms)
        			.withValue(Data.MIMETYPE, ContactsManager.MIMETYPE_PHOTO)
        			.withValue(Photo.PHOTO, iconData)
        			.withValue(Data.IS_PRIMARY, 1)
        			.build());
        }
        
        long rcsRawContactId = INVALID_ID;
        try {
            ContentProviderResult[] results;
            results = ctx.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            rcsRawContactId =  ContentUris.parseId(results[rawContactRefIms].uri);
        } catch (RemoteException e) {
            return INVALID_ID;
        } catch (OperationApplicationException e) {
            return INVALID_ID;
        }
        
        // Check the rawContactId where we will explicitly do aggregations
        rawContactId = getRawContactIdFromPhoneNumber(phoneNumber);
        
        ops.clear();
        ops.add(ContentProviderOperation.newUpdate(ContactsContract.AggregationExceptions.CONTENT_URI)
                           .withValue(AggregationExceptions.TYPE, AggregationExceptions.TYPE_KEEP_TOGETHER)
                           .withValue(AggregationExceptions.RAW_CONTACT_ID1, rcsRawContactId)
                           .withValue(AggregationExceptions.RAW_CONTACT_ID2, rawContactId).build());

        try {
        	ctx.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
		} catch (RemoteException e) {
        	if (logger.isActivated()){
        		logger.debug("Remote exception => "+e);
        	}
			return rcsRawContactId;
		} catch (OperationApplicationException e) {
        	if (logger.isActivated()){
        		logger.debug("Operation exception => "+e);
        	}
			return rcsRawContactId;
		}

        return rcsRawContactId;
        
    }

    /**
     * Converts the specified bitmap to a byte array.
     *
     * @param bitmap the Bitmap to convert
     * @return the bitmap as bytes, null if converting fails.
     */
    private byte[] convertBitmapToBytes(final Bitmap bitmap) {
        byte[] iconData = null;
        int size = bitmap.getRowBytes() * bitmap.getHeight();

        ByteArrayOutputStream out = new ByteArrayOutputStream(size);
        try {
            if (bitmap.compress(Bitmap.CompressFormat.PNG, 0 /* quality ignored for PNG */, out)) {
                out.close();
                iconData = out.toByteArray();
            } else {
                out.close();
                if (logger.isActivated()){
                	logger.debug("Unable to convert bitmap, compression failed");
                }
            }
        } catch (IOException e) {
        	if (logger.isActivated()){
        		logger.error("Unable to convert bitmap", e);
        	}
            iconData = null;
        }

        return iconData;
    }
    
    /**
     * Utility method to create the "Me" raw contact.
     *
     * @param context The application context.
     * @return the rawContactId of the newly created contact
     */
    public long createMyContact() {

		if (!RcsSettings.getInstance().isSocialPresenceSupported()){
			return INVALID_ID;
		}
    	
        // Check if IMS account exists before continue
        AccountManager am = AccountManager.get(ctx);
        if (am.getAccountsByType(AuthenticationService.ACCOUNT_MANAGER_TYPE).length == 0) {
        	if (logger.isActivated()){
        		logger.error("Could not create \"Me\" contact, no RCS account found");
        	}
            throw new IllegalStateException("No RCS account found");
        }

        long imsRawContactId = 0L;
        
        // Check if RCS raw contact for "Me" does not already exist
        if (getRawContactIdForMe() != INVALID_ID) {
        	if (logger.isActivated()){
        		logger.error("\"Me\" contact already exists, no need to recreate");
        	}
        	imsRawContactId = getRawContactIdForMe();
        }else{
        	if (logger.isActivated()){
        		logger.error("\"Me\" contact does not already exists, creating it");
        	}
        	
            ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

            //Create rawcontact for RCS
            int rawContactRefIms = ops.size();
            ops.add(ContentProviderOperation.newInsert(RawContacts.CONTENT_URI)
                     .withValue(RawContacts.ACCOUNT_TYPE, AuthenticationService.ACCOUNT_MANAGER_TYPE)
                     .withValue(RawContacts.ACCOUNT_NAME, ctx.getString(R.string.rcs_core_account_username))
                     .withValue(RawContacts.SOURCE_ID, MYSELF)                     
                     .withValue(RawContacts.AGGREGATION_MODE, RawContacts.AGGREGATION_MODE_DISABLED)
                     .build());

            // Set name
            ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                    .withValueBackReference(Data.RAW_CONTACT_ID, rawContactRefIms)
                    .withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(StructuredName.DISPLAY_NAME, ctx.getString(R.string.rcs_core_my_profile))
                    .build());
            
            // Create RCS status row
            ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                    .withValueBackReference(Data.RAW_CONTACT_ID, rawContactRefIms)
                    .withValue(Data.MIMETYPE, ContactsManager.MIMETYPE_RCS_STATUS)
                    .withValue(Data.DATA1, MYSELF)
                    .withValue(Data.DATA2, ContactInfo.RCS_CAPABLE)
                    .build());
            
            // Create RCS status timestamp row
            ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                    .withValueBackReference(Data.RAW_CONTACT_ID, rawContactRefIms)
                    .withValue(Data.MIMETYPE, ContactsManager.MIMETYPE_RCS_STATUS_TIMESTAMP)
                    .withValue(Data.DATA1, MYSELF)
                    .withValue(Data.DATA2, System.currentTimeMillis())
                    .build());
            
            // Create my profile shortcut
            ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                    .withValueBackReference(Data.RAW_CONTACT_ID, rawContactRefIms)
                    .withValue(Data.MIMETYPE, ContactsManager.MIMETYPE_SEE_MY_PROFILE)
                    .withValue(Data.DATA1, MYSELF)
                    .build());
            
            // Insert presence timestamp
            ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                    .withValueBackReference(Data.RAW_CONTACT_ID, rawContactRefIms)
                    .withValue(Data.MIMETYPE, MIMETYPE_PRESENCE_TIMESTAMP)
                    .withValue(Data.DATA1, MYSELF)
                    .withValue(Data.DATA2, System.currentTimeMillis())
                    .build());
            
            // Insert presence free text
            ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                    .withValueBackReference(Data.RAW_CONTACT_ID, rawContactRefIms)
                    .withValue(Data.MIMETYPE, MIMETYPE_FREE_TEXT)
                    .withValue(Data.DATA1, MYSELF)
                    .withValue(Data.DATA2, "")
                    .build());
            
            // Insert presence status
            ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                    .withValueBackReference(Data.RAW_CONTACT_ID, rawContactRefIms)
                    .withValue(Data.MIMETYPE, MIMETYPE_PRESENCE_STATUS)
                    .withValue(Data.DATA1, MYSELF)
                    .withValue(Data.DATA2, PRESENCE_STATUS_NOT_SET)
                    .build());
            
            // Insert capabilities timestamp
            ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                    .withValueBackReference(Data.RAW_CONTACT_ID, rawContactRefIms)
                    .withValue(Data.MIMETYPE, MIMETYPE_CAPABILITY_TIMESTAMP)
                    .withValue(Data.DATA1, MYSELF)
                    .withValue(Data.DATA2, System.currentTimeMillis())
                    .build());
            
            // Insert default capabilities, these are read from the settings provider
            
            // Cs_video
            if (RcsSettings.getInstance().isCsVideoSupported()){
	            ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
	                    .withValueBackReference(Data.RAW_CONTACT_ID, rawContactRefIms)
	                    .withValue(Data.MIMETYPE, MIMETYPE_MY_CAPABILITY_CS_VIDEO)
	                    .withValue(Data.DATA1, MYSELF)
	                    .withValue(Data.DATA2, 0L)
	                    .build());
            }
            // File transfer
            if (RcsSettings.getInstance().isFileTransferSupported()){
	            ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
	                    .withValueBackReference(Data.RAW_CONTACT_ID, rawContactRefIms)
	                    .withValue(Data.MIMETYPE, MIMETYPE_MY_CAPABILITY_FILE_TRANSFER)
	                    .withValue(Data.DATA1, MYSELF)
	                    .withValue(Data.DATA2, 0L)
	                    .build());
            }

            // IM session
            if (RcsSettings.getInstance().isImSessionSupported()){
	            ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
	                    .withValueBackReference(Data.RAW_CONTACT_ID, rawContactRefIms)
	                    .withValue(Data.MIMETYPE, MIMETYPE_MY_CAPABILITY_IM_SESSION)
	                    .withValue(Data.DATA1, MYSELF)
	                    .withValue(Data.DATA2, 0L)
	                    .build());
            }

            // Image sharing
            if (RcsSettings.getInstance().isImageSharingSupported()){
	            ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
	                    .withValueBackReference(Data.RAW_CONTACT_ID, rawContactRefIms)
	                    .withValue(Data.MIMETYPE, MIMETYPE_MY_CAPABILITY_IMAGE_SHARING)
	                    .withValue(Data.DATA1, MYSELF)
	                    .withValue(Data.DATA2, 0L)
	                    .build());
            }
            
            // Video sharing
            if (RcsSettings.getInstance().isVideoSharingSupported()){
	            ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
	                    .withValueBackReference(Data.RAW_CONTACT_ID, rawContactRefIms)
	                    .withValue(Data.MIMETYPE, MIMETYPE_MY_CAPABILITY_VIDEO_SHARING)
	                    .withValue(Data.DATA1, MYSELF)
	                    .withValue(Data.DATA2, 0L)
	                    .build());
            }

            // Presence discovery
            if (RcsSettings.getInstance().isPresenceDiscoverySupported()){
            	ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
            			.withValueBackReference(Data.RAW_CONTACT_ID, rawContactRefIms)
            			.withValue(Data.MIMETYPE, MIMETYPE_MY_CAPABILITY_PRESENCE_DISCOVERY)
            			.withValue(Data.DATA1, MYSELF)
            			.withValue(Data.DATA2, 0L)
            			.build());
            }
            // Social presence
            if (RcsSettings.getInstance().isSocialPresenceSupported()){
            	ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
            			.withValueBackReference(Data.RAW_CONTACT_ID, rawContactRefIms)
            			.withValue(Data.MIMETYPE, MIMETYPE_MY_CAPABILITY_SOCIAL_PRESENCE)
            			.withValue(Data.DATA1, MYSELF)
            			.withValue(Data.DATA2, 0L)
            			.build());
            }
            
            // RCS extensions
            String extensions = RcsSettings.getInstance().getSupportedRcsExtensions();
            if (extensions!=null && extensions.length()>0){
            	ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
            			.withValueBackReference(Data.RAW_CONTACT_ID, rawContactRefIms)
            			.withValue(Data.MIMETYPE, MIMETYPE_MY_CAPABILITY_EXTENSIONS)
            			.withValue(Data.DATA1, MYSELF)
            			.withValue(Data.DATA2, extensions)
            			.build());
            }
            
            // Insert default avatar
            Bitmap rcsAvatar = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.rcs_core_default_portrait_icon);
            byte[] iconData = convertBitmapToBytes(rcsAvatar);
            ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                    .withValueBackReference(Data.RAW_CONTACT_ID, rawContactRefIms)
                    .withValue(Data.MIMETYPE, ContactsManager.MIMETYPE_PHOTO)
                    .withValue(Photo.PHOTO, iconData)
                    .withValue(Data.IS_PRIMARY, 1)
                    .build());

            try {
                ContentProviderResult[] results;
                results = ctx.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
                imsRawContactId = ContentUris.parseId(results[rawContactRefIms].uri);
            } catch (RemoteException e) {
            	imsRawContactId = INVALID_ID;
            } catch (OperationApplicationException e) {
            	imsRawContactId =  INVALID_ID;
            }

            // Set default free text to null and availability to online
            setContactFreeTextAndAvailability(MYSELF, null, PRESENCE_STATUS_ONLINE, 0L);
        }

        return imsRawContactId;
    }

    /**
     * Utility to find the RCS rawContactId for a specific phone number.
     *
     * @param phoneNumber the phoneNumber to search for
     * @return contactId, if not found INVALID_ID is returned
     */
    private long getRcsRawContactIdFromPhoneNumber(String phoneNumber) {
        String[] projection = { Data.RAW_CONTACT_ID };
        String selection = Data.MIMETYPE + "=? AND PHONE_NUMBERS_EQUAL(" + Phone.NUMBER + ", ?)";
        String[] selectionArgs = { MIMETYPE_NUMBER, phoneNumber };
        String sortOrder = Data.RAW_CONTACT_ID;

        Cursor cur = ctx.getContentResolver().query(Data.CONTENT_URI, 
        		projection, 
        		selection, 
        		selectionArgs,
                sortOrder);
        if (cur != null) {
            while (cur.moveToNext()) {
            	long rcsRawContactId = cur.getLong(cur.getColumnIndex(Data.RAW_CONTACT_ID));
            	cur.close();
            	return rcsRawContactId;
            }
            cur.close();
        }

        return INVALID_ID;
    }

    /**
     * Utility to find the rawContactId for a specific phone number that is not RCS.
     *
     * @param phoneNumber the phoneNumber to search for
     * @return contactId, if not found INVALID_ID is returned
     */
    private long getRawContactIdFromPhoneNumber(String phoneNumber) {
        String[] projection = { Data.RAW_CONTACT_ID };
        String selection = Data.MIMETYPE + "=? AND PHONE_NUMBERS_EQUAL(" + Phone.NUMBER + ", ?)";
        String[] selectionArgs = { Phone.CONTENT_ITEM_TYPE, phoneNumber };
        String sortOrder = Data.RAW_CONTACT_ID;

        // Starting LOOSE equal
        Cursor cur = ctx.getContentResolver().query(Data.CONTENT_URI, 
        		projection, 
        		selection, 
        		selectionArgs,
                sortOrder);
        if (cur != null) {
            while (cur.moveToNext()) {
                long rawContactId = cur.getLong(cur.getColumnIndex(Data.RAW_CONTACT_ID));
                if (!isRawContactRcs(rawContactId)) {
                    cur.close();
                    return rawContactId;
                }
            }
            cur.close();
        }

        /* No match found using LOOSE equals, starting STRICT equals.
         *
         * This is done because of that the PHONE_NUMBERS_EQUAL function in Android
         * dosent always return true when doing loose lookup of a phone number
         * against itself
         */
        String selectionStrict = Data.MIMETYPE + "=? AND (NOT PHONE_NUMBERS_EQUAL(" + Phone.NUMBER
                + ", ?) AND PHONE_NUMBERS_EQUAL(" + Phone.NUMBER + ", ?, 1))";
        String[] selectionArgsStrict = { Phone.CONTENT_ITEM_TYPE, phoneNumber, phoneNumber };
        cur = ctx.getContentResolver().query(Data.CONTENT_URI, 
        		projection, 
        		selectionStrict, 
        		selectionArgsStrict,
                sortOrder);
        if (cur != null) {
            while (cur.moveToNext()) {
                long rawContactId = cur.getLong(cur.getColumnIndex(Data.RAW_CONTACT_ID));
                if (!isRawContactRcs(rawContactId)) {
                    cur.close();
                    return rawContactId;
                }
            }
            cur.close();
        }
        
        return INVALID_ID;
    }
    
    /**
     * Utility to check if a rawContact is associated to a RCS account
     *
     * @param rawContactId the id of the rawContact to check
     * @return true if contact is associated to a RCS raw contact, else false
     */
    public boolean isRawContactRcsAssociated(final long rawContactId) {
    	
    	// We look at the aggregation exception table to see which contacts are aggregated to this one
    	// Raw contact ID 2 is always the raw contact corresponding to the phone entry
    	// Raw contact ID 1 contains the RCS raw contact
    	String[] rawProjection = { AggregationExceptions.RAW_CONTACT_ID1, AggregationExceptions.RAW_CONTACT_ID2};
        String rawSelection = AggregationExceptions.RAW_CONTACT_ID2 + "=?"+ " OR " +AggregationExceptions.RAW_CONTACT_ID1 + "=?";
        String[] rawSelectionArgs = { Long.toString(rawContactId), Long.toString(rawContactId) };

        Cursor rawCur = ctx.getContentResolver().query(ContactsContract.AggregationExceptions.CONTENT_URI, 
        		rawProjection, 
        		rawSelection,
                rawSelectionArgs, 
                null);
        if (rawCur != null){ 
        	while (rawCur.moveToNext()){
        		long rawContactToBeChecked = rawCur.getLong(1);
        		if (isRawContactRcs(rawContactToBeChecked)){
                    rawCur.close();
        			return true;
        		}
        	}        	
            rawCur.close();
        }
        
        // The raw contact is not associated to a RCS account
        return false;
    }
    
    /**
     * Utility to check if a rawContact is owned by RCS account
     *
     * @param rawContactId the id of the rawContact to check
     * @return true if contact is RCS, else false
     */
    private boolean isRawContactRcs(final long rawContactId) {
        String[] rawProjection = { RawContacts._ID };
        String rawSelection = RawContacts.ACCOUNT_TYPE + "=? AND " 
        		+ RawContacts._ID + "=?";
        String[] rawSelectionArgs = {
        		AuthenticationService.ACCOUNT_MANAGER_TYPE,
                Long.toString(rawContactId)
        };
        Cursor rawCur = ctx.getContentResolver().query(RawContacts.CONTENT_URI, 
        		rawProjection, 
        		rawSelection,
                rawSelectionArgs, 
                null);
        if (rawCur != null){ 
        	if (rawCur.getCount() > 0) {
        		rawCur.close();
                return true;
        	}
            rawCur.close();
        }
        return false;
    }
    
    /**
     * Utility to get access to Android's PHONE_NUMBERS_EQUAL SQL function.
     *
     * @note Impl and comments can be found in
     *       /external/sqlite/android/PhoneNumberUtils.cpp
     *       (phone_number_compare_inter)
     *
     * @param phone1 the first phone number
     * @param phone2 the second phone number
     * @param useStrictComparison set to false if loose comparison should be
     *            used (normal), true if strict comparison should be used
     * @return true when equal
     */
    private boolean phoneNumbersEqual(final String phone1, final String phone2, final boolean useStrictComparison) {
        boolean result = false;
        // Create a temporary db in memory to get access to the SQL engine
        SQLiteDatabase db = SQLiteDatabase.create(null);
        if (db == null) {
            throw new IllegalStateException("Could not retrieve db");
        }
        // CSOFF: InlineConditionals
        String test = "SELECT CASE WHEN PHONE_NUMBERS_EQUAL(" + phone1 + "," + phone2 + ","
                + Integer.toString((useStrictComparison) ? 1 : 0) + ") " + "THEN 1 ELSE 0 END";
        // CSON: InlineConditionals
        Cursor cur = db.rawQuery(test, null);
        if (cur != null){
        	if (cur.moveToNext()) {
	            if (cur.getString(0).equals("1")) {
	                result = true;
	            } else {
	                result = false;
	            }
        	}
            cur.close();
        }
        db.close();
        return result;
    }

    /**
     * Get the data id associated to a given mimeType for a contact.
     *
     * @param rawContactId the RCS rawcontact
     * @param mimeType The searched mimetype 
     * @return The id of the data
     */
    private long getDataIdForRawContact(final long rawContactId, final String mimeType) {

    	long dataId = INVALID_ID;
        String[] projection = {Data._ID, Data.RAW_CONTACT_ID, Data.MIMETYPE };

        String selection = Data.RAW_CONTACT_ID + "=? AND " + Data.MIMETYPE + "=?";
        String[] selectionArgs = { Long.toString(rawContactId), mimeType };
        Cursor cur = ctx.getContentResolver().query(Data.CONTENT_URI, 
        		projection, 
        		selection, 
        		selectionArgs, 
        		null);
        if (cur == null) {
        	return INVALID_ID;
        }
        if (cur.moveToNext()) {
        	dataId = cur.getLong(0);
        }
        cur.close();
        
        return dataId;
    }

    /**
     * Utility to set RCS status of a RCS contact.
     *
     * @param contact the RCS contact
     * @param status RCS status of the contact
     */
    private void setContactRcsPresenceStatus(final String contact, final String status) {

    	// Update data in provider
    	addOrModifyRcsContactInProvider(contact, status);
    	
    	long rawContactId = getRcsRawContactIdFromContact(contact);
    	
        long dataId = getDataIdForRawContact(rawContactId, MIMETYPE_RCS_STATUS);
        if (dataId == INVALID_ID) {
        	if (logger.isActivated()){
        		logger.debug("Not updating RCS status in ContactsContract, row not existing yet");
        	}
            return;
        }

        ContentValues values = new ContentValues();
        values.put(Data.MIMETYPE, MIMETYPE_RCS_STATUS);
        values.put(Data.DATA2, status);
        
        ctx.getContentResolver().update(Data.CONTENT_URI, 
        		values, 
        		Data._ID + "=?",
        		new String[] {String.valueOf(dataId)});
    }
    
    /**
     * Get the raw contact id for the given contact
     * 
     * @param contact The contact
     * @return raw contact id associated to this contact, INVALID_ID if none was found
     */
    private long getRcsRawContactIdFromContact(final String contact){
    	long rawContactId = INVALID_ID;
    	if (contact.equalsIgnoreCase(MYSELF)){
    		rawContactId = getRawContactIdForMe();
    	}else{
    		rawContactId = getRcsRawContactIdFromPhoneNumber(contact);
    	}
    	return rawContactId;
    }
    
    /**
     * Utility to set the freetext and availability attributes on a RCS contact.
     *
     * @param contact Contact
     * @param freeText the new freetext, null if freeText should be removed
     * @param availability the new availability
     * @param timestamp the timestamp when the freeText was changed, if null the current time will be used
     */
    private void setContactFreeTextAndAvailability(final String contact, final String freeText, final int availability, final long timestamp) {
		if (logger.isActivated()) {
			logger.info("Set free text for contact " + contact + " to "+freeText+" and availability to "+availability);
		}
    	
    	long rawContactId = getRcsRawContactIdFromContact(contact);
    	
        String[] projection = {Data._ID, Data.RAW_CONTACT_ID, Data.MIMETYPE };

        long dataId = INVALID_ID;
        String selection = Data.RAW_CONTACT_ID + "=?";
        String[] selectionArgs = { Long.toString(rawContactId)};
        Cursor cur = ctx.getContentResolver().query(Data.CONTENT_URI, 
        		projection, 
        		selection, 
        		selectionArgs, 
        		null);
        if (!cur.moveToNext()) {
        	dataId = INVALID_ID;
        }else{
        	dataId = cur.getLong(0);
        }
        cur.close();
        
        ContentValues values = new ContentValues();
        values.put(StatusUpdates.DATA_ID, dataId);
        values.put(StatusUpdates.STATUS, freeText);
        values.put(StatusUpdates.STATUS_LABEL, R.string.rcs_core_account_id);
        values.put(StatusUpdates.STATUS_RES_PACKAGE, ctx.getPackageName());
        values.put(StatusUpdates.STATUS_ICON, R.drawable.rcs_icon);
       	values.put(StatusUpdates.PRESENCE, availability);	
        // Needed for inserting PRESENCE
        values.put(StatusUpdates.PROTOCOL, Im.PROTOCOL_CUSTOM);
        values.put(StatusUpdates.CUSTOM_PROTOCOL, " " /* Intentional left blank */);

        long ts;
        long systemTs = System.currentTimeMillis();
        Date date = new Date(timestamp);
        if (timestamp > 0L) {
            ts = date.getTime();
            if (ts > systemTs) {
                //Do not allow times in the future, threat them as now
                ts = systemTs;
            }
        } else {
            ts = systemTs;
        }
        values.put(StatusUpdates.STATUS_TIMESTAMP, ts);

        ctx.getContentResolver().insert(StatusUpdates.CONTENT_URI, values);
    }

    /**
     * Utility to set the weblink attribute on a RCS contact.
     *
     * @param contact the RCS contact
     * @param webLink the new webLink, null if webLink should be removed
     * @param makeSuperPrimary true if the weblink should be set to super primary.
     */
    private void setContactWeblink(final String contact, final String webLink, final boolean makeSuperPrimary) {

		if (logger.isActivated()) {
			logger.info("Set weblink for contact " + contact + " to "+webLink);
		}
    	
    	long rawContactId = getRcsRawContactIdFromContact(contact);
    	
        String[] projection = { Data._ID };
        String selection = Data.RAW_CONTACT_ID + "=? AND " + Data.MIMETYPE + "=?";
        String[] selectionArgs = { Long.toString(rawContactId), MIMETYPE_WEBLINK };
        String sortOrder = Data._ID + " DESC";

        Cursor cur = ctx.getContentResolver().query(Data.CONTENT_URI, 
        		projection, 
        		selection, 
        		selectionArgs,
                sortOrder);
        
        if (cur == null) {
            return;
        }

        try {
            long dataId = INVALID_ID;
            if (TextUtils.isEmpty(webLink)) {
                if (cur.moveToNext()) {
                    dataId = cur.getLong(cur.getColumnIndex(Data._ID));
                    ctx.getContentResolver().delete(Data.CONTENT_URI, Data._ID + "=" + dataId, null);
                }
            } else {
                ContentValues values = new ContentValues();
                values.put(Data.RAW_CONTACT_ID, rawContactId);
                values.put(Data.MIMETYPE, MIMETYPE_WEBLINK);
                values.put(Website.URL, webLink);
                values.put(Website.TYPE, Website.TYPE_HOMEPAGE);
                values.put(Data.IS_PRIMARY, 1);
                if (makeSuperPrimary) {
                    values.put(Data.IS_SUPER_PRIMARY, 1);
                }
                if (cur.moveToNext()) {
                    dataId = cur.getLong(cur.getColumnIndex(Data._ID));
                    ctx.getContentResolver().update(Data.CONTENT_URI, values, Data._ID + "=" + dataId, null);
                } else {
                	ctx.getContentResolver().insert(Data.CONTENT_URI, values);
                }

            }
        } finally {
            cur.close();
        }
    }
    
    /**
     * Utility to get the weblink attribute on a RCS contact.
     *
     * @param contact the RCS contact
     * @return contact weblink
     */
    private String getContactWeblink(final String contact) {

    	long rawContactId = getRcsRawContactIdFromContact(contact);
    	
        String[] projection = { Data._ID, Website.URL};
        String selection = Data.RAW_CONTACT_ID + "=? AND " + Data.MIMETYPE + "=?";
        String[] selectionArgs = { Long.toString(rawContactId), MIMETYPE_WEBLINK };
        String sortOrder = Data._ID + " DESC";

        Cursor cur = ctx.getContentResolver().query(Data.CONTENT_URI, 
        		projection, 
        		selection, 
        		selectionArgs,
                sortOrder);
        
        if (cur == null) {
            return null;
        }

        try {
        	if (cur.moveToNext()) {
        		return cur.getString(1);
            }
        } finally {
            cur.close();
        }
        
        return null;
    }

    /**
     * Utility to set the free text attribute on a RCS contact.
     *
     * @param contact the RCS contact
     * @param freeText the new free text, null if free text should be removed
     */
    private void setContactFreeText(final String contact, final String freeText) {

		if (logger.isActivated()) {
			logger.info("Set free text for contact " + contact + " to "+freeText);
		}
    	
    	long rawContactId = getRcsRawContactIdFromContact(contact);
    	
        String[] projection = { Data._ID };
        String selection = Data.RAW_CONTACT_ID + "=? AND " + Data.MIMETYPE + "=?";
        String[] selectionArgs = { Long.toString(rawContactId), MIMETYPE_FREE_TEXT };
        String sortOrder = Data._ID + " DESC";

        Cursor cur = ctx.getContentResolver().query(Data.CONTENT_URI, 
        		projection, 
        		selection, 
        		selectionArgs,
                sortOrder);
        
        if (cur == null) {
            return;
        }

        try {
            long dataId = INVALID_ID;
            if (TextUtils.isEmpty(freeText)) {
                if (cur.moveToNext()) {
                    dataId = cur.getLong(cur.getColumnIndex(Data._ID));
                    ctx.getContentResolver().delete(Data.CONTENT_URI, Data._ID + "=" + dataId, null);
                }
            } else {
                ContentValues values = new ContentValues();
                values.put(Data.RAW_CONTACT_ID, rawContactId);
                values.put(Data.MIMETYPE, MIMETYPE_FREE_TEXT);
                values.put(Data.DATA2, freeText);
                if (cur.moveToNext()) {
                    dataId = cur.getLong(cur.getColumnIndex(Data._ID));
                    ctx.getContentResolver().update(Data.CONTENT_URI, values, Data._ID + "=" + dataId, null);
                } else {
                	ctx.getContentResolver().insert(Data.CONTENT_URI, values);
                }

            }
        } finally {
            cur.close();
        }
    }
    
    /**
     * Utility to set the presence status attribute on a RCS contact.
     *
     * @param contact the RCS contact
     * @param presenceStatus the new presence status
     */
    private void setContactPresenceStatus(final String contact, final int presenceStatus) {

		if (logger.isActivated()) {
			logger.info("Set presence status for contact " + contact + " to "+presenceStatus);
		}
    	
    	long rawContactId = getRcsRawContactIdFromContact(contact);
    	
        String[] projection = { Data._ID };
        String selection = Data.RAW_CONTACT_ID + "=? AND " + Data.MIMETYPE + "=?";
        String[] selectionArgs = { Long.toString(rawContactId), MIMETYPE_FREE_TEXT };
        String sortOrder = Data._ID + " DESC";

        Cursor cur = ctx.getContentResolver().query(Data.CONTENT_URI, 
        		projection, 
        		selection, 
        		selectionArgs,
                sortOrder);
        
        if (cur == null) {
            return;
        }

        try {
            long dataId = INVALID_ID;
                ContentValues values = new ContentValues();
                values.put(Data.RAW_CONTACT_ID, rawContactId);
                values.put(Data.MIMETYPE, MIMETYPE_FREE_TEXT);
                values.put(Data.DATA2, presenceStatus);
                if (cur.moveToNext()) {
                    dataId = cur.getLong(cur.getColumnIndex(Data._ID));
                    ctx.getContentResolver().update(Data.CONTENT_URI, values, Data._ID + "=" + dataId, null);
                }           
        } finally {
            cur.close();
        }
    }
    
    /**
     * Utility to set the icon attribute on a RCS contact.
     *
     * @param rawContactId the RCS rawcontact
     * @param photoIcon The photoIcon
     * @param makeSuperPrimary whether or not to set the super primary flag
     */
    private void setContactPhoto(long rawContactId, PhotoIcon photoIcon, boolean makeSuperPrimary) {

        String[] projection = { Data._ID };
        String selection = Data.RAW_CONTACT_ID + "=? AND " + Data.MIMETYPE + "=?";
        String[] selectionArgs = { Long.toString(rawContactId), Photo.CONTENT_ITEM_TYPE };
        String sortOrder = Data._ID + " DESC";

        Cursor cur = ctx.getContentResolver().query(Data.CONTENT_URI, 
        		projection, 
        		selection,
                selectionArgs, 
                sortOrder);
        if (cur == null) {
            return;
        }
        
        byte[] iconData = null;
        if (photoIcon!=null){
        	iconData = photoIcon.getContent();	
        }         

        // Insert default avatar if icon is null
        if (iconData == null
                && rawContactId != getRawContactIdForMe()) {
        	Bitmap rcsAvatar = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.rcs_core_default_portrait_icon);
            iconData = convertBitmapToBytes(rcsAvatar);
        }

        try {
            long dataId = INVALID_ID;
            if (iconData == null) {
                // Only for myself
                if (cur.moveToNext()) {
                    dataId = cur.getLong(cur.getColumnIndex(Data._ID));
                    ctx.getContentResolver().delete(Data.CONTENT_URI, 
                    		Data._ID + "=" + dataId,
                            null);
                }
            } else {
                ContentValues values = new ContentValues();
                values.put(Data.RAW_CONTACT_ID, rawContactId);
                values.put(Data.MIMETYPE, MIMETYPE_PHOTO);
                values.put(Photo.PHOTO, iconData);
                values.put(Data.IS_PRIMARY, 1);
                if (makeSuperPrimary) {
                    values.put(Data.IS_SUPER_PRIMARY, 1);
                }
                if (cur.moveToNext()) {
                    dataId = cur.getLong(cur.getColumnIndex(Data._ID));
                    ctx.getContentResolver().update(Data.CONTENT_URI, values,
                            Data._ID + "=" + dataId, null);
                } else {
                    ctx.getContentResolver().insert(Data.CONTENT_URI, values);
                }
                
                values.clear();
                
                // Set etag
                values.put(Data.RAW_CONTACT_ID, rawContactId);
                values.put(Data.MIMETYPE, MIMETYPE_PHOTO_ETAG);
                String etag = null;
                if (photoIcon!=null){
                	etag = photoIcon.getEtag();
                }
                values.put(Data.DATA2, etag);
                
                String[] projection2 = { Data._ID, Data.RAW_CONTACT_ID, Data.MIMETYPE };
                String selection2 = Data.RAW_CONTACT_ID + "=? AND " + Data.MIMETYPE + "=?";
                String[] selectionArgs2 = { Long.toString(rawContactId), MIMETYPE_PHOTO_ETAG };

                Cursor cur2 = ctx.getContentResolver().query(Data.CONTENT_URI, 
                		projection2, 
                		selection2,
                        selectionArgs2, 
                        null);
                 if (cur2.moveToNext()){
                	 dataId = cur2.getLong(0);
                     ctx.getContentResolver().update(Data.CONTENT_URI, values,
                     		Data._ID + "=?" , 
                     		new String[]{Long.toString(dataId)});
                 }else{
                     ctx.getContentResolver().insert(Data.CONTENT_URI, values);
                 }              
                 cur2.close();
            }
        } finally {
            cur.close();
        }
    }

    /**
     * Utility to get the etag of a contact icon.
     *
     * @param contact
     * @return the icon etag 
     */
    public String getContactPhotoEtag(String contact) {
        String etag = null;
		
        contact = PhoneUtils.extractNumberFromUri(contact);
		
        long rawContactId = getRcsRawContactIdFromContact(contact);

        String[] projection = { Data.DATA2 };
        
        String selection = Data.RAW_CONTACT_ID + "=? AND " + Data.MIMETYPE + "=?";
        String[] selectionArgs = { Long.toString(rawContactId), MIMETYPE_PHOTO_ETAG };

        Cursor cur = ctx.getContentResolver().query(Data.CONTENT_URI, 
        		projection, 
        		selection,
                selectionArgs, 
                null);
         if (cur.moveToNext()){
        	 etag = cur.getString(0);
        }
        cur.close();
        
        return etag;
    }
    
    /**
     * Get the raw contact id of the "Me" contact.
     *
     * @return rawContactId, if not found INVALID_ID is returned
     */
    private long getRawContactIdForMe() {
        String[] projection = {
                RawContacts.ACCOUNT_TYPE, 
                RawContacts._ID,
                RawContacts.SOURCE_ID
        };
        String selection = RawContacts.ACCOUNT_TYPE + "=? AND " 
        		+ RawContacts.SOURCE_ID + "=?";
        String[] selectionArgs = {
        		AuthenticationService.ACCOUNT_MANAGER_TYPE,
                MYSELF
        };

        Cursor cur = ctx.getContentResolver().query(RawContacts.CONTENT_URI, 
        		projection, 
        		selection, 
        		selectionArgs,
                null);
        if (cur == null) {
            return INVALID_ID;
        }
        if (!cur.moveToNext()) {
            cur.close();
            return INVALID_ID;
        }

        long rawContactId = cur.getLong(1);
        cur.close();
        return rawContactId;
    }
    
    /**
     * Mark the contact as "blocked for IM"
     * 
     * @param contact
     * @param flag indicating if we enable or disable the IM sessions with the contact
     */
    public void setImBlockedForContact(String contact, boolean flag){
		contact = PhoneUtils.extractNumberFromUri(contact);
		setCapabilityForContact(contact, MIMETYPE_IM_BLOCKED, flag);
    }
    
    /**
     * Get whether the "IM" feature is enabled or not for the contact
     * 
     * @param contact
     * @return flag indicating if IM sessions with the contact are enabled or not
     */
    public boolean isImBlockedForContact(String contact){
		contact = PhoneUtils.extractNumberFromUri(contact);
    	return getCapabilityForContact(contact, MIMETYPE_IM_BLOCKED);
    }
    
	/**
	 * Get the contacts that are "IM blocked"
	 * 
	 * @return list containing all contacts that are "IM blocked" 
	 */
	public List<String> getIMBlockedContacts(){
		List<String> IMBlockedNumbers = new ArrayList<String>();
        String[] projection = {Data.DATA1, Data.MIMETYPE};

        String selection = Data.MIMETYPE + "=?";
        String[] selectionArgs = { MIMETYPE_IM_BLOCKED };
        Cursor c = ctx.getContentResolver().query(Data.CONTENT_URI, 
        		projection, 
        		selection, 
        		selectionArgs, 
        		null);
		
		while (c.moveToNext()) {
			IMBlockedNumbers.add(c.getString(0));
		}
		c.close();
		return IMBlockedNumbers;
	}
    
    /**
     * Utility to create a ContactInfo object from a cursor containing data
     * 
     * @param cursor
     * @param rawContactId
     * @return contactInfo
     */
    private ContactInfo getContactInfoFromCursor(Cursor cursor, long rawContactId){
    	ContactInfo contactInfo = new ContactInfo();
    	PresenceInfo presenceInfo = new PresenceInfo();
    	Capabilities capabilities = new Capabilities();
    	byte[] photoContent = null;
    	String photoEtag = null;
    	
    	while(cursor.moveToNext()){
    		String mimeType = cursor.getString(cursor.getColumnIndex(Data.MIMETYPE));
    		if (mimeType.equalsIgnoreCase(MIMETYPE_WEBLINK)){
    			// Set weblink
    			int columnIndex = cursor.getColumnIndex(Website.URL);
    			if (columnIndex!=-1){
    				presenceInfo.setFavoriteLinkUrl(cursor.getString(columnIndex));
    			}
    		}else if (mimeType.equalsIgnoreCase(MIMETYPE_PHOTO)){
    			// Set photo
    			int columnIndex = cursor.getColumnIndex(Photo.PHOTO);
    			if (columnIndex!=-1){
    				photoContent = cursor.getBlob(columnIndex);
    			}
    		}else if (mimeType.equalsIgnoreCase(MIMETYPE_PHOTO_ETAG)){
    			// Set photo etag
    			int columnIndex = cursor.getColumnIndex(Data.DATA2);
    			if (columnIndex!=-1){
    				photoEtag = cursor.getString(columnIndex);
    			}    			
    		}else if (mimeType.equalsIgnoreCase(MIMETYPE_PRESENCE_TIMESTAMP)){
    			// Set presence timestamp
    			int columnIndex = cursor.getColumnIndex(Data.DATA2);
    			if (columnIndex!=-1){
    				presenceInfo.setTimestamp(cursor.getLong(columnIndex));
    			}    			
    		}else if (mimeType.equalsIgnoreCase(MIMETYPE_CAPABILITY_TIMESTAMP)){
    			// Set capability timestamp
    			int columnIndex = cursor.getColumnIndex(Data.DATA2);
    			if (columnIndex!=-1){
    				capabilities.setTimestamp(cursor.getLong(columnIndex));
    			}    			
    		}else if (mimeType.equalsIgnoreCase(MIMETYPE_CAPABILITY_CS_VIDEO)
    					||mimeType.equalsIgnoreCase(MIMETYPE_MY_CAPABILITY_CS_VIDEO)){
    			// Set capability cs_video
   				capabilities.setCsVideoSupport(true);
    		}else if (mimeType.equalsIgnoreCase(MIMETYPE_CAPABILITY_IMAGE_SHARING)
					||mimeType.equalsIgnoreCase(MIMETYPE_MY_CAPABILITY_IMAGE_SHARING)){
    			// Set capability image sharing
   				capabilities.setImageSharingSupport(true);
    		}else if (mimeType.equalsIgnoreCase(MIMETYPE_CAPABILITY_VIDEO_SHARING)
					||mimeType.equalsIgnoreCase(MIMETYPE_MY_CAPABILITY_VIDEO_SHARING)){
    			// Set capability video sharing
   				capabilities.setVideoSharingSupport(true);
    		}else if (mimeType.equalsIgnoreCase(MIMETYPE_CAPABILITY_IM_SESSION)
					||mimeType.equalsIgnoreCase(MIMETYPE_MY_CAPABILITY_IM_SESSION)){
    			// Set capability IM session
   				capabilities.setImSessionSupport(true);
    		}else if (mimeType.equalsIgnoreCase(MIMETYPE_CAPABILITY_FILE_TRANSFER)
					||mimeType.equalsIgnoreCase(MIMETYPE_MY_CAPABILITY_FILE_TRANSFER)){
    			// Set capability file transfer
   				capabilities.setFileTransferSupport(true);
    		}else if (mimeType.equalsIgnoreCase(MIMETYPE_CAPABILITY_PRESENCE_DISCOVERY)
				||mimeType.equalsIgnoreCase(MIMETYPE_MY_CAPABILITY_PRESENCE_DISCOVERY)){
    			// Set capability presence discovery
				capabilities.setPresenceDiscoverySupport(true);
    		}else if (mimeType.equalsIgnoreCase(MIMETYPE_CAPABILITY_SOCIAL_PRESENCE)
    			||mimeType.equalsIgnoreCase(MIMETYPE_MY_CAPABILITY_SOCIAL_PRESENCE)){
    			// Set capability social presence
				capabilities.setSocialPresenceSupport(true);
    		}else if (mimeType.equalsIgnoreCase(MIMETYPE_CAPABILITY_EXTENSIONS)
    			|| mimeType.equalsIgnoreCase(MIMETYPE_MY_CAPABILITY_EXTENSIONS)){
    			// Set RCS extensions capability
    			int columnIndex = cursor.getColumnIndex(Data.DATA2);
    			if (columnIndex!=-1){
    				String extensions = cursor.getString(columnIndex);
    				String[] extensionList = extensions.split(";");
    				for (int i=0;i<extensionList.length;i++){
    					capabilities.addSupportedExtension(extensionList[i]);
    				}
    			}
    		}else if (mimeType.equalsIgnoreCase(MIMETYPE_FREE_TEXT)){
    			// Set free text
    			int columnIndex = cursor.getColumnIndex(Data.DATA2);
    			if (columnIndex!=-1){
    				presenceInfo.setFreetext(cursor.getString(columnIndex));
    			}
    		}else if (mimeType.equalsIgnoreCase(MIMETYPE_PRESENCE_STATUS)){
    			// Set presence status
    			int columnIndex = cursor.getColumnIndex(Data.DATA2);
    			if (columnIndex!=-1){
    				int presence = cursor.getInt(columnIndex);
    				if (presence == PRESENCE_STATUS_ONLINE){
    					presenceInfo.setPresenceStatus(PresenceInfo.ONLINE);
    				}else if (presence == PRESENCE_STATUS_OFFLINE){
    					presenceInfo.setPresenceStatus(PresenceInfo.OFFLINE);
    				}else{
    					presenceInfo.setPresenceStatus(PresenceInfo.UNKNOWN);
    				}
    			}
    		}else if (mimeType.equalsIgnoreCase(MIMETYPE_REGISTRATION_STATE)){
    			contactInfo.setRegistrationState(true);
    		}else if (mimeType.equalsIgnoreCase(MIMETYPE_RCS_STATUS)){
    			// Set RCS status
    			int columnIndex = cursor.getColumnIndex(Data.DATA2);
    			if (columnIndex!=-1){
    				contactInfo.setRcsStatus(cursor.getString(columnIndex));
    			}
    		}else if (mimeType.equalsIgnoreCase(MIMETYPE_RCS_STATUS_TIMESTAMP)){
    			// Set RCS status timestamp
    			int columnIndex = cursor.getColumnIndex(Data.DATA2);
    			if (columnIndex!=-1){
    				contactInfo.setRcsStatusTimestamp(cursor.getLong(columnIndex));
    			}
    		}else if (mimeType.equalsIgnoreCase(MIMETYPE_NUMBER)){
    			// Set contact
    			int columnIndex = cursor.getColumnIndex(Data.DATA1);
    			if (columnIndex!=-1){
    				contactInfo.setContact(cursor.getString(columnIndex));
    			}
    		}
    	}
    	cursor.close();
    	
    	PhotoIcon photoIcon = null;
    	if (photoContent!=null){
    		Bitmap bmp = BitmapFactory.decodeByteArray(photoContent, 0, photoContent.length);
			if (bmp != null) {
				photoIcon = new PhotoIcon(photoContent, bmp.getWidth(), bmp.getHeight(), photoEtag);
			}
    	}
    	presenceInfo.setPhotoIcon(photoIcon);
		contactInfo.setPresenceInfo(presenceInfo);
    	contactInfo.setCapabilities(capabilities);
		
    	return contactInfo;
    }
    
    /**
     * Utility to extract data from a raw contact.
     *
     * @param rawContactId the rawContactId
     * @return A cursor containing the requested data.
     */
    private Cursor getRawContactDataCursor(final long rawContactId) {
        String[] projection = {
                Data._ID, 
                Data.MIMETYPE, 
                Data.DATA1, 
                Data.DATA2, 
                Website.URL,
                Photo.PHOTO          
        };

        // Filter the mime types 
        String selection = "(" + Data.RAW_CONTACT_ID + " =?) AND (" 
                + Data.MIMETYPE + "=? OR " 
                + Data.MIMETYPE + "=? OR "
                + Data.MIMETYPE + "=? OR "
                + Data.MIMETYPE + "=? OR "
                + Data.MIMETYPE + "=? OR "
                + Data.MIMETYPE + "=? OR "
                + Data.MIMETYPE + "=? OR "
                + Data.MIMETYPE + "=? OR "
                + Data.MIMETYPE + "=? OR "
                + Data.MIMETYPE + "=? OR "
                + Data.MIMETYPE + "=? OR "
                + Data.MIMETYPE + "=? OR "
                + Data.MIMETYPE + "=? OR "
                + Data.MIMETYPE + "=? OR "
                + Data.MIMETYPE + "=? OR "
                + Data.MIMETYPE + "=? OR "
                + Data.MIMETYPE + "=? OR "
                + Data.MIMETYPE + "=? OR "
                + Data.MIMETYPE + "=? OR "
                + Data.MIMETYPE + "=? OR "
                + Data.MIMETYPE + "=? OR "
                + Data.MIMETYPE + "=? OR "
                + Data.MIMETYPE + "=? OR "
                + Data.MIMETYPE + "=? OR "
                + Data.MIMETYPE + "=? OR "
                + Data.MIMETYPE + "=? OR "
                + Data.MIMETYPE + "=?)";
        String[] selectionArgs = {
                Long.toString(rawContactId), 
                MIMETYPE_WEBLINK,
                MIMETYPE_PHOTO,
                MIMETYPE_PHOTO_ETAG,
                MIMETYPE_RCS_STATUS,
                MIMETYPE_RCS_STATUS_TIMESTAMP,
                MIMETYPE_REGISTRATION_STATE,
                MIMETYPE_PRESENCE_STATUS,
                MIMETYPE_PRESENCE_TIMESTAMP,
                MIMETYPE_FREE_TEXT,
                MIMETYPE_NUMBER,
                MIMETYPE_CAPABILITY_TIMESTAMP,
                MIMETYPE_CAPABILITY_CS_VIDEO,
                MIMETYPE_CAPABILITY_IMAGE_SHARING,
                MIMETYPE_CAPABILITY_VIDEO_SHARING,
                MIMETYPE_CAPABILITY_IM_SESSION,
                MIMETYPE_CAPABILITY_FILE_TRANSFER,
                MIMETYPE_CAPABILITY_PRESENCE_DISCOVERY,
                MIMETYPE_CAPABILITY_SOCIAL_PRESENCE,
                MIMETYPE_CAPABILITY_EXTENSIONS,
                MIMETYPE_MY_CAPABILITY_CS_VIDEO,
                MIMETYPE_MY_CAPABILITY_IMAGE_SHARING,
                MIMETYPE_MY_CAPABILITY_VIDEO_SHARING,
                MIMETYPE_MY_CAPABILITY_IM_SESSION,
                MIMETYPE_MY_CAPABILITY_FILE_TRANSFER,
                MIMETYPE_MY_CAPABILITY_PRESENCE_DISCOVERY,
                MIMETYPE_MY_CAPABILITY_SOCIAL_PRESENCE,
                MIMETYPE_MY_CAPABILITY_EXTENSIONS
        };

        Cursor cur = ctx.getContentResolver().query(Data.CONTENT_URI, 
        		projection, 
        		selection, 
        		selectionArgs, 
        		null);

        return cur;
    }
    
    /**
     * Update UI strings when device's locale has changed
     */
    public void updateStrings(){
    	// Update My profile display name
    	ContentValues values = new ContentValues();
    	values.put(StructuredName.DISPLAY_NAME, ctx.getString(R.string.rcs_core_my_profile));
    	ctx.getContentResolver().update(Data.CONTENT_URI, 
    			values, 
    			"(" + Data.RAW_CONTACT_ID + " =?) AND (" + Data.MIMETYPE + "=?)", 
    			new String[]{Long.toString(getRawContactIdForMe()), StructuredName.DISPLAY_NAME});
    	
    	
    	// Update file transfer menu
    	values.clear();
    	
    	values.put(Data.DATA2, getMimeTypeDescription(MIMETYPE_CAPABILITY_FILE_TRANSFER));
    	ctx.getContentResolver().update(Data.CONTENT_URI, 
    			values, 
    			Data.MIMETYPE + "=?", 
    			new String[]{MIMETYPE_CAPABILITY_FILE_TRANSFER});
    	
    	// Update chat menu 
    	values.clear();
    	values.put(Data.DATA2, getMimeTypeDescription(MIMETYPE_CAPABILITY_IM_SESSION));
    	ctx.getContentResolver().update(Data.CONTENT_URI, 
    			values, 
    			Data.MIMETYPE + "=?", 
    			new String[]{MIMETYPE_CAPABILITY_IM_SESSION});


    	// Update image sharing menu 
    	values.clear();
    	values.put(Data.DATA2, getMimeTypeDescription(MIMETYPE_CAPABILITY_IMAGE_SHARING));
    	ctx.getContentResolver().update(Data.CONTENT_URI, 
    			values, 
    			Data.MIMETYPE + "=?", 
    			new String[]{MIMETYPE_CAPABILITY_IMAGE_SHARING});

    	// Update video sharing menu 
    	values.clear();
    	values.put(Data.DATA2, getMimeTypeDescription(MIMETYPE_CAPABILITY_VIDEO_SHARING));
    	ctx.getContentResolver().update(Data.CONTENT_URI, 
    			values, 
    			Data.MIMETYPE + "=?", 
    			new String[]{MIMETYPE_CAPABILITY_VIDEO_SHARING});

    	// Update CS video menu 
    	values.clear();
    	values.put(Data.DATA2, getMimeTypeDescription(MIMETYPE_CAPABILITY_CS_VIDEO));
    	ctx.getContentResolver().update(Data.CONTENT_URI, 
    			values, 
    			Data.MIMETYPE + "=?", 
    			new String[]{MIMETYPE_CAPABILITY_CS_VIDEO});
    	
    	// Update event log menu 
    	values.clear();
    	values.put(Data.DATA2, getMimeTypeDescription(MIMETYPE_EVENT_LOG));
    	ctx.getContentResolver().update(Data.CONTENT_URI, 
    			values, 
    			Data.MIMETYPE + "=?", 
    			new String[]{MIMETYPE_EVENT_LOG});
    	
    }
    
    /**
     * Clean the RCS entries
     * 
     * <br>This removes the RCS entries that are associated to numbers not present in the address book anymore
     */
    public void cleanRCSEntries(){
    	String[] projection = {
                Data.RAW_CONTACT_ID, 
                Data.DATA1          
        };

        // Filter the mime types 
        String selection = Data.MIMETYPE + "=?";
        String[] selectionArgs = {
                MIMETYPE_NUMBER
        };
    	
    	// Get all RCS raw contacts id
    	Cursor cursor = ctx.getContentResolver().query(Data.CONTENT_URI, 
    			projection, 
    			selection, 
    			selectionArgs, 
    			null);
    	while (cursor.moveToNext()){
    		long rawContactId = cursor.getLong(0);
    		String phoneNumber = cursor.getString(1);
    		if (getRawContactIdFromPhoneNumber(phoneNumber)==INVALID_ID){
    			// This number is not in the address book anymore, delete the RCS entry
    			int count = ctx.getContentResolver().delete(RawContacts.CONTENT_URI, 
    					RawContacts._ID + "=?", 
    					new String[]{Long.toString(rawContactId)});
    			if (count>0){
    				if (logger.isActivated()){
    					logger.debug("We removed an old RCS entry for number "+phoneNumber);
    				}
    			}
    		}
    	}
    	cursor.close();   	
    }
    
    /**
     * Is the number in the regular address book
     * 
     * @param number to be tested
     * @return boolean
     */
    public boolean isNumberInAddressBook(String number){
    	return (getRawContactIdFromPhoneNumber(number)!=INVALID_ID);
    }
    
}
