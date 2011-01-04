package com.orangelabs.rcs.provider;

import com.orangelabs.rcs.provider.messaging.RichMessagingData;
import com.orangelabs.rcs.provider.messaging.RichMessagingProvider;
import com.orangelabs.rcs.provider.sharing.RichCallData;
import com.orangelabs.rcs.provider.sharing.RichCallProvider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import android.database.sqlite.SQLiteOpenHelper;

public class RichProviderHelper extends SQLiteOpenHelper{
	private static final String DATABASE_NAME = "eventlog.db";
	private static final int DATABASE_VERSION = 2;

	@Override
	public void onCreate(SQLiteDatabase db){
		db.execSQL("create table " + RichMessagingProvider.TABLE + " ("
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
				+ RichMessagingData.KEY_DOWNLOADED_SIZE + " long, "
				+ RichMessagingData.KEY_NUMBER_MESSAGES+ " integer);"
				);
		
		db.execSQL("create table " + RichCallProvider.TABLE + " ("
				+ RichCallData.KEY_ID + " integer primary key, "
				+ RichCallData.KEY_CONTACT_NUMBER + " TEXT, "
				+ RichCallData.KEY_DESTINATION + " integer, "
				+ RichCallData.KEY_MIME_TYPE + " TEXT, "
				+ RichCallData.KEY_NAME + " TEXT, "
				+ RichCallData.KEY_SIZE + " long, "
				+ RichCallData.KEY_DATA + " TEXT, "
				+ RichCallData.KEY_TRANSFER_DATE + " long,"
				+ RichCallData.KEY_NUMBER_MESSAGES + " integer,"
				+ RichCallData.KEY_STATUS + " integer,"
				+ RichCallData.KEY_SESSION_ID+ " TEXT);"
				);
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
		db.execSQL("DROP TABLE IF EXISTS " + RichMessagingProvider.TABLE);
		db.execSQL("DROP TABLE IF EXISTS " + RichCallProvider.TABLE);
		onCreate(db);
	}
	
	
	/**
	 * To manage an unique instance.
	 */
	private static RichProviderHelper instance = null;
	
	public static synchronized void createInstance(Context ctx) {
		if (instance == null) {
			instance = new RichProviderHelper(ctx);
		}
	}
	
	public static RichProviderHelper getInstance() {
		return instance;
	}

	private RichProviderHelper(Context context) {
		 super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
}
