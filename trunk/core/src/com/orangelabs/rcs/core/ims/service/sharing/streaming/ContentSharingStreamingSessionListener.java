package com.orangelabs.rcs.core.ims.service.sharing.streaming;

import com.orangelabs.rcs.core.ims.service.ImsSessionListener;
import com.orangelabs.rcs.core.ims.service.sharing.ContentSharingError;

/**
 * Content sharing streaming session listener
 * 
 * @author jexa7410
 */
public interface ContentSharingStreamingSessionListener extends ImsSessionListener  {
    /**
     * Content sharing error
     * 
     * @param error Error
     */
    public void handleSharingError(ContentSharingError error);    
}
