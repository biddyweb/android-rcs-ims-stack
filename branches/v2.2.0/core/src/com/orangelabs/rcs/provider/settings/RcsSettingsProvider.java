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
package com.orangelabs.rcs.provider.settings;

import com.orangelabs.rcs.R;
import java.util.ArrayList;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

/**
 * RCS settings provider
 * 
 * @author jexa7410
 */
public class RcsSettingsProvider extends ContentProvider {
	/**
	 * Database table
	 */
	private static final String TABLE = "settings";

	/**
	 * Boolean value "true"
	 */
	private static final String TRUE = Boolean.toString(true);

	/**
	 * Boolean value "false"
	 */
	private static final String FALSE = Boolean.toString(false);
	
	/**
	 * Read only parameter
	 */
	private static final int READONLY = 0;
	
	/**
	 * Read/write parameter
	 */
	private static final int READWRITE = 1;

	/**
	 * No reboot after provisionning
	 */
	private static final int NO_REBOOT = 0;
	
	/**
	 * Reboot after provisionning
	 */
	private static final int REBOOT = 1;

	// Create the constants used to differentiate between the different URI requests
	private static final int SETTINGS = 1;
    private static final int SETTINGS_ID = 2;
    
	// Allocate the UriMatcher object, where a URI ending in 'settings'
	// will correspond to a request for all settings, and 'settings'
	// with a trailing '/[rowID]' will represent a single settings row.
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        uriMatcher.addURI("com.orangelabs.rcs.settings", "settings", SETTINGS);
        uriMatcher.addURI("com.orangelabs.rcs.settings", "settings/#", SETTINGS_ID);
    }

    /**
     * Database helper class
     */
    private SQLiteOpenHelper openHelper;

    /**
     * Helper class for opening, creating and managing database version control
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "rcs_settings.db";
        private static final int DATABASE_VERSION = 18;

        private Context ctx;
        
        public DatabaseHelper(Context ctx) {
            super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
            
            this.ctx = ctx;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {        	
        	db.execSQL("CREATE TABLE " + TABLE + " ("
        			+ RcsSettingsData.KEY_ID + " integer primary key autoincrement,"
                    + RcsSettingsData.KEY_KEY + " TEXT,"
                    + RcsSettingsData.KEY_VALUE + " TEXT,"
                    + RcsSettingsData.KEY_READ_ONLY + " integer,"
                    + RcsSettingsData.KEY_REBOOT + " integer);");

            // Insert default values for parameters
        	
            // UI parameters
            addParameter(db, RcsSettingsData.SERVICE_ACTIVATED, 				TRUE, READWRITE, NO_REBOOT);
            addParameter(db, RcsSettingsData.ROAMING_AUTHORIZED, 				TRUE, READWRITE, NO_REBOOT);            
            addParameter(db, RcsSettingsData.PRESENCE_INVITATION_RINGTONE, 		"", READWRITE, NO_REBOOT);
            addParameter(db, RcsSettingsData.PRESENCE_INVITATION_VIBRATE, 		TRUE, READWRITE, NO_REBOOT);
            addParameter(db, RcsSettingsData.CSH_INVITATION_RINGTONE, 			"", READWRITE, NO_REBOOT);
            addParameter(db, RcsSettingsData.CSH_INVITATION_VIBRATE, 			TRUE, READWRITE, NO_REBOOT);
            addParameter(db, RcsSettingsData.CSH_AVAILABLE_BEEP, 				TRUE, READWRITE, NO_REBOOT);
            addParameter(db, RcsSettingsData.CSH_VIDEO_FORMAT, 					ctx.getString(R.string.rcs_settings_label_default_video_format), READWRITE, NO_REBOOT);
            addParameter(db, RcsSettingsData.CSH_VIDEO_SIZE, 					ctx.getString(R.string.rcs_settings_label_default_video_size), READWRITE, NO_REBOOT);
            addParameter(db, RcsSettingsData.FILETRANSFER_INVITATION_RINGTONE, 	"", READWRITE, NO_REBOOT);
            addParameter(db, RcsSettingsData.FILETRANSFER_INVITATION_VIBRATE, 	TRUE, READWRITE, NO_REBOOT);
            addParameter(db, RcsSettingsData.CHAT_INVITATION_RINGTONE, 			"", READWRITE, NO_REBOOT);
            addParameter(db, RcsSettingsData.CHAT_INVITATION_VIBRATE, 			TRUE, READWRITE, NO_REBOOT);
            addParameter(db, RcsSettingsData.CHAT_INVITATION_AUTO_ACCEPT, 		TRUE, READWRITE, NO_REBOOT);
            addParameter(db, RcsSettingsData.FREETEXT1, 						ctx.getString(R.string.rcs_settings_label_default_freetext_1), READWRITE, NO_REBOOT);
            addParameter(db, RcsSettingsData.FREETEXT2, 						ctx.getString(R.string.rcs_settings_label_default_freetext_2), READWRITE, NO_REBOOT);
            addParameter(db, RcsSettingsData.FREETEXT3,							ctx.getString(R.string.rcs_settings_label_default_freetext_3), READWRITE, NO_REBOOT);
            addParameter(db, RcsSettingsData.FREETEXT4,							ctx.getString(R.string.rcs_settings_label_default_freetext_4), READWRITE, NO_REBOOT);

            // UI read only parameters
            addParameter(db, RcsSettingsData.MAX_RCS_CONTACTS, 					"20", READONLY, NO_REBOOT);
            addParameter(db, RcsSettingsData.MAX_PHOTO_ICON_SIZE, 				"256", READONLY, NO_REBOOT);
            addParameter(db, RcsSettingsData.MAX_FREETXT_LENGTH, 				"100", READONLY, NO_REBOOT);
            addParameter(db, RcsSettingsData.MAX_CHAT_PARTICIPANTS, 			"10", READONLY, NO_REBOOT);
            addParameter(db, RcsSettingsData.MAX_CHAT_MSG_LENGTH, 				"100", READONLY, NO_REBOOT);
            addParameter(db, RcsSettingsData.CHAT_IDLE_DURATION, 				"120", READONLY, NO_REBOOT);
            addParameter(db, RcsSettingsData.MAX_FILE_TRANSFER_SIZE, 			"2048", READONLY, NO_REBOOT);
            addParameter(db, RcsSettingsData.MAX_IMAGE_SHARE_SIZE, 				"2048", READONLY, NO_REBOOT);
            addParameter(db, RcsSettingsData.MAX_VIDEO_SHARE_DURATION, 			"600", READONLY, NO_REBOOT);
            addParameter(db, RcsSettingsData.MAX_CHAT_SESSIONS, 				"5", READONLY, NO_REBOOT);
            addParameter(db, RcsSettingsData.MAX_FILE_TRANSFER_SESSIONS, 		"1", READONLY, NO_REBOOT);
            addParameter(db, RcsSettingsData.ANONYMOUS_FETCH_SERVICE, 			TRUE, READONLY, NO_REBOOT);
            addParameter(db, RcsSettingsData.CHAT_SERVICE, 						TRUE, READONLY, NO_REBOOT);
            addParameter(db, RcsSettingsData.SMS_FALLBACK_SERVICE, 				TRUE, READONLY, NO_REBOOT);

            // Stack parameters
            addParameter(db, RcsSettingsData.USERPROFILE_IMS_USERNAME, 			"", READWRITE, REBOOT);
            addParameter(db, RcsSettingsData.USERPROFILE_IMS_DISPLAY_NAME, 		"", READWRITE, REBOOT);
            addParameter(db, RcsSettingsData.USERPROFILE_IMS_PRIVATE_ID, 		"", READWRITE, REBOOT);
            addParameter(db, RcsSettingsData.USERPROFILE_IMS_PASSWORD, 			"", READWRITE, REBOOT);
            addParameter(db, RcsSettingsData.USERPROFILE_IMS_HOME_DOMAIN, 		"", READWRITE, REBOOT);
		    addParameter(db, RcsSettingsData.USERPROFILE_IMS_PROXY,				"127.0.0.1:5060", READWRITE, REBOOT);
		    addParameter(db, RcsSettingsData.USERPROFILE_XDM_SERVER, 			"127.0.0.1:8080/services", READWRITE, REBOOT);
		    addParameter(db, RcsSettingsData.USERPROFILE_XDM_LOGIN,				"", READWRITE, REBOOT);
		    addParameter(db, RcsSettingsData.USERPROFILE_XDM_PASSWORD, 			"password", READWRITE, REBOOT);
            addParameter(db, RcsSettingsData.USERPROFILE_IM_CONF_URI, 			"Conference-Factory", READWRITE, REBOOT);
            
            // Stack read only parameters
            addParameter(db, RcsSettingsData.IMS_CONNECTION_POLLING_PERIOD, 	"30", READONLY, REBOOT);
            addParameter(db, RcsSettingsData.IMS_SERVICE_POLLING_PERIOD, 		"300", READONLY, REBOOT);
            addParameter(db, RcsSettingsData.SIP_DEFAULT_PORT, 					"5060", READONLY, REBOOT);
            addParameter(db, RcsSettingsData.SIP_DEFAULT_PROTOCOL,				"UDP", READONLY, REBOOT);
            addParameter(db, RcsSettingsData.SIP_TRANSACTION_TIMEOUT, 			"30", READONLY, REBOOT);
            addParameter(db, RcsSettingsData.MSRP_DEFAULT_PORT, 				"20000", READONLY, REBOOT);
            addParameter(db, RcsSettingsData.RTP_DEFAULT_PORT, 					"10000", READONLY, REBOOT);
            addParameter(db, RcsSettingsData.MSRP_TRANSACTION_TIMEOUT, 			"5", READONLY, REBOOT);
            addParameter(db, RcsSettingsData.REGISTER_EXPIRE_PERIOD, 			"3600", READONLY, REBOOT);
            addParameter(db, RcsSettingsData.PUBLISH_EXPIRE_PERIOD, 			"600000", READONLY, REBOOT);
            addParameter(db, RcsSettingsData.ANONYMOUS_FETCH_REFRESH_TIMEOUT, 	"86400", READONLY, REBOOT);
            addParameter(db, RcsSettingsData.REVOKE_TIMEOUT, 					"300", READONLY, REBOOT);
            addParameter(db, RcsSettingsData.IMS_AUTHENT_MODE, 					"DIGEST", READONLY, REBOOT);
            addParameter(db, RcsSettingsData.TEL_URI_FORMAT, 					TRUE, READONLY, REBOOT);
            addParameter(db, RcsSettingsData.RINGING_SESSION_PERIOD, 			"60", READONLY, REBOOT);
            addParameter(db, RcsSettingsData.SUBSCRIBE_EXPIRE_PERIOD, 			"600000", READONLY, REBOOT);
            addParameter(db, RcsSettingsData.IS_COMPOSING_TIMEOUT, 				"15", READONLY, REBOOT);
            addParameter(db, RcsSettingsData.SESSION_REFRESH_EXPIRE_PERIOD, 	"3600", READONLY, REBOOT);
            addParameter(db, RcsSettingsData.RICHCALL_MODE,			 			TRUE, READONLY, REBOOT);
            addParameter(db, RcsSettingsData.PERMANENT_STATE_MODE,	 			TRUE, READONLY, REBOOT);
            addParameter(db, RcsSettingsData.TRACE_ACTIVATION,			 		TRUE, READONLY, REBOOT);
            addParameter(db, RcsSettingsData.TRACE_LEVEL,	 					"DEBUG", READONLY, REBOOT);
            addParameter(db, RcsSettingsData.SIP_TRACE_ACTIVATION, 				FALSE, READONLY, REBOOT);
            addParameter(db, RcsSettingsData.MEDIA_TRACE_ACTIVATION,			FALSE, READONLY, REBOOT);
        }
        
        /**
         * Add a parameter in the database
         *  
         * @param db Database
         * @param key Key
         * @param value Value
         * @param readonly Read only parameter or not
         * @param reboot Reboot needed after parameter update or not
         */
        private void addParameter(SQLiteDatabase db, String key, String value, int readonly, int reboot) {
            String sql = "INSERT INTO " + TABLE + " (" +
            	RcsSettingsData.KEY_KEY + "," +
            	RcsSettingsData.KEY_VALUE + "," +
            	RcsSettingsData.KEY_READ_ONLY + "," +
            	RcsSettingsData.KEY_REBOOT + ") VALUES ('" +
            	key + "','" +
            	value + "'," + 
            	readonly + "," +
            	reboot + ");";
            db.execSQL(sql);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int currentVersion) {
        	// Get old data before deleting the table
        	Cursor oldDataCursor = db.query(TABLE, null, null, null, null, null, null);
    	
        	// Get all the pairs key/value of the old table to insert them back after update      	
        	ArrayList<ContentValues> valuesList = new ArrayList<ContentValues>();
        	while(oldDataCursor.moveToNext()){
        		String key = null;
        		String value = null;
        		int index = oldDataCursor.getColumnIndex(RcsSettingsData.KEY_KEY);
        		if (index!=-1) {
        			key = oldDataCursor.getString(index);
        		}
        		index = oldDataCursor.getColumnIndex(RcsSettingsData.KEY_VALUE);
        		if (index!=-1) {
        			value = oldDataCursor.getString(index);
        		}
        		if (key!=null && value!=null) {
	        		ContentValues values = new ContentValues();
	        		values.put(RcsSettingsData.KEY_KEY, key);
	        		values.put(RcsSettingsData.KEY_VALUE, value);
	        		valuesList.add(values);
        		}
        	}
            oldDataCursor.close();
        	
        	// Delete old table
        	db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        	
            // Recreate table
        	onCreate(db);
        	
        	// Put the old values back when possible
        	for (int i=0; i<valuesList.size();i++) {
        		ContentValues values = valuesList.get(i);
        		String whereClause = RcsSettingsData.KEY_KEY + "=" + "\""+ values.getAsString(RcsSettingsData.KEY_KEY) + "\"";
        		// Update the value with this key in the newly created database
	    		// If key is not present in the new version, this won't do anything
	   			db.update(TABLE, values, whereClause, null);
        	}
        }
    }

    @Override
    public boolean onCreate() {
        openHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        int match = uriMatcher.match(uri);
        switch(match) {
            case SETTINGS:
                return "vnd.android.cursor.dir/com.orangelabs.rcs.settings";
            case SETTINGS_ID:
                return "vnd.android.cursor.item/com.orangelabs.rcs.settings";
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }
    	
    @Override
    public Cursor query(Uri uri, String[] projectionIn, String selection, String[] selectionArgs, String sort) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE);

        // Generate the body of the query
        int match = uriMatcher.match(uri);
        switch(match) {
            case SETTINGS:
                break;
            case SETTINGS_ID:
                qb.appendWhere(RcsSettingsData.KEY_ID + "=");
                qb.appendWhere(uri.getPathSegments().get(1));
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        SQLiteDatabase db = openHelper.getReadableDatabase();
        Cursor c = qb.query(db, projectionIn, selection, selectionArgs, null, null, sort);

		// Register the contexts ContentResolver to be notified if
		// the cursor result set changes.
        if (c != null) {
            c.setNotificationUri(getContext().getContentResolver(), uri);
        }

        return c;
    }
    
    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        int count;
        SQLiteDatabase db = openHelper.getWritableDatabase();

        int match = uriMatcher.match(uri);
        switch (match) {
	        case SETTINGS:
	            count = db.update(TABLE, values, where, null);
	            break;
            case SETTINGS_ID:
                String segment = uri.getPathSegments().get(1);
                int id = Integer.parseInt(segment);
                count = db.update(TABLE, values, RcsSettingsData.KEY_ID + "=" + id, null);
                break;
            default:
                throw new UnsupportedOperationException("Cannot update URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
    
    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        throw new UnsupportedOperationException();
    }
}
