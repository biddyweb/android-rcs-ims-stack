package com.orangelabs.rcs.core.ims.service.ipcall;

import java.util.Vector;

import android.os.RemoteException;

import com.orangelabs.rcs.core.content.LiveAudioContent;
import com.orangelabs.rcs.core.content.LiveVideoContent;
import com.orangelabs.rcs.core.content.VideoContent;
import com.orangelabs.rcs.core.ims.network.sip.SipMessageFactory;
import com.orangelabs.rcs.core.ims.network.sip.SipUtils;
import com.orangelabs.rcs.core.ims.protocol.sdp.MediaDescription;
import com.orangelabs.rcs.core.ims.protocol.sdp.SdpParser;
import com.orangelabs.rcs.core.ims.protocol.sdp.SdpUtils;
import com.orangelabs.rcs.core.ims.protocol.sip.SipException;
import com.orangelabs.rcs.core.ims.protocol.sip.SipRequest;
import com.orangelabs.rcs.core.ims.protocol.sip.SipResponse;
import com.orangelabs.rcs.core.ims.service.ImsService;
import com.orangelabs.rcs.core.ims.service.ImsServiceError;
import com.orangelabs.rcs.core.ims.service.ImsServiceSession;
import com.orangelabs.rcs.core.ims.service.ImsSessionBasedServiceError;
import com.orangelabs.rcs.core.ims.service.richcall.video.SdpOrientationExtension;
import com.orangelabs.rcs.core.ims.service.richcall.video.VideoCodecManager;
import com.orangelabs.rcs.core.ims.service.richcall.video.VideoSdpBuilder;
import com.orangelabs.rcs.service.api.client.media.IAudioEventListener;
import com.orangelabs.rcs.service.api.client.media.IAudioPlayer;
import com.orangelabs.rcs.service.api.client.media.IAudioRenderer;
import com.orangelabs.rcs.service.api.client.media.IVideoEventListener;
import com.orangelabs.rcs.service.api.client.media.IVideoPlayer;
import com.orangelabs.rcs.service.api.client.media.IVideoRenderer;
import com.orangelabs.rcs.service.api.client.media.audio.AudioCodec;
import com.orangelabs.rcs.service.api.client.media.video.VideoCodec;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * IP call session
 * 
 * @author opob7414
 */
public abstract class IPCallStreamingSession extends ImsServiceSession {

	/**
	 * Constant values for session update request type
	 */
	public final static int ADD_VIDEO = 0;
	public final static int REMOVE_VIDEO = 1;
	public final static int SET_ON_HOLD = 2;
	public final static int SET_ON_RESUME = 3;

	/**
	 * Constant values for session direction type
	 */
	public static final int TYPE_INCOMING_IPCALL = 16;
	public static final int TYPE_OUTGOING_IPCALL = 17;

	
	/**
	 * Live video content to be shared
	 */
	private LiveVideoContent videoContent = null;

	/**
	 * Live audio content to be shared
	 */
	private LiveAudioContent audioContent = null;

	/**
	 * Audio renderer
	 */
	private IAudioRenderer audioRenderer = null;

	/**
	 * Audio player
	 */
	private IAudioPlayer audioPlayer = null;

	/**
	 * Video renderer
	 */
	private IVideoRenderer videoRenderer = null;

	/**
	 * Video player
	 */
	private IVideoPlayer videoPlayer = null;

	/**
	 * The logger
	 */
	private Logger logger = Logger.getLogger(this.getClass().getName());
	
	/**
	 * Constructor
	 * 
	 * @param parent IMS service
	 * @param contact Remote contact
	 * @param audioContent AudioContent
	 * @param videoContent VideoContent
	 */
	public IPCallStreamingSession(ImsService imsService, String contact, LiveAudioContent audioContent, LiveVideoContent videoContent) {
		super(imsService, contact);

		this.videoContent = videoContent;
		this.audioContent = audioContent;
	}

	/**
	 * Returns the video content
	 * 
	 * @return Live video content
	 */
	public LiveVideoContent getVideoContent() {
		return videoContent;
	}

	/**
	 * Set the video content
	 * 
	 * @param videoContent Live video content
	 */
	public void setVideoContent(LiveVideoContent videoContent) {
		this.videoContent = videoContent;
	}

	/**
	 * Returns the audio content
	 * 
	 * @return Live audio content
	 */
	public LiveAudioContent getAudioContent() {
		return audioContent;
	}

	/**
	 * Set the audio content
	 * 
	 * @param audioContent Live audio content
	 */
	public void setAudioContent(LiveAudioContent audioContent) {
		this.audioContent = audioContent;
	}

	/**
	 * Get the audio renderer
	 * 
	 * @return Audio renderer
	 */
	public IAudioRenderer getAudioRenderer() {
		return audioRenderer;
	}

	/**
	 * Set the audio renderer
	 * 
	 * @param audioRenderer Audio renderer
	 */
	public void setAudioRenderer(IAudioRenderer audioRenderer) {
		this.audioRenderer = audioRenderer;
	}

	/**
	 * Get the audio player
	 * 
	 * @return Audio player
	 */
	public IAudioPlayer getAudioPlayer() {
		return audioPlayer;
	}

	/**
	 * Set the audio player
	 * 
	 * @param audioPlayer Audio player
	 */
	public void setAudioPlayer(IAudioPlayer audioPlayer) {
		this.audioPlayer = audioPlayer;
	}

	/**
	 * Get the video renderer
	 * 
	 * @return Video renderer
	 */
	public IVideoRenderer getVideoRenderer() {
		return videoRenderer;
	}

