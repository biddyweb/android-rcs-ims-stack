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
package com.orangelabs.rcs.service.api.server.voip;

import com.orangelabs.rcs.core.Core;
import com.orangelabs.rcs.core.ims.service.ImsServiceSession;
import com.orangelabs.rcs.core.media.MediaPlayer;
import com.orangelabs.rcs.core.media.MediaRenderer;
import com.orangelabs.rcs.service.api.client.IMediaPlayer;
import com.orangelabs.rcs.service.api.client.IMediaRenderer;
import com.orangelabs.rcs.service.api.client.voip.IVoIpApi;
import com.orangelabs.rcs.service.api.client.voip.IVoIpSession;
import com.orangelabs.rcs.service.api.server.RemoteMediaPlayer;
import com.orangelabs.rcs.service.api.server.RemoteMediaRenderer;
import com.orangelabs.rcs.service.api.server.ServerApiException;
import com.orangelabs.rcs.service.api.server.ServerApiUtils;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * VoIP API service
 * 
 * @author jexa7410
 */
public class VoIpApiService extends IVoIpApi.Stub {

    /**
	 * The logger
	 */
	private Logger logger = Logger.getLogger(this.getClass().getName());

	/**
	 * Constructor
	 */
	public VoIpApiService() {
		if (logger.isActivated()) {
			logger.info("VoIP API is loaded");
		}
	}

	/**
	 * Close API
	 */
	public void close() {
	}
    
	/**
	 * Initiate a VoIP call
	 * 
	 * @param contact Contact
	 * @param player Media player
	 * @param renderer Media renderer
	 * @throws ServerApiException
	 */
	public IVoIpSession initiateVoIpCall(String contact, IMediaPlayer player, IMediaRenderer renderer) throws ServerApiException {
		// Test IMS connection
		ServerApiUtils.testIms();

		try {
			MediaPlayer corePlayer = new RemoteMediaPlayer(player);
			MediaRenderer coreRenderer = new RemoteMediaRenderer(renderer);
			com.orangelabs.rcs.core.ims.service.voip.VoIpSession session = Core.getInstance().getVoIpService().initiateCall(contact, corePlayer, coreRenderer);
			return new VoIpSession(session);
		} catch(Exception e) {
			throw new ServerApiException(e.getMessage());
		}
	}

	/**
	 * Get a VoIP session from its session ID
	 *
	 * @param id Session ID
	 * @return Session
	 * @throws ServerApiException
	 */
	public IVoIpSession getVoIpSession(String id) throws ServerApiException {
		// Test core availability
		ServerApiUtils.testCore();
		
		try {
			ImsServiceSession session = Core.getInstance().getVoIpService().getSession(id);
			if ((session != null) && (session instanceof com.orangelabs.rcs.core.ims.service.voip.VoIpSession)) {
				return new VoIpSession((com.orangelabs.rcs.core.ims.service.voip.VoIpSession)session);
			} else {
				return null;
			}
		} catch(Exception e) {
			throw new ServerApiException(e.getMessage());
		}
	}
}
