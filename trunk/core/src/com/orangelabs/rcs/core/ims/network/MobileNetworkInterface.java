package com.orangelabs.rcs.core.ims.network;

import com.orangelabs.rcs.core.CoreException;
import com.orangelabs.rcs.core.access.MobileNetworkAccess;
import com.orangelabs.rcs.core.ims.ImsModule;

/**
 * Mobile network interface
 *  
 * @author JM. Auffret
 */
public class MobileNetworkInterface extends ImsNetworkInterface {
    /**
     * Constructor
     * 
     * @param imsModule IMS module
     * @throws CoreException
     */
    public MobileNetworkInterface(ImsModule imsModule) throws CoreException {
    	super(imsModule, new MobileNetworkAccess());    	
    }
}