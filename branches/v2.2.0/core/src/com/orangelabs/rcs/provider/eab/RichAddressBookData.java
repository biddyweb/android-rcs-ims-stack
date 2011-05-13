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
	public static final String KEY_PRESENCE_SHARING_STATUS = "presence_sharing_status";
	public static final String KEY_TIMESTAMP = "timestamp";
	
	// Common column indexes
	// -------------------------------------------------------------

	public static final int COLUMN_KEY_ID = 0;
	public static final int COLUMN_CONTACT_NUMBER = 1;
	public static final int COLUMN_PRESENCE_SHARING_STATUS = 2;
	
	// Common values
	// -------------------------------------------------------------
	
	// Status values
	public static final String STATUS_REVOKED = "revoked";
	public static final String STATUS_BLOCKED = "blocked";
	public static final String STATUS_INVITED = "pending_out";
	public static final String STATUS_WILLING = "pending";
	public static final String STATUS_ACTIVE = "active";
	public static final String STATUS_CANCELLED = "cancelled";
	public static final String STATUS_RCS_CAPABLE = "rcs_capable";
	public static final String STATUS_NOT_RCS = "not_rcs";
	
}
