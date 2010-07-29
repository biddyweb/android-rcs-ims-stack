/*******************************************************************************
 * Software Name : RCS IMS Stack
 * Version : 2.0.0
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
package com.orangelabs.rcs.core.ims;

import android.content.Context;
import android.net.ConnectivityManager;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;

import com.orangelabs.rcs.core.ims.network.ImsNetworkInterface;
import com.orangelabs.rcs.platform.AndroidFactory;
import com.orangelabs.rcs.platform.network.NetworkFactory;
import com.orangelabs.rcs.utils.NetworkRessourceManager;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * IMS connection manager
 * 
 * @author JM. Auffret
 */
public class ImsConnectionManager implements Runnable {
    /**
     * IMS module
     */
    private ImsModule imsModule;
    
    /**
     * IMS network interface
     */
    private ImsNetworkInterface currentNetworkInterface;
    
    /**
     * Connectivity manager
     */
    private ConnectivityManager connectivityManager;
    
	/**
	 * Telephony manager 
	 */
	private TelephonyManager telephonyManager;

    /**
     * IMS connection polling period in second
     */
    private int pollingPeriod;
    
    /**
     * IMS polling thread
     */
    private Thread imsPollingThread = null;

    /**
     * IMS activation flag
     */
    private boolean imsActivationFlag = false;

    /**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());
    
    /**
	 * Constructor
	 * 
	 * @param pollingPeriod Connection polling period
	 */
	public ImsConnectionManager(ImsModule imsModule, int pollingPeriod) {
		this.imsModule = imsModule;
		this.pollingPeriod = pollingPeriod;
		
		// Set the current network interface
		currentNetworkInterface = imsModule.getDefaultNetworkInterface();
		
		// Get connectivity manager instance
		connectivityManager = (ConnectivityManager)AndroidFactory.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
	
		// Listen to data connectivity events
		telephonyManager = (TelephonyManager)AndroidFactory.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
		telephonyManager.listen(dataActivityListener,
				PhoneStateListener.LISTEN_DATA_CONNECTION_STATE |
				PhoneStateListener.LISTEN_SERVICE_STATE);
	}
	
	/**
     * Returns the current network interface
     * 
     * @return Current network interface
     */
	public ImsNetworkInterface getCurrentNetworkInterface() {
		return currentNetworkInterface;
	}

	/**
     * Terminate the connection manager
     */
    public void terminate() {
    	if (logger.isActivated()) {
    		logger.info("Terminate the IMS connection manager");
    	}

    	// Stop the IMS connection manager
    	stopImsConnection();
    	
    	// Unregister from the IMS
		currentNetworkInterface.unregister();
		    	
    	if (logger.isActivated()) {
    		logger.info("IMS connection manager has been terminated");
    	}
    }

	/**
	 * Start the IMS connection
	 */
	public synchronized void startImsConnection() {
		if (imsActivationFlag) {
			// Already connected
			return;
		}
		
		// Set the connection flag
    	if (logger.isActivated()) {
    		logger.info("Start the IMS connection manager");
    	}
		imsActivationFlag = true;
    	
		// Start background polling thread
		try {
			imsPollingThread = new Thread(this);
			imsPollingThread.start();
		} catch(Exception e) {
			if (logger.isActivated()) {
				logger.error("Intrenal exception while starting IMS polling thread", e);
			}
		}
	}
	
	/**
	 * Stop the IMS connection
	 */
	public synchronized void stopImsConnection() {
		if (!imsActivationFlag) {
			// Already disconnected
			return;
		}

		// Set the connection flag
		if (logger.isActivated()) {
    		logger.info("Stop the IMS connection manager");
    	}
		imsActivationFlag = false;

    	// Stop background polling thread
		try {
			imsPollingThread.interrupt();
			imsPollingThread = null;
		} catch(Exception e) {
			if (logger.isActivated()) {
				logger.error("Intrenal exception while stopping IMS polling thread", e);
			}
		}
		
		// Stop IMS services
		imsModule.stopImsServices();
	}

