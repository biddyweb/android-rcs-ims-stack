package com.orangelabs.rcs.core.ims.service.ipcall;

import java.util.Vector;

import android.os.RemoteException;

import com.orangelabs.rcs.core.content.ContentManager;
import com.orangelabs.rcs.core.content.LiveVideoContent;
import com.orangelabs.rcs.core.ims.network.sip.SipUtils;
import com.orangelabs.rcs.core.ims.protocol.sdp.MediaDescription;
import com.orangelabs.rcs.core.ims.protocol.sdp.SdpParser;
import com.orangelabs.rcs.core.ims.protocol.sdp.SdpUtils;
import com.orangelabs.rcs.core.ims.protocol.sip.SipRequest;
import com.orangelabs.rcs.core.ims.service.richcall.video.VideoCodecManager;
import com.orangelabs.rcs.core.ims.service.richcall.video.VideoSdpBuilder;
import com.orangelabs.rcs.service.api.client.media.IVideoPlayer;
import com.orangelabs.rcs.service.api.client.media.IVideoRenderer;
import com.orangelabs.rcs.service.api.client.media.video.VideoCodec;
import com.orangelabs.rcs.utils.CodecsUtils;


public class RemoteAddVideo extends AddVideoManager {
	
	/**
	 * Constructor
	 */
	public RemoteAddVideo(IPCallStreamingSession session) {
		super(session);
	}

	
	/**
	 * Add Video
	 * 
	 * @param reInvite  reInvite Request received
	 */
	public LiveVideoContent addVideo(SipRequest reInvite) {
		if (logger.isActivated()) {
			logger.info("addVideo() - RemoteAddVideo");
			logger.info("video status =" + session.getVideoContent());
		}
		synchronized (this) {
			//set AddVideoManager state
			state = AddVideoManager.ADD_VIDEO_INPROGRESS;

			// create Video Content and set it on session
			LiveVideoContent videocontent = ContentManager
					.createLiveVideoContentFromSdp(reInvite.getContentBytes());
			session.setVideoContent(videocontent);

			// processes user Answer and SIP response
			session.getUpdateSessionManager().waitUserAckAndSendReInviteResp(
					reInvite, IPCallService.FEATURE_TAGS_IP_VIDEO_CALL,
					IPCallStreamingSession.ADD_VIDEO);

			return videocontent;
		}
	}

	

	/**
	 * Remove Video
	 * 
	 * @param reInvite  reInvite Request received
	 */
	public void removeVideo(SipRequest reInvite) {
		if (logger.isActivated()) {
			logger.info("removeVideo() - RemoteAddVideo");
			logger.info("video status =" + session.getVideoContent());
		}
		synchronized (this) {
				//set AddVideoManager state
				state = AddVideoManager.REMOVE_VIDEO_INPROGRESS;
				
				// build sdp response
				String sdp = buildRemoveVideoSdpResponse();

				// set sdp response as local content
				session.getDialogPath().setLocalContent(sdp);

				// process user Answer and SIP response
				session.getUpdateSessionManager().send200OkReInviteResp(
						reInvite, IPCallService.FEATURE_TAGS_IP_VOICE_CALL,
						sdp, IPCallStreamingSession.REMOVE_VIDEO);

		}
	}
			
	
	
	/**
	 * Build sdp response for addVideo
	 * 
	 * @param reInvite  reInvite Request received
	 * @return sdp content
	 */
	public String buildAddVideoSdpResponse(SipRequest reInvite) {
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
			if ((session.getVideoPlayer() == null) || (session.getVideoPlayer().getVideoCodec() == null)) {
				session.handleError(new IPCallError(IPCallError.UNSUPPORTED_VIDEO_TYPE,
						"Video player null or Video codec not selected"));
			} else
			if ((session.getVideoRenderer() == null) || (session.getVideoRenderer().getVideoCodec() == null)) {
				session.handleError(new IPCallError(IPCallError.UNSUPPORTED_VIDEO_TYPE,
						"Video renderer null or Video codec not selected"));
			} else {
				// Codec negotiation
				VideoCodec selectedVideoCodec = VideoCodecManager.negociateVideoCodec(session.getVideoRenderer().getSupportedVideoCodecs(),
						proposedVideoCodecs);

				if (selectedVideoCodec == null) {
					// Support of video codec of profile 1B is compulsory. Even if not proposed explicitly it shall be selected.
					selectedVideoCodec = CodecsUtils.getVideoCodecProfile1b(session.getVideoRenderer().getSupportedVideoCodecs());
					if (selectedVideoCodec != null) {
						if (logger.isActivated())
							logger.info("Video codec profile 1B is selected by default");
					}
				}
				if (selectedVideoCodec == null) {
					if (logger.isActivated()) {
						logger.debug("Proposed codecs are not supported");
					}

					// Send a 415 Unsupported media type response
					session.send415Error(reInvite);

					// Unsupported media type
					session.handleError(new IPCallError(IPCallError.UNSUPPORTED_VIDEO_TYPE));
				} else {
					// Build SDP part for response
					String ntpTime = SipUtils.constructNTPtime(System.currentTimeMillis());
					String ipAddress = session.getDialogPath().getSipStack().getLocalIpAddress();
					String videoSdp = VideoSdpBuilder.buildSdpAnswer(
							selectedVideoCodec.getMediaCodec(),
							session.getVideoRenderer().getLocalRtpPort(), mediaVideo);
					String audioSdp = AudioSdpBuilder.buildSdpAnswer(
							session.getAudioPlayer().getAudioCodec(),
							session.getAudioRenderer().getLocalRtpPort());
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
			session.handleError(new IPCallError(IPCallError.UNEXPECTED_EXCEPTION,
					e.getMessage()));
		}

		return sdp;
	}
	
	
	/**
	 * Build sdp response for removeVideo
	 * 
	 * @return sdp content
	 */
	private String buildRemoveVideoSdpResponse() {
		if (logger.isActivated()) {
			logger.info("buildRemoveVideoSdpResponse()");
		}

		// Build SDP part
		String sdp = "";
		String ntpTime = SipUtils.constructNTPtime(System.currentTimeMillis());
		String ipAddress = session.getDialogPath().getSipStack().getLocalIpAddress();

		
		try {
			logger.warn("Build audio sdp");
			session.getAudioPlayer().getLocalRtpPort();
			String audioSdp = AudioSdpBuilder.buildSdpAnswer(session.getAudioPlayer()
					.getAudioCodec(), session.getAudioPlayer().getLocalRtpPort());			
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
			session.handleError(new IPCallError(IPCallError.UNEXPECTED_EXCEPTION,
					e.getMessage()));
		}
		return sdp;
	}
	
	
	public void addVideo(IVideoPlayer videoPlayer, IVideoRenderer videoRenderer) {
		// Not used in Remote Add Video Manager		
	}


	public void removeVideo() {
		// Not used in Remote Add Video Manager		
	}


	
	
}
