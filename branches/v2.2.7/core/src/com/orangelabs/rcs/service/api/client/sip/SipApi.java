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

package com.orangelabs.rcs.service.api.client.sip;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.orangelabs.rcs.service.api.client.ClientApi;
import com.orangelabs.rcs.service.api.client.ClientApiException;
import com.orangelabs.rcs.service.api.client.CoreServiceNotAvailableException;

/**
 * SIP API
 * 
 * @author jexa7410
 */
public class SipApi extends ClientApi {
	/**
	 * Core service API
	 */
	private ISipApi coreApi = null;

	/**
     * Constructor
     * 
     * @param ctx Application context
     */
    public SipApi(Context ctx) {
    	super(ctx);
    }
    
    /**
     * Connect API
     */
    public void connectApi() {
    	super.connectApi();
    	
		ctx.bindService(new Intent(ISipApi.class.getName()), apiConnection, 0);
    }
    
    /**
     * Disconnect API
     */
    public void disconnectApi() {
    	super.disconnectApi();
    	
		ctx.unbindService(apiConnection);
    }

    /**
     * Returns the core service API
     * 
     * @return API
     */
	public ISipApi getCoreServiceApi() {
		return coreApi;
	}
	
	/**
	 * Core service API connection
	 */
	private ServiceConnection apiConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
        	coreApi = ISipApi.Stub.asInterface(service);

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
	 * Initiate a new session
	 * 
	 * @param contact Contact
	 * @param sdp SDP offer
	 * @return Session
	 * @throws ClientApiException
	 */
	public ISipSession initiateSession(String contact, String offer) throws ClientApiException {
    	if (coreApi != null) {
			try {
		    	return coreApi.initiateSession(contact, offer);
			} catch(Exception e) {
				throw new ClientApiException(e.getMessage());
			}
		} else {
			throw new CoreServiceNotAvailableException();
		}
	}

	/**
	 * Get a session from its session ID
	 *
	 * @param id Session ID
	 * @return Session
	 * @throws ClientApiException
	 */
	public ISipSession getSession(String id) throws ClientApiException {
    	if (coreApi != null) {
			try {
		    	return coreApi.getSession(id);
			} catch(Exception e) {
				throw new ClientApiException(e.getMessage());
			}
		} else {
			throw new CoreServiceNotAvailableException();
		}
	}
}