	/**
	 * Background processing
	 */
	public void run() {
    	if (logger.isActivated()) {
    		logger.info("Start polling of the IMS connection");
    	}
    	
		while(imsActivationFlag) {
	    	if (logger.isActivated()) {
	    		logger.info("Polling: check IMS connection");
	    	}

	    	// Connection management
    		try {
    	    	// Test IMS registration
    			if (!currentNetworkInterface.isRegistered()) {
    				if (logger.isActivated()) {
    					logger.debug("Not yet registered to IMS: try registration");
    				}

    				// Try to register to IMS
    	    		if (currentNetworkInterface.register()) {
    	            	if (logger.isActivated()) {
    	            		logger.debug("Registered to the IMS with success: start IMS services");
    	            	}
        	        	imsModule.startImsServices();
    	    		} else {
    	            	if (logger.isActivated()) {
    	            		logger.debug("Can't register to the IMS");
    	            	}
    	    		}
    			} else {
    	        	if (logger.isActivated()) {
    	        		logger.debug("Already registered to IMS: check IMS services");
    	        	}
    	        	imsModule.checkImsServices();
    			}
			} catch(Exception e) {
				if (logger.isActivated()) {
		    		logger.error("Internal exception", e);
		    	}
			}
	    	
			// Make a pause before a new polling
	    	try {
                Thread.sleep(pollingPeriod * 1000);
            } catch (InterruptedException e) {
                break;
            }		    	
    	}

		if (logger.isActivated()) {
    		logger.info("IMS connection polling is terminated");
    	}
	}
	
	/**
	 * Phone state listener
	 */
	private PhoneStateListener dataActivityListener = new PhoneStateListener() {
		/**
		 * Callback invoked when connection state changes
		 * 
		 * @param state State
		 */
		public void onDataConnectionStateChanged(int state) {
			if (state == TelephonyManager.DATA_CONNECTED) {
				// Data network access connected
				if (logger.isActivated()) {
					logger.debug("Data connection state: CONNECTED");
				}
				
				// Test if the SIP outbound proxy is reachable
				String outboundProxy = ImsModule.IMS_USER_PROFILE.getOutboundProxyAddr();
				int index = outboundProxy.indexOf(":");
				outboundProxy = outboundProxy.substring(0, index);
				int host = NetworkRessourceManager.ipToInt(outboundProxy);
				boolean routed = connectivityManager.requestRouteToHost(ConnectivityManager.TYPE_MOBILE, host); 
				if (routed) {
					// Get the local IP address
					String localIpAddr = NetworkFactory.getFactory().getLocalIpAddress();
					
					// Connected to the network access
					if (logger.isActivated()) {
						logger.debug("Connected to network access at address " + localIpAddr);
					}
					currentNetworkInterface.getNetworkAccess().connect(localIpAddr);
					
					// Start the IMS connection
					startImsConnection();
				}
			} else
			if ((state == TelephonyManager.DATA_SUSPENDED) || (state == TelephonyManager.DATA_DISCONNECTED)) {
				// Data network access disconnected
				if (logger.isActivated()) {
					logger.debug("Data connection state: DISCONNECTED or SUSPENDED");
				}
		
				// Stop the IMS connection
				stopImsConnection();

				// Registration terminated 
				currentNetworkInterface.registrationTerminated();
				
				// Disconnect from the network access
				if (logger.isActivated()) {
					logger.debug("Disconnect from network access");
				}
				currentNetworkInterface.getNetworkAccess().disconnect();
        	} else
			if (state == TelephonyManager.DATA_CONNECTING) {
				// Data network access connected
				if (logger.isActivated()) {
					logger.debug("Data connection state: CONNECTING");
				}
			}
		}

		/**
		 * Callback invoked when device service state changes
		 * 
		 * @param serviceState Service state
		 */
		public void onServiceStateChanged(ServiceState serviceState) {
			if (serviceState.getState() == ServiceState.STATE_IN_SERVICE) {
				if (logger.isActivated()) {
					logger.debug("Service state: IN_SERVICE");
				}
			} else 
			if (serviceState.getState() == ServiceState.STATE_EMERGENCY_ONLY) {
				if (logger.isActivated()) {
					logger.debug("Service state: EMERGENCY_ONLY");
				}
			} else 
			if (serviceState.getState() == ServiceState.STATE_OUT_OF_SERVICE) {
				if (logger.isActivated()) {
					logger.debug("Service state: OUT_OF_SERVICE");
				}
			} else 
			if (serviceState.getState() == ServiceState.STATE_POWER_OFF) {
				if (logger.isActivated()) {
					logger.debug("Service state: POWER_OFF");
				}				
			} 
		}
	};
}
