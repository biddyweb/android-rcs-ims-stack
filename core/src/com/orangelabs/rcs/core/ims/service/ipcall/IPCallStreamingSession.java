package com.orangelabs.rcs.core.ims.service.ipcall;

import java.util.Vector;

import javax2.sip.Dialog;

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
import com.orangelabs.rcs.core.ims.service.richcall.ContentSharingError;
import com.orangelabs.rcs.core.ims.service.richcall.video.VideoCodecManager;
import com.orangelabs.rcs.core.ims.service.richcall.video.VideoSdpBuilder;
import com.orangelabs.rcs.core.ims.service.richcall.video.VideoStreamingSessionListener;
import com.orangelabs.rcs.service.api.client.media.IAudioEventListener;
import com.orangelabs.rcs.service.api.client.media.IAudioPlayer;
import com.orangelabs.rcs.service.api.client.media.IAudioRenderer;
import com.orangelabs.rcs.service.api.client.media.IMediaPlayer;
import com.orangelabs.rcs.service.api.client.media.IMediaRenderer;
import com.orangelabs.rcs.service.api.client.media.video.VideoCodec;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * IP call session
 * 
 * @author opob7414
 */
public abstract class IPCallStreamingSession extends ImsServiceSession {

	/**
	 * Session Update Request Type
	 */
	public final static int ADD_VIDEO = 0;
	public final static int REMOVE_VIDEO = 1;
	public final static int SET_ON_HOLD = 2;
	public final static int SET_ON_RESUME = 3;

	/**
	 * The logger
	 */
	private Logger logger = Logger.getLogger(this.getClass().getName());

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
	private IMediaRenderer videoRenderer = null;

	/**
	 * Video player
	 */
	private IMediaPlayer videoPlayer = null;

	/**
	 * Update session manager
	 */

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            IMS service
	 * @param videoContent
	 *            VideoContent
	 * @param audioContent
	 *            AudioContent
	 * @param contact
	 *            Remote contact
	 */
	public IPCallStreamingSession(ImsService imsService,
			LiveVideoContent videoContent, LiveAudioContent audioContent,
			String contact) {
		super(imsService, contact);

		this.videoContent = videoContent;

		this.audioContent = audioContent;
	}

	/**
	 * Returns the video content
	 * 
	 * @return videoContent
	 */
	public LiveVideoContent getVideoContent() {
		return videoContent;
	}

	/**
	 * Set the video content
	 * 
	 * @param videoContent
	 *            LiveVideoContent
	 */
	public void setVideoContent(LiveVideoContent videoContent) {
		this.videoContent = videoContent;
	}

	/**
	 * Returns the audio content
	 * 
	 * @return audioContent
	 */
	public LiveAudioContent getAudioContent() {
		return audioContent;
	}

	/**
	 * Set the audio content
	 * 
	 * @param audioContent
	 *            LiveAudioContent
	 */
	public void setAudioContent(LiveAudioContent audioContent) {
		this.audioContent = audioContent;
	}

	/**
	 * Get the audio renderer
	 * 
	 * @return AudioRenderer
	 */
	public IAudioRenderer getAudioRenderer() {
		return audioRenderer;
	}

	/**
	 * Set the audio renderer
	 * 
	 * @param audioRenderer
	 *            AudioRenderer
	 */
	public void setAudioRenderer(IAudioRenderer audioRenderer) {
		this.audioRenderer = audioRenderer;
	}

	/**
	 * Get the audio player
	 * 
	 * @return AudioPlayer
	 */
	public IAudioPlayer getAudioPlayer() {
		return audioPlayer;
	}

	/**
	 * Set the video player
	 * 
	 * @param IMediaPlayer
	 */
	public void setAudioPlayer(IAudioPlayer audioPlayer) {
		this.audioPlayer = audioPlayer;
	}

	/**
	 * Get the video renderer
	 * 
	 * @return MediaRenderer
	 */
	public IMediaRenderer getVideoRenderer() {
		return videoRenderer;
	}

	/**
	 * Set the video renderer
	 * 
	 * @param videoRenderer
	 *            MediaRenderer
	 */
	public void setVideoRenderer(IMediaRenderer videoRenderer) {
		this.videoRenderer = videoRenderer;
	}

	/**
	 * Get the video player
	 * 
	 * @return MediaPlayer
	 */
	public IMediaPlayer getVideoPlayer() {
		return videoPlayer;
	}

	/**
	 * Set the video player
	 * 
	 * @param IMediaPlayer
	 */
	public void setVideoPlayer(IMediaPlayer videoPlayer) {
		this.videoPlayer = videoPlayer;
	}

