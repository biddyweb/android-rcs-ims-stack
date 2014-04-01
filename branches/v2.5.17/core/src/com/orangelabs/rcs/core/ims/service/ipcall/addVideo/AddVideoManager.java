package com.orangelabs.rcs.core.ims.service.ipcall.addVideo;



import com.orangelabs.rcs.core.content.LiveVideoContent;
import com.orangelabs.rcs.core.ims.protocol.sip.SipRequest;
import com.orangelabs.rcs.core.ims.protocol.sip.SipResponse;
import com.orangelabs.rcs.core.ims.service.ImsServiceSession;
import com.orangelabs.rcs.core.ims.service.UpdateSessionManagerListener;
import com.orangelabs.rcs.core.ims.service.ipcall.IPCallStreamingSession;
import com.orangelabs.rcs.core.ims.service.ipcall.IPCallStreamingSessionListener;
import com.orangelabs.rcs.service.api.client.media.IVideoPlayer;
import com.orangelabs.rcs.service.api.client.media.IVideoRenderer;
import com.orangelabs.rcs.utils.logger.Logger;


/**
 * Super class for IP Call Add Video Manager 
 * 
 * @author O. Magnon
 */
public class AddVideoManager implements UpdateSessionManagerListener {

	/**
	 * Enumerated for AddVideo manager state values
	 */
	public enum stateValue {
		IDLE(0), ADD_VIDEO_INPROGRESS(1), REMOVE_VIDEO_INPROGRESS(2);
		
		int value;
		
		private stateValue(int val){
			this.value = val;
		}
	};
	
	/**
	 * Add Video state
	 */
	protected stateValue state  ;
	
	/**
	 * session handled by AddVideoManager
	 */
	private IPCallStreamingSession session ; 	
	
	/**
	 * AddVideo Object
	 */
	private AddVideoImpl m_addVideoImpl;
	
	/**
	 * The logger
	 */
	private Logger logger = Logger.getLogger(this.getClass().getName());	
	
	/**
	 * constructor
	 */
	public AddVideoManager(IPCallStreamingSession session){
		if (logger.isActivated()){
			logger.info("AddVideoManager()");
		}
		this.state = AddVideoManager.stateValue.IDLE;
		this.session = session;
	}	
	
	/**
	 * get AddVideoManager state
	 * 
	 * @return int state
	 */
	public stateValue getState(){
		return state;
	}	
	
	/**
	 * set AddVideoManager state
	 */
	public void setState(stateValue val){
		state = val;
	}
	
	
	/**
	 * ReInvite response received
	 * 
	 * @param code  Sip response code
	 * @param response  Sip response request
	 */
	public void handleReInviteResponse(int code, SipResponse response) {
		if (logger.isActivated()) {
			logger.info("handleReInviteResponse: " + code);
		}

		// case Add video
		if (state == stateValue.ADD_VIDEO_INPROGRESS) {
			if (code == 200) { // 200 OK response
				//prepare video session
				m_addVideoImpl.prepareVideoSession();
				
				//start video session
				m_addVideoImpl.startVideoSession();
				
				// set AddVideoManager state
				state = stateValue.IDLE ;
				
				// Notify listeners
				if (!session.isInterrupted()) {
					for (int i = 0; i < session.getListeners().size(); i++) {
						((IPCallStreamingSessionListener) session.getListeners().get(i))
								.handleAddVideoAccepted();
					}
				}
				
			} else if ((code == ImsServiceSession.INVITATION_REJECTED)
					|| (code == ImsServiceSession.TERMINATION_BY_TIMEOUT)) {	

				//reset add video manager - set video content to null
				setState(stateValue.IDLE);
				session.setVideoContent(null);	
				
				// Notify listeners
				if (!session.isInterrupted()) {
					for (int i = 0; i < session.getListeners().size(); i++) {
						((IPCallStreamingSessionListener) session.getListeners().get(i))
								.handleAddVideoAborted(code);
					}
				}	
			}			
			// release addVideoImpl  object
			m_addVideoImpl = null;
			
		// case Remove Video
		} else if (state == stateValue.REMOVE_VIDEO_INPROGRESS) {
			if (code == 200) { // 200 OK response
				// close video media session
				m_addVideoImpl.closeVideoSession();	
				
				// set AddVideoManager state
				state = stateValue.IDLE;

				// Notify listeners
				if (!session.isInterrupted()) {
					for (int i = 0; i < session.getListeners().size(); i++) {
						((IPCallStreamingSessionListener) session.getListeners().get(i))
								.handleRemoveVideoAccepted();
					}
				}
			} else if (code == ImsServiceSession.TERMINATION_BY_TIMEOUT) { // No answer or 
																		//408 TimeOut response
				//reset add video manager state to "idle"
				setState(stateValue.IDLE);
				
				// Notify listeners
				if (!session.isInterrupted()) {
					for (int i = 0; i < session.getListeners().size(); i++) {
						((IPCallStreamingSessionListener) session.getListeners().get(i))
								.handleRemoveVideoAborted(code);
					}
				}
			}
			// release addVideoImpl  object
			m_addVideoImpl = null;
		}  
	}

