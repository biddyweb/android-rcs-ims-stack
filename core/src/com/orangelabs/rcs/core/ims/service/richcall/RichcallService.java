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

import java.util.Vector;

import com.orangelabs.rcs.core.CoreException;
import com.orangelabs.rcs.core.content.MmContent;
import com.orangelabs.rcs.core.content.VideoContent;
import com.orangelabs.rcs.core.ims.ImsModule;
import com.orangelabs.rcs.core.ims.network.sip.SipMessageFactory;
import com.orangelabs.rcs.core.ims.network.sip.SipUtils;
import com.orangelabs.rcs.core.ims.protocol.rtp.MediaRegistry;
import com.orangelabs.rcs.core.ims.protocol.rtp.format.video.VideoFormat;
import com.orangelabs.rcs.core.ims.protocol.sip.SipRequest;
import com.orangelabs.rcs.core.ims.protocol.sip.SipResponse;
import com.orangelabs.rcs.core.ims.service.ImsService;
import com.orangelabs.rcs.core.ims.service.sharing.streaming.ContentSharingStreamingSession;
import com.orangelabs.rcs.core.ims.service.sharing.transfer.ContentSharingTransferSession;
import com.orangelabs.rcs.provider.settings.RcsSettings;
import com.orangelabs.rcs.service.api.client.media.IMediaPlayer;
import com.orangelabs.rcs.utils.MimeManager;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Rich call service has in charge to monitor the GSM call in order to stop the current
 * content sharing when the call terminates, to process capability request from remote
 * and to request remote capabilities.
 *  
 * @author jexa7410
 */
public class RichcallService extends ImsService {
	/**
     * Video sharing config
     */
    private String videoSharingConfig;
    
	/**
     * Image sharing config
     */
    private String imageSharingConfig;

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
		super(parent, "richcall_service.xml", activated);
		
    	// Get video config
    	Vector<VideoFormat> videoFormats = MediaRegistry.getSupportedVideoFormats();
    	videoSharingConfig = "";
    	for(int i=0; i < videoFormats.size(); i++) {
    		VideoFormat fmt = videoFormats.elementAt(i);
    		videoSharingConfig += "m=video 0 RTP/AVP " + fmt.getPayload() + SipUtils.CRLF;
    		videoSharingConfig += "a=rtpmap:" + fmt.getPayload() + " " + fmt.getCodec() + SipUtils.CRLF;
    	}
    	
    	// Get supported image MIME types
    	String supportedImageFormats = "";
    	Vector<String> mimeTypes = MimeManager.getSupportedImageMimeTypes();
    	for(int i=0; i < mimeTypes.size(); i++) {
			supportedImageFormats += mimeTypes.elementAt(i) + " ";
	    }    	    	
		supportedImageFormats = supportedImageFormats.trim();

		// Get the image config
    	imageSharingConfig = "m=message 0 TCP/MSRP *"  + SipUtils.CRLF +
    		"a=accept-types:" + supportedImageFormats + SipUtils.CRLF +
    		"a=file-selector" + SipUtils.CRLF +
    		"a=max-size:" + ContentSharingTransferSession.MAX_CONTENT_SIZE + SipUtils.CRLF;
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
     * Build an SDP part
     * 
     * @return SDP part
     */
    private String buildSdp() {
	    try {
	    	// Build the local SDP
	    	String ipAddress = getImsModule().getCurrentNetworkInterface().getNetworkAccess().getIpAddress();
	    	String ntpTime = SipUtils.constructNTPtime(System.currentTimeMillis());
	    	String sdp = "v=0" + SipUtils.CRLF +
		        	"o=- " + ntpTime + " " + ntpTime + " IN IP4 " + ipAddress + SipUtils.CRLF +
		            "s=-" + SipUtils.CRLF +
		            "c=IN IP4 " + ipAddress + SipUtils.CRLF +
		            "t=0 0" + SipUtils.CRLF;
	        if (RcsSettings.getInstance().isVideoSharingSupported()) {
		    	sdp += videoSharingConfig;
	        }
	        if (RcsSettings.getInstance().isImageSharingSupported()) {
		    	sdp += imageSharingConfig;
	        }
	        return sdp;
	    } catch(Exception e) {
        	if (logger.isActivated()) {
        		logger.error("Can't build SDP part", e);
        	}
        	return null;
	    }
    } 
    
    /**
	 * Initiate an image sharing session
	 * 
	 * @param contact Remote contact
	 * @param content Content to be shared 
	 * @return CSh session 
	 */
	public ContentSharingTransferSession initiateImageSharingSession(String contact, MmContent content) {
		// TODO: test we are in call with contact
		return getImsModule().getContentSharingService().initiateImageSharingSession(contact, content);
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
		// TODO: test we are in call with contact
		return getImsModule().getContentSharingService().initiatePreRecordedVideoSharingSession(contact, content, player);
	}
	
	/**
	 * Initiate a live video sharing session
	 * 
	 * @param contact Remote contact
	 * @param player Media player
	 * @return CSh session
	 */
	public ContentSharingStreamingSession initiateLiveVideoSharingSession(String contact, IMediaPlayer player) {
		return getImsModule().getContentSharingService().initiateLiveVideoSharingSession(contact, player);
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

		// Test number of session 
		if (getNumberOfSessions() > 0) {
			if (logger.isActivated()) {
				logger.debug("The max number of sharing sessions is achieved: reject the invitation");
			}
			try {
				// Send a 486 Busy response
		    	if (logger.isActivated()) {
		    		logger.info("Send 486 Busy here");
		    	}
		        SipResponse resp = SipMessageFactory.createResponse(invite, 486);

		        // Send response
		        getImsModule().getSipManager().sendSipResponse(resp);
			} catch(Exception e) {
				if (logger.isActivated()) {
					logger.error("Can't send 486 Busy here", e);
				}
			}
			return;
		}
		
		// Process the session invitation
		getImsModule().getContentSharingService().receiveVideoSharingInvitation(invite);
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

		// Test number of session 
		if (getNumberOfSessions() > 0) {
			if (logger.isActivated()) {
				logger.debug("The max number of sharing sessions is achieved: reject the invitation");
			}
			try {
				// Create a 486 Busy response
		    	if (logger.isActivated()) {
		    		logger.info("Send 486 Busy here");
		    	}
		        SipResponse resp = SipMessageFactory.createResponse(invite, 486);
		        
		        // Send response
		        getImsModule().getSipManager().sendSipResponse(resp);
			} catch(Exception e) {
				if (logger.isActivated()) {
					logger.error("Can't send 486 Busy here", e);
				}
			}
			return;
		}
		
		// Process the session invitation
		getImsModule().getContentSharingService().receiveImageSharingInvitation(invite);
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
	    	// Build the local SDP
    		String sdp = buildSdp();

	    	// Create 200 OK response
	        SipResponse resp = SipMessageFactory.create200OkOptionsResponse(options,
	        		getImsModule().getSipManager().getSipStack().getContactHeader(),
	        		SipUtils.getSupportedFeatureTags(true), sdp);

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
}
