/*******************************************************************************
 * Software Name : RCS IMS Stack
 *
 * Copyright (C) 2010 France Telecom S.A.
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

package com.orangelabs.rcs.core.ims.service.ipcall;

import java.util.Vector;

import android.util.Log;

import com.orangelabs.rcs.core.content.ContentManager;
import com.orangelabs.rcs.core.ims.network.sip.SipMessageFactory;
import com.orangelabs.rcs.core.ims.network.sip.SipUtils;
import com.orangelabs.rcs.core.ims.protocol.sdp.MediaDescription;
import com.orangelabs.rcs.core.ims.protocol.sdp.SdpParser;
import com.orangelabs.rcs.core.ims.protocol.sdp.SdpUtils;
import com.orangelabs.rcs.core.ims.protocol.sip.SipRequest;
import com.orangelabs.rcs.core.ims.protocol.sip.SipResponse;
import com.orangelabs.rcs.core.ims.protocol.sip.SipTransactionContext;
import com.orangelabs.rcs.core.ims.service.ImsService;
import com.orangelabs.rcs.core.ims.service.ImsServiceSession;
import com.orangelabs.rcs.core.ims.service.SessionTimerManager;
import com.orangelabs.rcs.service.api.client.media.audio.AudioCodec;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Terminating live video content sharing session (streaming)
 *
 * @author opob7414
 */
public class TerminatingIPCallStreamingSession extends IPCallStreamingSession {
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
    public TerminatingIPCallStreamingSession(ImsService parent, SipRequest invite) {
        super(parent, ContentManager.createLiveVideoContentFromSdp(invite.getContentBytes()), ContentManager.createLiveAudioContentFromSdp(invite.getContentBytes()), SipUtils.getAssertedIdentity(invite));

        // Create dialog path
        createTerminatingDialogPath(invite);
    }

