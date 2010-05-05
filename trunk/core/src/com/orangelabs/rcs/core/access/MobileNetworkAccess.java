package com.orangelabs.rcs.core.access;

import android.content.Context;
import android.telephony.TelephonyManager;

import com.orangelabs.rcs.core.CoreException;
import com.orangelabs.rcs.platform.AndroidFactory;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Mobile access network
 * 
 * @author jexa7410
 */
public class MobileNetworkAccess extends NetworkAccess {
	/**
	 * Telephony manager
	 */
	private TelephonyManager telephonyManager;
	
    /**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());

	/**
	 * Constructor
	 * 
     * @throws CoreException
	 */
	public MobileNetworkAccess() throws CoreException {
		super();

		// Get telephony info
		telephonyManager = (TelephonyManager)AndroidFactory.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);

		if (logger.isActivated()) {
    		logger.info("Mobile access has been created");
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
		int type = telephonyManager.getNetworkType();
		switch(type) {
			case TelephonyManager.NETWORK_TYPE_UNKNOWN:
				return null;
			case TelephonyManager.NETWORK_TYPE_GPRS:
				return "3GPP-GERAN";
			case TelephonyManager.NETWORK_TYPE_EDGE:
				return "3GPP-GERAN";
			case TelephonyManager.NETWORK_TYPE_UMTS:
				return "3GPP-UTRAN-TDD";
			default:
				return null;
		}
	}
}
