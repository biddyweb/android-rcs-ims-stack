package com.orangelabs.rcs.ri.ipcall;

import java.util.ArrayList;
import java.util.HashMap;

import android.os.IBinder;

import com.orangelabs.rcs.service.api.client.ClientApiListener;
import com.orangelabs.rcs.service.api.client.ImsEventListener;
import com.orangelabs.rcs.service.api.client.ipcall.IIPCallEventListener;
import com.orangelabs.rcs.service.api.client.ipcall.IIPCallSession;
import com.orangelabs.rcs.service.api.client.ipcall.IPCallApi;
import com.orangelabs.rcs.utils.logger.Logger;

public class IPCallSessionsData {

	private static IPCallSessionsData instance = null;
	
	/**
	 * list of constant value for IP Call states
	 */
	public static final int IDLE = 0;

	public static final int DISCONNECTED = 4;

	public static final int CONNECTED = 2;

	public static final int CONNECTING = 1;

	public static final int ON_HOLD = 6;

	
	/**
	 * list of established sessions
	 */
	public ArrayList<IBinder> sessions = null;
	
	/**
	 * stored sessions data HashMap
	 */
	public HashMap<String, IPCallSessionData> sessionsStates = null;

	/**
	 * IP call API object
	 */
	public IPCallApi callApi;

	/**
	 * IP call API connected status
	 */
	public boolean isCallApiConnected;
	
	/**
	 * Client API listener
	 */
	public ClientApiListener callApiListener;
	
	/**
	 * Ims Event listener
	 */
	public ImsEventListener imsEventListener;
	
	/**
	 * event listener of the current session
	 */
	public IIPCallEventListener sessionEventListener;

	/**
	 * The logger
	 */
	private static Logger logger = Logger.getLogger(IPCallSessionsData.class.getName());	
	
	/**
	 * constructor
	 */
	public IPCallSessionsData() {
		this.sessions = null;
		this.sessionsStates = new HashMap<String, IPCallSessionData>();
		this.callApi = null;
		this.isCallApiConnected= false;
		this.callApiListener = null;
		this.imsEventListener = null;
		
	}
	
	public static IPCallSessionsData getInstance(){
		if (instance == null) {
			instance = new IPCallSessionsData();
		}

		return instance;
	}
	
	
	/**
	 * store session data 
	 */
	public void saveSessionData(String key,
			IPCallSessionData value) {
		if (logger.isActivated()) {
			logger.debug("saveSessionData()");		
		}
		IPCallSessionData savedSessionData = this.sessionsStates.put(key, value);
		if (logger.isActivated()) {
			logger.debug("savedSessionData ="+savedSessionData);		
		}
	}

	/**
	 * set session data
	 */
	public void setSessionData(String sessionId, IPCallView sessionActivity) {
		IPCallSessionData object = null ;
		if (logger.isActivated()) {
			logger.debug("getSessionData()");		
		}
		if (this.sessionsStates.containsKey(sessionId)) {
			if (logger.isActivated()) {
				logger.debug("sessionsStates contains key :"+sessionId);		
			}
			object = (IPCallSessionData) this.sessionsStates
					.get(sessionId);
		}

		if (object != null) {
			sessionActivity.audioCallState = object.getAudioCallState();
			sessionActivity.videoCallState = object.getVideoCallState();
			sessionActivity.remoteContact = object.getRemoteContact();
			sessionActivity.direction = object.getSessionDirection();
			this.sessionEventListener = object.getSessionEventListener();
			if (logger.isActivated()) {
				logger.debug("audioCallState ="+object.getAudioCallState());
				logger.debug("videoCallState ="+object.getVideoCallState());
				logger.debug("remoteContact ="+object.getRemoteContact());
				logger.debug("direction ="+object.getSessionDirection());
			}
		}
	}
	
	
	/**
	 * return session data 
	 */
	public IPCallSessionData getSessionData(String sessionId) {
		IPCallSessionData object = null ;
		if (logger.isActivated()) {
			logger.debug("getSessionData()");		
		}
		if (this.sessionsStates.containsKey(sessionId)) {
			if (logger.isActivated()) {
				logger.debug("sessionsStates contains key :"+sessionId);		
			}
			object = (IPCallSessionData) this.sessionsStates
					.get(sessionId);
		}

		return object;
	}
	
	/**
	 * remove session data 
	 */
	public void removeSessionData(String key) {
		if (logger.isActivated()) {
			logger.debug("removeSessionData()");	
			logger.debug("key :"+key);
		}
		IPCallSessionData savedData = this.sessionsStates.remove(key);
		
		if (savedData != null){
			if (logger.isActivated()) {
				logger.debug("removeSessionData done");		
			}
		} else {
			if (logger.isActivated()) {
				logger.debug("any data to remove");		
			}
		}
		
	}

}
