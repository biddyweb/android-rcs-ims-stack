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
package com.orangelabs.rcs.provider.eab;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.orangelabs.rcs.core.ims.service.capability.Capabilities;
import com.orangelabs.rcs.core.ims.service.presence.FavoriteLink;
import com.orangelabs.rcs.core.ims.service.presence.Geoloc;
import com.orangelabs.rcs.core.ims.service.presence.PhotoIcon;
import com.orangelabs.rcs.core.ims.service.presence.PresenceInfo;
import com.orangelabs.rcs.service.RcsCoreService;
import com.orangelabs.rcs.utils.ContactUtils;
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
	}

	/**
	 * Return my row id in the EAB
	 * 
	 * @return Row id or -1 in case of error
	 */
	private int getMyRowId() {
		int rowId = -1;
		try {
			String where = RichAddressBookData.KEY_CONTACT_NUMBER + " = " + "\"" + RichAddressBookData.END_USER_ROW_CONTACT_NUMBER + "\"";
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
	 * Return the row id of a profile number in the EAB
	 * 
	 * @param number Profile number
	 * @return Row id
	 */
	private int getProfileRowId(String number) {
		int rowId = -1;
		try {
			String where = RichAddressBookData.KEY_CONTACT_NUMBER + " = " + "\"" + number + "\"";
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
	 * Returns the photo URI of a profile in the EAB
	 * 
	 * @param id Profile id
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
		return ContentUris.withAppendedId(RichAddressBookData.CONTENT_URI, getMyRowId());
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
			values.put(RichAddressBookData.KEY_CONTACT_NUMBER, RichAddressBookData.END_USER_ROW_CONTACT_NUMBER);
			
			// Set the timestamp
			values.put(RichAddressBookData.KEY_TIMESTAMP, Calendar.getInstance().getTimeInMillis());
			
			// Set the availability status
			values.put(RichAddressBookData.KEY_AVAILABILITY_STATUS, info.getPresenceStatus());
			
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
			String where = RichAddressBookData.KEY_CONTACT_NUMBER + " = " + "\""+ RichAddressBookData.END_USER_ROW_CONTACT_NUMBER + "\"";
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
			
			// Set the timestamp
			values.put(RichAddressBookData.KEY_TIMESTAMP, Calendar.getInstance().getTimeInMillis());
			
			// Set the exist flag
			values.put(RichAddressBookData.KEY_PHOTO_EXIST_FLAG, RichAddressBookData.TRUE_VALUE);
			
			// Set the etag
			values.put(RichAddressBookData.KEY_PHOTO_ETAG, photo.getEtag());

			// Update the database
			String where = RichAddressBookData.KEY_CONTACT_NUMBER + " = " +  "\"" + RichAddressBookData.END_USER_ROW_CONTACT_NUMBER + "\"";
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

			// Set the timestamp
			values.put(RichAddressBookData.KEY_TIMESTAMP, Calendar.getInstance().getTimeInMillis());
			
			// Set the exist flag
			values.put(RichAddressBookData.KEY_PHOTO_EXIST_FLAG, RichAddressBookData.FALSE_VALUE);

			// Set the etag
			values.put(RichAddressBookData.KEY_PHOTO_ETAG, "");
			
			// Update the database
			String where = RichAddressBookData.KEY_CONTACT_NUMBER + " = " +  "\"" + RichAddressBookData.END_USER_ROW_CONTACT_NUMBER + "\"";
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
			String where = RichAddressBookData.KEY_CONTACT_NUMBER + " = " + "\"" + RichAddressBookData.END_USER_ROW_CONTACT_NUMBER + "\"";
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
				
				// Get the availability status
				presenceInfo.setPresenceStatus(c.getString(RichAddressBookData.COLUMN_AVAILABILITY_STATUS));
				
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
					InputStream stream = cr.openInputStream(photoUri);
					byte[] content = new byte[stream.available()];
					stream.read(content, 0, content.length);
					Bitmap bmp = BitmapFactory.decodeByteArray(content, 0, content.length);
					if (bmp != null) {
						photoIcon = new PhotoIcon(content, bmp.getWidth(), bmp.getHeight(), etag);
					}
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

		// May be called from outside the core, so be sure the number format is international before doing the queries 
		contact = PhoneUtils.extractNumberFromUri(contact);
		
		try {
			ContentValues values = new ContentValues();

			// Set the contact number
			values.put(RichAddressBookData.KEY_CONTACT_NUMBER, contact);

			// Set the timestamp 
			values.put(RichAddressBookData.KEY_TIMESTAMP, info.getTimestamp());

			// Set the availability status
			values.put(RichAddressBookData.KEY_AVAILABILITY_STATUS, info.getPresenceStatus());

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

			// Set the weblink updated flag if necessary, ie set it to false if new favorite link is different from last one
			// Read the value in database
			// Query the database
			String where = RichAddressBookData.KEY_CONTACT_NUMBER + " = " + "\"" + contact + "\"";
			Cursor c = cr.query(databaseUri, 
					new String[]{RichAddressBookData.KEY_FAVORITE_LINK_URL, RichAddressBookData.KEY_FAVORITE_LINK_NAME}, 
					where, 
					null, 
					null);
			if (c.moveToFirst()) {
				String lastFavoriteUrl = c.getString(0);
				String lastFavoriteName = c.getString(1);
				// Check if weblink has changed
				boolean urlChanged = ((lastFavoriteUrl!=null) && !(lastFavoriteUrl.equalsIgnoreCase(linkUrl)))
					|| (lastFavoriteUrl==null && linkUrl!=null);
				boolean nameChanged = ((lastFavoriteName!=null) && !(lastFavoriteName.equalsIgnoreCase(linkName)))
					|| (lastFavoriteName==null && linkName!=null);
					
				if (urlChanged || nameChanged){
					values.put(RichAddressBookData.KEY_WEBLINK_UPDATED_FLAG, RichAddressBookData.TRUE_VALUE);
				}
			}
			c.close();
			
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
			int updatedRow = cr.update(databaseUri, values, where, null);			

			if (updatedRow>0){
				if (logger.isActivated()) {
					logger.debug("Presence info has been updated for " + contact);
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

		// May be called from outside the core, so be sure the number format is international before doing the queries 
		contact = PhoneUtils.extractNumberFromUri(contact);
		
		try {
			ContentValues values = new ContentValues();

			// Set the contact id 
			values.put(RichAddressBookData.KEY_CONTACT_NUMBER, contact);

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
			String where = RichAddressBookData.KEY_CONTACT_NUMBER + " = " + "\"" +  contact + "\"";
			int updatedRow = cr.update(databaseUri, values, where, null);

			if (updatedRow>0){
				// Save the photo content
				if (photoData != null) {
					int rowId = getProfileRowId(contact);
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

		// May be called from outside the core, so be sure the number format is international before doing the queries 
		contact = PhoneUtils.extractNumberFromUri(contact);
		
		PresenceInfo presenceInfo = null;
		try {
			// Query the database
			String where = RichAddressBookData.KEY_CONTACT_NUMBER + " = " + "\"" + contact + "\"";
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

				// Get the availability status
				presenceInfo.setPresenceStatus(c.getString(RichAddressBookData.COLUMN_AVAILABILITY_STATUS));

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
					InputStream stream = cr.openInputStream(photoUri);
					byte[] content = new byte[stream.available()];
					stream.read(content, 0, content.length);
					Bitmap bmp = BitmapFactory.decodeByteArray(content, 0, content.length);
					if (bmp != null) {
						photoIcon = new PhotoIcon(content, bmp.getWidth(), bmp.getHeight(), etag);
					}
				}
				presenceInfo.setPhotoIcon(photoIcon);
				if (logger.isActivated()) {
					logger.debug("Presence info has been read for " + contact);
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
	 * Set the sharing status of a contact in the EAB
	 * 
	 * @param contact Contact
	 * @param status Sharing status
	 * @param reason Reason associated to the status
	 * @throws RichAddressBookException
	 */
	public void setContactSharingStatus(String contact, String status, String reason) throws RichAddressBookException {
		if (logger.isActivated()) {
			logger.info("Set sharing status for contact " + contact + " to "+status+ " with reason "+reason);
		}

		// May be called from outside the core, so be sure the number format is international before doing the queries 
		contact= PhoneUtils.extractNumberFromUri(contact);

		try {
			String where = RichAddressBookData.KEY_CONTACT_NUMBER + " = " + "\"" + contact + "\"";
			ContentValues values = new ContentValues();
			values.put(RichAddressBookData.KEY_CONTACT_NUMBER, contact);
			// Be sure to put a default availability status
			values.put(RichAddressBookData.KEY_AVAILABILITY_STATUS, PresenceInfo.UNKNOWN);
			
			// Get the current contact sharing status EAB database, if there is one
			String currentStatus = getContactSharingStatus(contact);
			if (currentStatus!=null && !currentStatus.equalsIgnoreCase(RichAddressBookData.STATUS_CANCELLED)){
				// We already are in a given RCS state, different from cancelled
				/**
				 * INVITED STATE
				 */
				if (currentStatus.equalsIgnoreCase(RichAddressBookData.STATUS_INVITED)){
					// State: we have invited the remote contact
					// We leave this state only on a "terminated/rejected" or an "active" status
					if(status.equalsIgnoreCase("terminated") &&
							(reason != null) && reason.equalsIgnoreCase("rejected")){
						// We have ended our profile sharing, destroy entry in EAB
						cr.delete(databaseUri, where, null);
						if (logger.isActivated()) {
							logger.debug(contact + " has passed from \"pending_out\" to \"terminated/rejected\" state");
							logger.debug("=> Remove contact entry from EAB");
						}
						return;
					}else if (status.equalsIgnoreCase("active")){
						// Contact has accepted our invitation, we are now active
						values.put(RichAddressBookData.KEY_TIMESTAMP, Calendar.getInstance().getTimeInMillis());
						values.put(RichAddressBookData.KEY_PRESENCE_SHARING_STATUS, RichAddressBookData.STATUS_ACTIVE);
						// Update entry in EAB
						cr.update(databaseUri, values, where, null);
						if (logger.isActivated()) {
							logger.debug(contact + " has passed from \"pending_out\" to \"active\" state");
						}
						return;
					}
				}
					
				/**
				 * WILLING STATE
				 */
				if (currentStatus.equalsIgnoreCase(RichAddressBookData.STATUS_WILLING)){
					// State: we have been invited by the remote contact
					if(status.equalsIgnoreCase("active")){
						// We have accepted the invitation, we are now active
						values.put(RichAddressBookData.KEY_TIMESTAMP, Calendar.getInstance().getTimeInMillis());
						values.put(RichAddressBookData.KEY_PRESENCE_SHARING_STATUS, RichAddressBookData.STATUS_ACTIVE);
						// Update entry in EAB
						cr.update(databaseUri, values, where, null);
						if (logger.isActivated()) {
							logger.debug(contact + " has passed from \"pending\" to \"active\" state");
						}
						return;
					}else if (status.equalsIgnoreCase("terminated") &&
							(reason != null) && reason.equalsIgnoreCase("giveup")){
						// Contact has cancelled its invitation
						// Destroy entry in EAB
						cr.delete(databaseUri, where, null);
						if (logger.isActivated()) {
							logger.debug(contact + " has passed from \"pending\" to \"terminated/giveup\" state");
							logger.debug("=> Remove contact entry from EAB");
						}
						return;
					}else if (status.equalsIgnoreCase(RichAddressBookData.STATUS_BLOCKED)){
						// We have declined the invitation
						values.put(RichAddressBookData.KEY_TIMESTAMP, Calendar.getInstance().getTimeInMillis());
						values.put(RichAddressBookData.KEY_PRESENCE_SHARING_STATUS, RichAddressBookData.STATUS_BLOCKED);
						cr.update(databaseUri, values, where, null);
						if (logger.isActivated()) {
							logger.debug(contact + " has passed from \"pending\" to \"blocked\" state");
						}
						return;
					}
				}
				
				/**
				 * ACTIVE STATE
				 */
				if (currentStatus.equalsIgnoreCase(RichAddressBookData.STATUS_ACTIVE)){
					// State: we have shared our profile with contact
					// We leave this state only on a "terminated/rejected" status
					if(status.equalsIgnoreCase("terminated") &&
							(reason != null) && reason.equalsIgnoreCase("rejected")){
						// We have ended our profile sharing, destroy entry in EAB
						cr.delete(databaseUri, where, null);
						if (logger.isActivated()) {
							logger.debug(contact + " has passed from \"pending_out\" to \"terminated/rejected\" state");
							logger.debug("=> Remove contact entry from EAB");
						}
						return;
					}
				}
				
				/**
				 * BLOCKED STATE
				 */
				if (currentStatus.equalsIgnoreCase(RichAddressBookData.STATUS_BLOCKED)){
					// State: we have blocked this contact
					// We leave this state only when user unblocks, ie destroys the entry
						return;
				}
				
			}else if (currentStatus==null){
				// We have no entry for contact in EAB
				/**
				 * NO ENTRY IN EAB
				 */
				if (status.equalsIgnoreCase(RichAddressBookData.STATUS_INVITED)){
					// We invite contact to share presence
					// Contact has accepted our invitation, we are now active
					values.put(RichAddressBookData.KEY_TIMESTAMP, Calendar.getInstance().getTimeInMillis());
					values.put(RichAddressBookData.KEY_PRESENCE_SHARING_STATUS, RichAddressBookData.STATUS_INVITED);
					// Update entry in EAB
					cr.insert(databaseUri, values);
					if (logger.isActivated()) {
						logger.debug(contact + " has been added to the EAB with the \"pending_out\" state");
					}
					return;
				}
				
				if (status.equalsIgnoreCase(RichAddressBookData.STATUS_ACTIVE)){
					//TODO there may be some other stuff to do
					// We received an active state from the contact, but we have no entry for him in EAB yet
					// It may occur if the number was deleted from native EAB, or if there was an error when we deleted/modified it
					// or if we logged on this RCS account on a new phone

					// => We create the entry in EAB
					values.put(RichAddressBookData.KEY_TIMESTAMP, Calendar.getInstance().getTimeInMillis());
					values.put(RichAddressBookData.KEY_PRESENCE_SHARING_STATUS, RichAddressBookData.STATUS_ACTIVE);
					// Update entry in EAB
					cr.insert(databaseUri, values);
					if (logger.isActivated()) {
						logger.debug(contact + " has been added to the EAB with the \"active\" state");
					}
					
					// => We create the entry in the regular address book
					ContactUtils.createRcsContactIfNeeded(RcsCoreService.CONTEXT, contact);
					return;
				}

				if (status.equalsIgnoreCase(RichAddressBookData.STATUS_WILLING)){
					// We received a "pending" notification => contact has invited us 
					// => We create the entry in EAB
					values.put(RichAddressBookData.KEY_TIMESTAMP, Calendar.getInstance().getTimeInMillis());
					values.put(RichAddressBookData.KEY_PRESENCE_SHARING_STATUS, RichAddressBookData.STATUS_WILLING);
					// Update entry in EAB
					cr.insert(databaseUri, values);
					if (logger.isActivated()) {
						logger.debug(contact + " has been added to the EAB with the \"pending\" state");
					}
					return;
				}
				
				if (status.equalsIgnoreCase(RichAddressBookData.STATUS_BLOCKED)){
					// We block the contact to prevent invitations from him
					values.put(RichAddressBookData.KEY_TIMESTAMP, Calendar.getInstance().getTimeInMillis());
					values.put(RichAddressBookData.KEY_PRESENCE_SHARING_STATUS, RichAddressBookData.STATUS_BLOCKED);
					// Update entry in EAB
					cr.insert(databaseUri, values);
					if (logger.isActivated()) {
						logger.debug(contact + " has been added to the EAB with the \"blocked\" state");
					}
					return;
				}
			}else if (currentStatus.equalsIgnoreCase(RichAddressBookData.STATUS_CANCELLED)){
				// We are in the cancelled state
				// Behavior is the same than in "not in address book", except we have to update the status as the entry already exists
				/**
				 * CANCELLED STATE
				 */
				if (status.equalsIgnoreCase(RichAddressBookData.STATUS_INVITED)){
					// We invite contact to share presence
					// Contact has accepted our invitation, we are now active
					values.put(RichAddressBookData.KEY_TIMESTAMP, Calendar.getInstance().getTimeInMillis());
					values.put(RichAddressBookData.KEY_PRESENCE_SHARING_STATUS, RichAddressBookData.STATUS_INVITED);
					// Update entry in EAB
					cr.update(databaseUri, values, where, null);
					if (logger.isActivated()) {
						logger.debug(contact + " has been updated in the EAB with the \"pending_out\" state");
					}
					return;
				}
				
				if (status.equalsIgnoreCase(RichAddressBookData.STATUS_ACTIVE)){
					values.put(RichAddressBookData.KEY_TIMESTAMP, Calendar.getInstance().getTimeInMillis());
					values.put(RichAddressBookData.KEY_PRESENCE_SHARING_STATUS, RichAddressBookData.STATUS_ACTIVE);
					// Update entry in EAB
					cr.update(databaseUri, values, where, null);
					if (logger.isActivated()) {
						logger.debug(contact + " has been updated in the EAB with the \"active\" state");
					}
					return;
				}

				if (status.equalsIgnoreCase(RichAddressBookData.STATUS_WILLING)){
					// We received a "pending" notification => contact has invited us 
					// => We create the entry in EAB
					values.put(RichAddressBookData.KEY_TIMESTAMP, Calendar.getInstance().getTimeInMillis());
					values.put(RichAddressBookData.KEY_PRESENCE_SHARING_STATUS, RichAddressBookData.STATUS_WILLING);
					// Update entry in EAB
					cr.update(databaseUri, values, where, null);
					if (logger.isActivated()) {
						logger.debug(contact + " has been updated in the EAB with the \"pending\" state");
					}
					return;
				}
				
				if (status.equalsIgnoreCase(RichAddressBookData.STATUS_BLOCKED)){
					// We block the contact to prevent invitations from him
					values.put(RichAddressBookData.KEY_TIMESTAMP, Calendar.getInstance().getTimeInMillis());
					values.put(RichAddressBookData.KEY_PRESENCE_SHARING_STATUS, RichAddressBookData.STATUS_BLOCKED);
					// Update entry in EAB
					cr.update(databaseUri, values, where, null);
					if (logger.isActivated()) {
						logger.debug(contact + " has been updated in the EAB with the \"blocked\" state");
					}
					return;
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
		
		// May be called from outside the core, so be sure the number format is international before doing the queries 
		contact = PhoneUtils.extractNumberFromUri(contact);
		
		String result = null;
		try {
			// Query the database
			String where = RichAddressBookData.KEY_CONTACT_NUMBER + " = " + "\"" + contact + "\"";
			Cursor c = cr.query(databaseUri, new String[] { RichAddressBookData.KEY_PRESENCE_SHARING_STATUS }, where, null, null);
			if (c.moveToFirst()) {
				result = c.getString(0);
			}
			c.close();
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
		contact = PhoneUtils.extractNumberFromUri(contact);

		// Update the database
		ContentValues values = new ContentValues();
		// Set the exist flag
		if (updated) {
			values.put(RichAddressBookData.KEY_WEBLINK_UPDATED_FLAG, RichAddressBookData.TRUE_VALUE);
		} else {
			values.put(RichAddressBookData.KEY_WEBLINK_UPDATED_FLAG, RichAddressBookData.FALSE_VALUE);
		}
		// Set the timestamp
		values.put(RichAddressBookData.KEY_TIMESTAMP, Calendar.getInstance().getTimeInMillis());

		String where = RichAddressBookData.KEY_CONTACT_NUMBER + " = " + "\"" + contact + "\"";
		cr.update(databaseUri, values, where, null);
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
		contact = PhoneUtils.extractNumberFromUri(contact);

		// Query the database
		boolean result = false;
		String where = RichAddressBookData.KEY_CONTACT_NUMBER + " = " + "\"" + contact + "\"";
		Cursor c = cr.query(databaseUri, new String[]{RichAddressBookData.KEY_WEBLINK_UPDATED_FLAG}, where, null, null);
		if (c.moveToFirst()) {
			result = Boolean.parseBoolean(c.getString(0));
		}
		c.close();
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

		// May be called from outside the core, so be sure the number format is international before doing the queries 
		contact = PhoneUtils.extractNumberFromUri(contact);
		
		try {
			String where = RichAddressBookData.KEY_CONTACT_NUMBER + " = " + "\"" + contact + "\"";

			// Update the status
			ContentValues values = new ContentValues();
			values.put(RichAddressBookData.KEY_PRESENCE_SHARING_STATUS, RichAddressBookData.STATUS_REVOKED);
			int updatedRow = cr.update(RichAddressBookData.CONTENT_URI, values, where, null);
			if (updatedRow>0){
				if (logger.isActivated()) {
					logger.debug("Contact " + contact + " is now revoked");
				}
			}else{
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

		// May be called from outside the core, so be sure the number format is international before doing the queries 
		contact = PhoneUtils.extractNumberFromUri(contact);

		try {
			// Delete from the EAB database
			String where = RichAddressBookData.KEY_CONTACT_NUMBER + " = " + "\"" + contact + "\"";
			int deletedRow = cr.delete(RichAddressBookData.CONTENT_URI, where,	null);
			if (deletedRow>0){
				if (logger.isActivated()) {
					logger.debug("Contact " + contact + " has been removed from rich address book database");
				}
			}else {
				if (logger.isActivated()) {
					logger.error("Contact " + contact + " not found");
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
	 * Block a contact
	 * 
	 * @param contact Contact
	 * @throws RichAddressBookException
	 */	
	public void blockContact(String contact) throws RichAddressBookException {
		if (logger.isActivated()) {
			logger.info("Block contact " + contact);
		}

		// May be called from outside the core, so be sure the number format is international before doing the queries 
		contact = PhoneUtils.extractNumberFromUri(contact);

		try {
			String where = RichAddressBookData.KEY_CONTACT_NUMBER + " = " + "\"" + contact + "\"";
			
			// Update the status
			ContentValues values = new ContentValues();
			values.put(RichAddressBookData.KEY_CONTACT_NUMBER, contact);
			values.put(RichAddressBookData.KEY_PRESENCE_SHARING_STATUS, RichAddressBookData.STATUS_BLOCKED);
			int updatedRow = cr.update(RichAddressBookData.CONTENT_URI, values, where, null);
			if (updatedRow>0){
				if (logger.isActivated()) {
					logger.debug("Contact " + contact + " is now blocked");
				}
			}else{
				if (logger.isActivated()) {
					logger.error("Contact " + contact + " not found, create an entry to mark it as blocked");
				}
				cr.insert(RichAddressBookData.CONTENT_URI, values);
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

		// May be called from outside the core, so be sure the number format is international before doing the queries 
		contact = PhoneUtils.extractNumberFromUri(contact);

		try {
			// Delete from the EAB database
			String where = RichAddressBookData.KEY_CONTACT_NUMBER + " = " + "\"" + contact + "\"";
			int deletedRow = cr.delete(RichAddressBookData.CONTENT_URI, where,	null);
			if (deletedRow>0){
				if (logger.isActivated()) {
					logger.debug("Contact " + contact + " has been removed from rich address book database");
				}
			}else {
				if (logger.isActivated()) {
					logger.error("Contact " + contact + " not found");
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
	 * Remove rich entry for a contact
	 * 
	 * @param contact Contact
	 * @throws RichAddressBookException
	 */	
	public void removeContact(String contact) throws RichAddressBookException {
		if (logger.isActivated()) {
			logger.info("Remove contact " + contact + " from the rich address book");
		}

		// May be called from outside the core, so be sure the number format is international before doing the queries 
		contact = PhoneUtils.extractNumberFromUri(contact);

		try {
			// Delete from the EAB database
			String where = RichAddressBookData.KEY_CONTACT_NUMBER + " = " + "\"" + contact + "\"";
			int deletedRow = cr.delete(RichAddressBookData.CONTENT_URI, where,	null);
			if (deletedRow>0){
				if (logger.isActivated()) {
					logger.debug("Contact " + contact + " has been removed from rich address book database");
				}
			}else {
				if (logger.isActivated()) {
					logger.error("Contact " + contact + " not found");
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
	 * Returns the photo etag of a contact
	 *  
	 * @param contact Contact
	 * @return Etag or null if the contact is not found or in case of error
	 */
	public String getContactPhotoEtag(String contact) {
		String result = null;
		
		// May be called from outside the core, so be sure the number format is international before doing the queries 
		contact = PhoneUtils.extractNumberFromUri(contact);
		
		try {
			// Query the database
			String where = RichAddressBookData.KEY_CONTACT_NUMBER + " = " + "\"" + contact + "\"";
			Cursor c = cr.query(databaseUri, new String[] { RichAddressBookData.KEY_PHOTO_ETAG }, where, null, null);
			if (c.moveToFirst()) {
				result = c.getString(0);
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
	 * Get the RCS contacts
	 * 
	 * @return list containing all RCS contacts, "Me" item excluded 
	 */
	public List<String> getRcsContacts(){
		List<String> rcsNumbers = new ArrayList<String>();
		String where = RichAddressBookData.KEY_CONTACT_NUMBER + " <> " +  "\"" + RichAddressBookData.END_USER_ROW_CONTACT_NUMBER + "\"";
		Cursor c = cr.query(databaseUri, 
				new String[] { RichAddressBookData.KEY_CONTACT_NUMBER}, 
				where, 
				null, 
				null);
		while (c.moveToNext()) {
			rcsNumbers.add(c.getString(0));
		}
		c.close();
		return rcsNumbers;
	}
	
	/**
	 * Flush all data contained in the database
	 */
	public void flushAllData(){
		// Delete all entries except "My Profile"
		String where = RichAddressBookData.KEY_CONTACT_NUMBER + "<>" + RichAddressBookData.END_USER_ROW_CONTACT_NUMBER;
		cr.delete(databaseUri, where, null);
		
		// Update the "My Profile" item, with empty values
		ContentValues values = new ContentValues();
    	values.put(RichAddressBookData.KEY_TIMESTAMP, System.currentTimeMillis());
    	values.put(RichAddressBookData.KEY_PHOTO_EXIST_FLAG, RichAddressBookData.FALSE_VALUE);
    	values.put(RichAddressBookData.KEY_FREE_TEXT, "");
    	// TODO: geoloc

    	// Update the end user row id
		where = RichAddressBookData.KEY_CONTACT_NUMBER + " = " +  "\"" + RichAddressBookData.END_USER_ROW_CONTACT_NUMBER + "\"";
		cr.update(databaseUri, values, where, null);
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
		if (status!=null && status.equalsIgnoreCase(RichAddressBookData.STATUS_BLOCKED)){
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
		// Get this number status in address book
		String status = getContactSharingStatus(number);
		if (status!=null && status.equalsIgnoreCase(RichAddressBookData.STATUS_ACTIVE)){
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
		// Get this number status in address book
		String status = getContactSharingStatus(number);
		if (status!=null && status.equalsIgnoreCase(RichAddressBookData.STATUS_INVITED)){
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
		// Get this number status in address book
		String status = getContactSharingStatus(number);
		if (status!=null && status.equalsIgnoreCase(RichAddressBookData.STATUS_WILLING)){
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
		// Get this number status in address book
		String status = getContactSharingStatus(number);
		if (status!=null && status.equalsIgnoreCase(RichAddressBookData.STATUS_CANCELLED)){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * Has the number CS Video capability
	 * 
	 * @param number Number to check
	 * @return boolean
	 */
	public boolean isNumberCSVideoCapable(String number) {
		PresenceInfo info = getContactPresenceInfo(number);
		if (info!=null){
			return info.getCapabilities().isCsVideoSupported();
		}else{
			return false;
		}
	}
	
	/**
	 * Has the number Image Sharing capability
	 * 
	 * @param number Number to check
	 * @return boolean
	 */
	public boolean isNumberImageShareCapable(String number) {
		PresenceInfo info = getContactPresenceInfo(number);
		if (info!=null){
			return info.getCapabilities().isImageSharingSupported();
		}else{
			return false;
		}
	}
	
	/**
	 * Has the number Video Sharing capability
	 * 
	 * @param number Number to check
	 * @return boolean
	 */
	public boolean isNumberVideoShareCapable(String number) {
		PresenceInfo info = getContactPresenceInfo(number);
		if (info!=null){
			return info.getCapabilities().isVideoSharingSupported();
		}else{
			return false;
		}
	}
	
	/**
	 * Has the number Instant Messaging Session capability
	 * 
	 * @param number Number to check
	 * @return boolean
	 */
	public boolean isNumberImSessionCapable(String number) {
		PresenceInfo info = getContactPresenceInfo(number);
		if (info!=null){
			return info.getCapabilities().isImSessionSupported();
		}else{
			return false;
		}
	}
	
	/**
	 * Has the number file transfer capability
	 * 
	 * @param number Number to check
	 * @return boolean
	 */
	public boolean isNumberFileTransferCapable(String number) {
		PresenceInfo info = getContactPresenceInfo(number);
		if (info!=null){
			return info.getCapabilities().isFileTransferSupported();
		}else{
			return false;
		}
	}
}
