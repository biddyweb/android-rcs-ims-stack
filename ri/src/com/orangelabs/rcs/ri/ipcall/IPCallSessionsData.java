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
	public static ArrayList<IBinder> sessions;
	
	/**
	 * stored sessions data HashMap
	 */
	public static HashMap<String, IPCallSessionData> sessionsStates = new HashMap<String, IPCallSessionData>();

	/**
	 * current session
	 */
	public static IIPCallSession session = null;

	/**
	 *  current session ID
	 */
	public static String sessionId = null;

	/**
	 * current RemoteContact
	 */
	public static String remoteContact;

	/**
	 * event listener of the current session
	 */
	public static IIPCallEventListener sessionEventListener;

	/**
	 * Audio Call State of the current session
	 */
	public static int audioCallState = IDLE;

	/**
	 * Video Call State of the current session
	 */
	public static int videoCallState = IDLE;
	
	/**
	 * direction ("outgoing" "incoming") of current session
	 */
	public static String direction  = "";
	
	/**
	 * IP call API object
	 */
	public static IPCallApi callApi = null;

	/**
	 * IP call API connected status
	 */
	public static boolean isCallApiConnected = false;
	
	/**
	 * Client API listener
	 */
	public static ClientApiListener callApiListener = null;
	
	/**
	 * Ims Event listener
	 */
	public static ImsEventListener imsEventListener = null;

	/**
	 * Wait API connected to do getIncomingSession
	 */
	public static boolean getIncomingSessionWhenApiConnected = false;

	/**
	 * Wait API connected to do startOutgoingSession
	 */
	public static boolean startOutgoingSessionWhenApiConnected = false;

	/**
	 * Wait API connected to launch recoverSessions()
	 */
	public static boolean recoverSessionsWhenApiConnected = false;

	/**
	 * The logger
	 */
	private static Logger logger = Logger.getLogger(IPCallSessionsData.class.getName());		
	
	
	/**
	 * store session data 
	 */
	public static void saveSessionData(String key,
			IPCallSessionData value) {
		if (logger.isActivated()) {
			logger.debug("saveSessionData()");		
		}
		IPCallSessionData previousSessionData = sessionsStates.put(key, value);
		if (logger.isActivated()) {
			logger.debug("previousSessionData ="+previousSessionData);		
		}
	}

	/**
	 * get session data in IPCallSessionsData
	 */
	public static void getSessionData(String sessionId) {
		IPCallSessionData object = null ;
		if (logger.isActivated()) {
			logger.debug("getSessionData()");		
		}
		if (IPCallSessionsData.sessionsStates.containsKey(sessionId)) {
			if (logger.isActivated()) {
				logger.debug("sessionsStates contains key :"+sessionId);		
			}
			object = (IPCallSessionData) IPCallSessionsData.sessionsStates
					.get(sessionId);
		}

		if (object != null) {
			IPCallSessionsData.audioCallState = object.audioCallState;
			IPCallSessionsData.videoCallState = object.videoCallState;
			IPCallSessionsData.remoteContact = object.remoteContact;
			IPCallSessionsData.direction = object.sessionDirection;
			IPCallSessionsData.sessionEventListener = object.sessionEventListener;
			if (logger.isActivated()) {
				logger.debug("audioCallState ="+object.audioCallState);
				logger.debug("videoCallState ="+object.videoCallState);
				logger.debug("remoteContact ="+object.remoteContact);
				logger.debug("direction ="+object.sessionDirection);
			}
		}
	}
	
	
	/**
	 * remove session data 
	 */
	public static void removeSessionData(String key) {
		if (logger.isActivated()) {
			logger.debug("removeSessionData()");	
			logger.debug("key :"+key);
		}
		IPCallSessionData savedData = sessionsStates.remove(key);
		
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