	/**
	 * Receive BYE request
	 * 
	 * @param bye
	 *            BYE request
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
	 * Add video in the current call
	 * 
	 * @param videoPlayer Video player
	 * @param videoRenderer Video renderer
	 */
	public void addVideo(IMediaPlayer videoPlayer, IMediaRenderer videoRenderer) {
		if (logger.isActivated()) {
			logger.info("addVideo");
		}

		// set video player/render
		setVideoPlayer(videoPlayer);
		setVideoRenderer(videoRenderer);

		// build sdp
		String sdp = buildAddVideoSdpProposal();

		// Set sdp proposal as the local SDP part in the dialog path
		getDialogPath().setLocalContent(sdp);

		// create reInvite
		SipRequest reInvite = getUpdateSessionManager().createReInvite(
				IPCallService.FEATURE_TAGS_IP_AUDIOVIDEO_CALL, sdp);

		// send reInvite
		getUpdateSessionManager().sendReInvite(reInvite,
				IPCallStreamingSession.ADD_VIDEO);
	}

	/**
	 * Remove video from the current call
	 */
	public void removeVideo() {
		if (logger.isActivated()) {
			logger.info("removeVideo");
		}

		setVideoPlayer(null);
		setVideoRenderer(null);

		// build sdp
		String sdp = buildRemoveVideoSdp();

		// Set the sdp proposal as local SDP content in the dialog path
		getDialogPath().setLocalContent(sdp);

		// create reInvite
		SipRequest reInvite = getUpdateSessionManager().createReInvite(
				IPCallService.FEATURE_TAGS_IP_VOICE_CALL, sdp);

		// send reInvite
		getUpdateSessionManager().sendReInvite(reInvite,
				IPCallStreamingSession.REMOVE_VIDEO);
	}

