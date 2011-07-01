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

package com.orangelabs.rcs.core.ims.service.sharing.streaming;

import com.orangelabs.rcs.core.content.ContentManager;
import com.orangelabs.rcs.core.ims.network.sip.SipManager;
import com.orangelabs.rcs.core.ims.network.sip.SipMessageFactory;
import com.orangelabs.rcs.core.ims.network.sip.SipUtils;
import com.orangelabs.rcs.core.ims.protocol.rtp.MediaRegistry;
import com.orangelabs.rcs.core.ims.protocol.sdp.MediaAttribute;
import com.orangelabs.rcs.core.ims.protocol.sdp.MediaDescription;
import com.orangelabs.rcs.core.ims.protocol.sdp.SdpParser;
import com.orangelabs.rcs.core.ims.protocol.sdp.SdpUtils;
import com.orangelabs.rcs.core.ims.protocol.sip.SipRequest;
import com.orangelabs.rcs.core.ims.protocol.sip.SipResponse;
import com.orangelabs.rcs.core.ims.protocol.sip.SipTransactionContext;
import com.orangelabs.rcs.core.ims.service.ImsService;
import com.orangelabs.rcs.core.ims.service.ImsServiceSession;
import com.orangelabs.rcs.core.ims.service.SessionTimerManager;
import com.orangelabs.rcs.core.ims.service.sharing.ContentSharingError;
import com.orangelabs.rcs.core.ims.service.sharing.ContentSharingService;
import com.orangelabs.rcs.service.api.client.media.IMediaEventListener;
import com.orangelabs.rcs.utils.Config;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Terminating live video content sharing session (streaming)
 * 
 * @author jexa7410
 */
public class TerminatingVideoContentSharingSession extends ContentSharingStreamingSession {
	/**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     * Constructor
     * 
	 * @param parent IMS service
	 * @param invite Initial INVITE request
	 */
	public TerminatingVideoContentSharingSession(ImsService parent, SipRequest invite) {
		super(parent, ContentManager.createLiveVideoContentFromSdp(invite.getContentBytes()), SipUtils.getAssertedIdentity(invite));

		// Create dialog path
		createTerminatingDialogPath(invite);
	}
		
