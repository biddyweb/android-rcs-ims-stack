package com.orangelabs.rcs.ri.ipcall;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import com.orangelabs.rcs.provider.settings.RcsSettings;
import com.orangelabs.rcs.ri.R;
import com.orangelabs.rcs.service.api.client.ClientApiException;
import com.orangelabs.rcs.service.api.client.ClientApiListener;
import com.orangelabs.rcs.service.api.client.ImsEventListener;
import com.orangelabs.rcs.service.api.client.ipcall.IIPCallSession;
import com.orangelabs.rcs.service.api.client.ipcall.IPCallApi;
import com.orangelabs.rcs.utils.logger.Logger;

public class IPCallSessionsListActivity extends Activity implements
ClientApiListener, ImsEventListener {

	
	
	/**
	 * UI handler
	 */
	Handler handler = new Handler() ;
	
	/**
	 * synchronization object
	 */
	Object callApiConnected = new Object(); 
	
	/**
	 * layout of activity
	 */
	ListView sessionsList ;
	
	/**
	 * Logger
	 */
	private static Logger logger = Logger.getLogger(IPCallSessionsListActivity.class.getName());
	
	
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
		setContentView(R.layout.ipcall_sessionslist);

		// Set title
		setTitle(R.string.menu_ipcall_sessionslist);

		sessionsList = (ListView) findViewById(R.id.sessions_list);

		RcsSettings.createInstance(getApplicationContext());

		// wait Api connection - get sessions when connected
		Thread thread = new Thread() {
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
					}
				}
				if (logger.isActivated()) {
					logger.info("callApi connected");
				}
				try {
					IPCallSessionsData.sessions = (ArrayList<IBinder>) IPCallSessionsData.callApi
							.getSessions();
					if (logger.isActivated()) {
						logger.info("sessions list initialized");
					}
				} catch (ClientApiException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// set sessions in listView
				handler.post(new Runnable() {
					public void run() {
						sessionsList.setAdapter(new ArrayAdapter<IBinder>(
								IPCallSessionsListActivity.this,
								android.R.layout.simple_list_item_1,
								IPCallSessionsData.sessions));
						sessionsList
								.setOnItemClickListener(sessionsListListener);
					}
				});
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
	}

   
    @Override
    public void onResume() {
    	super.onResume();
    	
    	if (logger.isActivated()) {
			logger.info("onResume()");
		}
    	
    	if (IPCallSessionsData.isCallApiConnected) {
			try {
				IPCallSessionsData.sessions.clear();

				IPCallSessionsData.sessions = (ArrayList<IBinder>) IPCallSessionsData.callApi
						.getSessions();
				if (logger.isActivated()) {
					logger.info("sessions list initialized");
				}
			} catch (ClientApiException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//get listView and display sessions 
   	        ListView sessionsList = (ListView)findViewById(R.id.sessions_list);	
   	        	sessionsList.setAdapter(new ArrayAdapter<IBinder>(IPCallSessionsListActivity.this, android.R.layout.simple_list_item_1, IPCallSessionsData.sessions));
   	        	sessionsList.setOnItemClickListener(sessionsListListener);       
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
		if (IPCallSessionsData.recoverSessionsWhenApiConnected) {
			getApplicationContext().startActivity(setIntentRecoverSession());
			IPCallSessionsData.recoverSessionsWhenApiConnected = false;
		}
	}

	@Override
	public void handleApiDisabled() {
		if (logger.isActivated()) {
			logger.debug("API, disabled");
		}
		IPCallSessionsData.isCallApiConnected = false;

		String msg = IPCallSessionsListActivity.this.getString(R.string.label_api_disabled);
		
		// Api disabled
				Intent intent = new Intent(IPCallSessionsListActivity.this.getApplicationContext(), IPCallSessionActivity.class);
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
		
		String msg = IPCallSessionsListActivity.this.getString(R.string.label_api_disconnected);

		// Service has been disconnected
		Intent intent = new Intent(IPCallSessionsListActivity.this.getApplicationContext(), IPCallSessionActivity.class);
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
		
		String msg = IPCallSessionsListActivity.this.getString(R.string.label_ims_disconnected);
		
		// IMS has been disconnected
		Intent intent = new Intent(IPCallSessionsListActivity.this.getApplicationContext(), IPCallSessionActivity.class);
		intent.setAction("ExitActivity");
		intent.putExtra("messages", msg);
		getApplicationContext().startActivity(intent);
	}
	
	private OnItemClickListener sessionsListListener = new OnItemClickListener() {

	    @Override
	    public void onItemClick(AdapterView<?> parent, View view, int position,
	            long id) {
	    	if (logger.isActivated()) {logger.debug("onItemClick");	}
	        
	    	// get session
	    	IBinder  iBinder  = (IBinder) parent.getItemAtPosition(position);
	        IPCallSessionsData.session = IIPCallSession.Stub.asInterface(iBinder);
	        
	        try {
	        	//get sessionId
	        	IPCallSessionsData.sessionId = IPCallSessionsData.session.getSessionID();
	        	if (logger.isActivated()) {
					logger.debug("IPCallSessionsData.sessionId = "+IPCallSessionsData.sessionId);
				}
	        	 // get Session Data
	        	IPCallSessionsData.getSessionData(IPCallSessionsData.sessionId);
	        	//launch IPCallSessionActivity
				getApplicationContext().startActivity(setIntentRecoverSession());
				IPCallSessionsListActivity.this.finish();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block 
				e.printStackTrace();
			}
	    }
	};
	
	private Intent setIntentRecoverSession() {
		// Initiate Intent to launch outgoing IP call
		Intent intent = new Intent(getApplicationContext(), IPCallSessionActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("contact", IPCallSessionsData.remoteContact);
        intent.setAction("recover");
        
        return intent;
	}
	
}
