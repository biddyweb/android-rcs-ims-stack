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

import java.util.Vector;

import com.orangelabs.rcs.core.content.LiveVideoContent;
import com.orangelabs.rcs.core.ims.network.sip.SipManager;
import com.orangelabs.rcs.core.ims.network.sip.SipMessageFactory;
import com.orangelabs.rcs.core.ims.network.sip.SipUtils;
import com.orangelabs.rcs.core.ims.protocol.rtp.MediaRegistry;
import com.orangelabs.rcs.core.ims.protocol.rtp.format.video.VideoFormat;
import com.orangelabs.rcs.core.ims.protocol.sdp.MediaDescription;
import com.orangelabs.rcs.core.ims.protocol.sdp.SdpParser;
import com.orangelabs.rcs.core.ims.protocol.sdp.SdpUtils;
import com.orangelabs.rcs.core.ims.protocol.sip.SipException;
import com.orangelabs.rcs.core.ims.protocol.sip.SipRequest;
import com.orangelabs.rcs.core.ims.protocol.sip.SipResponse;
import com.orangelabs.rcs.core.ims.protocol.sip.SipTransactionContext;
import com.orangelabs.rcs.core.ims.service.ImsService;
import com.orangelabs.rcs.core.ims.service.ImsServiceSession;
import com.orangelabs.rcs.core.ims.service.sharing.ContentSharingError;
import com.orangelabs.rcs.core.ims.service.sharing.ContentSharingService;
import com.orangelabs.rcs.service.api.client.media.IMediaPlayer;
import com.orangelabs.rcs.service.api.client.media.IMediaEventListener;
import com.orangelabs.rcs.utils.Config;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Originating live video content sharing session (streaming)
 * 
 * @author jexa7410
 */
public class OriginatingLiveVideoContentSharingSession extends ContentSharingStreamingSession {
    /**
	 * Media player
	 */
	private IMediaPlayer player = null;

	/**
	 * Video format
	 */
	private VideoFormat videoFormat = null;
	
    /**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());

	/**
	 * Constructor
	 * 
	 * @param parent IMS service
	 * @param player Media player
	 * @param content Content to be shared
	 * @param contact Remote contact
	 */
	public OriginatingLiveVideoContentSharingSession(ImsService parent, IMediaPlayer player, LiveVideoContent content, String contact) {
		super(parent, content, contact);
		
		// Create dialog path
		createOriginatingDialogPath();

		// Create the video format
    	String codec = getContent().getCodec();
		videoFormat = (VideoFormat)MediaRegistry.generateFormat(codec);
		
		// Set the media player 
		this.player = player;
	}

