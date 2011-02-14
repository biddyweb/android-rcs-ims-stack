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
package com.orangelabs.rcs.ri.presence;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.database.CursorWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

import com.orangelabs.rcs.core.ims.service.presence.PresenceInfo;
import com.orangelabs.rcs.provider.eab.RichAddressBook;
import com.orangelabs.rcs.ri.R;
import com.orangelabs.rcs.ri.utils.Utils;

/**
 * Show RCS contacts content provider
 * 
 * @author jexa7410
 */
public class ShowEab extends Activity {
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set layout
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.presence_eab);
        
        // Set title
        setTitle(R.string.menu_eab);
        
        // Set the contact selector
        Spinner spinner = (Spinner)findViewById(R.id.contact);
        spinner.setAdapter(Utils.createContactListAdapter(this));
        spinner.setOnItemSelectedListener(listChangeListener);
        
        // Instanciate the RCS contacts content provider
        RichAddressBook.createInstance(getApplicationContext());
    }

    /**
     * Returns the selected contact
     * 
     * @return Contact
     */
    private String getSelectedContact() {
	    Spinner spinner = (Spinner)findViewById(R.id.contact);
	    CursorWrapper cursor = (CursorWrapper)spinner.getSelectedItem();
	    return cursor.getString(1);
    }
    /**
     * Display the contact info of the selected RCS contact
     */
    private void displayContactInfo() {
    	try {
	        String contact = getSelectedContact();
	        PresenceInfo presenceInfo = RichAddressBook.getInstance().getContactPresenceInfo(contact);

	        TextView msisdn = (TextView)findViewById(R.id.phone_number);
	        msisdn.setText(contact);
	        
	        if (presenceInfo != null) {
		        TextView type = (TextView)findViewById(R.id.type);
		        type.setText(getString(R.string.label_rcs_contact));
		        
		        TextView lastModified = (TextView)findViewById(R.id.last_modified);
		        lastModified.setText(Utils.formatDateToString(presenceInfo.getTimestamp()));
		        
		        ImageView photoView = (ImageView)findViewById(R.id.photo);
		        if (presenceInfo.getPhotoIcon() != null) {
		            byte[] data = presenceInfo.getPhotoIcon().getContent();
		            Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
		        	photoView.setImageBitmap(bmp);
		        } else {
		        	photoView.setImageResource(R.drawable.ri_no_photo_icon);
		        }
		        
		    	TextView status = (TextView)findViewById(R.id.presence_status);
		    	status.setText(presenceInfo.getPresenceStatus());

				TextView freetextEdit = (TextView)findViewById(R.id.freetext);
		    	freetextEdit.setText(presenceInfo.getFreetext());
		    	
		    	TextView favoritelinkEdit = (TextView)findViewById(R.id.favoritelink);
				favoritelinkEdit.setText(presenceInfo.getFavoriteLinkUrl());

				CheckBox imageCSh = (CheckBox)findViewById(R.id.image_sharing);
				imageCSh.setVisibility(View.VISIBLE);
				imageCSh.setChecked(presenceInfo.getCapabilities().isImageSharingSupported());
				CheckBox videoCSh = (CheckBox)findViewById(R.id.video_sharing);
				videoCSh.setVisibility(View.VISIBLE);
				videoCSh.setChecked(presenceInfo.getCapabilities().isVideoSharingSupported());
				CheckBox ft = (CheckBox)findViewById(R.id.file_transfer);
				ft.setVisibility(View.VISIBLE);
				ft.setChecked(presenceInfo.getCapabilities().isFileTransferSupported());
				CheckBox im = (CheckBox)findViewById(R.id.im);
				im.setVisibility(View.VISIBLE);
				im.setChecked(presenceInfo.getCapabilities().isImSessionSupported());
				CheckBox csVideo = (CheckBox)findViewById(R.id.cs_video);
				csVideo.setVisibility(View.VISIBLE);
				csVideo.setChecked(presenceInfo.getCapabilities().isCsVideoSupported());
	        } else {
		        TextView type = (TextView)findViewById(R.id.type);
		        type.setText(getString(R.string.label_normal_contact));
		        
		        TextView lastModified = (TextView)findViewById(R.id.last_modified);
		        lastModified.setText("");
		        
		        ImageView photoView = (ImageView)findViewById(R.id.photo);
		        photoView.setImageBitmap(null);
		        
		    	TextView status = (TextView)findViewById(R.id.presence_status);
		    	status.setText("");

		    	TextView freetextEdit = (TextView)findViewById(R.id.freetext);
		    	freetextEdit.setText("");
		    	
		    	TextView favoritelinkEdit = (TextView)findViewById(R.id.favoritelink);
				favoritelinkEdit.setText("");
				
				CheckBox imageCSh = (CheckBox)findViewById(R.id.image_sharing);
				imageCSh.setVisibility(View.GONE);
				CheckBox videoCSh = (CheckBox)findViewById(R.id.video_sharing);
				videoCSh.setVisibility(View.GONE);
				CheckBox ft = (CheckBox)findViewById(R.id.file_transfer);
				ft.setVisibility(View.GONE);
				CheckBox im = (CheckBox)findViewById(R.id.im);
				im.setVisibility(View.GONE);
				CheckBox csVideo = (CheckBox)findViewById(R.id.cs_video);
				csVideo.setVisibility(View.GONE);
	        }
		} catch(Exception e) {
			e.printStackTrace();
			Utils.showMessageAndExit(ShowEab.this, getString(R.string.label_read_eab_ko));
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
        	ShowEab.this.displayContactInfo();
        }
        
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };
}
