package com.orangelabs.rcs.core.gsm;

import java.util.Enumeration;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.orangelabs.rcs.core.Core;
import com.orangelabs.rcs.core.CoreException;
import com.orangelabs.rcs.core.ims.service.ImsService;
import com.orangelabs.rcs.core.ims.service.ImsServiceSession;
import com.orangelabs.rcs.platform.AndroidFactory;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Call manager
 * 
 * @author jexa7410
 */
public class CallManager {
	/**
	 * Call state ringing
	 */
	public final static int RINGING = 0;

	/**
	 * Call state connected
	 */
	public final static int CONNECTED = 1;
	
	/**
	 * Call state disconnected
	 */
	public final static int DISCONNECTED = 2;

	/**
	 * Core instance
	 */
	private Core core;
	
    /**
     * Call state
     */
    private int callState = DISCONNECTED;

    /**
     * Remote party
     */
    private String remoteParty = null;
    
	/**
	 * Telephony manager 
	 */
	private TelephonyManager tm;
	
	/**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     * Constructor
     * 
     * @param core Core
     */
	public CallManager(Core core) throws CoreException {
		this.core = core;

		// Instanciate the telephony manager
		tm = (TelephonyManager)AndroidFactory.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);

		if (logger.isActivated()) {
			logger.info("Call manager is created");
		}
	}
	
	/**
	 * Start call monitoring 
	 */
	public void startCallMonitoring() {
		if (logger.isActivated()) {
			logger.info("Start call monitoring");
		}
		
		// Monitor phone state
	    tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE |
	    		PhoneStateListener.LISTEN_DATA_CONNECTION_STATE);

		// Monitor outgoing call 
		IntentFilter filter = new IntentFilter(Intent.ACTION_NEW_OUTGOING_CALL);
		AndroidFactory.getApplicationContext().registerReceiver(outgoingCallReceiver, filter, null, null);
	}

	/**
	 * Stop call monitoring 
	 */
	public void stopCallMonitoring() {
		if (logger.isActivated()) {
			logger.info("Stop call monitoring");
		}
		
		// Unmonitor phone state
	    tm.listen(listener, PhoneStateListener.LISTEN_NONE);
	    
	    // Unmonitor outgoing call
	    AndroidFactory.getApplicationContext().unregisterReceiver(outgoingCallReceiver);
	}
	
	/**
	 * Phone state listener
	 */
	private PhoneStateListener listener = new PhoneStateListener() {
		public void onCallStateChanged(int state, String phoneNumber) {
			switch(state) {
				case TelephonyManager.CALL_STATE_RINGING:
					remoteParty = phoneNumber;
					callState = CallManager.RINGING;
					if (logger.isActivated()) {
						logger.debug("Call state: state=ringing, remote=" + remoteParty);
					}
					break;
					
				case TelephonyManager.CALL_STATE_IDLE:
					remoteParty = null;
					callState = CallManager.DISCONNECTED;
					if (logger.isActivated()) {
						logger.debug("Call state: state=idle, remote=null");
					}
					
			    	// Notify event listeners that there is no more call
					core.getListener().handleContentSharingCapabilitiesIndication(remoteParty, false, false, null);

					// Abort all the pending content sharing sessions
					for (Enumeration<ImsServiceSession> e = core.getCShService().getSessions(); e.hasMoreElements() ;) {
						ImsServiceSession session = (ImsServiceSession)e.nextElement();
						if (logger.isActivated()) {
							logger.debug("Call disconnected: abort session " + session.getSessionID());
						}
						session.abortSession();
					}				
					break;
					
				case TelephonyManager.CALL_STATE_OFFHOOK:
					if ((phoneNumber != null) && (phoneNumber.length() > 0)) {
						remoteParty = phoneNumber;
					}
					callState = CallManager.CONNECTED;
					if (logger.isActivated()) {
						logger.debug("Call state: state=connected, remote=" + remoteParty);
					}
					
					// If terminal connected to IMS then request capabilities
					if (core.getImsModule().getCurrentNetworkInterface().isRegistered()) {
						if (logger.isActivated()) {
							logger.debug("Request capabilities to " + remoteParty);
						}
						if (remoteParty != null) {
							core.getCapabilityService().requestCapability(remoteParty);
						}
					}
					break;
					
				default:
					if (logger.isActivated()) {
						logger.debug("Call state: state=" + state +  ", remote=" + phoneNumber);
					}
					break;
			}
		}
		
		/**
		 * Callback invoked when connection state changes
		 * 
		 * @param state State
		 */
		public void onDataConnectionStateChanged(int state) {
			if ((state == TelephonyManager.DATA_SUSPENDED) || (state == TelephonyManager.DATA_DISCONNECTED)) {
		    	// Notify event listeners that there is no more data connection
				core.getListener().handleContentSharingCapabilitiesIndication(remoteParty, false, false, null);
				
				// If there is no data connection then stop the pending sessions
				if (logger.isActivated()) {
					logger.debug("Data network is disconnected: abort all the pending sessions");
				}
				ImsService[] services = core.getAllServices();
				for(int i=0; i < services.length; i++) {
					ImsService service = services[i];
					for (Enumeration<ImsServiceSession> e = service.getSessions(); e.hasMoreElements() ;) {
						ImsServiceSession session = (ImsServiceSession)e.nextElement();
						if (logger.isActivated()) {
							logger.debug("Abort session " + session.getSessionID());
						}
						session.abortSession();
					}
				}
        	}
		}		
	};	
	
	/**
	 * Outgoing call receiver which is used to get the remote phone number
	 */
	private BroadcastReceiver outgoingCallReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
	        String phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
			if ((phoneNumber != null) && (phoneNumber.length() > 0)) {
	        	remoteParty = phoneNumber;
	        }
		}
	};	
	
	
	/**
	 * Returns the calling remote party
	 * 
	 * @return MSISDN
	 */
	public String getRemoteParty() {
		return remoteParty;
	}
	
	/**
	 * Is remote party connected
	 * 
	 * @param remote Remmote party (Tel-URI or SIP-URI)
	 * @return Boolean
	 */
	public boolean isConnected(String remote) {
		// TODO: check also the remote MSISDN
		return (callState == CONNECTED);
	}
}