	/**
	 * Set the video renderer
	 * 
	 * @param videoRenderer Video renderer
	 */
	public void setVideoRenderer(IVideoRenderer videoRenderer) {
		this.videoRenderer = videoRenderer;
	}

	/**
	 * Get the video player
	 * 
	 * @return Video player
	 */
	public IVideoPlayer getVideoPlayer() {
		return videoPlayer;
	}

	/**
	 * Set the video player
	 * 
	 * @param videoPlayer Video player
	 */
	public void setVideoPlayer(IVideoPlayer videoPlayer) {
		this.videoPlayer = videoPlayer;
	}

	/**
	 * Set the video player
	 * 
	 * @param videoPlayer Video player
	 */
	public int getSessionDirection() {
		int direction =  (this instanceof OriginatingIPCallStreamingSession)?TYPE_OUTGOING_IPCALL : TYPE_INCOMING_IPCALL ;
		return direction;
	}
	
	
	/**
	 * Receive BYE request
	 * 
	 * @param bye BYE request
	 */
	public void receiveBye(SipRequest bye) {
		super.receiveBye(bye);

		// Request capabilities to the remote
		getImsService().getImsModule().getCapabilityService()
				.requestContactCapabilities(getDialogPath().getRemoteParty());
	}

	/**
	 * Create an INVITE request
	 * 
	 * @return the INVITE request
	 * @throws SipException
	 */
	public SipRequest createInvite() throws SipException {
		return SipMessageFactory.createInvite(getDialogPath(), null,
				getDialogPath().getLocalContent());
	}
	
	/**
     * Close media session
     */
	public void closeMediaSession() {
		if (logger.isActivated()) {
			logger.info("Close media session");
		}

//		if (audioRenderer != null) {
//			// Close the audio renderer
//			try {
//				audioRenderer.stop();
//				audioRenderer.close();
//				if (logger.isActivated()) {
//					logger.info("Stop and Close the audio renderer");
//				}
//			} catch (RemoteException e) {
//				if (logger.isActivated()) {
//					logger.error("Exception when closing the audio renderer", e);
//				}
//			}
//		}
//		if (audioPlayer != null) {
//			// Close the audio player
//			try {
//				audioPlayer.stop();
//				audioPlayer.close();
//				if (logger.isActivated()) {
//					logger.info("Stop and Close the audio player");
//				}
//			} catch (RemoteException e) {
//				if (logger.isActivated()) {
//					logger.error("Exception when closing the audio player", e);
//				}
//			}
//		}
		
		if (videoRenderer != null) {
			// Close the video renderer
			try {
				videoRenderer.stop();
				videoRenderer.close();
				if (logger.isActivated()) {
					logger.info("Stop and close video renderer");
				}
			} catch (RemoteException e) {
				if (logger.isActivated()) {
					logger.error("Exception when closing the video renderer", e);
				}
			}
		}
		if (videoPlayer != null) {
			// Close the video player
			try {
				videoPlayer.stop();
				videoPlayer.close();
				if (logger.isActivated()) {
					logger.info("stop and close video player");
				}
			} catch (Exception e) {
				if (logger.isActivated()) {
					logger.error("Exception when closing the video player", e);
				}
			}
		}
		setAudioPlayer(null);
		setAudioRenderer(null);
		setVideoPlayer(null);
		setVideoRenderer(null);
	}
	


	/**
	 * Add video in the current call
	 * 
	 * @param videoPlayer Video player
	 * @param videoRenderer Video renderer
	 */
	public void addVideo(IVideoPlayer videoPlayer, IVideoRenderer videoRenderer) {
		if (logger.isActivated()) {
			logger.info("Add video");
		}

		// Set video player/render
		setVideoRenderer(videoRenderer);
		
		setVideoPlayer(videoPlayer);
		

		// Build SDP
		String sdp = buildAddVideoSdpProposal();

		// Set SDP proposal as the local SDP part in the dialog path
		getDialogPath().setLocalContent(sdp);

		// Create re-INVITE
		SipRequest reInvite = getUpdateSessionManager().createReInvite(
				IPCallService.FEATURE_TAGS_IP_VIDEO_CALL, sdp);

		// Send re-INVITE
		getUpdateSessionManager().sendReInvite(reInvite,
				IPCallStreamingSession.ADD_VIDEO);

	}

	/**
	 * Remove video from the current call
	 */
	public void removeVideo() {
		if (logger.isActivated()) {
			logger.info("Remove video");
		}
		
		// Build SDP
		String sdp = buildRemoveVideoSdpProposal();

		// Set the SDP proposal as local SDP content in the dialog path
		getDialogPath().setLocalContent(sdp);

		// Create re-INVITE
		SipRequest reInvite = getUpdateSessionManager().createReInvite(
				IPCallService.FEATURE_TAGS_IP_VOICE_CALL, sdp);

		// Send re-INVITE
		getUpdateSessionManager().sendReInvite(reInvite,
				IPCallStreamingSession.REMOVE_VIDEO);
	}

	/**
	 * Is tag present in SDP
	 * 
	 * @param sdp SDP
	 * @param tag Tag to be searched
	 * @return Boolean
	 */
	private boolean isTagPresent(String sdp, String tag) {
		if ((sdp != null) && (sdp.toLowerCase().indexOf(tag) != -1)) {
			return true;
		} else {
			return false;
		}
	}
	

