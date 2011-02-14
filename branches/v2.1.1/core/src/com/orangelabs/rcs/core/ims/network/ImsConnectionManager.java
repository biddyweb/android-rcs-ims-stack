/*******************************************************************************
 * Software Name : RCS IMS Stack
 * Version : 2.0
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

import com.orangelabs.rcs.core.CoreException;
import com.orangelabs.rcs.core.ims.ImsModule;
import com.orangelabs.rcs.platform.AndroidFactory;
import com.orangelabs.rcs.platform.network.NetworkFactory;
import com.orangelabs.rcs.provider.settings.RcsSettings;
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
	 * Network access
	 */
	private String network;

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

		// Get network operator parameters
		network = imsModule.getCore().getConfig().getString("NetworkAccess", "");
		operator = imsModule.getCore().getConfig().getString("Operator", "");
		apn = imsModule.getCore().getConfig().getString("APN", "");
		
		// Set the connectivity manager
		connectivityMgr = (ConnectivityManager)AndroidFactory.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		
        // Instanciates the IMS network interfaces
        networkInterfaces[0] = new MobileNetworkInterface(imsModule);
        networkInterfaces[1] = new WifiNetworkInterface(imsModule);

        // Set the current network interface
		currentNetworkInterface = getMobileNetworkInterface();
	
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
     * Returns the Wi-fi network interface
     * 
     * @return Wi-fi network interface
     */
	public ImsNetworkInterface getWifiNetworkInterface() {
		return networkInterfaces[1];
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
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            	           	
				NetworkInfo netInfo = connectivityMgr.getActiveNetworkInfo();

				if ((netInfo != null) && (netInfo.getType() != currentNetworkInterface.getType())) {
					if (logger.isActivated()) {
						logger.info("Data connection state: FAILOVER");
					}
				
					// Disconnect from IMS network interface
					disconnectFromIms();
					
					// Change of network interface
					if (netInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
						if (logger.isActivated()) {
							logger.debug("Set the network interface to mobile");
						}
						currentNetworkInterface = getMobileNetworkInterface();
					} else
					if (netInfo.getType() == ConnectivityManager.TYPE_WIFI) {
						if (logger.isActivated()) {
							logger.debug("Set the network interface to Wi-fi");
						}
						currentNetworkInterface = getWifiNetworkInterface();
					}
				}
				
				if ((netInfo != null) && (netInfo.isConnected())) {
    				if (logger.isActivated()) {
    					logger.info("Data connection state: CONNECTED to " + netInfo.getTypeName());
    				}

    				if (netInfo.isRoaming() &&
    					(netInfo.getType() == ConnectivityManager.TYPE_MOBILE) &&
    						(!RcsSettings.getInstance().isRoamingAuthorized())) {
						if (logger.isActivated()) {
    						logger.warn("RCS not authorized in roaming");
    					}
						return;
    				}
    				
    				// Test the connected network
    				if ((network.length() > 0) && (!network.equalsIgnoreCase(netInfo.getTypeName()))) {
    					if (logger.isActivated()) {
    						logger.warn("Access type not authorized");
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
    				Cursor c = cr.query(
    						Uri.parse("content://telephony/carriers/preferapn"),
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

    				// Get the current local IP address
    				String localIpAddr = NetworkFactory.getFactory().getLocalIpAddress();
    				if (logger.isActivated()) {
    					logger.debug("Local IP address is " + localIpAddr);
    				}   				

    				// Connect to IMS network interface
    				connectToIms(localIpAddr);
    			} else {
    				if (logger.isActivated()) {
    					logger.info("Data connection state: DISCONNECTED");
    				}

    				// Disconnect from IMS network interface
    				disconnectFromIms();
            	}
            }
        }
    };    
    
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
    	
		long registerPollingPeriod = RcsSettings.getInstance().getImsConnectionPollingPeriod();
		long servicePollingPeriod = RcsSettings.getInstance().getImsServicePollingPeriod();

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

			// Make a pause before the next polling
	    	try {
    			if (!currentNetworkInterface.isRegistered()) {
    				// Pause before the next register attempt
	    			Thread.sleep(registerPollingPeriod * 1000);
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