	/**
	 * Background processing
	 */
	public void run() {
		try {
	    	if (logger.isActivated()) {
	    		logger.info("Initiate a new live video sharing session as originating");
	    	}
    	
	        // Build SDP part
	    	Config config = getImsService().getConfig();
	    	int videoWidth = config.getInteger("VideoWidth"); 
	    	int videoHeight = config.getInteger("VideoHeight");
	    	String clockRate = config.getString("VideoClockRate");
	    	String frameRate = config.getString("VideoFrameRate");
	    	String codecParameters = config.getString("VideoCodecParams");
			int payload = videoFormat.getPayload();
	    	String ntpTime = SipUtils.constructNTPtime(System.currentTimeMillis());
			String sdp =
	    		"v=0" + SipUtils.CRLF +
	            "o=- " + ntpTime + " " + ntpTime + " IN IP4 " + getDialogPath().getSipStack().getLocalIpAddress() + SipUtils.CRLF +
	            "s=-" + SipUtils.CRLF +
				"c=IN IP4 " + getDialogPath().getSipStack().getLocalIpAddress() + SipUtils.CRLF +
	            "t=0 0" + SipUtils.CRLF +
	            "m=video " + player.getLocalRtpPort() + " RTP/AVP " + payload + SipUtils.CRLF + 
	            "a=rtpmap:" + payload + " " + videoFormat.getCodec() + "/" + clockRate + SipUtils.CRLF +
	            "a=framesize:" + payload + " " + videoWidth + "-" + videoHeight + SipUtils.CRLF +
	            "a=framerate:" + frameRate + SipUtils.CRLF +
	            "a=fmtp:" + payload + " "  + codecParameters + SipUtils.CRLF + 	            
	    		"a=sendonly" + SipUtils.CRLF;
	    	
	    	// Set X-Type attribute
	    	String xType = getXTypeAttribute();
	    	if (xType != null) {
	    		sdp += "a=X-type:" + xType + SipUtils.CRLF;
	    	}

			// Set the local SDP part in the dialog path
	        getDialogPath().setLocalContent(sdp);

	        // Create an INVITE request
	        if (logger.isActivated()) {
	        	logger.info("Send INVITE");
	        }
	        SipRequest invite = SipMessageFactory.createInvite(getDialogPath(),
	        		ContentSharingService.FEATURE_TAGS_VIDEO_SHARE, sdp);

	        // Set initial request in the dialog path
	        getDialogPath().setInvite(invite);
	        
	        // Send INVITE request
	        sendInvite(invite);	        
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
	 * Send INVITE message
	 * 
	 * @param invite SIP INVITE
	 * @throws SipException
	 */
	private void sendInvite(SipRequest invite) throws SipException {
		// Send INVITE request
		SipTransactionContext ctx = getImsService().getImsModule().getSipManager().sendSipMessageAndWait(invite);
		
        // Wait response
        ctx.waitResponse(ImsServiceSession.RINGING_PERIOD + SipManager.TIMEOUT);
        
        // Analyze the received response 
        if (ctx.isSipResponse()) {
	        // A response has been received
            if (ctx.getStatusCode() == 200) {
            	// 200 OK
            	handle200OK(ctx.getSipResponse());
            } else
            if (ctx.getStatusCode() == 407) {
            	// 407 Proxy Authentication Required
            	handle407Authentication(ctx.getSipResponse());
            } else {
            	// Error response
                if (ctx.getStatusCode() == 603) {
                	handleError(new ContentSharingError(ContentSharingError.SESSION_INITIATION_DECLINED,
        					ctx.getReasonPhrase()));
                } else
                if (ctx.getStatusCode() == 487) {
                	handleError(new ContentSharingError(ContentSharingError.SESSION_INITIATION_CANCELLED,
        					ctx.getReasonPhrase()));
                } else {
                	handleError(new ContentSharingError(ContentSharingError.SESSION_INITIATION_FAILED,
                			ctx.getStatusCode() + " " + ctx.getReasonPhrase()));
                }
            }
        } else {
        	// No response received: timeout
        	handleError(new ContentSharingError(ContentSharingError.SESSION_INITIATION_FAILED, "timeout"));
        }
	}	

	/**
	 * Handle 200 0K response 
	 * 
	 * @param resp 200 OK response
	 */
	public void handle200OK(SipResponse resp) {
		try {
	        // 200 OK received
			if (logger.isActivated()) {
				logger.info("200 OK response received");
			}

	        // The signalisation is established
	        getDialogPath().sigEstablished();

	        // Set the remote tag
	        getDialogPath().setRemoteTag(resp.getToTag());
	        
	        // Set the target
	        getDialogPath().setTarget(resp.getContactURI());
	
	        // Set the route path with the Record-Route header
	        Vector<String> newRoute = SipUtils.routeProcessing(resp, true);
			getDialogPath().setRoute(newRoute);
	
	        // Set the remote SDP part
	        getDialogPath().setRemoteContent(resp.getContent());
	                      		
	        // The session is established
	        getDialogPath().sessionEstablished();

	        // Parse the remote SDP part
        	SdpParser parser = new SdpParser(getDialogPath().getRemoteContent().getBytes());
            String remoteHost = SdpUtils.extractRemoteHost(parser.sessionDescription.connectionInfo);    		
            MediaDescription mediaVideo = parser.getMediaDescription("video");
            int remotePort = mediaVideo.port;
	        
	    	// Set media player event listener
	        player.addListener(new MediaPlayerEventListener(this));

	        // Open the media player
    		player.open(remoteHost, remotePort);
	        
	        // Send ACK request
	        if (logger.isActivated()) {
	        	logger.info("Send ACK");
	        }
	        getImsService().getImsModule().getSipManager().sendSipAck(getDialogPath());
	        
	        // Start the media player
	        player.start();
	        
        	// Start session timer
        	if (getSessionTimerManager().isSessionTimerActivated(resp)) {
        		getSessionTimerManager().start(resp.getSessionTimerRefresher(), resp.getSessionTimerExpire());
        	}

	        // Notify listener
	        if (getListener() != null) {
	        	getListener().handleSessionStarted();
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
	 * Handle 407 Proxy Authentication Required 
	 * 
	 * @param resp 407 response
	 */
	public void handle407Authentication(SipResponse resp) {
		try {
	        if (logger.isActivated()) {
	        	logger.info("407 response received");
	        }
	
	        // Set the remote tag
	        getDialogPath().setRemoteTag(resp.getToTag());
	
	        // Update the authentication agent
	    	getAuthenticationAgent().readProxyAuthenticateHeader(resp);            
	
	        // Increment the Cseq number of the dialog path
	        getDialogPath().incrementCseq();
	
	        // Create a second INVITE request with the right token
	        if (logger.isActivated()) {
	        	logger.info("Send second INVITE");
	        }
	        SipRequest invite = SipMessageFactory.createInvite(
	        		getDialogPath(),
	        		ContentSharingService.FEATURE_TAGS_VIDEO_SHARE,
					getDialogPath().getLocalContent());

	        // Reset initial request in the dialog path
	        getDialogPath().setInvite(invite);
	        
	        // Set the Proxy-Authorization header
	        getAuthenticationAgent().setProxyAuthorizationHeader(invite);
	
	        // Send INVITE request
	        sendInvite(invite);
	        
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
	 * Close media session
	 */
	public void closeMediaSession() {
		try {
			// Close the media player
    		player.stop();
    		player.close();
		} catch(Exception e) {
			if (logger.isActivated()) {
				logger.error("Exception when closing the media player", e);
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
				logger.debug("Media player is opened");
			}
		}
		
		/**
		 * Media player is closed
		 */
		public void mediaClosed() {
			if (logger.isActivated()) {
				logger.debug("Media player is closed");
			}
		}

		/**
		 * Media player is started
		 */
		public void mediaStarted() {
			if (logger.isActivated()) {
				logger.debug("Media player is started");
			}
		}
		
		/**
		 * Media player is stopped
		 */
		public void mediaStopped() {
			if (logger.isActivated()) {
				logger.debug("Media player is stopped");
			}
		}

		/**
		 * Media player has failed
		 * 
		 * @param error Error
		 */
		public void mediaError(String error) {
			if (logger.isActivated()) {
				logger.error("Media has failed: " + error);
			}
			
	        // Close media session
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
