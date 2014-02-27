package com.orangelabs.rcs.core.ims.service.ipcall.hold;


import com.orangelabs.rcs.core.ims.protocol.sip.SipRequest;
import com.orangelabs.rcs.core.ims.protocol.sip.SipResponse;
import com.orangelabs.rcs.core.ims.service.ImsServiceSession;
import com.orangelabs.rcs.core.ims.service.UpdateSessionManagerListener;
import com.orangelabs.rcs.core.ims.service.ipcall.IPCallStreamingSession;
import com.orangelabs.rcs.core.ims.service.ipcall.IPCallStreamingSessionListener;
import com.orangelabs.rcs.utils.logger.Logger;


/**
 * Hold Manager 
 * 
 * @author O. Magnon
 */
public  class  HoldManager implements UpdateSessionManagerListener {
	
	/**
	 * Enumerated for Hold manager state values
	 */
	public enum stateValue {
		IDLE(0), HOLD_INPROGRESS(1), HOLD(2), UNHOLD_INPROGRESS(3), REMOTE_HOLD_INPROGRESS(4), REMOTE_HOLD(5), REMOTE_UNHOLD_INPROGRESS(6);
		
		int value;
		
		private stateValue(int val){
			this.value = val;
		}
	};
	
	/**
	 * Hold state
	 */
	protected stateValue  state;	
	
	/**
	 * session handled by Hold manager
	 */
	private IPCallStreamingSession session ; 
	
	
	/**
	 * Hold Object
	 */
	private HoldImpl m_holdImpl;
	
	/**
	 * The logger
	 */
	private Logger logger = Logger.getLogger(this.getClass().getName());
	
	
	/**
	 * constructor
	 */
	public HoldManager(IPCallStreamingSession session){
		if (logger.isActivated()){
			logger.info("HoldManager()");
		}
		this.session = session;
		this.state = stateValue.IDLE;
	}
	
	/**
	 * get HoldManager state
	 * 
	 * @return int state
	 */
	public stateValue getState(){
		return state;
	}
	
	/**
	 * set HoldManager state
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

		// case set On Hold
		if (state == stateValue.HOLD_INPROGRESS) {
			if (code == 200) { // 200 OK response
				// prepare media session
				m_holdImpl.holdMediaSession();
				setState(stateValue.HOLD);

				// Notify listeners
				if (!session.isInterrupted()) {
					for (int i = 0; i < session.getListeners().size(); i++) {
						((IPCallStreamingSessionListener) session
								.getListeners().get(i))
								.handleCallHoldAccepted();
					}
				}
			} else if (code == ImsServiceSession.TERMINATION_BY_TIMEOUT) { 
				// No answer or 408 TimeOut response
				// reset hold manager state to "idle"
				setState(stateValue.IDLE);

				// Notify listeners
				if (!session.isInterrupted()) {
					for (int i = 0; i < session.getListeners().size(); i++) {
						((IPCallStreamingSessionListener) session
								.getListeners().get(i))
								.handleCallHoldAborted(code);
					}
				}

			}
			// release hold  object
			m_holdImpl = null;

			// case Set On Resume
		} else if (state == stateValue.UNHOLD_INPROGRESS) {
			if (code == 200) { // 200 OK response
				// prepare media session
				m_holdImpl.resumeMediaSession();
				setState(stateValue.IDLE);

				// Notify listeners
				if (!session.isInterrupted()) {
					for (int i = 0; i < session.getListeners().size(); i++) {
						((IPCallStreamingSessionListener) session
								.getListeners().get(i))
								.handleCallResumeAccepted();
					}
				}
			} else if (code == ImsServiceSession.TERMINATION_BY_TIMEOUT) { 
				// No answer or 408 TimeOut response
				// reset hold manager state to "Hold"
				setState(stateValue.HOLD);

				// Notify listeners
				if (!session.isInterrupted()) {
					for (int i = 0; i < session.getListeners().size(); i++) {
						((IPCallStreamingSessionListener) session
								.getListeners().get(i))
								.handleCallResumeAborted(code);
					}
				}
			}

			// release hold  object
			m_holdImpl = null;
		}
	}

    /**
     * User answer to invitation received 
     * 
	 * @param code  response code
     */
    public void handleReInviteUserAnswer(int code) {
    	// Not used in Hold Manager
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

		
		// case Set On Hold
		 if ((state == stateValue.REMOTE_HOLD_INPROGRESS)&& (code == 200)) {						
			//prepare media session
			 m_holdImpl.holdMediaSession();
			 setState(stateValue.REMOTE_HOLD);
			
			// Notify listeners
			if (!session.isInterrupted()) {
				for (int i = 0; i < session.getListeners().size(); i++) {
					((IPCallStreamingSessionListener) session.getListeners().get(i))
							.handleCallHoldAccepted();
				}
			}
			// release hold  object
			m_holdImpl = null;
			
		// case Set On Resume	
		} else if ((state == stateValue.REMOTE_UNHOLD_INPROGRESS)
				&& (code == 200)) {// case On Resume
			// prepare media session
			m_holdImpl.resumeMediaSession();
			setState(stateValue.IDLE);
			
			// Notify listeners
			if (!session.isInterrupted()) {
				for (int i = 0; i < session.getListeners().size(); i++) {
					((IPCallStreamingSessionListener) session.getListeners().get(i))
							.handleCallResumeAccepted();
				}
			}
			
			// release hold  object
			m_holdImpl = null;
		}
    }
	
    
    public String buildReInviteSdpResponse(SipRequest reInvite){
    	// Not used in Remote Hold Manager
    	return null; 	
    }
    

	
	/**
	 * set call on Hold/onResume (case local HoldManager)
	 * 
	 * @param calHoldAction hold action (true: call hold/false: call resume)
	 * @throws Exception
	 */
	public void setCallHold(boolean callHoldAction) throws Exception {
		m_holdImpl = new HoldInactiveImpl(session, this);
		
		if (callHoldAction) {setState(stateValue.HOLD_INPROGRESS);}
		else {setState(stateValue.UNHOLD_INPROGRESS);}
		m_holdImpl.setCallHold(callHoldAction);
	}
	
	/**
	 * set call on Hold/onResume (case remote HoldManager)
	 * 
	 * @param calHoldAction hold action (true: call hold/false: call resume)
	 * @param reInvite reInvite SIP request received
	 */
	public void setCallHold(boolean callHoldAction, SipRequest reInvite){
		m_holdImpl = new HoldInactiveImpl(session, this);
		
		if (callHoldAction) {setState(stateValue.REMOTE_HOLD_INPROGRESS);}
		else {setState(stateValue.REMOTE_UNHOLD_INPROGRESS);}
		m_holdImpl.setCallHold(callHoldAction, reInvite);
		
	}

	

}
