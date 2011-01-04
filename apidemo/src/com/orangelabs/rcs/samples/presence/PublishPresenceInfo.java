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

import java.io.ByteArrayOutputStream;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.orangelabs.rcs.core.ims.service.presence.PhotoIcon;
import com.orangelabs.rcs.core.ims.service.presence.PresenceInfo;
import com.orangelabs.rcs.provider.eab.RichAddressBook;
import com.orangelabs.rcs.samples.R;
import com.orangelabs.rcs.service.api.client.presence.PresenceApi;

/**
 * Publish my presence info
 * 
 * @author jexa7410
 */
public class PublishPresenceInfo extends Activity {
	/**
	 * Presence info
	 */
	private PresenceInfo presenceInfo = null;

	/**
	 * Presence API
	 */
    public PresenceApi presenceApi;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.presence_publish_presence);
        
        // Set UI title
        setTitle(R.string.menu_publish_presence_info);
        
        // Instanciate the RCS contact content provider
        RichAddressBook.createInstance(getApplicationContext());
        
		// Display the current presence info from the RCS contacts content provider
    	presenceInfo = RichAddressBook.getInstance().getMyPresenceInfo();
        EditText freetextEdit = (EditText)findViewById(R.id.freetext);
    	freetextEdit.setText(presenceInfo.getFreetext());
        EditText favoritelinkEdit = (EditText)findViewById(R.id.favoritelink);
		favoritelinkEdit.setText(presenceInfo.getFavoriteLinkUrl());
        if (presenceInfo.getPhotoIcon() != null) {
            ImageView photoView = (ImageView)findViewById(R.id.photo);
            byte[] data = presenceInfo.getPhotoIcon().getContent();
            Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
        	photoView.setImageBitmap(bmp);
        }
        
        // Set buttons callback
        Button btn = (Button)findViewById(R.id.publish);
        btn.setOnClickListener(btnPublishPresenceListener);
        btn = (Button)findViewById(R.id.select);
        btn.setOnClickListener(btnSelectPhotoListener);
        btn = (Button)findViewById(R.id.delete);
        btn.setOnClickListener(btnDeletePhotoListener);
        
        // Instanciate presence API
        presenceApi = new PresenceApi(getApplicationContext());
        presenceApi.connectApi();
    }

    @Override
    public void onDestroy() {
    	super.onDestroy();
    	
        // Disconnect presence API
    	presenceApi.disconnectApi();
    }
    
    /**
     * Publish button listener
     */
    private OnClickListener btnPublishPresenceListener = new OnClickListener() {
        public void onClick(View v) {
        	try {
        		// Get the new presence info to be published
                EditText freetextEdit = (EditText)findViewById(R.id.freetext);
                presenceInfo.setFreetext(freetextEdit.getText().toString());
                EditText favoritelinkEdit = (EditText)findViewById(R.id.favoritelink);
                presenceInfo.setFavoriteLinkUrl(favoritelinkEdit.getText().toString());
                
                // Publish the new presence info
                if (presenceApi.setMyPresenceInfo(presenceInfo)) {
    				Toast.makeText(PublishPresenceInfo.this, R.string.label_publish_ok, Toast.LENGTH_SHORT).show();
        		} else {
    				Toast.makeText(PublishPresenceInfo.this, R.string.label_publish_ko, Toast.LENGTH_SHORT).show();
        		}
        	} catch(Exception e) {
				Toast.makeText(PublishPresenceInfo.this, R.string.label_publish_ko, Toast.LENGTH_SHORT).show();
        	}
        }
    };

    /**
     * Delete photo button listener
     */
    private OnClickListener btnDeletePhotoListener = new OnClickListener() {
        public void onClick(View v) {
            ImageView photoView = (ImageView)findViewById(R.id.photo);
            photoView.setImageBitmap(null);
            presenceInfo.setPhotoIcon(null);
        }
    };

    /**
     * Select photo button listener
     */
    private OnClickListener btnSelectPhotoListener = new OnClickListener() {
        public void onClick(View v) {
        	// Select a photo from the gallery
        	Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.putExtra("crop", "true");
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("outputX", 96);
            intent.putExtra("outputY", 96);
            intent.putExtra("return-data", true);
            startActivityForResult(intent, 0);            
        }
    };

    /**
     * On activity result
     * 
     * @param requestCode Request code
     * @param resultCode Result code
     * @param data Data
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (resultCode != RESULT_OK) {
    		return;
    	}
    	
        switch(requestCode) {
            case 0: {
            	if (data != null) {
            		// Display the selected photo
            		Bundle extras = data.getExtras();            		
            		Bitmap bmp = extras.getParcelable("data");
                    ImageView photoView = (ImageView)findViewById(R.id.photo);
                    photoView.setImageBitmap(bmp);
                    
					ByteArrayOutputStream stream = new ByteArrayOutputStream();
					bmp.compress(Bitmap.CompressFormat.JPEG, 75, stream);
					
					byte[] content = stream.toByteArray();
					PhotoIcon photoIcon = new PhotoIcon(content, bmp.getWidth(), bmp.getHeight());
                    presenceInfo.setPhotoIcon(photoIcon);
            	}
	    	}             
            break;
        }
    }
}
