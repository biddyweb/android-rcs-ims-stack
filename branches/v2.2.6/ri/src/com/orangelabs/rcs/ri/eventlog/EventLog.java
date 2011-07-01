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

package com.orangelabs.rcs.ri.eventlog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

import com.orangelabs.rcs.provider.eventlogs.EventLogData;
import com.orangelabs.rcs.ri.R;
import com.orangelabs.rcs.ri.utils.Utils;
import com.orangelabs.rcs.service.api.client.eventslog.EventsLogApi;
import com.orangelabs.rcs.utils.PhoneUtils;

/**
 * Event log
 */
public class EventLog extends Activity {
	
	/**
	 * EventLog Adapter based on a ResourceCursorAdapter which gets its data from 
	 * the EventLogProvider.
	 * 
	 * The EventLogProvider is a virtual provider that aggregates RichMessagingProvider,
	 * RichCallProvider, Android CallLogProvider, Android SMS Provider and Android MMS Provider. 
	 */
	private EventLogResourceCursorAdapter resourceCursorAdapter;
	
	/**
	 * Selected contact number from spinner.
	 */
	private String currentContactNumber;
	
	/**
	 * Selected contact label from spinner.
	 * i.e.: Home, Mobile...
	 */
	private String currentLabel;

	/********************************************************************
	 * 							Filtering 
	 ********************************************************************/
	
	/**
	 * Current selected mode of the EventLog.
	 * SelectedMode is a part of the URI sent to the EventLogProvider :
	 * 		Uri.parse(EventLogData.CONTENT_URI+ Integer.toString(selectedMode))
	 * This will determine which filter to apply on the query/delete in the provider.
	 * 
	 * So, selectedMode is calculated according to the filter value 
	 * to match the different mode supported by the provider. 
	 * 
	 * See itemsIndexInCheckedArray description below.
	 */
	private int selectedMode = EventsLogApi.MODE_RC_CHAT_FT_CALL_SMS;
	
	/**
	 * AlertDialog to show for selecting filters.
	 */
	private AlertDialog filterAlertDialog;
	
	/**
	 * The filter values as CharSequence[].
	 * Ordered according to itemsIndexInCheckedArray.
	 */
	private CharSequence[] items={"SMS/MMS", "Calls", "File Transfer", "Chat", "RichCall"};
	
