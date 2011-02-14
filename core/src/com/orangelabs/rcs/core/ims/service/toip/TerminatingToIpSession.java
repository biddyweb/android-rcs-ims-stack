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
package com.orangelabs.rcs.core.ims.service.toip;

import java.util.Vector;

import com.orangelabs.rcs.core.ims.network.sip.SipManager;
import com.orangelabs.rcs.core.ims.network.sip.SipMessageFactory;
import com.orangelabs.rcs.core.ims.network.sip.SipUtils;
import com.orangelabs.rcs.core.ims.protocol.rtp.MediaRegistry;
import com.orangelabs.rcs.core.ims.protocol.rtp.MediaRtpReceiver;
import com.orangelabs.rcs.core.ims.protocol.rtp.MediaRtpSender;
import com.orangelabs.rcs.core.ims.protocol.rtp.format.text.RedFormat;
import com.orangelabs.rcs.core.ims.protocol.rtp.format.text.T140Format;
import com.orangelabs.rcs.core.ims.protocol.sdp.MediaAttribute;
import com.orangelabs.rcs.core.ims.protocol.sdp.MediaDescription;
import com.orangelabs.rcs.core.ims.protocol.sdp.SdpParser;
import com.orangelabs.rcs.core.ims.protocol.sdp.SdpUtils;
import com.orangelabs.rcs.core.ims.protocol.sip.SipRequest;
import com.orangelabs.rcs.core.ims.protocol.sip.SipResponse;
import com.orangelabs.rcs.core.ims.protocol.sip.SipTransactionContext;
import com.orangelabs.rcs.core.ims.service.ImsService;
import com.orangelabs.rcs.core.ims.service.ImsServiceSession;
import com.orangelabs.rcs.core.media.MediaListener;
import com.orangelabs.rcs.utils.NetworkRessourceManager;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Terminating ToIP session
 * 
 * @author jexa7410
 */
public class TerminatingToIpSession extends ToIpSession implements MediaListener {
	/**
	 * Text format
	 */
	private T140Format textFormat = new T140Format();

	/**
	 * Redundant text format
	 */
	private RedFormat  redFormat  = new RedFormat();
	
	/**
	 * Clock rate
	 */
	private double clockRate;
	
	/**
	 * Local RTP port
	 */
	private int localRtpPort;
	
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
	public TerminatingToIpSession(ImsService parent, SipRequest invite) {
		super(parent, SipUtils.getAssertedIdentity(invite));

		// Get clock rate
		clockRate = parent.getConfig().getDouble("TextClockRate", 1000);

		// Create dialog path
		createTerminatingDialogPath(invite);
	}
		
