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

package com.orangelabs.rcs.ri.messaging;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;

import com.orangelabs.rcs.ri.R;
import com.orangelabs.rcs.ri.utils.Utils;
import com.orangelabs.rcs.service.api.client.ClientApiListener;
import com.orangelabs.rcs.service.api.client.contacts.ContactsApi;
import com.orangelabs.rcs.service.api.client.messaging.IChatSession;
import com.orangelabs.rcs.service.api.client.messaging.MessagingApi;
import com.orangelabs.rcs.utils.PhoneUtils;

/**
 * List of current chat sessions and blocked contacts
 */
public class ChatList extends ListActivity implements ClientApiListener, OnItemClickListener{
	/**
	 * Position of headers
	 */
	private static final int ACTIVE_IM_HEADER_POSITION = 0;
	private static int IM_CAPABLE_CONTACTS_HEADER_POSITION = 0;
	
	/**
	 * View IDs
	 */
	private static final int HEADER_VIEW_TYPE = 0;
	private static final int ACTIVE_IM_VIEW_TYPE = 1;
	private static final int IM_CAPABLE_VIEW_TYPE = 2;

    /**
     * UI handler
     */
    private Handler handler = new Handler();
    
    /**
	 * List of contacts supporting the chat feature
	 */
	private List<ImCapableElement> imCapableElements = new ArrayList<ImCapableElement>();
	
	/**
	 * List of current chat sessions
	 */
	private List<ImElement> activeImSessionsElements = new ArrayList<ImElement>();
	
	/**
	 * Layout
	 */
	private LayoutInflater layoutInflater;
	
	/**
	 * Messaging API
	 */
	private MessagingApi messagingApi;
	
	/**
	 * Contacts API
	 */
    private ContactsApi contactsApi;
    
    /**
	 * List adapter
	 */
	private ChatListAdapter chatListAdapter; 
   
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        // Set layout
		layoutInflater = LayoutInflater.from(getApplicationContext());

		// Set UI title
        setTitle(getString(R.string.menu_chat_list));

        // Instantiate messaging API
		messagingApi = new MessagingApi(getApplicationContext());
		
        // Instantiate contacts API
        contactsApi = new ContactsApi(getApplicationContext());
        
        // Set list adapter
		chatListAdapter = new ChatListAdapter();
		setListAdapter(chatListAdapter);
		getListView().setOnItemClickListener(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		if (messagingApi != null) {
			messagingApi.addApiEventListener(this);
			messagingApi.connectApi();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		
		if (messagingApi != null) {
			messagingApi.removeApiEventListener(this);
			messagingApi.disconnectApi();
		}
	}
		
	/**
	 * Chat session list adapter
	 */
	private class ChatListAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			return 2 + imCapableElements.size()+ activeImSessionsElements.size(); //  2 headers + lists size
		}

		@Override
		public Object getItem(int position) {
			if (isImCapableItem(position)){
				return imCapableElements.get((int)getItemId(position));
			}
			if (isAnActiveImSession(position)){
				return activeImSessionsElements.get((int)getItemId(position));
			}
			return null;
		}

		@Override
		public long getItemId(int position) {
			if (position == ACTIVE_IM_HEADER_POSITION){
				return 0;
			}
			if (position == IM_CAPABLE_CONTACTS_HEADER_POSITION){
				return 0;
			}
			if (isAnActiveImSession(position)){
				return position-1;
			}
			if (isImCapableItem(position)){
				return position - activeImSessionsElements.size()-2;
			}
			throw new IllegalArgumentException("Position not handled"); 
		}
		
		@Override
		public int getViewTypeCount() {
			return 3;	
		}
		