	/**
	 * Background processing
	 */
	public void run() {
		try {		
	    	if (logger.isActivated()) {
	    		logger.info("Initiate a new live video sharing session as terminating");
	    	}
	
	    	// Send a 180 Ringing response
			send180Ringing(getDialogPath().getInvite(), getDialogPath().getLocalTag());
        
	        // Parse the remote SDP part
	        SdpParser parser = new SdpParser(getDialogPath().getRemoteContent().getBytes());
            String remoteHost = SdpUtils.extractRemoteHost(parser.sessionDescription.connectionInfo);    		
    		MediaDescription mediaVideo = parser.getMediaDescription("video");
    		Config config = getImsService().getConfig();
            int remotePort = mediaVideo.port;
			
    		// Extract the payload type
    		int payload = mediaVideo.payload_type;
            String rtpmap = mediaVideo.getMediaAttribute("rtpmap").getValue();
            
            // Extract the video encoding
            String encoding = rtpmap.substring(rtpmap.indexOf(mediaVideo.payload)+mediaVideo.payload.length()+1);
            String codec = encoding.toLowerCase().trim();
            String clockRate = "";
            int index = encoding.indexOf("/");
			if (index != -1) {
				codec = encoding.substring(0, index);
				clockRate = "/" + encoding.substring(index+1);
			}
			if (logger.isActivated()) {
				logger.debug("Video codec: " + codec);
			}
			
			// Check if the codec is supported
    		if (!MediaRegistry.isCodecSupported(codec)) {
    			if (logger.isActivated()){
    				logger.debug("Codec " + codec + " is not supported");
    			}
    			
        		// Send a 415 Unsupported media type response
				send415Error(getDialogPath().getInvite());

				// Unsupported media type
				handleError(new ContentSharingError(ContentSharingError.UNSUPPORTED_MEDIA_TYPE, encoding));
        		return;
    		}
    		
            // Extract the video frame size. If not specified by remote, default value is read from config file 
           	MediaAttribute frameSize = mediaVideo.getMediaAttribute("framesize");
           	int videoWidth;
           	int videoHeight;
           	if (frameSize != null) {
        		String value = frameSize.getValue();
        		int index2 = value.indexOf(mediaVideo.payload); 
    			int separator = value.indexOf('-');
    			videoWidth = Integer.parseInt(value.substring(index2+mediaVideo.payload.length()+1, separator));
    			videoHeight = Integer.parseInt(value.substring(separator+1));
				if (logger.isActivated()) {
					logger.debug("Frame size: " + videoWidth + "-" + videoHeight);
				}
        	} else {
               	videoWidth = config.getInteger("VideoWidth");
               	videoHeight = config.getInteger("VideoHeight");
				if (logger.isActivated()) {
					logger.debug("Default frame size is used: " +  videoWidth + "-" + videoHeight);
				}
        	}
           	
			// Check if the video size is supported
    		if ((videoWidth != 176) || (videoHeight != 144)) {
    			if (logger.isActivated()){
    				logger.debug("Video size " + videoWidth + "x" + videoHeight + " is not supported");
    			}
    			
        		// Send a 415 Unsupported media type response
				send415Error(getDialogPath().getInvite());

				// Unsupported media type
				handleError(new ContentSharingError(ContentSharingError.UNSUPPORTED_MEDIA_TYPE, encoding));
        		return;
    		}
           	
           	// Extract the frame rate. If not specified by remote, default value is read from config file 
        	MediaAttribute attr = mediaVideo.getMediaAttribute("framerate");
        	String frameRate;
        	if (attr != null) {
        		frameRate = attr.getValue();
				if (logger.isActivated()) {
					logger.debug("Frame rate: " + frameRate);
				}
        	} else {
    	    	frameRate = config.getString("VideoFrameRate");
				if (logger.isActivated()) {
					logger.debug("Default frame rate is used: " + frameRate);
				}
        	}
        	
        	// Extract the video codec parameters. If not specified by remote, default value is read from config file
        	MediaAttribute fmtp = mediaVideo.getMediaAttribute("fmtp");
        	String codecParameters;
        	if (fmtp != null){
        		codecParameters = fmtp.getValue();
				if (logger.isActivated()) {
					logger.debug("Codec parameters: " + codecParameters);
				}
        	} else {
    	    	codecParameters = payload + " " + config.getString("VideoCodecParams");
				if (logger.isActivated()) {
					logger.debug("Default codec parameters is used: " + codecParameters);
				}
        	}
        	
			// Wait invitation answer
	    	int answer = waitInvitationAnswer();
			if (answer == ImsServiceSession.INVITATION_REJECTED) {
				if (logger.isActivated()) {
					logger.debug("Session has been rejected by user");
				}
				
		    	// Remove the current session
		    	getImsService().removeSession(this);

		    	// Notify listener
		        if (getListener() != null) {
	        		getListener().handleSessionAborted();
		        }
				return;
			} else
			if (answer == ImsServiceSession.INVITATION_NOT_ANSWERED) {
				if (logger.isActivated()) {
					logger.debug("Session has been rejected on timeout");
				}

				// Ringing period timeout
				send603Decline(getDialogPath().getInvite(), getDialogPath().getLocalTag());
				
		    	// Remove the current session
		    	getImsService().removeSession(this);

		    	// Notify listener
		        if (getListener() != null) {
	        		getListener().handleSessionAborted();
		        }
				return;
			}
			
			// Check that a media renderer has been set
			if (getMediaRenderer() == null) {
				handleError(new ContentSharingError(ContentSharingError.MEDIA_RENDERER_NOT_INITIALIZED));
				return;
			}
		
        	// Set media renderer event listener
        	getMediaRenderer().addListener(new MediaPlayerEventListener(this));

        	// Open the media renderer
        	getMediaRenderer().open(remoteHost, remotePort);
	    	
			// Build SDP part
			String ntpTime = SipUtils.constructNTPtime(System.currentTimeMillis());
			String sdp =
				"v=0" + SipUtils.CRLF +
	            "o=- " + ntpTime + " " + ntpTime + " IN IP4 " + getDialogPath().getSipStack().getLocalIpAddress() + SipUtils.CRLF +
				"s=-" + SipUtils.CRLF +
				"c=IN IP4 " + getDialogPath().getSipStack().getLocalIpAddress() + SipUtils.CRLF +
				"t=0 0" + SipUtils.CRLF +
				"m=video " + getMediaRenderer().getLocalRtpPort() + " RTP/AVP " + payload + SipUtils.CRLF +
				"b=AS:54" + SipUtils.CRLF +
	            "a=rtpmap:" + payload + " " + codec + clockRate + SipUtils.CRLF +
	            "a=framesize:" + payload + " " + videoWidth + "-" + videoHeight + SipUtils.CRLF +
        		"a=framerate:" + frameRate + SipUtils.CRLF +
	            "a=fmtp:" + codecParameters + SipUtils.CRLF +
				"a=recvonly" + SipUtils.CRLF;

	    	// Set X-Type attribute
	    	String xType = getXTypeAttribute();
	    	if (xType != null) {
	    		sdp += "a=X-type:" + xType + SipUtils.CRLF;
	    	}
			
			// Set the local SDP part in the dialog path
			getDialogPath().setLocalContent(sdp);

			// Create a 200 OK response
			if (logger.isActivated()) {
				logger.info("Send 200 OK");
			}
			SipResponse resp = SipMessageFactory.create200OkInviteResponse(getDialogPath(),
					ContentSharingService.FEATURE_TAGS_VIDEO_SHARE, sdp);

	        // Send response
	        SipTransactionContext ctx = getImsService().getImsModule().getSipManager().sendSipMessageAndWait(resp);

	        // The signalisation is established
	        getDialogPath().sigEstablished();

			// Wait response
			ctx.waitResponse(SipManager.TIMEOUT);

			// Analyze the received response 
			if (ctx.isSipAck()) {
				// ACK received
				if (logger.isActivated()) {
					logger.info("ACK request received");
				}

				// The session is established
				getDialogPath().sessionEstablished();

				// Start the media renderer
				getMediaRenderer().start();

            	// Start session timer
            	if (getSessionTimerManager().isSessionTimerActivated(resp)) {        	
            		getSessionTimerManager().start(SessionTimerManager.UAS_ROLE, getDialogPath().getSessionExpireTime());
            	}

            	// Notify listener
		        if (getListener() != null) {
		        	getListener().handleSessionStarted();
		        }
			} else {
	    		if (logger.isActivated()) {
	        		logger.debug("No ACK received for INVITE");
	        	}

	    		// No response received: timeout
            	handleError(new ContentSharingError(ContentSharingError.SESSION_INITIATION_FAILED));
			}
		} catch(Exception e) {
        	if (logger.isActivated()) {
        		logger.error("Session initiation has failed", e);
        	}

        	// Unexpected error
			handleError(new ContentSharingError(ContentSharingError.UNEXPECTED_EXCEPTION,
					e.getMessage()));
		}		
	}

