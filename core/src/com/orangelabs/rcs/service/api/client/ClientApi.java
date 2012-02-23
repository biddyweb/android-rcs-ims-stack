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

package com.orangelabs.rcs.service.api.client;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import java.util.List;
import java.util.Vector;

/**
 * Client API
 * 
 * @author jexa7410
 */
public abstract class ClientApi {
	/**
	 * RCS permission
	 */
	public final static String RCS_PERMISSION = "com.orangelabs.rcs.permission.RCS";

	/**
	 * RCS extensions permission
	 */
	public final static String RCS_EXTENSION_PERMISSION = "com.orangelabs.rcs.permission.RCS_EXTENSION";

	/**
	 * API event listeners
	 */
	private Vector<ClientApiListener> listeners = new Vector<ClientApiListener>();
	
	/**
	 * IMS API event listeners
	 */
	private Vector<ImsEventListener> imsListeners = new Vector<ImsEventListener>();
		
	/**
	 * Application context
	 */
	protected Context ctx;
	
	/**
	 * IMS core API
	 */
	protected IImsApi imsCoreApi;
	
	/**
	 * Constructor
	 */
	public ClientApi(Context ctx) {
		this.ctx = ctx;
	}
	
    /**
     * Connect API
     */
    public void connectApi(){
    	// Connect to IMS API
    	ctx.bindService(new Intent(IImsApi.class.getName()), imsApiConnection, 0);
    	
		// Register the IMS connection broadcast receiver
		ctx.registerReceiver(imsConnectionReceiver, new IntentFilter(ClientApiIntents.SERVICE_REGISTRATION));

		if (!ClientApi.isServiceStarted(ctx)) {
        	// Notify event listener
        	notifyEventApiDisabled();
		}
    }
    
    /**
     * Disconnect API
     */
    public void disconnectApi(){
		// Unregister the broadcast receiver
    	ctx.unregisterReceiver(imsConnectionReceiver);
    	
    	// Disconnect from IMS API
    	ctx.unbindService(imsApiConnection);
    }
	
	/**
	 * IMS API connection
	 */
	protected ServiceConnection imsApiConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            imsCoreApi = IImsApi.Stub.asInterface(service);

            try {
				if (imsCoreApi.isImsConnected()) {
					notifyEventImsConnected();
				} else {
					notifyEventImsDisconnected();
				}
			} catch (RemoteException e) {
			}
        }

        public void onServiceDisconnected(ComponentName className) {
        	imsCoreApi = null;
        }
    };

    /**
	 * Add an API event listener
	 * 
	 * @param listener Listener
	 */
	public void addApiEventListener(ClientApiListener listener) {
		listeners.addElement(listener);
	}

	/**
	 * Remove an API event listener
	 * 
	 * @param listener Listener
	 */
	public void removeApiEventListener(ClientApiListener listener) {
		listeners.removeElement(listener);
	}
	
	/**
	 * Add an IMS event listener
	 * 
	 * @param listener Listener
	 */
	public void addImsEventListener(ImsEventListener listener) {
		imsListeners.addElement(listener);
	}

	/**
	 * Remove an IMS event listener
	 * 
	 * @param listener Listener
	 */
	public void removeImsEventListener(ImsEventListener listener) {
		imsListeners.removeElement(listener);
	}

	/**
	 * Remove all API event listeners
	 */
	public void removeAllApiEventListeners() {
		listeners.removeAllElements();
		imsListeners.removeAllElements();
	}
	
	/**
	 * Notify listeners when API is disabled
	 */
	protected void notifyEventApiDisabled() {
		for(int i=0; i < listeners.size(); i++) {
			ClientApiListener listener = (ClientApiListener)listeners.elementAt(i);
			listener.handleApiDisabled();
		}
	}

	/**
	 * Notify listeners when API is connected to the server
	 */
	protected void notifyEventApiConnected() {
		for(int i=0; i < listeners.size(); i++) {
			ClientApiListener listener = (ClientApiListener)listeners.elementAt(i);
			listener.handleApiConnected();
		}
	}

	/**
	 * Notify listeners when API is disconnected from the server
	 */
	protected void notifyEventApiDisconnected() {
		for(int i=0; i < listeners.size(); i++) {
			ClientApiListener listener = (ClientApiListener)listeners.elementAt(i);
			listener.handleApiDisconnected();
		}
	}
	
	/**
	 * Broadcast receiver to be aware of IMS connection changes
	 */
	protected BroadcastReceiver imsConnectionReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, final Intent intent) {
			if (intent.getBooleanExtra("status", false)){
				// Connected to IMS
				notifyEventImsConnected();
			} else {
				// Disconnected from IMS
				notifyEventImsDisconnected();
			}
		}
	};	
	
	/**
	 * Notify listeners when service is registered to the IMS
	 */
	private void notifyEventImsConnected() {
		for(int i=0; i < imsListeners.size(); i++) {
			ImsEventListener imsListener = (ImsEventListener)imsListeners.elementAt(i);
			imsListener.handleImsConnected();
		}
	}

	/**
	 * Notify listeners when service is not registered to the IMS
	 */
	private void notifyEventImsDisconnected() {
		for(int i=0; i < imsListeners.size(); i++) {
			ImsEventListener imsListener = (ImsEventListener)imsListeners.elementAt(i);
			imsListener.handleImsDisconnected();
		}
	}
	
	/**
	 * Is service connected to the IMS
	 * 
	 * @param ctx Context
	 * @return Boolean
	 */
	public boolean isImsConnected(Context ctx) throws ClientApiException {
		if (imsCoreApi != null) {
			try {
				return imsCoreApi.isImsConnected();
			} catch(Exception e) {
				throw new ClientApiException(e.getMessage());
			}
		} else {
			throw new CoreServiceNotAvailableException();
		}
	}
	
	/**
	 * Is service started
	 *
	 * @param ctx Context
	 * @return Boolean
	 */
	public static boolean isServiceStarted(Context ctx) {
	    ActivityManager activityManager = (ActivityManager)ctx.getSystemService(Context.ACTIVITY_SERVICE);
	    List<ActivityManager.RunningServiceInfo> serviceList = activityManager.getRunningServices(Integer.MAX_VALUE);
	     for(int i = 0; i < serviceList.size(); i++) {
	           ActivityManager.RunningServiceInfo serviceInfo = serviceList.get(i);
	           ComponentName serviceName = serviceInfo.service;
	           if (serviceName.getClassName().equals("com.orangelabs.rcs.service.RcsCoreService")) {
	                 if (serviceInfo.pid != 0) {
	                      return true;
	                 } else {
	                      return false;
	                 }
	           }
	     }
	     return false;
	}	
	
	/**
	 * Start RCS service
	 *
	 * @param ctx Context
	 */
	public static void startRcsService(Context ctx) {
        ctx.startService(new Intent("com.orangelabs.rcs.service.START"));
        // Intentional use of string and not class SERVICE_NAME
	}

	/**
	 * Stop RCS service
	 *
	 * @param ctx Context
	 */
	public static void stopRcsService(Context ctx) {
        ctx.stopService(new Intent("com.orangelabs.rcs.service.START"));
        ctx.stopService(new Intent("com.orangelabs.rcs.provisioning.HTTPS"));
        ctx.stopService(new Intent("com.orangelabs.rcs.SERVICE"));
        // Intentional use of string and not class SERVICE_NAME
	}

}
