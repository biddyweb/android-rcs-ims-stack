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

import com.orangelabs.rcs.core.ims.service.presence.PresenceInfo;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Rich address book provider
 *
 * <br>This provider contains all RCS infos for the end user and its RCS contacts.
 * <br>When the user changes his account (by changing it manually or by switching SIM cards), we backup the current data in a table named eab_contact_for_"givenaccount".
 * <br>When he later switches back to this account, we restore these data to the eab_contacts table.
 * <br>All the queries done by users are affecting the eab_contacts table (representing the current end user account), not the others.
 *  
 */
public class RichAddressBookProvider extends ContentProvider {
	// Database table
	public static String TABLE = "eab_contacts";
	
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
    private DatabaseHelper openHelper;
    
    /**
     * Instance
     */
    private static RichAddressBookProvider instance;

	/**
	 * The logger
	 */
	private Logger logger = Logger.getLogger(this.getClass().getName());
    
    /**
     * Helper class for opening, creating and managing database version control
     */
	private static class DatabaseHelper extends SQLiteOpenHelper{
		private static final String DATABASE_NAME = "eab.db";
		private static final int DATABASE_VERSION = 7;
		
        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
            
		@Override
		public void onCreate(SQLiteDatabase db) {
			// Create the eab_contacts table
			createDb(db);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// Get all tables
			final String SQL_GET_ALL_TABLES = "SELECT name FROM " + "sqlite_master WHERE type='table' ORDER BY name";
			Cursor cursor = db.rawQuery(SQL_GET_ALL_TABLES,null);
			// Cursor contains all table names
			while (cursor.moveToNext()){
				String tableName = cursor.getString(0);
				// We want to drop all tables that starts with "eab_contacts"
				if (tableName.startsWith(TABLE)){
					Cursor cursor2 = db.rawQuery("DROP TABLE " + tableName, null);
					cursor2.close();
				}
			}
			cursor.close();
			// Create the eab_contacts table
			createDb(db);
		}
		
		private void createDb(SQLiteDatabase db){
			db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE + " ("
					+ RichAddressBookData.KEY_ID + " integer primary key autoincrement, "
					+ RichAddressBookData.KEY_CONTACT_NUMBER + " TEXT, "
					+ RichAddressBookData.KEY_TIMESTAMP + " long, "
					+ RichAddressBookData.KEY_CS_VIDEO_SUPPORTED + " integer, "
					+ RichAddressBookData.KEY_IMAGE_SHARING_SUPPORTED + " integer, "
					+ RichAddressBookData.KEY_VIDEO_SHARING_SUPPORTED + " integer, "
					+ RichAddressBookData.KEY_IM_SESSION_SUPPORTED + " integer, "
					+ RichAddressBookData.KEY_FILE_TRANSFER_SUPPORTED + " integer, "
					+ RichAddressBookData.KEY_PRESENCE_SHARING_STATUS + " TEXT, "
					+ RichAddressBookData.KEY_AVAILABILITY_STATUS+ " TEXT, "
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
		
	}
	