	/**
	 * Build SDP proposal for session call init
	 * 
	 * @return SDP content or null in case of error
	 */
	private String buildCallInitSdpProposal() {
		 // Build SDP part

		try {
        String ntpTime = SipUtils.constructNTPtime(System.currentTimeMillis());
    	String ipAddress = getDialogPath().getSipStack().getLocalIpAddress();
        
    	String audioSdp = AudioSdpBuilder.buildSdpOffer(getAudioPlayer().getSupportedAudioCodecs(),
    			getAudioPlayer().getLocalRtpPort());
    	String videoSdp = "";
        if ((getVideoContent()!= null)&&(getVideoPlayer()!= null)&&(getVideoRenderer()!= null)) {
        	
				videoSdp = VideoSdpBuilder.buildSdpOfferWithOrientation(
						getVideoPlayer().getSupportedVideoCodecs(),
						getVideoRenderer().getLocalRtpPort());		
        }
    	
    	String  sdp =
        	"v=0" + SipUtils.CRLF +
        	"o=- " + ntpTime + " " + ntpTime + " " + SdpUtils.formatAddressType(ipAddress) + SipUtils.CRLF +
        	"s=-" + SipUtils.CRLF +
        	"c=" + SdpUtils.formatAddressType(ipAddress) + SipUtils.CRLF +
        	"t=0 0" + SipUtils.CRLF +
        	audioSdp +
        	videoSdp +
        	"a=sendrcv" + SipUtils.CRLF;

    	return sdp;
		
		} catch (RemoteException e) {
			if (logger.isActivated()) {
                logger.error("Session initiation has failed", e);
            }
            
            // Unexpected error
            handleError(new IPCallError(IPCallError.UNEXPECTED_EXCEPTION,
                    e.getMessage()));
			return null;
		}	
	}
	

	/**
	 * Build SDP proposal to add video stream in the session
	 * 
	 * @return SDP content or null in case of error
	 */
	private String buildAddVideoSdpProposal() {
		if (logger.isActivated()) {
			logger.debug("Build SDP proposal to add video stream in the session");
		}

		try {
			// Build SDP part
			String ntpTime = SipUtils.constructNTPtime(System.currentTimeMillis());
			String ipAddress = getDialogPath().getSipStack().getLocalIpAddress();

			String audioSdp = AudioSdpBuilder.buildSdpOffer(getAudioPlayer().getSupportedAudioCodecs(), 
					getAudioPlayer().getLocalRtpPort());
			
			String videoSdp = "";
			videoSdp = VideoSdpBuilder.buildSdpOfferWithOrientation(
					getVideoPlayer().getSupportedVideoCodecs(),
					getVideoRenderer().getLocalRtpPort());
			
			return "v=0" + SipUtils.CRLF +
					"o=- " + ntpTime + " " + ntpTime + " " + SdpUtils.formatAddressType(ipAddress) + SipUtils.CRLF +
					"s=-" + SipUtils.CRLF +
					"c=" + SdpUtils.formatAddressType(ipAddress) + SipUtils.CRLF +
					"t=0 0" + SipUtils.CRLF + audioSdp +
					"a=sendrcv"	+ SipUtils.CRLF + videoSdp +
					"a=sendrcv"	+ SipUtils.CRLF;
		} catch (RemoteException e) {
			if (logger.isActivated()) {
				logger.error("Add video has failed", e);
			}

			// Unexpected error
			handleError(new IPCallError(IPCallError.UNEXPECTED_EXCEPTION, e.getMessage()));
			return null;
		}
	}

	/**
	 * Build SDP proposal to remove video stream from the session
	 * 
	 * @return SDP content or null in case of error
	 */
	private String buildRemoveVideoSdpProposal() {
		if (logger.isActivated()) {
			logger.debug("Build SDP proposal to remove video stream from the session");
		}

		try {
			// Build SDP part
			String ntpTime = SipUtils.constructNTPtime(System.currentTimeMillis());
			String ipAddress = getDialogPath().getSipStack().getLocalIpAddress();

			getAudioPlayer().getLocalRtpPort();			
			String audioSdp = AudioSdpBuilder.buildSdpOffer(getAudioPlayer().getSupportedAudioCodecs(), getAudioPlayer().getLocalRtpPort());
			
			return "v=0" + SipUtils.CRLF +
					"o=- " + ntpTime + " " + ntpTime + " " + SdpUtils.formatAddressType(ipAddress) + SipUtils.CRLF +
					"s=-" + SipUtils.CRLF +
					"c=" + SdpUtils.formatAddressType(ipAddress) + SipUtils.CRLF +
					"t=0 0" + SipUtils.CRLF + audioSdp +
					"a=sendrcv"	+ SipUtils.CRLF;
		} catch (RemoteException e) {
			if (logger.isActivated()) {
				logger.error("Remove video has failed", e);
			}

			// Unexpected error
			handleError(new IPCallError(IPCallError.UNEXPECTED_EXCEPTION, e.getMessage()));
			return null;
		}
	}

	/**
	 * select sdp builder method (to build sdp response) according to serviceContext - build and return sdp
	 * 
	 * @param reInvite
	 *            reInvite received request
	 * @param serviceContext
	 *            context of service (Add Video, Remove Video ...)
	 * @return sdp built by builder
	 */
	public String buildReInviteSdpResponse(SipRequest reInvite, int serviceContext) {
		String localSdp = "";
		switch (serviceContext) {
			case (IPCallStreamingSession.ADD_VIDEO): {
				localSdp = buildAddVideoSdpResponse(reInvite);
				break;
			}
			case (IPCallStreamingSession.REMOVE_VIDEO): {
				localSdp = buildRemoveVideoSdpResponse(); // for remove Video: same sdp used for response as the one used for proposal 
				break;
			}
		}
		return localSdp;
	}

