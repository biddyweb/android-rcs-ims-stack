package com.orangelabs.rcs.core.ims.service.sharing.transfer;

import com.orangelabs.rcs.core.ims.service.ImsSessionListener;
import com.orangelabs.rcs.core.ims.service.sharing.ContentSharingError;

/**
 * Content sharing transfer session listener
 * 
 * @author jexa7410
 */
public interface ContentSharingTransferSessionListener extends ImsSessionListener  {
	/**
	 * Content sharing progress
	 * 
	 * @param currentSize Data size transfered 
	 * @param totalSize Total size to be transfered
	 */
    public void handleSharingProgress(long currentSize, long totalSize);
    
    /**
     * Content sharing error
     * 
     * @param error Error
     */
    public void handleSharingError(ContentSharingError error);
    
    /**
     * Content has been transfered
     * 
     * @param filename Filename associated to the received content
     */
    public void handleContentTransfered(String filename);
}
