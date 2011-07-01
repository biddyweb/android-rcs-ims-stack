/*******************************************************************************
 * Software Name : RCS IMS Stack
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

import java.io.File;
import java.io.FileInputStream;
import java.util.Date;
import java.util.Vector;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.orangelabs.rcs.ri.R;
import com.orangelabs.rcs.utils.PhoneUtils;

/**
 * Utility functions
 * 
 * @author jexa7410
 */
public class Utils {
	/**
	 * Format caller id
	 * 
	 * @param intent Intent invitation
	 * @return Id
	 */
	public static String formatCallerId(Intent invitation) {
		String number = invitation.getStringExtra("contact");
		String displayName = invitation.getStringExtra("contactDisplayname"); 
		if ((displayName != null) && (displayName.length() > 0)) { 
			return displayName + " (" + number + ")";
		} else {
			return number;
		}
	}
	
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
		// There is now some filtering to do
		// A number may be present in national or international format, it is considered to be the same

		// List of unique number
		Vector<String> treatedNumbers = new Vector<String>();
		
		MatrixCursor matrix = new MatrixCursor(PROJECTION);
		while (cursor.moveToNext()){
			// Keep a trace of already treated row. Key is (phone number in international, phone contact id)
			String phoneNumber = PhoneUtils.formatNumberToInternational(cursor.getString(1));
			
			if (!treatedNumbers.contains(phoneNumber)){
				matrix.addRow(new Object[]{cursor.getLong(0), 
						phoneNumber,
						cursor.getString(2),
						cursor.getInt(3),
						cursor.getLong(4)});
				treatedNumbers.add(phoneNumber);
			}
		}
		cursor.close();
		
		return new ContactListAdapter(activity, matrix);
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
		// There is now some filtering to do
		// Same number may be used many time for a single contact (for example, this is needed to aggregate contacts with ContactsContract)
		// A number may be present in national or international format, it is considered to be the same

		// There is now some filtering to do
		// A number may be present in national or international format, it is considered to be the same

		// List of unique number
		Vector<String> treatedNumbers = new Vector<String>();
		
		MatrixCursor matrix = new MatrixCursor(PROJECTION);
		while (cursor.moveToNext()){
			// Keep a trace of already treated row. Key is (phone number in international, phone contact id)
			String phoneNumber = PhoneUtils.formatNumberToInternational(cursor.getString(1));
			
			if (!treatedNumbers.contains(phoneNumber)){
				matrix.addRow(new Object[]{cursor.getLong(0), 
						phoneNumber,
						cursor.getString(2),
						cursor.getInt(3),
						cursor.getLong(4)});
				treatedNumbers.add(phoneNumber);
			}
		}
		cursor.close();
        activity.startManagingCursor(matrix);
		return new MultiContactListAdapter(activity, matrix);
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
	 * Show a message and exit activity
	 * 
	 * @param activity Activity
	 * @param msg Message to be displayed
	 */
    public static void showMessageAndExit(final Activity activity, String msg) {
    	AlertDialog.Builder builder = new AlertDialog.Builder(activity);
    	builder.setMessage(msg);
    	builder.setTitle(R.string.title_msg);
    	builder.setCancelable(false);
    	builder.setPositiveButton(activity.getString(R.string.label_ok), new DialogInterface.OnClickListener() {
    		public void onClick(DialogInterface dialog, int which) {
    			activity.finish();
    		}
    	});
    	AlertDialog alert = builder.create();
    	alert.show();
    }

	/**
	 * Show an message
	 * 
	 * @param activity Activity
	 * @param msg Message to be displayed
	 * @return Dialog
	 */
    public static AlertDialog showMessage(Activity activity, String msg) {
    	AlertDialog.Builder builder = new AlertDialog.Builder(activity);
    	builder.setMessage(msg);
    	builder.setTitle(R.string.title_msg);
    	builder.setCancelable(false);
    	builder.setPositiveButton(activity.getString(R.string.label_ok), null);
    	AlertDialog alert = builder.create();
    	alert.show();
		return alert;
    }

	/**
	 * Show a message with a specific title
	 * 
	 * @param activity Activity
	 * @param title Title of the dialog
	 * @param msg Message to be displayed
	 * @return Dialog
	 */
    public static AlertDialog showMessage(Activity activity, String title, String msg) {
    	AlertDialog.Builder builder = new AlertDialog.Builder(activity);
    	builder.setMessage(msg);
    	builder.setTitle(title);
    	builder.setCancelable(false);
    	builder.setPositiveButton(activity.getString(R.string.label_ok), null);
    	AlertDialog alert = builder.create();
    	alert.show();
		return alert;
    }

    /**
	 * Show a picture and exit activity
	 * 
	 * @param activity Activity
	 * @param url Picture to be displayed
	 */
    public static void showPictureAndExit(final Activity activity, String url) {
        Bitmap bitmap = null;
    	try {
	        File file = new File(url);
	        FileInputStream stream =  new FileInputStream(file);
	        bitmap = BitmapFactory.decodeStream(stream);
		} catch(Exception e) {
		}

		if (bitmap != null) {
	        LayoutInflater factory = LayoutInflater.from(activity);
	        final View view = factory.inflate(R.layout.utils_show_picture, null);
	        ImageView imgView = (ImageView)view.findViewById(R.id.picture);
	        imgView.setImageBitmap(bitmap);        	
	        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
	        builder.setTitle(R.string.title_picture);
	        builder.setCancelable(false);
	        builder.setView(view);
	    	builder.setPositiveButton(activity.getString(R.string.label_ok), new DialogInterface.OnClickListener() {
	    		public void onClick(DialogInterface dialog, int which) {
	    			activity.finish();
	    		}
	    	});
	    	AlertDialog alert = builder.create();
	    	alert.show();
		} else {
			// TODO: display error
		}
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
    	AlertDialog.Builder builder = new AlertDialog.Builder(activity);
    	builder.setTitle(title);
    	builder.setCancelable(false);
    	builder.setPositiveButton(activity.getString(R.string.label_ok), null);
        builder.setItems(items, null);
        AlertDialog alert = builder.create();
    	alert.show();
		return alert;
    }
    
	/**
	 * Show a progress dialog with the given parameters 
	 * 
	 * @param activity Activity
	 * @param msg Message to be displayed
	 * @return Dialog
	 */
	public static ProgressDialog showProgressDialog(Activity activity, String msg) {
		ProgressDialog dlg = new ProgressDialog(activity);
		dlg.setMessage(msg);
		dlg.setIndeterminate(true);
		dlg.setCancelable(true);
		dlg.show();
		return dlg;
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
