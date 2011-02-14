package com.orangelabs.rcs.ri.eventlog;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.orangelabs.rcs.provider.eventlogs.EventLogData;
import com.orangelabs.rcs.ri.R;

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
		String number = cursor.getString(EventLogData.COLUMN_EVENT_PHONE_NUMBER);
		numberView.setText(number);
		numberView.setVisibility(View.VISIBLE);
		
		/* Set the label of the phone number */
		labelView.setText(((EventLog) context).getCurrentLabel());
		labelView.setVisibility(View.VISIBLE);
		
		/* Set the date/time field by mixing relative and absolute times. */
		long date = cursor.getLong(EventLogData.COLUMN_EVENT_DATE);		
		dateView.setText(DateUtils.getRelativeTimeSpanString(date,
				System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS,
				DateUtils.FORMAT_ABBREV_RELATIVE));
		
		/* Set the status text and destination icon */
		int status = cursor.getInt(EventLogData.COLUMN_EVENT_STATUS);
		int dest = cursor.getInt(EventLogData.COLUMN_EVENT_DESTINATION);
		switch (dest) {
		case EventLogData.VALUE_EVENT_DEST_INCOMING:
			if(status==EventLogData.VALUE_EVENT_STATUS_FAILED)
				eventDirectionIconView.setImageDrawable(mDrawableIncomingFailed);
			else 
				eventDirectionIconView.setImageDrawable(mDrawableIncoming);
			break;
		case EventLogData.VALUE_EVENT_DEST_OUTGOING:
			if(status==EventLogData.VALUE_EVENT_STATUS_FAILED)
				eventDirectionIconView.setImageDrawable(mDrawableOutgoingFailed);
			else 
				eventDirectionIconView.setImageDrawable(mDrawableOutgoing);
			break;
		case EventLogData.VALUE_EVENT_DEST_MISSED:
			eventDirectionIconView.setImageDrawable(mDrawableMissed);
			break;
		}
		
		
		/* Set icon and data*/
		/* Set the line text */
		String data = cursor.getString(EventLogData.COLUMN_EVENT_DATA);
		String mimeType = cursor.getString(EventLogData.COLUMN_EVENT_MIMETYPE);
		if(data!=null){
			line1View.setText(data);
		}else{
			line1View.setText(mimeType);
		}
		int type = cursor.getInt(EventLogData.COLUMN_EVENT_TYPE);
		switch(type){
		case EventLogData.VALUE_EVENT_TYPE_CALL:
			eventIconView.setImageDrawable(mDrawableCall);
			line1View.setText(DateUtils.formatElapsedTime(Long.parseLong(data)));
			break;
		case EventLogData.VALUE_EVENT_TYPE_CHAT:
			eventIconView.setImageDrawable(mDrawableChat);
			if(status==EventLogData.VALUE_EVENT_STATUS_STARTED){
				line1View.setText(R.string.label_eventlog_session_started);
			}else if(status==EventLogData.VALUE_EVENT_STATUS_TERMINATED){
				line1View.setText(R.string.label_eventlog_session_terminated);
			}else if(status==EventLogData.VALUE_EVENT_STATUS_FAILED){
				line1View.setText(R.string.label_eventlog_session_failed);
			}
			break;
		case EventLogData.VALUE_EVENT_TYPE_FILETRANSFER:
			eventIconView.setImageDrawable(mDrawableFileTransfer);
			break;
		case EventLogData.VALUE_EVENT_TYPE_MMS_SMS:
			if(mimeType.equals(EventLogData.MMS_MIMETYPE)){
				eventIconView.setImageDrawable(mDrawableMms);
			}else{
				eventIconView.setImageDrawable(mDrawableSms);
			}
			break;
		case EventLogData.VALUE_EVENT_TYPE_RICH_CALL:
			eventIconView.setImageDrawable(mDrawableRichCall);
			break;
		}
	}		
}
