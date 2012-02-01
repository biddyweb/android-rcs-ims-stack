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

package com.orangelabs.rcs.service.api.server;

import android.content.Intent;

import com.orangelabs.rcs.platform.AndroidFactory;
import com.orangelabs.rcs.service.api.client.ClientApiIntents;
import com.orangelabs.rcs.service.api.client.IImsApi;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * IMS API service
 */
public class ImsApiService extends IImsApi.Stub {
    /**
	 * The logger
	 */
	private Logger logger = Logger.getLogger(this.getClass().getName());

	/**
	 * Constructor
	 */
	public ImsApiService() {
		if (logger.isActivated()) {
			logger.info("IMS API service is loaded");
		}
	}

	/**
	 * Close API
	 */
	public void close() {
	}
    
	/** 
	 * Request IMS connection state
	 * 
	 * @return IMS connection state
	 */
    public boolean isImsConnected()throws ServerApiException {
		if (logger.isActivated()) {
			logger.info("Get IMS connection state");
		}

		try {
			// Test IMS connection
			return ServerApiUtils.isImsConnected();
		} catch(Exception e) {
			throw new ServerApiException(e.getMessage());
		}
	}
    
    /**
     * User confirmation request
     * 
     * @param id Request ID
     * @param type Type of request
     * @param pin PIN number requested
     * @param subject Subject
     * @param text Text
     */
    public void handleUserConfirmationRequest(String id, String type, boolean pin, String subject, String text) {
		// Broadcast intent related to the received request
    	Intent intent = new Intent(ClientApiIntents.USER_CONFIRMATION_REQUEST);
    	intent.putExtra("id", id);
    	intent.putExtra("type", type);
    	intent.putExtra("pin", pin);
    	intent.putExtra("subject", subject);
    	intent.putExtra("text", text);
    	AndroidFactory.getApplicationContext().sendBroadcast(intent);
    }

    /**
     * User terms confirmation acknowledge
     * 
     * @param id Request ID
     * @param status Status
     * @param subject Subject
     * @param text Text
     */
    public void handleUserConfirmationAck(String id, String status, String subject, String text) {
		// Broadcast intent related to the received request
    	Intent intent = new Intent(ClientApiIntents.USER_CONFIRMATION_REQUEST);
    	intent.putExtra("id", id);
    	intent.putExtra("status", status);
    	intent.putExtra("subject", subject);
    	intent.putExtra("text", text);
    	AndroidFactory.getApplicationContext().sendBroadcast(intent);
    }
}