		@Override
		public int getItemViewType(int position) {
			if (position == ACTIVE_IM_HEADER_POSITION || position == IM_CAPABLE_CONTACTS_HEADER_POSITION){
				return HEADER_VIEW_TYPE;
			}else if (isImCapableItem(position)){
				return IM_CAPABLE_VIEW_TYPE;
			}else if (isAnActiveImSession(position)) {
				return ACTIVE_IM_VIEW_TYPE;
			} 
			throw new IllegalArgumentException("No view type defined for position "+position);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			if (position == ACTIVE_IM_HEADER_POSITION){
				if (convertView == null) {
					convertView = (TextView)layoutInflater.inflate(R.layout.messaging_chat_list_header, null);
				}
				((TextView)convertView).setText(getString(R.string.label_active_im_sessions_header));
				return convertView;
			}
			
			if (isAnActiveImSession(position)){
				if (convertView == null) {
					convertView = (TextView)layoutInflater.inflate(android.R.layout.simple_list_item_1, null);
				}
				int id=(int) getItemId(position);
				((TextView)convertView).setText(activeImSessionsElements.get(id).contact);
				return convertView;
			}
			
			if (position ==  IM_CAPABLE_CONTACTS_HEADER_POSITION){
				if (convertView == null) {
					convertView = (TextView)layoutInflater.inflate(R.layout.messaging_chat_list_header, null);
				}
				((TextView)convertView).setText(getString(R.string.label_im_capable_contacts_header));
				return convertView;
			}
			
			if (isImCapableItem(position)){
				if (convertView == null) {
					convertView = (CheckedTextView)layoutInflater.inflate(android.R.layout.simple_list_item_checked, null);
				}
				int id = (int)getItemId(position);
				((CheckedTextView)convertView).setText(imCapableElements.get(id).contact);
				((CheckedTextView)convertView).setChecked(imCapableElements.get(id).blocked);
				return convertView;
			}
			
			throw new IllegalArgumentException("Item not handled in the list");
		}
	}

	private boolean isAnActiveImSession(int position){
		return (position > ACTIVE_IM_HEADER_POSITION) && (position <= ACTIVE_IM_HEADER_POSITION + activeImSessionsElements.size());
	}
	
	private boolean isImCapableItem(int position){
		return (position > IM_CAPABLE_CONTACTS_HEADER_POSITION) && (position <= IM_CAPABLE_CONTACTS_HEADER_POSITION + imCapableElements.size());
	}
	
    /**
     * API disabled
     */
	public void handleApiDisabled() {
		handler.post(new Runnable() { 
			public void run() {
				Utils.showMessageAndExit(ChatList.this, getString(R.string.label_api_disabled));
			}
		});
	}

    /**
     * API connected
     */
	public void handleApiConnected() {
		updateDataSet();
	}

    /**
     * API disconnected
     */
	public void handleApiDisconnected() {
		handler.post(new Runnable(){
			public void run(){
				Utils.showMessageAndExit(ChatList.this, getString(R.string.label_api_disconnected));
			}
		});	
	}
	
	/**
	 * Update data
	 */
	private void updateDataSet(){
		// Reset lists
		activeImSessionsElements.clear();
		imCapableElements.clear();

		try {
			// Get active chat sessions
			List<IBinder> chatSessionsBinder = messagingApi.getChatSessions();
			for (IBinder binder : chatSessionsBinder) {
				IChatSession chatSession = IChatSession.Stub.asInterface(binder);
				String contact;
				if (chatSession.isChatGroup()) {
					contact = "Group chat";
				} else {
					contact = PhoneUtils.extractNumberFromUri(chatSession.getRemoteContact());
				}
				String sessionID = chatSession.getSessionID();
				ImElement imElements=new ImElement(contact, sessionID);
				activeImSessionsElements.add(imElements);
			}
		} catch (Exception e) {
			Utils.showMessageAndExit(ChatList.this, getString(R.string.label_api_failed));
		}

		// Set 2nd header offset
		IM_CAPABLE_CONTACTS_HEADER_POSITION = activeImSessionsElements.size() + 1;
		
		// Get blocked contacts
		List<String> blockedContacts = contactsApi.getBlockedContactsForIm();
		
		// Get chat capable contacts
		List<String> chatCapableContacts = contactsApi.getImSessionCapableContacts();
		for (String  contact : chatCapableContacts) {
			if (blockedContacts.contains(contact)){
				imCapableElements.add(new ImCapableElement(contact, true));
			} else {
				imCapableElements.add(new ImCapableElement(contact, false));
			}
		}
		chatListAdapter.notifyDataSetChanged();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_chat_list, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.refresh_menu){
			updateDataSet();
		}
		return true;
	}
	
	@Override
	public void onItemClick(AdapterView<?> parentView, View view, int position, long id) {
		if (isImCapableItem(position)) {
			String contact = (String) ((ImCapableElement) getListView().getItemAtPosition(position)).contact;
			if (((ImCapableElement) getListView().getItemAtPosition(position)).blocked) {
				contactsApi.setImBlockedForContact(contact, false);
				((CheckedTextView) view).setChecked(false);
				((ImCapableElement) getListView().getItemAtPosition(position)).blocked = false;
				Utils.displayToast(this, getString(R.string.label_contact_unblocked, contact));
			} else {
				contactsApi.setImBlockedForContact(contact, true);
				((ImCapableElement) getListView().getItemAtPosition(position)).blocked = true;
				((CheckedTextView) view).setChecked(true);
				Utils.displayToast(this, getString(R.string.label_contact_blocked, contact));
			}
		}
		if (isAnActiveImSession(position)) {
			Intent intent=new Intent(getApplicationContext(), ChatView.class);
			ImElement imElements = (ImElement)parentView.getItemAtPosition(position);
			intent.putExtra("sessionId", imElements.sessionId);
			startActivity(intent);
		}
	}
	
	/**
	 * IM list item
	 */
	private class ImElement{
		private String contact;
		private String sessionId;
		public ImElement(String contact, String sessionId) {
			super();
			this.contact = contact;
			this.sessionId = sessionId;
		}
	}
	
	/**
	 * Contact list item
	 */
	private class ImCapableElement{
		private String contact;
		private boolean blocked;
		public ImCapableElement(String contact, boolean blocked) {
			super();
			this.contact = contact;
			this.blocked = blocked;
		}
	}
}