	/**
	 *  CheckedItems contains states of the Filter menu. (checked, unchecked)
	 *  This state determines the value of the selectedMode
	 *  by calculating it on a binary representation. 
	 *  
	 *  Whenever the items are reordered or not in UI, the checkedItems array will always be ordered as this.
	 */
	private boolean[] checkedItems = {
			/*(sms/mms	bit0)*/true,
			/*(calls	bit1)*/true,
			/*(FT		bit2)*/true,
			/*(chat		bit3)*/true,
			/*(RC		bit4)*/true};
	/**
	 * Enables the UI elements to be indexed in the binary representation of the selectedMode. 
	 * *****************************
	 * Bit representation of the selectedMode :
	 * sms/mms  = bit0
	 * calls 	= bit1
	 * FT 		= bit2
	 * chat 	= bit3
	 * RC 		= bit4
	 * *****************************
	 * If UI element should be reordered, then reorder also the itemsIndexInCheckedArray, following the bits representation above.
	 * 
	 * i.e. : items = {"RichCall", "Calls", "File Transfer", "Chat", "SMS/MMS"};
	 * 		  itemsIndexInCheckedArray = {4,1,2,3,0};
	 * 		  
	 * 		  Reordering is done with: checkItems[itemsIndexInCheckedArray[which]] = isChecked
	 * 		  Then : selectedMode = for(i=0..4) checkItems[i]*2^i 
	 */
	private final int[] itemsIndexInCheckedArray = {0,1,2,3,4};
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set layout
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.eventlog);
        
        // Set title
        setTitle(R.string.menu_eventlog);
        
        // Set the contact selector
        Spinner spinner = (Spinner)findViewById(R.id.contact);
        spinner.setAdapter(Utils.createContactListAdapter(this));
        spinner.setOnItemSelectedListener(new OnItemSelectedListener(){
    		@Override
    		public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
    			/* Call when an item is selected so also at the start of the activity to initialize */
    			setSelectedContact();
    			setSelectedLabel();
    			startQuery();
    		}
    		@Override
    		public void onNothingSelected(AdapterView<?> parent) {}
    	});

        resourceCursorAdapter = new EventLogResourceCursorAdapter(this);
        ListView view = (ListView)findViewById(android.R.id.list);
        TextView emptyView = (TextView)findViewById(android.R.id.empty);
        view.setEmptyView(emptyView);
        view.setAdapter(resourceCursorAdapter);

        // Synchronize selected mode with the checked items value
        saveSelectedMode();
        
        // Select the corresponding contact from the intent
        Intent intent = getIntent();
        Uri contactUri = intent.getData();
    	if (contactUri != null) {
	        Cursor cursor = managedQuery(contactUri, null, null, null, null);
	        if (cursor.moveToNext()) {
	        	String selectedContact = cursor.getString(cursor.getColumnIndex(Data.DATA1));
	            if (selectedContact != null) {
	    	        for (int i=0;i<spinner.getAdapter().getCount();i++) {
	    	        	MatrixCursor cursor2 = (MatrixCursor)spinner.getAdapter().getItem(i);
	    	        	if (selectedContact.equalsIgnoreCase(cursor2.getString(1))) {
	    	        		// Select contact
	    	                spinner.setSelection(i);
	    	                spinner.setEnabled(false);
	    	                break;
	    	        	}
	    	        }
	            }
	        }
	        cursor.close();
        }        
	}
	
	private void setSelectedContact() {
	    Spinner spinner = (Spinner)findViewById(R.id.contact);
	    MatrixCursor cursor = (MatrixCursor)spinner.getSelectedItem();
	    currentContactNumber = cursor.getString(1);
    }
	
	private void setSelectedLabel() {
		Spinner spinner = (Spinner)findViewById(R.id.contact);
		MatrixCursor cursor = (MatrixCursor)spinner.getSelectedItem();
	    String label = cursor.getString(2);
    	if (label==null){
			// Label is not custom, get the string corresponding to the phone type
			int type = cursor.getInt(3);
			label = getString(Phone.getTypeLabelResource(type));
		}
    	currentLabel = label;
	}
	
	private void startQuery(){
		try{
			Cursor result = getContentResolver().query(
					Uri.parse(EventLogData.CONTENT_URI+ Integer.toString(selectedMode)), 
					null, buildContactsListSelection() ,null, null);
			resourceCursorAdapter.changeCursor(result);
		}catch(IllegalArgumentException e){
		}
	}
	
	private void startDelete(){
		try{
			getContentResolver().delete(
					Uri.parse(EventLogData.CONTENT_URI+ Integer.toString(selectedMode)), 
					buildContactsListSelection() ,null);
			startQuery();
		}catch(IllegalArgumentException e){
		}
	}
	
	/**
	 * Build the selection parameter for the select query.
	 * Several numbers could be added for one request. In case of an aggregated log per contact, all numbers for the
	 * specified contactId of the contact should be concatenated in the 'selection' string.
	 * 
	 * 'selection' must be formatted as this : " IN ('number1','number2',...)
	 * It is highly recommended to add the international format of the number if it isn't already formatted to be sure to 
	 * get back the RCS events.
	 */
	private String buildContactsListSelection() {
		String selection = " IN (\'" + currentContactNumber + "\'";
		// If number matches a non international phone number
		if (!currentContactNumber.startsWith(PhoneUtils.getCountryCode())){
			selection += ",\'" + PhoneUtils.formatNumberToInternational(currentContactNumber)+"\'";
		}
		selection+=")";
		return selection;
	}
		
	private void confirmDeleteDialog(OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.title_eventlog_confirm_dialog_delete_title);
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setCancelable(true);
        builder.setMessage(R.string.label_eventlog_confirm_delete_message);
        builder.setPositiveButton(R.string.label_delete, listener);
        builder.setNegativeButton(R.string.label_cancel, null);
        builder.show();
    }
	
	/**
	 * Save the selectedMode by taking the checkItems values representation.
	 * Use before showing the filter menu and when activity changes orientation.
	 */
	public void saveSelectedMode(){
		int val = 0;
		for(int i = 0; i< checkedItems.length; i++){
			val += ((int)(checkedItems[i]?1:0))*Math.pow(2,i);
		}
		selectedMode = val;
	}
	
	/**
	 * Restore the checkItems representation by taking the selectedMode value.
	 * Use after showing the filter menu (onCancel) and when activity changes orientation.
	 */
	public void restoreSelectedMode(){
		String binaryString = Integer.toBinaryString(selectedMode);
		
		/* Adding some zeros at the beginning to escape the cases 
		 * where the integer is below a items bits sized value */
		String offset = "";
		int length = items.length-binaryString.length();
		for(int i = 0; i< length; i++){
			offset+='0';
		}
		binaryString = offset + binaryString;	
		for(int i = 0; i< checkedItems.length; i++){
			checkedItems[i] = (binaryString.charAt(checkedItems.length-i-1)=='1'?true:false);
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt("selectedMode", selectedMode);
		super.onSaveInstanceState(outState);
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle state) {
		selectedMode = state.getInt("selectedMode");
		restoreSelectedMode();
		super.onRestoreInstanceState(state);
	}
	
	public String getCurrentLabel(){
		return currentLabel;
	}
	
	public String getCurrentContactNumber(){
		return currentContactNumber;
	}

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater=new MenuInflater(getApplicationContext());
		inflater.inflate(R.menu.menu_evtlog, menu);
		return true;
	}
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_filter:
				saveSelectedMode();
				AlertDialog.Builder builder = new AlertDialog.Builder(EventLog.this);
				builder.setTitle(R.string.title_eventlog_dialog_filter_logs_title);
				builder.setMultiChoiceItems(items, checkedItems, new DialogInterface.OnMultiChoiceClickListener(){
					public void onClick(DialogInterface dialog, int which, boolean isChecked) {
						checkedItems[itemsIndexInCheckedArray[which]] = isChecked;
					}
				});
				builder.setPositiveButton(R.string.label_ok, new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog, int which) {
						filterAlertDialog.dismiss();
						saveSelectedMode();
						startQuery();
					}			
				});
				builder.setNegativeButton(R.string.label_cancel, new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog, int which) {
						filterAlertDialog.dismiss();
						restoreSelectedMode();
					}			
				});
				filterAlertDialog = builder.show();
				break;
			case R.id.menu_clear_log:
				OnClickListener l = new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						startDelete();
					}
				};
				confirmDeleteDialog(l);
				break;
		}
		return true;
	}
}
