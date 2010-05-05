package com.orangelabs.rcs.provider.settings;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.orangelabs.rcs.R;

/**
 * RCS settings provider
 * 
 * @author jexa7410
 */
public class RcsSettingsProvider extends ContentProvider {
	// Database table
	public static final String TABLE = "settings";

	// Create the constants used to differentiate between the different
	// URI requests
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
        private static final int DATABASE_VERSION = 1;

        private Context ctx;
        
        public DatabaseHelper(Context ctx) {
            super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
            
            this.ctx = ctx;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {        	
        	db.execSQL("CREATE TABLE " + TABLE + " ("
        			+ RcsSettingsData.KEY_ID + " INTEGER PRIMARY KEY,"
                    + RcsSettingsData.KEY_KEY + " TEXT,"
                    + RcsSettingsData.KEY_VALUE + " TEXT);");

            // insert default alarms
            String insertMe = "INSERT INTO " + TABLE + " (key, value) VALUES ";
            db.execSQL(insertMe + "('" + RcsSettingsData.PRESENCE_INVITATION_VIBRATE + "', '" + RcsSettingsData.FALSE_VALUE + "');");
            db.execSQL(insertMe + "('" + RcsSettingsData.PRESENCE_INVITATION_RINGTONE + "', '');");
            db.execSQL(insertMe + "('" + RcsSettingsData.CSH_INVITATION_VIBRATE + "', '" + RcsSettingsData.FALSE_VALUE + "');");
            db.execSQL(insertMe + "('" + RcsSettingsData.CSH_INVITATION_RINGTONE + "', '');");
            db.execSQL(insertMe + "('" + RcsSettingsData.FILETRANSFER_INVITATION_VIBRATE + "', '" + RcsSettingsData.FALSE_VALUE + "');");
            db.execSQL(insertMe + "('" + RcsSettingsData.FILETRANSFER_INVITATION_RINGTONE + "', '');");
            db.execSQL(insertMe + "('" + RcsSettingsData.FREETEXT1 + "', '" + ctx.getString(R.string.default_freetext_1)+ "');");
            db.execSQL(insertMe + "('" + RcsSettingsData.FREETEXT2 + "', '" + ctx.getString(R.string.default_freetext_2)+ "');");
            db.execSQL(insertMe + "('" + RcsSettingsData.FREETEXT3 + "', '" + ctx.getString(R.string.default_freetext_3)+ "');");
            db.execSQL(insertMe + "('" + RcsSettingsData.FREETEXT4 + "', '" + ctx.getString(R.string.default_freetext_4)+ "');");
            
            db.execSQL(insertMe + "('" + RcsSettingsData.USERPROFILE_USERNAME + "', '');");
            db.execSQL(insertMe + "('" + RcsSettingsData.USERPROFILE_DISPLAY_NAME + "', '');");
            db.execSQL(insertMe + "('" + RcsSettingsData.USERPROFILE_PRIVATE_ID + "', '');");
            db.execSQL(insertMe + "('" + RcsSettingsData.USERPROFILE_PASSWORD + "', '');");
            db.execSQL(insertMe + "('" + RcsSettingsData.USERPROFILE_HOME_DOMAIN + "', '');");
            db.execSQL(insertMe + "('" + RcsSettingsData.USERPROFILE_SIP_PROXY + "', '');");
            db.execSQL(insertMe + "('" + RcsSettingsData.USERPROFILE_XDM_SERVER + "', '');");
            db.execSQL(insertMe + "('" + RcsSettingsData.USERPROFILE_XDM_LOGIN + "', '');");
            db.execSQL(insertMe + "('" + RcsSettingsData.USERPROFILE_XDM_PASSWORD + "', '');");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int currentVersion) {
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
