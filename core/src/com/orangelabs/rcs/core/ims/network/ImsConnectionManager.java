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

package com.orangelabs.rcs.core.ims.network;

import com.orangelabs.rcs.core.CoreException;
import com.orangelabs.rcs.core.ims.ImsModule;
import com.orangelabs.rcs.platform.AndroidFactory;
import com.orangelabs.rcs.platform.network.NetworkFactory;
import com.orangelabs.rcs.provider.settings.RcsSettings;
import com.orangelabs.rcs.provider.settings.RcsSettingsData;
import com.orangelabs.rcs.utils.logger.Logger;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.telephony.TelephonyManager;

import java.util.Random;

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
     * Network interfaces
     */
    private ImsNetworkInterface[] networkInterfaces = new ImsNetworkInterface[2];

    /**
     * IMS network interface
     */
    private ImsNetworkInterface currentNetworkInterface;
    
    /**
     * IMS polling thread
     */
    private Thread imsPollingThread = null;

    /**
     * IMS activation flag
     */
    private boolean imsActivationFlag = false;

    /**
     * Connectivity manager
     */
	private ConnectivityManager connectivityMgr;
	
	/**
	 * Network access type
	 */
	private int network;

	/**
	 * Operator
	 */
	private String operator;

	/**
	 * APN
	 */
	private String apn;

	/**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());
    
    /**
	 * Constructor
	 * 
	 * @param imsModule IMS module
	 * @throws CoreException
	 */
	public ImsConnectionManager(ImsModule imsModule) throws CoreException {
		this.imsModule = imsModule;

		// Get network access parameters
		network = RcsSettings.getInstance().getNetworkAccess();

		// Get network operator parameters
		operator = RcsSettings.getInstance().getNetworkOperator();
		apn = RcsSettings.getInstance().getNetworkApn();
		
		// Set the connectivity manager
		connectivityMgr = (ConnectivityManager)AndroidFactory.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		
        // Instanciates the IMS network interfaces
        networkInterfaces[0] = new MobileNetworkInterface(imsModule);
        networkInterfaces[1] = new WifiNetworkInterface(imsModule);

        // Set the mobile network interface by default
		currentNetworkInterface = getMobileNetworkInterface();

		// Reset the user profile
		resetUserProfile();
		
		// Register network state listener
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		AndroidFactory.getApplicationContext().registerReceiver(networkStateListener, intentFilter);
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
     * Returns the mobile network interface
     * 
     * @return Mobile network interface
     */
	public ImsNetworkInterface getMobileNetworkInterface() {
		return networkInterfaces[0];
	}
	
	/**
     * Returns the Wi-Fi network interface
     * 
     * @return Wi-Fi network interface
     */
	public ImsNetworkInterface getWifiNetworkInterface() {
		return networkInterfaces[1];
	}
	
	/**
	 * Reset the user profile associated to the network interface
	 */
	private void resetUserProfile() {
    	ImsModule.IMS_USER_PROFILE = currentNetworkInterface.getUserProfile();
	}
	
	/**
     * Terminate the connection manager
     */
    public void terminate() {
    	if (logger.isActivated()) {
    		logger.info("Terminate the IMS connection manager");
    	}

		// Unregister network state listener
		AndroidFactory.getApplicationContext().unregisterReceiver(networkStateListener);
    	
    	// Stop the IMS connection manager
    	stopImsConnection();
    	
    	// Unregister from the IMS
		currentNetworkInterface.unregister();
		    	
    	if (logger.isActivated()) {
    		logger.info("IMS connection manager has been terminated");
    	}
    }

    /**
     * Network state listener
     */
	private BroadcastReceiver networkStateListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
        	Thread t = new Thread() {
        		public void run() {
        			connectionEvent(intent.getAction());
        		}
        	};
        	t.start();
        }
    };    

    /**
     * Connection event
     * 
     * @param action Connectivity action
     */
    private void connectionEvent(String action) {
		if (logger.isActivated()) {
			logger.debug("Connection event " + action);
		}

		if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
	    	// Check received network info
	    	NetworkInfo networkInfo = connectivityMgr.getActiveNetworkInfo();
			if ((networkInfo == null) || (currentNetworkInterface == null)) {
				if (logger.isActivated()) {
					logger.debug("Disconnect from IMS: no network (e.g. air plane mode)");
				}
				disconnectFromIms();
				return;
			}
			
			// Save last network interface 
			ImsNetworkInterface lastNetworkInterface = currentNetworkInterface;
			
			// Check in the network access type has changed 
			if (networkInfo.getType() != currentNetworkInterface.getType()) {
				if (logger.isActivated()) {
					logger.info("Data connection state: NETWORK ACCESS CHANGED");
				}
			
				// Change of network interface
				if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
					if (logger.isActivated()) {
						logger.debug("Change the network interface to mobile");
					}
					currentNetworkInterface = getMobileNetworkInterface();
				} else
				if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
					if (logger.isActivated()) {
						logger.debug("Change the network interface to Wi-Fi");
					}
					currentNetworkInterface = getWifiNetworkInterface();
				}
				
				// Reset the user profile
				resetUserProfile();
				if (logger.isActivated()) {
					logger.debug("User profile has been reloaded");
				}
			}
			
			// Get the current local IP address
			String localIpAddr = NetworkFactory.getFactory().getLocalIpAddress();
			if (logger.isActivated()) {
				logger.debug("Local IP address is " + localIpAddr);
			}   				

			// Check if the IP address has changed
			if ((localIpAddr != null) &&
					!localIpAddr.equals(lastNetworkInterface.getNetworkAccess().getIpAddress())) {
				if (logger.isActivated()) {
					logger.debug("Disconnect from IMS: IP address has changed");
				}
				disconnectFromIms();
			}
			
			// Check if there is an IP connectivity
			if (networkInfo.isConnected()) {
				if (logger.isActivated()) {
					logger.info("Data connection state: CONNECTED to " + networkInfo.getTypeName());
				}
	
				// Test roaming
				if (networkInfo.isRoaming() &&
					(networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) &&
						(!RcsSettings.getInstance().isRoamingAuthorized())) {
					if (logger.isActivated()) {
						logger.warn("RCS not authorized in roaming");
					}
					return;
				}
				
				// Test the connected network
				if ((network != RcsSettingsData.ANY_ACCESS) && (network != networkInfo.getType())) {
					if (logger.isActivated()) {
						logger.warn("Network access " + networkInfo.getTypeName() + " is not authorized");
					}
					return;
				}
	
				// Test the operator id
				TelephonyManager tm = (TelephonyManager)AndroidFactory.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
				String currentOpe = tm.getSimOperatorName();
				if ((operator.length() > 0) && !currentOpe.equalsIgnoreCase(operator)) {
					if (logger.isActivated()) {
						logger.warn("Operator not authorized");
					}
					return;
				}
				
				// Test the default APN configuration
				ContentResolver cr = AndroidFactory.getApplicationContext().getContentResolver();
				String currentApn = null;
				Cursor c = cr.query(Uri.parse("content://telephony/carriers/preferapn"),
						new String[] { "apn" }, null, null, null);
				if (c != null) {
					final int apnIndex = c.getColumnIndexOrThrow("apn");
					if (c.moveToFirst()) {
						currentApn = c.getString(apnIndex);
					}
					c.close();
				}
				if ((apn.length() > 0) && !apn.equalsIgnoreCase(currentApn)) {
					if (logger.isActivated()) {
						logger.warn("APN not authorized");
					}
					return;
				}
									
				// Connect to IMS network interface
				if (logger.isActivated()) {
					logger.debug("Connect to IMS");
				}
				connectToIms(localIpAddr);
			} else {
				if (logger.isActivated()) {
					logger.info("Data connection state: DISCONNECTED from " + networkInfo.getTypeName());
				}
	
				// Disconnect from IMS network interface
				if (logger.isActivated()) {
					logger.debug("Disconnect from IMS: IP connection lost");
				}
				disconnectFromIms();
	    	}
	    }
    }    
    
    /**
     * Connect to IMS network interface
     * 
     * @param ipAddr IP address
     */
    private void connectToIms(String ipAddr) {
    	// Connected to the network access
		currentNetworkInterface.getNetworkAccess().connect(ipAddr);

		// Start the IMS connection
		startImsConnection();
    }
    
    /**
     * Disconnect from IMS network interface
     */
    private void disconnectFromIms() {
		// Stop the IMS connection
		stopImsConnection();

		// Registration terminated 
		currentNetworkInterface.registrationTerminated();
		
		// Disconnect from the network access
		currentNetworkInterface.getNetworkAccess().disconnect();
    }
    
	/**
	 * Start the IMS connection
	 */
	private synchronized void startImsConnection() {
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
	private synchronized void stopImsConnection() {
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
    		logger.debug("Start polling of the IMS connection");
    	}
    	
		int servicePollingPeriod = RcsSettings.getInstance().getImsServicePollingPeriod();
		int regBaseTime = RcsSettings.getInstance().getRegisterRetryBaseTime();
		int regMaxTime = RcsSettings.getInstance().getRegisterRetryMaxTime();
		Random random = new Random();
		int nbFailures = 0;

		while(imsActivationFlag) {
	    	if (logger.isActivated()) {
	    		logger.debug("Polling: check IMS connection");
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
    	            	
    	            	// Start IMS services
        	        	imsModule.startImsServices();
        	        	
        	        	// Reset number of failures
        	        	nbFailures = 0;
    	    		} else {
    	            	if (logger.isActivated()) {
    	            		logger.debug("Can't register to the IMS");
    	            	}
    	            	
    	            	// Increment number of failures
    	            	nbFailures++;
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

			// Make a pause before the next polling
	    	try {
    			if (!currentNetworkInterface.isRegistered()) {
    				// Pause before the next register attempt
    				double w = Math.min(regMaxTime, (regBaseTime * Math.pow(2, nbFailures)));
    				double coeff = (random.nextInt(51) + 50) / 100.0; // Coeff between 50% and 100%
    				int retryPeriod = (int)(coeff * w);
    	        	if (logger.isActivated()) {
    	        		logger.debug("Wait " + retryPeriod + "s before retry registration (failures=" + nbFailures + ", coeff="+ coeff + ")");
    	        	}
    				Thread.sleep(retryPeriod * 1000);
	    		} else {
    				// Pause before the next service check
	    			Thread.sleep(servicePollingPeriod * 1000);
	    		}
            } catch (InterruptedException e) {
                break;
            }		    	
		}

		if (logger.isActivated()) {
    		logger.debug("IMS connection polling is terminated");
    	}
	}
}
