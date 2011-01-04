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
package com.orangelabs.rcs.provider.messaging;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

/**
 * Rich messaging provider
 * 
 * @author jexa7410
 */
public class RichMessagingProvider extends ContentProvider {
	// Database table
	public static final String TABLE = "messaging";
	
	// Create the constants used to differentiate between the different
	// URI requests
	private static final int MESSAGING = 1;
	private static final int MESSAGING_ID = 2;
	
	// Allocate the UriMatcher object, where a URI ending in 'contacts'
	// will correspond to a request for all contacts, and 'contacts'
	// with a trailing '/[rowID]' will represent a single contact row.
	private static final UriMatcher uriMatcher;
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI("com.orangelabs.rcs.messaging", "messaging", MESSAGING);
		uriMatcher.addURI("com.orangelabs.rcs.messaging", "messaging/#", MESSAGING_ID);
	}

    /**
     * Database helper class
     */
    private SQLiteOpenHelper openHelper;

    /**
     * Helper class for opening, creating and managing database version control
     */
	private static class DatabaseHelper extends SQLiteOpenHelper{
		private static final String DATABASE_NAME = "messaging.db";
		private static final int DATABASE_VERSION = 3;

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
            
		@Override
		public void onCreate(SQLiteDatabase db){
			// Create eab table
			// Some fields are not used for column consistancy when doing a cursor merge with android.providers.telephony
			db.execSQL("create table " + TABLE + " ("
				+ RichMessagingData.KEY_ID + " integer primary key, "
				+ RichMessagingData.KEY_TYPE_DISCRIMINATOR + " integer, "
				+ RichMessagingData.KEY_SESSION_ID + " TEXT, "
				+ RichMessagingData.KEY_FT_SESSION_ID + " TEXT, "
				+ RichMessagingData.KEY_CONTACT_NUMBER + " TEXT, "
				+ RichMessagingData.KEY_DESTINATION + " integer, "
				+ RichMessagingData.KEY_MIME_TYPE + " TEXT, "
				+ RichMessagingData.KEY_NAME + " TEXT, "
				+ RichMessagingData.KEY_SIZE + " integer, "
				+ RichMessagingData.KEY_DATA + " TEXT, "
				+ RichMessagingData.KEY_TRANSFER_STATUS + " integer, "
				+ RichMessagingData.KEY_TRANSFER_DATE + " long, "
				+ RichMessagingData.KEY_DOWNLOADED_SIZE + " long);"
				);
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
        return true;
	}

	@Override
	public String getType(Uri uri) {
		switch(uriMatcher.match(uri)){
			case MESSAGING:
				return "vnd.android.cursor.dir/com.orangelabs.rcs.messaging";
			case MESSAGING_ID:
				return "vnd.android.cursor.item/com.orangelabs.rcs.messaging";
			default:
				throw new IllegalArgumentException("Unsupported URI " + uri);
		}
	}
	
    @Override
    public Cursor query(Uri uri, String[] projectionIn, String selection, String[] selectionArgs, String sort) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE);

        // Generate the body of the query
        int match = uriMatcher.match(uri);
        switch(match) {
            case MESSAGING:
                break;
            case MESSAGING_ID:
                qb.appendWhere(RichMessagingData.KEY_ID + "=" + uri.getPathSegments().get(1));
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        SQLiteDatabase db = openHelper.getReadableDatabase();
        Cursor c = qb.query(db, projectionIn, selection, selectionArgs, null, null, sort);

		// Register the contexts ContentResolver to be notified if
		// the cursor result set changes.
       // if (c != null) {
            c.setNotificationUri(getContext().getContentResolver(), RichMessagingData.CONTENT_URI);
       // }

        return c;
    }
    
    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        int count;
        SQLiteDatabase db = openHelper.getWritableDatabase();

        int match = uriMatcher.match(uri);
        switch (match) {
	        case MESSAGING:
	            count = db.update(TABLE, values, where, null);
	            break;
            case MESSAGING_ID:
                String segment = uri.getPathSegments().get(1);
                int id = Integer.parseInt(segment);
                count = db.update(TABLE, values, RichMessagingData.KEY_ID + "=" + id, null);
                break;
            default:
                throw new UnsupportedOperationException("Cannot update URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
    
    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        SQLiteDatabase db = openHelper.getWritableDatabase();
        switch(uriMatcher.match(uri)){
	        case MESSAGING:
	        case MESSAGING_ID:
	            // Insert the new row, will return the row number if successful
	        	// Use system clock to generate id : it should not be a common int otherwise it could be the 
	        	// same as an id present in MmsSms table (and that will create uniqueness problem when doing the tables merge) 
	        	int id = (int)System.currentTimeMillis();
	        	if (Integer.signum(id) == -1){
	        		// If generated id is <0, it is problematic for uris
	        		id = -id;
	        	}
	        	initialValues.put(RichMessagingData.KEY_ID, id);
	        	initialValues.put(RichMessagingData.KEY_DOWNLOADED_SIZE,0);
	    		db.insert(TABLE, null, initialValues);
	        	break;
	        default:
	    		throw new SQLException("Failed to insert row into " + uri);
        }
		getContext().getContentResolver().notifyChange(uri, null);
        return uri;
    }
    
    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db = openHelper.getWritableDatabase();
        int count = 0;
        switch(uriMatcher.match(uri)){
	        case MESSAGING:
	        	count = db.delete(TABLE, where, whereArgs);
	        	break;
	        case MESSAGING_ID:
	        	String segment = uri.getPathSegments().get(1);
				count = db.delete(TABLE, RichMessagingData.KEY_ID + "="
						+ segment
						+ (!TextUtils.isEmpty(where) ? " AND ("	+ where + ')' : ""),
						whereArgs);
				
				break;
	        	
	        default:
	    		throw new SQLException("Failed to delete row " + uri);
        }
		getContext().getContentResolver().notifyChange(uri, null);
        return count;    
   }	
    
}
