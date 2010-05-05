package com.orangelabs.rcs.provider.eab;

import android.net.Uri;

/**
 * Rich address book data constants
 * 
 * @author jexa7410
 */
public class RichAddressBookData {
	// Database URI
	public static final Uri CONTENT_URI = Uri.parse("content://com.orangelabs.rcs.eab/eab");
	public static final Uri BLACKLIST_CONTENT_URI = Uri.parse("content://com.orangelabs.rcs.eab/blacklisted");
	
	// Common column names for EAB and blacklist table
	// -------------------------------------------------------------

	public static final String KEY_ID = "_id";
	public static final String KEY_CONTACT_ID = "contact_id";
	public static final String KEY_CONTACT_NUMBER = "contact_number";
	
	// Column names for EAB table
	// -------------------------------------------------------------

	public static final String KEY_TIMESTAMP = "timestamp";
	public static final String KEY_CS_VIDEO_SUPPORTED = "cs_video_supported";
	public static final String KEY_IMAGE_SHARING_SUPPORTED = "image_sharing_supported";
	public static final String KEY_VIDEO_SHARING_SUPPORTED = "video_sharing_supported";
	public static final String KEY_IM_SESSION_SUPPORTED = "im_session_supported";
	public static final String KEY_FILE_TRANSFER_SUPPORTED = "file_transfer_supported";
	public static final String KEY_PRESENCE_SHARING_STATUS = "presence_sharing_status";
	public static final String KEY_HYPER_AVAILABILITY_FLAG = "hyper_availability_flag";
	public static final String KEY_FREE_TEXT = "free_text";
	public static final String KEY_FAVORITE_LINK_URL = "favorite_link_url";
	public static final String KEY_FAVORITE_LINK_NAME = "favorite_link_name";
	public static final String KEY_PHOTO_EXIST_FLAG = "photo_exist_flag";
	public static final String KEY_PHOTO_ETAG = "photo_etag";
	public static final String KEY_PHOTO_DATA = "_data";
	public static final String KEY_GEOLOC_EXIST_FLAG = "geoloc_exist_flag";
	public static final String KEY_GEOLOC_LATITUDE = "geoloc_latitude";
	public static final String KEY_GEOLOC_LONGITUDE = "geoloc_longitude";
	public static final String KEY_GEOLOC_ALTITUDE = "geoloc_altitude";
	
	// Column names for blacklist table
	// -------------------------------------------------------------

	public static final String KEY_BLACKLIST_STATUS = "blacklist_status";
	
	// Common column indexes
	// -------------------------------------------------------------

	public static final int COLUMN_KEY_ID = 0;
	public static final int COLUMN_CONTACT_ID = 1;
	public static final int COLUMN_CONTACT_NUMBER = 2;
	
	// Column indexes for EAB table
	// -------------------------------------------------------------

	public static final int COLUMN_TIMESTAMP = 3;
	public static final int COLUMN_CS_VIDEO_SUPPORTED = 4;
	public static final int COLUMN_IMAGE_SHARING_SUPPORTED = 5;
	public static final int COLUMN_VIDEO_SHARING_SUPPORTED = 6;
	public static final int COLUMN_IM_SESSION_SUPPORTED = 7;
	public static final int COLUMN_FILE_TRANSFER_SUPPORTED = 8;
	public static final int COLUMN_PRESENCE_SHARING_STATUS = 9;
	public static final int COLUMN_HYPER_AVAILABILITY_FLAG = 10;
	public static final int COLUMN_FREE_TEXT = 11;
	public static final int COLUMN_FAVORITE_LINK_URL = 12;
	public static final int COLUMN_FAVORITE_LINK_NAME = 13;
	public static final int COLUMN_PHOTO_EXIST_FLAG = 14;
	public static final int COLUMN_PHOTO_ETAG = 15;
	public static final int COLUMN_PHOTO_DATA = 16;
	public static final int COLUMN_GEOLOC_EXIST_FLAG = 17;
	public static final int COLUMN_GEOLOC_LATITUDE = 18;
	public static final int COLUMN_GEOLOC_LONGITUDE = 19;
	public static final int COLUMN_GEOLOC_ALTITUDE = 20;
	
	// Column indexes for blacklist table
	// -------------------------------------------------------------

	public static final int COLUMN_BLACKLIST_STATUS = 3;
	
	// Common values
	// -------------------------------------------------------------
	
	// Contact id for the end user row
	public static final int END_USER_ROW_CONTACT_ID = -5;
	
	// Boolean value
	public static final String TRUE_VALUE = Boolean.toString(true);
	public static final String FALSE_VALUE = Boolean.toString(false);
	
	// Blacklist values
	public static final int REVOKED_VALUE = 1;
	public static final int BLOCKED_VALUE = 2;
}