	/**
	 * Background processing
	 */
	public void run() {
		try {		
	    	if (logger.isActivated()) {
	    		logger.info("Initiate a new ToIP session as terminating");
	    	}
	
	    	// Send a 180 Ringing
			send180Ringing(getDialogPath().getInvite(), getDialogPath().getLocalTag());
        
			// Wait invitation answer
			if (waitInvitationAnswer() != ImsServiceSession.INVITATION_ACCEPTED) {
				if (logger.isActivated()) {
					logger.debug("Session has been rejected or not answered");
				}
				
		    	// Remove the current session
		    	getImsService().removeSession(this);

		    	// Notify listener
		        if (getListener() != null) {
		        	getListener().handleSessionAborted();
		        }
				return;
			}

			// Get local RTP port
			localRtpPort = NetworkRessourceManager.generateLocalRtpPort();
			
	        // Parse the remote SDP part
	        SdpParser parser = new SdpParser(getDialogPath().getRemoteSdp().getBytes());
            String remoteHost = SdpUtils.extractRemoteHost(parser.sessionDescription.connectionInfo);    		
    		MediaDescription desc = parser.getMediaDescription("text");
    		
    		// Extract payloads
    		String[] payloads = desc.payload.split(" ");
    		    		
    		// Extract proposed codecs
    		Vector<String> codecs = new Vector<String>();
    		String unsupportedCodec = new String("");
    		int payloadIndex = 0;
    		for (int i = 0; i< desc.mediaAttributes.size(); i++){
    			MediaAttribute att = desc.mediaAttributes.elementAt(i);
    			if (att.getName().equalsIgnoreCase("rtpmap")){
    				String rtpmap = att.getValue();
    				// Extract the text encoding
    	            String encoding = rtpmap.substring(rtpmap.indexOf(payloads[payloadIndex])+payloads[payloadIndex].length()+1);
    	            String codec = encoding.toLowerCase().trim();
    	            int index = encoding.indexOf("/");
    				if (index != -1) {
    					codec = encoding.substring(0, index);
    				}
    				if (MediaRegistry.isCodecSupported(codec)){
    					codecs.add(codec);
    				} else {
    					unsupportedCodec = unsupportedCodec + codec + " ";
    				}
    			}
    		}
    		
    		// No codec proposed is supported
    		if (codecs.size()==0){
    			// Send a 415 Unsupported media type response
				send415Error(getDialogPath().getInvite());

				// Unsupported media type				
				handleError(new ToIpError(ToIpError.UNSUPPORTED_MEDIA_TYPE, unsupportedCodec));
        		return;
    		}
    		
			// Check that a media player has been set
			if (getMediaPlayer() == null) {
		    	handleError(new ToIpError(ToIpError.MEDIA_PLAYER_NOT_INITIALIZED));
				return;
			}    		
            getMediaPlayer().addListener(this);

			// Check that a media renderer has been set
			if (getMediaRenderer() == null) {
		    	handleError(new ToIpError(ToIpError.MEDIA_RENDERER_NOT_INITIALIZED));
				return;
			}    		
            getMediaRenderer().addListener(this);
    		           	        	
			// Create the RTP sessions
			setRtpReceiver(new MediaRtpReceiver(localRtpPort));
			
	    	// Create the text format with the first proposed payload type
    		int payload = 0;
    		String sdpMedia  = new String("");
        	if (codecs.elementAt(0).equals(RedFormat.ENCODING)){
        		if (logger.isActivated()) {
					logger.debug("Codec choosed: "+redFormat.getCodec());
				}
        		payload = redFormat.getPayload();
        		setRtpSender(new MediaRtpSender(redFormat));
        		getRtpReceiver().prepareSession(getMediaRenderer(), redFormat);
        		sdpMedia = sdpMedia + 
	        		"m=text " + localRtpPort + " RTP/AVP " + payload + SipUtils.CRLF +
	        		"a=rtpmap:" + payload + " " + redFormat.getCodec()  + "/" + clockRate + SipUtils.CRLF +
	        		"a=fmtp:" + payload + " " + textFormat.getPayload()+ "/" + textFormat.getPayload()+ "/" + textFormat.getPayload() + SipUtils.CRLF +
					"a=sendrecv" + SipUtils.CRLF;
        	} else {
        		if (logger.isActivated()) {
					logger.debug("Codec choosed: "+textFormat.getCodec());
				}
        		payload = textFormat.getPayload();
        		setRtpSender(new MediaRtpSender(textFormat));
        		getRtpReceiver().prepareSession(getMediaRenderer(), textFormat);
        		sdpMedia = sdpMedia + 
	        		"m=text " + localRtpPort + " RTP/AVP " + payload + SipUtils.CRLF +
	        		"a=rtpmap:" + payload + " " + textFormat.getCodec() + "/" + clockRate + SipUtils.CRLF +
	        		"a=sendrecv" + SipUtils.CRLF;
        	}			

			// Prepare the RTP sessions			
			getRtpSender().prepareSession(getMediaPlayer(), remoteHost, desc.port);

			// Build SDP part
			String ntpTime = SipUtils.constructNTPtime(System.currentTimeMillis());
			String sdp =
	    		"v=0" + SipUtils.CRLF +
	            "o=- " + ntpTime + " " + ntpTime + " IN IP4 " + getDialogPath().getSipStack().getLocalIpAddress() + SipUtils.CRLF +
	            "s=-" + SipUtils.CRLF +
				"c=IN IP4 " + getDialogPath().getSipStack().getLocalIpAddress() + SipUtils.CRLF +
	            "t=0 0" + SipUtils.CRLF + 
	            sdpMedia;
	            
			
			// Set the local SDP part in the dialog path
			getDialogPath().setLocalSdp(sdp);

			// Send a 200 OK response
			if (logger.isActivated()) {
				logger.info("Send 200 OK");
			}
			SipResponse resp = SipMessageFactory.create200OkInviteResponse(getDialogPath(), sdp);

	        // Send the response
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

				// Start the media player
				getMediaPlayer().start();

				// Start the media renderer
				getMediaRenderer().start();

				// Start the RTP sessions
				getRtpReceiver().startSession();
				getRtpSender().startSession();

				// Notify listener
		        if (getListener() != null) {
		        	getListener().handleSessionStarted();
		        }
			} else {
	    		if (logger.isActivated()) {
	        		logger.debug("No ACK received for INVITE");
	        	}

	    		// No response received: timeout
            	handleError(new ToIpError(ToIpError.SESSION_INITIATION_FAILED));
			}
		} catch(Exception e) {
        	if (logger.isActivated()) {
        		logger.error("Session initiation has failed", e);
        	}

        	// Unexpected error
			handleError(new ToIpError(ToIpError.UNEXPECTED_EXCEPTION, e.getMessage()));
		}		
	}

	/**
	 * Handle error 
	 * 
	 * @param error Error
	 */
	public void handleError(ToIpError error) {
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
        	getListener().handleToIpError(error);
        }
	}
	
	/**
	 * Player is started
	 */
	public void mediaStarted() {
		if (logger.isActivated()) {
			logger.debug("Media renderer is started");
		}
	}
	
	/**
	 * Player is stopped
	 */
	public void mediaStopped() {
		if (logger.isActivated()) {
			logger.debug("Media renderer is stopped");
		}
	}

	/**
	 * Player has failed
	 * 
	 * @param error Error
	 */
	public void mediaError(String error) {
		if (logger.isActivated()) {
			logger.error("Media renderer has failed: " + error);
		}
		
		// Close the RTP session
		closeRtpSession();
			
		// Terminate session
		terminateSession();

    	// Remove the current session
    	getImsService().removeSession(this);

    	// Notify listener
    	if ((!isInterrupted()) && (getListener() != null)) {
        	getListener().handleToIpError(new ToIpError(ToIpError.MEDIA_FAILED, error));
        }
	}

	/**
	 * Close the RTP session
	 */
	private void closeRtpSession() {
		if (getMediaPlayer() != null) {
			getMediaPlayer().stop();
		}
		if (getMediaRenderer() != null) {
			getMediaRenderer().stop();
		}
		
		if (getRtpSender() != null) {
			getRtpSender().stopSession();
		}
		if (getRtpReceiver() != null){
			getRtpReceiver().stopSession();
		}
	}

	/**
	 * Close media session
	 */
	public void closeMediaSession() {
        // Close RTP session
        closeRtpSession();
	}
}

