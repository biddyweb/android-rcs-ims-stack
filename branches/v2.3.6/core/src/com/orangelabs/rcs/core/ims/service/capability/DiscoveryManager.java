package com.orangelabs.rcs.core.ims.service.capability;

/**
 * Discovery manager interface
 * 
 * @author jexa7410
 */
public interface DiscoveryManager {
	/**
     * Request contact capabilities
     * 
     * @param contact Remote contact
     * @return Returns true if success
     */
    public boolean requestCapabilities(String contact);
}
