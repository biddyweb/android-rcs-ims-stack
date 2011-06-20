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

package com.orangelabs.rcs.service.api.server.richcall;

import com.orangelabs.rcs.core.Core;
import com.orangelabs.rcs.core.content.ContentManager;
import com.orangelabs.rcs.core.content.MmContent;
import com.orangelabs.rcs.core.content.VideoContent;
import com.orangelabs.rcs.core.ims.service.ImsServiceSession;
import com.orangelabs.rcs.core.ims.service.sharing.streaming.ContentSharingStreamingSession;
import com.orangelabs.rcs.core.ims.service.sharing.transfer.ContentSharingTransferSession;
import com.orangelabs.rcs.platform.file.FileDescription;
import com.orangelabs.rcs.platform.file.FileFactory;
import com.orangelabs.rcs.provider.sharing.RichCall;
import com.orangelabs.rcs.provider.sharing.RichCallData;
import com.orangelabs.rcs.service.api.client.media.IMediaPlayer;
import com.orangelabs.rcs.service.api.client.richcall.IImageSharingSession;
import com.orangelabs.rcs.service.api.client.richcall.IRichCallApi;
import com.orangelabs.rcs.service.api.client.richcall.IVideoSharingSession;
import com.orangelabs.rcs.service.api.server.ServerApiException;
import com.orangelabs.rcs.service.api.server.ServerApiUtils;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Rich call API service
 * 
 * @author jexa7410
 */
public class RichCallApiService extends IRichCallApi.Stub {
    /**
	 * The logger
	 */
	private Logger logger = Logger.getLogger(this.getClass().getName());

	/**
	 * Constructor
	 */
	public RichCallApiService() {
		if (logger.isActivated()) {
			logger.info("Rich call API is loaded");
		}
	}

	/**
	 * Close API
	 */
	public void close() {
	}
    
	/**
	 * Get the remote phone number involved in the current call
	 * 
	 * @return Phone number or null if there is no call in progress
	 * @throws ServerApiException
	 */
	public String getRemotePhoneNumber() throws ServerApiException {
		if (logger.isActivated()) {
			logger.info("Get remote phone number");
		}

		// Check permission
		ServerApiUtils.testPermission();

		// Test core availability
		ServerApiUtils.testCore();

		try {
			return Core.getInstance().getImsModule().getCallManager().getRemoteParty();
		} catch(Exception e) {
			throw new ServerApiException(e.getMessage());
		}
	}
	
	/**
	 * Initiate a live video sharing session
	 * 
	 * @param contact Contact
	 * @param player Media player
	 * @throws ServerApiException
	 */
	public IVideoSharingSession initiateLiveVideoSharing(String contact, IMediaPlayer player) throws ServerApiException {
		if (logger.isActivated()) {
			logger.info("Initiate a live video session with " + contact);
		}

		// Check permission
		ServerApiUtils.testPermission();

		// Test IMS connection
		ServerApiUtils.testIms();

		try {
			// Initiate a new session
			ContentSharingStreamingSession session = Core.getInstance().getRichcallService().initiateLiveVideoSharingSession(contact, player);
			
			// Update rich call history
			RichCall.getInstance().addCall(contact, session.getSessionID(),
	    			RichCallData.EVENT_OUTGOING, 
	    			session.getContent(),
	    			RichCallData.STATUS_STARTED);
			
			return new VideoSharingSession(session);
		} catch(Exception e) {
			throw new ServerApiException(e.getMessage());
		}
	}

	/**
	 * Initiate a pre-recorded video sharing session
	 * 
 	 * @param contact Contact
 	 * @param file Video file
	 * @param player Media player
	 * @throws ServerApiException
 	 */
	public IVideoSharingSession initiateVideoSharing(String contact, String file, IMediaPlayer player) throws ServerApiException {
		if (logger.isActivated()) {
			logger.info("Initiate a pre-recorded video session with " + contact);
		}

		// Check permission
		ServerApiUtils.testPermission();

		// Test IMS connection
		ServerApiUtils.testIms();

		try {
			FileDescription desc = FileFactory.getFactory().getFileDescription(file);
			VideoContent content = (VideoContent)ContentManager.createMmContentFromUrl(file, desc.getSize());
			ContentSharingStreamingSession session = Core.getInstance().getRichcallService().initiatePreRecordedVideoSharingSession(contact, content, player);
			
			// Update rich call history
			RichCall.getInstance().addCall(contact, session.getSessionID(),
	    			RichCallData.EVENT_OUTGOING, 
	    			session.getContent(),
	    			RichCallData.STATUS_STARTED);
			
			return new VideoSharingSession(session);
		} catch(Exception e) {
			throw new ServerApiException(e.getMessage());
		}
	}

	/**
	 * Get a video sharing session from its session ID
	 *
	 * @param id Session ID
	 * @return Session
	 * @throws ServerApiException
	 */
	public IVideoSharingSession getVideoSharingSession(String id) throws ServerApiException {
		if (logger.isActivated()) {
			logger.info("Get video sharing session " + id);
		}

		// Check permission
		ServerApiUtils.testPermission();

		// Test core availability
		ServerApiUtils.testCore();
		
		try {
			ImsServiceSession session = Core.getInstance().getContentSharingService().getSession(id);
			if ((session != null) && (session instanceof ContentSharingStreamingSession)) {
				return new VideoSharingSession((ContentSharingStreamingSession)session);
			} else {
				return null;
			}
		} catch(Exception e) {
			throw new ServerApiException(e.getMessage());
		}
	}

	/**
	 * Initiate an image sharing session
	 * 
	 * @param contact Contact
	 * @param file Image file
	 * @throws ServerApiException
	 */
	public IImageSharingSession initiateImageSharing(String contact, String file) throws ServerApiException {
		if (logger.isActivated()) {
			logger.info("Initiate an image sharing session with " + contact);
		}

		// Check permission
		ServerApiUtils.testPermission();

		// Test IMS connection
		ServerApiUtils.testIms();
		
		try {
			FileDescription desc = FileFactory.getFactory().getFileDescription(file);
			MmContent content = ContentManager.createMmContentFromUrl(file, desc.getSize());
			ContentSharingTransferSession session = Core.getInstance().getRichcallService().initiateImageSharingSession(contact, content);
			
			// Update rich call history
			RichCall.getInstance().addCall(contact, session.getSessionID(),
	    			RichCallData.EVENT_OUTGOING, 
	    			session.getContent(),
	    			RichCallData.STATUS_STARTED);
			
			return new ImageSharingSession(session);
		} catch(Exception e) {
			throw new ServerApiException(e.getMessage());
		}
	}

	/**
	 * Get an image sharing session from its session ID
	 * 
	 * @param id Session ID
	 * @return Session
	 * @throws ServerApiException
	 */
	public IImageSharingSession getImageSharingSession(String id) throws ServerApiException {
		if (logger.isActivated()) {
			logger.info("Get image sharing session " + id);
		}

		// Check permission
		ServerApiUtils.testPermission();

		// Test core availability
		ServerApiUtils.testCore();
		
		try {
			ImsServiceSession session = Core.getInstance().getContentSharingService().getSession(id);
			if ((session != null) && (session instanceof ContentSharingTransferSession)) {
				return new ImageSharingSession((ContentSharingTransferSession)session);
			} else {
				return null;
			}
		} catch(Exception e) {
			throw new ServerApiException(e.getMessage());
		}
	}
}
