package com.orangelabs.rcs.ri.ipcall;

import com.orangelabs.rcs.service.api.client.ipcall.IIPCallEventListener;
import com.orangelabs.rcs.service.api.client.ipcall.IIPCallSession;
import com.orangelabs.rcs.utils.logger.Logger;

public class IPCallSessionData {

	/**
	 * Audio Call State of the session
	 */
	public int audioCallState = IPCallSessionsData.IDLE;

	/**
	 * Video Call State of the session
	 */
	public int videoCallState = IPCallSessionsData.IDLE;

	/**
	 * remote contact of the session
	 */
	public String remoteContact = "";
	
	/**
	 * direction of the session
	 */
	public String sessionDirection = "";
	
	/**
	 * event listener of the session
	 */	
	public IIPCallEventListener sessionEventListener ; 
	
	/**
	 * The logger
	 */
	private static Logger logger = Logger.getLogger(IPCallSessionData.class.getName());		
	
	
	
	public IPCallSessionData(int audioState, int videoState, String contact, String direction, IIPCallEventListener listener){
		this.audioCallState = audioState;
		this.videoCallState = videoState ;
		this.remoteContact = contact;
		this.sessionDirection = direction;
		this.sessionEventListener = listener ;
		
		if (logger.isActivated()) {
			logger.debug("new IPCallSessionData()");
			logger.debug("audioCallState ="+audioCallState);
			logger.debug("videoCallState ="+videoCallState);
			logger.debug("sessionDirection ="+sessionDirection);
			logger.debug("remoteContact ="+remoteContact);
		}
		
	}
	
	
}