	/**
	 * Test a tag is present or not in SIP message
	 * 
	 * @param message
	 *            Message or message part
	 * @param tag
	 *            Tag to be searched
	 * @return Boolean
	 */
	private boolean isTagPresent(String message, String tag) {
		if ((message != null) && (tag != null)
				&& (message.toLowerCase().indexOf(tag) != -1)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Build sdp proposal for addVideo
	 * @return sdp content
	 */
	private String buildAddVideoSdpProposal() {
		if (logger.isActivated()) {
			logger.info("buildAddVideoSdpProposal()");
		}

		String sdp = "";
		try {
			if ((getVideoPlayer() == null)
					|| (getVideoPlayer().getMediaCodec() == null)) {
				handleError(new IPCallError(IPCallError.UNSUPPORTED_VIDEO_TYPE,
						"Video codec not selected"));
			} else {
				// Build SDP part
				String ntpTime = SipUtils.constructNTPtime(System
						.currentTimeMillis());
				String ipAddress = getDialogPath().getSipStack()
						.getLocalIpAddress();
				String videoSdp;

				videoSdp = VideoSdpBuilder.buildSdpWithOrientationExtension(
						getVideoPlayer().getSupportedMediaCodecs(),
						getVideoPlayer().getLocalRtpPort());
				logger.warn("Build audio sdp");
				getAudioPlayer().getLocalRtpPort();
				String audioSdp = AudioSdpBuilder.buildResponseSdp(
						getAudioPlayer().getAudioCodec(), getAudioPlayer()
								.getLocalRtpPort());
				sdp = "v=0" + SipUtils.CRLF + "o=- " + ntpTime + " " + ntpTime
						+ " " + SdpUtils.formatAddressType(ipAddress)
						+ SipUtils.CRLF + "s=-" + SipUtils.CRLF + "c="
						+ SdpUtils.formatAddressType(ipAddress) + SipUtils.CRLF
						+ "t=0 0" + SipUtils.CRLF + audioSdp + "a=sendrcv"
						+ SipUtils.CRLF + videoSdp + "a=sendrcv"
						+ SipUtils.CRLF;

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
	 * Build sdp proposal/response for removeVideo
	 * @return sdp content
	 */
	private String buildRemoveVideoSdp() {
		if (logger.isActivated()) {
			logger.info("buildRemoveVideoSdp()");
		}

		// Build SDP part
		String sdp = "";
		String ntpTime = SipUtils.constructNTPtime(System.currentTimeMillis());
		String ipAddress = getDialogPath().getSipStack().getLocalIpAddress();

		logger.warn("Build audio sdp");
		try {
			getAudioPlayer().getLocalRtpPort();
			String audioSdp = AudioSdpBuilder.buildResponseSdp(getAudioPlayer()
					.getAudioCodec(), getAudioPlayer().getLocalRtpPort());
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
	 * select sdp builder method (to build sdp response) according to serviceContext - build and return sdp
	 * 
	 * @param ReInvite
	 *            reInvite received request
	 * @param serviceContext
	 *            context of service (Add Video, Remove Video ...)
	 * @return sdp built by builder
	 */
	public String buildReInviteSdpResponse(SipRequest ReInvite,
			int serviceContext) {

		String localSdp = "";

		switch (serviceContext) {
			case (IPCallStreamingSession.ADD_VIDEO): {
				localSdp = buildAddVideoSdpResponse(ReInvite);
				break;
			}
			case (IPCallStreamingSession.REMOVE_VIDEO): {
				localSdp = buildRemoveVideoSdp(); // for remove Video: same sdp used for response as the one used for proposal 
				break;
			}
		}
		// Set the local SDP part in the dialog path
		getDialogPath().setLocalContent(localSdp);

		return localSdp;
	}

	/**
	 * Build sdp response for addVideo
	 * 
	 * @param reInvite  reInvite Request received
	 */
	private String buildAddVideoSdpResponse(SipRequest reInvite) {
		if (logger.isActivated()) {
			logger.info("buildReInviteSdpResponse()");
		}
		
		String sdp = "";
		// Parse the remote SDP part
		SdpParser parser = new SdpParser(reInvite.getSdpContent().getBytes());
		MediaDescription mediaVideo = parser.getMediaDescription("video");

		// Extract video codecs from SDP
		Vector<MediaDescription> medias = parser.getMediaDescriptions("video");
		Vector<VideoCodec> proposedVideoCodecs = VideoCodecManager
				.extractVideoCodecsFromSdp(medias);

		// Check that a media renderer has been set      
        if (getVideoRenderer() == null) { 
        	if (logger.isActivated()) {
                logger.debug("Video Renderer Not Initialized");
            }	
                          
            handleError(new IPCallError(IPCallError.VIDEO_RENDERER_NOT_INITIALIZED));
        }
        else {
        	// Codec negotiation
    		VideoCodec selectedVideoCodec = null;
    		try {
    			selectedVideoCodec = VideoCodecManager.negociateVideoCodec(
    					getVideoRenderer().getSupportedMediaCodecs(),
    					proposedVideoCodecs);
    			if (selectedVideoCodec == null) {
    				if (logger.isActivated()) {
    					logger.debug("Proposed codecs are not supported");
    				}

    				// Send a 415 Unsupported media type response
    				send415Error(reInvite);

    				// Unsupported media type
    				handleError(new IPCallError(IPCallError.UNSUPPORTED_VIDEO_TYPE));
    			} else { // Build SDP part for response
    				String ntpTime = SipUtils.constructNTPtime(System
    						.currentTimeMillis());
    				String ipAddress = getDialogPath().getSipStack()
    						.getLocalIpAddress();

    				String videoSdp = VideoSdpBuilder.buildResponseSdp(
    						selectedVideoCodec.getMediaCodec(), getVideoRenderer()
    								.getLocalRtpPort(), mediaVideo);
    				String audioSdp = AudioSdpBuilder.buildResponseSdp(
    						getAudioPlayer().getAudioCodec(), getAudioRenderer()
    								.getLocalRtpPort()); // mediaAudio is not used
    														// here
    				sdp = "v=0" + SipUtils.CRLF + "o=- " + ntpTime + " " + ntpTime
    						+ " " + SdpUtils.formatAddressType(ipAddress)
    						+ SipUtils.CRLF + "s=-" + SipUtils.CRLF + "c="
    						+ SdpUtils.formatAddressType(ipAddress) + SipUtils.CRLF
    						+ "t=0 0" + SipUtils.CRLF + audioSdp + "a=sendrcv"
    						+ SipUtils.CRLF + videoSdp + "a=sendrcv"
    						+ SipUtils.CRLF;

    				// Set the local SDP part in the dialog path
    				getDialogPath().setLocalContent(sdp);

    			}
    		} catch (RemoteException e) {
    			if (logger.isActivated()) {
    				logger.error("Add Video has failed", e);
    			}

    			// Unexpected error
    			handleError(new IPCallError(IPCallError.UNEXPECTED_EXCEPTION,
    					e.getMessage()));
    		}
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

		// Set local CSeq to remote CSeq value
		while (getDialogPath().getCseq() < reInvite.getCSeq()) {
			// Increment the Cseq number of the dialog path
			getDialogPath().incrementCseq();
			if (logger.isActivated()) {
				logger.info("Increment DialogPath CSeq - DialogPath CSeq ="
						+ getDialogPath().getCseq());
			}
		}

		Dialog dlg = getDialogPath().getStackDialog(); 
		// Increment internal stack CSeq (NIST stack issue?)
		while ((dlg != null)
				&& (dlg.getLocalSeqNumber() < getDialogPath().getCseq())) {
			dlg.incrementLocalSequenceNumber();
			if (logger.isActivated()) {
				logger.info("Increment LocalSequenceNumber -  Dialog local Seq Number ="
						+ dlg.getLocalSeqNumber());
			}
		}

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
						IPCallService.FEATURE_TAGS_IP_AUDIOVIDEO_CALL,
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
								.handleRemoveVideoInvitation();
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
				
				// startVideoSession() ;
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
				// close video media session
				closeVideoSession();
				
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
	public void handle200OkReInviteResponse(int code, int requestType){
		if (logger.isActivated()) {
			logger.info("handle200OkReInviteResponse: " + code);
		}

		// case Add video
		if (requestType == IPCallStreamingSession.ADD_VIDEO) {
			if (code == 200) {
				// Notify listeners
				if (!isInterrupted()) {
					for (int i = 0; i < getListeners().size(); i++) {
						((IPCallStreamingSessionListener) getListeners().get(i))
								.handleAddVideoAccepted();
					}
				}
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

			// case Remove Video
		} else if (requestType == IPCallStreamingSession.REMOVE_VIDEO) {
			if (code == 200) {
				// Notify listeners
				if (!isInterrupted()) {
					for (int i = 0; i < getListeners().size(); i++) {
						((IPCallStreamingSessionListener) getListeners().get(i))
								.handleRemoveVideoAccepted();
					}
				}
			} else if (code == ImsServiceSession.INVITATION_NOT_ANSWERED) {
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
	 * Handle 486 Busy
	 * 
	 * @param resp SipResponse
	 *            
	 */
	public void handle486Busy(SipResponse resp) {
		if (logger.isActivated()) {
			logger.info("486 Busy");
		}

		// Close media session
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
					IPCallService.FEATURE_TAGS_IP_AUDIOVIDEO_CALL, content);
		} else if (requestType == IPCallStreamingSession.REMOVE_VIDEO) {
			reInvite = getUpdateSessionManager().createReInvite(
					IPCallService.FEATURE_TAGS_IP_AUDIOVIDEO_CALL, content);
		} else {
			// TODO for set On Hold
			reInvite = getUpdateSessionManager().createReInvite(
					IPCallService.FEATURE_TAGS_IP_AUDIOVIDEO_CALL, content);
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

		// Close media session
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
	 * Prepare video media session (set codec, get remote Host and port ...) 
	 * 
	 */
	public void prepareVideoSession(){
		if (logger.isActivated()) {
			logger.info("prepareVideoSession()");
		}
		// Parse the remote SDP part
        SdpParser parser = new SdpParser(getDialogPath().getRemoteContent().getBytes());
        String remoteHost = SdpUtils.extractRemoteHost(parser.sessionDescription.connectionInfo);
        MediaDescription mediaVideo = parser.getMediaDescription("video");
        int remotePort = mediaVideo.port;
        
        // Extract audio codecs from SDP
        Vector<MediaDescription> medias = parser.getMediaDescriptions("video");
        Vector<VideoCodec> proposedCodecs = VideoCodecManager.extractVideoCodecsFromSdp(medias);

        // Codec negotiation
        VideoCodec selectedVideoCodec;
		try {
			selectedVideoCodec = VideoCodecManager.negociateVideoCodec(getVideoPlayer().getSupportedMediaCodecs(), proposedCodecs);
			if (selectedVideoCodec == null) {
	            if (logger.isActivated()) {
	                logger.debug("Proposed codecs are not supported");
	            }	            	            
	            
	            // Report error
	            handleError(new IPCallError(IPCallError.UNSUPPORTED_VIDEO_TYPE));
	            return;
	        }
	        
			//getContent().setEncoding("video/" + selectedVideoCodec.getCodecName()); => to remove is not used
			
			// Set the selected video codec
			getVideoPlayer().setMediaCodec(selectedVideoCodec.getMediaCodec());
			
			 // Set audio player event listener
	        //getVideoPlayer().addListener(new VideoPlayerEventListener(this));
	        
	        // Open the audio player
			//getAudioPlayer().open(remoteHost, remotePort);
		} catch (RemoteException e) {
			// Report error
			handleError(new IPCallError(IPCallError.UNEXPECTED_EXCEPTION));

		}
        	
	}
	
	
	/**
	 * Prepare video media session (set codec, get remote Host and port ...) 
	 * 
	 */
	public void closeVideoSession(){
		// TODO
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
		 * @param error
		 *            Error
		 */
		public void audioError(String error) {
			if (logger.isActivated()) {
				logger.error("Audio player has failed: " + error);
			}

			// Close the media session
			closeMediaSession();

			// Terminate session
			terminateSession(ImsServiceSession.TERMINATION_BY_SYSTEM);

			// Remove the current session
			getImsService().removeSession(session);

			// Notify listeners
			if (!isInterrupted()) {
				for (int i = 0; i < getListeners().size(); i++) {
					((VideoStreamingSessionListener) getListeners().get(i))
							.handleSharingError(new ContentSharingError(
									ContentSharingError.MEDIA_STREAMING_FAILED,
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

}
