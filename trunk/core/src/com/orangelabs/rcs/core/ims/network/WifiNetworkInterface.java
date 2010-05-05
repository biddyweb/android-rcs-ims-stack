package com.orangelabs.rcs.core.ims.network;

import com.orangelabs.rcs.core.CoreException;
import com.orangelabs.rcs.core.access.WifiNetworkAccess;
import com.orangelabs.rcs.core.ims.ImsModule;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Wi-fi network interface
 *  
 * @author JM. Auffret
 */
public class WifiNetworkInterface extends ImsNetworkInterface {
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
    public WifiNetworkInterface(ImsModule imsModule) throws CoreException {
    	super(imsModule, new WifiNetworkAccess());    	

    	if (logger.isActivated()) {
    		logger.info("Wi-fi network interface has been loaded");
    	}
    }
}