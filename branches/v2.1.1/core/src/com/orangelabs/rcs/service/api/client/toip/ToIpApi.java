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
package com.orangelabs.rcs.service.api.client.toip;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.orangelabs.rcs.service.api.client.ClientApi;
import com.orangelabs.rcs.service.api.client.ClientApiException;
import com.orangelabs.rcs.service.api.client.IMediaPlayer;
import com.orangelabs.rcs.service.api.client.CoreServiceNotAvailableException;
import com.orangelabs.rcs.service.api.client.IMediaRenderer;

/**
 * ToIP API
 * 
 * @author jexa7410
 */
public class ToIpApi extends ClientApi {
	/**
	 * Application context
	 */
	private Context ctx;
	
	/**
	 * Core service API
	 */
	private IToIpApi coreApi = null;

	/**
     * Constructor
     * 
     * @param ctx Application context
     */
    public ToIpApi(Context ctx) {
    	this.ctx = ctx;
    }
    
    /**
     * Connect API
     */
    public void connectApi() {
		ctx.bindService(new Intent(IToIpApi.class.getName()), apiConnection, 0);

		if (!ClientApi.isServiceStarted(ctx)) {
        	// Notify event listener
        	notifyEventApiDisabled();
		}		
    }
    
    /**
     * Disconnect API
     */
    public void disconnectApi() {
		ctx.unbindService(apiConnection);
    }

    /**
     * Returns the core service API
     * 
     * @return API
     */
	public IToIpApi getCoreServiceApi() {
		return coreApi;
	}
	
	/**
	 * Core service API connection
	 */
	private ServiceConnection apiConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
        	coreApi = IToIpApi.Stub.asInterface(service);

        	// Notify event listener
        	notifyEventApiConnected();
        }

        public void onServiceDisconnected(ComponentName className) {
        	// Notify event listener
        	notifyEventApiDisconnected();

        	coreApi = null;
        }
    };

	/**
	 * Initiate a ToIP call
	 *  
 	 * @param contact Contact
	 * @param player Media player
	 * @param renderer Media renderer
	 * @return ToIP session
	 * @throws ClientApiException
 	 */
	public IToIpSession initiateToIpCall(String contact, IMediaPlayer player, IMediaRenderer renderer) throws ClientApiException {
    	if (coreApi != null) {
			try {
		    	return coreApi.initiateToIpCall(contact, player, renderer);
			} catch(Exception e) {
				throw new ClientApiException(e.getMessage());
			}
		} else {
			throw new CoreServiceNotAvailableException();
		}
	}

	/**
	 * Get a ToIP session from its session ID
	 *
	 * @param id Session ID
	 * @return Session
	 * @throws ClientApiException
	 */
	public IToIpSession getToIpSession(String id) throws ClientApiException {
    	if (coreApi != null) {
			try {
		    	return coreApi.getToIpSession(id);
			} catch(Exception e) {
				throw new ClientApiException(e.getMessage());
			}
		} else {
			throw new CoreServiceNotAvailableException();
		}
	}
}
