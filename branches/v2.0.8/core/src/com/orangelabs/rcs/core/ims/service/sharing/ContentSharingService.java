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
package com.orangelabs.rcs.core.ims.service.sharing;

import com.orangelabs.rcs.core.CoreException;
import com.orangelabs.rcs.core.content.ContentManager;
import com.orangelabs.rcs.core.content.LiveVideoContent;
import com.orangelabs.rcs.core.content.MmContent;
import com.orangelabs.rcs.core.content.VideoContent;
import com.orangelabs.rcs.core.ims.ImsModule;
import com.orangelabs.rcs.core.ims.network.sip.SipMessageFactory;
import com.orangelabs.rcs.core.ims.protocol.sip.SipRequest;
import com.orangelabs.rcs.core.ims.protocol.sip.SipResponse;
import com.orangelabs.rcs.core.ims.service.ImsService;
import com.orangelabs.rcs.core.ims.service.sharing.streaming.ContentSharingStreamingSession;
import com.orangelabs.rcs.core.ims.service.sharing.streaming.OriginatingLiveVideoContentSharingSession;
import com.orangelabs.rcs.core.ims.service.sharing.streaming.OriginatingPreRecordedVideoContentSharingSession;
import com.orangelabs.rcs.core.ims.service.sharing.streaming.TerminatingVideoContentSharingSession;
import com.orangelabs.rcs.core.ims.service.sharing.transfer.ContentSharingTransferSession;
import com.orangelabs.rcs.core.ims.service.sharing.transfer.OriginatingContentSharingSession;
import com.orangelabs.rcs.core.ims.service.sharing.transfer.TerminatingContentSharingSession;
import com.orangelabs.rcs.core.media.MediaPlayer;
import com.orangelabs.rcs.utils.PhoneUtils;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Content sharing service
 * 
 * @author jexa7410
 */
public class ContentSharingService extends ImsService {
	/**
	 * Rich call mode
	 */
	private static boolean RICH_CALL = false;
	
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
	public ContentSharingService(ImsModule parent, boolean activated) throws CoreException {
		super(parent, "csh_service.xml", activated);
	}

    /**
     * Set the rich call flag
     * 
     * @param flag Activation flag
     */
    public static void setRichCall(boolean flag) {
    	RICH_CALL = flag;
    }
	
    
    /**
     * Set the rich call flag
     * 
     * @return Boolean
     */
    public static boolean isRichCall() {
    	return RICH_CALL;
    }
    
    /**
	 * Start the IMS service 
	 */
	public void start() {
	}

	/**
	 * Stop the IMS service 
	 */
	public void stop() {
	}
	
	/**
	 * Check the IMS service 
	 */
	public void check() {
	}

	/**
	 * Send a content (transfer)
	 * 
	 * @param contact Remote contact
	 * @param content Content to be shared 
	 * @return CSh session 
	 */
	public ContentSharingTransferSession sendContent(String contact, MmContent content) {
		if (logger.isActivated()) {
			logger.info("Send content " + content.toString());
		}
			
		// Create a new session
		OriginatingContentSharingSession session = new OriginatingContentSharingSession(
				this,
				content,
				PhoneUtils.formatNumberToSipAddress(contact));
		
		// Start the session
		session.startSession();
		return session;
	}
	
	/**
	 * Stream a pre-recorded video content
	 * 
	 * @param contact Remote contact
	 * @param player Media player
	 * @param content Video content to share
	 * @return CSh session
	 */
	public ContentSharingStreamingSession streamPreRecordedVideoContent(String contact, MediaPlayer player, VideoContent content) {
		if (logger.isActivated()) {
			logger.info("Stream pre-recorded video content " + content.toString());
		}

		// Create a new session
		OriginatingPreRecordedVideoContentSharingSession session = new OriginatingPreRecordedVideoContentSharingSession(
				this,
				player,
				content,
				PhoneUtils.formatNumberToSipAddress(contact));
		
		// Start the session
		session.startSession();
		return session;
	}
	
	/**
	 * Stream a live video content (streaming)
	 * 
	 * @param contact Remote contact
	 * @param player Media player
	 * @return CSh session
	 */
	public ContentSharingStreamingSession streamLiveVideoContent(String contact, MediaPlayer player) {
		if (logger.isActivated()) {
			logger.info("Stream live video content");
		}

		// Create a live video content
		String codec = getConfig().getString("VideoCodec", "h263-2000");		
		LiveVideoContent content = ContentManager.createLiveVideoContent(codec);
		
		// Create a new session
		OriginatingLiveVideoContentSharingSession session = new OriginatingLiveVideoContentSharingSession(
				this,
				player,
				content,
				PhoneUtils.formatNumberToSipAddress(contact));
		
		// Start the session
		session.startSession();
		return session;
	}

	/**
	 * Reveive image sharing invitation
	 * 
	 * @param invite Initial invite
	 */
	public void receiveImageSharingInvitation(SipRequest invite) {
		// Test if there is already a CS session 
		if (getNumberOfSessions() > 0) {
			try {
				// Send a 486 Busy response
		    	if (logger.isActivated()) {
		    		logger.info("Send 486 Busy here");
		    	}
		        SipResponse resp = SipMessageFactory.createResponse(invite, 486);
		        getImsModule().getSipManager().sendSipMessage(resp);
			} catch(Exception e) {
				if (logger.isActivated()) {
					logger.error("Can't send 486 Busy here", e);
				}
			}
			return;
		}

		// Create a new session
    	ContentSharingTransferSession session = new TerminatingContentSharingSession(
					this,
					invite);
		
		
		// Start the session
		session.startSession();

		// Notify listener
		getImsModule().getCore().getListener().handleContentSharingTransferInvitation(session);
	}
	
	/**
	 * Reveive a video streaming invitation
	 * 
	 * @param invite Initial invite
	 */
	public void receiveVideoStreamingInvitation(SipRequest invite) {	
		// Test if there is already a CS session 
		if (getNumberOfSessions() > 0) {
			try {
				// Send a 486 Busy response
		    	if (logger.isActivated()) {
		    		logger.info("Send 486 Busy here");
		    	}
		        SipResponse resp = SipMessageFactory.createResponse(invite, 486);
		        getImsModule().getSipManager().sendSipMessage(resp);
			} catch(Exception e) {
				if (logger.isActivated()) {
					logger.error("Can't send 486 Busy here", e);
				}
			}
			return;
		}

		// Create a new session
		ContentSharingStreamingSession session = new TerminatingVideoContentSharingSession(
					this,
					invite);

		
		// Start the session
		session.startSession();

		// Notify listener
		getImsModule().getCore().getListener().handleContentSharingStreamingInvitation(session);
	}
}
