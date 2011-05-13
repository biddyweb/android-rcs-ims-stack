/*******************************************************************************
 * Software Name : RCS IMS Stack
 * Version : 2.0
 * 
 * Copyright © 2010 France Telecom S.A.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.orangelabs.rcs.core.ims.service.presence;

import java.util.ArrayList;
import java.util.Vector;

import javax.sip.header.AcceptHeader;
import javax.sip.header.EventHeader;

import com.orangelabs.rcs.addressbook.ContactsManager;
import com.orangelabs.rcs.core.CoreException;
import com.orangelabs.rcs.core.ims.ImsModule;
import com.orangelabs.rcs.core.ims.network.sip.SipManager;
import com.orangelabs.rcs.core.ims.network.sip.SipMessageFactory;
import com.orangelabs.rcs.core.ims.network.sip.SipUtils;
import com.orangelabs.rcs.core.ims.protocol.sip.SipDialogPath;
import com.orangelabs.rcs.core.ims.protocol.sip.SipException;
import com.orangelabs.rcs.core.ims.protocol.sip.SipRequest;
import com.orangelabs.rcs.core.ims.protocol.sip.SipResponse;
import com.orangelabs.rcs.core.ims.protocol.sip.SipTransactionContext;
import com.orangelabs.rcs.core.ims.service.SessionAuthenticationAgent;
import com.orangelabs.rcs.core.ims.service.capability.Capabilities;
import com.orangelabs.rcs.utils.PhoneUtils;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Anonymous fetch procedure which permits to request the capabilities
 * for a list of contacts thanks to a one shot subscribe
 */
public class MultipleAnonymousFetchCapabilities extends Thread {
    /**
     * IMS module
     */
    private ImsModule imsModule;
    
    /**
     * Presentities
     */
    private ArrayList<String> presentities;
    
	/***
	 * Refresh timeout (in seconds)
	 */
	private int refreshTimeout;
    
    /**
     * Dialog path
     */
    private SipDialogPath dialogPath = null;
    
    /**
	 * Authentication agent
	 */
	private SessionAuthenticationAgent authenticationAgent = new SessionAuthenticationAgent();

	/**
     * The log4j logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     * Constructor
     * 
     * @param parent IMS module
     * @param presentities List of presentity
     * @param 
     */
    public MultipleAnonymousFetchCapabilities(ImsModule parent, ArrayList<String> presentities, int refreshTimeout) {
        this.imsModule = parent;
        this.presentities = presentities;
        this.refreshTimeout = refreshTimeout;
    }
    
	/**
	 * Backgroung processing
	 */
	public void run() {
		// Stop the monitoring the address book changes while doing the queries, as it will generate other checks
		imsModule.getCore().getAddressBookManager().pauseAddressBookMonitoring();
		
		for (int i=0;i<presentities.size();i++){

			String contact = presentities.get(i);
			// Read capabilities from the database
			Capabilities capabilities = ContactsManager.getInstance().getContactCapabilities(contact);
			if (capabilities == null) {
		    	if (logger.isActivated()) {
		    		logger.debug("No capabilities exist for " + contact);
		    	}

				// Send the request for a contact
				subscribe(contact);
			} else {
		    	if (logger.isActivated()) {
		    		logger.debug("Capabilities exist for " + contact);
		    	}
				long delta = (System.currentTimeMillis()-capabilities.getTimestamp())/1000;
				if ((delta > refreshTimeout) || (delta < 0)) {
			    	if (logger.isActivated()) {
			    		logger.debug("Capabilities have expired for " + contact);
			    	}

			    	// Capabilities are too old: request a new anonymous fetch
					subscribe(contact);
				}
			}
		}
		
		// Monitor the address book changes again
		imsModule.getCore().getAddressBookManager().resumeAddressBookMonitoring();
		
	}

    /**
     * One-shot subscribe for a contact
     * 
     * @param presentity
     */
    private void subscribe(String presentity) {
    	if (logger.isActivated()) {
    		logger.info("One shot subscribe for " + presentity);
    	}

    	try {
	        // Create a dialog path
    		String contactUri = PhoneUtils.formatNumberToSipAddress(presentity);

        	// Set Call-Id
        	String callId = imsModule.getSipManager().getSipStack().generateCallId();

        	// Set target
        	String target = contactUri;

            // Set local party
        	String localParty = "sip:anonymous@" + ImsModule.IMS_USER_PROFILE.getHomeDomain();

        	// Set remote party
        	String remoteParty = contactUri;

        	// Set the route path
        	Vector<String> route = imsModule.getSipManager().getSipStack().getServiceRoutePath();

        	// Create a dialog path
        	dialogPath = new SipDialogPath(
            		imsModule.getSipManager().getSipStack(),
            		callId,
            		1,
            		target,
            		localParty,
            		remoteParty,
            		route);
            
            // Send a SUBSCRIBE
        	SipRequest subscribe = createSubscribe();
	        sendSubscribe(subscribe, presentity);
        } catch (Exception e) {
        	if (logger.isActivated()) {
        		logger.error("Subscribe has failed", e);
        	}
        	handleError(new PresenceError(PresenceError.UNEXPECTED_EXCEPTION, e.getMessage()), presentity);
        }        
    }

