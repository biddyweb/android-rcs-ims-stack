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

import java.util.Vector;

import com.orangelabs.rcs.core.CoreException;
import com.orangelabs.rcs.core.ims.ImsModule;
import com.orangelabs.rcs.core.ims.network.sip.SipManager;
import com.orangelabs.rcs.core.ims.network.sip.SipMessageFactory;
import com.orangelabs.rcs.core.ims.protocol.sip.SipDialogPath;
import com.orangelabs.rcs.core.ims.protocol.sip.SipException;
import com.orangelabs.rcs.core.ims.protocol.sip.SipRequest;
import com.orangelabs.rcs.core.ims.protocol.sip.SipResponse;
import com.orangelabs.rcs.core.ims.protocol.sip.SipTransactionContext;
import com.orangelabs.rcs.core.ims.service.SessionAuthenticationAgent;
import com.orangelabs.rcs.utils.PhoneUtils;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Anonymous fetch procedure which permits to request the capabilities
 * for a given contact thanks to a one shot subscribe.
 * 
 * @author jexa7410
 */
public class AnonymousFetchCapabilities extends Thread {
    /**
     * IMS module
     */
    private ImsModule imsModule;
    
    /**
     * Presentity
     */
    private String presentity;
    
    /**
     * Subscription flag
     */
    private boolean subscribed = false;
    
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
     * @param presentity Presentity
     */
    public AnonymousFetchCapabilities(ImsModule parent, String presentity) {
        this.imsModule = parent;
        this.presentity = presentity;
    }
    
	/**
	 * Backgroung processing
	 */
	public void run() {
		subscribe();
	}

    /**
     * One-shot subscribe
     */
    private void subscribe() {
    	if (logger.isActivated()) {
    		logger.info("One shot subscribe for " + presentity);
    	}

    	try {
	        // Create a dialog path
    		String contactUri = PhoneUtils.formatNumberToSipAddress(presentity);

        	// Set Call-Id
        	String callId = imsModule.getSipManager().generateCallId();

        	// Set target
        	String target = contactUri;

            // Set local party
        	String localParty = "sip:anonymous@" + ImsModule.IMS_USER_PROFILE.getHomeDomain();

        	// Set remote party
        	String remoteParty = contactUri;

        	// Set the route path
        	Vector<String> route = imsModule.getSipManager().getSipStack().getDefaultRoutePath();

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
	        sendSubscribe(subscribe);
        } catch (Exception e) {
        	if (logger.isActivated()) {
        		logger.error("Subscribe has failed", e);
        	}
        	handleError(new PresenceError(PresenceError.UNEXPECTED_EXCEPTION, e.getMessage()));
        }        
    }

	/**
     * Create a SUBSCRIBE request
     * 
	 * @return SIP request
	 * @throws SipException
	 * @throws CoreException
     */
    public SipRequest createSubscribe() throws SipException, CoreException {
    	SipRequest subscribe = SipMessageFactory.createSubscribe(dialogPath,
    			0,
    			imsModule.getCurrentNetworkInterface().getAccessInfo());
    	
    	// Set the Event header
    	subscribe.addHeader("Event: presence");

    	// Set the Accept header
    	subscribe.addHeader("Accept: application/pidf+xml");
    	
    	return subscribe;
    }

    /**
	 * Send SUBSCRIBE message
	 * 
	 * @param subscribe SIP SUBSCRIBE
	 * @throws Exception
	 */
	private void sendSubscribe(SipRequest subscribe) throws Exception {
        // Send a SUBSCRIBE
        if (logger.isActivated()) {
        	logger.info("Send SUBSCRIBE, expire=" + subscribe.getExpires());
        }

        if (subscribed) {
	        // Set the Authorization header
            authenticationAgent.setProxyAuthorizationHeader(subscribe);
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
            if (ctx.getStatusCode() == 200) {
            	// 200 OK
    			handle200OK(ctx);
            } else
            if (ctx.getStatusCode() == 407) {
            	// 407 Proxy Authentication Required
            	handle407Authentication(ctx);
            } else {
            	// Other error response
    			handleError(new PresenceError(PresenceError.SUBSCRIBE_FAILED, ctx.getReasonPhrase()));    					
            }
        } else {
    		if (logger.isActivated()) {
        		logger.debug("No response received for SUBSCRIBE");
        	}

    		// No response received: timeout
        	handleError(new PresenceError(PresenceError.SUBSCRIBE_FAILED));
        }
	}    

	/**
	 * Handle 200 0K response 
	 * 
	 * @param ctx SIP transaction context
	 */
	private void handle200OK(SipTransactionContext ctx) {
        // 20x response received
        if (logger.isActivated()) {
            logger.info("20x response received");
        }
        subscribed = true;
	}
	
    /**
	 * Handle 407 response 
	 * 
	 * @param ctx SIP transaction context
	 * @throws Exception
	 */
	private void handle407Authentication(SipTransactionContext ctx) throws Exception {
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
    	sendSubscribe(subscribe);
	}	
	
	/**
	 * Handle error response 
	 * 
	 * @param error Error
	 */
	private void handleError(PresenceError error) {
        // Error
    	if (logger.isActivated()) {
    		logger.info("Subscribe has failed: " + error.getErrorCode() + ", reason=" + error.getMessage());
    	}
        subscribed = false;
        
		// Notify listener
    	imsModule.getCore().getListener().handleAnonymousFetchNotification(presentity, null);
    }
}
