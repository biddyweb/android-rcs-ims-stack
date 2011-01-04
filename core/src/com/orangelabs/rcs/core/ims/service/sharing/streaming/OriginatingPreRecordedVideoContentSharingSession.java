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
package com.orangelabs.rcs.core.ims.service.sharing.streaming;

import java.util.Vector;

import com.orangelabs.rcs.core.content.VideoContent;
import com.orangelabs.rcs.core.ims.ImsModule;
import com.orangelabs.rcs.core.ims.network.sip.SipManager;
import com.orangelabs.rcs.core.ims.network.sip.SipMessageFactory;
import com.orangelabs.rcs.core.ims.network.sip.SipUtils;
import com.orangelabs.rcs.core.ims.protocol.rtp.MediaRegistry;
import com.orangelabs.rcs.core.ims.protocol.rtp.MediaRtpSender;
import com.orangelabs.rcs.core.ims.protocol.rtp.core.RtpPacketReceiver;
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
import com.orangelabs.rcs.core.media.MediaListener;
import com.orangelabs.rcs.core.media.MediaPlayer;
import com.orangelabs.rcs.utils.Config;
import com.orangelabs.rcs.utils.NetworkRessourceManager;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Originating pre-recorded video content sharing session (streaming)
 * 
 * @author jexa7410
 */
public class OriginatingPreRecordedVideoContentSharingSession extends ContentSharingStreamingSession implements MediaListener {
	/**
	 * Media player
	 */
	private MediaPlayer player = null;

	/**
	 * RTP sender session
	 */
	private MediaRtpSender rtpSender = null;
	
	/**
	 * RTP session receiver (used for symetric RTP feature)
	 */
	private RtpPacketReceiver rtpReceiver = null;

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
	public OriginatingPreRecordedVideoContentSharingSession(ImsService parent, MediaPlayer player, VideoContent content, String contact) {
		super(parent, content, contact);

		// Create dialog path
		createOriginatingDialogPath();

		// Create the video format
    	String codec = getImsService().getConfig().getString("VideoCodec", "h263-2000");
		videoFormat = (VideoFormat)MediaRegistry.generateFormat(codec);

		// Set media player 
		this.player = player;
        player.addListener(this);
	}

	/**
	 * Background processing
	 */
	public void run() {
		try {
	    	if (logger.isActivated()) {
	    		logger.info("Initiate a new pre-recorded video sharing session as originating");
	    	}

	    	// Create the RTP manager
			int localRtpPort = NetworkRessourceManager.generateLocalUdpPort();
            rtpReceiver = new RtpPacketReceiver(localRtpPort);
            rtpSender = new MediaRtpSender(videoFormat);
	    	
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
	            "o=" + ImsModule.IMS_USER_PROFILE.getUsername() + " "
						+ ntpTime + " " + ntpTime + " IN IP4 "
						+ getDialogPath().getSipStack().getLocalIpAddress() + SipUtils.CRLF +
	            "s=-" + SipUtils.CRLF +
				"c=IN IP4 " + getDialogPath().getSipStack().getLocalIpAddress() + SipUtils.CRLF +
	            "t=0 0" + SipUtils.CRLF +
	            "m=video " + localRtpPort + " RTP/AVP " + payload + SipUtils.CRLF + 
	            "a=rtpmap:" + payload + " " + videoFormat.getCodec() + "/" + clockRate + SipUtils.CRLF +
	            "a=framesize:" + payload + " " + videoWidth + "-" + videoHeight + SipUtils.CRLF +
	            "a=framerate:" + frameRate + SipUtils.CRLF +
	            "a=fmtp:" + payload + " " + codecParameters + SipUtils.CRLF + 	            
	    		"a=sendonly" + SipUtils.CRLF;
	    	
	    	// Set X-Type attribute
	    	String xType = getXTypeAttribute();
	    	if (xType != null) {
	    		sdp += "a=X-type:" + xType + SipUtils.CRLF;
	    	}

			// Set the local SDP part in the dialog path
	        getDialogPath().setLocalSdp(sdp);

	        // Send an INVITE
	        if (logger.isActivated()) {
	        	logger.info("Send INVITE");
	        }
	        SipRequest invite = SipMessageFactory.createInvite(getDialogPath(), -1, sdp);
	        
	        // Set feature tags
	        String[] tags = {SipUtils.FEATURE_3GPP_CS_VOICE};
	        SipUtils.setFeatureTags(invite, tags);

	        // Set initial request in the dialog path
	        getDialogPath().setInvite(invite);
	        
	        // Send the INVITE request
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
            	// Send ACK
            	sendAck(getDialogPath());

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
        					ctx.getReasonPhrase()));
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
	        String to = resp.getTo();
	        getDialogPath().setRemoteTag(SipUtils.extractTag(to));

