package com.orangelabs.rcs.core.ims.service.toip;

import com.orangelabs.rcs.core.ims.service.ImsSessionListener;

/**
 * ToIP session listener
 * 
 * @author JM. Auffret
 */
public interface ToIpSessionListener extends ImsSessionListener {
    /**
     * ToIP error
     * 
     * @param error Error
     */
    public void handleToIpError(ToIpError error);
}