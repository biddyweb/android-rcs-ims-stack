package com.orangelabs.rcs.core.ims.service.ipcall;

import android.os.RemoteException;

import com.orangelabs.rcs.core.ims.network.sip.SipUtils;
import com.orangelabs.rcs.core.ims.protocol.sdp.SdpUtils;
import com.orangelabs.rcs.core.ims.protocol.sip.SipRequest;
import com.orangelabs.rcs.core.ims.service.richcall.video.VideoSdpBuilder;



public class IPCall_RemoteHoldInactive extends IPCall_Hold {

	public IPCall_RemoteHoldInactive(IPCallStreamingSession session){
		super(session);
	}
	
	@Override
	public void setCallHold(boolean callHoldAction, SipRequest reInvite) {
		
		// build sdp response
		String sdp = buildCallHoldSdpResponse(callHoldAction);
		
		// set sdp response as local content
		session.getDialogPath().setLocalContent(sdp);

		//get feature tags 
				String[] featureTags = null;
				if (session.isTagPresent(reInvite.getContent(), "m=video")) { // audio+ video
					featureTags = IPCallService.FEATURE_TAGS_IP_VIDEO_CALL;	
				} else {  // audio only
					featureTags = IPCallService.FEATURE_TAGS_IP_VOICE_CALL; }
		
		int requestType = (callHoldAction)? IPCallStreamingSession.SET_ON_HOLD : IPCallStreamingSession.SET_ON_RESUME ;		
		
		// process user Answer and SIP response
		session.getUpdateSessionManager().send200OkReInviteResp(reInvite,
				featureTags,
				sdp,
				requestType);

	}
	
	
	private String buildCallHoldSdpResponse(boolean action){
		try {
			// Build SDP part
			String ntpTime = SipUtils.constructNTPtime(System.currentTimeMillis());
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
	            	audioSdp  +
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

	
	@Override
	public void prepareSession() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void setCallHold(boolean callHoldAction) {
		// not used in IPCall-RemoteHoldInactive class
		
	}

	

}
 