/*******************************************************************************
 * Software Name : RCS IMS Stack
 *
 * Copyright (C) 2010 France Telecom S.A.
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

package com.orangelabs.rcs.service.api.client.ipcall;

import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.orangelabs.rcs.service.api.client.ClientApi;
import com.orangelabs.rcs.service.api.client.ClientApiException;
import com.orangelabs.rcs.service.api.client.CoreServiceNotAvailableException;
import com.orangelabs.rcs.service.api.client.media.IAudioPlayer;
import com.orangelabs.rcs.service.api.client.media.IAudioRenderer;
import com.orangelabs.rcs.service.api.client.media.IMediaPlayer;
import com.orangelabs.rcs.service.api.client.media.IMediaRenderer;

/**
 * IP call API
 * 
 * @author owom5460
 */
public class IPCallApi extends ClientApi {
	/**
	 * Core service API
	 */
	private IIPCallApi coreApi = null;
	
	/**
     * Constructor
     * 
     * @param ctx Application context
     */
    public IPCallApi(Context ctx) {
    	super(ctx);
    }
    
    /**
     * Connect API
     */
    public void connectApi() {
    	super.connectApi();
    	
		ctx.bindService(new Intent(IIPCallApi.class.getName()), apiConnection, 0);
    }
    
    /**
     * Disconnect API
     */
    public void disconnectApi() {
    	super.disconnectApi();
    	
    	try {
    		ctx.unbindService(apiConnection);
        } catch (IllegalArgumentException e) {
        	// Nothing to do
        }
    }
	
	/**
	 * Core service API connection
	 */
	private ServiceConnection apiConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
        	coreApi = IIPCallApi.Stub.asInterface(service);

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
	 * Initiate an IP call session with audio and video
	 * 
	 * @param contact Contact
	 * @param audioplayer Audio player
	 * @param audiorenderer Audio renderer
	 * @param videoplayer Video player 
	 * @param videorenderer Video renderer
	 * @return IP call session
	 * @throws ClientApiException
	 */
	public IIPCallSession initiateCall(String contact, IAudioPlayer audioplayer, IAudioRenderer audiorenderer, IMediaPlayer videoplayer, IMediaRenderer videorenderer) throws ClientApiException {
    	if (coreApi != null) {
			try {
		    	return coreApi.initiateCall(contact, audioplayer, audiorenderer, videoplayer, videorenderer);
			} catch(Exception e) {
				throw new ClientApiException(e.getMessage());
			}
		} else {
			throw new CoreServiceNotAvailableException();
		}
	}

	/**
	 * Initiate an IP call session with audio only
	 * 
	 * @param contact Contact
	 * @param audioplayer Audio player
	 * @param audiorenderer Audio renderer
	 * @return IP call session
	 * @throws ClientApiException
	 */
	public IIPCallSession initiateCall(String contact, IAudioPlayer audioplayer, IAudioRenderer audiorenderer) throws ClientApiException {
		if (coreApi != null) {
			try {
				return coreApi.initiateCall(contact, audioplayer, audiorenderer, null, null);
			} catch(Exception e) {
				throw new ClientApiException(e.getMessage());
			}
		} else {
			throw new CoreServiceNotAvailableException();
		}
	}
	
	/**
	 * Get an IP call session from its session ID
	 *
	 * @param id Session ID
	 * @return Session
	 * @throws ClientApiException
	 */
	public IIPCallSession getSession(String id) throws ClientApiException {
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

	/**
	 * Get list of current IP call sessions
	 * 
	 * @return List of sessions
	 * @throws ClientApiException
	 */
	public List<IBinder> getSessions() throws ClientApiException {
    	if (coreApi != null) {
			try {
		    	return coreApi.getSessions();
			} catch(Exception e) {
				throw new ClientApiException(e.getMessage());
			}
		} else {
			throw new CoreServiceNotAvailableException();
		}
	}	
}
