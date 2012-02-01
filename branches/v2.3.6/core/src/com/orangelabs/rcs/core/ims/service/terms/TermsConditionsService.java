/*******************************************************************************
 * Software Name : RCS IMS Stack
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

package com.orangelabs.rcs.core.ims.service.terms;

import java.io.ByteArrayInputStream;

import org.xml.sax.InputSource;

import com.orangelabs.rcs.core.CoreException;
import com.orangelabs.rcs.core.ims.ImsModule;
import com.orangelabs.rcs.core.ims.protocol.sip.SipRequest;
import com.orangelabs.rcs.core.ims.service.ImsService;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Terms & conditions service
 * 
 * @author jexa7410
 */
public class TermsConditionsService extends ImsService {
	/**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());

	/**
	 * Is a terms & conditions request
	 * 
     * @param request Request
     * @return Boolean
	 */
	public static boolean isTermsRequest(SipRequest request) {
    	String contentType = request.getContentType();
    	if ((contentType != null) &&
    			contentType.startsWith("application/end-user-confirmation")) {
    		return true;
    	} else {
    		return false;
    	}
	}

	/**
     * Constructor
     * 
     * @param parent IMS module
     * @throws CoreException
     */
	public TermsConditionsService(ImsModule parent) throws CoreException {
        super(parent, true);
	}

	/**
	 * Start the IMS service
	 */
	public synchronized void start() {
		if (isServiceStarted()) {
			// Already started
			return;
		}
		setServiceStarted(true);
	}

    /**
     * Stop the IMS service
     */
	public synchronized void stop() {
		if (!isServiceStarted()) {
			// Already stopped
			return;
		}
		setServiceStarted(false);
	}

	/**
     * Check the IMS service
     */
	public void check() {
	}

	/**
     * Receive a message
     * 
     * @param message Received message
     */
    public void receiveMessage(SipRequest message) {
    	if (logger.isActivated()) {
    		logger.debug("Receive end user confirmation message");
    	}
    	
    	try {
	    	if (message.getContentType().equals("application/end-user-confirmation-request+xml")) {
		    	// Parse content
				InputSource input = new InputSource(new ByteArrayInputStream(message.getContentBytes()));
				TermsRequestParser parser = new TermsRequestParser(input);

				// Notify listener
	    		getImsModule().getCore().getListener().handleUserConfirmationRequest(
	    				parser.getId(),
	    				parser.getType(),
	    				parser.getPin(),
	    				parser.getSubject(),
	    				parser.getText());
	    	} else
	    	if (message.getContentType().equals("application/end-user-confirmation-ack+xml")) {
		    	// Parse content
				InputSource input = new InputSource(new ByteArrayInputStream(message.getContentBytes()));
				TermsAckParser parser = new TermsAckParser(input);

				// Notify listener
	    		getImsModule().getCore().getListener().handleUserConfirmationAck(
	    				parser.getId(),
	    				parser.getStatus(),
	    				parser.getSubject(),
	    				parser.getText());
	    	} else {
	    		if (logger.isActivated()) {
	    			logger.debug("Unknown user confirmation request");
	    		}
	    	}
    	} catch(Exception e) {
    		if (logger.isActivated()) {
    			logger.error("Can't parse user confirmation message", e);
    		}
    	}
    }

    /**
	 * Accept terms & conditions
	 */
	public void acceptTerms() {
		if (logger.isActivated()) {
			logger.info("Accept terms & conditions");
		}
		// TODO
	}

	/**
	 * Reject terms & conditions
	 */
	public void rejectTerms() {
		if (logger.isActivated()) {
			logger.info("Reject terms & conditions");
		}
		// TODO
	}
}
