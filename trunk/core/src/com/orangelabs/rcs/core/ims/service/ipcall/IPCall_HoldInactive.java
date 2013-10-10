package com.orangelabs.rcs.core.ims.service.ipcall;

import android.os.RemoteException;

import com.orangelabs.rcs.core.ims.network.sip.SipUtils;
import com.orangelabs.rcs.core.ims.protocol.sdp.SdpUtils;
import com.orangelabs.rcs.core.ims.protocol.sip.SipRequest;
import com.orangelabs.rcs.core.ims.service.richcall.video.VideoSdpBuilder;
import com.orangelabs.rcs.utils.logger.Logger;

public class IPCall_HoldInactive extends IPCall_Hold {

	public IPCall_HoldInactive(IPCallStreamingSession session){
		super(session);
	}
	
	@Override
	public void setCallHold(boolean callHoldAction) {
		// Build SDP
		String sdp = buildCallHoldSdpProposal(callHoldAction);

		// Set SDP proposal as the local SDP part in the dialog path
		session.getDialogPath().setLocalContent(sdp);
		
		//get feature tags 
		String[] featureTags = null;
		if (session.isTagPresent(sdp, "m=video")) { // audio+ video
			featureTags = IPCallService.FEATURE_TAGS_IP_VIDEO_CALL;	
		} else {  // audio only
			featureTags = IPCallService.FEATURE_TAGS_IP_VOICE_CALL; }

		// Create re-INVITE
		SipRequest reInvite = session.getUpdateSessionManager().createReInvite(
							featureTags, sdp);

		// Send re-INVITE
		int requestType = (callHoldAction)? IPCallStreamingSession.SET_ON_HOLD : IPCallStreamingSession.SET_ON_RESUME ;		
		session.getUpdateSessionManager().sendReInvite(reInvite,
				requestType);

	}
	
	private String buildCallHoldSdpProposal(boolean action){
		try {
			// Build SDP part
			String ntpTime = SipUtils.constructNTPtime(System.currentTimeMillis());
			if (logger.isActivated()){
				logger.info("session ="+session);
			}
			String ipAddress = session.getDialogPath().getSipStack().getLocalIpAddress();			
			String  aVar = (action)? "a=inactive" : "a=sendrcv" ;
			
			String audioSdp = AudioSdpBuilder.buildSdpOffer(session.getAudioPlayer().getSupportedAudioCodecs(), 
					session.getAudioPlayer().getLocalRtpPort())+ aVar + SipUtils.CRLF;

			
			
			String videoSdp = "";
	        if ((session.getVideoContent()!= null)&&(session.getVideoPlayer()!= null)&&(session.getVideoRenderer()!= null)) {	        	
					videoSdp = VideoSdpBuilder.buildSdpOfferWithOrientation(
							session.getVideoPlayer().getSupportedVideoCodecs(),
							session.getVideoRenderer().getLocalRtpPort())+ aVar + SipUtils.CRLF;		
	        }
			
			
	        
	        String  sdp =
	            	"v=0" + SipUtils.CRLF +
	            	"o=- " + ntpTime + " " + ntpTime + " " + SdpUtils.formatAddressType(ipAddress) + SipUtils.CRLF +
	            	"s=-" + SipUtils.CRLF +
	            	"c=" + SdpUtils.formatAddressType(ipAddress) + SipUtils.CRLF +
	            	"t=0 0" + SipUtils.CRLF +
	            	audioSdp + 
	            	videoSdp ;

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
	
	public void prepareSession() {
		
	}

	@Override
	public void setCallHold(boolean callHoldAction, SipRequest reInvite) {
		// Not used in IPCall_HoldInactive class
		
	}

}
