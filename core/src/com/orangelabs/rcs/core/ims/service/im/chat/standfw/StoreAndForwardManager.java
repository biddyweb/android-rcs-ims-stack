package com.orangelabs.rcs.core.ims.service.im.chat.standfw;

import com.orangelabs.rcs.core.ims.protocol.sip.SipRequest;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Store & forward manager
 */
public class StoreAndForwardManager {
	/**
	 * Store & forward service URI
	 */
	public final static String SERVICE_URI = "rcse-standfw@";
	
    /**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());
    
    /**
     * Constructor
     */    
    public StoreAndForwardManager() {
    }
    
    /**
     * Receive stored messages
     * 
     * @param invite Received invite
     */
    public void receiveStoredMessages(SipRequest invite) {
    	if (logger.isActivated()) {
			logger.debug("Receive stored messages");
		}    	
    	// TODO: auto-accept PUSH messages
    }    
}
