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

import android.net.Uri;

/**
 * Rich address book data constants
 * 
 * @author jexa7410
 */
public class RichAddressBookData {
	// Database URI
	public static final Uri CONTENT_URI = Uri.parse("content://com.orangelabs.rcs.eab/eab");
	
	// Common column names
	// -------------------------------------------------------------

	public static final String KEY_ID = "_id";
	public static final String KEY_CONTACT_NUMBER = "contact_number";
	public static final String KEY_TIMESTAMP = "timestamp";
	public static final String KEY_CS_VIDEO_SUPPORTED = "cs_video_supported";
	public static final String KEY_IMAGE_SHARING_SUPPORTED = "image_sharing_supported";
	public static final String KEY_VIDEO_SHARING_SUPPORTED = "video_sharing_supported";
	public static final String KEY_IM_SESSION_SUPPORTED = "im_session_supported";
	public static final String KEY_FILE_TRANSFER_SUPPORTED = "file_transfer_supported";
	public static final String KEY_PRESENCE_SHARING_STATUS = "presence_sharing_status";
	public static final String KEY_AVAILABILITY_STATUS = "availability_status";
	public static final String KEY_FREE_TEXT = "free_text";
	public static final String KEY_FAVORITE_LINK_URL = "favorite_link_url";
	public static final String KEY_FAVORITE_LINK_NAME = "favorite_link_name";
	public static final String KEY_WEBLINK_UPDATED_FLAG = "weblink_updated_flag";
	public static final String KEY_PHOTO_EXIST_FLAG = "photo_exist_flag";
	public static final String KEY_PHOTO_ETAG = "photo_etag";
	public static final String KEY_PHOTO_DATA = "_data";
	public static final String KEY_GEOLOC_EXIST_FLAG = "geoloc_exist_flag";
	public static final String KEY_GEOLOC_LATITUDE = "geoloc_latitude";
	public static final String KEY_GEOLOC_LONGITUDE = "geoloc_longitude";
	public static final String KEY_GEOLOC_ALTITUDE = "geoloc_altitude";
	
	
	// Common column indexes
	// -------------------------------------------------------------

	public static final int COLUMN_KEY_ID = 0;
	public static final int COLUMN_CONTACT_NUMBER = 1;
	public static final int COLUMN_TIMESTAMP = 2;
	public static final int COLUMN_CS_VIDEO_SUPPORTED = 3;
	public static final int COLUMN_IMAGE_SHARING_SUPPORTED = 4;
	public static final int COLUMN_VIDEO_SHARING_SUPPORTED = 5;
	public static final int COLUMN_IM_SESSION_SUPPORTED = 6;
	public static final int COLUMN_FILE_TRANSFER_SUPPORTED = 7;
	public static final int COLUMN_PRESENCE_SHARING_STATUS = 8;
	public static final int COLUMN_AVAILABILITY_STATUS = 9;
	public static final int COLUMN_FREE_TEXT = 10;
	public static final int COLUMN_FAVORITE_LINK_URL = 11;
	public static final int COLUMN_FAVORITE_LINK_NAME = 12;
	public static final int COLUMN_WEBLINK_UPDATED_FLAG = 13;
	public static final int COLUMN_PHOTO_EXIST_FLAG = 14;
	public static final int COLUMN_PHOTO_ETAG = 15;
	public static final int COLUMN_PHOTO_DATA = 16;
	public static final int COLUMN_GEOLOC_EXIST_FLAG = 17;
	public static final int COLUMN_GEOLOC_LATITUDE = 18;
	public static final int COLUMN_GEOLOC_LONGITUDE = 19;
	public static final int COLUMN_GEOLOC_ALTITUDE = 20;
	
	// Common values
	// -------------------------------------------------------------
	
	// Contact number for the end user row
	public static final int END_USER_ROW_CONTACT_NUMBER = -5;
	
	// Status values
	public static final String STATUS_REVOKED = "revoked";
	public static final String STATUS_BLOCKED = "blocked";
	public static final String STATUS_INVITED = "pending_out";
	public static final String STATUS_WILLING = "pending";
	public static final String STATUS_ACTIVE = "active";
	public static final String STATUS_CANCELLED = "cancelled";
	
	// Boolean value
	public static final String TRUE_VALUE = Boolean.toString(true);
	public static final String FALSE_VALUE = Boolean.toString(false);
	
}