	        // Set the target
	        getDialogPath().setTarget(resp.getContactURI());
	
	        // Set the route path with the Record-Route header
	        Vector<String> newRoute = SipUtils.routeProcessing(resp, true);
			getDialogPath().setRoute(newRoute);
	
	        // Set the remote SDP part
	        getDialogPath().setRemoteSdp(resp.getContent());
	                      		
	        // The session is established
	        getDialogPath().sessionEstablished();

	        // Parse the remote SDP part
        	SdpParser parser = new SdpParser(getDialogPath().getRemoteSdp().getBytes());
            String remoteHost = SdpUtils.extractRemoteHost(parser.sessionDescription.connectionInfo);    		
            MediaDescription video = parser.getMediaDescription("video");
            int remotePort = video.port;
	        
            // Prepare the RTP session
    		rtpSender.prepareSession(player, remoteHost, remotePort);
	        
			// Send ACK message
	        if (logger.isActivated()) {
	        	logger.info("Send ACK");
	        }
	        SipRequest ack = SipMessageFactory.createAck(getDialogPath());
	        getImsService().getImsModule().getSipManager().sendSipMessage(ack);
	        
			// Start the RTP session
	        rtpSender.startSession();
	        
			// Start the media player
			player.start();

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
	        String to = resp.getTo();
	        getDialogPath().setRemoteTag(SipUtils.extractTag(to));
	
	    	// Send ACK message
	        if (logger.isActivated()) {
	        	logger.info("Send ACK for 407 response");
	        }
	        SipRequest ack = SipMessageFactory.createAck(getDialogPath());
	        getImsService().getImsModule().getSipManager().sendSipMessage(ack);
	
	        // Update the authentication agent
	    	getAuthenticationAgent().readProxyAuthenticateHeader(resp);            
	
	        // Increment the Cseq number of the dialog path
	        getDialogPath().incrementCseq();
	
	        // Send a second INVITE with the right token
	        if (logger.isActivated()) {
	        	logger.info("Send second INVITE");
	        }
	        SipRequest invite = SipMessageFactory.createInvite(
	        		getDialogPath(),
					-1,
					getDialogPath().getLocalSdp());
	               
	        // Set feature tags
	        String[] tags = {SipUtils.FEATURE_3GPP_CS_VOICE};
	        SipUtils.setFeatureTags(invite, tags);

	        // Reset initial request in the dialog path
	        getDialogPath().setInvite(invite);
	        
	        // Set the Proxy-Authorization header
	        getAuthenticationAgent().setProxyAuthorizationHeader(invite);
	
	        // Send the second INVITE request
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

        // Close RTP session
    	closeRtpSession();
    	
		// Remove the current session
    	getImsService().removeSession(this);

