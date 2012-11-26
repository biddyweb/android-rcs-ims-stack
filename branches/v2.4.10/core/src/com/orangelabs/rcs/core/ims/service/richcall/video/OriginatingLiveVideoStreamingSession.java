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

package com.orangelabs.rcs.core.ims.service.richcall.video;

import java.util.Vector;

import com.orangelabs.rcs.core.content.LiveVideoContent;
import com.orangelabs.rcs.core.ims.network.sip.SipMessageFactory;
import com.orangelabs.rcs.core.ims.network.sip.SipUtils;
import com.orangelabs.rcs.core.ims.protocol.sdp.MediaDescription;
import com.orangelabs.rcs.core.ims.protocol.sdp.SdpParser;
import com.orangelabs.rcs.core.ims.protocol.sdp.SdpUtils;
import com.orangelabs.rcs.core.ims.protocol.sip.SipRequest;
import com.orangelabs.rcs.core.ims.service.ImsService;
import com.orangelabs.rcs.core.ims.service.richcall.ContentSharingError;
import com.orangelabs.rcs.core.ims.service.richcall.RichcallService;
import com.orangelabs.rcs.service.api.client.media.IMediaEventListener;
import com.orangelabs.rcs.service.api.client.media.IMediaPlayer;
import com.orangelabs.rcs.service.api.client.media.video.VideoCodec;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Originating live video content sharing session (streaming)
 *
 * @author jexa7410
 */
public class OriginatingLiveVideoStreamingSession extends VideoStreamingSession {
    /**
     * Media player
     */
    private IMediaPlayer player = null;

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
    public OriginatingLiveVideoStreamingSession(ImsService parent, IMediaPlayer player,
            LiveVideoContent content, String contact) {
        super(parent, content, contact);

        // Create dialog path
        createOriginatingDialogPath();

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

            // Check player 
            if ((player == null) || (player.getMediaCodec() == null)) {
                handleError(new ContentSharingError(ContentSharingError.UNSUPPORTED_MEDIA_TYPE,
                        "Video codec not selected"));
                return;
            }

            // Build SDP part
            String ntpTime = SipUtils.constructNTPtime(System.currentTimeMillis());
	    	String ipAddress = getDialogPath().getSipStack().getLocalIpAddress();
            String videoSdp = VideoCodecManager.createCodecSdpPart(player.getSupportedMediaCodecs(), player.getLocalRtpPort());
	    	String sdp =
            	"v=0" + SipUtils.CRLF +
            	"o=- " + ntpTime + " " + ntpTime + " " + SdpUtils.formatAddressType(ipAddress) + SipUtils.CRLF +
            	"s=-" + SipUtils.CRLF +
            	"c=" + SdpUtils.formatAddressType(ipAddress) + SipUtils.CRLF +
            	"t=0 0" + SipUtils.CRLF +
            	videoSdp +
            	"a=sendonly" + SipUtils.CRLF;

            // Set the local SDP part in the dialog path
            getDialogPath().setLocalContent(sdp);

            // Create an INVITE request
            if (logger.isActivated()) {
                logger.info("Send INVITE");
            }
            SipRequest invite = SipMessageFactory.createInvite(getDialogPath(),
                    RichcallService.FEATURE_TAGS_VIDEO_SHARE, sdp);

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
            handleError(new ContentSharingError(ContentSharingError.UNEXPECTED_EXCEPTION,
                    e.getMessage()));
        }
    }

    /**
     * Prepare media session
     * 
     * @throws Exception 
     */
    public void prepareMediaSession() throws Exception {
        // Parse the remote SDP part
        SdpParser parser = new SdpParser(getDialogPath().getRemoteContent().getBytes());
        String remoteHost = SdpUtils.extractRemoteHost(parser.sessionDescription.connectionInfo);
        MediaDescription mediaVideo = parser.getMediaDescription("video");
        int remotePort = mediaVideo.port;

        // Extract video codecs from SDP
        Vector<MediaDescription> medias = parser.getMediaDescriptions("video");
        Vector<VideoCodec> proposedCodecs = VideoCodecManager.extractVideoCodecsFromSdp(medias);

        // Codec negotiation
        VideoCodec selectedVideoCodec = VideoCodecManager.negociateVideoCodec(
                player.getSupportedMediaCodecs(), proposedCodecs);
        if (selectedVideoCodec == null) {
            if (logger.isActivated()) {
                logger.debug("Proposed codecs are not supported");
            }
            
            // Terminate session
            terminateSession();
            handleError(new ContentSharingError(ContentSharingError.UNSUPPORTED_MEDIA_TYPE));
            return;
        }
        getContent().setEncoding("video/" + selectedVideoCodec.getCodecName());

        // Set the selected media codec
        player.setMediaCodec(selectedVideoCodec.getMediaCodec());

        // Set media player event listener
        player.addListener(new MediaPlayerEventListener(this));

        // Open the media player
        player.open(remoteHost, remotePort);
    }

    /**
     * Start media session
     * 
     * @throws Exception 
     */
    public void startMediaSession() throws Exception {
        // Start the media player
        player.start();
    }

    /**
     * Close media session
     */
    public void closeMediaSession() {
        try {
            // Close the media player
            player.stop();
            player.close();
        } catch (Exception e) {
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
        private VideoStreamingSession session;

        /**
         * Constructor
         *
         * @param session Streaming session
         */
        public MediaPlayerEventListener(VideoStreamingSession session) {
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

            // Notify listeners
            if (!isInterrupted()) {
                for (int i = 0; i < getListeners().size(); i++) {
                    ((VideoStreamingSessionListener) getListeners().get(i))
                            .handleSharingError(new ContentSharingError(
                                    ContentSharingError.MEDIA_STREAMING_FAILED, error));
                }
            }
        }
    }
}