	/**
	 * Set the current active user
	 * <br>It will create the corresponding table if it is not already present 
	 * 
	 * @param currentUserAccount The account we now want to use
	 * @param lastUserAccount The account we used before
	 */
	public void setCurrentUser(String currentUserAccount, String lastUserAccount){

		if (logger.isActivated()){
    		logger.debug("Setting current user to : " + currentUserAccount + ", changing from : "+lastUserAccount);
    	}
		
		if (currentUserAccount==null || currentUserAccount.equalsIgnoreCase(lastUserAccount)){
			// We could not get the current end user accound or did not change it, so there is nothing to do
			if (logger.isActivated()){
				logger.debug("No changes done to current user");
			}
			return;
		}
		
		SQLiteDatabase db = openHelper.getWritableDatabase();
		
		// Name of the table having data we want to restore
		String dataRetrievalTableName = TABLE + "_for_"+currentUserAccount;
		
		// Backup the data if we had a previously selected account
		if (lastUserAccount!=null){
			// Name of the table where we will save the current data
			String dataBackupTableName = TABLE + "_for_"+lastUserAccount;

			// Save the eab_contacts (TABLE) table to the dataBackupTable 
			db.execSQL("CREATE TABLE " + dataBackupTableName + " AS SELECT * from " + TABLE + " ;");
		}
		
		// Delete the eab_contacts (TABLE) table, it will be recreated, either from backup or from scratch
		db.execSQL("DROP TABLE "+TABLE+";");
		
		// Restore last data from the DataRetrievalTable if there is some
		Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE name='"+dataRetrievalTableName+"' ", null);
		
		if (cursor.getCount()>0){
			// The table dataRetrievalTable exists, so there is data to restore
			if (logger.isActivated()){
	    		logger.debug("New user is " + currentUserAccount + " and had already some data, we use it");
	    	}	
			// The table existed, so we copy it to TABLE
			db.execSQL("CREATE TABLE "+TABLE+" AS SELECT * from "+ dataRetrievalTableName + " ;");
			// We drop the backup table which is no more needed
			db.execSQL("DROP TABLE " + dataRetrievalTableName + " ;");
		} else {
			if (logger.isActivated()){
	    		logger.debug("New user is " + currentUserAccount + " and did not have any data, we create a new table");
	    	}	
			// The table did not exist, we create it from scratch
			db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE + " ("
					+ RichAddressBookData.KEY_ID + " integer primary key autoincrement, "
					+ RichAddressBookData.KEY_CONTACT_NUMBER + " TEXT, "
					+ RichAddressBookData.KEY_TIMESTAMP + " long, "
					+ RichAddressBookData.KEY_CS_VIDEO_SUPPORTED + " integer, "
					+ RichAddressBookData.KEY_IMAGE_SHARING_SUPPORTED + " integer, "
					+ RichAddressBookData.KEY_VIDEO_SHARING_SUPPORTED + " integer, "
					+ RichAddressBookData.KEY_IM_SESSION_SUPPORTED + " integer, "
					+ RichAddressBookData.KEY_FILE_TRANSFER_SUPPORTED + " integer, "
					+ RichAddressBookData.KEY_PRESENCE_SHARING_STATUS + " TEXT, "
					+ RichAddressBookData.KEY_AVAILABILITY_STATUS+ " TEXT, "
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
			
			// Add a "Me" item
			ContentValues values = new ContentValues();
			values.put(RichAddressBookData.KEY_CONTACT_NUMBER, RichAddressBookData.END_USER_ROW_CONTACT_NUMBER);
			values.put(RichAddressBookData.KEY_AVAILABILITY_STATUS, PresenceInfo.ONLINE);
			values.put(RichAddressBookData.KEY_TIMESTAMP, System.currentTimeMillis());       	
			values.put(RichAddressBookData.KEY_CS_VIDEO_SUPPORTED, RichAddressBookData.TRUE_VALUE);       	
			values.put(RichAddressBookData.KEY_IMAGE_SHARING_SUPPORTED, RichAddressBookData.TRUE_VALUE);       	
			values.put(RichAddressBookData.KEY_VIDEO_SHARING_SUPPORTED, RichAddressBookData.TRUE_VALUE);       	
			values.put(RichAddressBookData.KEY_IM_SESSION_SUPPORTED, RichAddressBookData.TRUE_VALUE);       	
			values.put(RichAddressBookData.KEY_FILE_TRANSFER_SUPPORTED, RichAddressBookData.TRUE_VALUE);       	
			values.put(RichAddressBookData.KEY_TIMESTAMP, System.currentTimeMillis());       	
			insert(RichAddressBookData.CONTENT_URI, values);
		}
		cursor.close();
	}
	
	/**
	 * Get instance
	 */
	public static RichAddressBookProvider getInstance(){
		return instance;
	}

	@Override 
	public boolean onCreate() {
        openHelper = new DatabaseHelper(getContext());
		instance = this;
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
