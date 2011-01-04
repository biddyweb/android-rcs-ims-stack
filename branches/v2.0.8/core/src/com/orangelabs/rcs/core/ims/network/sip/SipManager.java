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
package com.orangelabs.rcs.core.ims.network.sip;

import com.orangelabs.rcs.core.ims.network.ImsNetworkInterface;
import com.orangelabs.rcs.core.ims.protocol.sip.IdGenerator;
import com.orangelabs.rcs.core.ims.protocol.sip.SipException;
import com.orangelabs.rcs.core.ims.protocol.sip.SipMessage;
import com.orangelabs.rcs.core.ims.protocol.sip.SipStack;
import com.orangelabs.rcs.core.ims.protocol.sip.SipTransactionContext;
import com.orangelabs.rcs.utils.NetworkRessourceManager;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * SIP manager
 *  
 * @author JM. Auffret
 */
public class SipManager {

	/**
	 * SIP timeout (in seconds)
	 */
	public static int TIMEOUT = 30;
	   
	/**
	 * Outbound proxy address
	 */
	private String outboundProxy;
	
	/**
	 * Default listening port
	 */
	private int defaultListeningPort;

	/**
     * IMS network interface
     */
    private ImsNetworkInterface networkInterface;

    /**
	 * SIP stack
	 */
	private SipStack sipstack = null;

	/**
	 * The logger
	 */
	private Logger logger = Logger.getLogger(this.getClass().getName());
	
	/**
	 * Constructor
	 * 
     * @param parent IMS network interface
	 * @param outboundProxy Outbound proxy address
	 * @param defaultListeningPort Default listening port
	 */
	public SipManager(ImsNetworkInterface parent, String outboundProxy, int defaultListeningPort) {
		this.networkInterface = parent;
		this.outboundProxy = outboundProxy;
		this.defaultListeningPort = defaultListeningPort; 

		if (logger.isActivated()) {
			logger.info("SIP manager started");
		}
	}
	
	/**
	 * Returns the SIP stack
	 * 
	 * @return SIP stack
	 */
	public SipStack getSipStack() {
		return sipstack;
	}	

	/**
	 * Terminate the manager
	 */
	public void terminate() {
		if (logger.isActivated()) {
			logger.info("Terminate the SIP manager");
		}
		
		// Close the SIP stack
		if (sipstack != null) {
			closeStack();
		}
		
		if (logger.isActivated()) {
			logger.info("SIP manager has been terminated");
		}
	}
	
	/**
	 * Initialize the SIP stack
	 * 
	 * @param localAddr Local IP address
	 * @return SIP stack
	 * @throws SipException
	 */
	public void initStack(String localAddr) throws SipException {
		// Close the stack if necessary
		closeStack();
		
		// Set the listening port from default
		int listeningPort = NetworkRessourceManager.generateLocalUdpPort(defaultListeningPort);
		
		// Create the SIP stack
		sipstack = new SipStack(localAddr, outboundProxy, listeningPort);
	}	
	
	/**
	 * Close the SIP stack
	 */
	public void closeStack() {
		if (sipstack == null) {
			// Already closed
			return;
		}
		
		try {
			// Close the SIP stack
			sipstack.close();
			sipstack = null;
		} catch(Exception e) {
			if (logger.isActivated()) {
				logger.error("Can't close SIP stack properly", e);
			}
		}
	}

	/**
	 * Generate a unique call-ID
	 * 
	 * @return Call-Id
	 */
	public String generateCallId() {
		return IdGenerator.getIdentifier() + "@" + networkInterface.getNetworkAccess().getIpAddress();
	}
	
	/**
	 * Send a SIP message and wait a response
	 * 
	 * @param msg SIP message
	 * @return Transaction context
	 * @throws SipException
	 */
	public SipTransactionContext sendSipMessageAndWait(SipMessage msg) throws SipException {
		return sipstack.sendSipMessageAndWait(msg);
	}

	/**
	 * Send a SIP message
	 * 
	 * @param msg SIP message
	 * @throws SipException
	 */
	public void sendSipMessage(SipMessage msg) throws SipException {
		sipstack.sendSipMessage(msg);
	}
}
