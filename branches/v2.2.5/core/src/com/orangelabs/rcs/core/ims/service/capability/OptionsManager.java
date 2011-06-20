package com.orangelabs.rcs.core.ims.service.capability;

import com.orangelabs.rcs.core.ims.ImsModule;
import com.orangelabs.rcs.core.ims.network.sip.SipMessageFactory;
import com.orangelabs.rcs.core.ims.network.sip.SipUtils;
import com.orangelabs.rcs.core.ims.protocol.sip.SipRequest;
import com.orangelabs.rcs.core.ims.protocol.sip.SipResponse;
import com.orangelabs.rcs.provider.eab.ContactsManager;
import com.orangelabs.rcs.service.api.client.capability.Capabilities;
import com.orangelabs.rcs.service.api.client.contacts.ContactInfo;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Capability discovery manager using options procedure
 *  
 * @author jexa7410
 */
public class OptionsManager implements DiscoveryManager {
    /**
     * IMS module
     */
    private ImsModule imsModule;
    
    /**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     * Constructor
     * 
     * @param parent IMS module
     */
    public OptionsManager(ImsModule parent) {
        this.imsModule = parent;
    }
    
	/**
     * Request contact capabilities
     * 
     * @param contact Remote contact
     */
    public void requestCapabilities(String contact) {
    	if (logger.isActivated()) {
    		logger.debug("Request capabilities in background for " + contact);
    	}
    	
    	// Update capability timestamp
    	ContactsManager.getInstance().setContactCapabilitiesTimestamp(contact, System.currentTimeMillis());
    	
		// Check if we are in call with the contact
		boolean inCall = imsModule.isRichcallServiceActivated() && imsModule.getCallManager().isConnectedWith(contact);
		
    	// Start request in background
    	OptionsRequestTask task = new OptionsRequestTask(imsModule, contact, CapabilityUtils.getSupportedFeatureTags(inCall));
    	task.start();
    }
    
    /**
     * Receive a capability request (options procedure)
     * 
     * @param options Received options message
     */
    public void receiveCapabilityRequest(SipRequest options) {
    	String contact = SipUtils.getAssertedIdentity(options);

    	if (logger.isActivated()) {
			logger.debug("OPTIONS request received from " + contact);
		}
    	
	    try {
	    	// Create 200 OK response
	        SipResponse resp = SipMessageFactory.create200OkOptionsResponse(options,
	        		imsModule.getSipManager().getSipStack().getContactHeader(),
	        		CapabilityUtils.getSupportedFeatureTags(false), null);

	        // Send 200 OK response
	        imsModule.getSipManager().sendSipResponse(resp);
	    } catch(Exception e) {
        	if (logger.isActivated()) {
        		logger.error("Can't send 200 OK for OPTIONS", e);
        	}
	    }

    	// Read features tag in the request
    	Capabilities capabilities = CapabilityUtils.extractCapabilities(options);

    	// Update capabilities in database
    	if (capabilities.isImSessionSupported()) {
    		// RCS-e contact
    		ContactsManager.getInstance().setContactCapabilities(contact, capabilities, ContactInfo.RCS_CAPABLE, ContactsManager.REGISTRATION_STATUS_ONLINE);
    	} else {
    		// Not a RCS-e contact
    		ContactsManager.getInstance().setContactCapabilities(contact, capabilities, ContactInfo.NOT_RCS, ContactsManager.REGISTRATION_STATUS_UNKNOWN);
    	}
    	
    	// Notify listener
    	imsModule.getCore().getListener().handleCapabilitiesNotification(contact, capabilities);    	
    }
}