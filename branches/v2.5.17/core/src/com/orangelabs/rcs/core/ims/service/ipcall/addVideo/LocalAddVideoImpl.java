package com.orangelabs.rcs.core.ims.service.ipcall.addVideo;


import android.os.RemoteException;

import com.orangelabs.rcs.core.content.ContentManager;
import com.orangelabs.rcs.core.content.LiveVideoContent;
import com.orangelabs.rcs.core.ims.network.sip.SipUtils;
import com.orangelabs.rcs.core.ims.protocol.sdp.SdpUtils;
import com.orangelabs.rcs.core.ims.protocol.sip.SipRequest;
import com.orangelabs.rcs.core.ims.service.ipcall.AudioSdpBuilder;
import com.orangelabs.rcs.core.ims.service.ipcall.IPCallError;
import com.orangelabs.rcs.core.ims.service.ipcall.IPCallService;
import com.orangelabs.rcs.core.ims.service.ipcall.IPCallStreamingSession;
import com.orangelabs.rcs.core.ims.service.richcall.video.VideoSdpBuilder;
import com.orangelabs.rcs.service.api.client.media.IVideoPlayer;
import com.orangelabs.rcs.service.api.client.media.IVideoRenderer;

public class LocalAddVideoImpl extends AddVideoImpl {

	/**
	 * Constructor
	 */
	public LocalAddVideoImpl(IPCallStreamingSession session, AddVideoManager addVideoMngr) {
		super(session, addVideoMngr);
		logger.debug("LocalAddVideo() - addVideoMngr ="+addVideoMngr);
	}
	
	
	/**
	 * Add video to the session
	 *  
	 * @param videoPlayer video player instance
	 * @param videoRenderer video renderer instance
	 * @throws Exception 
	 */
	public void addVideo(IVideoPlayer videoPlayer, IVideoRenderer videoRenderer) throws Exception  {
		if (logger.isActivated()) {
			logger.info("addVideo() - LocalAddVideo");
		}
		synchronized (this) {
//			AddVideoManager.state = AddVideoManager.ADD_VIDEO_INPROGRESS;

			// Set video player/render
			session.setVideoRenderer(videoRenderer);
			session.setVideoPlayer(videoPlayer);

			// create and set live Video Content
			LiveVideoContent liveVideoContent = (videoPlayer == null) ? null
					: ContentManager.createGenericLiveVideoContent();
			session.setVideoContent(liveVideoContent);

			// Build SDP
			String sdp = buildAddVideoSdpProposal();
			
			if (sdp != null) {
				// Set SDP proposal as the local SDP part in the dialog path
				session.getDialogPath().setLocalContent(sdp);

				// Create re-INVITE
				SipRequest reInvite = session.getUpdateSessionManager()
						.createReInvite(IPCallService.FEATURE_TAGS_IP_VIDEO_CALL,
								sdp);

				// set "P-Preferred-service" header on reInvite request
				SipUtils.setPPreferredService(reInvite, IPCallService.P_PREFERRED_SERVICE_HEADER);

				
				// Send re-INVITE
				session.getUpdateSessionManager().sendReInvite(reInvite,
						addVideoMngr);
			}

			
		}
	}
	
	/**
	 * Remove video from the current call
	 * @throws Exception 
	 */
	public void removeVideo() throws Exception {
		if (logger.isActivated()) {
			logger.info("removeVideo() - LocalAddVideo");
			logger.info("video status =" + session.getVideoContent());
		}

		synchronized (this) {
//			state = AddVideoManager.REMOVE_VIDEO_INPROGRESS;

			// Build SDP
			String sdp = buildRemoveVideoSdpProposal();

			if (sdp != null) {
				// Set the SDP proposal as local SDP content in the dialog path
				session.getDialogPath().setLocalContent(sdp);

				// Create re-INVITE
				SipRequest reInvite = session.getUpdateSessionManager()
						.createReInvite(IPCallService.FEATURE_TAGS_IP_VOICE_CALL,
								sdp);
				
				// set "P-Preferred-service" header on reInvite request
				SipUtils.setPPreferredService(reInvite, IPCallService.P_PREFERRED_SERVICE_HEADER);

				// Send re-INVITE
				session.getUpdateSessionManager().sendReInvite(reInvite,
						addVideoMngr);
			}			
		}
	}
	
	
	/**
	 * Build SDP proposal for audio+ video session (call init or addVideo)
	 * 
	 * @return String (SDP content) or null in case of error
	 */
	private String buildAddVideoSdpProposal() {
		if (logger.isActivated()) {
			logger.debug("Build SDP proposal to add video stream in the session");
		}

		try {
			// Build SDP part
			String ntpTime = SipUtils.constructNTPtime(System.currentTimeMillis());
			String ipAddress = session.getDialogPath().getSipStack().getLocalIpAddress();
			
			String audioSdp = AudioSdpBuilder.buildSdpOffer(session.getAudioPlayer().getSupportedAudioCodecs(), 
					session.getAudioPlayer().getLocalRtpPort());
			
			String videoSdp = "";
	        if ((session.getVideoContent()!= null)&&(session.getVideoPlayer()!= null)&&(session.getVideoRenderer()!= null)) {	        	
					videoSdp = VideoSdpBuilder.buildSdpOfferWithOrientation(
							session.getVideoPlayer().getSupportedVideoCodecs(),
							session.getVideoRenderer().getLocalRtpPort());		
	        }
			
	        String  sdp =
	            	"v=0" + SipUtils.CRLF +
	            	"o=- " + ntpTime + " " + ntpTime + " " + SdpUtils.formatAddressType(ipAddress) + SipUtils.CRLF +
	            	"s=-" + SipUtils.CRLF +
	            	"c=" + SdpUtils.formatAddressType(ipAddress) + SipUtils.CRLF +
	            	"t=0 0" + SipUtils.CRLF +
	            	audioSdp + "a=sendrcv" + SipUtils.CRLF +
	            	videoSdp + "a=sendrcv" + SipUtils.CRLF;

	        	return sdp;

		} catch (RemoteException e) {
			if (logger.isActivated()) {
				logger.error("Add video has failed", e);
			}

			// Unexpected error
			session.handleError(new IPCallError(IPCallError.UNEXPECTED_EXCEPTION, e.getMessage()));
			return null;
		}
	}

	
	/**
	 * Build SDP proposal to remove video stream from the session
	 * 
	 * @return String (SDP content) or null in case of error
	 */
	private String buildRemoveVideoSdpProposal() {
		if (logger.isActivated()) {
			logger.debug("Build SDP proposal to remove video stream from the session");
		}

		try {
			// Build SDP part
			String ntpTime = SipUtils.constructNTPtime(System.currentTimeMillis());
			String ipAddress = session.getDialogPath().getSipStack().getLocalIpAddress();

			session.getAudioPlayer().getLocalRtpPort();			
			String audioSdp = AudioSdpBuilder.buildSdpOffer(session.getAudioPlayer().getSupportedAudioCodecs(), session.getAudioPlayer().getLocalRtpPort());
			
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
			session.handleError(new IPCallError(IPCallError.UNEXPECTED_EXCEPTION, e.getMessage()));
			return null;
		}
	}
	



	@Override
	public LiveVideoContent addVideo(SipRequest reInvite) {
		return null;
		// Not used in Local Add Video Manager
	}


	@Override
	public void removeVideo(SipRequest reInvite) {
		// Not used in Local Add Video Manager		
	}


	@Override
	public String buildAddVideoSdpResponse(SipRequest reInvite) {
		// Not used in Local Add Video Manager		
		return null;
	}
}
		
		
