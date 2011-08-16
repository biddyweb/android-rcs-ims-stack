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

package com.orangelabs.rcs.core.ims.service.sharing;

import com.orangelabs.rcs.core.CoreException;
import com.orangelabs.rcs.core.content.ContentManager;
import com.orangelabs.rcs.core.content.LiveVideoContent;
import com.orangelabs.rcs.core.content.MmContent;
import com.orangelabs.rcs.core.content.VideoContent;
import com.orangelabs.rcs.core.ims.ImsModule;
import com.orangelabs.rcs.core.ims.network.sip.FeatureTags;
import com.orangelabs.rcs.core.ims.protocol.sip.SipRequest;
import com.orangelabs.rcs.core.ims.service.ImsService;
import com.orangelabs.rcs.core.ims.service.sharing.streaming.ContentSharingStreamingSession;
import com.orangelabs.rcs.core.ims.service.sharing.streaming.OriginatingLiveVideoContentSharingSession;
import com.orangelabs.rcs.core.ims.service.sharing.streaming.OriginatingPreRecordedVideoContentSharingSession;
import com.orangelabs.rcs.core.ims.service.sharing.streaming.TerminatingVideoContentSharingSession;
import com.orangelabs.rcs.core.ims.service.sharing.transfer.ContentSharingTransferSession;
import com.orangelabs.rcs.core.ims.service.sharing.transfer.OriginatingContentSharingSession;
import com.orangelabs.rcs.core.ims.service.sharing.transfer.TerminatingContentSharingSession;
import com.orangelabs.rcs.service.api.client.media.IMediaPlayer;
import com.orangelabs.rcs.utils.PhoneUtils;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Content sharing service
 * 
 * @author jexa7410
 */
public class ContentSharingService extends ImsService {
    /**
     * Video share features tags
     */
    public final static String[] FEATURE_TAGS_VIDEO_SHARE = { FeatureTags.FEATURE_RCSE_VIDEO_SHARE };
    
    /**
     * Image share features tags
     */
    public final static String[] FEATURE_TAGS_IMAGE_SHARE = { FeatureTags.FEATURE_RCSE_VIDEO_SHARE ,
    	FeatureTags.FEATURE_RCSE + "=\"" + FeatureTags.FEATURE_RCSE_IMAGE_SHARE + "\"" };
	
	/**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());

    /**
	 * Constructor
	 * 
	 * @param parent IMS module
	 * @param activated Activation flag
	 * @throws CoreException
	 */
	public ContentSharingService(ImsModule parent) throws CoreException {
		super(parent, "csh_service.xml", true);
	}
    
    /**
	 * Start the IMS service 
	 */
	public synchronized void start() {
		if (isServiceStarted()) {
			// Already started
			return;
		}
		setServiceStarted(true);
	}

	/**
	 * Stop the IMS service 
	 */
	public synchronized void stop() {
		if (!isServiceStarted()) {
			// Already stopped
			return;
		}
		setServiceStarted(false);
	}
	
	/**
	 * Check the IMS service 
	 */
	public void check() {
	}

	/**
	 * Initiate an image sharing session
	 * 
	 * @param contact Remote contact
	 * @param content Content to be shared 
	 * @return CSh session 
	 */
	public ContentSharingTransferSession initiateImageSharingSession(String contact, MmContent content) {
		if (logger.isActivated()) {
			logger.info("Initiate image sharing session with contact " + contact + ", file " + content.toString());
		}
			
		// Create a new session
		OriginatingContentSharingSession session = new OriginatingContentSharingSession(
				this,
				content,
				PhoneUtils.formatNumberToSipUri(contact));
		
		// Start the session
		session.startSession();
		return session;
	}
	
	/**
	 * Initiate a pre-recorded video sharing session
	 * 
	 * @param contact Remote contact
	 * @param content Video content to share
	 * @param player Media player
	 * @return CSh session
	 */
	public ContentSharingStreamingSession initiatePreRecordedVideoSharingSession(String contact, VideoContent content, IMediaPlayer player) {
		if (logger.isActivated()) {
			logger.info("Initiate a pre-recorded video sharing session with contact " + contact + ", file " + content.toString());
		}

		// Create a new session
		OriginatingPreRecordedVideoContentSharingSession session = new OriginatingPreRecordedVideoContentSharingSession(
				this,
				player,
				content,
				PhoneUtils.formatNumberToSipUri(contact));
		
		// Start the session
		session.startSession();
		return session;
	}
	
	/**
	 * Initiate a live video sharing session
	 * 
	 * @param contact Remote contact
	 * @param player Media player
	 * @return CSh session
	 */
	public ContentSharingStreamingSession initiateLiveVideoSharingSession(String contact, IMediaPlayer player) {
		if (logger.isActivated()) {
			logger.info("Initiate a live video sharing session");
		}

		// Create a live video content
		String codec = getConfig().getString("VideoCodec", "h263-2000");		
		LiveVideoContent content = ContentManager.createLiveVideoContent(codec);
		
		// Create a new session
		OriginatingLiveVideoContentSharingSession session = new OriginatingLiveVideoContentSharingSession(
				this,
				player,
				content,
				PhoneUtils.formatNumberToSipUri(contact));
		
		// Start the session
		session.startSession();
		return session;
	}

	/**
	 * Receive a video sharing invitation
	 * 
	 * @param invite Initial invite
	 */
	public void receiveVideoSharingInvitation(SipRequest invite) {	
		// Create a new session
		ContentSharingStreamingSession session = new TerminatingVideoContentSharingSession(this, invite);
		
		// Start the session
		session.startSession();

		// Notify listener
		getImsModule().getCore().getListener().handleContentSharingStreamingInvitation(session);
	}
	
	/**
	 * Receive an image sharing invitation
	 * 
	 * @param invite Initial invite
	 */
	public void receiveImageSharingInvitation(SipRequest invite) {
		if (logger.isActivated()) {
    		logger.info("Receive an image sharing session invitation");
    	}

		// Test image size
		// TODO
		
		// Create a new session
    	ContentSharingTransferSession session = new TerminatingContentSharingSession(this, invite);
		
		// Start the session
		session.startSession();

		// Notify listener
		getImsModule().getCore().getListener().handleContentSharingTransferInvitation(session);
	}
}
