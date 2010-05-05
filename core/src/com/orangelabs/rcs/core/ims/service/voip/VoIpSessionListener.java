package com.orangelabs.rcs.core.ims.service.voip;

import com.orangelabs.rcs.core.ims.service.ImsSessionListener;

/**
 * VoIP session listener
 * 
 * @author JM. Auffret
 */
public interface VoIpSessionListener extends ImsSessionListener {
    /**
     * VoIP error
     * 
     * @param error Error
     */
    public void handleVoIpError(VoIpError error);
}