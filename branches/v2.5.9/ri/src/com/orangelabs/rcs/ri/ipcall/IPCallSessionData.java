package com.orangelabs.rcs.ri.ipcall;

import com.orangelabs.rcs.service.api.client.ipcall.IIPCallEventListener;
import com.orangelabs.rcs.utils.logger.Logger;

public class IPCallSessionData {

	/**
	 * Audio Call State of the session
	 */
	private int audioCallState = IPCallSessionsData.IDLE;

	/**
	 * Video Call State of the session
	 */
	private int videoCallState = IPCallSessionsData.IDLE;

	/**
	 * remote contact of the session
	 */
	private String remoteContact = "";
	
	/**
	 * direction of the session
	 */
	private String sessionDirection = "";
	
	/**
	 * event listener of the session
	 */	
	private IIPCallEventListener sessionEventListener ; 
	
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
	
	 public int getAudioCallState() {
		 return audioCallState;
	 }
	 
	 public int getVideoCallState() {
		 return videoCallState;
	 }
	 
	 public String getRemoteContact() {
		 return remoteContact;
	 }
	 
	 public String getSessionDirection() {
		 return sessionDirection;
	 }
	 
	 public IIPCallEventListener getSessionEventListener() {
		 return sessionEventListener;
	 }
}