    /**
     * Background processing
     */
    public void run() {
        try {
        	Log.i("terminating", "initiating a new ip call session");
            if (logger.isActivated()) {
                logger.info("Initiate a new ip call session as terminating");
            }

            // Send a 180 Ringing response
            send180Ringing(getDialogPath().getInvite(), getDialogPath().getLocalTag());

            // Parse the remote SDP part
            SdpParser parser = new SdpParser(getDialogPath().getRemoteContent().getBytes());
            String remoteHost = SdpUtils.extractRemoteHost(parser.sessionDescription.connectionInfo);
            // TODO String remoteHost = SdpUtils.extractRemoteHost(parser.sessionDescription, mediaDesc);
           
            // TODO extract media video for a videocall here
//            MediaDescription mediaVideo = parser.getMediaDescription("video");
//            int remotePort = mediaVideo.port;
           
            MediaDescription mediaAudio = parser.getMediaDescription("audio");
            int remotePort = mediaAudio.port;

            // Extract video codecs from SDP            
//            Vector<MediaDescription> medias = parser.getMediaDescriptions("video");
//            Vector<VideoCodec> proposedCodecs = VideoCodecManager.extractVideoCodecsFromSdp(medias);
            
            // Extract the audio codecs from SDP
            Vector<MediaDescription> medias = parser.getMediaDescriptions("audio");
            Vector<AudioCodec> proposedCodecs = AudioCodecManager.extractAudioCodecsFromSdp(medias);

            // Notify listener            
            getImsService().getImsModule().getCore().getListener().handleIPCallInvitation(this);

            // Wait invitation answer
            int answer = waitInvitationAnswer();
            if (answer == ImsServiceSession.INVITATION_REJECTED) {
                if (logger.isActivated()) {
                    logger.debug("Session has been rejected by user");
                }

                // Remove the current session
                getImsService().removeSession(this);

                // Notify listeners
                for (int i = 0; i < getListeners().size(); i++) {
                    getListeners().get(i).handleSessionAborted(ImsServiceSession.TERMINATION_BY_USER);
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
                
                // Notify listeners
                for (int i = 0; i < getListeners().size(); i++) {
                    getListeners().get(i).handleSessionAborted(ImsServiceSession.TERMINATION_BY_TIMEOUT);
                }
                return;
            } else
            if (answer == ImsServiceSession.INVITATION_CANCELED) {
                if (logger.isActivated()) {
                    logger.debug("Session has been canceled");
                }
                return;
            }

            // Check if an audio renderer has been set            
            if (getAudioRenderer() == null) { 
            	if (logger.isActivated()) {
                    logger.debug("Audio Renderer Not Initialized");
                }
                handleError(new IPCallError(
                		IPCallError.AUDIO_RENDERER_NOT_INITIALIZED));
                return;
            }
            
            // Check if an audio player has been set
            if (getAudioPlayer() == null) { 
            	if (logger.isActivated()) {
                    logger.debug("Audio Player Not Initialized");
                }
                handleError(new IPCallError(
                		IPCallError.AUDIO_PLAYER_NOT_INITIALIZED));
                return;
            }

            // Codec negotiation
            // TODO do it for audio and for video                     
//            VideoCodec selectedVideoCodec = VideoCodecManager.negociateVideoCodec(
//                    getMediaRenderer().getSupportedMediaCodecs(), proposedCodecs);
//            if (selectedVideoCodec == null) {
//                if (logger.isActivated()){
//                    logger.debug("Proposed codecs are not supported");
//                }
//                
//                // Send a 415 Unsupported media type response
//                send415Error(getDialogPath().getInvite());
//                
//                // Unsupported media type
//                handleError(new ContentSharingError(ContentSharingError.UNSUPPORTED_MEDIA_TYPE));
//                return;
//            }
			AudioCodec selectedAudioCodec = AudioCodecManager.negociateAudioCodec(getAudioRenderer().getSupportedAudioCodecs(), proposedCodecs);
			if (selectedAudioCodec == null) {
				if (logger.isActivated()) {
					logger.debug("Proposed codecs are not supported");
				}

				// Send a 415 Unsupported media type response
				send415Error(getDialogPath().getInvite());

				if (logger.isActivated()) {
                    logger.debug("Media Renderer Not Initialized");
                }
                              
				// Unsupported media type
				handleError(new IPCallError(
						IPCallError.UNSUPPORTED_AUDIO_TYPE));
				return;
			}
            
            // Set the OrientationHeaderID
			// TODO do this for video only            
//            SdpOrientationExtension extensionHeader = SdpOrientationExtension.create(mediaVideo);
//            if (extensionHeader != null) {
//                getMediaRenderer().setOrientationHeaderId(extensionHeader.getExtensionId());
//            }

            // Set the audio codec in audio renderer            
            getAudioRenderer().setAudioCodec(selectedAudioCodec.getMediaCodec());
			if (logger.isActivated()) {
				logger.debug("Set audio codec in the audio renderer: " + selectedAudioCodec.getMediaCodec().getCodecName());
			}
			
            // Set the audio codec in audio player            
            getAudioPlayer().setAudioCodec(selectedAudioCodec.getMediaCodec());
			if (logger.isActivated()) {
				logger.debug("Set audio codec in the audio player: " + selectedAudioCodec.getMediaCodec().getCodecName());
			}
			
            // Set audio renderer event listener            
            getAudioRenderer().addListener(new AudioPlayerEventListener(this));
			if (logger.isActivated()) {
				logger.debug("Add listener to audio renderer");
			}
			
            // Set audio player event listener            
            getAudioPlayer().addListener(new AudioPlayerEventListener(this));
			if (logger.isActivated()) {
				logger.debug("Add listener to audio player");
			}

            // Open the audio renderer
            getAudioRenderer().open(remoteHost, remotePort);
			if (logger.isActivated()) {
				logger.debug("Open audio renderer with remoteHost ("+remoteHost+") and remotePort ("+remotePort+")");
			}
			
            // Open the audio player
            getAudioPlayer().open(remoteHost, remotePort);
			if (logger.isActivated()) {
				logger.debug("Open audio player on renderer RTP stream");
			}
			
            // Build SDP part for response
            String ntpTime = SipUtils.constructNTPtime(System.currentTimeMillis());
	    	String ipAddress = getDialogPath().getSipStack().getLocalIpAddress();
	    	
            // String videoSdp = VideoSdpBuilder.buildResponseSdp(selectedVideoCodec.getMediaCodec(), getMediaRenderer().getLocalRtpPort(), mediaVideo);
	    	String audioSdp = AudioSdpBuilder.buildResponseSdp(selectedAudioCodec.getMediaCodec(), getAudioRenderer().getLocalRtpPort()); // mediaAudio is not used here
            String sdp =
            	"v=0" + SipUtils.CRLF +
            	"o=- " + ntpTime + " " + ntpTime + " " + SdpUtils.formatAddressType(ipAddress) + SipUtils.CRLF +
            	"s=-" + SipUtils.CRLF +
            	"c=" + SdpUtils.formatAddressType(ipAddress) + SipUtils.CRLF +
                "t=0 0" + SipUtils.CRLF +
            //    videoSdp +
                audioSdp +
                "a=sendrcv" + SipUtils.CRLF;

            // Set the local SDP part in the dialog path
            getDialogPath().setLocalContent(sdp);
            
            if (logger.isActivated()) {
                logger.debug("AudioContent = "+this.getAudioContent());
                logger.debug("VideoContent = "+this.getVideoContent());
                logger.debug("AudioPlayer = "+this.getAudioPlayer());
                logger.debug("VideoPlayer = "+this.getVideoPlayer());
            }
            
            // Create a 200 OK response
            SipResponse resp = null ;
            if (getAudioPlayer()!= null) {
            	if (getVideoPlayer() != null) {
            		// audio+video IP Call
	            	resp = SipMessageFactory.create200OkInviteResponse(getDialogPath(),IPCallService.FEATURE_TAGS_IP_AUDIOVIDEO_CALL, sdp);
	            }
	            else {
	            	// audio IP Call
	            	resp = SipMessageFactory.create200OkInviteResponse(getDialogPath(),IPCallService.FEATURE_TAGS_IP_VOICE_CALL, sdp);
	            }
            } else {
            	handleError(new IPCallError(IPCallError.UNEXPECTED_EXCEPTION,
                        "Audio player not initialized"));
            }
            
            // The signalisation is established
            getDialogPath().sigEstablished();

            if (logger.isActivated()) {
                logger.info("Send 200 OK");
            }
            // Send response
            SipTransactionContext ctx = getImsService().getImsModule().getSipManager().sendSipMessageAndWait(resp);

            // Analyze the received response
            if (ctx.isSipAck()) {
                // ACK received
                if (logger.isActivated()) {
                    logger.info("ACK request received");
                }

                // The session is established
                getDialogPath().sessionEstablished();
                
                // Start the audio renderer
                getAudioRenderer().start();
                if (logger.isActivated()) {
                	logger.debug("Start audio renderer");
                }
                
                // Start the audio player
                getAudioPlayer().start();
                if (logger.isActivated()) {
                	logger.debug("Start audio player");
                }

                // Start session timer
                if (getSessionTimerManager().isSessionTimerActivated(resp)) {
                    getSessionTimerManager().start(SessionTimerManager.UAS_ROLE, getDialogPath().getSessionExpireTime());
                }
                                
                // Notify listeners
                for(int i=0; i < getListeners().size(); i++) {
                    getListeners().get(i).handleSessionStarted();
                }
            } else {
                if (logger.isActivated()) {
                    logger.debug("No ACK received for INVITE");
                }
                
                // No response received: timeout
                handleError(new IPCallError(IPCallError.SESSION_INITIATION_FAILED));
            }
        } catch(Exception e) {
            if (logger.isActivated()) {
                logger.error("Session initiation has failed", e);
            }
            
            // Unexpected error
            handleError(new IPCallError(IPCallError.UNEXPECTED_EXCEPTION,
                    e.getMessage()));
        }
    }

    /**
     * Handle error
     *
     * @param error Error
     */
    public void handleError(IPCallError error) {
        // Error
        if (logger.isActivated()) {
            logger.info("Session error: " + error.getErrorCode() + ", reason=" + error.getMessage());
        }

        // Close media session
        closeMediaSession();

        // Remove the current session
        getImsService().removeSession(this);
        
        // Notify listener
        if (!isInterrupted()) {
            for(int i=0; i < getListeners().size(); i++) {
                ((IPCallStreamingSessionListener)getListeners().get(i)).handleCallError(error);
            }
        }
    }

    /**
     * Close media session
     */
    public void closeMediaSession() {
        try {
            // Close the media renderer
        	// TODO do this for audio and video
            if (getAudioRenderer() != null) {
                getAudioRenderer().stop();
                if (logger.isActivated()) {
                    logger.info("Stop the audio renderer");
                }
                getAudioRenderer().close();
                if (logger.isActivated()) {
                    logger.info("Close the audio renderer");
                }
            }
            if (getAudioPlayer() != null) {
            	getAudioPlayer().stop();
                if (logger.isActivated()) {
                    logger.info("Stop the audio player");
                }
                getAudioPlayer().close();
                if (logger.isActivated()) {
                    logger.info("Close the audio player");
                }
            }
        } catch(Exception e) {
            if (logger.isActivated()) {
                logger.error("Exception when closing the media renderer or player", e);
            }
        }
    }

    /**
     * Prepare media session
     * 
     * @throws Exception 
     */
    public void prepareMediaSession() throws Exception {
        // Nothing to do in terminating side
    }

    /**
     * Start media session
     * 
     * @throws Exception 
     */
    public void startMediaSession() throws Exception {
        // Nothing to do in terminating side
//    	getAudioRenderer().start();
//        if (logger.isActivated()) {
//            logger.info("Start the audio renderer");
//        }
//    	getAudioPlayer().start();
//        if (logger.isActivated()) {
//            logger.info("Start the audio player");
//        }
    	
    }
}