	/**
     * Create a SUBSCRIBE request
     * 
	 * @return SIP request
	 * @throws SipException
	 * @throws CoreException
     */
    private SipRequest createSubscribe() throws SipException, CoreException {
    	SipRequest subscribe = SipMessageFactory.createSubscribe(dialogPath,
    			0,
    			imsModule.getCurrentNetworkInterface().getAccessInfo());
    	
    	// Set the Privacy header
    	subscribe.addHeader(SipUtils.HEADER_PRIVACY, "id");
    	
    	// Set the Event header
    	subscribe.addHeader(EventHeader.NAME, "presence");

    	// Set the Accept header
    	subscribe.addHeader(AcceptHeader.NAME, "application/pidf+xml");
    	
    	return subscribe;
    }

    /**
	 * Send SUBSCRIBE message
	 * 
	 * @param subscribe SIP SUBSCRIBE
	 * @param presentity
	 * @throws Exception
	 */
	private void sendSubscribe(SipRequest subscribe, String presentity) throws Exception {
        // Send a SUBSCRIBE
        if (logger.isActivated()) {
        	logger.info("Send SUBSCRIBE, expire=" + subscribe.getExpires());
        }

        // Send message
        SipTransactionContext ctx = imsModule.getSipManager().sendSipMessageAndWait(subscribe);

        // Wait response
        if (logger.isActivated()) {
        	logger.info("Wait response");
        }
        ctx.waitResponse(SipManager.TIMEOUT);
        
        // Analyze the received response 
        if (ctx.isSipResponse()) {
        	// A response has been received
            if ((ctx.getStatusCode() >= 200) && (ctx.getStatusCode() < 300)) {
            	// 200 OK
    			handle200OK(ctx);
            } else
            if ((ctx.getStatusCode() == 403) || (ctx.getStatusCode() == 404)) {
            	// Not an IMS user
            	handleUserNotFound(ctx, presentity);
            } else
            if (ctx.getStatusCode() == 407) {
            	// 407 Proxy Authentication Required
            	handle407Authentication(ctx, presentity);
            } else {
            	// Other error response
    			handleError(new PresenceError(PresenceError.SUBSCRIBE_FAILED,
    					ctx.getStatusCode() + " " + ctx.getReasonPhrase()), presentity);    					
            }
        } else {
    		if (logger.isActivated()) {
        		logger.debug("No response received for SUBSCRIBE");
        	}

    		// No response received: timeout
        	handleError(new PresenceError(PresenceError.SUBSCRIBE_FAILED), presentity);
        }
	}    

	/**
	 * Handle 200 0K response 
	 * 
	 * @param ctx SIP transaction context
	 */
	private void handle200OK(SipTransactionContext ctx) {
        // 200 OK response received
        if (logger.isActivated()) {
            logger.info("200 OK response received");
        }
	}
	
    /**
	 * Handle 407 response 
	 * 
	 * @param ctx SIP transaction context
	 * @param presentity
	 * @throws Exception
	 */
	private void handle407Authentication(SipTransactionContext ctx, String presentity) throws Exception {
        // 407 response received
    	if (logger.isActivated()) {
    		logger.info("407 response received");
    	}

    	SipResponse resp = ctx.getSipResponse();

    	// Set the Proxy-Authorization header
    	authenticationAgent.readProxyAuthenticateHeader(resp);

        // Increment the Cseq number of the dialog path
        dialogPath.incrementCseq();

        // Send a second SUBSCRIBE with the right token
        if (logger.isActivated()) {
        	logger.info("Send second SUBSCRIBE");
        }
    	SipRequest subscribe = createSubscribe();
    	
        // Set the Authorization header
        authenticationAgent.setProxyAuthorizationHeader(subscribe);
    	
        // Send message
    	sendSubscribe(subscribe, presentity);
	}	
	
	/**
	 * Handle error response 
	 * 
	 * @param error Error
	 * @param presentity
	 */
	private void handleError(PresenceError error, String presentity) {
        // On error don't modify the existing capabilities
    	if (logger.isActivated()) {
    		logger.info("Subscribe has failed: " + error.getErrorCode() + ", reason=" + error.getMessage());
    	}
    	
    	// We update the database capability timestamp
    	ContactsManager.getInstance().setContactCapabilitiesTimestamp(presentity, System.currentTimeMillis());	
	}

	/**
	 * Handle user not found 
	 * 
	 * @param ctx SIP transaction context
	 */
	private void handleUserNotFound(SipTransactionContext ctx, String presentity) {
        if (logger.isActivated()) {
            logger.info("User not found (" + ctx.getStatusCode() + " error)");
        }

        // We update the database with empty capabilities.
    	Capabilities capabilities = new Capabilities();
    	ContactsManager.getInstance().setContactCapabilities(presentity, capabilities, false);
	}
}
