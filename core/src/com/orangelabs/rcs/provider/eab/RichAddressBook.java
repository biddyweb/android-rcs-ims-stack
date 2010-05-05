package com.orangelabs.rcs.provider.eab;

import java.io.OutputStream;
import java.util.Calendar;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.Contacts;

import com.orangelabs.rcs.core.ims.service.capability.Capabilities;
import com.orangelabs.rcs.core.ims.service.presence.FavoriteLink;
import com.orangelabs.rcs.core.ims.service.presence.Geoloc;
import com.orangelabs.rcs.core.ims.service.presence.PhotoIcon;
import com.orangelabs.rcs.core.ims.service.presence.PresenceInfo;
import com.orangelabs.rcs.utils.PhoneUtils;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Rich address book content provider
 * 
 * @author jexa7410
 */
public class RichAddressBook {
	/**
	 * Current instance
	 */
	private static RichAddressBook instance = null;

	/**
	 * Content resolver
	 */
	private ContentResolver cr;

	/**
	 * Database URI
	 */
	private Uri databaseUri;

	/**
	 * My row id
	 */
	private int myRowId;

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
			instance = new RichAddressBook(ctx);
		}
	}

	/**
	 * Returns instance
	 * 
	 * @return Instance
	 */
	public static RichAddressBook getInstance() {
		return instance;
	}

	/**
     * Constructor
     * 
     * @param ctx Application context
     */
	private RichAddressBook(Context ctx) {
		databaseUri = RichAddressBookData.CONTENT_URI;
		cr = ctx.getContentResolver();
		myRowId = getMyRowId();
	}

	/**
	 * Return my row id in the EAB
	 * 
	 * @return Row id or -1 in case of error
	 */
	private int getMyRowId() {
		int rowId = -1;
		try {
			String where = RichAddressBookData.KEY_CONTACT_ID + " = " +  RichAddressBookData.END_USER_ROW_CONTACT_ID;
			Cursor c = cr.query(databaseUri, null, where, null, null);
			if (c.moveToFirst()) {
				rowId = c.getInt(RichAddressBookData.COLUMN_KEY_ID);
			}
			c.close();
		} catch (Exception e) {
			if (logger.isActivated()) {
				logger.error("Internal exception", e);
			}
		}
		return rowId;
	}

	/**
	 * Return the row id of a contact in the EAB
	 * 
	 * @param contactId Contact id
	 * @return Row id
	 */
	private int getContactRowId(int contactId) {
		int rowId = -1;
		try {
			String where = RichAddressBookData.KEY_CONTACT_ID + " = " +  contactId;
			Cursor c = cr.query(databaseUri, null, where, null, null);
			if (c.moveToFirst()) {
				rowId = c.getInt(RichAddressBookData.COLUMN_KEY_ID);
			}
			c.close();
		} catch (Exception e) {
			if (logger.isActivated()) {
				logger.error("Internal exception", e);
			}
		}
		return rowId;
	}
	
	/**
	 * Returns the contact id associated to a contact URI in the EAB
	 * 
	 * @param uri Contact URI
	 * @return Id or -1 if the contact does not exist or in case of error
	 */
	public int getContactId(String number) {
		int id = -1;
		try {
			number = PhoneUtils.extractNumberFromUri(number);
	    	Cursor c = cr.query(Contacts.Phones.CONTENT_URI, new String[]{Contacts.Phones.PERSON_ID, Contacts.Phones.NUMBER}, null, null, null);
	    	while(c.moveToNext()) {
	    		String databaseNumber = PhoneUtils.extractNumberFromUri(c.getString(1));
	    		if (databaseNumber.equals(number)) {
	    			id = c.getInt(0);
	    			break;
	    		}
	       	}
	       	c.close();
		} catch (Exception e) {
			if (logger.isActivated()) {
				logger.error("Internal exception", e);
			}
		}
        return id;
	}

	/**
	 * Returns the photo URI of a contact in the EAB
	 * 
	 * @param id Contact id
	 * @return URI
	 */
	private Uri getContactPhotoUri(int id) {
		return ContentUris.withAppendedId(RichAddressBookData.CONTENT_URI, id);
	}

	/**
	 * Returns the photo URI of a contact in the EAB
	 * 
	 * @param id Contact id
	 * @return URI
	 */
	private Uri getMyPhotoUri() {
		return ContentUris.withAppendedId(RichAddressBookData.CONTENT_URI, myRowId);
	}

	/**
	 * Set my presence info in the EAB
	 * 
	 * @param info Presence info
	 * @throws RichAddressBookException
	 */
	public void setMyPresenceInfo(PresenceInfo info) throws RichAddressBookException {
		if (logger.isActivated()) {
			logger.info("Set my presence info");
		}

		try {
			ContentValues values = new ContentValues();

			// Set the contact id 
			values.put(RichAddressBookData.KEY_CONTACT_ID, RichAddressBookData.END_USER_ROW_CONTACT_ID);
			
			// Set the timestamp
			values.put(RichAddressBookData.KEY_TIMESTAMP, Calendar.getInstance().getTimeInMillis());
			
			// Set the hyper-availability status
			if (info.isHyperavailable()) {
				values.put(RichAddressBookData.KEY_HYPER_AVAILABILITY_FLAG, RichAddressBookData.TRUE_VALUE);
			} else {
				values.put(RichAddressBookData.KEY_HYPER_AVAILABILITY_FLAG, RichAddressBookData.FALSE_VALUE);
			}
			
			// Set the freetext
			values.put(RichAddressBookData.KEY_FREE_TEXT, info.getFreetext());
			
			// Set the favorite link
			FavoriteLink favoriteLink = info.getFavoriteLink();
			String linkUrl = null;
			String linkName = null;
			if (favoriteLink != null) {
				linkUrl = favoriteLink.getLink();
				linkName = favoriteLink.getName();
			}
			values.put(RichAddressBookData.KEY_FAVORITE_LINK_URL, linkUrl);
			values.put(RichAddressBookData.KEY_FAVORITE_LINK_NAME, linkName);

			// Set the geoloc
			Geoloc geoloc = info.getGeoloc();
			if (geoloc != null) {
				values.put(RichAddressBookData.KEY_GEOLOC_EXIST_FLAG, RichAddressBookData.TRUE_VALUE);
				values.put(RichAddressBookData.KEY_GEOLOC_LATITUDE, geoloc.getLatitude());
				values.put(RichAddressBookData.KEY_GEOLOC_LONGITUDE, geoloc.getLongitude());
				values.put(RichAddressBookData.KEY_GEOLOC_ALTITUDE, geoloc.getAltitude());
			} else {
				values.put(RichAddressBookData.KEY_GEOLOC_EXIST_FLAG,	RichAddressBookData.FALSE_VALUE);
				values.put(RichAddressBookData.KEY_GEOLOC_LATITUDE, 0);
				values.put(RichAddressBookData.KEY_GEOLOC_LONGITUDE, 0);
				values.put(RichAddressBookData.KEY_GEOLOC_ALTITUDE, 0);
			}

			// Set the capabilities
			Capabilities capabilities = info.getCapabilities();
			if (capabilities != null) {
				if (capabilities.isCsVideoSupported()) {
					values.put(RichAddressBookData.KEY_CS_VIDEO_SUPPORTED, RichAddressBookData.TRUE_VALUE);
				} else {
					values.put(RichAddressBookData.KEY_CS_VIDEO_SUPPORTED, RichAddressBookData.FALSE_VALUE);
				}
				if (capabilities.isImageSharingSupported()) {
					values.put(RichAddressBookData.KEY_IMAGE_SHARING_SUPPORTED, RichAddressBookData.TRUE_VALUE);
				} else {
					values.put(RichAddressBookData.KEY_IMAGE_SHARING_SUPPORTED, RichAddressBookData.FALSE_VALUE);
				}
				if (capabilities.isVideoSharingSupported()) {
					values.put(RichAddressBookData.KEY_VIDEO_SHARING_SUPPORTED, RichAddressBookData.TRUE_VALUE);
				} else {
					values.put(RichAddressBookData.KEY_VIDEO_SHARING_SUPPORTED, RichAddressBookData.FALSE_VALUE);
				}
				if (capabilities.isImSessionSupported()) {
					values.put(RichAddressBookData.KEY_IM_SESSION_SUPPORTED, RichAddressBookData.TRUE_VALUE);
				} else {
					values.put(RichAddressBookData.KEY_IM_SESSION_SUPPORTED, RichAddressBookData.FALSE_VALUE);
				}
				if (capabilities.isFileTransferSupported()) {
					values.put(RichAddressBookData.KEY_FILE_TRANSFER_SUPPORTED, RichAddressBookData.TRUE_VALUE);
				} else {
					values.put(RichAddressBookData.KEY_FILE_TRANSFER_SUPPORTED, RichAddressBookData.FALSE_VALUE);
				}
			} else {
				values.put(RichAddressBookData.KEY_CS_VIDEO_SUPPORTED, RichAddressBookData.FALSE_VALUE);
				values.put(RichAddressBookData.KEY_IMAGE_SHARING_SUPPORTED, RichAddressBookData.FALSE_VALUE);
				values.put(RichAddressBookData.KEY_VIDEO_SHARING_SUPPORTED, RichAddressBookData.FALSE_VALUE);
				values.put(RichAddressBookData.KEY_IM_SESSION_SUPPORTED, RichAddressBookData.FALSE_VALUE);
				values.put(RichAddressBookData.KEY_FILE_TRANSFER_SUPPORTED, RichAddressBookData.FALSE_VALUE);
			}
			
			// Update the database
			String where = RichAddressBookData.KEY_CONTACT_ID + " = " +  RichAddressBookData.END_USER_ROW_CONTACT_ID;
			cr.update(databaseUri, values, where, null);
			if (logger.isActivated()) {
				logger.debug("My presence info has been updated");
			}
		} catch (Exception e) {
			if (logger.isActivated()) {
				logger.error("Internal exception", e);
			}
			throw new RichAddressBookException(e.getMessage());
		}
		
		// Set the photo-icon
		PhotoIcon photo = info.getPhotoIcon();
		if (photo != null) {
			setMyPhotoIcon(photo);
		} else {
			removeMyPhotoIcon();
		}
	}

	/**
	 * Set my photo-icon in the EAB
	 * 
	 * @param photo Photo
	 * @throws RichAddressBookException
	 */
	public void setMyPhotoIcon(PhotoIcon photo)	throws RichAddressBookException {
		if (logger.isActivated()) {
			logger.info("Set my photo-icon");
		}

		try {
			ContentValues values = new ContentValues();
			
			// Set the contact id 
			values.put(RichAddressBookData.KEY_CONTACT_ID, RichAddressBookData.END_USER_ROW_CONTACT_ID);

			// Set the timestamp
			values.put(RichAddressBookData.KEY_TIMESTAMP, Calendar.getInstance().getTimeInMillis());
			
			// Set the exist flag
			values.put(RichAddressBookData.KEY_PHOTO_EXIST_FLAG, RichAddressBookData.TRUE_VALUE);
			
			// Set the etag
			values.put(RichAddressBookData.KEY_PHOTO_ETAG, photo.getEtag());

			// Update the database
			String where = RichAddressBookData.KEY_CONTACT_ID + " = " +  RichAddressBookData.END_USER_ROW_CONTACT_ID;
			cr.update(databaseUri, values, where, null);

			// Save the photo content
			Uri photoUri = getMyPhotoUri();
			OutputStream outstream = cr.openOutputStream(photoUri);
			outstream.write(photo.getContent());
			outstream.flush();
			outstream.close();
			if (logger.isActivated()) {
				logger.debug("My photo icon has been updated");
			}
		} catch (Exception e) {
			if (logger.isActivated()) {
				logger.error("Internal exception", e);
			}
			throw new RichAddressBookException(e.getMessage());
		}
	}

	/**
	 * Remove my photo-icon in the EAB
	 * 
	 * @throws RichAddressBookException
	 */
	public void removeMyPhotoIcon() throws RichAddressBookException {
		if (logger.isActivated()) {
			logger.info("Remove my photo-icon");
		}

		try {
			ContentValues values = new ContentValues();

			// Set the contact id 
			values.put(RichAddressBookData.KEY_CONTACT_ID, RichAddressBookData.END_USER_ROW_CONTACT_ID);

			// Set the timestamp
			values.put(RichAddressBookData.KEY_TIMESTAMP, Calendar.getInstance().getTimeInMillis());
			
			// Set the exist flag
			values.put(RichAddressBookData.KEY_PHOTO_EXIST_FLAG, RichAddressBookData.FALSE_VALUE);

			// Set the etag
			values.put(RichAddressBookData.KEY_PHOTO_ETAG, "");
			
			// Update the database
			String where = RichAddressBookData.KEY_CONTACT_ID + " = " +  RichAddressBookData.END_USER_ROW_CONTACT_ID;
			cr.update(databaseUri, values, where, null);
			if (logger.isActivated()) {
				logger.debug("My photo icon has been removed");
			}
		} catch (Exception e) {
			if (logger.isActivated()) {
				logger.error("Internal exception", e);
			}
			throw new RichAddressBookException(e.getMessage());
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

		PresenceInfo presenceInfo = null;
		try {
			// Query the database
			String where = RichAddressBookData.KEY_CONTACT_ID + " = " +  RichAddressBookData.END_USER_ROW_CONTACT_ID;
			Cursor c = cr.query(databaseUri, null, where, null, null);
			if (c.moveToFirst()) {
				presenceInfo = new PresenceInfo();
				
				// Get the timestamp
				presenceInfo.setTimestamp(c.getLong(RichAddressBookData.COLUMN_TIMESTAMP));
				
				// Get the favorite link 
				FavoriteLink favoriteLink = null;
				String url = c.getString(RichAddressBookData.COLUMN_FAVORITE_LINK_URL);
				String name = c.getString(RichAddressBookData.COLUMN_FAVORITE_LINK_NAME);
				if ((url != null) && (url.trim().length() > 0)) {
					favoriteLink = new FavoriteLink(url);
					if ((name != null) && (name.trim().length() > 0)) {
						favoriteLink.setName(name);
					}
				}
				presenceInfo.setFavoriteLink(favoriteLink);
				
				// Get the freetext
				presenceInfo.setFreetext(c.getString(RichAddressBookData.COLUMN_FREE_TEXT));
				
				// Get the hyper-availability status
				presenceInfo.setHyperavailabilityStatus(Boolean.parseBoolean(c.getString(RichAddressBookData.COLUMN_HYPER_AVAILABILITY_FLAG)));
				
				// Get the geoloc
				Geoloc geoloc = null;
				boolean isGeolocExist = Boolean.parseBoolean(c.getString(RichAddressBookData.COLUMN_GEOLOC_EXIST_FLAG));
				if (isGeolocExist) {
					double latitude = c.getDouble(RichAddressBookData.COLUMN_GEOLOC_LATITUDE);
					double longitude = c.getDouble(RichAddressBookData.COLUMN_GEOLOC_LONGITUDE);
					double altitude = c.getDouble(RichAddressBookData.COLUMN_GEOLOC_ALTITUDE);
					geoloc = new Geoloc(latitude, longitude, altitude);
				}
				presenceInfo.setGeoloc(geoloc);
				
				// Get the capabilities
				Capabilities capabilities = new Capabilities(); 
				capabilities.setCsVideoSupport(Boolean.parseBoolean(c.getString(RichAddressBookData.COLUMN_CS_VIDEO_SUPPORTED)));
				capabilities.setFileTransferSupport(Boolean.parseBoolean(c.getString(RichAddressBookData.COLUMN_FILE_TRANSFER_SUPPORTED)));
				capabilities.setImageSharingSupport(Boolean.parseBoolean(c.getString(RichAddressBookData.COLUMN_IMAGE_SHARING_SUPPORTED)));
				capabilities.setImSessionSupport(Boolean.parseBoolean(c.getString(RichAddressBookData.COLUMN_IM_SESSION_SUPPORTED)));
				capabilities.setVideoSharingSupport(Boolean.parseBoolean(c.getString(RichAddressBookData.COLUMN_VIDEO_SHARING_SUPPORTED)));
				presenceInfo.setCapabilities(capabilities);
				
				// Get the photo-icon
				PhotoIcon photoIcon = null;
				boolean isPhotoExist = Boolean.parseBoolean(c.getString(RichAddressBookData.COLUMN_PHOTO_EXIST_FLAG));
				if (isPhotoExist) {
					Uri photoUri = getMyPhotoUri();
					String etag = c.getString(RichAddressBookData.COLUMN_PHOTO_ETAG);
					android.graphics.Bitmap bmp = BitmapFactory.decodeStream(cr.openInputStream(photoUri));
					photoIcon = new PhotoIcon(bmp, etag);
				}
				presenceInfo.setPhotoIcon(photoIcon);
				if (logger.isActivated()) {
					logger.debug("My presence info has been read");
				}				
			}
			c.close();
		} catch (Exception e) {
			if (logger.isActivated()) {
				logger.error("Internal exception", e);
			}
		}
		return presenceInfo;
	}

	/**
	 * Set the presence info of a contact
	 * 
	 * @param contact Contact
	 * @param info Presence info
	 * @throws ContactNotFoundException
	 * @throws RichAddressBookException
	 */
	public void setContactPresenceInfo(String contact, PresenceInfo info) throws RichAddressBookException {
		if (logger.isActivated()) {
			logger.info("Set presence info for contact " + contact);
		}

		try {
			// Get the contact id
			int contactId = getContactId(contact);
			if (contactId != -1) {
				ContentValues values = new ContentValues();
				
				// Set the contact id 
				values.put(RichAddressBookData.KEY_CONTACT_ID, contactId);

				// Set the timestamp 
				values.put(RichAddressBookData.KEY_TIMESTAMP, info.getTimestamp());
				
				// Set the hyper-availability
				if (info.isHyperavailable()) {
					values.put(RichAddressBookData.KEY_HYPER_AVAILABILITY_FLAG, RichAddressBookData.TRUE_VALUE);
				} else {
					values.put(RichAddressBookData.KEY_HYPER_AVAILABILITY_FLAG, RichAddressBookData.FALSE_VALUE);
				}
				
				// Set the freetext
				values.put(RichAddressBookData.KEY_FREE_TEXT, info.getFreetext());
				
				// Set the favorite link
				FavoriteLink favoriteLink = info.getFavoriteLink();
				String linkUrl = null;
				String linkName = null;
				if (favoriteLink != null) {
					linkUrl = favoriteLink.getLink();
					linkName = favoriteLink.getName();
				}
				values.put(RichAddressBookData.KEY_FAVORITE_LINK_URL, linkUrl);
				values.put(RichAddressBookData.KEY_FAVORITE_LINK_NAME, linkName);

				// Set the geoloc
				Geoloc geoloc = info.getGeoloc();
				if (geoloc != null) {
					values.put(RichAddressBookData.KEY_GEOLOC_EXIST_FLAG, RichAddressBookData.TRUE_VALUE);
					values.put(RichAddressBookData.KEY_GEOLOC_LATITUDE, geoloc.getLatitude());
					values.put(RichAddressBookData.KEY_GEOLOC_LONGITUDE, geoloc.getLongitude());
					values.put(RichAddressBookData.KEY_GEOLOC_ALTITUDE, geoloc.getAltitude());
				} else {
					values.put(RichAddressBookData.KEY_GEOLOC_EXIST_FLAG,	RichAddressBookData.FALSE_VALUE);
					values.put(RichAddressBookData.KEY_GEOLOC_LATITUDE, 0);
					values.put(RichAddressBookData.KEY_GEOLOC_LONGITUDE, 0);
					values.put(RichAddressBookData.KEY_GEOLOC_ALTITUDE, 0);
				}
				
				// Set the capabilities
				Capabilities capabilities = info.getCapabilities();
				if (capabilities != null) {
					if (capabilities.isCsVideoSupported()) {
						values.put(RichAddressBookData.KEY_CS_VIDEO_SUPPORTED, RichAddressBookData.TRUE_VALUE);
					} else {
						values.put(RichAddressBookData.KEY_CS_VIDEO_SUPPORTED, RichAddressBookData.FALSE_VALUE);
					}
					if (capabilities.isImageSharingSupported()) {
						values.put(RichAddressBookData.KEY_IMAGE_SHARING_SUPPORTED, RichAddressBookData.TRUE_VALUE);
					} else {
						values.put(RichAddressBookData.KEY_IMAGE_SHARING_SUPPORTED, RichAddressBookData.FALSE_VALUE);
					}
					if (capabilities.isVideoSharingSupported()) {
						values.put(RichAddressBookData.KEY_VIDEO_SHARING_SUPPORTED, RichAddressBookData.TRUE_VALUE);
					} else {
						values.put(RichAddressBookData.KEY_VIDEO_SHARING_SUPPORTED, RichAddressBookData.FALSE_VALUE);
					}
					if (capabilities.isImSessionSupported()) {
						values.put(RichAddressBookData.KEY_IM_SESSION_SUPPORTED, RichAddressBookData.TRUE_VALUE);
					} else {
						values.put(RichAddressBookData.KEY_IM_SESSION_SUPPORTED, RichAddressBookData.FALSE_VALUE);
					}
					if (capabilities.isFileTransferSupported()) {
						values.put(RichAddressBookData.KEY_FILE_TRANSFER_SUPPORTED, RichAddressBookData.TRUE_VALUE);
					} else {
						values.put(RichAddressBookData.KEY_FILE_TRANSFER_SUPPORTED, RichAddressBookData.FALSE_VALUE);
					}
				} else {
					values.put(RichAddressBookData.KEY_CS_VIDEO_SUPPORTED, RichAddressBookData.FALSE_VALUE);
					values.put(RichAddressBookData.KEY_IMAGE_SHARING_SUPPORTED, RichAddressBookData.FALSE_VALUE);
					values.put(RichAddressBookData.KEY_VIDEO_SHARING_SUPPORTED, RichAddressBookData.FALSE_VALUE);
					values.put(RichAddressBookData.KEY_IM_SESSION_SUPPORTED, RichAddressBookData.FALSE_VALUE);
					values.put(RichAddressBookData.KEY_FILE_TRANSFER_SUPPORTED, RichAddressBookData.FALSE_VALUE);
				}
				
				// Update the database
				String where = RichAddressBookData.KEY_CONTACT_ID + " = " + contactId;
				cr.update(databaseUri, values, where, null);				
				if (logger.isActivated()) {
					logger.debug("Presence info has been updated for " + contact);
				}
			} else {
				if (logger.isActivated()) {
					logger.error("Contact " + contact + " not found");
				}
				throw new ContactNotFoundException();
			}
		} catch (Exception e) {
			if (logger.isActivated()) {
				logger.error("Internal exception", e);
			}
			throw new RichAddressBookException(e.getMessage());
		}
	}

	/**
	 * Set the photo-icon of a contact in the EAB
	 * 
	 * @param contact Contact
	 * @param photoData Photo data
	 * @param etag Photo etag
	 * @throws ContactNotFoundException
	 * @throws RichAddressBookException
	 */
	public void setContactPhotoIcon(String contact, byte photoData[], String etag) throws RichAddressBookException {
		if (logger.isActivated()) {
			logger.info("Set photo-icon for contact " + contact);
		}

		try {
			// Get the contact id
			int contactId = getContactId(contact);
			if (contactId != -1) {
				ContentValues values = new ContentValues();
				
				// Set the contact id 
				values.put(RichAddressBookData.KEY_CONTACT_ID, contactId);

				// Set the timestamp
				values.put(RichAddressBookData.KEY_TIMESTAMP, Calendar.getInstance().getTimeInMillis());
				
				// Set the exist flag
				if (photoData != null) {
					values.put(RichAddressBookData.KEY_PHOTO_EXIST_FLAG, RichAddressBookData.TRUE_VALUE);
				} else {
					values.put(RichAddressBookData.KEY_PHOTO_EXIST_FLAG, RichAddressBookData.FALSE_VALUE);
				}
				
				// Set the etag
				values.put(RichAddressBookData.KEY_PHOTO_ETAG, etag);
				
				// Update the database
				String where = RichAddressBookData.KEY_CONTACT_ID + " = " + contactId;
				cr.update(databaseUri, values, where, null);
				
				// Save the photo content
				if (photoData != null) {
					int rowId = getContactRowId(contactId);
					Uri photoUri = getContactPhotoUri(rowId);
					OutputStream outstream = cr.openOutputStream(photoUri);
					outstream.write(photoData);
					outstream.flush();
					outstream.close();
				}				
				if (logger.isActivated()) {
					logger.debug("Photo icon has been updated for " + contact);
				}
			} else {
				if (logger.isActivated()) {
					logger.error("Contact " + contact + " not found");
				}
				throw new ContactNotFoundException();
			}
		} catch (Exception e) {
			if (logger.isActivated()) {
				logger.error("Internal exception", e);
			}
			throw new RichAddressBookException(e.getMessage());
		}
	}

	/**
	 * Get the presence info of a contact in the EAB
	 *  	
	 * @param contact Contact
	 * @return Presence info or null if contact not found or in case of error
	 */
	public PresenceInfo getContactPresenceInfo(String contact) {
		if (logger.isActivated()) {
			logger.info("Get presence info for contact " + contact);
		}

		PresenceInfo presenceInfo = null;
		try {
			// Get the contact id
			int contactId = getContactId(contact);
			if (contactId != -1) {
				// Query the database
				String where = RichAddressBookData.KEY_CONTACT_ID + " = " + contactId;
				Cursor c = cr.query(databaseUri, null, where, null, null);
				if (c.moveToFirst()) {
					presenceInfo = new PresenceInfo();

					// Get the timestamp
					presenceInfo.setTimestamp(c.getLong(RichAddressBookData.COLUMN_TIMESTAMP));

					// Get the favorite link 
					FavoriteLink favoriteLink = null;
					String url = c.getString(RichAddressBookData.COLUMN_FAVORITE_LINK_URL);
					String name = c.getString(RichAddressBookData.COLUMN_FAVORITE_LINK_NAME);
					if ((url != null) && (url.trim().length() > 0)) {
						favoriteLink = new FavoriteLink(url);
						if ((name != null) && (name.trim().length() > 0)) {
							favoriteLink.setName(name);
						}
					}
					presenceInfo.setFavoriteLink(favoriteLink);
					
					// Get the freetext
					presenceInfo.setFreetext(c.getString(RichAddressBookData.COLUMN_FREE_TEXT));
					
					// Get the hyper-availability
					presenceInfo.setHyperavailabilityStatus(Boolean.parseBoolean(c.getString(RichAddressBookData.COLUMN_HYPER_AVAILABILITY_FLAG)));
					
					// Get the geoloc
					Geoloc geoloc = null;
					boolean isGeolocExist = Boolean.parseBoolean(c.getString(RichAddressBookData.COLUMN_GEOLOC_EXIST_FLAG));
					if (isGeolocExist) {
						double latitude = c.getDouble(RichAddressBookData.COLUMN_GEOLOC_LATITUDE);
						double longitude = c.getDouble(RichAddressBookData.COLUMN_GEOLOC_LONGITUDE);
						double altitude = c.getDouble(RichAddressBookData.COLUMN_GEOLOC_ALTITUDE);
						geoloc = new Geoloc(latitude, longitude, altitude);
					}
					presenceInfo.setGeoloc(geoloc);

					// Set the capabilities
					Capabilities capabilities = new Capabilities(); 
					capabilities.setCsVideoSupport(Boolean.parseBoolean(c.getString(RichAddressBookData.COLUMN_CS_VIDEO_SUPPORTED)));
					capabilities.setFileTransferSupport(Boolean.parseBoolean(c.getString(RichAddressBookData.COLUMN_FILE_TRANSFER_SUPPORTED)));
					capabilities.setImageSharingSupport(Boolean.parseBoolean(c.getString(RichAddressBookData.COLUMN_IMAGE_SHARING_SUPPORTED)));
					capabilities.setImSessionSupport(Boolean.parseBoolean(c.getString(RichAddressBookData.COLUMN_IM_SESSION_SUPPORTED)));
					capabilities.setVideoSharingSupport(Boolean.parseBoolean(c.getString(RichAddressBookData.COLUMN_VIDEO_SHARING_SUPPORTED)));
					presenceInfo.setCapabilities(capabilities);
					
					// Get the photo-icon
					PhotoIcon photoIcon = null;
					boolean isPhotoExist = Boolean.parseBoolean(c.getString(RichAddressBookData.COLUMN_PHOTO_EXIST_FLAG));
					if (isPhotoExist) {
						int rowId = c.getInt(RichAddressBookData.COLUMN_KEY_ID);
						Uri photoUri = getContactPhotoUri(rowId);
						String etag = c.getString(RichAddressBookData.COLUMN_PHOTO_ETAG);
						Bitmap bmp = BitmapFactory.decodeStream(cr.openInputStream(photoUri));
						photoIcon = new PhotoIcon(bmp, etag);
					}
					presenceInfo.setPhotoIcon(photoIcon);
					c.close();
					if (logger.isActivated()) {
						logger.debug("Presence info has been read for " + contact);
					}
				} else {
					if (logger.isActivated()) {
						logger.error("Contact " + contact + " not found");
					}
				}
			}
		} catch (Exception e) {
			if (logger.isActivated()) {
				logger.error("Internal exception", e);
			}
		}
		return presenceInfo;
	}

	/**
	 * Set the sharing status of a contact in the EAB
	 * 
	 * @param contact Contact
	 * @param status Sharing status
	 * @param reason Reason associated to the status
	 * @throws RichAddressBookException
	 */
	public void setContactSharingStatus(String contact, String status, String reason) throws RichAddressBookException {
		if (logger.isActivated()) {
			logger.info("Set sharing status for contact " + contact);
		}

		try {
			// Get the contact id
			int contactId = getContactId(contact);
			if (contactId == -1) {
				if (logger.isActivated()){
					logger.debug("Contact " + contact + " does not exist");
				}
				return;
			}			
				
			// Search if the contact has an entry in EAB
			PresenceInfo info = getContactPresenceInfo(contact);
			if (info != null) {
					// Contact exist in EAB, update it
					if (!status.equalsIgnoreCase("terminated")) {
						// Event is not "terminated"
						String currentStatus = getContactSharingStatus(contact);
						if (status.equalsIgnoreCase("pending") &&
								(currentStatus != null) &&
									currentStatus.equalsIgnoreCase("pending_out")) {
							// Event "pending"
							if (logger.isActivated()) {
								logger.debug("Contact " + contact + " is already pending_out");
							}
							return;
						}
						
						if ((currentStatus != null) && currentStatus.equalsIgnoreCase("blocked")) {
							// Event "blocked"
							if (logger.isActivated()) {
								logger.debug("Contact " + contact + " is currently blocked");
							}
							return;
						}
	
						// Update the database
						ContentValues values = new ContentValues();
						values.put(RichAddressBookData.KEY_PRESENCE_SHARING_STATUS, status);
						String where = RichAddressBookData.KEY_CONTACT_ID + " = " + contactId;
						cr.update(databaseUri, values, where, null);
						if (logger.isActivated()) {
							logger.debug("Update sharing status to " + status + "/" + reason + " for contact " + contact);
						}
					} else
					if (status.equalsIgnoreCase("terminated") &&
							(reason != null) && reason.equalsIgnoreCase("rejected")) {
						// Event "terminated/rejected" 
						String where = RichAddressBookData.KEY_CONTACT_ID + " = " + contactId;
						cr.delete(databaseUri, where, null);
						if (logger.isActivated()) {
							logger.debug("Delete contact " + contact);
						}
					}
			} else {
				if (!status.equalsIgnoreCase("terminated")) {
					// Event is not "terminated"
					ContentValues values = new ContentValues();
					values.put(RichAddressBookData.KEY_CONTACT_ID, contactId);
					values.put(RichAddressBookData.KEY_PRESENCE_SHARING_STATUS, status);
					cr.insert(databaseUri, values);
					if (logger.isActivated()) {
						logger.debug("Insert sharing status to " + status + "/" + reason + " for contact " + contact);
					}
				}
			}
		} catch (Exception e) {
			if (logger.isActivated()) {
				logger.error("Internal exception", e);
			}
			throw new RichAddressBookException(e.getMessage());
		}
	}
	
	/**
	 * Get sharing status of a contact
	 *  
	 * @param contact Contact
	 * @return Status or if contact not found or in case of error
	 */
	public String getContactSharingStatus(String contact) {
		if (logger.isActivated()) {
			logger.info("Get sharing status for contact " + contact);
		}

		String result = null;
		try {
			// Get the contact id
			int contactId = getContactId(contact);
			if (contactId != -1) {
				// Query the database
				String where = RichAddressBookData.KEY_CONTACT_ID + " = " + contactId;
				Cursor c = cr.query(databaseUri, new String[] { RichAddressBookData.KEY_PRESENCE_SHARING_STATUS }, where, null, null);
				if (c.moveToFirst()) {
					result = c.getString(0);
				}
				c.close();
				if (logger.isActivated()) {
					logger.debug("Sharing status is " + result);
				}
			} else {
				if (logger.isActivated()) {
					logger.error("Contact " + contact + " not found");
				}
			}
		} catch (Exception e) {
			if (logger.isActivated()) {
				logger.error("Internal exception", e);
			}
		}
		return result;
	}

	/**
	 * Revoke a contact
	 * 
	 * @param contact Contact
	 * @throws ContactNotFoundException
	 * @throws RichAddressBookException
	 */
	public void revokeContact(String contact) throws RichAddressBookException {
		if (logger.isActivated()) {
			logger.info("Revoke contact " + contact);
		}

		try {
			// Get the contact id
			int contactId = getContactId(contact);
			if (contactId != -1) {
				String where = RichAddressBookData.KEY_CONTACT_ID + " = " + contactId;

				// Update the blacklist
				ContentValues values = new ContentValues();
				values.put(RichAddressBookData.KEY_BLACKLIST_STATUS, RichAddressBookData.REVOKED_VALUE);
				values.put(RichAddressBookData.KEY_CONTACT_ID, contactId);
				Cursor cursor = cr.query(RichAddressBookData.BLACKLIST_CONTENT_URI,	new String[] { RichAddressBookData.KEY_CONTACT_ID }, where, null, null);
				if (cursor.getCount() > 0) {
					// Update existing entry
					cr.update(RichAddressBookData.BLACKLIST_CONTENT_URI, values, where, null);
				} else {
					// Add a new entry
					cr.insert(RichAddressBookData.BLACKLIST_CONTENT_URI, values);
				}
				cursor.close();
				if (logger.isActivated()) {
					logger.debug("Contact " + contact + " has been added to blacklist");
				}

				// Delete the contact in EAB
				cr.delete(databaseUri, where, null);
				if (logger.isActivated()) {
					logger.debug("Contact " + contact + " has been deleted from EAB");
				}
			} else {
				if (logger.isActivated()) {
					logger.error("Contact " + contact + " not found");
				}
				throw new ContactNotFoundException();
			}
		} catch (Exception e) {
			if (logger.isActivated()) {
				logger.error("Internal exception", e);
			}
			throw new RichAddressBookException(e.getMessage());
		}
	}

	/**
	 * Unrevoke a contact
	 * 
	 * @param contact Contact
	 * @throws ContactNotFoundException
	 * @throws RichAddressBookException
	 */
	public void unrevokeContact(String contact) throws RichAddressBookException {
		if (logger.isActivated()) {
			logger.info("Unrevoke contact " + contact);
		}

		try {
			// Get the contact id
			int contactId = getContactId(contact);
			if (contactId != -1) {
				// Update the blacklist
				String where = RichAddressBookData.KEY_CONTACT_ID + " = " + contactId + " AND " +
					RichAddressBookData.KEY_BLACKLIST_STATUS + " = " + RichAddressBookData.REVOKED_VALUE;
				cr.delete(RichAddressBookData.BLACKLIST_CONTENT_URI, where,	null);
				if (logger.isActivated()) {
					logger.debug("Contact " + contact + " has been removed from blacklist");
				}
			} else {
				if (logger.isActivated()) {
					logger.error("Contact " + contact + " not found");
				}
				throw new ContactNotFoundException();
			}
		} catch (Exception e) {
			if (logger.isActivated()) {
				logger.error("Internal exception", e);
			}
			throw new RichAddressBookException(e.getMessage());
		}
	}

	/**
	 * Block a contact
	 * 
	 * @param contact Contact
	 * @throws RichAddressBookException
	 */	
	public void blockContact(String contact) throws RichAddressBookException {
		if (logger.isActivated()) {
			logger.info("Block contact " + contact);
		}

		try {
			// Get the contact id
			int contactId = getContactId(contact);
			if (contactId != -1) {
				// Contact exist in EAB
				String where = RichAddressBookData.KEY_CONTACT_ID + " = " + contactId;
				
				// Update the blacklist
				ContentValues values = new ContentValues();
				values.put(RichAddressBookData.KEY_CONTACT_ID, contactId);
				values.put(RichAddressBookData.KEY_BLACKLIST_STATUS, RichAddressBookData.BLOCKED_VALUE);
				Cursor cursor = cr.query(RichAddressBookData.BLACKLIST_CONTENT_URI,	new String[] { RichAddressBookData.KEY_CONTACT_ID }, where, null, null);
				if (cursor.getCount() > 0) {
					// Update existing entry
					cr.update(RichAddressBookData.BLACKLIST_CONTENT_URI, values, where, null);
				} else {
					// Add a new entry
					cr.insert(RichAddressBookData.BLACKLIST_CONTENT_URI, values);
				}
				if (logger.isActivated()) {
					logger.debug("Contact " + contact + " has been added to blacklist");
				}
				cursor.close();
				
				// Update EAB
				values = new ContentValues();
				values.put(RichAddressBookData.KEY_PRESENCE_SHARING_STATUS, "blocked");
				cr.update(databaseUri, values, where, null);
				if (logger.isActivated()) {
					logger.debug("Update sharing status to blocked for contact " + contact);
				}
			} else {
				// Contact does not exist in EAB
				String number = PhoneUtils.extractNumberFromUri(contact);

				// Update the blacklist
				ContentValues values = new ContentValues();
				values.put(RichAddressBookData.KEY_CONTACT_ID, -1);
				values.put(RichAddressBookData.KEY_BLACKLIST_STATUS, RichAddressBookData.BLOCKED_VALUE);
				values.put(RichAddressBookData.KEY_CONTACT_NUMBER, number);
				String where = RichAddressBookData.KEY_CONTACT_NUMBER + " = " + number;
				Cursor cursor = cr.query(RichAddressBookData.BLACKLIST_CONTENT_URI,	new String[] { RichAddressBookData.KEY_CONTACT_NUMBER }, where, null, null);
				if (cursor.getCount() > 0) {
					// Update existing entry
					cr.update(RichAddressBookData.BLACKLIST_CONTENT_URI, values, where, null);
				} else {
					// Add a new entry
					cr.insert(RichAddressBookData.BLACKLIST_CONTENT_URI, values);
				}
				if (logger.isActivated()) {
					logger.debug("Contact " + contact + " has been added to blacklist");
				}
				cursor.close();
			}
		} catch (Exception e) {
			if (logger.isActivated()) {
				logger.error("Internal exception", e);
			}
			throw new RichAddressBookException(e.getMessage());
		}
	}

	/**
	 * Unblock a contact
	 * 
	 * @param contact Contact
	 * @throws RichAddressBookException
	 */	
	public void unblockContact(String contact) throws RichAddressBookException {
		if (logger.isActivated()) {
			logger.info("Unblock contact " + contact);
		}

		try {
			// Get the contact id
			int contactId = getContactId(contact);
			if (contactId != -1) {
				// Update the blacklist
				String where = RichAddressBookData.KEY_CONTACT_ID + " = " + contactId + " AND " +
					RichAddressBookData.KEY_BLACKLIST_STATUS + " = " + RichAddressBookData.BLOCKED_VALUE;
				cr.delete(RichAddressBookData.BLACKLIST_CONTENT_URI, where,	null);
				if (logger.isActivated()) {
					logger.debug("Contact " + contact + " has been deleted from blacklist");
				}
				
				// Update EAB
				ContentValues values = new ContentValues();
				values.put(RichAddressBookData.KEY_PRESENCE_SHARING_STATUS, "active");
				cr.update(databaseUri, values, RichAddressBookData.KEY_CONTACT_ID + " = " + contactId, null);
				if (logger.isActivated()) {
					logger.debug("Update sharing status to active for contact " + contact);
				}
			} else {
				// Update the blacklist
				String where = RichAddressBookData.KEY_CONTACT_NUMBER + " = " +
					PhoneUtils.extractNumberFromUri(contact) + " AND " +
					RichAddressBookData.KEY_BLACKLIST_STATUS + " = " + RichAddressBookData.BLOCKED_VALUE;
				cr.delete(RichAddressBookData.BLACKLIST_CONTENT_URI, where,	null);
				if (logger.isActivated()) {
					logger.debug("Contact " + contact + " has been deleted from blacklist");
				}
			}
		} catch (Exception e) {
			if (logger.isActivated()) {
				logger.error("Internal exception", e);
			}
			throw new RichAddressBookException(e.getMessage());
		}
	}

	/**
	 * Returns the display name of a contact
	 *  
	 * @param contact Contact
	 * @return Display name or null if the contact is not found or in case of error
	 */
	public String getContactDisplayName(String contact) {
		String result = null;
		try {
			String number = PhoneUtils.extractNumberFromUri(contact);
			Cursor c = cr.query(Contacts.Phones.CONTENT_URI,
					new String[] { Contacts.Phones.DISPLAY_NAME, Contacts.Phones.NUMBER }, null, null, null);
			while(c.moveToNext()) {
				String databaseNumber = PhoneUtils.extractNumberFromUri(c.getString(1));
				if (databaseNumber.equals(number)) {
					result = c.getString(0);
					break;
				}
			}
			c.close();
		} catch (Exception e) {
			if (logger.isActivated()) {
				logger.error("Internal exception", e);
			}
		}
		return result;
	}

	/**
	 * Returns the photo etag of a contact
	 *  
	 * @param contact Contact
	 * @return Etag or null if the contact is not found or in case of error
	 */
	public String getContactPhotoEtag(String contact) {
		String result = null;
		try {
			// Get the contact id
			int contactId = getContactId(contact);
			if (contactId != -1) {
				// Query the database
				String where = RichAddressBookData.KEY_CONTACT_ID + " = " + contactId;
				Cursor c = cr.query(databaseUri, new String[] { RichAddressBookData.KEY_PHOTO_ETAG }, where, null, null);
				if (c.moveToFirst()) {
					result = c.getString(0);
				}
				c.close();
			}
		} catch (Exception e) {
			if (logger.isActivated()) {
				logger.error("Internal exception", e);
			}
		}
		return result;
	}

	/**
	 * Create a new contact in the address book
	 * 
	 * @param contact Contact
	 * @throws RichAddressBookException
	 */
	public void createContact(String contact) throws RichAddressBookException {
		try {
			// Get the contact id
			int contactId = getContactId(contact);
			if (contactId == -1) {
				// Contact does not exist in the address book
				if (logger.isActivated()) {
					logger.info("Create contact " + contact + " in the address book");
				}
				
				// Add a new contact in the address book
				ContentValues values = new ContentValues();
				String number = PhoneUtils.extractNumberFromUri(contact);
				values.put(Contacts.People.NAME, number);
				// TODO: deprecated method
				Uri newPersonUri = Contacts.People.createPersonInMyContactsGroup(cr, values);
				
				// Add a primary mobile phone number to the new created contact
				ContentValues phoneValues = new ContentValues();
				Uri phoneUri = Uri.withAppendedPath(newPersonUri, Contacts.People.Phones.CONTENT_DIRECTORY);
				phoneValues.put(Contacts.Phones.NUMBER, number);
				phoneValues.put(Contacts.Phones.ISPRIMARY, 1);
				phoneValues.put(Contacts.Phones.TYPE, Contacts.Phones.TYPE_MOBILE);
				cr.insert(phoneUri, phoneValues);
				if (logger.isActivated()) {
					logger.debug("Contact " + contact + " has been created in the address book");
				}
			}
		} catch (Exception e) {
			if (logger.isActivated()) {
				logger.error("Internal exception", e);
			}
			throw new RichAddressBookException(e.getMessage());
		}
	}
}