		// Notify listener
    	if ((!isInterrupted()) && (getListener() != null)) {
        	getListener().handleSharingError(error);
        }
	}

	/**
	 * Receive BYE request 
	 * 
	 * @param bye BYE request
	 */
	public void receiveBye(SipRequest bye) {
    	if (logger.isActivated()) {
    		logger.info("Receive a BYE message from the remote");
    	}

    	// Close RTP session
    	closeRtpSession();

    	// Close SIP session
		try {
	        // Update the dialog path status
			getDialogPath().sessionTerminated();
	
	        // Send a 200 OK response
			if (logger.isActivated()) {
				logger.info("Send 200 OK");
			}
	        SipResponse resp = SipMessageFactory.createResponse(bye, 200);
	        getImsService().getImsModule().getSipManager().sendSipMessage(resp);
		} catch(Exception e) {
	       	if (logger.isActivated()) {
        		logger.error("Session termination has failed", e);
        	}
		}

    	// Remove the current session
    	getImsService().removeSession(this);

        // Notify listener that transfer is terminated because sharer finished it
        if (getListener() != null) {
        	getListener().handleSessionTerminatedByRemote();
        }
	}
	
	/**
	 * Receive CANCEL request 
	 * 
	 * @param cancel CANCEL request
	 */
	public void receiveCancel(SipRequest cancel) {
    	if (logger.isActivated()) {
    		logger.info("Receive a CANCEL message from the remote");
    	}
    	
		if (getDialogPath().isSigEstablished()) {
	    	if (logger.isActivated()) {
	    		logger.info("Ignore the received CANCEL message from the remote (session already established)");
	    	}
			return;
		}

		// Close RTP session
    	closeRtpSession();

    	// Close SIP session
    	try {
	    	// Update dialog path
			getDialogPath().sessionCancelled();

			// Send a 200 OK
	    	if (logger.isActivated()) {
	    		logger.info("Send 200 OK");
	    	}
	        SipResponse cancelResp = SipMessageFactory.createResponse(cancel, 200);
	        getImsService().getImsModule().getSipManager().sendSipMessage(cancelResp);
		} catch(Exception e) {
	    	if (logger.isActivated()) {
	    		logger.error("Session has been cancelled", e);
	    	}
		}
   	
    	// Remove the current session
    	getImsService().removeSession(this);

		// Notify listener
        if (getListener() != null) {
        	getListener().handleSessionTerminatedByRemote();
        }
	}
	
	/**
	 * Abort the session
	 */
	public void abortSession(){
    	if (logger.isActivated()) {
    		logger.info("Abort the session");
    	}

        // Interrupt the current session
        interruptSession();

        // Close RTP session
    	closeRtpSession();
        
		// Terminate session
		terminateSession();

    	// Remove the current session
    	getImsService().removeSession(this);
		   	
    	// Notify listener
        if (getListener() != null) {
        	getListener().handleSessionAborted();
        }
	}
	
	/**
	 * Media player is started
	 */
	public void mediaStarted() {
		if (logger.isActivated()) {
			logger.debug("Media player is started");
		}

    	// Update sharing provider
    	// TODO
	}
	
	/**
	 * Media player is stopped
	 */
	public void mediaStopped() {
		if (logger.isActivated()) {
			logger.debug("Media player is stopped");
		}

        // Close RTP session
		closeRtpSession();
			
		// Terminate session
		terminateSession();

    	// Remove the current session
    	getImsService().removeSession(this);
		
	   	// Notify listener
        if (getListener() != null) {
        	getListener().handleSessionTerminated();
        }
	}

	/**
	 * Media player has failed
	 * 
	 * @param error Error
	 */
	public void mediaError(String error) {
		if (logger.isActivated()) {
			logger.error("Media player has failed: " + error);
		}
		
        // Close RTP session
    	closeRtpSession();
    	
    	// Terminate session
    	terminateSession();
    	
		// Remove the current session
    	getImsService().removeSession(this);

		// Notify listener
    	if ((!isInterrupted()) && (getListener() != null)) {
        	getListener().handleSharingError(new ContentSharingError(ContentSharingError.MEDIA_STREAMING_FAILED, error));
        }
	}

	/**
	 * Close the RTP session
	 */
	private void closeRtpSession() {
    	if (player != null) {
    		player.stop();
    	}
    	
    	if (rtpSender != null) {
    		rtpSender.stopSession();
    	}

    	if (rtpReceiver != null) {
    		try {
    			rtpReceiver.close();
    		} catch(Exception e) {}
    	}
	}
}
