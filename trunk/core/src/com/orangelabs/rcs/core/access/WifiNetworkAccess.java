package com.orangelabs.rcs.core.access;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.orangelabs.rcs.core.CoreException;
import com.orangelabs.rcs.platform.AndroidFactory;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Wifi access network
 * 
 * @author jexa7410
 */
public class WifiNetworkAccess extends NetworkAccess {
	/**
	 * Wi-fi manager
	 */
	private WifiManager wifiManager;
	
	/**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());

	/**
	 * Constructor
	 * 
     * @throws CoreException
	 */
	public WifiNetworkAccess() throws CoreException {
		super();

		// Get wi-fi info
		wifiManager = (WifiManager)AndroidFactory.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

		if (logger.isActivated()) {
    		logger.info("Wi-fi access has been created");
    	}
    }
			
	/**
     * Connect to the network access
     * 
     * @param ipAddress IP address
     */
    public void connect(String ipAddress) {
		this.ipAddress = ipAddress;
    }
    
	/**
     * Disconnect from the network access
     */
    public void disconnect() {
    	ipAddress = null;
    }
    
	/**
	 * Return the type of access
	 * 
	 * @return Type
	 */
	public String getType() {
		WifiInfo info = wifiManager.getConnectionInfo();
		if (info.getLinkSpeed() <= 11) {
			return "IEEE-802.11b";
		} else {
			return "IEEE-802.11a";
		}
	}    
}