	/**
	 * Build sdp response for addVideo
	 * 
	 * @param reInvite  reInvite Request received
	 */
	private String buildCallInitSdpResponse(SipRequest reInvite) {
		// Parse the remote SDP part
        SdpParser parser = new SdpParser(getDialogPath().getRemoteContent().getBytes());
        
        // Extract the remote host (same between audio and video)
        //String remoteHost = SdpUtils.extractRemoteHost(parser.sessionDescription.connectionInfo);
        // TODO String remoteHost = SdpUtils.extractRemoteHost(parser.sessionDescription, mediaDesc);
       
        // Extract the audio port
        MediaDescription mediaAudio = parser.getMediaDescription("audio");
        int audioRemotePort = mediaAudio.port;

        // Extract the video port
        MediaDescription mediaVideo = parser.getMediaDescription("video");
        //int videoRemotePort = mediaVideo.port;
        int videoRemotePort = -1;
        if (mediaVideo != null) {
            videoRemotePort = mediaVideo.port;
        }

        // Extract the audio codecs from SDP
        Vector<MediaDescription> audio = parser.getMediaDescriptions("audio");
        Vector<AudioCodec> proposedAudioCodecs = AudioCodecManager.extractAudioCodecsFromSdp(audio);

        // Extract video codecs from SDP            
        Vector<MediaDescription> video = parser.getMediaDescriptions("video");
        Vector<VideoCodec> proposedVideoCodecs = VideoCodecManager.extractVideoCodecsFromSdp(video);
        
        // Audio codec negotiation
		AudioCodec selectedAudioCodec;
		try {
			selectedAudioCodec = AudioCodecManager.negociateAudioCodec(getAudioRenderer().getSupportedAudioCodecs(), proposedAudioCodecs);
			if (selectedAudioCodec == null) {
				if (logger.isActivated()) {
					logger.debug("Proposed audio codecs are not supported");
				}

				// Send a 415 Unsupported media type response
				send415Error(getDialogPath().getInvite());

				// Unsupported media type
				handleError(new IPCallError(IPCallError.UNSUPPORTED_AUDIO_TYPE));
				return null;
			}
			
	        // Video codec negotiation
			VideoCodec selectedVideoCodec = null;
			if (getVideoPlayer() != null) {
					selectedVideoCodec = VideoCodecManager.negociateVideoCodec(getVideoPlayer().getSupportedVideoCodecs(), proposedVideoCodecs);
					if (selectedVideoCodec == null) {
			            if (logger.isActivated()) {
			                logger.debug("Proposed video codecs are not supported");
			            }
			            
			            // Terminate session
			            terminateSession(ImsServiceSession.TERMINATION_BY_SYSTEM);
			            
			            // Report error
			            handleError(new IPCallError(IPCallError.UNSUPPORTED_VIDEO_TYPE));
			            return null;
			        }
			}
			

			
	    	
	    	String audioSdp = AudioSdpBuilder.buildSdpAnswer(selectedAudioCodec.getMediaCodec(),
	    			getAudioPlayer().getLocalRtpPort());
	        
	    	String videoSdp = "";
	        if ((getVideoContent() != null) && (getVideoRenderer() != null)){
	        	if (selectedVideoCodec != null) {
	            	videoSdp = VideoSdpBuilder.buildSdpAnswer(selectedVideoCodec.getMediaCodec(),
	            			getVideoRenderer().getLocalRtpPort(), mediaVideo);
	            }	
	        }
	        
	     // Build audioSdp and videoSdp part
	        String ntpTime = SipUtils.constructNTPtime(System.currentTimeMillis());
	    	String ipAddress = getDialogPath().getSipStack().getLocalIpAddress();

	        // Build SDP for response
	        String sdp =
	        	"v=0" + SipUtils.CRLF +
	        	"o=- " + ntpTime + " " + ntpTime + " " + SdpUtils.formatAddressType(ipAddress) + SipUtils.CRLF +
	        	"s=-" + SipUtils.CRLF +
	        	"c=" + SdpUtils.formatAddressType(ipAddress) + SipUtils.CRLF +
	            "t=0 0" + SipUtils.CRLF +
	            audioSdp +
	            videoSdp +
	            "a=sendrcv" + SipUtils.CRLF;
	        
			return sdp;
	        
		} catch (RemoteException e) {
			if (logger.isActivated()) {
                logger.error("Session initiation has failed", e);
            }
            
            // Unexpected error
            handleError(new IPCallError(IPCallError.UNEXPECTED_EXCEPTION,
                    e.getMessage()));
            
            return null;
		}


	}
	
