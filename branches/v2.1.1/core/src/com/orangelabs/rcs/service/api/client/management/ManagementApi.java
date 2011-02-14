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
package com.orangelabs.rcs.service.api.client.management;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.orangelabs.rcs.service.api.client.ClientApi;
import com.orangelabs.rcs.service.api.client.ClientApiException;
import com.orangelabs.rcs.service.api.client.CoreServiceNotAvailableException;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Management API
 * 
 * @author jexa7410
 */
public class ManagementApi extends ClientApi {
	/**
	 * Application context
	 */
	private Context ctx;
	
	/**
	 * Core service API
	 */
	private IManagementApi coreApi = null;

    /**
	 * The logger
	 */
	private Logger logger = Logger.getLogger(this.getClass().getName());

	/**
     * Constructor
     * 
     * @param ctx Application context
     */
    public ManagementApi(Context ctx) {
    	this.ctx = ctx;
    }
    
    /**
     * Connect API
     */
    public void connectApi() {
		ctx.bindService(new Intent(IManagementApi.class.getName()), apiConnection, 0);

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
	public IManagementApi getCoreServiceApi() {
		return coreApi;
	}

	/**
	 * Core service API connection
	 */
	private ServiceConnection apiConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            coreApi = IManagementApi.Stub.asInterface(service);

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
	 * Set trace activation flag
	 * 
	 * @param state State
	 * @throws ClientApiException
	 */
	public void setTraceActivation(boolean state) throws ClientApiException {
		if (logger.isActivated()) {
			logger.info("Set trace activation");
		}

		if (coreApi != null) {
			try {
				coreApi.setTraceActivation(state);
			} catch(Exception e) {
				throw new ClientApiException(e.getMessage());
			}
		} else {
			throw new CoreServiceNotAvailableException();
		}
	}
	
	/**
	 * Set trace level
	 * 
	 * @param level Level
	 * @throws ClientApiException
	 */
	public void setTraceLevel(int level) throws ClientApiException {
		if (logger.isActivated()) {
			logger.info("Set trace level");
		}

		if (coreApi != null) {
			try {
				coreApi.setTraceLevel(level);
			} catch(Exception e) {
				throw new ClientApiException(e.getMessage());
			}
		} else {
			throw new CoreServiceNotAvailableException();
		}
	}
	
	/**
	 * Is trace activated
	 * 
	 * @return Boolean
	 * @throws ClientApiException
	 */
	public boolean isTraceActivated() throws ClientApiException {
		if (logger.isActivated()) {
			logger.info("Is trace activated");
		}

		if (coreApi != null) {
			try {
				return coreApi.isTraceActivated();
			} catch(Exception e) {
				throw new ClientApiException(e.getMessage());
			}
		} else {
			throw new CoreServiceNotAvailableException();
		}
	}
	
	/**
	 * Get trace level
	 * 
	 * @return Level of trace
	 * @throws ClientApiException
	 */
	public int getTraceLevel() throws ClientApiException {
		if (logger.isActivated()) {
			logger.info("Get trace level");
		}

		if (coreApi != null) {
			try {
				return coreApi.getTraceLevel();
			} catch(Exception e) {
				throw new ClientApiException(e.getMessage());
			}
		} else {
			throw new CoreServiceNotAvailableException();
		}
	}	
}