    /**
     * User answer to invitation received 
     * 
	 * @param code  response code
     */
    public void handleReInviteUserAnswer(int code) {
    	if (logger.isActivated()) {
			logger.info("handleReInviteUserAnswer: " + code);
		}

		// case Add video
		if (state == stateValue.ADD_VIDEO_INPROGRESS) {// Invitation accepted			
			if (code == ImsServiceSession.INVITATION_ACCEPTED) {
				// prepare Video media session
				m_addVideoImpl.prepareVideoSession();
	
			} else if ((code == ImsServiceSession.INVITATION_NOT_ANSWERED)||(code == ImsServiceSession.INVITATION_REJECTED)) {// Invitation declined or not answered
				//reset add video manager - set video content to null
				setState(stateValue.IDLE);
				session.setVideoContent(null);				
				
				// Notify listeners
				if (!session.isInterrupted()) {
					for (int i = 0; i < session.getListeners().size(); i++) {
						((IPCallStreamingSessionListener) session.getListeners().get(i))
								.handleAddVideoAborted(code);
					}
				}
			}
		}
    }
    
    /**
     * ReInvite Ack received  
     * 
     * @param code  Sip response code
     */
    public void handleReInviteAck(int code) {
    	if (logger.isActivated()) {
			logger.info("handleReInviteAckResponse: " + code);
		}

		// case Add video
		if ((state == stateValue.ADD_VIDEO_INPROGRESS) && (code == 200)) {
			// start Video media session
			m_addVideoImpl.startVideoSession();
			
			// set AddVideoManager state
			state = stateValue.IDLE ;

			// Notify listeners
			if (!session.isInterrupted()) {
				for (int i = 0; i < session.getListeners().size(); i++) {
					((IPCallStreamingSessionListener) session.getListeners().get(i))
							.handleAddVideoAccepted();
				}
			}
			// release addVideoImpl  object
			m_addVideoImpl = null;
						
		// case Remove Video	
		} else if ((state == stateValue.REMOVE_VIDEO_INPROGRESS)&& (code == 200)) {						
			// close video media session
			m_addVideoImpl.closeVideoSession();
			
			// set AddVideoManager state
			state = stateValue.IDLE;

			// Notify listeners
			if (!session.isInterrupted()) {
				for (int i = 0; i < session.getListeners().size(); i++) {
					((IPCallStreamingSessionListener) session.getListeners().get(i))
							.handleRemoveVideoAccepted();
				}
			}
			// release addVideoImpl  object
			m_addVideoImpl = null;
			
		} 
    }
	
    /**
     * Sdp requested for ReInvite Response 
     * 
     * @param reInvite  Sip reInvite request received
     */
    public String buildReInviteSdpResponse(SipRequest reInvite){
    	String localSdp = "";
		if (state == stateValue.ADD_VIDEO_INPROGRESS) {
				localSdp = m_addVideoImpl.buildAddVideoSdpResponse(reInvite);
		}

		return localSdp;
    }
    
    
	
	/**
	 * add Video to session (case local AddVideoManager)
	 * 
	 * @param videoPlayer video player instance
	 * @param videoRenderer video renderer instance
	 * @throws Exception
	 */
	public  void addVideo(IVideoPlayer videoPlayer, IVideoRenderer videoRenderer) throws Exception {
		state = stateValue.ADD_VIDEO_INPROGRESS;
		
		m_addVideoImpl = new LocalAddVideoImpl(session, this);
		m_addVideoImpl.addVideo(videoPlayer, videoRenderer);
	}
	
	
	/**
	 * add Video to session (case remote AddVideoManager)
	 * 
	 * @param reInvite reInvite SIP request received
	 */
	public LiveVideoContent addVideo(SipRequest reInvite){
		state = stateValue.ADD_VIDEO_INPROGRESS;
		
		m_addVideoImpl = new RemoteAddVideoImpl(session, this);
		return m_addVideoImpl.addVideo(reInvite);
	}
	
	
	/**
	 * remove Video from session (case local AddVideoManager)
	 * 
	 * @throws Exception
	 */
	public void removeVideo()throws Exception {
		state = stateValue.REMOVE_VIDEO_INPROGRESS;
		
		m_addVideoImpl = new LocalAddVideoImpl(session, this);
		m_addVideoImpl.removeVideo();
	}
	
	
	/**
	 * remove Video from session (case remote AddVideoManager)
	 * 
	 * @param reInvite reInvite SIP request received
	 */
	public void removeVideo(SipRequest reInvite){
		state = stateValue.REMOVE_VIDEO_INPROGRESS;
		
		m_addVideoImpl = new RemoteAddVideoImpl(session, this);
		m_addVideoImpl.removeVideo(reInvite);		
	}

}