	/**
	 * Build sdp response for addVideo
	 * 
	 * @param reInvite  reInvite Request received
	 */
	private String buildAddVideoSdpResponse(SipRequest reInvite) {
		if (logger.isActivated()) {
			logger.info("buildAddVideoSdpResponse()");
		}

		String sdp = "";
		// Parse the remote SDP part
		SdpParser parser = new SdpParser(reInvite.getSdpContent().getBytes());
		MediaDescription mediaVideo = parser.getMediaDescription("video");

		// Extract video codecs from SDP
		Vector<MediaDescription> medias = parser.getMediaDescriptions("video");
		Vector<VideoCodec> proposedVideoCodecs = VideoCodecManager
				.extractVideoCodecsFromSdp(medias);
		try {
			// Check that a video player and renderer has been set
			if ((getVideoPlayer() == null) || (getVideoPlayer().getVideoCodec() == null)) {
				handleError(new IPCallError(IPCallError.UNSUPPORTED_VIDEO_TYPE,
						"Video player null or Video codec not selected"));
			} else
			if ((getVideoRenderer() == null) || (getVideoRenderer().getVideoCodec() == null)) {
				handleError(new IPCallError(IPCallError.UNSUPPORTED_VIDEO_TYPE,
						"Video renderer null or Video codec not selected"));
			} else {
				// Codec negotiation
				VideoCodec selectedVideoCodec = VideoCodecManager.negociateVideoCodec(
						getVideoRenderer().getSupportedVideoCodecs(),
						proposedVideoCodecs);
				if (selectedVideoCodec == null) {
					if (logger.isActivated()) {
						logger.debug("Proposed codecs are not supported");
					}

					// Send a 415 Unsupported media type response
					send415Error(reInvite);

					// Unsupported media type
					handleError(new IPCallError(
							IPCallError.UNSUPPORTED_VIDEO_TYPE));
				} else {
					// Build SDP part for response
					String ntpTime = SipUtils.constructNTPtime(System.currentTimeMillis());
					String ipAddress = getDialogPath().getSipStack().getLocalIpAddress();
					String videoSdp = VideoSdpBuilder.buildSdpAnswer(
							selectedVideoCodec.getMediaCodec(),
							getVideoRenderer().getLocalRtpPort(), mediaVideo);
					String audioSdp = AudioSdpBuilder.buildSdpAnswer(
							getAudioPlayer().getAudioCodec(),
							getAudioRenderer().getLocalRtpPort());
					sdp = "v=0" + SipUtils.CRLF +
							"o=- " + ntpTime + " " + ntpTime + " " + SdpUtils.formatAddressType(ipAddress) + SipUtils.CRLF +
							"s=-" + SipUtils.CRLF +
							"c=" + SdpUtils.formatAddressType(ipAddress) + SipUtils.CRLF +
							"t=0 0" + SipUtils.CRLF
							+ audioSdp +
							"a=sendrcv" + SipUtils.CRLF +
							videoSdp +
							"a=sendrcv" + SipUtils.CRLF;
				}
			}
		} catch (RemoteException e) {
			if (logger.isActivated()) {
				logger.error("Add Video has failed", e);
			}

			// Unexpected error
			handleError(new IPCallError(IPCallError.UNEXPECTED_EXCEPTION,
					e.getMessage()));
		}

		return sdp;
	}

	/**
	 * Build sdp response for removeVideo
	 * @return sdp content
	 */
	private String buildRemoveVideoSdpResponse() {
		if (logger.isActivated()) {
			logger.info("buildRemoveVideoSdpResponse()");
		}

		// Build SDP part
		String sdp = "";
		String ntpTime = SipUtils.constructNTPtime(System.currentTimeMillis());
		String ipAddress = getDialogPath().getSipStack().getLocalIpAddress();

		
		try {
			logger.warn("Build audio sdp");
			getAudioPlayer().getLocalRtpPort();
			String audioSdp = AudioSdpBuilder.buildSdpAnswer(getAudioPlayer()
					.getAudioCodec(), getAudioPlayer().getLocalRtpPort());			
			//String audioSdp = AudioSdpBuilder.buildSdp(getAudioPlayer().getSupportedAudioCodecs(), getAudioPlayer().getLocalRtpPort());
			
			sdp = "v=0" + SipUtils.CRLF + "o=- " + ntpTime + " " + ntpTime
					+ " " + SdpUtils.formatAddressType(ipAddress)
					+ SipUtils.CRLF + "s=-" + SipUtils.CRLF + "c="
					+ SdpUtils.formatAddressType(ipAddress) + SipUtils.CRLF
					+ "t=0 0" + SipUtils.CRLF + audioSdp + "a=sendrcv"
					+ SipUtils.CRLF;
		} catch (RemoteException e) {
			if (logger.isActivated()) {
				logger.error("Remove Video has failed", e);
			}

			// Unexpected error
			handleError(new IPCallError(IPCallError.UNEXPECTED_EXCEPTION,
					e.getMessage()));
		}
		return sdp;
	}
	
	/**
	 * Receive re-INVITE request
	 * 
	 * @param reInvite
	 *            re-INVITE received request
	 */
	public void receiveReInvite(SipRequest reInvite) {
		if (logger.isActivated()) {
			logger.info("receiveReInvite");
		}

		int requestType;
		if (reInvite.getSdpContent() == null) { 
			// "Keep Alive" ReInvite	
			getSessionTimerManager().receiveReInvite(reInvite);
		} 
		else { 
			// ReInvite for Update of Session
			
			// set received sdp proposal as remote sdp content in dialogPath
			getDialogPath().setRemoteContent(reInvite.getSdpContent());
			
			// Analyze sdp to dispatch according to sdp content
			if (isTagPresent(reInvite.getSdpContent(), "m=video")) {
				requestType = 0;
			} else {
				requestType = 1;
			}

			switch (requestType) {
			case (0): { // Case Add Video
				
				// processes user Answer and SIP response
				getUpdateSessionManager().waitUserAckAndSendReInviteResp(
						reInvite,
						IPCallService.FEATURE_TAGS_IP_VIDEO_CALL,
						IPCallStreamingSession.ADD_VIDEO);
				
				// get video Encoding , video Width and video Height
				VideoContent videocontent = (VideoContent) getVideoContent();
				String videoEncoding = (videocontent == null) ? ""
						: videocontent.getEncoding();
				int videoWidth = (videocontent == null) ? 0 : videocontent
						.getWidth();
				int videoHeight = (videocontent == null) ? 0 : videocontent
						.getHeight();
				
				// Notify listeners
				if (!isInterrupted()) {
					for (int i = 0; i < getListeners().size(); i++) {
						((IPCallStreamingSessionListener) getListeners().get(i))
								.handleAddVideoInvitation(videoEncoding,
										videoWidth, videoHeight);
					}
				}
			}
				break;
			case (1): { // Case Remove Video
				// Set the local SDP part in the dialog path
				getDialogPath().setLocalContent(reInvite.getSdpContent());

				// process user Answer and SIP response
				getUpdateSessionManager().send200OkReInviteResp(reInvite,
						IPCallService.FEATURE_TAGS_IP_VOICE_CALL,
						IPCallStreamingSession.REMOVE_VIDEO);

				// Notify listeners
				if (!isInterrupted()) {
					for (int i = 0; i < getListeners().size(); i++) {
						((IPCallStreamingSessionListener) getListeners().get(i))
								.handleRemoveVideo();
					}
				}
			}
				break;
			}
		}
	}

