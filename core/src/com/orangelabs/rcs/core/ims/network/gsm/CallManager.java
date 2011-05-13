/*******************************************************************************
 * Software Name : RCS IMS Stack
 *
 * Copyright © 2010 France Telecom S.A.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.orangelabs.rcs.core.ims.network.gsm;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.orangelabs.rcs.core.CoreException;
import com.orangelabs.rcs.core.ims.ImsModule;
import com.orangelabs.rcs.platform.AndroidFactory;
import com.orangelabs.rcs.utils.PhoneUtils;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Call manager. Note: for outgoing call the capability request is initiated only when we receive
 * the OPTIONS from the remote because the call state goes directly to CONNETED even if the remote
 * has not ringing. For the incoming call, the capability are requested when phone is ringing.
 * 
 * @author jexa7410
 */
public class CallManager {
	/**
	 * Call state unknown
	 */
	public final static int UNKNOWN = 0;

	/**
	 * Call state ringing
	 */
	public final static int RINGING = 1;

	/**
	 * Call state connected
	 */
	public final static int CONNECTED = 2;
	
	/**
	 * Call state disconnected
	 */
	public final static int DISCONNECTED = 3;

	/**
	 * IMS module
	 */
	private ImsModule imsModule;
	
    /**
     * Call state
     */
    private int callState = UNKNOWN;

    /**
     * Remote party
     */
    private static String remoteParty = null;

    /**
     * Incoming call
     */
    private boolean incomingCall = false;
    
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
	public CallManager(ImsModule parent) throws CoreException {
		this.imsModule = parent;

		// Instanciate the telephony manager
		tm = (TelephonyManager)AndroidFactory.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
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
	}
	
	/**
	 * Phone state listener
	 */
	private PhoneStateListener listener = new PhoneStateListener() {
		public void onCallStateChanged(int state, String incomingNumber) {
			switch(state) {
				case TelephonyManager.CALL_STATE_RINGING:
					if (logger.isActivated()) {
						logger.debug("Call is RINGING: incoming number=" + incomingNumber);
					}

					// Phone is ringing: this state is only used for incoming call
					callState = CallManager.RINGING;
				    incomingCall = true;

					// Set remote party
				    remoteParty = incomingNumber;

					// Incoming call is received: request capabilities
					requestCapabilities(remoteParty);
					break;
					
				case TelephonyManager.CALL_STATE_IDLE:
					if (logger.isActivated()) {
						logger.debug("Call is IDLE: last number=" + remoteParty);
					}

					// No more call in progress
					callState = CallManager.DISCONNECTED;
				    incomingCall = false;

				    // End of call: update capabilities
					requestCapabilities(remoteParty);

					// Reset remote party
					remoteParty = null;
					break;
					
				case TelephonyManager.CALL_STATE_OFFHOOK:
					if (logger.isActivated()) {
						logger.debug("Call is CONNECTED: connected number=" + remoteParty);
					}

					// Both parties are connected
					callState = CallManager.CONNECTED;
					break;
					
				default:
					if (logger.isActivated()) {
						logger.debug("Unknown call state " + state);
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
				if (logger.isActivated()) {
					logger.debug("Data network has been disconnected: abort all pending sessions");
				}
				imsModule.abortAllSessions();
			}
		}		
	};	
	
	/**
	 * Set the remote phone number
	 * 
	 * @param number Phone number
	 */
	public static void setRemoteParty(String number) {
		CallManager.remoteParty = number;
	}
	
	/**
	 * Get the remote phone number
	 * 
	 * @return Phone number
	 */
	public String getRemotePhoneNumber() {
		if (callState == CallManager.DISCONNECTED) {
			return null;
		} else {
			return remoteParty;
		}
	}
	
	/**
	 * Returns the calling remote party
	 * 
	 * @return MSISDN
	 */
	public String getRemoteParty() {
		return remoteParty;
	}
	
	/**
	 * Is call connected
	 * 
	 * @return Boolean
	 */
	public boolean isConnected() {
		return (callState == CONNECTED || callState == RINGING);
	}
	
	/**
	 * Is call connected with a given contact
	 * 
	 * @param contact Contact
	 * @return Boolean
	 */
	public boolean isConnectedWith(String contact) {
		return (isConnected() && PhoneUtils.compareNumbers(contact, getRemotePhoneNumber()));
	}
	
	/**
	 * Is an incoming call
	 * 
	 * @return Boolean
	 */
	public boolean isIncomingCall() {
		return incomingCall;
	}
	
	/**
	 * Request capabilities to a given contact
	 * 
	 * @param contact Contact
	 */
	private void requestCapabilities(String contact) {
		 if (contact != null) {
			 imsModule.getCapabilityService().requestContactCapabilities(contact);
		 }
	}	
}
