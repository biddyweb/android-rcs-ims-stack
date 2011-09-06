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

package com.orangelabs.rcs.core.ims.service.richcall;

import com.orangelabs.rcs.core.CoreException;
import com.orangelabs.rcs.core.content.LiveVideoContent;
import com.orangelabs.rcs.core.content.MmContent;
import com.orangelabs.rcs.core.content.VideoContent;
import com.orangelabs.rcs.core.ims.ImsModule;
import com.orangelabs.rcs.core.ims.network.sip.FeatureTags;
import com.orangelabs.rcs.core.ims.network.sip.SipMessageFactory;
import com.orangelabs.rcs.core.ims.network.sip.SipUtils;
import com.orangelabs.rcs.core.ims.protocol.sip.SipRequest;
import com.orangelabs.rcs.core.ims.protocol.sip.SipResponse;
import com.orangelabs.rcs.core.ims.service.ImsService;
import com.orangelabs.rcs.core.ims.service.ImsServiceSession;
import com.orangelabs.rcs.core.ims.service.capability.CapabilityUtils;
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

import java.util.Enumeration;

/**
 * Rich call service has in charge to monitor the GSM call in order to stop the
 * current content sharing when the call terminates, to process capability
 * request from remote and to request remote capabilities.
 * 
 * @author jexa7410
 */
public class RichcallService extends ImsService {
    /**
     * Video share features tags
     */
    public final static String[] FEATURE_TAGS_VIDEO_SHARE = { FeatureTags.FEATURE_3GPP_VIDEO_SHARE };

