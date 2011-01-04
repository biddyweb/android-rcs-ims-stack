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
package com.orangelabs.rcs.samples.presence;

import android.app.Activity;
import android.database.CursorWrapper;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

import com.orangelabs.rcs.core.ims.service.presence.PresenceInfo;
import com.orangelabs.rcs.provider.eab.RichAddressBook;
import com.orangelabs.rcs.samples.R;
import com.orangelabs.rcs.samples.utils.Utils;

/**
 * Show RCS contacts content provider
 * 
 * @author jexa7410
 */
public class ShowEab extends Activity {
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.presence_show_eab);
        
        // Set UI title
        setTitle(R.string.menu_show_eab);
        
        // Set the contact selector
        Spinner spinner = (Spinner)findViewById(R.id.contact);
        spinner.setAdapter(Utils.createContactListAdapter(this));
        spinner.setOnItemSelectedListener(listChangeListener);
        
        // Instanciate the RCS contacts content provider
        RichAddressBook.createInstance(getApplicationContext());
        
        // Display the contact info of the fisrt contact by default
        displayContactInfo(0);
    }

    /**
     * Display the contact info of the selected RCS contact
     * 
     * @param position Item position
     */
    private void displayContactInfo(int position) {
    	try {
	        Spinner spinner = (Spinner)findViewById(R.id.contact);
	        CursorWrapper cursor = (CursorWrapper)spinner.getItemAtPosition(position);
	        String contact = cursor.getString(1);
	        PresenceInfo info = RichAddressBook.getInstance().getContactPresenceInfo(contact);
	        TextView txt = (TextView)findViewById(R.id.result);
        	String value = "Phone number: " + contact + "\n";
	        if (info != null) {
	        	value = value + "Type: RCS contact" + "\n"; 
	        	value = value + info.toString(); 
	        } else {
	        	value = value + "Type: not a RCS contact" + "\n";
	        }
	        txt.setText(value);
		} catch(Exception e) {
			Utils.showError(ShowEab.this, getString(R.string.label_read_eab_ko));
		}
    }

    @Override
    public void onDestroy() {
    	super.onDestroy();
    }

    /**
     * Contact selector listener
     */
    private OnItemSelectedListener listChangeListener = new OnItemSelectedListener() {
        public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
        	ShowEab.this.displayContactInfo(position);
        }
        
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };
}
