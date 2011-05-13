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

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.orangelabs.rcs.ri.R;
import com.orangelabs.rcs.service.api.client.eventslog.EventsLogApi;

/**
 * EventLog Adapter based on a ResourceCursorAdapter which gets its data from 
 * the EventLogProvider.
 * 
 * @author mhsm6403
 */
public class EventLogResourceCursorAdapter extends ResourceCursorAdapter{
	
	/**
	 * Drawables that will be displayed in eventLog
	 */
	private Drawable mDrawableIncomingFailed;
	private Drawable mDrawableOutgoingFailed;
	private Drawable mDrawableIncoming;
	private Drawable mDrawableOutgoing;
	private Drawable mDrawableMissed;
	private Drawable mDrawableCall;
	private Drawable mDrawableSms;
	private Drawable mDrawableMms;
	private Drawable mDrawableChat;
	private Drawable mDrawableRichCall;
	private Drawable mDrawableFileTransfer;
	
	public EventLogResourceCursorAdapter(Context context) {
		super(context, R.layout.eventlog_list_item, null);
		
		/**
		 * Load the Drawables to use in the bindView method.
		 */
		mDrawableIncomingFailed = context.getResources().getDrawable(R.drawable.ri_eventlog_list_incoming_call_failed);
		mDrawableOutgoingFailed = context.getResources().getDrawable(R.drawable.ri_eventlog_list_outgoing_call_failed);
		mDrawableIncoming = context.getResources().getDrawable(R.drawable.ri_eventlog_list_incoming_call);
		mDrawableOutgoing = context.getResources().getDrawable(R.drawable.ri_eventlog_list_outgoing_call);
		mDrawableMissed = context.getResources().getDrawable(R.drawable.ri_eventlog_list_missed_call);
		mDrawableCall = context.getResources().getDrawable(android.R.drawable.sym_action_call);
		mDrawableSms = context.getResources().getDrawable(R.drawable.ri_eventlog_sms);
		mDrawableMms = context.getResources().getDrawable(R.drawable.ri_eventlog_mms);
		mDrawableChat = context.getResources().getDrawable(R.drawable.ri_eventlog_chat);
		mDrawableRichCall = context.getResources().getDrawable(R.drawable.ri_eventlog_csh);
		mDrawableFileTransfer = context.getResources().getDrawable(R.drawable.ri_eventlog_filetransfer);
	}
	
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		TextView line1View = (TextView) view.findViewById(R.id.line1); 
		TextView labelView = (TextView) view.findViewById(R.id.label);
		TextView numberView = (TextView) view.findViewById(R.id.number);
		TextView dateView = (TextView) view.findViewById(R.id.date);
		
		ImageView eventDirectionIconView = (ImageView) view.findViewById(R.id.call_type_icon);
		ImageView eventIconView = (ImageView) view.findViewById(R.id.call_icon);
		
		/* Set the number */
		String number = cursor.getString(EventsLogApi.CONTACT_COLUMN);
		numberView.setText(number);
		numberView.setVisibility(View.VISIBLE);
		
		/* Set the label of the phone number */
		labelView.setText(((EventLog) context).getCurrentLabel());
		labelView.setVisibility(View.VISIBLE);
		
		/* Set the date/time field by mixing relative and absolute times. */
		long date = cursor.getLong(EventsLogApi.DATE_COLUMN);		
		dateView.setText(DateUtils.getRelativeTimeSpanString(date,
				System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS,
				DateUtils.FORMAT_ABBREV_RELATIVE));
		
		/* Set the status text and destination icon */
		int type = cursor.getInt(EventsLogApi.TYPE_COLUMN);
		int status = cursor.getInt(EventsLogApi.STATUS_COLUMN);
		switch (type) {
		case EventsLogApi.TYPE_INCOMING_CHAT_MESSAGE:
		case EventsLogApi.TYPE_INCOMING_GROUP_CHAT_MESSAGE:
		case EventsLogApi.TYPE_INCOMING_FILE_TRANSFER:
		case EventsLogApi.TYPE_INCOMING_GSM_CALL:
		case EventsLogApi.TYPE_INCOMING_RICH_CALL:
		case EventsLogApi.TYPE_INCOMING_SMS:
			if(status==EventsLogApi.STATUS_FAILED)
				eventDirectionIconView.setImageDrawable(mDrawableIncomingFailed);
			else 
				eventDirectionIconView.setImageDrawable(mDrawableIncoming);
			break;
		case EventsLogApi.TYPE_OUTGOING_CHAT_MESSAGE:
		case EventsLogApi.TYPE_OUTGOING_GROUP_CHAT_MESSAGE:
		case EventsLogApi.TYPE_OUTGOING_FILE_TRANSFER:
		case EventsLogApi.TYPE_OUTGOING_GSM_CALL:
		case EventsLogApi.TYPE_OUTGOING_RICH_CALL:
		case EventsLogApi.TYPE_OUTGOING_SMS:
			if(status==EventsLogApi.STATUS_FAILED)
				eventDirectionIconView.setImageDrawable(mDrawableOutgoingFailed);
			else 
				eventDirectionIconView.setImageDrawable(mDrawableOutgoing);
			break;
		case EventsLogApi.TYPE_MISSED_GSM_CALL:
			eventDirectionIconView.setImageDrawable(mDrawableMissed);
			break;
		}
		
		
		/* Set icon and data*/
		/* Set the line text */
		String data = cursor.getString(EventsLogApi.DATA_COLUMN);
		String mimeType = cursor.getString(EventsLogApi.MIMETYPE_COLUMN);
		if(data!=null){
			line1View.setText(data);
		}else{
			line1View.setText(mimeType);
		}
		
		switch(type){
		case EventsLogApi.TYPE_INCOMING_GSM_CALL:
		case EventsLogApi.TYPE_OUTGOING_GSM_CALL:
			eventIconView.setImageDrawable(mDrawableCall);
			line1View.setText(DateUtils.formatElapsedTime(Long.parseLong(data)));
			break;
		
		case EventsLogApi.TYPE_GROUP_CHAT_SYSTEM_MESSAGE:
		case EventsLogApi.TYPE_INCOMING_GROUP_CHAT_MESSAGE:
		case EventsLogApi.TYPE_OUTGOING_GROUP_CHAT_MESSAGE:
			line1View.setText(R.string.label_eventlog_group_chat);
			eventIconView.setImageDrawable(mDrawableChat);
			break;
		case EventsLogApi.TYPE_OUTGOING_CHAT_MESSAGE:
		case EventsLogApi.TYPE_INCOMING_CHAT_MESSAGE:
		case EventsLogApi.TYPE_CHAT_SYSTEM_MESSAGE:
			line1View.setText(R.string.label_eventlog_chat);
			eventIconView.setImageDrawable(mDrawableChat);
			break;
		case EventsLogApi.TYPE_INCOMING_FILE_TRANSFER:
		case EventsLogApi.TYPE_OUTGOING_FILE_TRANSFER:
			eventIconView.setImageDrawable(mDrawableFileTransfer);
			break;
		case EventsLogApi.TYPE_INCOMING_SMS:
		case EventsLogApi.TYPE_OUTGOING_SMS:
			if(mimeType.contains("mms")){
				eventIconView.setImageDrawable(mDrawableMms);
			}else{
				eventIconView.setImageDrawable(mDrawableSms);
			}
			break;
		case EventsLogApi.TYPE_INCOMING_RICH_CALL:
		case EventsLogApi.TYPE_OUTGOING_RICH_CALL:
			eventIconView.setImageDrawable(mDrawableRichCall);
			break;
		}
	}		
}
