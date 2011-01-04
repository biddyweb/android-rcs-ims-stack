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
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.orangelabs.rcs.samples.R;
import com.orangelabs.rcs.samples.utils.Utils;
import com.orangelabs.rcs.service.api.client.presence.PresenceApi;

/**
 * Manage RCS contacts (invite, block, unblock, revoke, .etc)
 * 
 * @author jexa7410
 */
public class ManageContactList extends Activity {

	/**
	 * Presence API
	 */
    public static PresenceApi presenceApi;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.presence_mgt_contact_list);
        
        // Set UI title
        setTitle(R.string.menu_mgt_contact_list);
        
        // Set the contact selector
        Spinner spinner = (Spinner)findViewById(R.id.contact);
        spinner.setAdapter(Utils.createContactListAdapter(this));

        // Set buttons callbacks
        Button btn = (Button)findViewById(R.id.invite);
        btn.setOnClickListener(btnInviteListener);
        btn = (Button)findViewById(R.id.block);
        btn.setOnClickListener(btnBlockListener);
        btn = (Button)findViewById(R.id.revoke);
        btn.setOnClickListener(btnRevokeListener);
        btn = (Button)findViewById(R.id.unblock);
        btn.setOnClickListener(btnUnblockListener);
        btn = (Button)findViewById(R.id.unrevoke);
        btn.setOnClickListener(btnUnrevokeListener);
        
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
     * Invite button listener
     */
    private OnClickListener btnInviteListener = new OnClickListener() {
        public void onClick(View v) {
        	try {
        		// Get the remote contact
                Spinner spinner = (Spinner)findViewById(R.id.contact);
                CursorWrapper cursor = (CursorWrapper)spinner.getSelectedItem();
                String contact = cursor.getString(1);

                // Invite the contact
                if (presenceApi.inviteContact(contact)) {
    				Toast.makeText(ManageContactList.this, R.string.label_invite_contact_ok, Toast.LENGTH_SHORT).show();
        		} else {
    				Utils.showError(ManageContactList.this, getString(R.string.label_invite_contact_ko));
        		}
        	} catch(Exception e) {
				Utils.showError(ManageContactList.this, getString(R.string.label_invite_contact_ko));
        	}
        }
    };

    /**
     * Revoke button listener
     */
    private OnClickListener btnRevokeListener = new OnClickListener() {
        public void onClick(View v) {
        	try {
        		// Get the remote contact
        		Spinner spinner = (Spinner)findViewById(R.id.contact);
                CursorWrapper cursor = (CursorWrapper)spinner.getSelectedItem();
                String contact = cursor.getString(1);

                // Revoke the contact
                if (presenceApi.revokeContact(contact)) {
    				Toast.makeText(ManageContactList.this, R.string.label_revoke_contact_ok, Toast.LENGTH_SHORT).show();
        		} else {
    				Utils.showError(ManageContactList.this, getString(R.string.label_revoke_contact_ko));
        		}
        	} catch(Exception e) {
				Utils.showError(ManageContactList.this, getString(R.string.label_revoke_contact_ko));
        	}
        }
    };
   
    /**
     * Block button listener
     */
    private OnClickListener btnBlockListener = new OnClickListener() {
        public void onClick(View v) {
        	try {
        		// Get the remote contact
        		Spinner spinner = (Spinner)findViewById(R.id.contact);
                CursorWrapper cursor = (CursorWrapper)spinner.getSelectedItem();
                String contact = cursor.getString(1);

                // Block the conatct
                if (presenceApi.rejectSharingInvitation(contact)) {
    				Toast.makeText(ManageContactList.this, R.string.label_block_contact_ok, Toast.LENGTH_SHORT).show();
        		} else {
    				Utils.showError(ManageContactList.this, getString(R.string.label_block_contact_ko));
        		}
        	} catch(Exception e) {
				Utils.showError(ManageContactList.this, getString(R.string.label_block_contact_ko));
        	}
        }
    };

    /**
     * Unblock button listener
     */
    private OnClickListener btnUnblockListener = new OnClickListener() {
        public void onClick(View v) {
        	try {
        		// Get the remote contact
        		Spinner spinner = (Spinner)findViewById(R.id.contact);
                CursorWrapper cursor = (CursorWrapper)spinner.getSelectedItem();
                String contact = cursor.getString(1);

                // Unblock the contact
                if (presenceApi.unblockContact(contact)) {
    				Toast.makeText(ManageContactList.this, R.string.label_unblock_contact_ok, Toast.LENGTH_SHORT).show();
        		} else {
    				Utils.showError(ManageContactList.this, getString(R.string.label_unblock_contact_ko));
        		}
        	} catch(Exception e) {
				Utils.showError(ManageContactList.this, getString(R.string.label_unblock_contact_ko));
        	}
        }
    };

    /**
     * Unrevoke button listener
     */
    private OnClickListener btnUnrevokeListener = new OnClickListener() {
        public void onClick(View v) {
        	try {
        		// Get the remote contact
        		Spinner spinner = (Spinner)findViewById(R.id.contact);
                CursorWrapper cursor = (CursorWrapper)spinner.getSelectedItem();
                String contact = cursor.getString(1);

                // Unrevoke the contact
                if (presenceApi.unrevokeContact(contact)) {
    				Toast.makeText(ManageContactList.this, R.string.label_unrevoke_contact_ok, Toast.LENGTH_SHORT).show();
        		} else {
    				Utils.showError(ManageContactList.this, getString(R.string.label_unrevoke_contact_ko));
        		}
        	} catch(Exception e) {
				Utils.showError(ManageContactList.this, getString(R.string.label_unrevoke_contact_ko));
        	}
        }
    };
}
