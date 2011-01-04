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
package com.orangelabs.rcs.ri.utils;

import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.widget.Toast;

import com.orangelabs.rcs.ri.R;

/**
 * Utility functions
 * 
 * @author jexa7410
 */
public class Utils {
	/**
	 * Create a contact selector based on the native address book
	 * 
	 * @param activity Activity
	 * @return List adapter
	 */
	public static ContactListAdapter createContactListAdapter(Activity activity) {
	    String[] PROJECTION = new String[] {
	    		Phone._ID,
	    		Phone.NUMBER,
	    		Phone.LABEL,
	    		Phone.TYPE,
	    		Phone.CONTACT_ID
		    };
        ContentResolver content = activity.getContentResolver();
		Cursor cursor = content.query(Phone.CONTENT_URI, PROJECTION, Phone.NUMBER + "!='null'", null, null);
		
		return new ContactListAdapter(activity, cursor);
	}
	
	/**
	 * Create a multi contacts selector based on the native address book
	 * 
	 * @param activity Activity
	 * @return List adapter
	 */
	public static MultiContactListAdapter createMultiContactListAdapter(Activity activity) {
	    String[] PROJECTION = new String[] {
	    		Phone._ID,
	    		Phone.NUMBER,
	    		Phone.LABEL,
	    		Phone.TYPE,
	    		Phone.CONTACT_ID
		    };
        ContentResolver content = activity.getContentResolver();
		Cursor cursor = content.query(Phone.CONTENT_URI, PROJECTION, Phone.NUMBER + "!='null'", null, null);
        activity.startManagingCursor(cursor);
		return new MultiContactListAdapter(activity, cursor);
	}
	
	/**
	 * Display a toast
	 * 
	 * @param activity Activity
	 * @param message Message to be displayed
	 */
    public static void displayToast(Activity activity, String message) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
    }

	/**
	 * Display a long toast
	 * 
	 * @param activity Activity
	 * @param message Message to be displayed
	 */
    public static void displayLongToast(Activity activity, String message) {
        Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
    }
    
    /**
	 * Show an error info
	 * 
	 * @param activity Activity
	 * @param msg Message to be displayed
	 * @return Dialog
	 */
    public static AlertDialog showError(Activity activity, String msg) {
    	AlertDialog alert = null;
    	try {
	    	AlertDialog.Builder builder = new AlertDialog.Builder(activity);
	    	builder.setMessage(msg);
	    	builder.setTitle(R.string.title_error);
	    	builder.setCancelable(false);
	    	builder.setPositiveButton(activity.getString(R.string.label_ok), null);
	    	alert = builder.create();
	    	alert.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return alert;
    }

	/**
	 * Show an info
	 * 
	 * @param activity Activity
	 * @param msg Message to be displayed
	 * @return Dialog
	 */
    public static AlertDialog showInfo(Activity activity, String msg) {
    	AlertDialog alert = null;
    	try {
	    	AlertDialog.Builder builder = new AlertDialog.Builder(activity);
	    	builder.setMessage(msg);
	    	builder.setTitle(R.string.title_info);
	    	builder.setCancelable(false);
	    	builder.setPositiveButton(activity.getString(R.string.label_ok), null);
	    	alert = builder.create();
	    	alert.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return alert;
    }

	/**
	 * Show an info with a specific title
	 * 
	 * @param activity Activity
	 * @param title Title of the dialog
	 * @param msg Message to be displayed
	 * @return Dialog
	 */
    public static AlertDialog showInfo(Activity activity, String title, String msg) {
    	AlertDialog alert = null;
    	try {
	    	AlertDialog.Builder builder = new AlertDialog.Builder(activity);
	    	builder.setMessage(msg);
	    	builder.setTitle(title);
	    	builder.setCancelable(false);
	    	builder.setPositiveButton(activity.getString(R.string.label_ok), null);
	    	alert = builder.create();
	    	alert.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return alert;
    }

	/**
	 * Show an info with a specific title
	 * 
	 * @param activity Activity
	 * @param title Title of the dialog
	 * @param items Items
	 * @return Dialog
	 */
    public static AlertDialog showList(Activity activity, String title, CharSequence[] items) {
    	AlertDialog alert = null;
    	try {
	    	AlertDialog.Builder builder = new AlertDialog.Builder(activity);
	    	builder.setTitle(title);
	    	builder.setCancelable(false);
	    	builder.setPositiveButton(activity.getString(R.string.label_ok), null);
	        builder.setItems(items, null);
	    	alert = builder.create();
	    	alert.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return alert;
    }
    
    /**
     * Format a date to string
     * 
     * @param d Date
     * @return String
     */
    public static String formatDateToString(long d) {
    	if (d > 0L) {
	    	Date df = new Date(d);
	    	return df.toLocaleString();
    	} else {
    		return "";
    	}
    }
}
