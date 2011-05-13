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

package com.orangelabs.rcs.service.api.server.toip;

import com.orangelabs.rcs.core.Core;
import com.orangelabs.rcs.core.ims.service.ImsServiceSession;
import com.orangelabs.rcs.service.api.client.media.IMediaPlayer;
import com.orangelabs.rcs.service.api.client.media.IMediaRenderer;
import com.orangelabs.rcs.service.api.client.toip.IToIpApi;
import com.orangelabs.rcs.service.api.client.toip.IToIpSession;
import com.orangelabs.rcs.service.api.server.ServerApiException;
import com.orangelabs.rcs.service.api.server.ServerApiUtils;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * ToIP API service
 * 
 * @author jexa7410
 */
public class ToIpApiService extends IToIpApi.Stub {
    /**
	 * The logger
	 */
	private Logger logger = Logger.getLogger(this.getClass().getName());

	/**
	 * Constructor
	 */
	public ToIpApiService() {
		if (logger.isActivated()) {
			logger.info("ToIP API is loaded");
		}
	}

	/**
	 * Close API
	 */
	public void close() {
	}
	
	/**
	 * Initiate a ToIP call
	 * 
	 * @param contact Contact
	 * @param player Media player
	 * @param renderer Media renderer
	 * @throws ServerApiException
	 */
	public IToIpSession initiateToIpCall(String contact, IMediaPlayer player, IMediaRenderer renderer) throws ServerApiException {
		// Test IMS connection
		ServerApiUtils.testIms();

		try {
			com.orangelabs.rcs.core.ims.service.toip.ToIpSession session = Core.getInstance().getToIpService().initiateCall(contact, player, renderer);
			return new ToIpSession(session);
		} catch(Exception e) {
			throw new ServerApiException(e.getMessage());
		}
	}

	/**
	 * Get a ToIP session from its session ID
	 *
	 * @param id Session ID
	 * @return Session
	 * @throws ServerApiException
	 */
	public IToIpSession getToIpSession(String id) throws ServerApiException {
		// Test core availability
		ServerApiUtils.testCore();
		
		try {
			ImsServiceSession session = Core.getInstance().getToIpService().getSession(id);
			if ((session != null) && (session instanceof com.orangelabs.rcs.core.ims.service.toip.ToIpSession)) {
				return new ToIpSession((com.orangelabs.rcs.core.ims.service.toip.ToIpSession)session);
			} else {
				return null;
			}
		} catch(Exception e) {
			throw new ServerApiException(e.getMessage());
		}
	}
}