	/**
	 * Handle Sip Response to ReInvite / originating side 
	 * 
	 * @param int code response code
	 * @param response
	 *            Sip response to sent ReInvite
	 * @param requestType
	 *            Type type of request (addVideo/RemoveVideo/Set on Hold/Set on
	 *            Resume)
	 */
	public void handleReInviteResponse(int code, SipResponse response, int requestType) {
		if (logger.isActivated()) {
			logger.info("handleReInviteResponse: " + code);
		}

		// case Add video
		if (requestType == IPCallStreamingSession.ADD_VIDEO) {
			if (code == 200) {	// 200 OK response			
				//prepare Video media session
				prepareVideoSession();
				
				// Notify listeners
				if (!isInterrupted()) {
					for (int i = 0; i < getListeners().size(); i++) {
						((IPCallStreamingSessionListener) getListeners().get(i))
								.handleAddVideoAccepted();
					}
				}
				
				try {
					startVideoSession(true) ;
				} catch (Exception e) {
					if (logger.isActivated()) {
		                logger.error("Start Video session has failed", e);
		            }
					handleError(new ImsSessionBasedServiceError(
							ImsSessionBasedServiceError.UNEXPECTED_EXCEPTION, e.getMessage()));
				}
			} else if ((code == ImsServiceSession.INVITATION_REJECTED)
					|| (code == ImsServiceSession.TERMINATION_BY_TIMEOUT)) {
				// Notify listeners
				if (!isInterrupted()) {
					for (int i = 0; i < getListeners().size(); i++) {
						((IPCallStreamingSessionListener) getListeners().get(i))
								.handleAddVideoAborted(code);
					}
				}
			}

			// case Remove Video
		} else if (requestType == IPCallStreamingSession.REMOVE_VIDEO) {
			if (code == 200) { // 200 OK response
				// close video media session and set player/renderer to null
				closeVideoSession();
				setVideoPlayer(null);
				setVideoRenderer(null);
				
				// Notify listeners
				if (!isInterrupted()) {
					for (int i = 0; i < getListeners().size(); i++) {
						((IPCallStreamingSessionListener) getListeners().get(i))
								.handleRemoveVideoAccepted();
					}
				}
			} else if (code == ImsServiceSession.TERMINATION_BY_TIMEOUT) { // No answer or 408 TimeOut response
				// Notify listeners
				if (!isInterrupted()) {
					for (int i = 0; i < getListeners().size(); i++) {
						((IPCallStreamingSessionListener) getListeners().get(i))
								.handleRemoveVideoAborted(code);
					}
				}
			}
		}
	}
	

	/**
	 * Handle Sip Response to ReInvite/ terminating side
	 * 
	 * @param int code response code
	 * @param requestType
	 *            Type type of request (addVideo/RemoveVideo/Set on Hold/Set on
	 *            Resume)
	 */
	public void handleReInviteUserAnswer(int code, int requestType){
		if (logger.isActivated()) {
			logger.info("handleReInviteUserAnswer: " + code);
		}

		// case Add video
		if (requestType == IPCallStreamingSession.ADD_VIDEO) {
			//Invitation accepted
			if (code == ImsServiceSession.INVITATION_ACCEPTED) {
				prepareVideoSession();
			// Invitation declined or not answered
			} else if (code == ImsServiceSession.INVITATION_NOT_ANSWERED)
					 {
				// Notify listeners
				if (!isInterrupted()) {
					for (int i = 0; i < getListeners().size(); i++) {
						((IPCallStreamingSessionListener) getListeners().get(i))
								.handleAddVideoAborted(code);
					}
				}
			}		
		} 		
	}
	
	
	/**
	 * Handle Sip Response to ReInvite/ terminating side
	 * 
	 * @param int code response code
	 * @param requestType
	 *            Type type of request (addVideo/RemoveVideo/Set on Hold/Set on
	 *            Resume)
	 */
	public void handleReInviteAck(int code, int requestType) {
		if (logger.isActivated()) {
			logger.info("handleReInviteAckResponse: " + code);
		}

		// case Add video
		if ((requestType == IPCallStreamingSession.ADD_VIDEO) && (code == 200)) {
			try {
				startVideoSession(false);
			} catch (Exception e) {
				if (logger.isActivated()) {
					logger.error("Start Video session has failed", e);
				}
				handleError(new ImsSessionBasedServiceError(
						ImsSessionBasedServiceError.UNEXPECTED_EXCEPTION,
						e.getMessage()));
			}
			
			// Notify listeners
			if (!isInterrupted()) {
				for (int i = 0; i < getListeners().size(); i++) {
					((IPCallStreamingSessionListener) getListeners().get(i))
							.handleAddVideoAccepted();
				}
			}

			
			// case Remove Video
		} else if ((requestType == IPCallStreamingSession.REMOVE_VIDEO)
				&& (code == 200)) {
			// close video media session
			closeVideoSession();

			// set video player/renderer to null
			setVideoPlayer(null);
			setVideoRenderer(null);

			// Notify listeners
			if (!isInterrupted()) {
				for (int i = 0; i < getListeners().size(); i++) {
					((IPCallStreamingSessionListener) getListeners().get(i))
							.handleRemoveVideoAccepted();
				}
			}
		}
	}
	
	
	/**
	 * Handle 486 Busy
	 * 
	 * @param resp SipResponse
	 *            
	 */
	public void handle486Busy(SipResponse resp) {
		if (logger.isActivated()) {
			logger.info("486 Busy");
		}

		// Close audio and video session
		closeMediaSession();

		// Remove the current session
		getImsService().removeSession(this);

		// Request capabilities to the remote
		getImsService().getImsModule().getCapabilityService()
				.requestContactCapabilities(getDialogPath().getRemoteParty());

		// Notify listeners
		if (!isInterrupted()) {
			for (int i = 0; i < getListeners().size(); i++) {
				((IPCallStreamingSessionListener) getListeners().get(i))
						.handle486Busy();
			}
		}
	}