	/**
	 * Handle error 
	 * 
	 * @param error Error
	 */
	public void handleError(ContentSharingError error) {
        // Error	
    	if (logger.isActivated()) {
    		logger.info("Session error: " + error.getErrorCode() + ", reason=" + error.getMessage());
    	}

    	// Close media session
    	closeMediaSession();
      
    	// Remove the current session
    	getImsService().removeSession(this);

		// Notify listener
    	if ((!isInterrupted()) && (getListener() != null)) {
        	getListener().handleSharingError(error);
        }
	}
	
	/**
	 * Returns the "X-type" attribute
	 * 
	 * @return String
	 */
	public String getXTypeAttribute() {
		return "videolive";
	}	

	/**
	 * Close media session
	 */
	public void closeMediaSession() {
		try {
			// Close the media renderer
	    	if (getMediaRenderer() != null) {
	    		getMediaRenderer().stop();
	    		getMediaRenderer().close();
	    	}
		} catch(Exception e) {
			if (logger.isActivated()) {
				logger.error("Exception when closing the media renderer", e);
			}
		}
	}

	/**
	 * Media player event listener
	 */
	private class MediaPlayerEventListener extends IMediaEventListener.Stub {
		/**
		 * Streaming session
		 */
		private ContentSharingStreamingSession session;
		
		/**
		 * Constructor
		 * 
		 * @param session Streaming session
		 */
		public MediaPlayerEventListener(ContentSharingStreamingSession session) {
			this.session = session;
		}
		
		/**
		 * Media player is opened
		 */
		public void mediaOpened() {
			if (logger.isActivated()) {
				logger.debug("Media renderer is opened");
			}
		}
		
		/**
		 * Media player is closed
		 */
		public void mediaClosed() {
			if (logger.isActivated()) {
				logger.debug("Media renderer is closed");
			}
		}
		
		/**
		 * Media player is started
		 */
		public void mediaStarted() {
			if (logger.isActivated()) {
				logger.debug("Media renderer is started");
			}
		}
		
		/**
		 * Media player is stopped
		 */
		public void mediaStopped() {
			if (logger.isActivated()) {
				logger.debug("Media renderer is stopped");
			}
		}

		/**
		 * Media player has failed
		 * 
		 * @param error Error
		 */
		public void mediaError(String error) {
			if (logger.isActivated()) {
				logger.error("Media renderer has failed: " + error);
			}
			
			// Close the media session
			closeMediaSession();
				
			// Terminate session
			terminateSession();

	    	// Remove the current session
	    	getImsService().removeSession(session);
	    	
	    	// Notify listener
	    	if ((!isInterrupted()) && (getListener() != null)) {
	        	getListener().handleSharingError(new ContentSharingError(ContentSharingError.MEDIA_STREAMING_FAILED, error));
	        }
		}
	}
}

