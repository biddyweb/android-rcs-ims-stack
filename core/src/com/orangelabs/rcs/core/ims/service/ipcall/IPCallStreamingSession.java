package com.orangelabs.rcs.core.ims.service.ipcall;
import android.os.RemoteException;

import com.orangelabs.rcs.core.content.LiveAudioContent;
import com.orangelabs.rcs.core.content.LiveVideoContent;
import com.orangelabs.rcs.core.ims.network.sip.SipMessageFactory;
import com.orangelabs.rcs.core.ims.network.sip.SipUtils;
import com.orangelabs.rcs.core.ims.protocol.sip.SipException;
import com.orangelabs.rcs.core.ims.protocol.sip.SipRequest;
import com.orangelabs.rcs.core.ims.protocol.sip.SipResponse;
import com.orangelabs.rcs.core.ims.service.ImsService;
import com.orangelabs.rcs.core.ims.service.ImsServiceError;
import com.orangelabs.rcs.core.ims.service.ImsServiceSession;
import com.orangelabs.rcs.core.ims.service.ImsSessionBasedServiceError;
import com.orangelabs.rcs.core.ims.service.UpdateSessionManagerListener;
import com.orangelabs.rcs.core.ims.service.ipcall.addVideo.AddVideoManager;
import com.orangelabs.rcs.core.ims.service.ipcall.hold.HoldManager;
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
	 * Call Hold Manager
	 */
	private HoldManager holdMgr;
	
	/**
	 * Add Video Manager
	 */
	public AddVideoManager addVideoMgr;
	
	/**
	 * Selected Audio Codec
	 */
	public AudioCodec selectedAudioCodec = null;
	
	
	/**
	 * Selected Video Codec
	 */
	public VideoCodec selectedVideoCodec = null;

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
		this.holdMgr = new HoldManager(this) ;
		this.addVideoMgr = new AddVideoManager(this);

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
		
		if (getVideoContent() == null) {
        	// Voice call	
			return SipMessageFactory.createInvite(getDialogPath(), IPCallService.FEATURE_TAGS_IP_VOICE_CALL, getDialogPath().getLocalContent(), IPCallService.P_PREFERRED_SERVICE_HEADER);
        } else {
        	// Video call
        	return SipMessageFactory.createInvite(getDialogPath(), IPCallService.FEATURE_TAGS_IP_VIDEO_CALL, getDialogPath().getLocalContent(), IPCallService.P_PREFERRED_SERVICE_HEADER);
        } 
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

		if (reInvite.getSdpContent() == null) {
			// "Keep Alive" ReInvite
			getSessionTimerManager().receiveReInvite(reInvite);
		} else {// "SessionUpdate" ReInvite		
			String content = reInvite.getSdpContent();
			int requestType = -1;
			
			// Analyze sdp to dispatch according to sdp content
			if (isTagPresent(content, "a=inactive")) {// Set On Hold "Inactive"
				if ((addVideoMgr.getState() == AddVideoManager.stateValue.IDLE)
						&& (holdMgr.getState() == HoldManager.stateValue.IDLE)) {
					requestType = 2;
				}
			} else if (isTagPresent(content, "a=sendrcv")&&(getVideoContent()==null)&&(isTagPresent(content, "m=video"))){
				if ((addVideoMgr.getState()==AddVideoManager.stateValue.IDLE)&&(holdMgr.getState()==HoldManager.stateValue.IDLE)) {
					requestType = 0; // Add Video	
				} 
			} else if  (isTagPresent(content, "a=sendrcv")&&(getVideoContent()!=null)&&(!isTagPresent(content, "m=video"))){
				 if ((addVideoMgr.getState()==AddVideoManager.stateValue.IDLE)&&((holdMgr.getState()==HoldManager.stateValue.IDLE)||(holdMgr.getState()==HoldManager.stateValue.HOLD)||(holdMgr.getState()==HoldManager.stateValue.REMOTE_HOLD))) {
					requestType = 1;// Remove Video
				}
			} else if (isTagPresent(content, "a=sendrcv")) {				
				if ((addVideoMgr.getState()==AddVideoManager.stateValue.IDLE)&&(holdMgr.getState() == HoldManager.stateValue.REMOTE_HOLD)){
					requestType = 5;// Set on Resume
				}			
			} else {
				// send error to remote client
				sendErrorResponse(reInvite, getDialogPath().getLocalTag(), 603);
			}
			
			 if (requestType != -1) {
				 // set received sdp proposal as remote sdp content in dialogPath
				 getDialogPath().setRemoteContent(content);
			 }
					

			switch (requestType) {
			case (-1): {// unable to process request -  send error to remote client
				sendErrorResponse(reInvite, getDialogPath().getLocalTag(), 603);
			}
			
			case (0): { // Case Add Video

				// instantiate Manager and requests addVideo
//				addVideoMgr = new RemoteAddVideo(this);
				LiveVideoContent videocontent = addVideoMgr.addVideo(reInvite);

				// get video Encoding , video Width and video Height
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
								.handleAddVideo(videoEncoding,
										videoWidth, videoHeight);
					}
				}
			}
				break;
			case (1): { // Case Remove Video
				// instantiate Manager and requests removeVideo
//				addVideoMgr = new RemoteAddVideo(this);				
				addVideoMgr.removeVideo(reInvite);

				// Notify listeners
				if (!isInterrupted()) {
					for (int i = 0; i < getListeners().size(); i++) {
						((IPCallStreamingSessionListener) getListeners().get(i))
								.handleRemoveVideo();
					}
				}

			}
				break;
			case (2): { // Case Set "Inactive" On Hold
				// launch callHold
				holdMgr.setCallHold(true, reInvite);

				// Notify listeners
				if (!isInterrupted()) {
					for (int i = 0; i < getListeners().size(); i++) {
						((IPCallStreamingSessionListener) getListeners().get(i))
								.handleCallHold();
					}
				}
			}
				break;
			case (5): { // Case Set On Resume
				// launch callHold
				holdMgr.setCallHold(false, reInvite);

				if (!isInterrupted()) {
					for (int i = 0; i < getListeners().size(); i++) {
						((IPCallStreamingSessionListener) getListeners().get(i))
								.handleCallResume();
					}
				}
			}
				break;
			}
		}
	}

	
	/**
	 * Add video in the current call
	 * 
	 * @param videoPlayer Video player
	 * @param videoRenderer Video renderer
	 * @throws Exception 
	 */
	public void addVideo(IVideoPlayer videoPlayer, IVideoRenderer videoRenderer)  {
		if (logger.isActivated()) {
			logger.info("Add video");
		}

		if ((getVideoContent()== null)&&(addVideoMgr.getState() == AddVideoManager.stateValue.IDLE)&&(holdMgr.getState() == HoldManager.stateValue.IDLE)) {			
//			addVideoMgr = new LocalAddVideo(this);

			// launch addVideo
			try {
				addVideoMgr.addVideo(videoPlayer, videoRenderer);
			} catch (Exception e) {
				if (logger.isActivated()) {
	                logger.error("Add Video has failed", e);
	            }
	            
				if (!isInterrupted()) {
					for (int i = 0; i < getListeners().size(); i++) {
						((IPCallStreamingSessionListener) getListeners().get(i))
								.handleAddVideoAborted(IPCallError.UNEXPECTED_EXCEPTION);
					}
				}
	        }
		} else {
			if (!isInterrupted()) {
				for (int i = 0; i < getListeners().size(); i++) {
					((IPCallStreamingSessionListener) getListeners().get(i))
							.handleAddVideoAborted(IPCallError.INVALID_COMMAND);
				}
			}
		}
	}
		

	
	/**
	 * Remove video from the current call
	 */
	public void removeVideo() {
		if (logger.isActivated()) {
			logger.info("Remove video");
			logger.info("addVideoMgr.getState() ="+addVideoMgr.getState());
			logger.info("getVideoContent() ="+getVideoContent());
		}
		
		if ((getVideoContent()!= null)
				&&(addVideoMgr.getState()== AddVideoManager.stateValue.IDLE)
				&&((holdMgr.getState() == HoldManager.stateValue.IDLE)||(holdMgr.getState() == HoldManager.stateValue.HOLD)
				||(holdMgr.getState() == HoldManager.stateValue.REMOTE_HOLD))){
			// instantiate Add Video Manager and requests add video
//			addVideoMgr = new LocalAddVideo(this);

			// launch removeVideo
			try {
				addVideoMgr.removeVideo();
			} catch (Exception e) {
				if (logger.isActivated()) {
	                logger.error("Remove Video has failed", e);
	            }
	            
				if (!isInterrupted()) {
					for (int i = 0; i < getListeners().size(); i++) {
						((IPCallStreamingSessionListener) getListeners().get(i))
								.handleRemoveVideoAborted(IPCallError.UNEXPECTED_EXCEPTION);
					}
				}
			}
		}
		else {
			if (!isInterrupted()) {
				for (int i = 0; i < getListeners().size(); i++) {
					((IPCallStreamingSessionListener) getListeners().get(i))
							.handleRemoveVideoAborted(IPCallError.INVALID_COMMAND);
				}
			}
		}
	}

	
	/**
	 * Set On Hold/on Resume the current call
	 * 
	 * @param callHoldAction boolean defining call hold action 
	 */
	public void setOnHold(boolean callHoldAction){
		if (logger.isActivated()) {
			logger.info("setOnHold");
		}
		
		if ((addVideoMgr.getState()==AddVideoManager.stateValue.IDLE) 
				&& (((callHoldAction)&&(holdMgr.getState()==HoldManager.stateValue.IDLE))
				||((!callHoldAction)&&(holdMgr.getState() == HoldManager.stateValue.HOLD)))){
			
			// launch callHold/callResume (depending on boolean value)
			try {
				holdMgr.setCallHold(callHoldAction);
			} catch (Exception e) {
				if (!isInterrupted()) {
					for (int i = 0; i < getListeners().size(); i++) {
						if (callHoldAction){
							((IPCallStreamingSessionListener) getListeners().get(i))
							.handleCallHoldAborted(IPCallError.UNEXPECTED_EXCEPTION);
						}
						else {
							((IPCallStreamingSessionListener) getListeners().get(i))
							.handleCallResumeAborted(IPCallError.UNEXPECTED_EXCEPTION);
						}					
					}
				}
			}	
		}
		else {
			if (!isInterrupted()) {
				for (int i = 0; i < getListeners().size(); i++) {
					if (callHoldAction){
						((IPCallStreamingSessionListener) getListeners().get(i))
						.handleCallHoldAborted(IPCallError.INVALID_COMMAND);
					}
					else {
						((IPCallStreamingSessionListener) getListeners().get(i))
						.handleCallResumeAborted(IPCallError.INVALID_COMMAND);
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
	public void handleReInvite407ProxyAuthent(SipResponse response,	UpdateSessionManagerListener sessionManagerObj) {
		// Set the remote tag
		getDialogPath().setRemoteTag(response.getToTag());

		// Update the authentication agent
		getAuthenticationAgent().readProxyAuthenticateHeader(response);
		
		//create reInvite
		SipRequest reInvite = null;		
		if (getVideoContent() == null) {
        	// Voice call
			reInvite = getUpdateSessionManager().createReInvite(
					IPCallService.FEATURE_TAGS_IP_VOICE_CALL, getDialogPath().getLocalContent());
        } else {
        	// Video call
        	reInvite = getUpdateSessionManager().createReInvite(
					IPCallService.FEATURE_TAGS_IP_VIDEO_CALL, getDialogPath().getLocalContent());
        } 

		// set "P-Preferred-service" header on reInvite request
		try {
			SipUtils.setPPreferredService(reInvite, IPCallService.P_PREFERRED_SERVICE_HEADER);
		} catch (Exception e) {
			if (logger.isActivated()) {
				logger.error("Send ReInvite has failed", e);
			}
			handleError(new ImsSessionBasedServiceError(
					ImsSessionBasedServiceError.UNEXPECTED_EXCEPTION, e
							.getMessage()));
		}
		
		// retry to send reInvite request
		getUpdateSessionManager().sendReInvite(reInvite, sessionManagerObj);
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
	 * Is tag present in SDP
	 * 
	 * @param sdp SDP
	 * @param tag Tag to be searched
	 * @return Boolean
	 */
	public boolean isTagPresent(String sdp, String tag) {
		if ((sdp != null) && (sdp.toLowerCase().indexOf(tag) != -1)) {
			return true;
		} else {
			return false;
		}
	}
	
	//******************************************************************************
	//******************************************************************************
	//******************      Media Session Management Methods      ****************
	//******************************************************************************
	//******************************************************************************

	/**
     * Close media session
     */
	public void closeMediaSession() {
		if (logger.isActivated()) {
			logger.info("Close media session");
		}
//
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
	

	//******************************************************************************
	//******************************************************************************
	//*************************       Media Listeners       ************************
	//******************************************************************************
	//******************************************************************************
	
	/**
	 * Audio player event listener
	 */
	public class AudioPlayerEventListener extends IAudioEventListener.Stub {
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
    public class VideoPlayerEventListener extends IVideoEventListener.Stub {
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
