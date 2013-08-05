package com.orangelabs.rcs.ri.ipcall;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

import com.orangelabs.rcs.provider.settings.RcsSettings;
import com.orangelabs.rcs.ri.R;
import com.orangelabs.rcs.ri.utils.Utils;
import com.orangelabs.rcs.service.api.client.ClientApiException;
import com.orangelabs.rcs.service.api.client.ClientApiListener;
import com.orangelabs.rcs.service.api.client.ImsEventListener;
import com.orangelabs.rcs.service.api.client.ipcall.IIPCallApi;
import com.orangelabs.rcs.service.api.client.ipcall.IIPCallSession;
import com.orangelabs.rcs.service.api.client.ipcall.IPCallApi;
import com.orangelabs.rcs.utils.logger.Logger;

public class InitiateIPCallActivity extends Activity implements
ClientApiListener, ImsEventListener {

	/**
	 * Logger
	 */
	private static Logger logger = Logger.getLogger(InitiateIPCallActivity.class.getName());
	
	/**
	 * UI handler
	 */
	Handler handler = new Handler() ;
	
	/**
	 * synchronization object
	 */
	Object callApiConnected = new Object(); 
	
	/**
	 * audio call button
	 */
	Button audioVideoInviteBtn ;  
	
	/**
	 * audio+video call button
	 */
	Button audioInviteBtn ;
	
	/**
	 * contacts list spinner
	 */
	Spinner spinner ;
	
	
	 /* *****************************************
     *                Activity
     ***************************************** */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (logger.isActivated()) {
			logger.info("onCreate()");
		}
        // Set layout
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.ipcall_initiate_call);

        // Set title
        setTitle(R.string.menu_initiate_ipcall);
        
        // get Buttons listview and spinner 
        audioVideoInviteBtn = (Button)findViewById(R.id.audio_video_invite_btn);
    	audioInviteBtn = (Button)findViewById(R.id.audio_invite_btn);
    	spinner = (Spinner)findViewById(R.id.contact);	
    	
    	audioInviteBtn.setEnabled(false);
     	audioVideoInviteBtn.setEnabled(false);
    	
    	 RcsSettings.createInstance(getApplicationContext());
    	 
         // Set the contact selector 
         spinner.setAdapter(Utils.createRcsContactListAdapter(this)); 

         // Set button listeners
         audioVideoInviteBtn.setOnClickListener(audioVideoInviteBtnListener);
         audioInviteBtn.setOnClickListener(audioInviteBtnListener);


         // wait Api connection -  activate buttons when connected
        Thread thread = new Thread(){
       	 public void run() {
       		
           		synchronized (callApiConnected) {
           			while (!IPCallSessionsData.isCallApiConnected) {
           			if (logger.isActivated()) {
               			logger.info("wait Api connected");
               		}
           			try {
						callApiConnected.wait(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
           			if (logger.isActivated()) {
               			logger.info("sortie wait");
               		}         			
           			
           			//activate buttons if at least one contact
           	        handler.post(new Runnable() {
           	        	public void run(){
           	        		if (spinner.getAdapter().getCount() > 0) { 
           	        			audioVideoInviteBtn.setEnabled(true);
                   	        	audioInviteBtn.setEnabled(true);
           	        		}
           	        	}
           	        });
           			}
           		}
           		if (logger.isActivated()) {
					logger.info("callApi connected");
				}
       	}
       };
       thread.start();
       
       	// Instantiate IP call API and connect it
   		if (IPCallSessionsData.callApi == null) {
   			IPCallSessionsData.callApi = new IPCallApi(getApplicationContext());
   			
   		}
   		IPCallSessionsData.callApi.addApiEventListener(this);
  			IPCallSessionsData.callApi.addImsEventListener(this);
  			IPCallSessionsData.callApi.connectApi();
		
  			
  
  	// if incoming session (got from notif) => launch IPCallSessionActivity
  			Intent receivedIntent = getIntent();
  			if ((receivedIntent != null) && (receivedIntent.getAction()!= null)){
  				if (receivedIntent.getAction().equals("incoming")){
  					Intent  launchIntent = new Intent(receivedIntent);  
  					launchIntent.setClass(getApplicationContext(), IPCallSessionActivity.class);
  	  				getApplicationContext().startActivity(launchIntent);
  	  				this.finish();
  				}
  			}
     
    }

   
    @Override
    public void onResume() {
    	super.onResume();
    	
    	if (logger.isActivated()) {
			logger.info("onResume()");
		}	
    }
    
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	
    	if (logger.isActivated()) {
			logger.info("onDestroy()");
		}
    	
    	// Disconnect ip call API
    	IPCallSessionsData.callApi.removeApiEventListener(this);
    	IPCallSessionsData.callApi.removeImsEventListener(this);
		IPCallSessionsData.callApi.disconnectApi();
		IPCallSessionsData.isCallApiConnected = false;
    }

    /**
     * Dial button listener
     */
    private OnClickListener audioInviteBtnListener = new OnClickListener() {
        public void onClick(View v) {
        	if (logger.isActivated()) {
    			logger.debug("audioInviteBtnListener - onClick()");
    		}
        	// Get the remote contact
            Spinner spinner = (Spinner)findViewById(R.id.contact);
            MatrixCursor cursor = (MatrixCursor)spinner.getSelectedItem();
            final String remote = cursor.getString(1);
            
            getApplicationContext().startActivity(setIntentOutgoingSession(remote, false));
            InitiateIPCallActivity.this.finish();
        }
    };

    /**
     * Invite button listener
     */
    private OnClickListener audioVideoInviteBtnListener = new OnClickListener() {
        public void onClick(View v) {
        	if (logger.isActivated()) {
    			logger.debug("audioVideoInviteBtnListener - onClick()");
    		}
        	// Get the remote contact
            Spinner spinner = (Spinner)findViewById(R.id.contact);
            MatrixCursor cursor = (MatrixCursor)spinner.getSelectedItem();
            final String remote = cursor.getString(1);

            getApplicationContext().startActivity(setIntentOutgoingSession(remote, true));
            InitiateIPCallActivity.this.finish();
        }
    };



	@Override
	public void handleApiConnected() {
		if (logger.isActivated()) {
			logger.debug("API, connected");
		}

		
		IPCallSessionsData.isCallApiConnected = true;
		if (logger.isActivated()) {
			logger.debug("IPCallSessionData.isCallApiConnected set to "+IPCallSessionsData.isCallApiConnected);
		}
		synchronized (callApiConnected){
			callApiConnected.notifyAll() ;
		}		
		
		if (IPCallSessionsData.getIncomingSessionWhenApiConnected) {
			getApplicationContext().startActivity(setIntentIncomingSession(IPCallSessionsData.remoteContact));
			IPCallSessionsData.getIncomingSessionWhenApiConnected = false;
			InitiateIPCallActivity.this.finish();
		}
		if (IPCallSessionsData.startOutgoingSessionWhenApiConnected) {
			getApplicationContext().startActivity(setIntentOutgoingSession(IPCallSessionsData.remoteContact, false));
			IPCallSessionsData.startOutgoingSessionWhenApiConnected = false;
			InitiateIPCallActivity.this.finish();
		}
	}

	@Override
	public void handleApiDisabled() {
		if (logger.isActivated()) {
			logger.debug("API, disabled");
		}
		IPCallSessionsData.isCallApiConnected = false;

		String msg = InitiateIPCallActivity.this.getString(R.string.label_api_disabled);
		
		// Api disabled
				Intent intent = new Intent(InitiateIPCallActivity.this.getApplicationContext(), IPCallSessionActivity.class);
				intent.setAction("ExitActivity");
				intent.putExtra("messages", msg);
				getApplicationContext().startActivity(intent);
	}

	@Override
	public void handleApiDisconnected() {
		if (logger.isActivated()) {
			logger.debug("API, disconnected");
		}
		IPCallSessionsData.isCallApiConnected = false;
		
		String msg = InitiateIPCallActivity.this.getString(R.string.label_api_disconnected);

		// Service has been disconnected
		Intent intent = new Intent(InitiateIPCallActivity.this.getApplicationContext(), IPCallSessionActivity.class);
		intent.setAction("ExitActivity");
		intent.putExtra("messages", msg);
		getApplicationContext().startActivity(intent);
	}
	
	@Override
	public void handleImsConnected() {
		if (logger.isActivated()) {
			logger.debug("IMS, connected");
		}
		// nothing to do
	}

	@Override
	public void handleImsDisconnected(int arg0) {
		if (logger.isActivated()) {
			logger.debug("IMS, disconnected");
		}
		
		String msg = InitiateIPCallActivity.this.getString(R.string.label_ims_disconnected);
		
		// IMS has been disconnected
		Intent intent = new Intent(InitiateIPCallActivity.this.getApplicationContext(), IPCallSessionActivity.class);
		intent.setAction("ExitActivity");
		intent.putExtra("messages", msg);
		getApplicationContext().startActivity(intent);
	}
	
	
	
	private Intent setIntentIncomingSession(String remote) {
		// Initiate Intent to recover incoming IP call
		Intent intent = new Intent(getApplicationContext(), IPCallSessionActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("contact", remote);
		intent.setAction("incoming");
		
		return intent ;
	}
	
	private Intent  setIntentOutgoingSession(String remote, boolean video){
		// Initiate Intent to launch outgoing IP call
        Intent intent = new Intent(getApplicationContext(), IPCallSessionActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("contact", remote);
        intent.putExtra("video", video);
        intent.setAction("outgoing");
        
        return intent;
	}


	/**
	 * Add IP call notification
	 * 
	 * @param context
	 *            Context
	 * @param Intent
	 *            invitation
	 */
	public static void addIPCallInvitationNotification(Context context,
			Intent invitation) {
		if (logger.isActivated()) {
			logger.debug("API, connected");
		}
		// Initialize settings
		RcsSettings.createInstance(context);

		// Create notification
		Intent intent = new Intent(invitation);
		intent.setClass(context, InitiateIPCallActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction("incoming");
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
		String notifTitle = context.getString(R.string.title_recv_ipcall_audio);

		Notification notif = new Notification(R.drawable.ri_notif_csh_icon,
				notifTitle, System.currentTimeMillis());
		notif.setLatestEventInfo(
				context,
				notifTitle,
				context.getString(R.string.label_from) + " "
						+ Utils.formatCallerId(invitation), contentIntent);

		// Set ringtone
		String ringtone = RcsSettings.getInstance().getCShInvitationRingtone();
		if (!TextUtils.isEmpty(ringtone)) {
			notif.sound = Uri.parse(ringtone);
		}

		// Set vibration
		if (RcsSettings.getInstance().isPhoneVibrateForCShInvitation()) {
			notif.defaults |= Notification.DEFAULT_VIBRATE;
		}

		// Send notification
		String sessionId = invitation.getStringExtra("sessionId");
		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(Utils.NOTIF_ID_IPCALL);
		notificationManager.notify(sessionId, Utils.NOTIF_ID_IPCALL, notif);
	}

	/**
	 * Remove IP call notification
	 * 
	 * @param context
	 *            Context
	 * @param sessionId
	 *            Session ID
	 */
	public static void removeIPCallNotification(Context context,
			String sessionId) {
		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(sessionId, Utils.NOTIF_ID_IPCALL);
	}
}

	
