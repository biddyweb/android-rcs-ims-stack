package com.orangelabs.rcs.provider.eventlogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.orangelabs.rcs.provider.RichProviderHelper;
import com.orangelabs.rcs.provider.messaging.RichMessagingData;
import com.orangelabs.rcs.provider.messaging.RichMessagingProvider;
import com.orangelabs.rcs.provider.sharing.RichCallData;
import com.orangelabs.rcs.provider.sharing.RichCallProvider;
import com.orangelabs.rcs.utils.MimeManager;
import com.orangelabs.rcs.utils.PhoneUtils;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.net.Uri.Builder;
import android.provider.BaseColumns;
import android.provider.CallLog;
import android.provider.CallLog.Calls;


/**
 * Virtual provider that enables EventLogs queries to be done in Core with access to SQLiteDatabase object.
 * 
 * Events are from : SMS, MMS, Calls, File Transfer, Chat, Content Sharing tables
 * 
 * @author mhsm6403
 */
public class EventLogProvider extends ContentProvider {

	private SQLiteOpenHelper openHelper;
	/**
	 * The uriMatcher that define all cases to be treated. Requests are not made on an unique table in an unique database so we define Uris to implement 
	 * each cases of filters.
	 */
	private static final UriMatcher uriMatcher;
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI("com.orangelabs.rcs.eventlogs", Integer.toString(EventLogData.MODE_RC_CHAT_FT_CALL_SMS), EventLogData.MODE_RC_CHAT_FT_CALL_SMS);
		uriMatcher.addURI("com.orangelabs.rcs.eventlogs", Integer.toString(EventLogData.MODE_RC_CHAT_FT_CALL), EventLogData.MODE_RC_CHAT_FT_CALL);
		uriMatcher.addURI("com.orangelabs.rcs.eventlogs", Integer.toString(EventLogData.MODE_RC_CHAT_FT_SMS), EventLogData.MODE_RC_CHAT_FT_SMS);
		uriMatcher.addURI("com.orangelabs.rcs.eventlogs", Integer.toString(EventLogData.MODE_RC_CHAT_FT), EventLogData.MODE_RC_CHAT_FT);
		uriMatcher.addURI("com.orangelabs.rcs.eventlogs", Integer.toString(EventLogData.MODE_RC_CHAT_CALL_SMS), EventLogData.MODE_RC_CHAT_CALL_SMS);
		uriMatcher.addURI("com.orangelabs.rcs.eventlogs", Integer.toString(EventLogData.MODE_RC_CHAT_CALL), EventLogData.MODE_RC_CHAT_CALL);
		uriMatcher.addURI("com.orangelabs.rcs.eventlogs", Integer.toString(EventLogData.MODE_RC_CHAT_SMS), EventLogData.MODE_RC_CHAT_SMS);
		uriMatcher.addURI("com.orangelabs.rcs.eventlogs", Integer.toString(EventLogData.MODE_RC_CHAT), EventLogData.MODE_RC_CHAT);
		uriMatcher.addURI("com.orangelabs.rcs.eventlogs", Integer.toString(EventLogData.MODE_RC_FT_CALL_SMS), EventLogData.MODE_RC_FT_CALL_SMS);
		uriMatcher.addURI("com.orangelabs.rcs.eventlogs", Integer.toString(EventLogData.MODE_RC_FT_CALL), EventLogData.MODE_RC_FT_CALL);
		uriMatcher.addURI("com.orangelabs.rcs.eventlogs", Integer.toString(EventLogData.MODE_RC_FT_SMS), EventLogData.MODE_RC_FT_SMS);
		uriMatcher.addURI("com.orangelabs.rcs.eventlogs", Integer.toString(EventLogData.MODE_RC_FT), EventLogData.MODE_RC_FT);
		uriMatcher.addURI("com.orangelabs.rcs.eventlogs", Integer.toString(EventLogData.MODE_RC_CALL_SMS), EventLogData.MODE_RC_CALL_SMS);
		uriMatcher.addURI("com.orangelabs.rcs.eventlogs", Integer.toString(EventLogData.MODE_RC_CALL), EventLogData.MODE_RC_CALL);
		uriMatcher.addURI("com.orangelabs.rcs.eventlogs", Integer.toString(EventLogData.MODE_RC_SMS), EventLogData.MODE_RC_SMS);
		uriMatcher.addURI("com.orangelabs.rcs.eventlogs", Integer.toString(EventLogData.MODE_RC), EventLogData.MODE_RC);
		uriMatcher.addURI("com.orangelabs.rcs.eventlogs", Integer.toString(EventLogData.MODE_CHAT_FT_CALL_SMS), EventLogData.MODE_CHAT_FT_CALL_SMS);
		uriMatcher.addURI("com.orangelabs.rcs.eventlogs", Integer.toString(EventLogData.MODE_CHAT_FT_CALL), EventLogData.MODE_CHAT_FT_CALL);
		uriMatcher.addURI("com.orangelabs.rcs.eventlogs", Integer.toString(EventLogData.MODE_CHAT_FT_SMS), EventLogData.MODE_CHAT_FT_SMS);
		uriMatcher.addURI("com.orangelabs.rcs.eventlogs", Integer.toString(EventLogData.MODE_CHAT_FT), EventLogData.MODE_CHAT_FT);
		uriMatcher.addURI("com.orangelabs.rcs.eventlogs", Integer.toString(EventLogData.MODE_CHAT_CALL_SMS), EventLogData.MODE_CHAT_CALL_SMS);
		uriMatcher.addURI("com.orangelabs.rcs.eventlogs", Integer.toString(EventLogData.MODE_CHAT_CALL), EventLogData.MODE_CHAT_CALL);
		uriMatcher.addURI("com.orangelabs.rcs.eventlogs", Integer.toString(EventLogData.MODE_CHAT_SMS), EventLogData.MODE_CHAT_SMS);
		uriMatcher.addURI("com.orangelabs.rcs.eventlogs", Integer.toString(EventLogData.MODE_CHAT), EventLogData.MODE_CHAT);
		uriMatcher.addURI("com.orangelabs.rcs.eventlogs", Integer.toString(EventLogData.MODE_FT_CALL_SMS), EventLogData.MODE_FT_CALL_SMS);
		uriMatcher.addURI("com.orangelabs.rcs.eventlogs", Integer.toString(EventLogData.MODE_FT_CALL), EventLogData.MODE_FT_CALL);
		uriMatcher.addURI("com.orangelabs.rcs.eventlogs", Integer.toString(EventLogData.MODE_FT_SMS), EventLogData.MODE_FT_SMS);
		uriMatcher.addURI("com.orangelabs.rcs.eventlogs", Integer.toString(EventLogData.MODE_FT), EventLogData.MODE_FT);
		uriMatcher.addURI("com.orangelabs.rcs.eventlogs", Integer.toString(EventLogData.MODE_CALL_SMS), EventLogData.MODE_CALL_SMS);
		uriMatcher.addURI("com.orangelabs.rcs.eventlogs", Integer.toString(EventLogData.MODE_CALL), EventLogData.MODE_CALL);
		uriMatcher.addURI("com.orangelabs.rcs.eventlogs", Integer.toString(EventLogData.MODE_SMS), EventLogData.MODE_SMS);
		uriMatcher.addURI("com.orangelabs.rcs.eventlogs", Integer.toString(EventLogData.MODE_NONE), EventLogData.MODE_NONE);		
		
	}
	
	@Override 
	public boolean onCreate() {
		if(RichProviderHelper.getInstance()==null){
        	RichProviderHelper.createInstance(getContext());
        }
        this.openHelper = RichProviderHelper.getInstance();
        return true;
	}
	
	/**
	 * Not used
	 */
	@Override
	public String getType(Uri uri) {
		return null;
	}

	/**
	 * Not used
	 */
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		return null;
	}
	
	/**
	 * Check if the selection parameter match the syntax. IN ('.....','.....',...)
	 * @param selection
	 * @return
	 */
	private boolean checkSelection(String selection){
		//To match correct phone number " IN \\((\\'\\+?\\d{1,12}?\\'\\,?)++\\)" 
		return selection.matches(" IN \\((\\'.+\\'\\,?)++\\)");
	}

	/**
	 * Build a Date sorted Cursor which contains event logs of the mobile for a specific contact.
	 *  
	 * This method is only called with uris from uriMatcher.
	 * Each one defines a state of the eventlog filter and therefore aggregates events of selected types. 
	 * 
	 * selection parameter is specially build : " IN ('phonenumber1','phonenumber2'...)" Must not be null
	 * sortOrder must be (EventLogData.KEY_EVENT_SESSION_ID+ " DESC , "+EventLogData.KEY_EVENT_DATE + " DESC ")
	 */
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		
		if(selection == null || !checkSelection(selection)){
			throw new IllegalArgumentException("Selection must not be " + selection);
		}
			
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		SQLiteDatabase db = openHelper.getWritableDatabase();

		String limit = null;
		String typeDiscriminatorColumn = EventLogData.KEY_EVENT_TYPE;
		sortOrder = EventLogData.KEY_EVENT_SESSION_ID+ " DESC , "+EventLogData.KEY_EVENT_DATE + " DESC ";
		Cursor sortCursor = null;
		Cursor unionCursor = null;
		Cursor callCursor = null;
		Cursor smsCursor = null;
		String unionQuery = null;
		String richMessagingSelectQuery = null;
		String richCallSelectQuery = null;

		int match = uriMatcher.match(uri);
        
        switch(match) {      
        case EventLogData.MODE_NONE:
        case EventLogData.MODE_RC_CHAT_FT_CALL_SMS:		
			/* Query the call table */
			callCursor = queryCallTable(selection, sortOrder);
			/* Query the sms/mms table */
			smsCursor = queryMmsSmsTable(selection, sortOrder);
			/* Query for Rich Messaging */
			richMessagingSelectQuery = buildRichMessagingQuery(typeDiscriminatorColumn, selection, null);
			/* Query for RichCALL */
			richCallSelectQuery = buildRichCallQuery(typeDiscriminatorColumn, selection);
	        unionQuery = builder.buildUnionQuery(new String[] { richMessagingSelectQuery,richCallSelectQuery },sortOrder,limit);
			unionCursor = db.rawQuery(unionQuery, null);
			/* Build a SortCursor with all cursors sorted by Date */
			sortCursor = new SortCursor(new Cursor[]{unionCursor,callCursor,smsCursor},EventLogData.KEY_EVENT_DATE,SortCursor.TYPE_NUMERIC,false);
			break;
    	case EventLogData.MODE_RC_CHAT_FT_CALL:
    		/* Query the call table */
			callCursor = queryCallTable(selection, sortOrder);
    		/* Query for Rich Messaging */
			richMessagingSelectQuery = buildRichMessagingQuery(typeDiscriminatorColumn, selection, null);
			/* Query for RichCALL */
			richCallSelectQuery = buildRichCallQuery(typeDiscriminatorColumn, selection);
			unionQuery = builder.buildUnionQuery(new String[] { richMessagingSelectQuery,richCallSelectQuery },sortOrder,limit);
			unionCursor = db.rawQuery(unionQuery, null);
			sortCursor = new SortCursor(new Cursor[]{unionCursor,callCursor},EventLogData.KEY_EVENT_DATE,SortCursor.TYPE_NUMERIC,false);
			break;
    	case EventLogData.MODE_RC_CHAT_FT_SMS:
    		/* Query the sms/mms table */
			smsCursor = queryMmsSmsTable(selection, sortOrder);
    		/* Query for Rich Messaging */
			richMessagingSelectQuery = buildRichMessagingQuery(typeDiscriminatorColumn, selection, null);
			/* Query for RichCALL */
			richCallSelectQuery = buildRichCallQuery(typeDiscriminatorColumn, selection);
			unionQuery = builder.buildUnionQuery(new String[] { richMessagingSelectQuery,richCallSelectQuery },sortOrder,limit);
			unionCursor = db.rawQuery(unionQuery, null);
			sortCursor = new SortCursor(new Cursor[]{unionCursor,smsCursor},EventLogData.KEY_EVENT_DATE,SortCursor.TYPE_NUMERIC,false);
			break;
		case EventLogData.MODE_RC_CHAT_FT:
			/* Query for Rich Messaging */
			richMessagingSelectQuery = buildRichMessagingQuery(typeDiscriminatorColumn, selection, null);
			/* Query for RichCALL */
			richCallSelectQuery = buildRichCallQuery(typeDiscriminatorColumn, selection);
			unionQuery = builder.buildUnionQuery(new String[] { richMessagingSelectQuery,richCallSelectQuery },sortOrder,limit);
			sortCursor = db.rawQuery(unionQuery, null);
			break;
    	case EventLogData.MODE_RC_CHAT_CALL_SMS:
    		/* Query the sms/mms table */
			smsCursor = queryMmsSmsTable(selection, sortOrder);
    		/* Query the call table */
			callCursor = queryCallTable(selection, sortOrder);
    		/* Query for Rich Messaging */
			richMessagingSelectQuery = buildRichMessagingQuery(typeDiscriminatorColumn, selection, EventLogData.VALUE_EVENT_TYPE_CHAT);
			/* Query for RichCALL */
			richCallSelectQuery = buildRichCallQuery(typeDiscriminatorColumn, selection);
			unionQuery = builder.buildUnionQuery(new String[] { richMessagingSelectQuery,richCallSelectQuery },sortOrder,limit);
			unionCursor = db.rawQuery(unionQuery, null);
			sortCursor = new SortCursor(new Cursor[]{unionCursor,callCursor,smsCursor},EventLogData.KEY_EVENT_DATE,SortCursor.TYPE_NUMERIC,false);
			break;
    	case EventLogData.MODE_RC_CHAT_CALL:
    		/* Query the call table */
			callCursor = queryCallTable(selection, sortOrder);
    		/* Query for Rich Messaging */
			richMessagingSelectQuery = buildRichMessagingQuery(typeDiscriminatorColumn, selection, EventLogData.VALUE_EVENT_TYPE_CHAT);
			/* Query for RichCALL */
			richCallSelectQuery = buildRichCallQuery(typeDiscriminatorColumn, selection);
			unionQuery = builder.buildUnionQuery(new String[] { richMessagingSelectQuery,richCallSelectQuery },sortOrder,limit);
			unionCursor = db.rawQuery(unionQuery, null);
			sortCursor = new SortCursor(new Cursor[]{unionCursor,callCursor},EventLogData.KEY_EVENT_DATE,SortCursor.TYPE_NUMERIC,false);
			break;
    	case EventLogData.MODE_RC_CHAT_SMS:
    		/* Query the sms/mms table */
			smsCursor = queryMmsSmsTable(selection, sortOrder);
    		/* Query for Rich Messaging */
			richMessagingSelectQuery = buildRichMessagingQuery(typeDiscriminatorColumn, selection, EventLogData.VALUE_EVENT_TYPE_CHAT);
			/* Query for RichCALL */
			richCallSelectQuery = buildRichCallQuery(typeDiscriminatorColumn, selection);
			unionQuery = builder.buildUnionQuery(new String[] { richMessagingSelectQuery,richCallSelectQuery },sortOrder,limit);
			unionCursor = db.rawQuery(unionQuery, null);
			sortCursor = new SortCursor(new Cursor[]{unionCursor,smsCursor},EventLogData.KEY_EVENT_DATE,SortCursor.TYPE_NUMERIC,false);
			break;
    	case EventLogData.MODE_RC_CHAT:
    		/* Query for Rich Messaging */
			richMessagingSelectQuery = buildRichMessagingQuery(typeDiscriminatorColumn, selection, EventLogData.VALUE_EVENT_TYPE_CHAT);
			/* Query for RichCALL */
			richCallSelectQuery = buildRichCallQuery(typeDiscriminatorColumn, selection);
			unionQuery = builder.buildUnionQuery(new String[] { richMessagingSelectQuery,richCallSelectQuery },sortOrder,limit);
			sortCursor = db.rawQuery(unionQuery, null);
			break;
    	case EventLogData.MODE_RC_FT_CALL_SMS:
    		/* Query the sms/mms table */
			smsCursor = queryMmsSmsTable(selection, sortOrder);
    		/* Query the call table */
			callCursor = queryCallTable(selection, sortOrder);
    		/* Query for Rich Messaging */
			richMessagingSelectQuery = buildRichMessagingQuery(typeDiscriminatorColumn, selection, EventLogData.VALUE_EVENT_TYPE_FILETRANSFER);
			/* Query for RichCALL */
			richCallSelectQuery = buildRichCallQuery(typeDiscriminatorColumn, selection);
			unionQuery = builder.buildUnionQuery(new String[] { richMessagingSelectQuery,richCallSelectQuery },sortOrder,limit);
			unionCursor = db.rawQuery(unionQuery, null);
			sortCursor = new SortCursor(new Cursor[]{unionCursor,callCursor,smsCursor},EventLogData.KEY_EVENT_DATE,SortCursor.TYPE_NUMERIC,false);
			break;
    	case EventLogData.MODE_RC_FT_CALL:
    		/* Query the call table */
			callCursor = queryCallTable(selection, sortOrder);
    		/* Query for Rich Messaging */
			richMessagingSelectQuery = buildRichMessagingQuery(typeDiscriminatorColumn, selection, EventLogData.VALUE_EVENT_TYPE_FILETRANSFER);
			/* Query for RichCALL */
			richCallSelectQuery = buildRichCallQuery(typeDiscriminatorColumn, selection);
			unionQuery = builder.buildUnionQuery(new String[] { richMessagingSelectQuery,richCallSelectQuery },sortOrder,limit);
			unionCursor = db.rawQuery(unionQuery, null);
			sortCursor = new SortCursor(new Cursor[]{unionCursor,callCursor},EventLogData.KEY_EVENT_DATE,SortCursor.TYPE_NUMERIC,false);
			break;
    	case EventLogData.MODE_RC_FT_SMS:
    		/* Query the sms/mms table */
			smsCursor = queryMmsSmsTable(selection, sortOrder);
    		/* Query for Rich Messaging */
			richMessagingSelectQuery = buildRichMessagingQuery(typeDiscriminatorColumn, selection, EventLogData.VALUE_EVENT_TYPE_FILETRANSFER);
			/* Query for RichCALL */
			richCallSelectQuery = buildRichCallQuery(typeDiscriminatorColumn, selection);
			unionQuery = builder.buildUnionQuery(new String[] { richMessagingSelectQuery,richCallSelectQuery },sortOrder,limit);
			unionCursor = db.rawQuery(unionQuery, null);
			sortCursor = new SortCursor(new Cursor[]{unionCursor,smsCursor},EventLogData.KEY_EVENT_DATE,SortCursor.TYPE_NUMERIC,false);
			break;
    	case EventLogData.MODE_RC_FT:
    		/* Query for Rich Messaging */
			richMessagingSelectQuery = buildRichMessagingQuery(typeDiscriminatorColumn, selection, EventLogData.VALUE_EVENT_TYPE_FILETRANSFER);
			/* Query for RichCALL */
			richCallSelectQuery = buildRichCallQuery(typeDiscriminatorColumn, selection);
			unionQuery = builder.buildUnionQuery(new String[] { richMessagingSelectQuery,richCallSelectQuery },sortOrder,limit);
			sortCursor = db.rawQuery(unionQuery, null);
			break;
    	case EventLogData.MODE_RC_CALL_SMS:
    		/* Query the sms/mms table */
			smsCursor = queryMmsSmsTable(selection, sortOrder);
    		/* Query the call table */
			callCursor = queryCallTable(selection, sortOrder);
			/* Query for RichCALL */
			richCallSelectQuery = buildRichCallQuery(typeDiscriminatorColumn, selection);
			unionQuery = builder.buildUnionQuery(new String[] { richCallSelectQuery },sortOrder,limit);
			unionCursor = db.rawQuery(unionQuery, null);
			sortCursor = new SortCursor(new Cursor[]{unionCursor,callCursor,smsCursor},EventLogData.KEY_EVENT_DATE,SortCursor.TYPE_NUMERIC,false);
			break;
		case EventLogData.MODE_RC_CALL:
			/* Query the call table */
			callCursor = queryCallTable(selection, sortOrder);
			/* Query for RichCALL */
			richCallSelectQuery = buildRichCallQuery(typeDiscriminatorColumn, selection);
			unionQuery = builder.buildUnionQuery(new String[] { richCallSelectQuery },sortOrder,limit);
			unionCursor = db.rawQuery(unionQuery, null);
			sortCursor = new SortCursor(new Cursor[]{unionCursor,callCursor},EventLogData.KEY_EVENT_DATE,SortCursor.TYPE_NUMERIC,false);
			break;
		case EventLogData.MODE_RC_SMS:
			/* Query the sms/mms table */
			smsCursor = queryMmsSmsTable(selection, sortOrder);
			/* Query for Rich Messaging */
			richCallSelectQuery = buildRichCallQuery(typeDiscriminatorColumn, selection);
			unionQuery = builder.buildUnionQuery(new String[] { richCallSelectQuery },sortOrder,limit);
			unionCursor = db.rawQuery(unionQuery, null);
			sortCursor = new SortCursor(new Cursor[]{unionCursor,smsCursor},EventLogData.KEY_EVENT_DATE,SortCursor.TYPE_NUMERIC,false);
			break;
		case EventLogData.MODE_RC:
			/* Query for RichCALL */
			richCallSelectQuery = buildRichCallQuery(typeDiscriminatorColumn, selection);
			unionQuery = builder.buildUnionQuery(new String[] { richCallSelectQuery },sortOrder,limit);
			sortCursor = db.rawQuery(unionQuery, null);
			break;
    	case EventLogData.MODE_CHAT_FT_CALL_SMS:
    		/* Query the call table */
			callCursor = queryCallTable(selection, sortOrder);
			/* Query the sms/mms table */
			smsCursor = queryMmsSmsTable(selection, sortOrder);
			/* Query for Rich Messaging */
			richMessagingSelectQuery = buildRichMessagingQuery(typeDiscriminatorColumn, selection, null);
	        unionQuery = builder.buildUnionQuery(new String[] { richMessagingSelectQuery },sortOrder,limit);
			unionCursor = db.rawQuery(unionQuery, null);
			sortCursor = new SortCursor(new Cursor[]{unionCursor,callCursor,smsCursor},EventLogData.KEY_EVENT_DATE,SortCursor.TYPE_NUMERIC,false);
			break;
    	case EventLogData.MODE_CHAT_FT_CALL:
    		/* Query the sms/mms table */
    		callCursor = queryMmsSmsTable(selection, sortOrder);
			/* Query for Rich Messaging */
			richMessagingSelectQuery = buildRichMessagingQuery(typeDiscriminatorColumn, selection, null);
			unionQuery = builder.buildUnionQuery(new String[] { richMessagingSelectQuery },sortOrder,limit);
			unionCursor = db.rawQuery(unionQuery, null);
			sortCursor = new SortCursor(new Cursor[]{unionCursor,callCursor},EventLogData.KEY_EVENT_DATE,SortCursor.TYPE_NUMERIC,false);
			break;
		case EventLogData.MODE_CHAT_FT_SMS: 	
			/* Query the sms/mms table */
			smsCursor = queryMmsSmsTable(selection, sortOrder);
			/* Query for Rich Messaging */
			richMessagingSelectQuery = buildRichMessagingQuery(typeDiscriminatorColumn, selection, null);
			unionQuery = builder.buildUnionQuery(new String[] { richMessagingSelectQuery },sortOrder,limit);
			unionCursor = db.rawQuery(unionQuery, null);
			sortCursor = new SortCursor(new Cursor[]{unionCursor,smsCursor},EventLogData.KEY_EVENT_DATE,SortCursor.TYPE_NUMERIC,false);
			break;
		case EventLogData.MODE_CHAT_FT: 
			/* Query for Rich Messaging */
			richMessagingSelectQuery = buildRichMessagingQuery(typeDiscriminatorColumn, selection, null);
			unionQuery = builder.buildUnionQuery(new String[] { richMessagingSelectQuery },sortOrder,limit);
			sortCursor = db.rawQuery(unionQuery, null);
			break;
    	case EventLogData.MODE_CHAT_CALL_SMS:
    		/* Query the call table */
			callCursor = queryCallTable(selection, sortOrder);
			/* Query the sms/mms table */
			smsCursor = queryMmsSmsTable(selection, sortOrder);
			/* Query for Rich Messaging */
			richMessagingSelectQuery = buildRichMessagingQuery(typeDiscriminatorColumn, selection, EventLogData.VALUE_EVENT_TYPE_CHAT);
	        unionQuery = builder.buildUnionQuery(new String[] { richMessagingSelectQuery },sortOrder,limit);
			unionCursor = db.rawQuery(unionQuery, null);
			sortCursor = new SortCursor(new Cursor[]{unionCursor,callCursor,smsCursor},EventLogData.KEY_EVENT_DATE,SortCursor.TYPE_NUMERIC,false);
			break;
    	case EventLogData.MODE_CHAT_CALL:
    		/* Query the call table */
			callCursor = queryCallTable(selection, sortOrder);
			/* Query for Rich Messaging */
			richMessagingSelectQuery = buildRichMessagingQuery(typeDiscriminatorColumn, selection, EventLogData.VALUE_EVENT_TYPE_CHAT);
			unionQuery = builder.buildUnionQuery(new String[] { richMessagingSelectQuery },sortOrder,limit);
			unionCursor = db.rawQuery(unionQuery, null);
			sortCursor = new SortCursor(new Cursor[]{unionCursor,callCursor},EventLogData.KEY_EVENT_DATE,SortCursor.TYPE_NUMERIC,false);
			break;
    	case EventLogData.MODE_CHAT_SMS:
    		/* Query the sms/mms table */
			smsCursor = queryMmsSmsTable(selection, sortOrder);
			/* Query for Rich Messaging */
			richMessagingSelectQuery = buildRichMessagingQuery(typeDiscriminatorColumn, selection, EventLogData.VALUE_EVENT_TYPE_CHAT);
			unionQuery = builder.buildUnionQuery(new String[] { richMessagingSelectQuery },sortOrder,limit);
			unionCursor = db.rawQuery(unionQuery, null);
			sortCursor = new SortCursor(new Cursor[]{unionCursor,smsCursor},EventLogData.KEY_EVENT_DATE,SortCursor.TYPE_NUMERIC,false);
			break;
    	case EventLogData.MODE_CHAT:
    		richMessagingSelectQuery = buildRichMessagingQuery(typeDiscriminatorColumn, selection, EventLogData.VALUE_EVENT_TYPE_CHAT);
			unionQuery = builder.buildUnionQuery(new String[] { richMessagingSelectQuery },sortOrder,limit);
			sortCursor = db.rawQuery(unionQuery, null);
    		break;
    	case EventLogData.MODE_FT_CALL_SMS:
    		/* Query the call table */
			callCursor = queryCallTable(selection, sortOrder);
			/* Query the sms/mms table */
			smsCursor = queryMmsSmsTable(selection, sortOrder);
			/* Query for Rich Messaging */
			richMessagingSelectQuery = buildRichMessagingQuery(typeDiscriminatorColumn, selection, EventLogData.VALUE_EVENT_TYPE_FILETRANSFER);
	        unionQuery = builder.buildUnionQuery(new String[] { richMessagingSelectQuery },sortOrder,limit);
			unionCursor = db.rawQuery(unionQuery, null);
			sortCursor = new SortCursor(new Cursor[]{unionCursor,callCursor,smsCursor},EventLogData.KEY_EVENT_DATE,SortCursor.TYPE_NUMERIC,false);
			break;
    	case EventLogData.MODE_FT_CALL:
    		/* Query the call table */
			callCursor = queryCallTable(selection, sortOrder);
			/* Query for Rich Messaging */
			richMessagingSelectQuery = buildRichMessagingQuery(typeDiscriminatorColumn, selection, EventLogData.VALUE_EVENT_TYPE_FILETRANSFER);
			unionQuery = builder.buildUnionQuery(new String[] { richMessagingSelectQuery },sortOrder,limit);
			unionCursor = db.rawQuery(unionQuery, null);
			sortCursor = new SortCursor(new Cursor[]{unionCursor,callCursor},EventLogData.KEY_EVENT_DATE,SortCursor.TYPE_NUMERIC,false);
			break;
    	case EventLogData.MODE_FT_SMS:
    		/* Query the sms/mms table */
			smsCursor = queryMmsSmsTable(selection, sortOrder);
			/* Query for Rich Messaging */
			richMessagingSelectQuery = buildRichMessagingQuery(typeDiscriminatorColumn, selection, EventLogData.VALUE_EVENT_TYPE_FILETRANSFER);
			unionQuery = builder.buildUnionQuery(new String[] { richMessagingSelectQuery },sortOrder,limit);
			unionCursor = db.rawQuery(unionQuery, null);
			sortCursor = new SortCursor(new Cursor[]{unionCursor,smsCursor},EventLogData.KEY_EVENT_DATE,SortCursor.TYPE_NUMERIC,false);
			break;
    	case EventLogData.MODE_FT:
    		richMessagingSelectQuery = buildRichMessagingQuery(typeDiscriminatorColumn, selection, EventLogData.VALUE_EVENT_TYPE_FILETRANSFER);
			unionQuery = builder.buildUnionQuery(new String[] { richMessagingSelectQuery },sortOrder,limit);
			sortCursor = db.rawQuery(unionQuery, null);
    		break;
    	case EventLogData.MODE_CALL_SMS:
    		/* Query the call table */
			callCursor = queryCallTable(selection, sortOrder);
			/* Query the sms/mms table */
			smsCursor = queryMmsSmsTable(selection, sortOrder);
			sortCursor = new SortCursor(new Cursor[]{smsCursor,callCursor},EventLogData.KEY_EVENT_DATE,SortCursor.TYPE_NUMERIC,false);
			break;
    	case EventLogData.MODE_CALL:
			/* Query the call table */
			sortCursor = queryCallTable(selection, sortOrder);
			break;
		case EventLogData.MODE_SMS:
			/* Query the sms/mms table */
			sortCursor = queryMmsSmsTable(selection, sortOrder);
			break;
		default:
	        throw new IllegalArgumentException("Unknown URI " + uri);
		}
        
        // Register the contexts ContentResolver to be notified if
		// the cursor result set changes.
        if (sortCursor != null) {
        	sortCursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        
		// Return a cursor to the query result
		return sortCursor;
	}
	
	/**
	 * Not used
	 */
	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		return 0;
	}

	
	/* Android native tables projections */
	private static String[] callProjection = new String[]{
			Calls._ID+ " AS "+ EventLogData.KEY_EVENT_ROW_ID,
			Calls.DATE+ " AS "+ EventLogData.KEY_EVENT_DATE,
			"\'"+EventLogData.GSM_MIMETYPE+ "\' AS "+ EventLogData.KEY_EVENT_MIMETYPE,
			Calls.DURATION+ " AS "+ EventLogData.KEY_EVENT_DATA,
			Calls.TYPE+ " AS "+ EventLogData.KEY_EVENT_DESTINATION,
			Calls.NEW+ " AS "+ EventLogData.KEY_EVENT_STATUS,
			Calls.NUMBER+" AS "+EventLogData.KEY_EVENT_PHONE_NUMBER,
			EventLogData.VALUE_EVENT_TYPE_CALL+ " AS "+ EventLogData.KEY_EVENT_TYPE,
			Calls._ID+ " AS "+ EventLogData.KEY_EVENT_SESSION_ID};
	
	private static String KEY_SMS_ADDRESS = "address";
	private static String KEY_SMS_DATE = "date";
	private static String KEY_SMS_BODY = "body";
	private static String KEY_SMS_TYPE = "type";
	private static String KEY_SMS_STATUS = "status";
		
	private static String[] smsProjection = new String[]{
		BaseColumns._ID+" AS "+EventLogData.KEY_EVENT_ROW_ID,
		KEY_SMS_DATE+ " AS "+ EventLogData.KEY_EVENT_DATE,
		"\'"+EventLogData.SMS_MIMETYPE+ "\' AS "+ EventLogData.KEY_EVENT_MIMETYPE,
		KEY_SMS_BODY+ " AS "+ EventLogData.KEY_EVENT_DATA,
		KEY_SMS_TYPE+ " AS "+ EventLogData.KEY_EVENT_DESTINATION,/* 1 received , 2 sent */
		KEY_SMS_STATUS+ " AS "+ EventLogData.KEY_EVENT_STATUS,
		KEY_SMS_ADDRESS+" AS "+EventLogData.KEY_EVENT_PHONE_NUMBER,
		EventLogData.VALUE_EVENT_TYPE_MMS_SMS+ " AS "+ EventLogData.KEY_EVENT_TYPE,
		BaseColumns._ID+" AS "+EventLogData.KEY_EVENT_SESSION_ID};
		
	/* RCS projections */
	private static String [] unionRichMessagingColumns = new String[]{
			RichMessagingData.KEY_ID+" AS "+EventLogData.KEY_EVENT_ROW_ID,
			RichMessagingData.KEY_TRANSFER_DATE+" AS "+EventLogData.KEY_EVENT_DATE,
			RichMessagingData.KEY_MIME_TYPE+" AS "+EventLogData.KEY_EVENT_MIMETYPE,
			RichMessagingData.KEY_DATA+" AS "+EventLogData.KEY_EVENT_DATA,
			RichMessagingData.KEY_DESTINATION+" AS "+EventLogData.KEY_EVENT_DESTINATION,
			RichMessagingData.KEY_TRANSFER_STATUS+" AS "+EventLogData.KEY_EVENT_STATUS,
			RichMessagingData.KEY_CONTACT_NUMBER+" AS "+EventLogData.KEY_EVENT_PHONE_NUMBER,
			RichMessagingData.KEY_TYPE_DISCRIMINATOR+" AS "+EventLogData.KEY_EVENT_TYPE,
			RichMessagingData.KEY_SESSION_ID+" AS "+EventLogData.KEY_EVENT_SESSION_ID};
	
	private static Set<String> columnsPresentInRichMessagingTable = new HashSet<String>(Arrays.asList(new String[]{
			RichMessagingData.KEY_ID,
			RichMessagingData.KEY_TRANSFER_DATE,
			RichMessagingData.KEY_MIME_TYPE,
			RichMessagingData.KEY_DATA,
			RichMessagingData.KEY_DESTINATION,
			RichMessagingData.KEY_TRANSFER_STATUS,
			RichMessagingData.KEY_CONTACT_NUMBER,
			RichMessagingData.KEY_TYPE_DISCRIMINATOR,
			RichMessagingData.KEY_SESSION_ID}));
	
	private static String [] unionRichCallColumns = new String[]{
			RichCallData.KEY_ID+" AS "+EventLogData.KEY_EVENT_ROW_ID,
			RichCallData.KEY_TRANSFER_DATE+" AS "+EventLogData.KEY_EVENT_DATE,
			RichCallData.KEY_MIME_TYPE+" AS "+EventLogData.KEY_EVENT_MIMETYPE,
			RichCallData.KEY_DATA+" AS "+EventLogData.KEY_EVENT_DATA,
			RichCallData.KEY_DESTINATION+" AS "+EventLogData.KEY_EVENT_DESTINATION,
			RichCallData.KEY_STATUS+" AS "+EventLogData.KEY_EVENT_STATUS,
			RichCallData.KEY_CONTACT_NUMBER+" AS "+EventLogData.KEY_EVENT_PHONE_NUMBER,
			EventLogData.KEY_EVENT_TYPE,
			RichCallData.KEY_SESSION_ID+" AS "+EventLogData.KEY_EVENT_SESSION_ID};
	
	private static Set<String> columnsPresentInRichCallTable = new HashSet<String>(Arrays.asList(new String []{
			RichCallData.KEY_ID,
			RichCallData.KEY_TRANSFER_DATE,
			RichCallData.KEY_MIME_TYPE,
			RichCallData.KEY_DATA,
			RichCallData.KEY_DESTINATION,
			RichCallData.KEY_CONTACT_NUMBER,
			RichCallData.KEY_STATUS,
			RichCallData.KEY_SESSION_ID}));
	
	
	/**
	 *  Query the Android CallLog table to get all CallLog for the specified Numbers in selection
	 * @param selection
	 * @param sortOrder
	 * @return
	 */
	private Cursor queryCallTable(String selection, String sortOrder) {
		return getContext().getContentResolver().query(CallLog.Calls.CONTENT_URI, 
				callProjection, (selection!=null?Calls.NUMBER+selection:null) , null, sortOrder);
	}
	
	/**
	 *  Query the Android MmsSms table to get all message for the specified Numbers in selection
	 * @param selection
	 * @param sortOrder
	 * @return
	 */
	private Cursor queryMmsSmsTable(String selection, String sortOrder) {
		selection = getThreadIdSelection(selection);

		Cursor smsCursor = getContext().getContentResolver().query(EventLogData.SMS_URI,smsProjection, selection , null, sortOrder);	
		Cursor mmsCursor = getMMSCursor(selection);
		Cursor sortCursor = new SortCursor(new Cursor[]{smsCursor,mmsCursor},EventLogData.KEY_EVENT_DATE,SortCursor.TYPE_NUMERIC,false);	
		return sortCursor;
	}

	private String getThreadIdSelection(String selection){
		/* Unbuild selection */
		selection = selection.substring(5,selection.length()-1);
		String[] numbers;
		if(selection!=null){
			numbers = selection.split(",");
			/* Get the threadIds of each number for the contacts */
			Uri.Builder builder;
			Cursor cThreadId;
			List<Integer> threadIds = new ArrayList<Integer>();
			for(int i = 0; i < numbers.length; i++){
				builder = new Builder();
				builder.scheme("content");
				builder.authority("mms-sms");
				builder.path("threadID");
				builder.appendQueryParameter("recipient", numbers[i].substring(1,numbers[i].length()-1));
				cThreadId = getContext().getContentResolver().query(builder.build(),null, null, null, null);
				while(cThreadId.moveToNext()){
					threadIds.add(cThreadId.getInt(0));
				}
				cThreadId.close();
			}

			/* Get SMS and MMS from those ThreadIds */
			selection = "thread_id IN (";
			for(int i = 0; i <threadIds.size();i++){
				selection+=threadIds.get(i)+",";
			}
			selection = selection.substring(0, selection.length()-1)+")";
		}
		return selection;
	}
	
	/**
	 * Get MMS messages info from MMS tables according to the selection.
	 * @param selection, selection must be constructed around thread_id parameter. See getThreadIdSelection().
	 * @return
	 */
	private Cursor getMMSCursor(String selection) {
		MatrixCursor matrixCursor = new MatrixCursor(new String[]{
				EventLogData.KEY_EVENT_ROW_ID,
				EventLogData.KEY_EVENT_DATE,
				EventLogData.KEY_EVENT_MIMETYPE,
				EventLogData.KEY_EVENT_DATA,
				EventLogData.KEY_EVENT_DESTINATION,
				EventLogData.KEY_EVENT_STATUS,
				EventLogData.KEY_EVENT_PHONE_NUMBER,
				EventLogData.KEY_EVENT_TYPE,
				EventLogData.KEY_EVENT_SESSION_ID});

		Cursor curPdu = getContext().getContentResolver().query(EventLogData.MMS_URI, null, selection, null, null);
		String id = null;
		int dest;
		String status = null;
		String date = null;
		String mmsText = null;
		String fileName = null;
		String address = null;

		while (curPdu.moveToNext()) {
			id = curPdu.getString(curPdu.getColumnIndex("_id"));
			dest = curPdu.getInt(curPdu.getColumnIndex("msg_box"));
			date = curPdu.getString(curPdu.getColumnIndex("date"));
			status = curPdu.getString(curPdu.getColumnIndex("st"));

			
			/**
			 *  Find addresses related to the message.
			 *  In Addr table, type is represented by PduHeader Constant FROM TO CC etc.. which have different type
			 *  FROM = 137
			 *  TO = 151
			 */
			Uri uriAddr = Uri.parse("content://mms/" + id + "/addr");
			Cursor curAddr = getContext().getContentResolver().query(uriAddr, null,null, null, null);
			while (curAddr.moveToNext()) {
				int type = curAddr.getInt(curAddr.getColumnIndex("type"));
				if((dest==EventLogData.VALUE_EVENT_DEST_INCOMING && type == 137) || 
						(dest==EventLogData.VALUE_EVENT_DEST_OUTGOING && type == 151)) {
					address = curAddr.getString(curAddr.getColumnIndex("address"));
					break;
				}
			}
			curAddr.close();
			
			
			/**
			 *  Find all parts according to the id of the mms in the Pdu table.
			 */
			Cursor curPart = getContext().getApplicationContext().getContentResolver().query(Uri.parse("content://mms/"+id+"/part"), null, null,null, null);
			String mimeType = null ,dataMimeType = null;
			while (curPart.moveToNext()) {
				mimeType = curPart.getString(3);
				if (MimeManager.isTextType(mimeType)) {
					mmsText = curPart.getString(13);
				}
				if (MimeManager.isPictureType(mimeType) || MimeManager.isVideoType(mimeType)) {
					fileName = curPart.getString(9);
					dataMimeType = mimeType;
				}
			}
			curPart.close();

			/**
			 *  Build a cursor with all mms entries for the specifics numbers
			 */
			matrixCursor.addRow(new Object[]{
					id,
					date+"000",
					EventLogData.MMS_MIMETYPE,
					mmsText+";"+fileName+";"+dataMimeType,
					dest,
					status,
					PhoneUtils.formatNumberToInternational(address),
					EventLogData.VALUE_EVENT_TYPE_MMS_SMS,
					id});
		}
		curPdu.close();
		
		return matrixCursor;
	}

	
	/**
	 * Build a Sql query to be part of a union query on the rcs Table
	 * Get all RichMessaging of type 'type' for the specified Numbers in selection
	 * If no type is specified, get all RichMessaging.
	 * @param typeDiscriminatorColumn
	 * @param selection
	 * @param type discriminator for RichMessaging (CHAT, IM, FT ...)
	 * @return
	 */
	private String buildRichMessagingQuery(String typeDiscriminatorColumn, String selection, Integer type){
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables("\""+RichMessagingProvider.TABLE+"\"");
		return builder.buildUnionSubQuery(
				typeDiscriminatorColumn, 
				unionRichMessagingColumns, 
				columnsPresentInRichMessagingTable, 
				9, 
				EventLogData.KEY_EVENT_TYPE, 
				(selection!=null?
						RichMessagingData.KEY_CONTACT_NUMBER+selection+(type!=null?" AND "+RichMessagingData.KEY_TYPE_DISCRIMINATOR+" = "+type:"")
						:
						(type!=null?RichMessagingData.KEY_TYPE_DISCRIMINATOR+" = "+type:null)), null, null, null);
	}
		
	/**
	 * Build a Sql query to be part of a union query on the rcs Table
	 * Get all RichCall for the specified Numbers in selection
	 * @param typeDiscriminatorColumn
	 * @param selection
	 * @return
	 */
	private String buildRichCallQuery(String typeDiscriminatorColumn, String selection){
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables("\""+RichCallProvider.TABLE+"\"");
		
		return builder.buildUnionSubQuery(
				typeDiscriminatorColumn, 
				unionRichCallColumns, 
				columnsPresentInRichCallTable, 
				8, 
				Integer.toString(EventLogData.VALUE_EVENT_TYPE_RICH_CALL), 
				(selection!=null?RichCallData.KEY_CONTACT_NUMBER+selection:null), null, null, null);
	}	

	/**
	 * Delete all events from the selected mode.
	 * selection parameter is specially build : " IN ('phonenumber1','phonenumber2'...)" Must not be null
	 */
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		if(selection == null || !checkSelection(selection)){
			throw new IllegalArgumentException("Selection must not be " + selection);
		}
		
		int deletedRows = 0;
		
		// Generate the body of the query 
        int match = uriMatcher.match(uri);

        switch(match) {      
        case EventLogData.MODE_NONE:
        case EventLogData.MODE_RC_CHAT_FT_CALL_SMS:
        	deletedRows+=deleteRichCalls(selection);
        	deletedRows+=deleteRichMessaging(selection);
        	deletedRows+=deleteCalls(selection);
        	deletedRows+=deleteSMSMMS(selection);
        	break;
    	case EventLogData.MODE_RC_CHAT_FT_CALL:
    		deletedRows+=deleteRichCalls(selection);
        	deletedRows+=deleteRichMessaging(selection);
        	deletedRows+=deleteCalls(selection);
    		break;
    	case EventLogData.MODE_RC_CHAT_FT_SMS:
    		deletedRows+=deleteRichCalls(selection);
        	deletedRows+=deleteRichMessaging(selection);
        	deletedRows+=deleteSMSMMS(selection);
    		break;
		case EventLogData.MODE_RC_CHAT_FT:
			deletedRows+=deleteRichCalls(selection);
        	deletedRows+=deleteRichMessaging(selection);
			break;
    	case EventLogData.MODE_RC_CHAT_CALL_SMS:
    		deletedRows+=deleteRichCalls(selection);
        	deletedRows+=deleteChat(selection);
        	deletedRows+=deleteCalls(selection);
        	deletedRows+=deleteSMSMMS(selection);
    		break;
    	case EventLogData.MODE_RC_CHAT_CALL:
    		deletedRows+=deleteRichCalls(selection);
        	deletedRows+=deleteChat(selection);
        	deletedRows+=deleteCalls(selection);
    		break;
    	case EventLogData.MODE_RC_CHAT_SMS:
    		deletedRows+=deleteRichCalls(selection);
        	deletedRows+=deleteChat(selection);
        	deletedRows+=deleteSMSMMS(selection);
    		break;
    	case EventLogData.MODE_RC_CHAT:
    		deletedRows+=deleteRichCalls(selection);
        	deletedRows+=deleteChat(selection);
    		break;
    	case EventLogData.MODE_RC_FT_CALL_SMS:
    		deletedRows+=deleteRichCalls(selection);
        	deletedRows+=deleteFT(selection);
        	deletedRows+=deleteCalls(selection);
        	deletedRows+=deleteSMSMMS(selection);
    		break;
    	case EventLogData.MODE_RC_FT_CALL:
    		deletedRows+=deleteRichCalls(selection);
        	deletedRows+=deleteFT(selection);
        	deletedRows+=deleteCalls(selection);
    		break;
    	case EventLogData.MODE_RC_FT_SMS:
    		deletedRows+=deleteRichCalls(selection);
        	deletedRows+=deleteFT(selection);
        	deletedRows+=deleteSMSMMS(selection);
    		break;
    	case EventLogData.MODE_RC_FT:
    		deletedRows+=deleteRichCalls(selection);
        	deletedRows+=deleteFT(selection);
    		break;
    	case EventLogData.MODE_RC_CALL_SMS:
    		deletedRows+=deleteRichCalls(selection);
        	deletedRows+=deleteCalls(selection);
        	deletedRows+=deleteSMSMMS(selection);
    		break;
		case EventLogData.MODE_RC_CALL:
    		deletedRows+=deleteRichCalls(selection);
        	deletedRows+=deleteCalls(selection);
			break;
		case EventLogData.MODE_RC_SMS:
    		deletedRows+=deleteRichCalls(selection);
        	deletedRows+=deleteSMSMMS(selection);
			break;
		case EventLogData.MODE_RC:
    		deletedRows+=deleteRichCalls(selection);
			break;
    	case EventLogData.MODE_CHAT_FT_CALL_SMS:
        	deletedRows+=deleteRichMessaging(selection);
        	deletedRows+=deleteCalls(selection);
        	deletedRows+=deleteSMSMMS(selection);
    		break;
    	case EventLogData.MODE_CHAT_FT_CALL:
        	deletedRows+=deleteRichMessaging(selection);
        	deletedRows+=deleteCalls(selection);
    		break;
		case EventLogData.MODE_CHAT_FT_SMS:
        	deletedRows+=deleteRichMessaging(selection);
        	deletedRows+=deleteSMSMMS(selection);
			break;
		case EventLogData.MODE_CHAT_FT: 
        	deletedRows+=deleteRichMessaging(selection);
			break;
    	case EventLogData.MODE_CHAT_CALL_SMS:
        	deletedRows+=deleteChat(selection);
        	deletedRows+=deleteCalls(selection);
        	deletedRows+=deleteSMSMMS(selection);
    		break;
    	case EventLogData.MODE_CHAT_CALL:
        	deletedRows+=deleteChat(selection);
        	deletedRows+=deleteCalls(selection);
    		break;
    	case EventLogData.MODE_CHAT_SMS:
        	deletedRows+=deleteChat(selection);
        	deletedRows+=deleteSMSMMS(selection);
    		break;
    	case EventLogData.MODE_CHAT:
        	deletedRows+=deleteChat(selection);
    		break;
    	case EventLogData.MODE_FT_CALL_SMS:
        	deletedRows+=deleteFT(selection);
        	deletedRows+=deleteCalls(selection);
        	deletedRows+=deleteSMSMMS(selection);
    		break;
    	case EventLogData.MODE_FT_CALL:
        	deletedRows+=deleteFT(selection);
        	deletedRows+=deleteCalls(selection);
    		break;
    	case EventLogData.MODE_FT_SMS:
        	deletedRows+=deleteFT(selection);
        	deletedRows+=deleteSMSMMS(selection);
    		break;
    	case EventLogData.MODE_FT:
        	deletedRows+=deleteFT(selection);
    		break;
    	case EventLogData.MODE_CALL_SMS:
        	deletedRows+=deleteCalls(selection);
        	deletedRows+=deleteSMSMMS(selection);
    		break;
    	case EventLogData.MODE_CALL:
        	deletedRows+=deleteCalls(selection);
    		break;
		case EventLogData.MODE_SMS:
        	deletedRows+=deleteSMSMMS(selection);
        	break;
		default:
	        throw new IllegalArgumentException("Unknown URI " + uri);
		}
        return deletedRows;
	}
	
	private int deleteChat(String selection){
        return getContext().getContentResolver().delete(
        		RichMessagingData.CONTENT_URI,
        		(selection!=null?
        				RichMessagingData.KEY_CONTACT_NUMBER+selection+
        				" AND "+RichMessagingData.KEY_TYPE_DISCRIMINATOR+" = "+RichMessagingData.TYPE_CHAT
        				:RichMessagingData.KEY_TYPE_DISCRIMINATOR+" = "+RichMessagingData.TYPE_CHAT)
        				,null);
	}
	
	private int deleteFT(String selection){
        return getContext().getContentResolver().delete(
        		RichMessagingData.CONTENT_URI,
        		(selection!=null?
        				RichMessagingData.KEY_CONTACT_NUMBER+selection+
        				" AND "+RichMessagingData.KEY_TYPE_DISCRIMINATOR+" = "+RichMessagingData.TYPE_FILETRANSFER
        				:RichMessagingData.KEY_TYPE_DISCRIMINATOR+" = "+RichMessagingData.TYPE_FILETRANSFER)
        				,null);
	}
	
	private int deleteSMSMMS(String selection){
		int deletedRows = 0;
		deletedRows+=getContext().getContentResolver().delete(EventLogData.SMS_URI,(selection!=null?KEY_SMS_ADDRESS+selection:null),null);
    	deletedRows+=getContext().getContentResolver().delete(EventLogData.MMS_URI,(selection!=null?getThreadIdSelection(selection):null),null);
		return deletedRows;
	}
	
	private int deleteCalls(String selection){
		return getContext().getContentResolver().delete(CallLog.Calls.CONTENT_URI,(selection!=null?Calls.NUMBER+selection:null),null);
	}
	
	private int deleteRichCalls(String selection){
		return getContext().getContentResolver().delete(RichCallData.CONTENT_URI,(selection!=null?RichCallData.KEY_CONTACT_NUMBER+selection:null),null);
	}
	
	private int deleteRichMessaging(String selection){
		return getContext().getContentResolver().delete(RichMessagingData.CONTENT_URI,(selection!=null?RichMessagingData.KEY_CONTACT_NUMBER+selection:null),null);
	}
}
