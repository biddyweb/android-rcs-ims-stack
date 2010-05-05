package com.orangelabs.rcs.core.ims.service;

/**
 * Listener of events sent during an IMS session
 * 
 * @author JM. Auffret
 */
public interface ImsSessionListener {
	/**
	 * Session is started
	 */
    public void handleSessionStarted();

    /**
     * Session has been aborted
     */
    public void handleSessionAborted();
    
    /**
     * Session has been terminated
     */
    public void handleSessionTerminated();
    
    /**
     * Session has been terminated by remote
     */
    public void handleSessionTerminatedByRemote();
}