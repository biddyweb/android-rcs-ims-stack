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

import android.os.RemoteException;

import com.orangelabs.rcs.core.content.LiveAudioContent;
import com.orangelabs.rcs.core.content.LiveVideoContent;
import com.orangelabs.rcs.core.ims.network.sip.SipMessageFactory;
import com.orangelabs.rcs.core.ims.network.sip.SipUtils;
import com.orangelabs.rcs.core.ims.protocol.sdp.MediaDescription;
import com.orangelabs.rcs.core.ims.protocol.sdp.SdpParser;
import com.orangelabs.rcs.core.ims.protocol.sdp.SdpUtils;
import com.orangelabs.rcs.core.ims.protocol.sip.SipRequest;
import com.orangelabs.rcs.core.ims.service.ImsService;
import com.orangelabs.rcs.core.ims.service.ImsServiceSession;
import com.orangelabs.rcs.service.api.client.media.IAudioPlayer;
import com.orangelabs.rcs.service.api.client.media.IMediaPlayer;
import com.orangelabs.rcs.service.api.client.media.audio.AudioCodec;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Originating IP call session
 *
 * @author opob7414
 */
public class OriginatingIPCallStreamingSession extends IPCallStreamingSession {
	
    /**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());
    
    /**
     * Constructor
     *
     * @param parent IMS service
     * @param videoPlayer Media player
     * @param audioPlayer Audio player
     * @param videoContent VideoContent to be shared
     * @param audioContent AudioContent to be shared
     * @param contact Remote contact
     */
    public OriginatingIPCallStreamingSession(ImsService parent, IMediaPlayer videoPlayer, IAudioPlayer audioPlayer,
            LiveVideoContent videoContent, LiveAudioContent audioContent, String contact) {
    	super(parent, videoContent, audioContent, contact);
    	
        // Create dialog path
        createOriginatingDialogPath();
        
        // Set the video player
        setVideoPlayer(videoPlayer);
        
        // Set the audio player
        setAudioPlayer(audioPlayer);
        
    }
    
    /**
     * Background processing
     */
    public void run() {
        try {
            if (logger.isActivated()) {
                logger.info("Initiate a new ip call session as originating");
            }

            // Check player 
            if ((getAudioPlayer() == null) || (getAudioPlayer().getAudioCodec() == null)) {
                handleError(new IPCallError(IPCallError.UNSUPPORTED_AUDIO_TYPE,
                        "Audio codec not selected"));
                return;
            }
//            if ((getVideoPlayer() == null) || (getVideoPlayer().getMediaCodec() == null)) {
//                handleError(new ContentSharingError(IPCallError.UNSUPPORTED_VIDEO_TYPE,
//                        "Video codec not selected"));
//                return;
//            }

            // Build SDP part
            String ntpTime = SipUtils.constructNTPtime(System.currentTimeMillis());
	    	String ipAddress = getDialogPath().getSipStack().getLocalIpAddress();
            //String videoSdp = VideoSdpBuilder.buildSdpWithOrientationExtension(getVideoPlayer().getSupportedMediaCodecs(), getVideoPlayer().getLocalRtpPort());
	    	String audioSdp = AudioSdpBuilder.buildSdp(getAudioPlayer().getSupportedAudioCodecs(), getAudioPlayer().getLocalRtpPort());
	    	String sdp =
            	"v=0" + SipUtils.CRLF +
            	"o=- " + ntpTime + " " + ntpTime + " " + SdpUtils.formatAddressType(ipAddress) + SipUtils.CRLF +
            	"s=-" + SipUtils.CRLF +
            	"c=" + SdpUtils.formatAddressType(ipAddress) + SipUtils.CRLF +
            	"t=0 0" + SipUtils.CRLF +
            	audioSdp +
            	//videoSdp +
            	"a=sendrcv" + SipUtils.CRLF;

            // Set the local SDP part in the dialog path
            getDialogPath().setLocalContent(sdp); 

            // Create an INVITE request
            if (logger.isActivated()) {
                logger.info("Send INVITE");
            }
            SipRequest invite;  
            if (getVideoPlayer()== null){
            	// Audio Call
            	invite = SipMessageFactory.createInvite(getDialogPath(),IPCallService.FEATURE_TAGS_IP_VOICE_CALL, sdp); }
            else {
            	// Audio + Video call
            	invite = SipMessageFactory.createInvite(getDialogPath(),IPCallService.FEATURE_TAGS_IP_VIDEO_CALL, sdp); } 

	        // Set the Authorization header
	        getAuthenticationAgent().setAuthorizationHeader(invite);

	        // Set initial request in the dialog path
            getDialogPath().setInvite(invite);

            // Send INVITE request
            sendInvite(invite);
            
        } catch (Exception e) {
            if (logger.isActivated()) {
                logger.error("Session initiation has failed", e);
            }
            
            // Unexpected error
            handleError(new IPCallError(IPCallError.UNEXPECTED_EXCEPTION,
                    e.getMessage()));
        }
    }

	@Override
	public void prepareMediaSession() throws Exception {
		
        // Parse the remote SDP part
        SdpParser parser = new SdpParser(getDialogPath().getRemoteContent().getBytes());
        String remoteHost = SdpUtils.extractRemoteHost(parser.sessionDescription.connectionInfo);
        MediaDescription mediaAudio = parser.getMediaDescription("audio");
        int remotePort = mediaAudio.port;
        
        // Extract audio codecs from SDP
        Vector<MediaDescription> medias = parser.getMediaDescriptions("audio");
        Vector<AudioCodec> proposedCodecs = AudioCodecManager.extractAudioCodecsFromSdp(medias);

        // Codec negotiation
        AudioCodec selectedAudioCodec = AudioCodecManager.negociateAudioCodec(getAudioPlayer().getSupportedAudioCodecs(), proposedCodecs);
        if (selectedAudioCodec == null) {
            if (logger.isActivated()) {
                logger.debug("Proposed codecs are not supported");
            }
            
            // Terminate session
            terminateSession(ImsServiceSession.TERMINATION_BY_SYSTEM);
            
            // Report error
            handleError(new IPCallError(IPCallError.UNSUPPORTED_AUDIO_TYPE));
            return;
        }
        //getContent().setEncoding("audio/" + selectedAudioCodec.getCodecName()); => to remove is not used

        // Set the selected audio codec
        getAudioPlayer().setAudioCodec(selectedAudioCodec.getMediaCodec());
        
        // Set audio player event listener
        getAudioPlayer().addListener(new AudioPlayerEventListener(this));
        
        // Open the audio player
		getAudioPlayer().open(remoteHost, remotePort);
		
	}

	@Override
	public void startMediaSession() throws Exception {
		getAudioPlayer().start();		
	}

	@Override
	public void closeMediaSession() {
		try {
			if (logger.isActivated()) {
				logger.debug("Stop audio player");
			}
			getAudioPlayer().stop();
			
			if (logger.isActivated()) {
				logger.debug("Close audio player");
			}
			getAudioPlayer().close();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
