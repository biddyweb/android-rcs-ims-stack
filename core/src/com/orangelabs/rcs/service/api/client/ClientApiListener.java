package com.orangelabs.rcs.service.api.client;

/**
 * Client API listener
 * 
 * @author jexa7410
 */
public interface ClientApiListener {
    /**
     * API is connected to the server
     */
    public void handleApiConnected();

    /**
     * API is disconnected from the server
     */
    public void handleApiDisconnected();
}