	/**
	 * Handle 407 Proxy authent error
	 * 
	 * @param resp SipResponse
	 * @param requestType type of request (addVideo/RemoveVideo/Set on Hold/Set on
	 *            Resume)            
	 */
	public void handleReInvite407ProxyAuthent(SipResponse response,
			int requestType) {

		// // Set the remote tag
		getDialogPath().setRemoteTag(response.getToTag());

		// Update the authentication agent
		getAuthenticationAgent().readProxyAuthenticateHeader(response);

		// get sdp content
		String content = getDialogPath().getLocalContent();

		SipRequest reInvite = null;
		// create reInvite request
		if (requestType == IPCallStreamingSession.ADD_VIDEO) {
			reInvite = getUpdateSessionManager().createReInvite(
					IPCallService.FEATURE_TAGS_IP_VIDEO_CALL, content);
		} else if (requestType == IPCallStreamingSession.REMOVE_VIDEO) {
			reInvite = getUpdateSessionManager().createReInvite(
					IPCallService.FEATURE_TAGS_IP_VIDEO_CALL, content);
		} else {
			// TODO for set On Hold
			reInvite = getUpdateSessionManager().createReInvite(
					IPCallService.FEATURE_TAGS_IP_VIDEO_CALL, content);
		}

		// send reInvite request
		getUpdateSessionManager().sendReInvite(reInvite, requestType);

	}

	
	/**
	 * Handle error
	 * 
	 * @param error Error
	 */
	public void handleError(ImsServiceError error) {
		if (logger.isActivated()) {
			logger.info("Session error: " + error.getErrorCode() + ", reason="
					+ error.getMessage());
		}

		// Close Audio and Video session
		closeMediaSession();

		// Remove the current session
		getImsService().removeSession(this);

		// Request capabilities to the remote
		getImsService().getImsModule().getCapabilityService()
				.requestContactCapabilities(getDialogPath().getRemoteParty());

		// Notify listeners
		if (!isInterrupted()) {
			for (int i = 0; i < getListeners().size(); i++) {
				((IPCallStreamingSessionListener) getListeners().get(i))
						.handleCallError(new IPCallError(error));
			}
		}
	}

	/**
	 * Prepare video session (set codec, get remote Host and port ...) 
	 * 
	 */
	public void prepareVideoSession() {
		if (logger.isActivated()) {
			logger.info("prepareVideoSession()");
		}
		// Parse the remote SDP part
		SdpParser parser = new SdpParser(getDialogPath().getRemoteContent().getBytes());
		MediaDescription mediaVideo = parser.getMediaDescription("video");
		String remoteHost = SdpUtils.extractRemoteHost(parser.sessionDescription.connectionInfo);
        // TODO String remoteHost = SdpUtils.extractRemoteHost(parser.sessionDescription, mediaVideo);
		int remotePort = mediaVideo.port;

		try {
			// Extract video codecs from SDP
			Vector<MediaDescription> medias = parser
					.getMediaDescriptions("video");
			Vector<VideoCodec> proposedCodecs = VideoCodecManager
					.extractVideoCodecsFromSdp(medias);

			// Codec negotiation
			VideoCodec selectedVideoCodec = VideoCodecManager
					.negociateVideoCodec(getVideoPlayer()
							.getSupportedVideoCodecs(), proposedCodecs);

			if (selectedVideoCodec == null) {
				if (logger.isActivated()) {
					logger.debug("Proposed codecs are not supported");
				}

				// Terminate session
				terminateSession(ImsServiceSession.TERMINATION_BY_SYSTEM);

				// Report error Unsupported video type
				handleError(new IPCallError(IPCallError.UNSUPPORTED_VIDEO_TYPE));
				return;
			}

			// getContent().setEncoding("video/" +
			// selectedPlayerVideoCodec.getCodecName()); => to remove is not
			// used

			// Set the selected video codec
			getVideoPlayer().setVideoCodec(selectedVideoCodec.getMediaCodec());
			getVideoRenderer()
					.setVideoCodec(selectedVideoCodec.getMediaCodec());

			if (logger.isActivated()) {
				logger.info("Video Player - selectedVideoCodec = "
						+ getVideoPlayer().getVideoCodec().getCodecName());
				logger.info("Video Renderer - selectedVideoCodec = "
						+ getVideoRenderer().getVideoCodec().getCodecName());
			}

			// Set the OrientationHeaderID
			SdpOrientationExtension extensionHeader = SdpOrientationExtension
					.create(mediaVideo);
			if (extensionHeader != null) {
				getVideoRenderer().setOrientationHeaderId(
						extensionHeader.getExtensionId());
				getVideoPlayer().setOrientationHeaderId(
						extensionHeader.getExtensionId());
			}

			// Set video player and renderer event listeners
			getVideoRenderer().addListener(new VideoPlayerEventListener(this));
			getVideoPlayer().addListener(new VideoPlayerEventListener(this));

			// Open the video renderer and player
			getVideoRenderer().open(remoteHost, remotePort);
			getVideoPlayer().open(remoteHost, remotePort);

		} catch (RemoteException e) {
			// Report error
			handleError(new IPCallError(IPCallError.UNEXPECTED_EXCEPTION));

		}

	}
	
