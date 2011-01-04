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

import java.io.FileNotFoundException;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;

import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Rich address book provider
 * 
 * @author jexa7410
 */
public class RichAddressBookProvider extends ContentProvider {
	// Database table
	public static final String TABLE = "eab_contacts";
	
	// Create the constants used to differentiate between the different URI requests
	private static final int CONTACTS = 1;
	private static final int CONTACT_ID = 2;
	
	// Allocate the UriMatcher object, where a URI ending in 'contacts'
	// will correspond to a request for all contacts, and 'contacts'
	// with a trailing '/[rowID]' will represent a single contact row.
	private static final UriMatcher uriMatcher;
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI("com.orangelabs.rcs.eab", "eab", CONTACTS);
		uriMatcher.addURI("com.orangelabs.rcs.eab", "eab/#", CONTACT_ID);
	}

    /**
     * Database helper class
     */
    private SQLiteOpenHelper openHelper;

	/**
	 * The logger
	 */
	private Logger logger = Logger.getLogger(this.getClass().getName());
    
    /**
     * Helper class for opening, creating and managing database version control
     */
	private static class DatabaseHelper extends SQLiteOpenHelper{
		private static final String DATABASE_NAME = "eab.db";
		private static final int DATABASE_VERSION = 4;

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
            
		@Override
		public void onCreate(SQLiteDatabase db) {
			// Create eab table
			db.execSQL("create table " + TABLE + " ("
				+ RichAddressBookData.KEY_ID + " integer primary key autoincrement, "
				+ RichAddressBookData.KEY_CONTACT_NUMBER + " TEXT, "
				+ RichAddressBookData.KEY_TIMESTAMP + " long, "
				+ RichAddressBookData.KEY_CS_VIDEO_SUPPORTED + " integer, "
				+ RichAddressBookData.KEY_IMAGE_SHARING_SUPPORTED + " integer, "
				+ RichAddressBookData.KEY_VIDEO_SHARING_SUPPORTED + " integer, "
				+ RichAddressBookData.KEY_IM_SESSION_SUPPORTED + " integer, "
				+ RichAddressBookData.KEY_FILE_TRANSFER_SUPPORTED + " integer, "
				+ RichAddressBookData.KEY_PRESENCE_SHARING_STATUS + " TEXT, "
				+ RichAddressBookData.KEY_HYPER_AVAILABILITY_FLAG + " TEXT, "
				+ RichAddressBookData.KEY_FREE_TEXT + " TEXT, "
				+ RichAddressBookData.KEY_FAVORITE_LINK_URL + " TEXT, "
				+ RichAddressBookData.KEY_FAVORITE_LINK_NAME + " TEXT, "
				+ RichAddressBookData.KEY_WEBLINK_UPDATED_FLAG + " TEXT, "
	            + RichAddressBookData.KEY_PHOTO_EXIST_FLAG + " TEXT, "
				+ RichAddressBookData.KEY_PHOTO_ETAG + " TEXT, "
	            + RichAddressBookData.KEY_PHOTO_DATA + " TEXT, "
	            + RichAddressBookData.KEY_GEOLOC_EXIST_FLAG + " TEXT, "
	            + RichAddressBookData.KEY_GEOLOC_LATITUDE + " double, "
	            + RichAddressBookData.KEY_GEOLOC_LONGITUDE + " double, "
	            + RichAddressBookData.KEY_GEOLOC_ALTITUDE + " double);");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + TABLE);
			onCreate(db);
		}
	}

	@Override 
	public boolean onCreate() {
        openHelper = new DatabaseHelper(getContext());
		
        // Populate the table with a predefined row for the end user if it does not exist already
        Cursor cursor = query(RichAddressBookData.CONTENT_URI, 
        		new String[]{RichAddressBookData.KEY_CONTACT_NUMBER}, 
        		RichAddressBookData.KEY_CONTACT_NUMBER + " = " + "\"" + RichAddressBookData.END_USER_ROW_CONTACT_NUMBER + "\"", 
        		null, 
        		null);
        if (cursor.getCount() == 0) {
        	if (logger.isActivated()){
        		logger.debug("Add end user profile entry in database with number " + RichAddressBookData.END_USER_ROW_CONTACT_NUMBER);
        	}
        	ContentValues values = new ContentValues();
        	values.put(RichAddressBookData.KEY_CONTACT_NUMBER, RichAddressBookData.END_USER_ROW_CONTACT_NUMBER);
        	values.put(RichAddressBookData.KEY_TIMESTAMP, System.currentTimeMillis());       	
        	insert(RichAddressBookData.CONTENT_URI, values);
        }
        cursor.close();
        
        return true;
	}

	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		int count = 0;

        SQLiteDatabase db = openHelper.getWritableDatabase();
		switch(uriMatcher.match(uri)){
			case CONTACTS:
				count = db.delete(TABLE, where, whereArgs);
				break;

			case CONTACT_ID:
				String segment = uri.getPathSegments().get(1);
				count = db.delete(TABLE, RichAddressBookData.KEY_ID + "="
						+ segment
						+ (!TextUtils.isEmpty(where) ? " AND ("	+ where + ')' : ""),
						whereArgs);
				
				break;

			default:
				throw new IllegalArgumentException("Unsupported URI: " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public String getType(Uri uri) {
		switch(uriMatcher.match(uri)){
			case CONTACTS:
				return "vnd.android.cursor.dir/com.orangelabs.rcs.eab";
			case CONTACT_ID:
				return "vnd.android.cursor.item/com.orangelabs.rcs.eab";
			default:
				throw new IllegalArgumentException("Unsupported URI " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
        SQLiteDatabase db = openHelper.getWritableDatabase();
        switch(uriMatcher.match(uri)){
	        case CONTACTS:
	        case CONTACT_ID:
	            // Insert the new row, will return the row number if successful
	    		long rowID = db.insert(TABLE, null, initialValues);

	    		// Return a URI to the newly inserted row on success
	    		if (rowID > 0) {
	    			if (!initialValues.containsKey(RichAddressBookData.KEY_PHOTO_DATA)){
	    				try{
	    					String filename = "photoData" + rowID;
	    					getContext().openFileOutput(filename, Context.MODE_PRIVATE).close();
	    					String path = getContext().getFileStreamPath(filename).getAbsolutePath();
	    					initialValues.put(RichAddressBookData.KEY_PHOTO_DATA, path);
	    					initialValues.put(RichAddressBookData.KEY_PHOTO_EXIST_FLAG, RichAddressBookData.FALSE_VALUE);
	    				}catch(Exception e){
	    					if (logger.isActivated()){
	    						logger.error("Problem while creating photoData", e);
	    					}
	    				}
	    			}
	    		    rowID = db.update(TABLE, initialValues, RichAddressBookData.KEY_ID + "="+rowID, null);
	    		    Uri newUri = ContentUris.withAppendedId(RichAddressBookData.CONTENT_URI, rowID);
	    		    getContext().getContentResolver().notifyChange(newUri, null);
	    		    return newUri;
	    		}
	        	
	        	break;
	        
        }
        
		
		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sort) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        // Generate the body of the query
        int match = uriMatcher.match(uri);
        switch(match) {
        	case CONTACTS:
        		qb.setTables(TABLE);
        		break;
	        case CONTACT_ID:
	        	qb.setTables(TABLE);
				qb.appendWhere(RichAddressBookData.KEY_ID + "=" + uri.getPathSegments().get(1));
	            break;
	        default:
	            throw new IllegalArgumentException("Unknown URI " + uri);
		}

		// If no sort order is specified sort by contact id
		String orderBy;
		if (TextUtils.isEmpty(sort)){
			orderBy = RichAddressBookData.KEY_CONTACT_NUMBER;
		} else {
			orderBy = sort;
		}

		// Apply the query to the underlying database.
        SQLiteDatabase db = openHelper.getWritableDatabase();
		Cursor c = qb.query(db, 
				projection, 
				selection, selectionArgs, 
				null, null,
				orderBy);

		// Register the contexts ContentResolver to be notified if
		// the cursor result set changes.
        if (c != null) {
			c.setNotificationUri(getContext().getContentResolver(), uri);
        }
        
		// Return a cursor to the query result
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
		int count = 0;
        SQLiteDatabase db = openHelper.getWritableDatabase();

        int match = uriMatcher.match(uri);
        switch (match) {
			case CONTACTS:
				count = db.update(TABLE, values, where, whereArgs);
				break;
			case CONTACT_ID:
				String segment = uri.getPathSegments().get(1);
				count = db.update(TABLE, values, RichAddressBookData.KEY_ID + "="
						+ segment
						+ (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""),
						whereArgs);
				break;
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
		if (uriMatcher.match(uri) != CONTACT_ID) {
			throw new IllegalArgumentException("URI not supported for directories");
		}
		
		try {
			return this.openFileHelper(uri, mode);
		} catch (FileNotFoundException e) {
			if (logger.isActivated()) {
				logger.error("File not found exception", e);
			}
			throw new FileNotFoundException();
		}
	} 
}