    /**
     * Image share features tags
     */
    public final static String[] FEATURE_TAGS_IMAGE_SHARE = { FeatureTags.FEATURE_3GPP_VIDEO_SHARE, FeatureTags.FEATURE_3GPP_IMAGE_SHARE };

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
	public RichcallService(ImsModule parent, boolean activated) throws CoreException {
        super(parent, activated);
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
     * @throws CoreException
     */
	public ContentSharingTransferSession initiateImageSharingSession(String contact, MmContent content) throws CoreException {
		if (logger.isActivated()) {
			logger.info("Initiate image sharing session with contact " + contact + ", file " + content.toString());
		}

        // TODO: authorize 1 session incoming and 1 session outgoing with the
        // same contact
        // // Test number of sessions
        // if (getNumberOfSessions() >= 1) {
        // if (logger.isActivated()) {
        // logger.debug("The max number of sharing sessions is achieved: cancel the initiation");
        // }
        // throw new CoreException("Max content sharing sessions achieved");
        // }

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
     * @throws CoreException
     */
	public ContentSharingStreamingSession initiatePreRecordedVideoSharingSession(String contact, VideoContent content, IMediaPlayer player) throws CoreException {
		if (logger.isActivated()) {
			logger.info("Initiate a pre-recorded video sharing session with contact " + contact + ", file " + content.toString());
		}

        // TODO: authorize 1 session incoming and 1 session outgoing with the
        // same contact
        // // Test number of sessions
        // if (getNumberOfSessions() >= 1) {
        // if (logger.isActivated()) {
        // logger.debug("The max number of sharing sessions is achieved: cancel the initiation");
        // }
        // throw new CoreException("Max content sharing sessions achieved");
        // }

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
     * @param content Video content to share
     * @param player Media player
     * @return CSh session
     * @throws CoreException
     */
	public ContentSharingStreamingSession initiateLiveVideoSharingSession(String contact, LiveVideoContent content, IMediaPlayer player) throws CoreException {
		if (logger.isActivated()) {
			logger.info("Initiate a live video sharing session");
		}

        // TODO: authorize 1 session incoming and 1 session outgoing with the
        // same contact
        // // Test number of sessions
        // if (getNumberOfSessions() >= 1) {
        // if (logger.isActivated()) {
        // logger.debug("The max number of sharing sessions is achieved: cancel the initiation");
        // }
        // throw new CoreException("Max content sharing sessions achieved");
        // }

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
		// Test if call is established
		if (!getImsModule().getCallManager().isConnected()) {
			if (logger.isActivated()) {
				logger.debug("Rich call not established: reject the invitation");
			}
			try {
				// Create a 606 Not Acceptable response
		    	if (logger.isActivated()) {
		    		logger.info("Send 606 Not Acceptable");
		    	}
		        SipResponse resp = SipMessageFactory.createResponse(invite, 606);

		        // Send response
		        getImsModule().getSipManager().sendSipResponse(resp);
			} catch(Exception e) {
				if (logger.isActivated()) {
					logger.error("Can't send 606 Not Acceptable", e);
				}
			}
			return;
		}

        // TODO: authorize 1 session incoming and 1 session outgoing with the
        // same contact
        // // Test number of session
        // if (getNumberOfSessions() > 0) {
        // if (logger.isActivated()) {
        // logger.debug("The max number of sharing sessions is achieved: reject the invitation");
        // }
        // try {
        // // Send a 486 Busy response
        // if (logger.isActivated()) {
        // logger.info("Send 486 Busy here");
        // }
        // SipResponse resp = SipMessageFactory.createResponse(invite, 486);
        //
        // // Send response
        // getImsModule().getSipManager().sendSipResponse(resp);
        // } catch(Exception e) {
        // if (logger.isActivated()) {
        // logger.error("Can't send 486 Busy here", e);
        // }
        // }
        // return;
        // }

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

		// Test if call is established
		if (!getImsModule().getCallManager().isConnected()) {
			if (logger.isActivated()) {
				logger.debug("Rich call not established: reject the invitation");
			}
			try {
				// Create a 606 Not Acceptable response
		    	if (logger.isActivated()) {
		    		logger.info("Send 606 Not Acceptable");
		    	}
		        SipResponse resp = SipMessageFactory.createResponse(invite, 606);

		        // Send response
		        getImsModule().getSipManager().sendSipResponse(resp);
			} catch(Exception e) {
				if (logger.isActivated()) {
					logger.error("Can't send 606 Not Acceptable", e);
				}
			}
			return;
		}

        // TODO: authorize 1 session incoming and 1 session outgoing with the
        // same contact
        // // Test number of session
        // if (getNumberOfSessions() > 0) {
        // if (logger.isActivated()) {
        // logger.debug("The max number of sharing sessions is achieved: reject the invitation");
        // }
        // try {
        // // Create a 486 Busy response
        // if (logger.isActivated()) {
        // logger.info("Send 486 Busy here");
        // }
        // SipResponse resp = SipMessageFactory.createResponse(invite, 486);
        //
        // // Send response
        // getImsModule().getSipManager().sendSipResponse(resp);
        // } catch(Exception e) {
        // if (logger.isActivated()) {
        // logger.error("Can't send 486 Busy here", e);
        // }
        // }
        // return;
        // }

		// Create a new session
    	ContentSharingTransferSession session = new TerminatingContentSharingSession(this, invite);

		// Start the session
		session.startSession();

		// Notify listener
		getImsModule().getCore().getListener().handleContentSharingTransferInvitation(session);
	}

    /**
     * Receive a capability request (options procedure)
     * 
     * @param options Received options message
     */
    public void receiveCapabilityRequest(SipRequest options) {
    	String contact = SipUtils.getAssertedIdentity(options);

    	if (logger.isActivated()) {
			logger.debug("OPTIONS request received during a call from " + contact);
		}

	    try {
	    	// Create 200 OK response
	    	String ipAddress = getImsModule().getCurrentNetworkInterface().getNetworkAccess().getIpAddress();
	        SipResponse resp = SipMessageFactory.create200OkOptionsResponse(options,
	        		getImsModule().getSipManager().getSipStack().getLocalContact(),
	        		CapabilityUtils.getSupportedFeatureTags(true),
	        		CapabilityUtils.buildSdp(ipAddress));

	        // Send 200 OK response
	        getImsModule().getSipManager().sendSipResponse(resp);
	    } catch(Exception e) {
        	if (logger.isActivated()) {
        		logger.error("Can't send 200 OK for OPTIONS", e);
        	}
	    }

	    // Request also capabilities to the remote if it's an outgoing call
		if (getImsModule().getCallManager().isConnectedWith(contact) && !getImsModule().getCallManager().isIncomingCall()) {
			// Outgoing call is received: request capabilities
			getImsModule().getCapabilityService().requestContactCapabilities(contact);
		}
    }

	/**
	 * Abort all pending sessions
	 */
	public void abortAllSessions() {
		if (logger.isActivated()) {
			logger.debug("Abort all pending sessions");
		}
		for (Enumeration<ImsServiceSession> e = getSessions(); e.hasMoreElements() ;) {
			ImsServiceSession session = (ImsServiceSession)e.nextElement();
			if (logger.isActivated()) {
				logger.debug("Abort pending session " + session.getSessionID());
			}
			session.abortSession();
		}
    }
}