	/**
     * Start video session
     *
     * @throws Exception 
     */
	public void startVideoSession(boolean originating) throws Exception {
		if (logger.isActivated()) {
			logger.info("startVideoSession()");
		}

		// Start the video player and renderer
		getVideoPlayer().start();
		getVideoRenderer().start();
	}
	
	/**
	 * Close video session  
	 */
	public void closeVideoSession(){
		if (logger.isActivated()) {
			logger.info("closeVideoSession()");
		}
		
		if (getVideoPlayer() != null) {
			// Close the video player
			try {	
				getVideoPlayer().stop();
				getVideoPlayer().close();
				if (logger.isActivated()) {
					logger.info("stop and close Video player");
				}
			} catch (Exception e) {
				if (logger.isActivated()) {
					logger.error("Exception when closing the video player", e);
				}
			}
		}
		
		if (getVideoRenderer()!= null) {
			// Close the video renderer
        	try {
				getVideoRenderer().stop();
				getVideoRenderer().close();
				if (logger.isActivated()) {
        			logger.info("stop and close Video renderer");
        		}
			} catch (RemoteException e) {
				if (logger.isActivated()) {
	                logger.error("Exception when closing the video renderer", e);
	            }
			}
        }
	}
	
	/**
	 * Audio player event listener
	 */
	protected class AudioPlayerEventListener extends IAudioEventListener.Stub {
		/**
		 * Streaming session
		 */
		private IPCallStreamingSession session;

		/**
		 * Constructor
		 * 
		 * @param session
		 *            Streaming session
		 */
		public AudioPlayerEventListener(IPCallStreamingSession session) {
			this.session = session;
		}

		/**
		 * Audio player is opened
		 */
		public void audioOpened() {
			if (logger.isActivated()) {
				logger.debug("Audio player is opened");
			}
		}

		/**
		 * Audio player is closed
		 */
		public void audioClosed() {
			if (logger.isActivated()) {
				logger.debug("Audio player is closed");
			}
		}

		/**
		 * Audio player is started
		 */
		public void audioStarted() {
			if (logger.isActivated()) {
				logger.debug("Audio player is started");
			}
		}

		/**
		 * Audio player is stopped
		 */
		public void audioStopped() {
			if (logger.isActivated()) {
				logger.debug("Audio player is stopped");
			}
		}

		/**
		 * Audio player has failed
		 * 
		 * @param error Error
		 */
		public void audioError(String error) {
			if (logger.isActivated()) {
				logger.error("Audio player has failed: " + error);
			}

			// Close the media (audio, video) session
			closeMediaSession();

			// Terminate session
			terminateSession(ImsServiceSession.TERMINATION_BY_SYSTEM);

			// Remove the current session
			getImsService().removeSession(session);

			// Notify listeners
			if (!isInterrupted()) {
				for (int i = 0; i < getListeners().size(); i++) {
					((IPCallStreamingSessionListener) getListeners().get(i))
							.handleCallError(new IPCallError(
									IPCallError.AUDIO_STREAMING_FAILED,
									error));
				}
			}

			// Request capabilities to the remote
			getImsService()
					.getImsModule()
					.getCapabilityService()
					.requestContactCapabilities(
							getDialogPath().getRemoteParty());
		}
	}
	
	
	
    /**
     * Video player event listener
     */
    protected class VideoPlayerEventListener extends IVideoEventListener.Stub {
        /**
         * Streaming session
         */
        private IPCallStreamingSession session;

        /**
         * Constructor
         *
         * @param session Streaming session
         */
        public VideoPlayerEventListener(IPCallStreamingSession session) {
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
         * Video stream has been resized
         *
         * @param width Video width
         * @param height Video height
         */
        public void mediaResized(int width, int height) {
            if (logger.isActivated()) {
                logger.debug("The size of media has changed " + width + "x" + height);
            }
            // Notify listeners
            if (!isInterrupted()) {
                for (int i = 0; i < getListeners().size(); i++) {
                    ((IPCallStreamingSessionListener) getListeners().get(i))
                            .handleVideoResized(width, height);
                }
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

            // Close the audio and video session
            closeMediaSession();

            // Terminate session
            terminateSession(ImsServiceSession.TERMINATION_BY_SYSTEM);

            // Remove the current session
            getImsService().removeSession(session);

            // Notify listeners
            if (!isInterrupted()) {
                for(int i=0; i < getListeners().size(); i++) {
                    ((IPCallStreamingSessionListener)getListeners().get(i)).handleCallError(new IPCallError(IPCallError.VIDEO_STREAMING_FAILED, error));
                }
            }

            // Request capabilities to the remote
            getImsService().getImsModule().getCapabilityService().requestContactCapabilities(getDialogPath().getRemoteParty());
        }
    }
}
